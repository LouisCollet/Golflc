package Controllers;

import entite.Cotisation;
import entite.EquipmentsAndBasic;
import entite.EquipmentsAndBasicAndRange;
import entite.Player;
import entite.Round;
import entite.TarifMember;
//import static interfaces.GolfInterface.temporalFirst;
//import static interfaces.GolfInterface.temporalLast;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

//used in CourseController
public class TarifMemberController implements interfaces.Log{
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public TarifMemberController(){  // constructor
    }

 
 
public TarifMember inputTarifMembersCotisation(TarifMember tarifMember) throws SQLException, Exception{  // used in tarif_members.xhtml
try{
    LOG.debug("entering inputTarifMembersCotisation with tarifMember = !"+ tarifMember);
  LOG.debug("workRangeAge = " + tarifMember.getWorkRangeAge());
     if(tarifMember.getWorkRangeAge() == null){ // pas complété dans écran
         tarifMember.setWorkRangeAge("00-00");
         LOG.debug("workRangeAge was null, setted to : " + tarifMember.getWorkRangeAge());
    }
     // à modifier : on prend toujours les dates de la dernière période introduite !
    tarifMember.setStartDate(tarifMember.getWorkStartDate());
    tarifMember.setEndDate(tarifMember.getWorkEndDate());
    EquipmentsAndBasicAndRange basic = new EquipmentsAndBasicAndRange( // mod 09/05/2022
            tarifMember.getWorkStartDate(),
            tarifMember.getWorkEndDate(),
            tarifMember.getWorkItem(),
            tarifMember.getWorkRangeAge(),
            tarifMember.getWorkPrice(),
            0) // quantity
            ; 
    tarifMember.getBasicList().add(basic);
  // house keeping
    tarifMember.setWorkItem(null); // pour le prochain affichage
    tarifMember.setWorkPrice(null);
    tarifMember.setWorkRangeAge(null);
    tarifMember.setWorkStartDate(null);
    tarifMember.setWorkEndDate(null);
    String msg = "inputTarifMembers updated = " + tarifMember.getBasicList();
        LOG.debug(msg);
        showMessageInfo(msg);
   return tarifMember;
}catch(Exception ex){
    String msg = "inputTarifMembers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method

public TarifMember inputTarifMembersEquipments(TarifMember tarifMember) throws SQLException, Exception{  // used in tarif_equipments.xhtml
try{
       LOG.debug("entering inputTarifMembersEquipments with tarifMember = "+ tarifMember);
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
}catch(Exception ex){
    String msg = "inputTarifMembersEquipments Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
 } // end method

public Cotisation completeCotisation(TarifMember tarif, Player player, Round round) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     Cotisation cotisation = new Cotisation();
    try{
        LOG.debug("entering " + methodName);
        LOG.debug("with TarifMember = " + tarif);
        LOG.debug("for Player = " + player.getIdplayer());
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
  //  à faire ?? à vérifier utiliser la date round pour déterminer start et enddate ?? 

           LOG.debug("fake start and end hours !!  needs registration real dates , needs new screen !!");
 //   TemporalAdjuster temporalLast = TemporalAdjusters.lastDayOfYear();
 //   LocalDateTime lastDayOfYear = LocalDateTime.now().with(temporalLast).withNano(0);
    cotisation.setCotisationStartDate(LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear()));
 //   LocalDateTime lastDayOfYear = LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear()).withNano(0);
 //       LOG.debug("lastDayOfYear = " + lastDayOfYear);
  // cotisation.setCotisationStartDate(LocalDateTime.now().with(temporalFirst).withNano(0)); TemporalAdjusters.firstDayOfYear()
  //  cotisation.setCotisationEndDate(LocalDateTime.parse("2023-12-31T23:59:59"));
  //  cotisation.setCotisationEndDate(LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59));
    cotisation.setCotisationEndDate(LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear()));
    
  //   LocalDateTime firstDayOfYear = LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear());
 //   LocalDateTime first = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
  //  LOG.debug("firstDayOfYear = " + firstDayOfYear);
    
       LOG.debug("with Cotisation modified start and end date= " + cotisation);    
 //      LOG.debug("cotisation startDate = " + cotisation.getCotisationStartDate());
 //      LOG.debug("cotisation endDate   = " + cotisation.getCotisationEndDate());
    cotisation = new TarifMemberController().calcCotisationPrice(tarif, player, cotisation); 
          LOG.debug("cotisation with price calculated = " + cotisation.getPrice());
    if(cotisation.getPrice() == 0.0){
            String msgerr = "Le total est zéro - il faut choisir au moins un item !!!";
            LOG.error(msgerr);
            showMessageFatal(msgerr);
            return cotisation;
         }
       StringBuilder sb = new StringBuilder("");
      LOG.debug("starting cotisation");
 //      cotisation.setStatus("N"); // utilisé par savoir facilement si le member est en ordre pour la période
       for(int i = 0 ; i < tarif.getBasicList().size() ; i++) {
                var v = tarif.getBasicList().get(i);
                sb.append(v.getItem()) // item
                  .append(" (")
                  .append(v.getPrice()) //prix
                  .append("*").append(v.getQuantity())
                  .append("),");
                cotisation.setStatus("Y"); // ?? le player a payé sa cotisation et est abonné pour la période
         } //end for

  /// constitution items pour equipments
     LOG.debug("starting equipment");
       for(int i = 0 ; i < tarif.getEquipmentsList().size() ; i++) {
                var v = tarif.getEquipmentsList().get(i);
                sb.append(v.getItem()) // item
                  .append(" (")
                  .append(v.getPrice()) //prix
                  .append("*")
                  .append(v.getQuantity())
                  .append("),");
         } //end for
          sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //         LOG.debug("final selectedItems = " + sb);
      cotisation.setItems(sb.toString());
  return cotisation;
}catch (Exception ex){
    String msg = "Exception in " + methodName + " / " +ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return null;
}
finally{}
} //end method
public Cotisation calcCotisationPrice (TarifMember tarif, Player player, Cotisation cotisation){
     LOG.debug(" -- Start of CalcTarifMember.findTarif");
     LOG.debug(" -- Start of CalcTarifMember.findTarif with tarifMember= " + tarif);
     LOG.debug(" -- Start of CalcTarifMember.findTarif with cotisation= " + cotisation);
 try {
 //       à faire : proportion pour abonnement en cours d'année (année incomplète)
          LOG.debug("validating cotisation age range- --------");
     int yourAge = utils.LCUtil.calculateAgeFirstJanuary(player.getPlayerBirthDate());

     // calcul du nombre de jours
  //    Duration dur = Duration.between(tarifMember.getMemberStartDate(),
  //                                    tarifMember.getMemberEndDate());
    //  int days = (int)dur.toDays();
  //    double days = dur.toDays();
  //       LOG.debug("duration in days = " + days);
  //    if(days < 0){
 //         LOG.debug("negative days = " + days);
 //     }
      
      // à faire : paiement pour des mois, y compris mois incomplets !
    
    LOG.debug("player age = " + yourAge);

  	for(int i = 0 ; i < tarif.getBasicList().size() ; i++) {
                LOG.debug("i = " + i);
   //             LOG.debug("base for calculation = " + Arrays.toString(tarif.getMembersBase()[i]));
//                LOG.debug("choice for calculation = " + tarif.getMembersChoice()[i]);
 //            if(tarif.getMembersChoice()[i] == 0){
 //                LOG.debug("skipped : you are not paying this cotisation");
 //                continue;} // passe item suivant
        //     String range = tarif.getMembersBase()[i][2];
             String range = tarif.getBasicList().get(i).getRange();
                LOG.debug("age range for this item = " + range);
             if(range.equals("00-00")){
                 LOG.debug("accepted : not considering age range");
                 continue;}
             int startRange = Integer.parseInt(range.substring(0,2));
                LOG.debug("start for this item = " + startRange);
             if(startRange < 18){
                 String msg = "Accepted : you are older, but we consider you are paying for your kids";
                 LOG.debug(msg);
                 LCUtil.showMessageInfo(msg);
                 continue;}
             int endRange = Integer.parseInt(range.substring(range.length()-2));
                LOG.debug("end for this item = " + endRange);
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
        LOG.debug("total after cotisation member= " + total);

       LOG.debug("calculating equipments- 07/05/2022----------------");
       for (EquipmentsAndBasic equipment : tarif.getEquipmentsList()) {
           total = total + (equipment.getPrice() * equipment.getQuantity());
       }
         LOG.debug("total after equipments = " + total);
     cotisation.setCommunication(cotisation.getCommunication() + total);       
         

     double discount = 0.0;
        LOG.debug("tarif.getDiscount = " + tarif.getDiscount());
        
  //   if(tarif.getDiscount() == null){  // new 05-06-2021 pour continuer, à revoir !!
 //       tarif.setDiscount("Days");
  //   }
     cotisation.setCommunication(LCUtil.prepareMessageBean("cotisation.communication"));
     if(tarif.getDiscount().equals("Year") || tarif.getDiscount() == null){
//          LOG.debug("number of days = " + Year.of(year).length());
             LOG.debug("discount = Year - no discount !");
     }
     if(tarif.getDiscount().equals("Months")){
             LOG.debug("entering discount = Months !");
             BigDecimal months = BigDecimal.valueOf(
                  ChronoUnit.MONTHS.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate()));
                LOG.debug("duration in months = " + months);
///             discount = total.multiply(BigDecimal.valueOf(12).subtract(months)).divide(BigDecimal.valueOf(12),RoundingMode.HALF_UP);
                LOG.debug("discount for months = " + discount);
///             total = total.subtract(discount);
             cotisation.setCommunication(cotisation.getCommunication() + " - Discount " + months + " Months = " + discount);
         }
   // à vérifier
         if(tarif.getDiscount().equals("Days")){
                LOG.debug("entering discount = Days  !");
             int year = cotisation.getCotisationEndDate().getYear();
                LOG.debug("year = " + year);
           //  int days = Year.of(year).length();
             int days = Year.of(cotisation.getCotisationEndDate().getYear()).length();
                LOG.debug("duration Year in days = " + days);
    //         double daysYear = Double.valueOf(days);
             double daysPeriod = Double.valueOf(
                        ChronoUnit.DAYS.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate()));
                LOG.debug("duration period in days = " + daysPeriod);
             Duration duration = Duration.between(cotisation.getCotisationStartDate(), cotisation.getCotisationEndDate());  
              long result = duration.toDays();     
                LOG.debug("duration period in days # 2 = " + result);
      //       discount = total.multiply(BigDecimal.valueOf(365).subtract(daysPeriod)).divide(BigDecimal.valueOf(365),RoundingMode.HALF_UP);
// mod             discount = (daysYear.subtract(daysPeriod)).multiply(total).divide(BigDecimal.valueOf(365),RoundingMode.HALF_UP);
             LOG.debug("discount for days = " + discount);
         //    total = total.multiply(days).divide(new BigDecimal("365"));
 /// mod            total = total.subtract(discount);
             cotisation.setCommunication(cotisation.getCommunication() + " - Discount " + days + " Days = " + discount);
         }
         LOG.debug("total for cotisation and equipments after discount = " + total);
         cotisation.setPrice(utils.LCUtil.myDoubleRound(total,2));
      return cotisation;
 } catch (Exception e) {
      String msg = " -- Error in calcTarifMember " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 }
 finally { }
} // end method 
} //end Class