package delete;

import entite.Player;
import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class DeletePaymentGreenfee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeletePaymentGreenfee() { }

    public boolean delete(final Player player, final Round round) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                DELETE
                FROM payments_greenfee
                WHERE GreenfeeIdPlayer = ?
                AND GreenfeeIdRound = ?
                """;

        int rowDeleted = dao.execute(query, player.getIdplayer(), round.getIdround());
        if (rowDeleted != 0) {
            String msg = "PaymentGreenfee deleted ! ";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        } else {
            String msg = "PaymentGreenfee not found !";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(Player, Round)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324715);
        Round round = new Round();
        round.setIdround(758);
        boolean b = new DeletePaymentGreenfee().delete(player, round);
        LOG.debug("from main - resultat deleted PaymentGreenfee = {}", b);
    } // end main
    */

} // end class
