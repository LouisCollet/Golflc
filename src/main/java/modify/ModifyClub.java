package modify;

//import create.*;
//import entite.Handicap;
import entite.Club;
import java.io.Serializable;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class ModifyClub implements Serializable, interfaces.Log, interfaces.GolfInterface
{
    // private final static String photoFile = "no photo";
 //   @Inject private Card card;
     
public boolean modifyClub(final Club club, final Connection conn) throws Exception
    {
        PreparedStatement ps = null;
        boolean b = false;
        try {
            LOG.info("club Name       = " + club.getClubName());
            LOG.info("club Address    = " + club.getClubAddress());
            LOG.info("club City       = " + club.getClubCity());
            LOG.info("club Country    = " + club.getClubCountry());
            LOG.info("club Latitude   = " + club.getClubLatitude());
            LOG.info("club Longitude  = " + String.format("%.6f", club.getClubLongitude() ) );
            LOG.info("club Web site   = " + club.getClubWebsite());
            LOG.info("club Zone ID   = " + club.getClubTimeZone().getTimeZoneId());
            
    String s = utils.DBMeta.listMetaColumnsUpdate(conn, "club");
        LOG.info("String from listMetaColumns = " + s);
    String query = "UPDATE club SET "
                   + s
                   + "  WHERE club.idclub=?";
        
 //   ClubName=?, ClubAddress=?, ClubCity=?, ClubCountry=?, ClubLatitude=?, ClubLongitude=?, ClubWebsite=?, ClubZoneId=? 
 //           LOG.info("isValidEmailAddress = " + ok);  // on n'en fait rien ??

            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
   //         ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getClubAddress());
            ps.setString(3, club.getClubCity());
            ps.setString(4, club.getClubCountry());
            ps.setBigDecimal(5, club.getClubLatitude());
            ps.setBigDecimal(6, club.getClubLongitude().setScale(6,RoundingMode.CEILING) ); // 6 positions décimales
            ps.setString(7, club.getClubWebsite());
            ps.setString(8, club.getClubTimeZone().getTimeZoneId() ); // new 01/08/2017 using GoogleTimeZone
            ps.setInt(9, club.getIdclub());  // ne pas oublier
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg =  LCUtil.prepareMessageBean("club.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + club.getIdclub()
                            + " <br/>Name = " + club.getClubName()
                            + " <br/>Address = " + club.getClubAddress();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful modify Club row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not modify club"));
                //    return false; pas compatible avec throw
            }
return true;
        } // end try
catch(SQLException sqle) {
            String msg = "£££ SQLException in Modify Club = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Modify Club = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyClub
} //end Class