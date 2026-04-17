package update;

import entite.HolesGlobal;
import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
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

    @Inject
    private dao.GenericDAO dao;

    /**
     * Met à jour plusieurs Holes en une seule transaction (batch)
     * Compatible avec l'ancien code qui utilisait HolesGlobal
     * 
     * @param holesGlobal Objet contenant les données de tous les trous
     * @param tee Le tee associé aux holes
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean update(final HolesGlobal holesGlobal, final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;
        
        try (Connection conn = dao.getConnection()) {
            
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
            LOG.debug("holesGlobal - new holes values = {}", holesGlobal);
            LOG.debug("tee = {}", tee.toString());
            LOG.debug("longueur = {}", totalHoles);
            
            // ========================================
            // Query pour batch update
            // ========================================
            // Structure SQL réelle: HolePar TINYINT, HoleDistance SMALLINT, HoleStrokeIndex TINYINT
            String updateQuery = """
                UPDATE hole
                SET HolePar = ?,
                    HoleDistance = ?,
                    HoleStrokeIndex = ?
                WHERE tee_idtee = ?
                  AND HoleNumber = ?
                """;

            String insertQuery = """
                INSERT INTO hole (idhole, HoleNumber, HolePar, HoleDistance, HoleStrokeIndex,
                                  tee_idtee, tee_course_idcourse, HoleModificationDate)
                VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)
                """;

            LOG.debug("query UpdateHolesGlobal = {}", updateQuery);

            int updateCount = 0;
            int insertCount = 0;

            // ========================================
            // Boucle sur tous les holes — upsert
            // ========================================
            try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery);
                 PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {

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
                    // Tenter l'UPDATE d'abord
                    // ========================================
                    // HoleDistance toujours 0 — trigger MySQL exige HoleDistance=0
                    // (distances réelles stockées dans table distances depuis 19-08-2023)
                    int index = 0;
                    psUpdate.setByte(++index, par);                   // HolePar TINYINT
                    psUpdate.setShort(++index, (short) 0);            // HoleDistance = 0 (trigger constraint)
                    psUpdate.setByte(++index, strokeIndex);           // HoleStrokeIndex TINYINT
                    psUpdate.setInt(++index, tee.getIdtee());         // WHERE tee_idtee = ?
                    psUpdate.setByte(++index, (byte) holeNumber);     // AND HoleNumber = ? (TINYINT)

                    LCUtil.logps(psUpdate);
                    int row = psUpdate.executeUpdate();

                    if (row != 0) {
                        updateCount++;
                        LOG.debug("-- Successful UPDATE hole #{} for tee = {} row = {}",
                                 holeNumber, tee.getIdtee(), row);
                    } else {
                        // ========================================
                        // Hole n'existe pas — INSERT automatique
                        // ========================================
                        LOG.info("Hole #{} not found for tee {} — inserting new row",
                                 holeNumber, tee.getIdtee());

                        // INSERT avec distance=0 (trigger MySQL exige HoleDistance=0 à l'insertion)
                        index = 0;
                        psInsert.setShort(++index, (short) holeNumber);  // HoleNumber
                        psInsert.setByte(++index, par);                   // HolePar
                        psInsert.setShort(++index, (short) 0);            // HoleDistance = 0 (trigger constraint)
                        psInsert.setByte(++index, strokeIndex);           // HoleStrokeIndex
                        psInsert.setInt(++index, tee.getIdtee());         // tee_idtee
                        psInsert.setInt(++index, tee.getCourse_idcourse()); // tee_course_idcourse
                        psInsert.setTimestamp(++index, Timestamp.from(Instant.now())); // HoleModificationDate

                        LCUtil.logps(psInsert);
                        int inserted = psInsert.executeUpdate();

                        if (inserted != 0) {
                            insertCount++;
                            LOG.debug("-- Successful INSERT hole #{} for tee = {}", holeNumber, tee.getIdtee());
                            // Distance non mise à jour ici — trigger MySQL exige HoleDistance=0
                            // sur INSERT ET UPDATE. Distances gérées dans table distances.
                        } else {
                            msg = "-- ERROR insert Hole for hole : " + holeNumber;
                            LOG.error(msg);
                            LCUtil.showMessageFatal(msg);
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }
            
            // ========================================
            // Vérification et commit
            // ========================================
            msg = String.format("Batch upsert completed: %d updated, %d inserted (%d total) for tee %d",
                               updateCount, insertCount, totalHoles, tee.getIdtee());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
            // Commit transaction
            conn.commit();
            LOG.debug("Batch update committed successfully");
            
            // ✅ PARTIE NON MODIFIÉE - FIN
            
            return true;
            
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            HolesGlobal holesGlobal = new HolesGlobal();
            Tee tee = new Tee();
            tee.setIdtee(100);
            LOG.debug("Main ready (CDI required for execution)");
        } catch (Exception e) {
            LOG.error("Exception in main: {}", e.getMessage(), e);
        }
    } // end main
*/
} // end class


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
        LOG.debug("entering {}", methodName); 
        LOG.debug("holesGlobal - new holes values = {}", holesGlobal);
        LOG.debug("tee = {}", tee.toString());
    PreparedStatement ps = null;
try{
    String ho = utils.DBMeta.listMetaColumnsUpdate(conn, "hole"); // MAJ blacklist !!
        LOG.debug("String for updateHoles from listMetaColumns = {}", ho);
     // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
    final String query = """
            UPDATE hole
            SET %s
            WHERE tee_idtee=?
               AND Hole.holenumber=?
           """.formatted(ho);
    LOG.debug("query UpdateHolesGlobal = {}", query);
    LOG.debug("longueur = {}", holesGlobal.getDataHoles().length);
 for (int i=0; i<holesGlobal.getDataHoles().length; i++) {
  //      var v = holesGlobal.getDataHoles()[i];
  //      LOG.debug(" v = "+ v);
        ps = conn.prepareStatement(query);
        LOG.debug(" i = {}", i);
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
                LOG.debug("Successfull update Hole for hole={} tee={} row={}", holesGlobal.getDataHoles()[i][0], tee.getIdtee(), row);
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
       LOG.error("Exception: {}", e.getMessage());
       return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
      //  return false;
    }
} //end updateHoles
} // end class
*/