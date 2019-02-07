package lists;

import entite.ECourseList;
import entite.Player;
import entite.Subscription;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class SubscriptionRenewalList implements interfaces.Log{
    private static List<ECourseList> liste = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    
public List<ECourseList> getListSubscriptions(final Connection conn) throws Exception{
if(liste == null){
    try{
           LOG.debug("starting getListSubscriptions = " );
       liste = new ArrayList<>();
       String su = utils.DBMeta.listMetaColumnsLoad(conn, "subscription");
       String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
       final String query =
        "SELECT "
             + su + "," + pl +
     //           "subscription_player_id, SubscriptionStartDate, SubscriptionEndDate, SubscriptionTrialCount," +
      //  "     player.idplayer, player.PlayerFirstName, player.PlayerLastName, player.PlayerGender, player.PlayerEmail, player.PlayerLanguage" +
        " FROM subscription" +
        " JOIN player" +
        "   on player.idplayer = subscription.subscription_player_id" +
        "   and PlayerActivation = '1' " +
        " WHERE " +
        "          YEAR(SubscriptionEndDate) = YEAR(CURRENT_DATE())" +
        "     AND MONTH(SubscriptionEndDate) = MONTH(CURRENT_DATE()) + 1"  // subscriptions à échéance le mois suivant
        ;
       ps = conn.prepareStatement(query);
       rs = ps.executeQuery();
       rs.last(); //on récupère le numéro de la ligne
            LOG.info("SubscriptionRenewalList  has {} players ", rs.getRow() );

    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>(); // new 02/06/2013
    while(rs.next())
     {
          ECourseList ecl = new ECourseList(); // est réi, donc total = 0

          Player p = new Player();
          p = entite.Player.mapPlayer(rs);
          ecl.setPlayer(p);

          Subscription s = new Subscription();
          s = entite.Subscription.mapSubscription(rs);
          ecl.setSubscription(s);
          
          liste.add(ecl);
     } // end while

  //  LOG.debug("closing SubscriptionRenevalList with players = " + Arrays.deepToString(liste.toArray()) );
  liste.forEach(item -> LOG.info("players candidates to renewal =  " + item));  // java 8 lambda
  
  //// partie 2
   for(ECourseList item : liste)
     {
        	LOG.info("Player we send a Subscription Renewal mail = " + item.Eplayer.getPlayerLastName());
             mail.SubscriptionMail sm = new mail.SubscriptionMail();
             sm.sendMail(item.getPlayer(),item.getSubscription());
      } //end for
return liste;

} catch(SQLException sqle){
    String msg = "£££ SQL exception in SubscriptionRenewalList = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in SubscriptionRenewalList = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
           DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //  LOG.debug("escaped to listallplayers repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
}
    //end if
} //end method

    public static List<ECourseList> getListe() {
        return liste;
    }

    public static void setListe(List<ECourseList> liste) {
        SubscriptionRenewalList.liste = liste;
    }
} //end Class