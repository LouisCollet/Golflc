package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.google.api.client.util.Base64;
import entite.Creditcard;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import io.mikael.urlbuilder.UrlBuilder;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.io.Serializable;
import static java.lang.System.out;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static utils.LCUtil.showMessageFatal;
import io.mikael.urlbuilder.UrlBuilder;
import static interfaces.ConsoleColors.RED_BOLD;
import static interfaces.ConsoleColors.RESET;
import static interfaces.GolfInterface.ZDF_YEAR_MONTH_DAY;

@Named("httpC") // utile ?
@SessionScoped   // utile ?

@Path("httpController") //14-08-2025 was creditcardController

//return redirect("http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_accepted",code=302)
// d'où vient le /rest  ??
// Before deploying your application, you need a JAX-RS class activator, which is a class extending
//  and declaring the Path where JAX-RS Services will be available:
// in other words : sets the Web context for the REST Service, 
// le code se trouve dans dans package rest

//public class JaxRsActivator extends Application {
//}

public class HttpController implements Serializable{
    //@Context
    //private CookieManager cookieManager;
   // private UriInfo uriInfo;
    private CookieManager cookieManager = null;
    private CookieStore   cookieStore = null;
    private HttpClient httpClient = null;
  //  @Inject  // enlevé LC 24-02-2026
    private Creditcard creditcard;

    public Creditcard getCreditcard() {
        return creditcard;
    }

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    }

public HttpController() {  // constructor
  cookieManager = new CookieManager(); 
  cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    LOG.debug("new cookieManager in constructor");
  cookieStore = cookieManager.getCookieStore();  // abstract cannot be instanciated
    LOG.debug("cookieStore created in constructor");
  httpClient = getHttpClient();  // immutable 
    LOG.debug("new httpClient in constructor");
  
} 
/* fonctionne pas
public String goBack() { // You can retrieve the previous page's URL using the Referer header from the HttpServletRequest.
    LOG.debug("entering goBack");
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    String referer = request.getHeader(HttpHeaders.REFERER); // request.getHeader(HttpHeaders.REFERER) was "referer"
    LOG.debug("referer = " + referer);
    return "redirect:" + referer + "?faces-redirect=true";
}
*/
/*
@GET
@Path("payment_accepted") //  from /rest/creditcardController/payment_accepted @app.route("/contact", methods=["GET", "POST"]) de creditcardService.py
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // nécessaire !! et dans @app route contact il faut : response.headers['Content-type'] = 'application/json'
// @Consumes({MediaType.TEXT_PLAIN,MediaType.TEXT_HTML}) unsupported media type ??
// @Produces(MediaType.TEXT_PLAIN)
 // https://stackoverflow.com/questions/19481834/how-to-redirect-to-jsf-page-from-jax-rs-method?rq=3   
//  https://stackoverflow.com/questions/19481834/how-to-redirect-to-jsf-page-from-jax-rs-method

// https://www.baeldung.com/cookies-java

*/

//public final HttpClient getHttpClient(){ // mod 10-08-2025
  public final HttpClient getHttpClient(){ // remis 10-08-2025 ??
    try{
        LOG.debug("entering getHttpClient()"); // voir initialisations dans constructor !!
     //Once created, an HttpClient instance is immutable, thus automatically thread-safe, and you can send multiple requests with it.   
    // CookieManager adds the cookies to the CookieStore for every HTTP response and retrieves cookies from the CookieStore for every HTTP request.

    
  // skipper SSL control Alternatively, we can programmatically set this property before creating our client:
//Properties props = System.getProperties();
//props.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());

    HttpClient httpClient2 = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1) // If you know in advance that the server only speaks HTTP/1.1,
                    //you may create the client with version(Version.HTTP_1_1).
        .connectTimeout(Duration.ofSeconds(50)) // if the connection can't be established, the client throws a HttpConnectTimeoutException exception.
        .followRedirects(HttpClient.Redirect.NORMAL) // or ALWAYS ?CookieHandler.setDefault(cm);
        .cookieHandler(cookieManager)  // instanciated in constructor
        .build();
    LOG.debug("exiting getHttpClient()");
   return httpClient2;
  }catch (Exception e){
     String msg = "fatal error in getHttpClient() " + e ;
     LOG.debug(msg );
     return null;
}    
} // end getClient()
    

