package delete;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class DeleteActivation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteActivation() { }

    public int deleteExpired() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        int countBefore = dao.querySingle("SELECT COUNT(*) FROM activation", rs -> rs.getInt(1));
        LOG.info("activation count before cleanup = {}", countBefore);

        int deleted = dao.execute("""
                DELETE FROM activation
                WHERE activation.ActivationCreationDate < DATE_SUB(CURRENT_DATE, INTERVAL 1 WEEK)
                """);

        int countAfter = dao.querySingle("SELECT COUNT(*) FROM activation", rs -> rs.getInt(1));
        LOG.info("activation cleanup done — deleted={} remaining={}", deleted, countAfter);

        return deleted;
    } // end method

    public Boolean delete(String uuid) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                DELETE
                FROM activation
                WHERE activationkey = ?
                """;

        int rows = dao.execute(query, uuid);
        if (rows == 1) {
            LOG.debug("Successful Delete 1 row of table Activation");
            return true;
        } else {
            LOG.debug("NOT successful Delete rows = {}", rows);
            return false;
        }
    } // end method

} // end class
