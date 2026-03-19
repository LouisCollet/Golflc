package find;

import entite.Course;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class FindTeeStart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<String> liste = null;

    public FindTeeStart() { }

    public List<String> find(final Course course, final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - escaped thanks to lazy loading");
            return liste;
        }

        LOG.debug(methodName + " - for course = " + course);
        LOG.debug(methodName + " - for player = " + player);
        LOG.debug(methodName + " - for round = " + round);

        final String query = """
                 SELECT *
                 FROM course, tee
                 WHERE course.idcourse = ?
                     AND tee.TeeGender = ?
                     AND tee.course_idcourse = course.idcourse
                     AND SUBSTR(TeeHolesPlayed, 1, 2) = ?
                     AND SUBSTR(TeeHolesPlayed, 4, 2) = ?
                 """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, course.getIdcourse());
            ps.setString(2, player.getPlayerGender());
            DecimalFormat df = new DecimalFormat("00");
            ps.setString(3, df.format(round.getRoundStart()));
            ps.setString(4, df.format(round.getRoundHoles() + round.getRoundStart() - 1));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                while (rs.next()) {
                    liste.add(rs.getString("TeeStart")
                            + " / " + rs.getString("TeeGender")
                            + " / " + rs.getString("TeeHolesPlayed")
                            + " / " + rs.getInt("idtee"));
                }
                if (liste.isEmpty()) {
                    String msg = "Empty result for " + methodName;
                    liste.add("No TeeStart found for gender : " + player.getPlayerGender());
                    LOG.error(msg);
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
                liste.forEach(item -> LOG.debug("TeeStart list " + item));
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
    public List<String> getListe()               { return liste; }
    public void setListe(List<String> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
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
        Course course = new Course();
        course.setIdcourse(99);
        Player player = new Player();
        player.setPlayerGender("M");
        Round round = new Round();
        round.setIdround(748);
        List<String> b = find(course, player, round);
        b.forEach(item -> LOG.debug("TeeStart list " + item));
        LOG.debug("from main, after = " + b);
    } // end main
    */

} // end class
