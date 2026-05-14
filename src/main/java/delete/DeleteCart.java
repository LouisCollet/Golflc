package delete;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@ApplicationScoped
public class DeleteCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteCart() { }

    public void deleteByPlayerClubType(int playerId, int clubId, String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} playerId={} type={}", methodName, playerId, type);

        int rows = dao.execute(
            "DELETE FROM cart WHERE cartPlayerId = ? AND cartClubId = ? AND cartType = ?",
            playerId, clubId, type);
        LOG.debug("cart deleted rows={} playerId={} type={}", rows, playerId, type);
    } // end method

    public void deleteAllByPlayerClub(int playerId, int clubId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} playerId={}", methodName, playerId);
        int rows = dao.execute(
            "DELETE FROM cart WHERE cartPlayerId = ? AND cartClubId = ?",
            playerId, clubId);
        LOG.debug("all carts deleted rows={} playerId={}", rows, playerId);
    } // end method
/*
    public void deleteExpiredBefore(LocalDateTime threshold) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("threshold={}", threshold);

        int rows = dao.execute(
            "DELETE FROM cart WHERE cartStatus = 'PENDING' AND cartCreatedAt < ?",
            Timestamp.valueOf(threshold));
        LOG.info("expired PENDING carts deleted rows={} threshold={}", rows, threshold);
    } // end method
*/
    
    public int deleteExpiredBefore(LocalDateTime threshold) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("threshold={}", threshold);

        long startTime = System.currentTimeMillis();
        Timestamp ts = Timestamp.valueOf(threshold);

        int pendingRows = dao.execute(
            """
            DELETE FROM cart
            WHERE cartStatus = 'PENDING'
            AND cartCreatedAt < ?
            """,
            ts);

        int terminalRows = dao.execute(
            """
            DELETE FROM cart
            WHERE cartStatus IN ('COMPLETED', 'CANCELED')
            AND cartModificationDate < ?
            """,
            ts);

        int total = pendingRows + terminalRows;
        long durationMs = System.currentTimeMillis() - startTime;
        LOG.info("cart cleanup: pending={} terminal={} threshold={} durationMs={}",
                 pendingRows, terminalRows, threshold, durationMs);
        return total;
    } // end method

} // end class
