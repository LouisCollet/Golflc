
package Controllers;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient; // Interface MongoDatabase
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.time.LocalDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class MongoCalculationsController {
    //https://mongodb.github.io/mongo-java-driver/3.12/javadoc/
//    String uri = "mongodb://localhost:27017/?maxPoolSize=20&w=majority";
   final static private String COLLECTION_NAME = "logging_calculations";
 //     final private String collection_name = "help_view";
   final private MongoClient mongoClient = MongoClients.create();
   final private MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
   final static private String DATABASE_NAME = "golflc"; 
    
public long delete(LoggingUser loggingUser) {
      LOG.debug("entering delete");
   try (MongoClient mongoClient = MongoClients.create()) {
      MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
      try{
        DeleteResult result = collection.deleteMany(userFilter(loggingUser)); // or deleteOne 
           LOG.debug("deletedcount = "+ result.getDeletedCount());
        return result.getDeletedCount();
     }catch (MongoException me) {
         String msg = "Unable to delete due to an error: " + me;
         LOG.error(msg);
         return 99;
     }
   }// end try resources
} // end method

public long update(LoggingUser loggingUser) {
      LOG.debug("entering update");
     // https://mongodb.github.io/mongo-java-driver/4.7/apidocs/mongodb-driver-core/com/mongodb/client/model/Updates.html
   try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
        
        MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
      try{
        Bson update = Updates.combine ( Updates.set("loggingCalculations", loggingUser.getLoggingCalculations()),
                                        Updates.set("loggingModificationDate", LocalDateTime.now())
                                      );
        UpdateResult result = collection.updateOne(userFilter(loggingUser), update);
     return result.getModifiedCount();
     }catch (MongoException me) {
         String msg = "Unable to delete due to an error: " + me;
         LOG.error(msg);
         return 99;
     }
   }// end try resources
} // end method

  public boolean create(LoggingUser loggingUser) {
     
      LOG.debug("entering create for = " + loggingUser);
    if(Controllers.LoggingUserController.getText() == null){
        // testing only
        loggingUser.setLoggingCalculations(loggingUser.getLoggingCalculations());
    }else{
        // real life production
        loggingUser.setLoggingCalculations(Controllers.LoggingUserController.getText());
    }

       LOG.debug("create or Update ? with logging = " + loggingUser);
      if(new MongoCalculationsController().find(loggingUser)){
          LOG.debug("existing situation found ==> update !!");
          new MongoCalculationsController().update(loggingUser);
          return true;
      }else{
          LOG.debug("NO existing situation found ==> create !!");
      }
// this is a creation
 // Creating a default codec registery  https://codersathi.com/mapping-mongodb-document-to-pojo-class-in-java/
   try (MongoClient mongoClient = MongoClients.create()) {
      try{
         CodecRegistry codecRegistry = CodecRegistries
                .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),CodecRegistries
                .fromProviders(PojoCodecProvider.builder()
                .automatic(true)
                .build()));
       // Creating one instance of MongoCollection for POJO with codec registry   KEY KEY KEY !!!
         MongoCollection<LoggingUser> collection = mongoClient
                .getDatabase(DATABASE_NAME)
                .getCollection(COLLECTION_NAME, LoggingUser.class)
                .withCodecRegistry(codecRegistry);
        // Inserting data into database directly from POJO entite.LoggingUser object
	 collection.insertOne(loggingUser);
// verification
    //    Bson findByIdPlayer = new Document("loggingIdPlayer", loggingUser.getLoggingIdPlayer());
//	LoggingUser lu = collection.find(findByIdPlayer).first();
    //      LOG.debug("inserted logging-calculations first document = " + lu);
      return true;
     }catch (MongoException me) {
        String msg = "Unable to create due to an error: " + me;
        LOG.error(msg);
        return false;
     }
  } // end try // auto-closeable resource
 } //end method create
  
