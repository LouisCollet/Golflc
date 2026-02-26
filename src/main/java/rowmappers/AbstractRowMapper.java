package rowmappers;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Classe de base pour tous les RowMapper.
 * Centralise la gestion défensive du ResultSet :
 *  - colonnes optionnelles
 *  - valeurs NULL
 *  - conversions types sûres
 */
public abstract class AbstractRowMapper<T> implements RowMapper<T> {

    /**
     * Méthode principale à implémenter dans chaque mapper concret.
     */
    @Override
    public abstract T map(ResultSet rs) throws SQLException;

    // =========================================================
    // Détection de colonne
    // =========================================================

    protected boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
        //    LOG.debug("column found = " + column);
            return true;
        } catch (SQLException e) {
            LOG.debug("column not found = " + column + " exception = " + e);
            return false;
        }
    }

    // =========================================================
    // Helpers types simples
    // =========================================================

    protected String getString(ResultSet rs, String column) throws SQLException {
        if (!hasColumn(rs, column))  return null;
        return rs.getString(column);
    }

    protected Integer getInteger(ResultSet rs, String column) throws SQLException {
        if (!hasColumn(rs, column)) return null;
        int val = rs.getInt(column);
        return rs.wasNull() ? null : val;
    }

    protected Short getShort(ResultSet rs, String column) throws SQLException {
        if (!hasColumn(rs, column)) return null;
        short val = rs.getShort(column);
        return rs.wasNull() ? null : val;
    }

    protected Boolean getBoolean(ResultSet rs, String column) throws SQLException {
        if (!hasColumn(rs, column)) return null;
        boolean val = rs.getBoolean(column);
        return rs.wasNull() ? null : val;
    }

    protected Double getDouble(ResultSet rs, String column) throws SQLException {
        if (!hasColumn(rs, column)) return null;
        double val = rs.getDouble(column);
        return rs.wasNull() ? null : val;
    }

    protected BigDecimal getBigDecimal(ResultSet rs, String column) throws SQLException {
        return hasColumn(rs, column) ? rs.getBigDecimal(column) : null;
    }

    // =========================================================
    // Enums
    // =========================================================

    protected <E extends Enum<E>> E getEnum(ResultSet rs, String column, Class<E> enumClass)
            throws SQLException {

        if (!hasColumn(rs, column)) return null;

        String value = rs.getString(column);
        if (value == null || value.isBlank()) return null;

        try {
            return Enum.valueOf(enumClass, value.trim());
        } catch (IllegalArgumentException e) {
            handleGenericException(e, "AbstractRowMapper.getEnum");
            return null;
        }
    }

    // =========================================================
    // Dates / heures
    // =========================================================

    protected Timestamp getTimestamp(ResultSet rs, String column) throws SQLException {
        return hasColumn(rs, column) ? rs.getTimestamp(column) : null;
    }

    protected LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = getTimestamp(rs, column);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    protected ZonedDateTime getZonedDateTime(ResultSet rs, String column, ZoneId zone)
            throws SQLException {

        LocalDateTime ldt = getLocalDateTime(rs, column);
        return ldt != null ? ldt.atZone(zone) : null;
    }

    // =========================================================
    // Colonnes obligatoires (optionnel mais recommandé)
    // =========================================================

    protected <R> R require(R value, String column) {
        if (value == null) {
            throw new IllegalStateException("Required column missing or NULL: " + column);
        }
        return value;
    }

    // =========================================================
    // Safe mapping utilitaire
    // =========================================================

    protected <R> R safeMap(ResultSet rs, MapperFunction<ResultSet, R> mapper) {
        try {
            return mapper.apply(rs);
        } catch (Exception e) {
            handleGenericException(e, this.getClass().getSimpleName());
            return null;
        }
    }

    @FunctionalInterface
    public interface MapperFunction<T, R> {
        R apply(T t) throws Exception;
    }
// new 24-01-2026 
    protected String getStringOrDefault(
        ResultSet rs,
        String column,
        String defaultValue
) throws SQLException {

    if (!hasColumn(rs, column)) {
        throw new IllegalStateException("Missing column: " + column);
    }

    String value = rs.getString(column);
    return (value == null || value.isBlank()) ? defaultValue : value;
}

}