package dao;

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

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * Generic DAO — reduces boilerplate across 47+ list services.
 * ✅ @ApplicationScoped — singleton CDI
 * ✅ DataSource JNDI standard
 * ✅ Standards CDI : methodName + handleGenericException/handleSQLException
 *
 * Usage:
 *   @Inject private GenericDAO dao;
 *   List<Club> clubs = dao.queryList("SELECT * FROM club", new ClubRowMapper());
 *   Club club = dao.querySingle("SELECT * FROM club WHERE id=?", new ClubRowMapper(), 108);
 *   int rows = dao.execute("DELETE FROM club WHERE id=?", 108);
 *
 * @author GolfLC
 * @version 1.0 — 2026-03-18
 */
@ApplicationScoped
public class GenericDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public GenericDAO() {} // end constructor

    /**
     * Exécute une requête SELECT et retourne une liste mappée.
     *
     * @param sql    requête SQL avec placeholders ?
     * @param mapper RowMapper pour convertir chaque ligne
     * @param params paramètres positionnels (dans l'ordre des ?)
     * @return liste (jamais null — Collections.emptyList() en cas d'erreur)
     */
    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws SQLException {

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
        }
    } // end method

    /**
     * Exécute une requête SELECT et retourne un seul résultat (ou null).
     */
    public <T> T querySingle(String sql, RowMapper<T> mapper, Object... params) throws SQLException {

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
        }
    } // end method

    /**
     * Exécute un INSERT, UPDATE ou DELETE.
     *
     * @return nombre de lignes affectées (0 en cas d'erreur)
     */
    public int execute(String sql, Object... params) throws SQLException {

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
        }
    } // end method

    /**
     * Expose a raw JDBC Connection for edge-case queries that cannot use queryList/querySingle
     * (e.g. DBMeta calls, temporary tables).
     * Caller is responsible for closing with try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    } // end method

    /**
     * Bind les paramètres positionnels sur le PreparedStatement.
     */
    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    } // end method

} // end class
