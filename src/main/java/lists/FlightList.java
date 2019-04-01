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
public class FlightList implements interfaces.Log
{
    private static ArrayList<Flight> liste = null;
  //  private static final Connection CONN = null;
    
public ArrayList<Flight> listAllFlights(final Connection conn) throws Exception{
if(liste == null)
{    
    LOG.debug("liste == null; then starting listAllFlights()," );
        PreparedStatement ps = null;
        ResultSet rs = null;
        liste = new ArrayList<>();
        String fl = utils.DBMeta.listMetaColumnsLoad(conn, "flight");
        final String query =
            "SELECT "
                + fl +
       //         + " flight.idflight, flight.FlightStart, flight.course_idcourse" +
            " FROM flight" +
            " WHERE DATE_FORMAT(flight.FlightStart, '%Y-%m-%d %H:%i')" +   // élimine les secondes
            "     NOT IN" +
            "     (" +
            "     SELECT DATE_FORMAT(round.RoundDate, '%Y-%m-%d %H:%i')" +
            "     FROM round" +
            "     WHERE round.course_idcourse = flight.course_idcourse" +
            "     )" +
            " ORDER BY flight.FlightStart";
try{
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            liste = new ArrayList<>();
            while (rs.next())
            { //  
                Flight f = new Flight(); 
                f = entite.Flight.mapFlight(rs);
// ici insérer chargement from mauvaise idée
//                f.setRoundPlayers(Short.MIN_VALUE);
               liste.add(f);
            } // end while

//  liste.forEach(item -> LOG.info("Flight list " + item));  // java 8 lambda
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
        FlightList.liste = liste;
    }
 public static void main(String[] args) throws SQLException {
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