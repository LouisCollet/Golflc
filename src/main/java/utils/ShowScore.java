package utils;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;

@ApplicationScoped
public class ShowScore implements Serializable {

    private static final long serialVersionUID = 1L;

    public ShowScore() { }

    public String show(final Player player, final Club club, final Course course,
            final Round round, final Inscription inscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with player = " + player);
        LOG.debug("with club = " + club);
        LOG.debug("with course = " + course);
        LOG.debug("with round = " + round);
        try {
            LOG.debug(methodName + " - ending scorecard with round = " + round.getIdround());
            LOG.debug(methodName + " - ending scorecard with player = " + player.getIdplayer());
            String idround = Integer.toString(round.getIdround());
            return "show_scorecard.xhtml?faces-redirect=true&idround=" + idround;
        } catch (Exception e) {
            String msg = "Exception in " + methodName + " = " + e.getMessage()
                    + " round = " + round.getIdround()
                    + " date  = " + round.getRoundDate();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
        }
    } // end method

    public String show_empty(final Player player, final Club club, final Course course,
            final Round round, final Inscription inscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("idPlayer = " + player.getIdplayer());
        LOG.debug("idClub = " + club.getIdclub());
        LOG.debug("idCourse = " + course.getIdcourse());
        LOG.debug("IdRound = " + round.getIdround());
        LOG.debug("RoundGame = " + round.getRoundGame());
        LOG.debug("RoundHoles = " + round.getRoundHoles());
        LOG.debug("RoundName = " + round.getRoundName());
        LOG.debug("RoundQualifying = " + round.getRoundQualifying());
        try {
            String msg = "<br/> Successful selected scorecard = " + club.getClubName()
                    + "<br/> course = " + course.getCourseName()
                    + "<br/> round  = " + round.getIdround()
                    + "<br/> game   = " + round.getRoundGame()
                    + "<br/> player = " + player.getIdplayer()
                    + "<br/> TotalPar = " + course.getCoursePar();
            LOG.debug(msg);
            utils.LCUtil.showMessageInfo(msg);
            LOG.debug(methodName + " - ending scorecard with round = " + round.getIdround());
            LOG.debug(methodName + " - ending scorecard with player = " + player.getIdplayer());
            String idround = Integer.toString(round.getIdround());
            return "show_scorecard.xhtml?faces-redirect=true&idround=" + idround;
        } catch (Exception e) {
            String msg = "Exception in " + methodName + " = " + e.getMessage()
                    + " round = " + round.getIdround()
                    + " date  = " + round.getRoundDate();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        Club club = new Club();
        club.setIdclub(104);
        // String s = show(player, club, course, round, inscription);
    } // end main
    */

} // end class
