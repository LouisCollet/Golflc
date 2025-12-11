package lists;

import entite.Course;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class TeesCourseList {
    private static List<Tee> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<Tee> list(final Course course, final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
       LOG.debug("with Course " + course);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
//    String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");  // fields list, comma separated
    final String query = """
        SELECT *
        FROM tee, course
        WHERE tee.course_idcourse = course.idcourse
          AND course.idcourse = ?
     """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, course.getIdcourse());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
         Tee t = Tee.dtoMapper(rs);
         liste.add(t);
     } // end while
     if(liste.isEmpty()){
         String error = "££ Empty Result Table in " + methodName;
         LOG.error(error);
         LCUtil.showMessageFatal(error);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //  liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
return liste;
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + "/" + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //   LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
} //end if
} //end method
    

    public static List<Tee> getListe() {
        return liste;
    }
    public static void setListe(List<Tee> liste) {
        TeesCourseList.liste = liste;
    }
    
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(41);
    List<Tee> tees = new TeesCourseList().list(course, conn);
        LOG.debug("tee list  for a course = " + tees.size());
    tees.forEach(item -> LOG.debug("Tee list for a Course " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class