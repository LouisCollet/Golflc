package read;

import entite.Activation;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;

@ApplicationScoped
public class ReadActivation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadActivation() { }

    public Activation read(final Activation activation) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for activation = " + activation);

        // utilisé pour new player et Reset Password
        final String query = """
                SELECT *
                FROM activation
                WHERE activationkey = ?
                """;

        Activation a = dao.querySingle(query, rs -> entite.Activation.map(rs), activation.getActivationKey());
        if (a == null || a.getActivationKey() == null) {
            String msg = "Votre enregistrement à Golflc ou votre demande de password reset n'ont pas été trouvés !!";
            LOG.error(methodName + " - " + msg);
            utils.LCUtil.showMessageFatal(msg);
            return null;
        }
        LOG.debug(methodName + " - activation found = " + a);
        return a;
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Activation activation = new Activation();
        // activation.setActivationKey("5563e1cf-b31b-46f1-95b2-292fe4f0895");
        // activation = new ReadActivation().read(activation);
        // LOG.debug("after call = " + activation);
    } // end main
    */

} // end class
