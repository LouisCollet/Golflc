
package utils;

import static interfaces.Log.LOG;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;

public class Holidays {
 // vérifier si la date est un jour férié
     
  public boolean CountryHolidays(LocalDate ld, String country)
    {
            LOG.info("entering CountryHolidays");
            LOG.info("localdate = " + ld);
         country = country.toUpperCase();    
            LOG.info("country = " + country);
   /*            
    LocalDate date = LocalDate.of(2018, Month.JANUARY,16);
    System.out.println(date);
    LocalDate firstDayOfJuly = date.with(TemporalAdjusters.firstDayOfMonth()); // 2014-07-01
    System.out.println(firstDayOfJuly);
    LocalDate dateOfFirstMonday = date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)); // 2014-07-07
    System.out.println("first monday september 2018 =" + dateOfFirstMonday);
    
    date = LocalDate.of(2018, Month.MAY, 16);
    LocalDate dateOfLastMonday = date.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)); // 2014-07-07
    System.out.println("last monday may 2018 = " + dateOfLastMonday);
            
        date = LocalDate.of(2018, Month.JANUARY, 16);
    LocalDate d = date.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY));
            System.out.println("3e lundi du mois de janvier 2018 = "+ d);
            
         
    LocalDate startDate = LocalDate.of(2018, Month.JANUARY, 15);
        List<LocalDate> optionExDates = optionExpirationDates(startDate, 1,DayOfWeek.MONDAY, 2);  //signifie 3
        for (LocalDate temp : optionExDates) {
            System.out.println("third monday of january = " + temp);
        } 
        
         startDate = LocalDate.of(2018, Month.FEBRUARY, 15);
         optionExDates = optionExpirationDates(startDate, 1, DayOfWeek.MONDAY, 2);  // signifie 3
        for (LocalDate temp : optionExDates) {
            System.out.println("third monday of february = " + temp);
        } 
    
                 startDate = LocalDate.of(2018, Month.OCTOBER, 15);
         optionExDates = optionExpirationDates(startDate, 1, DayOfWeek.MONDAY, 1);  // signifie 2
        for (LocalDate temp : optionExDates) {
            System.out.println("second monday of october = " + temp);
        } 
        
        startDate = LocalDate.of(2018, Month.NOVEMBER, 15);
         optionExDates = optionExpirationDates(startDate, 1, DayOfWeek.THURSDAY, 3);  // signifie 4
        for (LocalDate temp : optionExDates) {
            System.out.println("fourth thursday of november = " + temp);
        } 
        LocalDate d1 = LocalDate.now();
           
d = d.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
System.out.println("3e vendredi du mois "+ d); // 2016-12-16
    //    LocalDate thirdFriday = LocalDate startDate
       //                     .with(lastDayOfMonth())
       //                     .with(previous(DayOfWeek.FRIDAY)).minusDays(7);
   //     Stream //.iterate(startDate, date -> date.plusDays(1))
  //              map(LocalDate -> LocalDate.with(TemporalAdjusters.firstDayOfMonth()).minusDays(1)
  //                     .with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).plusWeeks(1));
         */
   
 // https://github.com/martinjw/Holiday/blob/master/src/PublicHoliday/BelgiumPublicHoliday.cs
 // https://github.com/martinjw/Holiday/blob/master/src/PublicHoliday/HolidayCalculator.cs
 // https://github.com/martinjw/Holiday/blob/master/src/PublicHoliday/SpainPublicHoliday.cs
        
         boolean b = false;
        if(country.equals("BE")){
            b = CountryBelgium(ld);
        }
        if(country.equals("ES")){
            b = CountrySpain(ld);
        }
        if(country.equals("US")){
            b = CountryUSA(ld);
        }
        return b;
    }

