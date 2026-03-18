
package rest;

import static interfaces.Log.LOG;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.annotation.security.DenyAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

// http://www.mastertheboss.com/jboss-frameworks/resteasy/resteasy-tutorial

//public class JaxRsActivator extends Application {
//}
// accès via http://localhost:8080/rest-demo-1.0/rest/tutorial/helloworld
//The @Path annotation at class level is used to specify the base URL of the Web service
@Path("tutorial")
@DenyAll // security audit 2026-03-09 — test/demo endpoints disabled in production
public class PaymentService{
  
  /*/  **
  //   * Use uriInfo to get current context path and to build HATEOAS links 
     * */
 //   @Context
    UriInfo uriInfo;
  // Default instance of client
    Client client = ClientBuilder.newClient();
    // Additional configuration of default client
//client.property("MyProperty", "MyValue")
// .register(MyProvider.class)
// .register(MyFeature.class);
   //  com.demo.CardBelgium cardBelgium= null;
     
     
// you can assign a @Path annotation at method level to specify the single action you want to invoke.    
@GET
@Path("/pojoJson")
@Produces(MediaType.APPLICATION_JSON)
//public CardBelgium getPojoResponseJson() {
public Response cardBelgiumResponseJson(@Context UriInfo uriInfo) throws Exception {
//public Response cardBelgiumResponseJson() throws Exception {    
try{
    LOG.debug("entering cardBelgiumResponseJson");
    // ici aller chercher dans HandleSmartCard
   LOG.debug("uriInfo Path = " + uriInfo.getPath());
  LOG.debug("uriInfo BaseUri = " + uriInfo.getBaseUri());
    
   URI requestUri = uriInfo.getRequestUri();
  //     LOG.debug("uriInfo RequestUri = " + requestUri.toString());
 
       LOG.debug("we go to HandleSmartCard !");
   try{    
//     cardBelgium = new com.demo.HandleSmartCard().handle();
 //      LOG.debug("we are back with cardBelgium = " + cardBelgium);
    } catch (Exception ie) {
            String msg = "Exception in com.demo.HandleSmartCard : " + ie.getMessage();
            LOG.error(msg);
     //       LCUtil.showMessageFatal(msg);
            Response.serverError();//return null;
    }
    
    
  //  LOG.debug("we come back with cardBelgium = " + cardBelgium);
  //  CardBelgium cardBelgium = new CardBelgium();
 //   cardBelgium.setCountry("Belgium");
//    cardBelgium.setFirstName("Louis, Jean"); //() + cardBelgium.getFirstname3());
//    cardBelgium.setLastName("Collet");
//    cardBelgium.setCity("1060 - Brussels");
//    cardBelgium.setBirthDate(new SimpleDateFormat("dd/MM/yyyy").parse("25/04/1950"));
// améliorer si erreur ==> 
//Response.serverError()  .status(Response.Status.INTERNAL_SERVER_ERROR)
   return Response
      .status(Response.Status.OK)
      .type(MediaType.APPLICATION_JSON)
      .language(Locale.ENGLISH)
      .location(requestUri)
 //     .entity(cardBelgium)
      .build();
} catch (Exception ie) {
            String msg = "EIDException in cardBelgiumResponseJson: " + ie.getMessage();
            LOG.error(msg);
     //       LCUtil.showMessageFatal(msg);
           return null;
}
} //end method   

@GET
@Path("helloworld")
public String helloworld() throws IOException {

    LOG.debug("from helloworld : uri = " + uriInfo);
        return "creditcard_accepted.xhtml?faces-redirect=true";
    }

@GET
@Path("item")
//@Produces({"application/xml"})
@Produces(MediaType.APPLICATION_JSON) 
public Item getItem() {
  Item item = new Item("computer",2500);
  return item;
}    

@GET
@Path("itemArray")
@Produces({"application/xml"})
public Item[]  getItemArray() {
  Item item[] = new Item[2];
  item[0] = new Item("computer",2500);
  item[1] = new Item("chair",100);

  return item;
}  
@GET
@Path("itemListXml")
@Produces(MediaType.APPLICATION_XML)

public List<Item> getCollItems() {
//	 list;
        List<Item> list = new ArrayList<>();
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
@Produces(MediaType.APPLICATION_JSON)

public List<Item> getJSONItems() {
    LOG.info("entering getJsonItems de com.demo.HelloWorld");
        List<Item> list = new ArrayList<>();
        Item item1 = new Item("computer",2500);
        Item item2 = new Item("chair",100);
        Item item3 = new Item("table",200);

        list.add(item1);
        list.add(item2);
        list.add(item3);
           LOG.debug("list = " + list.toString());
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
@Path("not_ok") // idem que "/not_ok"
public Response getNOkTextResponse() {
    String message = "from not_ok = <h1 style='color:red;background-color:powderblue;'>"
            + " Louis, There was an internal server error";
    return Response
   //   .status(Response.Status.INTERNAL_SERVER_ERROR)
      .status(Response.Status.OK)
      .entity(message)
      .type(MediaType.TEXT_HTML_TYPE)
      .build();
}   



@GET
@Path("/text_plain")

//@Produces(MediaType.APPLICATION_JSON)
public Response getTextResponseTypeDefined() {

    String message = "<h1 style='color:red;'>This is a plain text response";

    return Response
      .status(Response.Status.OK)
      .entity(message)
      .type(MediaType.TEXT_HTML_TYPE)  // ne prend pas html !!
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
      .type(MediaType.APPLICATION_JSON)
      .entity(person)
      .build();
}
//The Person POJO can now be used to return JSON as the Response body:


 // The Person POJO will be transformed into a JSON and sent back as a response:
//{"address":"Nepal","name":"Abhinayak"}
@GET
@Path("/json")
public Response getJsonResponse() {

    String message = "{\"hello\": \"This is a JSON response\"}";

    return Response
      .status(Response.Status.OK)
      .entity(message)
      .type(MediaType.APPLICATION_JSON)
      .build();
}
//Calling this resource will return a JSON:
//{"hello":"This is a JSON response"}

} //end class