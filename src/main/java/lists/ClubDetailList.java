package lists;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import entite.Club;
import entite.Course;
import entite.Tee;
import entite.composite.ECourseList;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import static interfaces.Log.LOG;

@ApplicationScoped
public class ClubDetailList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private transient Cache<Integer, List<ECourseList>> cache;

    public ClubDetailList() { }

    @PostConstruct
    void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(50)
                .build();
    } // end method

    public List<ECourseList> list(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with club = {}", club);

        int clubId = club.getIdclub();
        List<ECourseList> cached = cache.getIfPresent(clubId);
        if (cached != null) {
            LOG.debug("returning cached list size = {}", cached.size());
            return cached;
        }

        final String query = """
            SELECT *
            FROM club, course, tee
            WHERE club.idclub = course.club_idclub
               AND tee.course_idcourse = course.idcourse
               AND idclub = ?
            ORDER by idclub, idcourse, teegender, teestart
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();

        List<ECourseList> result = dao.queryList(query, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .course(courseMapper.map(rs))
                .tee(teeMapper.map(rs))
                .build(),
                clubId);

        if (result.isEmpty()) {
            LOG.warn("empty result list for clubId = {}", clubId);
        } else {
            LOG.debug("list size = {}", result.size());
            cache.put(clubId, result);
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
        Club club = new Club();
        club.setIdclub(101);
        List<ECourseList> ec = new ClubDetailList().list(club);
        LOG.debug("from main, ec = {}", ec);
    } // end main
    */

} // end class
