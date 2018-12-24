
package test_instruction;

import com.mysql.cj.xdevapi.ClientFactory;
import com.mysql.cj.xdevapi.Collection;
import com.mysql.cj.xdevapi.DocResult;
import com.mysql.cj.xdevapi.Schema;
import com.mysql.cj.xdevapi.Session;
import com.mysql.cj.xdevapi.SessionFactory;
import static interfaces.Log.LOG;
import java.util.List;


public class TestXDevAPI {

  public static void main(String[] args)
  {
   try{
       LOG.info("Wildfly - entering TestXDevAPI ...");    


//https://insidemysql.com/connector-j-8-0-11-the-face-for-your-brand-new-document-oriented-database/

SessionFactory sFact = new SessionFactory();
    LOG.info("line 01");
    
//Session mySession = sFact.getSession("mysqlx://LouisCollet:lc1lc2@localhost:33060");  // also working !!!
    LOG.info("line 02");
    /* create
Schema schema = mySession.createSchema("demo", true);
    LOG.info("line 03");

Collection myCollection = schema.createCollection("greetings");
myCollection.add("{\"language\":\"English\", \"greeting\":\"Hello World\"}")
        .add("{\"language\":\"Português\", \"greeting\":\"Olá Mundo\"}")
        .add("{\"language\":\"Français\", \"greeting\":\"Bonjour Monde\"}")
        .execute();

DocResult res = myCollection.find("$.language='Português'").execute();
for (DbDoc doc : res.fetchAll()) {
    LOG.info("language portuguess" + doc);
}
 
res = myCollection.find("greeting LIKE '%World%'").execute();
for (DbDoc doc : res.fetchAll()) {
    LOG.info("like %world%" +  doc);
}
*/
// Connect to server on localhost
// Session mySession = new SessionFactory().getSession("mysqlx://localhost:33060/test?user=user&password=password");

// Connect to server on localhost using a connection URI
Session mySession = new SessionFactory().getSession("mysqlx://localhost:33060/demo?user=LouisCollet&password=lc1lc2");

Schema myDb = mySession.getSchema("demo");

// Use the collection 'my_collection'
Collection myCollection = myDb.getCollection("greetings");
//Collection myCollection = myDb.getCollection("greetings");

DocResult res = myCollection.find("$.language='Português'").execute();
res.fetchAll().forEach((doc) -> {
    LOG.info("language portuguess" + doc);
       });

// Specify which document to find with Collection.find() and
// fetch it from the database with .execute()
DocResult myDocs = myCollection.find("name like :Monde").limit(1).bind("Monde", "S%").execute();

// Print document
LOG.info("fetched document = "+ myDocs.fetchOne());

// Connecting to MySQL and working with a Session
// Connect to a dedicated MySQL server using a connection URI
mySession = new SessionFactory().getSession("mysqlx://localhost:33060/demo?user=LouisCollet&password=lc1lc2");

// Get a list of all available schemas
List<Schema> schemaList = mySession.getSchemas();

    LOG.info("Available schemas in this session:");

// Loop over all available schemas and print their name
for (Schema schema : schemaList) {
    LOG.info("liste des schémas = " + schema.getName());
}

mySession.close();

//Obtain new ClientFactory
ClientFactory cf = new ClientFactory(); 


 // ne fait rien ... myCollection.add("{\"name\":\"Jack\", \"age\":15}", "{\"name\":\"Susanne\", \"age\":24}", "{\"name\":\"User\", \"age\":39}");



/*
//Obtain Client from ClientFactory
Client cli = cf.getClient(this.baseUrl, "{"pooling":{"enabled":true, "maxSize":8, "maxIdleTime":30000, "queueTimeout":10000} }");
Session sess = cli.getSession();

//Use Session as usual

//Close Client after use
cli.close();
*/

    LOG.info("exiting TestXDevAPI");
  }catch (Exception e){
	LOG.error("Fatal Exception in TestXDevAPI : "  + e);
}
  } //end method init

} //end class