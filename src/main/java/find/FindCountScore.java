package find;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class FindCountScore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public int find(Player player, Round round, String operation) throws SQLException { // ✅ conn supprimé
        LOG.debug("entering find");
        LOG.debug("round  =  " + round);
        LOG.debug("operation =  " + operation);

        String query;
        if (operation.equalsIgnoreCase("rows")) {
            query = """
                    SELECT count(*)
                    FROM score
                    WHERE score.inscription_player_idplayer = ?
                      AND inscription_round_idround = ?
                    """;
        } else {
            query = """
                    SELECT sum(scorestroke)
                    FROM score
                    WHERE score.inscription_player_idplayer = ?
                      AND inscription_round_idround = ?
                    """;
        }

        try (Connection conn = dao.getConnection()) {                    // ✅ try-with-resources niveau 1
            try (PreparedStatement ps = conn.prepareStatement(query)) {         // ✅ try-with-resources niveau 2
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                utils.LCUtil.logps(ps);
                try (ResultSet rs = ps.executeQuery()) {                        // ✅ try-with-resources niveau 3
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 99; // error code
                    }
                }
            }
        } catch (SQLException sqle) {
            String msg = "SQLException in FindCountScore = " + sqle.getMessage()
                    + " ,SQLState = " + sqle.getSQLState()
                    + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            throw sqle;                                                         // ✅ throw au lieu de return 99
        } catch (Exception e) {
            String msg = "Exception in FindCountScore = " + e.getMessage();
            LOG.error(msg);
            throw new SQLException(msg, e);                                     // ✅ throw au lieu de return 99
        }
    } // end method

    void main() throws Exception {
        // ⚠️ Sans CDI container : dao sera null → à tester via WildFly uniquement
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(628);
        int i = new FindCountScore().find(player, round, "rows");
        LOG.debug("CountScore = " + i);
    } // end main

} // end class
