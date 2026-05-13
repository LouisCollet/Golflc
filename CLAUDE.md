# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

---

## 🎯 Project Overview

**GolfLC** is a golf club management web application (Jakarta EE 11, Java 26) handling players, rounds, handicap (WHS-compatible), competitions, scoring, payments, and scheduling. It deploys as a WAR to WildFly.

**Current State:** Migration in progress from Java EE (manual JDBC) → Jakarta EE 11 (CDI + GenericDAO pattern)

---

## 🔨 Build & Deployment Commands
```bash
# Build and auto-deploy to running WildFly (main development workflow)
mvn clean package

# Full rebuild
mvn clean install

# Run unit tests only (skipped by default)
mvn test -Pfast-ut

# Run integration tests (requires WildFly running at localhost:9990)
mvn failsafe:integration-test -Pfast-it
mvn verify -Pfast-it

# Run a specific unit test class
mvn test -Pfast-ut -Dtest=MyTestClass

# Run a specific IT test
mvn failsafe:integration-test -Pfast-it -Dit.test=MyListIT

# Run a standalone main() class
mvn exec:java -Dexec.mainClass=fully.qualified.ClassName
```

**Build constraints:**
- Requires **JDK 26** at `C:\Program Files\Java\jdk-26` (Maven enforcer checks this)
- Requires **Maven 3.9.11+** and **Windows** OS
- Java 26 **preview features are enabled** (`--enable-preview`) in compiler and test configs
- Unit tests are **skipped by default**; integration tests run by default

---

## 🌍 Environment Requirements

- Set `MYSQL_USERNAME` and `MYSQL_PASSWORD` environment variables before running
- WildFly must be running at `localhost:8080` / `localhost:9990` for deployment and integration tests
- WildFly JNDI datasource `java:jboss/datasources/golflc` must point to MySQL database `golflc` at **port 3308** (non-standard)
- `app.devMode=true` JVM arg is injected automatically by the wildfly-maven-plugin at deploy time

---

## 🏗️ Architecture

### Data Access — CDI GenericDAO Pattern

Services use `@Inject dao.GenericDAO dao` for all database access. `GenericDAO` wraps the JNDI DataSource and provides typed query helpers.

**Legacy Pattern (⚠️ BEING REPLACED):**
```java
// ❌ OLD - connection_package.DBConnection
Connection conn = new DBConnection().getConnection();
try {
    new create.CreateXxx().create(entity, conn);
} finally {
    DBConnection.closeQuietly(conn, null, null, null);
}
```

**New Pattern (✅ TARGET):**
```java
// ✅ NEW - CDI GenericDAO injection
@ApplicationScoped
public class CreateXxx implements Serializable {
    @Inject private dao.GenericDAO dao;

    public void create(Entity entity) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // JDBC logic
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method
}
```

**GenericDAO helpers (prefer over raw JDBC when possible):**
```java
// List query
List<T> result = dao.queryList(query, new XxxRowMapper(), param1, param2);

// Single value
Integer count = dao.querySingle("SELECT COUNT(*) FROM ...", rs -> rs.getInt(1));

// DML (INSERT / UPDATE / DELETE) — returns rows affected
int rows = dao.execute("DELETE FROM ... WHERE id = ?", id);

// Raw connection (for transactions, generated keys)
try (Connection conn = dao.getConnection()) { ... }
```

### Schedulers

`startup/ExpirationScheduler` — CDI observer (`@Observes @Initialized(ApplicationScoped.class)`) qui tourne toutes les 24h et appelle :
- `delete.DeleteCart.deleteExpiredBefore()` — supprime les paniers PENDING > 7 jours
- `delete.DeleteActivation.deleteExpired()` — supprime les tokens d'activation > 1 semaine

---

## 📋 CDI Migration Standards

### ✅ Standard — Premières lignes de chaque méthode (OBLIGATOIRE)
```java
final String methodName = utils.LCUtil.getCurrentMethodName();
LOG.debug("entering {}", methodName);
```

### ✅ Standard — Logging paramétré (OBLIGATOIRE)

Toujours utiliser le style **SLF4J paramétré** avec `{}` — jamais la concaténation de strings.
Log4j2 n'évalue pas la string si le niveau de log est désactivé → meilleure performance.
Le pattern Log4j2 (`%class{5} . %method %line`) fournit déjà classe+méthode+ligne — ne pas répéter `methodName` dans les messages.

```java
// ✅ CORRECT
LOG.debug("entering {}", methodName);
LOG.debug("list size = {}", liste.size());
LOG.debug("player = {}, club = {}", player, club);
LOG.warn("player {} already connected", playerId);

// ❌ INTERDIT — concaténation de strings
LOG.debug("entering " + methodName);
LOG.debug(methodName + " - list size = " + liste.size());

// ❌ INTERDIT — methodName dans les messages autres que "entering"
LOG.debug(methodName + " - returning cached list");  // redondant avec pattern Log4j2
LOG.warn(methodName + " - empty result list");
```

### ✅ Standard — Fin de chaque méthode et classe
```java
} // end method
} // end class
```

