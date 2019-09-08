
package charts;

import entite.Handicap;
import entite.Player;
import static interfaces.Log.LOG;
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
public class HandicapDetail {
    private static List<Handicap> liste = null;

public List<Handicap> getStatHcp(final Connection conn, final Player player) throws SQLException
{
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
      LOG.info(" ... starting getStatHcp with player = " + player); 
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
" SELECT   PlayerFirstName, PlayerLastName, idhandicap, HandicapPlayer" +
" FROM player, handicap" +
 " WHERE   player.idplayer=?" +
"	AND handicap.player_idplayer = player.idplayer"
     ;
        LOG.info("player = " + player.getIdplayer());
      ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
       LOG.info("ResultSet getStatHcp has " + rs.getRow() + " lines.");
 //   chart = new String [rs.getRow()][6]; // taille array en fonction des parties jouées sur le parcours
    rs.beforeFirst(); //on replace le curseur avant la première ligne
    liste = new ArrayList<>();
    Handicap hcp = new Handicap();
while(rs.next())
{
            hcp = new Handicap();
            hcp.setHandicapStart(rs.getDate("idhandicap") ); 
            hcp.setHandicapPlayer(rs.getBigDecimal("HandicapPlayer") ); 
            liste.add(hcp);			//store all data into a List
} //end while
      LOG.info("liste after while = " + liste.toString() );
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in HandicapDetail() = " + e.toString() + ", SQLState = " + e.getSQLState().toString()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    String msg="NullPointerException in getStatHcp() " + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg );
     return null;
}catch (Exception ex){
    String msg = "Other Exception in getStatHcp! " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
     DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method
} //end class