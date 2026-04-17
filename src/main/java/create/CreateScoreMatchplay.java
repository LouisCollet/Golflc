package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Inject private dao.GenericDAO dao;

    public CreateScoreMatchplay() { }

    public boolean create(final ScoreMatchplay score, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("scorematchplay = {}", score);
        LOG.debug("Round = {}", round);
        try {
            final String query
                    = "UPDATE round"
                    + " SET RoundMatchplayResult = ?"
                    + " WHERE idround = ?";

            try (Connection conn = dao.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, OBJECT_MAPPER.writeValueAsString(score));
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
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