  public boolean CountryBelgium(LocalDate lda)
    {
        try{
        boolean b = false;
        int year = lda.getYear();
        String slda = lda.format(ISO_LOCAL_DATE); // yyyy/MM/dd
            LOG.info("entering CountryBelgium with localdate = " + lda);
            LOG.info("converted to String = = " + slda);
       
         LocalDate easter = utils.LCUtil.EasterSundayDate(year);
            LOG.info("easter = " + easter);

         Map<LocalDate,String> hol = new LinkedHashMap<>();
    //        hol.put(EasterSundayDate(year), "Easter");
            hol.put(easter.plusDays(1), "Easter Monday");
            hol.put(easter.plusDays(39), "Ascension");
            hol.put(easter.plusDays(50), "Pentecost");
            hol.put(LocalDate.of(year, Month.JANUARY, 01), "NewYear");
            hol.put(LocalDate.of(year, Month.MAY, 01), "Labor Day");
            hol.put(LocalDate.of(year, Month.JULY, 21), "National Day");
            hol.put(LocalDate.of(year, Month.AUGUST, 15), "Assumption");
            hol.put(LocalDate.of(year, Month.NOVEMBER, 01), "Toussaint");
            hol.put(LocalDate.of(year, Month.NOVEMBER, 01), "Armistice");
            hol.put(LocalDate.of(year, Month.DECEMBER, 25), "Christmas");

hol.forEach((k,v) -> LOG.info("Holidays Belgium = Holiday : " + v + " / Date : " + k));
//SortedSet<String> keys = new TreeSet<String>(hol.keySet());


if (hol.containsKey(lda) ) {
    LOG.info("Hash map contains lda : " + hol.get(lda)+ " /" + slda);
     return true;
}else{
    LOG.info("is NOT an holiday : " + slda);
    return false; // is not an holiday
}
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   return false;
    } 
 } // end method Country Belgium
  
    public boolean CountrySpain(LocalDate lda)
    {
        try{
        boolean b = false;
        String slda = lda.format(ISO_LOCAL_DATE); // yyyy/MM/dd
            LOG.info("entering CountrySpain with localdate = " + lda);
            LOG.info("converted to String = = " + slda);
        int year = lda.getYear();
         LocalDate easter = utils.LCUtil.EasterSundayDate(year);
            LOG.info("easter = " + easter);

         Map<LocalDate,String> hol = new LinkedHashMap<>();
    //        hol.put(EasterSundayDate(year), "Easter");
            hol.put(easter.minusDays(2), "Good Friday - Viernes Santo");
     //       hol.put(easter.plusDays(39), "Ascension");
     //       hol.put(easter.plusDays(50), "Pentecost");
            hol.put(LocalDate.of(year, Month.JANUARY, 01), "NewYear - Año Nuevo");
            hol.put(LocalDate.of(year, Month.JANUARY, 06), "Epiphany - Dia de Reyes");
            hol.put(LocalDate.of(year, Month.MAY, 01), "Labor Day - Dia del Trabajador");
            hol.put(LocalDate.of(year, Month.AUGUST, 15), "Assumption - Asunción");
            hol.put(LocalDate.of(year, Month.OCTOBER, 12), "National Day - Fiesta National de España");  // Alt 0241
            hol.put(LocalDate.of(year, Month.NOVEMBER, 01), "All Saints - Dia de todos los santos");
            hol.put(LocalDate.of(year, Month.DECEMBER, 06), "Constitution Day - Dia de la Constitución");
            hol.put(LocalDate.of(year, Month.DECEMBER, 8), "Immaculate Conception - Immaculada Concepción"); // ó
            hol.put(LocalDate.of(year, Month.DECEMBER, 25), "Christmas - Navidad");

hol.forEach((k,v) -> LOG.info("Holidays Spain = Holiday : " + v + " / Date : " + k));

if (hol.containsKey(lda) ) {
    LOG.info("Hash map contains lda : " + hol.get(lda)+ " /" + slda);
     return true;
}else{
    LOG.info("is NOT an holiday : " + slda);
    return false; // is not an holiday
}
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   return false;
    } 
 } // end method Country Spain
  
    
     public boolean CountryUSA(LocalDate lda)
    {
        /// Federal Holidays in the US
/*
            bHols.Add(NewYear(year)); //1st January
            bHols.Add(MartinLutherKing(year)); // Third Monday in January
            bHols.Add(PresidentsDay(year)); //Third Monday in February
            bHols.Add(MemorialDay(year)); //Last Monday in May
            bHols.Add(IndependenceDay(year)); //4 July
            bHols.Add(LaborDay(year)); //First Monday in September
            bHols.Add(ColumbusDay(year)); //Second Monday in October
            bHols.Add(VeteransDay(year)); //11 November
            bHols.Add(Thanksgiving(year)); //Fourth Thursday in November
            bHols.Add(Christmas(year)); //25 December
        */
      //https://gist.github.com/bdkosher/9414748   tableau pour vérifier          
        try{
        boolean b = false;
    //    String slda = lda.format(ISO_LOCAL_DATE); // yyyy/MM/dd
     //       LOG.info("entering CountryUSA with localdate = " + lda);
       //     LOG.info("converted to String = " + slda);
     //        LocalDate easter = EasterSundayDate(year);
         int year = lda.getYear();
         LocalDate wlda = null;   // work field
 //           LOG.info("easter = " + easter);

         Map<LocalDate,String> hol = new LinkedHashMap<>();
            hol.put(FixWeekend(LocalDate.of(year, Month.JANUARY, 01)), "NewYear");  // si samedi ==> vendredi
                wlda = LocalDate.of(year,Month.JANUARY,15);
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY)), "Martin Luther King Jr Day");
                wlda = LocalDate.of(year,Month.FEBRUARY,15);
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY)), "President's Day");    
                wlda = LocalDate.of(year,Month.MAY,15);
            hol.put(wlda.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)), "Memorial Day");
            hol.put (FixWeekend(LocalDate.of(year, Month.JULY, 04)), "Independance day");  // fixed !!
                wlda = LocalDate.of(year,Month.SEPTEMBER,15);
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.MONDAY)), "Labor Day");
                wlda = LocalDate.of(year,Month.OCTOBER,15);
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY)), "Colombus Day");
            hol.put(FixWeekend(LocalDate.of(year, Month.NOVEMBER, 11)), "Veterans Day"); // fixed
                wlda = LocalDate.of(year,Month.NOVEMBER,15);
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)), "Thanksgiving");
            hol.put(wlda.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)).plusDays(1), "Day After Thanksgiving");
            hol.put(FixWeekend(LocalDate.of(year, Month.DECEMBER, 25)), "Christmas - Navidad"); //fixed

