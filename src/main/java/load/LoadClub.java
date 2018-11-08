package load;

import entite.Club;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadClub
{

public Club LoadClub(Connection conn, int idclub) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadClub");
    String s = utils.DBMeta.listMetaColumnsLoad(conn, "club");
        LOG.info("String from listMetaColumns = " + s);
     //   LOG.info("simple name = " + club.)

final String query = "SELECT "
        + s
        + " FROM Club"
        + " WHERE idclub = ?" ;
     //   LOG.info("Club  = " + club.getIdclub() ); 
        LOG.info("Selected Club  = " + idclub); 
     ps = conn.prepareStatement(query);
  //   ps.setInt(1, club.getIdclub());
     ps.setInt(1, idclub);
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Club c = new Club(); 
     while(rs.next())
                {
               c = entite.Club.mapClub(rs);
		/*	sg.setIdclub(rs.getInt("idclub") );
                        sg.setClubName(rs.getString("clubName") );
                        sg.setClubAddress(rs.getString("clubAddress") );
                        sg.setClubCity(rs.getString("clubCity") );
                        sg.setClubCountry(rs.getString("clubCountry") );
                        sg.setClubLatitude(rs.getBigDecimal("ClubLatitude") );
                        sg.setClubLongitude(rs.getBigDecimal("ClubLongitude") );
                        sg.setClubWebsite(rs.getString("clubWebsite") );
                  
                  //      String r = DBMeta.setterGenerator(conn, "club");
                  //      LOG.info("inserted generator " + r);
                        
                        GoogleTimeZone gtz = new GoogleTimeZone();
                          gtz.setTimeZoneId(rs.getString("clubZoneId") );
                        sg.setClubTimeZone(gtz);
                      ccr.getClubModificationDate(rs.getDate("clubModificationDate"));*/
		}  //end while
    return c;
}catch (SQLException e){
    String msg = "SQLException in LoadClub() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadClub() " + npe);
    LCUtil.showMessageFatal("Exception = " + npe.toString() );
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

public static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
  //  Club club = new Club();
//    club.setIdclub(104);
//round.setIdround(206);
    LoadClub lc = new LoadClub();
    Club club = lc.LoadClub(conn, 104);
       LOG.info(" club = " + club.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
