package update;

import entite.Creditcard;
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
import utils.LCUtil;

/**
 * Service de modification de Creditcard
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class ModifyCreditcard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    public ModifyCreditcard() { }

    public boolean modify(Creditcard creditcard) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Creditcard = " + creditcard.toString());

        final String query = """
                UPDATE creditcard
                SET CreditcardHolder = ?,
                    CreditcardNumber = ?,
                    CreditcardExpirationDate = ?,
                    CreditcardType = ?,
                    CreditcardVerificationCode = ?
                WHERE
                    CreditcardIdPlayer=?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, creditcard.getCreditcardHolder());
            ps.setString(2, creditcard.getCreditcardNumber());
            ps.setTimestamp(3, Timestamp.valueOf(creditcard.getCreditCardExpirationDateLdt()));
            ps.setString(4, creditcard.getCreditcardType());
            ps.setShort(5, creditcard.getCreditcardVerificationCode());
            ps.setInt(6, creditcard.getCreditCardIdPlayer());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            LOG.debug("rows = " + row);
            if (row != 0) {
                String msg = LCUtil.prepareMessageBean("creditcard.registered") + NEW_LINE + creditcard;
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "NOT NOT Successful update, row = 0 player = " + creditcard.getCreditcardHolder();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
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
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
