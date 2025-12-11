
package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Cotisation;
import entite.Creditcard;
import entite.Creditcard.etypePayment;
import entite.Greenfee;
import entite.Lesson;
import entite.Player;
import entite.Professional;
import entite.Subscription;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.apache.http.Header;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

@Named("creditcardC")
@SessionScoped
@Path("creditcardController") // link avec server python !! projet restEasy-api
//return redirect("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_accepted",code=302)
// d'où vient le /rest  ??
// Before deploying your application, you need a JAX-RS class activator, which is a class extending
// javax.ws.rs.core.Application and declaring the Path where JAX-RS Services will be available:
// in other words : sets the Web context for the REST Service, 
// le code se trouve dans dans package rest

//public class JaxRsActivator extends Application {
//}

// @Dependent // new 13-008-2025
public class CreditcardController implements Serializable{
    @Context
    private CookieManager cookieManager;
 //   private UriInfo uriInfo;
  //  @Context
  //  private HttpServletRequest request;
  //  @Context 
  //  private HttpServletResponse response;
    
 //   @Context
 //   private ResourceContext resourceContext;
    //@Inject private Creditcard creditcard;
  //  public static Creditcard creditcard;
     private static Creditcard creditcard;  // mod 19-08-2025
    

    public static Creditcard getCreditcard() {
        return creditcard;
    }

