package lists;

import entite.Course;
import entite.ECourseList;
import entite.Handicap;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class HandicapList implements interfaces.Log
{
    private static List<ECourseList> liste = null;

public List<ECourseList> getHandicapList(final @Valid Player player,
                                                    final @NotNull Connection conn) throws SQLException
{  
if(liste == null)
{
        LOG.info(" ... entering HandicapList !! ");
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
 
String query =
        "SELECT idhandicap, HandicapPlayer, PlayerFirstName, PlayerLastName, idplayer,"
        + " RoundDate, RoundCompetition, RoundGame, idround, "
        + "  CourseName, HandicapModificationDate"
        + " FROM handicap, round, course, player"
        + " WHERE handicap.round_idround = round.idround"
        + "     and round.course_idcourse = course.idcourse"
        + "     and player.idplayer = ?"
	+"      and handicap.player_idplayer = player.idplayer"
        + " GROUP by idhandicap"
        + " ORDER by idhandicap DESC"
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet HandicapList has " + rs.getRow() + " lines.");
           if(rs.getRow() == 0)    
            {String msg = "-- Empty Result Table for HandicapList !! ";
             LOG.error(msg);
    //         LCUtil.showMessageFatal(msg);
    //         throw new Exception(msg);
            }    
        
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
			ECourseList ecl = new ECourseList(); // liste pour sélectionner un round
                        Handicap h= new Handicap();
//			ccr.setPlayerFirstName(rs.getString("PlayerFirstName") );
//                        ccr.setPlayerLastName(rs.getString("PlayerLastName") );
//                        ccr.setIdplayer(rs.getInt("idplayer") );
                        h.setHandicapPlayer(rs.getBigDecimal("HandicapPlayer") );
                        h.setHandicapStart(rs.getDate("idhandicap") );
                        ecl.setHandicap(h);
                    //    ccr.setRoundDate(rs.getDate("RoundDate") );
                //        ccr.setRoundDate(rs.getTimestamp("roundDate") ); // mod 02/08/2015 avec minutes
                Round r = new Round();
                    r.setIdround(rs.getInt("idround") );
                    java.util.Date d = rs.getTimestamp("roundDate");
                    LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    r.setRoundDate(date);
                    r.setRoundGame(rs.getString("roundgame") );
                    r.setRoundCompetition(rs.getString("RoundCompetition") );
             //       r.setRoundHoles(rs.getShort("RoundHoles") );
             //       r.setRoundPlayers(rs.getShort("RoundPlayers") ); // new 20/06/2017
              //      r.setRoundStart(rs.getShort("RoundStart") );
                ecl.setRound(r);
                
                Course o = new Course();
     //       o.setIdcourse(rs.getInt("idcourse"));
                    o.setCourseName(rs.getString("CourseName") );
                ecl.setCourse(o);
                
                     
       //                 ccr.setIdplayer(rs.getInt("idplayer"));
        //                ccr.setPlayerLastName(rs.getString("PlayerLastName"));

			//store all data into a List
			liste.add(ecl);
		}
//LOG.debug(" -- query 5= listcc = " + listcc.toString() );
    return liste;
}catch (SQLException e){
        String msg = "SQL Exception in getHandicapList : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    String msg = "NullPointerException in getHandicapList() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in getHandicapList() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{

        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

}else{
   //  LOG.debug("escaped to handicaplist repetition with lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        HandicapList.liste = liste;
    }
} //end class