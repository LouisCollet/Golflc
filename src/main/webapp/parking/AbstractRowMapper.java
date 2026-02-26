
package rowmappers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class AbstractRowMapper<T> implements RowMapper<T> {

    /* =======================
       Date / Time
       ======================= */

    protected static LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        return Optional.ofNullable(rs.getTimestamp(column))
                       .map(ts -> ts.toLocalDateTime())
                       .orElse(null);
    }

    /* =======================
       String
       ======================= */

    protected static String getString(ResultSet rs, String column) throws SQLException {
        return Optional.ofNullable(rs.getString(column))
                       .orElse("");
    }

    /* =======================
       Boolean
       ======================= */

    protected static Boolean getBoolean(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean aBoolean) {
            return aBoolean;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    /* =======================
       BigDecimal
       ======================= */

    protected static BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        return Optional.ofNullable(rs.getBigDecimal(column))
                       .orElse(BigDecimal.ZERO);
    }

    /* =======================
       Enum
       ======================= */

    protected static <E extends Enum<E>> E getEnum(
            ResultSet rs,
            String column,
            Class<E> enumClass
    ) throws SQLException {

        String value = rs.getString(column);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                "Invalid value '" + value + "' for enum " + enumClass.getSimpleName(), e
            );
        }
    }
} //end class
