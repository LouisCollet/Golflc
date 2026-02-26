
package sql;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CrudService {
/*
    @Resource(lookup = "java:/jdbc/MyDS")
    private DataSource dataSource;

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
            boolean autoIncrement
    ) {}

    // -----------------------------
    // Lire les colonnes d’une table
    // -----------------------------
    public List<ColumnMeta> readTableMeta(String schema, String tableName) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            Set<String> primaryKeys = readPrimaryKeys(meta, schema, tableName);
            List<ColumnMeta> columns = new ArrayList<>();

            try (ResultSet rs = meta.getColumns(null, schema, tableName, null)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    boolean nullable = rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
                    boolean autoIncrement = "YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT"));
                    boolean primaryKey = primaryKeys.contains(columnName);

                    columns.add(new ColumnMeta(
                            columnName,
                            rs.getInt("DATA_TYPE"),
                            rs.getString("TYPE_NAME"),
                            rs.getInt("COLUMN_SIZE"),
                            nullable,
                            primaryKey,
                            autoIncrement
                    ));
                }
            }
            return columns;
        }
    }

    private Set<String> readPrimaryKeys(DatabaseMetaData meta, String schema, String tableName) throws SQLException {
        Set<String> pk = new HashSet<>();
        try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                pk.add(rs.getString("COLUMN_NAME"));
            }
        }
        return pk;
    }

    // -----------------------------
    // Génération SQL générique
    // -----------------------------
    private String generateInsert(String table, List<ColumnMeta> columns) {
        List<ColumnMeta> insertCols = columns.stream().filter(c -> !c.autoIncrement()).toList();
        String fields = insertCols.stream().map(ColumnMeta::name).collect(Collectors.joining(", "));
        String values = insertCols.stream().map(c -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ")";
    }

    private String generateUpdate(String table, List<ColumnMeta> columns) {
        List<ColumnMeta> updateCols = columns.stream().filter(c -> !c.primaryKey() && !c.autoIncrement()).toList();
        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
        if (pkCols.isEmpty()) throw new IllegalStateException("Table " + table + " has no primary key");

        String setClause = updateCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(", "));
        String whereClause = pkCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(" AND "));
        return "UPDATE " + table + " SET " + setClause + " WHERE " + whereClause;
    }

    private String generateDelete(String table, List<ColumnMeta> columns) {
        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
        if (pkCols.isEmpty()) throw new IllegalStateException("Table " + table + " has no primary key");
        String whereClause = pkCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(" AND "));
        return "DELETE FROM " + table + " WHERE " + whereClause;
    }

    private String generateSelectByPK(String table, List<ColumnMeta> columns) {
        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
        if (pkCols.isEmpty()) throw new IllegalStateException("Table " + table + " has no primary key");
        String whereClause = pkCols.stream().map(c -> c.name() + " = ?").collect(Collectors.joining(" AND "));
        return "SELECT * FROM " + table + " WHERE " + whereClause;
    }

    // -----------------------------
    // PreparedStatement générique
    // -----------------------------
    private PreparedStatement prepareInsert(Connection conn, String table, List<ColumnMeta> columns, Map<String,Object> values) throws SQLException {
        String sql = generateInsert(table, columns);
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int index = 1;
        for (ColumnMeta c : columns) if (!c.autoIncrement()) ps.setObject(index++, values.get(c.name()));
        return ps;
    }

    private PreparedStatement prepareUpdate(Connection conn, String table, List<ColumnMeta> columns, Map<String,Object> values) throws SQLException {
        String sql = generateUpdate(table, columns);
        PreparedStatement ps = conn.prepareStatement(sql);

        List<ColumnMeta> updateCols = columns.stream().filter(c -> !c.primaryKey() && !c.autoIncrement()).toList();
        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();

        int index = 1;
        for (ColumnMeta c : updateCols) ps.setObject(index++, values.get(c.name()));
        for (ColumnMeta c : pkCols) ps.setObject(index++, values.get(c.name()));

        return ps;
    }

    private PreparedStatement prepareDelete(Connection conn, String table, List<ColumnMeta> columns, Map<String,Object> pkValues) throws SQLException {
        String sql = generateDelete(table, columns);
        PreparedStatement ps = conn.prepareStatement(sql);

        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
        int index = 1;
        for (ColumnMeta c : pkCols) ps.setObject(index++, pkValues.get(c.name()));

        return ps;
    }

    private PreparedStatement prepareSelectByPK(Connection conn, String table, List<ColumnMeta> columns, Map<String,Object> pkValues) throws SQLException {
        String sql = generateSelectByPK(table, columns);
        PreparedStatement ps = conn.prepareStatement(sql);

        List<ColumnMeta> pkCols = columns.stream().filter(ColumnMeta::primaryKey).toList();
        int index = 1;
        for (ColumnMeta c : pkCols) ps.setObject(index++, pkValues.get(c.name()));

        return ps;
    }

    // -----------------------------
    // Mapping ResultSet → record générique
    // -----------------------------
    public static <T> T mapToRecord(ResultSet rs, Class<T> recordClass) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        Object[] values = new Object[colCount];

     //   Constructor<T> ctor = recordClass.getDeclaredConstructors()[0]; // constructeur record
      //  @SuppressWarnings("unchecked")
         Constructor<T> ctor = (Constructor<T>) recordClass.getDeclaredConstructors()[0];
        Class<?>[] types = ctor.getParameterTypes();

        for (int i = 0; i < colCount; i++) {
            Object val = rs.getObject(i + 1);
            values[i] = convert(val, types[i]);
        }
        return ctor.newInstance(values);
    }

    private static Object convert(Object val, Class<?> targetType) {
        if (val == null) return null;
        if (targetType.isInstance(val)) return val;

        if (val instanceof Number n) {
            if (targetType == Integer.class) return n.intValue();
            if (targetType == Long.class) return n.longValue();
            if (targetType == Double.class) return n.doubleValue();
            if (targetType == Float.class) return n.floatValue();
        }
        if (targetType == String.class) return val.toString();

        return val;
    }

    // -----------------------------
    // Exemples CRUD génériques
    // -----------------------------
    public <T> T selectByPK(String table, List<ColumnMeta> meta, Map<String,Object> pkValues, Class<T> recordClass) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = prepareSelectByPK(conn, table, meta, pkValues)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapToRecord(rs, recordClass);
                    }
                    return null;
                }
            }
        }
    }

    public int insert(String table, List<ColumnMeta> meta, Map<String,Object> values) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = prepareInsert(conn, table, meta, values)) {
                return ps.executeUpdate();
            }
        }
    }

    public int update(String table, List<ColumnMeta> meta, Map<String,Object> values) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = prepareUpdate(conn, table, meta, values)) {
                return ps.executeUpdate();
            }
        }
    }

    public int delete(String table, List<ColumnMeta> meta, Map<String,Object> pkValues) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = prepareDelete(conn, table, meta, pkValues)) {
                return ps.executeUpdate();
            }
        }
    }
*/
}
