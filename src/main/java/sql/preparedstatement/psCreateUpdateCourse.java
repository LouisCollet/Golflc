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
        int index = 0;
        try {
            LOG.debug("entering mapUpdate for course = {}", course);
            
            // ========================================
            // Champs SQL RÉELS (PAS de slope, rating, description, type, active)
            // ========================================
       /*     ps.setString(++index, course.getCourseName());                // CourseName VARCHAR(45)
            ps.setByte(++index, course.getCourseHoles().byteValue());     // CourseHoles TINYINT DEFAULT 18
            ps.setByte(++index, course.getCoursePar().byteValue());       // CoursePar TINYINT
            ps.setInt(++index, course.getClubIdclub());                   // club_idclub INT
            
            // ⚠️ NOUVEAUX CHAMPS - Dates Begin/End
            ps.setTimestamp(++index, course.getCourseBeginDate());        // CourseBeginDate TIMESTAMP
            ps.setTimestamp(++index, course.getCourseEndDate());          // CourseEndDate DATETIME
            
           
            ps.setInt(++index, course.getIdcourse());                     // WHERE idcourse = ?
            -------------
      */      
            ps.setString(1, course.getCourseName());
          //  ps.setShort(2, (short) 18);// mod 12-11-2018 toujour 18 holes  enlevé dans blacklist de columns update
            Integer intValue = 18;
            ps.setShort(2, intValue.shortValue());
            ps.setShort(3, course.getCoursePar()); 
            ps.setTimestamp(4, Timestamp.valueOf(DATE_BEGIN_COURSE)); // date de début fictive pour tous les parcours
            ps.setTimestamp(5, Timestamp.valueOf(course.getCourseEndDate())); //DATE_END_COURSE)); // date de fin fictive pour tous les parcours
             // WHERE clause
            ps.setInt(6, course.getIdcourse());  // ne pas oublier
                LOG.debug("PreparedStatement for course update: {}", ps);
        } catch (Exception e) {
            String msg = "Exception in mapUpdate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    /**
     * Prépare le PreparedStatement pour un insert de Course
     */
    public static void mapCreate(PreparedStatement ps, Course course) throws Exception {
     //   int index = 0;
        try {
            LOG.debug("entering mapCreate with course = {}", course);
            
        /*    ps.setInt(++index, course.getIdcourse());                     // idcourse INT
            ps.setString(++index, course.getCourseName());                // CourseName VARCHAR(45)
            // CourseHoles avec valeur par défaut
            byte holes = (course.getCourseHoles() != null) ? course.getCourseHoles().byteValue() : 18;
            ps.setByte(++index, holes);                                   // CourseHoles TINYINT DEFAULT 18
            ps.setByte(++index, course.getCoursePar().byteValue());       // CoursePar TINYINT
            ps.setInt(++index, course.getClubIdclub());                   // club_idclub INT
            // Dates avec valeurs par défaut
            Timestamp beginDate = (course.getCourseBeginDate() != null) 
                                ? course.getCourseBeginDate() 
                                : Timestamp.valueOf("2010-01-01 00:00:00");
            ps.setTimestamp(++index, beginDate);                          // CourseBeginDate TIMESTAMP DEFAULT '2010-01-01'
            Timestamp endDate = (course.getCourseEndDate() != null)
                              ? course.getCourseEndDate()
                              : Timestamp.valueOf("2099-12-31 00:00:00");
            ps.setTimestamp(++index, endDate);                            // CourseEndDate DATETIME DEFAULT '2099-12-31'
            
            ps.setTimestamp(++index, Timestamp.from(java.time.Instant.now())); // CourseModificationDate
      */      
            
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