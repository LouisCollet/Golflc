package read;

import entite.Tee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReadTeeTest {

    private ReadTee readTee;
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        readTee = new ReadTee();

        // Injecter le mock DataSource via réflexion (remplace @Resource)
        Field dsField = ReadTee.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        dsField.set(readTee, dataSource);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
    }

    /**
     * Cas 1 : Tee trouvé — simule 1 ligne retournée par MySQL
     */
    @Test
    void read_teeFound_returnsTee() throws Exception {
        // Arrange — simuler 1 row
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("idtee")).thenReturn(42);
        when(rs.getString("TeeGender")).thenReturn("M");
        when(rs.getString("TeeStart")).thenReturn("YELLOW");
        when(rs.getShort("teeslope")).thenReturn((short) 113);
        when(rs.getBigDecimal("teerating")).thenReturn(new BigDecimal("68.3"));
        when(rs.getInt("TeeClubHandicap")).thenReturn(0);
        when(rs.getInt("tee.course_idcourse")).thenReturn(10);
        when(rs.getString("TeeHolesPlayed")).thenReturn("01-18");
        when(rs.getShort("TeePar")).thenReturn((short) 72);
        when(rs.getInt("TeeMasterTee")).thenReturn(1);
        when(rs.getInt("TeeDistanceTee")).thenReturn(5800);

        Tee input = new Tee();
        input.setIdtee(42);

        // Act
        Tee result = readTee.read(input);

        // Assert
        assertNotNull(result);
        assertFalse(result.isNotFound());
        assertEquals(42, result.getIdtee());
        assertEquals("M", result.getTeeGender());
        assertEquals("YELLOW", result.getTeeStart());
        assertEquals((short) 113, result.getTeeSlope());
        assertEquals(new BigDecimal("68.3"), result.getTeeRating());
        assertEquals(0, result.getTeeClubHandicap());
        assertEquals(10, result.getCourse_idcourse());
        assertEquals("01-18", result.getTeeHolesPlayed());
        assertEquals((short) 72, result.getTeePar());
        assertEquals(1, result.getTeeMasterTee());
        assertEquals(5800, result.getTeeDistanceTee());
        verify(ps).setInt(1, 42);
    }

    /**
     * Cas 2 : Tee pas trouvé (= ancien test main avec idtee=3000)
     */
    @Test
    void read_teeNotFound_setsNotFoundFlag() throws Exception {
        // Arrange — simuler 0 rows
        when(rs.next()).thenReturn(false);

        Tee input = new Tee();
        input.setIdtee(3000);

        // Act
        Tee result = readTee.read(input);

        // Assert
        assertNotNull(result);
        assertTrue(result.isNotFound());
        verify(ps).setInt(1, 3000);
    }

    /**
     * Cas 3 : SQLException — vérifie que l'exception est propagée
     */
    @Test
    void read_sqlException_throws() throws Exception {
        // Arrange — simuler une erreur de connexion
        when(dataSource.getConnection()).thenThrow(
                new java.sql.SQLException("Connection refused", "08001", 0));

        Tee input = new Tee();
        input.setIdtee(1);

        // Act & Assert
        assertThrows(java.sql.SQLException.class, () -> readTee.read(input));
    }

} // end class
