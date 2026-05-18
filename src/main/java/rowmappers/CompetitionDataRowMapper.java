package rowmappers;

import entite.CompetitionData;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import static utils.LCUtil.getCurrentMethodName;

public class CompetitionDataRowMapper extends AbstractRowMapper<CompetitionData> {

    @Override
    public CompetitionData map(ResultSet rs) throws SQLException {
        try {
            CompetitionData c = new CompetitionData();
            c.setCmpDataId(getInteger(rs, "CmpDataId"));
            c.setCmpDataCompetitionId(getInteger(rs, "CmpDataCompetitionId"));
            c.setCmpDataPlayerId(getInteger(rs, "CmpDataPlayerId"));
            c.setCmpDataPlayingHandicap(getShort(rs, "CmpDataPlayingHandicap"));
            c.setCmpDataHandicap(getDouble(rs, "CmpDataHandicap"));
            Time flightStart = rs.getTime("CmpDataFlightStart");
            c.setCmpDataFlightStart(flightStart != null ? flightStart.toLocalTime() : null);
            c.setCmpDataFlightNumber(getShort(rs, "CmpDataFlightNumber"));
            c.setCmpDataScorePoints(getShort(rs, "CmpDataScorePoints"));
            c.setCmpDataLastHoles(getString(rs, "CmpDataLastHoles"));
            c.setCmpDataPlayerFirstLastName(getString(rs, "CmpDataPlayerFirstLastName"));
            c.setCmpDataAskedStartTime(getString(rs, "CmpDataAskedStartTime"));
            c.setCmpDataPlayerGender(getString(rs, "CmpDataPlayerGender"));
            c.setCmpDataRoundId(getInteger(rs, "CmpDataRoundId"));
            c.setCmpDataTeeStart(getString(rs, "CmpDataTeeStart"));
            c.setCmpDataScoreDifferential(getDouble(rs, "CmpDataScoreDifferential"));
            return c;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
