package lists;

import entite.Classment;
import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import exceptions.InvalidRoundException;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

@Named
@ApplicationScoped
public class ParticipantsRoundList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Inject private read.ReadClassment readClassment;

    // cache intentionally disabled — always recomputes (scores change in real time)
    private List<ECourseList> liste = null;

    public ParticipantsRoundList() { }

    public List<ECourseList> list(final Round round) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Round = " + round);
        LOG.debug("with holes = " + round.getRoundHoles());

        if (round == null) {
            LOG.error(methodName + " - Round cannot be null");
            throw new IllegalArgumentException("Round cannot be null");
        }

        // Validation : 9 ou 18 trous requis
        if (round.getRoundHoles() != 9 && round.getRoundHoles() != 18) {
            LOG.warn("Round {} has {} holes, expected 9 or 18", round.getIdround(), round.getRoundHoles());
            throw new InvalidRoundException(
                "Le round doit avoir 9 ou 18 trous. Trouvé : " + round.getRoundHoles()
            );
        }

        final String query = """
            SELECT *
            FROM player
            JOIN round
               ON round.idround = ?
            JOIN course
               ON round.course_idcourse = course.idcourse
            JOIN club
               ON course.club_idclub = club.idclub
            JOIN player_has_round
               ON  InscriptionIdPlayer = player.idplayer
               AND InscriptionIdRound = round.idround
            ORDER BY player_has_round.InscriptionFinalResult, InscriptionMatchplayTeam ASC;
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Course> courseMapper = new CourseRowMapper();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                while (rs.next()) {
                    Player player = playerMapper.map(rs);
                    Club club = clubMapper.map(rs);
                    Round r = roundMapper.map(rs, club);
                    Classment classment = readClassment.read(player, r);
                    LOG.debug("classment = " + classment);
                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .course(courseMapper.map(rs))
                            .inscription(inscriptionMapper.map(rs))
                            .round(r)
                            .player(player)
                            .classment(classment)
                            .build();
                    liste.add(ecl);
                } // end while
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty list for round=" + round.getIdround());
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
                LOG.debug("ending with liste = {}", Arrays.deepToString(liste.toArray()));
                // https://stackoverflow.com/questions/369512/how-to-compare-objects-by-multiple-fields
                liste.sort(Comparator.comparingInt((ECourseList p) -> p.classment().getTotalPoints() != null ? p.classment().getTotalPoints() : 0).reversed()
                        .thenComparingInt((ECourseList p) -> p.classment().getLast9() != null ? p.classment().getLast9() : 0).reversed()
                        .thenComparingInt((ECourseList p) -> p.classment().getLast6() != null ? p.classment().getLast6() : 0).reversed()
                        .thenComparingInt((ECourseList p) -> p.classment().getLast3() != null ? p.classment().getLast3() : 0).reversed()
                        .thenComparingInt((ECourseList p) -> p.classment().getLast1() != null ? p.classment().getLast1() : 0).reversed());
                liste.forEach(item -> LOG.debug("liste AFTER sort = " + item.player().getPlayerFirstName()
                        + " / " + item.player().getIdplayer() + " / " + item.classment()));
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> getListe()                       { return liste; }
    public void              setListe(List<ECourseList> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Round round = new Round();
        round.setIdround(767); // doit avoir 18 trous
        round.setRoundHoles(18);
        List<ECourseList> p1 = list(round);
        LOG.debug("inscription list = " + p1.toString());
        p1.forEach(item -> LOG.debug("Participants Stableford list = "
                + item.classment() + "/" + item.player().getPlayerLastName()));
    } // end main
    */

} // end class
