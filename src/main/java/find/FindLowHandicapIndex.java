package find;

import entite.HandicapIndex;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import utils.LCUtil;

@ApplicationScoped
public class FindLowHandicapIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindLowHandicapIndex() { }

    public Double find(final HandicapIndex handicapIndex) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for handicapIndex = " + handicapIndex);

        final String query = """
            SELECT MIN(HandicapWHS) LowHandicapIndex
            FROM handicap_index
            WHERE HandicapPlayerId = ?
            AND HandicapDate < ?
            AND HandicapDate > ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, handicapIndex.getHandicapPlayerId());
            ps.setTimestamp(2, Timestamp.valueOf(handicapIndex.getHandicapDate()));
            ps.setTimestamp(3, Timestamp.valueOf(handicapIndex.getHandicapDate().minusYears(2)));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double lowHandicapIndex = rs.getDouble("LowHandicapIndex");
                    LOG.debug("Low Handicap Index = " + lowHandicapIndex);
                    if (lowHandicapIndex == 0.0) {
                        String msg = "Abnormal situation lowHandicapIndex = 0.0";
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                    }
                    return lowHandicapIndex;
                }
                return 0.0;
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0.0;
        }
    } // end method

    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        HandicapIndex handicapIndex = new HandicapIndex();
        handicapIndex.setHandicapPlayerId(324713);
        handicapIndex.setHandicapDate(java.time.LocalDateTime.parse("2022-07-17T17:11:30"));
        Double b = new FindLowHandicapIndex().find(handicapIndex);
        LOG.debug("player FindLowHandicapIndexWHS = " + b.toString());
    } // end main
    */

} // end class