public String read(LoggingUser loggingUser) {
      LOG.debug("entering read for " + loggingUser);
try (MongoClient mongoClient = MongoClients.create()) {
     MongoCollection<Document> collection =  mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
    try {
      // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/findOne/
        Document document = collection.find(userFilter(loggingUser)).first();
            if(document == null) {
                String msg = "No calculations details found !!";
                LOG.debug(msg);
                return msg;
            }else{
                LOG.debug("found a document !!" + document);
                return document.getString("loggingCalculations");
            }
     }catch (MongoException me) {
         String msg = "Unable to read due to an error: " + me;
         LOG.error(msg);
         return null;
     }
 } // end try 1
} // end method
  public boolean find(LoggingUser loggingUser) {
      LOG.debug("entering find for " + loggingUser);
    // but : décider si create (existe pas) ou update (existe déjà) 
try (MongoClient mongoClien = MongoClients.create()) {
     
    try {
        MongoCollection<Document> collectio =  mongoClien.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
         Bson query = eq("_id",loggingUser.getLoggingIdPlayer());
       if(collectio.countDocuments(userFilter(loggingUser)) == 0){
 //          LOG.debug("count documents query = " + count);
           return false;
       }else{
  //         LOG.debug("count documents query = " + count);
           return true;
       }
     }catch (MongoException me) {
         String msg = "Unable to read due to an error: " + me;
         LOG.error(msg);
         return false;
     }
 } // end try 1
} // end method
  
  public static Bson userFilter(LoggingUser loggingUser){
      //DRY : Don't Repeat Yourself
       return Filters.and(Filters.eq("loggingIdPlayer", loggingUser.getLoggingIdPlayer()),
                          Filters.eq("loggingIdRound",  loggingUser.getLoggingIdRound()),
                          Filters.eq("loggingType",     loggingUser.getLoggingType())
                         );
  }
 
  // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/command/
  public void utilities() {
        // Replace the uri string with your MongoDB deployment's connection string
  //     String uri = "<connection string uri>";
        try (MongoClient mongoClient = MongoClients.create()) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            try {
                
  // create index
  //https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/indexes/
  
  
                MongoCollection<Document> collection = database.getCollection("logging_calculations");
                String resultCreateIndex = collection.createIndex(Indexes.ascending("loggingIdPlayer", "loggingIdRound","loggingType"));
                   LOG.debug("createIndex result = : " + resultCreateIndex);
                LOG.debug(String.format("Index created: %s", resultCreateIndex));
// executed 21/11/2022 11:01 ures
// Index created: loggingIdPlayer_1_loggingIdRound_1_loggingType_1
      
                Bson command = new BsonDocument("dbStats", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                    LOG.debug("database Stats: " + commandResult.toJson());
                command = new BsonDocument("buildinfo", new BsonInt64(1));
                commandResult = database.runCommand(command);
                    LOG.debug("buildinfo: " + commandResult.toJson());
                command = new Document("collStats", COLLECTION_NAME);
                commandResult = database.runCommand(command);
                LOG.debug("collection Stats: " + commandResult.toJson());
            } catch (MongoException me) {
                System.err.println("An error occurred in utilities: " + me);
            }
        }
    } // end method
  
  void main() {
       LOG.debug("starting main");
     LoggingUser logging = new LoggingUser();
     logging.setLoggingIdPlayer(324713);
     logging.setLoggingIdRound(698);
     logging.setLoggingType("R");
   //  logging.setLoggingCalculations(LocalDateTime.now() + "updated LCLC!! these are the calculations details !!" + logging.getLoggingIdPlayer());
     logging.setLoggingModificationDate(LocalDateTime.now());
  //   boolean b = new MongoController().mongo(logging);
  //   long b = new MongoController().delete(logging);
   // boolean b = new MongoController().create(logging);
   //  LOG.debug("result create = " + b);
 //     long l = new MongoCalculationsController().update(logging);
  //     LOG.debug("\n\nupdate result = " + l);
 //   String s = new MongoCalculationsController().read(logging);
  //    LOG.debug("\n\n read result = " + s);
      new MongoCalculationsController().utilities();
   //   LOG.debug("result main = " + b);
  }
  
} // end class
