package integration.support;

import connection_package.JdbcConnectionProvider;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTestDataSource implements DataSource {

    @Override
    public Connection getConnection() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return new JdbcConnectionProvider().getConnection();
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    } // end method

    @Override public <T> T unwrap(Class<T> iface)               { return null; }
    @Override public boolean isWrapperFor(Class<?> iface)        { return false; }
    @Override public java.io.PrintWriter getLogWriter()          { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out)  {}
    @Override public void setLoginTimeout(int seconds)           {}
    @Override public int getLoginTimeout()                       { return 0; }
    @Override public java.util.logging.Logger getParentLogger()  {
        return java.util.logging.Logger.getGlobal();
    }

} // end class
