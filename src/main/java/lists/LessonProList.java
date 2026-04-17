package lists;

import entite.Lesson;
import entite.Professional;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Named
@ApplicationScoped
public class LessonProList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<Lesson> liste = null;

    public LessonProList() { }

    public List<Lesson> list(final Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("proPlayerId = {}", professional.getProPlayerId());

        if (liste != null) {
            LOG.debug("returning cached list size = {}", liste.size());
            return liste;
        }

        // Join through professional → all clubs for this pro player + club name + paid status via FK
        // Double join on player for pro name and student name
        final String query = """
                SELECT lesson.*, club.ClubName,
                       CONCAT(pro_p.PlayerFirstName, ' ', pro_p.PlayerLastName)     AS ProName,
                       CONCAT(stu_p.PlayerFirstName, ' ', stu_p.PlayerLastName)     AS StudentName
                FROM lesson
                JOIN professional ON lesson.EventProId       = professional.ProId
                JOIN club         ON professional.ProClubId  = club.idclub
                JOIN player AS pro_p ON professional.ProPlayerId = pro_p.idplayer
                LEFT JOIN player AS stu_p ON lesson.EventPlayerId = stu_p.idplayer
                WHERE professional.ProPlayerId = ?
                  AND lesson.EventStartDate > ?
                """;

        // dimanche de la 2e semaine qui precede — semaine courante + semaine precedente
        Timestamp cutoff = Timestamp.valueOf(LocalDate.now().minusWeeks(2).with(DayOfWeek.SUNDAY).atStartOfDay());

        liste = dao.queryList(query, new rowmappers.LessonRowMapper(), professional.getProPlayerId(), cutoff);

        if (liste.isEmpty()) {
            LOG.warn("empty result list for proPlayerId={}", professional.getProPlayerId());
        } else {
            LOG.debug("list size = {}", liste.size());
        }
        return liste;
    } // end method

    public List<Lesson> getListe()             { return liste; }
    public void setListe(List<Lesson> liste)   { this.liste = liste; }

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
        Professional pro = new Professional();
        pro.setProId(1);
        List<Lesson> schedules = list(pro);
        LOG.debug("size schedule list for a Pro = " + schedules.size());
        schedules.forEach(item -> LOG.debug("Schedule list for a Pro StartDate " + item.getEventStartDate()));
    } // end main
    */

} // end class
