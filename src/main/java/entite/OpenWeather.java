package entite;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Generated;
import jakarta.inject.Named;

// generated !!https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started#using-jsonschema2pojo-within-your-java-project-embedded
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"coord",
"weather",
"base",
"main",
"visibility",
"wind",
"clouds",
"dt",
"sys",
"timezone",
"id",
"name",
"cod"
})
@Generated("jsonschema2pojo")
public class OpenWeather {

@JsonProperty("coord")
private Coord coord;
@JsonProperty("weather")
private List<Weather> weather = null;
@JsonProperty("base")
private String base;
@JsonProperty("main")
private Main main;
@JsonProperty("visibility")
private Integer visibility;
@JsonProperty("wind")
private Wind wind;
@JsonProperty("clouds")
private Clouds clouds;
@JsonProperty("dt")
private Integer dt;
@JsonProperty("sys")
private Sys sys;
@JsonProperty("timezone")
private Integer timezone;
@JsonProperty("id")
private Integer id;
@JsonProperty("name")
private String name;
@JsonProperty("cod")
private Integer cod;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("coord")
public Coord getCoord() {
return coord;
}

@JsonProperty("coord")
public void setCoord(Coord coord) {
this.coord = coord;
}

@JsonProperty("weather")
public List<Weather> getWeather() {
return weather;
}

@JsonProperty("weather")
public void setWeather(List<Weather> weather) {
this.weather = weather;
}

@JsonProperty("base")
public String getBase() {
return base;
}

@JsonProperty("base")
public void setBase(String base) {
this.base = base;
}

@JsonProperty("main")
public Main getMain() {
return main;
}

@JsonProperty("main")
public void setMain(Main main) {
this.main = main;
}

@JsonProperty("visibility")
public Integer getVisibility() {
return visibility;
}

@JsonProperty("visibility")
public void setVisibility(Integer visibility) {
this.visibility = visibility;
}

@JsonProperty("wind")
public Wind getWind() {
return wind;
}

@JsonProperty("wind")
public void setWind(Wind wind) {
this.wind = wind;
}

@JsonProperty("clouds")
public Clouds getClouds() {
return clouds;
}

@JsonProperty("clouds")
public void setClouds(Clouds clouds) {
this.clouds = clouds;
}

@JsonProperty("dt")
public Integer getDt() {
return dt;
}

@JsonProperty("dt")
public void setDt(Integer dt) {
this.dt = dt;
}

@JsonProperty("sys")
public Sys getSys() {
return sys;
}

@JsonProperty("sys")
public void setSys(Sys sys) {
this.sys = sys;
}

@JsonProperty("timezone")
public Integer getTimezone() {
return timezone;
}

@JsonProperty("timezone")
public void setTimezone(Integer timezone) {
this.timezone = timezone;
}

@JsonProperty("id")
public Integer getId() {
return id;
}

@JsonProperty("id")
public void setId(Integer id) {
this.id = id;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("cod")
public Integer getCod() {
return cod;
}

@JsonProperty("cod")
public void setCod(Integer cod) {
this.cod = cod;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}


// ----------------------------------

@Named

//-----------------------------------com.example.Clouds.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"all"
})
@Generated("jsonschema2pojo")
public static class Clouds {

@JsonProperty("all")
private Integer all;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("all")
public Integer getAll() {
return all;
}

@JsonProperty("all")
public void setAll(Integer all) {
this.all = all;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
// -----------------------------------com.example.Coord.java-----------------------------------


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"lon",
"lat"
})
@Generated("jsonschema2pojo")
 public static class Coord {

@JsonProperty("lon")
private Double lon;
@JsonProperty("lat")
private Double lat;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("lon")
public Double getLon() {
return lon;
}

@JsonProperty("lon")
public void setLon(Double lon) {
this.lon = lon;
}

@JsonProperty("lat")
public Double getLat() {
return lat;
}

@JsonProperty("lat")
public void setLat(Double lat) {
this.lat = lat;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
//-----------------------------------com.example.Example.java-----------------------------------
//-com.example.Main.java-----------------------------------
//

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"temp",
"feels_like",
"temp_min",
"temp_max",
"pressure",
"humidity"
})
@Generated("jsonschema2pojo")
 public static class Main {

@JsonProperty("temp")
private Double temp;
@JsonProperty("feels_like")
private Double feelsLike;
@JsonProperty("temp_min")
private Double tempMin;
@JsonProperty("temp_max")
private Double tempMax;
@JsonProperty("pressure")
private Integer pressure;
@JsonProperty("humidity")
private Integer humidity;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

@JsonProperty("temp_min")
public Double getTempMin() {
return tempMin;
}

@JsonProperty("temp_min")
public void setTempMin(Double tempMin) {
this.tempMin = tempMin;
}

@JsonProperty("temp_max")
public Double getTempMax() {
return tempMax;
}

@JsonProperty("temp_max")
public void setTempMax(Double tempMax) {
this.tempMax = tempMax;
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

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
//-----------------------------------com.example.Sys.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"type",
"id",
"message",
"country",
"sunrise",
"sunset"
})
@Generated("jsonschema2pojo")
 public static class Sys {

@JsonProperty("type")
private Integer type;
@JsonProperty("id")
private Integer id;
@JsonProperty("message")
private Double message;
@JsonProperty("country")
private String country;
@JsonProperty("sunrise")
private Integer sunrise;
@JsonProperty("sunset")
private Integer sunset;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("type")
public Integer getType() {
return type;
}

@JsonProperty("type")
public void setType(Integer type) {
this.type = type;
}

@JsonProperty("id")
public Integer getId() {
return id;
}

@JsonProperty("id")
public void setId(Integer id) {
this.id = id;
}

@JsonProperty("message")
public Double getMessage() {
return message;
}

@JsonProperty("message")
public void setMessage(Double message) {
this.message = message;
}

@JsonProperty("country")
public String getCountry() {
return country;
}

@JsonProperty("country")
public void setCountry(String country) {
this.country = country;
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

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
//-----------------------------------com.example.Weather.java-----------------------------------



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
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
//-----------------------------------com.example.Wind.java-----------------------------------

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"speed",
"deg"
})
@Generated("jsonschema2pojo")
 public static class Wind {

@JsonProperty("speed")
private Double speed;
@JsonProperty("deg")
private Integer deg;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("speed")
public Double getSpeed() {
return speed;
}

@JsonProperty("speed")
public void setSpeed(Double speed) {
this.speed = speed;
}

@JsonProperty("deg")
public Integer getDeg() {
return deg;
}

@JsonProperty("deg")
public void setDeg(Integer deg) {
this.deg = deg;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}
} //end Wind

} // end class Openweather