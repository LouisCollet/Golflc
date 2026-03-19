package read;

import entite.Player;
import entite.composite.EPlayerPassword;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import static interfaces.Log.LOG;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import utils.LCUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe refactorée pour lire un Player ou EPlayerPassword
 * ✅ Migré CDI GenericDAO — 2026-03-18
 */
@ApplicationScoped
public class ReadPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadPlayer() {} // end constructor

    /**
     * Lecture d'un Player simple par id
     */
    public Player read(Player player) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                SELECT *
                FROM Player
                WHERE idplayer = ?
                """;

        Player p = dao.querySingle(query, new PlayerRowMapper(), player.getIdplayer());
        if (p != null) {
            LOG.debug(methodName + " - Player loaded: " + p);
            return p;
        }
        String msg = "Player not found in " + methodName;
        LOG.warn(msg);
        LCUtil.showMessageFatal(msg);
        return null;
    } // end method

    /**
     * Lecture d'un EPlayerPassword
     */
    public EPlayerPassword read(EPlayerPassword epp) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                SELECT *
                FROM Player
                WHERE idplayer = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, epp.player().getIdplayer());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                EPlayerPassword result = new EPlayerPassword(null, null);

                if (rs.next()) {
                    Player player = playerMapper.map(rs);
                    var password = entite.Password.map(rs);
                    result = new EPlayerPassword(player, password);
                    LOG.debug(methodName + " - EPlayerPassword loaded: " + result);
                } else {
                    String msg = "Player not found in " + methodName;
                    LOG.warn(msg);
                    LCUtil.showMessageFatal(msg);
                }
                return result;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new EPlayerPassword(null, null);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new EPlayerPassword(null, null);
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // tests locaux
    } // end main
    */

} // end class
