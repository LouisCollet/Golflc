package lists;

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

public class HandicapIndexList {
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<ECourseList> list(final Player player, final Connection conn) throws SQLException{  
if(liste == null){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{

 final String query ="""
  WITH selection AS (
     SELECT * from handicap_index, player_has_round, player
     WHERE player.idplayer = ?
       AND handicap_index.HandicapPlayerId = player.idplayer
       AND player_has_round.InscriptionIdPlayer = player.idplayer
       AND player_has_round.InscriptionIdRound = handicap_index.HandicapRoundId
      )
  SELECT * FROM selection
     JOIN round
         ON round.idround = selection.HandicapRoundId
     JOIN course
          ON course.idcourse = round.course_idcourse
     JOIN club
          ON club.idclub = course.club_idclub
     ORDER BY selection.HandicapDate DESC
     LIMIT 30
 """;             
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
	     ECourseList ecl = new ECourseList();
             ecl.setHandicapIndex(entite.HandicapIndex.map(rs));
             ecl.setInscription(entite.Inscription.map(rs));
             ecl.setRound(new entite.Round().dtoMapper(rs)); // genère club = null
             ecl.setClub(entite.Club.dtoMapper(rs)); // new 17-04-2025
             ecl.setCourse(entite.Course.dtoMapper(rs));
	liste.add(ecl);
      }
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
    return liste;
}catch (SQLException e){
  //      String msg = "Vous devez d'abord participer à une compétition !";
        String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageInfo(msg);
        return liste;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return liste;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;
}
} //end method

 public static List<ECourseList> getListe() {
        return liste;
    }

 public static void setListe(List<ECourseList> liste) {
        HandicapIndexList.liste = liste;
    }

 void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    List<ECourseList> li = new HandicapIndexList().list(player, conn);
        LOG.debug("HandicapIndexlist = " + li.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class