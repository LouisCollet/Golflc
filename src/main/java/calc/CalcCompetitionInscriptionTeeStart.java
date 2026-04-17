package calc;

import entite.Course;
import entite.Tee;
import entite.composite.ECompetition;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class CalcCompetitionInscriptionTeeStart implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private lists.TeesCourseList teesCourseList;

    public CalcCompetitionInscriptionTeeStart() { }

    public String calc(final ECompetition competition) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for Competition = " + competition);

        try {
            var cde = competition.competitionDescription();
            var cda = competition.competitionData();
            LOG.debug(methodName + " - series handicap = " + Arrays.deepToString(cde.getSeriesHandicap()));
            LOG.debug(methodName + " - gender = " + cda.getCmpDataPlayerGender());
            LOG.debug(methodName + " - handicap player = " + cda.getCmpDataHandicap());

            Course course = new Course();
            course.setIdcourse(cde.getCompetitionCourseId());
            List<Tee> tees = teesCourseList.list(course.getIdcourse());
            tees.forEach(item -> LOG.debug(methodName + " - tee = " + item));

            String teeStart;
            if (cda.getCmpDataPlayerGender().equals("M")) {
                teeStart = "YELLOW / M / 01-18 / 37";
                LOG.debug(methodName + " - TeeStart forced to = " + teeStart);
            } else {
                teeStart = "BLUE / L / 01-18 / 188";
                LOG.debug(methodName + " - TeeStart forced to = " + teeStart);
            }
            return teeStart;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "";
        }
    } // end method

    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // String teeStart = calc(competition);
        // LOG.debug("from main, TeeStart = " + teeStart);
        LOG.debug("from main, CalcCompetitionInscriptionTeeStart = ");
    } // end main
    */

} // end class
