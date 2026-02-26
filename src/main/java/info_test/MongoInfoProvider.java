
package info_test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;

@ApplicationScoped
public class MongoInfoProvider implements InfoProvider {

    @Override
    public String name() {
        return "MongoDB";
    }

    @Override
    public String get() {
        try (MongoClient client =
                MongoClients.create("mongodb://localhost")) {

            Document d = client.getDatabase("admin")
                               .runCommand(new Document("buildInfo", 1));

            return "MongoDB " + d.getString("version");
        }
    }
}
