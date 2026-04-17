package utils;

// import connection_package.DBConnection; // removed 2026-02-26 — CDI migration
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DBMeta{

/**
 * Validates that a SQL identifier (table or column name) contains only safe characters.
 * Prevents SQL injection via dynamic identifier concatenation.
 * Security audit 2026-03-09
 *
 * @param identifier the table or column name to validate
 * @return the validated identifier (unchanged)
 * @throws IllegalArgumentException if the identifier contains unsafe characters
 */
public static String validateIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
        throw new IllegalArgumentException("SQL identifier must not be null or blank");
    }
    // Allow only letters, digits, underscores — standard SQL identifier chars
    if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
        throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
    }
    return identifier;
} // end method

public static int CountColumns_old(Connection conn, String table) throws SQLException{
    Statement st = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
try{
    validateIdentifier(table); // security audit 2026-03-09
    final String query = "SELECT * FROM " + table;
    st = conn.createStatement();
    st.executeQuery(query);
    rs = st.getResultSet();
    rsmd = rs.getMetaData();
    return rsmd.getColumnCount();
}catch (SQLException e){
	LOG.error("SQLException in getCountColumnsMeta : " + e);
        return 0;
        //throw e;
}finally{
    rs.close();
    st.close();
}
} // end method

/////////////////////////////////////////
public static void showColumns(Connection conn, String table) throws SQLException{
    Statement st = null;
    ResultSet rs = null;
    ResultSetMetaData rsmd = null;
try{
    validateIdentifier(table); // security audit 2026-03-09
    st = conn.createStatement();
    st.executeQuery("select * from " + table);
    rs = st.getResultSet();
    rsmd = rs.getMetaData();
    LOG.debug("Schema: " +  rsmd.getSchemaName(1) +
              " /Table : = " + rsmd.getTableName(1) );
    for (int i = 1; i <= rsmd.getColumnCount(); i++)
        {LOG.debug("Column " + i + " = "
            + rsmd.getColumnName(i) + " ("
            + rsmd.getColumnTypeName(i) + ")"
            + rsmd.getPrecision(i)
            + " "
            +rsmd.getColumnClassName(i)
                + " "
                + rsmd.getColumnType(i) );
        } // end for
}
catch (NullPointerException npe){
	LOG.error("NullPointerException showColumns : " + npe);
        throw npe;
}catch (SQLException e){
	LOG.error("SQLException in showColumns : " + e);
        //throw e;
}finally{
    LOG.error("finally2 executed ! ");
    if (rs != null) try { rs.close(); } catch (Exception ignored) {}
    if (st != null) try { st.close(); } catch (Exception ignored) {}
}
} // end method

