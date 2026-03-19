
package lists;

import entite.Tee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rowmappers.TeeRowMapper;

@ApplicationScoped
public class TeesCourseList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Tee> liste = null;

 //   public List<Tee> list(final Course course) throws SQLException {
     public List<Tee> list(final int courseId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        // Early return si cache existe
        if (liste != null) {
            LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
            return liste;
        }

        // Sinon, charger depuis la base de donnees
        LOG.debug("entering " + methodName);
         LOG.debug("with Course " + courseId);

        final String query = """
            SELECT *
            FROM tee, course
            WHERE tee.course_idcourse = course.idcourse
              AND course.idcourse = ?
            """;

        liste = dao.queryList(query, new TeeRowMapper(), courseId);

        if (liste.isEmpty()) {
            LOG.warn("Empty Result Table in " + methodName);
        } else {
            LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
        }
        return liste;
    } // end method

    // Getters/setters d'instance
    public List<Tee> getListe()               { return liste; }
    public void setListe(List<Tee> liste)     { this.liste = liste; }

    // Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        Course course = new Course();
        course.setIdcourse(41);
        List<Tee> tees = new TeesCourseList().list(course.getIdcourse());
        LOG.debug("tee list for a course = " + tees.size());
        tees.forEach(item -> LOG.debug("Tee list for a Course " + item));
    } // end main
    */

} // end Class
/*
import entite.Course;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import utils.LCUtil;

public class TeesCourseList {
    private static List<Tee> liste = null;


public List<Tee> list(final Course course, final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName();
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
     RowMapper<Tee> teeMapper = new TeeRowMapper();
     while(rs.next()){
         Tee t = teeMapper.map(rs);
         liste.add(t);
     } // end while
     if(liste.isEmpty()){
         String error = "Empty Result Table in " + methodName;
         LOG.error(error);
         LCUtil.showMessageFatal(error);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //  liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
return liste;
} catch(SQLException e){
    handleSQLException(e, methodName);
    return null;
}catch(Exception e){
    handleGenericException(e, methodName);
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
    Connection conn = new connection_package.DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(41);
    List<Tee> tees = new TeesCourseList().list(course, conn);
        LOG.debug("tee list  for a course = " + tees.size());
    tees.forEach(item -> LOG.debug("Tee list for a Course " + item));
    connection_package.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class
*/
