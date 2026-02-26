
package jollyday;

import de.focus_shift.jollyday.core.CalendarHierarchy;
import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import java.time.Year;
import java.util.Set;
import static de.focus_shift.jollyday.core.HolidayCalendar.GERMANY;
import static de.focus_shift.jollyday.core.HolidayCalendar.FRANCE;
import de.focus_shift.jollyday.core.ManagerParameters;
import static interfaces.Log.LOG;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ResourceBundle;
import java.util.stream.Collectors;



public class JollyDay {
// https://github.com/focus-shift/jollyday

    
    public Set<LocalDate> getPublicHolidays(String countryCode, int year) {
        HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(countryCode.toUpperCase()));
        return manager.getHolidays(Year.of(year))
                      .stream()
                      .map(Holiday::getDate)
                      .collect(Collectors.toSet());
    }

    public boolean isCountrySupportedCal(HolidayCalendar calendar) {
    try {
        HolidayManager.getInstance(ManagerParameters.create(calendar));
        return true;
    } catch (Exception e) {
        return false;
    }
}
   
    public static boolean isCountrySupportedCountryCode(String countryCode) {
    try {
        
        HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(countryCode));
        return true;
    } catch (Exception e) {
        String msg = "££ Exception in isCountrySupportedCountryCode = " + e.getMessage();
           LOG.error(msg);
           return false;
    }
}
    
    // used from 12-12-2025
    public static boolean isPublicHoliday(String countryCode, LocalDate date) {
        try{
           LOG.debug("entering JollyDay.isPublicHoliday");
           LOG.debug("countryCode = " + countryCode);
           LOG.debug("date = " + date);
        if(isCountrySupportedCountryCode(countryCode)){ // faudrait code region an = andalousia
              HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(countryCode));
          //    Set<Holiday> holidays = manager.getHolidays(Year.of(date.getYear()));
        // Afficher les dates
          //    holidays.forEach(h -> LOG.debug("all Holidays "+ countryCode + "/" + h.getDate() + " : " + h.getPropertiesKey()));
        //      manager = HolidayManager.getInstance(ManagerParameters.create(countryCode.toUpperCase()));
              return manager.isHoliday(date);
        }else{
               LOG.debug("country not supported in isPublicHoliday= " + countryCode);
            return false;
        }
    } catch (Exception e) {
        String msg = "££ Exception in isPublicHoliday = " + e.getMessage();
        LOG.error(msg);
        return false;
    }
        
    } // end method
