package lists;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

@Named
@ApplicationScoped
public class InscriptionListForOneRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<ECourseList> liste = null;

    public InscriptionListForOneRound() { }

    public List<ECourseList> list(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            /* lists.InscriptionListForOneRound.list */
            WITH selection AS (
                SELECT * FROM round
                WHERE round.idround = ?
            )
            SELECT * FROM selection
               JOIN course
                  ON course.idcourse = selection.course_idcourse
               JOIN club
                  ON club.idclub = course.club_idclub
               ORDER BY roundDate DESC
               LIMIT 30;
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper();

        liste = new ArrayList<>(dao.queryList(query, rs -> {
            Club club = clubMapper.map(rs);
            return ECourseList.builder()
                    .club(club)
                    .course(courseMapper.map(rs))
                    .round(roundMapper.map(rs, club))
                    .build();
        }, round.getIdround()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list for round=" + round.getIdround());
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<ECourseList> getListe()                       { return liste; }
    public void              setListe(List<ECourseList> liste) { this.liste = liste; }

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
        Round round = new Round();
        round.setIdround(260);
        List<ECourseList> p1 = list(round);
        LOG.debug("number extracted = " + p1.size());
        LOG.debug("inscription list = " + p1.toString());
    } // end main
    */

} // end class
