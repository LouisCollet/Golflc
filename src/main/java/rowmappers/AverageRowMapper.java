package rowmappers;

import entite.Average;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class AverageRowMapper extends AbstractRowMapper<Average> {

    @Override
    public Average map(ResultSet rs) throws SQLException {
        try {
            Average a = new Average();
            a.setAvgHole(getShort(rs, "ScoreHole"));
            a.setAvgPar(getShort(rs, "ScorePar"));
            a.setAvgStrokeIndex(getShort(rs, "ScoreStrokeIndex"));
            a.setAvgExtraStroke(getShort(rs, "ScoreExtraStroke"));
            a.setAvgStroke(getDouble(rs, "averageStroke"));
            a.setAvgPoints(getDouble(rs, "averagePoints"));
            a.setCountRounds(getShort(rs, "countround"));
            return a;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
