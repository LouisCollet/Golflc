package find;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FindInscriptionRoundTest {

    private FindInscriptionRound findInscriptionRound;

    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    // Entités de test (= ancien main() : idround=633, idplayer=324715)
    private Round round;
    private Player player;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        findInscriptionRound = new FindInscriptionRound();
        Field dsField = FindInscriptionRound.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        dsField.set(findInscriptionRound, dataSource);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        round = new Round();
        round.setIdround(633);

        player = new Player();
        player.setIdplayer(324715);
    }

    /**
     * Cas 1 : Joueur inscrit — COUNT(*) = 1 → true
     * (= ancien main() avec idround=633, idplayer=324715)
     */
    @Test
    void find_playerInscribed_returnsTrue() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt(1)).thenReturn(1);

        boolean result = findInscriptionRound.find(round, player);

        assertTrue(result);
        verify(ps).setInt(1, 633);
        verify(ps).setInt(2, 324715);
    }

    /**
     * Cas 2 : Joueur pas inscrit — COUNT(*) = 0 → false
     */
    @Test
    void find_playerNotInscribed_returnsFalse() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt(1)).thenReturn(0);

        boolean result = findInscriptionRound.find(round, player);

        assertFalse(result);
        verify(ps).setInt(1, 633);
        verify(ps).setInt(2, 324715);
    }

    /**
     * Cas 3 : Plusieurs inscriptions (doublon) — COUNT(*) = 2 → true
     */
    @Test
    void find_multipleInscriptions_returnsTrue() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt(1)).thenReturn(2);

        boolean result = findInscriptionRound.find(round, player);

        assertTrue(result);
    }

    /**
     * Cas 4 : ResultSet vide (pas de row) — count reste 0 → false
     */
    @Test
    void find_emptyResultSet_returnsFalse() throws Exception {
        when(rs.next()).thenReturn(false);

        boolean result = findInscriptionRound.find(round, player);

        assertFalse(result);
    }

    /**
     * Cas 5 : SQLException — handleSQLException propage
     */
    @Test
    void find_sqlException_throws() throws Exception {
        when(ps.executeQuery()).thenThrow(
                new SQLException("Table not found", "42S02", 1146));

        assertThrows(SQLException.class,
                () -> findInscriptionRound.find(round, player));
    }

} // end class
