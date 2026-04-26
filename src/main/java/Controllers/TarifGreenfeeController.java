package Controllers;

import entite.Club;
import entite.EquipmentsAndBasic;
import entite.Greenfee;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import utils.LCUtil;
import static utils.LCUtil.myDoubleRound;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import utils.TimeOverlap;

@ApplicationScoped
public class TarifGreenfeeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.OverlapChecker overlapChecker;

    public TarifGreenfeeController() { }

public TarifGreenfee inputTarifGreenfeePeriods(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
        LOG.debug("with inputTarifGreenfeePeriods with tarifGreenfee = {}", tarifGreenfee);
        if (tarifGreenfee.getStartDate() == null) {
            showMessageFatal(LCUtil.prepareMessageBean("tarif.period.startdate.notnull"));
            return tarifGreenfee;
        }
        if (tarifGreenfee.getEndDate() == null) {
            showMessageFatal(LCUtil.prepareMessageBean("tarif.period.enddate.notnull"));
            return tarifGreenfee;
        }
        if (!tarifGreenfee.getStartDate().isBefore(tarifGreenfee.getEndDate())) {
            String msg = "Start date must be before end date : "
                    + ZDF_DAY.format(tarifGreenfee.getStartDate())
                    + " / " + ZDF_DAY.format(tarifGreenfee.getEndDate());
            LOG.error("- {}", msg);
            showMessageFatal(msg);
            return tarifGreenfee;
        }
  // verification overlapping et chronologie start-end
        if (overlapCheckPeriods(tarifGreenfee)) {
            LOG.debug("yes there is overlap ");
            return tarifGreenfee;
        }
        LOG.debug("no overlap detected - we update !"); 
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

public boolean overlapCheckPeriods(TarifGreenfee tarif) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug("new period : startDate={} endDate={}", tarif.getStartDate(), tarif.getEndDate());
    return overlapChecker.check(
            tarif.getStartDate(), tarif.getEndDate(),
            tarif.getDatesSeasonsList(),
            TarifGreenfee.DatesSeasons::getStartDate,
            TarifGreenfee.DatesSeasons::getEndDate);
} // end method

