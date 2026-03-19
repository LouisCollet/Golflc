package create;

import entite.HandicapIndex;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreateOrModifyHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateOrModifyHandicapIndex() { }

    public Integer status(final HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with HandicapIndex  = " + handicapIndex);

        final String query = """
            SELECT HandicapId
            FROM handicap_index
            WHERE HandicapRoundId = ?
            AND HandicapPlayerId = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, handicapIndex.getHandicapRoundId());
            ps.setInt(2, handicapIndex.getHandicapPlayerId());
            utils.LCUtil.logps(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int handicapId = rs.getInt(1);
                    LOG.debug("HandicapId already exists - This is a modification = " + handicapId);
                    return handicapId;
                } else {
                    LOG.debug("HandicapId doesn't exists - This is a creation");
                    return 0;
                }
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
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        HandicapIndex handicapindex = new HandicapIndex();
        handicapindex.setHandicapRoundId(589);
        handicapindex.setHandicapPlayerId(324715);
        int i = new CreateOrModifyHandicapIndex().status(handicapindex);
        LOG.debug("Creation or modification ? (0 = creation) " + i);
    } // end main
    */

} // end class
