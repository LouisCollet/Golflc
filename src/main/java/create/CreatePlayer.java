package create;

import com.google.maps.model.LatLng;
import entite.Handicap;
import entite.Player;
import googlemaps.GoogleTimeZone;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;

public class CreatePlayer implements java.io.Serializable, interfaces.Log, interfaces.GolfInterface{
  //  private static String photo;
     
    public boolean create(final entite.Player player,
            final entite.Handicap handicap,
                final java.sql.Connection conn, final String batch) throws Exception{
        java.sql.PreparedStatement ps = null;
        conn.setAutoCommit(false); // il faut le commit pour écrire dans database !!!
        int row = 0;
        boolean b = false;
        String photo;
        try {
            LOG.info("entering create.createPlayer");
      //      LOG.info("playerid = " + player.getIdplayer());
            LOG.info("player     = " + player);
            LOG.info("handicap   = " + handicap);
            LOG.info("Batch A = normal creation, B = batch creation = " + batch);

            String query = LCUtil.generateInsertQuery(conn, "player");
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setInt(1, player.getIdplayer());
            ps.setString(2, player.getPlayerFirstName());
            ps.setString(3, player.getPlayerLastName());
            ps.setString(4, player.getPlayerCity());
            ps.setString(5, player.getPlayerCountry());
            ps.setDate(6, LCUtil.getSqlDate(player.getPlayerBirthDate())); // ??
            ps.setString(7, player.getPlayerGender());
            ps.setInt(8, player.getPlayerHomeClub());
            if(player.iseID()){ // player with belgian eID
       //             photo = FilleIDcardPlayer.photoURL(CourseController.eID);
                    photo = player.getPlayerPhotoLocation();
                        LOG.info("photo file for database = " + photo );
            }else{
                 photo = "no photo";}
            ps.setString(9, photo);
            ps.setString(10, player.getPlayerLanguage());
            ps.setString(11, player.getPlayerEmail());
            if(batch.equals("B")){
                LOG.info("is batch execution ");
                ps.setShort(12, Short.parseShort("1"));
            }else{
                ps.setShort(12, (short) 0);}
            ps.setString(13, player.getPlayerTimeZone().getTimeZoneId() ); // new 28/03/2017 using GoogleTimeZone
            ps.setString(14, player.getPlayerLatLng().toString()); // new 28/03/2017
            // new 07/08/2018  attention password est null à la création !!
            ps.setString(15, null); 
            ps.setString(16, "PLAYER"); // PlayerRole = default
            ps.setTimestamp(17, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
            row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if(row != 0){
                String msg = "Successful insert Player : "
                            + " <br/></h1>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "Fatal Error Player ! ";
                    LOG.info(msg);
                   LCUtil.showMessageFatal(msg);
                   throw (new SQLException("Fatal Error player"));
                //    return false; pas compatible avec throw
            }

            if(row != 0){ // insert initial handicap, si successfull insert player
                 handicap.setPlayerIdplayer(player.getIdplayer()); // new 12-05-2019
                if(new CreateInitialHandicap().create(conn, handicap, batch)){  // new 12-05-2019
                    String msg = "Initial Handicap created !!";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                }else{
                    String msg = "create Handicap is false  ==> rollback of CreatePlayer";
                    LOG.info(msg);
                    LCUtil.showMessageFatal(msg);
                    throw (new SQLException("row = 0 - Could not insert Handicap"));
                 //   return false;
                }
            } 
    // new 02/02/2019
            if (row != 0){ // insert initial Subscription, si successfull insert player
                if(new CreateSubscription().create(player, conn)){
                    String msg = "Initial Subscription created !!";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                }else{
                    String msg = " create Subscription is false ==> rollback of CreatePlayer , initialHandicap";
                    LOG.info(msg);
                    LCUtil.showMessageFatal(msg);
                    throw (new SQLException("Fatal Error Subscription"));
                 //   return false;
                }
            } 

 //           LOG.info("row = " + row);
 //           LOG.info("batch = " + batch);
 //           LOG.info("b = " + b);
            
            if ((row != 0) && (! batch.equals("B"))){ // && (b == true)){ // insert initial Activation , si successfull insert player and handicap
                                                    // pas d'activation en batch mode
                LOG.info("not batch activation");
              if(new CreateActivation().create(conn, player, handicap)){
         //   if(new CreateSubscription().create(player, conn)){
                    String msg = "Initial Activation created !!";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                    conn.commit(); // new 13-05-2019
                }else{
                    String msg = " create Activation is false ==> rollback of CreatePlayer , initialHandicap, Subscription";
                    LOG.info(msg);
                    LCUtil.showMessageFatal(msg);
                    throw (new SQLException("Fatal error activation"));
               //     return false;
                } 
            }
return true;
 }catch(SQLException sqle) {
            printSQLException(sqle); // new 13-05-2019
     //     if (conn != null) {
      //      try {
                LOG.error("Transaction is being rolled back");
                conn.rollback();
       //     } catch(SQLException excep) {
       //         printSQLException(excep);
       //     }

             String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.player.fail");
                 msg = msg + player.getIdplayer();
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                 return false;
            }else{
                 msg = "SQLException in createPlayer = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();}
                 LOG.error(msg);
                 LCUtil.showMessageFatal(msg);
                 return false;
  
   } catch(Exception nfe) {
            String msg = "£££ Exception in Insert Player = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            conn.setAutoCommit(true); // reset écriture directe dans database !!!
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
            
        }

    } //end createplayer
    
  public static void main(String[] args) throws SQLException {
     Connection conn = null;
  try{
        conn = new DBConnection().getConnection();
        Player player = new Player();
        player.setIdplayer(528953); // 528951 529952
        player.setPlayerFirstName("first test");
        player.setPlayerLastName("last test");
        player.setPlayerBirthDate(SDF.parse("01/03/2000"));
        GoogleTimeZone gtz = new GoogleTimeZone();
        gtz.setTimeZoneId("Europe/Brussels");
        player.setPlayerTimeZone(gtz);
        player.setPlayerHomeClub(101);
        player.setPlayerCity("Brussels");
        player.setPlayerGender("M");
        player.setPlayerLanguage("fr");
        player.setPlayerCountry("BE");
        player.setPlayerLatLng(new LatLng(Double.parseDouble("50.8262271"), Double.parseDouble("4.3571382"))); // amazone 55

        Handicap handicap = new Handicap();
        handicap.setHandicapPlayer(BigDecimal.valueOf(36.0));
        handicap.setHandicapStart(SDF.parse("16/04/2018"));
        
        Boolean b = new create.CreatePlayer().create(player, handicap, conn, "A");
        LOG.info("resultat = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
           LCUtil.showMessageFatal(msg);
   }finally{
          DBConnection.closeQuietly(conn, null, null, null);
          }
   } // end main//
} //end Class