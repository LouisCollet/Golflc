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

public class HandicapDetail {
    private static List<Handicap> liste = null;

public List<Handicap> getStatHcp(final Connection conn, final Player player) throws SQLException{
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      LOG.debug(" ... starting getStatHcp with player = " + player); 
      // mod 10-12-2024 non testé
String query = """
SELECT PlayerFirstName, PlayerLastName, idhandicap, HandicapPlayer
FROM player, handicap
WHERE player.idplayer=?
AND handicap.player_idplayer = player.idplayer
""";
        LOG.debug("player = " + player.getIdplayer());
      ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     utils.LCUtil.logps(ps);
		//get round data from database
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    Handicap handicap = new Handicap();
    while(rs.next()){
            handicap = new Handicap();
            handicap.setHandicapStart(rs.getDate("idhandicap") ); 
            handicap.setHandicapPlayerEGA(rs.getBigDecimal("HandicapPlayerEGA") ); 
            liste.add(handicap);
    } //end while
      LOG.debug("liste after while = " + liste.toString() );
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in HandicapDetail() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
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