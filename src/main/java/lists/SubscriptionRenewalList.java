package lists;

import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDate;

/**
 *
 * @author collet
 */
public class SubscriptionRenewalList implements interfaces.Log
{
    private static List<Player> liste = null;
    
public List<Player> getListSubscriptions(final Connection conn) throws Exception
{
if(liste == null)
{    
    LOG.debug("starting getListSubscriptions = " );
        PreparedStatement ps = null;
        ResultSet rs = null;
        liste = new ArrayList<>();
        final String sql =
        "SELECT subscription_player_id, SubscriptionStartDate, SubscriptionEndDate, SubscriptionTrialCount," +
        "     player.idplayer, player.PlayerFirstName, player.PlayerLastName, player.PlayerGender, player.PlayerEmail, player.PlayerLanguage" +
        " FROM subscription" +
        " JOIN player" +
        "   on player.idplayer = subscription.subscription_player_id" +
        "   and PlayerActivation = '1' " +
        " WHERE " +
        "          YEAR(SubscriptionEndDate) = YEAR(CURRENT_DATE())" +
        "     AND MONTH(SubscriptionEndDate) = MONTH(CURRENT_DATE()) + 1"  // subscriptions à échéance le mois suivant
        ;
try
{
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next())
            {
                liste.add(mapPlayer(rs));
            }
    LOG.debug("closing SubscriptionRenevalList with players = " + Arrays.deepToString(liste.toArray()) );
  liste.forEach(item -> LOG.info("liste " + item));  // java 8 lambda
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
    
    private static Player mapPlayer(ResultSet rs) throws SQLException
{
        Player p = new Player();
        p.setIdplayer(rs.getInt("idplayer"));
            //LOG.info(" -- map : playerId = " + p.getIdplayer() );
        p.setPlayerFirstName(rs.getString("playerfirstname"));
        p.setPlayerLastName(rs.getString("playerlastname"));
 //       p.setPlayerCity(rs.getString("playercity"));
 //       p.setPlayerCountry(rs.getString("playerCountry"));
 //       p.setPlayerBirthDate(rs.getDate("playerbirthdate"));
        p.setPlayerGender(rs.getString("playergender"));
 //       p.setPlayerHomeClub(rs.getInt("playerhomeclub"));
        p.setPlayerLanguage(rs.getString("playerLanguage"));
        p.setPlayerEmail(rs.getString("playerEmail"));
        
        java.util.Date d = rs.getTimestamp("SubscriptionEndDate");
     //   LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        p.setEndDate(DatetoLocalDate(d));
  //      p.setEndDate(rs.getString("PlayerPhotoLocation"));
 //       p.setPlayerModificationDate(rs.getTimestamp("playerModificationDate"));
            //LOG.info("map = success !!!");
return p;
} //end method

    public static List<Player> getListe() {
        return liste;
    }

    public static void setListe(List<Player> liste) {
        SubscriptionRenewalList.liste = liste;
    }

} //end Class