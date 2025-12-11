package Controllers;

import entite.Club;
import entite.Flight;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
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
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;

/**
 *http://www.javacodegeeks.com/2015/01/primefaces-opening-external-pages-in-dynamically-generated-dialog.html
 * http://www.journaldev.com/4056/primefaces-utilities-requestcontext-el-functions-dialog-framework-search-expression-framework
 * 
 */
//PrimeFaces's Dialog Framework (DF) 
@Named("dialogC")
@RequestScoped // mod 12-02-2023
public class DialogController implements Serializable{

  public static void showUnavailable(){
        LOG.debug("entering showUnavailable");
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("height", 650);
    options.put("width", 500);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("closable", true); // closed by a button
    options.put("includeViewParams", true); 
    
    /*  from PF 12 : https://primefaces.github.io/primefaces/12_0_0/#/core/dialogframework?id=dialog-framework
    http://www.primefaces.org:8080/showcase/ui/df/basic.xhtml?jfwid=8a8b6
    */

    
    PrimeFaces.current().dialog().openDynamic("dialogUnavailable.xhtml", options, null); 
        LOG.debug("dialogUnavailable.xhtml is opened !");
}
  /*  
  public static void showHandicap(){
        LOG.debug("entering showHandicap");
 // new 12-02-2023       
    
  //  Map<String,Object> options = new HashMap<>();
  //  options.put("modal", true);
  //  options.put("draggable", false);
 //   options.put("resizable", true);
  //  options.put("contentHeight", 320);
  //  options.put("contentWidth", 640);  //default
 //   options.put("closable", true); // closed by a button
 //   options.put("includeViewParams", true); 
    
    PrimeFaces.current().dialog().openDynamic("dialogHandicap.xhtml", options, null); 
        LOG.debug("dialogHandicap.xhtml is opened !");
}
  */
 public static void showHandicapIndex(){
        LOG.debug("entering DialogController showHandicapIndex");
    DialogFrameworkOptions options = DialogFrameworkOptions.builder()
        .draggable(false)
        .modal(true)
        .height("60%") 
        .width("50%")
        .contentHeight("100%")
        .contentWidth("100%")
        .closeOnEscape(true) // Whether the dialog can be closed with escape key.
        .build();

//    PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage(FacesMessage.SEVERITY_INFO, "What we do in life", "Echoes in eternity."));
    PrimeFaces.current().dialog().openDynamic("dialog_handicap_index.xhtml", options, null); 
        LOG.debug("dialogHandicap_index.xhtml is opened !");
}
  
public static void showFlight(){
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
        .closeOnEscape(true) // Whether the dialog can be closed with escape key.
        .includeViewParams(true)
        .headerElement("customheader")
        .build();
     PrimeFaces.current().dialog().openDynamic("dialogFlight.xhtml", options, null); 
        LOG.debug("exiting DialogController showFlight");
}  
   public void showMatchplayClassment(String c){
 //       LOG.debug("entering showClubDetail");
 //       LOG.debug("withclub = " + c);
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("width", 900);
    options.put("height", 1800);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("closable", true); // closed by a button
    options.put("includeViewParams", true); 
    
    //public abstract void openDynamic(String outcome, Map<String,Object> options, Map<String,List<String>> params);
    Map<String, List<String>> params = new HashMap<>(); 
    List<String> values = new ArrayList<>(); 
 //   values.add(Integer.toString(c.getIdclub())); 
    params.put("IdClub", values);
    PrimeFaces.current().dialog().openDynamic("dialog_matchplay_classment.xhtml", options, params);
        LOG.debug("dialog_matchplay_classment.xhtml is opened !");
}
 // new 21-04-2019 - coming from courseC
    public void showClubDetail(Club c){
 //       LOG.debug("entering showClubDetail");
 //       LOG.debug("withclub = " + c);
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("width", 900);
    options.put("height", 800);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("closable", true); // closed by a button
    options.put("includeViewParams", true); 
    
    //public abstract void openDynamic(String outcome, Map<String,Object> options, Map<String,List<String>> params);
    Map<String, List<String>> params = new HashMap<>(); 
    List<String> values = new ArrayList<>(); 
    values.add(Integer.toString(c.getIdclub())); 
    params.put("IdClub", values);
    PrimeFaces.current().dialog().openDynamic("dialogClubDetail.xhtml", options, params);
        LOG.debug("dialogClubDetail.xhtml is opened !");
} //end 
    
 public static void showSelectClub(String param){
       LOG.debug("entering DialogController showSelectClub");
       LOG.debug("with param = " + param); 
    DialogFrameworkOptions options = DialogFrameworkOptions.builder()
        .draggable(false)
        .modal(true)
        .height("70%") 
        .width("50%")
        .contentHeight("100%")
        .contentWidth("100%")
        .closeOnEscape(true) // Whether the dialog can be closed with escape key.
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
  //  PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage
   ///                   (FacesMessage.SEVERITY_INFO, "Starting ...", "Club selection = " + param));
    PrimeFaces.current().dialog().openDynamic("dialogClub.xhtml?faces-redirect=true", options, params); // mod 04-06-2024
 //   NO NO !PrimeFaces.current().dialog().openDynamic("dialogClub.xhtml?faces-redirect=true", options, params); // mod 04-06-2024
 //       LOG.debug("exiting DialogController showSelectClub");
} // end showSelectClub

