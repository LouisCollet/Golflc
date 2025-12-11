package update;

import entite.Course;
import static interfaces.GolfInterface.DATE_BEGIN_COURSE;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateCourse {
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean update(final Course course, final Connection conn) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
 try {
            LOG.debug("... entering = " + methodName);
            LOG.debug(" with course = " + course);
            
            
            
            
        String co = utils.DBMeta.listMetaColumnsUpdate(conn, "course");
            LOG.debug("String from listMetaColumns = " + co); //  coursename=?, courseholes=?, coursepar=?, coursebegindate=?, courseenddate=?
     final String query = """
          UPDATE course
          SET %s
          WHERE course.idcourse=?;
         """.formatted(co) ;
     
     /* %s replaced by co https://stackoverflow.com/questions/63687580/how-can-i-add-variables-inside-java-15-text-block-feature   
        Java 15 does not support interpolation directly within text blocks
        "String interpolation" meaning  evaluating a string literal containing one or more placeholders,
        yielding a result in which the placeholders are replaced with their corresponding values
        The solution in Java 15 is to use String.formatted() method: 
     */
        ps = conn.prepareStatement(query);
            ps.setString(1, course.getCourseName());
            ps.setShort(2, (short) 18);// mod 12-11-2018 toujour 18 holes  enlevé dans blacklist de columns update
            ps.setShort(3, course.getCoursePar()); 
       //     ps.setDate(4, LCUtil.getSqlDate(course.getCourseBeginDate())); mod 03-12-2025
       //     ps.setDate(5, LCUtil.getSqlDate(course.getCourseEndDate()));
            ps.setTimestamp(4, Timestamp.valueOf(DATE_BEGIN_COURSE)); // date de début fictive pour tous les parcours
          //  ps.setTimestamp(5, Timestamp.valueOf(DATE_END_COURSE)); // date de fin fictive pour tous les parcours
            ps.setTimestamp(5, Timestamp.valueOf(course.getCourseEndDate())); //DATE_END_COURSE)); // date de fin fictive pour tous les parcours
              ps.setInt(6, course.getIdcourse());  // ne pas oublier

            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.debug("row = " + row);
            if (row != 0) {
                String msg =  LCUtil.prepareMessageBean("course.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + course.getIdcourse()
                            + " <br/>Name = " + course.getCourseName()
                            + " <br/>Par = " + course.getCoursePar();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "row = 0 - Could not modify course";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException(msg));
                //    return false; pas compatible avec throw
            }
return true;
        } // end try
catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
}catch (Exception e) {
            String msg = "£££ SQLException in " + methodName + e;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyCourse

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      // définition dans pom.xml <artifactId>exec-maven-plugin</artifactId>
       System.out.println("=== System Properties ===");
      LOG.debug("System.getProperty property env = " + System.getProperty("env"));
      LOG.debug("System.getProperty String   mode = " + System.getProperty("mode"));
       System.out.println("\n=== Environment Variables ===");
      LOG.debug("System.getenv      String API_URL = " + System.getenv("API_URL"));  // null ??
      LOG.debug("MY_VAR = " + System.getenv("MY_VAR"));  // also null 
    //  LOG.debug("MY_VAR = " + ClassLayout.System.getenv("MY_VAR"));  // also null 
     // System.getenv().forEach((k, v) -> System.out.println(k + "=" + v));
      
      // Pour tester le debug
      //  String s = null;
        // Déclenche une exception pour voir la stack trace avec lignes et variables
      //  s.length();
     Course course = new Course();
     course.setIdcourse(24);  // chateau ardenne
     course = new read.ReadCourse().read(course,conn);
     course.setCourseName("drop table club_test");  // SQL injection  ?? non
     course.setCourseEndDate(LocalDateTime.parse("2014-12-31T23:59:59"));
     boolean b = new UpdateCourse().update(course,conn);
         LOG.debug("from main, resultat = " + b);
         
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
         //   e.printStackTrace(); // imprime la stack trace complète
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
 }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
 }
} // end main
} //end Class