
package lists;

import entite.Club;
import entite.composite.ECourseList;
import entite.Player;
import entite.Round;
import exceptions.LCException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ScoreCardList1EGA implements interfaces.Log{
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<ECourseList> list(final Player player, final Round round, final Connection conn) throws SQLException, NullPointerException, LCException{  
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
    LOG.debug("... entering " + methodName);
    LOG.debug(" with player = " + player.toString());
    LOG.debug(" for round : {} with listsc1 = {}", round, liste);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        // String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
  //   String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ha = utils.DBMeta.listMetaColumnsLoad(conn, "Handicap");
String query =
        "SELECT" + pl + "," + ha + "," + ro
        + " FROM player, handicap, round"
        + " WHERE"
        + "   player.idplayer=?"
        + "   AND round.idround=?"
        + "   AND handicap.player_idplayer = player.idplayer"
        + "   AND date(RoundDate) between idhandicap and handicapend"
   ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
	while(rs.next()){
          ECourseList ecl = new ECourseList();
          ecl.setPlayer(entite.Player.map(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,new Club()));
          ecl.setHandicap(entite.Handicap.map(rs));
	liste.add(ecl);
	} //end while
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
        
 ///   LOG.debug("exiting ScoreCard1List with " + liste.toString());
    return liste;

}catch (SQLException e){
    String msg = " SQL Exception in ScoreCardList1() " + e;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in ScoreCardList1() " + ex;
     throw new LCException(msg);
  //  rethrow common technique used to encapsulate exceptions
  //  LCUtil.showMessageFatal(msg);
  //  return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
      //     LOG.debug("escaped to ScoreCard1 repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        ScoreCardList1EGA.liste = liste;
    }
    
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round(); 
        round.setIdround(578);
       List<ECourseList> ec = new ScoreCardList1EGA().list(player, round, conn);
        LOG.debug("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class