    public static void setCreditcard(Creditcard creditcard) {
        CreditcardController.creditcard = creditcard;  // mod 13-08-2025
        LOG.debug("creditcarfd setted = " + creditcard);
    }
/* old solution
public Creditcard getCC1(Creditcard cc) throws Exception {
    creditcard = new HttpController().sendPaymentServer(cc);
       LOG.debug("creditcard returned from sendGetPaymentServer = ");
   // 14-08-2025 provoque 2 payements ?
    return creditcard;  // vers CourseController
}
    */
//mod 26-10-2025
public String getCC2(Creditcard cc) throws Exception {
    creditcard = cc; // new 26-10-2025
    var v = new HttpController().sendPaymentServer(creditcard);
       LOG.debug("v returned from sendGetPaymentServer = " + v );
    return v;  // back to CourseController
}

public CreditcardController() {  
// constructor
} 
/*
@GET
@Path("payment_accepted") //  from /rest/creditcardController/payment_accepted @app.route("/contact", methods=["GET", "POST"]) de creditcardService.py
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) // @Consumes({"application/xml", "application/json"})
//@Produces({"application/xml", "application/json"})
@Consumes(MediaType.APPLICATION_JSON) // nécessaire !! et dans @app route contact il faut : response.headers['Content-type'] = 'application/json'
// @Consumes({MediaType.TEXT_PLAIN,MediaType.TEXT_HTML}) unsupported media type ??
// @Produces(MediaType.TEXT_PLAIN)
 // https://stackoverflow.com/questions/19481834/how-to-redirect-to-jsf-page-from-jax-rs-method?rq=3   
//  https://stackoverflow.com/questions/19481834/how-to-redirect-to-jsf-page-from-jax-rs-method
//public Response paymentDone(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {

public String checkSession(@CookieParam("JSESSIONID") String sessionid,
                           @HeaderParam("User-Agent") String whichBrowser,
                           @HeaderParam("From") String from,
                           @HeaderParam("X-Parachutes") String parachute, //X-Parachutes null ?
                           @CookieParam("User") String user,
                           @Context UriInfo uriInfo)
{
    try{
        LOG.debug("entering checkSession, coming from python server ");
        LOG.debug("From = " + from);
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        LOG.debug("queryparameters = " + queryParameters.toString());
    MultivaluedMap<String, String> pathParameters = uriInfo.getPathParameters();
        LOG.debug("pathparameters = " + pathParameters.toString());
    
     LOG.debug("sessionId = " + sessionid);
     LOG.debug("browser = " + whichBrowser);
     LOG.debug("parachute = " + parachute);
     LOG.debug("User = " + user);
     
    return "Sessionid is " + sessionid;  // 01-08-2025 donne sur une page séparée : Sessionid is iKCVIUa_Ij7YASQKj4lHbx1pT9BgrwcNDIyoRXhB.laptop-ihtoaibb
   } catch (Exception e) {
            String msg = "£££ Exception in checkSession" +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}
} //end 
//https://www.mastertheboss.com/jboss-frameworks/resteasy/resteasy-tutorial-part-two-web-parameters/
*/
@GET
@Path("header")
public String checkBrowser(@HeaderParam("User-Agent") String whichBrowser,@CookieParam("JSESSIONID") String sessionid) {
     LOG.debug("entering checkBrowser, coming from python server ");
     LOG.debug("browser = " + whichBrowser);
  return "Browser is " + whichBrowser;
}
/*
public String getRequestHeaders(@Context HttpHeaders hh) {
    MultivaluedMap<String, String> headerParameters = hh.getRequestHeaders();
    Map<String, Cookie> params = hh.getCookies();
    StringBuffer sb = new StringBuffer();
    for (String key : params.keySet()) {
        sb.append(key + ": " + params.get(key));
    }
    return sb.toString();
}
*/
//@Inject  private ExternalContext externalContext;
//@Context context  20-08-2025 enlevé @DefaultValue("false") car provoque crash !
private String firstPartUrl(){  // donne erreur Cannot invoke "jakarta.faces.context.FacesContext.getExternalContext()" because "utils.LCUtil.context" is null 
    return utils.LCUtil.firstPartUrl();
}

@GET
@jakarta.ws.rs.Path("payment_choice/{isbn}") // point d'entrée unique en provenance de python server !
    public jakarta.ws.rs.core.Response paymentChoice(
            @PathParam("isbn") String param,
            @Context HttpServletRequest servletRequest,
            @Context ServletContext servletContext,
            @Context jakarta.ws.rs.core.Request request,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String Currency
        ) throws IOException, WebApplicationException {
       try{    
        CacheControl cacheControl = new CacheControl(); // new 27-10-2025
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        cacheControl.setMustRevalidate(true);
            LOG.debug("entering paymentChoice, coming from payment server");
            LOG.debug("with param = " + param);
            LOG.debug("with UriInfo = " + uriInfo.getRequestUri().toString());
            LOG.debug("ServletRequest = " + servletRequest);
          // LOG.debug("with Request = " + request.toString());
            LOG.debug("Amount = " + amount);
            LOG.debug("PaymentReference = " + reference);
            LOG.debug("ServletContext getContextPath = " + servletContext.getContextPath());
            LOG.debug("ServletContext getRealPath = " + servletContext.getRealPath("WEB-INF/classes"));  // 
            String href = "http://localhost:8080" + servletContext.getContextPath(); //GolfWfly-1.0-SNAPSHOT"; // aller le chercher dans method classique ?
            String location = href + "/rest/creditcardController";
                LOG.debug("location = " + location);
            String location2 = href + "/rest/courseController";
            // résultat = h ttp://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_canceled/101 
            URI currentUri = uriInfo.getRequestUri();
                LOG.debug("currentUri = " + currentUri);
    //        LOG.debug("firstPartUrl = " + firstPartUrl()); // new 21-08-2024    
    // .status(Response.Status.MOVED_PERMANENTLY) // 301
    //return Response.seeOther(URI.create("/example/new")).build();303 redirect
    // .movedPermanently(URI.create("https://example.com/new-path"))
   // .temporaryRedirect(URI.create("https://www.google.com"))
        if(param.equals("phase1")){
                LOG.debug("handling param phase1 = " + param);
                LOG.debug(" request.getHeader Amount = " + servletRequest.getHeader("amount")); // existe pas
            return jakarta.ws.rs.core.Response
                .status(Response.Status.FOUND) // 302
              //  ("h ttp://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_canceled" + "/101"))   // fonctionne
                .location(java.net.URI.create(location + "/payment_canceled" + "/101"))
                .header("type", "payment_cancel")
             //   .cacheControl(cacheControl)   
                .build();
        } //end phase 1 
            
        if(param.equals("phase2")){
                LOG.debug("handling param phase2 = " + param);
                LOG.debug(" request.getHeader = " + servletRequest.getHeader("amount")); // existe pas
            return Response
                .status(Response.Status.FOUND) // 302
              //  ("h ttp://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_confirmed"))
                .location(java.net.URI.create(location + "/payment_confirmed"))
                 .build();
        } // end phase 2
 
        if(param.equals("phase3")){
                LOG.debug("handling param phase3 = " + param);
                LOG.debug(" request.getHeader = " + servletRequest.getHeader("amount")); // existe pas
         return Response
                .status(Response.Status.FOUND) // 302
               // .location(java.net.URI.create("h ttp://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/courseController/payment_handle" + "/101"))   // fonctionne
                .location(java.net.URI.create(location2 + "/payment_handle" + "/101"))
                .build();
        } // end phase 3

    //    return "unknown param or not yet implemented = " + param;
        return Response.status(Status.NOT_FOUND).entity("redirect from payment_choice - unknown param or not yet implemented").build();
    } catch (Exception e) {
            String msg = "£££ Exception in paymentChoice : " +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   } 
 } //end method

@GET
@jakarta.ws.rs.Path("payment_canceled/{isbn}") 
    public Response paymentCancel(
            @PathParam("isbn") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
        //    @Context HttpHeaders headers,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("type") String type, // new 25-10-2025
         //   @HeaderParam("From") String from,
            @CookieParam("Amount") String amount
    ) throws IOException, WebApplicationException {
 try{
    CacheControl cacheControl = new CacheControl(); // new 27-10-2025
    cacheControl.setMaxAge(3600); // durée du cache en secondes ici = 1 heure
    cacheControl.setNoCache(true); // le navigateur doit revalider
    cacheControl.setNoStore(true); // empêche tout stocakge
    cacheControl.setMustRevalidate(true);
    cacheControl.setPrivate(true); // cahce uniquement côté navigateur , pax proxy
        LOG.debug("CreditcardController - entering paymentCancel, coming from python server");
        LOG.debug("PathParam isbn converted to id = " + id);  // pas en avant le  13-08-2025 !
        LOG.debug("JSESSIONID = " + sessionid);
        LOG.debug("Amount = " + amount);
        LOG.debug("@HeaderParam type = " + type);
    creditcard.setCreditcardPaymentReference(null);
    creditcard.setCommunication("Payment refused by User Client");
         LOG.debug("going to /creditcard_payment_canceled.xhtml");
        LOG.debug("request.getContextPath() = " + request.getContextPath());  
    response.sendRedirect(request.getContextPath() + "/creditcard_payment_canceled.xhtml?faces-redirect=true&message=Canceled by User");
         LOG.debug("on passe ici dans payment/cancel ??");
    return Response
            .status(Status.ACCEPTED)
            .entity("Payment is canceled, louis")
            .header("Pragma","no-cache") //new 27-10-2025
            .header("Expires", "0")   // id
            .cacheControl(cacheControl)  // id
            .build(); // mod 14-08-2025  was accepted
 } catch (Exception e) {
            String msg = "£££ Exception in paymentCancel : " +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   } 
} //end method


@GET
@jakarta.ws.rs.Path("payment_confirmed")
    public Response paymentConfirmed(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context HttpHeaders headers,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String Currency
        //                   @Context UriInfo uriInfo
    ) throws IOException {
 try{
        LOG.debug(NEW_LINE);
        LOG.debug("CreditcardController - entering paymentConfirmed, coming from python server");
        LOG.debug(NEW_LINE);
        LOG.debug("JSESSIONID = " + sessionid);
        LOG.debug("headers = " + headers);
        LOG.debug("response header payment reference = " + response.getHeader("PaymentReference"));
        LOG.debug("creditcard = " + creditcard);  // est null !! !!!
        LOG.debug("browser = " + whichBrowser);
        LOG.debug("User = " + user);
        LOG.debug("Amount = " + amount);
    creditcard.setTotalPrice(Double.valueOf(amount));
    creditcard.setCreditcardPaymentReference(reference);
       LOG.debug("going to /creditcard_payment_executed.xhtml");
    response.sendRedirect(request.getContextPath() + "/creditcard_payment_executed.xhtml");
       //LOG.debug("passe ici aussi ?");
    return Response.status(Status.ACCEPTED).build();
 } catch (Exception e) {
        String msg = "£££ Exception in paymentConfirmed : " +  e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
   } 
} //end method
/*
    @Override
    public void process(HttpRequest request, HttpContext context) throws IOException {
        String message = buildRequestEntry(request, context) 
                    + buildHeadersEntry(request.getAllHeaders())
                    + buildEntityEntry(request);
        LOG.info(message);
    }
    */

