package update;

import entite.Player;
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
 * Met à jour le tee choisi pour une inscription (inscription) existante.
 * Utilisé quand le joueur change son tee dans score_stableford.xhtml.
 */
@ApplicationScoped
public class UpdateInscriptionTee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateInscriptionTee() { }

    public boolean update(final Round round, final Player player,
                          final String inscriptionTeeStart, final int idtee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("round={}, player={}, tee={}, idtee={}",
                round != null ? round.getIdround() : null,
                player != null ? player.getIdplayer() : null,
                inscriptionTeeStart, idtee);

        final String query = """
            UPDATE inscription
               SET InscriptionTeeStart = ?,
                   InscriptionIdTee    = ?
             WHERE InscriptionIdRound  = ?
               AND InscriptionIdPlayer = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, inscriptionTeeStart);
            ps.setInt(2, idtee);
            ps.setInt(3, round.getIdround());
            ps.setInt(4, player.getIdplayer());
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