/*@Inject  private ExternalContext externalContext;
public Creditcard sendPaymentServer(Creditcard creditc) throws Exception {
    HttpResponse<String> response = null;
try{
       LOG.debug("entering sendGetPaymentServer()");
       LOG.debug("with creditcard = " + creditc);
/// bien au bon endroit ?
    creditc.setCreditCardExpirationDate(creditc.getCreditCardExpirationDateLdt().format(ZDF_YEAR_MONTH_DAY)); // "yyyy-MM-dd"
    LOG.debug("contenu Expiration date formatted = " + creditc.getCreditCardExpirationDate());
 
 //   String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
    
    /*   
    Path path = Paths.get("cert.pem");
X509Certificate cert;
try (Reader reader = Files.newBufferedReader(path)) {
    cert = PemReader.readCertificate(reader);
}
System.out.println(cert.getSubjectX500Principal());
    
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule()); 
    om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
    String strJson = om.writeValueAsString(creditc);
       LOG.debug("creditcard data converted in json format = " + NEW_LINE + strJson);
    URI uri = UrlBuilder.empty()
        .withScheme("https")
        .withHost("localhost")
        .withPort(5000) // default Flask
        .withPath("creditcard/")
        .toUri();
        LOG.debug("uri from urlbuilder = " + uri.toString());
 //   String href = utils.LCUtil.firstPartUrl();
 //       LOG.debug("href from firstpart = " + href);
 //       LOG.debug("externalContext = " + FacesContext.getCurrentInstance().getExternalContext());
   
    HttpRequest request = HttpRequest.newBuilder()
                .GET() // default
              //  .uri(URI.create("https://localhost:5000/creditcard/" + URLEncoder.encode(strJson,"utf-8")))  // 5000 = default Flask
                .uri(URI.create(uri + URLEncoder.encode(strJson,"utf-8")))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1) // default HTTP_2  Flask ne supporte que 1_1
                .timeout(Duration.ofSeconds(5)) // time out si redis server not loaded
              //  .header("key1", "value1") // on récupère le paramètre dans python  request.headers.get('key1')
              //  .header("key2", "value2")
               // .header("returnDirectory", "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/") 
                .header("ReturnDirectory",  utils.LCUtil.firstPartUrl() + "/rest/creditcardController/")
                .header("MerchantSite", "GolfLC Merchant Site") // new 20-10-2025
                .build();
     //   https://stackoverflow.com/questions/17493027/can-i-open-a-new-window-and-populate-it-with-a-string-variable
//     LOG.debug("just before send HttpRequest");
        LOG.debug("request sended = " + request.toString());
        request.headers().map().forEach((k, v) -> LOG.debug("request headers map() are : " + k + ":" + v)); // ne donne que le premier !!
        LOG.debug("Request headers toString are " + request.headers().toString()); //contains only user added headers/cookies
    try{
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); // send blocks the calling thread until the response is available.
    }catch (ConnectException e){
        String msg = "ConnectException - Amazone Payment Inc Server not available ! - Ask the Administrator to run the creditCardService.py";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
    //    creditc.setErrorMessage(msg);
        showMessageFatal(msg);
        return creditc;
    }catch (HttpTimeoutException e){
        String msg = "After response HttpTimeoutException : Redis Server not available (request timed out) ! - Ask the Administrator to start it : wsl sudo systemctl start redis";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
    //    creditc.setErrorMessage(msg);
        showMessageFatal(msg);
        return creditc; 
    }
        LOG.debug("we have a valid HttpResponse = ");
        printResponse(response);
        LOG.debug("response.statusCode() = " + response.statusCode());
        LOG.debug("response uri = " + response.uri());
 /*       
    if(response.statusCode() == 500){  // n'arrive jamais ? 500 = redis non actif ??
            String msg = "Redis Server not started (statusCode = " + response.statusCode();
            out.println(RED_BOLD + msg + RESET); // fonctionne pas toujours !!d'abord run de l'application ?
            LOG.error(msg);
            creditc.setErrorMessage(msg);
            showMessageFatal(msg);
            return creditc;
    }
    ///  si code 302 key = location :  - value = http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_accepted
  
    if(response.statusCode() != 200){
            LOG.debug("we have an error code = " + response.statusCode());
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object jsonObject = mapper.readValue(response.body(), Object.class);
        String msg = "we have an error " + mapper.writeValueAsString(jsonObject);
        LOG.error(msg);
    //    creditc.setErrorMessage(msg);
        showMessageFatal(msg);
        return creditc;
    }
    if(response.statusCode() == 200){
            LOG.info("handling statusCode = 200");
            LOG.debug("response uri=  " + response.uri());
            LOG.debug("response sslSession isPresent :  " + response.sslSession().isPresent());
         //   LOG.debug("response sslSession =  " + response.sslSession().toString());
        response.headers().map().forEach((k, v) -> LOG.debug("response headers are : " + k + ":" + v));
          //  LOG.debug("response.headers()PaymentReference = " + response.headers().firstValue("PaymentReference").get());
         //  LOG.debug("response.headers()Currency = " + response.headers().firstValue("Currency").get());
            LOG.debug("response.headers()allValues = " + response.headers().allValues("Currency").getFirst());
            LOG.debug("response.headers()set-cookie getfirst= " + response.headers().allValues("set-cookie").getFirst());  // Amount=135.0; Path=/
            LOG.debug("response.headers()set-cookie getLast = " + response.headers().map().get("set-cookie").getLast());
            LOG.debug("response.headers()set-cookie get(2) = " + response.headers().map().get("set-cookie").get(2));
        String s2 = getCookie(response.headers().map().get("set-cookie").get(2));
           LOG.debug("string s2 = " + s2);
        for (String value : response.headers().allValues("Currency")) {
            LOG.debug("value = " + value);
        }
        String firstValue = response.headers().firstValue("Currency").orElse("");  // no result !
            LOG.debug("firstValue = ", firstValue);
            LOG.debug("response version =  " + response.version());
        List <HttpCookie> listCookies = cookieStore.getCookies();
            LOG.debug("cookieStore length = " + listCookies.size());
        listCookies.forEach(item -> LOG.debug("cookie Store 2 - cookies are : " + item));
        for (HttpCookie cookie: listCookies) {
          LOG.debug("CookieStore retrieved cookie: " + cookie.getName() + " / " + cookie.getValue() + " / " + cookie.toString());
        }
        List<URI> listUris = cookieStore.getURIs();
        listUris.forEach(item -> LOG.debug("URIs are : " + item));
    // important
        creditc = handle200(creditc, listCookies);   // important !!
      LOG.debug("before going to 5000/about");
// magic sinon s'affiche en interne ...
       CreditcardController.setCreditcard(creditcard);  // new 24-10-2025
      
   //   return jakarta.ws.rs.core.Response
    //            .status(jakarta.ws.rs.core.Response.Status.FOUND) // 302
     //           .location(java.net.URI.create("https://localhost:5000/payment_generator"))
      //          .build();

/// non response.sendRedirect("https://localhost:5000/payment_generator");

      URL url = new URI("https://localhost:5000/about").toURL();
      Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()});
 
 //   long kilobytes = Runtime.getRuntime().freeMemory() / 1024;
 //   long megabytes = kilobytes / 1024;
 //   long gigabytes = megabytes / 1024;
 //   LOG.debug("freememory KB : " + kilobytes + "/ mega : " + megabytes + "/ giga : " + gigabytes);
       }  // end response 200
return creditc;
} catch(HttpConnectTimeoutException to){
    String msg = "HttpConnectTimeoutException sendGetPaymentServer = " + to.getMessage() + " ,responnse = " + response;
    LOG.error(msg);
//    creditc.setErrorMessage(msg);
    showMessageFatal(msg);
    return null;
}  catch(Exception e) {
    if(e.getMessage() == null){
        String msg= "final catch Python Server not available ! - Ask the Administrator to run the creditCardService.py on the python server";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
//        creditc.setErrorMessage(msg);
        showMessageFatal(msg);
        return creditc;
    }
    if("request timed out".equals(e.getMessage())){
        String msg= "final catch Exception : Redis Server not available (request timed out) ! - Ask the Administrator to start it : wsl Sudo Systemctl start redis";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
//        creditc.setErrorMessage(msg);
        showMessageFatal(msg);
        return creditc;   
    }
    String msg = "Exception in sendGetPaymentServer = " + e.getMessage() + " ,response = " + response;
    LOG.error(msg);
//    creditc.setErrorMessage(msg);
    showMessageFatal(msg);
    return creditc;
}
} //end sendGetPaymentServer
*/
//@Inject  private ExternalContext externalContext;//jakarta.ws.rs.core.Response
public String sendPaymentServer(Creditcard creditc) throws Exception { // creditcardController public String getCC2(Creditcard cc)
    HttpResponse<String> response = null;
try{
       LOG.debug("entering sendGetPaymentServer()");
       LOG.debug("with creditcard = " + creditc);
/// bien au bon endroit ?
    creditc.setCreditCardExpirationDate(creditc.getCreditCardExpirationDateLdt().format(ZDF_YEAR_MONTH_DAY)); // "yyyy-MM-dd"
    LOG.debug("contenu Expiration date formatted = " + creditc.getCreditCardExpirationDate());
 
 //   String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
    
    /*   
    Path path = Paths.get("cert.pem");
X509Certificate cert;
try (Reader reader = Files.newBufferedReader(path)) {
    cert = PemReader.readCertificate(reader);
}
System.out.println(cert.getSubjectX500Principal());
    */
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule()); 
    om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
    String strJson = om.writeValueAsString(creditc);
       LOG.debug("creditcard data converted in json format = " + NEW_LINE + strJson);
    URI uri = UrlBuilder.empty()
        .withScheme("https")
        .withHost("localhost")
        .withPort(5000) // default Flask
        .withPath("creditcard/")
        .toUri();
        LOG.debug("uri from urlbuilder = " + uri.toString());
    HttpRequest request = HttpRequest.newBuilder()
                .GET() // default
              //  .uri(URI.create("https://localhost:5000/creditcard/" + URLEncoder.encode(strJson,"utf-8")))  // 5000 = default Flask
                .uri(URI.create(uri + URLEncoder.encode(strJson,"utf-8")))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1) // default HTTP_2  Flask ne supporte que 1_1
                .timeout(Duration.ofSeconds(5)) // time out si redis server not loaded
              //  .header("key1", "value1") // on récupère le paramètre dans python  request.headers.get('key1')
              //  .header("key2", "value2")
               // .header("returnDirectory", "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/") 
                .header("ReturnDirectory",  utils.LCUtil.firstPartUrl() + "/rest/creditcardController/")
                .header("MerchantSite", "GolfLC Merchant Site") // new 20-10-2025
                .build();
     //   https://stackoverflow.com/questions/17493027/can-i-open-a-new-window-and-populate-it-with-a-string-variable
