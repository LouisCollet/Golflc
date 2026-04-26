package Controllers;

import entite.Cotisation;
import entite.EquipmentsAndBasic;
import entite.EquipmentsAndBasicAndRange;
import entite.Player;
import entite.TarifMember;
//import static interfaces.GolfInterface.temporalFirst;
//import static interfaces.GolfInterface.temporalLast;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class TarifMemberController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private Controller.refact.PlayerController playerController;

    public TarifMemberController() { }

 
 
public TarifMember inputTarifMembersCotisation(TarifMember tarifMember) throws SQLException, Exception{  // used in tarif_members.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
    LOG.debug("with inputTarifMembersCotisation with tarifMember = !"+ tarifMember);
    LOG.debug("workRangeAge = {}", tarifMember.getWorkRangeAge());
    if (tarifMember.getStartDate() == null || tarifMember.getEndDate() == null) {
        String msg = "period not created — cannot add cotisation item";
        LOG.error(msg);
        showMessageFatal(msg);
        return tarifMember;
    }
    if (tarifMember.getWorkRangeAge() == null || tarifMember.getWorkRangeAge().isBlank()) { // pas complété dans écran
        tarifMember.setWorkRangeAge("00-00");
        LOG.debug("workRangeAge was null, setted to : {}", tarifMember.getWorkRangeAge());
    }
    for (EquipmentsAndBasicAndRange existing : tarifMember.getBasicList()) {
        if (rangesOverlap(existing.getRange(), tarifMember.getWorkRangeAge())) {
            String msg = LCUtil.prepareMessageBean("tarif.member.range.overlap") + " " + existing.getRange();
            LOG.warn(msg);
            showMessageFatal(msg);
            return tarifMember;
        }
    }
    EquipmentsAndBasicAndRange basic = new EquipmentsAndBasicAndRange(
            tarifMember.getStartDate(),
            tarifMember.getEndDate(),
            tarifMember.getWorkItem(),
            tarifMember.getWorkRangeAge(),
            tarifMember.getWorkPrice(),
            0); // quantity
    tarifMember.getBasicList().add(basic);
  // house keeping
    tarifMember.setWorkItem(null); // pour le prochain affichage
    tarifMember.setWorkPrice(null);
    tarifMember.setWorkRangeAge(null);
    String msg = "inputTarifMembers updated = " + tarifMember.getBasicList();
        LOG.debug(msg);
        showMessageInfo(msg);
   return tarifMember;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

public TarifMember inputTarifMembersEquipments(TarifMember tarifMember) throws SQLException, Exception{  // used in tarif_equipments.xhtml
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
try{
    LOG.debug("with inputTarifMembersEquipments with tarifMember = "+ tarifMember);
    EquipmentsAndBasic equipments = new EquipmentsAndBasic( // mod 09/05/2022
            tarifMember.getWorkItem(),
            "H", // season default ??
            tarifMember.getWorkPrice()
            ,0); //quantity
    tarifMember.getEquipmentsList().add(equipments);
 // house keeping
    tarifMember.setWorkItem(null); // init pour le prochain affichage
    tarifMember.setWorkPrice(null);
    String msg = "Tarif Member Equipments updated= " + tarifMember.getEquipmentsList();
    LOG.info(msg);
    showMessageInfo(msg);
  return tarifMember; 
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} // end method

public Cotisation completeCotisation(TarifMember tarif, Player player, LocalDate referenceDate) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName();
     Cotisation cotisation = new Cotisation();
    try{
        LOG.debug("entering {}", methodName);
        LOG.debug("with TarifMember = {}", tarif);
        LOG.debug("for Player = {}", player.getIdplayer());
        LOG.debug("for referenceDate = {}", referenceDate);
     // de 00:00 heures à 23:59 introduit le 07-04-2021
     cotisation.setCotisationError(false);
     /* validation sur date début et fin !
     if(cotisation.getCotisationStartDateTime().isAfter(cotisation.getCotisationEndDateTime())){
         String msg = "error cotisation start is after end";
         LOG.debug(msg);
         showMessageFatal(msg);
         return null;
     }
   */
    LocalDateTime reference = referenceDate.atStartOfDay();
    cotisation.setCotisationStartDate(reference.with(TemporalAdjusters.firstDayOfYear()));
    cotisation.setCotisationEndDate(reference.with(TemporalAdjusters.lastDayOfYear()));
    
  //   LocalDateTime firstDayOfYear = LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear());
 //   LocalDateTime first = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
  //  LOG.debug("firstDayOfYear = {}", firstDayOfYear);
    
       LOG.debug("with Cotisation modified start and end date= {}", cotisation);    
 //      LOG.debug("cotisation startDate = {}", cotisation.getCotisationStartDate());
 //      LOG.debug("cotisation endDate   = {}", cotisation.getCotisationEndDate());
    cotisation = this.calcCotisationPrice(tarif, player, cotisation);
          LOG.debug("cotisation with price calculated = {}", cotisation.getPrice());
    if(cotisation.getPrice() == 0.0){
            String msgerr = "Le total est zéro - il faut choisir au moins un item !!!";
            LOG.error(msgerr);
            showMessageFatal(msgerr);
            return cotisation;
         }
       StringBuilder sb = new StringBuilder("");
      LOG.debug("starting cotisation");
       for(int i = 0 ; i < tarif.getBasicList().size() ; i++) {
                var v = tarif.getBasicList().get(i);
                if (v.getQuantity() == null || v.getQuantity() <= 0) continue; // skip unselected
                sb.append(v.getItem())
                  .append(" (")
                  .append(v.getPrice())
                  .append("*").append(v.getQuantity())
                  .append("),");
                cotisation.setStatus("Y");
         } //end for

     LOG.debug("starting equipment");
       for(int i = 0 ; i < tarif.getEquipmentsList().size() ; i++) {
                var v = tarif.getEquipmentsList().get(i);
                if (v.getQuantity() == null || v.getQuantity() <= 0) continue; // skip unselected
                sb.append(v.getItem())
                  .append(" (")
                  .append(v.getPrice())
                  .append("*")
                  .append(v.getQuantity())
                  .append("),");
         } //end for
         if (sb.length() > 0) {
             sb.deleteCharAt(sb.lastIndexOf(","));
         }
         LOG.debug("selected items = {}", sb);
      cotisation.setItems(sb.toString());
  return cotisation;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} //end method
public Cotisation calcCotisationPrice (TarifMember tarif, Player player, Cotisation cotisation){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    LOG.debug(" -- Start of CalcTarifMember.findTarif with tarifMember= {}", tarif);
    LOG.debug(" -- Start of CalcTarifMember.findTarif with cotisation= {}", cotisation);
 try {
 //       à faire : proportion pour abonnement en cours d'année (année incomplète)
          LOG.debug("validating cotisation age range- --------");
     int yourAge = playerController.calculateAgeFirstJanuary(player.getPlayerBirthDate());

     // calcul du nombre de jours
  //    Duration dur = Duration.between(tarifMember.getMemberStartDate(),
  //                                    tarifMember.getMemberEndDate());
    //  int days = (int)dur.toDays();
  //    double days = dur.toDays();
  //       LOG.debug("duration in days = {}", days);
  //    if(days < 0){
 //         LOG.debug("negative days = {}", days);
 //     }
      
      // à faire : paiement pour des mois, y compris mois incomplets !
    
    LOG.debug("player age = {}", yourAge);

  	for(int i = 0 ; i < tarif.getBasicList().size() ; i++) {
                LOG.debug("i = {}", i);
             EquipmentsAndBasicAndRange currentItem = tarif.getBasicList().get(i);
             if (currentItem.getQuantity() == null || currentItem.getQuantity() <= 0) {
                 LOG.debug("skipped : quantity = 0, not in cart");
                 continue;
             }
             String range = currentItem.getRange();
                LOG.debug("age range for this item = {}", range);
             if(range.equals("00-00")){
                 LOG.debug("accepted : not considering age range");
                 continue;}
             int startRange = Integer.parseInt(range.substring(0,2));
                LOG.debug("start for this item = {}", startRange);
             if(startRange < 18){
                 String msg = "Accepted : you are older, but we consider you are paying for your kids";
                 LOG.debug(msg);
                 LCUtil.showMessageInfo(msg);
                 continue;}
             int endRange = Integer.parseInt(range.substring(range.length()-2));
                LOG.debug("end for this item = {}", endRange);
 // to do : ne pas aller vers creditcard !!               
             if(yourAge < startRange || yourAge > endRange){
                String msg = "Rejected !!wrong cotisation range : " + range + " for your age = " + yourAge;
                LOG.info(msg);
            //    showMessageInfo(msg);
            //    cotisation.setCotisationError(true);  //attention ici !!
            //    Integer [] arr = tarif.getMembersChoice();
            //    arr[i] = 0;
            //    tarif.setMembersChoice(arr);
       //         return 9999999.9;
             }
        } //end calcul cotisation
  
 double total = 0.0;
// int quantity = 0;
         LOG.debug("calculating cotisation -----------------");
     for(EquipmentsAndBasicAndRange basic : tarif.getBasicList()) {
         total = total + (basic.getPrice() * basic.getQuantity());
     }
        LOG.debug("total after cotisation member= {}", total);

       LOG.debug("calculating equipments- 07/05/2022----------------");
       for (EquipmentsAndBasic equipment : tarif.getEquipmentsList()) {
           total = total + (equipment.getPrice() * equipment.getQuantity());
       }
         LOG.debug("total after equipments = {}", total);
     cotisation.setCommunication(cotisation.getCommunication() + total);       
         

     double discount = 0.0;
        LOG.debug("tarif.getDiscount = {}", tarif.getDiscount());
        
  //   if(tarif.getDiscount() == null){  // new 05-06-2021 pour continuer, à revoir !!
 //       tarif.setDiscount("Days");
  //   }
     cotisation.setCommunication(LCUtil.prepareMessageBean("cotisation.communication"));
     if(tarif.getDiscount().equals("Year") || tarif.getDiscount() == null){
//          LOG.debug("number of days = {}", Year.of(year).length());
             LOG.debug("discount = Year - no discount !");
     }
     if(tarif.getDiscount().equals("Months")){
             LOG.debug("entering discount = Months !");
             BigDecimal months = BigDecimal.valueOf(
                  ChronoUnit.MONTHS.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate()));
                LOG.debug("duration in months = {}", months);
///             discount = total.multiply(BigDecimal.valueOf(12).subtract(months)).divide(BigDecimal.valueOf(12),RoundingMode.HALF_UP);
                LOG.debug("discount for months = {}", discount);
///             total = total.subtract(discount);
             cotisation.setCommunication(cotisation.getCommunication() + " - Discount " + months + " Months = " + discount);
         }
   // à vérifier
         if(tarif.getDiscount().equals("Days")){
                LOG.debug("entering discount = Days  !");
             int year = cotisation.getCotisationEndDate().getYear();
                LOG.debug("year = {}", year);
           //  int days = Year.of(year).length();
             int days = Year.of(cotisation.getCotisationEndDate().getYear()).length();
                LOG.debug("duration Year in days = {}", days);
    //         double daysYear = Double.valueOf(days);
             double daysPeriod = Double.valueOf(
                        ChronoUnit.DAYS.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate()));
                LOG.debug("duration period in days = {}", daysPeriod);
             Duration duration = Duration.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate());  
              long result = duration.toDays();     
                LOG.debug("duration period in days # 2 = {}", result);
      //       discount = total.multiply(BigDecimal.valueOf(365).subtract(daysPeriod)).divide(BigDecimal.valueOf(365),RoundingMode.HALF_UP);
// mod             discount = (daysYear.subtract(daysPeriod)).multiply(total).divide(BigDecimal.valueOf(365),RoundingMode.HALF_UP);
             LOG.debug("discount for days = {}", discount);
         //    total = total.multiply(days).divide(new BigDecimal("365"));
 /// mod            total = total.subtract(discount);
             cotisation.setCommunication(cotisation.getCommunication() + " - Discount " + days + " Days = " + discount);
         }
         LOG.debug("total for cotisation and equipments after discount = {}", total);
         cotisation.setPrice(utils.LCUtil.myDoubleRound(total,2));
      return cotisation;
 } catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
 }
} // end method

    /**
     * Check if two age ranges overlap.
     * Format: "XX-YY" where XX = min age, YY = max age.
     * "00-00" is the wildcard "all ages" — overlaps with anything, including another "00-00".
     */
    private boolean rangesOverlap(String rangeA, String rangeB) {
        if (rangeA == null || rangeB == null) return false;
        if ("00-00".equals(rangeA) || "00-00".equals(rangeB)) return true;
        int[] a = parseRange(rangeA);
        int[] b = parseRange(rangeB);
        if (a == null || b == null) return false;
        return a[0] <= b[1] && b[0] <= a[1];
    } // end method

    private int[] parseRange(String range) {
        try {
            String[] parts = range.split("-");
            if (parts.length != 2) return null;
            return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
        } catch (NumberFormatException ex) {
            LOG.warn("invalid range format: {}", range);
            return null;
        }
    } // end method
} //end Class