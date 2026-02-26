
package dao;
/*
import entite.Club;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClubDAOTestJunit {

    @Inject
    private ClubDAO clubDAO;

    @Test
    public void testCreateReadUpdateDelete() throws Exception {
        Club club = new Club();
        club.setClubName("JUnit Club");
        club.setClubWebsite("https://junitclub.com");
        club.setClubLocalAdmin(1);

        // --- CREATE ---
        assertTrue(clubDAO.create(club));
        assertNotNull(club.getIdclub());

        // --- READ ---
        Club readClub = clubDAO.read(club);
        assertEquals("JUnit Club", readClub.getClubName());

        // --- UPDATE ---
        readClub.setClubWebsite("https://junit-updated.com");
        assertTrue(clubDAO.update(readClub));
        Club updatedClub = clubDAO.read(readClub);
        assertEquals("https://junit-updated.com", updatedClub.getClubWebsite());

        // --- DELETE ---
        assertTrue(clubDAO.delete(updatedClub));
        assertNull(clubDAO.read(updatedClub));
    }
}
*/