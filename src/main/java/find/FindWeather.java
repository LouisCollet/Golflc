package find;
//import static interfaces.GolfInterface.OWM_KEY_LC;
import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.unit.DegreeUnit;
import entite.Club;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import javax.enterprise.context.SessionScoped;
import javax.faces.annotation.SessionMap;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentUVIndex;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.DailyWeatherForecast;
import net.aksingh.owmjapis.model.HourlyWeatherForecast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import static utils.LCUtil.showMessageFatal;


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
    private static OWM owm = null;
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

 //   String owmApiKey = "65b6810c7fb377fb322b6a7486bfb87a"; /* YOUR OWM API KEY HERE */ //key LC
  //    String owmApiKey ="9229fc57d217e84684dfb717867b67f5"; // found internet
       // key found https://github.com/tiabaldu/weather/blob/master/WeatherStore/src/main/java/com/tia/weatherStorePack/WeatherRetreiver.java
        // declaring object of "OWM" class
   //     OWM owm = new OWM(owmApiKey);
    //    owm = new OWM(owmApiKey);
    LOG.info("starting currentWeatherByCityName");
    LOG.info("starting currentWeatherByCityName with club latitude = " + club.getClubLatitude());
    LOG.info("starting currentWeatherByCityName with club longitude = " + club.getClubLongitude());
 //   LOG.info("just before owm");

//		Channel result = service.getForecast("670807", DegreeUnit.CELSIUS);
	//	System.out.println(result.getDescription());
//System.out.println(result.getTitle());
// https://piunikaweb.com/2019/01/07/query-yahooapis-com-and-weather-yahooapis-com-not-working-heres-why/
// function stops on 03/02/2019  what a pity !!!

    String url = "https://query.yahooapis.com/v1/public/yql?q=select%20woeid%20from%20geo.places%20where%20text%3D%22("
            + club.getClubLatitude()// 43.95
            + "," 
            + club.getClubLongitude() //-79.88
            + ")%22%20limit%201&diagnostics=false";
