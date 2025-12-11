package find;

import entite.Player;
import entite.Subscription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class FindSubscriptionStatus {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   // private static List<Subscription> subscriptionList;
        
  public Boolean find (Subscription subscription,
          Player player,
          Connection conn) throws Exception{
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
   try{
         LOG.debug("entering " + methodName);
         LOG.debug("entering with subscription idplayer = " + subscription.getIdplayer());
    List<Subscription> subscriptionList = new find.FindCurrentSubscription().payments(player, "now", conn); // à la date du jour 
    if(subscriptionList == null){  // il n'existe pas de record Subscription pour ce player
 //         LOG.debug("no payments subscription for this player !");
//   String msg = "No subscription known : we start creating a subscription record for player = " + player.getIdplayer();
         String msg = LCUtil.prepareMessageBean("subscription.notfound");
             LOG.debug(msg);
             LCUtil.showMessageInfo(msg);
             return false;
      } // end no subscription
    
         LOG.debug("subscription detail found = " + Arrays.deepToString(subscriptionList.toArray()));
        
    //    LOG.debug("after call findssubcription");
 //       LOG.debug("subscription playerid " + subscr.get(0).getIdplayer());
 
   //  var v = subscriptionList.get(0);
     //   LOG.debug("subscription current = " + v);
        subscription = subscriptionList.get(0);
   //  subscription.setStartDate(v.getStartDate() );
   //  subscription.setEndDate(v.getEndDate() );
   //  subscription.setTrialCount(v.getTrialCount() );
 //       LOG.debug("subscription endDate " + subscription.getEndDate());
 //       LOG.debug("subscription Trial Count " + subscription.getTrialCount());
       LOG.debug("current subscription = " + subscription);
     if(subscription.getTrialCount() > 5 && LocalDateTime.now().isAfter(subscription.getEndDate())){  // new 22-02-2019 
          String msg = LCUtil.prepareMessageBean("subscription.create.toomuchtrials")
        //    String msg = "subscription Trial > 5 - Use Subscription Month of Year instead !!! "
                  + " player = " + player.getIdplayer()
                  + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>"
                  ;
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
             LOG.debug("returned to subscription.xhtml");
             return false;
          }
        if(LocalDateTime.now().isBefore(subscription.getStartDate())){
            String msg = "now is before subscription Start - subscription NOT valid !!! " + subscription.getStartDate().format(DateTimeFormatter.ISO_DATE);
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
        if(LocalDateTime.now().isAfter(subscription.getEndDate())){
                 String msg = "now is after subscription endDate - subscription NOT valid !!! " + subscription.getEndDate().format(DateTimeFormatter.ISO_DATE);
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                 return false;
        }
        return true;
     //   }else{
      //      LOG.debug("now is BEFORE endLocal - subscription IS valid !!!");
      //      LOG.debug("returned to welcome.xhtml");
       //     return true;
        //    return "welcome.xhtml?faces-redirect=true";
     // }  // le player est en ordre de souscription
      }catch (Exception e){
            String msg = "££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false; // indicates that the same view should be redisplayed
        } finally {
        }         
    } //end method
  
      void main()throws SQLException, Exception{ // testing purposes

    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(456989);
    Subscription subscription = new Subscription();
    // compléter ic certains éléments
    Boolean p1 = new FindSubscriptionStatus().find(subscription, player, conn);
        LOG.debug("subcription found ? = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class