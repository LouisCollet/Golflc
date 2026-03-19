package create;

import entite.HandicapIndex;
import entite.LatLng;
import entite.Player;
import entite.Subscription;
import entite.Subscription.etypeSubscription;
import jakarta.enterprise.context.ApplicationScoped;

import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import payment.PaymentSubscriptionController;
import sql.preparedstatement.psCreateUpdatePlayer;
import utils.LCUtil;

/**
 * Service de création de Player
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class CreatePlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Inject private dao.GenericDAO dao;
    @Inject private create.CreateHandicapIndex createHandicapIndexService;  // ✅ ajouter
    @Inject private create.CreateActivationPlayer createActivationPlayer;   // migrated 2026-02-24
    @Inject private payment.PaymentSubscriptionController paymentSubscriptionController; // migrated 2026-02-25
    /**
     * Crée un Player dans la base avec son Handicap et sa Subscription initiale.
     * ✅ @Transactional — JTA coordonne la transaction sur toutes les connexions CDI
     */
    @Transactional(rollbackOn = Exception.class)
    public boolean create(final Player player,
                          HandicapIndex handicapIndex,
                          final String batch) throws Exception {
        LOG.debug("entering CreatePlayer - create");
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

        LOG.debug("dao = {}", dao);

        try (Connection conn = dao.getConnection()) {

            // Vérification email
            if (batch.equals("A")
                    && !player.getPlayerEmail()
                    .equals(player.getPlayerEmailConfirmation())) {

                msg = LCUtil.prepareMessageBean("player.email.notmatch")
                        + " : " + player.getPlayerEmail()
                        + " / " + player.getPlayerEmailConfirmation();

                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new Exception(msg);
            }

            // Insert Player
            String query = LCUtil.generateInsertQuery(conn, "player");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                psCreateUpdatePlayer.mapCreate(ps, player, batch);
                LCUtil.logps(ps);

                int row = ps.executeUpdate();
                if (row == 0) {
                    msg = "Fatal Error executeUpdate in " + methodName;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }

            // Initial Handicap
            handicapIndex.setHandicapPlayerId(player.getIdplayer());
            handicapIndex.setHandicapPlayedStrokes((short) 0);

            handicapIndex = createHandicapIndexService.create(handicapIndex);

            if (handicapIndex != null) {
                msg = "Initial Handicap created !!";
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                msg = "create Initial Handicap failed";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new SQLException(msg); // JTA rollback automatique
            }

            // Initial Subscription
            Subscription subscription = new Subscription();
            subscription.setIdplayer(player.getIdplayer());
            subscription.setSubCode(etypeSubscription.INITIAL.toString());

            if (!paymentSubscriptionController.createPayment(subscription)) { // migrated 2026-02-25

                msg = "Initial Subscription creation failed";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new SQLException(msg); // JTA rollback automatique

            } else {
                msg = "Initial Subscription created until: "
                        + subscription.getEndDate();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            }

            // Activation non batch
            if (!batch.equals("B")) {
                if (!createActivationPlayer.create(player)) {
                    msg = "Activation failed";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg); // JTA rollback automatique
                } else {
                    msg = "Activation created for " + player;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                }
            }

            msg = "All records committed successfully";
            LOG.debug(msg);

            return true; // JTA commit automatique au retour normal

        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = "SQLException in " + methodName + ": "
                    + sqle.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle; // JTA rollback automatique via rollbackOn

        } catch (Exception e) {
            msg = "Exception in " + methodName + ": "
                    + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e; // JTA rollback automatique via rollbackOn
        }
    }

    /**
     * Main pour tests hors JSF
     * (non fonctionnel sans container CDI)
     */
    public static void main(String[] args) {
        try {

            Player player = new Player();
            player.setIdplayer(678905);
            player.setPlayerFirstName("first test");
            player.setPlayerLastName("last test");
            player.setPlayerBirthDate(
                    LocalDateTime.parse("2018-11-03T12:45:30"));
            player.getAddress().setZoneId("Europe/Brussels");
            player.setPlayerHomeClub(101);
            player.getAddress().setCity("Brussels");
            player.setPlayerGender("M");
            player.setPlayerLanguage("es");
            player.getAddress().getCountry().setCode("US");
            player.getAddress()
                    .setLatLng(new LatLng(50.8262271, 4.3571382));

            HandicapIndex handicapIndex = new HandicapIndex();
            handicapIndex.setHandicapDate(
                    LocalDateTime.parse("2018-11-03T12:45:30"));
            handicapIndex.setHandicapWHS(
                    BigDecimal.valueOf(36.0));

            LOG.debug("Main ready (CDI required for execution)");

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
            LCUtil.showMessageFatal(
                    "Exception in main: " + e.getMessage());
        }
    }
} // end class