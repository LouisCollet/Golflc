package find;

import entite.Cart;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import rowmappers.CartRowMapper;

@ApplicationScoped
public class FindCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindCart() { }

    public Optional<Cart> findPending(int playerId, int clubId, String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("playerId={} type={}", playerId, type);

        final String query = """
            SELECT idCart, cartPlayerId, cartClubId, cartType, cartItemsJson,
                   cartTotal, cartStatus, cartCreatedAt, cartModificationDate
            FROM cart
            WHERE cartPlayerId = ? AND cartClubId = ? AND cartType = ? AND cartStatus = 'PENDING'
            ORDER BY cartModificationDate DESC
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.setInt(2, clubId);
            ps.setString(3, type);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new CartRowMapper().map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Optional.empty();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Optional.empty();
        }
    } // end method

    public List<Cart> findAllPendingByPlayer(int playerId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("playerId={}", playerId);

        final String query = """
            SELECT idCart, cartPlayerId, cartClubId, cartType, cartItemsJson,
                   cartTotal, cartStatus, cartCreatedAt, cartModificationDate
            FROM cart
            WHERE cartPlayerId = ? AND cartStatus = 'PENDING'
            ORDER BY cartModificationDate DESC
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Cart> list = new ArrayList<>();
                CartRowMapper mapper = new CartRowMapper();
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
                LOG.debug("findAllPendingByPlayer size={}", list.size());
                return list;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<Cart> findAllPending(int playerId, int clubId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("playerId={}", playerId);

        final String query = """
            SELECT idCart, cartPlayerId, cartClubId, cartType, cartItemsJson,
                   cartTotal, cartStatus, cartCreatedAt, cartModificationDate
            FROM cart
            WHERE cartPlayerId = ? AND cartClubId = ? AND cartStatus = 'PENDING'
            ORDER BY cartModificationDate DESC
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.setInt(2, clubId);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<Cart> list = new ArrayList<>();
                CartRowMapper mapper = new CartRowMapper();
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
                LOG.debug("findAllPending size={}", list.size());
                return list;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public int countPendingItems(int playerId, int clubId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("playerId={}", playerId);

        final String query = """
            SELECT COUNT(*) FROM cart
            WHERE cartPlayerId = ? AND cartClubId = ? AND cartStatus = 'PENDING'
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, playerId);
            ps.setInt(2, clubId);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    LOG.debug("pending cart count={}", count);
                    return count;
                }
                return 0;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return 0;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
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
