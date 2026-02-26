
package test;

import test.prepareStatement.DbColumn;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;

public class prepareStatementFromPojo {

    public static <T> PreparedStatement prepareStatementFromPojo(Connection conn, String tableName, T pojo) {
        try {
            // Récupération de tous les champs, y compris les sous-objets
            var fields = getAllFields(pojo.getClass());

            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();

            for (Field field : fields) {
                DbColumn annotation = field.getAnnotation(DbColumn.class);
                if (annotation != null && annotation.ignore()) continue;

    //           String columnName = camelToSnake(field.getName());
    //            columns.append(columnName).append(", ");
                placeholders.append("?, ");
            }

            // Supprimer la dernière virgule
            if (columns.length() > 0) columns.setLength(columns.length() - 2);
            if (placeholders.length() > 0) placeholders.setLength(placeholders.length() - 2);

            String sql = """
                INSERT INTO %s (%s) VALUES (%s)
                """.formatted(tableName, columns, placeholders);

            PreparedStatement ps = conn.prepareStatement(sql);

            int index = 0;
            for (Field field : fields) {
                DbColumn annotation = field.getAnnotation(DbColumn.class);
                if (annotation != null && annotation.ignore()) continue;

                field.setAccessible(true);
                Object value = field.get(pojo);

                // Valeur par défaut si null
                if ((value == null || isEmptyString(value)) && annotation != null && !annotation.defaultValue().isEmpty()) {
                    value = annotation.defaultValue();
                }

                // Gestion des sous-objets
                if (!isPrimitiveOrWrapperOrString(value)) {
                    var subFields = getAllFields(value.getClass());
                    for (Field sub : subFields) {
                        sub.setAccessible(true);
                        Object subValue = sub.get(value);
                        setPreparedStatementValue(ps, ++index, subValue);
                    }
                } else {
                    setPreparedStatementValue(ps, ++index, value);
                }
            }

            return ps;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement from POJO", e);
        }
    }

    private static boolean isPrimitiveOrWrapperOrString(Object obj) {
        if (obj == null) return true;
        return switch (obj) {
    //        case String s, Integer i, Long l, Double d, Float f, Boolean b, Timestamp t, java.util.Date date, BigDecimal bd -> true;
            default -> false;
        };
    }
private static boolean isSimple(Object o) {
    return switch (o) {
        case String _,
             Integer _,
             Long _,
             Double _,
             Float _,
             Boolean _,
             Timestamp _,
             java.util.Date _,
             BigDecimal _ -> true;
        default -> false;
    };
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
        default -> isSimple(value) ? value : toJson(value);
    };
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

    private static void setPreparedStatementValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            switch (value) {
                case String s -> ps.setString(index, s);
                case Integer i -> ps.setInt(index, i);
                case Long l -> ps.setLong(index, l);
                case Double d -> ps.setDouble(index, d);
                case Float f -> ps.setFloat(index, f);
                case Boolean b -> ps.setBoolean(index, b);
                case Timestamp t -> ps.setTimestamp(index, t);
                case java.util.Date date -> ps.setTimestamp(index, new Timestamp(date.getTime()));
                case BigDecimal bd -> ps.setBigDecimal(index, bd);
                default -> ps.setObject(index, value);
            }
        }
    }

    private static boolean isEmptyString(Object value) {
        return value instanceof String s && s.isEmpty();
    }

    private static Field[] getAllFields(Class<?> type) {
        if (type == null || type == Object.class) return new Field[0];
        Field[] parentFields = getAllFields(type.getSuperclass());
        Field[] ownFields = type.getDeclaredFields();
        Field[] all = new Field[parentFields.length + ownFields.length];
        System.arraycopy(parentFields, 0, all, 0, parentFields.length);
        System.arraycopy(ownFields, 0, all, parentFields.length, ownFields.length);
        return all;
    }
}
