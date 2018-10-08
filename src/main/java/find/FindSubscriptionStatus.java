
package find;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import utils.LCUtil;

/**
 *
 * @author Collet
 */
public class FindSubscriptionStatus {
        private static List<Subscription> subscr;
        
  public String subscriptionStatus (Subscription subscription, Player player, Connection conn)
     {
     try{
         LOG.info("entering subcriptionStatus");
     //    LOG.info("entering subcriptionStatus with conn :" + conn);
     //    conn = utils.DBConnection.getConnection2(); 
   //  conn = DBConnection.getPooledConnection();
  //       LOG.info("entering subcriptionStatus with new conn :" + conn);
         find.FindSubscription fs = new find.FindSubscription();
         subscr = fs.subscriptionDetail(player, conn);
            LOG.info("subscription detail found = " + subscr.toArray().toString());
        if(subscr == null)  // player non trouvé ??
            {  String msg = "££ pas de subscription record for player = " + player.getIdplayer();
              //  CreateSubscription cs = new CreateSubscription();
              //  cs.createSubscription(player, conn);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "subscription.xhtml?faces-redirect=true";
            }
    //    LOG.info("after call findssubcription");
        LOG.info("subscription playerid " + subscr.get(0).getIdplayer());
     subscription.setStartDate(subscr.get(0).getStartDate() );
     subscription.setEndDate(subscr.get(0).getEndDate() );
     subscription.setTrialCount(subscr.get(0).getTrialCount() );
        LOG.info("subscription endDate " + subscription.getEndDate());
        LOG.info("subscription Trial Count " + subscription.getTrialCount());
       
     if(subscription.getTrialCount() > 5)
          {LOG.info("subscription Trial > 5 - Subscription Month of Year !!!");
            String msg = "Trial exceeded "
                  + " player = " + player.getIdplayer()
                  + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>"
                  ;
             LOG.info(msg);
             LCUtil.showMessageInfo(msg);
             LOG.info("returned to subscription.xhtml");
             
            return "subscription.xhtml?faces-redirect=true";}
      
       LOG.info("LocalDate now() = " + LocalDate.now());
      if(LocalDate.now().isAfter(subscription.getEndDate()))
             {LOG.info("now is after endLocal - subscription not valid !!!");
             LOG.info("return subscription.xhtml");
             
            return "subscription.xhtml?faces-redirect=true";
        }else{
            LOG.info("now is BEFORE endLocal - subscription IS valid !!!");
            LOG.info("returned to welcome.xhtml");
            
            return "welcome.xhtml?faces-redirect=true";}  // le player est accepté
      
      }catch (Exception e){
            String msg = "££ Exception subscriptionStatus = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {
        }         
    } //end method
} // end Class