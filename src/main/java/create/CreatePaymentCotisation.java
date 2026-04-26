package create;

import entite.Cotisation;
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
public class CreatePaymentCotisation implements Serializable {

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
                sql.preparedstatement.psCreatePaymentCotisation.psMapCreate(ps, cotisation);
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("cotisation payment created = {}", cotisation);
                    return true;
                } else {
                    LOG.error("insert payments_cotisation returned 0 rows");
                    LCUtil.showMessageFatal(LCUtil.prepareMessageBean("create.cotisation.error"));
                    return false;
                }
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                String msg = LCUtil.prepareMessageBean("create.cotisation.duplicate");
                LOG.error("duplicate cotisation player={} club={}", cotisation.getIdplayer(), cotisation.getIdclub());
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
