package rowmappers;

import entite.Cart;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartRowMapper extends AbstractRowMapper<Cart> {

    @Override
    public Cart map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            Cart c = new Cart();
            c.setIdCart(getInteger(rs, "idCart"));
            c.setCartPlayerId(getInteger(rs, "cartPlayerId"));
            c.setCartClubId(getInteger(rs, "cartClubId"));
            c.setCartStartDate(getLocalDateTime(rs, "cartStartDate"));
            c.setCartType(enumeration.eTypePayment.valueOf(getString(rs, "cartType")));
            c.setCartItemsJson(getString(rs, "cartItemsJson"));
            c.setCartTotal(getDouble(rs, "cartTotal"));
            c.setCartStatus(enumeration.CartStatus.valueOf(getString(rs, "cartStatus")));
            c.setCartCreatedAt(getLocalDateTime(rs, "cartCreatedAt"));
            c.setCartModificationDate(getLocalDateTime(rs, "cartModificationDate"));
            return c;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end map

} // end class
