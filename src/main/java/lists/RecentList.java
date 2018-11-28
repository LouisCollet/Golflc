package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class RecentList implements interfaces.Log, interfaces.GolfInterface
{
      private static List<ECourseList> liste = null;

public List<ECourseList> getRecentRoundList(final Player player, final Connection conn) throws SQLException
{
//    LOG.info("starting get RecentRoundList with list = " + liste);
if(liste == null)
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
  //   String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
    "SELECT "
          + cl + "," + co + "," + ro + "," + pl // + "," + ph
  //        + "idplayer, RoundDate, idround, RoundQualifying, roundgame, RoundCompetition, RoundHoles,"
  //        + "          idcourse, CourseName, idclub, ClubName, ClubCity, ClubWebsite, ClubLatitude, ClubLongitude"
          + "  FROM player"
          + "  JOIN player_has_round"
          + "    	ON player_has_round.player_idplayer = player.idplayer"
          + "  JOIN round"
          + "        ON round.idround = player_has_round.round_idround "
          + "  JOIN course"
          + "        ON course.idcourse = round.course_idcourse"
          + "  JOIN club"
          + "        ON club.idclub = course.club_idclub"
          + "  WHERE " //player.idplayer = ?"
          + "	     RoundDate > DATE_SUB(current_date() , INTERVAL 60 month)" // and
          + "  GROUP BY idround"  // new 04/06/2017
          + "  ORDER by date(RoundDate) desc"
     ;
        LOG.info("player = " + player) ;
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
	while(rs.next())
        {
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
	liste.add(ecl);
	} // end while
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){   
    String msg = "NullPointerException in getRecentRoundList()" + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}catch (Exception ex){
    String msg = "Exception in getRecentRoundList()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
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
        RecentList.liste = liste;
    }

} // end Class