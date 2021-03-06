
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;
import javax.naming.*;
import javax.sql.DataSource;

public class DBConnection implements interfaces.GolfInterface, interfaces.Log{

    static private Connection conn = null;
  //  static private DataSource ds = null;

public Connection getConnection() throws SQLException, Exception{

    String db_connection = null;
    Properties p = null;
    
try{
            LOG.info("entering getConnection");
       ClassLoader clo = Thread.currentThread().getContextClassLoader();
     // Netbeans Files en haut à gauche /src/main/resources
       InputStream is = clo.getResourceAsStream("jdbc.properties");
       p = new Properties();
       p.load(is);
       Enumeration keys = p.keys();
       while (keys.hasMoreElements()) {
         String key = (String)keys.nextElement();
         String value = (String)p.get(key);
            LOG.debug("jdbc.properties = " + key + ": " + value);
       }
         db_connection = p.getProperty("jdbc.mysql")
                       + p.getProperty("jdbc.host")
                       + p.getProperty("jdbc.dbname")
                       + p.getProperty("jdbc.params");
         // adapter également les params dans standalone-full.xml, pour le datasource !!!!!
         LOG.info("db_connection = " + db_connection);
 
 /* Load Driver pas nécessaire !! mais connector_j doit être dans pom.xml
    https://stackoverflow.com/questions/5484227/jdbc-class-forname-vs-drivermanager-registerdriver
*/        
 //Retrieving the list of all the Drivers
    Enumeration<Driver> e = DriverManager.getDrivers();
    //Printing the list
    while(e.hasMoreElements()) {
        LOG.info("Enumeration db driver = " + e.nextElement().getClass());
    }

        conn = DriverManager.getConnection(db_connection,
                p.getProperty("jdbc.username"), 
                p.getProperty("jdbc.password"));
        LOG.info("Connection not via DataSource = " + conn);

        DatabaseMetaData meta = conn.getMetaData();
         LOG.info(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
         LOG.info(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
         
    //     utils.DBMeta.cursorHoldabilitySupport(conn); // new 13-05-2019
         
	return conn;
}catch (SQLException e){
	LOG.error("SQLException in Opening Connection : " + db_connection + " Errorcode = " + e);
        return null;//conn = null;
        //throw e;
}catch (Exception e){
	LOG.error(NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "NO DATABASE FOUND !!!!!!!!!!!!!!!: " + p.getProperty("jdbc.dbname")
                + NEW_LINE + " Errorcode = " + e
                + NEW_LINE + "/");
        e.printStackTrace();
        return null;//conn = null;
        //throw e;        
 }finally{  
  //  LOG.info("conn = " + conn.toString());
    String c = conn.toString();
        LOG.info("Connection returned : {} on database = {}", c.substring(c.lastIndexOf("@"),c.length()), conn.getCatalog() );
}
} // end method
public Connection getConnection(String type) throws SQLException, Exception{
    String db_connection = null;
    Properties p = null;
try{
    LOG.info("attention ! we are using DB = " + type);
 ///   Class.forName("com.mysql.jdbc.Driver"); 
    LOG.info("after class for name");
       ClassLoader clo = Thread.currentThread().getContextClassLoader();
       InputStream is = clo.getResourceAsStream("jdbc.properties");
       p = new Properties();
       p.load(is);
         db_connection = p.getProperty("jdbc.mysql")
                       + p.getProperty("jdbc.host")
                  //     + p.getProperty("jdbc.dbname")
                       + type
                       + p.getProperty("jdbc.params");
        conn = DriverManager.getConnection(db_connection,
                p.getProperty("jdbc.username"), 
                p.getProperty("jdbc.password"));
        DatabaseMetaData meta = null;
        meta = conn.getMetaData();
         LOG.info(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
         LOG.info(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
	return conn;
}catch (SQLException e){
	LOG.error("SQLException in Opening Connection : " + db_connection + " Errorcode = " + e);
        return null;//conn = null;
        //throw e;
}catch (Exception e){
	LOG.error(NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "/"
                + NEW_LINE + "NO DATABASE FOUND !!!!!!!!!!!!!!!: " + p.getProperty("jdbc.dbname")
                + NEW_LINE + " Errorcode = " + e
                + NEW_LINE + "/");
        e.printStackTrace();
        return null;//conn = null;
        //throw e;        
 }finally{  
  //  LOG.info("conn = " + conn.toString());
    String c = conn.toString();
        LOG.info("Connection returned : {} on database = {}", c.substring(c.lastIndexOf("@"),c.length()), conn.getCatalog() );
}
} // end method
/////////////////////////////////////////
public static void closeQuietly(Connection connection, Statement statement,
  // enlevé static      public static void closeQuietly(Connection connection, Statement statement,
        ResultSet resultSet,PreparedStatement preparedStatement ) throws SQLException
{
    // https://openclassrooms.com/fr/courses/626954-creez-votre-application-web-avec-java-ee/624392-communiquez-avec-votre-bdd
 if (resultSet != null){
    try {
        resultSet.close();
        String r = resultSet.toString();
 //           LOG.info("resultSet closed : " + r.substring(r.lastIndexOf("@"),r.length() ));
        }
        catch (SQLException e){
            LOG.error("DBConnection : SQL error closing resultset : " + e);
        }
    }

if(preparedStatement != null && !preparedStatement.isClosed() ){
 try{
            String p = preparedStatement.toString();
          //      LOG.debug("preparedStatement closed : "
          //              + p.substring(p.lastIndexOf("@"), p.lastIndexOf(":") ));
            preparedStatement.close();
  }catch(SQLException e){
            LOG.error("SQL error closing preparedStatement : " + e);
            if(e.getSuppressed() != null)
            {
                for(Throwable t : e.getSuppressed())
                { 
                    LOG.error("Suppressed errors = " + t.getMessage() + " Class: " + t.getClass().getSimpleName()); 
                } 
            }
        }
} //end if
if(connection != null && !connection.isClosed()){
        try{
            connection.close();
        String c = connection.toString();
        LOG.info("connection closed : " + c.substring(c.lastIndexOf("@"),c.length() ) );
        }catch (SQLException e){
            LOG.error("SQL error closing connection : " + e);
        }
    }

if(statement != null){
        try {
            statement.close();
        String s = statement.toString();
            LOG.info("-- statement closed quietly : " + s);
        LOG.info("statement closed : " + s.substring(s.lastIndexOf("@"),statement.toString().length() ));
        }catch (SQLException e){
            LOG.error("SQL error closing Statement : " + e);
        }
    }
} // end method closeQuietly

public Properties findProperties() throws IOException{
    try{
          ClassLoader clo = Thread.currentThread().getContextClassLoader();
     // Netbeans Files en haut à gauche /src/main/resources
          InputStream is = clo.getResourceAsStream("jdbc.properties");
          Properties p = new Properties(); 
          p.load(is);
          return p;
    }catch(final Exception e){
        LOG.info("-- findProperties JDBC Exception = " + e.getMessage() );
        return null;      
}
}
  public javax.sql.DataSource setDataSource() throws Exception{ 
try{
    LOG.info("starting setDataSource" );
    /*
MySQL uses the term "schema" as a synonym of the term"database," while Connector/J historically takes theJDBC term "catalog"
    as synonymous to "database". Thisproperty sets for Connector/J which of the JDBC terms"catalog" and "schema" is used in
    an application torefer to a database. The property takes one of the twovalues CATALOG or SCHEMA and uses it to determine
    (1)which Connection methods can be used to set/get thecurrent database (e.g. setCatalog() or setSchema()?),
    (2) which arguments can be used within the variousDatabaseMetaData methods to filter results 
    (e.g. thecatalog or schemaPattern argument of getColumns()?),and (3) which fields in the ResultSet returned
    byDatabaseMetaData methods contain the databaseidentification information (i.e., the TABLE_CAT orTABLE_SCHEM
    field in the ResultSet returned bygetTables()?). 
If databaseTerm=CATALOG, schemaPattern for searchesare ignored and calls of schema methods (likesetSchema()
    or getSchema()) become no-ops, and viceversa. 
Default: CATALOG 
Since version: 8.0.17 
    */
          Properties p = findProperties();
        // à vérifier si c'est nécessaire ???  
          // properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");  
          // properties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");  
          
          Context ctx = new InitialContext(p);
             LOG.info("Initial Context ctx = " + ctx.getEnvironment());
  
          if(ctx == null){
              throw new Exception("setDataSource -- Exception = No Context: is null");
          }
     //        LOG.info("step 2" );
          String s = p.getProperty("jdbc.datasource");
          /* commentaires ici !!
          // copier le driver sous Wildfly...\standalone\deployments
          //<datasource jta="true" jndi-name="java:jboss/datasources/golflc" pool-name="MySqlDS" enabled="true" use-java-context="true" use-ccm="true">
                    <connection-url>jdbc:mysql://localhost:3307/golflc</connection-url>
                    <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
            */
             LOG.info("-- Using datasource Property String : " + s);
          javax.sql.DataSource ds = (javax.sql.DataSource)ctx.lookup(s);
          if(ds != null){
              LOG.info("Datasource created : " + ds.toString());
              return ds;
      //       connPool = ds.getConnection(p.getProperty("jdbc.username"),p.getProperty("jdbc.password"));
 //// voir jdbc.properties            conn.setCatalog(DB_NAME); // from webGolfInterface mod 4/12/2011
        //    LOG.info("-- setDataSource Database opened = " + connPool.getCatalog() );
     //        conn.setAutoCommit(true); //
        //     return ds;
          }else{
             LOG.info("-- setDataSource NOT found = null" );
                throw new Exception("setDatasource NOT established-- ");
           }
} catch(final NoInitialContextException e) {
        LOG.info("-- NoInitialContext Exception = " + e.getMessage() );
        return null;
}catch(final Exception e){
        LOG.info("-- setDataSource Exception = " + e.getMessage() );
        return null;
}finally{
    
    }
} // end setDataSource


  //public static Connection getPooledConnection(DataSource ds) throws Exception{ 
    public Connection getPooledConnection(DataSource ds) throws Exception{ 
try{   
    LOG.info("starting Pooled Connection with datasource = " + ds );
        if (ds != null){
             Properties p = findProperties();
       //     Connection connPool = ds.getConnection("LouisCollet","lc1lc2"); //(p.getProperty("jdbc.username"),p.getProperty("jdbc.password"));
            Connection connPool = ds.getConnection(p.getProperty("jdbc.username"),p.getProperty("jdbc.password")); 
 //// voir jdbc.properties            conn.setCatalog(DB_NAME); // from webGolfInterface mod 4/12/2011
            LOG.info("-- getPooledConnection Database opened = " + connPool.getCatalog() );
            LOG.info("-- getPooledConnection isValid ? = " + connPool.isValid(5)); // timeout 5 seconds ;
       //     LOG.info("-- PooledConnection ClientInfo = " + connPool.getClientInfo().toString());
          DatabaseMetaData meta = connPool.getMetaData();
            LOG.info(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
            LOG.info(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
            LOG.info(" -- System Functions = " + meta.getSystemFunctions());
      //        conn.setAutoCommit(true); //
 //     utils.DBMeta.cursorHoldabilitySupport(conn); // new 13-05-2019
             return connPool;
        }else{
             LOG.info("-- getPooledConnection NOT established = null" );
                throw new Exception("getPooledConnection NOT established-- ");
         //    return null;
          }
} catch(final NoInitialContextException e) {
        LOG.info("-- NoInitialContext Exception = " + e.getMessage() );
        return null;
}catch(final Exception e){
        LOG.info("-- getPooledConnection Exception = " + e.getMessage() );
        return null;
}finally{
    //    LOG.info("-- getPooledConnection finally" );
    }
} // end getPooledConnection


@Override
public String toString()
{ return 
        ("from DBConnection = nothing !!!"
//               + " ,conn : "   + DBConnection.getConn()
          //     + " ,club Name : " + this.getClubName()
          //     + " ,club City : " + this.getClubCity()
        );
}   
//public static void printDataSourceStats(DataSource ds) throws
//SQLException {
//         javax.sql.BasicDataSource bds = (BasicDataSource) ds;
//         LOG.info("NumActive: " + bds.getNumActive());
//         LOG.info("NumIdle: " + bds.getNumIdle());
//     }

public static void main(String[] args) throws SQLException, Exception{

 //   DBConnection dbc = new DBConnection();
 //   conn = dbc.getConnection();
 //   LOG.info(" -- connection success = " + conn);
  //  DBConnection.closeQuietly(conn, null, null, null);

// pooled connection
//conn = DBConnection.getConnection2(); //.getPooledConnection();
//LOG.info(" -/ pooled connection  obtained  = " + conn);
//DBConnection.closeQuietly(conn, null, null, null);

/// conn = DBConnection.getJNDIConnection();
/// LOG.info(" -- connextion JNDI obtained  = " + conn);
/// DBConnection.closeQuietly(conn, null, null, null);

}// end main

} // end class




/**
 * Opens database MySQL.
 */



// ------------------------------------------------

/*
public static Connection getConnection() throws Exception  // new 24/11/2011
    {
       return getPooledConnection(); //mod 5/8/2012
//    conn = dataSource.getConnection();
//             LOG.info("-- getPooledConnection established = " + dataSource.getConnection().toString());
//     conn.setCatalog(DataBaseName); // from GolfInterface mod 4/12/2011
//             LOG.info("-- getPooledConnection Database opened = " + dataSource.getConnection().getCatalog() );
//        return dataSource.getConnection();
    }
*/
/*
private static DataSource dataSource; // new 5/8/2012
static
    {   String name = "java:comp/env/jdbc/MySQLDataSource";
      //String name = "java:comp/env/jdbc/MySQLDataSource";
        try
        {
            dataSource = (DataSource) new InitialContext().lookup(name);
        }
        catch (NamingException e)
        {
            LOG.info("-- ExceptionInInitializerError, name = " + name + " / "+ e.toString() );
            throw new ExceptionInInitializerError(e);
            //I throw here ExceptionInInitializerError so that the application
            //will immediately stop so that you don't need to face "unexplainable"
            //NullPointerException when trying to obtain a connection.
        }
    }
*/
/*
private static Connection getPooledConnection() throws Exception // not used from 5/8/2012
{
    DataSource ds = null;
    try
    {   //LOG.info("Context  = " + getServletContext().getInitParameter("Administrator =") );
          ctx = new InitialContext();
          if(ctx == null )
            {throw new Exception("getPooledConnection -- Exception in ctx = No Context");}
          ds = (javax.sql.DataSource)ctx.lookup("java:comp/env/jdbc/MySQLDataSource");
             LOG.info("-- Connection Pool Used = java:comp/env/jdbc/MySQLDataSource = " + ds.toString());

          if (ds != null)
          {
             conn = ds.getConnection();
             LOG.info("-- getPooledConnection established = " + conn.toString());
             // mod 25/11/2011
             //stm = conn.createStatement();
             //rs = stm.executeQuery("use golflc");
             //String db = context .getServletContext().getInitParameter("name");
             //getServletContext().getInitParameter("DataBaseName");
             //LOG.info("-- getPooledConnection Database InitParameter = " + db );
             //conn.setCatalog("golflc"); // mod 25/11/2011
             conn.setCatalog(DataBaseName); // from GolfInterface mod 4/12/2011
                LOG.info("-- getPooledConnection Database opened = " + conn.getCatalog() );
             conn.setAutoCommit(true); //
             return conn;
          }else{
             LOG.info("-- getPooledConnection NOT established = null" );
             //throw new Exception("getPooledConnection -- Exception = No Context");
             return null;
          }
    } //end try
    catch(final Exception e)
    {
        LOG.info("-- getPooledConnection Exception = " + e.getMessage() );
   //     e.printStackTrace();
        throw e;
    }
    finally
    {

    }

} // getPooledConnection
*/

//public static void printDataSourceStats(DataSource ds) throws
//SQLException {
//         javax.sql.BasicDataSource bds = (BasicDataSource) ds;
//         LOG.info("NumActive: " + bds.getNumActive());
//         LOG.info("NumIdle: " + bds.getNumIdle());
//     }
/*
public static void closeQuietly(Connection connection, Statement statement,
        ResultSet resultSet,PreparedStatement preparedStatement ) throws SQLException
{
    if (connection != null && !connection.isClosed() ) // mod 18/12/2011
    {
        try { connection.close();
        String c = connection.toString();
        LOG.info("-- connection closed quietly : " + connection);
        LOG.info("-- connection closed quietly : " + c.substring(c.lastIndexOf("@"),c.length() ) );
        }
        catch (SQLException logOrIgnore) {LOG.info("bizarre bizarre");}
    }
    if (statement != null)
    {
        try { statement.close();
        String s = connection.toString();
        LOG.info("-- statement closed quietly : " + statement);
        LOG.info("-- statement closed quietly : " + s.substring(s.lastIndexOf("@"),s.length() ));
        }
        catch (SQLException logOrIgnore) {}
    }
    if (resultSet != null)
    {
        try { resultSet.close();
        LOG.info("-- resultset closed quietly : " + resultSet);
        }
        catch (SQLException logOrIgnore) {}
    }
    if (preparedStatement != null)
    {
        try { preparedStatement.close();
        LOG.info("-- preparedStatement closed quietly : " + preparedStatement);
        }
        catch (SQLException logOrIgnore) {}
    }
} // end
*/
