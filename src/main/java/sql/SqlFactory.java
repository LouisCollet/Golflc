package sql;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static interfaces.Log.TAB;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import utils.DBMeta;

public class SqlFactory {
     
    // remarque n'est utilisée que pour table club à titre de test
    // projet abandonné
    // maintien de l'ancienne solutiion ...
      // -----------------------------
    // ColumnMeta record
    // -----------------------------
    public record ColumnMeta(
            String name,
            int jdbcType,
            String sqlType,
            int size,
            boolean nullable,
            boolean primaryKey,
            boolean foreignKey,
            boolean autoIncrement
    ) {}
    
  private Set<String> readPrimaryKeys(DatabaseMetaData meta, String schema, String tableName) throws SQLException {
        Set<String> pk = new HashSet<>();
        try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                pk.add(rs.getString("COLUMN_NAME"));
            }
        }
        return pk;
    }
  
  private Set<String> readForeignKeys(
        DatabaseMetaData meta,
        String schema,
        String tableName) throws SQLException {

    Set<String> fk = new HashSet<>();

    try (ResultSet rs = meta.getImportedKeys(null, schema, tableName)) {
        while (rs.next()) {
            fk.add(rs.getString("FKCOLUMN_NAME"));
        }
    }
    return fk;
}
//-----------------------------
    // Lire les colonnes d’une table
    // -----------------------------
 private List<ColumnMeta> readTableMeta(Connection conn, String tableName) throws SQLException {
    try{
           LOG.debug("entering readTableMeta");
           LOG.debug("tableName = " + tableName);
        DatabaseMetaData meta = conn.getMetaData();
            LOG.debug("catalog = " + conn.getCatalog());
        Set<String> primaryKeys = readPrimaryKeys(meta, conn.getCatalog(), tableName);
            LOG.debug("primaryKeys = " + primaryKeys + " will be SKIPPED");
        Set<String> foreignKeys = readForeignKeys(meta, conn.getCatalog(), tableName); // bew 19-12-2025 pour clb_idclub etc...
            LOG.debug("foreignKeys = " + foreignKeys + " will be NOT SKIPPED");    // faux dans club pour clublocaladmin
        List<ColumnMeta> columns = new ArrayList<>();
        try (ResultSet rs = meta.getColumns(null, conn.getCatalog(), tableName, null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    LOG.debug("column name = " + columnName);
                    if(columnName.contains("ModificationDate")){  // skip car générée dans table sql via CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                        LOG.debug("column name SKIPPED : contains ModificationDate ! "); // TODO vérifier si TIMESTAMP
                        continue; // start next iteration
                    }
                    boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                    boolean autoIncrement = "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")); // ?? autoincrement et primary key même chose ?
                    boolean primaryKey = primaryKeys.contains(columnName);
                    boolean foreignKey = foreignKeys.contains(columnName);
                    
                    // Ignorer complètement les foreign keys
                    if (foreignKey) {
                        LOG.debug("column name no more SKIPPED : foreign key = " + columnName);
                      //continue;
                    }
                    
                    columns.add(new ColumnMeta(
                            columnName,

                            rs.getInt("DATA_TYPE"),
                            rs.getString("TYPE_NAME"),
                            rs.getInt("COLUMN_SIZE"),
                            nullable,
                            primaryKey,
                            foreignKey,
                            autoIncrement
                    ));
                } // end while
            columns.forEach(item -> LOG.debug("columns : " + item.sqlType));
            return columns;
            } //end try 2
    }catch(Exception e){
          LOG.error("Exception in generateUpdate(" + e);
          return null;        
    } 
   } //end method
 
  public String generateQueryUpdate(Connection conn, String tableName){ // called from update.UpdateClub
    try{
            LOG.debug("entering generateQueryUpdate");
            LOG.debug("tableName = " + tableName);
        List<ColumnMeta> columns =  readTableMeta(conn, tableName);
            //LOG.debug("columns = " + columns);
            columns.forEach(item -> LOG.debug("columns : " + item));
            LOG.debug("columns size = " + columns.size());
  // here is the magic !
        List<ColumnMeta> updateCols = columns.stream().filter(c -> !c
                .primaryKey()
            //    && !c.foreignKey()
                && !c.autoIncrement() ) // 19-12-2025 ajouté foreignKey pour éviter club_idclub
                .toList();
            LOG.debug("#updateCols NOT primary ad NOT Foreing and NOT AUTOIncrement= " + updateCols.size());
        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
            LOG.debug("#pkCols primary key = " + pkCols.size());
        List<ColumnMeta> foreignCols = columns.stream().filter(ColumnMeta::foreignKey).toList();
            LOG.debug("#pkCols foreign key = " + foreignCols.size());    
        if (pkCols.isEmpty()) throw new IllegalStateException("in generateUpdate : Table " + tableName + " has no primary key");
        String setClause = updateCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(", "));
            LOG.debug("setClause = " + setClause);
        // utiliser ? generateUpdateSetClause
        String whereClause = pkCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(" AND "));
            LOG.debug("whereClause = " + whereClause);
        String s = "UPDATE " + tableName + " SET " + setClause + " WHERE " + whereClause;
        LOG.debug("String update generated = " + s);
        
    //    LOG.debug("String SET generated = " + generateUpdateSetClause(columns));
     //   String tb = generateUpdateSql(tableName, setClause);
      //  LOG.debug("String text blocks generated = " + tb);
        return s;
