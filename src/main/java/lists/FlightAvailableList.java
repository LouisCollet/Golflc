package lists;

import entite.Flight;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Liste les flights disponibles (non encore réservés)
 * ✅ Migré vers GenericDAO
 * ✅ main() commentée
 */
@ApplicationScoped
public class FlightAvailableList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

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

    /**
     * Liste tous les flights disponibles (non réservés par un round).
     *
     * @return liste des flights disponibles, jamais null
     */
    public ArrayList<Flight> listAllFlights() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug("{} - returning cached list ({} entries)", methodName, liste.size());
            return liste;
        }

        LOG.debug("{} - liste is null, querying database", methodName);

        List<Flight> result = dao.queryList(QUERY, rs -> entite.Flight.mapFlight(rs));
        liste = new ArrayList<>(result);

        LOG.debug("{} - found {} available flights", methodName, liste.size());
        return liste;
    } // end method

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
