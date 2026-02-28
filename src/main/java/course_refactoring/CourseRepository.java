package course_refactoring;

import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import static exceptions.LCException.handleSQLException;
import static exceptions.LCException.handleGenericException;
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
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;

@ApplicationScoped
public class CourseRepository implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CourseRepository() { }

    public List<ECourseList> findAllValidCourses() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String sql = """
            SELECT *
            FROM club
            JOIN course ON club.idclub = course.club_idclub
            JOIN tee ON tee.course_idcourse = course.idcourse
            WHERE course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            ORDER BY clubname, coursename, idtee, teestart
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapper<Tee> teeMapper = new TeeRowMapper();

            List<ECourseList> result = new ArrayList<>();
            while (rs.next()) {
                ECourseList ecl = ECourseList.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                    .tee(teeMapper.map(rs))
                    .build();
                result.add(ecl);
            }
            LOG.debug(methodName + " - list size = " + result.size());
            return List.copyOf(result);

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

} // end class