### ✅ Standard — Gestion d'erreurs
```java
// Toujours utiliser handleGenericException / handleSQLException
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;

catch (SQLException e)  → handleSQLException(e, methodName);
catch (Exception e)     → handleGenericException(e, methodName);
```

### ✅ Standard — throws SQLException (OBLIGATOIRE avec handleSQLException)
```java
// Toute méthode qui utilise handleSQLException DOIT déclarer throws SQLException
public ReturnType method(params) throws SQLException {
    ...
    catch (SQLException e) {
        handleSQLException(e, methodName);
        return Collections.emptyList();
    }
} // end method

// ❌ INTERDIT — handleSQLException sans throws SQLException
public ReturnType method(params) {
    catch (SQLException e) {
        handleSQLException(e, methodName);   // ❌ SQLException non déclarée
    }
}
```

### ✅ Standard — Constructeur public (CDI obligatoire)
```java
// CDI ne peut pas instancier un bean avec constructeur privé
public XxxService() { }   // ✅ toujours public
```

---

### ✅ Service Pattern (CRUD operations)
```java
package [create|read|update|delete|find|calc];

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

@ApplicationScoped
public class ServiceName implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ServiceName() { }   // ✅ constructeur public obligatoire

    public ReturnType method(params) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT ...
            FROM ...
            WHERE ...
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, value);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                return result;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();   // ou valeur par défaut — jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method
} // end class
```

---

### ✅ List Pattern — Cache simple (liste globale, sans paramètre)

Pour les listes non paramétrées (ex: `ClubList`, `CourseList`) — un seul exemplaire en mémoire, invalidé explicitement.

```java
@Named
@ApplicationScoped
public class XxxList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Xxx> liste = null;

    public XxxList() { }

    public List<Xxx> list(params) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug("returning cached list size = {}", liste.size());
            return liste;
        }

        List<Xxx> result = dao.queryList(QUERY, new XxxRowMapper(), param);
        if (result.isEmpty()) {
            LOG.warn("empty result list");
        } else {
            LOG.debug("list size = {}", result.size());
        }
        liste = result;
        return liste;
    } // end method

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug("cache invalidated");
    } // end method

} // end class
```

---

### ✅ List Pattern — Cache Caffeine (liste paramétrée, multi-user safe)

Pour les listes paramétrées par clé (ex: `ClubsListLocalAdmin` par `adminId`, `HoleList` par `teeId`).
Caffeine garantit l'isolation entre utilisateurs et l'expiry automatique.

```java
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class XxxList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<Xxx>> cache;  // transient — non sérialisable

    public XxxList() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    } // end method

    public List<Xxx> list(int keyId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        List<Xxx> cached = cache.getIfPresent(keyId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        List<Xxx> result = dao.queryList(QUERY, new XxxRowMapper(), keyId);
        if (result.isEmpty()) {
            LOG.warn("empty result list for keyId = {}", keyId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(keyId, result);
        }
        return result;
    } // end method

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache.invalidateAll();
        LOG.debug("cache invalidated");
    } // end method

} // end class
```

**TTL recommandés :**
| Type de données | TTL | `maximumSize` |
|---|---|---|
| Par admin/user (clubs, courses) | 5 min | 100 |
| Club/course details | 10 min | 50 |
| Données structurelles (tees, holes) | 1 heure | 200 |
| Sunrise/sunset (change 1×/jour) | 24 heures | 50 |

---

### ✅ Controller/Manager Pattern
```java
@Named("xxxC")
@SessionScoped   // vérifier le scope — voir section Scope CDI
public class XxxController implements Serializable {

    private static final long serialVersionUID = 1L;

    // CDI injections (NEVER new Service())
    @Inject private create.CreateXxx        createXxxService;
    @Inject private cache.CacheInvalidator  cacheInvalidator;
    @Inject private context.ApplicationContext appContext;

    public XxxController() { }   // ✅ constructeur public obligatoire

    public String createXxx() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            createXxxService.create(xxx);
            cacheInvalidator.invalidateXxxCaches();   // ✅ via CacheInvalidator
            return "success.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method
} // end class
```

---

## 🔭 Scope CDI — Vérifier à chaque migration

> **Règle :** Vérifier le scope optimal à chaque occasion.

| Scope | Quand l'utiliser | Exemple |
|---|---|---|
| `@ApplicationScoped` | Stateless, pas d'état mutable, services utilitaires | `XxxList`, `Settings`, `ThumbnailsController` |
| `@SessionScoped` | État lié à la session utilisateur | `ClubController`, `PlayerController` |
| `@RequestScoped` | État lié à une seule requête HTTP (rare) | Peu utilisé dans ce projet |
| `@ViewScoped` | État lié à une vue JSF (dataTables, dialogs) | Controllers avec pagination/sélection |

**État mutable = champ d'instance dont la valeur change selon l'utilisateur/requête.**
Les champs `@Inject` et `@Resource` ne sont PAS des états mutables.

