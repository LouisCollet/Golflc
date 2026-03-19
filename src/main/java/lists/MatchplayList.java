package lists;

import entite.Matchplay;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@ApplicationScoped
public class MatchplayList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<Matchplay> liste = null;

    public MatchplayList() { }

    public List<Matchplay> getList(final String formula) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - with formula = " + formula);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT idround, roundgame, club.idclub, club.ClubName,
                   course.idcourse, course.CourseName, round.RoundDate, RoundName
            FROM round
            JOIN course
                ON course.idcourse = round.course_idcourse
            JOIN club
                ON club.idclub = course.club_idclub
            WHERE substring(roundgame, 1, 3) = ?
            ORDER BY rounddate DESC
            """;

        liste = new ArrayList<>(dao.queryList(query, rs -> {
            Matchplay mp = new Matchplay();
            mp.setIdround(rs.getInt("idround"));
            mp.setRoundDate(rs.getTimestamp("roundDate"));
            mp.setIdclub(rs.getInt("idclub"));
            mp.setClubName(rs.getString("clubName"));
            mp.setIdcourse(rs.getInt("idcourse"));
            mp.setCourseName(rs.getString("CourseName"));
            mp.setRoundName(rs.getString("RoundName"));
            mp.setRoundGame(rs.getString("roundgame"));
            return mp;
        }, formula.toUpperCase()));

        LOG.debug(methodName + " - liste {} = {} ", formula, Arrays.deepToString(liste.toArray()));
        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<Matchplay> getListe()                      { return liste; }
    public void            setListe(List<Matchplay> liste) { this.liste = liste; }

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
        // var li = getList("MP_");
        // LOG.debug("from main, list = " + li);
        LOG.debug("from main, MatchplayList = ");
    } // end main
    */

} // end class
