package read;

import entite.Handicap;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;

@ApplicationScoped
public class LoadHandicap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

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

        return dao.querySingle(query, rs -> entite.Handicap.map(rs),
                player.getIdplayer(), Timestamp.valueOf(round.getRoundDate()));
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
