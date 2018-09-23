
package lc.golfnew;

import delete.DeleteActivation;
import entite.Player;
import find.FindActivationPlayer;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import modify.ModifyPlayerActivation;
import utils.DBConnection;
import utils.LCUtil;

@Named("activationC")
@SessionScoped

public class ActivationController implements Serializable, interfaces.Log
{
// key = le nom de la field dans http://localhost:8080/HelloGolf-1.0-SNAPSHOT/activation_check.xhtml?key="keyLC"
private String uuid;
private boolean valid;
private static Player player = null;
//private Connection conn = null;

public ActivationController() // constructor
{
    //
}
public String checkActivation() throws SQLException, Exception, Throwable 
{ // was boolean String in_key
    // c'est ici qu'il faut vérifier !!!
     utils.DBConnection dbc = new utils.DBConnection();
     Connection conn = dbc.getConnection();
    try{
        
        
    LOG.info("key in check = " + uuid);  // récupérer de fViewParam dans activation_check.xhtml

    FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
        String keyLC = paramMap.get("uuid");
    LOG.info("keyLC  = " + keyLC);
    // ou aussi
    String keyLC2 = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("uuid");
    LOG.info("keyLC2  = " + keyLC2);
    
    FindActivationPlayer fap = new FindActivationPlayer();  // new instance
    player = fap.findActivationPlayer(conn, uuid);
    // mod 19/08/20108 encore à tester 
 ///   player = findActivationPlayer(uuid); // vérifie si uuid est en attente dans table activation
        LOG.info(" return from = " + player.getIdplayer());
    if(player.getIdplayer() != null) // trouvé dans table Activation
    {
        LOG.info("idplayer ready for activation  = " + player.getIdplayer() );
//       DBConnection dbc = new DBConnection();
      
        DeleteActivation da = new DeleteActivation();
        boolean b = da.deleteActivation(conn, uuid);
        // delete record dans table Activation
        if(b == false)
        { LOG.info("echec delete record Table activation !!!");
            setValid(false);
        }else{
            LOG.info("OK record deleted in Table activation  = ");
            // update player !!
            ModifyPlayerActivation mpa = new ModifyPlayerActivation();
            String d = mpa.updateRecordFromPlayer(player); // setPlayerActivation = 1 
            if(d==null)
            {
                LOG.info("echec update activation in Table player !!!");
                setValid(false);
            }else{
                 LOG.info("Sucessfull updated activation in Table player = " + d);
                setValid(true);
                // send mail to user
                     String sujet = "Succesfull activation to golflc !!!";
                     String msg ="ok with your activation !! click on next url : ";
                     String url = utils.LCUtil.firstPartUrl();
                    // à modifier utilier <href ....>
                    //  msg = msg + "http://localhost:8080/GolfNew-1.0-SNAPSHOT/login.xhtml";
                     String href = msg + url + "/login.xhtml";
 // à mofifier             //       <a href=" + href + ">"
                     String to = "louis.collet@skynet.be";
                     utils.SendEmail sm = new utils.SendEmail();
                     b = sm.sendHtmlMail(sujet,href,to);
                        LOG.info("HTML Mail status = " + b);
            }
        }

    }else{
        setValid(false);
        LOG.info("Player = null ");
    }
     LOG.info("final result valid in check = " + valid);
   // return valid;
     if(valid)
     { 
         String language = player.getPlayerLanguage();
         String playerid = Integer.toString(player.getIdplayer());
         //
         return "activation_success.xhtml?faces-redirect=true&language=" + language + "&id=" + playerid;
     }else{
         return "activation_failure.xhtml?faces-redirect=true";
     }
  } catch (SQLException sqle) {
            String msg = "£££ SQLException in ActivationController = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
      } catch (Exception e) {
            String msg = "£££ SQLException in activation controller  = " + e.getMessage(); // + " ,SQLState = "
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;        
}finally
{
        DBConnection.closeQuietly(conn, null, null, null);
}
}
    public boolean isValid()
    {   LOG.info("isValid = " + valid);
        return valid;
    }

    public void setValid(boolean valid)
    {  LOG.info("setValid = " + valid);
        this.valid = valid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public static void main(String[] args) throws Exception, Throwable {
 //    Connection conn = DBConnection.getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(566666); // 456895
        ActivationController ac = new  ActivationController();
        String s = ac.checkActivation(); //"fcb35e1e-970d-46fc-88f0-929a8555d0d8");
  //          LOG.info("01");
            LOG.info("from main, after !! = " + s);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
  //       DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//


} // end class