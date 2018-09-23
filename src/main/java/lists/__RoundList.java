
package lists;

import entite.ClubCourseRound;
import entite.Player;
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


public class __RoundList implements interfaces.Log
{
    private static List<ClubCourseRound> liste = null;
    
public List<ClubCourseRound> getRoundList(final Player player, final Connection conn) throws SQLException
{   
if (liste == null)
{
     LOG.debug("starting getRoundList...for player = {}", player);
  //  Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{   
String query =
        "SELECT idplayer, playerFirstName, playerLastName, round_idround,"
        + " idclub, idcourse, idround, "
        + " RoundDate, RoundCompetition, RoundQualifying, RoundGame,"
        + " RoundCSA, CourseName, ClubName, InscriptionFinalResult,"
        + " Player_has_roundZwanzeursResult, Player_has_roundZwanzeursGreenshirt"
        + " FROM player"
        + " JOIN player_has_round"
        + "     ON player_has_round.player_idplayer = player.idplayer"
        + "     AND player_has_round.InscriptionFinalResult > 0"
        + " JOIN round"
        + "     ON round.idround = player_has_round.round_idround "
        + " JOIN course"
        + "     ON course.idcourse = round.course_idcourse"
        + " JOIN club"
        + "     ON club.idclub = course.club_idclub"
        + " WHERE player.idplayer = ? "
        + " ORDER by roundDate desc "
        ;
        
     ps = conn.prepareStatement(query);
     // à modifier ici !!!!
    // ps.setInt(1, player.getIdplayer());
        ps.setInt(1, player.getIdplayer());
         //    String p = ps.toString();
         utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet RoundList has " + rs.getRow() + " lines.");
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
			ClubCourseRound cc = new ClubCourseRound(); // liste pour sélectionner un scoreCard
			//cc.setIdclub(rs.getInt("idclub") ); // was idscoreCard : not case sensitive ??
                        cc.setCourseName(rs.getString("CourseName") );
                        cc.setClubName(rs.getString("ClubName") );
                        
                        cc.setIdplayer(player.getIdplayer() );  // mod 25/10/2013
                        cc.setPlayerFirstName(rs.getString("playerFirstName") );
                        cc.setPlayerLastName(rs.getString("playerLastName") );
                        cc.setPlayerLastName(rs.getString("playerLastName") );
                        
                        cc.setIdclub(rs.getInt("idclub") );
                        cc.setIdcourse(rs.getInt("idcourse"));
                        cc.setIdround(rs.getInt("idround") );
                    //    cc.setRoundDate(rs.getDate("RoundDate") );
                  //      cc.setRoundDate(rs.getTimestamp("roundDate") ); // mod 02/08/2015 avec minutes 
                        
                        java.util.Date d = rs.getTimestamp("roundDate");
                        LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        cc.setRoundDate(date);
                         
                        cc.setRoundCompetition(rs.getString("RoundCompetition") );
                        cc.setRoundQualifying(rs.getString("RoundQualifying") );
                        cc.setRoundCBA(rs.getShort("RoundCSA") );
                        cc.setRoundGame(rs.getString("RoundGame") );
                        cc.setPlayerhasroundFinalResult(rs.getShort("InscriptionFinalResult") );
                        cc.setPlayerhasroundZwanzeursResult(rs.getShort("Player_has_roundZwanzeursResult") );
                        cc.setPlayerhasroundZwanzeursGreenshirt(rs.getShort("Player_has_roundZwanzeursGreenshirt") );
			liste.add(cc);  //store all data into a List
		}
//LOG.debug(" -- query 5= listcc = " + listcc.toString() );
    return liste;
}catch (NullPointerException npe){   String msg = "NullPointerException in getRoundList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (SQLException e){       String msg = "SQL Exception in getRoundList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in getRoundList() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
         //    LOG.debug("escaped to listRounds repetition with lazy loading ");
    return liste;
    }

} //end method

    public static List<ClubCourseRound> getListe() {
        return liste;
    }

    public static void setListe(List<ClubCourseRound> liste) {
        __RoundList.liste = liste;
    }

 
    
} //end Class