```java
// ✅ @ApplicationScoped — pas d'état mutable
public class ThumbnailsController {
    @Inject private Settings settings;   // injecté une fois, jamais modifié
    public boolean thumbs(File f) { ... } // reçoit tout en paramètre
}

// ✅ @SessionScoped — état mutable lié à l'utilisateur
public class ClubController {
    private Club club;        // change selon l'utilisateur
    private List<Club> clubs; // change selon la session
}
```

---

## ⚙️ Settings — Bean CDI Singleton

```java
// ✅ Settings est un @ApplicationScoped — injecter partout
@Named("settings")
@ApplicationScoped
public class Settings implements Serializable {
    public Settings() { }

    @PostConstruct
    public void init() { ... }                    // ✅ appelé automatiquement par WildFly

    public String getProperty(String key) { ... } // ✅ méthode d'instance — plus static
}

// ✅ Dans toutes les classes — injecter Settings
@Inject private entite.Settings settings;
String value = settings.getProperty("WEBAPP");    // ✅ instance

// ❌ Plus jamais
Settings.getProperty("WEBAPP");                   // ❌ méthode statique supprimée
Settings.init();                                   // ❌ @PostConstruct s'en charge
```

---

## 🚫 Migration Rules — NEVER Do This

### ❌ 1. Connection as parameter
```java
// ❌ FORBIDDEN
public void method(Entity entity, Connection conn)
```

### ❌ 2. Manual service instantiation
```java
// ❌ FORBIDDEN
new lists.XxxList().list(conn)
new create.CreateXxx().create(entity, conn)
```

### ❌ 3. Return null in catch
```java
// ❌ FORBIDDEN
catch (Exception e) { return null; }
// ✅ CORRECT
catch (Exception e) { handleGenericException(e, methodName); return Collections.emptyList(); }
```

### ❌ 4. DBConnection / closeQuietly
```java
// ❌ FORBIDDEN
import connection_package.DBConnection;
Connection conn = new DBConnection().getConnection();
DBConnection.closeQuietly(...);
```

### ❌ 5. PreparedStatement/ResultSet sans try-with-resources
```java
// ❌ FORBIDDEN
PreparedStatement ps = null;
try { ps = conn.prepareStatement(...); }
finally { DBConnection.closeQuietly(null, null, rs, ps); }
```

### ❌ 6. Constructeur privé sur bean CDI
```java
// ❌ FORBIDDEN — CDI ne peut pas instancier
private Settings() { }
// ✅ CORRECT
public Settings() { }
```

### ❌ 7. Méthodes static sur bean CDI injecté
```java
// ❌ FORBIDDEN — incompatible avec @Inject
public static String getProperty(String key) { }
public static List<Xxx> getListe() { }          // si la classe est @ApplicationScoped

// ✅ CORRECT — méthodes d'instance
public String getProperty(String key) { }
public List<Xxx> getListe() { }
```

### ❌ 8. @Asynchronous sur bean @ApplicationScoped CDI
```java
// ❌ FORBIDDEN — @Asynchronous est une annotation EJB
@ApplicationScoped
@Asynchronous               // ❌ incompatible
public class MailSender { }

// ✅ CORRECT — utiliser ManagedExecutorService
@Resource
private ManagedExecutorService mailExecutor;
CompletableFuture.runAsync(() -> { ... }, mailExecutor);
```

### ❌ 9. System.exit() dans WildFly
```java
// ❌ FORBIDDEN — tue le serveur entier
System.exit(1);
// ✅ CORRECT
LOG.error("module not found for lang = {}", lang);
return null;
```

### ❌ 10. @Inject sur un POJO (entité non-CDI)
```java
// ❌ FORBIDDEN — Round est un POJO, non injectable
@Inject private Round round;
// ✅ CORRECT — instancier localement
Round round = new Round();
```

### ❌ 11. setListe(null) statique pour invalider le cache
```java
// ❌ REMPLACÉ
lists.XxxList.setListe(null);
// ✅ CORRECT — via CacheInvalidator dans les controllers
@Inject private cache.CacheInvalidator cacheInvalidator;
cacheInvalidator.invalidateXxxCaches();
```

### ❌ 12. Cache liste sans clé pour données per-user
```java
// ❌ DANGEREUX — un seul cache global dans un @ApplicationScoped
// Les données du dernier utilisateur écrasent celles des autres
private List<Club> liste = null;  // si paramétré par localAdminId → fuite de données

// ✅ CORRECT — Caffeine avec clé par utilisateur
private transient Cache<Integer, List<Club>> cache;
cache.put(adminId, result);
```

---

## ✅ Migration Rules — ALWAYS Do This

### 1. Scope + Serializable + serialVersionUID
```java
@ApplicationScoped        // vérifier le scope optimal
public class ServiceName implements Serializable {
    private static final long serialVersionUID = 1L;
```

### 2. GenericDAO — standard d'accès aux données
```java
@Inject private dao.GenericDAO dao;

// Utiliser les helpers quand possible
dao.queryList(query, mapper, params)
dao.querySingle(query, mapper, params)
dao.execute(query, params)

// Connexion brute pour transactions ou generated keys
try (Connection conn = dao.getConnection()) { ... }
```

