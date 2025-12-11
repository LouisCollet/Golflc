package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.Creditcard;
import static interfaces.Log.LOG;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import rest.Item;
import static utils.LCUtil.showMessageFatal;

@Path("tutorialJava11")
// à transférer à CreditcardController apeès tests
public class Java11HttpClientExample {

private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

@GET
@Path("item") //  /rest/tutorialJava11/item
@Produces(MediaType.APPLICATION_JSON) 
public Item getItem() {
    LOG.debug("entering item");
  Item item = new Item("computer",2500);
  return item;
}    


/* attention : au préalable il faut lancer python PycharmProjects/rest-api et faire le run de creditCardService.py avec click droit !
public boolean sendGet(Creditcard creditcard) throws Exception {
try{
       LOG.debug("starting Java11HttpClientExample.sendGet()");
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());  // traiter LocalDateTime format ?? à enlever si on change la date en string ??
  //  creditcard.setCreditCardNumber("1111222233334445"); // wrong number last digit must be 4
    creditcard.setCreditCardExpirationDateTest("2025-02-17"); // 
    String strJson = om.writeValueAsString(creditcard);
       LOG.debug("stringJson = " + strJson);
    HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8083/creditcard/" + URLEncoder.encode(strJson,"utf-8")))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_2) // default Http2 new 13-03-2024
                .timeout(Duration.ofSeconds(10))
                .header("key1", "value1") // on récupère le paramètre dans python  request.headers.get('key1')
                .header("key2", "value2")
              //  .cookieHandler(new CookieManager())
                .build();
     //   https://stackoverflow.com/questions/17493027/can-i-open-a-new-window-and-populate-it-with-a-string-variable
      LOG.debug("just before send HttpRequest");
       HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
       // print response headers
       LOG.debug("just after HttpResponse = ");// tombe en exception si server python non chargé!!
       if(response == null){
           String msg = "response = null !! cata";
           LOG.error(msg);
           showMessageFatal(msg);
       }

     //  Optional<String> header = headers.firstValue("x-something");
    //  LOG.debug("Is any value present: " + header.isPresent()); // true
      //https://mkyong.com/java8/java-8-convert-optionalstring-to-string/#:~:text=Java%208%20%E2%80%93%20Convert%20Optional%20to%20String&text=In%20Java%208%2C%20we%20can,to%20a%20String%20.
    //  if(header.isPresent()) {
    //      String msg = "Returned param x-something = " + header.get();
    //      LOG.info(msg);
    //      showMessageInfo(msg);  
    //    }
  //  Optional<String> header = headers.firstValue("PaymentReference");
      
   //   LOG.debug("firstValue x-something = " + header);
        HttpHeaders headers = response.headers();
        LOG.debug("response uri=  " + response.uri());
        headers.map().forEach((k, v) -> LOG.debug("headers are : " + k + ":" + v));
         LOG.debug("response StatusCode =  " + response.statusCode());
         LOG.debug("response version =  " + response.version());
        if(response.statusCode() == 200){
            String msg = "response from Python server : OK  = " + NEW_LINE + response.body(); 
            LOG.info(msg);
            showMessageInfo(msg);
            Optional<String> header = response.headers().firstValue("PaymentReference");
            if(header.isPresent()){
              msg = "Payment Reference = " + header.get();
              LOG.info(msg);
              showMessageInfo(msg);  
            }else{ 
          //String msg = "Payment Reference = " + uuid.get();
              msg = "NO Payment Reference !!= ";
              LOG.error(msg);
              showMessageFatal(msg);
              return false;
            }
         // completes creditcard with PaymentReference
         // setPayment OK
         return true;
       }else{ // not 200
            String msg = "response from Python server : !NOT OK!  = " + NEW_LINE + response.body(); //getStatus() + "<br/>\n" + ws;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
    }
       // développer la suite
       // returns modified creditcard
} catch (Exception e) {
            if(e.getMessage() == null){
                String msg= "Server not available ! - Ask the Administrator to run the creditCardService.py on the python server";
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }else{
                String msg = "£££ Exception in sendGet = " + e.getMessage();
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }

   }
}
*/
  public boolean sendPost(Creditcard creditcard) throws Exception {
        // form parametersJava 11 HttpClient didn’t provide API for the form data, we have to construct it manually.
  try{  
      LOG.debug("starting sendPost()");
   //     Map<Object, Object> data = new HashMap<>();
    //    data.put("username", "abc");
    //    data.put("password", "123");
    //    data.put("custom", "secret");
    //    data.put("ts", System.currentTimeMillis());
  //ObjectMapper om = 
    String strJson = new ObjectMapper().writeValueAsString(creditcard);
       LOG.debug("stringJson = " + strJson);
       
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
               // .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create("https://localhost:5000/creditcard/" + URLEncoder.encode(strJson,"utf-8")))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .setHeader("Content-Type", "application/json")
                .header("returnDirectory", "http://localhost:8080/GolfWfly-1.0-SNAPSHOT/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
 
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.debug("response = " + response);
        LOG.debug(response.statusCode());
        // print response body
        LOG.debug("response body = " + response.body());
        return true;
 //   }
} catch (Exception e) {
            String msg = "£££ Exception in postWebService = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
   }
} //end method
    
    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
  void main() throws SQLException, Exception{   
 //public static void main(String[] args) throws Exception{
        Java11HttpClientExample client = new Java11HttpClientExample();
           LOG.debug("Testing 1 - Send Http GET request");
        Creditcard creditcard = new Creditcard();
        creditcard.setCreditCardHolder("LOUIS COLLET 11");
        creditcard.setCreditCardIdPlayer(324713);
        creditcard.setCommunication("creditcard using Java11HttpClientExample");
        //fonctionne en postman avec : http://localhost:5000/creditcard/{"totalPrice":35.0,"idplayer":324713,"creditCardHolder":"LOUIS_COLLET","creditCardNumber":"1111222233334444",
        String ldString = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));  // important - et pas /
        LOG.debug("ldString = " + ldString);
 ///       creditcard.setCreditCardExpirationDateTest(ldString);
 ///      LOG.debug("contenu test = " + creditcard.getCreditCardExpirationDateTest());
  // 
        creditcard.setCreditcardNumber("1111222233334444");
        creditcard.setTotalPrice(35.0);
        creditcard.setTypePayment("LESSON");
        creditcard.setCreditcardType("VISA");
        creditcard.setCreditcardVerificationCode((short)567);
        creditcard.setCreditcardCurrency("EUR");
 LOG.debug("just before send " + creditcard);
       //  client.sendPost(creditcard);
         client.sendPost(creditcard);
        LOG.debug("Testing 2 - Send Http get EXECUTED");
  ///      obj.sendPost();
        
 //   LOG.debug("Testing 2 - Send Http Post EXECUTED");
    }
}