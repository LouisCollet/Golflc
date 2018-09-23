
package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

public class DeleteActivation implements interfaces.Log, interfaces.GolfInterface
{
    public Boolean deleteActivation(final Connection conn, String in_uuid) throws Exception
    {
  //  Connection conn = null;
    PreparedStatement ps = null;
    Boolean b = false;
 try
{
 // conn = utils.DBConnection.getConnection();
     LOG.info("entering  delete from Table Activation ... = " );
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "DELETE"
        + "   FROM activation"
        + "   WHERE activationkey = ?"
     ;
        ps = conn.prepareStatement(query); 
        ps.setString(1, in_uuid);
          //    String p = ps.toString();
          utils.LCUtil.logps(ps); 
        int x = ps.executeUpdate();
            //LOG.info(" -- Table Activation - Rows deleted = " + x);
    if(x == 1)
            {
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
} //end class