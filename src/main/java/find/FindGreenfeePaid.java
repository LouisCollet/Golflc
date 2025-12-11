package find;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindGreenfeePaid implements interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public boolean find(final Player player, final Round round, final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
       LOG.debug("entering " + methodName + "for player = " + player.toString());
       LOG.debug(" for round = " + round.toString());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{ 
    final String query = """
      SELECT COUNT(*)
      FROM payments_greenfee
      WHERE GreenfeeIdRound = ?
        AND GreenfeeIdPlayer = ?
      """      ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    ps.setInt(2, player.getIdplayer());
 //   ps.setDate(3, utils.LCUtil.LocalDateTimetoSqlDate(round.getRoundDate()));
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
     int i = 0;
     if(rs.next()){ 
        i = rs.getInt(1);
     }
     if(i == 0){
 //        LOG.debug("greenfee non payé !");
            String msg = LCUtil.prepareMessageBean("greenfee.notfound") + " for player = " +
                    player.getPlayerLastName() + " / " + player.getIdplayer() + " <br/> for round = " + round;
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return false;
      }
        if(i > 1){
            String err = "Abnormal technical situation !! More than 1 greenfee paid ? =";
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            return false;
        }
        if(i == 1){
            String msg = LCUtil.prepareMessageBean("greenfee.paid") + 
                    player.getPlayerLastName() + " / " + player.getIdplayer()
                    + " for round : " + round.getRoundName();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        }
        return true;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return false;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws SQLException, Exception{
  Connection conn = new DBConnection().getConnection();
  Player player = new Player();
  player.setIdplayer(456781); // 456781
  Round round =new Round();
  round.setIdround(633);
    Boolean b = new FindGreenfeePaid().find(player, round,conn);
        LOG.debug("result player findGreenfeePaid = " + b);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class