  public static void showSelectCourse(String from, String clubid) throws IOException{
      // from in not used !!
              LOG.debug("entering DialogController showSelectCourse");
              LOG.debug("with param clubid = " + clubid);
            if(clubid == null){
                 String msg = "Please first select a club !" ; 
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
            }
    DialogFrameworkOptions options = DialogFrameworkOptions.builder()
        .draggable(true)
        .modal(true)
        .height("70%") 
        .width("50%")
        .contentHeight("100%")
        .contentWidth("100%")
        .closeOnEscape(true) // Whether the dialog can be closed with escape key.
        .includeViewParams(true)
        .headerElement("customheader")
        .closable(true)
        .resizable(true)
        .build();
    Map<String, List<String>> params = new HashMap<>();
    List<String> values = new ArrayList<>(); 
    values.add(clubid);
    params.put("clubId", values);
 //   PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage
 //                     (FacesMessage.SEVERITY_INFO, "Starting ... ", "Course Selection for clubid = " + clubid));
    PrimeFaces.current().dialog().openDynamic("dialogCourse.xhtml", options, params);
}    
 
    public static void showSelectPlayer(String p){
        // https://primefaces.github.io/primefaces/7_0/#/core/dialogframework
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
 //   options.put("contentHeight", 2440);
  //  options.put("contentWidth", 740);  //default
    options.put("closable", true); // in case of bug is useful
    options.put("modal", true);
    options.put("headerElement", "clubName"); // Client id of the element to display inside header.
    options.put("dynamic", true);
    
    // new 30-09-2020
    Map<String, List<String>> params = new HashMap<>(); 
    List<String> values = new ArrayList<>(); 
    values.add(p); 
    params.put("param_player", values);
    PrimeFaces.current().dialog().openDynamic("dialogPlayers.xhtml", options, params); 
        LOG.debug("exiting DialogController showSelectPlayer");
}  
    
    public static void showWeather(){
       LOG.debug("entering DialogWeather");
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", true);
    options.put("resizable", true);
    options.put("width", 900);
    options.put("height", 600);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("closable", true); // in case of bug is useful
    options.put("modal", true);
    options.put("headerElement", "customheader");
 //   options.put("header", "header by LC");
    PrimeFaces.current().dialog().openDynamic("dialogWeather.xhtml", options, null); 
        LOG.debug("exiting DialogController showWeather");
}  
// new 05/10/2024
 public static void showSelectRound(String param){
       LOG.debug("entering DialogController showSelectRound");
       LOG.debug("with param = " + param); 
    DialogFrameworkOptions options = DialogFrameworkOptions.builder()
        .draggable(false)
        .modal(true)
        .height("70%") 
        .width("50%")
        .contentHeight("100%")
        .contentWidth("100%")
        .closeOnEscape(true) // Whether the dialog can be closed with escape key.
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
  //  PrimeFaces.current().dialog().showMessageDynamic(new FacesMessage
  //                    (FacesMessage.SEVERITY_INFO, "Starting ...", "Round selection = " + param));
    PrimeFaces.current().dialog().openDynamic("dialogRound.xhtml", options, params); // mod 04-06-2024
} // end showSelectRound
  
  public static void showRound(){
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("contentHeight", 640);
    options.put("contentWidth", 840);
    options.put("closable", true); 
    options.put("header", "header by LC"); // new 30/08/2014 correct ?
    PrimeFaces.current().dialog().openDynamic("dialog_played_rounds.xhtml", options, null); 
}  

  public void onFlightChosen(SelectEvent<Object> event){ 
      // n'est pas utilisé ?? à vérifier 
        LOG.debug("entering onFlightChosen() !");
        LOG.debug("entering onFlightChosen() with source = " + event.getSource() );
    Flight flight = (Flight) event.getObject(); 
        String msg = "Dialog return with flight : " + flight.toString();
        LOG.debug("msg");
        LOG.debug("Workhour at this moment = " );
        PrimeFaces.current().ajax().update("form_round:idworkhour");
        LCUtil.showMessageInfo(msg);
} 

 // new 21-11-2020 
  public void handleReturnCourse(SelectEvent<Object> event) throws IOException{  // called in competition_create_description.xhtml
      LOG.debug("handleReturn called !!!");
      LOG.debug("event getObject = " + event.getObject());
  //  utils.LCUtil.showDialogInfo("Selection Course is a success");
   //  closeDialog("dialogCourse.xhtml"); ne fonctionne pas !!
}
  
  public static void onDialogReturn(SelectEvent<Object> event){
      LOG.debug("entering onDialogReturn !");
      LOG.debug("entering onDialogReturn with event = " + event.getSource() );
        Object rating = event.getObject(); 
        String msg = "Dialog return with rating : " + rating.toString();
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
} 

public static boolean closeDialog(Object obj) throws IOException{  // static added 16-02-2024
 try{ 
     LOG.debug("entering closeDialog with : " + obj.toString() );
      // https://github.com/primefaces/primefaces/blob/master/src/main/java/org/primefaces/context/RequestContext.java
  //    if (obj != null){
          PrimeFaces.current().dialog().closeDynamic(obj);
          String msg = "Dialog closed for = " + obj.toString();
          LOG.debug(msg);
          return true;
   //   }else{
   //       LOG.debug("obj = null");
  //        return false;
   //   }
      
 //     String msg = "Dialog closed for = " + obj.toString();
 //       LOG.debug(msg);
 // mod 04-06-2024     return true;
  } catch (Exception ex) {
            String msg = "Exception in closeDialog " + ex;
            LOG.error(msg);
        //   showMessageFatal(msg);
            return false;
}
}
/*public void onDialog2Return(SelectEvent event) {
    String msg = "Dialog 2 closed !!!! " + event.toString();
    LOG.debug(msg);
    LCUtil.showMessageInfo(msg);
    }
*/
} //end class