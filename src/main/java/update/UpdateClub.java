package update;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import sql.SqlFactory;
import utils.LCUtil;

/**
 * Service de mise à jour de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class UpdateClub implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Met à jour un Club dans la base de données
     * 
     * @param club Le club à mettre à jour
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean update(final Club club) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        LOG.debug("dataSource = {}", dataSource);
        
        try (Connection conn = dataSource.getConnection()) {
            
            // ========================================
            // Configuration transaction
            // ========================================
            conn.setAutoCommit(false);
            msg = "AutoCommit set to false";
            LOG.info(msg);
            
            // ✅ PARTIE NON MODIFIÉE - DÉBUT
            
            // ========================================
            // Validation
            // ========================================
            if (club == null) {
                msg = "Club cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (club.getIdclub() == null || club.getIdclub() == 0) {
                msg = "Club ID is required for update";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                msg = "Club name is required";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Updating club: {}", club.toString());
            
            // ========================================
            // Update Club
            // ========================================
            String query = new SqlFactory().generateQueryUpdate(conn, "club");
            LOG.debug("Update query: {}", query);
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                // Mapper les données
                sql.preparedstatement.psCreateUpdateClub.psMapUpdate(ps, club);
                LCUtil.logps(ps);
                
                // Exécuter l'update
                int rowsAffected = ps.executeUpdate();
                LOG.debug("Rows affected: {}", rowsAffected);
                
                if (rowsAffected == 0) {
                    msg = "No rows updated - Club may not exist: ID " + club.getIdclub();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }
            
            msg = String.format("Club updated: %s (ID: %d)", 
                               club.getClubName(), 
                               club.getIdclub());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            msg = "Club update committed successfully";
            LOG.debug(msg);
            
            // ✅ PARTIE NON MODIFIÉE - FIN
            
            return true;
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("SQLException in %s: %s (SQLState: %s, ErrorCode: %d)",
                               methodName,
                               sqle.getMessage(),
                               sqle.getSQLState(),
                               sqle.getErrorCode());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle;
            
        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }

    /**
     * Main pour tests hors JSF
     * Note: Non fonctionnel sans container CDI
     */
    public static void main(String[] args) {
        try {
            Club club = new Club();
            club.setIdclub(151);
            club.setClubName("Club Test Updated");
            
            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test club: {}", club);
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
            LCUtil.showMessageFatal("Exception in main: " + e.getMessage());
        }
    }
}
/*
package update;

import connection_package.ConnectionProvider;
import connection_package.ProdDB;
import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import sql.SqlFactory;
import utils.LCUtil;

/*
 * Service de mise à jour de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ Injection CDI du ConnectionProvider
 * ✅ Gestion transactionnelle avec commit/rollback
 
@ApplicationScoped
public class UpdateClub implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    // ========================================
    // Injection CDI
    // ========================================
    
    /**
     * Injection CDI du ConnectionProvider (Production DB)
 
    @Inject
    @ProdDB
    private ConnectionProvider connectionProvider;

    // ========================================
    // Mise à jour
    // ========================================
    
    /**
     * Met à jour un Club dans la base de données
     * 
     * @param club Le club à mettre à jour
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur

    public boolean update(final Club club) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        LOG.debug("connectionProvider = {}", connectionProvider);
        
        try (Connection conn = connectionProvider.getConnection()) {
            
            // ========================================
            // Configuration transaction
            // ========================================
            conn.setAutoCommit(false);
            msg = "AutoCommit set to false";
            LOG.info(msg);
            
            // ========================================
            // Validation
            // ========================================
            if (club == null) {
                msg = "Club cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (club.getIdclub() == null || club.getIdclub() == 0) {
                msg = "Club ID is required for update";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                msg = "Club name is required";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Updating club: {}", club.toString());
            
            // ========================================
            // Update Club
            // ========================================
            String query = new SqlFactory().generateQueryUpdate(conn, "club");
            LOG.debug("Update query: {}", query);
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                // Mapper les données
                sql.preparedstatement.psCreateUpdateClub.psMapUpdate(ps, club);
                LCUtil.logps(ps);
                
                // Exécuter l'update
                int rowsAffected = ps.executeUpdate();
                LOG.debug("Rows affected: {}", rowsAffected);
                
                if (rowsAffected == 0) {
                    msg = "No rows updated - Club may not exist: ID " + club.getIdclub();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }
            
            msg = String.format("Club updated: %s (ID: %d)", 
                               club.getClubName(), 
                               club.getIdclub());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            msg = "Club update committed successfully";
            LOG.debug(msg);
            
            return true;
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("SQLException in %s: %s (SQLState: %s, ErrorCode: %d)",
                               methodName,
                               sqle.getMessage(),
                               sqle.getSQLState(),
                               sqle.getErrorCode());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle;
            
        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }

    // ========================================
    // Main pour tests (hors container CDI)
    // ========================================
    
    /**
     * Main pour tests hors JSF
     * Note: Non fonctionnel sans container CDI

    public static void main(String[] args) {
        try {
            // Exemple de test (nécessite CDI)
            Club club = new Club();
            club.setIdclub(151);
            club.setClubName("Club Test Updated");
            
            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test club: {}", club);
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
            LCUtil.showMessageFatal("Exception in main: " + e.getMessage());
        }
    }
}
*/