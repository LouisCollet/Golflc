package calc;

import entite.Club;
import entite.Course;
import entite.EquipmentsAndBasic;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import entite.TarifGreenfee.DayType;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
// import connection_package.DBConnection; // removed 2026-02-26 — CDI migration
import utils.LCUtil;

@jakarta.enterprise.context.ApplicationScoped
public class CalcTarifGreenfee implements interfaces.GolfInterface, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public CalcTarifGreenfee() { }

    public TarifGreenfee calc(TarifGreenfee tarif, Round round, Club club, Player player){   
     LOG.debug(" -- Start of CalcTarifGreenfee with Tarif = " + tarif);
     LOG.debug(" --  with round = " + round.toString());
     LOG.debug(" --  with club = " + club.toString());  // pour avoir country
 try{
          LOG.debug("greenfeeType = " + tarif.getGreenfeeType());
          tarif.getTeeTimeChoosen().clear();
          tarif.getEquipmentChoosen().clear();
          tarif.getDayChoosen().clear();
          tarif.setCurrency(club.getAddress().getCountry().getCurrency()); // new 28-04-2025
       String season = findSeason(tarif, round);
          LOG.debug("Season season = " + season);
       tarif.setSeason(season);
       
       if(tarif.getGreenfeeType().equals("BA")){  // cas basic
          tarif = findBasic(tarif);
          tarif = findEquipments(tarif, null); // general equipments (linkedSlotKey == null)
          return tarif;
       }
       if(tarif.getGreenfeeType().equals("HO")){ // HOURS
           tarif = findHours(tarif, round);
           // also collect linked equipments for the chosen slot
           if (!tarif.getTeeTimeChoosen().isEmpty()) {
               String chosenSlotKey = tarif.getTeeTimeChoosen().get(0).getSlotKey();
               tarif = findEquipments(tarif, chosenSlotKey);
           }
           return tarif;
       }
       if(tarif.getGreenfeeType().equals("EQ")){  //E
            LocalDate lddob = player.getPlayerBirthDate().toLocalDate();
              LOG.debug("LocalDate playerBirthDate = " + lddob);
            tarif = findEquipments(tarif, null); // null = not linked to any slot
            return tarif;
       }  
       if(tarif.getGreenfeeType().equals("DA")){
            LocalDate birthDate = player.getPlayerBirthDate().toLocalDate();
              LOG.debug("LocalDate playerBirthDate = " + birthDate);
            tarif = findDays(tarif, round, club.getAddress().getCountry().getCode(), birthDate); // senior, junior, FRIDAY, WEEKEND, WEEK, HOLIDAY
            tarif = findEquipments(tarif, null); // general equipments (linkedSlotKey == null)
            return tarif;
       }
 return null;
 }catch (Exception e) {
      String msg = " -- Error in calcTarifGreenfee " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 }
} // end method 
    
//  public TarifGreenfee findBasic (TarifGreenfee tarif, Round round){
  public TarifGreenfee findBasic (TarifGreenfee tarif){
      LOG.debug(" -- Start of findBasic with Tarif = " + tarif);
 try{
    double price = findPriceBasic(tarif);
       LOG.debug("price greenfee basic = " + price);
    tarif.setPriceGreenfee(price);
    return tarif;
   }catch (Exception e){
      String msg = " -- Error in findBasic" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return null;
 }finally{ }  
 }  // end method
  

  public double findPriceBasic(TarifGreenfee tarif){
       LOG.debug("Starting with findPriceBasic = " + tarif.getBasicList());
       LOG.debug("Starting for season = " + tarif.getSeason());
 try{
        for (EquipmentsAndBasic price : tarif.getBasicList()) {
              // "A" = All seasons — matches any season
              if ("A".equals(price.getSeason()) || price.getSeason().equals(tarif.getSeason())) {
                  if (!price.isAvailable()) {
                      LOG.debug("basic price {} not available — skipped", price.getItem());
                      continue;
                  }
                  LOG.debug("found priceGreenfee= " + price);
                  if (price.getQuantity() == 0) {
                      price.setQuantity(1); // default: 1 greenfee per player
                  }
                  return price.getPrice();
              }
        }
     return 0.0;
  }catch (Exception e){
      String msg = " -- Error in findPriceBasic" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return 0.0;
 }finally{
 }    
 } // end method
     
 public String findSeason (TarifGreenfee tarif, Round round){
      LOG.debug("Starting findSeason with datesSeasonList = " + tarif.getDatesSeasonsList());
 try{
    var dround = round.getRoundDate();
     LOG.debug("searching season for round date = " + dround);
    String season = null;  // default was "H"
        for(int row = 0; row < tarif.getDatesSeasonsList().size(); row++){
            LOG.debug("examining " + tarif.getDatesSeasonsList().get(row));
                    var ddeb = tarif.getDatesSeasonsList().get(row).getStartDate();//     .t[row][0],ZDF_DAY);
         //              LOG.debug("ddeb = " + ddeb);
                    var dfin = tarif.getDatesSeasonsList().get(row).getEndDate(); 
         //              LOG.debug("dfin = " + dfin);
                    if (dround.isEqual(ddeb)
                     || dround.isEqual(dfin)
                     || (dround.isAfter(ddeb) && (dround.isBefore(dfin)))){
                           LOG.debug("Found in datesSeason for !!= " + tarif.getDatesSeasonsList().get(row));
                           season = tarif.getDatesSeasonsList().get(row).getSeason();
                           break;
                    }  
        } // end for
       LOG.debug("season found = " + season); 
       if(season == null){
           String msg = "season is null - cata";
           LOG.error(msg); 
           LCUtil.showMessageFatal(msg);
       }
  return season;
 }catch (Exception e){
      String msg = " -- Error in findPeriod" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return null;
 }finally{
 }
} // end method 
  
