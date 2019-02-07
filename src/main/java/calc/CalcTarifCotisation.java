package calc;

import entite.TarifMember;
import java.text.ParseException;
import java.util.Arrays;
import utils.LCUtil;
// rester à faire le cas des invités / guests
// a faire : greenfee 0 pour membre du club ! via une table member : greenfee 0 pour les membres du club
public class CalcTarifCotisation implements interfaces.GolfInterface, interfaces.Log{

public double findTarif (TarifMember tarifMember){
     LOG.info(" -- Start of CalcTarifCotisation");
     LOG.info(" -- Start of CalcTarifCotisation with tarifMember= " + tarifMember.toString());
 try {
 //       à faire : proportion pour abonnement en cours d'année (année incomplète)
 double total = 0;
 double amount = 0;
 int quantity = 0;
 //String selectedItems = "";
 	for(int i = 0 ; i < tarifMember.getMembersBase().length ; i++) {
            LOG.info("i = " + i);
            LOG.info("base for calculation = " + Arrays.toString(tarifMember.getMembersBase()[i]));
            LOG.info("choice for calculation = " + tarifMember.getMembersChoice()[i]);
            amount = Double.parseDouble(tarifMember.getMembersBase()[i][1]);
                LOG.info("amount = " + amount);
            quantity = tarifMember.getMembersChoice()[i];
                LOG.info("quantity = " + quantity);
       //     if(quantity > 0){
       //         LOG.info("item sélectionné = " + tarifMember.getMembersBase()[i][0]);
      //          selectedItems = selectedItems + tarifMember.getMembersBase()[i][0] + ",";
        //    }
            total = total + amount * quantity;
                LOG.info("total = " + total);
        }
          //   return Double.valueOf(price);
          return total;
          
 } catch (Exception e) {
      String msg = " -- Error in calcTarifCotisation " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99.9;
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