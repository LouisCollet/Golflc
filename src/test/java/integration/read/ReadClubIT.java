
package integration.read;

import static org.junit.jupiter.api.Assertions.*; // Importe toutes les assertions statiques de JUnit 5, comme assertNotNull, assertEquals, etc.
import org.jboss.weld.junit5.EnableWeld; /*
    Annotation spécifique à Weld JUnit 5.
    Permet de démarrer un conteneur CDI Weld pour le test.
    Active l’injection CDI dans le contexte de JUnit.
    */
import org.junit.jupiter.api.Test; // Import pour l’annotation @Test de JUnit 5, qui marque une méthode comme test exécutable.
//import connection_package.ConnectionProducer;
import entite.Club;
import static interfaces.Log.LOG;
import read.ReadClub;
import java.sql.Connection;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;

/*
ConnectionProducer → bean CDI qui fournit une connection JDBC à MySQL.
Club → entité / DTO représentant un club.
ReadClub → classe DAO qui contient la méthode read() pour récupérer un club depuis la base.
Connection → classe JDBC standard pour gérer la connexion SQL.
jakarta.inject.Inject → injection CDI pour obtenir des instances des beans dans le test.
*/

@EnableWeld
/*
Active Weld pour ce test JUnit 5.
Weld initialise un mini conteneur CDI et gère l’injection des beans annotés avec @Inject.
Grâce à ça, readClub et connectionProducer sont injectés automatiquement.

// @Tag("integration") // Junit 5 Sans ce tag, le test ne sera pas reconnu par failsafe si tu gardes cette configuration.
    // correspond à <groups>integration</groups> de maven-failsafe-plugin
    //Maven Failsafe exécutera uniquement les tests portant ce tag.
*/
public class ReadClubIT { // Convention : suffixer par IT pour indiquer que c’est un test d’intégration.
    // Weld démarre le conteneur CDI pour le test
    @Inject
        /*
        Injection CDI de l’objet ReadClub.
        Le test n’a pas besoin de créer manuellement l’instance avec new ReadClub().
        Weld s’assure que toutes les dépendances internes de ReadClub sont injectées correctemen
            */
    ReadClub readClub;  // ??

  //  @Inject
            /*
        Injection CDI du producer de connexions JDBC.
        Permet de récupérer une connexion MySQL directement depuis le conteneur CDI, sans instancier ConnectionProducer manuellement.
            */
 ///   ConnectionProducer connectionProducer;

    @Test
            /*
        Début de la méthode de test.
        Annotation @Test → JUnit 5 exécutera cette méthode comme un test.
        throws Exception → simplifie la gestion des exceptions dans le test (pas besoin de try/catch partout).
            */
    void testReadClub() throws Exception {
        // récupérer la connexion MySQL via le producer CDI
  //  à corriger merde !    try (Connection conn = connectionProducer.getConnection()) { //  obtient la connexion MySQL injectée par CDI.
  //          Club c = new Club();
  //          c.setIdclub(154);

   //         Club club = readClub.read(c, conn); // méthode adaptée pour recevoir la connection
    //        assertNotNull(club);
    //        LOG.debug("Club chargé : " + club.getClubName());
    //    }
    }
} // end



/*
@EnableAutoWeld
@Tag("integration")
class ReadClubIT {

    @Inject
    ConnectionProvider connectionProvider;

    @Test
    void shouldReadClubFromMySQL() throws Exception {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false); // protection DB

            Club input = new Club();
            input.setIdclub(154); // ID réel EXISTANT

            Club result = new read.ReadClub().read(input, conn);

            assertNotNull(result);
            assertEquals(154, result.getIdclub());
            assertNotNull(result.getClubName());

            conn.rollback(); // sécurité absolue
        }
    }
}
*/