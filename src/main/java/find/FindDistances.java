package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Distance;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import utils.DBConnection;
import utils.LCUtil;

public class FindDistances implements interfaces.GolfInterface{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

public Distance find(Tee tee, final Connection conn) throws SQLException{
           LOG.debug("entering FindDistances.find ...");
           LOG.debug(" for tee = " + tee);
        //   LOG.debug(" for round = " + round);
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
    Distance distance = new Distance();
try{
   final String query = """
       SELECT distances.DistanceArray
       FROM distances
       WHERE distances.DistanceIdTee = ?
     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getTeeDistanceTee()); // mod 12-08-2023
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    int i = 0;
    String json = null;
    while(rs.next()){
       i++;
       json = rs.getString("DistanceArray");
    }
     if(i == 0){
         String msg=  LCUtil.prepareMessageBean("distances.notfound") + "<br>" + tee;
         LOG.debug(msg);
         LCUtil.showMessageInfo(msg);
       //  Distance distance = new Distance();
         int[] array = new int[18];
         Arrays.fill(array, 0);
         distance.setDistanceArray(array);
         return distance;
 //        return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
           LOG.debug("Distance format json = "  + json);
        distance = om.readValue(json, Distance.class);
           LOG.debug("Distance extracted from database = "  + distance);
        return distance;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindDistances()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(ex.toString()); // new 04-01-2022
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
  //  Distance distance = new Distance();
  Tee tee = new Tee();
  tee.setIdtee(150);
  //  distance.setIdTee(10);
    Distance distance = new FindDistances().find(tee, conn);
     LOG.debug("Distance found in main = "  + distance);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class