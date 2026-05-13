package integration.create;

import create.CreateCourse;
import entite.Course;
import integration.support.AbstractDaoIT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class CourseUpsertIT extends AbstractDaoIT {

    private static final int TEST_COURSE_ID = 9999;
    private static final int TEST_CLUB_ID   = 1;

    @Test
    void upsertCourse_realDB_insertOrUpdate() throws Exception {

        CreateCourse createCourse = new CreateCourse();
        injectDao(createCourse);

        dao.execute("""
            DELETE FROM course
            WHERE idcourse = ?
            """, TEST_COURSE_ID);

        Course course = new Course();
        course.setIdcourse(TEST_COURSE_ID);
        course.setCourseName("Test Course IT");
        course.setCoursePar((short) 72);
        course.setClub_idclub(TEST_CLUB_ID);

        boolean result = createCourse.upsert(course);

        assertTrue(result);

        Integer count = dao.querySingle(
                """
                SELECT COUNT(*)
                FROM course
                WHERE idcourse = ?
                """,
                rs -> rs.getInt(1),
                TEST_COURSE_ID
        );

        assertEquals(1, count);
    } // end method

} // end class
