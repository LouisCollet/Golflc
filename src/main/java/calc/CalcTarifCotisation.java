package calc;

import entite.Player;
import entite.TarifMember;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import utils.LCUtil;
import static utils.LCUtil.myRound;

public class CalcTarifCotisation implements interfaces.GolfInterface, interfaces.Log{

public double findTarif (TarifMember tarifMember, Player player){
     LOG.info(" -- Start of CalcTarifCotisation.findTarif");
     LOG.info(" -- Start of CalcTarifCotisation.findTarif with tarifMember= " + tarifMember.toString());
 try {
 //       à faire : proportion pour abonnement en cours d'année (année incomplète)
          LOG.info("validating cotisation age range- --------");
    int yourAge = utils.LCUtil.calculateAgeFirstJanuary(player.getPlayerBirthDate());
    LOG.info("player age = " + yourAge);
  	for(int i = 0 ; i < tarifMember.getMembersBase().length ; i++) {
                LOG.info("i = " + i);
                LOG.info("base for calculation = " + Arrays.toString(tarifMember.getMembersBase()[i]));
                LOG.info("choice for calculation = " + tarifMember.getMembersChoice()[i]);
             if(tarifMember.getMembersChoice()[i] == 0){
                 LOG.info("skipped : you are not paying this cotisation");
                 continue;} // passe item suivant
             String range = tarifMember.getMembersBase()[i][2];
                LOG.info("age range for this item = " + range);
             if(range.equals("00-00")){
                 LOG.info("accepted : not considering age range");
                 continue;}
             int start = Integer.parseInt(range.substring(0,2));
                LOG.info("start for this item = " + start);
             if(start < 18){
                 LOG.info("accepted : you are older, but we consider you are paying for your kids");
         //        i = i + 1;
                 continue;}
             int end = Integer.parseInt(range.substring(range.length()-2));
                LOG.info("end for this item = " + end);
             if(yourAge < start || yourAge > end){
                String msg = " rejected !!wrong cotisation range for your age !! " + range;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                Integer [] arr = tarifMember.getMembersChoice();
                arr[i] = 0;
                tarifMember.setMembersChoice(arr);
       //         return 9999999.9;
             }
             
        } //end calcul cotisation
 
 
 BigDecimal total = BigDecimal.ZERO;
 double da = 0;
 int quantity = 0;
         LOG.info("calculating cotisation -----------------");
 	for(int i = 0 ; i < tarifMember.getMembersBase().length ; i++) {
                LOG.info("i = " + i);
                LOG.info("base for calculation = " + Arrays.toString(tarifMember.getMembersBase()[i]));
                LOG.info("choice for calculation = " + tarifMember.getMembersChoice()[i]);
            BigDecimal bda = new BigDecimal(tarifMember.getMembersBase()[i][1]); // from String
                LOG.info("amount = " + bda);
            BigDecimal bdq = BigDecimal.valueOf(tarifMember.getMembersChoice()[i]);  // from int
                LOG.info("quantity = " + bdq);
            total = total.add(bda.multiply(bdq));
                LOG.info("total cotisation = " + total);
        } //end calcul cotisation
        
              LOG.info("calculating equipments-----------------");
 	for(int i = 0 ; i < tarifMember.getPriceEquipments().length ; i++) {
            LOG.info("i = " + i);
            LOG.info("getPriceEquipments = " + Arrays.deepToString(tarifMember.getPriceEquipments()[i]));
            LOG.info("choiceEquipments = " + Arrays.deepToString(tarifMember.getEquipmentsChoice()));
            da = Double.parseDouble(tarifMember.getPriceEquipments()[i][1]);
        //    amount = Double.parseDouble(tarifMember.getPriceEquipments()[i][1]); //[1]);
            BigDecimal bda = BigDecimal.valueOf(da);
                LOG.info("equipment price = " + bda);
            quantity = tarifMember.getEquipmentsChoice()[i];
            BigDecimal bdq = BigDecimal.valueOf(quantity);
                LOG.info("quantity = " + bdq);
         //       LOG.info("quantity = " + quantity);
              total = total.add(bda.multiply(bdq));   
                  LOG.info("total after = " + total);
        }
         LOG.info("total for cotisation and equipments = " + total);
          return myRound(total.doubleValue(),2); // arrondi à deux décimales
          
 } catch (Exception e) {
      String msg = " -- Error in calcTarifCotisation " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 9999999.9;
 }
 finally {
   //return 0.0;
// LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
   //   return 99;
} // end method 

 public static void main(String[] args) throws ParseException, Exception {//throws SQLException // testing purposes
try{
    LOG.info("starting main");


//LOG.info("price greenfee = " + dd);
        
 } catch (Exception e) {
            String msg = "££ Exception in main CalcTarif= " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }finally{
      //   DBConnection.closeQuietly(conn, null, null,null); 
          }
}// end main    

} //end class