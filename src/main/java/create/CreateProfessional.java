package create;

import entite.Professional;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import utils.DBConnection;
import static utils.LCUtil.generatedKey;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
public class CreateProfessional implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public boolean create(final Professional pro, final Connection conn) throws SQLException{
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        PreparedStatement ps = null;
  try {
            LOG.debug("...entering " + methodName);
            LOG.debug(" with Professional  = " + pro);

            final String query = utils.LCUtil.generateInsertQuery(conn, "professional");
            ps = conn.prepareStatement(query);
            ps.setNull(1, java.sql.Types.INTEGER);  // auto generated
            ps.setInt(2, pro.getProClubId());
            ps.setTimestamp(3,Timestamp.valueOf(pro.getProStartDate()));
            ps.setTimestamp(4,Timestamp.valueOf(pro.getProEndDate()));
            ps.setInt(5, pro.getProPlayerId());
            ps.setDouble(6, pro.getProAmount()); // new 06-06-2021
            ps.setTimestamp(7, Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // + write into database
            if (row != 0){
                pro.setProId(generatedKey(conn));
                String msg = "Professional Created = " + pro;
                LOG.info(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>ERROR insert Professional : " + pro;
                LOG.debug(msg);
                showMessageFatal(msg);
                return false;
            }
   }catch (SQLException sqle) {
            String msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
    } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        //return null;
    } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/8/2014
        }
    } //end method
void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
try{
   Professional pro = new Professional();
   pro.setProStartDate(LocalDateTime.parse("2021-01-01T00:00:00"));
   pro.setProEndDate(LocalDateTime.parse  ("2050-12-31T23:59:59"));
   pro.setProClubId(1186);
   pro.setProPlayerId(324720);
    boolean lp = new CreateProfessional().create(pro, conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end 