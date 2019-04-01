
package find;

import create.CreateSubscription;
import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class FindSubscriptionStatus {
        private static List<Subscription> subscr;
        
  public Boolean subscriptionStatus (Subscription subscription, Player player, Connection conn) throws Exception
     {
     try{
         LOG.info("entering subcriptionStatus");
         subscr = new find.FindSubscription().subscriptionDetail(player, conn);
         if(subscr == null){  // il n'existe pas de record Subscription pour ce player
          //   String msg = "No subscription known : we start creating a subscription record for player = " + player.getIdplayer();
            String msg = LCUtil.prepareMessageBean("subscription.notfound");
             LOG.info(msg);
                LCUtil.showMessageInfo(msg);
             if(new CreateSubscription().create(player, conn)){ // resultat = ok
                msg = "Initial Subscription created ";
                LOG.info(msg);
             //   LCUtil.showMessageInfo(msg);
                return true;
             }else{
                  msg = "Subscription NOT created";
                  LOG.error(msg);
                  LCUtil.showMessageFatal(msg);
                  return false;
             } 
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
       
     if(subscription.getTrialCount() > 5 && LocalDate.now().isAfter(subscription.getEndDate())){  // new 22-02-2019 
          String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
        //    String msg = "subscription Trial > 5 - Use Subscription Month of Year instead !!! "
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
      if(LocalDate.now().isAfter(subscription.getEndDate())){
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
  
      public static void main(String[] args)throws SQLException, Exception{ // testing purposes

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Subscription subscription = new Subscription();
    // compléter ic certains éléments
    Boolean p1 = new FindSubscriptionStatus().subscriptionStatus(subscription, player, conn);
        LOG.info("player found = " + p1.toString());
//for (int x: par )
//        LOG.info(x + ",");
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
  
} // end Class