public static void listMetaData (Connection conn){
    DatabaseMetaData meta = null;
    //ResultSet rs = null;
try{ 
    meta = conn.getMetaData();
    //LOG.debug(" -- Meta Product Name = {} ", meta);
    LOG.debug(" -- Database server = " + meta.getDatabaseProductName()+ " " + meta.getDatabaseProductVersion() );
  //  LOG.debug(" -- Meta Product Version = "  );
    LOG.debug(" -- Meta Driver Name = " + meta.getDriverName() );
    LOG.debug(" -- Meta JDBC Connector Version = " + meta.getDriverVersion() );
    
 //   LOG.debug(" -- Meta Driver MajorVersion = " + meta.getDriverMajorVersion() );
 //   LOG.debug(" -- Meta Driver MinorVersion = " + meta.getDriverMinorVersion() );
    LOG.debug(" -- Meta JDBC Version = " + meta.getJDBCMajorVersion() + '.' + meta.getJDBCMinorVersion());
  //  LOG.debug(" -- Meta DB MinorVersion = " + meta.getDatabaseMinorVersion());// + '.' + meta.getJDBCMinorVersion());
 //   LOG.debug(" -- Meta DB MajorVersion = " + meta.getDatabaseMajorVersion());// + '.'
 //   LOG.debug(" -- Meta DB minorVersion = " + meta.getDatabaseMinorVersion());// + '.'
    LOG.debug(" -- Meta User = " + meta.getUserName() );
    LOG.debug(" -- Meta Connection Url = " + meta.getURL() );
    LOG.debug(" -- Meta Connection = " +  meta.getConnection() );
    String javaVersion = System.getProperty("java.runtime.version") + " from ";
    LOG.debug(" -- JAVA Virtual Machine = " +  System.getProperty("java.version") ); //System.getProperty("java.vm.version"));
    LOG.debug(" -- Architecture = " +  System.getProperty("os.arch") );
    LOG.debug(" , Java version = " + javaVersion + System.getProperty("java.vendor"));
    LOG.debug(" -- Java specification = " + System.getProperty("java.specification.version"));
    LOG.debug(" -- Java home = " + System.getProperty("java.home"));
    LOG.debug(" -- OS name = " + System.getProperty("os.name"));
    LOG.debug(" -- OS version = " + System.getProperty("os.version"));
    LOG.debug(" -- OS architecture = " + System.getProperty("os.arch"));
    LOG.debug(" -- JRE Version = " + System.getProperty("java.version"));
    LOG.debug(" -- Supports Transactions : " + meta.supportsTransactions());
    LOG.debug(" -- TEMP : " + System.getProperty("java.io.tmpdir"));
    LOG.debug(" -- diskpace : " + LCUtil.DiskSpace());
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)){
      LOG.debug("Transaction Isolation level " + "TRANSACTION_READ_COMMITTED is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)){
      LOG.debug("Transaction Isolation level " + "TRANSACTION_READ_UNCOMMITTED is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }
        if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ)){
      LOG.debug("Transaction Isolation level " + "TRANSACTION_REPEATABLE_READ is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
    }
    if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)){
      LOG.debug("Transaction Isolation level " + "TRANSACTION_SERIALIZABLE is supported.");
      //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
// new 28/01/2017    
// envoyer avec meta
     cursorHoldabilitySupport(meta);

/*/
    LOG.debug("ResultSet.HOLD_CURSORS_OVER_COMMIT = " +
        ResultSet.HOLD_CURSORS_OVER_COMMIT);

    LOG.debug("ResultSet.CLOSE_CURSORS_AT_COMMIT = " +
        ResultSet.CLOSE_CURSORS_AT_COMMIT);

    LOG.debug("Default cursor holdability: " +
        meta.getResultSetHoldability());

    LOG.debug("Supports HOLD_CURSORS_OVER_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.HOLD_CURSORS_OVER_COMMIT));

    LOG.debug("Supports CLOSE_CURSORS_AT_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.CLOSE_CURSORS_AT_COMMIT));
    */
 //       final Runtime runtime = Runtime.getRuntime();
    //* use availableProcessors method to determine
    // * how many processors are available to the Java Virtual Machine (JVM)
    //int numberOfProcessors = runtime.availableProcessors();
    LOG.debug(Runtime.getRuntime().availableProcessors() + " processor available to JVM");
    LOG.debug(" -- IP ADRESS = " + InetAddress.getByName("localhost"));
    LOG.debug(" -- IP ADRESS = " + InetAddress.getLocalHost());
    LOG.debug(" -- IP ADRESS = " + Arrays.deepToString(InetAddress.getAllByName("localhost")));
    LOG.debug(" -- user.dir  = " + System.getProperty("user.dir") ); //the root directory of the WildFly distribution
    LOG.debug(" -- jboss.home = " + System.getProperty("jboss.home") );
    LOG.debug(" -- jboss.server.base.dir = " + System.getProperty("jboss.server.base.dir") );
    LOG.debug(" -- jboss.server.data.dir = " + System.getProperty("jboss.server.data.dir") );
    LOG.debug(" -- jboss.server.log.dir = " + System.getProperty("jboss.server.log.dir") );
    LOG.debug(" -- jboss.server.tmp.dir = " + System.getProperty("jboss.server.tmp.dir") );
    LOG.debug(" -- jboss.server.temp.dir = " + System.getProperty("jboss.server.temp.dir") );
    LOG.debug(" -- jboss.server.config.dir = " + System.getProperty("jboss.server.config.dir") );
    LOG.debug(" -- jboss.server.name = " + System.getProperty("jboss.server.name") );
    LOG.debug(" -- jboss.server.deploy.dir = " + System.getProperty("jboss.server.deploy.dir") );
    LOG.debug(" -- logging.configuration = " + System.getProperty("logging.configuration") );
    LOG.debug(" -- user.home = " + System.getProperty("user.home") );
    LOG.debug(" -- user.name = " + System.getProperty("user.name") );
    LOG.debug(" -- user.name test compilation = " + System.getProperty("user.name") );
    LOG.debug(" -- classpath = " + System.getProperty("java.class.path") );
//    LOG.debug(" -- glassfish directory = " + System.getProperty("catalina.base") );
    LOG.debug("System Character Charset  = " + Charset.defaultCharset().name() );
    LOG.debug("System Character encoding = " + System.getProperty("file.encoding") );

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

while(result.next()){
    String tableName = result.getString(3);
     LOG.debug("table = " + tableName);
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
    //    LOG.debug("Function = " + function);
    //}

}catch(Exception e){
            LOG.error("Exception " + e);
}
finally{
   // LOG.error("finally2 executed ! ");
    if (result != null) try { result.close(); } catch (Exception ignored) {}
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
    return switch (TypeName) {
        case "DATE" -> "java.sql.date";
        case "DATETIME" -> "java.sql.timestamp";
        case "TIMESTAMP" -> "java.sql.timestamp";
        case "CHARACTER" -> "String";
        case "VARCHAR" -> "String";
        case "CHAR" -> "String";
        case "BIT" -> "Boolean";
        case "DECIMAL" -> "java.math.BigDecimal";
        case "TINYINT" -> "Short";
        case "TINYINT UNSIGNED" -> "Short";
        case "SMALLINT" -> "Short";
        case "INTEGER" -> "Int";
        case "INT" -> "Int";
        case "INT UNSIGNED" -> "Int";
        case "DOUBLE PRECISION" -> "Double";
        case "VARBINARY" -> "byte[]";
        default -> "*** Not FOUND ***";
    };
    }

public static String repla(String str) {
    String r = str.replace("idclub", "Idclub");
           r = str.replace("idcourse", "Idcourse");
    return r;
}

public static String datetime(String c) {
        LOG.debug("datetime for =" + c); // RoundDate
        StringBuilder sb9 = new StringBuilder();
        //java.util.Date d = rs.getTimestamp('RoundDate')'
        sb9.append(NEW_LINE)
           .append("java.util.Date d = rs.getTimestamp(')")
           .append(c)
           .append("')")
           .append(NEW_LINE)
           .append("LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();");
    //    .append()
     //   sb9.append(sb9).append(NEW_LINE).append
   //  LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
  //    sr.setRoundDate(date);
    return sb9.toString();
}

public static String setterGenerator(Connection conn, String table) throws SQLException{
    ResultSet rs = null;
try{
    DatabaseMetaData meta = conn.getMetaData();
    List<String> blacklist = Arrays.asList("clubzoneid", "clubmodificationdate", "coursemodificationdate",
        "", "");
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = new StringBuilder();
     rs = meta.getColumns(null, null, table, null);
      LOG.debug(NEW_LINE + "List of columns: for table = " + table + NEW_LINE);
      String b = "";
      String s = "";
      String r = "";
      String t = "";
      String c = "";
      while (rs.next()) {
                LOG.debug("column name = " + rs.getString("COLUMN_NAME"));
            c = rs.getString("COLUMN_NAME");
                LOG.debug("type name = " + rs.getString("TYPE_NAME"));
            t = rs.getString("TYPE_NAME");
            b = rs.getString("COLUMN_NAME").toLowerCase(); // 4.COLUMN_NAME String => column name 
            if(blacklist.contains(b))
                { LOG.debug(" *** rejected field ***  = " + b);
                continue;
            }
          r = repla(rs.getString("COLUMN_NAME"));  // idclub devient Idclub
            LOG.debug(" replaced = " + r);
            if("DATETIME".equals(t))
            {
                    LOG.debug("DATETIME");
                String d = datetime(c);
                    LOG.debug("return DATETIME = " + d);
            }
          s = SqltoJava(rs.getString("TYPE_NAME"));  // converstion SQL data type to Java data type

          sb.setLength(0);  // initialize
          sb.append(NEW_LINE);
          sb.append("sg.")   //setter generator
         .append("set")
         .append(r)
         .append("(rs.get")
         .append(s)
         .append("('")
         .append(r)
         .append("'));")
 ;
         LOG.debug("stringbuider = " + sb);
         sb2.append(sb);
      }
         
         LOG.debug("stringbuider 22222 = " + sb2);
         return sb2.toString();
   //    }
         //java.util.Date d = rs.getTimestamp("roundDate");
   //      LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
   //      sr.setRoundDate(date);
         
    //     type name = DATETIME
//2017-08-16T18:52:57,278 923  INFO utils.DBMeta setterGenerator DBMeta.java - stringbuider =
//ccr.setRoundDate(rs.getjava.sql.timestamp('RoundDate'));
         
     //    LOG.debug("column size = " + res.getInt("COLUMN_SIZE"));
     //    LOG.debug("nullable = " + res.getInt("NULLABLE")); 



/*
     
     
     
 //ResultSet resultSet = meta.getTypeInfo();
  rs = meta.getTypeInfo();
     rs.beforeFirst();
       LOG.debug(NEW_LINE + "List TYPE and DATA: " + NEW_LINE); 
    while (rs.next()) {
      typeName = rs.getString("TYPE_NAME");
        LOG.debug("typename T/D = " + typeName);
      short dataType = rs.getShort("DATA_TYPE");
      LOG.debug("datatype T/D = " + dataType);
      getJdbcTypeName(dataType);
    }

    rs.beforeFirst(); 
 while(rs.next())
{
  //  String columnName = result.getString(4);
      LOG.debug("COLUMN_NAME = " + rs.getString(4));
  //  int columnType = result.getInt(5);
       LOG.debug("DATA_TYPE = " + rs.getInt(5));
//    int columnPosition = result.getInt(15);
      LOG.debug("ORDINAL_POSITION = " + rs.getInt(17));
      LOG.debug("DECIMAL_DIGITS = " + rs.getInt(9));
      LOG.debug("COLUMN_SIZE = " + rs.getInt(7));
} //end while
 

rs.beforeFirst();
StringBuilder sb = new StringBuilder();
   while(rs.next())
    {
        sb.append(rs.getString(4));
        sb.append(", ");
 //       LOG.debug("inside loop, sb = " + sb);
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //   LOG.debug("sb capacity = " + sb.capacity());
        LOG.debug("sb = " + sb);
     */

   

}catch(Exception e){
      LOG.error("Exception n setterGenerator " + e);
      return null;
}finally{
    //LOG.error("finally2 executed ! ");
    if (rs != null) try { rs.close(); } catch (Exception ignored) {}
  //  return null;
}

} // end method
public static int CountColumns(Connection conn, String table) throws SQLException{
    ResultSet rs = null;
    PreparedStatement ps = null;
try{
    final String query = "SELECT count(*)" +
        " FROM information_schema.columns" +
        " WHERE " +
        " table_schema = ?" +  // new 12-04-2019 comptait 2 x car 2 db avec le même nom de table !!!
        " AND table_name = ?";
    ps = conn.prepareStatement(query);
    ps.setString(1, conn.getCatalog()); // database name
    ps.setString(2, table);
    
 //   utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    if(rs.next()){  
//        LOG.debug("resultat : CountColumns = " + rs.getInt(1) );
      return rs.getInt(1);
    }else{
        LOG.error("error : no result found no columns !! = " + rs.getInt(1) );
        return 99;  //error code
    }
}catch (SQLException e){
	LOG.error("SQLException in CountColumns : " + e);
        return 0;
        //throw e;
}finally{
    rs.close();
    ps.close();
}
} // end method
public static String listMetaColumnsLoad (Connection conn, String table) throws SQLException{
    ResultSet rs = null;
//https://docs.oracle.com/javase/8/docs/api/java/sql/DatabaseMetaData.html#getColumns-java.lang.String-java.lang.String-java.lang.String-java.lang.String-
try{
    validateIdentifier(table); // security audit 2026-03-09
///    LOG.debug("starting listMetaColumnsLoad for table = " + table );
    DatabaseMetaData meta = conn.getMetaData();
  rs = meta.getColumns(conn.getCatalog(), null, table, null);
 //    LOG.debug("database catalog = " + conn.getCatalog());
  rs.beforeFirst();
  StringBuilder sb = new StringBuilder();
  sb.append(" "); // space to separate query element
   while(rs.next()){
        sb.append(rs.getString("TABLE_NAME")).append(".").append(rs.getString("COLUMN_NAME"));
   //     if(rs.getString("COLUMN_NAME").equals("player_has_round")){
  //          LOG.debug("sb is now = " +sb.toString());
     //   }
        sb.append(", ");
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
 //  LOG.debug("MetaDataColumns = " + sb.toString());
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
    if (rs != null) try { rs.close(); } catch (Exception ignored) {}
}

} // end method

public record ColumnMeta(  // new 16-12-2025
        String name,
        int jdbcType,
        String sqlType,
        int size,
        boolean nullable,
        String defaultValue,
        boolean is_auto_increment,
        boolean is_generated_column
) {}

public static List<ColumnMeta> getTableColumns( // new 16-12-2025
        Connection connection,
        String schema,
        String tableName
) throws SQLException {
    LOG.debug("entering getTableColumns");
    DatabaseMetaData meta = connection.getMetaData();
    List<ColumnMeta> columns = new ArrayList<>();

    try (ResultSet rs = meta.getColumns(
            connection.getCatalog(),   // catalog (souvent null en MySQL)
            schema,                    // schema (database name)
            tableName,
            null                        // toutes les colonnes
    )) {
        while (rs.next()) {
            ColumnMeta col = new ColumnMeta(
                    rs.getString("COLUMN_NAME"),
                    rs.getInt("DATA_TYPE"),          // java.sql.Types
                    rs.getString("TYPE_NAME"),       // VARCHAR, INT, etc.
                    rs.getInt("COLUMN_SIZE"),
                 //   rs.getBoolean("NULLABLE") == DatabaseMetaData.columnNullable,
                    // DatabaseMetaData.columnNoNulls = 0 /DatabaseMetaData.columnNullable  = 1 /DatabaseMetaData.columnNullableUnknown = 2
                    rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable, 
                    rs.getString("COLUMN_DEF"),
                    "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")),
                    "YES".equalsIgnoreCase(rs.getString("IS_GENERATEDCOLUMN"))
            );
            columns.add(col);
        }
    }
     Set<String> pk = new HashSet<>();
        try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
        while (rs.next()) {
            pk.add(rs.getString("COLUMN_NAME"));
        }
        LOG.debug("pk = " + pk.toString()); //pour club donne [idclub] 
    }
       
    return columns;
    
}
/*
public String generateInsert(
        String table,
        List<ColumnMeta> columns
) {
    var insertable = columns.stream()
            .filter(c -> !c.isAutoIncrement())
            .toList();

    String fields = insertable.stream()
            .map(ColumnMeta::getName)
            .collect(Collectors.joining(", "));

    String values = insertable.stream()
            .map(c -> "?")
            .collect(Collectors.joining(", "));

    return "INSERT INTO " + table +
            " (" + fields + ") VALUES (" + values + ")";
}
*/





