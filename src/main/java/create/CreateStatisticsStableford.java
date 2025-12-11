package create;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreateStatisticsStableford implements interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public boolean create(final Player player, final Round round, final ScoreStableford score, final Connection conn) throws SQLException{
 final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
         PreparedStatement ps = null;
   try{
            LOG.debug(" ... entering " + methodName);
            LOG.debug("with round = " + round);
            LOG.debug("with scoreStableford = " + score);
    //     int holes = round.getRoundHoles();
    //            LOG.debug("holes = " + round.getRoundHoles());
  //      int start = round.getRoundStart();
  //              LOG.debug("start = " + start);
      //   var statistics = score.getStatistics();
      //       LOG.debug(" array statistics = " + Arrays.deepToString(statistics));
    //     var sta = score.getStatisticsList();
    //         LOG.debug(" List statistics = " + sta.toString());
         int rows = new find.FindCountScore().find(conn, player, round, "rows");
   //         LOG.debug("there are : " + rows + " if > 0 we replace the previous score");
         if(rows == 0){ // insert
                String msg = "Create in statistics : this must be an error !!!";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            } 

         final String query = """
                 UPDATE score
                 SET ScoreFairway=?, ScoreGreen=?, ScorePutts=?, ScoreBunker=?, ScorePenalty=?
                 WHERE ScoreHole = ?
                   AND player_has_round_player_idplayer=?
                   AND player_has_round_round_idround=?
          """;
         ps = conn.prepareStatement(query);
 // UPDATE loop        
      for(ScoreStableford.Statistics stt : score.getStatisticsList()){
  //       LOG.debug("This is the score " + sco);
                    ps.setInt(1, stt.getFairway()); 
                    ps.setInt(2, stt.getGreen()); 
                    ps.setInt(3, stt.getPutt()); 
                    ps.setInt(4, stt.getBunker()); 
                    ps.setInt(5, stt.getPenalty()); 
              // where fields
                    ps.setInt(6, stt.getHole());
                    ps.setInt(7, player.getIdplayer());
                    ps.setInt(8, round.getIdround());

                    utils.LCUtil.logps(ps); 
                    int x = ps.executeUpdate();
                    if (x != 0) {
                        String msg = "Succesfull update Statistics, hole  = " + stt.getHole();
                          //      + " , Statistics = " + Arrays.toString(statistics[i]); // mod 10/05/2013
       //                       + " , Statistics = " + sta.get(i).toString(); // mod 28/06/2022
                        LOG.debug(msg);
      //                  LCUtil.showMessageInfo(msg);
                    } else {
                        String msg = "ERROR updateStatisticsStablerford, hole  = " + stt.getHole();
                          //      + " , Strokes = " + Arrays.toString(statistics[i]);
  //                              + " , Statistics = " + sta.get(i).toString(); // mod 28/06/2022
                        LOG.debug(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
      } //end for (loop sur List)
                 return true;
} catch (SQLException sqle) {
            String msg = "£££ SQLException in Update Statistics = " + sqle.getMessage() + " <br/>SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode() + " , localized message = " + sqle.getLocalizedMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
} catch (Exception e) {
            String msg = "£££ Exception in Insert or Update Statistics = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
 } finally {
           // DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps);
 }
    } //end method
void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
 try{
     Player player = new Player();
     player.setIdplayer(324713);
     Round round=new Round();
     round.setIdround(630);
     ScoreStableford score = new ScoreStableford();
     // à compléter ici !
     
//     [[0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [1, 3, 1, 1, 0], [0, 0, 0, 0, 0], [1, 0, 0, 0, 0], [1, 1, 4, 0, 0], [1, 0, 0, 0, 0], [1, 0, 0, 0, 0], [1, 0, 0, 0, 0], [0, 0, 0, 0, 0], [1, 0, 0, 0, 0]]
     
   //    boolean b = new create.CreateStatistics().create(player, round, score, conn);
     LOG.debug("from main, CreateStatitics = "); // + b);
 }catch (Exception e){
            String msg = "££ Exception in main CreateStatisticsStableford = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
 }finally{
            DBConnection.closeQuietly(conn, null, null, null);
 }
} // end main//    
} //end class
     //      for (int i=start-1; i<start+holes-1; i++){
                         // start=10, holes=9, de 9 à 18
                         // start=1,  holes=9, de 0 à 9
                         // start=1,  holes=18,de 0 à 18
    //         LOG.debug("index i =  = " + i);
    //                ps = conn.prepareStatement(query);
                /* updated fields
                    ps.setInt(1, statistics[i][0]);  //scoreFairway
                    ps.setInt(2, statistics[i][1]);  //scoreGreen
                    ps.setInt(3, statistics[i][2]);  //scorePutts
                    ps.setInt(4, statistics[i][3]);  //scoreBunker
                    ps.setInt(5, statistics[i][4]);  //scorePenalty
                
          // mod 28-06-2022 updated fields
                    ps.setInt(1, sta.get(i).getFairway());  //scoreFairway
                    ps.setInt(2, sta.get(i).getGreen());  //scoreGreen
                    ps.setInt(3, sta.get(i).getPutt());  //scorePutts
                    ps.setInt(4, sta.get(i).getBunker());  //scoreBunker
                    ps.setInt(5, sta.get(i).getPenalty());  //scorePenalty
              // where fields
                    ps.setInt(6, i + 1);   // holeNumber, = 0 , ou i + 1
                    ps.setInt(7, player.getIdplayer());
                    ps.setInt(8, round.getIdround());
                      */