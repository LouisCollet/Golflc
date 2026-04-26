package lists;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.Tee;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;
import rowmappers.TeeRowMapper;

@Named
@ApplicationScoped
public class RegisterResultList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<ECourseList> liste = null;

    public RegisterResultList() { }

    public List<ECourseList> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with player = " + player);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            WITH selection AS (
                SELECT * FROM player
                    INNER JOIN inscription
                        ON inscription.InscriptionIdPlayer = player.idplayer
                    INNER JOIN round
                        ON round.idround = inscription.InscriptionIdRound
                    WHERE player.idplayer = ?
            )
            SELECT * FROM selection
                JOIN tee
                    ON tee.idtee = selection.InscriptionIdTee
                JOIN course
                    ON course.idcourse = selection.course_idcourse
                JOIN club
                    ON club.idclub = course.club_idclub
                ORDER BY selection.RoundDate DESC
                LIMIT 30
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Course> courseMapper = new CourseRowMapper();
        RowMapper<Tee> teeMapper = new TeeRowMapper();
        RowMapper<Player> playerMapper = new PlayerRowMapper();
        RowMapperRound<Round> roundMapper = new RoundRowMapper();

        liste = dao.queryList(query, rs -> {
            Club club = clubMapper.map(rs);
            return ECourseList.builder()
                .club(club)
                .course(courseMapper.map(rs))
                .player(playerMapper.map(rs))
                .round(roundMapper.map(rs, club))
                .tee(teeMapper.map(rs))
                .build();
        }, player.getIdplayer());

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
            liste.forEach(item -> LOG.debug(methodName + " - " + item));
        }
        return liste;
    } // end method

    public List<ECourseList> getListe()                          { return liste; }
    public void              setListe(List<ECourseList> liste)   { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // Player player = new Player();
        // player.setIdplayer(324715);
        // var ecl = list(player);
        // LOG.debug("from main, ecl = " + ecl.size());
        LOG.debug("from main, RegisterResultList = ");
    } // end main
    */

} // end class
