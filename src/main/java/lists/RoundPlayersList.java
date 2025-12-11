package lists;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class RoundPlayersList implements interfaces.Log{  
    private static List<Player> liste = null;
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public List<Player> list(final Round round ,final Connection conn) throws SQLException { 
 final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
if(liste == null){
    LOG.debug("... starting " + methodName);
    LOG.debug(" for round =  " + round);
    if(round.getIdround() == null){
         LOG.debug(" we checked round = null and we return null ");
         return null;
    }
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    String query = """
       SELECT *
       FROM player_has_round, round, player
       WHERE round.idround = ?
         AND InscriptionIdRound = round.idround
         AND player.idplayer = InscriptionIdPlayer
    """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, round.getIdround());
    utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    while(rs.next()){
        Player p = entite.Player.map(rs);
	liste.add(p);
    }
    if(liste.isEmpty()){
   //  if(liste == null){  
         
         String msg = "Pas encore d'inscription à ce round " + methodName;
         LOG.error(msg);
         LCUtil.showMessageInfo(msg);
         return liste;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + " / "  + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in RoundPlayersList()<br/> " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //     LOG.debug("escaped to listParticipants repetition with lazy loading");
    return liste;  //plusieurs fois ??
    }
} //end method

    public static List<Player> getListe() {
        return liste;
    }

    public static void setListe(List<Player> liste) {
        RoundPlayersList.liste = liste;
    }
    
  void main() throws SQLException, Exception {
    Connection conn = new DBConnection().getConnection();
   Round round = new Round();
   round.setIdround(628);
   List<Player> p1 = new RoundPlayersList().list(round, conn);
        LOG.debug("Inscription list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end class