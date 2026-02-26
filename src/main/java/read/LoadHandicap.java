package read;

import entite.Handicap;
import entite.Player;
import entite.Round;
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
import java.sql.Timestamp;
import javax.sql.DataSource;

@ApplicationScoped
public class LoadHandicap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public LoadHandicap() { }

    public Handicap load(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - player = " + player.toString());

        final String query = """
                SELECT *
                FROM handicap
                WHERE handicap.player_idplayer = ?
                AND DATE(?)
                    BETWEEN handicap.idhandicap
                    AND handicap.handicapend
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                Handicap h = new Handicap();
                while (rs.next()) {
                    h = entite.Handicap.map(rs);
                }
                return h;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324713);
        // Round round = new Round();
        // round.setRoundDate(LocalDateTime.of(2017, Month.AUGUST, 26, 0, 0));
        // Handicap h = new LoadHandicap().load(player, round);
        // LOG.debug(" handicap = " + h.toString());
    } // end main
    */

} // end class
