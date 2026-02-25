
package lists;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ShowScoreList implements interfaces.Log, interfaces.GolfInterface{

    public String show(final Player player, final Club club,
            final Course course, final Round round,
            final Inscription inscription, //final PlayerHasRound phr, mod 31-03-2019
            final Connection conn) throws SQLException {
        try {
            LOG.debug("starting ShowScorecard with : ");
            LOG.debug(" with player = " + player.toString());
            LOG.debug(" with club = " + club.toString());
            LOG.debug(" with course = " + course.toString());
            LOG.debug(" with round = " + round.toString());
   /*         
          String msg = "<br/> Successful selected scorecard = " + club.getClubName()
                    + "<br/> course = " + course.getCourseName()
                    + "<br/> date   = " + round.getRoundDate().format(ZDF_TIME)
                 //   LOG.debug("RoundDate = " + round.getRoundDat)e();
                    + "<br/> round  = " + round.getIdround()
                    + "<br/> game   = " + round.getRoundGame()
                    + "<br/> player = " + player.getIdplayer()
                    + "<br/> TotalPar = " + course.getCoursePar();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
*/

    //        String[] s1 = new CalculateController().calculate(player, round, course, conn); // mod 27/05/2017
            // tester array pour errors
   //         if (s1[0].equals("ERROR")) {
   //             String msg = " -- Fatal error in " + s1[1] + Arrays.deepToString(s1);
   //             LOG.error(msg);
   //             LCUtil.showMessageFatal(msg);
   //             throw new Exception(" -- Fatal error in Controller number 1 " + s1[1]); //stoppe exécution, génial
      //      }
            LOG.debug("ending scorecard with round  = " + round.getIdround());
            LOG.debug("ending scorecard with player = " + player.getIdplayer());
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
    
    public String show_empty(final Player player, final Club club,
            final Course course, final Round round,
            final Inscription phr, // was PlayerhasRound
            final Connection conn) throws SQLException{
        /// à modifiere
        try {
            LOG.debug("starting show_scorecard with : ");
            LOG.debug("idPlayer = " + player.getIdplayer());
            LOG.debug("idClub = " + club.getIdclub());
            LOG.debug("ClubName = " + club.getClubName());
            LOG.debug("idCourse = " + course.getIdcourse());
            LOG.debug("CourseName = " + course.getCourseName());
            LOG.debug("IdRound = " + round.getIdround());
            LOG.debug("RoundGame = " + round.getRoundGame());
            LOG.debug("RoundHoles = " + round.getRoundHoles());
        //    LOG.debug("RoundDate = " + SDF.format(round.getRoundDate()) );
 ////           LOG.debug("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
            LOG.debug("RoundName = " + round.getRoundName());
            LOG.debug("RoundQualifying = " + round.getRoundQualifying() );

            String msg = "<br/> Successful selected scorecard = " + club.getClubName()
                    + "<br/> course = " + course.getCourseName()
     ////               + "<br/> date   = " + round.getRoundDate().format(ZDF_TIME)
                 //   LOG.debug("RoundDate = " + round.getRoundDat)e();
                    + "<br/> round  = " + round.getIdround()
                    + "<br/> game   = " + round.getRoundGame()
                    + "<br/> player = " + player.getIdplayer()
                    + "<br/> TotalPar = " + course.getCoursePar();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);

      //      String[] s1 = CalculateController.calculate(player, round, course, phr, conn); // mod 19/01/2014
  //          CalculateController cc = new CalculateController();
     //       String[] s1 = cc.calculate(player, round, course, conn); // mod 27/05/2017
            
            // tester array pour errors
     //       if (s1[0].equals("ERROR")) {
     //           msg = " -- Fatal error in " + s1[1] + Arrays.deepToString(s1);
     //           LOG.error(msg);
    //            LCUtil.showMessageFatal(msg);
    //            throw new Exception(" -- Fatal error in Controller number 1 " + s1[1]); //stoppe exécution, génial
    //        }
            LOG.debug("ending scorecard with round  = " + round.getIdround());
            LOG.debug("ending scorecard with player = " + player.getIdplayer());
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
    
  void main() throws SQLException, Exception{
   
   Connection conn = new DBConnection().getConnection();
   Player player = new Player();
   
 Club club = new Club();
 club.setIdclub(104);
//round.setIdround(206);

 //  String s = new ShowScoreList().show(player, club, course,);
//      LOG.debug(" club = " + s.toString());

DBConnection.closeQuietly(conn, null, null, null);

}// end main
    
    
} //end class
