package delete;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteTarifSubscription() { }

    public boolean delete(final TarifSubscription tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with tarif = " + tarif);

        final String query = """
                DELETE FROM tarif_subscription
                WHERE TarifSubscriptionCode = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tarif.getCode());
            LCUtil.logps(ps);

            int rowDeleted = ps.executeUpdate();
            String msg = "There are " + rowDeleted + " TarifSubscription deleted for code = " + tarif.getCode();
            if (rowDeleted != 0) {
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                LOG.error(msg);
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

    public boolean deleteAll() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = "DELETE FROM tarif_subscription";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            String msg = "There are " + rowDeleted + " TarifSubscription deleted (all)";
            if (rowDeleted != 0) {
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                LOG.error(msg);
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
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
