package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.OpenWeather;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Finds current weather for a club via OpenWeatherMap API.
 * Migrated to CDI — 2026-02-25
 */
@ApplicationScoped
public class FindOpenWeather implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private static final String[] WIND_DIRECTION = {
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW", "W", "West-Northwest", "NW", "NNW"
    };

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Inject private entite.Settings settings;

    public FindOpenWeather() { }

    /**
     * Finds current weather for a club.
     * @param club the club (must have address with lat/lng and zoneId)
     * @return HTML-formatted weather string, or null on error
     */
    public String find(Club club, String language) { // fix multi-user 2026-03-07 — language as parameter
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with club = " + club + ", language = " + language);
        try {
            String string_url = "https://api.openweathermap.org/data/2.5/weather"
                    + "?lat=" + String.valueOf(club.getAddress().getLatLng().getLat())
                    + "&lon=" + String.valueOf(club.getAddress().getLatLng().getLng())
                    + "&appid=" + settings.getProperty("OPENWEATHER_API_KEY")
                    + "&units=metric"
                    + "&lang=" + language;

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(string_url))
                    .setHeader("User-Agent", "Java 11 HttpClient Bot")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            LOG.debug(methodName + " - response statuscode = " + response.statusCode());
            LOG.debug(methodName + " - response body = " + response.body());

            OpenWeather weather = OBJECT_MAPPER.readValue(response.body(), OpenWeather.class);

            String wd = null;
            if (weather.getWind().getDeg() != null) {
                wd = WIND_DIRECTION[(int) Math.floor((weather.getWind().getDeg() % 360) / 22.5)];
                LOG.debug(methodName + " - wind direction: " + wd);
            } else {
                LOG.debug(methodName + " - wind direction: unknown");
            }

            Instant instant = Instant.ofEpochSecond(weather.getDt());
            ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of(club.getAddress().getZoneId()));
            LOG.debug(methodName + " - time zoned = " + zdt);

            StringBuilder sb = new StringBuilder();
            sb.append("Infos météos").append(" at time = ").append(zdt)
              .append("<img src=http://openweathermap.org/img/wn/")
              .append(weather.getWeather().getFirst().getIcon()).append("@2x.png>")
              .append(" <b>general = </b>").append(weather.getWeather().getFirst().getMain())
              .append(" , ").append(weather.getWeather().getFirst().getDescription())
              .append(" <b>Wind direction = </b>").append(wd)
              .append(" <b>Speed = </b>").append(weather.getWind().getSpeed())
              .append(" <b>Temperature = </b>").append(weather.getMain().getTemp())
              .append(" <b>feels like = </b>").append(weather.getMain().getFeelsLike())
              .append(" <b>humidity = </b>").append(weather.getMain().getHumidity());

            String json = OBJECT_MAPPER.writeValueAsString(weather);
            LOG.debug(methodName + " - json = \n" + json);

            return sb.toString();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Club club = new Club();
            club.setIdclub(113); // anderlecht
            club = new read.ReadClub().read(club);
            FindOpenWeather fow = new FindOpenWeather();
            String s = fow.find(club);
            LOG.debug("response in main = :" + s);
        } catch (Exception e) {
            LOG.error("Exception in main = " + e.getMessage());
        }
    } // end main
*/

} // end class
