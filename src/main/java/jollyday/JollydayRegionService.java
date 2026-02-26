
package jollyday;

import jakarta.enterprise.context.ApplicationScoped;
import de.focus_shift.jollyday.core.CalendarHierarchy;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import static interfaces.Log.LOG;

import java.util.*;

@ApplicationScoped
public class JollydayRegionService {

    public List<JollydayRegion> getRegions(
            HolidayCalendar calendar,
            Locale locale) {
        LOG.debug("entering getRegions");
        HolidayManager manager =
                HolidayManager.getInstance(
                        ManagerParameters.create(calendar)
                );
LOG.debug("line 00");
        CalendarHierarchy root = manager.getCalendarHierarchy();

        ResourceBundle bundle = ResourceBundle.getBundle(
                "de.focus_shift.jollyday.config.country_description",
                locale
        );

        List<JollydayRegion> result = new ArrayList<>();

        Object childrenObj = root.getChildren();

        if (childrenObj instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) childrenObj) {
                CalendarHierarchy region = (CalendarHierarchy) o;

                String code = region.getId();
                String label = bundle.containsKey(code)
                        ? bundle.getString(code)
                        : code;

                result.add(new JollydayRegion(code, label));
            }
        }

        return result;
    }
}