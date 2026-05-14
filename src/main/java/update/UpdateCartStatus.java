package update;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class UpdateCartStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateCartStatus() { }

    public void setCompleted(int idCart) throws SQLException {
        setStatusById(idCart, enumeration.CartStatus.COMPLETED);
    } // end method

    public void setCanceled(int idCart) throws SQLException {
        setStatusById(idCart, enumeration.CartStatus.CANCELED);
    } // end method

    public void setCompletedByPlayerClubType(int playerId, int clubId, String type) throws SQLException {
        setStatusByPlayerClubType(playerId, clubId, type, enumeration.CartStatus.COMPLETED);
    } // end method

    public void setCanceledByPlayerClubType(int playerId, int clubId, String type) throws SQLException {
        setStatusByPlayerClubType(playerId, clubId, type, enumeration.CartStatus.CANCELED);
    } // end method

    public void setExpiredByPlayerClubType(int playerId, int clubId, String type) throws SQLException {
        setStatusByPlayerClubType(playerId, clubId, type, enumeration.CartStatus.EXPIRED);
    } // end method

    private void setStatusById(int idCart, enumeration.CartStatus status) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("idCart={} status={}", idCart, status);
        try {
            dao.execute("""
                UPDATE cart SET cartStatus = ?, cartModificationDate = CURRENT_TIMESTAMP
                WHERE idCart = ?
                """, status.name(), idCart);
            LOG.debug("cart status={} for idCart={}", status, idCart);
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    private void setStatusByPlayerClubType(int playerId, int clubId, String type, enumeration.CartStatus status) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("playerId={} type={} status={}", playerId, type, status);
        try {
            dao.execute("""
                UPDATE cart SET cartStatus = ?, cartModificationDate = CURRENT_TIMESTAMP
                WHERE cartPlayerId = ? AND cartClubId = ? AND cartType = ? AND cartStatus = 'PENDING'
                """, status.name(), playerId, clubId, type);
            LOG.debug("cart status={} playerId={} type={}", status, playerId, type);
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // test local
    } // end main
    */

} // end class
