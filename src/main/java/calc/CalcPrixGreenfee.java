package calc;

import entite.TarifGreenfee;
import java.text.ParseException;
import java.util.Arrays;
import utils.LCUtil;
import static utils.LCUtil.myRound;

public class CalcPrixGreenfee implements interfaces.GolfInterface, interfaces.Log{

public double calc(TarifGreenfee tarifGreenfee){
     LOG.info(" -- Start of calc.CalcPrixGreenfee");
     LOG.info(" -- Start of calc.CalcPrixGreenfee with tarifGreenfee= " + tarifGreenfee.toString());
 try {
 double total = 0;
 double amount = 0;
// int quantity = 0;
 
     LOG.info(" ,price equipments : " + Arrays.deepToString(tarifGreenfee.getPriceEquipments()) );
     LOG.info(" ,choice equipments: "   + Arrays.deepToString(tarifGreenfee.getEquipmentsChoice()));
     LOG.info( " ,getPriceGreenfee: "   + tarifGreenfee.getPriceGreenfee());
     LOG.info(" ,getPriceGreenfees: "   + Arrays.deepToString(tarifGreenfee.getPriceGreenfees()));
     LOG.info(" ,choice greenfees: "   + Arrays.deepToString(tarifGreenfee.getGreenfeesChoice()));

     LOG.info("calculating equipments-----------------");
 	for(int i = 0 ; i < tarifGreenfee.getPriceEquipments().length ; i++) {
            LOG.info("i = " + i);
            LOG.info("getPriceEquipments = " + Arrays.deepToString(tarifGreenfee.getPriceEquipments()[i]));
            LOG.info("choiceEquipments = " + Arrays.deepToString(tarifGreenfee.getEquipmentsChoice())); //[1]);
            amount = Double.parseDouble(tarifGreenfee.getPriceEquipments()[i][1]); //[1]);
                LOG.info("equipment price = " + amount);
            int quantity = tarifGreenfee.getEquipmentsChoice()[i];
                LOG.info("quantity = " + quantity);
       //     if(quantity > 0){
       //         LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
      //          selectedItems = selectedItems + tarifMember.getMembersBase()[i][0] + ",";
                LOG.info("total before = " + total);
            total = total + (amount * quantity);
                LOG.info("total  after = " + total);
        }
         LOG.info("total for equipments = " + total);
  LOG.info("calculating greenfee -----------------");
  
    if(tarifGreenfee.getInputtype().equals("DA") || tarifGreenfee.getInputtype().equals("HO")){
            LOG.info("inputtype = DA or HO");  // DAYS of HOURS
        amount = tarifGreenfee.getPriceGreenfee();
            LOG.info("greenfee amount format double = " + amount);
        int quantity = tarifGreenfee.getGreenfeesChoice()[0];
            LOG.info("quantity = " + quantity);
        total = total + (amount * quantity);
            LOG.info("total = " + total);
   //  return total;
     return myRound(total,2);  // arrondi à deux décimales
    }
  
 	for(int i = 0 ; i < tarifGreenfee.getPriceGreenfees().length ; i++) {
            LOG.info("i = " + i);
            LOG.info("getPriceGreenfees = " + Arrays.deepToString(tarifGreenfee.getPriceGreenfees()[i]));
            LOG.info("choiceGreenfees = " + Arrays.deepToString(tarifGreenfee.getGreenfeesChoice()));
            amount = Double.parseDouble(tarifGreenfee.getPriceGreenfees()[i][2]);
                LOG.info("greenfee amount format double = " + amount);
            int quantity = tarifGreenfee.getGreenfeesChoice()[i];
                LOG.info("quantity = " + quantity);
       //     if(quantity > 0){
       //         LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
      //          selectedItems = selectedItems + tarifMember.getMembersBase()[i][0] + ",";
                LOG.info("total before = " + total);
            total = total + (amount * quantity);
                LOG.info("total  after = " + total);
        }       
        LOG.info("total for equipments and greenfees = " + total);
    return myRound(total,2);  // arrondi à deux décimales
 } catch (Exception e) {
      String msg = " -- Error in calcPrixGreenfee " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99999999999999999.9;
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