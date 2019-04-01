
package delete;

import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteActivation implements interfaces.Log, interfaces.GolfInterface
{
    public Boolean delete(final Connection conn, String in_uuid) throws Exception {
    PreparedStatement ps = null;
    Boolean b = false;
 try{
     LOG.info("entering  delete from Table Activation ... = " );
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "DELETE"
        + "   FROM activation"
        + "   WHERE activationkey = ?"
     ;
        ps = conn.prepareStatement(query); 
        ps.setString(1, in_uuid);
          utils.LCUtil.logps(ps); 
        int x = ps.executeUpdate();
            //LOG.info(" -- Table Activation - Rows deleted = " + x);
    if(x == 1){
                LOG.info("-- Successfull Delete 1 row of table Activation = ");
                return true;
    }else{
                LOG.info("-- NOT NOT successful Delete rows " + x);
                return false;
            }
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in deleteActivation() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}

} //end method
    
  public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
  //  int idtee = 339;
    String uuid = "rrrrrrrrrrr";
    boolean b = new DeleteActivation().delete(conn, uuid);
        LOG.info("from main - resultat deletectivation = " + b);
 } catch (Exception e) {
        String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
    
    
    
} //end class