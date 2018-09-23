package lists;

import entite.Flight;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import utils.DBConnection;
import utils.LCUtil;

// ce programme élimine les flights déjà réservés !
public class FlightList implements interfaces.Log
{
    private static ArrayList<Flight> liste = null;
  //  private static final Connection CONN = null;
    
public ArrayList<Flight> listAllFlights(final Connection CONN) throws Exception
{
if(liste == null)
{    
    LOG.debug("starting listAllFlights()," );
        PreparedStatement ps = null;
        ResultSet rs = null;
        liste = new ArrayList<>();
        final String query =
            "SELECT  flight.idflight, flight.FlightStart, flight.course_idcourse" +
            " FROM    flight" +
            " WHERE   DATE_FORMAT(flight.FlightStart, '%Y-%m-%d %k:%i')" +   // élimine les secondes
        "	      NOT IN" +
    "        (" +
    "        SELECT DATE_FORMAT(reservation.ReservationStartFlight, '%Y-%m-%d %k:%i')" +
    "        FROM  reservation" +
    "        WHERE reservation.course_idcourse = flight.course_idcourse" +
    "        )" +
    "       ORDER BY flight.FlightStart";
try{
            ps = CONN.prepareStatement(query);
            rs = ps.executeQuery();
            liste = new ArrayList<>();
            while (rs.next())
            { //  
                Flight fl = new Flight(); 
                fl.setIdflight(rs.getInt("idflight") );
                
         //       fl.setFlightStart(rs.getDate("FlightStart"));
                java.util.Date d = rs.getTimestamp("FlightStart");
                LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                fl.setFlightStart(date);
                
                fl.setCourse_idcourse(rs.getInt("course_idcourse"));
                fl.setFlightPeriod("?");
                liste.add(fl);
            }
  //  LOG.debug("closing listAllPlayers() with players = " + Arrays.deepToString(liste.toArray()) );
  liste.forEach(item -> LOG.info("Flight list " + item));  // java 8 lambda
return liste;

} catch(SQLException sqle){
    String msg = "£££ SQL exception in ListAllFlight = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in ListAllFlights = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
           DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
     LOG.debug("escaped to listAllFlights repetition thanks to lazy loading");
     return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
}
    //end if

} //end method
    

    public static ArrayList<Flight> getListe() {
        return liste;
    }

    public static void setListe(ArrayList<Flight> liste) {
        FlightList.liste = liste;
    }
   public static void main(String[] args) throws SQLException // for testing purposes !
 {
       Connection conn1 = null;
       try{
           DBConnection dbc = new DBConnection();
        conn1 = dbc.getConnection();
        FlightList fl = new FlightList();
        fl.listAllFlights(conn1);
        LOG.info("main- after list");
        
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn1, null, null,null); 
          }
  
   } // end main//
} //end Class