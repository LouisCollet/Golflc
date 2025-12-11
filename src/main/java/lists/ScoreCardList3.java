package lists;

import entite.composite.ECourseList;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class ScoreCardList3 {
    private static List<ECourseList> liste = null; 
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public List<ECourseList> list(final Player player, final Round round ,
     //   final Inscription inscription,
        final Connection conn) throws SQLException{
 final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 

if(liste != null){ // mod 09-04-2023
    LOG.debug("escaped to ScoreCard3List repetition thanks to lazy loading");
  //  LOG.debug("ScoreCard3List = " + liste);
    liste.forEach(item -> LOG.debug("hole = " + item.getScoreStableford().getScoreHole()
                            + " / points = " + item.getScoreStableford().getScorePoints()));
    LOG.debug("total points = " + liste.stream()
             .mapToInt(o->o.getScoreStableford().getScorePoints())
             .sum());
    return liste;
} //end if
    
    LOG.debug("... entering " + methodName);
    LOG.debug("with Player = ... = " + player.toString());
    LOG.debug("with Round = ... = " + round.toString());
//    LOG.debug("with Inscription = ... = " + inscription);

    PreparedStatement ps = null;
    ResultSet rs = null;
try{
 //   String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
 //   String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
 //   String ho = utils.DBMeta.listMetaColumnsLoad(conn, "hole");
  // String cl = utils.DBMeta.listMetaColumnsLoad(conn, "Club");
  //   String co = utils.DBMeta.listMetaColumnsLoad(conn, "Course");
//    String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  
    String query = """
        SELECT *
         FROM course
         JOIN player
             ON player.idplayer = ?
         JOIN round
             ON round.idround = ?
             AND round.course_idcourse = course.idcourse
          JOIN player_has_round
            ON  InscriptionIdPlayer = player.idplayer
            AND InscriptionIdRound = round.idround
          JOIN tee
            ON course.idcourse = tee.course_idcourse
            AND player_has_round.InscriptionIdTee = tee.idtee
            AND tee.TeeGender = player.PlayerGender
          JOIN hole
            ON hole.tee_idtee = tee.TeeMasterTee
            AND hole.tee_course_idcourse = course.idcourse
            AND Hole.HoleNumber between roundstart and roundstart + roundholes - 1
          JOIN score
            ON score.player_has_round_player_idplayer = player.idplayer
            AND score.player_has_round_round_idround = round.idround
            AND hole.HoleNumber = score.ScoreHole
          ORDER by hole.HoleNumber
        """     ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
 //    ps.setString(3, inscription.getInscriptionTeeStart() );
         utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    while(rs.next()){
          ECourseList ecl = new ECourseList();
          ecl.setHole(entite.Hole.map(rs));
       //   ecl.setRound(new entite.Round().map(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
          ecl.setRound(new entite.Round().dtoMapper(rs));  // mod 21-10-2021
          ecl.setInscription(entite.Inscription.map(rs));
          ecl.setScoreStableford(entite.ScoreStableford.map(rs));

      liste.add(ecl );
            //    LOG.debug("just after add to listsc3");
  } //end while
     if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 ///  LOG.debug("exiting ScoreCard3List with " + liste.toString());
 
    return liste;
}catch (SQLException e){
        String msg = "SQL Exception in getScoreCard3List " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getScoreCard3List() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}

//}else{
//      LOG.debug("escaped to ScoreCard3List repetition thanks to lazy loading");
//      LOG.debug("ScoreCard3List = " + liste);
//return liste;
//}

} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        ScoreCardList3.liste = liste;
    }

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(636); // bawette
   //     Inscription inscription = new Inscription();
   //     inscription.setInscriptionIdTee(157);
        var v = new ScoreCardList3().list(player, round, conn);
     //   List<ECourseList> ec = new ScoreCard3List().list(player, round, conn);
         v.forEach(item -> LOG.debug("list of items =" + item));  // java 8 lambda
         v.forEach(item -> LOG.debug("scoreStableford =" + item.getScoreStableford()));  // java 8 lambda);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end Class