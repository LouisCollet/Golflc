package read;

import entite.Club;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ReadClub{

public Club read(Club club,Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering LoadClub");
        LOG.debug("with Club = " + club);
final String query = """
        SELECT *
        FROM Club
        WHERE idclub = ?
       """ ;
  //      LOG.debug("Selected Club  = " + club.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, club.getIdclub());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     while(rs.next()){
               club = Club.dtoMapper(rs);
	}  //end while
 //    LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
    return club;
}catch (SQLException e){
    String msg = "SQLException in LoadClub() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}
finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(154);
    Club c = new ReadClub().read(club, conn);
       LOG.debug(" club loaded = " + c.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
