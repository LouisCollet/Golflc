
package entite;

// -----------------------------------entite.Current.java-----------------------------------



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"dt",
"sunrise",
"sunset",
"temp",
"feels_like",
"pressure",
"humidity",
"dew_point",
"uvi",
"clouds",
"visibility",
"wind_speed",
"wind_deg",
"wind_gust",
"weather"
})

@Generated("jsonschema2pojo")
public class OpenWeatherOnecall {

@JsonProperty("lat")
private Double lat;
@JsonProperty("lon")
private Double lon;
@JsonProperty("timezone")
private String timezone;
@JsonProperty("timezone_offset")
private Integer timezoneOffset;
@JsonProperty("current")
@Valid
private Current current;
@JsonProperty("minutely")
@Valid
private List<Minutely> minutely = null;
@JsonProperty("hourly")
@Valid
private List<Hourly> hourly = null;
@JsonProperty("daily")
@Valid
private List<Daily> daily = null;

@JsonProperty("lat")
public Double getLat() {
return lat;
}

@JsonProperty("lat")
public void setLat(Double lat) {
this.lat = lat;
}

@JsonProperty("lon")
public Double getLon() {
return lon;
}

@JsonProperty("lon")
public void setLon(Double lon) {
this.lon = lon;
}

@JsonProperty("timezone")
public String getTimezone() {
return timezone;
}

@JsonProperty("timezone")
public void setTimezone(String timezone) {
this.timezone = timezone;
}

@JsonProperty("timezone_offset")
public Integer getTimezoneOffset() {
return timezoneOffset;
}

@JsonProperty("timezone_offset")
public void setTimezoneOffset(Integer timezoneOffset) {
this.timezoneOffset = timezoneOffset;
}

@JsonProperty("current")
public Current getCurrent() {
return current;
}

@JsonProperty("current")
public void setCurrent(Current current) {
this.current = current;
}

@JsonProperty("minutely")
public List<Minutely> getMinutely() {
return minutely;
}

@JsonProperty("minutely")
public void setMinutely(List<Minutely> minutely) {
this.minutely = minutely;
}

@JsonProperty("hourly")
public List<Hourly> getHourly() {
return hourly;
}

@JsonProperty("hourly")
public void setHourly(List<Hourly> hourly) {
this.hourly = hourly;
}

@JsonProperty("daily")
public List<Daily> getDaily() {
return daily;
}

@JsonProperty("daily")
public void setDaily(List<Daily> daily) {
this.daily = daily;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(OpenWeatherOnecall.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("lat");
sb.append('=');
sb.append(((this.lat == null)?"<null>":this.lat));
sb.append(',');
sb.append("lon");
sb.append('=');
sb.append(((this.lon == null)?"<null>":this.lon));
sb.append(',');
sb.append("timezone");
sb.append('=');
sb.append(((this.timezone == null)?"<null>":this.timezone));
sb.append(',');
sb.append("timezoneOffset");
sb.append('=');
sb.append(((this.timezoneOffset == null)?"<null>":this.timezoneOffset));
sb.append(',');
sb.append("current");
sb.append('=');
sb.append(((this.current == null)?"<null>":this.current));
sb.append(',');
sb.append("minutely");
sb.append('=');
sb.append(((this.minutely == null)?"<null>":this.minutely));
sb.append(',');
sb.append("hourly");
sb.append('=');
sb.append(((this.hourly == null)?"<null>":this.hourly));
sb.append(',');
sb.append("daily");
sb.append('=');
sb.append(((this.daily == null)?"<null>":this.daily));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

//}

public class Current {

@JsonProperty("dt")
private Integer dt;
@JsonProperty("sunrise")
private Integer sunrise;
@JsonProperty("sunset")
private Integer sunset;
@JsonProperty("temp")
private Double temp;
@JsonProperty("feels_like")
private Double feelsLike;
@JsonProperty("pressure")
private Integer pressure;
@JsonProperty("humidity")
private Integer humidity;
@JsonProperty("dew_point")
private Double dewPoint;
@JsonProperty("uvi")
private Double uvi;
@JsonProperty("clouds")
private Integer clouds;
@JsonProperty("visibility")
private Integer visibility;
@JsonProperty("wind_speed")
private Double windSpeed;
@JsonProperty("wind_deg")
private Integer windDeg;
@JsonProperty("wind_gust")
private Double windGust;
@JsonProperty("weather")
@Valid
private List<Weather> weather = null;

@JsonProperty("dt")
public Integer getDt() {
return dt;
}

@JsonProperty("dt")
public void setDt(Integer dt) {
this.dt = dt;
}

@JsonProperty("sunrise")
public Integer getSunrise() {
return sunrise;
}

@JsonProperty("sunrise")
public void setSunrise(Integer sunrise) {
this.sunrise = sunrise;
}

@JsonProperty("sunset")
public Integer getSunset() {
return sunset;
}

@JsonProperty("sunset")
public void setSunset(Integer sunset) {
this.sunset = sunset;
}

@JsonProperty("temp")
public Double getTemp() {
return temp;
}

@JsonProperty("temp")
public void setTemp(Double temp) {
this.temp = temp;
}

@JsonProperty("feels_like")
public Double getFeelsLike() {
return feelsLike;
}

@JsonProperty("feels_like")
public void setFeelsLike(Double feelsLike) {
this.feelsLike = feelsLike;
}

@JsonProperty("pressure")
public Integer getPressure() {
return pressure;
}

@JsonProperty("pressure")
public void setPressure(Integer pressure) {
this.pressure = pressure;
}

@JsonProperty("humidity")
public Integer getHumidity() {
return humidity;
}

@JsonProperty("humidity")
public void setHumidity(Integer humidity) {
this.humidity = humidity;
}

@JsonProperty("dew_point")
public Double getDewPoint() {
return dewPoint;
}

@JsonProperty("dew_point")
public void setDewPoint(Double dewPoint) {
this.dewPoint = dewPoint;
}

@JsonProperty("uvi")
public Double getUvi() {
return uvi;
}

@JsonProperty("uvi")
public void setUvi(Double uvi) {
this.uvi = uvi;
}

@JsonProperty("clouds")
public Integer getClouds() {
return clouds;
}

@JsonProperty("clouds")
public void setClouds(Integer clouds) {
this.clouds = clouds;
}

@JsonProperty("visibility")
public Integer getVisibility() {
return visibility;
}

@JsonProperty("visibility")
public void setVisibility(Integer visibility) {
this.visibility = visibility;
}

@JsonProperty("wind_speed")
public Double getWindSpeed() {
return windSpeed;
}

@JsonProperty("wind_speed")
public void setWindSpeed(Double windSpeed) {
this.windSpeed = windSpeed;
}

@JsonProperty("wind_deg")
public Integer getWindDeg() {
return windDeg;
}

@JsonProperty("wind_deg")
public void setWindDeg(Integer windDeg) {
this.windDeg = windDeg;
}

@JsonProperty("wind_gust")
public Double getWindGust() {
return windGust;
}

@JsonProperty("wind_gust")
public void setWindGust(Double windGust) {
this.windGust = windGust;
}

@JsonProperty("weather")
public List<Weather> getWeather() {
return weather;
}

@JsonProperty("weather")
public void setWeather(List<Weather> weather) {
this.weather = weather;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Current.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("dt");
sb.append('=');
sb.append(((this.dt == null)?"<null>":this.dt));
sb.append(',');
sb.append("sunrise");
sb.append('=');
sb.append(((this.sunrise == null)?"<null>":this.sunrise));
sb.append(',');
sb.append("sunset");
sb.append('=');
sb.append(((this.sunset == null)?"<null>":this.sunset));
sb.append(',');
sb.append("temp");
sb.append('=');
sb.append(((this.temp == null)?"<null>":this.temp));
sb.append(',');
sb.append("feelsLike");
sb.append('=');
sb.append(((this.feelsLike == null)?"<null>":this.feelsLike));
sb.append(',');
sb.append("pressure");
sb.append('=');
sb.append(((this.pressure == null)?"<null>":this.pressure));
sb.append(',');
sb.append("humidity");
sb.append('=');
sb.append(((this.humidity == null)?"<null>":this.humidity));
sb.append(',');
sb.append("dewPoint");
sb.append('=');
sb.append(((this.dewPoint == null)?"<null>":this.dewPoint));
sb.append(',');
sb.append("uvi");
sb.append('=');
sb.append(((this.uvi == null)?"<null>":this.uvi));
sb.append(',');
sb.append("clouds");
sb.append('=');
sb.append(((this.clouds == null)?"<null>":this.clouds));
sb.append(',');
sb.append("visibility");
sb.append('=');
sb.append(((this.visibility == null)?"<null>":this.visibility));
sb.append(',');
sb.append("windSpeed");
sb.append('=');
sb.append(((this.windSpeed == null)?"<null>":this.windSpeed));
sb.append(',');
sb.append("windDeg");
sb.append('=');
sb.append(((this.windDeg == null)?"<null>":this.windDeg));
sb.append(',');
sb.append("windGust");
sb.append('=');
sb.append(((this.windGust == null)?"<null>":this.windGust));
sb.append(',');
sb.append("weather");
sb.append('=');
sb.append(((this.weather == null)?"<null>":this.weather));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Daily.java-----------------------------------


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"dt",
"sunrise",
"sunset",
"moonrise",
"moonset",
"moon_phase",
"temp",
"feels_like",
"pressure",
"humidity",
"dew_point",
"wind_speed",
"wind_deg",
"wind_gust",
"weather",
"clouds",
"pop",
"rain",
"uvi"
})
@Generated("jsonschema2pojo")
public static class Daily {

@JsonProperty("dt")
private Integer dt;
@JsonProperty("sunrise")
private Integer sunrise;
@JsonProperty("sunset")
private Integer sunset;
@JsonProperty("moonrise")
private Integer moonrise;
@JsonProperty("moonset")
private Integer moonset;
@JsonProperty("moon_phase")
private Double moonPhase;
@JsonProperty("temp")
@Valid
private Temp temp;
@JsonProperty("feels_like")
@Valid
private FeelsLike feelsLike;
@JsonProperty("pressure")
private Integer pressure;
@JsonProperty("humidity")
private Integer humidity;
@JsonProperty("dew_point")
private Double dewPoint;
@JsonProperty("wind_speed")
private Double windSpeed;
@JsonProperty("wind_deg")
private Integer windDeg;
@JsonProperty("wind_gust")
private Double windGust;
@JsonProperty("weather")
@Valid
private List<Weather__2> weather = null;
@JsonProperty("clouds")
private Integer clouds;
@JsonProperty("pop")
private Integer pop;
@JsonProperty("rain")
private Double rain;
@JsonProperty("uvi")
private Integer uvi;

@JsonProperty("dt")
public Integer getDt() {
return dt;
}

@JsonProperty("dt")
public void setDt(Integer dt) {
this.dt = dt;
}

@JsonProperty("sunrise")
public Integer getSunrise() {
return sunrise;
}

@JsonProperty("sunrise")
public void setSunrise(Integer sunrise) {
this.sunrise = sunrise;
}

@JsonProperty("sunset")
public Integer getSunset() {
return sunset;
}

@JsonProperty("sunset")
public void setSunset(Integer sunset) {
this.sunset = sunset;
}

@JsonProperty("moonrise")
public Integer getMoonrise() {
return moonrise;
}

@JsonProperty("moonrise")
public void setMoonrise(Integer moonrise) {
this.moonrise = moonrise;
}

@JsonProperty("moonset")
public Integer getMoonset() {
return moonset;
}

@JsonProperty("moonset")
public void setMoonset(Integer moonset) {
this.moonset = moonset;
}

@JsonProperty("moon_phase")
public Double getMoonPhase() {
return moonPhase;
}

@JsonProperty("moon_phase")
public void setMoonPhase(Double moonPhase) {
this.moonPhase = moonPhase;
}

@JsonProperty("temp")
public Temp getTemp() {
return temp;
}

@JsonProperty("temp")
public void setTemp(Temp temp) {
this.temp = temp;
}

@JsonProperty("feels_like")
public FeelsLike getFeelsLike() {
return feelsLike;
}

@JsonProperty("feels_like")
public void setFeelsLike(FeelsLike feelsLike) {
this.feelsLike = feelsLike;
}

@JsonProperty("pressure")
public Integer getPressure() {
return pressure;
}

@JsonProperty("pressure")
public void setPressure(Integer pressure) {
this.pressure = pressure;
}

@JsonProperty("humidity")
public Integer getHumidity() {
return humidity;
}

@JsonProperty("humidity")
public void setHumidity(Integer humidity) {
this.humidity = humidity;
}

@JsonProperty("dew_point")
public Double getDewPoint() {
return dewPoint;
}

@JsonProperty("dew_point")
public void setDewPoint(Double dewPoint) {
this.dewPoint = dewPoint;
}

@JsonProperty("wind_speed")
public Double getWindSpeed() {
return windSpeed;
}

@JsonProperty("wind_speed")
public void setWindSpeed(Double windSpeed) {
this.windSpeed = windSpeed;
}

@JsonProperty("wind_deg")
public Integer getWindDeg() {
return windDeg;
}

@JsonProperty("wind_deg")
public void setWindDeg(Integer windDeg) {
this.windDeg = windDeg;
}

@JsonProperty("wind_gust")
public Double getWindGust() {
return windGust;
}

@JsonProperty("wind_gust")
public void setWindGust(Double windGust) {
this.windGust = windGust;
}

@JsonProperty("weather")
public List<Weather__2> getWeather() {
return weather;
}

@JsonProperty("weather")
public void setWeather(List<Weather__2> weather) {
this.weather = weather;
}

@JsonProperty("clouds")
public Integer getClouds() {
return clouds;
}

@JsonProperty("clouds")
public void setClouds(Integer clouds) {
this.clouds = clouds;
}

@JsonProperty("pop")
public Integer getPop() {
return pop;
}

@JsonProperty("pop")
public void setPop(Integer pop) {
this.pop = pop;
}

@JsonProperty("rain")
public Double getRain() {
return rain;
}

@JsonProperty("rain")
public void setRain(Double rain) {
this.rain = rain;
}

@JsonProperty("uvi")
public Integer getUvi() {
return uvi;
}

@JsonProperty("uvi")
public void setUvi(Integer uvi) {
this.uvi = uvi;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Daily.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("dt");
sb.append('=');
sb.append(((this.dt == null)?"<null>":this.dt));
sb.append(',');
sb.append("sunrise");
sb.append('=');
sb.append(((this.sunrise == null)?"<null>":this.sunrise));
sb.append(',');
sb.append("sunset");
sb.append('=');
sb.append(((this.sunset == null)?"<null>":this.sunset));
sb.append(',');
sb.append("moonrise");
sb.append('=');
sb.append(((this.moonrise == null)?"<null>":this.moonrise));
sb.append(',');
sb.append("moonset");
sb.append('=');
sb.append(((this.moonset == null)?"<null>":this.moonset));
sb.append(',');
sb.append("moonPhase");
sb.append('=');
sb.append(((this.moonPhase == null)?"<null>":this.moonPhase));
sb.append(',');
sb.append("temp");
sb.append('=');
sb.append(((this.temp == null)?"<null>":this.temp));
sb.append(',');
sb.append("feelsLike");
sb.append('=');
sb.append(((this.feelsLike == null)?"<null>":this.feelsLike));
sb.append(',');
sb.append("pressure");
sb.append('=');
sb.append(((this.pressure == null)?"<null>":this.pressure));
sb.append(',');
sb.append("humidity");
sb.append('=');
sb.append(((this.humidity == null)?"<null>":this.humidity));
sb.append(',');
sb.append("dewPoint");
sb.append('=');
sb.append(((this.dewPoint == null)?"<null>":this.dewPoint));
sb.append(',');
sb.append("windSpeed");
sb.append('=');
sb.append(((this.windSpeed == null)?"<null>":this.windSpeed));
sb.append(',');
sb.append("windDeg");
sb.append('=');
sb.append(((this.windDeg == null)?"<null>":this.windDeg));
sb.append(',');
sb.append("windGust");
sb.append('=');
sb.append(((this.windGust == null)?"<null>":this.windGust));
sb.append(',');
sb.append("weather");
sb.append('=');
sb.append(((this.weather == null)?"<null>":this.weather));
sb.append(',');
sb.append("clouds");
sb.append('=');
sb.append(((this.clouds == null)?"<null>":this.clouds));
sb.append(',');
sb.append("pop");
sb.append('=');
sb.append(((this.pop == null)?"<null>":this.pop));
sb.append(',');
sb.append("rain");
sb.append('=');
sb.append(((this.rain == null)?"<null>":this.rain));
sb.append(',');
sb.append("uvi");
sb.append('=');
sb.append(((this.uvi == null)?"<null>":this.uvi));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.FeelsLike.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"day",
"night",
"eve",
"morn"
})
@Generated("jsonschema2pojo")
public static class FeelsLike {

@JsonProperty("day")
private Double day;
@JsonProperty("night")
private Double night;
@JsonProperty("eve")
private Double eve;
@JsonProperty("morn")
private Double morn;

@JsonProperty("day")
public Double getDay() {
return day;
}

@JsonProperty("day")
public void setDay(Double day) {
this.day = day;
}

@JsonProperty("night")
public Double getNight() {
return night;
}

@JsonProperty("night")
public void setNight(Double night) {
this.night = night;
}

@JsonProperty("eve")
public Double getEve() {
return eve;
}

@JsonProperty("eve")
public void setEve(Double eve) {
this.eve = eve;
}

@JsonProperty("morn")
public Double getMorn() {
return morn;
}

@JsonProperty("morn")
public void setMorn(Double morn) {
this.morn = morn;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(FeelsLike.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("day");
sb.append('=');
sb.append(((this.day == null)?"<null>":this.day));
sb.append(',');
sb.append("night");
sb.append('=');
sb.append(((this.night == null)?"<null>":this.night));
sb.append(',');
sb.append("eve");
sb.append('=');
sb.append(((this.eve == null)?"<null>":this.eve));
sb.append(',');
sb.append("morn");
sb.append('=');
sb.append(((this.morn == null)?"<null>":this.morn));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Hourly.java-----------------------------------


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"dt",
"temp",
"feels_like",
"pressure",
"humidity",
"dew_point",
"uvi",
"clouds",
"visibility",
"wind_speed",
"wind_deg",
"wind_gust",
"weather",
"pop",
"rain"
})
@Generated("jsonschema2pojo")
public static class Hourly {

@JsonProperty("dt")
private Integer dt;
@JsonProperty("temp")
private Double temp;
@JsonProperty("feels_like")
private Double feelsLike;
@JsonProperty("pressure")
private Integer pressure;
@JsonProperty("humidity")
private Integer humidity;
@JsonProperty("dew_point")
private Double dewPoint;
@JsonProperty("uvi")
private Integer uvi;
@JsonProperty("clouds")
private Integer clouds;
@JsonProperty("visibility")
private Integer visibility;
@JsonProperty("wind_speed")
private Double windSpeed;
@JsonProperty("wind_deg")
private Integer windDeg;
@JsonProperty("wind_gust")
private Double windGust;
@JsonProperty("weather")
@Valid
private List<Weather__1> weather = null;
@JsonProperty("pop")
private Integer pop;
@JsonProperty("rain")
@Valid
private Rain rain;

@JsonProperty("dt")
public Integer getDt() {
return dt;
}

@JsonProperty("dt")
public void setDt(Integer dt) {
this.dt = dt;
}

@JsonProperty("temp")
public Double getTemp() {
return temp;
}

@JsonProperty("temp")
public void setTemp(Double temp) {
this.temp = temp;
}

@JsonProperty("feels_like")
public Double getFeelsLike() {
return feelsLike;
}

@JsonProperty("feels_like")
public void setFeelsLike(Double feelsLike) {
this.feelsLike = feelsLike;
}

@JsonProperty("pressure")
public Integer getPressure() {
return pressure;
}

@JsonProperty("pressure")
public void setPressure(Integer pressure) {
this.pressure = pressure;
}

@JsonProperty("humidity")
public Integer getHumidity() {
return humidity;
}

@JsonProperty("humidity")
public void setHumidity(Integer humidity) {
this.humidity = humidity;
}

@JsonProperty("dew_point")
public Double getDewPoint() {
return dewPoint;
}

@JsonProperty("dew_point")
public void setDewPoint(Double dewPoint) {
this.dewPoint = dewPoint;
}

@JsonProperty("uvi")
public Integer getUvi() {
return uvi;
}

@JsonProperty("uvi")
public void setUvi(Integer uvi) {
this.uvi = uvi;
}

@JsonProperty("clouds")
public Integer getClouds() {
return clouds;
}

@JsonProperty("clouds")
public void setClouds(Integer clouds) {
this.clouds = clouds;
}

@JsonProperty("visibility")
public Integer getVisibility() {
return visibility;
}

@JsonProperty("visibility")
public void setVisibility(Integer visibility) {
this.visibility = visibility;
}

@JsonProperty("wind_speed")
public Double getWindSpeed() {
return windSpeed;
}

@JsonProperty("wind_speed")
public void setWindSpeed(Double windSpeed) {
this.windSpeed = windSpeed;
}

@JsonProperty("wind_deg")
public Integer getWindDeg() {
return windDeg;
}

@JsonProperty("wind_deg")
public void setWindDeg(Integer windDeg) {
this.windDeg = windDeg;
}

@JsonProperty("wind_gust")
public Double getWindGust() {
return windGust;
}

@JsonProperty("wind_gust")
public void setWindGust(Double windGust) {
this.windGust = windGust;
}

@JsonProperty("weather")
public List<Weather__1> getWeather() {
return weather;
}

@JsonProperty("weather")
public void setWeather(List<Weather__1> weather) {
this.weather = weather;
}

@JsonProperty("pop")
public Integer getPop() {
return pop;
}

@JsonProperty("pop")
public void setPop(Integer pop) {
this.pop = pop;
}

@JsonProperty("rain")
public Rain getRain() {
return rain;
}

@JsonProperty("rain")
public void setRain(Rain rain) {
this.rain = rain;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Hourly.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("dt");
sb.append('=');
sb.append(((this.dt == null)?"<null>":this.dt));
sb.append(',');
sb.append("temp");
sb.append('=');
sb.append(((this.temp == null)?"<null>":this.temp));
sb.append(',');
sb.append("feelsLike");
sb.append('=');
sb.append(((this.feelsLike == null)?"<null>":this.feelsLike));
sb.append(',');
sb.append("pressure");
sb.append('=');
sb.append(((this.pressure == null)?"<null>":this.pressure));
sb.append(',');
sb.append("humidity");
sb.append('=');
sb.append(((this.humidity == null)?"<null>":this.humidity));
sb.append(',');
sb.append("dewPoint");
sb.append('=');
sb.append(((this.dewPoint == null)?"<null>":this.dewPoint));
sb.append(',');
sb.append("uvi");
sb.append('=');
sb.append(((this.uvi == null)?"<null>":this.uvi));
sb.append(',');
sb.append("clouds");
sb.append('=');
sb.append(((this.clouds == null)?"<null>":this.clouds));
sb.append(',');
sb.append("visibility");
sb.append('=');
sb.append(((this.visibility == null)?"<null>":this.visibility));
sb.append(',');
sb.append("windSpeed");
sb.append('=');
sb.append(((this.windSpeed == null)?"<null>":this.windSpeed));
sb.append(',');
sb.append("windDeg");
sb.append('=');
sb.append(((this.windDeg == null)?"<null>":this.windDeg));
sb.append(',');
sb.append("windGust");
sb.append('=');
sb.append(((this.windGust == null)?"<null>":this.windGust));
sb.append(',');
sb.append("weather");
sb.append('=');
sb.append(((this.weather == null)?"<null>":this.weather));
sb.append(',');
sb.append("pop");
sb.append('=');
sb.append(((this.pop == null)?"<null>":this.pop));
sb.append(',');
sb.append("rain");
sb.append('=');
sb.append(((this.rain == null)?"<null>":this.rain));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Minutely.java-----------------------------------


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"dt",
"precipitation"
})
@Generated("jsonschema2pojo")
public static class Minutely {

@JsonProperty("dt")
private Integer dt;
@JsonProperty("precipitation")
private Integer precipitation;

@JsonProperty("dt")
public Integer getDt() {
return dt;
}

@JsonProperty("dt")
public void setDt(Integer dt) {
this.dt = dt;
}

@JsonProperty("precipitation")
public Integer getPrecipitation() {
return precipitation;
}

@JsonProperty("precipitation")
public void setPrecipitation(Integer precipitation) {
this.precipitation = precipitation;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Minutely.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("dt");
sb.append('=');
sb.append(((this.dt == null)?"<null>":this.dt));
sb.append(',');
sb.append("precipitation");
sb.append('=');
sb.append(((this.precipitation == null)?"<null>":this.precipitation));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.OpenWeatherOnecall.java-----------------------------------


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"lat",
"lon",
"timezone",
"timezone_offset",
"current",
"minutely",
"hourly",
"daily"
})

//-----------------------------------entite.Rain.java-----------------------------------


//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder({
//"1h"
//})
@Generated("jsonschema2pojo")
public static class Rain {

@JsonProperty("1h")
private Double _1h;

@JsonProperty("1h")
public Double get1h() {
return _1h;
}

@JsonProperty("1h")
public void set1h(Double _1h) {
this._1h = _1h;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Rain.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("_1h");
sb.append('=');
sb.append(((this._1h == null)?"<null>":this._1h));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Temp.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"day",
"min",
"max",
"night",
"eve",
"morn"
})
@Generated("jsonschema2pojo")
public static class Temp {

@JsonProperty("day")
private Double day;
@JsonProperty("min")
private Double min;
@JsonProperty("max")
private Double max;
@JsonProperty("night")
private Double night;
@JsonProperty("eve")
private Double eve;
@JsonProperty("morn")
private Double morn;

@JsonProperty("day")
public Double getDay() {
return day;
}

@JsonProperty("day")
public void setDay(Double day) {
this.day = day;
}

@JsonProperty("min")
public Double getMin() {
return min;
}

@JsonProperty("min")
public void setMin(Double min) {
this.min = min;
}

@JsonProperty("max")
public Double getMax() {
return max;
}

@JsonProperty("max")
public void setMax(Double max) {
this.max = max;
}

@JsonProperty("night")
public Double getNight() {
return night;
}

@JsonProperty("night")
public void setNight(Double night) {
this.night = night;
}

@JsonProperty("eve")
public Double getEve() {
return eve;
}

@JsonProperty("eve")
public void setEve(Double eve) {
this.eve = eve;
}

@JsonProperty("morn")
public Double getMorn() {
return morn;
}

@JsonProperty("morn")
public void setMorn(Double morn) {
this.morn = morn;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Temp.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("day");
sb.append('=');
sb.append(((this.day == null)?"<null>":this.day));
sb.append(',');
sb.append("min");
sb.append('=');
sb.append(((this.min == null)?"<null>":this.min));
sb.append(',');
sb.append("max");
sb.append('=');
sb.append(((this.max == null)?"<null>":this.max));
sb.append(',');
sb.append("night");
sb.append('=');
sb.append(((this.night == null)?"<null>":this.night));
sb.append(',');
sb.append("eve");
sb.append('=');
sb.append(((this.eve == null)?"<null>":this.eve));
sb.append(',');
sb.append("morn");
sb.append('=');
sb.append(((this.morn == null)?"<null>":this.morn));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Weather.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"main",
"description",
"icon"
})
@Generated("jsonschema2pojo")
public static class Weather {

@JsonProperty("id")
private Integer id;
@JsonProperty("main")
private String main;
@JsonProperty("description")
private String description;
@JsonProperty("icon")
private String icon;

@JsonProperty("id")
public Integer getId() {
return id;
}

@JsonProperty("id")
public void setId(Integer id) {
this.id = id;
}

@JsonProperty("main")
public String getMain() {
return main;
}

@JsonProperty("main")
public void setMain(String main) {
this.main = main;
}

@JsonProperty("description")
public String getDescription() {
return description;
}

@JsonProperty("description")
public void setDescription(String description) {
this.description = description;
}

@JsonProperty("icon")
public String getIcon() {
return icon;
}

@JsonProperty("icon")
public void setIcon(String icon) {
this.icon = icon;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Weather.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("id");
sb.append('=');
sb.append(((this.id == null)?"<null>":this.id));
sb.append(',');
sb.append("main");
sb.append('=');
sb.append(((this.main == null)?"<null>":this.main));
sb.append(',');
sb.append("description");
sb.append('=');
sb.append(((this.description == null)?"<null>":this.description));
sb.append(',');
sb.append("icon");
sb.append('=');
sb.append(((this.icon == null)?"<null>":this.icon));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Weather__1.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"main",
"description",
"icon"
})
@Generated("jsonschema2pojo")
public static class Weather__1 {

@JsonProperty("id")
private Integer id;
@JsonProperty("main")
private String main;
@JsonProperty("description")
private String description;
@JsonProperty("icon")
private String icon;

@JsonProperty("id")
public Integer getId() {
return id;
}

@JsonProperty("id")
public void setId(Integer id) {
this.id = id;
}

@JsonProperty("main")
public String getMain() {
return main;
}

@JsonProperty("main")
public void setMain(String main) {
this.main = main;
}

@JsonProperty("description")
public String getDescription() {
return description;
}

@JsonProperty("description")
public void setDescription(String description) {
this.description = description;
}

@JsonProperty("icon")
public String getIcon() {
return icon;
}

@JsonProperty("icon")
public void setIcon(String icon) {
this.icon = icon;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Weather__1 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("id");
sb.append('=');
sb.append(((this.id == null)?"<null>":this.id));
sb.append(',');
sb.append("main");
sb.append('=');
sb.append(((this.main == null)?"<null>":this.main));
sb.append(',');
sb.append("description");
sb.append('=');
sb.append(((this.description == null)?"<null>":this.description));
sb.append(',');
sb.append("icon");
sb.append('=');
sb.append(((this.icon == null)?"<null>":this.icon));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
//-----------------------------------entite.Weather__2.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"main",
"description",
"icon"
})
@Generated("jsonschema2pojo")
public static class Weather__2 {

@JsonProperty("id")
private Integer id;
@JsonProperty("main")
private String main;
@JsonProperty("description")
private String description;
@JsonProperty("icon")
private String icon;

@JsonProperty("id")
public Integer getId() {
return id;
}

@JsonProperty("id")
public void setId(Integer id) {
this.id = id;
}

@JsonProperty("main")
public String getMain() {
return main;
}

@JsonProperty("main")
public void setMain(String main) {
this.main = main;
}

@JsonProperty("description")
public String getDescription() {
return description;
}

@JsonProperty("description")
public void setDescription(String description) {
this.description = description;
}

@JsonProperty("icon")
public String getIcon() {
return icon;
}

@JsonProperty("icon")
public void setIcon(String icon) {
this.icon = icon;
}

@Override
public String toString() {
StringBuilder sb = new StringBuilder();
sb.append(Weather__2 .class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
sb.append("id");
sb.append('=');
sb.append(((this.id == null)?"<null>":this.id));
sb.append(',');
sb.append("main");
sb.append('=');
sb.append(((this.main == null)?"<null>":this.main));
sb.append(',');
sb.append("description");
sb.append('=');
sb.append(((this.description == null)?"<null>":this.description));
sb.append(',');
sb.append("icon");
sb.append('=');
sb.append(((this.icon == null)?"<null>":this.icon));
sb.append(',');
if (sb.charAt((sb.length()- 1)) == ',') {
sb.setCharAt((sb.length()- 1), ']');
} else {
sb.append(']');
}
return sb.toString();
}

}
} // end class OpenWeatherOnecall