### 3. Premières lignes de méthode (TOUTES les méthodes)
```java
final String methodName = utils.LCUtil.getCurrentMethodName();
LOG.debug("entering {}", methodName);
```

### 4. Try-with-resources (3 niveaux)
```java
try (Connection conn = dao.getConnection();
     PreparedStatement ps = conn.prepareStatement(query)) {
    try (ResultSet rs = ps.executeQuery()) {
        // logic
    }
} catch (SQLException e) {
    handleSQLException(e, methodName);
} catch (Exception e) {
    handleGenericException(e, methodName);
}
```

### 5. CDI injection — jamais new Service()
```java
@Inject private lists.XxxList xxxListService;
xxxListService.list(params);   // ✅
```

### 6. Cache invalidation — toujours via CacheInvalidator dans les controllers
```java
@Inject private cache.CacheInvalidator cacheInvalidator;
cacheInvalidator.invalidateXxxCaches();   // ✅ jamais .invalidateCache() direct
```

### 7. Fin de méthode et classe
```java
} // end method
} // end class
```

### 8. main() conservé commenté
```java
/*
void main() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // tests locaux
} // end main
*/
```

### 9. Mise à jour des appelants lors de la migration d'une liste (OBLIGATOIRE)

> Quand une liste est migrée, il faut **simultanément** mettre à jour tous les fichiers qui l'appellent.

```java
// ✅ @Inject ajouté lors de la migration de XxxList — YYYY-MM-DD
@Inject private lists.XxxList xxxListService;

// Commenter l'ancien appel, ajouter le nouveau avec date
// lists.XxxList.setListe(null);
xxxListService.invalidateCache(); // migrated YYYY-MM-DD
```

### 10. Cache invalidation dans les controllers — toujours via CacheInvalidator

> **Règle :** Dans les controllers et managers, toutes les invalidations de cache passent par `cache.CacheInvalidator` — jamais d'appel direct à `xxxList.invalidateCache()` depuis un controller.

```java
// ✅ CORRECT — via CacheInvalidator injecté
@Inject private cache.CacheInvalidator cacheInvalidator;
cacheInvalidator.invalidateProfessionalCaches();

// ❌ INTERDIT dans un controller
@Inject private lists.LessonProList lessonProList;
lessonProList.invalidateCache();  // ❌ appel direct depuis controller
```

`CacheInvalidator` (`cache/CacheInvalidator.java`) est le point unique d'invalidation groupée.

---

## 🏛️ Architecture Controllers

| Controller | Règle |
|---|---|
| `ClubController` | Délègue à `ClubManager` — jamais aux services directement |
| `PlayerController` | Délègue à `PlayerManager` |
| `CourseController` | Délègue à `CourseManager` |
| `ApplicationContext` | Source de vérité partagée — `Settings`, `Player`, `Club`, `Course`, `HandicapIndex` |

### SelectionPurpose — Navigation club/course/round

Toute navigation vers une page de sélection passe par `enumeration.SelectionPurpose` :

```java
// Menu → clubC.to_selectPurpose_xhtml('CODE') → SelectionPurpose.fromCode()
//       → purpose.navigationToFirst()  (ex: selectClubCourse.xhtml)
// Action bouton → switch(purpose) { case CREATE_TARIF_MEMBER → ... }
```

Pages unifiées :
- `selectClubCourse.xhtml` — sélection club/course (remplace selectCourse, selectClubLocalAdmin, selectCourseLocalAdmin)
- `selectRound.xhtml` — sélection round (remplace selectStablefordRounds, select_participants_round, selectRegisteredRounds)

### Répertoire parking/

`src/main/webapp/parking/` contient des fichiers XHTML archivés (versions remplacées). **Ne pas lire ni modifier.** Voir `.claudeignore`.

```java
// ✅ ClubController — toujours via ClubManager
public void createClub() {
    clubManager.createClub(appContext.getClub());   // ✅
}

// ❌ ClubController — jamais direct
public void createClub() {
    createClubService.create(appContext.getClub()); // ❌
}
```

---

## 🌐 XHTML JSF Patterns

### ✅ Binding direct sans valueChangeListener
```xml
<!-- ❌ Anti-pattern — listener dans l'entité -->
<p:inputText value="#{clubC.club.clubName}"
             valueChangeListener="#{clubC.club.nameListener}"
             immediate="true"/>

<!-- ✅ Binding direct — JSF appelle setClubName() automatiquement -->
<p:inputText value="#{clubC.club.clubName}"/>
```

### ✅ immediate="true" — uniquement sur Cancel/Back
```xml
<!-- ✅ Justifié — bypass validation pour annuler -->
<p:commandButton value="Cancel" action="#{xxxC.cancel()}" immediate="true"/>

<!-- ❌ Non justifié — sur un inputText -->
<p:inputText value="#{xxx}" immediate="true"/>
```

### ✅ Parenthèses sur les actions EL
```xml
<!-- ❌ Manquant -->
action="#{clubC.createClub}"

<!-- ✅ Correct -->
action="#{clubC.createClub()}"
```

