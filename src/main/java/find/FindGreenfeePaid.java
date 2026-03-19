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
import utils.LCUtil;

@ApplicationScoped
public class FindGreenfeePaid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindGreenfeePaid() { }

    public boolean find(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);

        final String query = """
            SELECT COUNT(*)
            FROM payments_greenfee
            WHERE GreenfeeIdRound = ?
              AND GreenfeeIdPlayer = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                if (rs.next()) {
                    i = rs.getInt(1);
                }
                if (i == 0) {
                    String msg = LCUtil.prepareMessageBean("greenfee.notfound")
                            + " for player = " + player.getPlayerLastName()
                            + " / " + player.getIdplayer()
                            + " for round = " + round;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
                if (i > 1) {
                    String err = "Abnormal technical situation !! More than 1 greenfee paid ?";
                    LOG.error(err);
                    LCUtil.showMessageFatal(err);
                    return false;
                }
                String msg = LCUtil.prepareMessageBean("greenfee.paid")
                        + player.getPlayerLastName() + " / " + player.getIdplayer()
                        + " for round : " + round.getRoundName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
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
    // BRIDGE — @Deprecated — pour les appelants legacy (new FindGreenfeePaid().find(player, round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #find(Player, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(456781);
        Round round = new Round();
        round.setIdround(633);
        boolean b = find(player, round);
        LOG.debug("result findGreenfeePaid = " + b);
    } // end main
    */

} // end class
