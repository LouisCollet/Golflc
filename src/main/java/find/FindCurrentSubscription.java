package find;

import static interfaces.Log.LOG;
import entite.Player;
import entite.Subscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
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
import rowmappers.RowMapper;
import rowmappers.SubscriptionRowMapper;

@ApplicationScoped
public class FindCurrentSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindCurrentSubscription() { }

    public List<Subscription> payments(final Player player, final String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for player = " + player.getIdplayer() + " type = " + type);

        final String query;
        if (type.equals("now")) {
            query = """
                SELECT *
                FROM payments_subscription
                WHERE SubscriptionIdPlayer = ?
                  AND NOW() BETWEEN DATE_SUB(SubscriptionStartDate, INTERVAL 1 DAY) AND SubscriptionEndDate
                ORDER BY SubscriptionStartDate ASC
                LIMIT 1
                """;
        } else { // latest
            query = """
                SELECT *
                FROM payments_subscription
                WHERE SubscriptionIdPlayer = ?
                ORDER BY SubscriptionStartDate DESC
                LIMIT 1
                """;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Subscription> liste = new ArrayList<>();
                RowMapper<Subscription> subscriptionMapper = new SubscriptionRowMapper();
                while (rs.next()) {
                    liste.add(subscriptionMapper.map(rs));
                }
                if (liste.isEmpty()) {
                    String msg = "Empty result in " + methodName + " for player = " + player.getIdplayer();
                    LOG.error(msg);
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

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - no cache to invalidate");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        List<Subscription> p1 = payments(player, "now");
        LOG.debug("Subscription found = " + p1);
    } // end main
    */

} // end class
