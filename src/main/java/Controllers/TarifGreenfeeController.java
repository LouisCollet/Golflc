package Controllers;

import com.github.mawippel.validator.OverlappingVerificator;
import entite.Club;
//import entite.DatesSeasons;
//import entite.DaysGreenfee;
//import entite.DaysWeek;
import entite.EquipmentsAndBasic;
import entite.Greenfee;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
//import entite.TeeTimes;
//import entite.Twilight;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import utils.LCUtil;
import static utils.LCUtil.myDoubleRound;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import utils.TimeOverlap;

@ApplicationScoped
public class TarifGreenfeeController implements Serializable {

    private static final long serialVersionUID = 1L;

    public TarifGreenfeeController() { }

public TarifGreenfee inputTarifGreenfeePeriods(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
        LOG.debug("with inputTarifGreenfeePeriods with tarifGreenfee = " + tarifGreenfee);
  // verification overlapping et chronologie start-end
        if(tarifGreenfee.getDatesSeasonsList().size() >= 1){ // ce test nécessaire ??
            if(overlapCheckPeriods(tarifGreenfee)){
                 LOG.debug("yes there is overlap ");
                 return tarifGreenfee;
            }else{
                 LOG.debug("no overlap detected - we update !");
            }
         } // end if 
   // magic happens here         was non static
 /*  TarifGreenfee.DatesSeasons datesSeasons = tarifGreenfee.new DatesSeasons( // mod 23-06-2021 inner class DatesSeasons within TarifGreenfee !
//     DatesSeasons datesSeasons = new DatesSeasons(
             tarifGreenfee.getStartDate(),
             tarifGreenfee.getEndDate(),
             tarifGreenfee.getSeason());
   */
 
   //   see pavel polivka using inner classes for jackson serialization
     TarifGreenfee.DatesSeasons datesSeasons = new TarifGreenfee.DatesSeasons();
            datesSeasons.setStartDate(tarifGreenfee.getStartDate());
            datesSeasons.setEndDate(tarifGreenfee.getEndDate());
            datesSeasons.setSeason(tarifGreenfee.getSeason()); 
   
     tarifGreenfee.getDatesSeasonsList().add(datesSeasons);
     String msg = "Period introduced = " + datesSeasons;
     LOG.info(msg);
     showMessageInfo(msg);
     msg = "SeasonsList after add = " + tarifGreenfee.getDatesSeasonsList();
     LOG.info(msg);
     showMessageInfo(msg);
 // house keeping
    tarifGreenfee.setStartDate(null); // init pour affichage
    tarifGreenfee.setEndDate(null);
    
   return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return tarifGreenfee;
}
} // end method

public boolean overlapCheckPeriods(TarifGreenfee tarif){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
            for(int i = 0; i < tarif.getDatesSeasonsList().size(); i++) {
                LOG.debug("i = " + i);
              // new period
                LocalDateTime comparableStart = tarif.getStartDate().withHour(0).withMinute(0);
                LocalDateTime comparableEnd  = tarif.getEndDate().truncatedTo(ChronoUnit.DAYS);
              // existing periods
              var v = tarif.getDatesSeasonsList().get(i);
              LocalDateTime toCompareStart = v.getStartDate().truncatedTo(ChronoUnit.DAYS); //.withHour(0).withMinute(0);
          //      LocalDateTime toCompareInit  = LocalDate.parse(tarif.getDatesSeason()[i][0], ZDF_DAY).atStartOfDay();
              LocalDateTime toCompareEnd = v.getEndDate().withHour(0).withMinute(0); //[1], ZDF_DAY).atStartOfDay();
              if(OverlappingVerificator.isOverlap(comparableStart, comparableEnd, toCompareStart, toCompareEnd)){
        //           LOG.debug("overlap = " + isOverlap);
                           String msg =  LCUtil.prepareMessageBean("tarif.overlapping" )
                           + ZDF_DAY.format(comparableStart) + " - " + ZDF_DAY.format(comparableEnd)
                           + " against <br>"
                           + ZDF_DAY.format(toCompareStart) + " - " + ZDF_DAY.format(toCompareEnd);
                   LOG.error(msg);
                   showMessageFatal(msg);   
                   return true;
               } // end if
            }  // end for}
            return false;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return true;
}
}  // end method

