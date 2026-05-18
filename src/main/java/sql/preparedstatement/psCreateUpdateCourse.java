package sql.preparedstatement;

import entite.Course;
import static interfaces.GolfInterface.DATE_BEGIN_COURSE;
import static interfaces.GolfInterface.DATE_END_COURSE;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;
import static utils.LCUtil.getCurrentMethodName;

/**
 * PreparedStatement mapper pour Course
 * ✅ 100% BASÉ SUR LA STRUCTURE SQL RÉELLE
 */
public class psCreateUpdateCourse {

    /**
     * Prépare le PreparedStatement pour un update de Course
     */
    public static void mapUpdate(PreparedStatement ps, Course course) throws Exception {
        try {
            final String methodName = utils.LCUtil.getCurrentMethodName();
            LOG.debug("entering mapUpdate for course = {}", course);
            ps.setString(1, course.getCourseName());                     // CourseName
            ps.setShort(2, (short) 18);                                   // CourseHoles — always 18
            ps.setShort(3, course.getCoursePar());                        // CoursePar
            ps.setInt(4, course.getClub_idclub());                        // club_idclub
            ps.setTimestamp(5, Timestamp.valueOf(DATE_BEGIN_COURSE));      // CourseBeginDate
            ps.setTimestamp(6, Timestamp.valueOf(course.getCourseEndDate())); // CourseEndDate
            // WHERE clause
            ps.setInt(7, course.getIdcourse());                           // WHERE idcourse = ?
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
                LOG.debug("PreparedStatement for course update: {}", ps);
        } catch (Exception e) {
            String msg = "Exception in mapUpdate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    public static void mapUpsert(PreparedStatement ps, Course course) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setString(2, course.getCourseName());
            ps.setShort(3, (short) 18);
            ps.setShort(4, course.getCoursePar());
            ps.setInt(5, course.getClub_idclub());
            ps.setTimestamp(6, Timestamp.valueOf(DATE_BEGIN_COURSE));
            ps.setTimestamp(7, Timestamp.valueOf(DATE_END_COURSE));
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
        } catch (Exception e) {
            String msg = "Exception in mapUpsert = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    } // end method

    /**
     * Prépare le PreparedStatement pour un insert de Course
     */
    public static void mapCreate(PreparedStatement ps, Course course) throws Exception {
        try {
            LOG.debug("entering mapCreate with course = {}", course);
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, course.getCourseName());
            ps.setShort(3,(short)18); // mod toujours 18 holes for a course
            ps.setShort(4,course.getCoursePar());
            //ps.setInt(5, club.getIdclub());
            ps.setInt(5, course.getClub_idclub()); //club.getIdclub()); // mod 15-02-2026
    // provisoirement, changer ensuite les dates réelles via HeidiSQL
            ps.setTimestamp(6, Timestamp.valueOf(DATE_BEGIN_COURSE)); // date de début fictive pour tous les parcours
            ps.setTimestamp(7, Timestamp.valueOf(DATE_END_COURSE)); // date de fin fictive pour tous les parcours
            ps.setTimestamp(8, Timestamp.from(Instant.now()));
  //             LOG.debug("PreparedStatement for course creation: {}", ps);
            
        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }
}