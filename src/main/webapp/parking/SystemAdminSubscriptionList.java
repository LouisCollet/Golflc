package lists;

import entite.Player;
import entite.Subscription;
import entite.composite.ESubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
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
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;
import rowmappers.SubscriptionRowMapper;
import connection_package.DBConnection;
import entite.Club;
import java.util.Collections;
import rowmappers.ClubRowMapper;
import utils.LCUtil;

@Named("SASubscription")
@ViewScoped // nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml

public class SystemAdminSubscriptionList implements Serializable{

    private static List<ESubscription> liste = null;
public List<ESubscription> list(final Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
//if(liste == null){
    LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    if (liste != null) {
        return liste;
    }
    liste = new ArrayList<>();
    
    
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
    RowMapper<Player> playerMapper = new PlayerRowMapper();
    RowMapper<Subscription> subscriptionMapper = new SubscriptionRowMapper();
    RowMapper<Club> clubMapper = new ClubRowMapper();
	while(rs.next()){
           Subscription subscription = subscriptionMapper.map(rs);
           Player player = playerMapper.map(rs);
           Club club = null;
           ESubscription esub = new ESubscription(subscription,player, club); // via constructor
      //     ec.setPlayer(playerMapper.map(rs));
      //     ec.setSubscription(subscriptionMapper.map(rs)); // mod 26/09/2022
            liste.add(esub);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
      if(liste.isEmpty()){
        String msg = "££ Empty Result List in " + methodName;
           LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return Collections.emptyList();
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
//}else{
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
//    return liste;  //plusieurs fois ??
//}
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