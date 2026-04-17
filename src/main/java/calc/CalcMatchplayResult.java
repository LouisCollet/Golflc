package calc;

import entite.composite.EMatchplayResult;
import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named // nécessaire pour show_participants_matchplay.xhtml
@ApplicationScoped
public class CalcMatchplayResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private find.FindMatchplayResult findMatchplayResult;

    private List<EMatchplayResult> finalResult = null;

    public CalcMatchplayResult() { }

    public List<EMatchplayResult> calc(final Player player1, final Player player2, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with Round   = " + round.getIdround());
        LOG.debug(methodName + " - with Player1 = " + player1.getIdplayer());
        LOG.debug(methodName + " - with Player2 = " + player2.getIdplayer());

        if (finalResult != null) {
            LOG.debug(methodName + " - returning cached finalResult");
            return finalResult;
        }

        try {
            var p1 = findMatchplayResult.find(player1, round);
            var p2 = findMatchplayResult.find(player2, round);
            finalResult = new ArrayList<>();
            for (int i = 0; i < p1.size(); i++) {
                EMatchplayResult result = new EMatchplayResult(p1.get(i), p2.get(i));
                if (result.getPlayer1().getStrokes() > result.getPlayer2().getStrokes()) {
                    result.getPlayer1().setResult(0);
                    result.getPlayer2().setResult(1);
                }
                if (result.getPlayer1().getStrokes() < result.getPlayer2().getStrokes()) {
                    result.getPlayer1().setResult(1);
                    result.getPlayer2().setResult(0);
                }
                if (result.getPlayer1().getStrokes().equals(result.getPlayer2().getStrokes())) {
                    result.getPlayer1().setResult(0);
                    result.getPlayer2().setResult(0);
                }
                finalResult.add(result);
            } // end for
            return finalResult;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public int getTotalPlayer1Score() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (finalResult == null) return 0;
        return finalResult.stream()
                .mapToInt(o -> o.getPlayer1().getResult() == null ? 0 : o.getPlayer1().getResult())
                .sum();
    } // end method

    public int getTotalPlayer2Score() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (finalResult == null) return 0;
        return finalResult.stream()
                .mapToInt(o -> o.getPlayer2().getResult() == null ? 0 : o.getPlayer2().getResult())
                .sum();
    } // end method

    public List<EMatchplayResult> getFinalResult()                              { return finalResult; }
    public void                   setFinalResult(List<EMatchplayResult> result) { this.finalResult = result; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.finalResult = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // Player player1 = new Player();
        // player1.setIdplayer(324713);
        // Player player2 = new Player();
        // player2.setIdplayer(456781);
        // Round round = new Round();
        // round.setIdround(694);
        // var v = calc(player1, player2, round);
        // LOG.debug("result in main = " + v);
        LOG.debug("from main, CalcMatchplayResult = ");
    } // end main
    */

} // end class
