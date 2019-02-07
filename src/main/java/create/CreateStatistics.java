
package create;

import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class CreateStatistics implements interfaces.Log{
    
    public boolean createStatistics(final Player player, final Round round,
            final String[][] sc2, final Connection conn) throws SQLException{
  //      Connection conn = null;
        PreparedStatement ps = null;
   try{
            LOG.info(" ... entering createStatistics");
            LOG.info("Round ID = " + round.getIdround());
            int holes = round.getRoundHoles();
                LOG.info("holes = " + round.getRoundHoles());
            int start = round.getRoundStart();
                LOG.info("start = " + start);

    //        sc2 = score.getStatistics();
            LOG.info("scores array statistics : sc2 = " + Arrays.deepToString(sc2));
  //          conn = DBConnection.getConnection();
       //     int rows = GolfMySQL.getCountScore(conn, player.getIdplayer(), round.getIdround(), "rows");
            find.FindCountScore sciu = new find.FindCountScore();
            int rows = sciu.getCountScore(conn, player, round, "rows");
            LOG.info("there are : " + rows + " if > 0 we replace the previous score");
            if (rows == 0) // insert
            {
                LOG.info("this must be an error !!!");
            } else { // UPDATE loop

                final String query
                        = "UPDATE score"
                        + " SET ScoreFairway=?, ScoreGreen=?, ScorePutts=?, ScoreBunker=?, ScorePenalty=? ,ScoreModificationDate=?"
                        + " WHERE ScoreHole = ?"
                        + "  AND player_has_round_player_idplayer=?"
                        + "  AND player_has_round_round_idround=?";
               // for (int i = 0; (i < 18) && (sc2[i] != null); i++) // voir explication dans insert
                for (int i=start-1; i<start+holes-1; i++) // new 4/12/2013
                         // start=10, holes=9, de 9 à 18
                         // start=1,  holes=9, de 0 à 9
                         // start=1,  holes=18,de 0 à 18
                {
                    LOG.info("index i =  = " + i);
                    ps = conn.prepareStatement(query);
                    // updated fields
                    ps.setInt(1, Integer.parseInt(sc2[i][0]));  //scoreFairway
                    ps.setInt(2, Integer.parseInt(sc2[i][1]));  //scoreGreen
                    ps.setInt(3, Integer.parseInt(sc2[i][2]));  //scorePutts
                    ps.setInt(4, Integer.parseInt(sc2[i][3]));  //scoreBunker
                    ps.setInt(5, Integer.parseInt(sc2[i][4]));  //scorePenalty
                    //  LOG.info("score Fairway updated = " + sc2[i][0]);
                    ps.setTimestamp(6, LCUtil.getCurrentTimeStamp());
                    // where fields
                    ps.setInt(7, i + 1);   // holeNumber, = 0 , ou i + 1
                    ps.setInt(8, player.getIdplayer());
                    ps.setInt(9, round.getIdround());
                     //    String p = ps.toString();
                    utils.LCUtil.logps(ps); 
                    int x = ps.executeUpdate();
                    if (x != 0) {
                        String msg = "Succesfull update Statistics, hole  = " + (i + 1)
                                + " , Statistics = " + Arrays.deepToString(sc2[i]); // mod 10/05/2013
                        LOG.info(msg);
                   //     LCUtil.showMessageInfo(msg);
                    } else {
                        String msg = "NOT NOT Successful crateStatistics, hole  = " + (i + 1)
                                + " , Strokes = " + Arrays.deepToString(sc2[i]); // sc.get(i);
                        LOG.info(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
               } //end for (loop sur 9 ou 18 trous)
                 return true;
            } // end UPDATE
//ici ??
//            setNextScorecard(true); // affiche le bouton next(Scorecard) bas ecran à droite
        } catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert or Update Statistics = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
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
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
        return false;

    } //end method
} //end class