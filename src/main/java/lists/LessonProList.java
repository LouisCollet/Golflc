package lists;

import entite.Lesson;
import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

@Named
@ApplicationScoped
public class LessonProList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, professional.getProId());
            // dimanche de la 2e semaine qui précède — semaine courante + semaine précédente
            ps.setTimestamp(2, Timestamp.valueOf(
                    LocalDate.now().minusWeeks(2).with(DayOfWeek.SUNDAY).atStartOfDay()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                while (rs.next()) {
                    Lesson ev = Lesson.map(rs);
                    liste.add(ev);
                } // end while
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
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
