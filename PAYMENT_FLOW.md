# GolfLC — Documentation complète du flux de paiement

**Version :** 3.0 (architecture REST/JSF séparée, HMAC AWS Signature v4)
**Dernière mise à jour :** 2026-03-28

---

## Vue d'ensemble

Le flux de paiement implique **trois processus** qui communiquent via HTTP :

```
Browser (JSF)  ←→  WildFly (Jakarta EE)  ←→  Python Flask (port 5000)
```

Le joueur reste sur une page JSF pendant toute la saisie de sa carte. Au moment de valider,
WildFly envoie les données de paiement au serveur Python via une requête HTTPS authentifiée par
HMAC. Le serveur Python gère l'interface de paiement et redirige vers des callbacks REST JAX-RS
sur WildFly pour finaliser la transaction.

---

## Architecture des composants

| Composant | Classe | Scope | Rôle |
|---|---|---|---|
| `payC` | `PaymentController` | `@SessionScoped` | Entrée JSF, state UI, initiation du paiement |
| `httpC` | `HttpController` | `@SessionScoped` | Client HTTP vers Python, signature HMAC |
| `PaymentStateStore` | `PaymentStateStore` | `@ApplicationScoped` | Bridge JSF↔REST, stockage de la transaction |
| `PaymentRestResource` | `PaymentRestResource` | `@RequestScoped` | Endpoints REST pour les callbacks Python |
| `PaymentOrchestrator` | `PaymentOrchestrator` | POJO (instancié) | Dispatch vers le bon registrar selon le type |
| Python Flask | `creditCardService.py` | Process | UI de paiement, génération PDF, redirection |
| Redis | Memurai (Windows) | Service | Anti-replay HMAC (nonce + request_id) |

---

## Flux complet étape par étape

### Étape 1 — Entrée JSF : choix du type de paiement

Trois types de paiement existent, chacun avec sa méthode d'entrée dans `PaymentController` :

#### 1a. Cotisation (`manageCotisation()`)
- Appelée depuis `cotisation.xhtml`
- Complète la `Cotisation` via `tarifMemberController.completeCotisation()`
- Vérifie que le montant est non nul et sans erreur
- Détermine le type : `"spontaneous"` (PaymentCotisationSpontaneous) ou `"round"` (inscription à un round)
- Appelle `completeWithCotisation(cotisation, player)` pour remplir le `Creditcard`
- Redirige vers `creditcard.xhtml`

#### 1b. Greenfee (`manageGreenfee()`)
- Appelée depuis `price_round_greenfee.xhtml`
- Complète le `Greenfee` via `tarifGreenfeeController.completeGreenfee()`
- Vérifie que le montant est non nul
- Appelle `completeWithGreenfee(greenfee, player)` pour remplir le `Creditcard`
- Redirige vers `creditcard.xhtml`

#### 1c. Souscription (`manageSubscription()`)
- Appelée depuis `subscription.xhtml`
- Switch sur `subCode` :
  - `"TRIAL"` → inscription directe sans paiement (retour `welcome.xhtml`)
  - `"MONTHLY"` / `"YEARLY"` → complète la souscription et redirige vers `creditcard.xhtml`

---

### Étape 2 — Saisie de la carte : `creditcard.xhtml`

Le joueur voit un formulaire JSF avec les champs de sa carte (numéro, expiration, CVV, etc.)
gérés par `payC.creditcard` (objet `Creditcard` en session).

Le joueur valide → bouton qui appelle `payC.onCompletePayment()`.

---

### Étape 3 — `onCompletePayment()` : envoi au serveur Python

```
PaymentController.onCompletePayment()
  ├── creditcard.setTypePayment(appContext.getCreditcardType())
  ├── httpController.sendPaymentServer(creditcard)   [HTTP POST vers Python]
  │     └── retourne "200" si succès
  ├── Crée PaymentTransaction (snapshot de session)
  ├── paymentStateStore.store(nonce, tx)             [stockage bridge JSF↔REST]
  └── redirect → https://localhost:5000/about        [Python UI de paiement]
```

