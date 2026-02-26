
package lists;

import entite.Player;
import entite.Professional;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * Liste les professionnels pour un joueur donné
 * ✅ Migré vers CDI (@ApplicationScoped)
 * ✅ Connection supprimée — gérée via DataSource injecté
 * ✅ try-with-resources (plus de finally/closeQuietly)
 * ✅ handleGenericException / handleSQLException
 * ✅ main() conservée commentée
 */
@ApplicationScoped
public class FindCountListProfessional implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ Injection DataSource WildFly
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Professional> liste = null;

    private static final String QUERY = """
            SELECT *
            FROM professional
            WHERE professional.ProPlayerId = ?
            """;

    // ========================================
    // MÉTHODE PRINCIPALE
    // ✅ Connection supprimée — gérée en interne via DataSource
    // ========================================

    /**
     * Liste les professionnels pour un joueur donné.
     *
     * @param player le joueur dont on veut les données professionnelles
     * @return liste des Professional, jamais null
     */
    public List<Professional> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list (" + liste.size() + " entries)");
            return liste;
        }

        // Validation paramètre
        if (player == null || player.getIdplayer() == null || player.getIdplayer() <= 0) {
            LOG.warn(methodName + " - player is null or has no ID");
            return Collections.emptyList();
        }

        LOG.debug(methodName + " - querying database for player ID=" + player.getIdplayer());

        // ✅ try-with-resources : Connection, PreparedStatement, ResultSet fermés automatiquement
        try (Connection conn       = dataSource.getConnection();
             PreparedStatement ps  = conn.prepareStatement(QUERY)) {

            ps.setInt(1, player.getIdplayer());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {

                RowMapper<Professional> professionalMapper = new ProfessionalRowMapper();
                List<Professional> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(professionalMapper.map(rs));
                }

                if (result.isEmpty()) {
                    LOG.debug(methodName + " - no professionals found for player ID=" + player.getIdplayer());
                } else {
                    LOG.debug(methodName + " - found " + result.size() + " professional(s)");
                    result.forEach(item ->
                            LOG.debug("Professional: proId=" + item.getProId()
                                    + ", playerId=" + item.getProPlayerId()
                                    + ", club=" + item.getProClubId()));
                }

                liste = result;                                     // ✅ mise en cache
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();                         // ✅ jamais null
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();                         // ✅ jamais null
        }
    } // end method

    // ========================================
    // CACHE
    // ========================================

    public boolean isCached() {
        return liste != null;
    }

    // ✅ Getters/setters d'instance
    public List<Professional> getListe()              { return liste; }
    public void setListe(List<Professional> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    // ========================================
    // MAIN DE TEST - conservé commenté
    // ========================================

    /*
    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        try {
            conn = new DBConnection().getConnection();

            Player player = new Player();
            player.setIdplayer(324713);

            FindCountListProfessional finder = new FindCountListProfessional();

            LOG.debug("TEST 1: Premier appel - devrait faire la requête SQL");
            LOG.debug("isCached() avant = " + finder.isCached());
            List<Professional> lp1 = finder.list(player, conn);
            LOG.debug("isCached() après = " + finder.isCached());
            LOG.debug("Nombre de professionals = " + lp1.size());

            LOG.debug("TEST 2: Deuxième appel - devrait utiliser le cache");
            List<Professional> lp2 = finder.list(player, conn);
            LOG.debug("Même liste ? " + (lp1 == lp2));

            LOG.debug("TEST 3: Après invalidation");
            FindCountListProfessional.setListe(null);
            List<Professional> lp3 = finder.list(player, conn);
            LOG.debug("Nouvelle liste ? " + (lp1 != lp3));

            LOG.debug("TEST 4: player null");
            List<Professional> lpNull = finder.list(null, conn);
            LOG.debug("Liste avec player null = " + lpNull.size());

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main
    */

} // end class
/*

package lists;

import entite.Player;
import entite.Professional;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import connection_package.DBConnection;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

public class FindCountListProfessional {
    
    // Cache de la liste
    private static List<Professional> liste = null;
    
  
    public List<Professional> list(final Player player, Connection conn) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        
        // ✅ SI LA LISTE EST DÉJÀ CHARGÉE, LA RETOURNER
        if (liste != null) {
            LOG.debug("✓ Returning cached professionals list (" + liste.size() + " items) - no DB query");
            return liste;
        }
        
        // ✅ VALIDATION DES PARAMÈTRES
        if (player == null) {
            LOG.error("Player is null in " + methodName);
            liste = Collections.emptyList();
            return liste;
        }
        
        if (conn == null) {
            LOG.error("Connection is null in " + methodName);
            liste = Collections.emptyList();
            return liste;
        }
        
        if (player.getIdplayer() == null || player.getIdplayer() <= 0) {
            LOG.warn("Invalid player ID in " + methodName);
            liste = Collections.emptyList();
            return liste;
        }
        
        // ✅ LA LISTE EST NULL, FAIRE LA RECHERCHE SQL
        LOG.debug("⚡ Liste is null - executing SQL query for player ID: " + player.getIdplayer());
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            liste = new ArrayList<>();
            
            String query = """
                SELECT *
                FROM professional
                WHERE professional.ProPlayerId = ?
            """;
            
            ps = conn.prepareStatement(query);
            ps.setInt(1, player.getIdplayer());
            LCUtil.logps(ps);
            
            rs = ps.executeQuery();
            RowMapper<Professional> professionalMapper = new ProfessionalRowMapper();
            
            while (rs.next()) {
                Professional pro = professionalMapper.map(rs);
                liste.add(pro);
            }
            
            if (liste.isEmpty()) {
                LOG.debug("No professionals found for player ID: " + player.getIdplayer());
            } else {
                LOG.debug("Found " + liste.size() + " professional(s)");
                liste.forEach(item -> 
                    LOG.debug("Professional: proId=" + item.getProId() + 
                             ", PlayerId=" + item.getProPlayerId() + 
                             ", club=" + item.getProClubId())
                );
            }
            
            return liste;
            
        } catch (SQLException e) {
            String error = "SQL Exception in " + methodName + ": " + e.getMessage();
            LOG.error(error, e);
            LCUtil.showMessageFatal(error);
            liste = Collections.emptyList();
            return liste;
            
        } catch (Exception ex) {
            String error = "Exception in " + methodName + ": " + ex.getMessage();
            LOG.error(error, ex);
            LCUtil.showMessageFatal(error);
            liste = Collections.emptyList();
            return liste;
            
        } finally {
            DBConnection.closeQuietly(null, null, rs, ps);
        }
    }
    

    public void invalidateCache() {
       LOG.debug("🗑️ Invalidating professionals cache");
       FindCountListProfessional liste = null;
   }
    

    public boolean isCached() {
        return liste != null;
    }
    
    // Getters/Setters pour accès direct si nécessaire
    public List<Professional> getListe() {
        return liste;
    }
    
      public static void setListe(List<Professional> liste) {
        FindCountListProfessional.liste = liste;
    }
    
    

    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        try {
            conn = new DBConnection().getConnection();
            
            // Créer un player de test
            Player player = new Player();
            player.setIdplayer(324713); // Votre ID de test
            
            // Créer une instance du finder
            FindCountListProfessional finder = new FindCountListProfessional();
            
            LOG.debug("========================================");
            LOG.debug("TEST 1: Premier appel - devrait faire la requête SQL");
            LOG.debug("isCached() avant = " + finder.isCached());
            
            List<Professional> lp1 = finder.list(player, conn);
            
            LOG.debug("isCached() après = " + finder.isCached());
            LOG.debug("Nombre de professionals trouvés = " + lp1.size());
            lp1.forEach(item -> 
                LOG.debug("Professional: proId=" + item.getProId() + 
                         ", PlayerId=" + item.getProPlayerId() + 
                         ", club=" + item.getProClubId())
            );
            
            LOG.debug("========================================");
            LOG.debug("TEST 2: Deuxième appel - devrait utiliser le cache");
            
            List<Professional> lp2 = finder.list(player, conn);
            LOG.debug("Même liste ? " + (lp1 == lp2)); // devrait être true
            LOG.debug("Nombre = " + lp2.size());
            
            LOG.debug("========================================");
            LOG.debug("TEST 3: Après invalidation - devrait refaire la requête");
            
            finder.invalidateCache();
            LOG.debug("isCached() après invalidation = " + finder.isCached());
            
            List<Professional> lp3 = finder.list(player, conn);
            LOG.debug("Nouvelle liste ? " + (lp1 != lp3)); // devrait être true
            LOG.debug("Nombre = " + lp3.size());
            
            LOG.debug("========================================");
            LOG.debug("TEST 4: Test avec player null");
            
            FindCountListProfessional finder2 = new FindCountListProfessional();
            List<Professional> lpNull = finder2.list(null, conn);
            LOG.debug("Liste avec player null = " + lpNull.size() + " (devrait être 0)");
            
        } catch (Exception e) {
            String msg = "Exception in main: " + e.getMessage();
            LOG.error(msg, e);
            e.printStackTrace();
            
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
            LOG.debug("========================================");
            LOG.debug("Tests terminés");
        }
    }
}
*/