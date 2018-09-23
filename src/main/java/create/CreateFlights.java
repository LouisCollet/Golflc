
package create;

import entite.Flight;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import utils.DBConnection;
import utils.LCUtil;

public class CreateFlights implements interfaces.Log, interfaces.GolfInterface
{
    private static Statement stm = null;

    public boolean createFlight(final ArrayList<Flight> flight, final int idcourse, final Connection conn ) throws SQLException
    {
        PreparedStatement ps = null;
        try {
            LOG.info(" ... entering createFlights() with flight size = " + flight.size());
   //         LOG.info("Round ID = " + round.getIdround());
//            LOG.info("sc1 = " + Arrays.deepToString(flight));
       stm = conn.createStatement();
       int count = stm.executeUpdate("DELETE FROM flight");  //delete all records
        LOG.info(" -- Table flight is now empty, records deleted = " + count);
       final String query = LCUtil.generateInsertQuery(conn, "flight"); 
          LOG.info("String query = " + query);
       ps = conn.prepareStatement(query);
      // insérer dans l'ordre de la database : 1 = first db field
        for (Flight n : flight) {
                LOG.info("Flight in for loop = " + n);
            ps.setNull(1, java.sql.Types.INTEGER);//  default auto-increment in Database
        //        LOG.info("line 1 = ok ");
        //    ps.setTimestamp(2, LCUtil.getSqlTimestamp(SDF_TIME.parse(n.getFlightStart())) );
            LocalDateTime d = n.getFlightStart(); //.toLocalDate();
     //           LOG.info("line 2 = ok ");
            java.sql.Timestamp ts = Timestamp.valueOf(d); //.atStartOfDay());  // error !
     //           LOG.info("line 3 = ok");
      //                  LOG.info("new endDate inserted in DB = " + ts);
            ps.setTimestamp(2, ts);
       //     ps.setTimestamp(2, LCUtil.getSqlTimestamp(n.getFlightStart()) ); 
            ps.setInt(3, idcourse); 
            ps.setString(4,n.getFlightPeriod()); 
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0) {
               int key = LCUtil.generatedKey(conn);
                    LOG.info("flight generatedKey = " + key);
               String msg = "<br/>Successful insert for flight = " + n;
                    LOG.info(msg);
              // LCUtil.showMessageInfo(msg);
            } else {
                        String msg = "<br/>NOT NOT insert for flight = " + n;
             //                   + " , points = " + sc1[i] 
             //                   + " , round = " + round.getIdround();
               //     + " , hole = " + score.getScoreHole()
                        //     + " , Strokes = "  + score.getScoreStroke();
                        LOG.info(msg);
                        LCUtil.showMessageFatal(msg);
                    }
                } // end for
return true;
        } catch (NullPointerException npe) {
            String msg = "£££ NullPointerException in Insert flight = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (IndexOutOfBoundsException iobe) {
            String msg = "£££ IndexOutOfBoundsException in Insert flight = " + iobe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert flight = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Insert flght = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (Exception e) {
            String msg = "£££ Exception in Insert flight = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
           // DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/5014
        }
return false;
    } //end method
    
    public static void main(String[] args) throws ParseException //throws IOException
     {
  try{

        
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }
  
   } // end main//
    
    
    
} // end class
