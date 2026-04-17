package calc;

import entite.composite.ECompetition;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class CalcCompetitionTimeStartList implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    public CalcCompetitionTimeStartList() { }

    public List<String> calc(ECompetition competition) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try {
            LocalDateTime ldt = competition.competitionDescription().getCompetitionDate();
            int i = 0;
            int interval = 12; // 12 min écart entre flights
            int h = 0;
            List<String> liste = new ArrayList<>();
            liste.add(" - no time preference");
            while (h < 4) { // n donne le choix pour 4 périodes de départ
                LocalDateTime ldt1 = ldt.plusMinutes(i * interval);
                LocalDateTime ldt2 = ldt1.plusHours(1); // départs par tranches d'une heure
                liste.add(ldt1.toLocalTime() + " - " + ldt2.toLocalTime());
                i = i + 6;
                h++;
            }
            LOG.debug(methodName + " - start timelist = " + liste);
            return liste;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
*/

} // end class
