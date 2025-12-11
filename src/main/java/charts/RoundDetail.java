
package charts;

import entite.Average;
import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;

// not used !!
public class RoundDetail {
    private static List<Average> listAverage = null;

public List<Average> getRoundDetail(final Connection conn,
        final Player player,
      //  final Course course,
        final Round round) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      LOG.debug(" ... starting getRoundDetail with player = " + player + " //round  = " + round);
      // not used ??
      //query à modifier, voir CourseAverage avec with ....as
      // three " mod 09-12-2024 Text Bloc
  String query = """
   SELECT scorehole, scorepar, scorestrokeindex, scoreextrastroke,
     round( avg(scorestroke),1 ) as averageStroke,
     round( avg(scorepoints),1 ) as averagePoints,
      count(distinct idround) as countround
   FROM score, round, course
   WHERE
     round.idround = ?
     and score.player_has_round_player_idplayer = ?
     and score.player_has_round_round_idround = round.idround
   GROUP BY scorehole
   ORDER by scorehole
   """  ;
        LOG.debug("player = " + player.getIdplayer());
        LOG.debug("round = " + round.getIdround() );
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround() );
     ps.setInt(2, player.getIdplayer());

      utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
       LOG.debug("ResultSet getStatAvg has " + rs.getRow() + " lines.");
 //   chart = new String [rs.getRow()][6]; // taille array en fonction des parties jouées sur le parcours
    listAverage = new ArrayList<>();
    Average average = new Average();
    int i = 0;
while(rs.next()){
            i++;
         average = entite.Average.map(rs);
            listAverage.add(average);
} //end while
// log avec i
      LOG.debug("listavg after while = " + listAverage.toString() );
    return listAverage;
}catch (SQLException e){
    String msg = "SQL Exception in getRoundetail = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        utils.LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Other Exception in getRoundDetail()  " + ex;
    LOG.error(msg);
    utils.LCUtil.showMessageFatal(msg);
     return null;
}finally{
     DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
} //end class