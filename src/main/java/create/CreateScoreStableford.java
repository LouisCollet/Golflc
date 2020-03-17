
package create;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import lc.golfnew.CourseController;
import utils.DBConnection;
import utils.LCUtil;

public class CreateScoreStableford implements interfaces.Log{
  public  boolean createModifyScore(final ScoreStableford score, final Round round, final Player player,
            final Connection conn) throws SQLException{
        PreparedStatement ps = null;
 try {
                LOG.info(" ... entering createScore() ...");
                LOG.info("Score = " + score.toString());
                LOG.info("Round  = " + round.toString());
                LOG.info("Round Game = " + round.getRoundGame());
                LOG.info("Player  = " + player.toString());
            String[] sc1 = score.getHoles();
                LOG.info("Scores Bruts - Array sc1 = " + Arrays.deepToString(sc1));
    //        LOG.info("Scores Bruts - Array sc2 = " + Arrays.deepToString(sc2));
            int holes = round.getRoundHoles();
                LOG.info("Holes = " + round.getRoundHoles());
            int start = round.getRoundStart();
                LOG.info("Start = " + start);

          //  int rows = GolfMySQL.getCountScore(conn, player.getIdplayer(), round.getIdround(), "rows");
            find.FindCountScore sciu = new find.FindCountScore();
            int rows = sciu.getCountScore(conn, player, round, "rows");
                LOG.info("there are existing rows : " + rows + " if > 0 we replace the previous score");
    if (rows == 0) // insert
            {
                LOG.info("we are inserting");
                final String query = LCUtil.generateInsertQuery(conn, "score"); // new 15/11/2012
                //  String query = "INSERT INTO score VALUES (?,?,?,?,?)";
                ps = conn.prepareStatement(query);
      // insérer dans l'ordre de la database : 1 = first db field
// INSERT loop
              //  for (int i = 0; (i < 18) && (sc1[i] != null); i++) // was i<18 // mod 10/05/2013 
                for (int i=start-1; i<start+holes-1; i++) // new 4/12/2013
                         // start=10, holes=9, de 9 à 18
                         // start=1,  holes=9, de 0 à 9
                         // start=1,  holes=18,de 0 à 18
                
                // explication : si tour à 9 tours , start = 1, alors 10à18 sont null
                // si start = 10 ,alors 1à9 sont null
                {
                    ps.setNull(1, java.sql.Types.INTEGER);// auto-increment
                    // ps.setInt(2,Integer.parseInt(sc1[i+1]) );   // holeNumber, mod 17/11/2013
                    ps.setInt(2, i + 1);   // holeNumber, mod 23/11/2013
                    ps.setInt(3, Integer.parseInt(sc1[i]));  //ici scoreStroke //
                    // LOG.info("score strokes inserted = " + sc.get(i));
                        LOG.info("score BRUT inserted = " + sc1[i]);

                    ps.setInt(4, 0); // ScoreExtraStroke, introduit à zéro, complété CalculateController, GolMySQL.setScore
                    ps.setInt(5, 0); // ScorePoints, introduit à zéro,id ici ,,
                    ps.setInt(6, 0); // ScorePar, introduit à zéro, id
                    ps.setInt(7, 0); // ScoreStrokeInsdex, introduit à zéro, id
                    // new 27/10/2013
                    ps.setInt(8, 0); // ScoreFairway, introduit à 0
                    ps.setInt(9, 0); // ScoreGreen, introduit à zéro
                    ps.setInt(10, 0); // ScorePutts, introduit à zéro
                    ps.setInt(11, 0); // ScoreBunker, introduit à zéro
                    ps.setInt(12, 0); // ScorePenalty, introduit à zéro
                    ps.setInt(13, player.getIdplayer());
                    ps.setInt(14, round.getIdround());
                    ps.setTimestamp(15, Timestamp.from(Instant.now()));
                         //    String p = ps.toString();
                        utils.LCUtil.logps(ps);
                    int row = ps.executeUpdate(); // write into database
                    if (row != 0) {
                        int key = LCUtil.generatedKey(conn);
                        score.setIdscore(key);
   //                     setShowButtonCreateScore(false);  // n'affiche plus le bouton bas ecran
   //                     setShowButtonCreateStatistics(true);  // affiche plus le bouton bas ecran
                        
                       
                    } else {
                        String msg = "<br/>NOT NOT insert for scoreId = " + score.getIdscore()
                                + " , points = " + sc1[i] 
                                + " , round = " + round.getIdround();
               //     + " , hole = " + score.getScoreHole()
                        //     + " , Strokes = "  + score.getScoreStroke();
                        LOG.info(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }
    //                String msg = "<br/>Successful insert scores for player = "
    //                        + player.getIdplayer() + " /" + player.getPlayerLastName()
    //                        + " , round = " + round.getIdround();
    //                    LOG.info(msg);
    //                    LCUtil.showMessageInfo(msg);
                } // end for
                String msg = "<br/>Successful insert score = " 
                           + " , round = " + round.getIdround()
                           + " , player = " + player.getPlayerLastName()
                           + " , player id= " + player.getIdplayer();
                        LOG.info(msg);
                        LCUtil.showMessageInfo(msg);
// end insert
    }else{ // UPDATE loop
            LOG.info("we are updating");
                    LOG.info("... entering UPDATE loop with array sc1 = " + Arrays.deepToString(sc1));
                final String query
                        = "UPDATE score"
                        + " SET ScoreStroke=?, ScoreModificationDate=?"
                        + " WHERE ScoreHole = ?"
                        + "  AND player_has_round_player_idplayer=?"
                        + "  AND player_has_round_round_idround=?";
            //    for (int i = 0; (i < 18) && (sc1[i] != null); i++) // voir explication dans insert
              //   for (int i=start-1; i<18; i++) // new 3/12/2013
            for (int i=start-1; i<start+holes-1; i++){ // new 4/12/2013
                         // start=10, holes=9, de 9 à 18
                         // start=1,  holes=9, de 0 à 9
                         // start=1,  holes=18,de 0 à 18
                    ps = conn.prepareStatement(query);
                    // updated fields
                    ps.setInt(1, Integer.parseInt(sc1[i]));  //ici scoreStroke //
                    LOG.info("Update score : index i = " + i + " strokes updated = " + sc1[i]);
                    ps.setTimestamp(2, Timestamp.from(Instant.now()));
   // where fields
                    //  ps.setInt(3,Integer.parseInt(sc1[i+1]) );   // holeNumber, mod 17/11/2013
                    ps.setInt(3, i + 1);   // holeNumber, mod 23/11/2013
                    ps.setInt(4, player.getIdplayer());
                    ps.setInt(5, round.getIdround());
                     //    String p = ps.toString();
                        utils.LCUtil.logps(ps);
                    int x = ps.executeUpdate();
                    if (x != 0) {
   //                     setShowButtonCreateScore(false);  // n'affiche plus le bouton bas ecran
                        String msg = "Successful update, hole  = " + (i + 1)
                                + " , Strokes = " + sc1[i]; // mod 10/05/2013
                        LOG.info(msg);
                      //  LCUtil.showMessageInfo(msg);
                    }else{
                        String msg = "NOT NOT Successful update, hole  = " + (i + 1)
                                + " , Strokes = " + sc1[i]; // sc.get(i);
                        LOG.info(msg);
                        LCUtil.showMessageFatal(msg);
                        return false;
                    }

                } //end for (loop sur 9 ou 18 trous)
            } // end UPDATE
                        String msg = "<br/>Successful update scores for player = "
                                + player.getIdplayer() + " /" + player.getPlayerLastName()
                                + " , round = " + round.getIdround();
                        CourseController.setShowButtonCreateStatistics(true);  // affiche le bouton bas ecran
                        score.setScoreCardOK(true); // new 17/7/2017 pour permettre affichage scorecard dans ??
                        LOG.info(msg);
                        LCUtil.showMessageInfo(msg);

           return true;
//ici ??
 //          setNextScorecard(true); // affiche le bouton next(Scorecard) bas ecran à droite
        } catch (SQLException sqle) {
            String msg = "£££ SQLException in Insert or Update score = " + sqle.getMessage() + " , SQLState = "
                    + sqle.getSQLState() + " , ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in Insert or Update score = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
           // DBConnection.closeQuietly(conn, null, null, ps);
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/5014
        }
    } //end method
} //end class