//     LOG.debug("just before send HttpRequest");
        LOG.debug("request sended = " + request.toString());
        request.headers().map().forEach((k, v) -> LOG.debug("request headers map() are : " + k + ":" + v)); // ne donne que le premier !!
        LOG.debug("Request headers toString are " + request.headers().toString()); //contains only user added headers/cookies
    try{
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()); // send blocks the calling thread until the response is available.
    }catch (ConnectException e){
        String msg = "ConnectException - Amazone Payment Inc Server not available ! - Ask the Administrator to run the creditCardService.py";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
      //  LOG.debug(" returned in case of ConnectException = " + e.getMessage()); // null
        showMessageFatal(msg);
        return "ConnectException";
    }catch (HttpTimeoutException e){
        String msg = "Redis Server not available (request timed out) ! - Ask the Administrator to start it : wsl sudo systemctl start redis";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
        showMessageFatal(msg);
        return e.getMessage();
    }
        LOG.debug("we have a valid HttpResponse = ");
        printResponse(response);
        LOG.debug("response.statusCode() = " + response.statusCode());
        LOG.debug("response uri = " + response.uri());
 /*       
    if(response.statusCode() == 500){  // n'arrive jamais ? 500 = redis non actif ??
            String msg = "Redis Server not started (statusCode = " + response.statusCode();
            out.println(RED_BOLD + msg + RESET); // fonctionne pas toujours !!d'abord run de l'application ?
            LOG.error(msg);
            creditc.setErrorMessage(msg);
            showMessageFatal(msg);
            return creditc;
    }
    ///  si code 302 key = location :  - value = http://localhost:8080/GolfWfly-1.0-SNAPSHOT/rest/creditcardController/payment_accepted
  */
    if(response.statusCode() != 200){
           LOG.debug("we have an error code = " + response.statusCode());
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Object jsonObject = mapper.readValue(response.body(), Object.class);
        String msg = "we have an error " + mapper.writeValueAsString(jsonObject);
        LOG.error(msg);
        showMessageFatal(msg);
        return Integer.toString(response.statusCode());
    }
    if(response.statusCode() == 200){
            LOG.info("handling statusCode = 200");
            LOG.debug("response uri=  " + response.uri());
            LOG.debug("response sslSession isPresent :  " + response.sslSession().isPresent());
         //   LOG.debug("response sslSession =  " + response.sslSession().toString());
        response.headers().map().forEach((k, v) -> LOG.debug("response headers are : " + k + ":" + v));
          //  LOG.debug("response.headers()PaymentReference = " + response.headers().firstValue("PaymentReference").get());
         //  LOG.debug("response.headers()Currency = " + response.headers().firstValue("Currency").get());
            LOG.debug("response.headers()allValues = " + response.headers().allValues("Currency").getFirst());
            LOG.debug("response.headers()set-cookie getfirst= " + response.headers().allValues("set-cookie").getFirst());  // Amount=135.0; Path=/
            LOG.debug("response.headers()set-cookie getLast = " + response.headers().map().get("set-cookie").getLast());
            LOG.debug("response.headers()set-cookie get(2) = " + response.headers().map().get("set-cookie").get(2));
        String s2 = getCookie(response.headers().map().get("set-cookie").get(2));
           LOG.debug("string s2 = " + s2);
        for (String value : response.headers().allValues("Currency")) {
            LOG.debug("value = " + value);
        }
        String firstValue = response.headers().firstValue("Currency").orElse("");  // no result !
            LOG.debug("firstValue = ", firstValue);
            LOG.debug("response version =  " + response.version());
        List <HttpCookie> listCookies = cookieStore.getCookies();
            LOG.debug("cookieStore length = " + listCookies.size());
        listCookies.forEach(item -> LOG.debug("cookie Store 2 - cookies are : " + item));
        for (HttpCookie cookie: listCookies) {
          LOG.debug("CookieStore retrieved cookie: " + cookie.getName() + " / " + cookie.getValue() + " / " + cookie.toString());
        }
        List<URI> listUris = cookieStore.getURIs();
        listUris.forEach(item -> LOG.debug("URIs are : " + item));

// déplacé courseC
  // affreux hack : mais fonctionne  !!
     //   String url = new URI("https://localhost:5000/about").toURL().toString();
     //   Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
      //      LOG.debug("after rundll32 to 5000/about");
      //  return "OK"; 
        return Integer.toString(response.statusCode());
    }  // end response 200
  // return "KO2"; //autres cas
   return Integer.toString(response.statusCode());
} catch(HttpConnectTimeoutException e){
    String msg = "HttpConnectTimeoutException sendGetPaymentServer = " + e.getMessage() + " ,response = " + response;
    LOG.error(msg);
    showMessageFatal(msg);
    return e.getMessage();
}  catch(Exception e) {
    if(e.getMessage() == null){
        String msg= "final catch Python Server not available ! - Ask the Administrator to run the creditCardService.py on the python server";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
    if("request timed out".equals(e.getMessage())){
        String msg= "final catch Exception : Redis Server not available (request timed out) ! - Ask the Administrator to start it : wsl Sudo Systemctl start redis";
        out.println(RED_BOLD + msg + RESET);
        LOG.error(msg);
        showMessageFatal(msg);
        return e.getMessage();
    }
    String msg = "Exception in sendGetPaymentServer = " + e.getMessage() + " ,response = " + response;
    LOG.error(msg);
    showMessageFatal(msg);
    return e.getMessage();
}
} //end sendGetPaymentServer

