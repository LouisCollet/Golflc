package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateCompetitionDescription implements Serializable, interfaces.Log, interfaces.GolfInterface{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public boolean update(final CompetitionDescription cd, final Connection conn) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
  try {
           LOG.debug("... entering = " + methodName);
           LOG.debug(" with competition = " + cd);
    String co = utils.DBMeta.listMetaColumnsUpdate(conn, "competition_description");
           LOG.debug("String from listMetaColumns = " + co);
    final String query =  """
            UPDATE competition_description
            SET %s
            WHERE CompetitionId = ?
     """.formatted(co);
    ps = conn.prepareStatement(query);
    ps = CompetitionDescription.psCompetitionDescriptionModify(ps,cd);  // new 16/05/2022
     utils.LCUtil.logps(ps);
     int row = ps.executeUpdate(); // write into database
       LOG.debug("rows modified competition_description = " + row);
            if(row != 0) {
                String msg =  LCUtil.prepareMessageBean("competition.description.modify")
                            + " <br/>ID = " + cd.getCompetitionId()
                            + " <br/>Name = " + cd.getCompetitionName();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
            }else{
                    String msg = "-- NOT NOT successful " + methodName;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
            }
}catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
}catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end UpdateCompetitionDescription

  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
       CompetitionDescription competition = new CompetitionDescription();
       competition.setCompetitionId(25);
                      String msg =  LCUtil.prepareMessageBean("competition.description.modify")
                            + " <br/>ID = " + competition.getCompetitionId()
                            + " <br/>Name = " + competition.getCompetitionName();
     //                       + " <br/>Par = " + competition.getCompetitionDescriptionPar();
                    LOG.debug(msg);
      
    
/*    
     competition  = new LoadCompetitionDescription().load(competition,conn);
        LOG.debug(" loadedcompetition = " + competition);
     competition.setCompetitionAgeLadies((short)50);
     competition.setCompetitionAgeMens((short)60);
     competition.setCompetitionMaximumPlayers((short)59);
     boolean b = new modify.UpdateCompetitionDescription().modify(competition, conn);
        LOG.debug(" modified competition = " + b);
*/
     DBConnection.closeQuietly(conn, null, null, null);
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end Class