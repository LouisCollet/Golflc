package delete;

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
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteRound() { }

    public boolean delete(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for round = " + round);

        final String query = """
            DELETE FROM round
            WHERE round.idround = ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            LOG.debug(methodName + " - deleted round rows = " + rowDeleted);
            LCUtil.showMessageInfo("Round deleted = " + round.getIdround());
            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ATTENTION — supprime TOUTES les inscriptions et scores pour tous les joueurs
    // Utilisé uniquement pour les rounds de test
    public boolean deleteRoundAndChilds(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for round = " + round);

        try (Connection conn = dataSource.getConnection()) {

            // 1. Scores (niveau le plus bas)
            int rowScore = 0;
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM score
                    WHERE score.player_has_round_round_idround = ?
                    """)) {
                ps.setInt(1, round.getIdround());
                LCUtil.logps(ps);
                rowScore = ps.executeUpdate();
                LOG.debug(methodName + " - deleted score = " + rowScore);
            }

            // 2. Inscriptions
            int rowPhr = 0;
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM player_has_round
                    WHERE InscriptionIdRound = ?
                    """)) {
                ps.setInt(1, round.getIdround());
                LCUtil.logps(ps);
                rowPhr = ps.executeUpdate();
                LOG.debug(methodName + " - deleted inscription = " + rowPhr);
            }

            // 3. Round
            int rowRnd = 0;
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM round
                    WHERE round.idround = ?
                    """)) {
                ps.setInt(1, round.getIdround());
                LCUtil.logps(ps);
                rowRnd = ps.executeUpdate();
                LOG.debug(methodName + " - deleted round = " + rowRnd);
            }

            // 4. Payments greenfee — new 29-04-2025
            int rowPay = 0;
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM payments_greenfee
                    WHERE GreenfeeIdRound = ?
                    """)) {
                ps.setInt(1, round.getIdround());
                LCUtil.logps(ps);
                rowPay = ps.executeUpdate();
                LOG.debug(methodName + " - deleted payment = " + rowPay);
            }

            String msg = "Records deleted:"
                    + " round=" + round.getIdround()
                    + " score=" + rowScore
                    + " inscription=" + rowPhr
                    + " roundRows=" + rowRnd
                    + " payment=" + rowPay;
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return true;

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
        Round round = new Round();
        round.setIdround(760);
        boolean b = deleteRoundAndChilds(round);
        LOG.debug("from main - result deleteRoundAndChilds = " + b);
    } // end main
    */

} // end class
