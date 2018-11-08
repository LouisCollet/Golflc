
package lists;

//import entite.ScoreMatchplay;
import entite.Classment;
import entite.ECourseList;
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
import static utils.LCUtil.DatetoLocalDateTime;

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
{   
     LOG.debug("starting getParticipantsStableford ...for round  = "  + round.getIdround() );
    String query =
          "SELECT idround, idplayer, round.RoundGame, playerLastName, playerFirstName, playerPhotoLocation, " +
"		  RoundDate, round.RoundCompetition, round.RoundGame, round.roundTeam, " +
"		  InscriptionFinalResult, course.CourseName, course.idcourse, club.ClubName, club.idclub ," + 
"         RoundCompetition, round.RoundMatchplayResult, uncompress(round.RoundScoreStringCompressed) " +
"       FROM player " +
"       JOIN round " +
"           ON round.idround = ? " +
"       JOIN course " +
"       	ON round.course_idcourse = course.idcourse " +
"       JOIN club " +
"       	ON course.club_idclub = club.idclub " +
"       JOIN player_has_round " +
"           ON  player_has_round.player_idplayer = player.idplayer " +
"           AND player_has_round.round_idround = round.idround " +
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
    int rowNum = 0; //The method getRow lets you check the number of the row
        //              rowNum = rs.getRow() - 1;
	while(rs.next())
        {
         ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
         
         ecl.Eclub.setIdclub(rs.getInt("idclub") );
         ecl.Eclub.setClubName(rs.getString("clubName") );
  ////        Club clu = new Club();
  ////           clu.setIdclub(rs.getInt("idclub") );
  ////           clu.setClubName(rs.getString("clubName") );
       //      c.setClubCity(rs.getString("clubcity"));
       //      c.setClubWebsite(rs.getString("ClubWebsite"));
       //      c.setClubCountry(rs.getString("ClubCountry"));
    //   ecl.setClub(ecl.Eclub);
   ////       ecl.setClub(clu);

   ecl.Ecourse.setIdcourse(rs.getInt("idcourse"));
   ecl.Ecourse.setCourseName(rs.getString("CourseName") );
////          Course o = new Course();
////            o.setIdcourse(rs.getInt("idcourse"));
////            o.setCourseName(rs.getString("CourseName") );
////          ecl.setCourse(o);

          Round r = new Round();
            r.setIdround(rs.getInt("idround") );
                java.util.Date d = rs.getTimestamp("roundDate");
          //      LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                
            r.setRoundDate(DatetoLocalDateTime(d));
            r.setRoundGame(rs.getString("roundgame") );
            r.setRoundCompetition(rs.getString("RoundCompetition") );
      //      r.setRoundHoles(rs.getShort("RoundHoles") );
       //     r.setRoundPlayers(rs.getShort("RoundPlayers") ); // new 20/06/2017
       //     r.setRoundStart(rs.getShort("RoundStart") );
            r.setRoundTeam(rs.getString("roundTeam"));
          ecl.setRound(r);

   //      PlayerHasRound phr = new PlayerHasRound();
   //        phr.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult"));
   //      ecl.setInscription(phr);

   /*       Player p = new Player();
            p.setIdplayer(rs.getInt("idplayer") );
                LOG.info("current player  = " + p.getIdplayer());
            p.setPlayerFirstName(rs.getString("playerFirstName") );
            p.setPlayerLastName(rs.getString("playerLastName") );
               LOG.info("playerlastname = " + p.getPlayerLastName());
            p.setPlayerPhotoLocation(rs.getString("playerPhotoLocation") );
          ecl.setPlayer(p);
      */    
            ecl.Eplayer.setIdplayer(rs.getInt("idplayer") );
                LOG.info("current player  = " + ecl.Eplayer.getIdplayer());
            ecl.Eplayer.setPlayerFirstName(rs.getString("playerFirstName") );
            ecl.Eplayer.setPlayerLastName(rs.getString("playerLastName") );
                LOG.info("playerlastname = " + ecl.Eplayer.getPlayerLastName());
            ecl.Eplayer.setPlayerPhotoLocation(rs.getString("playerPhotoLocation") );
          
          
          
          
          Classment cla = new Classment();
            find.FindClassmentElements fcel = new find.FindClassmentElements();
            cla = fcel.findClassment(ecl.Eplayer.getIdplayer(), ecl.Eround.getIdround(), conn);
            LOG.info("");
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

} //end class