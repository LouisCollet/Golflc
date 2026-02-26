
package test.prepareStatement;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DbMetadataCache {

    private final Map<String, Set<String>> columnsCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> pkCache = new ConcurrentHashMap<>();

    public Set<String> getColumns(Connection conn, String table) throws SQLException {
        return columnsCache.computeIfAbsent(table, t -> loadColumns(conn, t));
    }

    public List<String> getPrimaryKeys(Connection conn, String table) throws SQLException {
        return pkCache.computeIfAbsent(table, t -> loadPrimaryKeys(conn, t));
    }

    private Set<String> loadColumns(Connection conn, String table) {
        try {
            Set<String> cols = new HashSet<>();
            var rs = conn.getMetaData().getColumns(null, null, table, null);
            while (rs.next()) cols.add(rs.getString("COLUMN_NAME"));
            LOG.debug("cols = " + cols);
            return cols;
        } catch (SQLException e) {
            LOG.debug("error in loadColumns = " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<String> loadPrimaryKeys(Connection conn, String table) {
        try {
            List<String> pks = new ArrayList<>();
            var rs = conn.getMetaData().getPrimaryKeys(null, null, table);
            while (rs.next()) pks.add(rs.getString("COLUMN_NAME"));
            return pks;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
