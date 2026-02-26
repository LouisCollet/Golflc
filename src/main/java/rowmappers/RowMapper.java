package rowmappers;

import java.sql.ResultSet;
import java.sql.SQLException;
// new 17-12-2025
@FunctionalInterface
public interface RowMapper<T> {
    T map(ResultSet rs) throws SQLException;
}