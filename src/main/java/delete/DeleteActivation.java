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

    public Boolean delete(String uuid) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                DELETE
                FROM activation
                WHERE activationkey = ?
                """;

        int rows = dao.execute(query, uuid);
        if (rows == 1) {
            LOG.debug(methodName + " - Successful Delete 1 row of table Activation");
            return true;
        } else {
            LOG.debug(methodName + " - NOT successful Delete rows = " + rows);
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
