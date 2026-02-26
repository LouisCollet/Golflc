/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package course_refactoring;

import entite.Club;
import entite.Course;
import entite.Tee;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import entite.composite.ECourseList;

@ApplicationScoped // ?? à changer je pense
public class CourseRepository {

 //   @Inject
 //   private DataSource dataSource;

    public List<ECourseList> findAllValidCourses() throws SQLException, Exception {

        String sql = """
            SELECT *
            FROM club
            JOIN course ON club.idclub = course.club_idclub
            JOIN tee ON tee.course_idcourse = course.idcourse
            WHERE course.CourseEndDate >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
            ORDER BY clubname, coursename, idtee, teestart
        """;

        List<ECourseList> result = new ArrayList<>();

        try (Connection conn = new DBConnection().getConnection(); // à vérifier va être fermée ?
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapper<Tee> teeMapper = new TeeRowMapper();

            while (rs.next()) {
             //   ECourseList ecl = new ECourseList();
                ECourseList ecl = ECourseList.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                    .tee(teeMapper.map(rs))
                .build();
       //         ecl.setClub(clubMapper.map(rs));
        //        ecl.setCourse(courseMapper.map(rs));
        //        ecl.setTee(teeMapper.map(rs));
                result.add(ecl);
            }
            LOG.debug("at this moment is conn closed ? " + conn.isClosed());
        }
      LOG.debug("list result = " + result.toString());
     //   return result;
        return List.copyOf(result);  //mod 02-01-2025
    }
} // end class