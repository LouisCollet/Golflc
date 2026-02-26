package find;

import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;

@ApplicationScoped
public class FindHandicapEGA implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindHandicapEGA() { }

    public double find(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" for player = " + player);
        LOG.debug("Round Date = " + round.getRoundDate());
        LOG.debug(" for round = " + round);

        final String query =
            "SELECT handicap.handicapPlayerEGA" +
            "    FROM handicap " +
            "    WHERE" +
            "        handicap.player_idplayer = ?" +
            "        and date(?)" +
            "            between handicap.idhandicap and handicap.handicapend;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal t = BigDecimal.ZERO;
                int i = 0;
                while (rs.next()) {
                    i++;
                    t = rs.getBigDecimal("handicapPlayerEGA");
                }
                LOG.debug("nombre de handicapEGA = " + i);
                LOG.debug("HandicapPlayerEGA = " + t);
                return t.doubleValue();
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return 0;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
        }
    } // end method

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/

} // end class
