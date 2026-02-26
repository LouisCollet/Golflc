
package test.prepareStatement;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;

@ApplicationScoped
public class DbExecutor {

    //@Inject
    //DataSourceProvider ds;

    @Inject
    DbMetadataCache metadata;

    @Transactional
    public <T> void upsert(String table, T entity) throws Exception {
           LOG.debug("entering upsert with table = " + table);
           LOG.debug("entering upsert with entity = " + entity);
        try{ Connection conn = new DBConnection().getConnection(); 
           LOG.debug("connection = " + conn);
            PreparedStatement ps = DbUpsertEngine.autoUpsert(
                conn, table, entity, metadata
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UPSERT failed on " + table, e);
        }
    }

    @Transactional
    public <T> void insert(String table, T entity) {
        try{ Connection conn = new DBConnection().getConnection(); 
            PreparedStatement ps = DbUpsertEngine.autoInsert(
                conn, table, entity, metadata
            );
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("INSERT failed on " + table, e);
        }
    }
// connection not closed !!
    @Transactional
    public <T> void update(String table, T entity) throws Exception {
        try { Connection conn = new DBConnection().getConnection();
            PreparedStatement ps = DbUpsertEngine.autoUpdate(
                conn, table, entity, metadata
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("UPDATE failed on " + table, e);
        }
    }
}
