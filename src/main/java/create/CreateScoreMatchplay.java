package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Round;
import entite.ScoreMatchplay;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreateScoreMatchplay implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateScoreMatchplay() { }

    public boolean create(final ScoreMatchplay score, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - scorematchplay = " + score);
        LOG.debug(methodName + " - Round = " + round);
        try {
            final String query
                    = "UPDATE round"
                    + " SET RoundMatchplayResult = ?"
                    + " WHERE idround = ?";

            try (Connection conn = dao.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ObjectMapper om = new ObjectMapper();
                ps.setString(1, om.writeValueAsString(score));
                ps.setInt(2, round.getIdround());
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Successful update score matchplay"
                            + " round = " + round.getIdround()
                            + " score = " + score;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "NOT NOT Successful update,"
                            + " round = " + round.getIdround();
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
