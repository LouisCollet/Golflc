package lists;

import entite.Player;
import entite.Subscription;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import rowmappers.SubscriptionRowMapper;

// non testé
@ApplicationScoped
public class SubscriptionRenewalList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @jakarta.inject.Inject private mail.SubscriptionMail subscriptionMail;  // migrated 2026-02-26

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public SubscriptionRenewalList() { }

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM payments_subscription
            JOIN player
               ON player.idplayer = subscriptionIdPlayer
               AND PlayerActivation = '1'
            WHERE YEAR(SubscriptionEndDate) = YEAR(CURRENT_DATE())
              AND MONTH(SubscriptionEndDate) = MONTH(CURRENT_DATE()) + 1
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                RowMapper<Subscription> subscriptionMapper = new SubscriptionRowMapper();

                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                            .player(playerMapper.map(rs))
                            .subscription(subscriptionMapper.map(rs))
                            .build();
                    liste.add(ecl);
                }
                liste.forEach(item -> LOG.debug("players candidates to renewal = " + item));

                // partie 2 — envoi des mails de renouvellement
                for (ECourseList item : liste) {
                    LOG.debug("Player we send a Subscription Renewal mail = " + item.player().getPlayerLastName());
                    // new mail.SubscriptionMail().sendMail(...)
                    subscriptionMail.sendMail(item.player(), item.subscription()); // migrated 2026-02-26
                } // end for

                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        List<ECourseList> ec = new SubscriptionRenewalList().list();
        LOG.debug("from main, ec = " + ec);
    } // end main
    */

} // end class
