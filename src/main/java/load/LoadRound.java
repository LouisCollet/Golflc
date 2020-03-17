package load;

import entite.Club;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadRound{

public Round load(Round round,Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadRound");
        LOG.info("Round to be loaded = " + round.getIdround()); 
    String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
  //      LOG.info("String from listMetaColumns = " + te);

final String query = "SELECT " + ro
        + " FROM Round "
        + " WHERE idround = ?" ;

     ps = conn.prepareStatement(query);
     ps.setInt(1, round.getIdround()); // where
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Round r = new Round(); 
     Club c = new Club();
     c = null;
     while(rs.next()){
         //  r = entite.Round.mapRound(rs);
           r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 pour générer ZonedDateTime
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

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
   Connection conn = new DBConnection().getConnection(); // main
   Round r = new Round();
   r.setIdround(443);
   r  = new LoadRound().load(r,conn);
     LOG.info(" loaded tee = " + r.toString());
   DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class