### ✅ f:validateBean pour bypasser @NotNull sur un champ spécifique
```xml
<p:datePicker value="#{courseC.round.roundDate}" immediate="true">
    <f:validateBean disabled="true"/>   <!-- ✅ bypass @NotNull sur ce seul champ -->
</p:datePicker>
```

### ✅ p:divider remplace p:separator (deprecated depuis PrimeFaces 10)
```xml
<!-- ❌ Deprecated -->
<p:separator/>
<!-- ✅ Correct -->
<p:divider layout="horizontal" type="solid"/>
```

### ✅ process="@this" — commandButton dans un formulaire avec dialog cachée

Quand un `<h:form>` contient un `<p:dialog>` avec des champs `required="true"`, tout bouton **hors du dialog** doit utiliser `process="@this"` pour éviter que Bean Validation valide les champs cachés du dialog.

```xml
<!-- ❌ Déclenche BV sur les champs cachés du dialog -->
<p:commandButton value="Select" action="#{xxxC.select(item)}" process="@form"/>

<!-- ✅ Valide uniquement le bouton lui-même -->
<p:commandButton value="Select" action="#{xxxC.select(item)}" process="@this"/>
```

### ✅ Initialisation des champs @NotNull sur les entités POJO

Les champs annotés `@NotNull` doivent être initialisés à une valeur par défaut pour éviter les erreurs BV sur une instance fraîche.

```java
// ❌ Champ null sur new Professional() → BV error si le form est soumis
@NotNull(message="{professional.amount.notnull}")
private Double proAmount;

// ✅ Valeur par défaut — BV passe, l'utilisateur peut corriger ensuite
@NotNull(message="{professional.amount.notnull}")
private Double proAmount = 0.0;
```

### ✅ FullCalendar — customButtons via extender JS

Pour ajouter des boutons personnalisés à la barre d'outils FullCalendar (`p:schedule`), utiliser un `extender` JS + un `p:commandButton` caché déclenché par JS.

```xml
<p:schedule extender="initSchedule" ...>
    ...
</p:schedule>

<!-- Bouton caché — déclenché par le customButton FullCalendar -->
<p:commandButton id="btnMyAction"
                 style="display:none"
                 ajax="false"
                 action="#{myC.myAction()}"
                 process="@form"/>

<script type="text/javascript">
function initSchedule() {
    this.cfg.options.customButtons = {
        myButton: {
            text: 'Mon Action',
            click: function() {
                document.getElementById('myFormId:btnMyAction').click();
            }
        }
    };
    this.cfg.options.headerToolbar = {
        left:   'prev,next today',
        center: 'title',
        right:  'myButton dayGridMonth,timeGridWeek,timeGridDay'
    };
}
</script>
```

---

## 📦 Package Conventions

| Package | Role | Migration Status |
|---|---|---|
| `Controllers/` | JSF backing beans (`@Named`, `@SessionScoped`) | 🔄 In progress |
| `Controller/refact/` | Controllers CDI refactorisés | 🔄 In progress |
| `manager/` | Business logic coordinators | 🔄 In progress |
| `context/` | ApplicationContext — état partagé | ✅ Done |
| `create/` | Create operations | 🔄 In progress |
| `read/` | Read operations | ✅ Mostly done |
| `update/` | Update operations | ⏳ Pending |
| `delete/` | Delete operations | ⏳ Pending |
| `lists/` | List services with caching | ✅ Mostly done |
| `find/` | Finder services | ✅ Mostly done |
| `calc/` | Pure calculation logic (no DB) | ✅ No migration needed |
| `entite/` | Domain objects — POJOs sans annotations CDI | ✅ No migration needed |
| `entite/composite/` | View-layer composite objects | ✅ No migration needed |
| `payment/` | Payment processing (`PaymentStateStore`, `PaymentTransaction`) | ✅ Done |
| `mail/` | Email services | ✅ Already CDI |
| `ical/` | iCal calendar generation | ✅ No migration needed |
| `security/` | Jakarta Security | ✅ Already CDI |
| `enumeration/` | Enums | ✅ No migration needed |
| `exceptions/` | Custom exceptions | ✅ No migration needed |
| `interfaces/` | Shared constants | ✅ No migration needed |
| `utils/` | Utilities | ✅ No migration needed |
| `numbertext/` | Number-to-text conversion | ✅ CDI migrated |
| `charts/`, `chartsdevx/` | Chart data prep | ⏳ Pending |
| `googlemaps/` | Google Maps integration | ✅ No migration needed |
| `batch/` | Jakarta Batch jobs | 🔄 In progress |
| `startup/` | CDI startup observers (`ExpirationScheduler`) | ✅ Done |
| `dao/`, `sql/`, `rowmappers/` | DAO layer | ✅ No migration needed |

---

## 🗂️ Cache Invalidation Map

> **Standard :** `invalidateCache()` via `@Inject`, pas `setListe(null)` statique.
> Dans les controllers/managers : toujours via `CacheInvalidator`.

### Players & Handicaps
```java
playersList.invalidateCache();
handicapList.invalidateCache();
handicapIndexList.invalidateCache();
```

