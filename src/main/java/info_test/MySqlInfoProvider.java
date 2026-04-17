
package info_test;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import javax.sql.DataSource;

@ApplicationScoped
public class MySqlInfoProvider implements InfoProvider, Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public MySqlInfoProvider() {} // end constructor

    @Override
    public String name() {
        return "MySQL";
    } // end method

    @Override
    public String get() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try (Connection c = dataSource.getConnection()) {
            return c.getMetaData().getDatabaseProductName() + " "
                 + c.getMetaData().getDatabaseProductVersion();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "unavailable";
        }
    } // end method

} // end class
