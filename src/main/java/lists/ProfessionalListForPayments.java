package lists;

import entite.Club;
import entite.LessonPayment;
import entite.Player;
import entite.Professional;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import manager.PlayerManager;
import rowmappers.LessonPaymentRowMapper;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro

@Named("ProPayment")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml
public class ProfessionalListForPayments implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Inject
    private PlayerManager playerManager;

    @Inject
    private read.ReadClub readClubService;

    // ✅ Cache d'instance — @ViewScoped resets per view automatically
    private List<ECourseList> liste = null;

    public ProfessionalListForPayments() { }

    public List<ECourseList> list(final Player pro) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Professional = " + pro);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            WITH selection AS (
                SELECT *
                FROM professional
                WHERE professional.ProplayerId = ?
            )
            SELECT * FROM selection
            JOIN payments_lesson
                ON payments_lesson.LessonIdPro = selection.ProId
            ORDER BY payments_lesson.LessonIdClub, payments_lesson.LessonStartDate DESC
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, pro.getIdplayer());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Professional> professionalMapper = new ProfessionalRowMapper();
                RowMapper<LessonPayment> lessonPaymentMapper = new LessonPaymentRowMapper();

                while (rs.next()) {
                    LessonPayment lesson = lessonPaymentMapper.map(rs);
                    int clubId = lesson.getPaymentIdClub();
                    Club club = new Club();
                    club.setIdclub(clubId);
                    club = readClubService.read(club);

                    Professional professional = professionalMapper.map(rs);
                    int studentId = lesson.getPaymentIdStudent();
                    Player student = new Player();
                    student.setIdplayer(studentId);
                    student = playerManager.readPlayer(student.getIdplayer());

                    ECourseList ecl = ECourseList.builder()
                            .club(club)
                            .professional(professional)
                            .player(student)
                            .lessonPayment(lesson)
                            .build();
                    liste.add(ecl);
                }

                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                    liste.forEach(item -> LOG.debug("Players list with student name = " + item.getPlayer().getPlayerLastName()
                            + " /paymentIdClub " + item.lessonPayment().getPaymentIdClub()
                            + " /idclub = " + item.club().getIdclub()));
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

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
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
        Player player = new Player();
        player.setIdplayer(324715);
        var prof = new ProfessionalListForPayments().list(player);
        LOG.debug("list Pro for payments size = " + prof.size());
    } // end main
    */

} // end class
