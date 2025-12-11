
package mongo;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import utils.DBConnection;

// non terminé, non testé 
public class BulkWriteCalculations {
    void main() throws Exception {
        // Replace the uri string with your MongoDB deployment's connection string
  //      String uri = "<connection string uri>";
        try (MongoClient mongoClient = MongoClients.create()) {
            MongoDatabase database = mongoClient.getDatabase("golflc");
     database.getCollection("logging-calculations").drop();
       LOG.debug("collection dropped ! ");
     database.createCollection("logging-calculations");
       LOG.debug("collection created ! ");
     MongoCollection<Document> collection = database.getCollection("logging-calculations");
     
     List<Document> list = new ArrayList<Document>();
     Connection conn = new DBConnection().getConnection();
     List<LoggingUser> v = new lists.LogginUserList().list(conn);
            try {
                for(int i=0; i<v.size(); i++){
                     Document document = new Document("loggingIdPlayer", v.get(i).getLoggingIdPlayer())
                        .append("loggingIdRound", v.get(i).getLoggingIdRound())
                        .append("loggingType", v.get(i).getLoggingType())
                        .append("loggingCalculations", v.get(i).getLoggingCalculations())
                        .append("loggingModificationDate", v.get(i).getLoggingModificationDate()
                        );
                     list.add(document);
                }
                collection.insertMany(list);
                LOG.debug("documents added from list input= " + v.size());
                LOG.debug("documents inserted in mongoDB = " + collection.countDocuments());
                  DBConnection.closeQuietly(conn, null, null , null); 
            } catch (MongoException me) {
                System.err.println("The bulk write operation failed due to an error: " + me);
                DBConnection.closeQuietly(conn, null, null , null); 
            }
        } // close resource
    } // end method
} //end class