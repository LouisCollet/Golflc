package lists;

import entite.Club;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapperRound;

@Named
@ApplicationScoped
public class ScoreCardList1EGA implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public ScoreCardList1EGA() { }

    public List<ECourseList> list(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        LOG.debug(methodName + " with player = " + player);
        LOG.debug(methodName + " for round = " + round);

        // Needs raw Connection for DBMeta.listMetaColumnsLoad (dynamic column list)
        try (Connection conn = dao.getConnection()) {

            // Dynamic column list via DBMeta (requires same connection)
            String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
            String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
            String ha = utils.DBMeta.listMetaColumnsLoad(conn, "Handicap");

            String query =
                    "SELECT" + pl + "," + ha + "," + ro
                    + " FROM player, handicap, round"
                    + " WHERE"
                    + "   player.idplayer=?"
                    + "   AND round.idround=?"
                    + "   AND handicap.player_idplayer = player.idplayer"
                    + "   AND date(RoundDate) between idhandicap and handicapend";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                utils.LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    liste = new ArrayList<>();
                    RowMapperRound<Round> roundMapper = new RoundRowMapper();
                    rowmappers.RowMapper<Club> clubMapper = new ClubRowMapper();

                    while (rs.next()) {
                        Club club = clubMapper.map(rs);
                        ECourseList ecl = ECourseList.builder()
                                .club(club)
                                .player(player)
                                .round(roundMapper.map(rs, club))
                                .handicap(entite.Handicap.map(rs))
                                .build();
                        liste.add(ecl);
                    }
                }
            }

            if (liste.isEmpty()) {
                LOG.warn(methodName + " - empty result list");
            } else {
                LOG.debug(methodName + " - list size = " + liste.size());
            }
            return liste;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // Getters/setters d'instance
    public List<ECourseList> getListe()                 { return liste; }
    public void setListe(List<ECourseList> liste)       { this.liste = liste; }

    // Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(578);
        // List<ECourseList> ec = list(player, round);
        // LOG.debug("from main, ec = " + ec);
        LOG.debug("from main, ScoreCardList1EGA = ");
    } // end main
    */

} // end class
