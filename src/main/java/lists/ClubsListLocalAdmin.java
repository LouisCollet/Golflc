package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Club;
import entite.Player;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.ClubRowMapper;
import static interfaces.Log.LOG;

@Named
@ApplicationScoped
public class ClubsListLocalAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public static final String QUERY = """
            SELECT *
            FROM club
            WHERE club.ClubLocalAdmin = ?
            """;

    private transient Cache<Integer, List<Club>> cache;

    public ClubsListLocalAdmin() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    } // end method

    public List<Club> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        int adminId = localAdmin.getIdplayer();
        List<Club> cached = cache.getIfPresent(adminId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        List<Club> result = dao.queryList(QUERY, new ClubRowMapper(), adminId);
        if (result.isEmpty()) {
            LOG.warn("empty result list for adminId = {}", adminId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(adminId, result);
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
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player localAdmin = new Player();
        localAdmin.setIdplayer(324715);
        List<Club> lp = list(localAdmin);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
