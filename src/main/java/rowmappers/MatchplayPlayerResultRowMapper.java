package rowmappers;

import entite.MatchplayPlayerResult;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class MatchplayPlayerResultRowMapper extends AbstractRowMapper<MatchplayPlayerResult> {

    @Override
    public MatchplayPlayerResult map(ResultSet rs) throws SQLException {
        try {
            MatchplayPlayerResult result = new MatchplayPlayerResult();
            result.setPlayerId(getInteger(rs, "idplayer"));
            result.setHole(getInteger(rs, "ScoreHole"));
            result.setStrokes(getInteger(rs, "ScoreStroke"));
            result.setResult(null);
            result.setPlayerLastFirst(getString(rs, "PlayerLastName") + ", " + getString(rs, "PlayerFirstName"));
            return result;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
