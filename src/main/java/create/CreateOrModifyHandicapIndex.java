
package create;

import entite.HandicapIndex;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreateOrModifyHandicapIndex implements interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
//public Integer status( final Round round, final Connection conn) throws SQLException{
 public Integer status( final HandicapIndex handicapIndex, final Connection conn) throws SQLException{   
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    PreparedStatement ps = null;
    ResultSet rs = null;
 try{
        LOG.debug(" ... entering " + methodName);
        LOG.debug("with HandicapIndex  = " + handicapIndex);
   final String query = """
        SELECT HandicapId
        FROM handicap_index
        WHERE HandicapRoundId = ?
        AND HandicapPlayerId = ?
     """;

    ps = conn.prepareStatement(query);
    ps.setInt(1,handicapIndex.getHandicapRoundId());
    ps.setInt(2,handicapIndex.getHandicapPlayerId());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    int handicapId = 0;
    if(rs.next()){ 
        handicapId = rs.getInt(1);  // or count ??
          LOG.debug("HandicapId already exists - This is a modification = " + handicapId);
        return handicapId;
    }else{
          LOG.debug("HandicapId doesn't exists - This is a creation" );
        return 0;
    }
} catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 0;
 } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return 0;
 } finally {
     //       DBConnection.closeQuietly(null, null, null, ps);
        }
    } //end method
  void main() throws SQLException, Exception{ //enlevé static
      Connection conn = new DBConnection().getConnection();
  try{
   HandicapIndex handicapindex= new HandicapIndex();
   handicapindex.setHandicapRoundId(589);
   handicapindex.setHandicapPlayerId(324715);
   int i = new CreateOrModifyHandicapIndex().status(handicapindex, conn);
        LOG.debug("Creation or modification ? (0 = creation) " + i);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
 }finally{
       DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end class