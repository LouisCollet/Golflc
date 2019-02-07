package lc.golfnew;

import entite.Flight;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import utils.LCUtil;
/**
 *http://www.javacodegeeks.com/2015/01/primefaces-opening-external-pages-in-dynamically-generated-dialog.html
 * http://www.journaldev.com/4056/primefaces-utilities-requestcontext-el-functions-dialog-framework-search-expression-framework
 */
@Named("dialogC")
@SessionScoped
public class DialogController implements Serializable, interfaces.Log
{
  public static void viewHandicap()
  {
        LOG.info("entering viewHandicap");
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("contentHeight", 320);
    options.put("contentWidth", 640);  //default
    options.put("closable", true); // closed by a button
    options.put("includeViewParams", true); 
    
 //   String bookName = "Grapes of wrath"; // exemple fictif
///    Map<String, List<String>> params = new HashMap<>();
///    List<String> values = new ArrayList<>();
///    values.add("Grapes of wrath");
///    params.put("param1", values); 
  //  RequestContext rc = RequestContext.getCurrentInstance();
//   RequestContext.getCurrentInstance().openDialog("dialogHandicap.xhtml", options, null); //deprecated mod 08/02/2018
    PrimeFaces.current().dialog().openDynamic("dialogHandicap.xhtml", options, null); 
        LOG.info("dialogHandicap.xhtml is opened !");
}
    public static void showFlight(){
       LOG.info("entering DialogController viewFlight");
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", true);
    options.put("resizable", true);
    options.put("width", 500);
    options.put("height", 600);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
 //   options.put("contentHeight", 2440);
  //  options.put("contentWidth", 740);  //default
    options.put("closable", true); // in case of bug is useful
    options.put("modal", true);
    options.put("headerElement", "customheader");
 //   options.put("header", "header by LC");
    PrimeFaces.current().dialog().openDynamic("dialogFlight.xhtml", options, null); 
        LOG.info("exiting DialogController showFlight");
}  
    public static void showSelectHomeClub(){
       LOG.info("entering DialogController showSelectHomeClub");
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
    options.put("headerElement", "customheader");
 //   options.put("header", "header by LC");
    PrimeFaces.current().dialog().openDynamic("dialogHomeClub.xhtml", options, null); 
        LOG.info("exiting DialogController showSelectHomeClub");
}  
    public static void showWeather(){
       LOG.info("entering DialogWeather");
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
        LOG.info("exiting DialogController showWeather");
}  

  
  public static void viewRound()
  {
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("contentHeight", 320);
    options.put("contentWidth", 640);
    options.put("closable", true); 
    options.put("header", "header by LC"); // new 30/08/2014 correct ?
//    RequestContext.getCurrentInstance().openDialog("dialogPlayedRounds.xhtml", options, null); // deprecated changed 08/02/2018
    PrimeFaces.current().dialog().openDynamic("dialogPlayedRounds.xhtml", options, null); 
}  

  public void onFlightChosen(SelectEvent event){ 
      // n'est paz utilisé ?? à vérifier 
        LOG.info("entering onFlightChosen() !");
        LOG.info("entering onFlightChosen() with source = " + event.getSource() );
    Flight flight = (Flight) event.getObject(); 
        String msg = "Dialog return with flight : " + flight.toString();
        LOG.info("msg");
        LOG.info("Workhour at this moment = " );
        PrimeFaces.current().ajax().update("form_round:idworkhour");
        LCUtil.showMessageInfo(msg);
} 

  public static void onDialogReturn(SelectEvent event) 
{  LOG.info("entering onDialogReturn !");
    LOG.info("entering onDialogReturn with source = " + event.getSource() );
        Object rating = event.getObject(); 
        String msg = "Dialog return with rating : " + rating.toString();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
         

} 

@Inject
private FacesContext facesContext;
@Inject
private UIViewRoot viewRoot;

public void closeDialog(Object obj) throws IOException
    { // if(obj == null
        LOG.info("entering closeDialog with : " + obj.toString() );
//      RequestContext.getCurrentInstance().closeDialog("LC - closeDialog");  // deprecated 08/02/2018
      // https://github.com/primefaces/primefaces/blob/master/src/main/java/org/primefaces/context/RequestContext.java
      PrimeFaces.current().dialog().closeDynamic(obj); 
      
 //   Faces.refresh();
 //new 22-10-2018
  //  FacesContext context = FacesContext.getCurrentInstance();
  
    String refreshpage = facesContext.getViewRoot().getViewId();
    ViewHandler handler = facesContext.getApplication().getViewHandler();
   // UIViewRoot root = handler.createView(facesContext, refreshpage);
    viewRoot = handler.createView(facesContext, refreshpage);
    viewRoot.setViewId(refreshpage);
    facesContext.setViewRoot(viewRoot);
 //      PrimeFaces.current().ajax().update("form_round:idworkhour"); // new 20-11-2018
      String msg = "Dialog closed for = " + obj.toString();
        LOG.info(msg);
  //      LCUtil.showMessageInfo(msg);
    }

public static void closeDialog2(Object obj) throws IOException{ // if(obj == null
        LOG.info("entering closeDialog with : " + obj.toString() );
//      RequestContext.getCurrentInstance().closeDialog("LC - closeDialog");  // deprecated 08/02/2018
      // https://github.com/primefaces/primefaces/blob/master/src/main/java/org/primefaces/context/RequestContext.java
      PrimeFaces.current().dialog().closeDynamic(obj); 
}

  } //end class