
package aa_test;


import connection_package.DatasourceStats;
import dao.CdiSanityChecker;
import dao.ClubService;
import entite.Club;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class ClubServiceCDITest {

    @Inject
    private ClubService clubService;
    
    @Deployment
public static WebArchive createDeployment() {
    LOG.debug("Entering createDeployment");
try{
    // 1️⃣ Crée le WAR de test
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml") // Active CDI
            .addPackages(true, "dao", "entite", "validator", "interfaces", "utils", "exceptions","connection_package"); // Code applicatif

// 1️⃣ Dépendances POM
    File[] pomDeps = Maven.resolver()
        .loadPomFromFile("pom.xml")
        .importRuntimeAndTestDependencies()
        .resolve()
        .withTransitivity()
        .asFile();

        LOG.debug("Dépendances POM - pomDeps = " + pomDeps.length);

// 2️⃣ Dépendances spécifiques (si pas dans le pom je pense queç a ne sert à rien !!
    File[] extraDeps = Maven.resolver()
        .resolve(
                    "org.primefaces:primefaces:15.0.10" //,   faut la version !! bien nécessaire ?
              //      "org.jboss.weld.se:weld-se-core:6.0.0.Final", // ✅ Weld pour tests
               //     "jakarta.annotation:jakarta.annotation-api:2.1.1" // utile pour @PostConstruct etc.
                )
        .withTransitivity()
        .asFile();
        LOG.debug("Dépendences spécifiques - extraDeps = " + extraDeps.length);
  
// 3️⃣ Fusionner et ajouter au WAR
    File[] libs = Stream.concat(Arrays.stream(pomDeps), Arrays.stream(extraDeps))
        .filter(f -> !isProvidedByWildFly(f))
        .toArray(File[]::new);
    
      LOG.debug("Dépendances fusionnées - libs = " + libs.length);

// Filtrage pour ne pas inclure Weld ou Jakarta EE fourni par WildFly
    File[] filteredLibs = Arrays.stream(libs)
            .filter(f -> !isProvidedByWildFly(f))
            .toArray(File[]::new);

        LOG.debug("added librairies = " + filteredLibs.length);

        /*/ suggestion claude 
        File[] libs = Stream.concat(
        Arrays.stream(pomDeps), 
        Arrays.stream(extraDeps))
    .filter(f -> !isProvidedByWildFly(f))
    .toArray(File[]::new);

war.addAsLibraries(libs);  // Directement
auditCdiJars(libs);
        */
        
   war.addAsLibraries(filteredLibs);
   auditCdiJars(filteredLibs);
  return war;
} catch (Exception e) {
        LOG.error("Exception in ClubServiceITest: " + e.getMessage(), e);
        throw new RuntimeException(e); // c'est un return ⚠ Ne pas retourner null
  }
} //end method

private static boolean isProvidedByWildFly(File file) {
    String n = file.getName().toLowerCase();
    return
           // n.contains("weld") || Note : on ne filtre plus “weld” dans les tests SE, sinon DefinitionException ne sera pas trouvé.
           n.contains("jakarta")
            || n.contains("javax")
            || n.contains("jboss")
            || n.contains("wildfly");
}

@Inject
CdiSanityChecker checker;

@Test
void cdi_should_resolve_core_beans() {
    assertTrue(checker.isBeanAvailable(ClubService.class),
            "ClubService n'est pas un bean CDI");

    assertTrue(checker.isBeanResolvable(ClubService.class),
            "ClubService ambigu ou non résoluble");

    assertEquals(1, checker.countBeans(ClubService.class),
            "Il doit y avoir exactement un ClubService");
}


@Test
void testInjection() {
    assertNotNull(clubService, "ClubService CDI injection failed");
}

@Test
void testCreateAndReadClub() throws Exception {
    
            LOG.debug("entering testCreateAndReadClub ");
            DatasourceStats stats = new DatasourceStats();
     int before = stats.getActiveCount();
      LOG.debug("Active connections BEFORE = " + before);
        Club club = new Club();
        club.setClubName("Club Arquillian");
        club.setClubWebsite("https://arquillian.test");

        int before2 = stats.getActiveCount();
        boolean created = clubService.createClub(club);
        int after = stats.getActiveCount();
        assertTrue(created);
        assertNotNull(club.getIdclub());
        assertEquals(before, after, "Fuite de connexion détectée"); //bel exemple !

        Club loaded = clubService.ReadClub(club);
        assertNotNull(loaded);
        assertEquals("Club Arquillian", loaded.getClubName());
        
        
        
    }

@Test
void encodingCheck() {
    LOG.debug("UTF-8 test : é è à ç ü €");
    System.out.println("UTF-8 stdout : é è à ç ü €");
}


    private static void auditCdiJars(File[] jars) {
LOG.debug("=== CDI Audit Report ===");
        for (File jar : jars) {
            try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jar)) {
                java.util.jar.Manifest manifest = jf.getManifest();
                if (manifest != null) {
                    java.util.jar.Attributes attrs = manifest.getMainAttributes();
                    String version = attrs.getValue("Implementation-Version");
                    String title = attrs.getValue("Implementation-Title");
                    System.out.printf("%s | Version: %s | Title: %s%n",
                            jar.getName(),
                            version != null ? version : "N/A",
                            title != null ? title : "N/A");
                }
            } catch (Exception e) {
                LOG.error("Cannot read manifest for JAR: " + jar);
            }
        }
       LOG.debug("========================");
    }
} //end Class
/*
 * Filtrage simple pour éviter les JAR fournis par WildFly
 */


    
    
