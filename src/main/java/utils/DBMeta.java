/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;
import static interfaces.GolfInterface.NEWLINE;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import lc.golfnew.Constants;

public class DBMeta // implements interfaces.Log //__GolfInterface
{

public static int getCountColumns(Connection conn, String table) throws SQLException
{
    Statement st = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
try
{
    final String query = "SELECT * FROM " + table;
    st = conn.createStatement();
    st.executeQuery(query);
    rs = st.getResultSet();
    rsmd = rs.getMetaData ();
    return rsmd.getColumnCount();
}
catch (SQLException e)
{
	LOG.error("SQLException in getCountColumnsMeta : " + e);
        return 0;
        //throw e;
}
finally
{
    rs.close();
    st.close();
}
} // end method
/////////////////////////////////////////
public static void showColumns(Connection conn, String table) throws SQLException
{
    Statement st = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
try
{
    st = conn.createStatement();
    st.executeQuery("select * from " + table);
    rs = st.getResultSet();
    rsmd = rs.getMetaData();
    LOG.info("Schema: " +  rsmd.getSchemaName(1) +
              " /Table : = " + rsmd.getTableName(1) );
    for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {LOG.info("Column " + i + " = "
            + rsmd.getColumnName(i) + " ("
            + rsmd.getColumnTypeName(i) + ")"
            + rsmd.getPrecision(i)
            + " "
            +rsmd.getColumnClassName(i)
                + " "
                + rsmd.getColumnType(i) );
        } // end for
}
catch (NullPointerException npe)
{
	LOG.error("NullPointerException showColumns : " + npe);
        throw npe;
}
catch (SQLException e)
{
	LOG.error("SQLException in showColumns : " + e);
        //throw e;
}
finally
{
    LOG.error("finally2 executed ! ");
    DBConnection.closeQuietly(null, st, rs, null);
}
} // end method
public static void listMetaData (Connection conn)//  throws java.sql.SQLException
{   DatabaseMetaData meta = null;
    //ResultSet rs = null;
try
{    meta = conn.getMetaData();
    //LOG.info(" -- Meta Product Name = {} ", meta);
    LOG.info(" -- Database server = " + meta.getDatabaseProductName()+ " " + meta.getDatabaseProductVersion() );
  //  LOG.info(" -- Meta Product Version = "  );
    LOG.info(" -- Meta Driver Name = " + meta.getDriverName() );
    LOG.info(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
    
 //   LOG.info(" -- Meta Driver MajorVersion = " + meta.getDriverMajorVersion() );
 //   LOG.info(" -- Meta Driver MinorVersion = " + meta.getDriverMinorVersion() );
    LOG.info(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
  //  LOG.info(" -- Meta DB MinorVersion = " + meta.getDatabaseMinorVersion());// + '.' + meta.getJDBCMinorVersion());
 //   LOG.info(" -- Meta DB MajorVersion = " + meta.getDatabaseMajorVersion());// + '.'
 //   LOG.info(" -- Meta DB minorVersion = " + meta.getDatabaseMinorVersion());// + '.'
    LOG.info(" -- Meta User = " + meta.getUserName() );
    LOG.info(" -- Meta Connection Url = " + meta.getURL() );
    LOG.info(" -- Meta Connection = " +  meta.getConnection() );
    String javaVersion = System.getProperty("java.runtime.version") + " from ";
    LOG.info(" -- JAVA Virtual Machine = " +  System.getProperty("java.version") ); //System.getProperty("java.vm.version"));
    LOG.info(" -- Architecture = " +  System.getProperty("os.arch") );
    LOG.info(" , Java version = " + javaVersion + System.getProperty("java.vendor"));
    LOG.info(" -- Java specification = " + System.getProperty("java.specification.version"));
    LOG.info(" -- Java home = " + System.getProperty("java.home"));
    LOG.info(" -- OS name = " + System.getProperty("os.name"));
    LOG.info(" -- OS version = " + System.getProperty("os.version"));
    LOG.info(" -- OS architecture = " + System.getProperty("os.arch"));
    LOG.info(" -- JRE Version = " + System.getProperty("java.version"));
    LOG.info(" -- Supports Transactions : " + meta.supportsTransactions());
    LOG.info(" -- TEMP : " + System.getProperty("java.io.tmpdir"));
    LOG.info(" -- diskpace : " + LCUtil.DiskSpace());
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
    {
      LOG.info("Transaction Isolation level " + "TRANSACTION_READ_COMMITTED is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED))
    {
      LOG.info("Transaction Isolation level " + "TRANSACTION_READ_UNCOMMITTED is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }
        if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
    {
      LOG.info("Transaction Isolation level " + "TRANSACTION_REPEATABLE_READ is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
    {
      LOG.info("Transaction Isolation level " + "TRANSACTION_SERIALIZABLE is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
// new 28/01/2017    
    LOG.info("ResultSet.HOLD_CURSORS_OVER_COMMIT = " +
        ResultSet.HOLD_CURSORS_OVER_COMMIT);

    LOG.info("ResultSet.CLOSE_CURSORS_AT_COMMIT = " +
        ResultSet.CLOSE_CURSORS_AT_COMMIT);

    LOG.info("Default cursor holdability: " +
        meta.getResultSetHoldability());

    LOG.info("Supports HOLD_CURSORS_OVER_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.HOLD_CURSORS_OVER_COMMIT));

    LOG.info("Supports CLOSE_CURSORS_AT_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.CLOSE_CURSORS_AT_COMMIT));
    
        final Runtime runtime = Runtime.getRuntime();
    //* use availableProcessors method to determine
    // * how many processors are available to the Java Virtual Machine (JVM)
    //int numberOfProcessors = runtime.availableProcessors();
    LOG.info(runtime.availableProcessors() + " processor available to JVM");
    LOG.info(" -- IP ADRESS = " + InetAddress.getByName("localhost"));
    LOG.info(" -- IP ADRESS = " + InetAddress.getLocalHost());
    LOG.info(" -- user.dir  = " + System.getProperty("user.dir") ); //the root directory of the WildFly distribution
    LOG.info(" -- Custom USER_DIR =  " + Constants.USER_DIR);
    LOG.info(" -- jboss.home = " + System.getProperty("jboss.home") );
    LOG.info(" -- jboss.server.base.dir = " + System.getProperty("jboss.server.base.dir") );
    LOG.info(" -- jboss.server.data.dir = " + System.getProperty("jboss.server.data.dir") );
    LOG.info(" -- jboss.server.log.dir = " + System.getProperty("jboss.server.log.dir") );
    LOG.info(" -- jboss.server.tmp.dir = " + System.getProperty("jboss.server.tmp.dir") );
    LOG.info(" -- jboss.server.temp.dir = " + System.getProperty("jboss.server.temp.dir") );
    LOG.info(" -- jboss.server.config.dir = " + System.getProperty("jboss.server.config.dir") );
    LOG.info(" -- jboss.server.name = " + System.getProperty("jboss.server.name") );
    LOG.info(" -- jboss.server.deploy.dir = " + System.getProperty("jboss.server.deploy.dir") );
    LOG.info(" -- logging.configuration = " + System.getProperty("logging.configuration") );
    LOG.info(" -- user.home = " + System.getProperty("user.home") );
    LOG.info(" -- user.name = " + System.getProperty("user.name") );
    LOG.info(" -- user.name test compilation = " + System.getProperty("user.name") );
    LOG.info(" -- classpath = " + System.getProperty("java.class.path") );
//    LOG.info(" -- glassfish directory = " + System.getProperty("catalina.base") );
    LOG.info("System Character Charset  = " + Charset.defaultCharset().name() );
    LOG.info("System Character encoding = " + System.getProperty("file.encoding") );

}
catch(Exception e)
{
    LOG.error("Exception in DBMeta = " + e);
} // end catch
} // end method

public static void listMetaTables (Connection conn) throws SQLException
{
    DatabaseMetaData meta = null;
    ResultSet result = null;
try
{
    meta = conn.getMetaData();
    String   catalog          = null;
    String   schemaPattern    = null;
    String   tableNamePattern = null;
    String[] types            = null;

 result = meta.getTables(catalog, schemaPattern, tableNamePattern, types );

while(result.next())
{
    String tableName = result.getString(3);
     LOG.info("table = " + tableName);
} //end while

/*First you call the getTables() method, passing it 4 parameters which are all null.
 * The parameters can help limit the number of tables that are returned in the ResultSet.
 * However, since I want all tables returned, I passed null in all of these parameters.
 * See the JavaDoc for more specific details about the parameters. 
The ResultSet returned from the getTables() method contains a list of table names
* matching the 4 given parameters (which were all null).
* This ResultSet contains 10 columns, which each contain information about the given table.
* The column with index 3 contains the table name itself
* . Check the JavaDoc for more details about the rest of the columns
* Each table description has the following columns: 
1.TABLE_CAT String => table catalog (may be null) 
2.TABLE_SCHEM String => table schema (may be null) 
3.TABLE_NAME String => table name 
4.TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM". 
5.REMARKS String => explanatory comment on the table 
6.TYPE_CAT String => the types catalog (may be null) 
7.TYPE_SCHEM String => the types schema (may be null) 
8.TYPE_NAME String => type name (may be null) 
9.SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null) 
10.REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null) 

*/
// Get date and time functions supported by database fonctionne mais enlevé 7/8/2011
   // String[] functions = meta.getTimeDateFunctions().split(",\\s*");
    //for (int i = 0; i < functions.length; i++)
    //{
    //    String function = functions[i];
    //    LOG.info("Function = " + function);
    //}

}
catch(Exception e)
{
            LOG.error("Exception " + e);
}
finally
{
   // LOG.error("finally2 executed ! ");
    DBConnection.closeQuietly(null, null, result, null);
}

} // end method
/*
public static void getJdbcTypeName(int jdbcType) {
    Map map = new HashMap();

    // Get all field in java.sql.Types
    Field[] fields = java.sql.Types.class.getFields();
    for (int i = 0; i < fields.length; i++) {
      try {
        String name = fields[i].getName();
        Integer value = (Integer) fields[i].get(null);
        map.put(value, name);
      } catch (IllegalAccessException e) {
      }
    }
    System.out.println(map);
  }
*/

public static String SqltoJava(String TypeName) {
    // converting SQL d ta type to Java data type
    TypeName = TypeName.toUpperCase();
        switch (TypeName) {
            case "DATE":
                return "java.sql.date";
            case "DATETIME":
                return "java.sql.timestamp";
            case "TIMESTAMP":
                return "java.sql.timestamp";
            case "CHARACTER":
                return "String";
            case "VARCHAR":
                return "String";
            case "CHAR":
                return "String";
            case "BIT":
                return "Boolean";
            case "DECIMAL":
                return "java.math.BigDecimal";
            case "TINYINT":
                return "Short";
            case "TINYINT UNSIGNED":
                return "Short";    
            case "SMALLINT":
                return "Short";
            case "INTEGER":
                return "Int";
            case "INT":
                return "Int";
            case "INT UNSIGNED":
                return "Int";
            case "DOUBLE PRECISION":
                return "Double";
            case "VARBINARY":
                return "byte[]";
            default:
                return "*** Not FOUND ***";
        }
    }

public static String repla(String str) {
    String r = str.replace("idclub", "Idclub");
           r = str.replace("idcourse", "Idcourse");
    return r;
}

public static String datetime(String c) {
        LOG.info("datetime for =" + c); // RoundDate
        StringBuilder sb9 = new StringBuilder();
        //java.util.Date d = rs.getTimestamp('RoundDate')'
        sb9.append(NEWLINE)
           .append("java.util.Date d = rs.getTimestamp(')")
           .append(c)
           .append("')")
           .append(NEWLINE)
           .append("LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();");
    //    .append()
     //   sb9.append(sb9).append(NEWLINE).append
  //    java.util.Date d = rs.getTimestamp("roundDate");
  //  LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  //    sr.setRoundDate(date);
    return sb9.toString();
}

public static String setterGenerator(Connection conn, String table) throws SQLException
{

    ResultSet rs = null;
try{
    DatabaseMetaData meta = conn.getMetaData();
    List<String> blacklist = Arrays.asList("clubzoneid", "clubmodificationdate", "coursemodificationdate",
        "", "");
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
     rs = meta.getColumns(null, null, table, null);
      LOG.info(NEWLINE + "List of columns: for table = " + table + NEWLINE);
      String b = "";
      String s = "";
      String r = "";
      String t = "";
      String c = "";
      while (rs.next()) {
                LOG.info("column name = " + rs.getString("COLUMN_NAME"));
            c = rs.getString("COLUMN_NAME");
                LOG.info("type name = " + rs.getString("TYPE_NAME"));
            t = rs.getString("TYPE_NAME");
            b = rs.getString("COLUMN_NAME").toLowerCase(); // 4.COLUMN_NAME String => column name 
            if(blacklist.contains(b))
                { LOG.info(" *** rejected field ***  = " + b);
                continue;
            }
          r = repla(rs.getString("COLUMN_NAME"));  // idclub devient Idclub
            LOG.info(" replaced = " + r);
            if("DATETIME".equals(t))
            {
                    LOG.info("DATETIME");
                String d = datetime(c);
                    LOG.info("return DATETIME = " + d);
            }
          s = SqltoJava(rs.getString("TYPE_NAME"));  // converstion SQL data type to Java data type

          sb.setLength(0);  // initialize
          sb.append(NEWLINE);
          sb.append("sg.")   //setter generator
         .append("set")
         .append(r)
         .append("(rs.get")
         .append(s)
         .append("('")
         .append(r)
         .append("'));")
 ;
         LOG.info("stringbuider = " + sb);
         sb2.append(sb);
      }
         
         LOG.info("stringbuider 22222 = " + sb2);
         return sb2.toString();
   //    }
         //java.util.Date d = rs.getTimestamp("roundDate");
   //      LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
   //      sr.setRoundDate(date);
         
    //     type name = DATETIME
//2017-08-16T18:52:57,278 923  INFO utils.DBMeta setterGenerator DBMeta.java - stringbuider =
//ccr.setRoundDate(rs.getjava.sql.timestamp('RoundDate'));
         
     //    LOG.info("column size = " + res.getInt("COLUMN_SIZE"));
     //    LOG.info("nullable = " + res.getInt("NULLABLE")); 

     //   ccr.setHandicapPlayer(rs.getBigDecimal("HandicapPlayer") );


/*
     
     
     
 //ResultSet resultSet = meta.getTypeInfo();
  rs = meta.getTypeInfo();
     rs.beforeFirst();
       LOG.info(NEWLINE + "List TYPE and DATA: " + NEWLINE); 
    while (rs.next()) {
      typeName = rs.getString("TYPE_NAME");
        LOG.info("typename T/D = " + typeName);
      short dataType = rs.getShort("DATA_TYPE");
      LOG.info("datatype T/D = " + dataType);
      getJdbcTypeName(dataType);
    }

    rs.beforeFirst(); 
 while(rs.next())
{
  //  String columnName = result.getString(4);
      LOG.info("COLUMN_NAME = " + rs.getString(4));
  //  int columnType = result.getInt(5);
       LOG.info("DATA_TYPE = " + rs.getInt(5));
//    int columnPosition = result.getInt(15);
      LOG.info("ORDINAL_POSITION = " + rs.getInt(17));
      LOG.info("DECIMAL_DIGITS = " + rs.getInt(9));
      LOG.info("COLUMN_SIZE = " + rs.getInt(7));
} //end while
 

rs.beforeFirst();
StringBuilder sb = new StringBuilder();
   while(rs.next())
    {
        sb.append(rs.getString(4));
        sb.append(", ");
 //       LOG.info("inside loop, sb = " + sb);
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //   LOG.info("sb capacity = " + sb.capacity());
        LOG.info("sb = " + sb);
     */

   

}catch(Exception e){
      LOG.error("Exception n setterGenerator " + e);
      return null;
}finally{
    //LOG.error("finally2 executed ! ");
    DBConnection.closeQuietly(null, null, rs, null);
  //  return null;
}

} // end method

public static String listMetaColumnsLoad (Connection conn, String table) throws SQLException
{      ResultSet rs = null;
//https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getColumns-java.lang.String-java.lang.String-java.lang.String-java.lang.String-
try{
///    LOG.info("starting listMetaColumnsLoad for table = " + table );
    DatabaseMetaData meta = conn.getMetaData();
  //  String   catalog          = null;
  //  String   schemaPattern    = null;
  //  String   tableNamePattern = table;
  //  String   columnNamePattern = null;
//LOG.info("line 01");
rs = meta.getColumns(null, null, table, null);
//LOG.info("line 02");
rs.beforeFirst();
StringBuilder sb = new StringBuilder();
sb.append(" "); // space to separate query element
//LOG.info("line 03");
   while(rs.next())
    {
 //    LOG.info("We are now with = " + rs.getString(4));  // column name
        sb.append(rs.getString("TABLE_NAME")).append(".").append(rs.getString("COLUMN_NAME"));
     //   sb.append(table + "." + rs.getString(4));  // column name
        sb.append(", ");
 //       LOG.info("inside loop, sb = " + sb);
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule

 ///       LOG.info("Table fields for " + table + " = " + sb);
return sb.toString();
   
/*http://docs.oracle.com/javase/6/docs/api/java/sql/DatabaseMetaData.html
 * The ResultSet returned by the getColumns() method contains a list of columns
 * for the given table. The column with index 4 contains the column name,
 * and the column with index 5 contains the column type.
 * The column type is an integer matching one of the type constants found in java.sql.Types 
To get more details about obtaining column information for tables, check out the JavaDoc.
* 
Each column description has the following columns: 
1.TABLE_CAT String => table catalog (may be null) 
2.TABLE_SCHEM String => table schema (may be null) 
3.TABLE_NAME String => table name 
4.COLUMN_NAME String => column name 
5.DATA_TYPE int => SQL type from java.sql.Types 
6.TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified 
7.COLUMN_SIZE int => column size. 
8.BUFFER_LENGTH is not used. 
9.DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable. 
10.NUM_PREC_RADIX int => Radix (typically either 10 or 2) 
11.NULLABLE int => is NULL allowed. ◦ columnNoNulls - might not allow NULL values 
◦ columnNullable - definitely allows NULL values 
◦ columnNullableUnknown - nullability unknown 

12.REMARKS String => comment describing column (may be null) 
13.COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null) 
14.SQL_DATA_TYPE int => unused 
15.SQL_DATETIME_SUB int => unused 
16.CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column 
17.ORDINAL_POSITION int => index of column in table (starting at 1) 
18.IS_NULLABLE String => ISO rules are used to determine the nullability for a column. ◦ YES --- if the parameter can include NULLs 
◦ NO --- if the parameter cannot include NULLs 
◦ empty string --- if the nullability for the parameter is unknown 

19.SCOPE_CATLOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF) 
20.SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF) 
21.SCOPE_TABLE String => table name that this the scope of a reference attribure (null if the DATA_TYPE isn't REF) 
22.SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type,
* SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF) 
23.IS_AUTOINCREMENT String => Indicates whether this column is auto incremented ◦ YES --- if the column is auto incremented 
◦ NO --- if the column is not auto incremented 
◦ empty string --- if it cannot be determined whether the column is auto incremented parameter is unknown 


The COLUMN_SIZE column the specified column size for the given column.
* For numeric data, this is the maximum precision. For character data, 
* this is the length in characters. For datetime datatypes, this is the length in characters
* of the String representation (assuming the maximum allowed precision of the fractional seconds component).
* For binary data, this is the length in bytes. For the ROWID datatype, this is the length in bytes.
* Null is returned for data types where the column size is not applicable. 


Parameters:
*/
}catch(Exception e){
      LOG.error("Exception " + e);
      return null;
}finally{
    //LOG.error("finally2 executed ! ");
    DBConnection.closeQuietly(null, null, rs, null);
}

} // end method

public static String listMetaColumnsUpdate (Connection conn, String table) throws SQLException{ // 10/08/2017
    ResultSet rs = null;
try{
    DatabaseMetaData meta = conn.getMetaData();
  //  String   catalog          = null;
  //  String   schemaPattern    = null;
  //  String   tableNamePattern = table;
   // String   columnNamePattern = null;

 rs = meta.getColumns(null, null, table, null);

//rs.beforeFirst();
rs.first();  // grosse astuce ! ne pas prendre la première field : idplayer, idclub, etc ...
StringBuilder sb = new StringBuilder();
// les colonnes suivantes ne doivent pas être MAJ en update
List<String> blacklist = Arrays.asList(
        "playerphotolocation", "playeractivation", "playermodificationdate" , "playerpassword", "playerRole",  // TOUT EN MINUSCULES !!!!
        "clubmodificationdate","club_idclub",
        "coursemodificationdate","course_idcourse",// "courseholes",
        "teemodificationdate", "tee_idtee", "tee_course_idcourse",
        "holenumber", "holemodificationdate"
        ); // 07-08-208
String s = "";
   while(rs.next())
    {   s = rs.getString(4).toLowerCase(); // 4.COLUMN_NAME String => column name 
        if(blacklist.contains(s))
            { LOG.info("rejected field = " + s);
             continue;
            }
        sb.append(s).append("=?, "); 
 //       LOG.info("inside loop, sb = " + sb);
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
        LOG.info("sb listMetaColumnsUpdate = " + sb);
return sb.toString();
   }catch(Exception e){
      LOG.error("Exception " + e);
      return null;
}finally{
    //LOG.error("finally2 executed ! ");
    DBConnection.closeQuietly(null, null, rs, null);
}
} //en method

public static void listMetaStoredPro(Connection conn, String nomProcedure)
        throws SQLException
{   DatabaseMetaData meta = conn.getMetaData();
    ResultSet rs = meta.getProcedureColumns(conn.getCatalog(),null,nomProcedure,"%");
        LOG.info("listMetaStoredPro : ############## " + nomProcedure);
    while(rs.next())
    {
        LOG.info("\n");
        LOG.info("Nom parametre  = " + rs.getString("COLUMN_NAME"));
        LOG.info("Type paramètre = " + rs.getInt("COLUMN_TYPE"));
        LOG.info("Type SQL       = " + rs.getString("TYPE_NAME"));
    }
     LOG.info("end listMetaStoredPro : ############## " + nomProcedure);
    rs.close();
 //   meta.close());
} // end method

/**
 *
 * @param args
     * @throws java.sql.SQLException
 */
public static void main(String[] args) throws SQLException, Exception // testing purposes
{
    DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
//showColumns(conn,"club");
//LOG.info(" -- success   !!!! ");
//int c = getCountColumns(conn,"club");
//LOG.info(" -- # columns = " + c);
//listMetaTables(conn);
//listMetaData(conn);
//String s = listMetaColumnsUpdate(conn, "round");

//String s = setterGenerator(conn, "handicap");

//DBMeta md = new DBMeta();
String p = listMetaColumnsLoad(conn,"Player");  // is static 
LOG.info(" main - player = " + p);
String p1 = listMetaColumnsLoad(conn,"Round"); 
LOG.info(" main - round = " + NEW_LINE + p);
p = p + NEW_LINE + "," + p1;
LOG.info(" main - player + round = " + p);
/*
ResultSet rs = md.getTables(null, null, "%", null);
while (rs.next()) {
  LOG.info(rs.getString(3));
  String s = setterGenerator(conn, rs.getString(3));
}
*/
//listMetaStoredPro(conn, "get_list_points");
DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class