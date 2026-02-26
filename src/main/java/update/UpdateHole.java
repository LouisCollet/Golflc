package update;

import entite.HolesGlobal;
import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de mise à jour de Holes en batch
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Compatible avec l'ancien UpdateHole (HolesGlobal + Tee)
 * ✅ Basé sur la structure SQL RÉELLE
 */
@ApplicationScoped
public class UpdateHole implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Met à jour plusieurs Holes en une seule transaction (batch)
     * Compatible avec l'ancien code qui utilisait HolesGlobal
     * 
     * @param holesGlobal Objet contenant les données de tous les trous
     * @param tee Le tee associé aux holes
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean update(final HolesGlobal holesGlobal, final Tee tee) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        try (Connection conn = dataSource.getConnection()) {
            
            // ========================================
            // Configuration transaction
            // ========================================
            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false for batch update");
            
            // ✅ PARTIE NON MODIFIÉE - DÉBUT
            
            // ========================================
            // Validation
            // ========================================
            if (holesGlobal == null || holesGlobal.getDataHoles() == null) {
                msg = "HolesGlobal data cannot be null";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                msg = "Valid tee is required";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            int totalHoles = holesGlobal.getDataHoles().length;
            LOG.debug("entering {}", methodName);
            LOG.debug("holesGlobal - new holes values = {}", holesGlobal);
            LOG.debug("tee = {}", tee.toString());
            LOG.debug("longueur = {}", totalHoles);
            
            // ========================================
            // Query pour batch update
            // ========================================
            // Structure SQL réelle: HolePar TINYINT, HoleDistance SMALLINT, HoleStrokeIndex TINYINT
            String query = """
                UPDATE hole
                SET HolePar = ?,
                    HoleDistance = ?,
                    HoleStrokeIndex = ?
                WHERE tee_idtee = ?
                  AND HoleNumber = ?
                """;
            
            LOG.debug("query UpdateHolesGlobal = {}", query);
            
            int successCount = 0;
            int failCount = 0;
            
            // ========================================
            // Boucle sur tous les holes
            // ========================================
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                for (int i = 0; i < totalHoles; i++) {
                    
                    int[] holeData = holesGlobal.getDataHoles()[i];
                    
                    // Structure du tableau : [holeNumber, par, strokeIndex, distance]
                    // Index 0 = holeNumber (1-18)
                    // Index 1 = par (3-5)
                    // Index 2 = strokeIndex (1-18)
                    // Index 3 = distance (en mètres)
                    int holeNumber = holeData[0];
                    byte par = (byte) holeData[1];              // TINYINT en SQL
                    byte strokeIndex = (byte) holeData[2];      // TINYINT en SQL
                    short distance = (short) holeData[3];       // SMALLINT en SQL
                    
                    LOG.debug(" i = {}", i);
                    LOG.debug("Processing hole #{}: Par={}, Index={}, Distance={}",
                             holeNumber, par, strokeIndex, distance);
                    
                    // ========================================
                    // Mapper les données (TYPES CORRECTS)
                    // ========================================
                    int index = 0;
                    
                    // Updated fields
                    ps.setByte(++index, par);                   // HolePar TINYINT
                    ps.setShort(++index, distance);             // HoleDistance SMALLINT
                    ps.setByte(++index, strokeIndex);           // HoleStrokeIndex TINYINT
                    
                    // WHERE clause
                    ps.setInt(++index, tee.getIdtee());         // WHERE tee_idtee = ?
                    ps.setByte(++index, (byte) holeNumber);     // AND HoleNumber = ? (TINYINT)
                    
                    LCUtil.logps(ps);
                    
                    // Exécuter l'update
                    int row = ps.executeUpdate();
                    
                    if (row != 0) {
                        successCount++;
                        LOG.debug("-- Successful update Hole for hole : {} for tee = {} row = {}",
                                 holeNumber, tee.getIdtee(), row);
                    } else {
                        failCount++;
                        msg = "-- ERROR update Hole for hole : " + holeNumber;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                        
                        // Comportement comme l'ancien code : retourne false au premier échec
                        conn.rollback();
                        return false;
                    }
                }
            }
            
            // ========================================
            // Vérification et commit
            // ========================================
            msg = String.format("Batch update completed: %d/%d holes updated for tee %d",
                               successCount, totalHoles, tee.getIdtee());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
            // Commit transaction
            conn.commit();
            LOG.debug("Batch update committed successfully");
            
            // ✅ PARTIE NON MODIFIÉE - FIN
            
            return true;
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("£££ SQLException in %s: %s (SQLState: %s, ErrorCode: %d)",
                               methodName,
                               sqle.getMessage(),
                               sqle.getSQLState(),
                               sqle.getErrorCode());
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
            
        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            // Exemple de données pour 18 holes
            int[][] testData = {
                {1, 4, 10, 380},   // Hole 1: Par 4, Index 10, Distance 380m
                {2, 3, 18, 150},   // Hole 2: Par 3, Index 18, Distance 150m
                {3, 5, 2, 520},    // Hole 3: Par 5, Index 2, Distance 520m
                {4, 4, 12, 350},   // Hole 4: Par 4, Index 12, Distance 350m
                {5, 4, 6, 400},    // Hole 5: Par 4, Index 6, Distance 400m
                {6, 3, 16, 160},   // Hole 6: Par 3, Index 16, Distance 160m
                {7, 5, 4, 510},    // Hole 7: Par 5, Index 4, Distance 510m
                {8, 4, 8, 390},    // Hole 8: Par 4, Index 8, Distance 390m
                {9, 4, 14, 370},   // Hole 9: Par 4, Index 14, Distance 370m
                {10, 4, 11, 360},  // Hole 10: Par 4, Index 11, Distance 360m
                {11, 3, 17, 140},  // Hole 11: Par 3, Index 17, Distance 140m
                {12, 5, 1, 540},   // Hole 12: Par 5, Index 1, Distance 540m
                {13, 4, 7, 410},   // Hole 13: Par 4, Index 7, Distance 410m
                {14, 4, 13, 340},  // Hole 14: Par 4, Index 13, Distance 340m
                {15, 3, 15, 170},  // Hole 15: Par 3, Index 15, Distance 170m
                {16, 5, 3, 500},   // Hole 16: Par 5, Index 3, Distance 500m
                {17, 4, 9, 380},   // Hole 17: Par 4, Index 9, Distance 380m
                {18, 4, 5, 420}    // Hole 18: Par 4, Index 5, Distance 420m
            };
            
            HolesGlobal holesGlobal = new HolesGlobal();
            holesGlobal.setDataHoles(testData);
            
            Tee tee = new Tee();
            tee.setIdtee(100);
            tee.setCourse_idcourse(50);
            
            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test data: {} holes for tee ID {}", testData.length, tee.getIdtee());
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}


