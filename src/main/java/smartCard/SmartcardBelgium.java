package smartCard;

//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Creditcard;
import entite.Player;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.sql.SQLException;
import static utils.LCUtil.showMessageFatal;
// http://www.mastertheboss.com/jboss-frameworks/resteasy/resteasy-tutorial

@Path("smartcard") // was tutorial
public class SmartcardBelgium{
    
    
  public SmartcardBelgium(){ // constructor
    //   LOG.debug("this is the constructor of smartcardBelgium");
}  
 //   https://dzone.com/articles/jax-rs-what-is-context
 //   @Context
 //  UriInfo uriInfo;
    Response response = null;
    Client client = null;
    // Additional configuration of default client
//client.property("MyProperty", "MyValue")
// .register(MyProvider.class)
// .register(MyFeature.class);
    
   @Inject private entite.CardBelgium cardBelgium;
//   @Inject Config config;
/*   
@GET
@Path("eidbelgium")
@Produces(MediaType.TEXT_PLAIN)
// belgian electronic identitycard
// sera appelé à partir de Golfwfly/CourseController, method public String registereIDPlayer() line 6292

public Response eidBelgium() throws Exception {
try{
            LOG.debug("entering eidBelgium");
            LOG.debug("before entering handle = " + uriInfo);
          cardBelgium= new handle.smartcard.HandleSmartCard().handle();
            LOG.debug("cardBelgium after handle = " + cardBelgium);
        
        String msg = "<h1 style='color:blue;'>Hello World Louis from eidbelgium! " + cardBelgium;
    return Response
      .status(Response.Status.OK)
      .entity(msg)
      .type(MediaType.TEXT_PLAIN)
      .build();
}catch(Exception ie) {
            String msg = "EIDException in SmartCardBelgium" + ie.getMessage();
            LOG.error(msg);
  //          LCUtil.showMessageFatal(ie.getMessage());
            return null; // indicates that the same view should be redisplayed
}
} // end method eidbelgium 


   public void testWebService() {
try{
    LOG.debug("entering testWebservice ");
        javax.ws.rs.client.Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:8080/mavenEID-1.0/rest/smartcard/text_plain");
        Invocation.Builder invocationBuilder = webTarget
                .request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        LOG.debug("response = " + response.readEntity(String.class));
 
   } catch (Exception e) {
            String msg = "£££ Exception in restwebservice = " + e.getMessage();
     //       cardBelgium = null;
            LOG.error(msg);
      //      return null;   
   }   
   }   
  */  
public Player initClient() {
try{
    ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());  // new 18/01/2019 traiter LocalDateTime format 
    	om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);  // fonctionne ??
   
    Creditcard creditcard = new Creditcard();
    creditcard.setCreditCardHolder("LOUIS COLLET");
    creditcard.setCommunication("creditcard communication");
    creditcard.setCreditCardExpirationDateLdt(LocalDateTime.MIN);
    creditcard.setCreditcardNumber("1111222233334444");
    creditcard.setTotalPrice(35.0);
    creditcard.setTypePayment("LESSON");
    String json = om.writeValueAsString(creditcard); //. prend 3 fields ??
       LOG.debug("creditcard data converted in json format = " + NEW_LINE + json);
    
        
       LOG.debug("starting initClient ");
//step 1 Client
        jakarta.ws.rs.client.Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target("http://localhost:8080/mavenEID-1.0/rest/smartcard/text_plain");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
    //            .header("Authorization", "Basic " + authStringEnc);

        Response response = invocationBuilder.get();
        LOG.debug("response = " + response.readEntity(String.class));

    client = ClientBuilder.newBuilder()
                         .connectTimeout(5, TimeUnit.SECONDS)
                         .readTimeout(5, TimeUnit.SECONDS)
 //       .withConfig(c)
 //       .register()
                         .build();
///    UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/rest-demo-1.0/rest/tutorial/pojoJson");
 // next line test with Python !!   
   /// DONNE ERREUR UriBuilder uriBuilder = UriBuilder.fromUri("http://127.0.0.1:8080/api/v3/resources/player?playerId=125560");
     UriBuilder uriBuilder = UriBuilder.fromUri
        //("http://localhost:8080/mavenEID-1.0/rest/smartcard/eidbelgium/player?playerId=125560");
    ("http://localhost:8080/mavenEID-1.0/rest/smartcard/text_plain");
     
