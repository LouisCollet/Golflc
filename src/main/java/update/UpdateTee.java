package update;

import entite.Tee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateTee implements Serializable, interfaces.Log, interfaces.GolfInterface {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
 public boolean update(final Tee tee, final Connection conn) throws Exception {
  final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);   
        PreparedStatement ps = null;
  try {
                LOG.debug("entering " + methodName);
                LOG.debug("with tee  = " + tee);
            String te = utils.DBMeta.listMetaColumnsUpdate(conn, "tee");
                // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
                // ne fonctionne pas
                //£££ SQLException in update.UpdateTee.update - Parameter index out of range (11 > number of parameters, which is 10). ,SQLState = S1009 ,ErrorCode = 0 
        final String query = """
            UPDATE tee
            SET %s
            WHERE tee.idtee = ?;
           """.formatted(te);
   
       LOG.debug("query formatted = " + NEW_LINE +query);
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setString(1, tee.getTeeGender());
            ps.setString(2, tee.getTeeStart());
            ps.setInt(3, tee.getTeeSlope());
            ps.setBigDecimal(4, tee.getTeeRating());
            ps.setInt(5, tee.getTeeClubHandicap());
            ps.setString(6, tee.getTeeHolesPlayed());
            ps.setShort(7, tee.getTeePar());
            ps.setInt(8, tee.getTeeMasterTee());
            if(tee.getTeeDistanceTee() == null){  // 12-08-2023
                tee.setTeeDistanceTee(0);
            }
            ps.setInt(9, tee.getTeeDistanceTee());  // new 12-08-2023
    // search key where
            ps.setInt(10, tee.getIdtee());  // ne pas oublier = where
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
      //         LOG.debug("row = " + row);
            if(row != 0) {
                String msg = LCUtil.prepareMessageBean("tee.modify")
                        + "</h1> <br/>ID = " + tee
                        + " <br/>Start position = " + tee.getTeeStart()
                        + " <br/>Gender = " + tee.getTeeGender()
                        + " <br/>Master Tee = " + tee.getTeeMasterTee();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "row = 0 - Could not modify tee";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
             //   throw (new SQLException(msg));
                return false; // pas compatible avec throw
            }
     //       return true;
  }catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyTee

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Tee tee = new Tee();
            tee.setIdtee(140);
            Tee t = new read.ReadTee().read(tee, conn);
            boolean b = new UpdateTee().update(t, conn);
            LOG.debug("from main, teemodified = " + b);
        } catch (Exception e) {
            String msg = "££ Exception in main Modify tee= " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class