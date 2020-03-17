package find;

import entite.Activation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindActivation implements interfaces.Log, interfaces.GolfInterface{

public Activation find(Connection conn, Activation activation) throws SQLException, Exception{ // throws SQLException, TimeLimitException, Throwable{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      LOG.info("starting findActivation with in_uuid = " + activation);
  String ac = utils.DBMeta.listMetaColumnsLoad(conn, "activation");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
        "SELECT " + ac
        + "   FROM activation"
        + "   WHERE activationkey = ?"
     ;
        ps = conn.prepareStatement(query);
        ps.setString(1, activation.getActivationKey());
           utils.LCUtil.logps(ps); 
	rs =  ps.executeQuery();
        rs.last(); // on se positionne sur la dernière ligne
            LOG.info("ResultSet Activation has " + rs.getRow() + " lines.");
        Activation a = new Activation();    
        if(rs.getRow() != 1){
             a = null;
             throw new Exception(" -- Zero or More than 1 Activation = " + rs.getRow() );  }
        rs.beforeFirst(); //on replace le curseur avant la première ligne
	while(rs.next()){
              a = entite.Activation.mapActivation(rs);
	}
    LOG.info("activation found from activation = " + a); // c'est OK
    return a;
 
 }catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode() + " " + e.getLocalizedMessage();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        throw new SQLException(msg);
}catch (Exception ex){
    String msg = "Exception in getActivation() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    throw new Exception(msg);
 //   return null;
}
finally
{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

public static void main(String[] args) throws SQLException, Exception, Throwable {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Activation activation = new Activation();
    activation.setActivationKey("5563e1cf-b31b-46f1-95b2-292fe4f0895");
    activation = new FindActivation().find(conn,activation );
        LOG.info("after call = " + activation);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class