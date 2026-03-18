package create;

import entite.Professional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CreateProfessionalTest {

    private CreateProfessional createProfessional;

    // Mocks JDBC
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private PreparedStatement psCountColumns;
    private ResultSet rsCountColumns;
    private Statement stGeneratedKey;
    private ResultSet rsGeneratedKey;

    // Entité de test (= ancien main())
    private Professional professional;

    @BeforeEach
    void setUp() throws Exception {
        // Créer les mocks JDBC
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        psCountColumns = mock(PreparedStatement.class);
        rsCountColumns = mock(ResultSet.class);
        stGeneratedKey = mock(Statement.class);
        rsGeneratedKey = mock(ResultSet.class);

        // Instancier et injecter via réflexion
        createProfessional = new CreateProfessional();
        injectField("dataSource", dataSource);

        // JDBC setup
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn("golflc");

        // Mock DBMeta.CountColumns — information_schema retourne 7 colonnes (table professional)
        when(conn.prepareStatement(argThat(q -> q != null && q.contains("information_schema"))))
                .thenReturn(psCountColumns);
        when(psCountColumns.executeQuery()).thenReturn(rsCountColumns);
        when(rsCountColumns.next()).thenReturn(true);
        when(rsCountColumns.getInt(1)).thenReturn(7);

        // Mock INSERT (toute autre requête)
        when(conn.prepareStatement(argThat(q -> q != null && !q.contains("information_schema"))))
                .thenReturn(ps);

        // Mock LCUtil.generatedKey
        when(conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                .thenReturn(stGeneratedKey);
        when(stGeneratedKey.executeQuery(anyString())).thenReturn(rsGeneratedKey);
        when(rsGeneratedKey.next()).thenReturn(true);
        when(rsGeneratedKey.getInt(1)).thenReturn(999);

        // Entité de test (= ancien main() lignes 66-70)
        professional = new Professional();
        professional.setProStartDate(LocalDateTime.parse("2021-01-01T00:00:00"));
        professional.setProEndDate(LocalDateTime.parse("2050-12-31T23:59:59"));
        professional.setProClubId(1186);
        professional.setProPlayerId(324720);
        professional.setProAmount(50.0);
    }

    /**
     * Cas 1 : Création réussie — INSERT retourne 1 row (= scénario main())
     */
    @Test
    void create_success_returnsTrue() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        boolean result = createProfessional.create(professional);

        // Assert
        assertTrue(result);
        assertEquals(999, professional.getProId());
        verify(ps).executeUpdate();
    }

    /**
     * Cas 2 : INSERT échoue (row=0) — retourne false
     */
    @Test
    void create_insertFails_returnsFalse() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(0);

        // Act
        boolean result = createProfessional.create(professional);

        // Assert
        assertFalse(result);
        verify(ps).executeUpdate();
    }

    /**
     * Cas 3 : SQLException — handleSQLException relance SQLException
     */
    @Test
    void create_sqlException_throwsSQLException() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenThrow(new java.sql.SQLException("Column 'ProId' cannot be null"));

        // Act & Assert
        assertThrows(java.sql.SQLException.class,
                () -> createProfessional.create(professional));
    }

    /**
     * Cas 4 : DataSource retourne null connection — SQLException relancée
     */
    @Test
    void create_connectionFails_throwsSQLException() throws Exception {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new java.sql.SQLException("Connection refused"));

        // Act & Assert
        assertThrows(java.sql.SQLException.class,
                () -> createProfessional.create(professional));
    }

    /**
     * Cas 5 : Professional avec proAmount=0 — création réussie
     */
    @Test
    void create_zeroAmount_returnsTrue() throws Exception {
        // Arrange
        professional.setProAmount(0.0);
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        boolean result = createProfessional.create(professional);

        // Assert
        assertTrue(result);
        assertEquals(999, professional.getProId());
    }

    /**
     * Cas 6 : Vérifie que psMapCreate positionne les paramètres clés
     */
    @Test
    void create_success_setsKeyParameters() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);

        // Act
        createProfessional.create(professional);

        // Assert — vérifie les paramètres clés du PreparedStatement
        verify(ps).setNull(1, java.sql.Types.INTEGER); // ProId auto-increment
        verify(ps).setInt(2, 1186);    // ProClubId
        verify(ps).setInt(5, 324720);  // ProPlayerId
        verify(ps).setDouble(6, 50.0); // ProAmount
    }

    // === Utilitaire réflexion ===

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = CreateProfessional.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(createProfessional, value);
    } // end method

} // end class