public TarifGreenfee findHours (TarifGreenfee tarif, Round round){
   //  LOG.debug(" -- Start of calcTarif with Tarif = " + tarif.toString());
  //   LOG.debug(" -- Start of calcTarif with round = " + round.toString());
 try {
     LOG.debug("Starting findHours " );
     LocalTime localTimeRound = round.getRoundDate().toLocalTime();
        LOG.debug("Starting findHours for LocalTime = " + localTimeRound);
        LOG.debug("Starting findHours for season = " + tarif.getSeason()); // H,M,L
        LOG.debug("List teeTimes = " + tarif.getTeeTimesList());
        LOG.debug("TwilightList = " + tarif.getTwilightList());
// new 09-06-2022
// concerne twilight et previous (tranche horaire avant twilight
 // ArrayList<TeeTimes> work = new ArrayList<>(); 
  ArrayList<TarifGreenfee.TeeTimes> work = new ArrayList<>(); // new 23-06-2022
  for(int i=0; i<tarif.getTeeTimesList().size(); i++){ 
            var v = tarif.getTeeTimesList().get(i);
                LOG.debug("work row handled  = " + v);
            if (!v.isAvailable()) {
                LOG.debug("tee time entry {} is not available — skipped", v.getItem());
                continue;
            }
            if(v.getTwilight().equals("P")){
                   LOG.debug("we handle PREVIOUS " + v); // il faut compléter endTime
                LocalTime twilightStartTime = twilightStart(v.getTwilight(), tarif.getTwilightList(), tarif.getSeason(), round);
                v.setEndTime(twilightStartTime.minusMinutes(1));
                 LOG.debug("Previous EndTime  = " + v.getEndTime());
                work.add(v);
            }
            if(v.getTwilight().equals("Y")){
                   LOG.debug("we handle TWILIGHT " + v); // il faut compléter startdate and endate à 18:00 heures ??
                LocalTime twilightStartTime = twilightStart(v.getTwilight(), tarif.getTwilightList(), tarif.getSeason(), round);
                v.setStartTime(twilightStartTime);
                   LOG.debug("Twilight StartTime  = " + v.getStartTime());
                v.setEndTime(LocalTime.of(18,0));
                   LOG.debug("Twilight EndTime  = " + v.getEndTime());
                work.add(v);
            }
            if(v.getTwilight().equals("N")){
                   LOG.debug("we handle NORMAL " + v);
                work.add(v);
            }
  } //end for
//  LOG.debug("list work = " + work.toString());
//  LOG.debug("before copy work");
  tarif.setTeeTimesList(work);
//    LOG.debug("after copy work");
          for(int row = 0; row < tarif.getTeeTimesList().size(); row++){  
                var v =  tarif.getTeeTimesList().get(row);
                   LOG.debug("row handled  = " + v);
                if(v.getSeason().equals(tarif.getSeason())){
     //               LocalTime tdeb = v.getStartTime();
     //               LocalTime tfin = v.getEndTime();
                    if (localTimeRound.equals(v.getStartTime())
                      || localTimeRound.equals(v.getEndTime())
                      || (localTimeRound.isAfter(v.getStartTime()) && (localTimeRound.isBefore(v.getEndTime())))) {
                              LOG.debug("Found in teeTimes !! for " + v); //for = " + ZDF_HOURS.format(tround));
                           if (v.getQuantity() == 0) { v.setQuantity(1); }
                           tarif.getTeeTimeChoosen().add(v);  // new 04/05/2022
                              LOG.debug("time added= " + v) ;
                           } //end if 2
                }  // end if 1
         } // end for
    return tarif;
 }catch(Exception e){
      String msg = " -- Error in findHours " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 } finally { }
} //end method finfPriceHours
  
 public TarifGreenfee findDays (TarifGreenfee tarif, Round round, String country, LocalDate lddob){  // day of birth
 try{
     LOG.debug("entering findDays with daysList = " + tarif.getDaysList());
     LOG.debug("datesSeason = " + tarif.getDatesSeasonsList());
     if(tarif.getTwilightList() != null){
         LOG.debug("TwilightList = " + tarif.getTwilightList());
     }
        // afaire TODO filtrer en fonction de l'heure !!!
     // ne prendre en daychoosen
      int iDay = findDayIndex(round, country);
        LOG.debug("DayIndex  = " + iDay);
  // new 19/05/2022
   for(int i=0; i<tarif.getDaysList().size();i++) { 
        var v = tarif.getDaysList().get(i);
        LOG.debug("handling DaysList v = " + v);
        if(v.getSeason().equals(tarif.getSeason())){
             boolean[] avail = v.getAvailable();
             if (avail != null && avail.length > iDay && !avail[iDay]) {
                 LOG.debug("day index {} is not available for category {} — skipped", iDay, v.getCategory());
                 continue;
             }
          //   var daysWeek = new DaysWeek();// new 04/05/2022
             var daysWeek = tarif.new DaysWeek();// new 04/05/2022
             //TarifGreenfee.Twilight twilight = tarifGreenfee.new Twilight(); // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
             daysWeek.setCategory(v.getCategory());
             daysWeek.setSeason(v.getSeason());
             daysWeek.setPrice(v.getPrice()[iDay]); // magic 0=monday, 1=week etc;
             daysWeek.setDayType(indexToDayType(iDay));
             daysWeek.setQuantity(0);
             // ici method ??
          //   LocalTime twilightStartTime = twilightStart(v, tarif.getTwilightList(), tarif.getSeason(), round);
             LocalTime twilightStartTime = twilightStart(v.getTwilight(), tarif.getTwilightList(), tarif.getSeason(), round);
                LOG.debug("twilight starttime = " + twilightStartTime);
             // new 29/05/2022
             daysWeek.setTwilightStartTime(twilightStartTime);
             tarif.getDayChoosen().add(daysWeek);
                LOG.debug("daysWeek added = " + daysWeek);
        }
    } //end for
       LOG.debug("final list dayChoosen = " + tarif.getDayChoosen());
   return tarif;
 } catch (Exception e) {
      String msg = " -- Error in calcTarifGreenfee" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 } 
   }  // end method
 
