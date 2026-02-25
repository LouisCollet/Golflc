package lists;

import entite.composite.ECourseList;
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
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<ECourseList> list(final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    
    LOG.debug(" ... entering InscriptionList !!");
if(liste == null){
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     LOG.debug("starting getInscriptionList.. = " );
/*String query = """
           
        SELECT *
        FROM round
        JOIN course
            ON round.course_idcourse = course.idcourse
        JOIN club
            ON club.idclub = course.club_idclub
        GROU P BY idround
        ORDER by rounddate DESC
        LIMIT 30;
""";*/
 final String query = """
            \n   /* lists.InscriptionList.list  */
   WITH selection AS (
    SELECT * from round
    )
   SELECT * FROM selection
   JOIN course
      ON course.idcourse = selection.course_idcourse
   JOIN club
      ON club.idclub = course.club_idclub
   ORDER BY roundDate DESC
   LIMIT 30;
""";

        ps = conn.prepareStatement(query);
        utils.LCUtil.logps(ps);
	rs =  ps.executeQuery();
        liste = new ArrayList<>();
	while(rs.next()){
            ECourseList ecl = new ECourseList(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
            ecl.setClub(entite.Club.dtoMapper(rs));
            ecl.setCourse(entite.Course.dtoMapper(rs));
            ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
	liste.add(ecl);
	} //end while
     
  //  if(liste == null){
     if(liste.isEmpty()){   
        String msg = "££ Empty Result List in " + methodName;
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
   //    return null;
    }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }    
  // LOG.debug("Inscription liste = " + liste.toString());
    return liste;
}catch (SQLException e){
    String error = "SQL Exception in " + methodName + " / " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(error);
        LCUtil.showMessageFatal(error);
        return null;
}catch (Exception ex){
    LOG.error("Exception in " + methodName + " / " + ex);
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
    
  void main() throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
  //  Player player = new Player();
  //  player.setIdplayer(324713);
  //  Round round = new Round(); 
  //  round.setIdround(260);
  //  Club club = new Club();
  //  club.setIdclub(1006);
    List<ECourseList> p1 = new InscriptionList().list(conn);
        LOG.debug("number extracted = " + p1.size());
        LOG.debug("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} //end class