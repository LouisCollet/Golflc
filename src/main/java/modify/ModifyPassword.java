
package modify;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.EPlayerPassword;
import entite.Password;
import entite.Player;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyPassword implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean modify(final EPlayerPassword epp, final Connection conn) throws Exception{
        PreparedStatement ps = null;
        int row = 0;
        boolean b = false;
        try {
            LOG.info("entering modifyPassword with epp = " + epp);
          Player player = epp.getPlayer();
          Password password = epp.getPassword();
     //       LOG.info("entite Password = " + password);
            if(password == null){ // tout premier password ???
                 LOG.info("entite Password = null " + password);
                 LOG.info("ca marche " );
            }
          List<String> list = new ArrayList<>();
            // encore à faire si l'entité est nulle, créer une liste vide // 
            //sinon bloque
            LOG.info("entite Password = " + password);
            LOG.info("Previous Passwords = " + password.getPreviouspasswords());
        if(password.getWrkpassword() == null){
             String err = "wrkpassword is null !!";
             LOG.info(err);
             // possible en ncas de reset !!
     //        LCUtil.showMessageFatal(err);
     //        throw new Exception(err);
        }

        if(password.getWrkpassword().equals("RESET PASSWORD")){
            LOG.info("case reset password");
        }else{
   // utile ??if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
           if(! password.getWrkpassword().equals(password.getWrkconfirmpassword() )){ 
                String err = LCUtil.prepareMessageBean("password.password.notmatch")+ " : " + password.getWrkpassword()
                        + " / " + password.getWrkconfirmpassword();
                //Le mot de passe de confirmation n'est pas le même que le mot de passe
                LOG.error(err); 
                LCUtil.showMessageFatal(err);
            //    throw new Exception(err);
                return false;
           }else{
               LOG.info("we continue because password.getWrkpassword().equals(password.getWrkconfirmpassword() !! ");
           }
        }
        
// new 11-03-2020
     LOG.info("starting completing list"); // on travaille sur une liste : plus facile permet add et contains
     // mais pour strocker en DB il y a un bug dans Jackson : et on doit stocker l'array ...
 
     list = password.getPreviouspasswords();
  //      LOG.info("list au début = " + list);
 // vérification de non utilisation !!!       
        LOG.info("contenu list interne = " + list);
     if(list.contains(password.getWrkpassword())){
         //   String err = "password already used in the past";
            String err = LCUtil.prepareMessageBean("player.password.reuse") + " : "
                    + password.getWrkpassword()
                    + " / " + password.getPreviouspasswords().toString();
              //Le mot de passe a déjà été utilisé dans le passé
                LOG.error(err); 
             LCUtil.showMessageFatal(err);
                return false;
        }else{
            LOG.info("ce password n'a jamais été utilisé");
        }
   // à faire : vérifier si le old ne se trouve pas déjà dans la liste pour ne pas l'y ajouter une nouvelle fois 
   //réfléchir si c'est possible !!

   if(password.getCurrentPassword() != null){
       list.add(password.getCurrentPassword()); // ancien password à ajouter à liste = modification de password
                                // après controle du password courant que l'on a saisi au passage
   }else{
       list.add(password.getWrkpassword()); // RESET PASSWORD ancien password à ajouter à liste = réinitialisation de password
   }
       
        LOG.info("list avec old password ajouté = " + list);
     password.setPreviouspasswords(list);
     String[] array = list.toArray(new String[list.size()]);
     password.setArraypasswords(array);// mettre l'array dans password
     epp.setPassword(password);
 //     LOG.info("password is now updated = " + password);
 //     LOG.info("array = " + Arrays.toString(password.getArraypasswords()));
 //     LOG.info("list password.getPreviouspasswords() = " + password.getPreviouspasswords());
       ObjectMapper om = new ObjectMapper();
       String json = om.writeValueAsString(password);
       // ne prendra que l'array car la liste est marquée @JsonIgnore et donc ne sera pas chargée en database
          LOG.info("Previouspasswords converted in json format= " + json);

    String query = " UPDATE Player " +
"SET player.PlayerPassword = SHA2(?,256)," +
"   player.PlayerPreviousPasswords = ?" +  // 11/03/2020  
"  WHERE player.idplayer = ?"
            ;
 //       LOG.info("query Modify Player 1 = " + query);
            ps = conn.prepareStatement(query);
            // si password oublié : o le réinitialise à NULL
            if(password.getWrkpassword().equals("RESET PASSWORD")){
                ps.setNull(1, java.sql.Types.CHAR);  // reset PlayerPassword to NULL
            }else{
                ps.setString(1, password.getWrkpassword());
            }
            ps.setString(2,json);  // list/aray old passwords
            ps.setInt(3, player.getIdplayer());
            utils.LCUtil.logps(ps);
  //          LOG.info("Prepared Statement after bind variables set:\n\t" + ps.toString());
            row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if(row == 1){
                LOG.info("PlayerPassword created or modified");
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
    //   DBConnection dbc = 
     Connection conn = new DBConnection().getConnection();
  try{
        Player p = new Player();
        p.setIdplayer(324713); // 222222
        EPlayerPassword epp = new EPlayerPassword();
        epp.setPlayer(p);
        epp = new load.LoadPlayer().load(epp, conn); // 2e version, la première reste valable output = player only
        Password pa = epp.getPassword();
        LOG.info("liste 01 = " + pa.getPreviouspasswords());
        // à modifier !!
 //       Password pa = new Password();
        pa.setWrkpassword("Lc1lc2%lc4");
        pa.setWrkconfirmpassword("Lc1lc2%lc4");
   //     pa.setPassword(pa);
          LOG.info("liste 02 = " + pa.getPreviouspasswords());
        p.setPlayerLanguage("fr");   // éviter erreurs avec messages
  //      EPlayerPassword epp = new EPlayerPassword();
        boolean b = new ModifyPassword().modify(epp, conn);
        LOG.info("from main, result = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
 
} //end Class