/*
final String query = """
    UPDATE club
    SET %s
    WHERE club.idclub=?;
""".formatted(cl);
        
        
        
        return s;
*/
    }catch(Exception e){
      LOG.error("Exception in generateQueryUpdate(" + e);
      return null;
}finally{
    //DBConnection.closeQuietly(null, null, rs, null);
}
    } //end method 
    //old solution
    public String listMetaColumnsUpdate2 (final Connection conn, final String table) throws SQLException{
    try{
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, null)) {
            LOG.debug("We SKIP the first column !! ");
            rs.first();  // ne pas prendre la première field : idplayer, idclub, etc ...
            StringBuilder sb = new StringBuilder();
            List<String> blacklist = Arrays.asList(
                "playerphotolocation", "playerActivation", "playermodificationdate","playerpassword", "playerPreviousPasswords",
                "clubmodificationdate","club_idclub",
                "coursemodificationdate","course_idcourse",
                "teemodificationdate", "tee_idtee", "tee_course_idcourse",
                "holenumber", "holemodificationdate",
                "auditstartdate","auditmodificationdate",
                "CmpDataCompetitionId"
            );
            for(int i=0,l=blacklist.size();i<l;++i){
                blacklist.set(i, blacklist.get(i).toLowerCase());
            }
            while(rs.next()){
                String s = rs.getString(4).toLowerCase();
                if(blacklist.contains(s)){
                    LOG.debug("We SKIP this column from the blacklist !!= " + s);
                    continue;
                }
                sb.append(s).append(" = ?, ");
            }  //end while
            sb.deleteCharAt(sb.lastIndexOf(","));
            LOG.debug("sb listMetaColumnsUpdate = " + sb);
            return sb.toString();
        } // end try ResultSet
    }catch(Exception e){
        LOG.error("Exception " + e);
        return null;
    }
} //end method

    // modification chatgpt