### Rounds & Inscriptions
```java
inscriptionList.invalidateCache();
inscriptionListForOneRound.invalidateCache();
participantsRoundList.invalidateCache();
recentRoundList.invalidateCache();
roundPlayersList.invalidateCache();
playedList.invalidateCache();
unavailableListForDate.invalidateCache();
```

### Competitions
```java
competitionInscriptionsList.invalidateCache();
competitionDescriptionList.invalidateCache();
competitionRoundsList.invalidateCache();
competitionStartList.invalidateCache();
participantsStablefordCompetitionList.invalidateCache();
matchplayList.invalidateCache();
```

### Clubs & Courses
```java
clubList.invalidateCache();
clubsListLocalAdmin.invalidateCache();      // Caffeine — invalidateAll()
clubDetailList.invalidateCache();           // Caffeine — invalidateAll()
courseList.invalidateCache();
courseListForClub.invalidateCache();        // Caffeine — invalidateAll()
coursesListLocalAdmin.invalidateCache();    // Caffeine — invalidateAll()
teesCourseList.invalidateCache();           // Caffeine — invalidateAll()
holeList.invalidateCache();                 // Caffeine — invalidateAll()
clubCourseTeeListOne.invalidateCache();
```

### Scores & Flights
```java
allFlightsList.invalidateCache();
flightAvailableList.invalidateCache();
scoreCardList1EGA.invalidateCache();
scoreCardList3.invalidateCache();
```

### Subscriptions
```java
subscriptionRenewalList.invalidateCache();
localAdminCotisationList.invalidateCache();
localAdminGreenfeeList.invalidateCache();
systemAdminSubscriptionList.invalidateCache();
```

### Professionals
```java
professionalClubList.invalidateCache();
lessonProList.invalidateCache();
professionalListForClub.invalidateCache();
professionalListForPayments.invalidateCache();
findCountListProfessional.invalidateCache();
registerResultList.invalidateCache();
```

### Find services
```java
findSlopeRating.invalidateCache();
findInfoStableford.invalidateCache();
findCurrentSubscription.invalidateCache();
findTeeStart.invalidateCache();
```

### Other
```java
sunriseSunsetList.invalidateCache();
```

---

## 🔄 Imports to Change

### ❌ Remove
```java
import connection_package.DBConnection;
import connection_package.ConnectionProvider;
import static org.omnifaces.util.Faces.getResourceAsStream; // si inutilisé
```

### ✅ Add
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
```

### ✅ Keep
```java
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
```

---

## ✅ Migration Checklist (per file)

- [ ] Vérifier le scope optimal (`@ApplicationScoped` si stateless)
- [ ] Ajouter `@ApplicationScoped` (ou scope approprié)
- [ ] Ajouter `implements Serializable` + `serialVersionUID`
- [ ] Ajouter `@Inject private dao.GenericDAO dao`
- [ ] Constructeur public ajouté
- [ ] Supprimer `Connection conn` des paramètres de méthode
- [ ] Remplacer par `dao.getConnection()` ou `dao.queryList()` / `dao.execute()`
- [ ] try-with-resources pour Connection, PreparedStatement et ResultSet
- [ ] Supprimer `DBConnection.closeQuietly()`
- [ ] `catch(SQLException)` → `handleSQLException(e, methodName)` + `throws SQLException`
- [ ] `catch(Exception)` → `handleGenericException(e, methodName)`
- [ ] `return null` → `Collections.emptyList()` ou valeur par défaut
- [ ] `final String methodName` + `LOG.debug("entering")` dans TOUTES les méthodes
- [ ] Logs : style paramétré `LOG.debug("msg = {}", value)` — pas de concaténation, pas de `methodName +`
- [ ] `// end method` + `// end class` ajoutés
- [ ] `main()` conservé commenté avec standards
- [ ] Pour les listes globales : cache d'instance + `invalidateCache()`
- [ ] Pour les listes paramétrées : Caffeine `Cache<K, List<T>>` + `@PostConstruct init()`
- [ ] Settings: injecté via `@Inject private entite.Settings settings`
- [ ] Méthodes static → instance si la classe est un bean CDI
- [ ] Vérifier absence de `System.exit()`
- [ ] Compiler: `mvn compile`
- [ ] Tester: `mvn test -Pfast-ut`

---

## 🐛 Common Migration Errors

### Error 1: Service not injected
```java
// ❌ BEFORE
List<Hole> holes = new lists.HoleList().listForTee(teeId, conn);
// ✅ AFTER
@Inject private lists.HoleList holeListService;
List<Hole> holes = holeListService.listForTee(teeId);
```

### Error 2: Wrong DataSource import
```java
// ❌ WRONG
import jakarta.activation.DataSource;
// ✅ CORRECT
import javax.sql.DataSource;  // uniquement dans GenericDAO — pas dans les services
```

### Error 3: Missing early return in list
```java
// ❌ WRONG
public List<Xxx> list() {
    if (liste == null) { /* load */ return liste; }
    else { return liste; }
}
// ✅ CORRECT
public List<Xxx> list() {
    if (liste != null) { return liste; }   // early return FIRST
    /* load */
    return liste;
}
```

