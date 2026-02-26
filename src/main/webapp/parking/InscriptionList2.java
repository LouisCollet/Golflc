package lists;

import inscriptionlist.AbstractCachedRepository;
import entite.*;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

/**
 * Repository InscriptionList2 thread-safe avec cache TTL et sliding TTL
 */
public class InscriptionList2 extends AbstractCachedRepository<ECourseList> {

// --- constructeur ---
    public InscriptionList2(long ttlMillis) {
        super(ttlMillis);
        // ✅ Sliding TTL activé après construction dains main par ex
    }

    // --- charge la liste depuis la DB ---     

    @Override // le nom de methode loadFromDatabase est obligatoire
    protected List<ECourseList> loadFromDatabase(Connection conn) throws SQLException {

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
          Club club = new Club();
          club = clubMapper.map(rs);
            while(rs.next()) {
                ECourseList ecl = ECourseList.builder()
                    .club(clubMapper.map(rs))
                    .course(courseMapper.map(rs))
                //    .handicapIndex(handicapIndexMapper.map(rs))
                //    .inscription(inscriptionMapper.map(rs))
                    .round(roundMapper.map(rs,club))
                            
                .build();
             //   Club club = clubMapper.map(rs);
              //  item.setClub(club);
             //   item.setCourse(courseMapper.map(rs));
             //   item.setRound(roundMapper.map(rs, club));

                result.add(ecl);
            }

        } catch (SQLException e) {
            LOG.error("SQLException loading ECourseList", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Exception loading ECourseList", e);
            throw new SQLException(e);
        }

        if (result.isEmpty()) {
            LOG.error("££ Empty Result List in InscriptionList2");
        }

        return result;
    }

    public static void main(String[] args) {
        InscriptionList2 repo = new InscriptionList2(30_000); // TTL 30 sec
        repo.setSlidingTtl(true);  // ✅ safe, après construction
        try (Connection conn = connection_package.DBConnection2.getConnection()) {

            List<ECourseList> liste = repo.list(conn);
            LOG.debug("Nombre d'éléments récupérés = " + liste.size());
            LOG.debug("Liste = " + liste);

            // Purge manuelle test
            repo.purge();
            LOG.debug("Cache purgé manuellement");

            // Changement TTL dynamique
            repo.setTtlMillis(60_000); // 60 sec
            LOG.debug("TTL mis à jour dynamiquement");

        } catch (Exception e) {
            LOG.error("Erreur lors du test du repository", e);
        }
    }
}
