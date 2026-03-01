package create;

import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.ValidationsLC.ValidationStatus;
import find.FindCotisationAtRoundDate;
import find.FindGreenfeePaid;
import find.FindInscriptionRound;
import lists.RoundPlayersList;
import mail.InscriptionMail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CreateInscriptionTest {

    private CreateInscription createInscription;

    // Mocks JDBC
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private PreparedStatement psCountColumns;
    private ResultSet rsCountColumns;
    private Statement stGeneratedKey;
    private ResultSet rsGeneratedKey;

    // Mocks services injectés
    private FindInscriptionRound findInscriptionRound;
    private FindCotisationAtRoundDate findCotisationAtRoundDate;
    private FindGreenfeePaid findGreenfeePaid;
    private RoundPlayersList roundPlayersList;
    private InscriptionMail inscriptionMail;

    // Entités de test (= ce que faisait le main())
    private Round round;
    private Player player;
    private Player invitedBy;
    private Club club;
    private Course course;
    private Inscription inscription;

    @BeforeEach
    void setUp() throws Exception {
        // Créer les mocks
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        psCountColumns = mock(PreparedStatement.class);
        rsCountColumns = mock(ResultSet.class);
        stGeneratedKey = mock(Statement.class);
        rsGeneratedKey = mock(ResultSet.class);
        findInscriptionRound = mock(FindInscriptionRound.class);
        findCotisationAtRoundDate = mock(FindCotisationAtRoundDate.class);
        findGreenfeePaid = mock(FindGreenfeePaid.class);
        roundPlayersList = mock(RoundPlayersList.class);
        inscriptionMail = mock(InscriptionMail.class);

        // Instancier le service et injecter les mocks via réflexion
        createInscription = new CreateInscription();
        injectField("dataSource", dataSource);
        injectField("findInscriptionRound", findInscriptionRound);
        injectField("findCotisationAtRoundDate", findCotisationAtRoundDate);
        injectField("findGreenfeePaid", findGreenfeePaid);
        injectField("roundPlayersList", roundPlayersList);
        injectField("inscriptionMail", inscriptionMail);

        // JDBC par défaut
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn("golflc");

        // Mock DBMeta.CountColumns — query sur information_schema retourne 10 colonnes
        when(conn.prepareStatement(argThat(q -> q != null && q.contains("information_schema"))))
                .thenReturn(psCountColumns);
        when(psCountColumns.executeQuery()).thenReturn(rsCountColumns);
        when(rsCountColumns.next()).thenReturn(true);
        when(rsCountColumns.getInt(1)).thenReturn(10);  // 10 colonnes dans player_has_round

        // Mock pour l'INSERT (toute autre requête)
        when(conn.prepareStatement(argThat(q -> q != null && !q.contains("information_schema"))))
                .thenReturn(ps);

        // Mock LCUtil.generatedKey — conn.createStatement() → SELECT LAST_INSERT_ID()
        when(conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                .thenReturn(stGeneratedKey);
        when(stGeneratedKey.executeQuery(anyString())).thenReturn(rsGeneratedKey);
        when(rsGeneratedKey.next()).thenReturn(true);
        when(rsGeneratedKey.getInt(1)).thenReturn(99999);  // ID généré simulé

        // Entités de test (= ancien main())
        player = new Player();
        player.setIdplayer(324714);
        player.setPlayerRole("ADMIN");
        invitedBy = player;

        round = new Round();
        round.setIdround(435);

        club = new Club();
        club.setIdclub(1135);

        course = new Course();
        course.setIdcourse(135);

        inscription = new Inscription();
        inscription.setInscriptionTeeStart("YELLOW / 154");
        inscription.setInscriptionMatchplayTeam("A");
    }

    /**
     * Cas 1 : Inscription réussie — joueur ADMIN, pas de doublon
     * (= le scénario du main() commenté)
     */
    @Test
    void create_adminPlayer_insertsSuccessfully() throws Exception {
        // Arrange — ADMIN bypass les validations cotisation/greenfee
        when(roundPlayersList.list(round)).thenReturn(Collections.emptyList());
        when(findInscriptionRound.find(round, player)).thenReturn(false);
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "A");

        // Assert
        assertNotNull(result);
        assertFalse(result.isInscriptionError());
        assertEquals("00", result.getErrorStatus());
        verify(ps).executeUpdate();
        verify(inscriptionMail).create(player, invitedBy, round, club, course);
    }

    /**
     * Cas 2 : Inscription sans envoi de mail (batch != "A")
     */
    @Test
    void create_batchNotA_noMailSent() throws Exception {
        // Arrange
        when(roundPlayersList.list(round)).thenReturn(Collections.emptyList());
        when(findInscriptionRound.find(round, player)).thenReturn(false);
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "B");

        // Assert
        assertFalse(result.isInscriptionError());
        assertEquals("00", result.getErrorStatus());
        verify(inscriptionMail, never()).create(any(), any(), any(), any(), any());
    }

    /**
     * Cas 3 : Joueur déjà inscrit — erreur 04
     */
    @Test
    void create_duplicateInscription_returnsError04() throws Exception {
        // Arrange — joueur non-ADMIN, findInscriptionRound retourne true (déjà inscrit)
        player.setPlayerRole("PLAYER");
        when(roundPlayersList.list(round)).thenReturn(Collections.emptyList());
        when(findInscriptionRound.find(round, player)).thenReturn(true);

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "A");

        // Assert
        assertTrue(result.isInscriptionError());
        assertEquals("04", result.getErrorStatus());
        verify(ps, never()).executeUpdate();  // pas d'INSERT
    }

    /**
     * Cas 4 : Trop de joueurs inscrits (>3) — rejeté, erreur 01
     */
    @Test
    void create_tooManyPlayers_returnsError01() throws Exception {
        // Arrange — 4 joueurs déjà inscrits
        Player p1 = new Player(); p1.setIdplayer(1); p1.setPlayerLastName("A");
        Player p2 = new Player(); p2.setIdplayer(2); p2.setPlayerLastName("B");
        Player p3 = new Player(); p3.setIdplayer(3); p3.setPlayerLastName("C");
        Player p4 = new Player(); p4.setIdplayer(4); p4.setPlayerLastName("D");
        when(roundPlayersList.list(round)).thenReturn(List.of(p1, p2, p3, p4));

        player.setPlayerRole("PLAYER"); // pas ADMIN

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "A");

        // Assert
        assertTrue(result.isInscriptionError());
        assertEquals("01", result.getErrorStatus());
        verify(ps, never()).executeUpdate();
    }

    /**
     * Cas 5 : Duplicate SQL (erreur MySQL 1062) — erreur 98
     */
    @Test
    void create_sqlDuplicate1062_returnsError98() throws Exception {
        // Arrange — validation OK mais INSERT échoue avec duplicate
        when(roundPlayersList.list(round)).thenReturn(Collections.emptyList());
        when(findInscriptionRound.find(round, player)).thenReturn(false);
        when(ps.executeUpdate()).thenThrow(new SQLException("Duplicate entry", "23000", 1062));

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "A");

        // Assert
        assertTrue(result.isInscriptionError());
        assertEquals("98", result.getErrorStatus());
    }

    /**
     * Cas 6 : Joueur membre avec cotisation valide — inscription OK
     */
    @Test
    void create_memberWithValidSubscription_insertsSuccessfully() throws Exception {
        // Arrange — joueur non-ADMIN mais avec cotisation "Y"
        player.setPlayerRole("PLAYER");
        Cotisation cotisation = new Cotisation();
        cotisation.setStatus("Y");

        when(roundPlayersList.list(round)).thenReturn(Collections.emptyList());
        when(findInscriptionRound.find(round, player)).thenReturn(false);
        when(findCotisationAtRoundDate.find(player, club, round)).thenReturn(cotisation);
        when(findGreenfeePaid.find(player, round)).thenReturn(false);
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        Inscription result = createInscription.create(round, player, invitedBy, inscription, club, course, "A");

        // Assert
        assertFalse(result.isInscriptionError());
        assertEquals("00", result.getErrorStatus());
        verify(ps).executeUpdate();
    }

    // === Méthode utilitaire pour injecter les mocks via réflexion ===

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = CreateInscription.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(createInscription, value);
    } // end method

} // end class
