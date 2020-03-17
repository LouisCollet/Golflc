package lists;

import entite.Club;
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

public class ScoreCard3List implements interfaces.Log{
    private static List<ECourseList> liste = null; 
    
public List<ECourseList> list(final Player player, final Round round ,
        final Inscription inscription,
        final Connection conn)
        throws SQLException{
if(liste == null){
    LOG.debug("entering ScoreCard3List... = ");
    LOG.info("with Player = ... = " + player.toString());
    LOG.info("with Round = ... = " + round.toString());
   LOG.info("with Inscription = ... = " + inscription.toString());

    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
    String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
    String ho = utils.DBMeta.listMetaColumnsLoad(conn, "hole");
  // String cl = utils.DBMeta.listMetaColumnsLoad(conn, "Club");
  //   String co = utils.DBMeta.listMetaColumnsLoad(conn, "Course");
    String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  
    String query =
        "SELECT" + sc + "," + ho + "," + ro + "," + ph
        + " FROM course"
        + " JOIN player"
        + "     ON player.idplayer = ?"
        + " JOIN round"
        + "     ON round.idround = ?"
        + "     AND round.course_idcourse = course.idcourse"
        + "  JOIN player_has_round" // new
        + "    ON  InscriptionIdPlayer = player.idplayer" //new
        + "    AND InscriptionIdRound = round.idround"    //new
            
        + " JOIN tee"
        + "     ON course.idcourse = tee.course_idcourse"
        + "    AND player_has_round.InscriptionIdTee = tee.idtee" // new 05-04-2019
        + "     AND tee.TeeGender = player.PlayerGender"
 //       + "     AND tee.TeeStart = ? "
        + " JOIN hole"
  //      + "     ON hole.tee_idtee = tee.idtee"  // enlevé 07-04-2019
        + "     ON hole.tee_idtee = tee.TeeMasterTee"    // remplacé ligne précédente 07-04-2019
        + "     AND hole.tee_course_idcourse = course.idcourse"
        + "     AND Hole.HoleNumber between roundstart and roundstart + roundholes - 1"
        + " JOIN score"
        + "     ON score.player_has_round_player_idplayer = player.idplayer"
        + "     AND score.player_has_round_round_idround = round.idround"
        + "     AND hole.HoleNumber = score.ScoreHole"
        + " ORDER by hole.HoleNumber"
         ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
 //    ps.setString(3, inscription.getInscriptionTeeStart() );
         utils.LCUtil.logps(ps);
		//get scoreCard data from database
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ScoreCard3List has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0){
            String msg = "-- Empty Result Table for ScoreCard3List !! ";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
    }   
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>(); // new 02/06/2013
  //  ECourseList cc = new ECourseList(); mod

     Club c = new Club();
     c = null;
while(rs.next()){
          ECourseList ecl = new ECourseList(); // est réi, donc total = 0

     // besoin de la zoneId pour générer RoundDate Zoned
   //       Club c = new Club();
   //       c = null;
   //       ecl.setClub(c);
          
     //     Hole h = new Hole();
     //     h = entite.Hole.mapHole(rs);
          ecl.setHole(entite.Hole.mapHole(rs));

   //       Round r = new Round();
      //    r = entite.Round.mapRound(rs); // à modifier !!
   //       r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 pour générer ZonedDateTime
          ecl.setRound(new entite.Round().mapRound(rs,c));
          
   //       Inscription i = new Inscription();
   //       i = entite.Inscription.mapInscription(rs);  
          ecl.setInscriptionNew(entite.Inscription.mapInscription(rs));

    //      ScoreStableford s = new ScoreStableford();
     //     s = entite.ScoreStableford.mapScoreStableford(rs);  
          ecl.setScoreStableford(entite.ScoreStableford.mapScoreStableford(rs));
      liste.add(ecl );//store all data into a List
            //    LOG.info("just after add to listsc3");
} //end while
 ///  LOG.info("exiting ScoreCard3List with " + liste.toString());
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
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
    //    LOG.debug("escaped to ScoreCard3 repetition thanks to lazy loading");
return liste;
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        ScoreCard3List.liste = liste;
    }

 public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(449); // bawette
        Inscription inscription = new Inscription();
        inscription.setInscriptionIdTee(157);
        List<ECourseList> ec = new ScoreCard3List().list(player, round, inscription, conn);
     //   List<ECourseList> ec = new ScoreCard3List().list(player, round, conn);
        LOG.info("from main, ec = " + ec);
 }catch (Exception e){
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end Class