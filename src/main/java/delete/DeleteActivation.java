
package delete;

import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteActivation implements interfaces.Log, interfaces.GolfInterface{
    public Boolean delete(final Connection conn, String uuid) throws Exception {
    PreparedStatement ps = null;
  //  Boolean b = false;
 try{
     LOG.debug("entering  delete from Table Activation ... = " );
  final String query = """
          DELETE
          FROM activation
          WHERE activationkey = ?
    """ ;
        ps = conn.prepareStatement(query); 
        ps.setString(1, uuid);
          utils.LCUtil.logps(ps); 
        int rows = ps.executeUpdate();
            //LOG.debug(" -- Table Activation - Rows deleted = " + x);
    if(rows == 1){
                LOG.debug("-- Successfull Delete 1 row of table Activation = ");
                return true;
    }else{
                LOG.debug("-- NOT NOT successful Delete rows " + rows);
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
    
  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
  //  int idtee = 339;
    String uuid = "rrrrrrrrrrr";
    boolean b = new DeleteActivation().delete(conn, uuid);
        LOG.debug("from main - resultat deletectivation = " + b);
 } catch (Exception e) {
        String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class