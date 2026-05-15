package sql.preparedstatement;

import entite.CompetitionData;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateUpdateCompetitionData implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final CompetitionData data) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setNull     (1,  Types.INTEGER);                        // CmpDataId — auto-increment
            ps.setInt      (2,  data.getCmpDataCompetitionId());
            ps.setInt      (3,  data.getCmpDataPlayerId());
            ps.setShort    (4,  (short) 0);                            // CmpDataPlayingHandicap
            ps.setDouble   (5,  0);                                    // CmpDataHandicap
            ps.setTime     (6,  Time.valueOf("00:00:00"));             // CmpDataFlightStart
            ps.setShort    (7,  (short) 0);                            // CmpDataFlightNumber
            ps.setShort    (8,  (short) 0);                            // CmpDataScorePoints
            ps.setString   (9,  data.getCmpDataLastHoles());
            ps.setString   (10, data.getCmpDataPlayerFirstLastName());
            ps.setString   (11, data.getCmpDataAskedStartTime());
            ps.setString   (12, data.getCmpDataPlayerGender());
            ps.setInt      (13, 0);                                    // CmpDataRoundId
            ps.setString   (14, "");                                   // CmpDataTeeStart
            ps.setDouble   (15, 0);                                    // CmpDataScoreDifferential
            ps.setTimestamp(16, Timestamp.from(Instant.now()));        // ModificationDate
            sql.PrintWarnings.print(ps.getWarnings(), methodName);
            utils.LCUtil.logps(ps);
            return ps;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Maps all SET columns for UPDATE competition_data. Column order matches DBMeta.listMetaColumnsUpdate().
     */
    public static PreparedStatement psMapUpdate(
            PreparedStatement ps,
            final CompetitionData cda) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ps.setInt      (1,  cda.getCmpDataPlayerId());
            ps.setShort    (2,  cda.getCmpDataPlayingHandicap());
            ps.setDouble   (3,  cda.getCmpDataHandicap());
            ps.setTime     (4,  Time.valueOf(cda.getCmpDataFlightStart()));
            ps.setShort    (5,  cda.getCmpDataFlightNumber());
            ps.setShort    (6,  cda.getCmpDataScorePoints());
            ps.setString   (7,  cda.getCmpDataLastHoles());
            ps.setString   (8,  cda.getCmpDataPlayerFirstLastName());
            ps.setString   (9,  cda.getCmpDataAskedStartTime());
            ps.setString   (10, cda.getCmpDataPlayerGender());
            ps.setInt      (11, cda.getCmpDataRoundId());
            ps.setString   (12, cda.getCmpDataTeeStart());
            ps.setDouble   (13, cda.getCmpDataScoreDifferential());
            ps.setTimestamp(14, Timestamp.from(Instant.now()));        // ModificationDate
            ps.setInt      (15, cda.getCmpDataId());                   // WHERE CmpDataId
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