hol.forEach((k,v) -> LOG.info("Holidays USA = Holiday : " + v + " / " + k));

if (hol.containsKey(lda) ) {
    LOG.info("Is an HOLIDAY !! : " + hol.get(lda)+ " /" + lda);
     return true;
}else{
    LOG.info("is NOT an holiday : " + lda);
    return false; // is not an holiday
}
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   return false;
    } 
 } // end method Country Spain
  

  private LocalDate FixWeekend(LocalDate hol)
        {
  //          Holidays adjustment
 /// If a holiday falls on a Saturday it is celebrated the preceding Friday;
/// if a holiday falls on a Sunday it is celebrated the following Monday. 
       //     LOG.info("entering FixWeekEnd");
             LOG.info("entering FixWeekEnd with : " + hol);
            DayOfWeek dayOfWeek = hol.getDayOfWeek();
    //        LOG.info("dayOfWeek = " + dayOfWeek);
            switch(dayOfWeek)
                {
                    case SATURDAY:
                         hol = hol.minusDays(1);
                         LOG.info("holiday MOVED from Saturday to Friday : " + hol);
                         break;
                    case SUNDAY:
                         hol = hol.plusDays(1);
                         LOG.info("holiday MOVED from Sunday to monday : " + hol);
                        break;
                    default:
             //           LOG.info("no change !!= " + dayOfWeek.name());
                        break;
       }
          return hol;
  //
        }

  public static void main(String[] args) throws ParseException, SQLException {
  try{
    //  LocalDate lda = LocalDate.of(2018, Month.MARCH, 15);
       LocalDate lda = LocalDate.of(2018, Month.DECEMBER, 24);
       Holidays ho = new Holidays();
       boolean b = ho.CountryHolidays(lda, "US");
         LOG.info(" main : is holiday ? = " + b);
  } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
 }
} //end main
} // end class