private Set<String> readPrimaryKeys(
        DatabaseMetaData meta,
        String schema,
        String tableName
) throws SQLException {

    Set<String> pk = new HashSet<>();

    try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
        while (rs.next()) {
            pk.add(rs.getString("COLUMN_NAME"));
        }
    }
    return pk;
}
public static String listMetaColumnsUpdate (final Connection conn, final String table) throws SQLException{
    ResultSet rs = null;
try{
    validateIdentifier(table); // security audit 2026-03-09
    DatabaseMetaData meta = conn.getMetaData();
  //  String   catalog          = null;
  //  String   schemaPattern    = null;
  //  String   tableNamePattern = table;
   // String   columnNamePattern = null;

rs = meta.getColumns(conn.getCatalog(), null, table, null);
rs.first();  // grosse astuce ! ne pas prendre la première field : idplayer, idclub, etc ...
     // parce que c'est toujours ?? la clé ??
StringBuilder sb = new StringBuilder();
 // http://mysql-0v34c10ck.blogspot.com/2011/05/better-way-to-get-primary-key-columns.html
// les colonnes suivantes ne doivent PAS être MAJ en update car ce sont des clés ou des zones protégées
List<String> blacklist = Arrays.asList( 
        "playerphotolocation", "playerActivation", "playermodificationdate" , 
        "playerpassword", "playerPreviousPasswords",
        "clubmodificationdate","club_idclub",
        "coursemodificationdate","course_idcourse",// "courseholes",
        "teemodificationdate", "tee_idtee", "tee_course_idcourse",
        "holenumber", "holemodificationdate",
        "auditstartdate","auditmodificationdate",
        "CmpDataCompetitionId"
        );
// tout en minuscules
   for(int i=0,l=blacklist.size();i<l;++i){
      blacklist.set(i, blacklist.get(i).toLowerCase());
      // il faudrait aussi enlever toutce qui a "modificationdate"
   }
   
   String s = "";
   while(rs.next()){
       s = rs.getString(4).toLowerCase(); // 4.COLUMN_NAME String => column name 
        if(blacklist.contains(s)){
            LOG.debug("We SKIP this column !!= " + s);
             continue;
        }else{
          //  LOG.debug("We will update this column = " + s);
        }
        sb.append(s).append("=?, "); 
 //       LOG.debug("inside loop, sb = " + sb);
    }  //end while
  sb.deleteCharAt(sb.lastIndexOf(","));// delete dernière virgule
        LOG.debug("sb listMetaColumnsUpdate = " + sb);
  return sb.toString();
}catch(Exception e){
      LOG.error("Exception " + e);
      return null;
}finally{
    if (rs != null) try { rs.close(); } catch (Exception ignored) {}
}
} //end method

