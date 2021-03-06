
package lists;

import entite.Club;
import entite.ECourseList;
import entite.Player;
import entite.Round;
import exceptions.LCCustomException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ScoreCard1List implements interfaces.Log{
    private static List<ECourseList> liste = null;
    
public List<ECourseList> list(final Player player, final Round round,
        final Connection conn) throws SQLException, NullPointerException, LCCustomException{  

if(liste == null){
    LOG.debug("... entering ScoreCard1List !!!");
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
     rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ScoreCardList1 has " + rs.getRow() + " lines.");
        if(rs.getRow() == 0){
            String msg = "££ Empty Result Table for ScoreCard1List ££";
             throw new Exception(msg);
            }    
     rs.beforeFirst(); //on replace le curseur avant la premiÃ¨re ligne
     liste = new ArrayList<>();
//     Club c = new Club();
 //    c = null;

	while(rs.next()){
          ECourseList ecl = new ECourseList();
      //    Player p = new Player();
      //    p = entite.Player.mapPlayer(rs);
          ecl.setPlayer(entite.Player.mapPlayer(rs));

      //    Round r = new Round();
      //    r = entite.Round.mapRound(rs);
      ///    r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 pour générer ZonedDateTime
          ecl.setRound(new entite.Round().mapRound(rs,new Club()));
          
     //     Handicap h = new Handicap();
     //     h = entite.Handicap.mapHandicap(rs);  
          ecl.setHandicap(entite.Handicap.mapHandicap(rs));
	liste.add(ecl);
	} //end while
        
 ///   LOG.info("exiting ScoreCard1List with " + liste.toString());
    return liste;

}catch (SQLException e){
    String msg = " SQL Exception in ScoreCardList1() " + e;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in ScoreCardList1() " + ex;
     throw new LCCustomException(msg);
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
        ScoreCard1List.liste = liste;
    }
    
 public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round(); 
        round.setIdround(436);
       List<ECourseList> ec = new ScoreCard1List().list(player, round, conn);
        LOG.info("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class