//public LocalTime twilightStart(DaysGreenfee aysGreenfee, List<Twilight> twilight, String season, Round round){
public LocalTime twilightStart(String twi, List<TarifGreenfee.Twilight> twilight, String season, Round round){    
 try{
         LOG.debug("entering twilightStart");
  //       LOG.debug("with <DaysGreenfee> = " + daysGreenfee);
         LOG.debug("with List<Twilight> = " + twilight);
         LOG.debug("with season  = " + season);
         LOG.debug("with round date  = " + round.getRoundDate());
  // chercher l'heure de début twilight
    if(twi.equals("N")){
         LOG.debug("twilight = N - nothing to do ");
         return null;}
 //     LOG.debug("season daysGreenfee = " + daysGreenfee.getSeason());
  // recherche si saison est complétée
    for(TarifGreenfee.Twilight tw : twilight) {
     //   if(tw.getSeason().equals(season)) {
     if(tw.getSeason() != null){
        LOG.debug("we search in Seasons for = " + season);
        if(tw.getSeason().equals(season)) {    
              LOG.debug("twilight Season found = " + tw.getSeason() + " / " + tw + " /" + tw.getStartTime());
           return tw.getStartTime();
        }
     }
           LOG.debug("we search in months = " + tw.getMonths());
        if(tw.getMonths().contains(round.getRoundDate().getMonthValue())) {  // getMonth contient [1,3,5] les mois pour lesquels le starTime est applicable
              LOG.debug("twilight Month found = " + tw.getMonths().toString() + " / " + tw + " /" + tw.getStartTime());
           return tw.getStartTime();
        }
    }//end for
return null;
  }catch (Exception e){
      String msg = " -- Error in twilightStart" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 }finally{ }    
 } // end method
 
 public int findDayIndex( Round round, String country){
 try{
 //    TarifGreenfee.DayType dayType = TarifGreenfee.DayType.WEEK;
        // Lundi, jours de la semaine, vendredi, week-end, jours fériés
        // MONDAY, WEEK, FRIDAY, WEEKEND, HOLIDAY
         LOG.debug("entering findDayIndex");
        LOG.debug("countryCode = " + country);
        LOG.debug("round = " + round);
        
     int iDay = 0;
  // is it an holiday ?    
  // new 12-12-2025
  
      boolean bol = jollyday.JollyDay.isPublicHoliday(country, round.getRoundDate().toLocalDate());
      LOG.debug("holiday from JollyDay = " + bol);
  
     if(new utils.Holidays().CountryHolidays(round.getRoundDate().toLocalDate(), country.toUpperCase())) { // BE, ES ...
         iDay = 4; //dayType = DayType.HOLIDAY;
           LOG.debug("this is an Holiday !! " + round);
        return iDay;
     }
        java.time.DayOfWeek dayOfWeek = round.getRoundDate().getDayOfWeek();
            LOG.debug("dayOfWeek Name = " + dayOfWeek.name());
        switch(dayOfWeek){
                    case MONDAY -> iDay = 0;
                    case TUESDAY,WEDNESDAY,THURSDAY ->  iDay = 1; //dayType = DayType.WEEK;
                    case FRIDAY -> iDay = 2; //dayType = DayType.FRIDAY;
                    case SATURDAY, SUNDAY -> iDay = 3; //dayType = DayType.WEEKEND;
                    default -> LOG.debug("default dayOfWeek");
        }// end switch
   return iDay;
  }catch (Exception e){
      String msg = " -- Error in findDayIndex" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99;
 }finally{ }    
 } // end method

 /**
  * Finds equipments matching the current season and optionally linked to a specific HO slot.
  * @param tarif the tarif being calculated
  * @param slotKey the slotKey of the chosen HO tee time, or null for EQ type (unlinked equipments)
  */
 public TarifGreenfee findEquipments (TarifGreenfee tarif, String slotKey){
     LOG.debug(" -- Start of findEquipments with slotKey = {}", slotKey);
 try {
     tarif.getEquipmentsList().stream()
             .peek(item -> LOG.debug("peek - element of equipmentsList ={}", item))
             .filter(x -> x.getSeason().equals(tarif.getSeason()) || x.getSeason().equals("A"))
             .filter(x -> {
                 // null linkedSlotKey = applies to all slots (or standalone EQ type)
                 if (x.getLinkedSlotKey() == null) return slotKey == null;
                 return x.getLinkedSlotKey().equals(slotKey);
             })
             .forEach(x -> {
                 if (x.getQuantity() == 0) { x.setQuantity(1); }
                 tarif.getEquipmentChoosen().add(x);
             });

         LOG.debug("equipmentChoosen = {}", tarif.getEquipmentChoosen());
 return tarif;

 }catch (Exception e){
      String msg = " -- Error in findEquipments" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return tarif;
 } finally {
 }
} // end method
  
 public TarifGreenfee.DayType indexToDayType(int i){
     DayType dayType = null;
     switch(i){     case 0 -> dayType = DayType.MONDAY;
                    case 1 -> dayType = DayType.WEEK;
                    case 2 -> dayType = DayType.FRIDAY;
                    case 3 -> dayType = DayType.WEEKEND;
                    case 4 -> dayType = DayType.HOLIDAY;
                    default -> LOG.debug("default dayOfWeek for i = " + i);
     }
     return dayType;
 } // end method
 
/*
void main() throws Exception, SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // requires CDI container — cannot run standalone
} //end main
*/
} //end class