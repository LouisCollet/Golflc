
package rowmappers;

import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

//public class ScoreStablefordRowMapper implements RowMapper<ScoreStableford> { // mod 23-01-2026
 public class ScoreStablefordRowMapper extends AbstractRowMapper<ScoreStableford> {
    @Override
   public ScoreStableford map(ResultSet rs) throws SQLException {  // with table score
       final String methodName = utils.LCUtil.getCurrentMethodName();  
   try{
           //LOG.debug("entering map for method = " + methodName);
            ScoreStableford scoreStableford = new ScoreStableford();
            scoreStableford.setScoreHole(getShort(rs,"ScoreHole") );
            scoreStableford.setScoreStroke(getShort(rs,"ScoreStroke") );
            scoreStableford.setScoreExtraStroke(getShort(rs,"ScoreExtraStroke") );
            scoreStableford.setScorePoints(getShort(rs,"ScorePoints") );
            scoreStableford.setScorePar(getShort(rs,"ScorePar") );
            scoreStableford.setScoreStrokeIndex(getShort(rs,"ScoreStrokeIndex"));
            scoreStableford.setScoreFairway(getShort(rs,"ScoreFairway") );
            scoreStableford.setScoreGreen(getShort(rs,"ScoreGreen") );
            scoreStableford.setScorePutts(getShort(rs,"ScorePutts") );
            scoreStableford.setScoreBunker(getShort(rs,"ScoreBunker") );
            scoreStableford.setScorePenalty(getShort(rs,"ScorePenalty") );
   return scoreStableford;
   
 }catch(Exception e){
        handleGenericException(e, methodName);
    return null;
 }
} //end method
} // end class