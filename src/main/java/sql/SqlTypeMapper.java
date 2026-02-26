
package sql;
import java.sql.Types;

public final class SqlTypeMapper {

    private SqlTypeMapper() {}

    public static String toJavaType(int jdbcType, String sqlTypeName, boolean nullable) {

        String type;

        switch (jdbcType) {
            case Types.INTEGER -> type = "Integer";
            case Types.BIGINT -> type = "Long";
            case Types.BOOLEAN, Types.BIT -> type = "Boolean";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR -> type = "String";
            case Types.DATE -> type = "LocalDate";
            case Types.TIMESTAMP -> type = "LocalDateTime";
            case Types.TIMESTAMP_WITH_TIMEZONE -> type = "OffsetDateTime";
            case Types.DECIMAL, Types.NUMERIC -> type = "BigDecimal";
       //     case Types.JSON -> type = "Object"; // ou Map / record ultérieur
            default -> type = "Object";
        }

        return type;
    }
}
