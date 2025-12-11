package create;

import entite.HandicapIndex;
import entite.LatLng;
import entite.Player;
import entite.Subscription;
import entite.Subscription.etypeSubscription;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;


public class CreatePlayer implements java.io.Serializable
      //  ,interfaces.PlayerInterface
{
    
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public boolean create(final Player player, HandicapIndex handicapIndex, final Connection conn, final String batch) throws Exception{
        PreparedStatement ps = null;
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
  /////////////
        conn.setAutoCommit(false); // il faut le commit pour écrire dans database !!!
        String msg = "autocommit set to false";
        LOG.info(msg);
        showMessageInfo(msg);
  //////////////
 try {
            LOG.debug("entering " + methodName);
            LOG.debug("playerid = " + player.getIdplayer());
            LOG.debug("with player     = " + player);
            LOG.debug("handicapIndex  = " + handicapIndex);
            LOG.debug("Batch A = normal creation, B = batch creation = " + batch);

        if(batch.equals("A")) {   // not for batch creation
             if(! player.getPlayerEmail().equals(player.getPlayerEmailConfirmation() )){ 
         //        String err = "email not match with email confirmation";
                String err = LCUtil.prepareMessageBean("player.email.notmatch")+ " : " + player.getPlayerEmail()
                        + " / " + player.getPlayerEmailConfirmation();
                //Le email de confirmation n'est pas le même que le premier email 
                LOG.debug("Wizard step = " + Controllers.PlayerWizard.getStep());
                LOG.error(err); 
                LCUtil.showMessageFatal(err);
                throw new Exception(err);
     //           return false;
           }else{
               LOG.debug("we continue because player.getPlayerEmail().equals(player.getPlayerEmailConfirmation() !! ");
           }
        } // enf if "A"
      final String query = LCUtil.generateInsertQuery(conn, "player");
      ps = conn.prepareStatement(query);
      ps = Player.psPlayerCreate(ps,player,batch);
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.debug("row = " + row);
            if(row != 0){
                msg = "Successful insert Player : "
                            + " <br/></h1>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                LOG.debug(msg);
      //          LCUtil.showMessageInfo(msg);
            }else{
                    msg = "Fatal Error executeUpdate in " + methodName;
                    LOG.debug(msg);
                   showMessageFatal(msg);
                   throw (new SQLException(msg));
                //    return false; pas compatible avec throw
            }
LOG.debug("CreatePlayer - Initial Handicap");
            if(row != 0){ // insert initial handicap, si successfull insert player
                 handicapIndex.setHandicapPlayerId(player.getIdplayer());
                 handicapIndex.setHandicapPlayedStrokes((short)0);
           //      Round round = new Round();
           //      round.setIdround(1); // round fictif utilisé pour handicap initial
           //      round = new load.LoadRound().load(round, conn);
           //      handicapIndex.setHandicapDate(round.getRoundDate());
                 handicapIndex = new create.CreateHandicapIndex().create(handicapIndex, conn);
                 if(handicapIndex != null){
                    msg = "Initial Handicap created !!";
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                }else{
                    msg = "create Initial Handicap is false  ==> rollback of CreatePlayer";
                    LOG.debug(msg);
                    showMessageFatal(msg);
                    throw (new SQLException(msg));
                 //   return false;
                }
            } 
LOG.debug("CreatePlayer - Initial Subscription");
            if (row != 0){ // insert initial Subscription, si successfull insert player
                Subscription subscription = new Subscription();
                subscription.setIdplayer(player.getIdplayer());
             // mod 23-02-2024     
           //     subscription.setSubCode("INIT");
                subscription.setSubCode(etypeSubscription.INITIAL.toString());// un mois gratuit !!
            //    subscription.setStartDate(LocalDateTime.now());
            //    subscription.setEndDate(LocalDateTime.now().plusMonths(1)); 
            //    subscription.setTrialCount((short)1);
           //     if(new CreatePaymentSubscription().create(subscription, conn)){
                if(new Controllers.PaymentsSubscriptionController().createPayment(subscription, conn)){ 
                    msg = "Initial Subscription created for one month : until : " + subscription.getEndDate();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                }else{
                    msg = " create Initial Subscription is false ==> rollback of CreatePlayer , HandicapIndex";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    throw (new SQLException(msg));
                 //   return false;
                }
            } 
 //           LOG.debug("row = " + row);
 //           LOG.debug("batch = " + batch);
 //           LOG.debug("b = " + b);
            
            if ((row != 0) && (! batch.equals("B"))){ // && (b == true)){ // insert initial Activation , si successfull insert player and handicap
                                                    // pas d'activation en batch mode
                LOG.debug("this is not a batch activation");
              if(new CreateActivationPlayer().create(conn, player)){
                    msg = "Activation created for !!" + player;
                    LOG.debug(msg);
                    showMessageInfo(msg);
                   
                }else{
                    msg = " create Activation is false ==> rollback of CreatePlayer , HandicapIndex, Subscription";
                    LOG.debug(msg);
                    showMessageFatal(msg);
                    throw (new SQLException(msg));
               //     return false;
                } 
            }
            
     LOG.debug("before commit all records");
         conn.commit(); // new 13-05-2019
     LOG.debug("after commit : all records are written in database");
            
return true;
 }catch(SQLException sqle) {
            printSQLException(sqle); // new 13-05-2019
            msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            showMessageInfo("Message info: " + sqle.getMessage());
    //         showMessageInfo("Cause: " + sqle.getCause().getLocalizedMessage());
       //     conn.rollback();
       //     LOG.error("Transaction is rolled back");
            if(sqle.getMessage().endsWith("'player.unique_email'")){ // error integrity constraint
      //           msg = "cette adresse mail est déjà utilisée";
                 msg = LCUtil.prepareMessageBean("create.player.fail.email") + player.getPlayerEmail();
                LOG.error(msg);
                showMessageFatal(msg);
                conn.rollback();
                LOG.error("Transaction is rolled back 1");
                
                return false;
             }
              

            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.player.fail") + player.getIdplayer();
      //           player existe déjà
                 LOG.error(msg);
                 showMessageFatal(msg);
                 conn.rollback();
                 LOG.error("Transaction is rolled back 2");
                 return false;
            }else{
                 msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();}
                 LOG.error(msg);
                 showMessageFatal(msg);
                 conn.rollback();
                 LOG.error("Transaction is rolled back 3");
                 return false;
  
   } catch(Exception e) {
            msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
   } finally {
            conn.setAutoCommit(true); // reset écriture directe dans database !!!
            msg = "autocommit set again to true !!";
            LOG.info(msg);
            showMessageInfo(msg);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }

    } //end createplayer
    
  void main() throws SQLException {
     Connection conn = null;
  try{
        conn = new DBConnection().getConnection();
        Player player = new Player();
        player.setIdplayer(678905); // 528951 529952
        player.setPlayerFirstName("first test");
        player.setPlayerLastName("last test");
        player.setPlayerBirthDate(LocalDateTime.parse("2018-11-03T12:45:30"));  // mod 13/04/2022
   //     round.setRoundDate(LocalDateTime.parse("2018-11-03T12:45:30"));
    //    TimeZone gtz = new TimeZone();
    //    gtz.setTimeZoneId("Europe/Brussels");
        player.getAddress().setZoneId("Europe/Brussels");
        player.setPlayerHomeClub(101);
        player.getAddress().setCity("Brussels");
        player.setPlayerGender("M");
        player.setPlayerLanguage("es");
    //    player.getAddress().setCountry("US");
        // mod 22-12-2022
        player.getAddress().getCountry().setCode("US");
        player.getAddress().setLatLng(new LatLng(Double.parseDouble("50.8262271"), Double.parseDouble("4.3571382"))); // amazone 55
     //   player.setPlayerLatLng(new entite.LatLng(Double.parseDouble("50.8262271"), Double.parseDouble("4.3571382"))); // amazone 55
         HandicapIndex handicapIndex = new HandicapIndex();
  //       handicapIndex.setHandicapPlayerId(player.getIdplayer());
         handicapIndex.setHandicapDate(LocalDateTime.parse("2018-11-03T12:45:30"));
         handicapIndex.setHandicapWHS(BigDecimal.valueOf(36.0));
         
        Boolean b = new create.CreatePlayer().create(player,handicapIndex,conn, "A");
           LOG.debug("resultat = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
           showMessageFatal(msg);
   }finally{
          DBConnection.closeQuietly(conn, null, null, null);
          }
   } // end main//
} //end Class