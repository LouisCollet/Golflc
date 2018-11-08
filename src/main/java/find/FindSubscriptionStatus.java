
package find;

import create.CreateSubscription;
import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import utils.LCUtil;

/**
 *
 * @author Collet
 */
public class FindSubscriptionStatus {
        private static List<Subscription> subscr;
        
  public Boolean subscriptionStatus (Subscription subscription, Player player, Connection conn) throws Exception
     {
     try{
         LOG.info("entering subcriptionStatus");
     //    LOG.info("entering subcriptionStatus with conn :" + conn);
     //    conn = utils.DBConnection.getConnection2(); 
   //  conn = DBConnection.getPooledConnection();
  //       LOG.info("entering subcriptionStatus with new conn :" + conn);
         find.FindSubscription fs = new find.FindSubscription();
         subscr = fs.subscriptionDetail(player, conn);
           
        if(subscr == null)  // player non trouvé ??
            {  String msg = "we create a subscription record for player = " + player.getIdplayer();
                CreateSubscription cs = new CreateSubscription();
                cs.createSubscription(player, conn);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            //    return "subscription.xhtml?faces-redirect=true";
            }
         LOG.info("subscription detail found = " + Arrays.deepToString(subscr.toArray()));
        
    //    LOG.info("after call findssubcription");
        LOG.info("subscription playerid " + subscr.get(0).getIdplayer());
     subscription.setStartDate(subscr.get(0).getStartDate() );
     subscription.setEndDate(subscr.get(0).getEndDate() );
     subscription.setTrialCount(subscr.get(0).getTrialCount() );
        LOG.info("subscription endDate " + subscription.getEndDate());
        LOG.info("subscription Trial Count " + subscription.getTrialCount());
       
     if(subscription.getTrialCount() > 5)
          {LOG.info("subscription Trial > 5 - Use Subscription Month of Year instead !!!");
            String msg = "Trial exceeded "
                  + " player = " + player.getIdplayer()
                  + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>"
                  ;
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             LOG.info("returned to subscription.xhtml");
             return false;
          }
         //   return "subscription.xhtml?faces-redirect=true";}
      
       LOG.info("LocalDate now() = " + LocalDate.now());
      if(LocalDate.now().isAfter(subscription.getEndDate()))
             {
                 String msg = "now is after endLocal - subscription NOT valid !!!";
        //     throw new Exception (msg);
     //        LOG.info("return subscription.xhtml");
         //    }
         return false;
        //    return "subscription.xhtml?faces-redirect=true";
        }else{
            LOG.info("now is BEFORE endLocal - subscription IS valid !!!");
            LOG.info("returned to welcome.xhtml");
            return true;
        //    return "welcome.xhtml?faces-redirect=true";
      }  // le player est en ordre de souscription
      
      }catch (Exception e){
            String msg = "££ Exception subscriptionStatus = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false; // indicates that the same view should be redisplayed
        } finally {
        }         
    } //end method
} // end Class