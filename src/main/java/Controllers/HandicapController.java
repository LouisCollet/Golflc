
package Controllers;
import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import update.UpdateHandicapIndex;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;

public class HandicapController implements interfaces.GolfInterface {
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 // mod 18-04-2025 
  public HandicapIndex create(final ScoreStableford scoreStableford,final Player player, final Round round, final Connection conn){    
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
try{
    HandicapIndex handicapIndex = new HandicapIndex();
    handicapIndex.setHandicapScoreDifferential(BigDecimal.valueOf(scoreStableford.getScoreDifferential()));
    handicapIndex.setHandicapPlayerId(player.getIdplayer());
    handicapIndex.setHandicapRoundId(round.getIdround());
    handicapIndex.setHandicapDate(round.getRoundDate());
    handicapIndex.setHandicapPlayedStrokes((short)scoreStableford.getTotalStrokes());
    handicapIndex.setHandicapHolesNotPlayed((short)scoreStableford.getHolesNotPlayed());
    handicapIndex.setHandicapExpectedSD9Holes(scoreStableford.getExpectedSD9Holes());
         LOG.debug("for HandicapIndex completed = " + handicapIndex);
    int handicapId = new create.CreateOrModifyHandicapIndex().status(handicapIndex, conn);
    if(handicapId == 0){ // existe pas : on le créé
        handicapIndex = new create.CreateHandicapIndex().create(handicapIndex, conn);
           LOG.debug("handicapIndex created = " + handicapIndex); //with HandicapId pour modification
           LOG.debug("key for later modify and insert HandicapWHS = " + handicapIndex.getHandicapId());
    }else{
        handicapIndex.setHandicapId(handicapId);
           LOG.debug("HandicapIndex existe déjà - modification de score = " + handicapIndex);
    }
    handicapIndex = new calc.CalculateHandicapIndex().calc(handicapIndex, conn);
    if(handicapIndex == null){
        LOG.debug("after calculatedHandicapWHS, handicapIndex = null : " + handicapIndex);
        return handicapIndex;
    }
        LOG.debug("after calculatedHandicapWHS, handicapIndex = " + handicapIndex);
        LOG.debug("HandicapIndex before modification = " + handicapIndex);
     
      if(new UpdateHandicapIndex().update(handicapIndex, conn)){
             LOG.debug("HandicapIndex after modification = " + handicapIndex);
             LOG.debug("status execution modifyHandicapIndex = OK");
      }else{
             LOG.debug("status execution modifyHandicapIndex = NOT OK - null returned");
      }
     return handicapIndex;
 }catch (Exception e){
     String msg = "Exception in HandicapController " + methodName + e;
     LOG.debug(msg);
     showMessageFatal(msg);
     return null;
 }finally{
    //LOG.debug(" -- array = " + Arrays.deepToString(points) );
    ///    LOG.debug(NEW_LINE + Arrays.deepToString(points) );
  //  return null;
 }
} // end method
  
  
  /*
 public boolean create_old(HandicapIndex handicapIndex, final Connection conn){    
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with HandicapIndex = " + handicapIndex);
try{
       int handicapId = new create.CreateOrModifyHandicapIndex().status(handicapIndex, conn);
       
       if(handicapId == 0){ // existe pas : on le créé
           handicapIndex = new create.CreateHandicapIndex().create(handicapIndex, conn);
           LOG.debug("handicapIndex created = " + handicapIndex); //with HandicapId pour modification
           LOG.debug("key for later modify and insert HandicapWHS = " + handicapIndex.getHandicapId());
       }else{
           handicapIndex.setHandicapId(handicapId);
           LOG.debug("HandicapIndex existe déjà - modification de score = " + handicapIndex);
       }

      handicapIndex = new calc.CalculateHandicapIndex().calc(handicapIndex, conn);
      if(handicapIndex == null){
          LOG.debug("after calculatedHandicapWHS, handicapIndex = null : " + handicapIndex);
          return false;
      }else{
           LOG.debug("after calculatedHandicapWHS, handicapIndex = " + handicapIndex);
      }
         LOG.debug("HandicapIndex before modification = " + handicapIndex);
     //  boolean b = new modify.ModifyHandicapIndex().modify(handicapIndex, conn);
     
      if(new UpdateHandicapIndex().update(handicapIndex, conn)){
             LOG.debug("HandicapIndex after modification = " + handicapIndex);
             LOG.debug("status execution modifyHandicapIndex = OK");
             return true;
      }else{
             LOG.debug("status execution modifyHandicapIndex = NOT OK - null returned");
             return false;
      }
 }catch (Exception e){
     String msg = "Exception in HandicapIndexWHSController" + methodName + e;
     LOG.debug(msg);
     showMessageFatal(msg);
     return false;
 }finally{
    //LOG.debug(" -- array = " + Arrays.deepToString(points) );
///    LOG.debug(NEW_LINE + Arrays.deepToString(points) );
 }
} // end method


 public static int calcAdjustedScore (int [][] points){
        // totalise les strokes bruts par hole
     LOG.debug(" -- Start of calcAdjustedScore with holes = " + points.length);
     LOG.debug(NEW_LINE + Arrays.deepToString(points));
  int roundResult = 0;
    for (int[] point : points) {
        roundResult = roundResult + point[3]; // strokes brut
    }
     LOG.debug(" -- round Result = " + roundResult);
return roundResult;
} // end method getRoundStablefordResult
 */
 
void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
 //   LOG.debug("line 010");
    Round round = new Round();
    round.setIdround(437);
 
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class