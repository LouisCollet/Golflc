
package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Player;
import entite.PlayerHasRound;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class PlayedList implements interfaces.Log
{
     private static List<ECourseList> liste = null;
    
public List<ECourseList> getPlayedList(final Player player, final Connection conn) throws SQLException
        // pour un joueur particulier !!!
    
{ //  LOG.debug("starting getPlayedList(), Connection = " + conn);
    
if (liste == null)
{
    LOG.debug("starting getPlayedList(), Player = {}", player.getIdplayer());
    LOG.debug("starting PlayedList(), listplayer = {}", liste);
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
     LOG.info("starting getPlayedList.. = " );
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
     "SELECT idplayer, RoundDate, idround, player_has_round.InscriptionFinalResult,"
   + " player_has_round.Player_has_roundZwanzeursResult, player_has_round.Player_has_roundZwanzeursGreenshirt,"
   + " RoundQualifying, roundgame, RoundCompetition, RoundHoles, RoundStart,"
   + " idcourse, CourseName, CourseBegin, CourseEnd, "
   + " idclub, ClubName, ClubCity, ClubWebsite, ClubLatitude, ClubLongitude, round.RoundQualifying"
   + "   FROM tee" 
   + "   JOIN player"
   + "      ON player.idplayer = ?"
   + "   JOIN player_has_round"
   + "      ON player_has_round.player_idplayer = player.idplayer"
// mod 14/07/2013  + "      AND player_has_round.InscriptionFinalResult = 0"
   + "   JOIN round"
   + "      ON round.idround = player_has_round.round_idround"
 //  + "      AND RoundDate > DATE_SUB(current_date() , INTERVAL 6 month)" mod 29/03/2016
 //  + "       AND substring(round.roundgame,1,3)= UPPER('sta') " // new line 27/07/2015 mod 29/03/2016
   + "   JOIN course"
   + "      ON course.idcourse = round.course_idcourse"
   + "   JOIN club"
   + "      ON club.idclub = course.club_idclub"
   + "   GROUP by round.idround"
   + "   ORDER by date(RoundDate) desc"
     ;
        LOG.info("player = " + player) ;
    ps = conn.prepareStatement(query);
       ps.setInt(1, player.getIdplayer());
         utils.LCUtil.logps(ps);
		//get round data from database
	rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet getPlayedList has " + rs.getRow() + " lines.");
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
          //LOG.info("just before while ! ");
		while(rs.next())
                {
			//LOG.info("just after while ! ");

                        ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
          Club c = new Club();
          	c.setIdclub(rs.getInt("idclub") );
                c.setClubName(rs.getString("clubName") );
                c.setClubWebsite(rs.getString("clubWebsite") );
                c.setClubLatitude(rs.getBigDecimal("ClubLatitude") );
                c.setClubLongitude(rs.getBigDecimal("ClubLongitude") );
                c.setClubCity(rs.getString("clubcity"));
          ecl.setClub(c);
          
          Course o = new Course();
            o.setIdcourse(rs.getInt("idcourse"));
            o.setCourseName(rs.getString("CourseName") );
            o.setCourseBegin(rs.getDate("courseBegin")); // new 13/06/2015
            o.setCourseEnd(rs.getDate("courseEnd")); // new 13/06/2015
          ecl.setCourse(o);
          
          Round r = new Round();
            r.setIdround(rs.getInt("idround") );
                java.util.Date d = rs.getTimestamp("roundDate");
                LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            r.setRoundDate(date);
            r.setRoundGame(rs.getString("roundgame") );
            r.setRoundCompetition(rs.getString("RoundCompetition") );
            r.setRoundHoles(rs.getShort("RoundHoles") );
            r.setRoundQualifying(rs.getString("RoundQualifying") );
     //       r.setRoundPlayers(rs.getShort("RoundPlayers") ); // new 20/06/2017
            r.setRoundStart(rs.getShort("RoundStart") );
          ecl.setRound(r);

            PlayerHasRound phr = new PlayerHasRound();             
               phr.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult"));
            ecl.setInscription(phr);

                        //LOG.info("inside while : " + ccr.toString());
			//store all data into a List
			liste.add(ecl);
		}
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in getPlayedList() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    String msg = "NullPointerException in getPlayedList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception getPlayedList= " + ex.toString() );
     return null;
}finally{
     //   DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
         LOG.debug("escaped to listPlayed repetition with lazy loading");
    return liste;  //plusieurs fois ??
}

} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        PlayedList.liste = liste;
    }
} //end Class
