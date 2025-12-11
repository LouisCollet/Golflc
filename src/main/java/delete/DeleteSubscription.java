package delete;

import entite.Subscription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteSubscription implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
  public boolean delete(final Subscription subscription, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      PreparedStatement ps = null;
try{ 
    LOG.debug("starting " + methodName);
 //       LOG.debug(" CASCADING DELETE ATTENTION ! for club "  + club); // new 15-02-2021
    String query = """
       DELETE from payments_subscription
       WHERE SubscriptionIdPlayer = ?
       """  ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, subscription.getIdplayer());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Subscription = " + row_deleted);
    String msg = "There are " + row_deleted + " Subscription deleted = " + subscription;
        LOG.debug(msg);
  //      LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     Subscription subscription = new Subscription();
     subscription.setIdplayer(125896);
    boolean b = new DeleteSubscription().delete(subscription, conn);
    LOG.debug("from main - resultat deleteSubscription = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class