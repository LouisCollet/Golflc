
package create;

import entite.Flight;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import utils.DBConnection;
import utils.LCUtil;

public class CreateAllFlights implements interfaces.Log, interfaces.GolfInterface{
    
private static Statement stm = null;
private static ResultSet rs = null;
private static ArrayList<Flight> liste = null;

public ArrayList<Flight> createTableFlights (ZonedDateTime sunrise, ZonedDateTime sunset,
                                                        String tz, int courseid, Connection conn) throws ParseException         
            
    {  LOG.info("entering createTableFlights ");
        //    LOG.info+ liste.toString());
   if(liste == null)
   { 
       LOG.info("liste == null");
        try{
        // test en partant d'une heure de départ en en arrêtant à une heure de fin, créer des départs Golf
            LOG.info("String date sunrise = " + sunrise);
 ////       ZonedDateTime sunrise = ZonedDateTime.parse(date_sunrise, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
 //           LOG.info("ZonedDateTime with iso-offset_date_time: " + sunrise);
 // ZonedDateTime dateTime = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]");

        Instant instant = sunrise.toInstant();
//            LOG.info("instant = " + instant);
        sunrise = instant.atZone(ZoneId.of(tz));
//        LOG.info("tz = " + tz);
//            LOG.info ("formatted tz sunrise = " + dtf_HHmm.format(sunrise));
//           LOG.info("String date sunset = " + date_sunset);
  ////      ZonedDateTime sunset = ZonedDateTime.parse(date_sunset, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        sunset = sunset.toInstant().atZone(ZoneId.of(tz));
//           LOG.info ("formatted tz sunset = " + dtf_HHmm.format(sunset));

 //       LocalTime HOUR_12 = LocalTime.of(12,0);
 //       LocalTime HOUR_14 = LocalTime.of(14,0);
   //     ZonedDateTime a = ZonedDateTime.of(2008, 6, 30, 23, 30, 59, 0,ZONE_0100);
  //      ZonedDateTime marketOpens = ZonedDateTime.of ( LocalDate.of ( 2016 , 1 , 4 ) , LocalTime.of ( 9 , 30 ) , ZoneId.of ( "America/New_York" ) );
      //  ArrayList<Flight> liste = new ArrayList<>();
        liste = new ArrayList<>();
        int i = 0;
        while (sunrise.isBefore(sunset.minusHours(2).minusMinutes(30))) // dernier départ 2 heures 30 avant sunset
        {    // Adding items to arrayList
            i++;
            Flight fl = new Flight(); 
            sunrise = sunrise.plusMinutes(12); // un départ toutes les 12 minutes
//                LOG.info("Flight 1 " + i + " = " + dtf_HHmm.format(sunrise));
//                LOG.info("Flight 2 " + i + " = " + sunrise);
            LocalDateTime ld = sunrise.toLocalDateTime();
       //     fl.setFlightStart(ZDF_TIME.format(sunrise));
            fl.setFlightStart(ld);
//                LOG.info("Flight 3 " + i + " = " + fl.getFlightStart());
            LocalTime lt = sunrise.toLocalTime();
//                LOG.info("local time lt = " + lt);
                if(lt.isBefore(LocalTime.of(12,0) )){
//                    LOG.info("lt isBefore 12 HH == " + lt);
                    fl.setFlightPeriod("A");
                } else if (lt.isAfter(LocalTime.of(14,0))){
//                    LOG.info("lt isAfter 14 HH == " + lt);
                    fl.setFlightPeriod("C");
                }else{
//                    LOG.info("lt isBetween 12 and 14 HH == " + lt);
                    fl.setFlightPeriod("B");
                }
                   
            liste.add(fl); // mod 17/04/2017
//            LOG.info("Flight 4 " + i + " = " + liste.toString() );
        }
////        LOG.info("Arraylist = " + flight.toString()); //Arrays.toString(list));
//        LOG.info("get12 : " + flight.get(12));  //exemple
           //       flight.forEach(System.out::println);
           // let us print all the elements available in list
           liste.forEach((n) -> {
               LOG.info("Flight = " + n);
           }); // initialisation et recréation de la table flight
         CreateFlights cf = new CreateFlights();
         boolean OK = cf.createFlight(liste, courseid, conn);  // fake = courseid
            LOG.info("boolean result create.CreateFlights = " + OK);
  // elimination des flights déjà réservés  ////////////        
        lists.FlightList fl = new lists.FlightList();
        fl.listAllFlights(conn);
            LOG.info("boolean result lists.FlightList =  = " + OK);
   return liste;  // à modifier, pas nécessaire !
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
        CreateAllFlights.liste = liste;
    }
 
 public static void main(String[] args) throws ParseException, SQLException {
  try{
      DBConnection dbc = new DBConnection();
        Connection conn = dbc.getConnection();
//// à modifier        ArrayList<Flight> flight = createTableFlights("2017-04-09T04:39:02+00:00", "2017-04-09T18:28:53+00:00","Europe/Brussels", 11 ,conn );
        LOG.info("after suncalc:" );
        stm = conn.createStatement();
        rs = stm.executeQuery("select * FROM flight");  //delete all records
        while (rs.next()) {
            LOG.info("flight from DB = " + rs.getString(2) + "/" + rs.getString(4));
        }
  //      for (String n : flight) {
  //          System.out.println("from main : Flight = " + n);
//        }

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
           LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(null, stm, rs, null); 
          }
   } // end main//
}  //end class
