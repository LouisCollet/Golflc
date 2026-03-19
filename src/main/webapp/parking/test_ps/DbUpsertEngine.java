
package test.prepareStatement;

import com.fasterxml.jackson.databind.ObjectMapper;
import static interfaces.Log.LOG;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public final class DbUpsertEngine {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DbUpsertEngine() {
        // utility class
    }

    /* =========================================================
       UPSERT
       ========================================================= */
    public static <T> PreparedStatement autoUpsert(
            Connection conn,
            String table,
            T pojo,
            DbMetadataCache metadata) {

        try {
            List<String> pkColumns = metadata.getPrimaryKeys(conn, table);

            if (pkColumns.isEmpty()) {
                throw new IllegalStateException("No primary key found for table " + table);
            }

            Map<String, Object> pkValues = extractPrimaryKeyValues(pojo, pkColumns);

            if (exists(conn, table, pkColumns, pkValues)) {
                return autoUpdate(conn, table, pojo, metadata);
            }
            return autoInsert(conn, table, pojo, metadata);

        } catch (Exception e) {
            throw new RuntimeException("UPSERT failed for table " + table, e);
        }
    }

    /* =========================================================
       INSERT
       ========================================================= */
    public static <T> PreparedStatement autoInsert(
            Connection conn,
            String table,
            T pojo,
            DbMetadataCache metadata) {

        try {
            utils.DBMeta.validateIdentifier(table); // security audit 2026-03-09
            LOG.debug("entering autoInsert");
            LOG.debug("with table = " + table);
            LOG.debug("with pojo = " + pojo);
            Set<String> dbColumns = metadata.getColumns(conn, table);
            Field[] fields = getAllFields(pojo.getClass());

            StringBuilder cols = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            List<Object> parameters = new ArrayList<>();

            for (Field field : fields) {
                DbColumn col = field.getAnnotation(DbColumn.class);
                if (col != null && col.ignore()) continue;

                String columnName = camelToSnake(field.getName());
                if (!dbColumns.contains(columnName)) continue;

                field.setAccessible(true);
                Object value = resolveValue(field.get(pojo), col);

                cols.append(columnName).append(", ");
                vals.append("?, ");
                parameters.add(toJdbcValue(value));
            }

            trim(cols);
            trim(vals);

            String sql = """
                INSERT INTO %s (%s)
                VALUES (%s)
                """.formatted(table, cols, vals);

            PreparedStatement ps = conn.prepareStatement(sql);
            bind(ps, parameters);
            return ps;

        } catch (Exception e) {
            throw new RuntimeException("INSERT failed for table " + table, e);
        }
    }

    /* =========================================================
       UPDATE
       ========================================================= */
    public static <T> PreparedStatement autoUpdate(
            Connection conn,
            String table,
            T pojo,
            DbMetadataCache metadata) {

        try {
            utils.DBMeta.validateIdentifier(table); // security audit 2026-03-09
            Set<String> dbColumns = metadata.getColumns(conn, table);
            List<String> pkColumns = metadata.getPrimaryKeys(conn, table);
            Field[] fields = getAllFields(pojo.getClass());

            StringBuilder set = new StringBuilder();
            StringBuilder where = new StringBuilder();
            List<Object> setValues = new ArrayList<>();
            List<Object> whereValues = new ArrayList<>();

            for (Field field : fields) {
                DbColumn col = field.getAnnotation(DbColumn.class);
                if (col != null && col.ignore()) continue;

                String column = camelToSnake(field.getName());
                if (!dbColumns.contains(column)) continue;

                field.setAccessible(true);
                Object value = resolveValue(field.get(pojo), col);

                if (pkColumns.contains(column)) {
                    where.append(column).append(" = ? AND ");
                    whereValues.add(toJdbcValue(value));
                } else {
                    set.append(column).append(" = ?, ");
                    setValues.add(toJdbcValue(value));
                }
            }

            trim(set);
            trim(where, " AND ");

            String sql = """
                UPDATE %s
                SET %s
                WHERE %s
                """.formatted(table, set, where);

            PreparedStatement ps = conn.prepareStatement(sql);
            bind(ps, setValues);
            bind(ps, whereValues, setValues.size());
            return ps;

        } catch (Exception e) {
            throw new RuntimeException("UPDATE failed for table " + table, e);
        }
    }

    /* =========================================================
       INTERNAL HELPERS
       ========================================================= */

    private static <T> Map<String, Object> extractPrimaryKeyValues(T pojo, List<String> pkColumns)
            throws IllegalAccessException {

        Map<String, Object> pk = new LinkedHashMap<>();
        for (Field f : getAllFields(pojo.getClass())) {
            String column = camelToSnake(f.getName());
            if (pkColumns.contains(column)) {
                f.setAccessible(true);
                pk.put(column, f.get(pojo));
            }
        }
        return pk;
    }

    private static boolean exists(
            Connection conn,
            String table,
            List<String> pkColumns,
            Map<String, Object> pkValues) throws SQLException {

        String where = pkColumns.stream()
                .map(c -> c + " = ?")
                .reduce((a, b) -> a + " AND " + b)
                .orElseThrow();

        utils.DBMeta.validateIdentifier(table); // security audit 2026-03-09
        String sql = "SELECT 1 FROM " + table + " WHERE " + where;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (String col : pkColumns) {
                bind(ps, i++, toJdbcValue(pkValues.get(col)));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Object resolveValue(Object value, DbColumn col) {
        if ((value == null || isEmpty(value)) && col != null && !col.defaultValue().isEmpty()) {
            return col.defaultValue();
        }
        return value;
    }

    private static Object toJdbcValue(Object value) {
        if (value == null) return null;

        return switch (value) {
            case String s -> s;
            case Integer i -> i;
            case Long l -> l;
            case Double d -> d;
            case Float f -> f;
            case Boolean b -> b;
            case Timestamp t -> t;
            case java.util.Date d -> new Timestamp(d.getTime());
            case BigDecimal bd -> bd;
            case Collection<?> c -> toJson(c);
            default -> isSimple(value) ? value : toJson(value);
        };
    }

    private static void bind(PreparedStatement ps, List<Object> values) throws SQLException {
        bind(ps, values, 0);
    }

    private static void bind(PreparedStatement ps, List<Object> values, int offset) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            set(ps, offset + i + 1, values.get(i));
        }
    }

    private static void bind(PreparedStatement ps, int index, Object value) throws SQLException {
        set(ps, index, value);
    }

    private static void set(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
            return;
        }

        switch (value) {
            case String s -> ps.setString(index, s);
            case Integer i -> ps.setInt(index, i);
            case Long l -> ps.setLong(index, l);
            case Double d -> ps.setDouble(index, d);
            case Float f -> ps.setFloat(index, f);
            case Boolean b -> ps.setBoolean(index, b);
            case Timestamp t -> ps.setTimestamp(index, t);
            case BigDecimal bd -> ps.setBigDecimal(index, bd);
            default -> ps.setObject(index, value);
        }
    }

    private static boolean isSimple(Object o) {
        return o instanceof String
                || o instanceof Number
                || o instanceof Boolean
                || o instanceof Timestamp
                || o instanceof java.util.Date;
    }

    private static boolean isEmpty(Object v) {
        return v instanceof String s && s.isEmpty();
    }

    private static void trim(StringBuilder sb) {
        if (sb.length() >= 2) sb.setLength(sb.length() - 2);
    }

    private static void trim(StringBuilder sb, String suffix) {
        if (sb.toString().endsWith(suffix)) {
            sb.setLength(sb.length() - suffix.length());
        }
    }

    private static Field[] getAllFields(Class<?> type) {
        if (type == null || type == Object.class) return new Field[0];
        Field[] parent = getAllFields(type.getSuperclass());
        Field[] own = type.getDeclaredFields();
        Field[] all = Arrays.copyOf(parent, parent.length + own.length);
        System.arraycopy(own, 0, all, parent.length, own.length);
        return all;
    }

    private static String camelToSnake(String s) {
        return s.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private static String toJson(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }
}
