package find;

import entite.Player;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindPassword implements interfaces.Log
{
   final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
   
public Boolean passwordMatch(final Player player , final Connection conn) throws SQLException
{   

      LOG.info("starting passwordMatch  ");
      LOG.info("starting passwordMatch for player = " + player.getIdplayer());
      LOG.info("starting passwordMatch with wrk password = " + player.getWrkpassword());
      LOG.info("starting passwordMatch with password = " + player.getPlayerPassword());
    //  var name = "Java"; //test 08-08-2018 nouveauté JDK10
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{   
    String query =
     " SELECT PlayerPassword " +
    "    FROM player" +
    "    WHERE player.idplayer = ?" +
    "    AND player.PlayerPassword = SHA2(?,256);"
    ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, player.getIdplayer()); 
    ps.setString(2, player.getWrkpassword()); 
      utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    rs.last(); //on récupère le numéro de la ligne
        LOG.info("ResultSet passwordMatch has " + rs.getRow() + " lines.");
    if(rs.getRow() == 1)
      {   String msg = "££ Password match !!! " + ClassName +  " for player = " + player.getIdplayer()
              +  " for password = " + player.getWrkpassword();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
      }    
    if(rs.getRow() == 0)
      {   String msg =  LCUtil.prepareMessageBean("password.notmatch");
        //  String msg = "££ Password does not match " 
         //     +  " for password = " + ;
       
        LCUtil.showMessageFatal(msg);
            LOG.error(msg + " for player = " + player.getIdplayer() + " // " + player.getWrkpassword());
        return false;
        
     //     throw new LCCustomException(msg);
      }
}catch (SQLException e){
    String msg = "SQL Exception in FindTeeStart : " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in ClassName " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
finally
{
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
        DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(121221);
        p.setWrkpassword("test123LC");
        FindPassword fp = new FindPassword();
        boolean b = fp.passwordMatch(p, conn);
        
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