// A WOEID (Where On Earth IDentifier) is a unique 32-bit reference identifier, 
//originally defined by GeoPlanet and now assigned by Yahoo!, that identifies any feature on Earth.
// In 2009, Yahoo! released GeoPlanet's WOEID data to the public,[3]				
    Document yahooApiResponse = Jsoup.connect(url).timeout(10 * 1000).get();
    String xmlString = yahooApiResponse.html();
    Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
 //   String JSOUP = "";
 //       LOG.info("from JSOUP = " + doc.select("woeid").first().text());
    String woeid = doc.select("woeid").first().text();
        LOG.info("JSOUP woeid = " + woeid);

    YahooWeatherService service = new YahooWeatherService();
    
 //   Channel result = service.getForecast(JSOUP, DegreeUnit.CELSIUS);
 //   LOG.info("YahooWeathereService Description = " + result.getDescription());
 //   LOG.info("YahooWeathereService Title = " + result.getTitle());
 //   LOG.info("YahooWeathereService Wind = " + result.getWind());
  //         LOG.info("before session old tyle test");
  //  FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("playerid", "test");
  //   LOG.info("after session old tyle test");
     
  //  List<Channel> channels = service.getForecastForLocation("1060 Brussels, Belgium", DegreeUnit.CELSIUS).first(3);   // was 3 - later
  // List<Channel> channels = service.getForecastForLocation(city + country, DegreeUnit.CELSIUS).first(3); 
    Channel channel = service.getForecast(woeid, DegreeUnit.CELSIUS); //.first(3));
   // StringBuilder sb = new StringBuilder ();
    StringJoiner sj = new StringJoiner("");
          Map<String,String> condition = new HashMap<>();
        condition.put("0","Tornado");
        condition.put("1","Tropical storm");
        condition.put("2","Hurricane");
        condition.put("3","Severe thunderstorms");
        condition.put("12","Showers");
        condition.put("23","Blustery");
        condition.put("26","Cloudy");
        condition.put("28","Mostly cloudy (day)");
        condition.put("30","Partly cloudy(day)");
        condition.put("34","Fair(day)");
        condition.put("39","Scattered thunderstorms");
    sj.add("<h1> Weather Information</h1>");
  //  for (Channel channel:channels){
            LOG.info("Yahoo channel = " + channel.getTitle());
        sj.add("<ul><li>");
        sj.add("<b>Localisation : </b>").add(channel.getTitle());
            LOG.info("Yahoo sunrise = " + channel.getAstronomy().getSunrise());
        String sunrise = utils.LCUtil.extractHHmm(channel.getAstronomy().getSunrise().toString());
        sj.add("</li><li><b>Sunrise : </b>").add(sunrise);
            LOG.info("Yahoo Sunset: " + channel.getAstronomy().getSunset());
        String sunset = utils.LCUtil.extractHHmm(channel.getAstronomy().getSunset().toString());    
        sj.add("</li><li><b>Sunset : </b>").add(sunset);
            LOG.info("Yahoo Atmosphere: " + channel.getAtmosphere());
        sj.add("</li><li><b>Relative Humidity : </b>").add(channel.getAtmosphere().getHumidity().toString()).add(" %");
            LOG.info("Yahoo Humidity: " + channel.getAtmosphere().getHumidity());
            LOG.info("Yahoo Wind: " + channel.getWind());
        sj.add("</li><li><b>Wind Speed : </b>").add(channel.getWind().getSpeed().toString()).add(" KM/H");
        sj.add("</li><li><b>Wind Direction : </b>").add(channel.getWind().getDirection().toString());
        String wd = WIND_DIRECTION[(int)Math.floor((channel.getWind().getDirection() % 360) / 22.5)];
            LOG.info(" Wind Direction wd = " + wd);
        sj.add(" - ").add(wd);
        sj.add("</li><li><b>Atmospheric Pressure : </b>").add(channel.getAtmosphere().getPressure().toString()).add(" MB");
        sj.add("</li><li><b>Visibility : </b>").add(channel.getAtmosphere().getVisibility().toString()).add(" KM");
        sj.add("</li><li><b>Rising : </b>").add(channel.getAtmosphere().getRising().toString());
    //   sj.add("<img src=\"http://l.yimg.com/a/i/us/we/52/26.gif\"/>");  // working !!
        String item = channel.getItem().toString(); 
        String url2 = item.substring(item.indexOf("<img src="),item.indexOf(".gif")+8);  // on prend .gif\"/> donc 8 car
        LOG.info("extracted image url = " + url2);
        sj.add(url2);
        sj.add("</li>");

            LOG.info("Yahoo Language: " + channel.getLanguage());
            LOG.info("Yahoo Link: " + channel.getLink());
              LOG.info("Yahoo Units: " + channel.getUnits());
            LOG.info("Yahoo Ttl : " + channel.getTtl()); 
            LOG.info("Yahoo Image : " + channel.getImage()); 
          LOG.info("Yahoo Item : " + channel.getItem()); 
          
  myList = new ArrayList<>(9);
  int start = 0;
  
     LOG.info("pattern = " + pattern);
 
  while (true) {
    start = item.indexOf("[day=",start);
 //   LOG.info("start 2 = " + start);
    String str1 = "], Forecast";
    int end2 = item.indexOf(str1,start);
 //   LOG.info("end 2 = " + end2);
    if(end2 == -1) break;
    String result = item.substring(start+1, end2);
        LOG.info("result 1 = " + result); 
    int comma =  result.indexOf(",", result.indexOf(",") + 1);  // cherche 2e virgule
        LOG.info("comma = " + comma);  
    String Datestr = result.substring(14,comma);  // commence char 18
        LOG.info("dateWeather = " + Datestr);
    LocalDateTime ldt = LocalDateTime.parse(Datestr, DTF);
        LOG.info(" success !! local date time = " + ldt); 
    result = ldt + " " + result;
       LOG.info("result 2 = " + result); 
       
           DayOfWeek dayOfWeek = ldt.getDayOfWeek();
           LOG.info("day of week  = " + dayOfWeek); 
           LOG.info("dayOfWeek Name = " + dayOfWeek.name());
       
    result = result.replaceAll("00.00.00", "").replaceAll("text=", "").replaceAll("day=", "").replaceAll("date=", "");
     LOG.info("result 3 = " + result); 
    myList.add(result);
  /////// for next iteration  
    start = end2 + str1.length()+1;
 }
    LOG.info("ending with myList = " + myList.toString());
  //  myList.forEach(System.out::println);
 sj.add("<h1> Forecast</h1>");
      for(int i = 0; i < myList.size(); i++) {
            String code = myList.get(i).substring(myList.get(i).length()-2);
                LOG.info("code = " + code);
            String value = condition.get(code);
                LOG.info("classic printing : <BR />" + myList.get(i) + " / " + value);
            sj.add("<li>").add(myList.get(i)).add(" / ").add(value).add("</li>");
      }

  //    LOG.info("before sessionMAPJSF23 test");
  //     sessionMapJSF23.put("testr", "test");
 
 //   } // end while sur array
     sj.add("</ul>");
            LOG.info("before sessionMAP ");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("weather", sj.toString()); // used in dialogWeather.xhtml
   //         LOG.info("after sessionMAP old style 2channel");
                    LOG.info("printing sessonMapJSF23 = ");
      utils.LCUtil.logMap(FacesContext.getCurrentInstance().getExternalContext().getSessionMap());
     //   sessionMapJSF23.put("weather", channel.getItem().toString());
  //      LOG.info("after sessionMAPJSF23");
    
          LOG.info("ending with sj = " + sj);
      return sj.toString();   // fake
    
/*
    // declaring object of "OWM" class 
    //    OWM owm = new OWM(OWM_KEY_LC);
  //  LOG.info("end of Yahoo");
  //  LOG.info("");
        
    
    if (null == owm) {
	owm = new OWM(OWM_KEY_LC);
        LOG.info("owm was null, new created");
    }

         LOG.info("key OK");
        // getting current weather data 
        owm.setLanguage(Language.ENGLISH);
        owm.setUnit(Unit.METRIC);
        CWD = owm.currentWeatherByCityName(club.getClubCity(), OWM.Country.BELGIUM);
        
        
  //      ho
//  LOG.info("line 01");

StringBuilder sb = new StringBuilder ();

    LOG.info("Sunrise: " + SDF_TIME.format(CWD.getSystemData().getSunriseDateTime()));
    sj.add("Sunrise: " + SDF_TIME.format(CWD.getSystemData().getSunriseDateTime()));
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
     wd = WIND_DIRECTION[(int)Math.floor((CWD.getWindData().getDegree() % 360) / 22.5)];
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
  
  
  owm = new OWM("9229fc57d217e84684dfb717867b67f5");
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
*/
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
    owm = new OWM("9229fc57d217e84684dfb717867b67f5");
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
        String s = fw.currentWeatherByCityName(club);
        LOG.info("current  returned is :" + s);
    //    s = dailyWeatherByCityName("Brussels", "Be",15);
    //    LOG.info("daily returned is :" + s);
 } // end method main
    
} // end class