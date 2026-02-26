package lists;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Club;
import entite.Flight;
import entite.Round;
import googlemaps.SunriseSunsetResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static interfaces.GolfInterface.ZDF_HOURS;
import static interfaces.GolfInterface.ZDF_YEAR_MONTH_DAY;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Appel API sunrise-sunset.org pour récupérer lever/coucher de soleil
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — non utilisée dans l'original
 * ✅ @Named supprimé — ce bean n'est pas accédé depuis les vues JSF
 * ✅ main() de test supprimée
 * ✅ Retour null remplacé par null explicitement documenté (pas de liste vide — Flight est un objet unique)
 */
@ApplicationScoped
public class SunriseSunsetList implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL = "https://api.sunrise-sunset.org/json";

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private Flight liste = null;

    // HttpClient réutilisable — thread-safe, @ApplicationScoped le partage sans risque
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — elle n'était pas utilisée dans l'original
    // ========================================

    /**
     * Récupère le lever et coucher de soleil pour un round et un club donnés.
     *
     * @param round le round (pour la date)
     * @param club  le club (pour les coordonnées GPS et la timezone)
     * @return un Flight avec sunrise/sunset, ou null si l'API est indisponible
     */
    public Flight list(Round round, Club club) {
        final String methodName = "SunriseSunsetList.list";

        if (liste != null) {
            LOG.debug("{} - returning cached flight", methodName);
            return liste;
        }

        try {
            Objects.requireNonNull(round, "round cannot be null");
            Objects.requireNonNull(club,  "club cannot be null");

            LOG.debug("{} - date={}", methodName, round.getRoundDate());
            LOG.debug("{} - timezone={}", methodName, club.getAddress().getZoneId());

            if (!utils.LCUtil.isValidTimeZone(club.getAddress().getZoneId())) {
                String msg = "Invalid timezone in " + methodName + ": " + club.getAddress().getZoneId();
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }

            String url = buildUrl(round, club);
            LOG.debug("{} - URL={}", methodName, url);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "Java HttpClient Bot")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            LOG.debug("{} - status={}", methodName, response.statusCode());
            LOG.debug("{} - body={}",   methodName, response.body());
            response.headers().map().forEach((k, v) -> LOG.debug("header={}:{}", k, v));

            ObjectMapper om = new ObjectMapper();
            om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            SunriseSunsetResponse sunriseSunset = om.readValue(response.body(), SunriseSunsetResponse.class);

            om.configure(SerializationFeature.INDENT_OUTPUT, true);
            LOG.debug("{} - parsed response:\n{}", methodName, om.writeValueAsString(sunriseSunset));

            if ("OK".equals(sunriseSunset.getStatus())) {
                Flight flight = buildFlight(sunriseSunset, club);
                LOG.debug("{} - flight={}", methodName, flight);
                liste = flight;                                         // ✅ mise en cache
                return liste;
            } else {
                String msg = methodName + " - API status not OK: " + sunriseSunset.getStatus();
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }

        } catch (Exception e) {
            return handleException(e, club, methodName);
        }
    }

    // ========================================
    // MÉTHODES PRIVÉES
    // ========================================

    private String buildUrl(Round round, Club club) {
        return API_URL
                + "?lat="    + club.getAddress().getLatLng().getLat()
                + "&lng="    + club.getAddress().getLatLng().getLng()
                + "&date="   + ZDF_YEAR_MONTH_DAY.format(round.getRoundDate())
                + "&formatted=0";
    }

    private Flight buildFlight(SunriseSunsetResponse sunriseSunset, Club club) {
        ZoneId zoneId = ZoneId.of(club.getAddress().getZoneId());

        ZonedDateTime sunrise = ZonedDateTime
                .parse(sunriseSunset.getResults().getSunrise(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant().atZone(zoneId);

        ZonedDateTime sunset = ZonedDateTime
                .parse(sunriseSunset.getResults().getSunset(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant().atZone(zoneId);

        LOG.debug("sunrise HH:mm = {}", ZDF_HOURS.format(sunrise));
        LOG.debug("sunset  HH:mm = {}", ZDF_HOURS.format(sunset));
        LOG.debug("offset        = {}", sunrise.getOffset());

        Flight flight = new Flight();
        flight.setSunrise(sunrise);
        flight.setSunset(sunset);
        return flight;
    }

    /**
     * Gestion de l'indisponibilité SSL de l'API (site hors ligne)
     * Conserve le comportement original : sunrise à 8h00, sunset +10h
     */
    private Flight handleException(Exception e, Club club, String methodName) {
        if (e.getMessage() != null && e.getMessage().contains("PKIX path validation failed")) {
            LOG.error("{} - no SSL connection, using fallback sunrise/sunset", methodName);

            LocalDateTime ldt = LocalDateTime.now()
                    .withHour(8).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of(club.getAddress().getZoneId()));

            Flight flight = new Flight();
            flight.setSunrise(zdt);
            flight.setSunset(zdt.plusHours(10));

            String msg = "Fallback sunrise/sunset: " + flight.getSunrise() + " / " + flight.getSunset();
            LOG.info(msg);
            showMessageInfo(msg);
            return flight;
        }

        String msg = "Exception in " + methodName + ": " + e.getMessage();
        LOG.error(msg, e);
        showMessageFatal(msg);
        return null;
    }

    // ========================================
    // CACHE - Getters / Setters statiques
    // ========================================

    // ✅ Getters/setters d'instance
    public Flight getListe()           { return liste; }
    public void setListe(Flight liste) { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

} // end class