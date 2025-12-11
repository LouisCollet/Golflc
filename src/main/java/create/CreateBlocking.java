package create;

import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class CreateBlocking implements Serializable,interfaces.Log{

public boolean create(final Player player, final Connection conn) throws SQLException {
        LOG.debug("starting CreateBlocking.create for player = " + player);
    PreparedStatement ps = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "blocking"); // new 26/05/2019
    ps = conn.prepareStatement(query);
    ps.setInt(1,player.getIdplayer());
    ps.setTimestamp(2,Timestamp.from(Instant.now())); // BlockingLastAttempt
    ps.setInt(3, 1); 
    ps.setTimestamp(4,Timestamp.from(Instant.now())); 
    ps.setTimestamp(5,Timestamp.from(Instant.now())); // ModificationDate
    utils.LCUtil.logps(ps);

    int rows = ps.executeUpdate(); // write into database
      if (rows!=0){ 
            String msg = "Tentative 1 - Après 3 erreurs successives, vous serez bloqué pendant 15 minutes ";
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
          return true;
      }else{
          LOG.debug("-- UNsuccessful insert Blocking !!! ");
          // lancer une erreur ??
          return false;
      }
}catch (Exception ex){
    String msg = "Exception in CreateBlocking = " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
    DBConnection.closeQuietly(null, null, null, ps);
}
} // end method 

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
            Player player = new Player();
            player.setIdplayer(324713);
            boolean b = new create.CreateBlocking().create(player, conn);
            LOG.debug("from main, CreateBlocking = " + b);
    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
} //end class