package test.prepareStatement;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class DBUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> PreparedStatement prepareStatementFromPojo(Connection conn, String tableName, T pojo) {
        try {
            var fields = getAllFields(pojo.getClass());

            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();

            for (Field field : fields) {
                DbColumn annotation = field.getAnnotation(DbColumn.class);
                if (annotation != null && annotation.ignore()) continue;

                columns.append(camelToSnake(field.getName())).append(", ");
                placeholders.append("?, ");
            }

            // Supprime les dernières virgules
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

                // Valeur par défaut
                if ((value == null || isEmptyString(value)) && annotation != null && !annotation.defaultValue().isEmpty()) {
                    value = annotation.defaultValue();
                }

                index++;
                setPreparedStatementValue(ps, index, value);
            }

            return ps;

        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement from POJO", e);
        }
    }

    private static void setPreparedStatementValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.VARCHAR);
            return;
        }

        // Switch pattern matching pour types connus
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
            case Collection<?> c -> ps.setString(index, toJson(c));
            case Object[] arr -> ps.setString(index, toJson(arr));
            default -> {
                if (!isPrimitiveOrWrapperOrString(value)) {
                    // Sous-objet complexe → JSON
                    ps.setString(index, toJson(value));
                } else {
                    ps.setObject(index, value);
                }
            }
        }
    }

 //   private static boolean isPrimitiveOrWrapperOrString(Object obj) {
 //       if (obj == null) return true;
 //       return switch (obj) {
 //           case String s, Integer i, Long l, Double d, Float f, Boolean b, Timestamp t, java.util.Date date, BigDecimal bd -> true;
 //           default -> false;
 //       };
 //   }

    private static boolean isEmptyString(Object value) {
        return value instanceof String s && s.isEmpty();
    }

    private static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
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

    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