### Error 4: POJO injecté
```java
// ❌ WRONG — Round est un POJO
@Inject private Round round;
// ✅ CORRECT — instancier localement
Round round = new Round();
```

### Error 5: Cache liste partagée pour données per-user
```java
// ❌ WRONG — liste globale pour données paramétrées par user → fuite de données
private List<Club> liste = null;  // données du dernier user vues par tous

// ✅ CORRECT — Caffeine avec clé
private transient Cache<Integer, List<Club>> cache;
```

### Error 6: Settings statique
```java
// ❌ WRONG — getProperty() plus static
Settings.getProperty("WEBAPP");
// ✅ CORRECT
@Inject private entite.Settings settings;
settings.getProperty("WEBAPP");
```

### Error 7: Cache invalidated directly from controller (bypass CacheInvalidator)
```java
// ❌ WRONG — appel direct depuis un controller
@Inject private lists.LessonProList lessonProList;
lessonProList.invalidateCache();

// ✅ CORRECT — toujours via CacheInvalidator
@Inject private cache.CacheInvalidator cacheInvalidator;
cacheInvalidator.invalidateProfessionalCaches();
```

### Error 8: throws SQLException manquant
```java
// ❌ WRONG — handleSQLException rethrows → throws SQLException obligatoire
public void method() {
    catch (SQLException e) { handleSQLException(e, methodName); }
}
// ✅ CORRECT
public void method() throws SQLException {
    catch (SQLException e) { handleSQLException(e, methodName); }
}
```

---

## 🧪 CDI / JSF Patterns

- CDI bean discovery est en mode `annotated` — chaque bean géré **doit** avoir une annotation de scope
- Les **entités POJO** (`entite/`) ne doivent PAS avoir `@Named` ou `@ViewScoped` — ce ne sont pas des beans CDI
- JSF views sont des fichiers `.xhtml` servis via le mapping FacesServlet `*.xhtml`
- **PrimeFaces 15** est la bibliothèque de composants UI
- **OmniFaces 5** est utilisé pour les utilitaires JSF
- Resource bundles: `msg` (messages), `settings` (golflc_settings), `bundle` (Bundle)
- Locales supportées: `en` (default), `fr`, `nl`, `de`, `es`

---

## 💳 Payment Architecture

JAX-RS + JSF sont strictement séparés :

| Class | Scope | Role |
|---|---|---|
| `PaymentController` | `@SessionScoped` | JSF backing bean — panier, navigation, persistance |
| `PaymentRestResource` | `@RequestScoped` | REST callbacks Python (phase1/phase2/phase3) |
| `PaymentStateStore` | `@ApplicationScoped` | Bridge `ConcurrentHashMap<nonce, PaymentTransaction>` |
| `PaymentTransaction` | POJO | Snapshot de la transaction (listLessons, creditcard, type) |

**Règle :** `PaymentController` ne doit **jamais** avoir d'annotation `@Path`. La persistance (INSERT en DB) est déclenchée par `onPaymentCompleted()` appelé via `preRenderView` sur `creditcard_payment_executed.xhtml` — garantit le contexte JSF et les messages d'erreur.

```java
// ✅ onPaymentCompleted() — point unique de persistance côté JSF
// Appelé par <f:event type="preRenderView" listener="#{payC.onPaymentCompleted}"/>
public void onPaymentCompleted() {
    // récupère PaymentTransaction depuis PaymentStateStore via nonce
    // INSERT lesson / payments_lesson / etc.
}
```

---

## 🌐 REST

JAX-RS est activé via `rest/RestActivator.java`. `PaymentRestResource` est la seule classe REST — les controllers JSF ne doivent pas avoir d'annotations `@Path`.

---

## 🧪 Testing

- **Unit tests** (`*Test.java`): JUnit Jupiter 5, Mockito 5, Weld JUnit 5 — run with `-Pfast-ut`
- **Integration tests** (`*IT.java`): JDBC direct via `JdbcConnectionProvider` — run with `-Pfast-it`
- **Selenium/E2E**: Selenium 4 + selenium-jupiter, PrimeFaces Selenium — dans `src/test/java/selenium/`

### ✅ IT Test Pattern — Upsert services (AbstractDaoIT)

Pour les tests create/upsert qui nécessitent `GenericDAO` injecté :

```java
@Tag("integration")
public class XxxUpsertIT extends integration.support.AbstractDaoIT {

    private static final int TEST_ID = 9999;

    @Test
    void upsertXxx_realDB_insertOrUpdate() throws Exception {
        CreateXxx createXxx = new CreateXxx();
        injectDao(createXxx);   // injection par réflexion via AbstractDaoIT

        dao.execute("DELETE FROM xxx WHERE idxxx = ?", TEST_ID);

        Xxx xxx = new Xxx();
        xxx.setIdxxx(TEST_ID);
        // ... set fields

        boolean result = createXxx.upsert(xxx);
        assertTrue(result);

        Integer count = dao.querySingle(
            "SELECT COUNT(*) FROM xxx WHERE idxxx = ?",
            rs -> rs.getInt(1), TEST_ID);
        assertEquals(1, count);
    } // end method

} // end class
```

