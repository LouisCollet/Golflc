package update;

import entite.CompetitionData;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import read.LoadCompetitionData;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateCompetitionData implements Serializable, interfaces.Log, interfaces.GolfInterface{
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

public boolean update(final CompetitionData cda, final Connection conn) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
  try {
            LOG.debug("... entering = " + methodName);
            LOG.debug(" with competition = " + cda);
     String co = utils.DBMeta.listMetaColumnsUpdate(conn, "competition_data");
 //       LOG.debug("String from listMetaColumns = " + co);
    final String query =
            "UPDATE competition_data" +
            " SET "  + co +
            "  WHERE CmpDataId = ?";
    ps = conn.prepareStatement(query);
  //    ps.setNull(++index, java.sql.Types.INTEGER); // CompetitionId  1ère ligne toujours ignorée
  //  ps.setInt(1,competition.getCmpDataId());  // blackist dans listMetaColumnsUpdate 
    ps.setInt(1, cda.getCmpDataPlayerId());
//  LOG.debug("line 04");
    ps.setShort(2,cda.getCmpDataPlayingHandicap());
    ps.setDouble(3,cda.getCmpDataHandicap());
 //     LOG.debug("line 04b");
    ps.setTime(4,Time.valueOf(cda.getCmpDataFlightStart()));
 //    LOG.debug("line 04c");
    ps.setShort(5,cda.getCmpDataFlightNumber()); // flight number
//  LOG.debug("line 07");
    ps.setShort(6, cda.getCmpDataScorePoints()); // scorepoints
    ps.setString(7, cda.getCmpDataLastHoles()); // à modifier utilisation provisoire !!
    ps.setString(8,cda.getCmpDataPlayerFirstLastName());
    ps.setString(9, cda.getCmpDataAskedStartTime());
    ps.setString(10,cda.getCmpDataPlayerGender()); // mod 07-10-2020
    ps.setInt(11,cda.getCmpDataRoundId()); // new 27-10-2020
    ps.setString(12,cda.getCmpDataTeeStart()); // new 03-11-2020
    ps.setDouble(13,cda.getCmpDataScoreDifferential()); // new 17-11-2020
    ps.setTimestamp(14, Timestamp.from(Instant.now()));

 // insert parameters at the location of the question mark
    ps.setInt(15, cda.getCmpDataId());  // ne pas oublier !!

    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
      LOG.debug("row modified competition_data = " + row);
            if(row != 0) {
                String msg =  // LCUtil.prepareMessageBean("course.modify")
                            " <br/>ID = " + cda.getCmpDataId()
                            + " <br/>Start Time = " + cda.getCmpDataAskedStartTime();
     //                       + " <br/>Par = " + competition.getCompetitionDataPar();
                    LOG.debug(msg);
         //           LCUtil.showMessageInfo(msg);
                    return true;
            }else{
                    String msg = "-- NOT NOT successful " + methodName;
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
            }

        } // end try
catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
}catch (Exception e) {
            String msg = "£££ SQLException in " + methodName + e;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end ModifyCompetitionData

  void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     CompetitionData competition = new CompetitionData();
     competition.setCmpDataId(25);
     competition  = new LoadCompetitionData().load(competition,conn);
        LOG.debug(" loadedcompetition = " + competition);
     competition.setCmpDataFlightNumber((short)1);
//setCompetitionAgeLadies((short)50);
   //  competition.setCompetitionAgeMens((short)60);
   //  competition.setCompetitionMaximumPlayers((short)59);
     boolean b = new update.UpdateCompetitionData().update(competition, conn);
        LOG.debug(" modified competition = " + b);
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