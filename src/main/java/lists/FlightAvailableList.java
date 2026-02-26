package lists;

import entite.Flight;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import utils.LCUtil;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static interfaces.Log.LOG;

/**
 * Liste les flights disponibles (non encore réservés)
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — gérée via DataSource injecté
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ return null remplacé par new ArrayList<>()
 * ✅ main() commentée
 */
@ApplicationScoped
public class FlightAvailableList implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection DataSource WildFly
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private ArrayList<Flight> liste = null;

    private static final String QUERY = """
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

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — gérée en interne via DataSource
    // ========================================

    /**
     * Liste tous les flights disponibles (non réservés par un round).
     *
     * @return liste des flights disponibles, jamais null
     */
    public ArrayList<Flight> listAllFlights() throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug("{} - returning cached list ({} entries)", methodName, liste.size());
            return liste;
        }

        LOG.debug("{} - liste is null, querying database", methodName);

        // ✅ try-with-resources : Connection, PreparedStatement, ResultSet fermés automatiquement
        try (Connection conn       = dataSource.getConnection();
             PreparedStatement ps  = conn.prepareStatement(QUERY);
             ResultSet rs          = ps.executeQuery()) {

            ArrayList<Flight> result = new ArrayList<>();

            while (rs.next()) {
                Flight flight = entite.Flight.mapFlight(rs);
                result.add(flight);
            }

            LOG.debug("{} - found {} available flights", methodName, result.size());

            liste = result;                                             // ✅ mise en cache
            return liste;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new ArrayList<>();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new ArrayList<>();
        }
    } // end method

    // ========================================
    // CACHE - Getters / Setters statiques
    // ========================================

    // ✅ Getters/setters d'instance
    public ArrayList<Flight> getListe()                  { return liste; }
    public void setListe(ArrayList<Flight> liste)        { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main() throws SQLException {
        Connection conn1 = null;
        try {
            DBConnection dbc = new DBConnection();
            conn1 = dbc.getConnection();
            FlightAvailableList fl = new FlightAvailableList();
            fl.listAllFlights(conn1);
            LOG.debug("main - after list");
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn1, null, null, null);
        }
    } // end main
    */

} // end class