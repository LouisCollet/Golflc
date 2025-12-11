package read;

import entite.Club;
import entite.Round;
import entite.UnavailablePeriod;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import utils.DBConnection;
import utils.LCUtil;

public class ReadUnavailablePeriod implements interfaces.Log, interfaces.GolfInterface{

    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 
    
public UnavailablePeriod read(final Club club, final Round round, final Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    LOG.debug("entering method : " + methodName); 
    LOG.debug(" for round = " + round.toString());
    LOG.debug(" for course = " + club.toString());

    PreparedStatement ps = null;
    ResultSet rs = null;
 try{ 
    final String query = """
   SELECT *
   FROM unavailable_periods
   WHERE UnavailableIdClub = ?
   AND DATE(UnavailableStartDate) <= DATE(?)
   AND DATE(UnavailableEndDate) >= DATE(?)
""";

    
    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub());
    ps.setTimestamp(2,Timestamp.valueOf(round.getRoundDate()));
    ps.setTimestamp(3,Timestamp.valueOf(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
         UnavailablePeriod period = new UnavailablePeriod();
	while(rs.next()){
              period = UnavailablePeriod.map(rs);
	}
        LOG.debug("unavailable period = " + period);
      if(period.getIdclub() == null){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
   //      LCUtil.showMessageFatal(msg);
         return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + 1 + " lines.");
     }
   return period;
}catch(SQLException e){
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
// return null;
}//end method

void main() throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(1122);
    Round round = new Round();
    round.setIdround(102);
    round.setRoundDate(LocalDateTime.of(2020, Month.FEBRUARY, 17, 12, 15));
  //  round.setRoundDate(LocalDateTime.of(2019,Month.APRIL,01,0,0));
    UnavailablePeriod period = new ReadUnavailablePeriod().read(club, round, conn);
        LOG.debug("unavailable found = " + period);
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end Class