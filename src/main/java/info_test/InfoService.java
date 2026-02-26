
package info_test;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class InfoService {

    @Inject
    private InfoExecutor executor;

    @Inject
    private NetworkInfoProvider ip;

    @Inject
    private GeoIpInfoProvider geo;

    @Inject
    private MySqlInfoProvider mysql;

    @Inject
    private MongoInfoProvider mongo;

    public String ip() {
        LOG.debug("InfoService.ip()");
        return executor.execute(ip, 2500);
    }

    public String geo() {
        return executor.execute(geo, 1500);
    }

    public String mysql() {
        return executor.execute(mysql, 2000);
    }

    public String mongo() {
        return executor.execute(mongo, 2000);
    }
}