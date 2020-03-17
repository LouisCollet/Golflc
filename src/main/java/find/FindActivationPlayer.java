package find;

import entite.Player;
import exceptions.TimeLimitException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class FindActivationPlayer implements interfaces.Log, interfaces.GolfInterface{

public Player find(Connection conn, String in_uuid) throws SQLException, TimeLimitException, Throwable{
    PreparedStatement ps = null;
    ResultSet rs = null;
    Player player = new Player();
try{
 //    LOG.info("starting findActivationPlayer... " );
     LOG.info("starting findActivationPlayer with in_uuid = " + in_uuid);
//  String ca = utils.DBMeta.listMetaColumnsLoad(conn, "activation");
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
          "SELECT ActivationKey, player_idplayer , ActivationPlayerLanguage, ActivationCreationDate"
        + "   FROM activation"
        + "   WHERE activationkey = ?"
     ;
        ps = conn.prepareStatement(query);
        ps.setString(1, in_uuid);
           utils.LCUtil.logps(ps); 
	rs =  ps.executeQuery();
        rs.last(); // on se positionne sur la dernière ligne
 //       int last = rs.getRow();//on récupère le numéro de la dernière ligne

            LOG.info("ResultSet Activation has " + rs.getRow() + " lines.");
            if(rs.getRow() != 1){
                throw new Exception(" -- Zero or More than 1 player = " + rs.getRow() );  }
           rs.beforeFirst(); //on replace le curseur avant la première ligne
          //LOG.info("just before while ! ");
	while(rs.next()){
              player.setIdplayer(rs.getInt("player_idplayer"));
              // récupérer rs.getTimestamp("ActivationCreationDate"
	}
    LOG.info("idplayer found from activation = " + player.getIdplayer()); // c'est OK
    player = new find.FindPlayer().find(player, conn);
    LOG.info("player found from activation = " + player); // c'est OK
 /*
         if(rs.getRow() == 0){
             String msg = "Empty Result for Activation player !! ";
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
       //      player.setIdplayer(null);
             player = null;
             return player;
            }   
    
        if (rs.getRow() != 1){ // soit 0, soit 2
             String msg = "No Activation found no!= 1 ";
                 LOG.info(msg);
             LCUtil.showMessageFatal(msg);
        //     player.setIdplayer(null);
             player = null;
             return player;
        }  //else{
            // 09/08/2018
 //mise en place de la notion de expirationDate : on laisse 10 minutes pour répondre au mail
            // ce temps n'est pas suffisant pour un hacker pour casser le UUID
            // idiot il n'y a rien à casser !
*/
         Duration difference = Duration.between(rs.getTimestamp("ActivationCreationDate").toLocalDateTime(),LocalDateTime.now());
         long differenceInMinutes = difference.toMinutes();
                LOG.info("difference in minutes = " + differenceInMinutes);
         if(differenceInMinutes < 10){
             //   player.setIdplayer(rs.getInt("player_idplayer"));
             //   player.setPlayerLanguage(rs.getString("ActivationPlayerLanguage"));
                String msg = "Activation - Respect of the dead line of 10 minutes :" + rs.getInt("player_idplayer")
                        + " remaining = " + (10 - differenceInMinutes);
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                LOG.info("player found < 10 minutes= " + player);
                return player;
           }else{
                player.setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for the reset of your Password " + rs.getInt("player_idplayer");
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
           //   throw new TimeLimitException(msg).initCause(new IOException("IO cause")); // "this is the cause");
            //    player.setIdplayer(null);
                player = null;
                return player; // = null;
           }  
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode() + " " + e.getLocalizedMessage();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        throw new SQLException(msg);
    //    return null;
}catch (Exception ex){
    String msg = "Exception in getActivation() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
finally
{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

public static void main(String[] args) throws SQLException, Exception, Throwable {// testing purposes
    Connection conn = new DBConnection().getConnection();
    Player player = new FindActivationPlayer().find(conn, "329c6b5b-939c-40eb-8d69-7084b62524a3");
        LOG.info("after call = " + player.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
    
} // end Class