package charts;

import entite.Handicap;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

@ApplicationScoped
public class HandicapDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public HandicapDetail() { }

    public List<Handicap> getStatHcp(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with player = " + player);

        final String query = """
            SELECT PlayerFirstName, PlayerLastName, idhandicap, HandicapPlayer
            FROM player, handicap
            WHERE player.idplayer=?
            AND handicap.player_idplayer = player.idplayer
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Handicap> liste = new ArrayList<>();
                while (rs.next()) {
                    Handicap handicap = new Handicap();
                    handicap.setHandicapStart(rs.getDate("idhandicap"));
                    handicap.setHandicapPlayerEGA(rs.getBigDecimal("HandicapPlayerEGA"));
                    liste.add(handicap);
                }
                LOG.debug("liste after while = " + liste.toString());
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // @Deprecated bridge removed 2026-02-28 — no callers with Connection conn

} // end class
