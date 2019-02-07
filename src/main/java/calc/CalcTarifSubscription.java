
package calc;

import entite.Subscription;
import java.text.ParseException;
import utils.LCUtil;
// rester à faire le cas des invités / guests
// a faire : greenfee 0 pour membre du club ! via une table member : greenfee 0 pour les membres du club
public class CalcTarifSubscription implements interfaces.GolfInterface, interfaces.Log{

  public double findTarif (Subscription subscription){
     LOG.info(" -- Start of CalcTarifSubscription");
     LOG.info(" -- Start of CalcTarifSubscription with = " + subscription.toString());
      String price = "";
 try {
 if(subscription.getSubCode().equals(Subscription.etypeSubscription.MONTHLY.toString())){
             price = utils.LCUtil.findProperties("subscription", "month");
                 LOG.info("Month subscription price = " + price);
             return Double.valueOf(price); // mod 18-11-2018
   }
 if(subscription.getSubCode().equals(Subscription.etypeSubscription.YEARLY.toString())){
             price =  utils.LCUtil.findProperties("subscription", "year");
             return Double.valueOf(price);
         }
 if(subscription.getSubCode().equals(Subscription.etypeSubscription.TRIAL.toString())){
         //    price =  utils.LCUtil.findProperties("subscription", "year");
             return 0.0;
         }
  
 } catch (Exception e) {
      String msg = " -- Error in findTarifSubscription " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99;
 }
 finally {
   //return 0.0;
// LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
      return 99;
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