public static void listMetaStoredPro(Connection conn, String nomProcedure)
        throws SQLException
{   DatabaseMetaData meta = conn.getMetaData();
    ResultSet rs = meta.getProcedureColumns(conn.getCatalog(),null,nomProcedure,"%");
        LOG.debug("listMetaStoredPro : ############## " + nomProcedure);
    while(rs.next()){
        LOG.debug("\n");
        LOG.debug("Nom parametre  = " + rs.getString("COLUMN_NAME"));
        LOG.debug("Type paramètre = " + rs.getInt("COLUMN_TYPE"));
        LOG.debug("Type SQL       = " + rs.getString("TYPE_NAME"));
    }
     LOG.debug("end listMetaStoredPro : ############## " + nomProcedure);
    rs.close();
 //   meta.close());
} // end method


//public static void cursorHoldabilitySupport(Connection conn) throws SQLException {
public static void cursorHoldabilitySupport(DatabaseMetaData meta) throws SQLException {

    
  //  DatabaseMetaData dbMetaData = conn.getMetaData();
    LOG.debug("ResultSet.HOLD_CURSORS_OVER_COMMIT = " +
        ResultSet.HOLD_CURSORS_OVER_COMMIT);

    LOG.debug("ResultSet.CLOSE_CURSORS_AT_COMMIT = " +
        ResultSet.CLOSE_CURSORS_AT_COMMIT);

    LOG.debug("Default cursor holdability: " +
        meta.getResultSetHoldability());

    LOG.debug("Supports HOLD_CURSORS_OVER_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.HOLD_CURSORS_OVER_COMMIT));

    LOG.debug("Supports CLOSE_CURSORS_AT_COMMIT? " +
        meta.supportsResultSetHoldability(
            ResultSet.CLOSE_CURSORS_AT_COMMIT));
}


/*
void main() throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering {}", methodName);
    // requires CDI container — cannot run standalone
} // end main
*/
} // end class