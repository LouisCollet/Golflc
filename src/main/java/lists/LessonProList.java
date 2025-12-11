package lists;

import entite.Professional;
import entite.Lesson;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class LessonProList implements interfaces.Log{
    private static List<Lesson> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
//public List<ScheduleEvent> list(final ScheduleEvent event, final Connection conn) throws Exception{
    public List<Lesson> list(final Professional professional, final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
       LOG.debug("entering " + methodName);
       LOG.debug("with Professional " + professional);
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
    final String query ="""
        SELECT *
        FROM lesson
        WHERE lesson.EventProId = ?
        AND EventStartDate > ?
     """ ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, professional.getProId());
  // dimanche de la 2e emaine qui précède et on teste sur startDate > pour avoir la semaine courante et la semaine précédente
     ps.setTimestamp(2,Timestamp.valueOf(LocalDate.now().minusWeeks(2).with(DayOfWeek.SUNDAY).atStartOfDay()));
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()){
         Lesson ev = Lesson.map(rs);
         liste.add(ev);
     }
     if(liste.isEmpty()){
         String info = "££ Empty Result Table in " + methodName;
         LOG.error(info);
         LCUtil.showMessageInfo(info);
  //       return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 //  liste.forEach(item -> LOG.debug("Players list with Players and passwords " + item));  // java 8 lambda
return liste;
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + " / " + sqle.getMessage() + " ,SQLState = " +
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
    

    public static List<Lesson> getListe() {
        return liste;
    }

    public static void setListe(List<Lesson> liste) {
        LessonProList.liste = liste;
    }

 
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
 //   ScheduleEvent event = new ScheduleEvent();
 //   event.setEventProId(1);
   Professional pro = new Professional();
   pro.setProId(1);
    List<Lesson> schedules = new LessonProList().list(pro, conn);
        LOG.debug("size schedule list for a Pro = " + schedules.size());
   schedules.forEach(item -> LOG.debug("Schedule list for a Pro StartDate " + item.getEventStartDate()));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class