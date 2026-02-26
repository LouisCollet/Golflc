
package rowmappers;

import entite.HandicapIndex;

import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HandicapIndexRowMapper extends AbstractRowMapper<HandicapIndex> {

    @Override
 public HandicapIndex map(ResultSet rs) throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    try {
        HandicapIndex h = new HandicapIndex();
        h.setHandicapId(getInteger(rs,"HandicapId") );
        h.setHandicapPlayerId(getInteger(rs,"HandicapPlayerId"));
        h.setHandicapRoundId(getInteger(rs,"HandicapRoundId"));
        h.setHandicapScoreDifferential(getBigDecimal(rs,"HandicapScoreDifferential") );
        h.setHandicapExpectedSD9Holes(getDouble(rs,"HandicapExpectedSD9Holes") );  // new 15-04-2025
        h.setHandicapHolesNotPlayed(getShort(rs,"HandicapHolesNotPlayed") );  // new 15-04-2025
        h.setHandicapDate(getTimestamp(rs,"HandicapDate").toLocalDateTime());
        h.setHandicapWHS(getBigDecimal(rs,"HandicapWHS"));
        h.setHandicapExceptionalScoreReduction(getShort(rs,"HandicapExceptionalScoreReduction"));
        h.setHandicapSoftHardCap(getString(rs,"HandicapSoftHardCap"));
        h.setHandicapComment(getString(rs,"HandicapComment"));
        h.setHandicapPlayedStrokes(getShort(rs,"HandicapPlayedStrokes"));
        h.setLowHandicapIndex(getDouble(rs,"HandicapPreviousLowHandicap"));
        return h;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}