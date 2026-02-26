
package dialog;

import entite.Club;
import entite.Flight;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DialogFrameworkOptions;

/**
 * Contrôleur pour la gestion des dialogues PrimeFaces via le Dialog Framework.
 * <p>
 * Ce contrôleur utilise le PrimeFaces Dialog Framework (DF) pour ouvrir
 * des dialogues dynamiques et gérer leurs retours.
 * </p>
 * <p>
 * <strong>Références :</strong>
 * <ul>
 *   <li><a href="https://primefaces.github.io/primefaces/12_0_0/#/core/dialogframework">PrimeFaces Dialog Framework</a></li>
 *   <li><a href="http://www.primefaces.org:8080/showcase/ui/df/basic.xhtml">PrimeFaces Showcase</a></li>
 * </ul>
 * </p>
 */
@Named("dialogC3")
@RequestScoped
public class DialogController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private DialogConfigService dialogConfig;
    
    /**
     * Affiche le dialogue d'indisponibilité.
     */
    public void showUnavailable() {
        LOG.debug("Opening unavailable dialog");
        
        DialogFrameworkOptions options = dialogConfig.createMediumModalDialog()
                .closable(true)
                .build();
        
        openDialog(DialogView.UNAVAILABLE, options);
    }
    
    /**
     * Affiche le dialogue de l'index de handicap.
     */
    public void showHandicapIndex() {
        LOG.debug("Opening handicap index dialog");
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .closeOnEscape(true)
                .build();
        
        openDialog(DialogView.HANDICAP_INDEX, options);
    }
    
    /**
     * Affiche le dialogue de sélection de flight.
     */
    public void showFlight() {
        LOG.debug("Opening flight selection dialog");
        
        DialogFrameworkOptions options = dialogConfig.createMediumModalDialog()
                .resizable(true)
                .closable(true)
                .closeOnEscape(true)
                .headerElement("customheader")
                .build();
        
        openDialog(DialogView.FLIGHT, options);
    }
    
    /**
     * Affiche le dialogue du classement matchplay.
     *
     * @param clubId l'identifiant du club (non utilisé actuellement)
     */
    public void showMatchplayClassment(String clubId) {
        LOG.debug("Opening matchplay classement dialog for club: {}", clubId);
        
        DialogFrameworkOptions options = dialogConfig.createExtraLargeModalDialog()
                .closable(true)
                .build();
        
        Map<String, List<String>> params = DialogParams.single("IdClub", clubId);
        openDialog(DialogView.MATCHPLAY_CLASSMENT, options, params);
    }
    
    /**
     * Affiche le dialogue des détails d'un club.
     *
     * @param club le club dont on veut afficher les détails
     * @throws IllegalArgumentException si le club est null
     */
    public void showClubDetail(Club club) {
        if (club == null) {
            throw new IllegalArgumentException("Club cannot be null");
        }
        
        LOG.debug("Opening club detail dialog for club ID: {}", club.getIdclub());
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .closable(true)
                .build();
        
        Map<String, List<String>> params = DialogParams.single(
            "IdClub", 
            String.valueOf(club.getIdclub())
        );
        
        openDialog(DialogView.CLUB_DETAIL, options, params);
    }
    
    /**
     * Affiche le dialogue de sélection de club.
     *
     * @param typeClub le type de club à sélectionner
     * @throws IllegalArgumentException si typeClub est null ou vide
     */
    public void showSelectClub(String typeClub) {
        if (typeClub == null || typeClub.isBlank()) {
            throw new IllegalArgumentException("Club type cannot be null or empty");
        }
        
        LOG.debug("Opening club selection dialog with type: {}", typeClub);
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .closeOnEscape(true)
                .headerElement("Header schowSelectClub")
                .build();
        
        Map<String, List<String>> params = DialogParams.single("type_club", typeClub);
        openDialog(DialogView.CLUB_SELECT, options, params);
    }
    
    /**
     * Affiche le dialogue de sélection de parcours.
     *
     * @param from source de l'appel (non utilisé actuellement)
     * @param clubId l'identifiant du club
     * @throws IllegalArgumentException si clubId est null ou vide
     */
    public void showSelectCourse(String from, String clubId) {
        if (clubId == null || clubId.isBlank()) {
            String msg = "Please first select a club!";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        LOG.debug("Opening course selection dialog for club ID: {}", clubId);
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .draggable(true)
                .closeOnEscape(true)
                .closable(true)
                .resizable(true)
                .headerElement("customheader")
                .build();
        
        Map<String, List<String>> params = DialogParams.single("clubId", clubId);
        openDialog(DialogView.COURSE_SELECT, options, params);
    }
    
    /**
     * Affiche le dialogue de sélection de joueur.
     *
     * @param playerParam paramètre de recherche du joueur
     * @throws IllegalArgumentException si playerParam est null ou vide
     */
    public void showSelectPlayer(String playerParam) {
        if (playerParam == null || playerParam.isBlank()) {
            throw new IllegalArgumentException("Player parameter cannot be null or empty");
        }
        
        LOG.debug("Opening player selection dialog with param: {}", playerParam);
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .draggable(true)
                .resizable(true)
                .closable(true)
                .headerElement("clubName")
                .build();
        
        Map<String, List<String>> params = DialogParams.single("param_player", playerParam);
        openDialog(DialogView.PLAYER_SELECT, options, params);
    }
    
    /**
     * Affiche le dialogue de la météo.
     */
    public void showWeather() {
        LOG.debug("Opening weather dialog");
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .draggable(true)
                .resizable(true)
                .closable(true)
                .headerElement("customheader")
                .build();
        
        openDialog(DialogView.WEATHER, options);
    }
    
    /**
     * Affiche le dialogue de sélection de round.
     *
     * @param typeClub le type de club pour filtrer les rounds
     * @throws IllegalArgumentException si typeClub est null ou vide
     */
    public void showSelectRound(String typeClub) {
        if (typeClub == null || typeClub.isBlank()) {
            throw new IllegalArgumentException("Club type cannot be null or empty");
        }
        
        LOG.debug("Opening round selection dialog with type: {}", typeClub);
        
        DialogFrameworkOptions options = dialogConfig.createLargeModalDialog()
                .closeOnEscape(true)
                .headerElement("Header showSelectRound")
                .build();
        
        Map<String, List<String>> params = DialogParams.single("type_club", typeClub);
        openDialog(DialogView.ROUND_SELECT, options, params);
    }
    
    /**
     * Affiche le dialogue des rounds joués.
     */
    public void showPlayedRounds() {
        LOG.debug("Opening played rounds dialog");
        
        DialogFrameworkOptions options = dialogConfig.createCustomDialog(840, 640)
                .closable(true)
                .build();
        
        openDialog(DialogView.PLAYED_ROUNDS, options);
    }
    
    /**
     * Gère le retour du dialogue de sélection de flight.
     *
     * @param event l'événement de sélection contenant le flight choisi
     */
    public void onFlightChosen(SelectEvent<Object> event) {
        LOG.debug("Flight selection event received from: {}", event.getSource());
        
        Flight flight = (Flight) event.getObject();
        LOG.info("Flight selected: {}", flight);
        
        // Mise à jour du composant affichant les heures de travail
        PrimeFaces.current().ajax().update("form_round:idworkhour");
        
        utils.LCUtil.showMessageInfo("Flight selected: " + flight.toString());
    }
    
    /**
     * Gère le retour du dialogue de sélection de parcours.
     * <p>
     * Appelé depuis competition_create_description.xhtml
     * </p>
     *
     * @param event l'événement de sélection contenant le parcours choisi
     */
    public void handleReturnCourse(SelectEvent<Object> event) {
        LOG.debug("Course selection event received");
        LOG.debug("Selected course: {}", event.getObject());
    }
    
    /**
     * Gère le retour générique d'un dialogue.
     *
     * @param event l'événement de sélection
     */
    public void onDialogReturn(SelectEvent<Object> event) {
        LOG.debug("Dialog return event received from: {}", event.getSource());
        
        Object result = event.getObject();
        LOG.info("Dialog returned with: {}", result);
        
        utils.LCUtil.showMessageInfo("Dialog returned: " + result.toString());
    }
    
    /**
     * Ferme un dialogue dynamique.
     *
     * @param result l'objet résultat à retourner au dialogue parent
     * @return true si le dialogue a été fermé avec succès, false sinon
     */
    public boolean closeDialog(Object result) {
        try {
            LOG.debug("Closing dialog with result: {}", result);
            
            PrimeFaces.current().dialog().closeDynamic(result);
            
            LOG.debug("Dialog closed successfully");
            return true;
            
        } catch (Exception e) {
            LOG.error("Error closing dialog", e);
            return false;
        }
    }
    
    /**
     * Ouvre un dialogue sans paramètres.
     *
     * @param view la vue du dialogue à ouvrir
     * @param options les options de configuration du dialogue
     */
    private void openDialog(DialogView view, DialogFrameworkOptions options) {
        openDialog(view, options, null);
    }
    
    /**
     * Ouvre un dialogue avec paramètres.
     *
     * @param view la vue du dialogue à ouvrir
     * @param options les options de configuration du dialogue
     * @param params les paramètres à passer au dialogue
     */
    private void openDialog(
            DialogView view, 
            DialogFrameworkOptions options, 
            Map<String, List<String>> params) {
        
        try {
            LOG.debug("Opening dialog: {} with params: {}", view.getViewName(), params);
            
            PrimeFaces.current().dialog().openDynamic(
                view.getViewName(), 
                options, 
                params
            );
            
            LOG.debug("Dialog {} opened successfully", view.getViewName());
            
        } catch (Exception e) {
            LOG.error("Error opening dialog: {}", view.getViewName(), e);
            throw new DialogOpenException("Failed to open dialog: " + view.getViewName(), e);
        }
    }
}