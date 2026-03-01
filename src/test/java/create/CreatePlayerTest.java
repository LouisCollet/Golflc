package create;

import entite.HandicapIndex;
import entite.LatLng;
import entite.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CreatePlayerTest {

    private CreatePlayer createPlayer;

    // Mocks JDBC
    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement ps;
    private PreparedStatement psCountColumns;
    private ResultSet rsCountColumns;
    private Statement stGeneratedKey;
    private ResultSet rsGeneratedKey;

    // Mocks services injectés
    private CreateHandicapIndex createHandicapIndexService;
    private CreateActivationPlayer createActivationPlayer;
    private payment.PaymentSubscriptionController paymentSubscriptionController;

    // Entités de test (= ancien main())
    private Player player;
    private HandicapIndex handicapIndex;

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

        // Créer les mocks services
        createHandicapIndexService = mock(CreateHandicapIndex.class);
        createActivationPlayer = mock(CreateActivationPlayer.class);
        paymentSubscriptionController = mock(payment.PaymentSubscriptionController.class);

        // Instancier et injecter via réflexion
        createPlayer = new CreatePlayer();
        injectField("dataSource", dataSource);
        injectField("createHandicapIndexService", createHandicapIndexService);
        injectField("createActivationPlayer", createActivationPlayer);
        injectField("paymentSubscriptionController", paymentSubscriptionController);

        // JDBC setup
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.getCatalog()).thenReturn("golflc");

        // Mock DBMeta.CountColumns — information_schema retourne 19 colonnes (table player)
        when(conn.prepareStatement(argThat(q -> q != null && q.contains("information_schema"))))
                .thenReturn(psCountColumns);
        when(psCountColumns.executeQuery()).thenReturn(rsCountColumns);
        when(rsCountColumns.next()).thenReturn(true);
        when(rsCountColumns.getInt(1)).thenReturn(19);

        // Mock INSERT (toute autre requête)
        when(conn.prepareStatement(argThat(q -> q != null && !q.contains("information_schema"))))
                .thenReturn(ps);

        // Mock LCUtil.generatedKey
        when(conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE))
                .thenReturn(stGeneratedKey);
        when(stGeneratedKey.executeQuery(anyString())).thenReturn(rsGeneratedKey);
        when(rsGeneratedKey.next()).thenReturn(true);
        when(rsGeneratedKey.getInt(1)).thenReturn(678905);

        // Entités de test (= ancien main() lignes 180-210)
        player = new Player();
        player.setIdplayer(678905);
        player.setPlayerFirstName("first test");
        player.setPlayerLastName("last test");
        player.setPlayerBirthDate(LocalDateTime.parse("2018-11-03T12:45:30"));
        player.getAddress().setZoneId("Europe/Brussels");
        player.setPlayerHomeClub(101);
        player.getAddress().setCity("Brussels");
        player.getAddress().setStreet("Rue Test 1");
        player.getAddress().setZipCode("1000");
        player.setPlayerGender("M");
        player.setPlayerLanguage("es");
        player.getAddress().getCountry().setCode("US");
        player.getAddress().setLatLng(new LatLng(50.8262271, 4.3571382));
        player.setPlayerEmail("test@test.com");
        player.setPlayerEmailConfirmation("test@test.com");
        player.setPlayerRole("PLAYER");

        handicapIndex = new HandicapIndex();
        handicapIndex.setHandicapDate(LocalDateTime.parse("2018-11-03T12:45:30"));
        handicapIndex.setHandicapWHS(BigDecimal.valueOf(36.0));
    }

    /**
     * Cas 1 : Création complète réussie — batch="A" (= scénario main())
     * INSERT player + handicap + subscription + activation
     */
    @Test
    void create_fullSuccess_returnsTrue() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);
        when(createHandicapIndexService.create(any(HandicapIndex.class))).thenReturn(handicapIndex);
        when(paymentSubscriptionController.createPayment(any())).thenReturn(true);
        when(createActivationPlayer.create(player)).thenReturn(true);

        // Act
        boolean result = createPlayer.create(player, handicapIndex, "A");

        // Assert
        assertTrue(result);
        verify(ps).executeUpdate();
        verify(conn).setAutoCommit(false);
        verify(conn).commit();
        verify(conn, never()).rollback();
        verify(createHandicapIndexService).create(any(HandicapIndex.class));
        verify(paymentSubscriptionController).createPayment(any());
        verify(createActivationPlayer).create(player);
    }

    /**
     * Cas 2 : Batch="B" — pas d'activation créée
     */
    @Test
    void create_batchB_skipsActivation() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);
        when(createHandicapIndexService.create(any(HandicapIndex.class))).thenReturn(handicapIndex);
        when(paymentSubscriptionController.createPayment(any())).thenReturn(true);

        // Act
        boolean result = createPlayer.create(player, handicapIndex, "B");

        // Assert
        assertTrue(result);
        verify(createActivationPlayer, never()).create(any());
        verify(conn).commit();
    }

    /**
     * Cas 3 : Emails ne matchent pas — Exception
     */
    @Test
    void create_emailMismatch_throwsException() throws Exception {
        // Arrange
        player.setPlayerEmailConfirmation("other@test.com");

        // Act & Assert
        Exception ex = assertThrows(Exception.class,
                () -> createPlayer.create(player, handicapIndex, "A"));
        assertTrue(ex.getMessage().contains("test@test.com"));
        verify(ps, never()).executeUpdate();
    }

    /**
     * Cas 4 : INSERT échoue (row=0) — SQLException + rollback
     */
    @Test
    void create_insertFails_throwsSQLException() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(0);

        // Act & Assert
        assertThrows(SQLException.class,
                () -> createPlayer.create(player, handicapIndex, "A"));
    }

    /**
     * Cas 5 : Handicap creation échoue — rollback
     */
    @Test
    void create_handicapFails_rollbackAndThrows() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);
        when(createHandicapIndexService.create(any(HandicapIndex.class))).thenReturn(null);

        // Act & Assert
        assertThrows(SQLException.class,
                () -> createPlayer.create(player, handicapIndex, "A"));
        verify(conn).rollback();
    }

    /**
     * Cas 6 : Subscription creation échoue — rollback
     */
    @Test
    void create_subscriptionFails_rollbackAndThrows() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);
        when(createHandicapIndexService.create(any(HandicapIndex.class))).thenReturn(handicapIndex);
        when(paymentSubscriptionController.createPayment(any())).thenReturn(false);

        // Act & Assert
        assertThrows(SQLException.class,
                () -> createPlayer.create(player, handicapIndex, "A"));
        verify(conn).rollback();
    }

    /**
     * Cas 7 : Activation échoue — rollback
     */
    @Test
    void create_activationFails_rollbackAndThrows() throws Exception {
        // Arrange
        when(ps.executeUpdate()).thenReturn(1);
        when(createHandicapIndexService.create(any(HandicapIndex.class))).thenReturn(handicapIndex);
        when(paymentSubscriptionController.createPayment(any())).thenReturn(true);
        when(createActivationPlayer.create(player)).thenReturn(false);

        // Act & Assert
        assertThrows(SQLException.class,
                () -> createPlayer.create(player, handicapIndex, "A"));
        verify(conn).rollback();
    }

    // === Utilitaire réflexion ===

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = CreatePlayer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(createPlayer, value);
    } // end method

} // end class
