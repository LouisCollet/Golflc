package create;

import entite.Cart;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class CreateCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateCart() { }

    public void upsert(final Cart cart) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("type={} startDate={} total={}", cart.getCartType().name(), cart.getCartStartDate(), cart.getCartTotal());

        final String query = """
            INSERT INTO cart (cartPlayerId, cartClubId, cartStartDate, cartType, cartItemsJson, cartTotal)
            VALUES (?, ?, ?, ?, ?, ?)
            AS vals
            ON DUPLICATE KEY UPDATE
                cartItemsJson = vals.cartItemsJson,
                cartTotal     = vals.cartTotal,
                cartStatus    = 'PENDING',
                cartModificationDate = CURRENT_TIMESTAMP
            """;

        try {
            dao.execute(query,
                cart.getCartPlayerId(),
                cart.getCartClubId(),
                java.sql.Timestamp.valueOf(cart.getCartStartDate()),
                cart.getCartType().name(),
                cart.getCartItemsJson(),
                cart.getCartTotal());
            LOG.debug("cart upserted type={}", cart.getCartType().name());
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

} // end class
