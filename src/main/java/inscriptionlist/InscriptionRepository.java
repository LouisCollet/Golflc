package inscriptionlist;

import connection_package.DBConnection2;
import entite.*;

import entite.composite.ECourseList;
import rowmappers.*;
import java.sql.*;
import java.util.*;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Repository pour Inscription/ECourseList avec cache TTL
 */
public class InscriptionRepository extends AbstractCachedRepository<ECourseList> {

    public InscriptionRepository(long ttlMillis) {
        super(ttlMillis);
    }

    /**
     * Charge la liste depuis la DB
     */
    @Override
    protected List<ECourseList> loadFromDatabase(Connection conn) throws SQLException {

        final String methodName = LCUtil.getCurrentMethodName();

        final String query = """
            WITH selection AS (
                SELECT * FROM round
            )
            SELECT *
            FROM selection
            JOIN course
                ON course.idcourse = selection.course_idcourse
            JOIN club
                ON club.idclub = course.club_idclub
            ORDER BY roundDate DESC
            LIMIT 30;
        """;

        List<ECourseList> result = new ArrayList<>();

    try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapperRound<Round> roundMapper = new RoundRowMapper();
            if (rs.next()) {
                Club club = clubMapper.map(rs);
             //   ECourseList item = new ECourseList();
                ECourseList ecl = ECourseList.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
               // .player(playerMapper.map(rs))
               // .inscription(inscriptionMapper.map(rs))
                    .round(roundMapper.map(rs,club))
              //  .tee(teeMapper.map(rs))
            .build();
            //    Club club = clubMapper.map(rs);
             //   item.setClub(club);
            //    item.setCourse(courseMapper.map(rs));
            //    item.setRound(roundMapper.map(rs, club));

                result.add(ecl);
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }

        if (result.isEmpty()) {
            String msg = "££ Empty Result List in " + methodName;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }

        return result;
    }

    /**
     * Méthode test rapide
     */
    public static void main(String[] args) {
        try (Connection conn = DBConnection2.getConnection()) {
// pour utiliser ttl
            InscriptionRepository repo = new InscriptionRepository(30_000); // TTL 30 sec
            repo.setSlidingTtl(true); // active sliding TTL

            List<ECourseList> p1 = repo.list(conn);
            LOG.debug("number extracted = " + p1.size());
            LOG.debug("Inscription list = " + p1.toString());

            // Purge manuelle test
            repo.purge();
            LOG.debug("Cache purged manually");

        } catch (Exception e) {
            LOG.error("Error in main: " + e.getMessage(), e);
        }
    }
}
