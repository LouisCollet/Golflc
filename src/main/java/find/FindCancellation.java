package find;

import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.Unavailable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class FindCancellation implements interfaces.Log, interfaces.GolfInterface
{
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    private static List<ECourseList> liste = null;
    
public List<ECourseList> find(final Unavailable unavailable, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2); 
 //   LOG.info("starting findCotisation.find for player = " + player.toString());
   // LOG.info("starting findCotisation.find for round = " + round.toString());
 //   LOG.info("starting findUnavailable.find for course = " + course.toString());
        LOG.info("connection = " + conn);
    PreparedStatement ps = null;
    ResultSet rs = null;
 try{ 
   //  String u = utils.DBMeta.listMetaColumnsLoad(conn, "unavailable");
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
    
    String query = 
     "SELECT " + cl + "," + co + "," + ro + "," + pl + "," + ph +
"     FROM player" +
"     JOIN player_has_round" +
"         ON InscriptionIdPlayer = player.idplayer" +
"     AND player_has_round.InscriptionFinalResult = 0" +
"      JOIN round" +
"         ON InscriptionIdRound = round.idround" +
"           AND DATE(round.RoundDate) >= DATE(?) " +
"           AND DATE(round.RoundDate) <= DATE(?)" +
"           AND DATE(round.RoundDate) > NOW()" +
"      JOIN course" +
"         ON course.idcourse = round.course_idcourse" +
"      JOIN club" +
"       ON club.idclub = course.club_idclub" +
"      ORDER by date(RoundDate) DESC"
   ;
    
   ps = conn.prepareStatement(query);
 //       java.sql.Timestamp ts = Timestamp.valueOf(unavailable.getStartDate());
      ps.setTimestamp(1,Timestamp.valueOf(unavailable.getStartDate()));
  //      ts = Timestamp.valueOf(unavailable.getEndDate());
      ps.setTimestamp(2,Timestamp.valueOf(unavailable.getEndDate()));
  //  ps.setDate(2, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindCancellation has " + rs.getRow() + " lines.");
 //    Unavailable unavailable= new Unavailable();
 //   if(rs.getRow() == 0){
  //          String msg = "il n'y a pas de cancellation pour ce round !" ;
  //          LOG.info(msg);
  //          liste = null;
 //           return unavailable;
 //    }
//     if(rs.getRow() > 1){
//            String msg = "il y a TROP d'indisponibilité pour ce round !" ;
//            LOG.info(msg);
//            showMessageFatal(msg);
//            return unavailable;
//     }
 //   if(rs.getRow() == 1){
  //          String msg = "il y a UNE indisponibilité pour ce round !" ;
  //          LOG.info(msg);
  //          showMessageFatal(msg);
         //   return null;
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
liste = new ArrayList<>();
while(rs.next()){
          ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
          
          Player p = new Player();
          p = entite.Player.mapPlayer(rs);
          ecl.setPlayer(p);
          
          Club c = new Club();
          c = entite.Club.mapClub(rs);
          ecl.setClub(c);
          
          Course o = new Course();
          o = entite.Course.mapCourse(rs);
          ecl.setCourse(o);
          
          Round r = new Round();
      //    r = entite.Round.mapRound(rs);
          r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 pour générer ZonedDateTime
          ecl.setRound(r);

          Inscription i = new Inscription();
          i = entite.Inscription.mapInscription(rs);  
       //        phr.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult"));
          ecl.setInscriptionNew(i);//.setInscriptionNew(i);
	liste.add(ecl);
	}
       liste.forEach(item -> {
           LOG.info("Cancellation list " + item + "/");
     });  // java 8 lambda     
      return liste;
 //    }     
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
 //return null;
}//end method

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
  //  LOG.info("Input main = " + s);
//    DBConnection dbc = new DBConnection();
    Connection conn = new DBConnection().getConnection();
 //   Player player = new Player();
 //   player.setIdplayer(324713);
    Round round = new Round(); 
    round.setIdround(260); // faut compléter rounddate
    round.setRoundDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    Unavailable unavailable = new Unavailable();
    unavailable.setStartDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    unavailable.setEndDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
  //  faut compléter start date et enddate LocalDateTime
    List<ECourseList> p1 = new FindCancellation().find(unavailable, round, conn);
    LOG.info("cancellation found = " + p1.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

