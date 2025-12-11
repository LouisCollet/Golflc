
package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.composite.EPlayerPassword;
import entite.Password;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class UpdatePassword implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean update(final EPlayerPassword epp, final Connection conn) throws Exception{
        PreparedStatement ps = null;
         boolean b = false;
        try {
            LOG.debug("entering modifyPassword with epp = " + epp);
          Player player = epp.getPlayer();
          Password password = epp.getPassword();
     //       LOG.debug("entite Password = " + password);
            if(password == null){ // tout premier password ???
                 LOG.debug("entite Password = null " + password);
                 LOG.debug("ca marche " );
            }
       //   List<String> list = new ArrayList<>();
            // encore à faire si l'entité est nulle, créer une liste vide // 
            //sinon bloque
            LOG.debug("entite Password = " + password);
            LOG.debug("Previous Passwords = " + password.getPreviousPasswords());
        if(password.getWrkpassword() == null){
             String err = "wrkpassword is null !!";
             LOG.debug(err);
             // possible en cas de reset !!
     //        LCUtil.showMessageFatal(err);
     //        throw new Exception(err);
        }

        if(password.getWrkpassword().equals("RESET PASSWORD")){
            LOG.debug("case reset password");
        }else{
   // utile ??if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
           if(! password.getWrkpassword().equals(password.getWrkconfirmpassword() )){ 
                String err = LCUtil.prepareMessageBean("player.password.notmatch")+ " : " + password.getWrkpassword()
                        + " / " + password.getWrkconfirmpassword();
                //Le mot de passe de confirmation n'est pas le même que le mot de passe initial
                LOG.error(err); 
                LCUtil.showMessageFatal(err);
            //    throw new Exception(err);
                return false;
           }else{
               LOG.debug("we continue because password.getWrkpassword().equals(password.getWrkconfirmpassword() !! ");
           }
        }
        LOG.debug("control : getPreviousPasswords = " + password.getPreviousPasswords().toString());
   //  LOG.debug("starting completing list"); // on travaille sur une liste : plus facile permet add et contains
     // mais pour strocker en DB on doit la transformer en array ...
   //  List<String> list = new ArrayList<>(); // mod 27-02-2024
//   if(password.getPreviousPasswords().get(0).equals("RESET PREVIOUS")){
//       LOG.error("list previousPasswords = RESET PREVIOUS"); 
//   }
     List<String> listPreviousPasswords = password.getPreviousPasswords();
        LOG.debug("list previousPasswords = " + listPreviousPasswords);
     if(listPreviousPasswords.contains(password.getWrkpassword())){
            String err = LCUtil.prepareMessageBean("player.password.reuse") + " : " //Le mot de passe a déjà été utilisé dans le passé
                    + password.getWrkpassword() + " / " + password.getPreviousPasswords().toString();
             LOG.error(err); 
             LCUtil.showMessageFatal(err);
             return false;
        }else{
            LOG.debug("ce password n'a jamais été utilisé");
        }

   if(password.getCurrentPassword() != null){
       listPreviousPasswords.add(password.getCurrentPassword()); // ancien password à ajouter à liste = modification de password
       LOG.debug("added to listPreviousPasswords = " + password.getCurrentPassword());
                                // après controle du password courant que l'on a saisi au passage
   }else{
       listPreviousPasswords.add(password.getWrkpassword()); // RESET PASSWORD ancien password à ajouter à liste = réinitialisation de password
       LOG.debug("added to listPreviousPasswords = " + password.getWrkpassword());
   }
        LOG.debug("list avec old password ajouté = " + listPreviousPasswords);
     password.setPreviousPasswords(listPreviousPasswords);
     
     String[] array = listPreviousPasswords.toArray(String[]::new); // mod 25-02-2024 converts list to array
        LOG.debug("array = " + Arrays.toString(array));
     password.setArrayPasswords(array);
     // next line ??
   // enlevé 27-02-2024  epp.setPassword(password);
 //     LOG.debug("password is now updated = " + password);
 //     LOG.debug("array = " + Arrays.toString(password.getArraypasswords()));
 //     LOG.debug("list password.getPreviouspasswords() = " + password.getPreviouspasswords());
       ObjectMapper om = new ObjectMapper();
       String json = om.writeValueAsString(password);
       // la liste est marquée @JsonIgnore et donc ne sera pas chargée en database
          LOG.debug("Previouspasswords converted in json format= " + json);

  final String query = """
    UPDATE Player
    SET player.PlayerPassword = SHA2(?,256),
        player.PlayerPreviousPasswords = ?
    WHERE player.idplayer = ?
    """ ;
 //       LOG.debug("query Modify Player 1 = " + query);
            ps = conn.prepareStatement(query);
            // si password oublié : o le réinitialise à NULL
            if(password.getWrkpassword().equals("RESET PASSWORD")){
                ps.setNull(1, java.sql.Types.CHAR);  // reset PlayerPassword to NULL
            }else{
                ps.setString(1, password.getWrkpassword());
            }
            ps.setString(2,json);  // list/array old passwords
            ps.setInt(3, player.getIdplayer());
            utils.LCUtil.logps(ps);
  //          LOG.debug("Prepared Statement after bind variables set:\n\t" + ps.toString());
            int row = ps.executeUpdate();
                LOG.debug("row = " + row);
            if(row == 1){
                LOG.debug("PlayerPassword created or modified");
                 String msg = LCUtil.prepareMessageBean("player.password.modified")
                         + " <br/>ID = " + player.getIdplayer()
                         + " <br/>password = " + password.getWrkpassword();
                 LOG.info(msg);
                 LCUtil.showMessageInfo(msg);
                 return true;
            }else{
                 String msg = "-- NOT NOT successful modify Player row = 0 !!! ";
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
                 //   throw (new SQLException("row = 0 - Could not modify password"));
                 return false; //pas compatible avec throw
            }
//return true;
        } // end try
catch(SQLException sqle){
            String msg = "£££ SQLException in UpdatePassword = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }catch (Exception e){
            String msg = "£££ Exception in ModifyPassword = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end ModifyPassword

   void main() throws Exception {
     Connection conn = new DBConnection().getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(324720); // 222222
        EPlayerPassword epp = new EPlayerPassword();
        epp.setPlayer(p);
        epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
        Password pa = epp.getPassword();
           LOG.debug("previousPasswords liste 01 = " + pa.getPreviousPasswords());
        pa.setWrkpassword("****");
        pa.setWrkconfirmpassword("****");
          LOG.debug("liste 02 = " + pa.getPreviousPasswords());
       
        boolean b = new UpdatePassword().update(epp, conn);
        LOG.debug("from main, result = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
 
} //end Class