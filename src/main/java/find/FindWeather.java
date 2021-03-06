package find;
//import static interfaces.GolfInterface.OWM_KEY_LC;
import entite.Club;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.annotation.SessionMap;
import javax.inject.Inject;
import javax.inject.Named;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.core.OWM.Language;
import net.aksingh.owmjapis.core.OWM.Unit;
import net.aksingh.owmjapis.core.OWMPro;
import net.aksingh.owmjapis.model.CurrentUVIndex;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyWeatherForecast;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import static utils.LCUtil.showMessageFatal;
// 23/07/2019 version 2.5.3.0 changed 
@Named("findWeather")
@SessionScoped
public class FindWeather implements Serializable, interfaces.GolfInterface, interfaces.Log{
    private static CurrentWeather CWD = null;
    private static HourlyWeatherForecast HWF = null;
    private static CurrentUVIndex CUVI = null;
    private static DailyWeatherForecast dwf;// = null;
    final private static String WIND_DIRECTION[] = {
          "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
      "S", "SSW", "SW", "WSW", "W", "West-Northwest", "NW", "NNW"};
    private static OWMPro owm = null; // mod OWM to OWMPro 23/07/2019
    private List<String> myList = new ArrayList<>(9);
    private String pattern = "EEE MMM dd HH:mm:ss z uuuu"; // uuuu = yyyy i strict mode
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US)
            .withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
    private Map<Long,Player> MESSAGES;
    //https://www.tutorialspoint.com/microservice_architecture/microservice_architecture_hands_on_soa.htm
