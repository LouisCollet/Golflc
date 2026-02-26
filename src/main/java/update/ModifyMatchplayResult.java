package update;

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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class ModifyMatchplayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ModifyMatchplayResult() { }

    public void modifyMPResult(final Round round, final Player player, final String result) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - Round ID = " + round.getIdround());
        LOG.debug(methodName + " - result = " + result);
        LOG.debug(methodName + " - Player ID = " + player.getIdplayer());
        try {
            final String query
                    = "  UPDATE player_has_round"
                    + "  SET player_has_round.Player_has_roundMatchplayResult = ?, Player_has_roundModificationDate = ? "
                    + "  WHERE "
                    + "       InscriptionIdPlayer = ?"
                    + "   AND InscriptionIdRound = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, result);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setInt(3, player.getIdplayer());
                ps.setInt(4, round.getIdround());
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Successful update : "
                            + " player = " + player.getIdplayer()
                            + " result  = " + result;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                } else {
                    String msg = "NOT NOT Successful update,"
                            + " player = " + player.getIdplayer();
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
