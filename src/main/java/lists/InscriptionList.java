package lists;

import entite.Club;
import entite.ECourseList;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class InscriptionList implements interfaces.Log{
    private static List<ECourseList> liste = null;
    
public List<ECourseList> list(final Connection conn) throws SQLException{
    LOG.info(" ... entering InscriptionList !!");
if(liste == null){
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.info("starting getInscriptionList.. = " );
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  //   String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
 
String query =
        "SELECT "+ cl + "," + co + "," + ro + //"," + // pl + "," +
    "	 FROM round" +
    "	   JOIN course"
        + "	ON round.course_idcourse = course.idcourse" +
    "	 JOIN club "
        + "	ON club.idclub = course.club_idclub" +
    "	    WHERE RoundDate > DATE_SUB(current_date(),INTERVAL 3000 month)" +  // à réduire après corrections
    "	    GROUP BY idround" +
    "       ORDER by rounddate desc "
;

     ps = conn.prepareStatement(query);
///     ps.setInt(1, player.getIdplayer());
///     ps.setString(2, formula.toUpperCase() ); // new 30/6/2015
         utils.LCUtil.logps(ps);
		//get round data from database
	rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet getInscriptionList has " + rs.getRow() + " lines.");
        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
          //LOG.info("just before while ! ");
	while(rs.next()){
		//LOG.info("just after while ! ");
          ECourseList ecl = new ECourseList(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
          Club c = new Club();
          c = entite.Club.mapClub(rs);
          ecl.setClub(c);
          
     //     Course o = new Course();
      //    o = entite.Course.mapCourse(rs);
          ecl.setCourse(entite.Course.mapCourse(rs));
          
          Round r = new Round();
          r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 a besoin nde club pour générer ZonedDateTime
          ecl.setRound(r);
          
//store all data into a List
	liste.add(ecl);
	} //end while
// LOG.info("Inscription liste = " + liste.toString());
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in InscriptionList= " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in getInscriptionList() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
     return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
    LCUtil.showMessageFatal("Exception = " + ex.toString() );
     return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
}else{
    //     LOG.debug("escaped to listinscription repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
} //end else    
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        InscriptionList.liste = liste;
    }
    
  public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
  //  Round round = new Round(); 
  //  round.setIdround(260);
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<ECourseList> p1 = new InscriptionList().list(conn);
        LOG.info("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
    
    
    
    
} //end class