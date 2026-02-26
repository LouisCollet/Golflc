
package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import connection_package.ConnectionProvider;
import connection_package.ProdDB;
import entite.Distance;
import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service pour trouver les distances d'un tee
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ Injection CDI du ConnectionProvider
 * ✅ Basé sur la table 'distances' avec champ JSON
 * 
 * Structure de la table distances :
 * - DistanceIdTee INT (FK vers tee.TeeDistanceTee)
 * - DistanceArray JSON (tableau des 18 distances)
 * 
 * IMPORTANT : Cherche par tee.getTeeDistanceTee() (pas tee.getIdtee())
 * 
 * @author GolfLC
 * @version 3.0 - Migration CDI
 */
@ApplicationScoped
public class FindDistances implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    @ProdDB
    private ConnectionProvider connectionProvider;
    
    /**
     * ObjectMapper Jackson (réutilisable)
     * Configuré avec INDENT_OUTPUT pour le debug
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Trouve les distances pour un tee
     * 
     * Cherche dans la table 'distances' par tee.getTeeDistanceTee()
     * Le champ DistanceArray est au format JSON et doit être désérialisé
     * 
     * Si aucune distance n'est trouvée, retourne un Distance avec tableau de zéros
     * 
     * @param tee Le tee dont on veut les distances
     * @return Distance avec le tableau int[18] (ou zéros si non trouvé)
     * @throws Exception en cas d'erreur
     */
    public Distance find(final Tee tee) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        LOG.debug("entering FindDistances.find...");
        LOG.debug("for tee = {}", tee);
        
        try (Connection conn = connectionProvider.getConnection()) {
            
            // ========================================
            // Validation
            // ========================================
            if (tee == null) {
                msg = "Tee cannot be null";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            // ⚠️ IMPORTANT : On cherche par TeeDistanceTee (pas idtee)
            if (tee.getTeeDistanceTee() == null) {
                LOG.warn("TeeDistanceTee is null for tee {}, returning zeros", tee.getIdtee());
                return createZeroDistance();
            }
            
            // ========================================
            // Query SQL
            // ========================================
            final String query = """
                SELECT distances.DistanceArray
                FROM distances
                WHERE distances.DistanceIdTee = ?
                """;
            
            LOG.debug("Query: {}", query);
            
            // ========================================
            // Exécution et mapping
            // ========================================
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                // ⚠️ On utilise getTeeDistanceTee() (pas getIdtee())
                ps.setInt(1, tee.getTeeDistanceTee());
                
                LCUtil.logps(ps);
                
                try (ResultSet rs = ps.executeQuery()) {
                    
                    int rowCount = 0;
                    String json = null;
                    
                    while (rs.next()) {
                        rowCount++;
                        json = rs.getString("DistanceArray");
                    }
                    
                    // ========================================
                    // Cas 1 : Aucune distance trouvée
                    // ========================================
                    if (rowCount == 0) {
                        msg = LCUtil.prepareMessageBean("distances.notfound") + "<br>" + tee;
                        LOG.debug(msg);
                        LCUtil.showMessageInfo(msg);
                        
                        return createZeroDistance();
                    }
                    
                    // ========================================
                    // Cas 2 : Distances trouvées (JSON)
                    // ========================================
                    LOG.debug("ResultSet {} has {} line(s)", methodName, rowCount);
                    LOG.debug("Distance format json = {}", json);
                    
                    // Désérialiser le JSON vers Distance
                    Distance distance = objectMapper.readValue(json, Distance.class);
                    
                    LOG.debug("Distance extracted from database = {}", distance);
                    
                    return distance;
                }
            }
            
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
            msg = "Exception in FindDistances(): " + e.getMessage();
            LOG.error(msg, e);
            LCUtil.showMessageFatal(e.toString());
            throw e;
        }
    }
    
    /**
     * Crée un Distance avec un tableau de zéros
     * Utilisé quand aucune distance n'est trouvée en base
     * 
     * @return Distance avec int[18] rempli de 0
     */
    private Distance createZeroDistance() {
        Distance distance = new Distance();
        int[] array = new int[18];
        Arrays.fill(array, 0);
        distance.setDistanceArray(array);
        
        LOG.debug("Created zero distance array");
        
        return distance;
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Tee tee = new Tee();
            tee.setIdtee(150);
            tee.setTeeDistanceTee(150); // ⚠️ Important pour FindDistances
            
            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test tee ID: {}, TeeDistanceTee: {}", 
                     tee.getIdtee(), tee.getTeeDistanceTee());
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}
/*
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Distance;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import connection_package.DBConnection;
import utils.LCUtil;

public class FindDistances implements interfaces.GolfInterface{
    

public Distance find(Tee tee, final Connection conn) throws SQLException{
           LOG.debug("entering FindDistances.find ...");
           LOG.debug(" for tee = " + tee);
        //   LOG.debug(" for round = " + round);
        final String methodName = utils.LCUtil.getCurrentMethodName(); 
    PreparedStatement ps = null;
    ResultSet rs = null;
    Distance distance = new Distance();
try{
   final String query = """
       SELECT distances.DistanceArray
       FROM distances
       WHERE distances.DistanceIdTee = ?
     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getTeeDistanceTee()); // mod 12-08-2023
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    int i = 0;
    String json = null;
    while(rs.next()){
       i++;
       json = rs.getString("DistanceArray");
    }
     if(i == 0){
         String msg=  LCUtil.prepareMessageBean("distances.notfound") + "<br>" + tee;
         LOG.debug(msg);
         LCUtil.showMessageInfo(msg);
       //  Distance distance = new Distance();
         int[] array = new int[18];
         Arrays.fill(array, 0);
         distance.setDistanceArray(array);
         return distance;
 //        return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
     }
        ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
           LOG.debug("Distance format json = "  + json);
        distance = om.readValue(json, Distance.class);
           LOG.debug("Distance extracted from database = "  + distance);
        return distance;
}catch (SQLException e){
    String msg = "SQL Exception for " + methodName + " " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindDistances()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(ex.toString()); // new 04-01-2022
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}//end method

void main() throws Exception , Exception{
    Connection conn = new DBConnection().getConnection();
  //  Distance distance = new Distance();
  Tee tee = new Tee();
  tee.setIdtee(150);
  //  distance.setIdTee(10);
    Distance distance = new FindDistances().find(tee, conn);
     LOG.debug("Distance found in main = "  + distance);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end Class
*/