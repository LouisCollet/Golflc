package create;

import entite.Greenfee;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentGreenfee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public CreatePaymentGreenfee() { }

    public boolean create(final Player player, final Greenfee greenfee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Greenfee  = " + greenfee);
        LOG.debug("for Player  = " + player);

        try (Connection conn = dataSource.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_greenfee");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
                ps.setInt(2, greenfee.getIdclub());
                ps.setInt(3, player.getIdplayer());
                ps.setInt(4, greenfee.getIdround());
                ps.setTimestamp(5, Timestamp.valueOf(greenfee.getRoundDate()));
                ps.setString(6, greenfee.getPaymentReference());
                ps.setString(7, greenfee.getCommunication());
                ps.setString(8, greenfee.getItems());
                ps.setString(9, greenfee.getStatus());
                ps.setDouble(10, greenfee.getPrice());
                ps.setString(11, greenfee.getCurrency());
                ps.setTimestamp(12, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Payment Greenfee done for = " + greenfee.getPrice() + " for round = " + greenfee.getIdround();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/>ERROR insert for Greenfee : ";
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
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
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
