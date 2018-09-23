package googlemaps;

import static interfaces.Log.LOG;

/*{
      "results":
      {
        "sunrise":"7:27:02 AM",
        "sunset":"5:05:55 PM",
        "solar_noon":"12:16:28 PM",
        "day_length":"9:38:53",
        "civil_twilight_begin":"6:58:14 AM",
        "civil_twilight_end":"5:34:43 PM",
        "nautical_twilight_begin":"6:25:47 AM",
        "nautical_twilight_end":"6:07:10 PM",
        "astronomical_twilight_begin":"5:54:14 AM",
        "astronomical_twilight_end":"6:38:43 PM"
      },
       "status":"OK"
    }
*/
// pour utiliser le mapper : donner les mêmes noms que dans Json (et String ou integer)
// commen utiliser les dates ?

public class SunriseSunsetResult {

private String sunrise ;
private String sunset;
private String solar_noon;
private String day_length ;
private String civil_twilight_begin;
private String civil_twilight_end;
private String nautical_twilight_begin;
private String nautical_twilight_end;
private String astronomical_twilight_begin;
private String astronomical_twilight_end;
private String status;
//private String error_message;

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getSolar_noon() {
        return solar_noon;
    }

    public void setSolar_noon(String solar_noon) {
        this.solar_noon = solar_noon;
    }

    public String getDay_length() {
        return day_length;
    }

    public void setDay_length(String day_length) {
        this.day_length = day_length;
    }

    public String getCivil_twilight_begin() {
        return civil_twilight_begin;
    }

    public void setCivil_twilight_begin(String civil_twilight_begin) {
        this.civil_twilight_begin = civil_twilight_begin;
    }

    public String getCivil_twilight_end() {
        return civil_twilight_end;
    }

    public void setCivil_twilight_end(String civil_twilight_end) {
        this.civil_twilight_end = civil_twilight_end;
    }

    public String getNautical_twilight_begin() {
        return nautical_twilight_begin;
    }

    public void setNautical_twilight_begin(String nautical_twilight_begin) {
        this.nautical_twilight_begin = nautical_twilight_begin;
    }

    public String getNautical_twilight_end() {
        return nautical_twilight_end;
    }

    public void setNautical_twilight_end(String nautical_twilight_end) {
        this.nautical_twilight_end = nautical_twilight_end;
    }

    public String getAstronomical_twilight_begin() {
        return astronomical_twilight_begin;
    }

    public void setAstronomical_twilight_begin(String astronomical_twilight_begin) {
        this.astronomical_twilight_begin = astronomical_twilight_begin;
    }

    public String getAstronomical_twilight_end() {
        return astronomical_twilight_end;
    }

    public void setAstronomical_twilight_end(String astronomical_twilight_end) {
        this.astronomical_twilight_end = astronomical_twilight_end;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    
public String toString()
{ return 
        ("from " + getClass().getSimpleName() + " : "
               + " ,sunrise : " + this.sunrise
               + " ,sunset : " + this.sunset
   //            + " ,ZoneId : " + this.timeZoneId
   //            + " ,ZoneName : " + this.timeZoneName
   //            + " ,status : " + this.status
   //            + " ,error_message : " + this.error_message
        );
}   
 
} //end class