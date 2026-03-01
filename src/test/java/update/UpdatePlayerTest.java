package update;

import entite.LatLng;
import entite.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class UpdatePlayerTest {

    private UpdatePlayer updatePlayer;

    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private DatabaseMetaData meta;
    private ResultSet rsColumns;

    private Player player;

    // Colonnes de la table player (dans l'ordre DB)
    // La première est skippée (idplayer = PK), les blacklisted aussi
    private static final List<String> PLAYER_COLUMNS = List.of(
            "idplayer",                  // skippé (première colonne)
            "playerfirstname",           // SET
            "playerlastname",            // SET
            "playerstreet",              // SET
            "playercity",               // SET
            "playercountry",            // SET
            "playerbirthdate",          // SET
            "playergender",             // SET
            "playerhomeclub",           // SET
            "playerlanguage",           // SET
            "playeremail",              // SET
            "playerzoneid",             // SET
            "playerlatlng",             // SET
            "playerrole",               // SET
            "playerphotolocation",      // blacklisté
            "playeractivation",         // blacklisté
            "playerpassword",           // blacklisté
            "playerpreviouspasswords",  // blacklisté
            "playermodificationdate"    // blacklisté (contient "modificationdate")
    );

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        meta = mock(DatabaseMetaData.class);
        rsColumns = mock(ResultSet.class);

        updatePlayer = new UpdatePlayer();
        Field dsField = UpdatePlayer.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        dsField.set(updatePlayer, dataSource);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn("golflc");

        // Mock DatabaseMetaData.getColumns — simule les colonnes de la table player
        when(conn.getMetaData()).thenReturn(meta);
        when(meta.getColumns(eq("golflc"), isNull(), eq("player"), isNull()))
                .thenReturn(rsColumns);

        // Simuler l'itération sur les colonnes : next() retourne true N fois puis false
        Boolean[] nexts = new Boolean[PLAYER_COLUMNS.size() + 1];
        for (int i = 0; i < PLAYER_COLUMNS.size(); i++) { nexts[i] = true; }
        nexts[PLAYER_COLUMNS.size()] = false;
        when(rsColumns.next()).thenReturn(nexts[0],
                java.util.Arrays.copyOfRange(nexts, 1, nexts.length));

        // Pour chaque appel getString("COLUMN_NAME"), retourner le nom de colonne correspondant
        String[] colNames = PLAYER_COLUMNS.toArray(new String[0]);
        when(rsColumns.getString("COLUMN_NAME"))
                .thenReturn(colNames[0],
                        java.util.Arrays.copyOfRange(colNames, 1, colNames.length));

        // Mock pour l'UPDATE PreparedStatement
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Entité de test
        player = new Player();
        player.setIdplayer(678905);
        player.setPlayerFirstName("first test");
        player.setPlayerLastName("last test");
        player.setPlayerBirthDate(LocalDateTime.parse("2018-11-03T12:45:30"));
        player.getAddress().setZoneId("Europe/Brussels");
        player.setPlayerHomeClub(101);
        player.getAddress().setCity("Brussels");
        player.getAddress().setStreet("Rue Test 1");
        player.setPlayerGender("M");
        player.setPlayerLanguage("es");
        player.getAddress().getCountry().setCode("US");
        player.getAddress().setLatLng(new LatLng(50.8262271, 4.3571382));
        player.setPlayerEmail("test@test.com");
        player.setPlayerRole("PLAYER");
    }

    /**
     * Cas 1 : Update réussi — commit
     */
    @Test
    void update_success_returnsTrue() throws Exception {
        when(ps.executeUpdate()).thenReturn(1);

        boolean result = updatePlayer.update(player);

        assertTrue(result);
        verify(conn).setAutoCommit(false);
        verify(conn).commit();
        verify(conn, never()).rollback();
        verify(ps).executeUpdate();

        // Vérifie que le WHERE contient l'idplayer (dernier paramètre = 14)
        verify(ps).setInt(14, 678905);
    }

    /**
     * Cas 2 : executeUpdate retourne 0 — rollback + SQLException
     */
    @Test
    void update_noRowUpdated_rollbackAndThrows() throws Exception {
        when(ps.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class, () -> updatePlayer.update(player));
        verify(conn).rollback();
        verify(conn, never()).commit();
    }

    /**
     * Cas 3 : SQLException pendant l'exécution — propage l'exception
     */
    @Test
    void update_sqlException_throws() throws Exception {
        when(ps.executeUpdate()).thenThrow(
                new SQLException("Deadlock found", "40001", 1213));

        assertThrows(SQLException.class, () -> updatePlayer.update(player));
    }

    /**
     * Cas 4 : Connexion échoue — propage l'exception
     */
    @Test
    void update_connectionFails_throws() throws Exception {
        when(dataSource.getConnection()).thenThrow(
                new SQLException("Connection refused", "08001", 0));

        assertThrows(SQLException.class, () -> updatePlayer.update(player));
        verify(ps, never()).executeUpdate();
    }

    /**
     * Cas 5 : Vérifie que la requête générée contient les bonnes colonnes SET
     */
    @Test
    void update_generatedQuery_containsExpectedColumns() throws Exception {
        when(ps.executeUpdate()).thenReturn(1);

        updatePlayer.update(player);

        // Capture la requête passée à prepareStatement
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(conn).prepareStatement(captor.capture());
        String query = captor.getValue();

        // Vérifie les colonnes SET (non blacklistées)
        assertTrue(query.contains("playerfirstname = ?"), "Missing playerfirstname");
        assertTrue(query.contains("playerlastname = ?"), "Missing playerlastname");
        assertTrue(query.contains("playeremail = ?"), "Missing playeremail");
        assertTrue(query.contains("playergender = ?"), "Missing playergender");

        // Vérifie que les colonnes blacklistées sont absentes
        assertFalse(query.contains("playerpassword"), "playerpassword should be excluded");
        assertFalse(query.contains("playerphotolocation"), "playerphotolocation should be excluded");
        assertFalse(query.contains("playeractivation"), "playeractivation should be excluded");

        // Vérifie la structure UPDATE ... SET ... WHERE
        assertTrue(query.contains("UPDATE player"), "Missing UPDATE player");
        assertTrue(query.contains("WHERE player.idplayer=?"), "Missing WHERE clause");
    }

} // end class
