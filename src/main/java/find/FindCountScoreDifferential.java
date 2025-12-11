package find;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCountScoreDifferential {

    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public Integer find(final Player player,  final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("entering : " + methodName);
    LOG.debug("starting " + methodName + " for player = " + player);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    final String query = """
       SELECT COUNT(*)
       FROM handicap_index
       WHERE HandicapPlayerId = ?
      """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
     int count = 0;
     if(rs.next()){ 
        count = rs.getInt(1);
     }
     LOG.debug("number of score differentials = " + count);
     return count;

}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
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
  Player player = new Player();
  player.setIdplayer(324713); // 456781
    Integer b = new FindCountScoreDifferential().find(player,conn);
        LOG.debug("player FindCountScoreDifferentialWHS = " + b.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
    
} // end Class