/* provisoirement
private static Creditcard handle200(Creditcard creditc, List<HttpCookie> listCookies){
  try{
       // LOG.debug("entering handle200 with creditcard = " + creditc);
            LOG.debug("entering handle200 with listCookies size = " + listCookies.size());    
        String referencePayment = getCookieValue(listCookies, "PaymentReference");
        creditc.setCreditcardPaymentReference(referencePayment);
            LOG.debug(" creditc - PaymentReference = " + creditc.getCreditcardPaymentReference());
//        creditc.setErrorMessage("OK");
        return creditc;
  }catch (Exception e){
     String msg = "fatal error in handle200 = " + e ;
     LOG.debug(msg );
     return null;
} 
}//end method 

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
        return("-1");

  }catch (Exception e){
     String msg = "fatal error in getCookieValue = " + e ;
     LOG.debug(msg );
     return "not found";
} 
 }//end method 
private static void printResponse(HttpResponse<?> response) {
    System.out.println("Response:");
    System.out.println("URI     : " + response.uri());
    System.out.println("Version : " + response.version());
    System.out.println("Status  : " + response.statusCode());
    System.out.println("Headers : " + response.headers());
    System.out.println("Body    : " + response.body());
    System.out.println("=======================================");
  }

 public static String getCookie(String header){
  try{
      LOG.debug("entering getCookie with header = " + header);
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
       return header.substring(firstIndex +1, lastIndex);
  }catch (Exception e){
     String msg = "fatal error in getCookie = " + e ;
     LOG.debug(msg );
     return "not found";
} 
 }//end method 

 /*

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
*/
    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class