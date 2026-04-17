# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

---

## 🎯 Project Overview

**GolfLC** is a golf club management web application (Jakarta EE 11, Java 25) handling players, rounds, handicap (WHS-compatible), competitions, scoring, payments, and scheduling. It deploys as a WAR to WildFly.

**Current State:** Migration in progress from Java EE (manual JDBC) → Jakarta EE 11 (CDI + DataSource pattern)

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

# Run a standalone main() class
mvn exec:java -Dexec.mainClass=fully.qualified.ClassName
```

**Build constraints:**
- Requires **JDK 25** at `C:\Program Files\Java\jdk-25` (Maven enforcer checks this)
- Requires **Maven 3.9.11+** and **Windows** OS
- Java 25 **preview features are enabled** (`--enable-preview`) in compiler and test configs
- Unit tests are **skipped by default**; integration tests run by default

---

## 🌍 Environment Requirements

- Set `MYSQL_USERNAME` and `MYSQL_PASSWORD` environment variables before running
- WildFly must be running at `localhost:8080` / `localhost:9990` for deployment and integration tests
- WildFly JNDI datasource `java:jboss/datasources/golflc` must point to MySQL database `golflc` at **port 3308** (non-standard)
- `app.devMode=true` JVM arg is injected automatically by the wildfly-maven-plugin at deploy time

---

## 🏗️ Architecture

### Data Access — Migration to CDI DataSource Pattern

**⚠️ MIGRATION IN PROGRESS** — The app is transitioning from manual JDBC to CDI-managed DataSource:

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
// ✅ NEW - CDI DataSource injection
@ApplicationScoped
public class CreateXxx implements Serializable {
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public void create(Entity entity) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            // JDBC logic
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    }
}
```

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
// ✅ CORRECT — entering avec methodName, autres messages sans
LOG.debug("entering {}", methodName);
LOG.debug("list size = {}", liste.size());
LOG.debug("player = {}, club = {}", player, club);
LOG.warn("player {} already connected", playerId);

// ❌ INTERDIT — concaténation
LOG.debug("entering " + methodName);
LOG.debug(methodName + " - list size = " + liste.size());

// ❌ INTERDIT — methodName dans le message (redondant avec le pattern Log4j2)
LOG.debug("entering {}", methodName);
LOG.debug("list size = {}", liste.size());
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

