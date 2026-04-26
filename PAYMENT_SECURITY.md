# Payment Security — GolfLC & Amazone Payments Inc

Architecture de securite entre l'application Java GolfLC (Jakarta EE / WildFly) et le serveur de paiement Python (Flask).

**Version:** 3.0
**Date:** 2026-03-21
**Audit:** Security audit 2026-03-09, 2026-03-19, architecture REST/JSF separation 2026-03-21

---

## Table des matieres

1. [Vue d'ensemble](#1-vue-densemble)
2. [Architecture REST/JSF separation](#2-architecture-restjsf-separation)
3. [HTTPS / TLS](#3-https--tls)
4. [Authentification HMAC-SHA256 (Java vers Python)](#4-authentification-hmac-sha256-java-vers-python)
5. [Nonce de transaction et signature des callbacks (Python vers Java)](#5-nonce-de-transaction-et-signature-des-callbacks-python-vers-java)
6. [PaymentStateStore — stockage server-side des transactions](#6-paymentstatestore--stockage-server-side-des-transactions)
7. [Chiffrement PAN au repos](#7-chiffrement-pan-au-repos)
8. [Semantique read-once du PAN](#8-semantique-read-once-du-pan)
9. [Validation du montant server-side](#9-validation-du-montant-server-side)
10. [Validation whitelist des redirections](#10-validation-whitelist-des-redirections)
11. [Cookies securises](#11-cookies-securises)
12. [Rate limiting](#12-rate-limiting)
13. [Isolation de session de paiement](#13-isolation-de-session-de-paiement)
14. [Flux complet](#14-flux-complet)
15. [Variables d'environnement](#15-variables-denvironnement)

---

## 1. Vue d'ensemble

Le flux de paiement repose sur un echange **machine-to-machine** entre deux serveurs :

| Composant | Technologie | Port |
|-----------|------------|------|
| GolfLC (application) | Jakarta EE 11, WildFly, Java 25 | 8080 / 8443 |
| Amazone Payments Inc | Flask, Python, Gunicorn | 5000 (HTTPS) |
| Session store | Redis / Memurai (Windows) | 6379 |
| Base de donnees | MySQL | 3308 |

**Principe fondamental :** aucune donnee sensible (PAN complet, CVV) ne transite en clair ni ne persiste au-dela du strict necessaire.

---

## 2. Architecture REST/JSF separation

### Probleme resolu (2026-03-21)

Avant la v3.0, `PaymentController` etait simultanement :
- `@Named("payC") @SessionScoped` — bean JSF
- `@Path("paymentController")` — resource JAX-RS

Les callbacks REST du serveur Python accedaient aux beans `@SessionScoped` (creditcard, appContext) via CDI. **Cela posait un probleme fondamental** : le CDI session context n'est pas garanti dans un contexte JAX-RS pur. Consequences :
- Perte de l'etat de session apres retour du serveur Python
- Menu non affiche sur l'ecran de retour (showMenu = false)
- `FacesContext.getCurrentInstance()` = null dans les endpoints REST

### Solution : separation en 3 couches

```
┌─────────────────────┐     ┌──────────────────────┐
│  PaymentController  │     │  PaymentRestResource  │
│  @Named("payC")     │     │  @Path("payment")     │
│  @SessionScoped     │     │  @RequestScoped        │
│                     │     │                        │
│  JSF actions only   │     │  REST callbacks only   │
│  - preparePayment() │     │  - paymentConfirmed()  │
│  - onComplete()     │     │  - paymentCanceled()   │
│  - progress bar     │     │  - handlePayments()    │
│  - onPaymentCompl() │     │  - paymentChoice()     │
└────────┬────────────┘     └───────────┬────────────┘
         │                              │
         │  store()                     │  get() / consume()
         ▼                              ▼
┌─────────────────────────────────────────────────┐
│          PaymentStateStore                       │
│          @ApplicationScoped                      │
│                                                  │
│  ConcurrentHashMap<String, PaymentTransaction>   │
│  cle = nonce (UUID)                              │
│                                                  │
│  + store(nonce, PaymentTransaction)              │
│  + get(nonce) → PaymentTransaction               │
│  + consume(nonce) → PaymentTransaction (one-use) │
│  + auto-expiration (TTL 30 min)                  │
└─────────────────────────────────────────────────┘
```

### Fichiers impliques

| Fichier | Role |
|---------|------|
| `Controller/refact/PaymentController.java` | JSF backing bean — store la transaction avant redirect vers Python, sync au retour via `onPaymentCompleted()` |
| `rest/PaymentRestResource.java` | `@RequestScoped` — recoit les callbacks Python, lookup par nonce, **zero dependance `@SessionScoped`** |
| `payment/PaymentStateStore.java` | `@ApplicationScoped` — ConcurrentHashMap thread-safe avec TTL 30 min |
| `payment/PaymentTransaction.java` | POJO — snapshot de toutes les donnees session (creditcard, playerId, type, round, club, etc.) |

### Avantages securite

| Probleme | Resolution |
|----------|-----------|
| Session CDI non garantie en JAX-RS | REST endpoint `@RequestScoped`, pas de dependance session |
| `FacesContext` = null en REST | Plus de code JSF dans le REST resource |
| Donnees payment perdues si session timeout | Donnees dans `PaymentStateStore` (`@ApplicationScoped`) avec TTL propre |
| Cookies `Amount`/`PaymentReference` falsifiables | Validation contre le `PaymentStateStore` server-side |
| Replay de callbacks | `consume()` — transaction utilisable une seule fois |

---

## 3. HTTPS / TLS

Toutes les communications entre Java et Python utilisent HTTPS avec un certificat CA custom.

### Principe

- Le client Java charge un certificat CA (`ca.pem`) dans un `SSLContext` custom
- Toutes les requetes HTTP vers le serveur Python transitent via TLS
- Le serveur Python est configure avec un certificat signe par cette meme CA

### Code Java — HttpController.java

```java
// Construction du HttpClient avec SSLContext custom
SSLContext sslContext = buildSSLContext(); // charge ca.pem
HttpClient.Builder builder = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_1_1)
    .connectTimeout(Duration.ofSeconds(50))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .cookieHandler(cookieManager);
if (sslContext != null) {
    builder.sslContext(sslContext);
}
httpClient = builder.build();
```

```java
// Requete POST vers le serveur de paiement — toujours HTTPS
HttpRequest request = HttpRequest.newBuilder()
    .POST(HttpRequest.BodyPublishers.ofString(strJson))
    .uri(URI.create("https://localhost:5000/creditcard"))
    // ...
    .build();
```

---

## 4. Authentification HMAC-SHA256 (Java vers Python)

### Principe

Chaque requete de paiement envoyee par Java au serveur Python est signee avec **HMAC-SHA256**. La signature est calculee sur `timestamp + body` avec un secret partage. Le serveur Python verifie :

1. La **fraicheur** du timestamp (ecart maximum : 5 minutes)
2. La **validite** de la signature HMAC
3. Utilise `hmac.compare_digest()` pour prevenir les attaques par timing

### Code Java — HttpController.java

```java
// Generation de la signature HMAC-SHA256
String timestamp = String.valueOf(Instant.now().getEpochSecond());
String payload = timestamp + strJson;
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(
    System.getenv("PAYMENT_HMAC_SECRET").getBytes(StandardCharsets.UTF_8),
    "HmacSHA256"));
String signature = HexFormat.of().formatHex(
    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

// Envoi dans les headers HTTP
HttpRequest request = HttpRequest.newBuilder()
    .header("X-Timestamp", timestamp)
    .header("X-Signature", signature)
    // ...
    .build();
```

### Code Python — auth.py

```python
HMAC_MAX_AGE_SECONDS = 300  # 5 minutes

def verify_hmac(secret, timestamp, body, signature):
    """Verify HMAC-SHA256 signature against timestamp + body."""
    payload = timestamp + body
    expected = hmac.new(
        secret.encode("utf-8"),
        payload.encode("utf-8"),
        hashlib.sha256
    ).hexdigest()
    return hmac.compare_digest(expected, signature)  # anti timing-attack

def require_hmac(f):
    """Decorator that enforces HMAC-SHA256 authentication on a Flask route."""
    @functools.wraps(f)
    def decorated(*args, **kwargs):
        secret = os.environ.get("PAYMENT_HMAC_SECRET")
        signature = request.headers.get("X-Signature")
        timestamp = request.headers.get("X-Timestamp")

        if not signature or not timestamp:
            return jsonify({"message": "Unauthorized"}), 401

        # Check timestamp freshness
        ts = int(timestamp)
        now = int(time.time())
        if abs(now - ts) > HMAC_MAX_AGE_SECONDS:
            return jsonify({"message": "Unauthorized"}), 401

        # Verify HMAC signature
        body = request.get_data(as_text=True)
        if not verify_hmac(secret, timestamp, body, signature):
            return jsonify({"message": "Unauthorized"}), 401

        return f(*args, **kwargs)
    return decorated
```

### Code Python — payment.py (utilisation)

```python
@pagesCC.route("/creditcard", methods=["GET", "POST"])
@limiter.limit("5 per minute")
@require_hmac     # <-- decoration : verifie HMAC avant d'entrer dans la fonction
def creditcard_fun():
    # ... traitement du paiement
```

---

## 5. Nonce de transaction et signature des callbacks (Python vers Java)

### Principe

Quand le serveur Python redirige le navigateur vers les endpoints REST de Java (callbacks), un attaquant pourrait forger ces URLs. Deux mesures combinees empechent cela :

- **Nonce unique par transaction (P2)** : Java genere un UUID unique a chaque paiement et l'envoie au serveur Python. Python le stocke dans Redis. A chaque callback, le nonce est inclus dans l'URL et Java verifie qu'il correspond a celui stocke dans le `PaymentStateStore`.

- **Signature HMAC des callbacks (P1)** : Python signe chaque redirect avec `HMAC-SHA256(nonce + phase)`. Java verifie cette signature avant de traiter le callback.

### Code Java — HttpController.java (generation du nonce)

```java
// Generation d'un nonce unique par transaction
String nonce = java.util.UUID.randomUUID().toString().replace("-", "");
creditc.setPaymentNonce(nonce);  // stocke dans l'objet creditcard

HttpRequest request = HttpRequest.newBuilder()
    .header("X-Payment-Nonce", nonce)  // envoye au serveur Python
    // ...
    .build();
```

### Code Java — PaymentController.java (stockage dans PaymentStateStore)

```java
// Apres sendPaymentServer, stocker le snapshot de transaction
String nonce = creditcard.getPaymentNonce();
PaymentTransaction tx = new PaymentTransaction(nonce);
tx.setCreditcard(creditcard);
tx.setPlayerId(appContext.getPlayer().getIdplayer());
tx.setSavedType(savedType);
tx.setCreditcardType(appContext.getCreditcardType());
tx.setSubscription(appContext.getSubscription());
// ... autres donnees de contexte
paymentStateStore.store(nonce, tx);
```

### Code Python — payment_session.py (stockage du nonce)

```python
def store_nonce(self, nonce):
    """Store payment nonce from Java."""
    self.client.set(self._key(self._KEY_NONCE), nonce, ex=600)  # TTL 10 min

def get_nonce(self) -> str | None:
    """Return the payment nonce."""
    return self.client.get(self._key(self._KEY_NONCE))
```

### Code Python — auth.py (signature du callback)

```python
def sign_callback(nonce: str, phase: str) -> str:
    """Sign a callback redirect URL with HMAC-SHA256(nonce + phase)."""
    secret = os.environ.get("PAYMENT_HMAC_SECRET", "")
    payload = nonce + phase
    return hmac.new(
        secret.encode("utf-8"),
        payload.encode("utf-8"),
        hashlib.sha256
    ).hexdigest()
```

### Code Python — payment.py (redirect signe)

```python
# Chaque redirect vers Java inclut nonce + signature
_nonce = payment_service.get_nonce() or ""
_phase = "phase3"
_sig = sign_callback(_nonce, _phase)
response = make_response(redirect(
    payment_service.get_return_directory()
    + "payment_choice/" + _phase
    + f"?nonce={_nonce}&sig={_sig}",
    code=302))
```

### Code Java — PaymentRestResource.java (verification du callback)

```java
private boolean verifyCallbackSignature(PaymentTransaction tx,
        String nonce, String sig, String phase) {
    // P2: verifier que le nonce correspond a celui de la transaction
    String expectedNonce = tx.getCreditcard().getPaymentNonce();
    if (expectedNonce == null || !expectedNonce.equals(nonce)) {
        LOG.warn("SECURITY: nonce mismatch!");
        return false;
    }

    // P1: verifier la signature HMAC
    String secret = System.getenv("PAYMENT_HMAC_SECRET");
    String payload = nonce + phase;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(
        secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String expected = HexFormat.of().formatHex(
        mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    return expected.equals(sig);
}
```

```java
// Utilisation dans le callback REST (@RequestScoped — pas de @SessionScoped)
@GET @Path("payment_choice/{isbn}")
public Response paymentChoice(
        @PathParam("isbn") String param,
        @QueryParam("nonce") String nonce,
        @QueryParam("sig") String sig,
        // ...
) {
    // Lookup transaction par nonce dans PaymentStateStore
    PaymentTransaction tx = paymentStateStore.get(nonce);
    if (tx == null) {
        return Response.status(403).entity("Unknown transaction").build();
    }
    // Verification de la signature
    if (!verifyCallbackSignature(tx, nonce, sig, param)) {
        return Response.status(403).entity("Invalid callback signature").build();
    }
    // ... traitement normal — aucun acces a @SessionScoped
}
```

---

## 6. PaymentStateStore — stockage server-side des transactions

### Principe

Le `PaymentStateStore` est un bean `@ApplicationScoped` qui stocke les transactions de paiement en cours dans un `ConcurrentHashMap`. Il sert de pont entre le contexte JSF (session) et le contexte REST (stateless).

### Garanties de securite

| Garantie | Mecanisme |
|----------|-----------|
| **Thread-safety** | `ConcurrentHashMap` |
| **One-time use** | `consume()` marque la transaction comme completee |
| **Anti-replay** | `consume()` retourne null si deja consommee |
| **TTL** | Eviction automatique apres 30 minutes |
| **Isolation** | Cle = nonce UUID, unique par transaction |

### Code Java — PaymentStateStore.java

```java
@ApplicationScoped
public class PaymentStateStore implements Serializable {

    private static final long TTL_MILLIS = 30 * 60 * 1000L;  // 30 minutes
    private final ConcurrentHashMap<String, PaymentTransaction> store = new ConcurrentHashMap<>();

    public void store(String nonce, PaymentTransaction tx) {
        cleanup();  // eviction lazy des transactions expirees
        store.put(nonce, tx);
    }

    public PaymentTransaction get(String nonce) {
        PaymentTransaction tx = store.get(nonce);
        if (tx == null || tx.isExpired(TTL_MILLIS)) return null;
        return tx;
    }

    public PaymentTransaction consume(String nonce) {
        PaymentTransaction tx = get(nonce);
        if (tx == null || tx.isCompleted()) return null;  // anti-replay
        tx.setCompleted(true);
        return tx;
    }

    private void cleanup() {
        store.entrySet().removeIf(e -> e.getValue().isExpired(TTL_MILLIS));
    }
}
```

---

## 7. Chiffrement PAN au repos

### Principe

Le numero de carte (PAN) est chiffre avec **Fernet (AES-128-CBC + HMAC-SHA256)** avant d'etre stocke dans Redis. La cle de chiffrement est derivee de `SECRET_KEY` via SHA-256.

### Code Python — crypto.py

```python
# Derivation de la cle Fernet depuis SECRET_KEY
_secret = os.environ.get("SECRET_KEY", "")
_derived = hashlib.sha256(_secret.encode("utf-8")).digest()
_fernet_key = base64.urlsafe_b64encode(_derived)
_fernet = Fernet(_fernet_key)

def encrypt_pan(pan: str) -> str:
    """Encrypt a PAN string. Returns a base64-encoded ciphertext."""
    return _fernet.encrypt(pan.encode("utf-8")).decode("utf-8")

def decrypt_pan(token: str) -> str:
    """Decrypt a PAN token back to the original string."""
    return _fernet.decrypt(token.encode("utf-8")).decode("utf-8")
```

### Code Python — payment_session.py (stockage chiffre)

```python
# PAN chiffre dans Redis avec TTL 10 minutes
self.client.set(
    self._key(self._KEY_NUMBER),
    encrypt_pan(str(card_number)),
    ex=600   # expire apres 10 minutes
)
```

---

## 8. Semantique read-once du PAN

### Principe

Le PAN complet n'est lu qu'une seule fois depuis Redis. Immediatement apres lecture, la cle est **supprimee**. Cela garantit qu'un PAN ne peut pas etre recupere deux fois, meme si Redis est compromis apres la transaction.

### Code Python — payment_session.py

```python
def get_number(self) -> str | None:
    """Return full PAN (decrypted) and DELETE from Redis immediately."""
    key = self._key(self._KEY_NUMBER)
    encrypted = self.client.get(key)
    if encrypted:
        self.client.delete(key)   # <-- suppression immediate
        return decrypt_pan(encrypted)
    return None
```

---

## 9. Validation du montant server-side

### Principe

Le montant affiche dans les cookies est **jamais utilise pour le traitement**. Le `PaymentRestResource` compare le montant du cookie avec la valeur server-side stockee dans le `PaymentStateStore` (via `PaymentTransaction.getCreditcard().getTotalPrice()`). Si l'ecart depasse 0.01 EUR, le paiement est rejete.

### Code Java — PaymentRestResource.java

```java
// Validation du montant — PaymentStateStore server-side (pas de @SessionScoped)
PaymentTransaction tx = paymentStateStore.get(nonce);
Creditcard cc = tx.getCreditcard();

double cookieAmount = Double.parseDouble(amount);    // cookie (non fiable)
double serverAmount = cc.getTotalPrice();             // PaymentStateStore (fiable)

if (Math.abs(cookieAmount - serverAmount) > 0.01) {
    LOG.warn("SECURITY: payment amount mismatch! Server=" + serverAmount
        + " Cookie=" + cookieAmount);
    cc.setPaymentOK(false);
    cc.setCommunication("Payment rejected: amount tampered");
    response.sendRedirect("creditcard_payment_canceled.xhtml?nonce=" + nonce
        + "&message=Amount+mismatch");
    return Response.status(Response.Status.FORBIDDEN)
        .entity("Payment amount mismatch").build();
}
```

---

## 10. Validation whitelist des redirections

### Principe

L'URL de retour (`ReturnDirectory`) envoyee par Java est validee contre une **whitelist de domaines autorises** avant d'etre stockee. Cela empeche les attaques de type open redirect.

### Code Python — payment_session.py

```python
ALLOWED_REDIRECT_HOSTS = {"localhost", "127.0.0.1"}

def validate_redirect_url(url: str) -> str:
    """Validate that a redirect URL points to an allowed host."""
    parsed = urlparse(url)
    if not parsed.scheme or not parsed.hostname:
        raise UnsafeRedirectError(f"Invalid redirect URL: {url}")
    if parsed.scheme not in ("http", "https"):
        raise UnsafeRedirectError(f"Disallowed scheme: {parsed.scheme}")
    if parsed.hostname not in ALLOWED_REDIRECT_HOSTS:
        raise UnsafeRedirectError(
            f"Host '{parsed.hostname}' not in allowed list")
    return url
```

```python
# Appele lors du stockage de la session de paiement
def store_payment_session(self, card_number, amount, ...):
    validate_redirect_url(return_directory)  # <-- rejet si domaine non autorise
    # ... stockage dans Redis
```

---

## 11. Cookies securises

### Principe

Tous les cookies echanges entre le serveur de paiement et le navigateur utilisent les attributs de securite maximaux.

### Code Python — payment.py

```python
response.set_cookie('Amount', str(amount),
    secure=True,          # HTTPS uniquement
    httponly=True,         # inaccessible depuis JavaScript
    samesite="Lax")       # protection CSRF

response.set_cookie('PaymentReference', uuid,
    secure=True, httponly=True, samesite="Lax")
```

### Code Java — web.xml (session cookie)

```xml
<cookie-config>
    <http-only>true</http-only>
    <secure>false</secure>   <!-- true en production -->
    <attribute>
        <attribute-name>SameSite</attribute-name>
        <attribute-value>Lax</attribute-value>
    </attribute>
</cookie-config>
```

---

## 12. Rate limiting

### Principe

Les endpoints de paiement sont proteges par un rate limiter pour prevenir les attaques par force brute et le spam de transactions.

### Code Python — payment.py

```python
@pagesCC.route("/creditcard", methods=["GET", "POST"])
@limiter.limit("5 per minute")        # 5 tentatives de paiement par minute max
@require_hmac
def creditcard_fun():
    # ...

@pagesCC.route("/payment_ok", methods=["GET", "POST"])
@limiter.limit("10 per minute")       # 10 requetes par minute
def payment_ok():
    # ...
```

### Code Java — RateLimitFilter (web.xml)

```xml
<filter-mapping>
    <filter-name>RateLimitFilter</filter-name>
    <url-pattern>*.xhtml</url-pattern>
    <url-pattern>/rest/*</url-pattern>    <!-- inclut les callbacks payment -->
</filter-mapping>
```

Limites Java : 10 req/min pour `/login.xhtml`, 60 req/min pour `/rest/*`, 120 req/min par defaut.

---

## 13. Isolation de session de paiement

### Principe

Chaque transaction de paiement recoit un **identifiant de session unique** (12 caracteres hex). Toutes les donnees dans Redis sont prefixees par cet identifiant, empechant les collisions entre utilisateurs concurrents.

### Code Python — payment_session.py

```python
def new_session(self) -> str:
    """Create a new payment session with a unique ID."""
    self._session_id = uuid.uuid4().hex[:12]
    return self._session_id

def _key(self, suffix: str) -> str:
    """Build a namespaced Redis key: ps:<session_id>:<suffix>."""
    return f"ps:{self._session_id}:{suffix}"
```

Exemple de cles Redis pour une transaction :
```
ps:a1b2c3d4e5f6:number          (PAN chiffre, TTL 10 min)
ps:a1b2c3d4e5f6:number_masked   (****1234)
ps:a1b2c3d4e5f6:amount          (150.00)
ps:a1b2c3d4e5f6:communication   (Subscription 2026)
ps:a1b2c3d4e5f6:ReturnDirectory (https://localhost:8443/rest/payment/)
ps:a1b2c3d4e5f6:nonce           (token unique de transaction)
ps:a1b2c3d4e5f6:uuid            (reference de transaction)
```

---

## 14. Flux complet

```
UTILISATEUR          JAVA (WildFly)                     PYTHON (Flask)              REDIS
    |                     |                                   |                       |
    |-- saisit carte ---->|                                   |                       |
    |                     |                                   |                       |
    |                     |-- POST /creditcard -------------->|                       |
    |                     |   Headers:                        |                       |
    |                     |     X-Timestamp (Unix epoch)      |                       |
    |                     |     X-Signature (HMAC-SHA256)     |                       |
    |                     |     X-Payment-Nonce (UUID)        |                       |
    |                     |     ReturnDirectory               |                       |
    |                     |   Body: JSON creditcard           |                       |
    |                     |                                   |                       |
    |                     |                                   |-- verify HMAC ------->|
    |                     |                                   |-- validate schema     |
    |                     |                                   |-- validate card (Luhn)|
    |                     |                                   |-- encrypt PAN ------->| SET (TTL 10m)
    |                     |                                   |-- store nonce ------->| SET (TTL 10m)
    |                     |                                   |                       |
    |                     |<-- 200 OK -------------------------|                       |
    |                     |                                   |                       |
    |                     |-- store PaymentTransaction ------->|                       |
    |                     |   in PaymentStateStore (nonce key) |                       |
    |                     |                                   |                       |
    |<-- redirect to Python /about ---|                        |                       |
    |                     |                                   |                       |
    |-- "Start Payment" ---------------------------------->---|                       |
    |                     |                                   |-- read PAN (delete)-->| GET+DEL
    |                     |                                   |-- create transaction  |
    |                     |                                   |-- store UUID -------->| SET
    |                     |                                   |-- get nonce --------->| GET
    |                     |                                   |-- sign_callback()     |
    |                     |                                   |                       |
    |<-- 302 redirect ----|<-- ?nonce=xxx&sig=HMAC(nonce+phase3) ---|                 |
    |                     |                                   |                       |
    |-- GET /rest/payment/payment_choice/phase3?nonce=xxx&sig=yyy -->|               |
    |                     |                                   |                       |
    |                     |-- PaymentRestResource             |                       |
    |                     |   (@RequestScoped, NOT @SessionScoped)                    |
    |                     |                                   |                       |
    |                     |-- paymentStateStore.get(nonce)     |                       |
    |                     |-- verifyCallbackSignature(tx, nonce, sig, phase)           |
    |                     |                                   |                       |
    |                     |-- 302 to /rest/payment/payment_handle/101?nonce=xxx       |
    |                     |                                   |                       |
    |                     |-- paymentStateStore.consume(nonce)  |                      |
    |                     |-- validate amount (PaymentTransaction vs cookie)           |
    |                     |-- PaymentOrchestrator.handle()     |                       |
    |                     |-- save to MySQL                   |                       |
    |                     |                                   |                       |
    |                     |-- 302 to Python /payment_generator |                       |
    |                     |                                   |                       |
    |<-- 302 to /rest/payment/payment_confirmed?nonce=xxx -----|                       |
    |                     |                                   |                       |
    |                     |-- paymentStateStore.get(nonce)     |                       |
    |                     |-- validate amount (server vs cookie)                       |
    |                     |                                   |                       |
    |<-- redirect to creditcard_payment_executed.xhtml?nonce=xxx                      |
    |                     |                                   |                       |
    |                     |-- JSF FacesServlet processes page  |                       |
    |                     |-- f:viewParam nonce -> payC.completionNonce                |
    |                     |-- preRenderView -> payC.onPaymentCompleted()               |
    |                     |     -> paymentStateStore.get(nonce)                        |
    |                     |     -> sync creditcard to JSF session                      |
    |                     |                                   |                       |
    |<-- page rendered with correct session (menu visible) ---|                       |
```

---

## 15. Variables d'environnement

| Variable | Ou | Description |
|----------|-----|-------------|
| `PAYMENT_HMAC_SECRET` | Java + Python | Secret partage pour HMAC-SHA256. **Doit etre identique des deux cotes.** |
| `PAYMENT_SERVICE_URL` | Java | URL du serveur Python (ex: `https://localhost:5000`) |
| `SECRET_KEY` | Python | Cle de chiffrement Fernet pour le PAN |
| `REDIS_PASSWORD` | Python | Mot de passe Redis/Memurai |
| `MYSQL_USERNAME` | Java + Python | Utilisateur MySQL |
| `MYSQL_PASSWORD` | Java + Python | Mot de passe MySQL |

---

## Resume des mesures de securite

| # | Mesure | Direction | Protection contre |
|---|--------|-----------|-------------------|
| 1 | Architecture REST/JSF separee | Java interne | Perte de session, etat incoherent |
| 2 | HTTPS/TLS avec CA custom | Java <-> Python | Interception reseau (MITM) |
| 3 | HMAC-SHA256 + timestamp | Java -> Python | Requetes forgees, replay attacks |
| 4 | Nonce unique par transaction | Java -> Python -> Java | Rejeu de callbacks |
| 5 | Signature HMAC des callbacks | Python -> Java | Callbacks forges |
| 6 | PaymentStateStore server-side | Java interne | Perte de donnees session, falsification |
| 7 | consume() one-time-use | Java interne | Replay de transactions |
| 8 | Chiffrement AES du PAN | Python -> Redis | Vol de donnees au repos |
| 9 | Read-once du PAN | Redis | Acces ulterieur au PAN |
| 10 | Validation montant server-side | Java | Falsification du montant |
| 11 | Whitelist des redirections | Python | Open redirect |
| 12 | Cookies securises | Python -> navigateur | Vol de cookies, CSRF |
| 13 | Rate limiting | Java + Python | Brute force, DDoS |
| 14 | Isolation de session Redis | Python -> Redis | Collision entre utilisateurs |
