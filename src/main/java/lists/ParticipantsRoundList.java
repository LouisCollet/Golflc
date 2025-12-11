package lists;

import entite.Classment;
import entite.composite.ECourseList;
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

public class ParticipantsRoundList implements Serializable, interfaces.Log{
    private static List<ECourseList> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 
  public List<ECourseList> list(final Round round ,final Connection conn) throws SQLException{ 
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    
if(liste == null){
    LOG.debug(" ... entering " + methodName);
    LOG.debug(" with Round = " + round);
    
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    String query = """
       SELECT *
       FROM player
       JOIN round
          ON round.idround = ?
       JOIN course
          ON round.course_idcourse = course.idcourse
       JOIN club
          ON course.club_idclub = club.idclub
       JOIN player_has_round
          ON  InscriptionIdPlayer = player.idplayer
          AND InscriptionIdRound = round.idround
       ORDER by player_has_round.InscriptionFinalResult,In‌scriptionMatchplayTeam asc;
    """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround() ); 
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
          ECourseList ecl = new ECourseList();
          ecl.setClub(entite.Club.dtoMapper(rs));
          ecl.setCourse(entite.Course.dtoMapper(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
          ecl.setInscription(entite.Inscription.map(rs));
          ecl.setPlayer(entite.Player.map(rs));
          Classment cla = new read.ReadClassment().read(ecl.getPlayer(), ecl.getRound(),conn);
          ecl.setClassment(cla);
	liste.add(ecl);
	} // end while
    if(liste.isEmpty()){
       String msg = "Empty List in " + methodName;
       LOG.info(msg);
       LCUtil.showMessageInfo(msg);
 //      return null;
    }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }    
    LOG.debug("ending with liste  =  ",  Arrays.deepToString(liste.toArray()) );
    
// https://stackoverflow.com/questions/369512/how-to-compare-objects-by-multiple-fields
// https://stackoverflow.com/questions/51565422/java-8-compare-multiple-fields-in-different-order-using-comparator

    liste.sort(Comparator.comparingInt((ECourseList p)-> p.getClassment().getTotalPoints() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.getClassment().getLast9() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.getClassment().getLast6() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.getClassment().getLast3() ).reversed()
                     .thenComparingInt((ECourseList p)-> p.getClassment().getLast1() ).reversed()
            );
     liste.forEach(item -> LOG.debug("liste AFTER sort = " + NEW_LINE + item.getPlayer().getPlayerFirstName() + " / "
                                            + item.getPlayer().getIdplayer() + " /" + item.getClassment()));
    return liste;
}catch (SQLException e){
        String msg = "SQL Exception in " + methodName + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getParticipantsStableford() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
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
        ParticipantsRoundList.liste = liste;
    }

void main() throws SQLException, Exception {
    Connection conn = new DBConnection().getConnection();
   Round round = new Round(); 
   round.setIdround(656);
  List<ECourseList> p1 = new ParticipantsRoundList().list(round, conn);
//  Classment c = new Classment();
      LOG.debug("Inscription list = " + p1.toString());
  p1.forEach(item -> LOG.debug("Participants Stableford list = " + item.getClassment() + "/" + item.getPlayer().getPlayerLastName()));  // java 8 lambda
  p1.forEach(item -> LOG.debug("Participants Round list = " + item.getPlayer().getIdplayer()
           + "/" + item.getInscription().getInscriptionMatchplayTeam()));
       //   getPlayer().getPlayerLastName()));  // java 8 lambda                     
     
     
     DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class