@Inject
@SessionMap
private Map<String, Object> sessionMapJSF23;

    public FindWeather() {
        this.MESSAGES = new HashMap<>();
    }
    public String currentWeatherByCityName(Club club) throws Exception {
    // city et country = concerne le parcours

    try{
  //  see   https://openweathermap.org/

 //   String owmApiKey = "e33f7134856ab9fbbe79a544cc23bc39"; /* YOUR OWM API KEY HERE */ //key LC
 //65b6810c7fb377fb322b6a7486bfb87a
 //String owmApiKey = "ddbb487d7785b2dc6f9069b418732ee6";
 
      String owmApiKey ="9229fc57d217e84684dfb717867b67f5"; // found internet
       // key found https://github.com/tiabaldu/weather/blob/master/WeatherStore/src/main/java/com/tia/weatherStorePack/WeatherRetreiver.java
        // declaring object of "OWM" class
        // https://www.programcreek.com/java-api-examples/?class=net.aksingh.owmjapis.OpenWeatherMap&method=currentWeatherByCityName
        owm = new OWMPro(owmApiKey);
    //    owm = new OWM(owmApiKey);
    LOG.info("starting currentWeatherByCityName");
    LOG.info("starting currentWeatherByCityName with club = " + club);

    if (null == owm) {
//	owm = new OWMPro(OWM_KEY_LC); // changed 23/07/2019
    //    owm = new OWMPro("1dcd0e1749fbf3af2f6ce66495886d14"); // 9229fc57d217e84684dfb717867b67f5
      owm = new OWMPro("65b6810c7fb377fb322b6a7486bfb87a"); // 9229fc57d217e84684dfb717867b67f5
        LOG.info("owm was null, new created");
    }

         LOG.info("owm was not null ==> key OK");
        // getting current weather data 
        owm.setLanguage(Language.ENGLISH);
        owm.setUnit(Unit.METRIC);
        CWD = owm.currentWeatherByCityName(club.getClubCity(), OWM.Country.BELGIUM);
        
        
  //      ho
//  LOG.info("line 01");

StringBuilder sb = new StringBuilder ();

    LOG.info("Sunrise: " + SDF_TIME.format(CWD.getSystemData().getSunriseDateTime()));
//    sj.add("Sunrise: " + SDF_TIME.format(CWD.getSystemData().getSunriseDateTime()));
    LOG.info("Sunset: " + SDF_TIME.format(CWD.getSystemData().getSunsetDateTime()));
    LOG.info("component 4: " + CWD.getSystemData().component4());
    LOG.info("component 7: " + CWD.getSystemData().component7());
    LOG.info("weather list " + CWD.getWeatherList());
    LOG.info("wind force km/h: " + CWD.getWindData().getSpeed() );
    LOG.info("wind gust/rafales : " + CWD.getWindData().getGust() );
    LOG.info("Humidity : " + CWD.getMainData().getHumidity());
    LOG.info("Pressure : " + CWD.getMainData().getPressure());
   //  LOG.info("Pressure : " + CWD.getMainData().getPressure());
// https://stackoverflow.com/questions/2131195/cardinal-direction-algorithm-in-java
//  http://snowfence.umn.edu/Components/winddirectionanddegreeswithouttable3.htm

if (CWD.getWindData().getDegree() != null) {
    LOG.info("wind degree: " + CWD.getWindData().getDegree() );
     String wd = WIND_DIRECTION[(int)Math.floor((CWD.getWindData().getDegree() % 360) / 22.5)];
    LOG.info("wind direction : " + wd );
}else{
    LOG.info("wind direction : unknown" );
}

    LOG.info("cloud cover % : " + CWD.getCloudData().getCloud() );
    LOG.info("latitude: " + CWD.getCoordData().getLatitude() );
    LOG.info("longitude: " + CWD.getCoordData().getLongitude() );
    LOG.info("respcode: " + CWD.getRespCode());
    
    if (CWD.hasRainData()) {
         LOG.info("rain: " + CWD.getRainData().getPrecipVol3h());
     }
        // checking data retrieval was successful or not
  if (CWD.hasRespCode() && CWD.getRespCode() == 200) {
            // checking if city name is available
            if (CWD.hasCityName()) {
                //printing city name from the retrieved data
                LOG.info("City: " + CWD.getCityName());
                
                LOG.info("get timeforecast at date = " + SDF.format(CWD.getDateTime()));
                LOG.info("get maindata = " + CWD.getMainData());
            }
            // checking if max. temp. and min. temp. is available
            if (CWD.hasMainData() && CWD.getMainData().hasTempMax() && CWD.getMainData().hasTempMin()) {
                // printing the max./min. temperature
                LOG.info("Temperature MAX = " + CWD.getMainData().getTempMax());
                LOG.info("Temperature min = " + CWD.getMainData().getTempMin());
                LOG.info("Humidity = " + CWD.getMainData().getHumidity());
                LOG.info("temperature now  = " + CWD.getMainData().getTemp());
       //                     + "/" + CWD.getMainData().getTempMin() + "\'K");
            } // end if 2
       } // end if 1 

  HWF = owm.hourlyWeatherForecastByCityName(club.getClubCity(), OWM.Country.BELGIUM);
    LOG.info("response code = " + HWF.getRespCode()); // 200 = OK response code
    LOG.info("HWF component 1 : " + HWF.component1());
    LOG.info("HWF component 2 : " + HWF.component2());
//    LOG.info("component 7: " + HWF..getSystemData().component7());
    LOG.info("HWF component 3 : " + HWF.component3());
    LOG.info("HWF component 4 : " + HWF.component4());
    LOG.info("HWF component 5 : " + HWF.component5());
 LOG.info("HourlyWeatherForecast prettyPrint = " + HourlyWeatherForecast.toJsonPretty(HWF)); 
  
  
  owm = new OWMPro("9229fc57d217e84684dfb717867b67f5");
 // owm = new OWM(OWM_KEY_LC);

  owm.setLanguage(OWM.Language.DUTCH);  //FRENCH etc ...
  owm.setUnit(OWM.Unit.METRIC);
   dwf = owm.dailyWeatherForecastByCoords(36.739055, -5.165161); // forecastDays)currentWeatherByCoords(36.739055, -5.165161); //ronda
   LOG.info("City after coord search : " + CWD.getCityName());
   LOG.info("normal termination of Weather information ");

      return sb.toString();
      
//   } catch (APIException api) {
//    LOG.info("API OWM exception by LC =  = " + api);
//    return(null);

//return null;
   } catch (Exception e) {
     String msg = "OWM exception by LC =  = " + e;
     LOG.info(msg);
     showMessageFatal(msg);
     return(null);
    }
 } //end method 
    
   public String dailyWeatherByCityName(String city, String country, Integer forecastDays) throws Exception {
    try{
 //  OWM owm = new OWM("9229fc57d217e84684dfb717867b67f5");
    owm = new OWMPro("9229fc57d217e84684dfb717867b67f5");
//               owm = new OWM(owmApiKey);
dwf = owm.dailyWeatherForecastByCityName(city, OWM.Country.BELGIUM, forecastDays); 
// LOG.info("after daily brussels forecast ");

java.util.Date date1 = SDF.parse("16/04/2018 10:01");
//  LOG.info("looking for weather at date = " + date1);
//dwf.setDataList(list);//;.setDateTime(date1);  // ne fonctionne pas pour currentweather c'est npormal !!!
   //    forecast.getRespCode();
    LOG.info("Found Weather Forecast for City " + dwf.getCityData().getName());
//      int numForecasts = dwf.getDataCount(); //getForecastCount();
 //     LOG.info("numForecasts = " + numForecasts);
 StringBuilder sb = new StringBuilder ();
 
  LOG.info("DailyWeatherForecast prettyPrint = " + DailyWeatherForecast.toJsonPretty(dwf)); 
      for (int i = 0; i < dwf.getDataCount(); i++) {
        LOG.info(" \ni = " + i);
        sb.append("\n");
        sb.append(i+1);
        sb.append(TAB);
     
         LOG.info("response code component 1 = " + dwf.getRespCode()); // 200 = OK response code
         LOG.info("message 2 = " + dwf.component2()); // message 
         LOG.info("component 3 = " + dwf.component3()); //city
         LOG.info("component 4 = " + dwf.component4()); // nombre items 
  //       LOG.info("component 5 DateTime = " + dwf.component5().get(i).getDateTime());
         LOG.info("component 5 DateTime = " + dwf.component5().get(i).getDateTime());
         sb.append(SDF.format(dwf.component5().get(i).getDateTime()));
         LOG.info("component 5 Cloud    = " + dwf.component5().get(i).getCloud());
         LOG.info("component 5 Degree   = " + dwf.component5().get(i).getDegree());
         String wd = WIND_DIRECTION[(int)Math.floor((dwf.component5().get(i).getDegree() % 360) / 22.5)];
         LOG.info(" Wind Direction  = " + wd);
         sb.append(" Wind direction = ");
         sb.append(wd);
         LOG.info("component 5 Humidity = " + dwf.component5().get(i).getHumidity());
         LOG.info("component 5 Pressure = " + dwf.component5().get(i).getPressure());
         LOG.info("component 5 Rain     = " + dwf.component5().get(i).getRain());
         LOG.info("component 5 Snow     = " + dwf.component5().get(i).getSnow());
         LOG.info("component 5 Speed    = " + dwf.component5().get(i).getSpeed());
         LOG.info("component 5 Temperature day = " + dwf.component5().get(i).getTempData().getTempDay());
         LOG.info("component 5 Temperature min = " + dwf.component5().get(i).getTempData().getTempMin());
         LOG.info("component 5 Temperature max = " + dwf.component5().get(i).getTempData().getTempMax());
         LOG.info("component 5 Temperature night = " + dwf.component5().get(i).getTempData().getTempNight());
         LOG.info("component 5 Temperature eve = " + dwf.component5().get(i).getTempData().getTempEvening());
         LOG.info("component 5 Temperature morn = " + dwf.component5().get(i).getTempData().getTempMorning());
         
         LOG.info("component 5 WeatherList Main = " + dwf.component5().get(i).getWeatherList().get(0).getMainInfo());
         LOG.info("component 5 WeatherList More = " + dwf.component5().get(i).getWeatherList().get(0).getMoreInfo());
         sb.append (" \tMore info = ");
      //   sj.add("\t");
         sb.append(dwf.component5().get(i).getWeatherList().get(0).getMoreInfo());
         LOG.info("component 5 WeatherList Description  = " + dwf.component5().get(i).getWeatherList().get(0).getDescription());
         LOG.info("component 5 WeatherList Icon = " + dwf.component5().get(i).getWeatherList().get(0).getIconCode());
         LOG.info("component 5 WeatherList Link = " + dwf.component5().get(i).getWeatherList().get(0).getIconLink());
     } // end for
      LOG.info("returned info = " + sb );
      return sb.toString();
 } catch (Exception e) {
     String msg = "OWM exception by LC =  = " + e;
     LOG.info(msg);
     showMessageFatal(msg);
     return(null);
    }
    } // end method

    public List<String> getMyList() {
        return myList;
    }

    public void setMyList(List<String> myList) {
        this.myList = myList;
    }
   
   
    public static void main(String[] args) throws Exception {
        LOG.info("starting main");
  //      @PostConstruct 
  //      public void init(){
  //      LOG.info("");
  //  }
        FindWeather fw = new FindWeather();
        Club club = new Club();
        club.setClubCity("Brussels");
        String s = fw.currentWeatherByCityName(club);
        LOG.info("current  returned is :" + s);
    //    s = dailyWeatherByCityName("Brussels", "Be",15);
    //    LOG.info("daily returned is :" + s);
 } // end method main
    
} // end class