package aa_test;

import dao.ClubService;
import dao.ClubService;
import entite.Club;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/*
@EnableAutoWeld
public class ClubServiceCdiTest {

    @Inject
    private ClubService clubService; // Bean CDI injecté

    @Test
    void testCreateReadUpdateDeleteClub() throws Exception {

        // -------------------
        // 1️⃣ CREATE
        // -------------------
        Club newClub = new Club();
        newClub.setClubName("Club CDI Test");
        newClub.setClubWebsite("https://cditest.com");

        boolean created = clubService.createClub(newClub);
        assertTrue(created, "Le club doit être créé");
        assertNotNull(newClub.getIdclub(), "L'ID du club doit être généré");

        // -------------------
        // 2️⃣ READ
        // -------------------
        Club readClub = clubService.getClubById(newClub.getIdclub());
        assertNotNull(readClub, "Le club doit exister après création");
        assertEquals("Club CDI Test", readClub.getClubName());

        // -------------------
        // 3️⃣ UPDATE
        // -------------------
        readClub.setClubWebsite("https://cditest-updated.com");
        boolean updated = clubService.updateClub(readClub);
        assertTrue(updated, "Le club doit être mis à jour");

        Club updatedClub = clubService.getClubById(readClub.getIdclub());
        assertEquals("https://cditest-updated.com", updatedClub.getClubWebsite());

        // -------------------
        // 4️⃣ DELETE
        // -------------------
        boolean deleted = clubService.deleteClub(updatedClub.getIdclub());
        assertTrue(deleted, "Le club doit être supprimé");

        Club deletedClub = clubService.getClubById(updatedClub.getIdclub());
        assertNull(deletedClub, "Le club doit être introuvable après suppression");
    }
}
*/