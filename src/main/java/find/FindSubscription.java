package find;


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

/**
 *
 * @author collet
 */
public class FindSubscription implements interfaces.Log
{
   private static List<Subscription> liste = null;
   final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public List<Subscription> subscriptionDetail (final Player player , Connection conn) throws SQLException, Exception{   
///if(liste == null)
   LOG.debug("starting subscriptionDetail.for player  = "  + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
    Subscription subscription;
 //   conn = DBConnection.getPooledConnection();
try{
     String s= utils.DBMeta.listMetaColumnsLoad(conn, "subscription");
    String query =
     " SELECT "
        + s +
            //subscription_player_id, SubscriptionStartDate, SubscriptionEndDate, SubscriptionTrialCount" +
     " FROM subscription" +
     " WHERE subscription.subscriptionIdPlayer=?"
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer()); 
         utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindSubscription has " + rs.getRow() + " lines.");
     if(rs.getRow() == 0){
         String msg = "££ Empty Result Table in " + ClassName + " for player = " + player.getIdplayer();
        LOG.error(msg);
    //    LCUtil.showMessageFatal(msg);
        return null;        
       //   throw new LCCustomException(msg);
      }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
                 //  modifié le 21-01-2019 mais non testé !!
                    subscription = entite.Subscription.mapSubscription(rs);
			liste.add(subscription);
		}
//LOG.debug(" -- query 5= listcc = " + listcc.toString() );
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in FindSubscription : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindSubsscription() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
///}
///else{
///       LOG.debug("escaped to FindSubscription repetition with lazy loading");
///    retu//rn liste;  //plusieurs fois ??
///    }
} //end method

    public static List<Subscription> getListe() {
        return liste;
    }

    public static void setListe(List<Subscription> liste) {
        FindSubscription.liste = liste;
    }
}  // end class