package integration.create;

import create.CreateClub;
import entite.Club;
import integration.support.AbstractDaoIT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class ClubUpsertIT extends AbstractDaoIT {

    private static final int TEST_CLUB_ID = 9998;

    @Test
    void upsertClub_realDB_insertOrUpdate() throws Exception {

        CreateClub createClub = new CreateClub();
        injectDao(createClub);

        dao.execute("""
            DELETE FROM club
            WHERE idclub = ?
            """, TEST_CLUB_ID);

        Club club = new Club();
        club.setIdclub(TEST_CLUB_ID);
        club.setClubName("Test Club IT");
        club.setClubWebsite("https://test-club.be");
        club.setClubLocalAdmin(324713);

        club.getAddress().setStreet("1 Rue de Test");
        club.getAddress().setCity("Bruxelles");
        club.getAddress().setZipCode("1000");
        club.getAddress().setZoneId("Europe/Brussels");
        club.getAddress().getCountry().setCode("BE");
        club.getAddress().getLatLng().setLat(50.8503);
        club.getAddress().getLatLng().setLng(4.3517);

        boolean result = createClub.upsert(club);

        assertTrue(result);

        Integer count = dao.querySingle(
                """
                SELECT COUNT(*)
                FROM club
                WHERE idclub = ?
                """,
                rs -> rs.getInt(1),
                TEST_CLUB_ID
        );

        assertEquals(1, count);
    } // end method

} // end class
