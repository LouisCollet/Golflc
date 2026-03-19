package connection_package;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import static utils.LCUtil.printProperties;

import static utils.LCUtil.showMessageFatal;

public final class DBConnection2 {

    private DBConnection2() {
        throw new UnsupportedOperationException("Utility class - cannot be instanciated");
    }
    /**
     * Retourne une nouvelle connexion JDBC.
     * L'appelant est responsable de la fermeture.
     */
    public static Connection getConnection() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        String jdbcUrl = null;
        try {
            LOG.debug("Opening JDBC connection");
            Properties props = findProperties();
            printProperties("jdbc.properties");

            jdbcUrl =
                    props.getProperty("jdbc.mysql")
                  + props.getProperty("jdbc.host")
                  + props.getProperty("jdbc.dbname")
                  + props.getProperty("jdbc.params");

            LOG.debug("JDBC URL = {}", jdbcUrl);

            String username = System.getenv("MYSQL_USERNAME");
            String password = System.getenv("MYSQL_PASSWORD");

            if (username == null || password == null) {
                throw new Exception("Missing environment variables MYSQL_USERNAME / MYSQL_PASSWORD");
            }

            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            logMetaData(conn);
            return conn;

        } catch (SQLException e) {
            LOG.error("SQLException while opening connection to {}", jdbcUrl, e);
            handleSQLException(e, methodName);
            return null;

        } catch (Exception e) {
            LOG.error("Exception while opening database connection", e);
            handleGenericException(e, methodName);
            return null;
        }
    }

private static void logMetaData(Connection conn) throws SQLException {
        if (conn == null) return;
        DatabaseMetaData meta = conn.getMetaData();
        LOG.debug("JDBC version        = {}.{}", meta.getJDBCMajorVersion(), meta.getJDBCMinorVersion());
        LOG.debug("JDBC driver version = {}", meta.getDriverVersion());
        LOG.debug("Connected database  = {}", conn.getCatalog());
    }

    /**
     * Charge le fichier jdbc.properties depuis le classpath.
     */
   public static Properties findProperties() throws IOException {
        String resourceName = "jdbc.properties";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Resource not found on classpath: " + resourceName);
            }
            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (IOException e) {
            LOG.error("Unable to load {}", resourceName, e);
           // handleGenericException(e, methodName);
           return null;
        }
    
    } // end findProperties
   
   public static void closeQuietly(Connection connection, Statement statement, ResultSet resultSet,PreparedStatement preparedStatement ) throws SQLException{
    try{
    // https://openclassrooms.com/fr/courses/626954-creez-votre-application-web-avec-java-ee/624392-communiquez-avec-votre-bdd
 if (resultSet != null){
    try {
        resultSet.close();
        String r = resultSet.toString();
 //           LOG.debug("resultSet closed : " + r.substring(r.lastIndexOf("@"),r.length() ));
        }
        catch (SQLException e){
            LOG.error("DBConnection : SQL error closing resultset : " + e);
        }
    }
  if(preparedStatement != null && !preparedStatement.isClosed() ){
    try{
         //   String p = preparedStatement.toString();
          //      LOG.debug("preparedStatement closed : "
          //              + p.substring(p.lastIndexOf("@"), p.lastIndexOf(":") ));
            
            preparedStatement.close();
    }catch(SQLException e){
            LOG.error("SQL error closing preparedStatement : " + e);
            if(e.getSuppressed() != null) {
                for(Throwable t : e.getSuppressed()){ 
                    LOG.error("Suppressed errors = " + t.getMessage() + " Class: " + t.getClass().getSimpleName()); 
                } 
            }
        }
} //end if
  if(connection != null && !connection.isClosed()){
        try{
            connection.close();
        String c = connection.toString();
        LOG.debug("connection closed : " + c.substring(c.lastIndexOf("@"),c.length() ) );
        }catch (SQLException e){
            LOG.error("SQL error closing connection : " + e);
        }
    }

if(statement != null){
        try {
            statement.close();
        String s = statement.toString();
            LOG.debug("-- statement closed quietly : " + s);
        LOG.debug("statement closed : " + s.substring(s.lastIndexOf("@"),statement.toString().length() ));
        }catch (SQLException e){
            LOG.error("SQL error closing Statement : " + e);
        }
    }
}catch (Exception ex){
    String msg = "Exception in FindSubsscription() " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
  //  return null;
}
} // end method closeQuietly
   
} //end class