
package create;

import entite.Course;
import entite.Flight;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import utils.LCUtil;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static interfaces.Log.LOG;

/**
 * Insère la liste des flights en base pour un course donné
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — gérée via DataSource injecté
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ main() commentée
 */
@ApplicationScoped
public class CreateTableFlights implements interfaces.GolfInterface, Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection DataSource WildFly
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — gérée en interne via DataSource
    // ========================================

    /**
     * Supprime tous les flights existants et insère la nouvelle liste pour le course donné.
     *
     * @param flights  liste des flights à insérer
     * @param course   le course associé
     * @return true si succès, false sinon
     */
    public boolean create(final ArrayList<Flight> flights, final Course course) {
        final String methodName = "CreateTableFlights.create";

        LOG.debug("{} - flights size={}", methodName, flights.size());
        LOG.debug("{} - course={}", methodName, course);

        // ✅ try-with-resources : Connection et Statement fermés automatiquement
        try (Connection conn = dataSource.getConnection();
             Statement stm = conn.createStatement()) {

            // Suppression de tous les records existants
            int count = stm.executeUpdate("DELETE FROM flight");
            LOG.debug("{} - table flight emptied, {} records deleted", methodName, count);

            final String query = LCUtil.generateInsertQuery(conn, "flight");
            LOG.debug("{} - query={}", methodName, query);

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                for (Flight fl : flights) {
                    ps.setNull(1, java.sql.Types.INTEGER);              // auto-increment

                    LocalDateTime ldt = fl.getFlightStart();
                    ps.setTimestamp(2, Timestamp.valueOf(ldt));
                    ps.setInt(3, course.getIdcourse());
                    ps.setString(4, fl.getFlightPeriod());

                    LCUtil.logps(ps);
                    int row = ps.executeUpdate();

                    if (row == 0) {
                        String msg = "Insert failed for flight: " + fl;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
                } // end for

            } // ps fermé automatiquement

            return true;

        } catch (SQLException sqle) {
            String msg = String.format("SQLException in %s: %s , SQLState=%s , ErrorCode=%d",
                    methodName, sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode());
            LOG.error(msg, sqle);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    }

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main() throws ParseException {
        try {
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
    } // end main
    */

} // end class
/*
import entite.Course;
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
import connection_package.DBConnection;
import utils.LCUtil;

public class CreateTableFlights implements interfaces.GolfInterface{
    private static Statement stm = null;

  public boolean create(final ArrayList<Flight> flight, final Course course, final Connection conn ) throws SQLException{
        PreparedStatement ps = null;
   try {
            LOG.debug(" ... entering createFlights() with flight size = " + flight.size()); // FlightStart and FlightPeriod only completed
            LOG.debug(" ... entering createFlights() with idcourse = " + course);
       //     flight.forEach(item -> LOG.debug("FlightList list " + item));
       stm = conn.createStatement();
       int count = stm.executeUpdate("DELETE FROM flight");  //delete all records
        LOG.debug(" -- Table flight is now empty, records deleted = " + count);
       final String query = LCUtil.generateInsertQuery(conn, "flight");
          LOG.debug("String query = " + query);
       ps = conn.prepareStatement(query);
      // insérer dans l'ordre de la database : 1 = first db field
        for (Flight n : flight) {
            ps.setNull(1, java.sql.Types.INTEGER);//  default auto-increment in Database
            LocalDateTime ldt = n.getFlightStart();
         //   LOG.debug("LocalDateTime = " + ldt);
            ps.setTimestamp(2, Timestamp.valueOf(ldt));
         //    LOG.debug("idcourse = " + idcourse);
            ps.setInt(3, course.getIdcourse());
            ps.setString(4,n.getFlightPeriod());
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            utils.LCUtil.logps(ps);
            if (row != 0) {
        //        LOG.debug("row = " + row);//
            } else {
                String msg = "<br/>NOT NOT insert for flight = " + n;
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        } // end for
     return true;
        } catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert flight = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in Insert flight = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
           // DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/5014
        }
//return false;
    } //end method
    
  void main() throws ParseException {
  try{
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }
   } // end main//
} // end class
*/