/*
    public Set<LocalDate> getPublicHolidaysWithRegion(String countryCode, String regionCode, int year) {
        HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(countryCode.toUpperCase(), regionCode));
        return manager.getHolidays(Year.of(year))
                      .stream()
                      .map(Holiday::getDate)
                      .collect(Collectors.toSet());
    }
  */  
    // not used
    private List<JollydayRegion> loadRegions(HolidayCalendar calendar, Locale locale) {
    try{
        LOG.debug("entering loadRegions");
        LOG.debug("with locale = " + locale);
        HolidayManager manager = HolidayManager.getInstance(
                ManagerParameters.create(calendar)
        );
        CalendarHierarchy root = manager.getCalendarHierarchy();
        ResourceBundle bundle = ResourceBundle.getBundle(
              //  "de.jollyday.config.country_description",
               // "de.jollyday.descriptions.country_description",
                  "descriptions.country_descriptions",  // fonctionne
                locale
        );
LOG.debug(" loadRegions 01");
        List<JollydayRegion> result = new ArrayList<>();

        // --- itération sur Map ou Iterable ---
        Object childrenObj = root.getChildren();

        if (childrenObj instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                if (value instanceof CalendarHierarchy region) {
                    String code = region.getId();
                    String label = bundle.containsKey(code) ? bundle.getString(code) : code;
                    result.add(new JollydayRegion(code, label));
                }
            }
        } else if (childrenObj instanceof Iterable<?> iterable) {
            for (Object o : iterable) {
                if (o instanceof CalendarHierarchy region) {
                    String code = region.getId();
                    String label = bundle.containsKey(code) ? bundle.getString(code) : code;
                    result.add(new JollydayRegion(code, label));
                }
            }
        }

        // --- Fallback automatique multi-pays ---
        if (result.isEmpty()) {
            LOG.debug(" loadRegions 02");
            result.addAll(fallbackRegions(calendar, locale));
        }
 LOG.debug(" loadRegions 03");
        return result;
 //   }
     } catch (Exception e) {
           String msg = "££ Exception in loadRegions = " + e.getMessage();
           LOG.error(msg);
           return null;
   }finally{
       //  DBConnection.closeQuietly(conn, null, null,null); 
   }
    } // end method   
    
    // not used
    private List<JollydayRegion> fallbackRegions(HolidayCalendar calendar, Locale locale) {
    try{    
        LOG.debug("entering fallbackRegions");
        LOG.debug("calendar = " + calendar);
        LOG.debug("locale = " + locale); 
        List<JollydayRegion> list = new ArrayList<>();
        switch (calendar) {
            case BELGIUM -> list.addAll(List.of(
                    new JollydayRegion("brussels", "Bruxelles"),
                    new JollydayRegion("flanders", "Flandre"),
                    new JollydayRegion("wallonia", "Wallonie")
            ));
            case FRANCE -> list.addAll(List.of(
                    new JollydayRegion("alsace", "Alsace"),
                    new JollydayRegion("aquitaine", "Aquitaine"),
                    new JollydayRegion("bourgogne", "Bourgogne")
                    // Ajouter toutes les régions françaises nécessaires
            ));
            case GERMANY -> list.addAll(List.of(
                    new JollydayRegion("bw", "Baden-Württemberg"),
                    new JollydayRegion("by", "Bavière"),
                    new JollydayRegion("he", "Hesse")
                    // Ajouter toutes les régions allemandes nécessaires
            ));
            case SWITZERLAND -> list.addAll(List.of(
                    new JollydayRegion("ag", "Argovie"),
                    new JollydayRegion("ge", "Genève"),
                    new JollydayRegion("zh", "Zurich")
            ));
            // Ajouter d’autres pays connus ici
         
            default -> list.add(new JollydayRegion("all", "All regions"));
        }
           LOG.debug(" fallbackRegions end");
        return list;
  //  }
    } catch (Exception e) {
            String msg = "££ Exception in fallbackRegions = " + e.getMessage();
            LOG.error(msg);
           return null;
   }finally{
   }
    } //en method 
    
    
    
    
  void main() throws Exception{
try{
      LOG.debug("entering main");

      
      
      HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.SPAIN));
      boolean supportedand = manager.getCalendarHierarchy()
        .getChildren()
        .containsKey("an"); // voir le code dans Holidays_es.xml du jar
      LOG.debug("andalusia supported = " + supportedand);
      

      
      
      
    boolean bol = isPublicHoliday("BE", LocalDate.of(2025, Month.DECEMBER, 25));
      LOG.debug("isPublicHoliday in non supported country = " + bol);
      
    boolean belgiumSupported = isCountrySupportedCal(HolidayCalendar.BELGIUM);
         LOG.debug("belgium supported = " + belgiumSupported);
    boolean japanSupported  = isCountrySupportedCal(HolidayCalendar.JAPAN);
         LOG.debug("japan supported = " + japanSupported);   
      
    boolean XXSupported = isCountrySupportedCountryCode("XY");
         LOG.debug("XY supported = " + XXSupported);   // false
      
        manager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.BELGIUM)); //HolidayCalendar.BELGIUM));
      //  HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create("de")); // new regions = [bb, hh, st, be, mv, nw, th, bw, sh, by, sl, hb, sn, ni, he, rp]
        Set<String> regions = manager.getCalendarHierarchy().getChildren().keySet();
        LOG.debug("belgium regions = " + regions);
       // Locale BeLocale = new Locale("de","De");
       // Initialize for Belgium (country code "be")
// HolidayManager manager = HolidayManager.getInstance(ManagerParameters.create("be"));

// Get holidays for the Flemish Region (vlg)
       
  //  LocalDate now = LocalDate.now();

  
   final Locale locale = Locale.of("nl","BE"); // si "fr","BE"  texte en français
     Set<Holiday> holidaysvlg = manager.getHolidays(Year.of(2025), "vlg");
       //LOG.debug("holidays vlaanderen = " + holidaysvlg);
       holidaysvlg.forEach(h -> {
           LOG.debug("all Holidays VLAANDEREN " + h.getDate() + " : " + h.getPropertiesKey() + " : " + h.getDescription(locale));
      });
     Set<Holiday> holidaysbru = manager.getHolidays(Year.of(2025), "bru");
       holidaysbru.forEach(h -> {
           LOG.debug("all Holidays BRUSSELS " + h.getDate() + " : " + h.getPropertiesKey() + " : " + h.getDescription(locale));
      });
      final Locale localewal = Locale.of("fr","BE"); 
     Set<Holiday> holidayswal = manager.getHolidays(Year.of(2025), "wal");
     holidayswal.forEach(h -> {
           LOG.debug("all Holidays WALLONIE " + h.getDate() + " : " + h.getPropertiesKey() + " : " + h.getDescription(localewal));
      });
       Set<Holiday> holidaysbel = manager.getHolidays(Year.of(2025));
     holidaysbel.forEach(h -> {
           LOG.debug("all Holidays BELGIUM " + h.getDate() + " : " + h.getPropertiesKey() + " : " + h.getDescription(locale));
      });
     
     
       
       
     //    locale = Locale.of("fr","BE");
 ////  à retester      var v = loadRegions(HolidayCalendar.BELGIUM, locale);
    //  LOG.debug("regions Locale = " + v);
        
  manager =
        HolidayManager.getInstance(
                ManagerParameters.create(HolidayCalendar.BELGIUM)
        );

