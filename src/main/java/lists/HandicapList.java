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
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class HandicapList implements interfaces.Log
{
    private static List<ECourseList> liste = null;

// public List<ECourseList> getHandicapList(final @Valid Player player,
public List<ECourseList> getHandicapList(final Player player,        
                                                    final Connection conn) throws SQLException{  
if(liste == null){
        LOG.info(" ... entering HandicapList !! ");
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String ha = utils.DBMeta.listMetaColumnsLoad(conn, "Handicap");
String query =
        "SELECT "
        +  co + "," + ro + "," + pl + "," + ha
   ////     + "idhandicap, HandicapPlayer, PlayerFirstName, PlayerLastName, idplayer,"
   //     + " RoundDate, RoundCompetition, RoundGame, idround, "
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
                Handicap h = new Handicap();
                h = entite.Handicap.mapHandicap(rs);
                ecl.setHandicap(h);
                
                Round r = new Round();
                r = entite.Round.mapRound(rs);
                ecl.setRound(r);
                
                Course o = new Course();
                o = entite.Course.mapCourse(rs);
                ecl.setCourse(o);
	liste.add(ecl);
}
//LOG.debug(" -- query 5= listcc = " + listcc.toString() );
    return liste;
}catch (SQLException e){
        String msg = "SQL Exception in getHandicapList : " + e;
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