/*
@Deployment
public static WebArchive createDeployment() throws Exception {

    // --- 1. Crée le WAR de test avec beans.xml ---
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    // --- 2. Ajouter automatiquement tous les packages utilisés par les tests ---
    Set<Class<?>> testClasses = getTestClasses();
    Set<String> packages = testClasses.stream()
            .map(c -> c.getPackageName())
            .collect(Collectors.toSet());
    packages.forEach(pkg -> war.addPackages(true, pkg));
    File[] allDeps = Maven.resolver()
    .loadPomFromFile("pom.xml")
    .importRuntimeAndTestDependencies() // inclut les dépendances runtime + test
    .resolve()
    .withTransitivity()
    .asFile();
    // --- 4. Filtrer les JAR fournis par WildFly ---
    File[] libs = Arrays.stream(allDeps)
            .filter(f -> !isProvidedByWildFly(f))
            .toArray(File[]::new);

    // --- 5. Ajouter les bibliothèques filtrées au WAR ---
    war.addAsLibraries(libs);
    return war;
}
*/
    /*
@Deployment  // indispensable ete un seul dans la class
public static WebArchive createDeployment() {
    LOG.debug("entering createDeployment");
    return ShrinkWrap.create(WebArchive.class, "test.war")
            // Active CDI
             .loadPomFromFile("pom.xml")        
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
         //   .addClass(validator.FirstUpperConstraint.class)
        //    .addPackages(true, "GolfJakarta")
            // Code applicatif
            .addPackages(true,"dao","entite","validator","interfaces")
                 //   ClubService.class.getPackage(),
                 //   Club.class.getPackage()
            .addAsLibraries(
                Maven.resolver()
                .resolve("org.primefaces:primefaces:14.0.10")
                .withTransitivity()
                .asFile()
            );
} // end method*/
    /*
@Deployment  // indispensable ete un seul dans la class
public static WebArchive createDeployment() {
    LOG.debug("entering createDeployment");

    // 1️⃣ Crée le WAR
    WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
            // Active CDI
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            // Code applicatif
            .addPackages(true, "dao", "entite", "validator", "interfaces", "utils");

    // 2️⃣ Résoudre les dépendances Maven depuis le pom
    File[] libs = Maven.resolver()
            .loadPomFromFile("pom.xml")                // <-- ici tu charges le pom
            .importRuntimeAndTestDependencies()       // inclut runtime + test
            //.resolve()
            .resolve("org.primefaces:primefaces")  // cherche la version dans le pom
            .withTransitivity()
            .asFile();

    // 3️⃣ Ajouter ces dépendances au WAR
    war.addAsLibraries(libs);

    return war;
}
*/

/*.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
 * Exemple simple : récupère toutes les classes de tests dans ce package
 * Tu peux adapter pour scanner automatiquement ton dossier /test

private static Set<Class<?>> getTestClasses() throws ClassNotFoundException {
    return Set.of(
            Class.forName("aa_test.ClubServiceITest")
            // ajouter d'autres tests si nécessaire
    );
}
 */
/**
 * Filtrage simple pour éviter les JAR fournis par WildFly

private static boolean isProvidedByWildFly(File file) {
    String name = file.getName().toLowerCase();
    return name.contains("wildfly") || name.contains("jboss");
}
 */