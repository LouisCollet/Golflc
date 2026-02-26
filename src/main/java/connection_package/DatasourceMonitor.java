
package connection_package;


import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import static interfaces.Log.LOG;

import java.net.InetAddress;

/**
 * Monitor WildFly datasource pour les tests
 * Permet de vérifier automatiquement les fuites de connexion
 */
public class DatasourceMonitor {

    private final String host;
    private final int port;
    private final String datasourceName;

    public DatasourceMonitor(String host, int port, String datasourceName) {
        this.host = host;
        this.port = port;
        this.datasourceName = datasourceName;
    }

    /**
     * Lit les statistiques du datasource via management API
     */
    public DatasourceStats getStats() {
        try (ModelControllerClient client = ModelControllerClient.Factory.create(
                InetAddress.getByName(host), port)) {

            ModelNode op = new ModelNode();
            op.get("operation").set("read-resource");
            op.get("address").add("subsystem", "datasources");
            op.get("address").add("data-source", datasourceName);
            op.get("include-runtime").set(true);

            ModelNode result = client.execute(op);

            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
                ModelNode res = result.get("result", "statistics");
                return new DatasourceStats(
                        res.get("ActiveCount").asInt(),
                        res.get("AvailableCount").asInt(),
                        res.get("InUseCount").asInt(),
                        res.get("CreatedCount").asInt()
                );
            } else {
                LOG.error("Erreur read-resource : " + result);
            }
        } catch (Exception e) {
            LOG.error("Impossible de se connecter au management API", e);
        }
        return new DatasourceStats(-1, -1, -1, -1);
    }

    /**
     * Vérifie qu'une opération n'engendre pas de fuite de connexion
     */
    public void assertNoLeak(Runnable operation) {
        DatasourceStats before = getStats();
        LOG.debug("Datasource before operation: " + before);

        operation.run();

        DatasourceStats after = getStats();
        LOG.debug("Datasource after operation: " + after);

        if (before.active != after.active) {
            throw new IllegalStateException(
                String.format(
                        "Fuite de connexion détectée ! Active avant: %d, après: %d",
                        before.active, after.active));
        }
    }

    /**
     * Simple POJO pour stocker les statistiques
     */
    public static class DatasourceStats {
        public final int active;
        public final int available;
        public final int inUse;
        public final int created;

        public DatasourceStats(int active, int available, int inUse, int created) {
            this.active = active;
            this.available = available;
            this.inUse = inUse;
            this.created = created;
        }

        @Override
        public String toString() {
            return String.format("Active=%d, Available=%d, InUse=%d, Created=%d",
                    active, available, inUse, created);
        }
    }
}
