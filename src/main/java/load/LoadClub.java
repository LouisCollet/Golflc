package load;

import entite.Club;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadClub{

public Club load(Club club,Connection conn) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadClub");
    String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
        LOG.info("String from listMetaColumns = " + cl);

final String query = "SELECT "
        + cl
        + " FROM Club"
        + " WHERE idclub = ?" ;

        LOG.info("Selected Club  = " + club.toString()); 
     ps = conn.prepareStatement(query);
     ps.setInt(1, club.getIdclub());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Club c = new Club(); 
     while(rs.next())
        {
               c = entite.Club.mapClub(rs);
	}  //end while
    return c;
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
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Club club = new Club();
    club.setIdclub(1104);
    Club c = new LoadClub().load(club, conn);
       LOG.info(" club = " + c.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
