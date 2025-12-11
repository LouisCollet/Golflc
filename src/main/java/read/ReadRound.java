package read;

import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadRound{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public Round read(Round round,Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);
  //      LOG.debug(" with round = " + round); 
     final String query = "SELECT * "
        + " FROM Round "
        + " WHERE idround = ?" ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     Round r = new Round(); 
     while(rs.next()){
       //    r = new entite.Round().map(rs,club); // mod 19-02-2020 pour générer ZonedDateTime
           r = new entite.Round().dtoMapper(rs); // club sera null
      }  //end while
    return r;
}catch (SQLException e){
    String msg = "SQLException in LoadRound() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in LoadRound = " + ex.toString();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{ // testing purposes
   Connection conn = new DBConnection().getConnection(); // main
   Round round = new Round();
   round.setIdround(630);
   round  = new ReadRound().read(round,conn);
     LOG.debug(" loaded tee = " + round);
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class