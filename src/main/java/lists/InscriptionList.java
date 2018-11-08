package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class InscriptionList implements interfaces.Log
{
    private static List<ECourseList> liste = null;
    
public List<ECourseList> getInscriptionList(final Connection conn) throws SQLException
{
    LOG.info(" ... entering InscriptionList !!");
if (liste == null)
{    
  //  Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
    
     LOG.info("starting getInscriptionList.. = " );
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  //   String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
 
String query =
        "SELECT "
        + cl + "," + co + "," + ro + //"," + // pl + "," +
          // attention faut un espace en fin de ligne avant le " !!!!
//" SELECT  RoundDate, idround, RoundQualifying, roundgame, RoundCompetition, RoundHoles, RoundPlayers, RoundStart, " +
//"         idcourse, CourseName, idclub, ClubName, clubcity, clubcountry, ClubWebsite" +
          
    "		 FROM round" +
    "		   JOIN course	ON round.course_idcourse = course.idcourse" +
    "			 JOIN club 	ON club.idclub = course.club_idclub" +
    "			    WHERE RoundDate > DATE_SUB(current_date(),INTERVAL 03 month)" +
    "				    GROUP BY idround" +
    "                       ORDER by rounddate desc "
;

       // LOG.info("player = " + player.getIdplayer() ) ;
     ps = conn.prepareStatement(query);
///     ps.setInt(1, player.getIdplayer());
///     ps.setString(2, formula.toUpperCase() ); // new 30/6/2015
         utils.LCUtil.logps(ps);
		//get round data from database
	rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet getInscriptionList has " + rs.getRow() + " lines.");
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
          //LOG.info("just before while ! ");
	while(rs.next())
       {
			//LOG.info("just after while ! ");
          ECourseList ecl = new ECourseList(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
          Club c = new Club();
          c = entite.Club.mapClub(rs);
   //          c.setIdclub(rs.getInt("idclub") );
   //          c.setClubName(rs.getString("clubName") );
   //          c.setClubCity(rs.getString("clubcity"));
   //          c.setClubWebsite(rs.getString("ClubWebsite"));
   //          c.setClubCountry(rs.getString("ClubCountry"));
          ecl.setClub(c);
          
          Course o = new Course();
          o = entite.Course.mapCourse(rs);
      //      o.setIdcourse(rs.getInt("idcourse"));
      //      o.setCourseName(rs.getString("CourseName") );
          ecl.setCourse(o);
          
          Round r = new Round();
          r = entite.Round.mapRound(rs);
  /*          r.setIdround(rs.getInt("idround") );
                java.util.Date d = rs.getTimestamp("roundDate");
            //    LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
          //  LocalDateTime date = DatetoLocalDateTime(d);
            r.setRoundDate(DatetoLocalDateTime(d));
            r.setRoundGame(rs.getString("roundgame") );
            r.setRoundCompetition(rs.getString("RoundCompetition") );
            r.setRoundHoles(rs.getShort("RoundHoles") );
            r.setRoundPlayers(rs.getShort("RoundPlayers") ); // new 20/06/2017
            r.setRoundStart(rs.getShort("RoundStart") );*/
          ecl.setRound(r);

 			//store all data into a List
	liste.add(ecl);
	} //end while
// LOG.info("Inscription liste = " + liste.toString());
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in InscriptionList= " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in getInscriptionList() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception = " + ex.toString() );
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
}else{
    //     LOG.debug("escaped to listinscription repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
} //end else    
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        InscriptionList.liste = liste;
    }
} //end class