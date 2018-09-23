
package lists;

import entite.ScoreMatchplay;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

/** extrait database les participants (2 ou 4) à un matchplay 
 *
 * @author collet
 */

public class ParticipantsMatchplay implements Serializable, interfaces.Log{
    private static List<ScoreMatchplay> liste = null;
    
 public  List<ScoreMatchplay> listAllParticipants(final Integer in_idround ,final Connection conn) throws SQLException        
{   
    LOG.info(" ... entering ParticipantsMatchplay !! with Connection = " + conn);
if(liste == null)
{   
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{   
     LOG.debug("starting getParticipantsList...for round  = "  + in_idround);
    String query =
          "SELECT idround, idplayer, round.RoundGame, playerLastName, playerFirstName,  " +
"		  RoundDate, round.RoundCompetition, round.RoundGame, player_has_round.Player_has_roundTeam, " +
"		  course.CourseName, club.ClubName, " +
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
"        ORDER by player_has_round.Player_has_roundTeam ASC " 
    ;
     ps = conn.prepareStatement(query);
      ps.setInt(1, in_idround); 
    //     //    String p = ps.toString();
      utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    //  à faire :
    // compressed string ==> chaqnger dans array
    // looop pour cahrger array players
    
  
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet ParticipantsList has " + rs.getRow() + " lines.");
       rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
    int rowNum = 0; //The method getRow lets you check the number of the row
	while(rs.next())
        {
 		ScoreMatchplay cc = new ScoreMatchplay(); // liste pour sélectionner un scoreCard
                        rowNum = rs.getRow() - 1;
                          LOG.info("row # = " + rowNum);
     //                     LOG.info("idplayer = " +);
      //                  cc.setPlayers[rowNum](rs.getInt("idplayer"));
                        cc.setIdplayer(rs.getInt("idplayer") );
			cc.setClubName(rs.getString("clubname") ); 
                        cc.setCourseName(rs.getString("coursename") );
                        cc.setIdround(in_idround); // new 23/11/2014
                        // next line load array2D from string
            cc.setScoreMP4(utils.LCUtil.stringToArray2D(rs.getString("uncompress(round.RoundScoreStringCompressed)")));
            cc.setPlayerFirstName(rs.getString("playerFirstName") );
                        cc.setPlayerLastName(rs.getString("playerLastName") );
//                        cc.setPlayerPhotoLocation(rs.getString("playerPhotoLocation") );
                    //    cc.setRoundDate(rs.getDate("RoundDate") );
                         cc.setRoundDate(rs.getTimestamp("roundDate") ); // mod 002/08/2015 avec minutes
                        cc.setRoundCompetition(rs.getString("RoundCompetition") );
                        cc.setRoundGame(rs.getString("RoundGame") ); // new 7/12/2014
  ////                      cc..setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult") );
   //                      cc.setPlayerhasroundTeam(rs.getString("Player_has_roundTeam"));
			//store all data into a List
			liste.add(cc);
		}
    //            boucler sur la liste ??
        LOG.info(" ending liste" + liste.toString() );
    return liste;
}catch (NullPointerException npe){ 
    String msg = "NullPointerException in getParticipantsList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){
    String msg = "SQL Exception in getParticipantsList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getParticipantsList() " + ex;
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

    public static List<ScoreMatchplay> getListe() {
        return liste;
    }

    public static void setListe(List<ScoreMatchplay> liste) {
        ParticipantsMatchplay.liste = liste;
    }

} //end class