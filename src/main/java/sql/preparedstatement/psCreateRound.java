package sql.preparedstatement;

import entite.Course;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import java.io.Serializable;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

public class psCreateRound implements Serializable, interfaces.Log, interfaces.GolfInterface {

    /**
     * @param ldt RoundDate convertie en UTC par l'appelant avant insertion
     */
    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final Round round,
            final Course course,
            final LocalDateTime ldt) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            ps.setNull(1, java.sql.Types.INTEGER);              // AUTO-INCREMENT
            ps.setObject(2, ldt, JDBCType.TIMESTAMP);           // RoundDate — UTC
            ps.setString(3, round.getRoundGame());               // RoundGame
            ps.setInt(4, round.getRoundCBA());                   // RoundCBA
            ps.setString(5, round.getRoundName());               // RoundName
            ps.setString(6, round.getRoundQualifying());         // RoundQualifying
            ps.setInt(7, round.getRoundHoles());                 // RoundHoles
            ps.setInt(8, round.getRoundStart());                 // RoundStart
            ps.setString(9, round.getRoundCompetition());        // RoundCompetition
            ps.setString(10, "no MP score");                     // MatchplayResult
            ps.setInt(11, 0);                                    // RoundPlayers — inutilisé depuis 16-09-2021
            ps.setString(12, round.getRoundTeam());              // RoundTeam
            ps.setInt(13, course.getIdcourse());                 // RoundIdCourse
            ps.setTimestamp(14, Timestamp.from(Instant.now())); // RoundModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);// TarifModificationDate
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
