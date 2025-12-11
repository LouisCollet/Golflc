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

public class FindCurrentSubscription implements interfaces.Log{
   private static List<Subscription> liste = null;
       private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   
public List<Subscription> payments (final Player player , String type, Connection conn) throws SQLException {// throws SQLException, Exception{   
///if(liste == null)
   LOG.debug("starting payments.for player  = "  + player.getIdplayer());
    PreparedStatement ps = null;
    ResultSet rs = null;
    Subscription subscription;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{ 
     /* si plusieurs subscriptions ouvertes à current date, on prend la plus récente
    mod 18-04-2024*/
     String query = "";  // mod 17-08-2025  AND NOW() BETWEEN SubscriptionStartDate AND SubscriptionEndDate
     // la comparaison ne tient pas compte de l'heure ! donc on enlève 1 jour !
    if(type.equals("now")){
        query = """
            SELECT *
            FROM payments_subscription
            WHERE SubscriptionIdPlayer=?
              AND NOW() BETWEEN DATE_SUB(SubscriptionStartDate, INTERVAL 1 DAY) AND SubscriptionEndDate
            ORDER BY SubscriptionStartDate ASC
            LIMIT 1
    """  ;
     }else{ // latest
       query = """
            SELECT *
            FROM payments_subscription
            WHERE SubscriptionIdPlayer=?
            ORDER BY SubscriptionStartDate DESC
            LIMIT 1
    """  ;
    }
  /*       AND NOW() BETWEEN SubscriptionStartDate AND SubscriptionEndDate


/*
    String query ="""
     SELECT *
     FROM payments_subscription
     WHERE SubscriptionIdPlayer=?
     ORDER BY SubscriptionEndDate DESC, SubscriptionStartDate DESC
     LIMIT 1
   """ ;
*/
     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer()); 
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
           subscription = entite.Subscription.map(rs);
	liste.add(subscription);
	}
   if(liste.isEmpty()){
         String msg = "££ Empty Result Table in " + methodName + " for player = " + player.getIdplayer();
         LOG.error(msg);
  //       LCUtil.showMessageFatal(msg);
         liste = null;
   //      return null;
     }else{
         LOG.debug("ResultSet FindSubscription has " + liste.size() + " lines.");
     }
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
}finally{
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
        FindCurrentSubscription.liste = liste;
    }
    
 void main() throws SQLException, Exception{ // testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    List<Subscription> p1 = new FindCurrentSubscription().payments(player,"now", conn);
        LOG.debug("Subscription found = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
}  // end class