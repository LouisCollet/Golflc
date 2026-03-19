package update;

import entite.Player;
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
 * Service de mise à jour de l'Exceptional Score Reduction (ESR)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateExceptionalScoreReduction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdateExceptionalScoreReduction() { }

    public boolean update(final Player player, double esr) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(" for player = " + player);
        LOG.debug(" for ExceptionalScoreReduction = " + esr);

        final String query = """
                UPDATE handicap_index AS UPD,
                (SELECT * FROM handicap_index
                 WHERE HandicapPlayerid=?
                 ORDER BY HandicapDate desc
                 LIMIT 20) SEL
                 SET UPD.HandicapExceptionalScoreReduction=?,
                 UPD.HandicapScoreDifferential=UPD.HandicapScoreDifferential ?
                 WHERE UPD.HandicapId=SEL.HandicapId
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setDouble(2, esr);
            ps.setDouble(3, esr);
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            LOG.debug(" ESR modified rows = " + row);
            if (row != 0) {
                String msg = "update records for esr successfull";
                LOG.debug(msg);
                return true;
            } else {
                String msg = "NOT NOT Successful update, row = 0 player = " + player;
                LOG.debug(msg);
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
        Player player = new Player();
        player.setIdplayer(324713);
        int esr = -1;
        boolean b = new update.UpdateExceptionalScoreReduction().update(player, esr);
        LOG.debug("from main, result = " + b);
    } // end main
    */

} // end class
