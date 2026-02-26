package read;

import entite.Activation;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class ReadActivation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, activation.getActivationKey());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                Activation a = new Activation();
                while (rs.next()) {
                    a = entite.Activation.map(rs);
                }
                if (a.getActivationKey() == null) {
                    String msg = "Votre enregistrement à Golflc ou votre demande de password reset n'ont pas été trouvés !!";
                    LOG.error(methodName + " - " + msg);
                    utils.LCUtil.showMessageFatal(msg);
                    return null;
                }
                LOG.debug(methodName + " - activation found = " + a);
                return a;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
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
