package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Hole;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.HoleRowMapper;
import static interfaces.Log.LOG;

@Named
@ApplicationScoped
public class HoleList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<Hole>> cache;

    public HoleList() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(200)
                .build();
    } // end method

    public List<Hole> listForTee(final int teeId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for teeId = {}", teeId);

        List<Hole> cached = cache.getIfPresent(teeId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        final String query = """
                SELECT *
                FROM hole
                WHERE hole.tee_idtee = ?
                ORDER BY HoleNumber
                """;

        List<Hole> result = dao.queryList(query, new HoleRowMapper(), teeId);

        if (result.isEmpty()) {
            LOG.warn("empty result list for teeId = {}", teeId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(teeId, result);
        }
        return result;
    } // end method

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache.invalidateAll();
        LOG.debug("cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        int teeId = 98;
        List<Hole> holes = new HoleList().listForTee(teeId);
        LOG.debug("hole list for tee = {}", holes.size());
        holes.forEach(hole -> LOG.debug("Hole: {} - Par: {}", hole.getHoleNumber(), hole.getHolePar()));
    } // end main
    */

} // end class
