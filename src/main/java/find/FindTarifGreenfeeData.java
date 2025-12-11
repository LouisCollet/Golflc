package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import utils.DBConnection;
import utils.LCUtil;

public class FindTarifGreenfeeData implements interfaces.GolfInterface{
    
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

public TarifGreenfee find(
     //   final Course course,
        final Round round, final Connection conn) throws SQLException{
           LOG.debug("entering FindTarifGreenfeeData.find ...");
    //       LOG.debug(" for course = " + course);
           LOG.debug(" for round = " + round);
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
   final String query ="""
              SELECT TarifJson
              FROM tarif_greenfee
              WHERE tarif_greenfee.TarifCourseId = ?
              AND ? BETWEEN tarif_greenfee.TarifStartDate AND tarif_greenfee.TarifEndDate
              """;
    ps = conn.prepareStatement(query);
 //   ps.setInt(1, course.getIdcourse());
    ps.setInt(1, round.getCourseIdcourse()); // mod 14-04-2025
    ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    int i = 0;
    String json = null;
    while(rs.next()){
       i ++;
       json = rs.getString("TarifJson");
    }
     if(i == 0){
         String msg=  LCUtil.prepareMessageBean("tarif.greenfee.notfound") + round.getCourseIdcourse();
         LOG.debug(msg);
         LCUtil.showMessageInfo(msg);
         throw new Exception(msg);
 //        return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        om.registerModule(new JavaTimeModule());  // traiter LocalDateTime format
        om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
          LOG.debug("Tarif Greenfee format json = "  + json);
        TarifGreenfee tarifGreenfee = om.readValue(json,TarifGreenfee.class);
            LOG.debug("Tarif Greenfee extracted from database = "  + tarifGreenfee);
        return tarifGreenfee;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindTarif()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(ex.toString()); // new 04-01-2022
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

public static void main(String[] args) throws SQLException, Exception {
     Connection conn = new DBConnection().getConnection();
 try{
   
///    Course course = new Course();
 ///   course.setIdcourse(165); // spiegelven
 //   course = new load.LoadCourse().load(conn,course);
    Round round = new Round();
    round.setIdround(755);
    round = new read.ReadRound().read(round, conn);
       LOG.debug("course from round = " + round.getCourseIdcourse());
    TarifGreenfee tarifGreenfee = new FindTarifGreenfeeData().find(round, conn);
     LOG.debug("TarifGreenfee in main = "  + tarifGreenfee);
  } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
}// end main
} // end Class