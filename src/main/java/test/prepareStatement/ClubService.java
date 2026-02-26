
package test.prepareStatement;

import entite.Address;
import entite.Club;
import entite.Country;
import entite.LatLng;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;

@ApplicationScoped
public class ClubService {

    @Inject
        DbExecutor dbExecutor;

    public void save(Club club) throws Exception {
        LOG.debug("entering save with club = " + club);
        LOG.debug("entering save dbExecutor = " + dbExecutor.toString());
        dbExecutor.upsert("club", club);
    }

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
        Club club = new Club();
        club.setClubName("Club de test");
        Address address = new Address();
        address.setCity("Brussels");
        address.setZipCode("B-1060 ");
        Country country = new Country();
        country.setCode("BE");
        address.setCountry(country);
        address.setStreet("Rue de l'Amazone 55");
        LatLng latlng = new LatLng();
        latlng.setLat(50.8262290);
        latlng.setLng(4.3571460);
        address.setLatLng(latlng);
        address.setZipCode("1060");
        club.setAddress(address);
        club.setClubLocalAdmin(324713);
        club.setClubWebsite("https://golf-empereur.com/");
        LOG.debug("starting with club = " + club);
        DbMetadataCache metadata = new DbMetadataCache();
           LOG.debug("metadata = " + metadata.toString());
        PreparedStatement ps = DbUpsertEngine.autoInsert(conn, "club", club, metadata);
           LOG.debug("preparedstatement = " + metadata.toString());
        int rows = ps.executeUpdate();
          LOG.debug("Rows inserted: " + rows);
         // LOG.debug("starting main ClubService with club = " + club);

         save(club);
         //   player.setIdplayer(324713);
         //   boolean b = new create.CreateAudit().create(player, conn);
            LOG.debug("from main, terminated = ");
        } catch (Exception e) {
            String msg = "££ Exception in main ClubService = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//

    
    
    
    
    
}
