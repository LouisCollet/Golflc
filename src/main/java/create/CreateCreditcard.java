package create;

import entite.Creditcard;
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
public class CreateCreditcard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateCreditcard() { }

    public boolean create(final Creditcard creditcard) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("player  = {}", creditcard.getCreditCardIdPlayer());
        LOG.debug("creditcard  = {}", creditcard);

        try (Connection conn = dao.getConnection()) {
      //      final String query = LCUtil.generateInsertQuery(conn, "creditcard");
            try (PreparedStatement ps = conn.prepareStatement(LCUtil.generateInsertQuery(conn, "creditcard"))) {
                sql.preparedstatement.psCreateCreditcard.psMapCreate(ps, creditcard);
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("creditcard created for player={}", creditcard.getCreditCardIdPlayer());
                    return true;
                } else {
                    LOG.error("insert creditcard returned 0 rows for player={}", creditcard.getCreditCardIdPlayer());
                    LCUtil.showMessageFatal(LCUtil.prepareMessageBean("creditcard.error"));
                    return false;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
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
