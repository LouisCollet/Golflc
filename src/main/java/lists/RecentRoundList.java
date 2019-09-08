package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Player;
import entite.Round;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class RecentRoundList implements interfaces.Log, interfaces.GolfInterface{
      private static List<ECourseList> liste = null;

public List<ECourseList> list(final Player player, final Connection conn) throws SQLException{

if(liste == null){
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    LOG.info("starting RecentRoundList ...");
    LOG.info(" with player = " + player.toString()) ;
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
    "SELECT " + cl + "," + co + "," + ro + "," + pl + "," + te
           + "  FROM player"
          + "  JOIN player_has_round"
          + "    	ON InscriptionIdPlayer = player.idplayer"
          + "  JOIN round"
          + "        ON round.idround = player_has_round.InscriptionIdRound "
          + "  JOIN course"
          + "        ON course.idcourse = round.course_idcourse"
          + "  JOIN tee" // new 04-04-2019
          + "        ON tee.idtee = player_has_round.InscriptionIdTee" // new 04-04-2019
          + "  JOIN club"
          + "        ON club.idclub = course.club_idclub"
          + "  WHERE "
          + "	     RoundDate > DATE_SUB(current_date() , INTERVAL 60 month)"  // à réduire
          + "  GROUP BY idround"
    //      + "  ORDER by date(RoundDate) desc"
          + "  ORDER by RoundDate desc" // mod 26-05-2019
     ;
     
    ps = conn.prepareStatement(query);
///       ps.setInt(1, player.getIdplayer());  // mod 04/06/2017
        utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet getRecentRoundList has " + rs.getRow() + " lines.");
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
          //LOG.info("just before while ! ");
	while(rs.next()){
		//LOG.info("just after while ! ");
          ECourseList ecl = new ECourseList();
          Club c = new Club();
          c = entite.Club.mapClub(rs);
          ecl.setClub(c);

          Course o = new Course();
          o = entite.Course.mapCourse(rs);
          ecl.setCourse(o);

          Round r = new Round();
          r = entite.Round.mapRound(rs);
          ecl.setRound(r);
          
          Player p = new Player();
          p = entite.Player.mapPlayer(rs);  
          ecl.setPlayer(p);
          
          Tee t = new Tee();
          t = entite.Tee.mapTee(rs);  
          ecl.setTee(t);
          
          
	liste.add(ecl);
	} // end while
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getRecentRoundList()" + ex;
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
        RecentRoundList.liste = liste;
    }
 public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324713);
        List<ECourseList> ec = new RecentRoundList().list(player,conn);
        LOG.info("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} // end Class