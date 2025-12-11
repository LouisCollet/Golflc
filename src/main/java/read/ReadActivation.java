package read;

import entite.Activation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadActivation implements interfaces.Log, interfaces.GolfInterface{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public Activation read(Connection conn, Activation activation) throws SQLException, Exception{ // throws SQLException, TimeLimitException, Throwable{
    PreparedStatement ps = null;
    ResultSet rs = null;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
try{
      LOG.debug("starting ReadActivation.read for  " + activation);
// utilisé pour new player et Reset Password //
  final String query =  """
        SELECT *
        FROM activation
        WHERE activationkey = ?
     """ ;
        ps = conn.prepareStatement(query);
        ps.setString(1, activation.getActivationKey());
        utils.LCUtil.logps(ps); 
	rs =  ps.executeQuery();
        Activation a = new Activation();
	while(rs.next()){
              a = entite.Activation.map(rs);
	}
      if(a.getActivationKey() == null){
         String msg = "Votre enregistrement à Golflc ou votre demande de password reset n'ont pas été trouvés !!";
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         throw new Exception(msg); // mod 01-01-2023
  //       return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + 1 + " lines.");
         LOG.debug("activation found from activation = " + a); // c'est OK
     }   
    
    return a;
 
 }catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode() + " " + e.getLocalizedMessage();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
      //  throw new SQLException(msg);
}catch (Exception ex){
    String msg = "Exception in LoadActivation() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
 //   throw new Exception(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

void main() throws SQLException, Exception, Throwable {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Activation activation = new Activation();
    activation.setActivationKey("5563e1cf-b31b-46f1-95b2-292fe4f0895");
    activation = new ReadActivation().read(conn,activation );
        LOG.debug("after call = " + activation);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class