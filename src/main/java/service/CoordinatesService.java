package service;

import com.google.maps.GeocodingApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import entite.Club;
import entite.Player;
import entite.LatLng;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;

/**
 * Service de géocodage Google Maps
 * ✅ @ApplicationScoped — instance unique partagée
 * ✅ Protection ArrayIndexOutOfBoundsException
 * ✅ Recherche par type de composant — robuste multi-pays
 */
@ApplicationScoped
@Named
public class CoordinatesService {

    private final String apiKey;

    public CoordinatesService() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Google Maps API key not set in environment variables");
        }
    }

    // ========================================
    // UPDATE COORDINATES — Club
    // ========================================

    /**
     * Met à jour les coordonnées et le fuseau horaire d'un Club.
     */
    public Club updateCoordinates(Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (club == null || club.getAddress() == null) {
                throw new IllegalArgumentException("Club or address is null");
            }

            String address = buildFullAddress(
                    club.getAddress().getStreet(),
                    club.getAddress().getZipCode(),
                    club.getAddress().getCity(),
                    club.getAddress().getCountry().getCode());

            LOG.debug(methodName + " - geocoding address = " + address);
            GeocodingResult result = geocodeFirst(address);
            applyCoordinatesAndTimeZone(result, club.getAddress());

            // ✅ Recherche par type — robuste, indépendant de l'index
            Arrays.stream(result.addressComponents)
                    .filter(c -> Arrays.asList(c.types)
                            .contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1))
                    .findFirst()
                    .ifPresentOrElse(
                            c -> {
                                String region = c.longName + "/" + c.shortName;
                                club.setRegion(region);
                                LOG.debug(methodName + " - region = " + region);
                            },
                            () -> LOG.warn(methodName + " - ADMINISTRATIVE_AREA_LEVEL_1 not found"
                                    + " — addressComponents length = "
                                    + result.addressComponents.length)
                    );

            club.setShowCoordinatesManual(true);
            return club;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return club;
        }
    } // end method

    // ========================================
    // UPDATE COORDINATES — Player
    // ========================================

    /**
     * Met à jour les coordonnées et le fuseau horaire d'un Player.
     */
    public Player updateCoordinates(Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (player == null || player.getAddress() == null) {
                throw new IllegalArgumentException("Player or address is null");
            }

            String address = buildFullAddress(
                    player.getAddress().getStreet(),
                    player.getAddress().getZipCode(),
                    player.getAddress().getCity(),
                    player.getAddress().getCountry().getCode());

            LOG.debug(methodName + " - geocoding address = " + address);
            GeocodingResult result = geocodeFirst(address);
            LOG.debug(methodName + " - addressComponents length = "
                    + result.addressComponents.length);

            applyCoordinatesAndTimeZone(result, player.getAddress());
            return player;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return player;
        }
    } // end method

    // ========================================
    // MÉTHODES PRIVÉES
    // ========================================

    /**
     * Construit une adresse complète pour Google Maps
     */
    private String buildFullAddress(String street, String zip, String city, String countryCode) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        return String.format("%s, %s %s, %s", street, zip, city, countryCode);
    } // end method

    /**
     * Appel à l'API Google Maps — premier résultat de géocodage
     */
    private GeocodingResult geocodeFirst(String address)
            throws InterruptedException, ApiException, IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try (GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build()) {

            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if (results == null || results.length == 0) {
                throw new IllegalStateException("No geocoding result for address: " + address);
            }
            LOG.debug(methodName + " - results count = " + results.length);
            return results[0];
        }
    } // end method

    /**
     * Applique les coordonnées et le fuseau horaire à l'adresse
     */
    private void applyCoordinatesAndTimeZone(GeocodingResult result,
                                             entite.Address address) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        LatLng latlng = new LatLng();
        latlng.setLat(result.geometry.location.lat);
        latlng.setLng(result.geometry.location.lng);
        address.setLatLng(latlng);
        LOG.debug(methodName + " - latlng = " + latlng);

        java.util.TimeZone timezone = find.FindTimeZone.find(latlng);
        address.setZoneId(timezone.getID());
        LOG.debug(methodName + " - zoneId = " + timezone.getID());
    } // end method

} // end class