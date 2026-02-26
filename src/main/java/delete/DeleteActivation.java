package delete;

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
public class DeleteActivation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteActivation() { }

    public Boolean delete(String uuid) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                DELETE
                FROM activation
                WHERE activationkey = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid);
            LCUtil.logps(ps);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                LOG.debug(methodName + " - Successful Delete 1 row of table Activation");
                return true;
            } else {
                LOG.debug(methodName + " - NOT successful Delete rows = " + rows);
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(String)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        String uuid = "rrrrrrrrrrr";
        boolean b = new DeleteActivation().delete(uuid);
        LOG.debug("from main - resultat deleteActivation = " + b);
    } // end main
    */

} // end class
