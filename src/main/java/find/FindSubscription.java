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
import static utils.LCUtil.DatetoLocalDate;

/**
 *
 * @author collet
 */
public class FindSubscription implements interfaces.Log
{
   private static List<Subscription> liste = null;
   final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public List<Subscription> subscriptionDetail (final Player player , Connection conn) throws SQLException, Exception
{   
///if(liste == null)
   LOG.debug("starting subscriptionDetail.for player  = "  + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
 //   conn = DBConnection.getPooledConnection();
try
{   
    String query =
     " SELECT subscription_player_id, SubscriptionStartDate, SubscriptionEndDate, SubscriptionTrialCount" +
     " FROM subscription" +
     " WHERE subscription.subscription_player_id = ?"
    ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer()); 
         utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet FindSubscription has " + rs.getRow() + " lines.");
     if(rs.getRow() == 0)
      {   String msg = "££ Empty Result Table in " + ClassName + " for player = " + player.getIdplayer();
      //String msg = "NullPointerException in " + npe;
        LOG.error(msg);
    //    LCUtil.showMessageFatal(msg);
  ////      create.CreateSubscription cs = new create.CreateSubscription();
  ////      cs.createSubscription(player,conn);
        return null;        
       //   throw new LCCustomException(msg);
      }     
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
      //LOG.debug(" -- query 4= " );
		while(rs.next())
                {
			Subscription cc = new Subscription();
                        cc.setIdplayer(rs.getInt("subscription_player_id") );
                  //      cc.setStartDate(rs.getDate("SubscriptionStartDate") );
                  //     rs.getTimestamp
                    //    cc.setEndDate(rs.getTimestamp("SubscriptionEndDate"));
                        java.util.Date d = rs.getTimestamp("SubscriptionStartDate");
                   //     LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        
                        cc.setStartDate(DatetoLocalDate(d));
                    
                        d = rs.getTimestamp("SubscriptionEndDate");
                     //   date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        cc.setEndDate(DatetoLocalDate(d));
                     //   cc.setEndDate(rs.getDate("SubscriptionEndDate"));
                        cc.setTrialCount(rs.getInt("SubscriptionTrialCount"));
			liste.add(cc);
		}
//LOG.debug(" -- query 5= listcc = " + listcc.toString() );
    return liste;

//}catch (LCCustomException e){
  //  String msg = " SQL Exception in getScoreCardList1() " + e;
  //  LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
//    return null;    
}catch (NullPointerException npe){
    String msg = "NullPointerException in " + ClassName + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
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