**Si le retour n'est pas "200" :** redirection vers `creditcard_payment_canceled.xhtml`.

---

### Étape 4 — `HttpController.sendPaymentServer()` : construction de la requête HMAC

```java
URI uri = UrlBuilder.empty()
    .withScheme("https").withHost("localhost").withPort(5000)
    .withPath("creditcard/")
    .addParameter("return_url", firstPartUrl() + "/rest/payment/")  // migrated 2026-03-28
    .toUri();

String nonce     = UUID.randomUUID().toString().replace("-", "");  // 32 hex chars
String requestId = UUID.randomUUID().toString();                   // UUID standard avec tirets
```

#### Construction du payload HMAC (AWS Signature v4 — migrated 2026-03-28)

```
Canonical string = method + "\n"
                 + path + "\n"
                 + query (URL-encoded) + "\n"
                 + timestamp (epoch secondes) + "\n"
                 + nonce + "\n"
                 + requestId + "\n"
                 + SHA-256(body en UTF-8)
```

```java
String bodyHash = HexFormat.of().formatHex(
    MessageDigest.getInstance("SHA-256").digest(strJson.getBytes(UTF_8))
);
String payload = String.join("\n", method, path, query, timestamp, nonce, requestId, bodyHash);
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(System.getenv("PAYMENT_HMAC_SECRET").getBytes(UTF_8), "HmacSHA256"));
String signature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(UTF_8)));
```

#### Headers HTTP envoyés à Python

| Header | Valeur |
|---|---|
| `Content-Type` | `application/json` |
| `X-Timestamp` | Epoch secondes (String) |
| `X-Signature` | HMAC-SHA256 hex |
| `X-Payment-Nonce` | 32 hex chars UUID sans tirets |
| `X-Request-ID` | UUID standard avec tirets |
| `MerchantSite` | `"GolfLC Merchant Site"` |

#### Query param dans l'URI

| Paramètre | Valeur |
|---|---|
| `return_url` | `https://<host>:<port>/<contextPath>/rest/payment/` |

#### SSL

`HttpController.buildSSLContext()` charge `~/.ssl/creditcard/ca.pem` comme CA de confiance
(évite de modifier le cacerts JDK à chaque mise à jour).

---

### Étape 5 — Python Flask : vérification HMAC et UI de paiement

#### `auth.py` — Décorateur `@require_hmac`

Chaque endpoint Python protégé vérifie :

1. **Headers présents :** `X-Signature`, `X-Timestamp`, `X-Request-ID`, `X-Payment-Nonce`
2. **`X-Request-ID` unique (anti-replay) :** vérification Redis namespace `hmac_req:{request_id}`
3. **`X-Payment-Nonce` unique (anti-replay) :** vérification Redis namespace `hmac_nonce:{nonce}`
4. **Fraîcheur du timestamp :** `|now - ts| <= 30 secondes`
5. **Signature HMAC :** recalcul du même canonical string et `hmac.compare_digest()`

```python
body_hash = hashlib.sha256(body.encode("utf-8")).hexdigest()
payload   = "\n".join([method, path, query, timestamp, nonce, request_id or "", body_hash])
expected  = hmac.new(secret.encode("utf-8"), payload.encode("utf-8"), hashlib.sha256).hexdigest()
return hmac.compare_digest(expected, signature)
```

**Fail-closed :** Si Redis est indisponible, la requête est rejetée (pas de dégradation silencieuse).

#### `payment.py` — Endpoint `/creditcard/`

1. Vérifie HMAC (via `@require_hmac`)
2. Lit `return_url` depuis `request.args.get('return_url')`  ← migrated 2026-03-28
3. Valide que `return_url` est une cible de redirection autorisée (protection open-redirect)
4. Stocke les données de paiement (montant, référence, devise) dans des cookies
5. Affiche l'interface de paiement au joueur (page Python)

