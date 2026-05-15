package sql.preparedstatement;

import entite.CompetitionData;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

public class psCreateCompetitionInscriptions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static PreparedStatement psMapCreate(
            PreparedStatement ps,
            final CompetitionData cda) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String teeStart = cda.getCmpDataTeeStart();
            int tee = Integer.parseInt(teeStart.substring(teeStart.lastIndexOf("/") + 2));
            LOG.debug("tee extracted = {}", tee);
            ps.setNull     (1,  Types.INTEGER);                        // idInscription — auto-increment
            ps.setInt      (2,  cda.getCmpDataRoundId());
            ps.setInt      (3,  cda.getCmpDataPlayerId());
            ps.setInt      (4,  0);                                    // FinalResults — initial value
            ps.setInt      (5,  0);                                    // NotUsed1 — initial value
            ps.setInt      (6,  0);                                    // NotUsed2 — initial value
            ps.setString   (7,  teeStart);
            ps.setInt      (8,  tee);
            ps.setInt      (9,  cda.getCmpDataPlayerId());
            ps.setTimestamp(10, Timestamp.from(Instant.now()));        // ModificationDate
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