 //    localhost:8080/maven-eid-1.0/rest/smartcard/text_plain
     
     
  //  WebTarget target = client.target("http://localhost:8080/mavenEID-1.0/rest/smartcard/eidbelgium");
// step 2 WebTarget
    WebTarget target = client.target(uriBuilder);
         LOG.debug("target uri = " + target.getUri());

// step 3 Invocation
    Invocation invocation = target.request()
        .accept(MediaType.APPLICATION_JSON) // was JSON
    //        .acceptLanguage("fr") // locales
   //         .async()
      //      .get(Item.class)
        .buildGet();
  //  LOG.debug("Invocation invo = " + invo.toString()); system
  //  response = target.request().get();
  
    response = invocation.invoke();
    LOG.debug("line 01 = ");
 //   Item items = response.readEntity(Item.class);
 //      LOG.debug("items = " + items);
// step 4 handle response
    if(response.getStatus() == Response.Status.OK.getStatusCode()){
        LOG.debug("response - it is OK because response status = " + response.getStatus());  // 404
    }else{
     //   LOG.debug("response - it is !NOT OK! because response status = " + response.getStatus());  // 404
        String msg = "response - it is !NOT OK! because response status = " + response.getStatus();
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
    
//        LOG.debug("response - getEntity = " + response.readEntity(Item.class));
         LOG.debug("response Date = " + response.getDate());
   //      LOG.debug("response language = " + response.getLanguage());
   //      LOG.debug("response location = " + response.getLocation());
   //      LOG.debug("response status = " + response.getStatus());  // 404
         LOG.debug("response statusInfo = " + response.getStatusInfo());
         
 //   String json = response.readEntity(String.class);
         LOG.debug("response String json = " + json);
///    json = json.replaceAll("[", "").replaceAll("]", "");
    LOG.debug("response String json without []= " + json);
   // response entity = [{"description":"computer","price":2500},{"description":"chair","price":100},{"description":"table","price":200}]
         // c'est déjà un json !!
//   ObjectMapper om = new ObjectMapper();
   om.configure(SerializationFeature.INDENT_OUTPUT,true);//Set pretty printing of json
   om.registerModule(new JavaTimeModule());  // new 18/01/2019 traiter LocalDateTime format 
   
   Player player = om.readValue(json, entite.Player.class);
   LOG.debug("player.getPlayerBirthDate() = " + player.getPlayerBirthDate());
   //Convereted to Type as array
 //  Player[] player = om.readValue(json, Player[].class );
      LOG.debug("entite player = " + player);
   
///    cardBelgium = om.readValue(json, entite.CardBelgium.class);
///      LOG.debug("entite CardBelgium = " + cardBelgium);
 //  String json = om.readValueAsString(entity); 
  //   String json = om.writeValueAsString(entity); 
  //      LOG.debug("json = " + json);
 // Item item = new Item();
  //   Person item = new Person();
   //  json = om.writeValueAsString(item);
//   return cardBelgium;
   return player;
   //       LOG.debug("json = " + json);
  } catch (Exception e) {
            String msg = "£££ Exception in initClient = " + e.getMessage();
            cardBelgium = null;
            LOG.error(msg);
            return null;
            
   }finally{
 //      LOG.info("finally - response = " + response);
       if(response != null){
           LOG.debug("finally : response will be closed");
           response.close();  // You should close connections!
           
           client.close();
    //       cardBelgium = null;
   //        return cardBelgium;
       }
   }   
} // end method initClient
 
