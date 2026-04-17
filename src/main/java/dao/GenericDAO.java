package dao;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.RowMapper;

@ApplicationScoped
public class GenericDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final long SLOW_QUERY_MS = 100;
    private static final long VERY_SLOW_QUERY_MS = 500;

    @Resource(lookup = "java:jboss/datasources/golflc") // jndi-name de <datasource> de mysql-ds.xml
    private DataSource dataSource;

    public GenericDAO() {}

    // =========================
    // QUERY LIST
    // =========================
    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws SQLException {

        long start = System.nanoTime();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {

                List<T> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(mapper.map(rs));
                }

                return result;
            }

        } catch (SQLException e) {
            handleSQLException(e, "GenericDAO.queryList");
            return Collections.emptyList();

        } catch (Exception e) {
            handleGenericException(e, "GenericDAO.queryList");
            return Collections.emptyList();

        } finally {
            logExecution("queryList", sql, params, start);
        }
    }

    // =========================
    // QUERY SINGLE
    // =========================
    public <T> T querySingle(String sql, RowMapper<T> mapper, Object... params) throws SQLException {

        long start = System.nanoTime();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.map(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            handleSQLException(e, "GenericDAO.querySingle");
            return null;

        } catch (Exception e) {
            handleGenericException(e, "GenericDAO.querySingle");
            return null;

        } finally {
            logExecution("querySingle", sql, params, start);
        }
    }

    // =========================
    // EXECUTE (INSERT/UPDATE/DELETE)
    // =========================
    public int execute(String sql, Object... params) throws SQLException {

        long start = System.nanoTime();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            return ps.executeUpdate();

        } catch (SQLException e) {
            handleSQLException(e, "GenericDAO.execute");
            return 0;

        } catch (Exception e) {
            handleGenericException(e, "GenericDAO.execute");
            return 0;

        } finally {
            logExecution("execute", sql, params, start);
        }
    }

    // =========================
    // CONNECTION
    // =========================
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // =========================
    // PARAM BINDING
    // =========================
    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    // =========================
    // 🔥 DAO INTERCEPTOR CORE
    // =========================
    private void logExecution(String type, String sql, Object[] params, long startNano) {

        long durationMs = (System.nanoTime() - startNano) / 1_000_000;

        if (durationMs > VERY_SLOW_QUERY_MS) {

            LOG.error("\nVERY SLOW SQL [{}] {} ms | SQL={} | params={}",
            type,
            durationMs,
            sql,
            formatParams(params)
            );

        } else if (durationMs > SLOW_QUERY_MS) {

          LOG.warn("\nSLOW SQL [{}] {} ms | SQL={} | params={}",
            type,
            durationMs,
            sql,
            formatParams(params)
            );

        } else {
            LOG.debug("SQL [{}] {} ms", type, durationMs);
        }
    } // end method

    // =========================
    // FORMAT PARAMS
    // =========================
    private String formatParams(Object[] params) {

        if (params == null || params.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i < params.length - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

}