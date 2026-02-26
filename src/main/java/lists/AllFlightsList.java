
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

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private ArrayList<Flight> liste = null;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — non utilisée dans l'original
    // ========================================

    /**
     * Génère la table des départs entre sunrise+20min et sunset-2h30
     * Intervalle de 12 minutes entre chaque départ
     *
     * @param flight objet contenant sunrise et sunset
     * @param tz     timezone du club (ex: "Europe/Brussels")
     * @return liste des flights générés, jamais null
     */
    public ArrayList<Flight> createTableFlights(Flight flight, String tz) {
        final String methodName = "AllFlightsList.createTableFlights";

        if (liste != null) {
            LOG.debug("{} - returning cached list ({} entries)", methodName, liste.size());
            return liste;
        }

        LOG.debug("{} - liste is null, generating flights", methodName);

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

            liste = result;                                             // ✅ mise en cache
            return liste;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(msg);
            return new ArrayList<>(Collections.emptyList());           // ✅ jamais null
        }
    }

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
    }

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
/*
import entite.Flight;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import connection_package.DBConnection;
import utils.LCUtil;

public class AllFlightsList implements interfaces.GolfInterface{

//private static Statement stm = null;
//private static ResultSet rs = null;
private static ArrayList<Flight> liste = null;

    public ArrayList<Flight> createTableFlights (Flight flight, String tz, final Connection conn) throws ParseException{
    
//   LOG.debug("entering createTableFlights ");
 if(liste == null){ 
       LOG.debug("liste == null then we do something ");
    //   LOG.debug("with course = " + course.toString());
   try{
        // test en partant d'une heure de départ en en arrêtant à une heure de fin, créer des départs Golf
            LOG.debug("String date sunrise = " + flight.getSunrise());
 // ZonedDateTime dateTime = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");
        flight.setSunrise(flight.getSunrise().toInstant().atZone(ZoneId.of(tz)));
           LOG.debug("sunrise = " + flight.getSunrise()); //sunrise);
        flight.setSunset(flight.getSunset().toInstant().atZone(ZoneId.of(tz)));
           LOG.debug (" sunset = " + flight.getSunset());
   //     ZonedDateTime a = ZonedDateTime.of(2008, 6, 30, 23, 30, 59, 0,ZONE_0100);
  //      ZonedDateTime marketOpens = ZonedDateTime.of ( LocalDate.of ( 2016 , 1 , 4 ) , LocalTime.of ( 9 , 30 ) , ZoneId.of ( "America/New_York" ) );
        liste = new ArrayList<>();
        int i = 0;
        ZonedDateTime ff = flight.getSunrise().plusMinutes(20+12);
  //          LOG.debug("premier départ 0h20 plus 12 après sunrise = " + ff);
        ZonedDateTime lf = flight.getSunset().minusHours(2).minusMinutes(30+12);
 //           LOG.debug("dernier départ 2h30 avant sunset = " + lf);
        flight.setSunrise(flight.getSunrise().plusMinutes(20)); // fera  environ 30 mn après sunrise : 20 + 12
        while(flight.getSunrise().isBefore(flight.getSunset().minusHours(2).minusMinutes(30))){ // dernier départ 2 heures 30 avant sunset
            i++;
            Flight fl = new Flight();
            flight.setSunrise(flight.getSunrise().plusMinutes(12));
            LocalDateTime ld = flight.getSunrise().toLocalDateTime();
            fl.setFlightStart(ld);
            LocalTime lt = flight.getSunrise().toLocalTime();
            fl.setFirstFlight(ff);
            fl.setLastFlight(lf);
                if(lt.isBefore(LocalTime.of(12,0) )){  // LOG.debug("lt isBefore 12 HH == " + lt);
                    fl.setFlightPeriod("A");
                } else if (lt.isAfter(LocalTime.of(14,0))){ // LOG.debug("lt isAfter 14 HH == " + lt);
                    fl.setFlightPeriod("C");
                }else{    //                    LOG.debug("lt isBetween 12 and 14 HH == " + lt);
                    fl.setFlightPeriod("B");
                }
            liste.add(fl); // mod 17/04/2017
//            LOG.debug("Flight 4 " + i + " = " + liste.toString() );
        }

  //       liste.forEach((n) -> {LOG.debug("at end of createTableFlights, Flight = " + n); }); 
   return liste;
   } catch (Exception e) {
            String msg = "£££ Exception in Insert flight = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   }
 }else{
     LOG.debug("escaped to createAllFlights repetition thanks to lazy loading");
     return liste;  //plusieurs fois ??
    }
} // end method

    public static ArrayList<Flight> getListe() {
        return liste;
    }

    public static void setListe(ArrayList<Flight> liste) {
        AllFlightsList.liste = liste;
    }
 
 void main(String [] args) throws ParseException, SQLException {
     LOG.debug("entering main with arguments length = " + args.length);
     if(args.length > 0){
         for (String arg : args){
             LOG.debug(arg);
         }
     }
  try{
        Connection conn = new DBConnection().getConnection();
//// à modifier        ArrayList<Flight> flight = createTableFlights("2017-04-09T04:39:02+00:00", "2017-04-09T18:28:53+00:00","Europe/Brussels", 11 ,conn );
        LOG.debug("after suncalc:" );
//        stm = conn.createStatement();
//        rs = stm.executeQuery("select * FROM flight");  //delete all records
//        while (rs.next()) {
//            LOG.debug("flight from DB = " + rs.getString(2) + "/" + rs.getString(4));
//        }
  //      for (String n : flight) {
  //          System.out.println("from main : Flight = " + n);
//        }

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
           LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(null, null, null, null); 
          }
   } // end main//
}  //end class
*/