package lists;

import entite.Club;
import entite.Course;
import entite.Round;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import connection_package.DBConnection;
import utils.LCUtil;
import static interfaces.Log.LOG;
public class InscriptionList {
    private static List<ECourseList> liste = null;
    
    
public List<ECourseList> list(final Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    
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
        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper(); // accepte rs et 
	while(rs.next()){
            ECourseList ecl = new ECourseList(); // liste pour sélectionner un round player = entite.Player.mapPlayer(rs);
            ecl.setClub(clubMapper.map(rs));
            ecl.setCourse(courseMapper.map(rs));
          //  ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
            ecl.setRound(roundMapper.map(rs, ecl.getClub()));
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
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
     handleGenericException(e, methodName);
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