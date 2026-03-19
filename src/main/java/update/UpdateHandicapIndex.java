package update;

import entite.HandicapIndex;
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

/**
 * Service de mise à jour du HandicapIndex (WHS, Cap, ESR)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateHandicapIndex() { }

    public boolean update(final HandicapIndex handicapIndex) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" for handicapIndex = " + handicapIndex);

        final String query = """
                UPDATE handicap_index
                 SET HandicapWHS = ?,
                    HandicapComment = ?,
                    HandicapSoftHardCap = ?,
                    HandicapExceptionalScoreReduction = ?,
                    HandicapPreviousLowHandicap = ?
                 WHERE HandicapId = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setBigDecimal(1, handicapIndex.getHandicapWHS());
            ps.setString(2, "UPD-" + handicapIndex.getHandicapComment());
            ps.setString(3, handicapIndex.getHandicapSoftHardCap());
            ps.setShort(4, handicapIndex.getHandicapExceptionalScoreReduction());
            ps.setDouble(5, handicapIndex.getLowHandicapIndex());
            ps.setInt(6, handicapIndex.getHandicapId());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "successful UPDATE HandicapIndex = " + handicapIndex;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "UNsuccessful result in " + methodName + " for player : " + handicapIndex.getHandicapPlayerId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
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
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        HandicapIndex index = new HandicapIndex();
        index.setHandicapId(26);
        index.setHandicapWHS(java.math.BigDecimal.valueOf(2.3));
        index.setHandicapComment("no comment for this handicap");
        index.setHandicapExceptionalScoreReduction((short) -3);
        index.setHandicapSoftHardCap("capM");
        boolean b = new update.UpdateHandicapIndex().update(index);
        LOG.debug("from main, result = " + b);
    } // end main
    */

} // end class
