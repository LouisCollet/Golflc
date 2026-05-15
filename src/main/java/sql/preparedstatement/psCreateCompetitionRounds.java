package sql.preparedstatement;

import entite.CompetitionDescription;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;

public class psCreateCompetitionRounds implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param ldt flight start datetime — computed by caller from competition date + flight start time
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final CompetitionDescription de,
            final LocalDateTime ldt) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1,  Types.INTEGER);                        // idround — auto-increment
            ps.setTimestamp(2,  Timestamp.valueOf(ldt));               // RoundDate
            ps.setString   (3,  de.getCompetitionGame());              // RoundGame
            ps.setInt      (4,  0);                                    // RoundCBA
            ps.setString   (5,  de.getCompetitionName());              // RoundName
            ps.setString   (6,  de.getCompetitionQualifying());        // RoundQualifying
            ps.setInt      (7,  18);                                   // RoundHoles
            ps.setInt      (8,  de.getCompetitionStartHole());         // RoundStart
            ps.setBytes    (9,  "0".getBytes());                       // RoundCompetition
            ps.setString   (10, "no MP score");                        // MatchplayResult
            ps.setInt      (11, 0);                                    // RoundPlayers
            ps.setString   (12, de.getCompetitionName());              // RoundTeam
            ps.setInt      (13, de.getCompetitionCourseId());          // RoundIdCourse
            ps.setTimestamp(14, Timestamp.from(Instant.now()));        // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
