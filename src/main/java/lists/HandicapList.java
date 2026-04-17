package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;

@ApplicationScoped
public class HandicapList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<ECourseList> liste = null;

    public HandicapList() { }

    /**
     * Note: This method cannot use dao.queryList() because the original logic
     * calls clubMapper.map(rs) BEFORE the while loop (on first row position),
     * then iterates with rs.next() using that club for RoundRowMapper.
     * This requires direct ResultSet control.
     */
    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
               /* lists.HandicapList.list  */
            SELECT *
            FROM handicap, round, course, player
            WHERE handicap.round_idround = round.idround
                 AND round.course_idcourse = course.idcourse
                 AND player.idplayer = ?
                 AND handicap.player_idplayer = player.idplayer
            GROUP by idhandicap
            ORDER by idhandicap DESC
            """;

        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper();
        RowMapper<Club> clubMapper = new ClubRowMapper();

        // Use dao.queryList with a lambda that maps club per-row
        liste = new ArrayList<>(dao.queryList(query, rs -> {
            Club club = clubMapper.map(rs);
            return ECourseList.builder()
                    .course(courseMapper.map(rs))
                    .round(roundMapper.map(rs, club))
                    .build();
        }, player.getIdplayer()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324713);
        List<ECourseList> p1 = new HandicapList().list(player);
        LOG.debug("Handicap list = " + p1);
    } // end main
    */

} // end class
