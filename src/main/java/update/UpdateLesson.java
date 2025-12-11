package update;

import entite.Lesson;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class UpdateLesson implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
 public boolean update(final Lesson before, final Lesson after, final Connection conn) throws SQLException{
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
try{
        LOG.debug("...starting " + methodName);
        LOG.debug("...for ScheduleEvent before " + before);
        LOG.debug("...for ScheduleEvent after " + after);

    final String query = """
             UPDATE lesson
             SET EventStartDate = ?, EventEndDate = ?
             WHERE EventProId = ?
             AND EventStartDate = ?
         """;
    ps = conn.prepareStatement(query);
    ps.setTimestamp(1,Timestamp.valueOf(after.getEventStartDate()));
    ps.setTimestamp(2,Timestamp.valueOf(after.getEventEndDate()));
    ps.setInt(3,before.getEventProId());
    ps.setTimestamp(4,Timestamp.valueOf(before.getEventStartDate()));
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate();
    if(row!=0){
          String msg = "successful UPDATE ScheduleEvent = " + after;
          LOG.info(msg);
          showMessageInfo(msg);
          return true;
     }else{
          String msg = "-- Unsuccessful result in " + methodName + " for event : " + after; //handicapIndex.getHandicapPlayerId();
          LOG.error(msg);
          showMessageFatal(msg);
          return false;
        }
}catch(SQLException e){
   String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}catch(Exception ex){
    String msg = "Exception in " + methodName + " / " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
 
  void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
try{
   Lesson before = new Lesson();
   before.setEventStartDate(LocalDateTime.parse("2021-05-12T08:00:00"));
   before.setEventProId(1); // rahm

   Lesson after = new Lesson();
   after.setEventStartDate(LocalDateTime.parse("2021-05-13T15:45:00"));
   after.setEventEndDate(LocalDateTime.parse  ("2021-05-13T16:15:00"));

   boolean b = new UpdateLesson().update(before, after, conn);
      LOG.debug("from main, after lp = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end class