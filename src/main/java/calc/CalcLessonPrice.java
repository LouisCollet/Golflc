package calc;

import entite.Club;
import entite.Professional;
import entite.ProTarif;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@ApplicationScoped
public class CalcLessonPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    public CalcLessonPrice() { }

    /**
     * Returns the lesson price for the given date and professional.
     * Public holidays are treated as weekend days (weekendPrice).
     * Returns 0.0 when ProTarif is absent — manageLesson() will warn the user and block cart navigation.
     */
    public Double calc(Professional professional, LocalDateTime date, Club club) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        ProTarif tarif = professional.getProTarifObject();
        if (tarif == null) {
            LOG.warn("ProTarif not configured for proId={}", professional.getProId());
            return 0.0;
        }
        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        if (!isWeekend && club != null && club.getAddress() != null && club.getAddress().getCountry() != null) {
            String countryCode = club.getAddress().getCountry().getCode();
            isWeekend = jollyday.JollyDay.isPublicHoliday(countryCode, date.toLocalDate());
            if (isWeekend) {
                LOG.debug("public holiday on {} — treated as weekend", date.toLocalDate());
            }
        }
        Double price = isWeekend ? tarif.getWeekendPrice() : tarif.getWeekdayPrice();
        Double result = (price != null && price > 0) ? price : 0.0;
        LOG.debug("lesson price = {} (isWeekend={})", result, isWeekend);
        return result;
    } // end method

} // end class
