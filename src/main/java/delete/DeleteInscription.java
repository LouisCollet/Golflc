package delete;

import entite.Club;
import entite.Course;
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
import utils.LCUtil;

@ApplicationScoped
public class DeleteInscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private find.FindCountScore findCountScore;

    public DeleteInscription() { }

    public boolean delete(final Player player, final Round round, final Club club, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);

        int rows = findCountScore.find(player, round, "rows");
        if (rows == 99) {
            LOG.error(methodName + " - fatal error in findCountScore");
            return false;
        }
        if (rows == 0) {
            LOG.debug(methodName + " - OK, score not yet registered");
        } else {
            String msg = " -- score already registered: delete refused rows = " + rows;
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }

        try (Connection conn = dao.getConnection()) {

            // 1. DELETE inscription
            int rowPhr;
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM player_has_round
                    WHERE InscriptionIdPlayer = ?
                      AND InscriptionIdRound = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                LCUtil.logps(ps);
                rowPhr = ps.executeUpdate();
                LOG.debug(methodName + " - deleted inscription = " + rowPhr);
            }

            if (rowPhr == 0) {
                String msg = LCUtil.prepareMessageBean("inscription.not.canceled")
                        + "<br/>player id = " + player.getIdplayer()
                        + "<br/>Player Last Name = " + player.getPlayerLastName()
                        + "<br/>Round id = " + round.getIdround();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return false;
            }

            String msg = LCUtil.prepareMessageBean("inscription.canceled")
                    + " <br/>Player id = " + player.getIdplayer()
                    + " <br/>Player Last Name = " + player.getPlayerLastName()
                    + " <br/>Round id = " + round.getIdround();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);

            // 2. DELETE greenfee payment
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM payments_greenfee
                    WHERE GreenfeeIdPlayer = ?
                      AND GreenfeeIdRound = ?
                    """)) {
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                LCUtil.logps(ps);
                LOG.debug(methodName + " - deleted PaymentGreenfee = " + ps.executeUpdate());
            }

            return true;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new DeleteInscription().delete(player, round, club, course, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #delete(Player, Round, Club, Course)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324715);
        Round round = new Round();
        round.setIdround(757);
        Club club = new Club();
        Course course = new Course();
        boolean b = delete(player, round, club, course);
        LOG.debug("delete result = " + b);
    } // end main
    */

} // end class
