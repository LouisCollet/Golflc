
package Controllers;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import static exceptions.LCException.handleGenericException;

/**
 * Controller MongoDB pour les calculs de logging.
 * ✅ Réutilise un seul MongoClient (singleton @ApplicationScoped)
 * ✅ @PreDestroy ferme le client proprement
 * Refactored 2026-03-18 — security audit D6
 */
@ApplicationScoped
public class MongoCalculationsController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATABASE_NAME = "golflc";
    private static final String COLLECTION_NAME = "logging_calculations";

    // ✅ Un seul MongoClient réutilisé partout — @ApplicationScoped garantit le singleton
    private final MongoClient mongoClient = MongoClients.create();

    // ✅ CodecRegistry initialisé une seule fois au lieu d'être recréé à chaque create()
    private final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    );

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering — creating index on {}", COLLECTION_NAME);
        try {
            String idx = getCollection().createIndex(
                    Indexes.ascending("loggingIdPlayer", "loggingIdRound", "loggingType"));
            LOG.debug("index result: {}", idx);
        } catch (MongoException me) {
            LOG.error("Unable to create index: {}", me);
        }
    } // end method

    @PreDestroy
    public void cleanup() {
        LOG.debug("MongoCalculationsController - closing MongoClient");
        mongoClient.close();
    } // end method

    // ========================================
    // HELPERS
    // ========================================

    private MongoDatabase getDatabase() {
        return mongoClient.getDatabase(DATABASE_NAME);
    } // end method

    private MongoCollection<Document> getCollection() {
        return getDatabase().getCollection(COLLECTION_NAME);
    } // end method

    private Bson userFilter(LoggingUser loggingUser) {
        return Filters.and(
                Filters.eq("loggingIdPlayer", loggingUser.getLoggingIdPlayer()),
                Filters.eq("loggingIdRound", loggingUser.getLoggingIdRound()),
                Filters.eq("loggingType", loggingUser.getLoggingType())
        );
    } // end method

    // ========================================
    // CRUD
    // ========================================

    public long delete(LoggingUser loggingUser) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            DeleteResult result = getCollection().deleteMany(userFilter(loggingUser));
            LOG.debug("deletedCount = {}", result.getDeletedCount());
            return result.getDeletedCount();
        } catch (MongoException me) {
            LOG.error("Unable to delete: {}", me);
            return 99;
        }
    } // end method

    public long update(LoggingUser loggingUser) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Bson update = Updates.combine(
                    Updates.set("loggingCalculations", loggingUser.getLoggingCalculations()),
                    Updates.set("loggingModificationDate", LocalDateTime.now())
            );
            UpdateResult result = getCollection().updateOne(userFilter(loggingUser), update);
            LOG.debug("modifiedCount = {}", result.getModifiedCount());
            return result.getModifiedCount();
        } catch (MongoException me) {
            LOG.error("Unable to update: {}", me);
            return 99;
        }
    } // end method

    public boolean create(LoggingUser loggingUser) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for = {}", loggingUser);

        // getText() est basé sur ThreadLocal — jamais null (initialisé avec "start text")
        loggingUser.setLoggingCalculations(Controllers.LoggingUserController.getText());

        LOG.debug("create or Update? with logging = {}", loggingUser);
        if (find(loggingUser)) {
            LOG.debug("existing situation found ==> update");
            update(loggingUser);
            return true;
        }
        LOG.debug("NO existing situation found ==> create");

        try {
            MongoCollection<LoggingUser> collection = getDatabase()
                    .getCollection(COLLECTION_NAME, LoggingUser.class)
                    .withCodecRegistry(codecRegistry);  // ✅ champ réutilisé
            collection.insertOne(loggingUser);
            return true;
        } catch (MongoException me) {
            LOG.error("Unable to create: {}", me);
            return false;
        }
    } // end method

    public String read(LoggingUser loggingUser) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for {}", loggingUser);
        try {
            Document document = getCollection().find(userFilter(loggingUser)).first();
            if (document == null) {
                LOG.debug("No calculations details found");
                return "No calculations details found !!";
            }
            LOG.debug("found document: {}", document);
            return document.getString("loggingCalculations");
        } catch (MongoException me) {
            LOG.error("Unable to read: {}", me);
            return "";
        }
    } // end method

    public boolean find(LoggingUser loggingUser) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering for {}", loggingUser);
        try {
            return getCollection().countDocuments(userFilter(loggingUser)) > 0;
        } catch (MongoException me) {
            LOG.error("Unable to find: {}", me);
            return false;
        }
    } // end method

    // ========================================
    // UTILITIES
    // ========================================

    public void utilities() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Document commandResult = getDatabase().runCommand(new BsonDocument("dbStats", new BsonInt64(1)));
            LOG.debug("database Stats: {}", commandResult.toJson());

            commandResult = getDatabase().runCommand(new BsonDocument("buildinfo", new BsonInt64(1)));
            LOG.debug("buildinfo: {}", commandResult.toJson());

            commandResult = getDatabase().runCommand(new Document("collStats", COLLECTION_NAME));
            LOG.debug("collection Stats: {}", commandResult.toJson());
        } catch (MongoException me) {
            LOG.error("An error occurred in utilities: {}", me);
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
