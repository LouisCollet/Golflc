package modify;

//import create.*;
//import entite.Handicap;
import entite.Tee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyTee implements Serializable, interfaces.Log, interfaces.GolfInterface
{

public boolean modifyTee(final Tee tee, final Connection conn) throws Exception
    {
        PreparedStatement ps = null;
    //    int row = 0;
        boolean b = false;
        try {
            LOG.info("tee Name  = " + tee.getTeeGender());
            LOG.info("tee Holes  = " + tee.getIdtee());
            LOG.info("tee Par  = " + tee.getTeeStart());
            
    String s = utils.DBMeta.listMetaColumnsUpdate(conn, "tee");
        LOG.info("String from listMetaColumns = " + s);
    String query = "UPDATE tee SET "
                   + s
                   + "  WHERE tee.idtee=?";

            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
   //         ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(1, tee.getTeeGender());
            ps.setString(2, tee.getTeeStart());
            ps.setInt(3, tee.getTeeSlope());
            ps.setBigDecimal(4, tee.getTeeRating());
            ps.setInt(5, tee.getTeeClubHandicap());  //new 05/07/2016
            
            ps.setInt(6, tee.getIdtee());  // ne pas oublier
////            ps.setTimestamp(9, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg =  LCUtil.prepareMessageBean("tee.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + tee.getIdtee()
                            + " <br/>Start position = " + tee.getTeeStart()
                            + " <br/>Gender = " + tee.getTeeGender();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful modify Tee row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not modify tee"));
                //    return false; pas compatible avec throw
            }
return true;
        } // end try
catch (SQLException sqle) {
            String msg = "£££ SQLException in Modify Tee = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Modify Tee = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyTee
} //end Class