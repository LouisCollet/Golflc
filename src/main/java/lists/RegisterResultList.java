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

public class RegisterResultList implements interfaces.Log, interfaces.GolfInterface{
 private static List<ECourseList> liste = null;
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 
public List<ECourseList> list(final Player player, final Connection conn) throws SQLException{
final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    LOG.debug("starting RegisterResultList ...");
    LOG.debug(" with player = " + player) ;
 
    final String query = """
  WITH selection AS (
     SELECT * from player, player_has_round, round
     WHERE player.idplayer = ?
       AND player_has_round.InscriptionIdPlayer = player.idplayer
       AND player_has_round.InscriptionIdRound = round.idround
      )
  SELECT * FROM selection
     JOIN tee
         ON tee.idtee = selection.InscriptionIdTee
     JOIN course
         ON course.idcourse = selection.course_idcourse
     JOIN club
         ON club.idclub = course.club_idclub
     ORDER BY selection.RoundDate DESC
     LIMIT 30;
  """;
  
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());  // mod 12/04/2022
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
          ECourseList ecl = new ECourseList();
          ecl.setClub(entite.Club.dtoMapper(rs));
          ecl.setCourse(entite.Course.dtoMapper(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
          ecl.setPlayer(entite.Player.map(rs));
          ecl.setTee(entite.Tee.dtoMapper(rs));
	liste.add(ecl);
	} // end while
        
      if(liste.isEmpty()){
         String msg = "££ Empty Register Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }     
  return liste;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getRegisterResultList()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
}else{
     //    LOG.debug("escaped to getRecentRoundList repetition thanks to lazy loading");
    return liste;  //not null, donc pas d'acces
}
}//end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        RegisterResultList.liste = liste;
    }
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324715);
        List<ECourseList> ecl = new RegisterResultList().list(player,conn);
        LOG.debug("from main, ec = " + ecl.size());
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
} // end main//
} // end Class