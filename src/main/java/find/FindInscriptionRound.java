package find;

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
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class FindInscriptionRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindInscriptionRound() { }

    public boolean find(final Round round, final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for round = " + round);
        LOG.debug("for player = " + player);

        final String query = """
            SELECT COUNT(*)
            FROM  inscription
            WHERE inscription.InscriptionIdRound = ?
               AND inscription.InscriptionIdPlayer = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                LOG.debug(methodName + " - count = " + count);
                return count > 0;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new FindInscriptionRound().find(round, player, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #find(Round, Player)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Round round = new Round();
        round.setIdround(633);
        Player player = new Player();
        player.setIdplayer(324715);
        boolean b = find(round, player);
        LOG.debug("inscription found = " + b);
    } // end main
    */

} // end class
