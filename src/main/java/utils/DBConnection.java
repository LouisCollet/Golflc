
package utils;

import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.naming.*;
import javax.sql.DataSource;

public class DBConnection {
    static private Connection conn = null;
  //  static private DataSource ds = null;

public Connection getConnection() throws SQLException, Exception{
    // 1ere posssibilité : db de production uniquement !
    // voir plus loin pour utliser une db de test
    String db_connection = null;
try{
            LOG.debug("entering getConnection");
     // Netbeans Files en haut à gauche /src/main/resources : absolument indispensable pour RUN !!!
       Properties p = findProperties();
       utils.LCUtil.printProperties("jdbc.properties");
       db_connection = p.getProperty("jdbc.mysql")
                     + p.getProperty("jdbc.host")
                     + p.getProperty("jdbc.dbname")
                     + p.getProperty("jdbc.params");
         // adapter également les params dans standalone-full.xml, pour le datasource !!!!!
            LOG.debug("db_connection = " + db_connection);

         conn = DriverManager.getConnection(db_connection,
                 System.getenv("MYSQL_USERNAME"), // mod 10-12-2025 environment variable
                 System.getenv("MYSQL_PASSWORD")
         );
            //    p.getProperty("jdbc.username"), old solution
            //    p.getProperty("jdbc.password"));
            LOG.debug("Connection not via DataSource = " + conn);

 /* Load Driver pas nécessaire !! mais connector_j doit être dans pom.xml
    https://stackoverflow.com/questions/5484227/jdbc-class-forname-vs-drivermanager-registerdriver
*/        
// //Retrieving the list of all the Drivers
//    Enumeration<Driver> e = DriverManager.getDrivers();
//    while(e.hasMoreElements()) {
//        LOG.debug("Enumeration db driver = " + e.nextElement().getClass());
//    }
    DatabaseMetaData meta = conn.getMetaData();
         LOG.debug(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
         LOG.debug(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
	return conn;
}catch (SQLException e){
    String msg = "SQLException in Opening Connection : \n<br/>" + db_connection + "\nErrorcode = " + e;
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception e){
	LOG.error(NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "NO DATABASE FOUND !!!!!!!!!!!!!!!: " // + p.getProperty("jdbc.dbname")
                + NEW_LINE + " Errorcode = " + e
                + NEW_LINE + "/");
     //   e.printStackTrace();
        return null;//conn = null;
        //throw e;        
 }finally{  
    String c = conn.toString();
        LOG.debug("Connection returned : {} on database = {}", c.substring(c.lastIndexOf("@"),c.length()), conn.getCatalog() );
}
} // end method

public Connection getConnection(String type) throws SQLException, Exception{
    // 2e possibilité pour utiliser une db de test !!!
    String db_connection = null;
 //   Properties p = null;
try{
          LOG.debug("attention ! we are using DB = " + type);
        Properties p = findProperties();
        db_connection = p.getProperty("jdbc.mysql")
                       + p.getProperty("jdbc.host")
                  //     + p.getProperty("jdbc.dbname")
                       + type
                       + p.getProperty("jdbc.params");
        conn = DriverManager.getConnection(db_connection,
                p.getProperty("jdbc.username"), 
                p.getProperty("jdbc.password"));
        
        DatabaseMetaData meta = conn.getMetaData();
         LOG.debug(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
         LOG.debug(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
	return conn;
}catch (SQLException e){
	String msg = "SQLException in Opening Connection : " + db_connection + " Errorcode = " + e;
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;//conn = null;
        //throw e;
}catch (Exception e){
	LOG.error(NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "NO DATABASE FOUND !!!!!!!!!!!!!!!: " // + p.getProperty("jdbc.dbname")
                + NEW_LINE + " Errorcode = " + e
                + NEW_LINE + "/");
   //     e.printStackTrace();
        return null;//conn = null;
        //throw e;        
 }finally{  
  //  LOG.debug("conn = " + conn.toString());
    String c = conn.toString();
        LOG.debug("Connection returned : {} on database = {}", c.substring(c.lastIndexOf("@"),c.length()), conn.getCatalog() );
}
} // end method
/////////////////////////////////////////
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
    LCUtil.showMessageFatal(msg);
  //  return null;
}
} // end method closeQuietly

public static Properties findProperties() throws IOException{
    InputStream is = null;
    try{
          ClassLoader clo = Thread.currentThread().getContextClassLoader();
     // Netbeans Files en haut à gauche /src/main/resources
          is = clo.getResourceAsStream("jdbc.properties");
          Properties p = new Properties(); 
          p.load(is);
      //    utils.LCUtil.printProperties("jdbc.properties");
          return p;
    }catch(final Exception e){
        LOG.debug("-- findProperties Exception = " + e.getMessage() );
        return null;      
}finally{
        is.close();
    }
} // end method
  public DataSource setDataSource() throws Exception{ 
try{
    LOG.debug("starting setDataSource" );
    /*
MySQL uses the term "schema" as a synonym of the term"database," while Connector/J historically takes theJDBC term "catalog"
    as synonymous to "database". Thisproperty sets for Connector/J which of the JDBC terms"catalog" and "schema" is used in
    an application torefer to a database. The property takes one of the twovalues CATALOG or SCHEMA and uses it to determine
    (1)which Connection methods can be used to set/get thecurrent database (e.g. setCatalog() or setSchema()?),
    (2) which arguments can be used within the variousDatabaseMetaData methods to filter results 
    (e.g. thecatalog or schemaPattern argument of getColumns()?),and (3) which fields in the ResultSet returned
    byDatabaseMetaData methods contain the database identification information (i.e., the TABLE_CAT or TABLE_SCHEM
    field in the ResultSet returned bygetTables()?). 
If databaseTerm=CATALOG, schemaPattern for searchesare ignored and calls of schema methods (like setSchema()
    or getSchema()) become no-ops, and viceversa. 
Default: CATALOG 
Since version: 8.0.17 
    */
        Properties p = findProperties();
        Context ctx = new InitialContext(p);
             LOG.debug("Initial Context ctx = " + ctx.getEnvironment());
        if(ctx == null){
              throw new Exception("setDataSource -- Exception = No Context: is null");
        }
     //        LOG.debug("step 2" );
        String s = p.getProperty("jdbc.datasource");
          /* commentaires ici !!
          // copier le driver sous Wildfly...\standalone\deployments
          //<datasource jta="true" jndi-name="java:jboss/datasources/golflc" pool-name="MySqlDS" enabled="true" use-java-context="true" use-ccm="true">
                    <connection-url>jdbc:mysql://localhost:3307/golflc</connection-url>
                    <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
            */
             LOG.debug("-- Using datasource Property String : " + s);
        DataSource dataSource = (DataSource)ctx.lookup(s);
        if(dataSource != null){
              LOG.debug("Datasource created : " + dataSource.toString());
              return dataSource;
        }else{
             LOG.debug("-- setDataSource NOT found = null" );
                throw new Exception("setDatasource NOT established-- ");
        }
} catch(final NoInitialContextException e) {
        LOG.error("-- NoInitialContext Exception = " + e.getMessage() );
        return null;
}catch(final Exception e){
        LOG.error("-- setDataSource Exception = " + e.getMessage() );
        return null;
}finally{ }
} // end setDataSource

    public Connection getPooledConnection(DataSource dataSource) throws Exception{ 
try{   
    LOG.debug("starting Pooled Connection with datasource = " + dataSource );
        if (dataSource != null){
            Connection connPool = dataSource.getConnection(
                    System.getenv("MYSQL_USERNAME"), // mod 10-12-2025 environment variable
                    System.getenv("MYSQL_PASSWORD")
            );
                //    p.getProperty("jdbc.username"),
                //    p.getProperty("jdbc.password")); 

            LOG.debug("-- getPooledConnection Database opened = " + connPool.getCatalog() );
            LOG.debug("-- getPooledConnection isValid ? = " + connPool.isValid(5)); // timeout 5 seconds ;
       //     LOG.debug("-- PooledConnection ClientInfo = " + connPool.getClientInfo().toString());
          DatabaseMetaData meta = connPool.getMetaData();
            LOG.debug(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
            LOG.debug(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
            LOG.debug(" -- System Functions = " + meta.getSystemFunctions());
      //        conn.setAutoCommit(true); //
 //     utils.DBMeta.cursorHoldabilitySupport(conn); // new 13-05-2019
             return connPool;
        }else{
             LOG.debug("-- getPooledConnection NOT established = null" );
                throw new Exception("getPooledConnection NOT established-- ");
         //    return null;
          }
} catch(final NoInitialContextException e) {
        LOG.debug("-- NoInitialContext Exception = " + e.getMessage() );
        return null;
}catch(final Exception e){
        String msg = "-- getPooledConnection Exception = " + e.getMessage();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        
        return null;
}finally{
    //    LOG.debug("-- getPooledConnection finally" );
    }
} // end getPooledConnection

@Override
public String toString()
{ return 
        ("from DBConnection = nothing !!!"
        );
}   
//public static void printDataSourceStats(DataSource ds) throws
//SQLException {
//         javax.sql.BasicDataSource bds = (BasicDataSource) ds;
//         LOG.debug("NumActive: " + bds.getNumActive());
//         LOG.debug("NumIdle: " + bds.getNumIdle());
//     }

void main() throws SQLException, Exception{

 //   DBConnection dbc = new DBConnection();
 //   conn = dbc.getConnection();
 //   LOG.debug(" -- connection success = " + conn);
  //  DBConnection.closeQuietly(conn, null, null, null);
}
} //end class