package find;

import entite.Greenfee;
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
import java.time.LocalDateTime;

@ApplicationScoped
public class FindGreenfeePaid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindGreenfeePaid() { }

    /** Cart duplicate check — compares cart keys (idplayer + roundDate + idclub) against payments_greenfee. */
    public boolean findByCartKeys(final Integer idplayer, final LocalDateTime roundDate, final Integer idclub) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for idplayer={} idclub={} roundDate={}", idplayer, idclub, roundDate);

        if (idplayer == null || roundDate == null || idclub == null) {
            LOG.debug("one of the cart keys is null — skipping check");
            return false;
        }

        final String query = """
            SELECT COUNT(*)
            FROM payments_greenfee
            WHERE GreenfeeIdPlayer  = ?
              AND GreenfeeRoundDate  = ?
              AND GreenfeeIdClub     = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idplayer);
            ps.setObject(2, roundDate);
            ps.setInt(3, idclub);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOG.debug("greenfee already paid idplayer={} idclub={} roundDate={}", idplayer, idclub, roundDate);
                    return true;
                }
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

    public boolean find(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for player = {}", player);
        LOG.debug("for round = {}", round);

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
                    LOG.debug("greenfee not found for player={} round={}", player.getIdplayer(), round.getIdround());
                    return false;
                }
                if (i > 1) {
                    LOG.error("abnormal: {} greenfee rows for player={} round={}", i, player.getIdplayer(), round.getIdround());
                    return false;
                }
                LOG.debug("greenfee found for player={} round={}", player.getIdplayer(), round.getIdround());
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

    public boolean find(final Greenfee greenfee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for greenfee player={} club={} roundDate={}", greenfee.getIdplayer(), greenfee.getIdclub(), greenfee.getRoundDate());

        final String query = """
            SELECT COUNT(*)
            FROM payments_greenfee
            WHERE GreenfeeRoundDate = ?
              AND GreenfeeIdClub    = ?
              AND GreenfeeIdPlayer  = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setObject(1, greenfee.getRoundDate());
            ps.setInt(2, greenfee.getIdclub());
            ps.setInt(3, greenfee.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOG.debug("greenfee already paid player={} club={} roundDate={}",
                        greenfee.getIdplayer(), greenfee.getIdclub(), greenfee.getRoundDate());
                    return true;
                }
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

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new FindGreenfeePaid().find(player, round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #find(Player, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(456781);
        Round round = new Round();
        round.setIdround(633);
        boolean b = find(player, round);
        LOG.debug("result findGreenfeePaid = " + b);
    } // end main
    */

} // end class
