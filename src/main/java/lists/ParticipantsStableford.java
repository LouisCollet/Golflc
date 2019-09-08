package lists;

import entite.Classment;
import entite.Club;
import entite.Course;
import entite.ECourseList;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

/** extrait database les participants (2 ou 4) à un matchplay 
 *
 * @author collet
 */

public class ParticipantsStableford implements Serializable, interfaces.Log{
    private static List<ECourseList> liste = null;
    
    public List<ECourseList> listAllParticipants(final Round round ,final Connection conn) throws SQLException        
{   
    LOG.info(" ... entering ParticipantsStableford !! with Round = " + round.getIdround() );
if(liste == null)
{   
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{        LOG.debug("starting getParticipantsStableford ...for round  = "  + round.getIdround() );
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
   //  String ha = utils.DBMeta.listMetaColumnsLoad(conn, "Handicap");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
    String query =
          "SELECT "
               + cl + "," + co + "," + ro + "," + pl + "," + ph +
    //        + "idround, idplayer, round.RoundGame, playerLastName, playerFirstName, playerPhotoLocation, " +
//   "		  RoundDate, round.RoundCompetition, round.RoundGame, round.roundTeam, " +
//   "		  InscriptionFinalResult, course.CourseName, course.idcourse, club.ClubName, club.idclub ," + 
//  "        RoundCompetition, round.RoundMatchplayResult, uncompress(round.RoundScoreStringCompressed) " +
            
"       FROM player " +
"       JOIN round " +
"           ON round.idround = ? " +
"       JOIN course " +
"       	ON round.course_idcourse = course.idcourse " +
"       JOIN club " +
"       	ON course.club_idclub = club.idclub " +
"       JOIN player_has_round " +
"           ON  InscriptionIdPlayer = player.idplayer " +
"           AND InscriptionIdRound = round.idround " +
//"        GROUP BY roundgame" +   // new 30/06/2016
"        ORDER by player_has_round.InscriptionFinalResult DESC " 
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround() ); 
    //     //    String p = ps.toString();
      utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    //  à faire :
    // compressed string ==> chaqnger dans array
    // looop pour cahrger array players
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ParticipantsStableford has {} players ", rs.getRow() );
       rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
 //   int rowNum = 0; //The method getRow lets you check the number of the row
        //              rowNum = rs.getRow() - 1;
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
          ecl.setInscriptionNew(i);//.setInscriptionNew(i);

          Player p = new Player();
          p = entite.Player.mapPlayer(rs);  
          ecl.setPlayer(p);
          
          Classment cla = new Classment();
          find.FindClassmentElements fcel = new find.FindClassmentElements();
          cla = fcel.findClassment(ecl.Eplayer.getIdplayer(), ecl.Eround.getIdround(), conn);
          ecl.setClassment(cla);
			//store all data into a List
	liste.add(ecl);
	}
    //            boucler sur la liste ??
    LOG.info("ending liste  =  ",  Arrays.deepToString(liste.toArray()) );
    
// https://stackoverflow.com/questions/369512/how-to-compare-objects-by-multiple-fields
  //LOG.info
  //liste.forEach(item -> LOG.info("liste before sort " + item));
  liste.forEach(item -> LOG.info("liste BEFORE sort " + NEW_LINE + item.Eplayer.getPlayerFirstName()+ item.Eclassment));
/* bug !!
  liste.sort(Comparator.comparingInt((ECourseList p)->p.Eclassment.getTotalPoints() ).reversed()  // descending
   //        liste.sort(Comparator.comparingInt((ECourseList p)->p.Eclassment.getLast3() ).reversed()
               //    .thenComparing((ECourseList p)->p.Eclassment.getTotalPoints() )
              .thenComparingInt((ECourseList p)->p.Eclassment.getLast9() ).reversed()
              .thenComparingInt((ECourseList p)->p.Eclassment.getLast6() ).reversed()
                 .thenComparingInt((ECourseList p)->p.Eclassment.getLast3() ).reversed()
                .thenComparingInt((ECourseList p)->p.Eclassment.getLast1() ).reversed()
            );
  liste.forEach(item -> LOG.info("liste AFTER sort1 " + NEW_LINE + item.Eplayer.getPlayerFirstName()+ item.Eclassment));
  */
   // attention au au -p
   ///https://stackoverflow.com/questions/51565422/java-8-compare-multiple-fields-in-different-order-using-comparator
   //There is no need to use the method Comparator::reverse. 
   //Since you want to reverse the comparison based on the integer, just negate the age -p.getAge()
// and it will be sorted in the descending order:
    liste.sort(Comparator.comparingInt((ECourseList p)-> -p.Eclassment.getTotalPoints() )  // -p=descending trick !!!
                     .thenComparingInt((ECourseList p)-> -p.Eclassment.getLast9() )
                     .thenComparingInt((ECourseList p)-> -p.Eclassment.getLast6() )
                     .thenComparingInt((ECourseList p)-> -p.Eclassment.getLast3() )
                     .thenComparingInt((ECourseList p)-> -p.Eclassment.getLast1() )
            );
     liste.forEach(item -> LOG.info("liste AFTER sort2 " + NEW_LINE + item.Eplayer.getPlayerFirstName() + " / "
                                                                    + item.Eplayer.getIdplayer()+ item.Eclassment));
 
    return liste;
}catch (NullPointerException npe){ 
    String msg = "NullPointerException in ParticipantsStableford() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
        String msg = "SQL Exception in ParticipantsStableford (): " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getParticipantsStableford() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //    LOG.debug("escaped to listParticipants repetition with lazy loading");
    return liste;  //plusieurs fois ??
    }
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        ParticipantsStableford.liste = liste;
    }

    
      public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
   Round round = new Round(); 
   round.setIdround(414);
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<ECourseList> p1 = new ParticipantsStableford().listAllParticipants(round, conn);
        LOG.info("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
    
    
    
    
    
    
} //end class