package create;

import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreatePlayer implements java.io.Serializable, interfaces.Log, interfaces.GolfInterface//, interfaces.PlayerDao
{  
    // private final static String photoFile = "no photo";
    private static String photo;
     
 //@Override
    public boolean createPlayer(final entite.Player player, final entite.Handicap handicap,
                final java.sql.Connection conn, final String batch) throws Exception{
        java.sql.PreparedStatement ps = null;
        int row = 0;
        boolean b = false;
        try {
            LOG.info("playerid = " + player.getIdplayer());
            LOG.info("player     = " + player.toString());
            LOG.info("handicap   = " + handicap.toString());
            LOG.info("Batch      = " + batch);
     //       LOG.info("Photo      = " + photoFile);

            String query = LCUtil.generateInsertQuery(conn, "player"); // new 15/11/2012

            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setInt(1, player.getIdplayer());
            ps.setString(2, player.getPlayerFirstName());
            ps.setString(3, player.getPlayerLastName());
            ps.setString(4, player.getPlayerCity());
            ps.setString(5, player.getPlayerCountry());
            ps.setDate(6, LCUtil.getSqlDate(player.getPlayerBirthDate()));
            ps.setString(7, player.getPlayerGender());
            ps.setInt(8, player.getPlayerHomeClub());
            // à modifier
     //       String photo = "";
            if(player.iseID()){ // player with belgian eID
       //             photo = FilleIDcardPlayer.photoURL(CourseController.eID);
                    photo = player.getPlayerPhotoLocation();
                        LOG.info("photo file for database = " + photo );
            }else{
                    photo = "no photo";}
            ps.setString(9, photo);
            ps.setString(10, player.getPlayerLanguage());
            ps.setString(11, player.getPlayerEmail()); // new 15/11/2012
            if(batch.equals("B")){
                LOG.info("is batch");
                ps.setShort(12, Short.parseShort("1"));
            }else{
                ps.setShort(12, (short) 0);} // getPlayerActivation , activÃ© Ã  0 doit Ãªtre 1 !! new 13/04/2013
            ps.setString(13, player.getPlayerTimeZone().getTimeZoneId() ); // new 28/03/2017 using GoogleTimeZone
            ps.setString(14, player.getPlayerLatLng().toString()); // new 28/03/2017
            // new 07/08/2018  attention password est null à la création !!
            ps.setString(15, null); 
            ps.setString(16, "PLAYER"); 
            ps.setTimestamp(17, LCUtil.getCurrentTimeStamp()); // was 15
            utils.LCUtil.logps(ps);
            row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg = "<h1> successful insert Player : "
                            + " <br/></h1>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful insert Player row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not insert player"));
                //    return false; pas compatible avec throw
            }
    // new 15/11/2012
            if (row != 0) // insert initial handicap, si successfull insert player
            {    
                CreateInitialHandicap cih = new CreateInitialHandicap();
                b = cih.createHandicap(conn, player, handicap, batch);  // new 24/06/2014
                LOG.info("returning from CreateInitialHandicap with = " + b);
                if(b == false) // new 20/10/2014
                {
                    String msg = "boolean returned from create InitialHandicap is false ==> rollback ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            } 

            if ((row != 0) && (! batch.equals("B")) && (b == true)) // insert initial Activation , si successfull insert player and handicap
                                                    // pas d'activation en batch mode
            {
                LOG.info("not batch activation");
                CreateActivation ca = new CreateActivation();
                b = ca.createActivation(conn, player, handicap);
                LOG.info("returning from create activation with = " + b);
                if(b == false) // new 20/10/2014
                {
                    String msg = "boolean returned from create activation is false ==> rollback ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }
return true;
        } // end try
catch (SQLException sqle) {
            String msg = "";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 )
            {
                 msg =  "<h1> "+ LCUtil.prepareMessageBean("create.player.fail")
                         + player.getIdplayer()+ "</h1>"; 
            }else{
                 msg = "SQLException in createPlayer = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();}
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;//null;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Insert Player = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end createplayer
} //end Class