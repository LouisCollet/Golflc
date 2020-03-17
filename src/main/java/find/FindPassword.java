package find;

import entite.EPlayerPassword;
import entite.Password;
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

public class FindPassword implements interfaces.Log{
   final private static String CLASS_NAME = Thread.currentThread().getStackTrace()[1].getClassName(); 
    private static List<Player> liste = null;
   
//public Boolean passwordMatch(final Player player , final Connection conn) throws SQLException{
    public Boolean passwordMatch(final EPlayerPassword epp , final Connection conn) throws SQLException{
      LOG.info("starting passwordMatch  ");
      Player player = epp.getPlayer();
      Password password = epp.getPassword();
      LOG.info("starting passwordMatch for player = " + player);
      LOG.info("starting passwordMatch with entite password = " + password);
   //   LOG.info("starting passwordMatch with password = " + password.getPlayerPassword());
   //   LOG.info("starting passwordMatch with old Password = " + password.getCurrentPassword());
    PreparedStatement ps = null;
    ResultSet rs = null;
try{   
    String p = utils.DBMeta.listMetaColumnsLoad(conn, "player");  // fields list, comma separated
    String query =
    "SELECT " + p +
    "    FROM player" +
    "    WHERE player.idplayer = ?" +
    "    AND player.PlayerPassword = SHA2(?,256);"
    ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer()); 
 //   ps.setString(2, player.getWrkpassword()); 
    ps.setString(2, password.getCurrentPassword()); 
      utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet passwordMatch has " + rs.getRow() + " lines.");
    if(rs.getRow() == 0){
        String msg =  LCUtil.prepareMessageBean("password.notmatch");
        LCUtil.showMessageFatal(msg);
            LOG.error(msg + " for player = " + player.getIdplayer() + " // " + password.getWrkpassword());
        return false;
      }    

    if(rs.getRow() == 1){
       liste = new ArrayList<>();
       rs.beforeFirst();
       while (rs.next()){
          liste.add(entite.Player.mapPlayer(rs));
       }
        String msg = LCUtil.prepareMessageBean("password.match") + password.getCurrentPassword();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        
        return true;
      }    

}catch (SQLException e){
    String msg = "SQL Exception in FindPassword : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in ClassName " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
///}
///else{
///       LOG.debug("escaped to FindPassword repetition with lazy loading");
///    retu//rn liste;  //plusieurs fois ??
///    }
return false;
} //end method

public static void main(String[] args) throws Exception {
    Connection conn = new DBConnection().getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(121221);
        EPlayerPassword epp = new EPlayerPassword();
        Password pa = new Password();
        pa.setWrkpassword("test123LC");
        epp.setPassword(pa);
        
        boolean b = new FindPassword().passwordMatch(epp, conn);
            LOG.info("from main, after = " + Boolean.toString(b).toUpperCase());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
}  // end class