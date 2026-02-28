package charts;

import entite.Average;
import entite.Player;
import entite.Round;
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

/**
 * @deprecated Not currently used — kept for potential future use.
 */
@Deprecated
@ApplicationScoped
public class RoundDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public RoundDetail() { }

    public List<Average> getRoundDetail(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with player = " + player + " //round  = " + round);

        final String query = """
            SELECT scorehole, scorepar, scorestrokeindex, scoreextrastroke,
                round( avg(scorestroke),1 ) as averageStroke,
                round( avg(scorepoints),1 ) as averagePoints,
                count(distinct idround) as countround
            FROM score, round, course
            WHERE
                round.idround = ?
                and score.player_has_round_player_idplayer = ?
                and score.player_has_round_round_idround = round.idround
            GROUP BY scorehole
            ORDER by scorehole
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Average> listAverage = new ArrayList<>();
                while (rs.next()) {
                    Average average = entite.Average.map(rs);
                    listAverage.add(average);
                }
                LOG.debug("listavg after while = " + listAverage.toString());
                return listAverage;
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