public TarifGreenfee inputTarifGreenfeeBasic(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{  // used in tarif_equipments.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
        LOG.debug("with inputTarifGreenfeeBasic with tarifGreenfee = " + tarifGreenfee);
        // 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      String msg = "Fatal error : season Basic does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }
 // magic happens here
    EquipmentsAndBasic basic = new EquipmentsAndBasic(
            tarifGreenfee.getWorkItem(),
            tarifGreenfee.getWorkSeason(),
            tarifGreenfee.getWorkPrice(),
            0); // quantity
    tarifGreenfee.getBasicList().add(basic);
    String msg = "Tarif Greenfees Basic = " + tarifGreenfee.getBasicList();
    LOG.info(msg);
    showMessageInfo(msg);
    msg = "Total Seasons Periods = " + tarifGreenfee.getDatesSeasonsList();
    LOG.info(msg);
    showMessageInfo(msg);
 //       LOG.debug("tarif Greenfee Basic updated = " + tarifGreenfee.getBasicList());
// house keeping
    tarifGreenfee.setGreenfeeType("BA");
    tarifGreenfee.setUpdateReady(true); // gestion menu
       LOG.debug("tarif BA setUpdateReady to true");
       // new 28/08/2022
    tarifGreenfee.setTwilightDone(true);   
    tarifGreenfee.setWorkItem(null); // init pour le prochain affichage
    tarifGreenfee.setWorkSeason(null);
    tarifGreenfee.setWorkPrice(null);
   return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return tarifGreenfee;
}
} // end method

 public TarifGreenfee inputTarifGreenfeeEquipments(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{  // used in tarif_equipments.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
      LOG.debug("with inputTarifGreenfeeEquipments !");
      LOG.debug("with tarif Equipments = " + tarifGreenfee.getEquipmentsList());
 // 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      String msg = "Fatal error : Season equipments does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }
  // magic happens here         
    EquipmentsAndBasic equipments = new EquipmentsAndBasic(
            tarifGreenfee.getWorkItem(),
            tarifGreenfee.getWorkSeason(),
            tarifGreenfee.getWorkPrice(),
            0); // quantity
    tarifGreenfee.getEquipmentsList().add(equipments);
    String msg = "EquipmentsAndBasic after add = " +  tarifGreenfee.getEquipmentsList(); 
    LOG.info(msg);
    showMessageInfo(msg);
// house keeping
    tarifGreenfee.setWorkItem(null); // init pour le prochain affichage
    tarifGreenfee.setWorkPrice(null); 
    tarifGreenfee.setEquipmentsReady(true); // gestion le menu
   return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

 public boolean validPeriod( ArrayList<TarifGreenfee.DatesSeasons> periods, String season) {
    if(season.equals("A")){  // all seasons accepted
        return true;
    }
    for (TarifGreenfee.DatesSeasons period : periods) {
        if (period.getSeason().equals(season)) {
            return true;
        }
    }
    return false;
}
 public TarifGreenfee inputTarifGreenfeeDays(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
     LOG.debug("with inputTarifGreenfeeDays with tarifGreenfee = " + tarifGreenfee);
     String msg = "input : DaysWorkPrice = "  + Arrays.toString(tarifGreenfee.getWorkDaysPrice());
     LOG.info(msg);
     showMessageInfo(msg);
// 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      msg = "Fatal error : season hours does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }   
    
// magic happens here 
    TarifGreenfee.DaysGreenfee daysGreenfee = tarifGreenfee.new DaysGreenfee(); // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
    daysGreenfee.setSeason(tarifGreenfee.getWorkSeason());
    daysGreenfee.setCategory(tarifGreenfee.getWorkItem());
    daysGreenfee.price[0] = tarifGreenfee.getWorkDaysPrice()[0]; // monday
    daysGreenfee.price[1] = tarifGreenfee.getWorkDaysPrice()[1];
    daysGreenfee.price[2] = tarifGreenfee.getWorkDaysPrice()[2];
    daysGreenfee.price[3] = tarifGreenfee.getWorkDaysPrice()[3];
    daysGreenfee.price[4] = tarifGreenfee.getWorkDaysPrice()[4];
    daysGreenfee.setTwilight(tarifGreenfee.getWorkTwilight());   // yes or no
    tarifGreenfee.getDaysList().add(daysGreenfee);
    msg = "DaysGreenfee after add = " +  tarifGreenfee.getDaysList(); 
    LOG.info(msg);
    showMessageInfo(msg);
// house keeping
    tarifGreenfee.setGreenfeeType("DA"); // for days
    tarifGreenfee.setWorkItem(null); 
    // ici correction 11-04-2025
    var numbers = new Double[5];
    Arrays.fill(numbers, 0.0);
    tarifGreenfee.setWorkDaysPrice(numbers); // gestion de l'écran ?
//    LOG.debug("line 00");
    tarifGreenfee.setUpdateReady(true); // gestion du menu
 //   LOG.debug("line 01a - twilight = " + daysGreenfee.getTwilight());
  if(tarifGreenfee.getWorkTwilight().equals("Y")){ // mod 11-04-2025 was "N"
        tarifGreenfee.setTwilightReady(true); // gestion du menu
   }
   LOG.debug("line 01 - twilight = N");
    tarifGreenfee.setWorkTwilight("N");  // init pour affichage
       LOG.debug("tarifGreenfee returned = " + tarifGreenfee);
  return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

 public TarifGreenfee inputTarifGreenfeeHours(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
    LOG.debug("with inputTarifHours with tarif = !" + tarifGreenfee);
            LOG.debug("teeTimes List = "  + tarifGreenfee.getTeeTimesList());
       // 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      String msg = "Fatal error : season Hours does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }
      if(!tarifGreenfee.getTeeTimesList().isEmpty()){ 
            if(overlapCheckHours(tarifGreenfee)){
                 LOG.debug("There is an overlap Hours ");
                 return tarifGreenfee;
            }else{
                LOG.debug("no overlap detected - we input !");
            }
  } 
      // new 08/06/2022 permettre previous see example la cala
      // la validations d'écran sont transférées ici
    if(tarifGreenfee.getWorkTwilight().equals("N")){ // normal = non twilight
          if(tarifGreenfee.getStartHour() == null){
              String msg = "Tarif Hours StartHour must be completed"; // + tarifGreenfee.getTeeTimesList();
              LOG.info(msg);
              showMessageFatal(msg);
              return tarifGreenfee;
          }
          if(tarifGreenfee.getEndHour() == null){  
              String msg = "Tarif Hours EndHour must be completed"; // + tarifGreenfee.getTeeTimesList();
              LOG.info(msg);
              showMessageFatal(msg);
              return tarifGreenfee;
          }
    }
    if(tarifGreenfee.getWorkTwilight().equals("P")){   // previous  endHour sera complété par programme
          if(tarifGreenfee.getStartHour() == null){
              String msg = "Tarif Hours StartHour must be completed"; // + tarifGreenfee.getTeeTimesList();
              LOG.info(msg);
              showMessageFatal(msg);
              return tarifGreenfee;
          }
    }
    
    if(tarifGreenfee.getWorkTwilight().equals("P") || tarifGreenfee.getWorkTwilight().equals("Y")){   
        String msg = "Tarif Twilight must be completed !! "; // + tarifGreenfee.getTeeTimesList();
        LOG.info(msg);
        showMessageInfo(msg);
    }
    
    // si WorkTwilight = Y les dates start et end seront complétées par programme
    
     //     if(tarifGreenfee.getEndHour() == null){
     //         String msg = "Tarif Hours EndHour must be completed"; // + tarifGreenfee.getTeeTimesList();
     //         LOG.info(msg);
     //         showMessageFatal(msg);
     //         return tarifGreenfee;
    //      }
 
// the magic happens here !
        TarifGreenfee.TeeTimes teeTimes = tarifGreenfee.new TeeTimes(); // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
        teeTimes.setStartTime(tarifGreenfee.getStartHour());
        teeTimes.setEndTime(tarifGreenfee.getEndHour());
        teeTimes.setSeason(tarifGreenfee.getSeason());
        teeTimes.setItem(tarifGreenfee.getWorkItem());
        teeTimes.setPrice(tarifGreenfee.getWorkPrice());
        teeTimes.setQuantity(0);
        teeTimes.setTwilight(tarifGreenfee.getWorkTwilight());   // yes or no 
        tarifGreenfee.getTeeTimesList().add(teeTimes);
  //        LOG.debug("TeeTimesList after add = " + tarifGreenfee.getTeeTimesList());
        String msg = "Tarif Hours = " + tarifGreenfee.getTeeTimesList();
        LOG.info(msg);
        showMessageInfo(msg);
  // house keeping
       tarifGreenfee.setGreenfeeType("HO");  // for Hours
  //  if(tarifGreenfee.getWorkTwilight().equals("N")){ //enlevé 24-06-2022
     if(tarifGreenfee.getWorkTwilight().equals("Y")){ //modifié 11-04-2025 
          tarifGreenfee.setTwilightReady(true); // gestion du menu : écran
    }
       tarifGreenfee.setStartHour(null); // init pour affichage
       tarifGreenfee.setEndHour(null);
       tarifGreenfee.setWorkItem(null);
       tarifGreenfee.setWorkPrice(null);
       tarifGreenfee.setUpdateReady(true); // gestion du menu
   return tarifGreenfee; 
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method
 
public TarifGreenfee inputTarifGreenfeeTwilight(TarifGreenfee tarifGreenfee) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
     LOG.debug("with inputTarifTwilight with tarifGreenfee = " + tarifGreenfee);
     // soit months est complété, soit season est complété
     List<Integer> months = null;
     if(tarifGreenfee.getMultiTwilight() != null){ // multiple months
         String msg = "input LocalDates Twilight = "  + tarifGreenfee.getMultiTwilight().toString(); // list with LocalDates
         LOG.info(msg);
         showMessageInfo(msg);
         months = tarifGreenfee.getMultiTwilight().stream()
              .map(x -> x.getMonthValue())
              .collect(Collectors.toList());
         LOG.debug("List months twilight = "  + months.toString());
     }
 //    tarifGreenfee.getMultiTwilight().forEach(item -> LOG.debug("Twilight list " + item));

/* 30/04/2022 validation
          si season est null alors on accepte les months
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      msg = "Fatal error : season hours does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }
 */
// magic happens here 
   TarifGreenfee.Twilight twilight = tarifGreenfee.new Twilight(); // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
   twilight.setStartTime(tarifGreenfee.getStartHour());
   twilight.setSeason(tarifGreenfee.getWorkSeasonTwilight());  // yes or no
   twilight.setMonths(months);
   tarifGreenfee.getTwilightList().add(twilight);
    String msg = "TwilightList after add = " +  tarifGreenfee.getTwilightList(); 
    LOG.info(msg);
    showMessageInfo(msg);
// house keeping : init pour affichage
    tarifGreenfee.setStartHour(null); 
    tarifGreenfee.setWorkSeasonTwilight(null);
    tarifGreenfee.setMultiTwilight(null);
    tarifGreenfee.setTwilightDone(true); // gestion du menu fera afficher create all
       LOG.debug("tarifGreenfee returned = " + tarifGreenfee);
  return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method
  
 public boolean overlapCheckHours(TarifGreenfee tarif){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
       LOG.debug("existing hours = " + tarif.getTeeTimesList());
       LOG.debug("trying to insert new hours = " + tarif.getStartHour() + "/" + tarif.getEndHour());
 // candidate
  if(tarif.getStartHour() == null || tarif.getEndHour() == null){ 
       return false;
 }
     TimeOverlap.LocalTimeRange range1 = new TimeOverlap.LocalTimeRange(tarif.getStartHour(),tarif.getEndHour());
  for (int i = 0; i < tarif.getTeeTimesList().size(); i++) {
 //           LOG.debug("i = " + i);
            var v =  tarif.getTeeTimesList().get(i);
            if(v.getStartTime() == null || v.getEndTime() == null               // skip control en entrée : twilight or previous
                || tarif.getStartHour() == null || tarif.getEndHour() == null) { // skip control déjà enregistré : twilight or previous
                return false;
            }
            LocalTime ltdeb = v.getStartTime().truncatedTo(ChronoUnit.MINUTES);
    //           LOG.debug("ltdeb = " + ltdeb); 
            LocalTime ltend = v.getEndTime().truncatedTo(ChronoUnit.MINUTES);
     //          LOG.debug("ltend = " + ltend); 
            TimeOverlap.LocalTimeRange range2 = new TimeOverlap.LocalTimeRange(ltdeb,ltend);
            if(range1.overlaps(range2)){
                String msg = "Overlap Hours detected between " 
                       + tarif.getTeeTimesList().get(i)
                       + " <br/> Start = "+ tarif.getStartHour() + " end = " + tarif.getEndHour();
                LOG.error(msg); 
                showMessageFatal(msg);
                return true;
            }
        } // end for 
   return false;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return true;
}
}  // end method
 
 public Greenfee completeGreenfee(TarifGreenfee tarif, Club club, Round round, Player player) throws Exception{
      final String methodName = utils.LCUtil.getCurrentMethodName();
      LOG.debug("entering completeGreenfee");
      Greenfee greenfee = new Greenfee();
try{
        LOG.debug("with tarif = " + tarif);
     double d = this.calcGreenfeePrice(tarif);
        LOG.debug("le prix du greenfee et des équipements est " + d);
     greenfee.setPrice(d);
/*     if(greenfee.getPrice() == 0.0){
         String msgerr = "Le total est zéro - il faut choisir au moins un item !!!";
            LOG.error(msgerr);
         utils.LCUtil.showMessageFatal(msgerr);
            throw new Exception(msgerr);
     }
*/
     greenfee.setIdplayer(player.getIdplayer()); // new 03-01-2022    
     greenfee.setCommunication("Greenfee parcours " + club.getClubName());
     greenfee.setIdclub(club.getIdclub());
     greenfee.setIdround(round.getIdround());
     greenfee.setRoundDate(round.getRoundDate());
     //new 28-04-2025
   //  greenfee.setCurrency(club.getAddress().getCountry().getCurrency());
     greenfee.setCurrency(tarif.getCurrency());
     greenfee.setStatus("N"); // sera mis à Y ci-après si dans le paiement est compris un greenfee (et pas uniquement des equipements
  // créer la liste des items pour stockage DB
  // à faire ultérieurement : noter les quantités : 2 greenfees, 3 buggys, etc...
    StringBuilder sb = new StringBuilder("");
      if(tarif.getGreenfeeType().equals("DA")){
           LOG.debug("inputtype = DA");
         greenfee.setStatus("Y");
           LOG.debug("status changed to  = " + greenfee.getStatus());
         sb.append("Greenfee (")
         .append(tarif.getPriceGreenfee())
         .append("),");
      }
/// constitution liste items pour greenfee
       for(int i = 0 ; i < tarif.getBasicList().size() ; i++) {
           var v = tarif.getBasicList().get(i);
                sb.append(v.getItem()) // item
                  .append(" (")
                  .append(v.getPrice())
                  .append(v.getSeason())
                  .append(v.getQuantity())
                  .append("),");
     //           LOG.debug("after append sb = " + sb.toString());
                        greenfee.setStatus("Y"); // le player a payé son greenfee et pas uniquement des équipements
    //          } //end if
         } //end for
 //      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.debug("final Items for greenfee = " + sb);
            
  /// constitution liste items pour equipments
        for(int i = 0 ; i < tarif.getEquipmentsList().size() ; i++) { 
                 var v = tarif.getEquipmentsList().get(i);
                  sb.append(v.getItem()) // item      
                  .append(" (")
                  .append(v.getSeason())
                  .append(v.getSeason())
                  .append(v.getQuantity())
                  .append("),");
     //           LOG.debug("after append sb = " + sb.toString());
       //       } //end if 
         } //end for
   //    LOG.debug("after for loop");
       sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.debug("final Items for greenfee and equipments = " + sb);
      greenfee.setItems(sb.toString());
  return greenfee;
  
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} //end method

 
 public double calcGreenfeePrice(TarifGreenfee tarif){
     final String methodName = utils.LCUtil.getCurrentMethodName();
     LOG.debug(" -- Start of calc.CalcGreenfeePrice with tarif= " + tarif);
     // calcul le prix total à payer par creditcard
 try {
    double total = 0;
     LOG.debug( " ,getPriceGreenfee: "   + tarif.getPriceGreenfee());
     LOG.debug("GreenfeeType = " + tarif.getGreenfeeType());
     LOG.debug(" ,equipmentsList : " + tarif.getEquipmentsList());
     LOG.debug("calculating equipments-----------------");
       for (EquipmentsAndBasic equipment : tarif.getEquipmentsList()) {
           total = total + (equipment.getPrice() * equipment.getQuantity());
       }
     LOG.debug("total for equipments = " + total);

LOG.debug("calculating greenfee -----------------");
 
 if(tarif.getGreenfeeType().equals("DA")){
       LOG.debug("Calculating greenfee for inputtype = DA");  // DAYS 
   //  new 02-05-2021
          LOG.debug("getDayChoosen  = " + tarif.getDayChoosen());
       for (TarifGreenfee.DaysWeek day : tarif.getDayChoosen()) {
            total = total + (day.getPrice() * day.getQuantity());
       }
          LOG.debug("total greenfee DA = " + total);
     return myDoubleRound(total,2);  // arrondi à deux décimales
} // end "DA"
 
  if(tarif.getGreenfeeType().equals("HO")){
          LOG.debug("handling inputType HO"); // hours
 //         LOG.debug(" teeTimesList: "   + tarif.getTeeTimesList());
          LOG.debug(" teeTimeChoosen: "   + tarif.getTeeTimeChoosen());
        for (TarifGreenfee.TeeTimes hours : tarif.getTeeTimeChoosen()) {  // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
     //  for (TeeTimes hours : tarif.getTeeTimeChoosen()) {
            total = total + (hours.getPrice() * hours.getQuantity());
       }
        LOG.debug("total greenfee HO = " + total);
     return myDoubleRound(total,2);  // arrondi à deux décimales      
 } // end "HO"

  if(tarif.getGreenfeeType().equals("BA")){
         LOG.debug("Calculating greenfee for inputtype = BA");
         LOG.debug(" basicList: "   + tarif.getBasicList());
      
     for(EquipmentsAndBasic basic : tarif.getBasicList()) {
         total = total + (basic.getPrice() * basic.getQuantity());
     }
        LOG.debug("total greenfee BA = " + total);
    return myDoubleRound(total,2);  // arrondi à deux décimales
  } // end "BA"
  
  LOG.debug("default case for tarif.getinputtype() = " + tarif.getGreenfeeType());
  // quels cas ??
  return 99.99;
 } catch (Exception e) {
    handleGenericException(e, methodName);
    return 0.0;
 }
} // end method

} //end Class