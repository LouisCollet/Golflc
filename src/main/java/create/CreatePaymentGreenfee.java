package create;

import entite.Greenfee;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentGreenfee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreatePaymentGreenfee() { }

    public boolean create(final Player player, final Greenfee greenfee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Greenfee  = {}", greenfee);
        LOG.debug("for Player  = {}", player);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_greenfee");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreatePaymentGreenfee.psMapCreate(ps, player, greenfee);
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("greenfee payment created price={} round={}", greenfee.getPrice(), greenfee.getIdround());
                    return true;
                } else {
                    LOG.error("insert payments_greenfee returned 0 rows player={} round={}", player.getIdplayer(), greenfee.getIdround());
                    LCUtil.showMessageFatal(LCUtil.prepareMessageBean("greenfee.error"));
                    return false;
                }
            }
        } catch (SQLException sqle) {
            String msg;
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                msg = LCUtil.prepareMessageBean("create.greenfee.duplicate")
                        + "player = " + player.getIdplayer() + " club = " + greenfee.getIdclub();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
