package entite;

import static interfaces.GolfInterface.ZDF_HOURS;
import static interfaces.Log.LOG;
// import jakarta.enterprise.context.RequestScoped;  // migrated 2026-02-24
// import jakarta.inject.Named;  // migrated 2026-02-24
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import utils.LCUtil;

// @Named  // migrated 2026-02-24
// @RequestScoped  // migrated 2026-02-24
public class Flight implements Serializable, interfaces.Log{
    
    private static final long serialVersionUID = 1L;
    private Integer idflight;
    private LocalDateTime flightStart;
    private Integer course_idcourse;
    private String flightPeriod;
    private ZonedDateTime sunrise;
    private ZonedDateTime sunset;
    private ZonedDateTime firstFlight;
    private ZonedDateTime lastFlight;
    
 public Flight(){
       
    }

    public Integer getIdflight() {
        return idflight;
    }

    public void setIdflight(Integer idflight) {
        this.idflight = idflight;
    }

    public LocalDateTime getFlightStart() {
            return flightStart;
    }
    public String getFlightHourStart() {
        //this.getRoundDate().format(ZDF_TIME_HHmm)
        return flightStart.format(ZDF_HOURS);
    }
    
    public void setFlightStart(LocalDateTime flightDepart) {
        this.flightStart = flightDepart;
    }

    public String getFlightPeriod() {
        return flightPeriod;
    }

    public void setFlightPeriod(String flightPeriod) {
        this.flightPeriod = flightPeriod;
    }

    public Integer getCourse_idcourse() {
        return course_idcourse;
    }

    public void setCourse_idcourse(Integer course_idcourse) {
        this.course_idcourse = course_idcourse;
    }

    public ZonedDateTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(ZonedDateTime sunrise) {
        this.sunrise = sunrise;
    }

    public ZonedDateTime getSunset() {
        return sunset;
    }

    public void setSunset(ZonedDateTime sunset) {
        this.sunset = sunset;
    }

    public ZonedDateTime getFirstFlight() {
        return firstFlight;
    }

    public void setFirstFlight(ZonedDateTime firstFlight) {
        this.firstFlight = firstFlight;
    }

    public ZonedDateTime getLastFlight() {
        return lastFlight;
    }

    public void setLastFlight(ZonedDateTime lastFlight) {
        this.lastFlight = lastFlight;
    }

     @Override
public String toString(){
  try{
     //   LOG.debug("starting toString Flight !");
    return 
        (NEW_LINE + "FROM ENTITE : " + this.getClass().getSimpleName()
               + " ,idflight : "   + this.getIdflight()
               + " ,FlightStart : " + this.getFlightStart()
               + " ,course_idcourse : " + this.getCourse_idcourse()
               + " ,period : " + this.getFlightPeriod()
               + " ,sunrise : " + this.getSunrise()
               + " ,sunset : " + this.getSunset()
        );
   }catch(Exception e){
        String msg = "£££ Exception in Flight.toString = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return msg;
  }
}
public static Flight mapFlight(ResultSet rs) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(); 
  try{
          Flight flight = new Flight();
          flight.setIdflight(rs.getInt("idflight") );
          flight.setFlightStart(rs.getTimestamp("FlightStart").toLocalDateTime());
          flight.setCourse_idcourse(rs.getInt("flight.course_idcourse"));
          flight.setFlightPeriod(rs.getString("FlightPeriod"));
   return flight;
  }catch(Exception e){
   String msg = "£££ Exception in rs = " + methodName + " / "+ e.getMessage(); //+ " for player = " + p.getPlayerLastName();
   LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
  }
} //end method
} // end class