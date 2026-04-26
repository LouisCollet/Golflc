package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.OpenWeather;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ApplicationScoped
public class FindOpenWeather2 implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String API = "https://api.openweathermap.org/data/2.5/weather";
    private static final String[] WIND_DIRECTION = {
            "N","NNE","NE","ENE","E","ESE","SE","SSE",
            "S","SSW","SW","WSW","W","WNW","NW","NNW"
    };

    @Inject private entite.Settings settings;

    public dto.WeatherDTO find(Club club, String language) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {

            if (club == null ||
                club.getAddress() == null ||
                club.getAddress().getLatLng() == null) {

                LOG.warn("Club without coordinates");
                return null;
            }

            double lat = club.getAddress().getLatLng().getLat();
            double lon = club.getAddress().getLatLng().getLng();

            String apiKey = settings.getProperty("OPENWEATHER_API_KEY");

            URI uri = URI.create(String.format(
                    "%s?lat=%s&lon=%s&appid=%s&units=metric&lang=%s",
                    API, lat, lon, apiKey, language));

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(uri)
                    .header("User-Agent", "Java HttpClient")
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.error("OpenWeather API error : " + response.statusCode());
                return null;
            }

            OpenWeather weather = MAPPER.readValue(response.body(), OpenWeather.class);
            return convert(weather, club);
        } catch (Exception e) {
            LOG.error("Weather service error", e);
            return null;
        }
    }

    private dto.WeatherDTO convert(OpenWeather w, Club club) {
        String windDir = null;
        if (w.getWind() != null && w.getWind().getDeg() != null) {
            int index = (int) Math.floor((w.getWind().getDeg() % 360) / 22.5);
            windDir = WIND_DIRECTION[index];
        }
        Instant instant = Instant.ofEpochSecond(w.getDt());
        ZonedDateTime dateTime =
                ZonedDateTime.ofInstant(
                        instant,
                        ZoneId.of(club.getAddress().getZoneId()));

        return new dto.WeatherDTO(
                dateTime,
                w.getWeather().get(0).getMain(),
                w.getWeather().get(0).getDescription(),
                w.getWeather().get(0).getIcon(),
                w.getMain().getTemp(),
                w.getMain().getFeelsLike(),
                w.getMain().getHumidity(),
                windDir,
                w.getWind().getSpeed()
        );
    }
}