#### Réponses Python selon le choix du joueur

| Action joueur | Python appelle | Phase |
|---|---|---|
| Annule | `return_url + "payment_choice/phase1?nonce=..."` | phase1 |
| Confirme (phase intermédiaire) | `return_url + "payment_choice/phase2?nonce=..."` | phase2 |
| Complète le paiement | `return_url + "payment_choice/phase3?nonce=..."` | phase3 |

Python signe chaque callback : `sig = HMAC-SHA256(nonce + phase)` et l'ajoute comme `?sig=`.

---

### Étape 6 — Callbacks REST Java : `PaymentRestResource`

Endpoint racine : `GET /rest/payment/payment_choice/{phase}`

#### Vérification de la signature du callback (`verifyCallbackSignature`)

Avant tout traitement, Java vérifie :
1. Le `nonce` est présent dans `PaymentStateStore` (transaction connue)
2. Le nonce reçu correspond au nonce stocké dans la transaction
3. `HMAC-SHA256(nonce + phase)` correspond au `sig` reçu

#### Routage selon la phase

| Phase | Redirection vers |
|---|---|
| `phase1` (annulation) | `GET /rest/payment/payment_canceled/101?nonce=` |
| `phase2` (confirmation) | `GET /rest/payment/payment_confirmed?nonce=` |
| `phase3` (exécution) | `GET /rest/payment/payment_handle/101?nonce=` |

---

### Étape 6a — Phase 1 : Annulation (`paymentCancel`)

```
GET /rest/payment/payment_canceled/101?nonce=...
  ├── paymentStateStore.get(nonce)          [non-consommé — peut être retentée]
  ├── tx.getCreditcard().setCreditcardPaymentReference(null)
  ├── tx.getCreditcard().setCommunication("Payment refused by User Client")
  ├── tx.setCanceled(true)
  └── sendRedirect → /creditcard_payment_canceled.xhtml?nonce=...
```

---

### Étape 6b — Phase 2 : Confirmation (`paymentConfirmed`)

```
GET /rest/payment/payment_confirmed?nonce=...
  ├── paymentStateStore.get(nonce)          [non-consommé]
  ├── Validation montant : |cookieAmount - cc.getTotalPrice()| <= 0.01
  │     si différent → reject, redirect canceled + message "Amount mismatch"
  ├── cc.setCreditcardPaymentReference(reference)
  └── sendRedirect → /creditcard_payment_executed.xhtml?nonce=...
```

**Sécurité montant :** le montant du cookie (fourni par Python) est comparé au montant
stocké côté serveur dans `PaymentTransaction`. Tout écart > 0.01 est rejeté.

---

### Étape 6c — Phase 3 : Exécution (`handlePayments`)

```
GET /rest/payment/payment_handle/101?nonce=...
  ├── paymentStateStore.consume(nonce)      [CONSOMME — one-time use]
  │     → null si déjà consommé (protection replay)
  ├── Validation montant cookie vs serveur (warning log si écart)
  ├── cc.setCreditcardPaymentReference(reference)
  ├── cc.setTypePayment(tx.getSavedType())
  ├── paymentManager.needsUpdate(cc, player) → créer/modifier en DB
  ├── cc.setPaymentOK(true)
  ├── switch(typePayment):
  │     "SUBSCRIPTION" → SubscriptionPayment(tx.getSubscription())
  │     "COTISATION"   → CotisationPayment(tx.getCotisation())
  │     "GREENFEE"     → GreenfeePayment(tx.getGreenfee())
  ├── new PaymentOrchestrator(...).handle(target)
  │     └── CotisationRegistrar / SubscriptionRegistrar / GreenfeeRegistrar
  └── redirect → https://localhost:5000/payment_generator  [reçu PDF]
```

---

### Étape 7 — `PaymentOrchestrator` : enregistrement métier

