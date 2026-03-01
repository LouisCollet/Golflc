package lists;

import entite.Player;
import entite.Round;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RoundPlayersListTest {

    private RoundPlayersList roundPlayersList;

    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    private Round round;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        roundPlayersList = new RoundPlayersList();
        injectField("dataSource", dataSource);
        // S'assurer que le cache est vide avant chaque test
        injectField("liste", null);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        round = new Round();
        round.setIdround(628);

        // Mock findColumn — toutes les colonnes du PlayerRowMapper + Address + LatLng
        // AbstractRowMapper.hasColumn() appelle rs.findColumn(column)
        for (String col : List.of(
                "idplayer", "PlayerFirstName", "PlayerLastName",
                "PlayerBirthDate", "playergender", "playerhomeclub",
                "playerLanguage", "PlayerEmail", "PlayerPhotoLocation",
                "PlayerRole", "PlayerModificationDate",
                "PlayerStreet", "PlayerCity", "PlayerCountry",
                "PlayerZoneId", "PlayerLatLng")) {
            when(rs.findColumn(col)).thenReturn(1);
        }
    }

    /**
     * Configure le mock ResultSet pour simuler un joueur
     */
    private void stubPlayer(int id, String firstName, String lastName) throws Exception {
        when(rs.getInt("idplayer")).thenReturn(id);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("PlayerFirstName")).thenReturn(firstName);
        when(rs.getString("PlayerLastName")).thenReturn(lastName);
        when(rs.getTimestamp("PlayerBirthDate")).thenReturn(Timestamp.valueOf(LocalDateTime.of(1990, 1, 1, 0, 0)));
        when(rs.getString("playergender")).thenReturn("M");
        when(rs.getInt("playerhomeclub")).thenReturn(101);
        when(rs.getString("playerLanguage")).thenReturn("fr");
        when(rs.getString("PlayerEmail")).thenReturn(firstName.toLowerCase() + "@test.com");
        when(rs.getString("PlayerPhotoLocation")).thenReturn(null);
        when(rs.getString("PlayerRole")).thenReturn("PLAYER");
        when(rs.getTimestamp("PlayerModificationDate")).thenReturn(null);
        when(rs.getString("PlayerStreet")).thenReturn("Rue Test");
        when(rs.getString("PlayerCity")).thenReturn("Brussels");
        when(rs.getString("PlayerCountry")).thenReturn("BE");
        when(rs.getString("PlayerZoneId")).thenReturn("Europe/Brussels");
        when(rs.getString("PlayerLatLng")).thenReturn("50.8262271,4.3571382");
    }

    /**
     * Cas 1 : 2 joueurs inscrits (= ancien main() idround=628)
     */
    @Test
    void list_twoPlayers_returnsList() throws Exception {
        // Arrange — 2 rows
        when(rs.next()).thenReturn(true, true, false);
        stubPlayer(324715, "Jean", "Dupont");

        // Act
        List<Player> result = roundPlayersList.list(round);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(ps).setInt(1, 628);
    }

    /**
     * Cas 2 : Aucun joueur inscrit — liste vide
     */
    @Test
    void list_noPlayers_returnsEmptyList() throws Exception {
        when(rs.next()).thenReturn(false);

        List<Player> result = roundPlayersList.list(round);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Cas 3 : Round avec idround null — early return emptyList
     */
    @Test
    void list_roundIdNull_returnsEmptyList() throws Exception {
        Round nullRound = new Round();
        // idround est null par défaut

        List<Player> result = roundPlayersList.list(nullRound);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dataSource, never()).getConnection(); // pas de requête DB
    }

    /**
     * Cas 4 : Cache — deuxième appel retourne le cache sans requête DB
     */
    @Test
    void list_secondCall_returnsCachedList() throws Exception {
        // Premier appel — charge depuis DB
        when(rs.next()).thenReturn(true, false);
        stubPlayer(324715, "Jean", "Dupont");

        List<Player> first = roundPlayersList.list(round);
        assertEquals(1, first.size());

        // Deuxième appel — doit retourner le cache
        List<Player> second = roundPlayersList.list(round);

        assertSame(first, second); // même instance
        verify(dataSource, times(1)).getConnection(); // une seule connexion DB
    }

    /**
     * Cas 5 : invalidateCache + re-appel — recharge depuis DB
     */
    @Test
    void invalidateCache_thenList_reloadsFromDB() throws Exception {
        // Premier appel
        when(rs.next()).thenReturn(true, false);
        stubPlayer(324715, "Jean", "Dupont");
        roundPlayersList.list(round);

        // Invalider le cache
        roundPlayersList.invalidateCache();
        assertNull(roundPlayersList.getListe());

        // Re-appel — doit re-requêter la DB
        when(rs.next()).thenReturn(true, true, false);
        stubPlayer(100, "Paul", "Martin");
        List<Player> reloaded = roundPlayersList.list(round);

        assertEquals(2, reloaded.size());
        verify(dataSource, times(2)).getConnection(); // 2 connexions DB
    }

    /**
     * Cas 6 : SQLException — handleSQLException propage
     */
    @Test
    void list_sqlException_throws() throws Exception {
        when(ps.executeQuery()).thenThrow(
                new SQLException("Connection lost", "08S01", 0));

        assertThrows(SQLException.class, () -> roundPlayersList.list(round));
    }

    // === Utilitaire réflexion ===

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = RoundPlayersList.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(roundPlayersList, value);
    } // end method

} // end class