public TarifGreenfee inputTarifGreenfeeBasic(TarifGreenfee tarifGreenfee){ // used in tarif_equipments.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
        LOG.debug("with inputTarifGreenfeeBasic with tarifGreenfee = {}", tarifGreenfee);
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
    basic.setTwilight(tarifGreenfee.getWorkTwilight() != null ? tarifGreenfee.getWorkTwilight() : "N");
    tarifGreenfee.getBasicList().add(basic);
    String msg = "Tarif Greenfees Basic = " + tarifGreenfee.getBasicList();
    LOG.info(msg);
    showMessageInfo(msg);
    msg = "Total Seasons Periods = " + tarifGreenfee.getDatesSeasonsList();
    LOG.info(msg);
    showMessageInfo(msg);
 //       LOG.debug("tarif Greenfee Basic updated = {}", tarifGreenfee.getBasicList());
// house keeping
    tarifGreenfee.setGreenfeeType("BA");
    tarifGreenfee.setUpdateReady(true); // gestion menu
       LOG.debug("tarif BA setUpdateReady to true");
    if ("Y".equals(tarifGreenfee.getWorkTwilight())) {
        tarifGreenfee.setTwilightReady(true); // affiche le tab Twilight
    }
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
    LOG.debug("entering {}", methodName);
try{
      LOG.debug("with inputTarifGreenfeeEquipments !");
      LOG.debug("with tarif Equipments = {}", tarifGreenfee.getEquipmentsList());
 // 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      String msg = "Fatal error : Season equipments does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      return tarifGreenfee;
  }
  // HO : dériver workSeason depuis le slot sélectionné (garde-fou côté serveur)
    if ("HO".equals(tarifGreenfee.getGreenfeeType()) && tarifGreenfee.getWorkLinkedSlotKey() != null) {
        tarifGreenfee.getTeeTimesList().stream()
                .filter(t -> tarifGreenfee.getWorkLinkedSlotKey().equals(t.getSlotKey()))
                .findFirst()
                .ifPresent(t -> tarifGreenfee.setWorkSeason(t.getSeason()));
    }
  // HO : slot obligatoire
    if ("HO".equals(tarifGreenfee.getGreenfeeType()) && tarifGreenfee.getWorkLinkedSlotKey() == null) {
        String msg = LCUtil.prepareMessageBean("tarif.equipment.slot.required");
        LOG.warn("HO equipment without slot key");
        showMessageFatal(msg);
        return tarifGreenfee;
    }
  // item + price obligatoires
    if (tarifGreenfee.getWorkItem() == null || tarifGreenfee.getWorkItem().isBlank()) {
        showMessageFatal(LCUtil.prepareMessageBean("tarif.equipment.item.required"));
        return tarifGreenfee;
    }
    if (tarifGreenfee.getWorkPrice() == null) {
        showMessageFatal(LCUtil.prepareMessageBean("tarif.equipment.price.required"));
        return tarifGreenfee;
    }
  // duplicate check — inclut linkedSlotKey pour autoriser même item/saison/prix sur slots différents
    boolean isDuplicate = tarifGreenfee.getEquipmentsList().stream().anyMatch(e ->
            java.util.Objects.equals(e.getItem(),          tarifGreenfee.getWorkItem())
         && java.util.Objects.equals(e.getSeason(),        tarifGreenfee.getWorkSeason())
         && java.util.Objects.equals(e.getPrice(),         tarifGreenfee.getWorkPrice())
         && java.util.Objects.equals(e.getLinkedSlotKey(), tarifGreenfee.getWorkLinkedSlotKey()));
    if (isDuplicate) {
        String msg = LCUtil.prepareMessageBean("tarif.equipment.duplicate");
        LOG.warn("duplicate equipment detected");
        showMessageFatal(msg);
        return tarifGreenfee;
    }
  // magic happens here
    EquipmentsAndBasic equipments = new EquipmentsAndBasic(
            tarifGreenfee.getWorkItem(),
            tarifGreenfee.getWorkSeason(),
            tarifGreenfee.getWorkPrice(),
            0); // quantity
    equipments.setLinkedSlotKey(tarifGreenfee.getWorkLinkedSlotKey()); // null if not linked to an HO slot
    tarifGreenfee.getEquipmentsList().add(equipments);
    String msg = "EquipmentsAndBasic after add = " +  tarifGreenfee.getEquipmentsList();
    LOG.info(msg);
    showMessageInfo(msg);
// house keeping
    tarifGreenfee.setWorkItem(null);
    tarifGreenfee.setWorkPrice(null);
    tarifGreenfee.setWorkSeason("A");
    tarifGreenfee.setWorkLinkedSlotKey(null);
    tarifGreenfee.setEquipmentsReady(true); // gestion le menu
   return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return tarifGreenfee;
}
} // end method

 public boolean validPeriod( ArrayList<TarifGreenfee.DatesSeasons> periods, String season) {
    if(season == null || season.equals("A")){  // null or all seasons accepted
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
    LOG.debug("entering {}", methodName);
try{
     LOG.debug("with inputTarifGreenfeeDays with tarifGreenfee = {}", tarifGreenfee);
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
    TarifGreenfee.DaysGreenfee daysGreenfee = new TarifGreenfee.DaysGreenfee();
    daysGreenfee.setSeason(tarifGreenfee.getWorkSeason());
    daysGreenfee.setCategory(tarifGreenfee.getWorkItem());
    daysGreenfee.price[0] = tarifGreenfee.getWorkDaysPrice()[0]; // monday
    daysGreenfee.price[1] = tarifGreenfee.getWorkDaysPrice()[1];
    daysGreenfee.price[2] = tarifGreenfee.getWorkDaysPrice()[2];
    daysGreenfee.price[3] = tarifGreenfee.getWorkDaysPrice()[3];
    daysGreenfee.price[4] = tarifGreenfee.getWorkDaysPrice()[4];
    boolean[] wa = tarifGreenfee.getWorkDaysAvailable();
    if (wa != null && wa.length == 5) {
        daysGreenfee.setAvailable(new boolean[]{wa[0], wa[1], wa[2], wa[3], wa[4]});
    }
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
    tarifGreenfee.setWorkDaysAvailable(new boolean[]{true, true, true, true, true}); // reset availability
//    LOG.debug("line 00");
    tarifGreenfee.setUpdateReady(true); // gestion du menu
 //   LOG.debug("line 01a - twilight = {}", daysGreenfee.getTwilight());
  if(tarifGreenfee.getWorkTwilight().equals("Y")){ // mod 11-04-2025 was "N"
        tarifGreenfee.setTwilightReady(true); // gestion du menu
   }
   LOG.debug("line 01 - twilight = N");
    tarifGreenfee.setWorkTwilight("N");  // init pour affichage
       LOG.debug("tarifGreenfee returned = {}", tarifGreenfee);
  return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

 public TarifGreenfee inputTarifGreenfeeHours(TarifGreenfee tarifGreenfee){// throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
    LOG.debug("with inputTarifHours with tarif = !{}", tarifGreenfee);
            LOG.debug("teeTimes List = "  + tarifGreenfee.getTeeTimesList());
       // 30/04/2022 validation
    if(! validPeriod(tarifGreenfee.getDatesSeasonsList(),tarifGreenfee.getWorkSeason())){
      String msg = "Fatal error : season Hours does not exist !";
      LOG.debug(msg);
      showMessageFatal(msg);
      resetWorkFields(tarifGreenfee);
      return tarifGreenfee;
  }
      if(!tarifGreenfee.getTeeTimesList().isEmpty()){ 
            if(overlapCheckHours(tarifGreenfee)){
                 LOG.debug("There is an overlap Hours ");
                 resetWorkFields(tarifGreenfee);
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
              resetWorkFields(tarifGreenfee);
              return tarifGreenfee;
          }
          if(tarifGreenfee.getEndHour() == null){
              String msg = "Tarif Hours EndHour must be completed"; // + tarifGreenfee.getTeeTimesList();
              LOG.info(msg);
              showMessageFatal(msg);
              resetWorkFields(tarifGreenfee);
              return tarifGreenfee;
          }
    }
    if(tarifGreenfee.getWorkTwilight().equals("P")){   // previous  endHour sera complété par programme
          if(tarifGreenfee.getStartHour() == null){
              String msg = "Tarif Hours StartHour must be completed"; // + tarifGreenfee.getTeeTimesList();
              LOG.info(msg);
              showMessageFatal(msg);
              resetWorkFields(tarifGreenfee);
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
 
    if (tarifGreenfee.getWorkPrice() == null || tarifGreenfee.getWorkPrice() <= 0) {
        String msg = "Tarif Hours Price must be completed";
        LOG.info(msg);
        showMessageFatal(msg);
        resetWorkFields(tarifGreenfee);
        return tarifGreenfee;
    }

// the magic happens here !
        TarifGreenfee.TeeTimes teeTimes = new TarifGreenfee.TeeTimes();
        teeTimes.setStartTime(tarifGreenfee.getStartHour());
        teeTimes.setEndTime(tarifGreenfee.getEndHour());
        teeTimes.setSeason(tarifGreenfee.getWorkSeason());
        teeTimes.setItem(tarifGreenfee.getWorkItem());
        teeTimes.setPrice(tarifGreenfee.getWorkPrice());
        teeTimes.setQuantity(0);
        teeTimes.setTwilight(tarifGreenfee.getWorkTwilight());   // yes or no
        teeTimes.setSlotKey(UUID.randomUUID().toString().substring(0, 8)); // short unique key for equipment linking
        tarifGreenfee.getTeeTimesList().add(teeTimes);
  //        LOG.debug("TeeTimesList after add = {}", tarifGreenfee.getTeeTimesList());
        String msg = "Tarif Hours = " + tarifGreenfee.getTeeTimesList();
        LOG.info(msg);
        showMessageInfo(msg);
  // house keeping
       tarifGreenfee.setGreenfeeType("HO");  // for Hours
  //  if(tarifGreenfee.getWorkTwilight().equals("N")){ //enlevé 24-06-2022
     if(tarifGreenfee.getWorkTwilight().equals("Y")){ //modifié 11-04-2025 
          tarifGreenfee.setTwilightReady(true); // gestion du menu : écran
    }
       resetWorkFields(tarifGreenfee);
       tarifGreenfee.setUpdateReady(true); // gestion du menu
   return tarifGreenfee; 
} catch (Exception e) {
    handleGenericException(e, methodName);
    resetWorkFields(tarifGreenfee);
    return tarifGreenfee;
}
} // end method

private void resetWorkFields(TarifGreenfee tarifGreenfee) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    tarifGreenfee.setStartHour(null);
    tarifGreenfee.setEndHour(null);
    tarifGreenfee.setWorkItem(null);
    tarifGreenfee.setWorkPrice(null);
} // end method

public TarifGreenfee inputTarifGreenfeeTwilight(TarifGreenfee tarifGreenfee) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
     LOG.debug("with inputTarifTwilight with tarifGreenfee = {}", tarifGreenfee);
     // soit months est complété, soit season est complété
     List<Integer> months = null;
     if(tarifGreenfee.getMultiTwilight() != null){ // multiple months
         String msg = "input LocalDates Twilight = "  + tarifGreenfee.getMultiTwilight().toString(); // list with LocalDates
         LOG.info(msg);
         showMessageInfo(msg);
         months = tarifGreenfee.getMultiTwilight().stream()
              .map(x -> x.getMonthValue())
              .collect(Collectors.toList());
         LOG.debug("List months twilight = {}", months);
         // Check for duplicate months already in twilightList
         List<Integer> existingMonths = tarifGreenfee.getTwilightList().stream()
              .filter(tw -> tw.getMonths() != null)
              .flatMap(tw -> tw.getMonths().stream())
              .collect(Collectors.toList());
         List<Integer> duplicates = months.stream()
              .filter(existingMonths::contains)
              .collect(Collectors.toList());
         if (!duplicates.isEmpty()) {
             LOG.warn("duplicate twilight months = {}", duplicates);
             showMessageFatal(LCUtil.prepareMessageBean("tarif.twilight.month.duplicate", " : " + duplicates));
             return tarifGreenfee;
         }
     }
 //    tarifGreenfee.getMultiTwilight().forEach(item -> LOG.debug("Twilight list {}", item));

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
   TarifGreenfee.Twilight twilight = new TarifGreenfee.Twilight();
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
       LOG.debug("tarifGreenfee returned = {}", tarifGreenfee);
  return tarifGreenfee;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method
  
 public boolean overlapCheckHours(TarifGreenfee tarif){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
       LOG.debug("existing hours = {}", tarif.getTeeTimesList());
       LOG.debug("trying to insert new hours = {}/{}", tarif.getStartHour(), tarif.getEndHour());
 // candidate
  if(tarif.getStartHour() == null || tarif.getEndHour() == null){ 
       return false;
 }
     TimeOverlap.LocalTimeRange range1 = new TimeOverlap.LocalTimeRange(tarif.getStartHour(),tarif.getEndHour());
  for (int i = 0; i < tarif.getTeeTimesList().size(); i++) {
            var v =  tarif.getTeeTimesList().get(i);
            if(v.getStartTime() == null || v.getEndTime() == null               // skip control en entrée : twilight or previous
                || tarif.getStartHour() == null || tarif.getEndHour() == null) { // skip control déjà enregistré : twilight or previous
                return false;
            }
            // compare seulement les slots de la même saison — "A" (toutes saisons) compare toujours
            boolean sameSeason = "A".equals(v.getSeason())
                              || "A".equals(tarif.getWorkSeason())
                              || v.getSeason().equals(tarif.getWorkSeason());
            if (!sameSeason) {
                LOG.debug("skip overlap check : different seasons {} vs {}", v.getSeason(), tarif.getWorkSeason());
                continue;
            }
            LocalTime ltdeb = v.getStartTime().truncatedTo(ChronoUnit.MINUTES);
            LocalTime ltend = v.getEndTime().truncatedTo(ChronoUnit.MINUTES);
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
        LOG.debug("with tarif = {}", tarif);
     double d = this.calcGreenfeePrice(tarif);
        LOG.debug("le prix du greenfee et des équipements est {}", d);
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
      if("DA".equals(tarif.getGreenfeeType())){
           LOG.debug("inputtype = DA");
         greenfee.setStatus("Y");
           LOG.debug("status changed to  = {}", greenfee.getStatus());
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
     //           LOG.debug("after append sb = {}", sb.toString());
                        greenfee.setStatus("Y"); // le player a payé son greenfee et pas uniquement des équipements
    //          } //end if
         } //end for
 //      sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
            LOG.debug("final Items for greenfee = {}", sb);
            
  /// constitution liste items pour equipments
        for(int i = 0 ; i < tarif.getEquipmentsList().size() ; i++) { 
                 var v = tarif.getEquipmentsList().get(i);
                  sb.append(v.getItem()) // item      
                  .append(" (")
                  .append(v.getSeason())
                  .append(v.getSeason())
                  .append(v.getQuantity())
                  .append("),");
     //           LOG.debug("after append sb = {}", sb.toString());
       //       } //end if 
         } //end for
   //    LOG.debug("after for loop");
       int lastComma = sb.lastIndexOf(",");
       if (lastComma >= 0) { sb.deleteCharAt(lastComma); } // delete dernière virgule
            LOG.debug("final Items for greenfee and equipments = {}", sb);
      greenfee.setItems(sb.toString());
  return greenfee;
  
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} //end method

 
 public double calcGreenfeePrice(TarifGreenfee tarif){
     final String methodName = utils.LCUtil.getCurrentMethodName();
     LOG.debug(" -- Start of calc.CalcGreenfeePrice with tarif= {}", tarif);
     // calcul le prix total à payer par creditcard
 try {
    double total = 0;
     LOG.debug( " ,getPriceGreenfee: "   + tarif.getPriceGreenfee());
     LOG.debug("GreenfeeType = {}", tarif.getGreenfeeType());
     LOG.debug(" ,equipmentChoosen : {}", tarif.getEquipmentChoosen());
     LOG.debug("calculating equipments-----------------");
       for (EquipmentsAndBasic equipment : tarif.getEquipmentChoosen()) {
           total = total + (equipment.getPrice() * equipment.getQuantity());
       }
     LOG.debug("total for equipments = {}", total);

LOG.debug("calculating greenfee -----------------");

 if (tarif.getGreenfeeType() == null) {
     String msg = methodName + " - greenfeeType is null for tarif courseId=" + tarif.getTarifCourseId()
             + " — pricing type must be selected (BA/HO/DA)";
     LOG.error(msg);
     LCUtil.showMessageFatal(msg);
     return 0.0;
 }

 if("DA".equals(tarif.getGreenfeeType())){
       LOG.debug("Calculating greenfee for inputtype = DA");  // DAYS 
   //  new 02-05-2021
          LOG.debug("getDayChoosen  = {}", tarif.getDayChoosen());
       for (TarifGreenfee.DaysWeek day : tarif.getDayChoosen()) {
            total = total + (day.getPrice() * day.getQuantity());
       }
          LOG.debug("total greenfee DA = {}", total);
     return myDoubleRound(total,2);  // arrondi à deux décimales
} // end "DA"
 
  if("HO".equals(tarif.getGreenfeeType())){
          LOG.debug("handling inputType HO"); // hours
 //         LOG.debug(" teeTimesList: "   + tarif.getTeeTimesList());
          LOG.debug(" teeTimeChoosen: "   + tarif.getTeeTimeChoosen());
        for (TarifGreenfee.TeeTimes hours : tarif.getTeeTimeChoosen()) {  // mod 23-06-2021 inner class Teetimes within TarifGreenfee !
     //  for (TeeTimes hours : tarif.getTeeTimeChoosen()) {
            total = total + (hours.getPrice() * hours.getQuantity());
       }
        LOG.debug("total greenfee HO = {}", total);
     return myDoubleRound(total,2);  // arrondi à deux décimales      
 } // end "HO"

  if("BA".equals(tarif.getGreenfeeType())){
         LOG.debug("Calculating greenfee for inputtype = BA");
         LOG.debug(" basicList: "   + tarif.getBasicList());
      
     for(EquipmentsAndBasic basic : tarif.getBasicList()) {
         total = total + (basic.getPrice() * basic.getQuantity());
     }
        LOG.debug("total greenfee BA = {}", total);
    return myDoubleRound(total,2);  // arrondi à deux décimales
  } // end "BA"
  
  LOG.debug("default case for tarif.getinputtype() = {}", tarif.getGreenfeeType());
  // quels cas ??
  return 99.99;
 } catch (Exception e) {
    handleGenericException(e, methodName);
    return 0.0;
 }
} // end method

} //end Class