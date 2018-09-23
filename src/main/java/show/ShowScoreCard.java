
package show;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.PlayerHasRound;
import entite.Round;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import lc.golfnew.CalculateController;
import utils.LCUtil;

public class ShowScoreCard implements interfaces.Log, interfaces.GolfInterface
{
    public String show_scorecard(final Player player, final Club club,
            final Course course, final Round round, final PlayerHasRound phr, final Connection conn) throws SQLException
    {
        try {
            LOG.info("starting show_scorecard with : ");
            LOG.info("idPlayer = " + player.getIdplayer());
            LOG.info("idClub = " + club.getIdclub());
            LOG.info("ClubName = " + club.getClubName());
            LOG.info("idCourse = " + course.getIdcourse());
            LOG.info("CourseName = " + course.getCourseName());
            LOG.info("IdRound = " + round.getIdround());
            LOG.info("RoundGame = " + round.getRoundGame());
            LOG.info("RoundHoles = " + round.getRoundHoles());
        //    LOG.info("RoundDate = " + SDF.format(round.getRoundDate()) );
            LOG.info("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
            LOG.info("RoundCompetition = " + round.getRoundCompetition());
            LOG.info("RoundQualifying = " + round.getRoundQualifying() );

            String msg = "<br/> Successful selected scorecard = " + club.getClubName()
                    + "<br/> course = " + course.getCourseName()
                    + "<br/> date   = " + round.getRoundDate().format(ZDF_TIME)
                 //   LOG.info("RoundDate = " + round.getRoundDat)e();
                    + "<br/> round  = " + round.getIdround()
                    + "<br/> game   = " + round.getRoundGame()
                    + "<br/> player = " + player.getIdplayer()
                    + "<br/> TotalPar = " + course.getCoursePar();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

      //      String[] s1 = CalculateController.calculate(player, round, course, phr, conn); // mod 19/01/2014
      CalculateController cc = new  CalculateController();
            String[] s1 = cc.calculate(player, round, course, conn); // mod 27/05/2017
            
            // tester array pour errors
            if (s1[0].equals("ERROR")) {
                msg = " -- Fatal error in " + s1[1] + Arrays.deepToString(s1);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new Exception(" -- Fatal error in Controller number 1 " + s1[1]); //stoppe exécution, génial
            }
            LOG.info("ending scorecard with round  = " + round.getIdround());
            LOG.info("ending scorecard with player = " + player.getIdplayer());
            String idround = Integer.toString(round.getIdround());
            return "show_scorecard.xhtml?faces-redirect=true&idround=" + idround;

        } catch (Exception e) {
            String msg = "£££ Exception in Show_scorecard = " + e.getMessage()
                    + " round = " + round.getIdround()
                    + " date  = " + round.getRoundDate();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        }
       
    } //end method scorecard
    
    public static String show_scorecard_empty(final Player player, final Club club,
            final Course course, final Round round, final PlayerHasRound phr, final Connection conn) throws SQLException
    {
        /// à modifiere
        try {
            LOG.info("starting show_scorecard with : ");
            LOG.info("idPlayer = " + player.getIdplayer());
            LOG.info("idClub = " + club.getIdclub());
            LOG.info("ClubName = " + club.getClubName());
            LOG.info("idCourse = " + course.getIdcourse());
            LOG.info("CourseName = " + course.getCourseName());
            LOG.info("IdRound = " + round.getIdround());
            LOG.info("RoundGame = " + round.getRoundGame());
            LOG.info("RoundHoles = " + round.getRoundHoles());
        //    LOG.info("RoundDate = " + SDF.format(round.getRoundDate()) );
 ////           LOG.info("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
            LOG.info("RoundCompetition = " + round.getRoundCompetition());
            LOG.info("RoundQualifying = " + round.getRoundQualifying() );

            String msg = "<br/> Successful selected scorecard = " + club.getClubName()
                    + "<br/> course = " + course.getCourseName()
     ////               + "<br/> date   = " + round.getRoundDate().format(ZDF_TIME)
                 //   LOG.info("RoundDate = " + round.getRoundDat)e();
                    + "<br/> round  = " + round.getIdround()
                    + "<br/> game   = " + round.getRoundGame()
                    + "<br/> player = " + player.getIdplayer()
                    + "<br/> TotalPar = " + course.getCoursePar();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

      //      String[] s1 = CalculateController.calculate(player, round, course, phr, conn); // mod 19/01/2014
            CalculateController cc = new CalculateController();
            String[] s1 = cc.calculate(player, round, course, conn); // mod 27/05/2017
            
            // tester array pour errors
            if (s1[0].equals("ERROR")) {
                msg = " -- Fatal error in " + s1[1] + Arrays.deepToString(s1);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new Exception(" -- Fatal error in Controller number 1 " + s1[1]); //stoppe exécution, génial
            }
            LOG.info("ending scorecard with round  = " + round.getIdround());
            LOG.info("ending scorecard with player = " + player.getIdplayer());
            String idround = Integer.toString(round.getIdround());
            return "show_scorecard.xhtml?faces-redirect=true&idround=" + idround;

        } catch (Exception e) {
            String msg = "£££ Exception in Show_scorecard_empty = " + e.getMessage()
                    + " round = " + round.getIdround()
                    + " date  = " + round.getRoundDate();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        }
       
    } //end method scorecard
    
    
    
    
} //end class
