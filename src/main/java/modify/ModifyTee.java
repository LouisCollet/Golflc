package modify;

import entite.Tee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyTee implements Serializable, interfaces.Log, interfaces.GolfInterface {

    public boolean modify(final Tee tee, final Connection conn) throws Exception {
        PreparedStatement ps = null;
        //    int row = 0;
        boolean b = false;
        try {
            LOG.info("entering modifyTee ... ");
            LOG.info("with tee  = " + tee.toString());

            String s = utils.DBMeta.listMetaColumnsUpdate(conn, "tee");
   //         LOG.info("String from listMetaColumns = " + s);
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
            ps.setInt(5, tee.getTeeClubHandicap());
            ps.setString(6, tee.getTeeHolesPlayed()); // new 29-03-2019
            ps.setShort(7, tee.getTeePar()); // new 03-04-2019
            ps.setInt(8, tee.getTeeMasterTee());// new 03-04-2019
            
            ps.setInt(9, tee.getIdtee());  // ne pas oublier = where
            // next = key
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            LOG.info("row = " + row);
            if(row != 0) {
                String msg = LCUtil.prepareMessageBean("tee.modify");
                msg = msg // + " successful modify Player : "
                        + " <br/>ID = " + tee.getIdtee()
                        + " <br/>Start position = " + tee.getTeeStart()
                        + " <br/>Gender = " + tee.getTeeGender();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
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
        } catch (Exception nfe) {
            String msg = "£££ Exception in Modify Tee = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyTee

 public static void main(String[] args) throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Tee tee = new Tee();
            tee.setIdtee(140);
            Tee t = new load.LoadTee().load(tee, conn);
  // field to test          t.setTeeClubHandicap(2);
            boolean b = new ModifyTee().modify(t, conn);
            LOG.info("from main, teemodified = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main Modify tee= " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//

} //end Class
