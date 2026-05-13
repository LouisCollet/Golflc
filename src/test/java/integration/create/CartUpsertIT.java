package integration.create;

import create.CreateCart;
import entite.Cart;
import integration.support.AbstractDaoIT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class CartUpsertIT extends AbstractDaoIT {

    private static final int TEST_PLAYER_ID = 324714;

    @Test
    void upsertCart_realDB_insertOrUpdate() throws Exception {

        CreateCart createCart = new CreateCart();
        injectDao(createCart);

        dao.execute("""
            DELETE FROM cart
            WHERE cartPlayerId = ?
            """, TEST_PLAYER_ID);

        Cart cart = new Cart();
        cart.setCartPlayerId(TEST_PLAYER_ID);
        cart.setCartClubId(101);
        cart.setCartType(enumeration.eTypePayment.COTISATION);
        cart.setCartItemsJson("""
            [{"item":"cotisation 101"}]
            """);
        cart.setCartTotal(49.90);

        createCart.upsert(cart);

        Integer count = dao.querySingle(
                """
                SELECT COUNT(*)
                FROM cart
                WHERE cartPlayerId = ?
                """,
                rs -> rs.getInt(1),
                TEST_PLAYER_ID
        );

        assertEquals(1, count);
    } // end method

} // end class
