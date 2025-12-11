package find;

import entite.composite.EPlayerPassword;
import entite.Password;
import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindPassword implements interfaces.Log{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

    public Boolean passwordMatch(final EPlayerPassword epp , final Connection conn) throws SQLException{
     final String methodName = CLASSNAME + Thread.currentThread().getStackTrace()[1].getMethodName() + " = ";    
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
      LOG.debug("starting " + methodName);
      LOG.debug(" for EPlayerPassword = " + epp);
 //   String p = utils.DBMeta.listMetaColumnsLoad(conn, "player");  // fields list, comma separated
    final String query = """
        SELECT *
        FROM player
        WHERE player.idplayer = ?
        AND player.PlayerPassword = SHA2(?,256)
   """ ;
    
    ps = conn.prepareStatement(query);
    ps.setInt(1, epp.getPlayer().getIdplayer()); 
    ps.setString(2, epp.getPassword().getCurrentPassword()); 
      utils.LCUtil.logps(ps); 
    rs = ps.executeQuery();
    
    int i = 0;
    if(rs.next()){ 
        i++;
     }
    
     if(i == 0){
      //     String msg = "password not match";
        String err = LCUtil.prepareMessageBean("password.notmatch");
         LCUtil.showMessageFatal(err);
            LOG.error(err); // + " for player = " + epp.getPlayer().getIdplayer() + " // " + epp.getPassword().getWrkpassword());
 //           LOG.debug("line 02 = ");
            return false;
       }else{
            LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
            String msg = LCUtil.prepareMessageBean("password.match") + epp.getPassword().getCurrentPassword();
         // String msg = "OK";
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
         return true;
     }

  //    }    

}catch (SQLException e){
    String msg = "SQL Exception = " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
finally{
        DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method

void main() throws Exception {
    Connection conn = new DBConnection().getConnection();
  try{
        EPlayerPassword epp = new EPlayerPassword();
        Player pl = new Player();
        pl.setIdplayer(324713);
        epp.setPlayer(pl);
        Password pa = new Password();
        pa.setCurrentPassword("***");
        epp.setPassword(pa);
        boolean b = new FindPassword().passwordMatch(epp, conn);
            LOG.debug("from main, after = " + Boolean.toString(b).toUpperCase());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
}  // end class