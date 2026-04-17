
package Controllers;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import entite.HelpView;
import static interfaces.GolfInterface.mongo_formatter;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.PreDestroy;
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
public class MongoHelpController implements Serializable {

    private static final long serialVersionUID = 1L;  // ✅ ajouté — nécessaire pour @SessionScoped passivation

    private static final String COLLECTION_NAME = "help_view";
    private static final String DATABASE_NAME = "golflc";

    private final MongoClient mongoClient = MongoClients.create();  // one per session — closed in @PreDestroy
    private final MongoCollection<Document> collection = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);

    @Inject private LanguageController languageController; // fix multi-user 2026-03-07
    private entite.HelpView helpView;

    public MongoHelpController() { }   // constructor

    @PreDestroy
    public void tearDown() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering — closing MongoClient for session");
        mongoClient.close();
        LOG.debug("MongoClient closed");
    } // end method

    public HelpView getHelpView() {
        return helpView;
    }

    public void setHelpView(HelpView helpView) {
        this.helpView = helpView;
    }

    // ========================================
    // HELPERS
    // ========================================

    private Bson userFilter(HelpView hv) {  // ✅ private — usage interne uniquement
        return Filters.eq("_id", hv.getId());
    } // end method

    // ========================================
    // CRUD
    // ========================================

    public long deleteOne(HelpView helpView) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for id = {}", helpView.getId());
        try {
            DeleteResult result = collection.deleteOne(userFilter(helpView));  // ✅ DRY via userFilter()
            LOG.debug("deletedCount = {}", result.getDeletedCount());
            return result.getDeletedCount();
        } catch (MongoException me) {
            LOG.error("Unable to delete: {}", me);
            return 99;
        }
    } // end method

    public long updateOne(HelpView helpView) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for id = {}", helpView.getId());
        try {
            Bson update = Updates.combine(
                    Updates.set("helpViewText", helpView.getHelpViewText()),
                    Updates.set("helpViewLanguage", helpView.getHelpViewLanguage()),
                    Updates.set("helpViewModificationDate", LocalDateTime.now())
            );
            UpdateResult result = collection.updateOne(userFilter(helpView), update);
            LOG.debug("modifiedCount = {}", result.getModifiedCount());
            return result.getModifiedCount();
        } catch (MongoException me) {
            LOG.error("error in updateOne: {}", me);
            return 99;
        }
    } // end method

    public boolean insertOne(HelpView helpV) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for id = {}", helpV.getId());
        try {
            collection.insertOne(new Document()
                    .append("_id", helpV.getId())
                    .append("helpViewText", helpV.getHelpViewText())
                    .append("helpViewLanguage", helpV.getHelpViewLanguage())
                    .append("helpViewModificationDate", LocalDateTime.now())
            );
            LOG.debug("inserted: {}", read(helpV));
            return true;
        } catch (MongoException me) {
            LOG.error("error in insertOne: {}", me);
            showMessageFatal(methodName + " - error in insertOne: " + me);
            return false;
        }
    } // end method

    public boolean create(HelpView helpV) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for = {}", helpV);
        // ⚠️ side effect intentionnel — synchronise l'état de session avec le document en cours d'édition
        helpView = helpV;
        LOG.debug("helpView updated to = {}", helpView);
        try {
            if (collection.countDocuments(userFilter(helpView)) > 0) {  // ✅ DRY via userFilter()
                LOG.debug("existing document found ==> update");
                long l = updateOne(helpView);
                LOG.debug("update result = {}", l);
            } else {
                LOG.debug("no existing document found ==> insert");
                insertOne(helpView);
            }
            return true;
        } catch (MongoException me) {
            LOG.error("error in create: {}", me);
            return false;
        }
    } // end method

    public HelpView read(HelpView helpView) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for HelpView = {}", helpView);
        try {
            Document document = collection.find(userFilter(helpView)).first();
            if (document == null) {
                String msg = "<p>No Help document found for view = " + helpView.getId();
                LOG.debug("- {}", msg);
                helpView.setHelpViewText(msg);
            } else {
                LOG.debug("Help document found in Mongo = {}", NEW_LINE + document);
                helpView.setHelpViewText(document.getString("helpViewText"));
                helpView.setHelpViewLanguage(document.getString("helpViewLanguage"));
                // ✅ null-guard — champ absent dans les documents legacy
                Object rawDate = document.get("helpViewModificationDate");
                if (rawDate != null) {
                    LocalDateTime ldt = LocalDateTime.parse(rawDate.toString(), mongo_formatter);
                    LOG.debug("modificationDate parsed = {}", ldt);
                    helpView.setHelpViewModificationDate(ldt);
                } else {
                    LOG.debug("helpViewModificationDate absent from document — skipped");
                }
            }
            return helpView;
        } catch (MongoException me) {
            LOG.error("Unable to read: {}", me);
            showMessageFatal(methodName + " - Unable to read: " + me);
            return helpView;  // ✅ retourne l'objet tel quel (pas null) — appelant peut afficher le message d'erreur
        }
    } // end method

    // ========================================
    // UTILITIES
    // ========================================

    public void utilities() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // ✅ utilise le MongoClient d'instance — plus de client éphémère
            Document commandResult = mongoClient.getDatabase(DATABASE_NAME)
                    .runCommand(new BsonDocument("dbStats", new BsonInt64(1)));
            LOG.debug("dbStats: {}", commandResult.toJson());
        } catch (MongoException me) {
            LOG.error("An error occurred in utilities: {}", me);
        }
    } // end method

    // ========================================
    // JSF ACTIONS
    // ========================================

    public String SaveHelpFile() {  // called in editor_help_mongo.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for = {}", helpView);
        try {
            helpView.setHelpViewModificationDate(LocalDateTime.now());
            if (create(helpView)) {
                String msg = "modification OK for help document : " + helpView.getId() + ".xhtml";
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = "ERROR modification KO for help document " + helpView.getId() + ".xhtml";
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return null;
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + " : " + ex;  // ✅ nom de méthode corrigé
            LOG.error(msg);
            return null;
        }
    } // end method

    public String BackCurrentHelpFile() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering — back to helpView {}", helpView);
        try {
            // workaround — language reverts to 'en' during save flow; root cause unknown
            languageController.setLanguage("fr"); // fix multi-user 2026-03-07
            return helpView.getId() + ".xhtml?faces-redirect=true";
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + " : " + ex;  // ✅ nom de méthode corrigé
            LOG.error(msg);
            return null;
        }
    } // end method

    public String showHelpFile() {  // called from header.xhtml for HelpWrite
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            helpView = read(currentHelpFile());
            return "editor_help_mongo.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + " : " + ex;
            LOG.error(msg);
            showMessageInfo(msg);
            return null;
        }
    } // end method

    public HelpView currentHelpFile() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            HelpView hv = new HelpView();
            String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId(); // "/welcome.xhtml"
            viewId = utils.LCUtil.removeFileExtension(viewId.substring(1), true);  // remove leading "/"
            LOG.debug("viewId = {}", viewId);
            hv.setId(viewId);
            hv.setHelpViewLanguage(languageController.getLanguage()); // fix multi-user 2026-03-07
            LOG.debug("language = {}", languageController.getLanguage());
            return read(hv);
        } catch (Exception ex) {
            String msg = "Exception in " + methodName + " : " + ex;
            LOG.error(msg);
            showMessageInfo(msg);
            return null;
        }
    } // end method

    public String ReadHelpFile() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            HelpView helpV = currentHelpFile();
            if (helpV.getHelpViewLanguage() == null) {
                LOG.debug("language forced to en");
                helpV.setHelpViewLanguage("en");
            }
            if ("fr".equalsIgnoreCase(helpV.getHelpViewLanguage())) {
                return helpV.getHelpViewText();  // pas de traduction — textes de base en français
            } else {
                return translation.FileTranslation.translateList(
                        Arrays.asList(helpV.getHelpViewText()), helpV.getHelpViewLanguage());
            }
        } catch (Exception e) {
            String msg = "Exception in " + methodName + " : " + e;
            LOG.error(msg);
            showMessageFatal(msg);
            return msg;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        HelpView helpview = new HelpView();
        helpview.setId("test_cotisation_4");
        helpview.setHelpViewText("CREATED this is the text from main");
        helpview.setHelpViewLanguage("es");
        helpview.setHelpViewModificationDate(LocalDateTime.now());
        boolean bo = insertOne(helpview);
        LOG.debug("inserted result = {}", bo);
    } // end main
    */

} // end class
