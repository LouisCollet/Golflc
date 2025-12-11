package lists;

import entite.Club;
import entite.composite.ECourseList;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class HandicapList {
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<ECourseList> list(final Player player, final Connection conn) throws SQLException{  
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
 //    String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
 //    String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
 //    String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
 //    String ha = utils.DBMeta.listMetaColumnsLoad(conn, "handicap");
/*String query =
        "SELECT "
        +  co + "," + ro + "," + pl + "," + ha
        + " FROM handicap, round, course, player"
        + " WHERE handicap.round_idround = round.idround"
        + "     and round.course_idcourse = course.idcourse"
        + "     and player.idplayer = ?"
	+"      and handicap.player_idplayer = player.idplayer"
        + " GROUP by idhandicap"
        + " ORDER by idhandicap DESC"
    ;
*/
 final String query = """
             \n   /* lists.HandicapList.list  */
        SELECT *
        FROM handicap, round, course, player
        WHERE handicap.round_idround = round.idround
             AND round.course_idcourse = course.idcourse
             AND player.idplayer = ?
             AND handicap.player_idplayer = player.idplayer
        GROUP by idhandicap
        ORDER by idhandicap DESC
    """;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
    liste = new ArrayList<>();
     while(rs.next()){
	     ECourseList ecl = new ECourseList();
             ecl.setHandicap(entite.Handicap.map(rs));
             ecl.setRound(new entite.Round().dtoMapper(rs,new Club()));
             ecl.setCourse(entite.Course.dtoMapper(rs));
	liste.add(ecl);
      }
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
    //     return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
  return liste;
}catch (SQLException e){
        String msg = "SQL Exception in " + methodName + " /" + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        HandicapList.liste = liste;
    }
    
    void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    List<ECourseList> p1 = new HandicapList().list(player, conn);
        LOG.debug("Handicap list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class