package lists;

import entite.composite.ESubscription;
import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

@Named("SASubscription")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml

public class SystemAdminSubscriptionList implements Serializable{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static List<ESubscription> liste = null;
public List<ESubscription> list(final Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     String query = """
            SELECT *
            FROM payments_subscription, player
            WHERE player.idplayer = payments_subscription.SubscriptionIdPLayer
            ORDER BY SubscriptionStartDate
            """;
     ps = conn.prepareStatement(query);
   //  ps.setInt(1, localAdmin.getIdplayer());, idplayer;
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
	while(rs.next()){
           ESubscription ec = new ESubscription();
           ec.setPlayer(entite.Player.map(rs));
           ec.setSubscription(entite.Subscription.map(rs)); // mod 26/09/2022
            liste.add(ec);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
      if(liste.isEmpty()){
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
        String error = "SQL Exception in SystemAdminSubscriptionList = " + e.toString() + ", SQLState = " + e.getSQLState() + ", ErrorCode = " + e.getErrorCode();
	LOG.error(error);
        LCUtil.showMessageFatal(error);
        return null;
}catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<ESubscription> getListe() {
        return liste;
    }

    public static void setListe(List<ESubscription> liste) {
        SystemAdminSubscriptionList.liste = liste;
    }

 void main() throws SQLException, Exception{
  //    OptionSet args = parseOptions(argv);
     Connection conn = new DBConnection().getConnection();
  try{
    //  Player player = new Player();
    //  player.setIdplayer(324715);
    var lp = new SystemAdminSubscriptionList().list(conn);
        LOG.debug("from main, result = " + lp.toString());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
 }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class