    // https://docs.jboss.org/resteasy/docs/6.2.7.Final/userguide/html/ch50.html
// ne fonctionne pas !!
@GET
@jakarta.ws.rs.Path("https://localhost:5000/creditcard/")
@Produces("application/json")
public Creditcard getProductInJSON() {
try{
    //ResteasyClient client = newClient.Builder.build();
    //Client client = ClientBuilder.newClient();
    ObjectMapper om = new ObjectMapper();
    Creditcard creditcard = new Creditcard();
    creditcard.setCreditCardIdPlayer(324713);
    creditcard.setCreditCardHolder("louis collet");
    String strJson = om.writeValueAsString(creditcard);
    
            Client client = ClientBuilder.newBuilder().build();
            LOG.debug("line 00");
            WebTarget target = client.target("http://localhost:5000/creditcard/"+ strJson); // h ttp://localhost:8081/test"
            LOG.debug("line 01");
    try (Response response = target.request().get()) {
        LOG.debug("line 02");
        Creditcard value = response.readEntity(Creditcard.class);
        LOG.debug("line 03");
        // You should close connections!
    }
 //   ResteasyWebTarget webTarget = client.target(UriBuilder.fromPath(ENDPOINT));
 //   UserClient userClient = webTarget.proxy(Creditcard.class);
    //User user = userClient.getUserById("1"
		return creditcard;
  } catch (Exception e) {
            String msg = "£££ Exception in getProductInJSON " +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
   } 
}    

public Boolean handlePaymentSubscription(Creditcard c, Subscription subscription, Connection conn) { // new 31-07-2025    
try{
LOG.debug("handlePayments for subscription = " + subscription);
          LOG.debug("handlePayments Subscription with creditcard common = " + creditcard); // est null
          LOG.debug("handlePayments Subscription with creditcard input = " + c);
          LOG.debug("handlePayments Subscription with subscription input = " + subscription);
        subscription.setPaymentReference(c.getCreditcardPaymentReference());
        subscription.setIdplayer(c.getCreditCardIdPlayer());
        
        if(new Controllers.PaymentsSubscriptionController().createPayment(subscription, conn)){
               LOG.debug("after Subscription createPayment : we are OK " + subscription);
            String msg = LCUtil.prepareMessageBean("subscription.success")
                + " start date = " + subscription.getStartDate().format(ZDF_DAY)
                + " end date = " + subscription.getEndDate().format(ZDF_DAY);
            LOG.info(msg);
        //    LOG.debug("just before return 'welcome.xhtml?faces-redirect=true' ");
           return true;
          //  return "welcome.xhtml?faces-redirect=true";
         //   response.sendRedirect(request.getContextPath() + "/creditcard_payment_executed.xhtml");
            
        }else{
            String msg = "error : subscription NOT modified !!";
            LOG.error(msg);
            showMessageFatal(msg);
            //return null; // retourne d'ou il vient
            return false;
        }
  } catch (Exception e) {
            String msg = "£££ Exception in " +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } 
}

public boolean needsUpdate(Creditcard c, Player player, Connection conn) {
try{
      LOG.debug("entering needsUpdate() ");
      LOG.debug("with creditcard input = " + c);
    Creditcard creditc = new read.ReadCreditcard().read(player, conn);
  //       LOG.debug("creditcard loaded = " + creditcard);
    //  if(creditc.getCreditCardNumber() == null){
      if(creditc == null){    
                LOG.debug("CREATION : First utilisation of a creditcard for user = " + player.getPlayerLastName());
          if(new create.CreateCreditcard().create(c, conn)){
              LOG.debug("creditcard is created in DB" );
              return true;
          }else{
              LOG.debug("ERROR : creditcard NOT  created in DB" );
              return false;
          }
      }  // end first cc
     //      LOG.debug("MODIFICATION : reutilisation of a creditcard for user = " + player.getPlayerLastName());
     //      LOG.debug("creditcard and c comparaison: c = " + creditcard);
     //      LOG.debug("creditcard and c comparaison: creditcard = " + creditcard);
     //      LOG.debug("creditcard expirationdate in DB = " + creditcard.getCreditCardExpirationDate());
     //      LOG.debug("creditcard expirationdate new from screen = " + creditcard.getCreditCardExpirationDate());

    if(creditc.getCreditCardExpirationDateLdt().equals(c.getCreditCardExpirationDateLdt())
     && creditc.getCreditcardNumber().equals(c.getCreditcardNumber())
     && creditc.getCreditcardHolder().equals(c.getCreditcardHolder())
     && creditc.getCreditcardVerificationCode().equals(c.getCreditcardVerificationCode())
     && creditc.getCreditcardType().equals(c.getCreditcardType()) ) {
       LOG.debug("creditcard already registered and all data are the same - no update DB needed");
       return false;
     }else{
          LOG.debug("a modified creditcard has been introduced !");
          if(new update.ModifyCreditcard().modify(creditcard,conn)){
             String msg = "creditcard is modified in DB";
             LOG.error(msg);
             showMessageInfo(msg);
             return true;
          }else{
             LOG.error("ERROR : creditcard is NOT NOT modified in DB");
             return false;
          }       
     } // end equals
  } catch (Exception e) {
            String msg = "£££ Exception in " +  e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }            
//      return false;
} //end method

private static Creditcard prefilling(Player player, Connection conn) throws SQLException{
      Creditcard c = new read.ReadCreditcard().read(player, conn);
          LOG.debug("creditcard loaded = " + c);
      if(c.getCreditcardNumber() == null){
                LOG.debug("First utilisation of a creditcard for user = " + player.getPlayerLastName());
            c.setCreditCardHolder("first use");
      }else{
          // new 31-01-2023
            c.setCreditCardIdPlayer(player.getIdplayer()); // mod 31-01-2023 setIdplayer(Integer.MIN_VALUE);
            c.setCreditCardHolder(c.getCreditcardHolder());
            c.setCreditcardNumber(c.getCreditcardNumber());
            c.setCreditcardType(c.getCreditcardType());
            c.setCreditCardExpirationDateLdt(c.getCreditCardExpirationDateLdt());  // mod 02-10-2021
            String lastTwo = String.valueOf(c.getCreditCardExpirationDateLdt().getYear()).substring(2);
      //         LOG.debug ("lasttwo = " + lastTwo);
            String s = String.valueOf(c.getCreditCardExpirationDateLdt().getMonthValue())
                + "/" + lastTwo;    
      //         LOG.debug("s formatted Subscription= " + s);
            c.setCreditCardExpirationDateString(s);
            c.setCreditcardVerificationCode(c.getCreditcardVerificationCode());
                LOG.debug("creditcard completed with db info = " + c);
       }
 return c;
 }
 
public Creditcard completeWithGreenfee(Greenfee greenfee, Player player, Connection conn) throws SQLException{
      Creditcard creditcard = null;
 try{
         LOG.debug("starting CompleteCreditcardWithGreenfee with Greenfee" + greenfee);
      if(greenfee.getPrice() == 0){
          LOG.debug("amount ZERO no payment needed !");
          return creditcard;
      }
      creditcard = prefilling(player, conn);
          LOG.debug("creditcard preffilled with player's data = " + creditcard);
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(greenfee.getPrice());
      creditcard.setCommunication(greenfee.getCommunication());
      creditcard.setTypePayment(etypePayment.GREENFEE.toString()); // mod 30-07-2025
      creditcard.setCreditcardCurrency(greenfee.getCurrency()); // new 28-04-2025
      return creditcard;
  }catch(Exception ex){
    String msg = "Creditcardpayment Greenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  //end method  

// new 18-01-2023
 public Creditcard completeWithLesson(Professional professional, List<Lesson> lessons,
         Player player,
         Connection conn) throws SQLException{
 try{
         LOG.debug("starting CompleteCreditcardWithLesson");
         LOG.debug("with listLessons = " + lessons);
         LOG.debug("Professional = " + professional);
         LOG.debug("Player = " + player);
          Creditcard creditcard = null;
      if(professional.getProAmount() == 0){
          LOG.debug("Amount ZERO no payment Lesson needed !");
          return creditcard;
      }
      creditcard = prefilling(player, conn);
         LOG.debug("creditcard after prefilling = " + creditcard);
      creditcard.setPaymentOK(false);
   // à vérifier
      creditcard.setTotalPrice(professional.getProAmount() * lessons.size()); // prix à la leçon multiplié par nombre de leçons // provisoire à faire une 2e fois
      String s = "";
      for(Lesson lesson : lessons){
          s = s  + lesson.getEventStartDate().toString() + " / ";
          LOG.debug("lesson = lesson");
      }
      creditcard.setCommunication("Réservation Lesson : "
           //   + lesson.getEventDescription() // pas correct is null
           //   + " / " + lesson.getEventTitle() 
           //   + " / " + lesson.getEventStartDate().format(ZDF_TIME_HHmm)
            + s + " ,pro # = " + professional.getProPlayerId());
      
     // creditcard.setTypePayment("LESSON");
      creditcard.setTypePayment(etypePayment.LESSON.toString()); // mod 30-07-2025
      // erreur !!
   //   creditcard.setIdplayer(professional.getProPlayerId());
// enlevé 31-01-2023 fai dans prefilling      creditcard.setIdplayer(professional.getProPlayerId());
          LOG.debug("exiting CompleteCreditcardWithLesson with creditcard = " + creditcard);
    return creditcard;
  }catch(Exception ex){
    String msg = "Exception in CompleteCreditcardWithLesson ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  //end method 
 
 public Creditcard completeWithCotisation(Cotisation cotisation, Player player, Connection conn) throws SQLException{
 try{
         LOG.debug("starting CompleteCreditcardWithCotisation");
         LOG.debug("Cotisation = " + cotisation);
         Creditcard creditcard = null;
      if(cotisation.getPrice() == 0){
          LOG.debug("amount ZERO no payment needed !");
          return creditcard;
      }
   // creditcard = new Controllers.CreditcardController().creditcardPrefilling(player, conn);
      creditcard = prefilling(player, conn);
          LOG.debug("creditcard preffilled = " + creditcard);
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(cotisation.getPrice());
      creditcard.setCommunication(cotisation.getCommunication());
      creditcard.setTypePayment(etypePayment.COTISATION.toString()); // mod 30-07-2025
         LOG.debug("creditcard completed with Cotisation = " + creditcard);
      return creditcard;
  }catch(Exception ex){
    String msg = "Creditcardpayment Cotisation Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  //end method competeWithCotisation
 
// new 23-01-2023
 public Creditcard completeWithSubscription(Subscription subscription, Player player, Connection conn) throws SQLException{
    try{
         LOG.debug("starting completeWithSubscription");
         LOG.debug("subscription = " + subscription);
      if(subscription.getSubscriptionAmount() == 0){ // mod 22-02-2024
          LOG.debug("amount ZERO -- No payment needed !");
          return null;}
      
   //   subscription = new PaymentsSubscriptionController().completePriceAndCommunication(subscription);
      
      
      creditcard = prefilling(player, conn);
 //         LOG.debug("creditcard preffilled = " + creditcard);
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(subscription.getSubscriptionAmount()); // mod 22-02-2024
      creditcard.setCommunication(subscription.getCommunication());
      creditcard.setTypePayment(etypePayment.SUBSCRIPTION.toString()); // mod 22-02-2024
        LOG.debug("creditcard class variable = " + creditcard);
    return creditcard;
}catch(Exception ex){
    String msg = "completeWithSubscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  //end method  

// https://www.baeldung.com/cookies-java
public HttpClient getClient(){
    try{
        LOG.debug("entering getClient()");
        
        cookieManager = new CookieManager();
    // CookieManager adds the cookies to the CookieStore for every HTTP response and retrieves cookies from the CookieStore for every HTTP request.
       cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);    
        
    HttpClient httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(50))
        .followRedirects(HttpClient.Redirect.NORMAL) // or ALWAYS ?
        .cookieHandler(cookieManager)
            //.cookieHandler(CookieHandler.getDefault())
        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
        .build();
  //  LOG.debug("line 00");
  //  CookieStore cookieStore = manager.getCookieStore();
  //  List <HttpCookie> cookies = cookieStore.getCookies();
  //  for (HttpCookie cookie: cookies) {
  //        LOG.debug("CookieHandler retrieved cookie: " + cookie);
  //  }
    LOG.debug("exiting getClient()");
   return httpClient;
  }catch (Exception e){
     String msg = "fatal error in getClient() " + e ;
     LOG.debug(msg );
     return null;
}    
} // end getClient()

    public void testWebService() {
try{
    LOG.debug("entering testWebservice ");
        Client client = ClientBuilder.newClient();
        LOG.debug("line 00");
      //  WebTarget webTarget = client.target("http://localhost:8080/mavenEID-1.0/rest/smartcard/text_plain");
        WebTarget webTarget = client.target("https://localhost:5000/creditcard/");
           LOG.debug("line 01");
        Invocation.Builder invocationBuilder = webTarget
                .request(MediaType.APPLICATION_JSON);
        LOG.debug("line 02");
        Response response = invocationBuilder.get();
        LOG.debug("line 03");
        LOG.debug("testWebservice response = " + response.readEntity(String.class));
 
   } catch (Exception e) {
            String msg = "£££ Exception in Testwebservice = " + e.getMessage();
     //       cardBelgium = null;
            LOG.error(msg);
      //      return null;   
   }   
   }   
    
private static Creditcard handle200(Creditcard creditc, List<HttpCookie> listCookies){
  try{
       // LOG.debug("entering handle200 with creditcard = " + creditc);
        LOG.debug("entering handle200 with listCookies size = " + listCookies.size());    
            String s = "PaymentReference";
            String referencePayment = getCookieValue(listCookies, s);
            String msg = "referenceCookie = " + referencePayment; // c'est bon
            LOG.debug(msg);
            creditc.setCreditcardPaymentReference(referencePayment);
                    LOG.debug(" String reference = " + creditc.getCreditcardPaymentReference());
                   // LOG.debug("amountCookie = " + listCookies.get(0));  // was 1
           // s = "Amount";
           // String amount = getCookieValue(listCookies, s)
            //        LOG.info("amount = " + amount);
                    
//            creditc.setErrorMessage("OK");
            return creditc;
  }catch (Exception e){
     String msg = "fatal error in handle200 = " + e ;
     LOG.debug(msg );
     return null;
} 
}//end method 
/* provisoirement
private static Creditcard handle302(Creditcard creditc, List<String> listCookies){
  try{  // à modifier comme handle 200
        LOG.debug("entering handle302");
           String referenceCookie = listCookies.getFirst();
            String msg = "referenceCookie = " + referenceCookie; // c'est bon
                   LOG.debug(msg);
            String reference = getCookie(referenceCookie);
            creditc.setPaymentReference(reference);
                    LOG.debug(" String reference = " + creditc.getPaymentReference());
                    LOG.debug("amountCookie = " + listCookies.get(1));
            String amount = getCookie(listCookies.get(1));  //voir dans 
                    LOG.info("amount = " + amount);
            creditc.setErrorMessage("OK");
            return creditc;
  }catch (Exception e){
     String msg = "fatal error in handle302 = " + e ;
     LOG.debug(msg );
     return null;
} 
}//end method 
*/
 public static String getCookieValue(List<HttpCookie> listCookies, String name){
  try{
      LOG.debug("entering getCookie with listCookies = " + listCookies.toString());
        for (HttpCookie cookie: listCookies) {
       //   LOG.debug("getCookie retrieved cookie : " + cookie);
          if (name.equals(cookie.getName())){
              LOG.debug("for cookie = " + name + " , value is : " + cookie.getValue());
              return cookie.getValue();
          }
        } // end for
           return("cookie name not found");

  }catch (Exception e){
     String msg = "fatal error in getCookieValue = " + e ;
     LOG.debug(msg );
     return "not found";
} 
 }//end method 

/*
 public static String getCookie(String header){
  try{
      LOG.debug("entering getCookie with header = " + header.toString());
       int firstIndex = header.indexOf("="); 
    //       LOG.debug("firstIndex = " + firstIndex);
       if(firstIndex == -1){
           LOG.debug("firstIndex not found");
           return("not found");
       }
       int lastIndex = header.indexOf(";");
       if(lastIndex == -1){
           LOG.debug("lastIndex not found");
           return("not found");
       }
   //        LOG.debug("lastIndex = " + lastIndex);
    //   LOG.debug("substring  = " + 
       return header.substring(firstIndex +1, lastIndex);
  }catch (Exception e){
     String msg = "fatal error in getCookie = " + e ;
     LOG.debug(msg );
     return "not found";
} 
 }//end method 
*/
 /**
     * Get the currencies code from the available locales information.
     *
     * @return a map of currencies code.
     */
    private Map<String, String> getAvailableCurrencies() {
        Locale[] locales = Locale.getAvailableLocales();

        // We use TreeMap so that the order of the data in the map sorted
        // based on the country name.
        Map<String, String> currencies = new TreeMap<>();
        for (Locale locale : locales) {
            try {
                currencies.put(locale.getDisplayCountry(),
                    Currency.getInstance(locale).getCurrencyCode());
            } catch (Exception e) {
                // when the locale is not supported
            }
        }
        return currencies;
    }
 
 /*https://sentry.io/answers/how-to-solve-pkix-path-building-failed-error-in-java/
 * Configures SSL to trust all certificates and bypass hostname verification.
 * This allows the application to connect to servers with untrusted or self-signed certificates.
 * WARNING: This approach is insecure for production use.
 
private static void configureTrustAllSSL() throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing - trust all clients
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing - trust all servers
            }
        }
    };

    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
}

 */
private boolean loadKeyStore() throws FileNotFoundException, KeyStoreException, NoSuchAlgorithmException {
  try{
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); // JDK24 est pkcs12
      LOG.debug("keyStore instanciated ");
      LOG.debug("keystore Type = " + keyStore.getType());
      LOG.debug("keystore loaded, provider = " + keyStore.getProvider());
  //  keystore.load(getClass().getResourceAsStream(KEYSTORE_FILE), KEYSTORE_PWD.toCharArray());
        String relativeCacertsPath = "/lib/security/cacerts".replace("/", File.separator); // extension added 01-05-2025
    String filename = System.getProperty("java.home") + relativeCacertsPath;
       LOG.debug("cacerts dir = " + filename);
  //The default password for this KeyStore is “changeit”, but it could be different if it was previously changed in our system
    keyStore.load(new FileInputStream(filename),("changeit").toCharArray());
         Date date = keyStore.getCreationDate("GolfLCCreditcardCertificate");
      LOG.debug("creation date = " + date);
/*
   PKIXParameters params = new PKIXParameters(keyStore);
   Set<TrustAnchor> trustAnchors = params.getTrustAnchors(); // A TrustAnchor instance simply represents a trusted certificate.
   List<Certificate> certificates = trustAnchors.stream()
      .map(TrustAnchor::getTrustedCert)
      .collect(Collectors.toList());
   certificates.forEach(item -> LOG.debug("list of certificates =" + item.toString()));  // java 8 lambda
   LOG.debug("there are certificates = " + certificates.size());

   TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init((KeyStore) null);

    List<TrustManager> trustManagers = Arrays.asList(trustManagerFactory.getTrustManagers());
    List<X509Certificate> certificatesX509 = trustManagers.stream()
      .filter(X509TrustManager.class::isInstance)
      .map(X509TrustManager.class::cast)
      .map(trustManager -> Arrays.asList(trustManager.getAcceptedIssuers()))
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  // certificatesX509.forEach(item -> LOG.debug("list of certificatesX59 =" + item.toString()));
    LOG.debug("there are certificates X59= " + certificatesX509.size());
   */
    Enumeration<String> aliasEnumeration = keyStore.aliases();
    List<String> aliases = Collections.list(aliasEnumeration);
 //    aliases.forEach(item -> LOG.debug("list of aliases = " + item));
    LOG.debug("there are aliases = " + aliases.size());
    LOG.debug("contains alias GolfLCCreditcardCertificate ? " + keyStore.containsAlias("GolfLCCreditcardCertificate"));
  //  KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(pwdArray);
    LOG.debug("getEntry GolfLCCreditcardCertificate = " + keyStore.getEntry("GolfLCCreditcardCertificate", null));  // 2e param = password si ...
    LOG.debug("isKeyEntry = " + keyStore.isKeyEntry("GolfLCCreditcardCertificate"));
  // var v = keyStore.getEntry("GolfLCCreditcardCertificate", null);
  //LOG.debug("attributes are = " + v.getAttributes().toString());
    LOG.debug("getCertificate ? " + keyStore.getCertificate("GolfLCCreditcardCertificate"));
  //  KeyStore readKeyStore() throws Exception {
   // KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
   // keystore.load(getClass().getResourceAsStream(KEYSTORE_FILE), KEYSTORE_PWD.toCharArray());
   // return keystore;
//}
    return true;
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in loadKeyStore = " + e.getMessage();
            LOG.error(msg);
            return false;
   }finally{
   }
} // end method
 
public static void main(String args[])throws SQLException, Exception{     
   //  Connection conn = new DBConnection().getConnection();
  try{
 //  var v = new CreditcardController().loadKeyStore(); ionvalidé provisoirement
  // LOG.debug("result loadKeysStore = " + v);
    Creditcard c = new Creditcard();
    c.setCreditCardHolder("LOUIS_COLLET");
    c.setCreditCardIdPlayer(324713); // mod 31-01-2023
    c.setCommunication("creditcardController using Java11HttpClientExample / RestEasy");
    //  LOG.debug("testLC = " + ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)); 
  //   String ldString = LocalDate.now().plusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // toujours valide !!
    c.setCreditCardExpirationDateLdt(LocalDateTime.now().plusMonths(2));
       LOG.debug("contenu CreditCardExpirationDate = " + c.getCreditCardExpirationDateLdt());
    c.setCreditcardNumber("6011000180331112");
    c.setTotalPrice(135.0);
    c.setTypePayment("LESSON");
    c.setCreditcardType("DISCOVER"); 
    c.setCreditcardCurrency("EUR"); // new 29-04-2025
     LOG.debug("contenu CreditCardCurrency = " + c.getCreditcardCurrency());
    c.setCreditcardVerificationCode((short)567);
       LOG.debug("with creditcard = " + c);
    String s = new HttpController().sendPaymentServer(c);
  //  LOG.debug("from main, after execution errormessage = " + s); //.getErrorMessage());
    
        LOG.debug("from main, after execution = " + s);
/*    Map<String, String> currencies = newCC.getAvailableCurrencies();
        for (String country : currencies.keySet()) {
            String currencyCode = currencies.get(country);
            LOG.debug(country + " => " + currencyCode);
        }

        Locale locale = Locale.getDefault();  //US
        locale = Locale.of("fr","BE");
        LOG.debug("locale currency = " + Currency.getInstance(locale)); // USD
        LOG.debug("locale symbol = " + Currency.getInstance(locale).getSymbol()); // USD
        LOG.debug("currency code = " + Currency.getInstance(locale).getCurrencyCode()); // USD
        LOG.debug("display name = " + Currency.getInstance(locale).getDisplayName()); // USD
        LOG.debug("display name locale = " + Currency.getInstance(locale).getDisplayName(locale)); // USD
*/        
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
      //   DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
}  //end class