`PaymentOrchestrator` est un POJO instancié dans `handlePayments`. Il reçoit tous les
paramètres de la transaction (`cc`, `player`, `round`, `club`, `course`, `inscription`)
et délègue selon le type de `PaymentTarget` :

| PaymentTarget | Registrar | Action |
|---|---|---|
| `SubscriptionPayment` | `SubscriptionRegistrar` | Crée la souscription en DB |
| `CotisationPayment` | `CotisationRegistrar` | Enregistre la cotisation |
| `GreenfeePayment` | `GreenfeeRegistrar` | Enregistre le greenfee + inscription |

---

### Étape 8 — Pages JSF de résultat

| Page | Déclencheur | Contenu |
|---|---|---|
| `creditcard_payment_executed.xhtml` | Redirect depuis `paymentConfirmed` | Confirmation de paiement + bouton mail |
| `creditcard_payment_canceled.xhtml` | Redirect depuis `paymentCancel` | Message d'annulation |

Sur `creditcard_payment_executed.xhtml`, le joueur peut déclencher `payC.creditCardMail()`
qui envoie un email de confirmation adapté au type (GREENFEE / SUBSCRIPTION / COTISATION).

---

## `PaymentStateStore` — Bridge JSF ↔ REST

```
@ApplicationScoped — singleton partagé toute l'application
ConcurrentHashMap<nonce, PaymentTransaction>
TTL = 30 minutes (vérifié à chaque accès)
Lazy cleanup : à chaque store(), les entrées expirées sont supprimées
```

### Méthodes

| Méthode | Comportement |
|---|---|
| `store(nonce, tx)` | Stocke + nettoyage des expirés |
| `get(nonce)` | Lit sans consommer (utilisé pour phase1/phase2 et vérifications) |
| `consume(nonce)` | Lit + marque `completed=true` — retourne `null` si déjà consommé |
| `remove(nonce)` | Suppression explicite |

**`consume()` est uniquement appelé par `handlePayments()` (phase3).** Les autres endpoints
utilisent `get()`, ce qui permet de retenter une phase1/phase2 sans invalider la transaction.

---

## `PaymentTransaction` — Snapshot de la session JSF

```java
public class PaymentTransaction {
    private final String nonce;
    private final long createdAt;           // pour TTL
    private Creditcard creditcard;
    private int playerId;
    private String savedType;               // "SUBSCRIPTION" | "COTISATION" | "GREENFEE"
    private String creditcardType;
    private Subscription subscription;
    private Cotisation cotisation;
    private Greenfee greenfee;
    private Round round;
    private Club club;
    private Course course;
    private Inscription inscription;
    private boolean completed;              // one-time use (consommé par handlePayments)
    private boolean canceled;
}
```

---

## Mécanismes de sécurité

### 1. HMAC-SHA256 (Java → Python)

| Propriété | Valeur |
|---|---|
| Algorithme | HMAC-SHA256 |
| Secret partagé | Variable d'env `PAYMENT_HMAC_SECRET` |
| Canonicalisation | `method\npath\nquery\ntimestamp\nnonce\nrequest_id\nSHA256(body)` |
| Séparateur | `\n` (évite ambiguïté avec contenu JSON) |
| Body | SHA-256 hexdigest (pas le body brut — PCI-friendly, taille fixe) |
| Expiration | 30 secondes sur le timestamp |

### 2. Anti-replay HMAC (Redis — Python)

Deux namespaces indépendants dans Redis :

| Namespace | Clé | TTL |
|---|---|---|
| Request ID | `hmac_req:{request_id}` | 60 secondes (30s × 2) |
| Nonce | `hmac_nonce:{nonce}` | 60 secondes |

Fail-closed : si Redis est indisponible, la requête est rejetée (401 ou 409).

### 3. Anti-replay callback (PaymentStateStore — Java)

- `consume()` marque la transaction `completed=true` → les appels suivants retournent `null`
- Protège `handlePayments()` (phase3) contre les doubles exécutions

### 4. Vérification de signature des callbacks (Python → Java)

