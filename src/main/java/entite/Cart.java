package entite;

import enumeration.CartStatus;
import enumeration.eTypePayment;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           idCart;
    private int           cartPlayerId;
    private int           cartClubId;
    private LocalDateTime cartStartDate;
    private eTypePayment  cartType;
    private String        cartItemsJson;
    private double        cartTotal;
    private CartStatus    cartStatus;
    private LocalDateTime cartCreatedAt;
    private LocalDateTime cartModificationDate;

    public Cart() { }

    public int           getIdCart()          { return idCart; }
    public void          setIdCart(int v)      { this.idCart = v; }

    public int           getCartPlayerId()     { return cartPlayerId; }
    public void          setCartPlayerId(int v){ this.cartPlayerId = v; }

    public int           getCartClubId()       { return cartClubId; }
    public void          setCartClubId(int v)  { this.cartClubId = v; }

    public LocalDateTime getCartStartDate()    { return cartStartDate; }
    public void          setCartStartDate(LocalDateTime v) { this.cartStartDate = v; }

    public enumeration.eTypePayment getCartType()                    { return cartType; }
    public void                setCartType(enumeration.eTypePayment v) { this.cartType = v; }

    public String        getCartItemsJson()    { return cartItemsJson; }
    public void          setCartItemsJson(String v) { this.cartItemsJson = v; }

    public double        getCartTotal()        { return cartTotal; }
    public void          setCartTotal(double v){ this.cartTotal = v; }

    public enumeration.CartStatus getCartStatus()                    { return cartStatus; }
    public void                   setCartStatus(enumeration.CartStatus v) { this.cartStatus = v; }

    public LocalDateTime getCartCreatedAt()    { return cartCreatedAt; }
    public void          setCartCreatedAt(LocalDateTime v) { this.cartCreatedAt = v; }

    public LocalDateTime getCartModificationDate()    { return cartModificationDate; }
    public void          setCartModificationDate(LocalDateTime v) { this.cartModificationDate = v; }

    @Override
    public String toString() {
        return "Cart{idCart=" + idCart + ", playerId=" + cartPlayerId
             + ", clubId=" + cartClubId + ", startDate=" + cartStartDate
             + ", type=" + cartType + ", total=" + cartTotal + ", status=" + cartStatus + "}";
    }

} // end class
