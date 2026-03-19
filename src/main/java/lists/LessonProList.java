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
        LOG.debug("entering " + methodName);
        LOG.debug("with Professional " + professional);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT *
                FROM lesson
                WHERE lesson.EventProId = ?
                AND EventStartDate > ?
                """;

        // dimanche de la 2e semaine qui precede — semaine courante + semaine precedente
        Timestamp cutoff = Timestamp.valueOf(
                LocalDate.now().minusWeeks(2).with(DayOfWeek.SUNDAY).atStartOfDay());

        liste = dao.queryList(query, rs -> Lesson.map(rs),
                professional.getProId(), cutoff);

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<Lesson> getListe()             { return liste; }
    public void setListe(List<Lesson> liste)   { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Professional pro = new Professional();
        pro.setProId(1);
        List<Lesson> schedules = list(pro);
        LOG.debug("size schedule list for a Pro = " + schedules.size());
        schedules.forEach(item -> LOG.debug("Schedule list for a Pro StartDate " + item.getEventStartDate()));
    } // end main
    */

} // end class