public String listMetaColumnsUpdate(final Connection conn, final String table) throws SQLException {
    try {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, null)) {
            StringBuilder sb = new StringBuilder();
            Set<String> blacklist = new HashSet<>(Arrays.asList(
                "playerphotolocation", "playeractivation",
                "playerpassword", "playerpreviouspasswords",
                "clubmodificationdate", "club_idclub",
                "coursemodificationdate", "course_idcourse",
                "teemodificationdate", "tee_idtee", "tee_course_idcourse",
                "holenumber", "holemodificationdate",
                "auditstartdate", "auditmodificationdate",
                "cmpdatacompetitionid"
            ));
            boolean firstColumnSkipped = false;
            while (rs.next()) {
                if (!firstColumnSkipped) {
                    firstColumnSkipped = true;
                    continue;
                }
                String column = rs.getString("COLUMN_NAME").toLowerCase();
                if (blacklist.contains(column) || column.contains("modificationdate")){
                    LOG.debug("We SKIP this column from the blacklist: {}", column);
                    continue;
                }
                sb.append(column).append(" = ?, ");
            }
            if (sb.length() >= 2) {
                sb.setLength(sb.length() - 2);
            }
            LOG.debug("sb listMetaColumnsUpdate = {}", sb);
            return sb.toString();
        } // end try ResultSet
    } catch (Exception e) {
        LOG.error("Exception in listMetaColumnsUpdate", e);
        throw e;
    }
}
// new 28-01 conn ??
  public UpdateSqlBuilder.SetClause listMetaColumnsUpdate3(Connection conn, String table) throws Exception {

    var columns = new sql.ColumnMetaReader().read(conn, table); //ColumnMetaReader().read(conn, table);
    return UpdateSqlBuilder.build(columns);
}
  
/*
public String listMetaColumnsUpdate3(Connection conn, String table) throws Exception {
    // 🔹 lit les métadonnées et marque les colonnes updatable
  //  List<UpdateSqlBuilder.ColumnMeta> cols = new ColumnMetaReader().read(conn, table);
  //  String sql = UpdateSqlBuilder.buildSetClause(cols);
  //  LOG.debug("string sql = " + sql);
    
  //  String setClause = UpdateSqlBuilder.buildSetClause(conn, table);
    

  
    List<UpdateSqlBuilder.ColumnMeta> cols = new ColumnMetaReader().read(conn, table);
    UpdateSqlBuilder.SetClauseResult result = UpdateSqlBuilder.buildSetClauseWithCount(cols);

System.out.println("Clause SET : " + result.clause());
System.out.println("Nombre de colonnes : " + result.columnCount());
    
    
  //   LOG.debug("string setClause = " + setClause);
    // 🔹 construit la clause SET filtrée
   // return UpdateSqlBuilder.buildSetClause(cols);
    return setClause;
}
*/
private static void debugChars(String label, String s) {
    LOG.debug("{} length={}", label, s.length());
    for (int i = 0; i < s.length(); i++) {
        LOG.debug("{}[{}] = '{}' (U+{})",
            label,
            i,
            s.charAt(i),
            String.format("%04X", (int) s.charAt(i))
        );
    }
}

    
    
    
    
    // old solution
    public static String generateInsertQuery (Connection conn, String table) throws SQLException, Exception{
        // utilisé pour gestion des database, SQL requests
    final String methodName = utils.LCUtil.getCurrentMethodName();
  try{
        int times = DBMeta.CountColumns(conn, table);
        //LOG.debug("times = " + times);
    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ")
        .append(table)
        .append(NEW_LINE)
        .append(" VALUES (");
    for(int i=0; i<times; i++){
        sb.append(TAB)
            .append("?,")
            .append(NEW_LINE); // = parameters placeholders, one par field
    }
    sb.deleteCharAt(sb.lastIndexOf(","))
            .append(");"); // delete dernière virgule
    //   LOG.debug("generated sb = " + sb);
    return sb.toString();
} catch(SQLException e){ // mod 15-12-2025 suggestion chatgpt
    handleSQLException(e, methodName);
    return null;
} catch (Exception e) {
    handleGenericException(e, methodName);
    return null;   
    
    
}
} // end method

public static void logps(PreparedStatement ps){
try{
 ///       LOG.debug("entering logps");
    String p = ps.toString();
    if(p.contains("ClientPreparedStatement")){ // connector-j 8xxx
        LOG.debug("Prepared Statement after bind variables set = "
                + NEW_LINE.repeat(2)
                + p.substring(p.indexOf(":")+2 , p.length() ));
    }else{
      //   LOG.debug("pooled connection for ");
    }
   }catch (Exception e){
        LOG.error("logps Exception " + e);
      }
}

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux — DBConnection removed 2026-02-28
} // end main
*/

} //end class