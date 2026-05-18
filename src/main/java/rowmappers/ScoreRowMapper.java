package rowmappers;

import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class ScoreRowMapper extends AbstractRowMapper<ScoreStableford.Score> {

    @Override
    public ScoreStableford.Score map(ResultSet rs) throws SQLException {
        try {
            ScoreStableford.Score score = new ScoreStableford().new Score();
            score.setHole(rs.getInt("ScoreHole"));
            score.setPar(rs.getInt("ScorePar"));
            score.setIndex(rs.getInt("ScoreStrokeIndex"));
            score.setExtra(rs.getInt("ScoreExtraStroke"));
            score.setStrokes(rs.getInt("ScoreStroke"));
            score.setPoints(rs.getInt("ScorePoints"));
            return score;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