### ✅ Standard — DataSource (lookup JNDI obligatoire)
```java
@Resource(lookup = "java:jboss/datasources/golflc")
private DataSource dataSource;
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
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class ServiceName implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ServiceName() { }   // ✅ constructeur public obligatoire

    public ReturnType method(params) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT ...
            FROM ...
            WHERE ...
            """;

        try (Connection conn = dataSource.getConnection();
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

### ✅ List Pattern (with cache)

> ⚠️ **Note:** Les listes utilisent un **cache d'instance** (pas static).
> `@ApplicationScoped` garantit le singleton — le static est inutile et dangereux.
> Pour l'invalidation du cache, utiliser `invalidateCache()` (pas `setListe(null)` statique).

```java
@Named
@ApplicationScoped
public class XxxList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Xxx> liste = null;

    public XxxList() { }   // ✅ constructeur public obligatoire

    public List<Xxx> list(params) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug("returning cached list size = {}", liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM table
            WHERE condition = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, param);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Xxx> mapper = new XxxRowMapper();
                while (rs.next()) {
                    liste.add(mapper.map(rs));
                }
                if (liste.isEmpty()) {
                    LOG.warn("empty result list");
                } else {
                    LOG.debug("list size = {}", liste.size());
                }
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();   // ✅ jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<Xxx> getListe()                  { return liste; }
    public void      setListe(List<Xxx> liste)   { this.liste = liste; }

    // ✅ Invalidation explicite — plus clair que setListe(null)
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug("cache invalidated");
    } // end method

} // end class
```

---

### ✅ Controller/Manager Pattern
```java
@Named("xxxC")
@SessionScoped   // vérifier le scope — voir section Scope CDI
public class XxxController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ CDI injections (NEVER new Service())
    @Inject private create.CreateXxx    createXxxService;
    @Inject private lists.XxxList       xxxListService;
    @Inject private context.ApplicationContext appContext;

    public XxxController() { }   // ✅ constructeur public obligatoire

    public String createXxx() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            createXxxService.create(xxx);
            xxxListService.invalidateCache();   // ✅ invalidation instance
            showMessageInfo("Xxx created successfully");
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
    public Settings() { }                          // constructeur public obligatoire

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
// ✅ CORRECT — via injection + invalidateCache()
@Inject private lists.XxxList xxxList;
xxxList.invalidateCache();
```

---

## ✅ Migration Rules — ALWAYS Do This

### 1. Scope + Serializable + serialVersionUID
```java
@ApplicationScoped        // vérifier le scope optimal
public class ServiceName implements Serializable {
    private static final long serialVersionUID = 1L;
```

### 2. DataSource JNDI standard du projet
```java
@Resource(lookup = "java:jboss/datasources/golflc")
private DataSource dataSource;
```

> **`dao.getConnection()` is the project standard** for obtaining JDBC connections in services.

### 3. Premières lignes de méthode (TOUTES les méthodes)
```java
final String methodName = utils.LCUtil.getCurrentMethodName();
LOG.debug("entering {}", methodName);
```

### 4. Try-with-resources (3 niveaux)
```java
try (Connection conn = dataSource.getConnection();
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

### 6. Cache invalidation via instance
```java
@Inject private lists.XxxList xxxList;
xxxList.invalidateCache();      // ✅ pas setListe(null) statique
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

**Dans le fichier appelant — ajouter l'injection EN DÉBUT de section @Inject :**
```java
// ✅ @Inject ajouté lors de la migration de XxxList — YYYY-MM-DD
@Inject private lists.XxxList xxxListService;
```

**Dans chaque méthode — commenter l'ancien appel, ajouter le nouveau avec date :**
```java
// lists.XxxList.setListe(null);
xxxListService.invalidateCache(); // migrated YYYY-MM-DD
```

**Règle :** La date du jour est toujours ajoutée en commentaire sur la ligne `invalidateCache()`.

```java
// Exemple concret — migration du 2026-02-23
// lists.ClubsListLocalAdmin.setListe(null);
clubsListLocalAdminService.invalidateCache(); // migrated 2026-02-23
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

`CacheInvalidator` (`cache/CacheInvalidator.java`) est le point unique d'invalidation groupée — il appelle `invalidateCache()` sur les listes concernées.

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

Les champs annotés `@NotNull` doivent être initialisés à une valeur par défaut pour éviter les erreurs BV sur une instance fraîche (ex: entité liée à un dialog non encore affiché).

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
| `dao/`, `sql/`, `rowmappers/` | DAO layer (emerging) | ✅ No migration needed |

---

## 🗂️ Cache Invalidation Map

> ⚠️ **Standard migré :** utiliser `invalidateCache()` via `@Inject`, pas `setListe(null)` statique.

```java
// ✅ Pattern standard — via injection
@Inject private lists.PlayedList          playedList;
@Inject private lists.HandicapIndexList   handicapIndexList;

playedList.invalidateCache();
handicapIndexList.invalidateCache();
```

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
clubsListLocalAdmin.invalidateCache();
clubDetailList.invalidateCache();
courseList.invalidateCache();
courseListForClub.invalidateCache();
coursesListLocalAdmin.invalidateCache();
clubCourseTeeListOne.invalidateCache();
teesCourseList.invalidateCache();
holeList.invalidateCache();
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
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
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
- [ ] Ajouter `@Resource(lookup="java:jboss/datasources/golflc") DataSource`
- [ ] Constructeur public ajouté
- [ ] Supprimer `Connection conn` des paramètres de méthode
- [ ] Remplacer par `try (Connection conn = dataSource.getConnection())`
- [ ] try-with-resources pour PreparedStatement et ResultSet
- [ ] Supprimer `DBConnection.closeQuietly()`
- [ ] `catch(SQLException)` → `handleSQLException(e, methodName)`
- [ ] `catch(Exception)` → `handleGenericException(e, methodName)`
- [ ] `return null` → `Collections.emptyList()` ou valeur par défaut
- [ ] `final String methodName` + `LOG.debug("entering")` dans TOUTES les méthodes
- [ ] Logs : style paramétré `LOG.debug("msg = {}", value)` — pas de concaténation
- [ ] `// end method` + `// end class` ajoutés
- [ ] `main()` conservé commenté avec standards
- [ ] Pour les listes: cache d'instance (pas static) + `invalidateCache()`
- [ ] Pour les listes: early return `if (liste != null) return liste;`
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
import javax.sql.DataSource;
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

### Error 5: Cache invalidation statique
```java
// ❌ WRONG — plus static après migration
lists.PlayedList.setListe(null);
// ✅ CORRECT — via injection
@Inject private lists.PlayedList playedList;
playedList.invalidateCache();
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

```bash
# Run a specific IT test
mvn failsafe:integration-test -Pfast-it -Dit.test=MyListIT
```

### ✅ IT Test Pattern — List services

```java
@Tag("integration")
public class XxxListIT {

    // ✅ SQL défini une seule fois dans la classe de production
    private static final String QUERY         = lists.XxxList.QUERY;
    private static final String EXPLAIN_QUERY = "EXPLAIN ANALYZE " + QUERY;

    @Test
    void query_realDB_executesWithoutError() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                assertTrue(rs.getInt("idplayer") > 0);
            }
        }
    }

    @Test
    void query_explainAnalyze_showsExecutionPlan() throws Exception {
        try (Connection conn = new JdbcConnectionProvider().getConnection();
             PreparedStatement ps = conn.prepareStatement(EXPLAIN_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { LOG.info("{}", rs.getString(1)); }
        }
    }
}
```

**Règles IT tests :**
- Pas de Mockito — connexion réelle à MySQL via `JdbcConnectionProvider`
- `QUERY` → tests de données (assertions sur colonnes/valeurs)
- `EXPLAIN_QUERY` → test diagnostique plan d'exécution (log uniquement, pas d'assertions)
- La constante SQL dans la liste de production doit être `public static final`
- Pas de DELETE sans `conn.setAutoCommit(false)` + `conn.rollback()` dans le catch

### ✅ SQL — Bonnes pratiques

#### ❌ YEAR()/MONTH() bloquent les index
```sql
-- ❌ Les fonctions sur la colonne empêchent l'utilisation d'un index
WHERE YEAR(SubscriptionEndDate) = YEAR(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))
  AND MONTH(SubscriptionEndDate) = MONTH(DATE_ADD(CURRENT_DATE(), INTERVAL 1 MONTH))

