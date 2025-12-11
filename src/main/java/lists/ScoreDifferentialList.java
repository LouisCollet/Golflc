package lists;

import entite.HandicapIndex;
import entite.Player;
import static interfaces.GolfInterface.ZDF_TIME_DAY;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ScoreDifferentialList {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    static List<HandicapIndex> liste = null;

    public List<HandicapIndex> list(final Player player, final String type, final Connection conn) throws SQLException{   
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
       LOG.debug("entering : " + methodName);
       LOG.debug(" for player = " + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  String query = null;
//////////////////////////////////////////  
if(type.equals("<20")){
  query = """
     SELECT *
      FROM handicap_index
      WHERE handicap_index.HandicapPlayerId = ?
      ORDER BY HandicapDate desc
      LIMIT 20
   """ ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
} // end < 20
///////////////////////////////////////////
if(type.equals(">20")){
/*  https://logicalread.com/mysql-session-variables-mc13/#.YR-AN44zZPZ  
    MySQL also lets you create temporary tables with the CREATE TEMPORARY TABLE command.
    These tables are so-called because they remain in existence only for the duration of a single MySQL session 
    and are automatically deleted when the client that instantiates them closes its connection with the MySQL server.
    These tables come in handy for transient, session-based data or calculations, or for the temporary storage of data.
    And because they’re session-dependent, two different sessions can use the same table name without conflicting.

Since temporary tables are stored in memory, they are significantly faster than disk-based tables. 
    Consequently, they can be effectively used as intermediate storage areas, to speed up query execution
    by helping to break up complex queries into simpler components, 
    or as a substitute for subquery and join support.
    */
// selection des 20 derniers SD
    LOG.debug("create temporary table with 20 last SD");
  query = """
     CREATE TEMPORARY TABLE top_differentials
     SELECT *
     FROM handicap_index
     WHERE handicap_index.HandicapPlayerId = ?
     ORDER BY HandicapDate desc
     LIMIT 20;
""";
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     int rows =  ps.executeUpdate();
     LOG.debug("rows = " + rows);
     
 // selection des 8 plus petits SD
   LOG.debug("select 8 lowest SD from temporary table");
 query = """ 
      SELECT *
      FROM top_differentials
      ORDER BY HandicapScoreDifferential asc
      LIMIT 8;
  """ ;
     ps = conn.prepareStatement(query);
     rs =  ps.executeQuery();
} // end > 20

     liste = new ArrayList<>();
      while(rs.next()){
         liste.add(entite.HandicapIndex.map(rs));
     }
 //       LOG.debug("Number of Score Differentials = " + i);
 //   A TEMPORARY table is visible only to the current connection,
//  and is dropped automatically when the connection is closed. 
// on garde la même connection si on fait deux fois le calcul de suite !!
  //   query = """
   //        DROP TEMPORARY TABLE IF EXISTS top_differentials;
   //  """ ;
     ps = conn.prepareStatement("DROP TEMPORARY TABLE IF EXISTS top_differentials");
     utils.LCUtil.logps(ps);
     int rows =  ps.executeUpdate();
        LOG.debug("temporary table 'top_differentials' dropped (0=OK) : " + rows);
  
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return liste;
     }else{
         LOG.debug("ResultSet Number of Score Differentials = " + methodName + " has " + liste.size() + " lines.");
         liste.forEach(item->LOG.debug("HandicapIndex list : Handicap = " + item.getHandicapWHS() + " date = " + item.getHandicapDate() + " SD = " + item.getHandicapScoreDifferential()));
     }
 return liste;
}catch (SQLException e){
       String msg = "SQL Exception = " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return null;
}catch (Exception ex){
       String msg = "Exception in " + methodName + ex.toString();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws SQLException, Exception{
  Connection conn = new DBConnection().getConnection();
  Player player = new Player();
  player.setIdplayer(324713); // 456781
  List<HandicapIndex> li = new ScoreDifferentialList().list(player,">", conn);
  LOG.debug("nombre items = " + li.size());
  li.forEach(item -> LOG.debug("Round  Date " + item.getHandicapDate().format(ZDF_TIME_DAY)
                                  + " - SD = " + item.getHandicapScoreDifferential()
                                  )
               );
  
  
 //   LOG.debug("main : player averageHcp WHS = " + li.toString());
  DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class