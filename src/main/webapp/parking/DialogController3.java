package Controllers;

import entite.Club;
import entite.Flight;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DialogFrameworkOptions;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Contrôleur pour la gestion des dialogues PrimeFaces.
 * MODIFICATION: Suppression de tous les "static" pour compatibilité CDI.
 */
@Named("dialogC2")
@RequestScoped
public class DialogController3 implements Serializable {

    // ============================================
    // CHANGEMENT: Supprimer "static" partout
    // ============================================

    public void showUnavailable() {  // ← Était "static"
        LOG.debug("entering showUnavailable");
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("height", 650);
        options.put("width", 500);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("closable", true);
        options.put("includeViewParams", true); 
        
        PrimeFaces.current().dialog().openDynamic("dialogUnavailable.xhtml", options, null); 
        LOG.debug("dialogUnavailable.xhtml is opened !");
    }
    
    public void showHandicapIndex() {  // ← Était "static"
        LOG.debug("entering DialogController showHandicapIndex");
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
            .draggable(false)
            .modal(true)
            .height("60%") 
            .width("50%")
            .contentHeight("100%")
            .contentWidth("100%")
            .closeOnEscape(true)
            .build();

        PrimeFaces.current().dialog().openDynamic("dialog_handicap_index.xhtml", options, null); 
        LOG.debug("dialogHandicap_index.xhtml is opened !");
    }
    
    public void showFlight() {  // ← Était "static"
        LOG.debug("entering DialogController showFlight");
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
            .draggable(false)
            .modal(true)
            .resizable(true)
            .height("70%") 
            .width("40%")
            .closable(true)
            .contentHeight("100%")
            .contentWidth("100%")
            .closeOnEscape(true)
            .includeViewParams(true)
            .headerElement("customheader")
            .build();
        PrimeFaces.current().dialog().openDynamic("dialogFlight.xhtml", options, null); 
        LOG.debug("exiting DialogController showFlight");
    }  
    
    public void showMatchplayClassment(String c) {  // ← Déjà non-static, OK
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("width", 900);
        options.put("height", 1800);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("closable", true);
        options.put("includeViewParams", true); 
        
        Map<String, List<String>> params = new HashMap<>(); 
        List<String> values = new ArrayList<>(); 
        params.put("IdClub", values);
        PrimeFaces.current().dialog().openDynamic("dialog_matchplay_classment.xhtml", options, params);
        LOG.debug("dialog_matchplay_classment.xhtml is opened !");
    }
    
    public void showClubDetail(Club c) {  // ← Déjà non-static, OK
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("width", 900);
        options.put("height", 800);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("closable", true);
        options.put("includeViewParams", true); 
        
        Map<String, List<String>> params = new HashMap<>(); 
        List<String> values = new ArrayList<>(); 
        values.add(Integer.toString(c.getIdclub())); 
        params.put("IdClub", values);
        PrimeFaces.current().dialog().openDynamic("dialogClubDetail.xhtml", options, params);
        LOG.debug("dialogClubDetail.xhtml is opened !");
    }
    
    public void showSelectClub(String param) {  // ← Était "static"
        LOG.debug("entering DialogController showSelectClub");
        LOG.debug("with param = " + param); 
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
            .draggable(false)
            .modal(true)
            .height("70%") 
            .width("50%")
            .contentHeight("100%")
            .contentWidth("100%")
            .closeOnEscape(true)
            .includeViewParams(true)
            .headerElement("Header schowSelectClub")
            .build();
        Map<String, List<String>> params = new HashMap<>(); 
        List<String> values = new ArrayList<>(); 
        values.add(param); 
        params.put("type_club", values);
        
        params.entrySet().forEach(entry -> {
            LOG.debug("Map params = " + entry.getKey() + ": " + entry.getValue());
        });
        PrimeFaces.current().dialog().openDynamic("dialogClub.xhtml", options, params);
    }
    
    public void showSelectCourse(String from, String clubid) throws IOException {  // ← Était "static"
        LOG.debug("entering DialogController showSelectCourse");
        LOG.debug("with param clubid = " + clubid);
        if(clubid == null){
            String msg = "Please first select a club !" ; 
            LOG.error(msg);
            showMessageFatal(msg);
        }
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
            .draggable(true)
            .modal(true)
            .height("70%") 
            .width("50%")
            .contentHeight("100%")
            .contentWidth("100%")
            .closeOnEscape(true)
            .includeViewParams(true)
            .headerElement("customheader")
            .closable(true)
            .resizable(true)
            .build();
        Map<String, List<String>> params = new HashMap<>();
        List<String> values = new ArrayList<>(); 
        values.add(clubid);
        params.put("clubId", values);
        PrimeFaces.current().dialog().openDynamic("dialogCourse.xhtml", options, params);
    }    
    
