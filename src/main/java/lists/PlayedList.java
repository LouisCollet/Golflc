package lists;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Inscription;
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

public class PlayedList implements interfaces.Log
{
     private static List<ECourseList> liste = null;
    
public List<ECourseList> getPlayedList(final Player player, final Connection conn) throws SQLException{
 //  LOG.debug("starting getPlayedList(), Connection = " + conn);
    
if (liste == null)
{
    LOG.debug("starting getPlayedList(), Player = {}", player.getIdplayer());
    LOG.debug("starting PlayedList(), listplayer = {}", liste);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("starting getPlayedList.. = " );
     
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
     
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
     "SELECT "
           + cl + "," + co + "," + ro + "," + pl + "," + ph
  //        + "idplayer, RoundDate, idround, player_has_round.InscriptionFinalResult,"
 //  + " player_has_round.Player_has_roundZwanzeursResult, player_has_round.Player_has_roundZwanzeursGreenshirt,"
 //  + " RoundQualifying, roundgame, RoundCompetition, RoundHoles, RoundStart,"
 //  + " idcourse, CourseName, CourseBegin, CourseEnd, "
//   + " idclub, ClubName, ClubCity, ClubWebsite, ClubLatitude, ClubLongitude, round.RoundQualifying"
          
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
 //       LOG.info("player = " + player.toString()) ;
    ps = conn.prepareStatement(query);
       ps.setInt(1, player.getIdplayer());
         utils.LCUtil.logps(ps);
		//get round data from database
	rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet getPlayedList has " + rs.getRow() + " lines.");
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
	while(rs.next())
        {
          ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
          Club c = new Club();
          c = entite.Club.mapClub(rs);
          ecl.setClub(c);
          
          Course o = new Course();
          o = entite.Course.mapCourse(rs);
          ecl.setCourse(o);
          
          Round r = new Round();
          r = entite.Round.mapRound(rs);
          ecl.setRound(r);

          Inscription i = new Inscription();
          i = entite.Inscription.mapInscription(rs);  
       //        phr.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult"));
          ecl.setInscriptionNew(i);//.setInscriptionNew(i);
	liste.add(ecl);
	}
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in getPlayedList() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
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
    
    public static void main(String[] args) throws SQLException, Exception 
     {
         DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
  try{
    Player player = new Player();
    player.setIdplayer(324713);
    PlayedList pl = new PlayedList();
    List<ECourseList> lp = pl.getPlayedList(player, conn);
        LOG.info("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
    
    
} //end Class
