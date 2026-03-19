package integration.read;

import jakarta.inject.Inject;
import entite.Club;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import read.ReadClub;


@EnableWeld
public class ReadClub2IT {

    @Inject
    ReadClub readClub;  // Injection CDI via constructeur @TestDB

    @Test
    void testReadClub() throws Exception {
        // Création d’un club avec ID connu
        Club c = new Club();
        c.setIdclub(154);

        // Lecture via ReadClub2 (utilise ConnectionProvider injecté par Weld)
        Club club = readClub.read(c);

        // Vérification JUnit
        assertNotNull(club, "Club should be loaded");
        System.out.println("Club chargé : " + club.getClubName());
    }
}
