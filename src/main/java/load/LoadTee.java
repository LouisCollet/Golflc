package load;

import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadTee
{
// à adapter
public Tee LoadTee(Connection conn, int idtee) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.info("entering LoadTee");
    String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
        LOG.info("String from listMetaColumns = " + te);
     //   LOG.info("simple name = " + club.)

final String query = "SELECT "
        + te
        + " FROM Tee "
        + " WHERE idtee = ?" ;

        LOG.info("Tee to be modified = " + idtee); 
     ps = conn.prepareStatement(query);
  //   ps.setInt(1, club.getIdclub());
     ps.setInt(1, idtee);
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.beforeFirst();
     Tee t = new Tee(); 
     while(rs.next())
     {
                t = entite.Tee.mapTee(rs);
	/*	t.setIdtee(rs.getInt("idtee"));
                t.setTeeStart(rs.getString("TeeStart"));
                t.setTeeGender(rs.getString("TeeGender"));
                t.setTeeSlope(rs.getShort("teeslope"));
                t.setTeeRating(rs.getBigDecimal("teerating"));
                t.setTeeClubHandicap(rs.getInt("TeeClubHandicap"));
                t.setCourse_idcourse(rs.getInt("course_idcourse"));*/
      }  //end while
    return t;
}catch (SQLException e){
    String msg = "SQLException in LoadTee() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    LOG.error("NullPointerException in LoadTee() " + npe);
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
   Connection conn = dbc.getConnection(); // main
  //  Club club = new Club();
//    club.setIdclub(104);
//round.setIdround(206);
   LoadTee lt = new LoadTee();
   Tee tee = lt.LoadTee(conn, 104);
     LOG.info(" club = " + tee.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class