CalendarHierarchy root = manager.getCalendarHierarchy();

System.out.println("ROOT ID = " + root.getId());

Object children = root.getChildren();

System.out.println("Children object = " + children);
System.out.println("Children class  = " +
        (children != null ? children.getClass() : "null"));
        
   //     Object children = root.getChildren();

if (children instanceof Iterable<?> iterable) {
    for (Object o : iterable) {
        CalendarHierarchy region = (CalendarHierarchy) o;
        System.out.println("REGION = " + region.getId());
    }
} else {
    LOG.debug("Aucune région disponible for " + HolidayCalendar.BELGIUM);
}
        
        
        
        
 /*       
var regions = regionService.getRegions(
            HolidayCalendar.BELGIUM,
            Locale.FRENCH
    );
LOG.debug("regions = " + regions.toString());
*/
//manager = HolidayManager.getInstance(ManagerParameters.create(FRANCE));

      Set<String> supported = HolidayManager.getSupportedCalendarCodes();
       supported.forEach(item -> LOG.debug("list of SupportedCalendarCodes = " + item));
        
 
        
    String countryCode = "GERMANY";
ResourceBundle bundle = ResourceBundle.getBundle(
     //   "de.focus_shift.jollyday.core.resources.country_description", Locale.FRENCH
         "descriptions/country_descriptions", Locale.FRENCH
);
    String countryNameFr = bundle.getString(countryCode); // "Allemagne"
    LOG.debug("description fr congés allemands = " + countryNameFr);
    
    
 
      //   Récupérer tous les jours fériés pour l’année 2025
        Set<Holiday> holidays2 = manager.getHolidays(Year.of(2025));
        // Afficher les dates
        holidays2.forEach(h -> LOG.debug("all Holidays FRANCE " + h.getDate() + " : " + h.getPropertiesKey()));
        /*
    //     Charger le ResourceBundle en français
     //   ResourceBundle bundle = ResourceBundle.getBundle(
     //           "de.focus_shift.jollyday.core.resources.HolidayNames", 
     //           Locale.FRENCH
     //   );
/*
        holidays.forEach(h -> {
            // Traduire la clé en nom français
            String name = bundle.containsKey(h.getPropertiesKey())
                    ? bundle.getString(h.getPropertiesKey())
                    : h.getPropertiesKey(); // fallback si pas trouvé
            LOG.debug("en français maintenant " + h.getDate() + " : " + name);
        });
        
   //     manager = HolidayManager.getInstance(ManagerParameters.create(GERMANY, "bw")); // subdivision Properties ??

        
        /* 3. Bundles FR
            ResourceBundle countryBundle = ResourceBundle.getBundle(
                    "de.focus_shift.jollyday.jollyday-core.src.main.resources.description.country_description",
                    Locale.FRENCH
            );

            ResourceBundle holidayBundle = ResourceBundle.getBundle(
                    "de.focus_shift.jollyday.core.resources.HolidayNames",
                    Locale.FRENCH
            );

            ResourceBundle regionBundle = ResourceBundle.getBundle(
                    "de.focus_shift.jollyday.core.resources.subdivisions",
                    Locale.FRENCH
            );
            LOG.debug("line 00 ");
        manager = HolidayManager.getInstance(ManagerParameters.create(GERMANY));

        LocalDate date = LocalDate.of(2025, 12, 25);
        boolean isholiday = manager.isHoliday(date);
           LOG.debug(date + " est un jour férié en Allemagne ? " + isholiday);
        boolean isHolidayInBW = manager.isHoliday(date, "bw"); // bade-wurtenberg
           LOG.debug(date + " est un jour férié en Allemagne ? " + isHolidayInBW);
         date = LocalDate.of(2025, 6, 6);
           LOG.debug(date + " est férié en Baden-Württemberg ? " + manager.isHoliday(date, "bw"));
*/
     } catch (Exception e) {
            String msg = "££ Exception in JollyDay = " + e.getMessage();
            LOG.error(msg);
          //  LCUtil.showMessageFatal(msg);
   }finally{
        // DBConnection.closeQuietly(conn, null, null,null); 
   }
    } // end main
} //end class