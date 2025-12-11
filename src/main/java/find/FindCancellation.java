package find;

import entite.Club;
import entite.composite.ECourseList;
import entite.Round;
import entite.UnavailablePeriod;
import static interfaces.Log.LOG;
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

public class FindCancellation implements interfaces.GolfInterface
{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 
    private static List<ECourseList> liste = null;
    
public List<ECourseList> find(final UnavailablePeriod unavailable, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.debug("entering : " + CLASSNAME); 
 //   LOG.debug("starting findCotisation.find for player = " + player.toString());
   // LOG.debug("starting findCotisation.find for round = " + round.toString());
 //   LOG.debug("starting findUnavailable.find for course = " + course.toString());
        LOG.debug("connection = " + conn);
    PreparedStatement ps = null;
    ResultSet rs = null;
 try{ 
   //  String u = utils.DBMeta.listMetaColumnsLoad(conn, "unavailable");
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
    
    final String query = 
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
        LOG.debug("ResultSet FindCancellation has " + rs.getRow() + " lines.");
 //    Unavailable unavailable= new Unavailable();
 //   if(rs.getRow() == 0){
  //          String msg = "il n'y a pas de cancellation pour ce round !" ;
  //          LOG.debug(msg);
  //          liste = null;
 //           return unavailable;
 //    }
//     if(rs.getRow() > 1){
//            String msg = "il y a TROP d'indisponibilité pour ce round !" ;
//            LOG.debug(msg);
//            showMessageFatal(msg);
//            return unavailable;
//     }
 //   if(rs.getRow() == 1){
  //          String msg = "il y a UNE indisponibilité pour ce round !" ;
  //          LOG.debug(msg);
  //          showMessageFatal(msg);
         //   return null;
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.debug("just before while ! ");
       liste = new ArrayList<>();
       while(rs.next()){
          ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
          ecl.setPlayer(entite.Player.map(rs));
          
          Club c = new Club();
          c = entite.Club.dtoMapper(rs);
          ecl.setClub(entite.Club.dtoMapper(rs));
          ecl.setCourse(entite.Course.dtoMapper(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,c)); // mod 19-02-2020 pour générer ZonedDateTime
          ecl.setInscription(entite.Inscription.map(rs));//.setInscriptionNew(i);
	liste.add(ecl);
	}
       liste.forEach(item -> {
           LOG.debug("Cancellation list " + item + "/");
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
    String msg = "Exception in " + CLASSNAME + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws SQLException, Exception // testing purposes
{
  //  LOG.debug("Input main = " + s);
//    DBConnection dbc = new DBConnection();
    Connection conn = new DBConnection().getConnection();
 //   Player player = new Player();
 //   player.setIdplayer(324713);
    Round round = new Round(); 
    round.setIdround(260); // faut compléter rounddate
    round.setRoundDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    UnavailablePeriod unavailable = new UnavailablePeriod();
    unavailable.setStartDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
    unavailable.setEndDate(LocalDateTime.of(2019, Month.MARCH, 23, 9,57));
  //  faut compléter start date et enddate LocalDateTime
    List<ECourseList> p1 = new FindCancellation().find(unavailable, round, conn);
    LOG.debug("cancellation found = " + p1.toString());
//for (int x: par )
//        LOG.debug(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

