package read;

import entite.Course;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class ReadParAndStrokeIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Lit les PAR et Stroke Index depuis le master tee (YELLOW, M, 01-18)
     * Complète le scoreStableford avec les arrays PAR et INDEX
     */
    public ScoreStableford read(final Course course, ScoreStableford scoreStableford) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        LOG.debug("entering {}", methodName);
        LOG.debug("with course = {}", course);
        LOG.debug("with scoreStableford = {}", scoreStableford);

        final String query = """
            WITH
               selection1 AS (
                  SELECT * FROM hole
                     WHERE hole.tee_course_idcourse = ?
                ),
               selection2 AS ( -- master tee
                  SELECT idtee FROM tee
                  WHERE tee.course_idcourse = ?
                    AND tee.TeeStart = "YELLOW"
                    AND tee.TeeGender = "M"
                    AND tee.TeeHolesPlayed = "01-18"
                )
            SELECT * FROM selection1
               JOIN selection2
               WHERE selection1.tee_idtee = selection2.idtee
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, course.getIdcourse());
            ps.setInt(2, course.getIdcourse());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                int[] PAR = new int[18];   // contient les par des 18 trous
                int[] INDEX = new int[18]; // contient les index des 18 trous

                while (rs.next()) {
                    PAR[i] = rs.getInt("HolePar");
                    INDEX[i] = rs.getInt("HoleStrokeIndex");
                    i++;
                }

                LOG.debug("finishing with par          = {}", Arrays.toString(PAR));
                LOG.debug("finishing with Stroke Index = {}", Arrays.toString(INDEX));

                scoreStableford.setParArray(PAR);
                scoreStableford.setIndexArray(INDEX);

                return scoreStableford;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return scoreStableford;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return scoreStableford;
        }
    } // end method

} // end class