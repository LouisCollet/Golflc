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

## ⚙️ Settings

`Settings` est `@ApplicationScoped`. Toujours injecter — jamais d'appel statique.

```java
@Inject private entite.Settings settings;
settings.getProperty("WEBAPP");   // ✅ instance — jamais Settings.getProperty()
```

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
| `/cdi-code-patterns` | Templates complets Service, List simple, List Caffeine, Controller, Settings |
| `/cdi-migration-guide` | Règles NEVER/ALWAYS, cache map, checklist, erreurs courantes, imports |
| `/jsf-xhtml-guide` | Patterns XHTML/PrimeFaces avec exemples de code |
| `/testing-sql-guide` | Patterns IT tests + bonnes pratiques SQL |

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

**Version:** 5.0 (slim — templates et guides déplacés dans les skills CDI)
**Last Updated:** 2026-05-15
**Maintainer:** GolfLC Team
