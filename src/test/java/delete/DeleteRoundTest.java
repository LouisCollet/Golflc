package delete;

import entite.Round;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DeleteRoundTest {

    private DeleteRound deleteRound;
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private Round round;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);

        deleteRound = new DeleteRound();
        Field dsField = DeleteRound.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        dsField.set(deleteRound, dataSource);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Entité de test (= ancien main() avec idround=760)
        round = new Round();
        round.setIdround(760);
    }

    // ===================== delete() =====================
    /**
     * Cas 1 : Suppression simple réussie
     */
    @Test
    void delete_success_returnsTrue() throws Exception {
        when(ps.executeUpdate()).thenReturn(1);
        boolean result = deleteRound.delete(round);
        assertTrue(result);
        verify(ps).setInt(1, 760);
        verify(ps).executeUpdate();
    }

    /**
     * Cas 2 : Round inexistant — executeUpdate retourne 0 mais pas d'erreur
     */
    @Test
    void delete_noRowDeleted_stillReturnsTrue() throws Exception {
        when(ps.executeUpdate()).thenReturn(0);

        boolean result = deleteRound.delete(round);

        assertTrue(result);  // le code retourne true même si 0 rows
        verify(ps).setInt(1, 760);
    }

    /**
     * Cas 3 : SQLException (ex: FK constraint) — handleSQLException propage
     */
    @Test
    void delete_sqlException_throws() throws Exception {
        when(ps.executeUpdate()).thenThrow(
                new SQLException("Cannot delete: FK constraint", "23000", 1451));

        assertThrows(SQLException.class, () -> deleteRound.delete(round));
    }

    // ===================== deleteRoundAndChilds() =====================

    /**
     * Cas 4 : Cascade complète réussie (= ancien main() avec idround=760)
     * score → inscription → round → payment
     */
    @Test
    void deleteRoundAndChilds_success_returnsTrue() throws Exception {
        // 4 appels executeUpdate successifs : score, inscription, round, payment
        when(ps.executeUpdate()).thenReturn(3, 2, 1, 1);

        boolean result = deleteRound.deleteRoundAndChilds(round);

        assertTrue(result);
        // 4 PreparedStatements créés sur la même connexion
        verify(conn, times(4)).prepareStatement(anyString());
        // Chaque PS reçoit l'idround
        verify(ps, times(4)).setInt(1, 760);
        verify(ps, times(4)).executeUpdate();
    }

    /**
     * Cas 5 : Cascade avec 0 enfants — toujours success
     */
    @Test
    void deleteRoundAndChilds_noChildren_returnsTrue() throws Exception {
        when(ps.executeUpdate()).thenReturn(0, 0, 1, 0);

        boolean result = deleteRound.deleteRoundAndChilds(round);

        assertTrue(result);
        verify(ps, times(4)).executeUpdate();
    }

    /**
     * Cas 6 : SQLException pendant la cascade — handleSQLException propage
     */
    @Test
    void deleteRoundAndChilds_sqlException_throws() throws Exception {
        // Le premier DELETE (score) échoue
        when(ps.executeUpdate()).thenThrow(
                new SQLException("Lock wait timeout", "40001", 1205));

        assertThrows(SQLException.class, () -> deleteRound.deleteRoundAndChilds(round));
    }

} // end class
