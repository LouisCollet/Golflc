package update;

import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Met à jour RoundGame et/ou RoundQualifying pour un round existant.
 * Utilisé quand le joueur change ces champs dans score_stableford.xhtml
 * (champs migrés depuis round.xhtml le 2026-04-21).
 */
@ApplicationScoped
public class UpdateRoundGameQualifying implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateRoundGameQualifying() { }

    public boolean update(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("idround={}, game={}, qualifying={}",
                round != null ? round.getIdround() : null,
                round != null ? round.getRoundGame() : null,
                round != null ? round.getRoundQualifying() : null);

        if (round == null || round.getIdround() == null) {
            LOG.warn("round or idround null — update skipped");
            return false;
        }

        final String query = """
            UPDATE round
               SET RoundGame       = ?,
                   RoundQualifying = ?
             WHERE idround = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, round.getRoundGame());
            ps.setString(2, round.getRoundQualifying());
            ps.setInt(3, round.getIdround());
            utils.LCUtil.logps(ps);

            int rows = ps.executeUpdate();
            LOG.debug("rows updated = {}", rows);
            return rows == 1;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
