
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
//import utils.LCUtil;

/**
 *
 * @author collet
 */
public class RoundDetail {
    private static List<Average> listavg = null;

public List<Average> getRoundDetail(final Connection conn, final Player player, final Course course,
        final Round round) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
      LOG.info(" ... starting getRoundDetail with player = " + player + " //round  = " + round);
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
   " SELECT scorehole, scorepar, scorestrokeindex, scoreextrastroke," +
"	 round( avg(scorestroke),1 ) as averageStroke," +
"	 round( avg(scorepoints),1 ) as averagePoints," +
      " count(distinct idround) as countround " +
"    from score, round, course" +
"   WHERE" +
"	round.idround = ?" +
"	and score.player_has_round_player_idplayer = ?" +
"	and score.player_has_round_round_idround = round.idround" +
"   GROUP BY scorehole" +
"   ORDER by scorehole"
     ;
        LOG.info("player = " + player.getIdplayer());
        LOG.info("round = " + round.getIdround() );
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround() );
     ps.setInt(2, player.getIdplayer());

      utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
       LOG.info("ResultSet getStatAvg has " + rs.getRow() + " lines.");
 //   chart = new String [rs.getRow()][6]; // taille array en fonction des parties jouées sur le parcours
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    listavg = new ArrayList<>();
    Average cc = new Average();
while(rs.next())
{
            cc = new Average();
            cc.setAvgHole(rs.getShort("ScoreHole") );
            cc.setAvgPar(rs.getShort("ScorePar") );
            cc.setAvgStrokeIndex(rs.getShort("ScoreStrokeIndex") );
            cc.setAvgExtraStroke(rs.getShort("ScoreExtraStroke") );
            cc.setAvgStroke(rs.getShort("averageStroke") );
            cc.setAvgPoints(rs.getShort("averagePoints") );
            cc.setCountRounds(rs.getShort("countround") );
            listavg.add(cc);			//store all data into a List
            //    LOG.info("just after add to listsc3");
} //end while

      LOG.info("listavg after while = " + listavg.toString() );
//      String [][] arlc = (String[][])listsc3.toArray();  // truc essentiel ?? list array vers array 2 D
//   LOG.info(" list to array 2D = " + NEWLINE + Arrays.deepToString(arlc) );
   //  LOG.debug(Arrays.deepToString(listsc3.toArray().toString() ));
  //    LOG.info("listsc3 after while = " + Arrays.toString(listsc3.toArray() ) );
//}
    return listavg;
}
catch (SQLException e)
{
    String msg = "SQL Exception in getRoundetail = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        utils.LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    String msg = "NullPointerException in getRoundDetail() " + npe;
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
