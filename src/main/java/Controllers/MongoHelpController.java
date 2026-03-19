
package Controllers;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import entite.HelpView;
import static interfaces.GolfInterface.mongo_formatter;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("mongoHelpC")
@SessionScoped

public class MongoHelpController implements Serializable{
    //https://mongodb.github.io/mongo-java-driver/5.0/javadoc/
//    String uri = "mongodb://localhost:27017/?maxPoolSize=20&w=majority";
    
   final static private String COLLECTION_NAME = "help_view";
   final static private String DATABASE_NAME = "golflc";
   final static private MongoClient mongoClient = MongoClients.create();  // Creates a new client with the default connection string "mongodb://localhost:".
   final static private MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
   @Inject private LanguageController languageController; // fix multi-user 2026-03-07
   private entite.HelpView helpView;
   public MongoHelpController() {   // constructor
  }
      
    public HelpView getHelpView() {
        return helpView;
    }

    public void setHelpView(HelpView helpView) {
        this.helpView = helpView;
    }

public long deleteOne(HelpView helpView) {
      LOG.debug("entering delete");
      try{
          Bson query = eq("_id",helpView.getId());
          DeleteResult result = collection.deleteOne(query);
            LOG.debug("deletedcount = "+ result.getDeletedCount());
        return result.getDeletedCount();
     }catch (MongoException me) {
         String msg = "Unable to delete due to an error: " + me;
         LOG.error(msg);
         return 99;
     }
 //  }// end try resources
} // end method

public static long updateOne(HelpView helpView) {
      LOG.debug("entering update");
     // https://mongodb.github.io/mongo-java-driver/5.0/apidocs/mongodb-driver-core/com/mongodb/client/model/Updates.html
 try{
        Bson update = Updates.combine ( Updates.set("helpViewText", helpView.getHelpViewText()),
                                        Updates.set("helpViewLanguage", helpView.getHelpViewLanguage()),
                                        Updates.set("helpViewModificationDate", LocalDateTime.now())
                                      );
        UpdateResult result = collection.updateOne(userFilter(helpView), update);
     return result.getModifiedCount();
  }catch (MongoException me) {
         String msg = "error in updateOne " + me;
         LOG.error(msg);
         return 99;
     }
} // end method

public static boolean insertOne(HelpView helpV) { // called from SaveHelpFile(), create()
      LOG.debug("entering insertOne");
     // https://mongodb.github.io/mongo-java-driver/5.0/apidocs/mongodb-driver-core/com/mongodb/client/model/Updates.html
   try{ 
        collection.insertOne(new Document()
                        .append("_id", helpV.getId())
                        .append("helpViewText", helpV.getHelpViewText())
                        .append("helpViewLanguage", helpV.getHelpViewLanguage())
                        .append("helpViewModificationDate", LocalDateTime.now())
          );
    LOG.debug("inserted One= " + read(helpV));
 return true;
     }catch (MongoException me) {
         String msg = "error in insertOne " + me;
         LOG.error(msg);
         showMessageFatal(msg);
         return false;
     }
//   }// end try resources
} // end method

public boolean create(HelpView helpV) {
  try{
         LOG.debug("entering create for = " + helpV);
   //      LOG.debug("helpV = " + helpV);
         LOG.debug("helpView = " + helpView);
      helpView = helpV;
         LOG.debug("helpView after = " + helpView);
      Bson query = eq("_id",helpView.getId());
      if(collection.countDocuments(query) > 0){
   //       LOG.debug("existing situation found ==> update !!");
          long l = updateOne(helpView);
          LOG.debug("result update = " + l);
          return true;
      }else{
   //       LOG.debug("NO existing situation found ==> create !!");
          insertOne(helpView);
          LOG.debug("inserted");
          return true;
      }
  }catch (MongoException me) {
        String msg = "error in create " + me;
        LOG.error(msg);
        return false;
     }
 } //end method create
  
public static HelpView read(HelpView helpView) {    
      LOG.debug("entering read for HelpView = " + helpView);
 try {
      // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/findOne/
        Bson query = eq("_id",helpView.getId());
        Document document = collection.find(query).first();  // ex: _id = club
        if(document == null) {
            String msg = "<p>No Help document found for view = " + helpView.getId();
               LOG.debug(msg);
            helpView.setHelpViewText(msg);
        }else{
               LOG.debug("Help document found in Mongo = " + NEW_LINE + document);
            helpView.setHelpViewText(document.getString("helpViewText"));
            helpView.setHelpViewLanguage(document.getString("helpViewLanguage"));  // normalement FR only
            String s = document.get("helpViewModificationDate").toString();
               LOG.debug("mongo document ModificationDate = " + s);
            LocalDateTime ldt = LocalDateTime.parse(s, mongo_formatter); // mod 17-07-2024 format UTC default in Mongo
               LOG.debug("localDateTime modification format UTC = " + ldt);
            helpView.setHelpViewModificationDate(ldt); 
        }
    return helpView;
 }catch (MongoException me) {
         String msg = "Unable to read due to this error: " + me;
         LOG.error(msg);
         showMessageFatal(msg);
         return null;
  }
} // end method

  public static Bson userFilter(HelpView helpView){
      //DRY : Don't Repeat Yourself
       return Filters.and(Filters.eq("_id", helpView.getId())//,
                  //        Filters.eq("helpViewText", helpView.getHelpViewText()),
                  //        Filters.eq("helpView Language", helpView.getHelpViewLanguage())
                         );
  }
    
  // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples/command/
  public void utilities() {
        // Replace the uri string with your MongoDB deployment's connection string
  //     String uri = "<connection string uri>";
    try (MongoClient mongoClient = MongoClients.create()) {
            MongoDatabase database = mongoClient.getDatabase("golflc");
            try {
                
  // create index pas nécessaire _id est indexé par le système
      //          MongoCollection<Document> collection = database.getCollection("HELP-VIEW");
      //          String resultCreateIndex = collection.createIndex(Indexes.ascending("loggingIdPlayer", "loggingIdRound","loggingType"));
      //             LOG.debug("createIndex result = : " + resultCreateIndex);
 
                Bson command = new BsonDocument("dbStats", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                LOG.debug("dbStats: " + commandResult.toJson());
            } catch (MongoException me) {
                LOG.error("An error occurred in statistics: " + me);
            }
        }
    } // end method
  
 public String SaveHelpFile(){// called in editor_help_mongo.xhtml
 try{
         LOG.debug("entering SaveHelpFile for = " + helpView);
    helpView.setHelpViewModificationDate(LocalDateTime.now());
    if(create(helpView)){
         String msg = "modification OK for help document : " + helpView.getId() + ".xhtml";
         LOG.info(msg);
         showMessageInfo(msg);
    }else{
        String msg = "ERROR modification KO KO KO for help document " + helpView.getId() + ".xhtml";
        LOG.error(msg);
        showMessageFatal(msg);
    }
       return null;

}catch (Exception ex){
         String msg = " <br/>££ Exception in BackCurrentHelpFileName() " + ex;
         LOG.error(msg);
         return null;
 }
} // end method
  
 public String BackCurrentHelpFile(){
 try{  // file.xhtml sur laquelle on était positionné avant WriteHelp
          LOG.debug("back to helpView " + helpView);
       // work around : changement vers 'en' se fait dans le process : pas trouvé pourquoi 
        //  ActiveLocale.setLanguageTag("fr");
          languageController.setLanguage("fr"); // fix multi-user 2026-03-07
     return helpView.getId() + ".xhtml?faces-redirect=true";
}catch (Exception ex){
         String msg = " <br/>££ Exception in BackCurrentHelpFileName() " + ex;
         LOG.error(msg);
         return null;
 }
} // end method
 
public String showHelpFile(){  // coming from header.xhtml pour HelpWrite (afficher le contenu actuel)
 try{  
       // HelpView helpV = currentHelpFile();
      //  helpView = Controllers.MongoHelpController.read(helpV);
        helpView = read(currentHelpFile());
     return("editor_help_mongo.xhtml?faces-redirect=true");
}catch (Exception ex){
         String msg =  "<br/>££ Exception in showHelpFile()" ;
         LOG.error(msg);
         showMessageInfo(msg);
         return null;
}    
} // end method
 
public HelpView currentHelpFile() { // fix multi-user 2026-03-07 — removed static
 try{  // file.xhtml pour laquelle on est positionné
        HelpView helpView = new HelpView();
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();// is "/welcome.xhtml"
        viewId = utils.LCUtil.removeFileExtension(viewId.substring(1), true);  // substring(1) = remove first character "/"
            LOG.debug("viewId for xhtml file = " + viewId); // is "welcome"
        helpView.setId(viewId);
        helpView.setHelpViewLanguage(languageController.getLanguage()); // fix multi-user 2026-03-07
            LOG.debug("languageController.getLanguage() = " + languageController.getLanguage());
      //  helpView = Controllers.MongoHelpController.read(helpView);
      //     LOG.debug("helpView returned from read = " + helpView);
      //  return read(helpView);
        return read(helpView);
}catch (Exception ex){
         String msg =  "<br/>££ Exception in currentHelpFile()" ;
         LOG.error(msg);
         showMessageInfo(msg);
         return null;
}    
} // end method

  public String ReadHelpFile() { // fix multi-user 2026-03-07 — removed static
 try{
     HelpView helpV = currentHelpFile();
     if(helpV.getHelpViewLanguage() == null){
         LOG.debug("ReadHelpFile := language forced to en");
         helpV.setHelpViewLanguage("en");
     }

     if("fr".equalsIgnoreCase(helpV.getHelpViewLanguage())){  // pas de traduction nécessaire !! les textes de base sont en français
        return helpV.getHelpViewText();
     }else{
        return translation.FileTranslation.translateList(Arrays.asList(helpV.getHelpViewText()), helpV.getHelpViewLanguage());
     }
}catch (Exception e){
         String msg = "<br/>££ Exception in ReadHelpFile() " + e;
         LOG.error(msg);
         showMessageFatal(msg);
         return msg;
}  

} // end method
  
/*
    void main() {
       LOG.debug("starting main");
     HelpView helpview = new HelpView();
   //  helpview.setId("club");
     helpview.setId("test_cotisation_4");
 //  helpview.setId("aaaa)");
     helpview.setHelpViewText("CREATED this is the text from main");
     helpview.setHelpViewLanguage("es");
     helpview.setHelpViewModificationDate(LocalDateTime.now());
     boolean bo = insertOne(helpview);
       LOG.debug("\n\ninserted  result = " + bo);
 //   boolean a = new MongoHelpController().find(helpview);
  //       LOG.debug("\n\nfind  result = " + a);
 //  long lo = new MongoHelpController().delete(helpview);
 //      LOG.debug("\n\ndelete  result = " + lo);
//  boolean b = new MongoHelpController().create(helpview);
//        LOG.debug("result create = " + b);
//  long l = new MongoHelpController().update(helpview);
 //      LOG.debug("\n\n  update result = " + l);
/// à modifier    String s = new MongoHelpController().read(helpview);
///     LOG.debug("\n\nread result = " + s);
   //   new MongoHelpController().utilities();
   //   LOG.debug("result main = " + b);
} // end main
*/
} // end class