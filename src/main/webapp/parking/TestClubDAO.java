
package dao;

import entite.Address;
import entite.Club;
import entite.Country;
import entite.LatLng;
import connection_package.DBConnection;
import static interfaces.Log.LOG;

import java.sql.Connection;

public class TestClubDAO {

    public static void main(String[] args) {

        // Connexion à la base
        try (Connection conn = new DBConnection().getConnection()) {

            ClubDAO clubDAO = new ClubDAOImpl(conn);
            LOG.debug("entering main");
            // -------------------
            // 1️⃣ CREATE
            // -------------------
            Club newClub = new Club();
                 Country country = new Country();
        country.setCode("BE");
       LatLng latlng = new LatLng();
        latlng.setLat(50.8262290);
        latlng.setLng(4.3571460);
       Address address = new Address();
        address.setCity("Brussels");
        address.setZipCode("B-1060");
        address.setCountry(country);
        address.setStreet("Rue de l'Amazone 55");
        address.setLatLng(latlng);
        address.setZipCode("1060");
     //  Club club = new Club();
        newClub.setClubName("Club de test");
        newClub.setAddress(address);
        newClub.setClubLocalAdmin(324713);
        newClub.setClubWebsite("https://golf-empereur.com/");

 // OK           boolean created = clubDAO.create(newClub, conn);
   //         LOG.debug("CREATE result: " + created + ", new Club ID = " + newClub.getIdclub());

            // -------------------
            // 2️⃣ READ
            // -------------------
            newClub.setIdclub(105); // kampenhout
            Club readClub = clubDAO.read(newClub);
            if (readClub != null) {
                LOG.debug("READ Club = " + readClub);
            } else {
                LOG.debug("READ failed, club not found");
            }

            // -------------------
            // 3️⃣ UPDATE
            // -------------------
            readClub.setClubWebsite("www.golfdao-updated.com");
            
            boolean updated = clubDAO.update(readClub);
            LOG.debug("UPDATE result: " + updated);

            // Vérification après update
            Club updatedClub = clubDAO.read(readClub);
            LOG.debug("READ after UPDATE: " + updatedClub);

            // -------------------
            // 4️⃣ DELETE (simple)
            // -------------------
     //       boolean deleted = clubDAO.delete(updatedClub, conn);
     //       LOG.debug("DELETE result: " + deleted);

            // -------------------
            // 5️⃣ DELETE with cascade
            // -------------------
            // On recrée le club pour tester le cascade
     //       clubDAO.create(newClub, conn);
     //       boolean cascadeDeleted = clubDAO.deleteClubAndChilds(newClub, conn);
     //       LOG.debug("DELETE with cascade result: " + cascadeDeleted);

        } catch (Exception e) {
            LOG.error("Exception in TestClubDAO: " + e.getMessage(), e);
        }
    }
}
