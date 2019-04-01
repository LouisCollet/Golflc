package find;

import entite.Course;
import entite.Round;
import entite.Unavailable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

public class FindUnavailable implements interfaces.Log, interfaces.GolfInterface
{
    final private static String CLASSNAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
public Unavailable find(final Course course, final Round round, final Connection conn) throws SQLException{
     String CLASSNAME2 = Thread.currentThread().getStackTrace()[1].getClassName(); 
    LOG.info("entering : " + CLASSNAME2); 
 //   LOG.info("starting findCotisation.find for player = " + player.toString());
    LOG.info("starting findCotisation.find for round = " + round.toString());
    LOG.info("starting findUnavailable.find for course = " + course.toString());
        LOG.info("connection = " + conn);
    PreparedStatement ps = null;
    ResultSet rs = null;
 try{ 
  //  String u = utils.DBMeta.listMetaColumnsLoad(conn, "unavailable");
    String query = 
     " SELECT  * " +
 //      + u +
    " FROM unavailable "
   + " WHERE unavailable.UnavailableIdCourse = ?"
     +  " AND DATE(unavailable.UnavailableStartDate) <= DATE(?) " //, '%Y-%m-%d %H:%i(?) BETWEEN (unavailable.UnavailableStartDate AND unavailable.UnavailableEndDate)"     
     +  " AND DATE(unavailable.UnavailableEndDate) >= DATE(?)"
            ;
    
    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse());
        java.sql.Timestamp ts = Timestamp.valueOf(round.getRoundDate());
    ps.setTimestamp(2,ts);
    ps.setTimestamp(3,ts);
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindUnavailable has " + rs.getRow() + " lines.");
     Unavailable unavailable= new Unavailable();
    if(rs.getRow() == 0){
            String msg = "il n'y a pas d'indisponibilité pour ce round !" ;
            LOG.info(msg);
            unavailable.setCause(null);
            return unavailable;
     }
     if(rs.getRow() > 1){
            String msg = "il y a TROP d'indisponibilité pour ce round !" ;
            LOG.info(msg);
            showMessageFatal(msg);
            return unavailable;
     }
    if(rs.getRow() == 1){
            String msg = "Il y a UNE indisponibilité pour ce round !" ;
            LOG.info(msg);
            showMessageFatal(msg);
         //   return null;
        rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
    //    Unavailable unavailable= new Unavailable();
	while(rs.next())
        {
             unavailable = entite.Unavailable.mapUnavailable(rs);
	}
        LOG.info("unavailability extracted = " + unavailable);
      return unavailable;
     }     
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in " + CLASSNAME2 + " / "  + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
 return null;
}//end method

public static void main(String[] args) throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Course course = new Course();
    course.setIdcourse(102);
    Round round = new Round();
    round.setIdround(102);
    Unavailable t1 = new FindUnavailable().find(course, round, conn);
        LOG.info("unavailable found = " + t1.toString());
//for (int x: par )
//        LOG.info(x + ",");
DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

