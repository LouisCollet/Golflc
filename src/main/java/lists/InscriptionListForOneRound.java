package lists;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

@Named
@ApplicationScoped
public class InscriptionListForOneRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    private List<ECourseList> liste = null;

    public InscriptionListForOneRound() { }

    public List<ECourseList> list(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                while (rs.next()) {
                    Club club = clubMapper.map(rs);
                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .course(courseMapper.map(rs))
                            .round(roundMapper.map(rs, club))
                            .build();
                    liste.add(ecl);
                } // end while
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list for round=" + round.getIdround());
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
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

    public List<ECourseList> getListe()                       { return liste; }
    public void              setListe(List<ECourseList> liste) { this.liste = liste; }

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
        Round round = new Round();
        round.setIdround(260);
        List<ECourseList> p1 = list(round);
        LOG.debug("number extracted = " + p1.size());
        LOG.debug("inscription list = " + p1.toString());
    } // end main
    */

} // end class
