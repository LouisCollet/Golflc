package create;

import entite.Cotisation;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;

@ApplicationScoped
public class CreatePaymentCotisation implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreatePaymentCotisation() { }

    public boolean create(final Cotisation cotisation) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with cotisation = {}", cotisation);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "payments_cotisation");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setNull(1, java.sql.Types.INTEGER); // AUTO-INCREMENT
                ps.setInt(2, cotisation.getIdclub());
                ps.setInt(3, cotisation.getIdplayer());
                ps.setTimestamp(4, Timestamp.valueOf(cotisation.getCotisationStartDate()));
                ps.setTimestamp(5, Timestamp.valueOf(cotisation.getCotisationEndDate()));
                ps.setString(6, cotisation.getPaymentReference());
                ps.setString(7, cotisation.getCommunication());
                ps.setString(8, cotisation.getItems());
                ps.setString(9, cotisation.getStatus());
                ps.setDouble(10, cotisation.getPrice());
                ps.setTimestamp(11, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Cotisation payement created = </h1>" + cotisation.getPrice();
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>ERROR payment for Cotisation : ";
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException sqle) {
            String msg;
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                msg = LCUtil.prepareMessageBean("create.cotisation.duplicate") + NEW_LINE + cotisation
                        + " player = " + cotisation.getIdplayer() + " club = " + cotisation.getIdclub();
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
