
package lists;

import entite.Flight;
import jakarta.enterprise.context.ApplicationScoped;
import utils.LCUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static interfaces.Log.LOG;

/**
 * Génère la liste des départs (flights) entre sunrise et sunset
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — non utilisée dans l'original
 * ✅ try-with-resources non nécessaire — pas de ressources JDBC
 * ✅ return null remplacé par Collections.emptyList()
 * ✅ main() commentée
 */
@ApplicationScoped
public class AllFlightsList implements interfaces.GolfInterface, Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — non utilisée dans l'original
    // Pas de cache : les inputs (sunrise/sunset/timezone) varient selon la date et le course
    // ========================================

    /**
     * Génère la table des départs entre sunrise+20min et sunset-2h30
     * Intervalle de 12 minutes entre chaque départ
     *
     * @param flight objet contenant sunrise et sunset
     * @param tz     timezone du club (ex: "Europe/Brussels")
     * @return liste des flights générés, jamais null
     */
    public ArrayList<Flight> generateFlights(Flight flight, String tz) {
        final String methodName = "AllFlightsList.generateFlights";

        LOG.debug("{} - generating flights for tz={}", methodName, tz);

        try {
            LOG.debug("{} - sunrise before tz conversion = {}", methodName, flight.getSunrise());

            // Conversion en timezone locale
            flight.setSunrise(flight.getSunrise().toInstant().atZone(ZoneId.of(tz)));
            flight.setSunset(flight.getSunset().toInstant().atZone(ZoneId.of(tz)));

            LOG.debug("{} - sunrise = {}", methodName, flight.getSunrise());
            LOG.debug("{} - sunset  = {}", methodName, flight.getSunset());

            // Premier et dernier départ
            ZonedDateTime firstFlight = flight.getSunrise().plusMinutes(20 + 12);
            ZonedDateTime lastFlight  = flight.getSunset().minusHours(2).minusMinutes(30 + 12);

            // Curseur de départ : sunrise + 20min (le +12 s'ajoute en début de boucle)
            flight.setSunrise(flight.getSunrise().plusMinutes(20));

            ArrayList<Flight> result = new ArrayList<>();
            int i = 0;

            while (flight.getSunrise().isBefore(flight.getSunset().minusHours(2).minusMinutes(30))) {
                i++;
                flight.setSunrise(flight.getSunrise().plusMinutes(12));

                LocalDateTime ld = flight.getSunrise().toLocalDateTime();
                LocalTime     lt = flight.getSunrise().toLocalTime();

                Flight fl = new Flight();
                fl.setFlightStart(ld);
                fl.setFirstFlight(firstFlight);
                fl.setLastFlight(lastFlight);
                fl.setFlightPeriod(computePeriod(lt));

                result.add(fl);
            }

            LOG.debug("{} - generated {} flights", methodName, i);

            return result;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
            return new ArrayList<>(Collections.emptyList());           // ✅ jamais null
        }
    } // end method

    // ========================================
    // MÉTHODES PRIVÉES
    // ========================================

    /**
     * Détermine la période de la journée selon l'heure de départ
     * A = matin (avant 12h), B = midi (12h-14h), C = après-midi (après 14h)
     */
    private String computePeriod(LocalTime lt) {
        if (lt.isBefore(LocalTime.of(12, 0))) {
            return "A";
        } else if (lt.isAfter(LocalTime.of(14, 0))) {
            return "C";
        } else {
            return "B";
        }
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    void main(String[] args) throws ParseException, SQLException {
        LOG.debug("entering main with arguments length = " + args.length);
        if (args.length > 0) {
            for (String arg : args) {
                LOG.debug(arg);
            }
        }
        try {
            Connection conn = new DBConnection().getConnection();
            // ArrayList<Flight> flight = createTableFlights("2017-04-09T04:39:02+00:00",
            //     "2017-04-09T18:28:53+00:00", "Europe/Brussels", 11, conn);
            LOG.debug("after suncalc:");
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(null, null, null, null);
        }
    } // end main
    */

} // end class
