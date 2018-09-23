package entite;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.inject.Named;
//import javax.validation.constraints.*;
//import javax.validation.constraints.Pattern;
//import javax.validation.constraints.Size;
/**
 *
 * @author collet
 */
@Named
public class Flight implements Serializable, interfaces.Log
{
    private static final long serialVersionUID = 1L;

    private Integer idflight;
    private LocalDateTime flightStart;
    private Integer course_idcourse;
    private String flightPeriod;

 
    public Flight()
    {
       
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

     @Override
public String toString()
{ return 
        ("from entite : " + this.getClass().getSimpleName()
               + " ,idflight : "   + this.getIdflight()
               + " ,FlightStart : " + this.getFlightStart()
               + " ,course_idcourse : " + this.getCourse_idcourse()
               + " ,period : " + this.getFlightPeriod()
        );
}

} // end class