Python signe chaque callback : `HMAC-SHA256(nonce + phase)`

Java vérifie dans `verifyCallbackSignature()` :
- Le nonce est connu dans `PaymentStateStore`
- Le nonce reçu correspond au nonce stocké dans la transaction
- La signature HMAC est valide

### 5. Validation du montant (Java)

Deux points de contrôle :
- `paymentConfirmed()` : rejet immédiat si écart > 0.01 entre cookie et serveur
- `handlePayments()` : warning log si écart (double vérification)

Le montant **autoritaire** est toujours celui stocké dans `PaymentTransaction.creditcard.totalPrice`
(fixé avant l'envoi à Python — le joueur ne peut pas le modifier côté Python).

### 6. Protection open-redirect (Python)

`return_url` est validé contre une liste de cibles autorisées avant toute redirection.

---

## Variables d'environnement requises

| Variable | Où | Usage |
|---|---|---|
| `PAYMENT_HMAC_SECRET` | Java + Python | Signature HMAC bidirectionnelle |
| `REDIS_PASSWORD` | Python | Connexion Redis anti-replay |
| `MYSQL_USERNAME` | Java | DataSource MySQL |
| `MYSQL_PASSWORD` | Java | DataSource MySQL |

### Propriétés `golflc_settings.properties`

| Clé | Usage |
|---|---|
| `PAYMENT_SERVICE_URL` | URL du serveur Python (ex: `https://localhost:5000`) |
| `APP_BASE_URL` | URL de base de WildFly (ex: `https://localhost:8443`) |

---

## Diagramme de séquence résumé

```
Player (Browser)
    │
    ├─[1]─► manageCotisation/manageGreenfee/manageSubscription()
    │           └─ creditcard.xhtml (saisie carte)
    │
    ├─[2]─► onCompletePayment()
    │           ├─ HttpController.sendPaymentServer(creditcard)
    │           │       POST https://localhost:5000/creditcard/?return_url=...
    │           │       Headers: X-Timestamp, X-Signature, X-Payment-Nonce, X-Request-ID
    │           │       Body: JSON Creditcard
    │           │
    │           │       Python: @require_hmac → vérifie tout → affiche UI paiement
    │           │
    │           ├─ paymentStateStore.store(nonce, PaymentTransaction)
    │           └─ redirect → https://localhost:5000/about
    │
    │           [Player interacts with Python payment UI]
    │
    │   [Cancel]──► Python: return_url/payment_choice/phase1?nonce=&sig=
    │                   └─ /rest/payment/payment_canceled/ → creditcard_payment_canceled.xhtml
    │
    │   [Confirm]─► Python: return_url/payment_choice/phase2?nonce=&sig=
    │                   └─ /rest/payment/payment_confirmed → creditcard_payment_executed.xhtml
    │
    │   [Execute]─► Python: return_url/payment_choice/phase3?nonce=&sig=
    │                   └─ /rest/payment/payment_handle/
    │                         ├─ paymentStateStore.consume(nonce)  [one-time]
    │                         ├─ PaymentOrchestrator.handle(target)
    │                         │     └─ CotisationRegistrar / SubscriptionRegistrar / GreenfeeRegistrar
    │                         └─ redirect → https://localhost:5000/payment_generator (PDF)
    │
    └─[8]─► creditCard.xhtml / creditcard_payment_executed.xhtml
                └─ creditCardMail() → email confirmation
```

---

## Services requis au démarrage

```
WildFly  → localhost:8080 / 8443
MySQL    → localhost:3308  (datasource JNDI golflc)
MongoDB  → localhost:27017 (optionnel — help views, logging)
Python   → localhost:5000  (creditCardService.py)
Redis    → localhost:6379  (Memurai sur Windows) — anti-replay HMAC
```

Démarrer Redis sur Windows :
```
net start Memurai
```

Démarrer le serveur Python :
```
cd C:\Users\Louis Collet\Documents\PycharmProjects\creditcard
python creditCardService.py
```
