package Controllers;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DBConnection;

public class SubscriptionController implements interfaces.GolfInterface {
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 
void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
 //   LOG.debug("line 010");
    Round round = new Round();
    round.setIdround(437);
 
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class