-- ✅ Condition de plage — permet l'utilisation d'un index sur la colonne
WHERE SubscriptionEndDate >= DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 1 MONTH)
  AND SubscriptionEndDate <  DATE_ADD(DATE_SUB(CURDATE(), INTERVAL DAY(CURDATE())-1 DAY), INTERVAL 2 MONTH)
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
| MongoDB Driver Sync 5.6 | MongoDB integration |
| iCal4j 4.2 | iCal generation |
| OpenPDF 3 | PDF generation |
| Google Maps Services | Geocoding |
| Google Cloud Translate | Translation API |
| Quartz 2.5 | Job scheduling |
| MaxMind GeoIP2 | IP geolocation |
| Log4j2 | Logging (config: `src/main/resources/log4j2.xml`) |

---

## 💬 Useful Commands for Migration
```bash
# Find all files still using Connection parameter
grep -r "Connection conn" src/main/java/

# Find all manual service instantiations
grep -r "new lists\." src/main/java/
grep -r "new create\." src/main/java/
grep -r "new read\." src/main/java/

# Find all uses of DBConnection
grep -r "DBConnection" src/main/java/

# Find static getProperty calls (Settings)
grep -r "Settings.getProperty" src/main/java/

# Find static setListe calls (cache invalidation)
grep -r "setListe(null)" src/main/java/

# Find System.exit
grep -r "System.exit" src/main/java/

# Count files remaining to migrate
find src/ -name "*.java" -exec grep -l "Connection conn" {} \; | wc -l

# Compile after changes
mvn clean compile

# Run tests
mvn test -Pfast-ut
```

---

## 📊 Migration Metrics

**Target:** Zero usage of `connection_package.DBConnection` in production code

**Check before considering migration complete:**
1. ✅ No `import connection_package.DBConnection` anywhere
2. ✅ No `new Service().method(conn)` in Controllers/Managers
3. ✅ All services have `@ApplicationScoped` (or appropriate scope)
4. ✅ All services have `@Resource DataSource`
5. ✅ All lists have early return + `invalidateCache()`
6. ✅ No `Settings.getProperty()` static calls
7. ✅ No `System.exit()` anywhere
8. ✅ Application deploys successfully to WildFly
9. ✅ Integration tests pass

---

**Version:** 3.1 (Payment architecture, CacheInvalidator, SelectionPurpose, XHTML patterns)
**Last Updated:** 2026-03-30
**Maintainer:** GolfLC Team