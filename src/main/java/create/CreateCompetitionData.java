package create;

import entite.CompetitionData;
import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;

public class CreateCompetitionData {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    private static CompetitionDescription cd = null;
    
 //public boolean create(ECompetition ec, final Player player, final Connection conn) throws SQLException, InstantiationException{
  public boolean create(CompetitionData data, final Connection conn) throws SQLException {
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        LOG.debug("... entering in " + methodName); 
        LOG.debug("with competitionData = " + data);
 //       LOG.debug("for player = " + player);
    PreparedStatement ps = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "competition_data");
    int index = 0;
    ps = conn.prepareStatement(query);
    ps.setNull(++index, java.sql.Types.INTEGER); // CompetitionId
  //  ps.setInt(2,cd.getCompetitionId());
    ps.setInt(2,data.getCmpDataCompetitionId());  // mod 18-03-2022
  //  ps.setInt(3, player.getIdplayer());
    ps.setInt(3, data.getCmpDataPlayerId()); // mod 18-03-2022
//  LOG.debug("line 04");
    ps.setShort(4,(short)0); // playingHandicap
    ps.setDouble(5,0); // handicap
 //     LOG.debug("line 04b");
    ps.setTime(6,Time.valueOf("00:00:00")); // flight start
 //    LOG.debug("line 04c");
    ps.setShort(7,(short)0); // flight number
//  LOG.debug("line 07");
    ps.setShort(8, (short)0); // scorepoints
    ps.setString(9,data.getCmpDataLastHoles()); // sert à quoi ? à modifier utilisation provisoire !!
    ps.setString(10,data.getCmpDataPlayerFirstLastName()); // mod 18-03-2022 player.getPlayerLastName() + ", " + player.getPlayerFirstName());
    ps.setString(11,data.getCmpDataAskedStartTime());
    ps.setString(12,data.getCmpDataPlayerGender());
    ps.setInt(13,0); // CmpDataRoundId()) // new 27-10-2020
    ps.setString(14,""); // CmpDataTeeStart new 03-11-2020
    ps.setDouble(15,0); // score differential
    ps.setTimestamp(16, Timestamp.from(Instant.now()));

    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
    if(row!=0){
         data.setCmpDataId(LCUtil.generatedKey(conn));
         LOG.debug("-- Successfull update CompetitionData : ");
         String msg =  LCUtil.prepareMessageBean("competition.data.create") + data + "<br>" + data;
         LOG.debug(msg); 
         LCUtil.showMessageInfo(msg);
         return true;
    }else{
         String msg = "-- ERROR update competitionData : " + data; 
         LOG.debug(msg); 
         LCUtil.showMessageFatal(msg);
         return false;
    }
} catch(SQLException sqle) {
        String msg="";
            if(sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062 ){
                 msg = LCUtil.prepareMessageBean("create.competitiondata.duplicate")
                 + " competition = " + data.getCmpDataCompetitionId();
            }else{
             msg = "SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            }
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
} catch(Exception e) {
       String msg = "£££ Exception in " + methodName + e.getMessage();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
    }finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
 //       return false;
    }
} //end updateHoles
 
  void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
       // to be completed
  //          Player player = new Player();
    //        player.setIdplayer(324713);
    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
 } // end class