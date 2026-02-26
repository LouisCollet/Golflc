package find;

import Controller.refact.PlayerController;
import br.com.esign.google.geocode.GoogleGeocode;
import br.com.esign.google.geocode.model.GeocodeResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entite.Player;
import entite.LatLng;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Service CDI moderne pour géocoder Player ou Club.
 */
@ApplicationScoped
public class CoordinatesService {

    @Inject
    PlayerController playerController; // Injection directe du playerController

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Met à jour les coordonnées du Player courant dans PlayerController.
     */
    public void updatePlayerCoordinates() {
        Optional.ofNullable(playerController.getPlayer())
                .ifPresentOrElse(this::geocodePlayer, 
                                 () -> LOG.debug("PlayerController player is null, skipping geocode"));
    }

    private void geocodePlayer(Player player) {
        try {
            String fullAddress = String.format("%s, %s, %s",
                    player.getAddress().getStreet(),
                    player.getAddress().getZipCode(),
                    player.getAddress().getCity() + ", " + player.getAddress().getCountry().getCode());

            GeocodeResponse response = new GoogleGeocode(System.getenv("GOOGLE_MAPS_API_KEY"), fullAddress)
                                        .getResponseObject();

            LOG.debug("GeocodeResponse: " + gson.toJson(response));

            if (!response.isStatusOK()) {
                showMessageFatal("PlayerCoordinates not found! " + response.getErrorMessage());
                return;
            }

            LatLng latlng = new LatLng();
            latlng.setLat(response.getGeometry().getLocation().getLat().doubleValue());
            latlng.setLng(response.getGeometry().getLocation().getLng().doubleValue());

            player.getAddress().setLatLng(latlng);

            TimeZone timezone = find.FindTimeZone.find(latlng);
            player.getAddress().setZoneId(timezone.getID());

            showMessageInfo("Coordinates successfully inserted in player: " + player);
            LOG.debug("Coordinates successfully inserted in player: " + player);

        } catch (Exception ex) {
            String msg = "Exception in updatePlayerCoordinates: " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
        }
    }
}