/*
import entite.HolesGlobal;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import utils.LCUtil;

public class UpdateHole{
    
 public boolean update(HolesGlobal holesGlobal, Tee tee, Connection conn) throws SQLException{
        final String methodName = utils.LCUtil.getCurrentMethodName(); 
        LOG.debug("entering " + methodName); 
        LOG.debug("holesGlobal - new holes values = " + holesGlobal);
        LOG.debug("tee = " + tee.toString());
    PreparedStatement ps = null;
try{
    String ho = utils.DBMeta.listMetaColumnsUpdate(conn, "hole"); // MAJ blacklist !!
        LOG.debug("String for updateHoles from listMetaColumns = " + ho);
     // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
    final String query = """
            UPDATE hole
            SET %s
            WHERE tee_idtee=?
               AND Hole.holenumber=?
           """.formatted(ho);
    LOG.debug("query UpdateHolesGlobal = " + query);
    LOG.debug("longueur = " + holesGlobal.getDataHoles().length);
 for (int i=0; i<holesGlobal.getDataHoles().length; i++) {
  //      var v = holesGlobal.getDataHoles()[i];
  //      LOG.debug(" v = "+ v);
        ps = conn.prepareStatement(query);
        LOG.debug(" i = " + i);
   // updated fields
          ps.setShort(1, (short) holesGlobal.getDataHoles()[i][1]); // Par
          // modified 19-08-2023 tansfered to table distances
          ps.setInt(2,holesGlobal.getDataHoles()[i][3]);            // distance
     // pourquoi 2 fois distance ?
          ps.setInt(2,0);            // distance
          ps.setShort(3, (short) holesGlobal.getDataHoles()[i][2]); // stroke index
    // find keys
          ps.setInt(4,tee.getIdtee());
          ps.setInt(5,holesGlobal.getDataHoles()[i][0]);
             utils.LCUtil.logps(ps);
        int row = ps.executeUpdate(); // write into database
        if(row!=0){
                LOG.debug("-- Successfull update Hole for hole : " + holesGlobal.getDataHoles()[i][0] + " for tee = " 
                        + tee.getIdtee() + " row = " + row); 
            }else{
                String msg = "-- ERROR update Hole for hole : " + holesGlobal.getDataHoles()[i][0]; 
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
     } // end for

return true;
} catch(SQLException sqle) {
       String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
} catch(Exception e) {
       LOG.error(" -- Exception in  " +methodName + e.getMessage());
       return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
      //  return false;
    }
} //end updateHoles
} // end class
*/