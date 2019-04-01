
package modify;

import entite.Player;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyPassword implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean modifypassword(final Player player, final Connection conn) throws Exception{
        PreparedStatement ps = null;
        int row = 0;
        boolean b = false;
        try {
            LOG.info("entering ModifyPassword");
       //     LOG.info("player   = " + player.getIdplayer());
            LOG.info("starting modifyPassword with player  = " + player.toString());
      //      LOG.info("starting modifyPassword with password      = " + player.getPlayerPassword());
  //  String s = utils.DBMeta.listMetaColumnsUpdate(conn, "player");
  //      LOG.info("String from listMetaColumns = " + s);
        // encrypted password with SHA2 function of mysql 
  //  s = s.replace("playerpassword=?" , "playerpassword=sha2(?,256)"); // new 07-08-2018 
    
  //      LOG.info("String modified for encryption password sha2 = " + s);
           if(! player.getWrkpassword().equals(player.getWrkconfirmpassword() )){ 
                String msgerr =  LCUtil.prepareMessageBean("player.password.notmatch");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return false;
           } 
    String query = " UPDATE Player " +
"	SET player.PlayerPassword = SHA2(?,256)" +
"	WHERE player.idplayer = ?"
            ;
        LOG.info("query Modify Player 1 = " + query);
            ps = conn.prepareStatement(query);
            // si password oublié : o le réinitialise à NULL
            if(player.getWrkpassword().equals("RESET PASSWORD")){
                ps.setNull(1, java.sql.Types.CHAR);  // reset PlayerPassword to NULL
            }else{
                ps.setString(1, player.getWrkpassword());
            }
            
            ps.setInt(2, player.getIdplayer());
            utils.LCUtil.logps(ps);
            row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if(row == 1){
                LOG.info("PlayerPassword created or modified");
      //          String msg =  LCUtil.prepareMessageBean("player.modify")
                 String msg = LCUtil.prepareMessageBean("player.password.modified");
                        msg = msg + " <br/>ID = " + player.getIdplayer();
                        msg = msg + " <br/>password = " + player.getWrkpassword();
                 LOG.info(msg);
                 LCUtil.showMessageInfo(msg);
              return true;
            }else{
                    String msg = "-- NOT NOT successful modify Player row = 0 !!! ";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
                 //   throw (new SQLException("row = 0 - Could not modify player"));
                    return false; //pas compatible avec throw
            }
//return true;
        } // end try
catch(SQLException sqle){
            String msg = "£££ SQLException in Modify Player = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }catch (Exception nfe){
            String msg = "£££ Exception in Modify Player = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyPassword
   public static void main(String[] args) throws Exception {
       DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection(); // main
  try{
        Player p = new Player();
        p.setIdplayer(456781);
        p.setWrkpassword("RESET PASSWORD");
   //     LOG.info("01");
        ModifyPassword mp = new ModifyPassword();
        boolean b = mp.modifypassword(p, conn);
        
        LOG.info("from main, after = " + Boolean.toString(b).toUpperCase());
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
 
} //end Class