package delete;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeletePaymentGreenfee implements interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
  public boolean delete(final Player player, Round round, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      PreparedStatement ps = null;
try{ 
       LOG.debug("starting " + methodName);
    final String query ="""
      DELETE
        FROM payments_greenfee
        WHERE GreenfeeIdPlayer = ?
        AND GreenfeeIdRound = ?;
    """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer());
    ps.setInt(2, round.getIdround());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
    if(row_deleted != 0){
        String msg = "PaymentGreenfee deleted ! ";
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
    }else{
        String msg = "PaymentGreenfee not found !";
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
    }
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

public static void main(String args[]) throws SQLException, Exception{     
     Connection conn = new DBConnection().getConnection();
 try{
     Player player = new Player();
     player.setIdplayer(324715);
     Round round = new Round();
     round.setIdround(758);
     boolean b = new DeletePaymentGreenfee().delete(player, round, conn);
       LOG.debug("from main - resultat deleted PaymentGreenfee = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class