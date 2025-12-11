package find;

import entite.HandicapIndex;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class FindLowHandicapIndex {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

//public Double find(final Player player, final Connection conn) throws SQLException{
 public Double find(final HandicapIndex handicapIndex, final Connection conn) throws SQLException{   
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering : " + methodName);
        LOG.debug(" for handicapIndex = " + handicapIndex);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
/*    String query ="""
            SELECT @last_id := MAX(HandicapDate)
             FROM handicap_index
             WHERE HandicapPlayerId = ?
            """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, handicapIndex.getHandicapPlayerId());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    LocalDateTime lsd = null;
    if(rs.next()){ 
        lsd = rs.getTimestamp(1).toLocalDateTime(); // HandicapDate
     }
     LOG.debug("Date last ScoreDifferential = " + lsd);
*/
 final String query = """
    SELECT MIN(HandicapWHS) LowHandicapIndex
     FROM handicap_index
     WHERE HandicapPlayerId = ?
     AND HandicapDate < ?
     AND HandicapDate > ?;
    """    ;
// meilleur hcp dans l'année qui précède la date dernier score differential
    ps = conn.prepareStatement(query);
    ps.setInt(1, handicapIndex.getHandicapPlayerId());
    ps.setTimestamp(2, Timestamp.valueOf(handicapIndex.getHandicapDate())); 
  //  ps.setTimestamp(3, Timestamp.valueOf(handicapIndex.getHandicapDate().minusYears(1)));  mod 15-09-2024 voir doc page 64 rule 5.7/1
    ps.setTimestamp(3, Timestamp.valueOf(handicapIndex.getHandicapDate().minusYears(2))); // noralement c'est un an 
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    double lowHandicapIndex = 0.0;
    if(rs.next()){
        lowHandicapIndex = rs.getDouble("LowHandicapIndex");
     }
     LOG.debug("Low Handicap Index = " + lowHandicapIndex);
    if(lowHandicapIndex == 0.0) {
        String msg = "Abnormal situation lowHandicapIndex = 0.0";
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
    }
    return lowHandicapIndex;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
// return false;
}//end method

void main() throws SQLException, Exception{ // testing purposes
  Connection conn = new DBConnection().getConnection();
  HandicapIndex handicapIndex = new HandicapIndex();
  handicapIndex.setHandicapPlayerId(324713);
  handicapIndex.setHandicapDate(LocalDateTime.parse("2022-07-17T17:11:30"));
  Double b = new find.FindLowHandicapIndex().find(handicapIndex,conn);
       LOG.debug("player FindLowHandicapIndexWHS = " + b.toString());
  DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class