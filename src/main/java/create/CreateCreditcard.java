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
import java.sql.Timestamp;
import java.time.Instant;
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
                ps.setNull(1, java.sql.Types.INTEGER);
                ps.setInt(2, creditcard.getCreditCardIdPlayer());
                ps.setString(3, creditcard.getCreditcardHolder());
                ps.setString(4, creditcard.getCreditcardNumber());
                ps.setTimestamp(5, Timestamp.valueOf(creditcard.getCreditCardExpirationDateLdt()));
                ps.setString(6, creditcard.getCreditcardType());
                ps.setShort(7, creditcard.getCreditcardVerificationCode());
                ps.setTimestamp(8, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = "Creditcard created for player = " + creditcard.getCreditCardIdPlayer();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/><br/>ERROR insert for creditcard : " + creditcard.getCreditCardIdPlayer();
                    LOG.debug(msg);
                    LCUtil.showMessageFatal(msg);
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