### ✅ IT Test Pattern — List services

```java
@Tag("integration")
public class XxxListIT {

    // SQL défini une seule fois dans la classe de production
    private static final String QUERY         = lists.XxxList.QUERY;
    private static final String EXPLAIN_QUERY = "EXPLAIN ANALYZE " + QUERY;

    @Test
    void query_realDB_executesWithoutError() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                assertTrue(rs.getInt("idxxx") > 0);
            }
        }
    } // end method

    @Test
    void query_explainAnalyze_showsExecutionPlan() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(EXPLAIN_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { LOG.info("{}", rs.getString(1)); }
        }
    } // end method
}
```

**Règles IT tests :**
- Pas de Mockito — connexion réelle à MySQL via `JdbcConnectionProvider`
- `QUERY` → tests de données (assertions sur colonnes/valeurs) — doit être `public static final` dans la classe de production
- `EXPLAIN_QUERY` → test diagnostique plan d'exécution (log uniquement, pas d'assertions)
- Pas de DELETE sans rollback (`conn.setAutoCommit(false)` + `conn.rollback()` dans le catch) sauf si le test gère lui-même le nettoyage

### ✅ SQL — Bonnes pratiques

#### ❌ YEAR()/MONTH() et DATE() bloquent les index
```sql
-- ❌ Les fonctions sur la colonne empêchent l'utilisation d'un index
WHERE YEAR(SubscriptionEndDate) = YEAR(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
WHERE DATE(activation.ActivationCreationDate) < DATE_SUB(current_date, INTERVAL 1 WEEK)

-- ✅ Condition de plage — permet l'utilisation d'un index sur la colonne
WHERE SubscriptionEndDate >= DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 1 MONTH)
  AND SubscriptionEndDate <  DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 2 MONTH)
WHERE activation.ActivationCreationDate < DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK)
```

#### ❌ Colonnes non qualifiées dans les JOINs
```sql
-- ❌ Ambigu — MySQL résout si unique, mais fragile
AND player.idplayer = cotisationIdPlayer

-- ✅ Toujours qualifier avec le nom de table
AND player.idplayer = payments_cotisation.CotisationIdPlayer
```

#### ❌ MONTH() + 1 déborde en décembre
```sql
-- ❌ Retourne 13 en décembre → zéro résultat
AND MONTH(col) = MONTH(CURRENT_DATE()) + 1

-- ✅ DATE_ADD gère le changement d'année
AND MONTH(col) = MONTH(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
AND YEAR(col)  = YEAR(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
```

---

## 📚 Key Third-Party Libraries

| Library | Purpose |
|---|---|
| Jakarta EE 11 | Enterprise platform |
| PrimeFaces 15 / Extensions | JSF UI components |
| OmniFaces 5 | JSF utilities |
| Apache POI 5 | Excel (XLSX) export |
| Jackson 2.21 | JSON serialization |
| Caffeine 3.2 | In-process cache (per-key TTL, multi-user safe) |
| MongoDB Driver Sync 5.6 | MongoDB integration |
| iCal4j 4.2 | iCal generation |
| OpenPDF 3 | PDF generation |
| Google Maps Services | Geocoding |
| Google Cloud Translate | Translation API |
| Quartz 2.5 | Job scheduling |
| MaxMind GeoIP2 | IP geolocation |
| Log4j2 | Logging (config: `src/main/resources/log4j2.xml`) |

---

## 💬 Skills disponibles

| Skill | Quand l'utiliser |
|---|---|
| `/mvn-compile` | Après chaque édition Java — automatique |
| `/mvn-it ClubUpsertIT` | Lancer un test d'intégration spécifique |
| `/cdi-migrate path/to/File.java` | Migrer un fichier vers le pattern CDI |
| `/xhtml-validate` | Valider un fichier XHTML — automatique |
| `/i18n-key` | Ajouter une clé dans les 4 bundles (en/fr/nl/es) |

---

## 📊 Migration Metrics

**Target:** Zero usage of `connection_package.DBConnection` in production code

**Check before considering migration complete:**
1. ✅ No `import connection_package.DBConnection` anywhere
2. ✅ No `new Service().method(conn)` in Controllers/Managers
3. ✅ All services have `@ApplicationScoped` (or appropriate scope)
4. ✅ All services use `@Inject private dao.GenericDAO dao`
5. ✅ All global lists have early return + `invalidateCache()`
6. ✅ All parameterized lists use Caffeine `Cache<K, List<T>>`
7. ✅ No `Settings.getProperty()` static calls
8. ✅ No `System.exit()` anywhere
9. ✅ Application deploys successfully to WildFly
10. ✅ Integration tests pass

---

**Version:** 4.0 (GenericDAO standard, Caffeine cache pattern, ExpirationScheduler)
**Last Updated:** 2026-05-13
**Maintainer:** GolfLC Team
