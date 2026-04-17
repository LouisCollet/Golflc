package lists;

import entite.Classment;
import entite.Club;
import entite.CompetitionDescription;
import entite.Course;
import entite.HandicapIndex;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.CourseRowMapper;
import rowmappers.InscriptionRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapper;
import rowmappers.RowMapperRound;

import static exceptions.LCException.handleGenericException;

@Named
@ApplicationScoped
public class ParticipantsStablefordCompetitionList implements Serializable, interfaces.Log {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private read.ReadClassment            readClassment;
    @Inject private find.FindHandicapIndexAtDate  findHandicapIndexAtDate;

    private List<ECourseList> liste = null;

    public ParticipantsStablefordCompetitionList() { }

    public List<ECourseList> list(final CompetitionDescription competition) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with Competition Description = " + competition);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM player
            JOIN competition_description
               ON CompetitionId = ?
            JOIN round
                ON round.RoundName = competition_description.CompetitionName
            JOIN course
                ON round.course_idcourse = course.idcourse
            JOIN club
                ON course.club_idclub = club.idclub
            JOIN player_has_round
                ON  InscriptionIdPlayer = player.idplayer
                AND InscriptionIdRound = round.idround
            ORDER BY player_has_round.InscriptionFinalResult DESC
            """;

        try {
            RowMapper<Club> clubMapper = new ClubRowMapper();
            RowMapper<Course> courseMapper = new CourseRowMapper();
            RowMapper<Player> playerMapper = new PlayerRowMapper();
            RowMapper<Inscription> inscriptionMapper = new InscriptionRowMapper();
            RowMapperRound<Round> roundMapper = new RoundRowMapper();

            liste = dao.queryList(query, rs -> {
                Player player = playerMapper.map(rs);
                Club club = clubMapper.map(rs);
                Round round = roundMapper.map(rs, club);
                Classment classment = readClassment.read(player, round);
                LOG.debug(methodName + " - classment in while = " + classment);
                HandicapIndex handicapIndex = new HandicapIndex();
                handicapIndex.setHandicapPlayerId(player.getIdplayer());
                handicapIndex.setHandicapDate(round.getRoundDate());
                handicapIndex = findHandicapIndexAtDate.find(handicapIndex);
                LOG.debug(methodName + " - handicapIndex in while = " + handicapIndex);
                return ECourseList.builder()
                        .club(club)
                        .course(courseMapper.map(rs))
                        .handicapIndex(handicapIndex)
                        .inscription(inscriptionMapper.map(rs))
                        .round(round)
                        .player(player)
                        .classment(classment)
                        .build();
            }, competition.getCompetitionId());

            if (liste.isEmpty()) {
                LOG.warn(methodName + " - empty result list");
            } else {
                LOG.debug(methodName + " - list size = " + liste.size());
            }

            liste.forEach(item -> LOG.debug("liste BEFORE sort" + NEW_LINE
                    + item.player().getPlayerLastName() + item.classment().getTotalPoints()));

            liste.sort(Comparator.comparingInt((ECourseList p) -> p.classment().getTotalPoints()).reversed()
                    .thenComparingInt((ECourseList p) -> p.classment().getLast9()).reversed()
                    .thenComparingInt((ECourseList p) -> p.classment().getLast6()).reversed()
                    .thenComparingInt((ECourseList p) -> p.classment().getLast3()).reversed()
                    .thenComparingInt((ECourseList p) -> p.classment().getLast1()).reversed());

            liste.forEach(item -> LOG.debug("liste AFTER sort = " + NEW_LINE
                    + item.player().getPlayerFirstName() + " / "
                    + item.player().getIdplayer() + " /" + item.classment()));
            return liste;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
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
        // CompetitionDescription cde = new CompetitionDescription();
        // cde.setCompetitionId(24);
        // var ecl = list(cde);
        // LOG.debug("from main, ecl = " + ecl);
        LOG.debug("from main, ParticipantsStablefordCompetitionList = ");
    } // end main
    */

} // end class
