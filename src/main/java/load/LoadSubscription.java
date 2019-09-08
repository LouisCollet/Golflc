package load;

import entite.Subscription;
import static interfaces.Log.LOG;
import java.time.LocalDate;
public class LoadSubscription{

public Subscription load(Subscription subscription) throws Exception{
try{
        LOG.info("entering LoadSubscription");
            if (subscription.getStartDate() == null){
                subscription.setStartDate(LocalDate.now());
                LOG.info("startDate filled with now() = " + subscription.getStartDate());}
            
            if (subscription.getEndDate() == null ){
                subscription.setEndDate(LocalDate.now());
                LOG.info("sendDate filled with now() = " + subscription.getStartDate());}
            
            if(LocalDate.now().isAfter(subscription.getEndDate())){   // subscription après période précédente
                subscription.setEndDate(LocalDate.now());
                LOG.info("LocalDate is after endDate == > replaced by now()");}
   //         LOG.info("line 00");
        //    int count = 0;
            LocalDate endDate = null;
   //         LOG.info("line 01");
            switch(subscription.getSubCode()){
                // switch ne fonctionne pas avec enum !! compile type
                    case "TRIAL":  // trial one day
                            LOG.info("getSubCode(): TRIAL");
                    //    d = subscription.getEndDate().plusDays(1);  // ok si enddate dans le futur !!
                        endDate = LocalDate.now().plusDays(1); // donc valable deux jours 
                    //    count = subscription.getTrialCount() + 1;
        //                Short s = subscription.getTrialCount();
        //                subscription.setTrialCount(s++);
                        break;
                    case "MONTHLY":
                            LOG.info("getSubCode(): MONTHLY");
                        endDate = subscription.getEndDate().plusMonths(1);
           //             subscription.setStartDate(LocalDate.now());
                        break;
                    case "YEARLY":
                            LOG.info("getSubCode(): YEARLY");
                        endDate = subscription.getEndDate().plusYears(1); 
                        break;
                    default:
                            LOG.info(": getSubCode() UNKNOWN = " + subscription.getSubCode() );
                } //end switch
      subscription.setEndDate(endDate);
     
 // fills communication    
       if(subscription.getSubCode().equals(Subscription.etypeSubscription.MONTHLY.toString())){
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.month"));
       }
      if(subscription.getSubCode().equals(Subscription.etypeSubscription.YEARLY.toString())){
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.year"));
         }
      if(subscription.getSubCode().equals(Subscription.etypeSubscription.TRIAL.toString())){
             subscription.setCommunication(utils.LCUtil.prepareMessageBean("subscription.trial"));
         }
// fils price
      double price = new calc.CalcTarifSubscription().findTarif(subscription);
      subscription.setPrice(price);
  return subscription;
}catch (Exception ex){
    LOG.error("Exception in LoadSubscription ! " + ex);
  //  LCUtil.showMessageFatal("Exception in LoadClub = " + ex.toString() );
     return null;
}
finally
{
       // DBConnection.closeQuietly(conn, null, rs, ps);
  //  DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}

} //end method

public static void main(String[] args) throws Exception // testing purposes
{
  //  DBConnection dbc = new DBConnection();
  //  Connection conn = dbc.getConnection();
  //  LoadClub lc = new LoadClub();
  //  Club club = lc.LoadClub(conn, 104);
  //     LOG.info(" club = " + club.toString());
//for (int x: par )
//        LOG.info(x + ",");
//DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
