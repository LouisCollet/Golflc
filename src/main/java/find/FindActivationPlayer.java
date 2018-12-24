package find;

import entite.Player;
import exceptions.TimeLimitException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import utils.DBConnection;
import utils.LCUtil;

public class FindActivationPlayer implements interfaces.Log, interfaces.GolfInterface
{
//private  Player player = null;
public Player findActivationPlayer(Connection conn, String in_uuid) throws SQLException, TimeLimitException, Throwable
{
    PreparedStatement ps = null;
    ResultSet rs = null;
    Player player = new Player();
try{
     LOG.info("starting findActivationPlayer... " );
     LOG.info("starting findActivationPlayer with in_uuid = " + in_uuid);
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
  //          LOG.info("Concerne Player = " + rs.getInt("player_idplayer"));
    //    if (rs.getRow() != 1)    
         if(rs.getRow() == 0)
            {String msg = "Empty Result for Activation player !! ";
             LOG.error(msg);
      //       LCUtil.showMessageFatal(msg);
             player.setIdplayer(null);
             return player;
            }   
    
    
        if (rs.getRow() != 1) // soit 0, soit 2
        {
             String msg = "No Activation found no!= 1 ";
                 LOG.info(msg);
             LCUtil.showMessageFatal(msg);
             player.setIdplayer(null);
             return player;
        }  //else{
            // 09/08/2018
 //mise en place de lanotion de expirationDate : on laisse 10 minutes pour répondre au mail
            // ce temps n'est pas suffisnt pour un hacker pour casser le UUID
        java.util.Date d = rs.getTimestamp("ActivationCreationDate");
                LOG.info("ActivationCreationDate = " + sdf_timeHHmm.format(d));
        java.util.Date now = new java.util.Date();
                LOG.info("current dateTime = " + sdf_timeHHmm.format(new java.util.Date())); 
        long differenceInMinutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - d.getTime()); // différence getTime en milliseconds
                LOG.info("Just in time for the 10 minutes " + differenceInMinutes); 
          //      ZonedDateTime now2 = ZonedDateTime.now();
          //      Instant instant = Instant.now() ; 
          //      LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         //       Duration duration = Duration.between(date, instant); //.toMinutes();
          //      LOG.info("duration modern = " + duration.toMinutes());
     //     LocalDateTime now = LocalDateTime.now();
         if(now.getTime() > d.getTime() + TimeUnit.MILLISECONDS.toMinutes(10) )   {
             LOG.info("autre manière : now est > échéance ");
         }    

 //    Player player = new Player();
        if(differenceInMinutes < 10){   //reliable?
                player.setIdplayer(rs.getInt("player_idplayer"));
                player.setPlayerLanguage(rs.getString("ActivationPlayerLanguage"));
                String msg = "Just in time for the dead line of 10 minutes :" + rs.getInt("player_idplayer")
                        + " remaining = " + (10 - differenceInMinutes);
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return player;
           }else{
                player.setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for the reset of your Password " + rs.getInt("player_idplayer");
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
           //   throw new TimeLimitException(msg).initCause(new IOException("IO cause")); // "this is the cause");
             //   player.setPlayerPhotoLocation(msg);
                return player; // = null;
           }  
/*     //   }
}catch (TimeLimitException e){
        String msg = "TimeLimitException = " + e.toString(); // + ", SQLState = " + e.getSQLState()
     //      + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        Player player = new Player();
        return player = null;
        */
}catch (SQLException e){
    
    //          if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 )
    
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

public static void main(String[] args) throws SQLException, Exception, Throwable // testing purposes
{
    LOG.info("Input main = ");
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    FindActivationPlayer  fap = new FindActivationPlayer();
  //  Player player = new Player();
    Player player = fap.findActivationPlayer(conn, "b03cfec9-974d-4e54-bc04-2810a5bf13c0");
        LOG.info("after call = " + player.toString());

DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
} // end Class

