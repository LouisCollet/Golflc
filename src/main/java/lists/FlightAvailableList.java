package lists;

import entite.Flight;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import utils.DBConnection;
import utils.LCUtil;

// ce programme élimine les flights déjà réservés !
public class FlightAvailableList implements interfaces.Log{
    private static ArrayList<Flight> liste = null;

public ArrayList<Flight> listAllFlights(final Connection conn) throws Exception{
    
if(liste == null){ 
    LOG.debug("liste == null; then starting listAllFlights()," );
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
        liste = new ArrayList<>();
 //       String fl = utils.DBMeta.listMetaColumnsLoad(conn, "flight");
 /*       final String query =
            "SELECT *" +
            " FROM flight" +
            " WHERE DATE_FORMAT(flight.FlightStart, '%Y-%m-%d %H:%i')" +   // élimine les secondes
            "     NOT IN" +
            "     (" +
            "     SELECT DATE_FORMAT(round.RoundDate, '%Y-%m-%d %H:%i')" +
            "     FROM round" +
            "     WHERE round.course_idcourse = flight.course_idcourse" +
            "     )" +
            " ORDER BY flight.FlightStart";
*/
          final String query = """
            SELECT *
            FROM flight
            WHERE DATE_FORMAT(flight.FlightStart, '%Y-%m-%d %H:%i')
               NOT IN
                 (
                 SELECT DATE_FORMAT(round.RoundDate, '%Y-%m-%d %H:%i')
                 FROM round
                 WHERE round.course_idcourse = flight.course_idcourse
                 )
             ORDER BY flight.FlightStart
         """;
   ps = conn.prepareStatement(query);
   rs = ps.executeQuery();
  liste = new ArrayList<>();
  while(rs.next()){
      Flight f = entite.Flight.mapFlight(rs);
      liste.add(f);
   } // end while
//  liste.forEach(item -> LOG.debug("Flight list " + item));
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
}

} //end method

    public static ArrayList<Flight> getListe() {
        return liste;
    }

    public static void setListe(ArrayList<Flight> liste) {
        FlightAvailableList.liste = liste;
    }
 void main() throws SQLException {
       Connection conn1 = null;
 try{
        DBConnection dbc = new DBConnection();
        conn1 = dbc.getConnection();
        FlightAvailableList fl = new FlightAvailableList();
        fl.listAllFlights(conn1);
        LOG.debug("main- after list");
        
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn1, null, null,null); 
          }
  
   } // end main//
} //end Class