//@PreDestroy
//public void tearDown() {
//    this.client.close();
//}
/*
@GET
@Path("item")
//@Produces({"application/xml"})
@Produces(MediaType.APPLICATION_JSON) 
public Item getItem() {
    LOG.debug(" ... entering getItem");
  Item item = new Item("computer",2500);
  return item;
}    

@GET
@Path("itemArray")
@Produces({MediaType.APPLICATION_XML})
public Item[]  getItemArray() {
  Item item[] = new Item[2];
  item[0] = new Item("computer",2500);
  item[1] = new Item("chair",100);
 return item;
}  
@GET
@Path("itemList")
@Produces(MediaType.APPLICATION_XML)
//    @SuppressWarnings("unchecked")
public List<Item> getCollItems() {
	List<Item> list = new ArrayList();
	Item item1 = new Item("computer",2500);
	Item item2 = new Item("chair",100);
	Item item3 = new Item("table",200);
	list.add(item1);
	list.add(item2);
	list.add(item3);
return list;
}

@GET
@Path("itemListJson")
//@Produces("application/json")
 //  @SuppressWarnings("unchecked")
@Produces(MediaType.APPLICATION_JSON)
public List<Item>  getJSONItems() {
        List<Item> list = new ArrayList<>();
        Item item1 = new webservices.Item("computer",2500);
        Item item2 = new Item("chair",100);
        Item item3 = new Item("table",200);

        list.add(item1);
        list.add(item2);
        list.add(item3);
 return list;
}

//@Path("/user-management")
//public class UserManagementModule
//{
    @GET
   @Path("users")  // /users
    public Response getAllUsers(){
        String result = "<h1>RESTful Demo Application</h1>In real world application, a collection of users will be returned !!";
        return Response.status(200).entity(result).build();
    }
    // exemple from web
@GET
    @Path("/{message}")
    public Response publishMessage(@PathParam("message") String msgStr){
         
        String responseStr = "Received message: "+msgStr;
        return Response.status(200).entity(responseStr).build();
    }
 @GET
@Path("/not_ok")
public Response getNOkTextResponse() {
    String message = "There was an internal server error";
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(message)
      .build();
}   
@GET
@Path("/text_plain")
public Response getTextResponseTypeDefined() {
    String message = "This is a plain text response";
    return Response
      .status(Response.Status.OK)
      .entity(message)
      .type(MediaType.TEXT_PLAIN)
      .build();
}    
 //The same outcome could also be achieved via the Produces annotation
// instead of using the type() method in the Response:

@GET
@Path("/text_plain_annotation")
@Produces({ MediaType.TEXT_PLAIN })
public Response getTextResponseTypeAnnotated() {

    String message = "This is a plain text response via annotation";

    return Response
      .status(Response.Status.OK)
      .entity(message)
      .build();
}   

@GET
@Path("/pojo")
public Response getPojoResponse() {

   Person person = new Person();
   person.setName("Collet");
  person.setAddress("amazone 55");
    return Response
      .status(Response.Status.OK)
      .entity(person)
      .build();
}
//The Person POJO can now be used to return JSON as the Response body:

@GET
@Path("/pojo")
public Response getPojoResponseJson() {

    Person person = new Person(); //"Abhinayak", "Nepal");

    return Response
      .status(Response.Status.OK)
      .entity(person)
      .build();
}
 // The Person POJO will be transformed into a JSON and sent back as a response:
//{"address":"Nepal","name":"Abhinayak"}
@GET
@Path("/json")
public Response getJsonResponse() {

    String message = "{\"hello\": \"This is a JSON response\"}";
ResponseBuilder builder = Response.ok(Response.Status.OK);
    return Response
      .status(Response.Status.OK)
      .entity(message)
      .type(MediaType.APPLICATION_JSON)
      .build();
}
//Calling this resource will return a JSON:
//{"hello":"This is a JSON response"}
*/
public static void main(String args[])throws SQLException, Exception{    
  try{
  //  CardBelgium lp = new HandleSmartCard().handle(); // proposition mod by netbeans
  LOG.debug("starting main");
       new SmartcardBelgium().initClient();
     LOG.debug("main - after initClient");  
/*      
Client client = ClientBuilder.newBuilder()
	       .connectTimeout(100, TimeUnit.SECONDS)
	       .readTimeout(2, TimeUnit.SECONDS)
	       .build();
// The connectTimeout method determines how long the client must wait when making a new server connection
// The readTimeout method determines how long the client must wait for a response from the server.

 //   Client client = ClientBuilder.newClient();
    WebTarget target = client.target("http://foo.com/resource");
    Response response = target.request().get();
    String value = response.readEntity(String.class);
    response.close();  // You should close connections!
 
Client asyncClient = new ResteasyClientBuilder().useAsyncHttpEngine()
                     .build();
Future<Response> future = asyncClient
                          .target("http://locahost:8080/test").request()
                          .async().get();
Response res = future.get();
Assert.assertEquals(HttpResponseCodes.SC_OK, res.getStatus());
String entity = res.readEntity(String.class);

 
 @WebMethod
    @RequestWrapper(localName = "echo", targetNamespace = "http://echo/", className = "echo.Echo_Type")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "http://echo/", className = "echo.EchoResponse")
    @WebResult(name = "return", targetNamespace = "")
    public java.lang.String echo(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    );
  //       var v1 = new SmartcardBelgium().uriInfo.getAbsolutePath().toASCIIString();
 //         LOG.info("from main, v1 = " + v1);
 //  Response resp = new SmartcardBelgium().helloworld();
 //       LOG.debug("v = " + resp);
*/
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{ }
 } // end main//
} //end class