    public void showSelectPlayer(String p) {  // ← Était "static"
        LOG.debug("entering DialogController showSelectPlayer");
        LOG.debug("with param = " + p);
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("width", 900);
        options.put("height", 600);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("closable", true);
        options.put("modal", true);
        options.put("headerElement", "clubName");
        options.put("dynamic", true);
        
        Map<String, List<String>> params = new HashMap<>(); 
        List<String> values = new ArrayList<>(); 
        values.add(p); 
        params.put("param_player", values);
        PrimeFaces.current().dialog().openDynamic("dialogPlayers.xhtml", options, params); 
        LOG.debug("exiting DialogController showSelectPlayer");
    }  
    
    public void showWeather() {  // ← Était "static"
        LOG.debug("entering DialogWeather");
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", true);
        options.put("resizable", true);
        options.put("width", 900);
        options.put("height", 600);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("closable", true);
        options.put("modal", true);
        options.put("headerElement", "customheader");
        PrimeFaces.current().dialog().openDynamic("dialogWeather.xhtml", options, null); 
        LOG.debug("exiting DialogController showWeather");
    }  
    
    public void showSelectRound(String param) {  // ← Était "static"
        LOG.debug("entering DialogController showSelectRound");
        LOG.debug("with param = " + param); 
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
            .draggable(false)
            .modal(true)
            .height("70%") 
            .width("50%")
            .contentHeight("100%")
            .contentWidth("100%")
            .closeOnEscape(true)
            .includeViewParams(true)
            .headerElement("Header showSelectRound")
            .build();
        Map<String, List<String>> params = new HashMap<>(); 
        List<String> values = new ArrayList<>(); 
        values.add(param); 
        params.put("type_club", values);
        params.entrySet().forEach(entry -> {
            LOG.debug("Map params = " + entry.getKey() + ": " + entry.getValue());
        });
        PrimeFaces.current().dialog().openDynamic("dialogRound.xhtml", options, params);
    }
    
    public void showRound() {  // ← Était "static"
        Map<String,Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("contentHeight", 640);
        options.put("contentWidth", 840);
        options.put("closable", true); 
        options.put("header", "header by LC");
        PrimeFaces.current().dialog().openDynamic("dialog_played_rounds.xhtml", options, null); 
    }  

    public void onFlightChosen(SelectEvent<Object> event) {  // ← Déjà non-static, OK
        LOG.debug("entering onFlightChosen() !");
        LOG.debug("entering onFlightChosen() with source = " + event.getSource() );
        Flight flight = (Flight) event.getObject(); 
        String msg = "Dialog return with flight : " + flight.toString();
        LOG.debug(msg);
        LOG.debug("Workhour at this moment = " );
        PrimeFaces.current().ajax().update("form_round:idworkhour");
        showMessageInfo(msg);
    } 

    public void handleReturnCourse(SelectEvent<Object> event) throws IOException {  // ← Déjà non-static, OK
        LOG.debug("handleReturn called !!!");
        LOG.debug("event getObject = " + event.getObject());
    }
    
    public void onDialogReturn(SelectEvent<Object> event) {  // ← Était "static"
        LOG.debug("entering onDialogReturn !");
        LOG.debug("entering onDialogReturn with event = " + event.getSource() );
        Object rating = event.getObject(); 
        String msg = "Dialog return with rating : " + rating.toString();
        LOG.debug(msg);
        showMessageInfo(msg);
    } 

    public boolean closeDialog(Object obj) throws IOException {  // ← Était "static"
        try{ 
                LOG.debug("entering closeDialog with : " + obj.toString() );
            PrimeFaces.current().dialog().closeDynamic(obj);
            String msg = "Dialog closed for = " + obj.toString();
                LOG.debug(msg);
            return true;
        } catch (Exception ex) {
            String msg = "Exception in closeDialog " + ex;
            LOG.error(msg);
            throw ex; // mod 2026
         //   return false;
        }
    }
}