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
 * Service d'activation de compte joueur (PlayerActivation = 1)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdatePlayerActivation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public UpdatePlayerActivation() { }

    public boolean update(Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(" with player = {}", player);

        final String query = """
                UPDATE player
                SET PlayerActivation = 1
                WHERE idplayer = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            if (row != 0) {
                LOG.debug("successful UPDATE player - PlayerActivation is now = 1");
                return true;
            } else {
                String msg = "UNsuccessful result in UPDATE for player : " + player.getIdplayer();
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
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        boolean b = new update.UpdatePlayerActivation().update(player);
        LOG.debug("from main, result = {}", b);
    } // end main
    */

} // end class
