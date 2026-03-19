package dao;

//import connection_package.DatasourceMonitor.DatasourceStats;
import connection_package.DatasourceStats;
import connection_package.SimpleJdbcConnectionProducer;
import entite.Club;
import static interfaces.Log.LOG;


/**
 * Test manuel de ClubDAOImpl sans CDI
 */
public class TestClubDAOMain {

public static void main(String[] args) {
    LOG.debug("entering main of TestClubDAOImpl");
 
 try {
     
     /*Pourquoi ça arrive
exec:java lance ton main dans une JVM séparée, sans le runtime WildFly.
Les classes comme org.jboss.as.controller.client.ModelControllerClient sont fournies par WildFly modules, pas par ton projet Maven.
Résultat : ClassNotFoundException.
Solutions
1️⃣ Ajouter la dépendance JBoss AS Controller Client dans ton pom.xml (test hors serveur)
<dependency>
    <groupId>org.jboss.as</groupId>
    <artifactId>jboss-as-controller-client</artifactId>
    <version>7.1.1.Final</version> <!-- adapter à ta version WildFly -->
</dependency>
     */

 //  DatasourceStats stats = new DatasourceStats();
 //       LOG.debug("stats = " + stats.toString());
 //  int before = stats.getActiveCount();
//        LOG.debug("Active connections BEFORE = " + before);
    ClubDAOImpl clubDAO = new ClubDAOImpl();
        // 1️⃣ Connexion manuelle à la base
    clubDAO.setConnectionProviderManually(new SimpleJdbcConnectionProducer());

    Club newClub = new Club();
    newClub.setClubName("Club Test DAO");
    newClub.setClubWebsite("https://testdao.com");

            boolean created = clubDAO.create(newClub);
            LOG.debug("CREATE result: " + created + ", ID = " + newClub.getIdclub());
// try{
            Club readClub = clubDAO.read(newClub);
            LOG.debug("READ result: " + readClub);

            readClub.setClubWebsite("https://updated-testdao.com");
            boolean updated = clubDAO.update(readClub);
            LOG.debug("UPDATE result: " + updated);

            Club updatedClub = clubDAO.read(readClub);
            LOG.debug("READ after UPDATE: " + updatedClub);

            boolean deleted = clubDAO.delete(updatedClub);
            LOG.debug("DELETE result: " + deleted);

 //    int after = stats.getActiveCount();
  //   LOG.debug("Active connections BEFORE = " + after);
    
            
        } catch (Exception e) {
            LOG.error("Exception pendant le test de ClubDAOImpl", e);
    }
}
} //end class
