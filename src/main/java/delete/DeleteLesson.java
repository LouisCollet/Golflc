package delete;

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

public class DeleteLesson implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
public boolean delete(final Lesson lesson, final Connection conn) throws SQLException    {
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
try {
        LOG.debug("...entering " + methodName);
        LOG.debug(" with lesson  = " +lesson);
      final String query = """
              DELETE FROM lesson
              WHERE EventProId = ?
              AND EventStartDate = ?
         """;
            ps = conn.prepareStatement(query);
            ps.setInt(1,lesson.getEventProId());
            ps.setTimestamp(2,Timestamp.valueOf(lesson.getEventStartDate()));
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if(row != 0){
                String msg = "ScheduleEvent Deleted = " + lesson;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "ERROR ScheduleEvent NOT Deleted !!: " + lesson;
                LOG.debug(msg);
                showMessageFatal(msg);
                return false;
            }
 }catch (SQLException sqle) {
            String msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
 } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        //return null;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method

void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
try{
   Lesson event = new Lesson();
   event.setEventStartDate(LocalDateTime.parse("2021-05-23T12:45:30")); //2021-05-23 12:45:30
   event.setEventProId(1); //rahm
   boolean lp = new DeleteLesson().delete(event, conn);
      LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end Class