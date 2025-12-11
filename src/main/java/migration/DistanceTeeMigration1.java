package migration;

import entite.Course;
import entite.Tee;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

// 13-08-2023
// complete column TeeDistanceTee
// execution ONE SHOT une seule fois pour la migration!!

public class DistanceTeeMigration1 {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public void list(final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
       LOG.debug("entering " + methodName);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
    final String query = """
        SELECT *
        FROM tee
        WHERE tee.TeeDistanceTee is null;
      """;
     ps = conn.prepareStatement(query);
 //    ps.setInt(1, course.getIdcourse());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     int i = 0;
     while(rs.next()){
         i++;
         Tee tee = Tee.dtoMapper(rs);
         completeDistanceTee(tee, conn);
      } // end while
       LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + "/" + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}catch(Exception e){
    String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

    public static void completeDistanceTee (Tee tee, Connection conn){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    try{
     //   LOG.debug("entering completeDistanceTee");
        Course course = new Course();
        course.setIdcourse(tee.getCourse_idcourse());
        tee.setTeeDistanceTee(new find.FindDistanceTee().find(course, tee, conn));
           LOG.debug("DistanceTee found = " + tee.getTeeDistanceTee() + NEW_LINE);
            if(tee.getTeeDistanceTee() == 0){   //error not found
                  String msg = "-- Fatal error : Distance tee not found !! first create a tee with 'YELLOW' and 'M' and '01-18'";
                  LOG.error(msg);
                  LCUtil.showMessageFatal(msg);
                  throw new Exception(msg);
             }
            if(new update.UpdateTee().update(tee, conn)){
                String msg = "DistanceTee inserted/modified = " + tee;
                LOG.debug("msg");
      //          LCUtil.showMessageInfo(msg);
            }

 }catch(SQLException sqle) {
            String msg = "£££ SQLexception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
 }catch(Exception e) {
           String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
        }
}// end method
    
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
    new DistanceTeeMigration1().list(conn);
        LOG.debug("main - tee list completed ! ") ; //+ tees.size());
   // tees.forEach(item -> LOG.debug("Tee list migration " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class