package sql.preparedstatement;

import entite.Hole;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;
import static utils.LCUtil.getCurrentMethodName;

/**
 * PreparedStatement mapper pour Hole
 * ⚠️ Basé sur la structure RÉELLE de l'entité Hole
 * Champs : holeNumber, holePar, holeDistance, holeStrokeIndex
 */
public class psCreateUpdateHole {

    /**
     * Prépare le PreparedStatement pour un update de Hole
     * 
     * @param ps PreparedStatement à remplir
     * @param hole Hole à mettre à jour
     * @throws Exception en cas d'erreur de mapping
     */
    public static void mapUpdate(PreparedStatement ps, Hole hole) throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapUpdate for hole = {}", hole);
            
            // ========================================
            // Validation
            // ========================================
            if (hole.getIdhole() == null || hole.getIdhole() == 0) {
                throw new IllegalArgumentException("Hole ID is required for update");
            }
            
            // ========================================
            // Mapping (champs RÉELS de l'entité Hole)
            // ========================================
            ps.setShort(++index, hole.getHoleNumber());           // holeNumber (Short, 1-18)
            ps.setShort(++index, hole.getHolePar());              // holePar (Short, 3-5)
            ps.setShort(++index, hole.getHoleDistance());         // holeDistance (Short, 59-540)
            ps.setShort(++index, hole.getHoleStrokeIndex());      // holeStrokeIndex (Short, 1-18)
            ps.setInt(++index, hole.getTee_idtee());              // tee_idtee (FK)
            ps.setInt(++index, hole.getTee_course_idcourse());    // tee_course_idcourse (FK)
            
            // WHERE clause
            ps.setInt(++index, hole.getIdhole());                 // WHERE idhole = ?


       LOG.debug("PreparedStatement for hole update completed with {} parameters", index);
            
        } catch (Exception e) {
            String msg = "Exception in mapUpdate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    /**
     * Prépare le PreparedStatement pour un insert de Hole
     * 
     * @param ps PreparedStatement à remplir
     * @param hole Hole à créer
     * @throws Exception en cas d'erreur de mapping
     */
    public static void mapCreate(PreparedStatement ps, Hole hole) throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapCreate with hole = {}", hole);
            
            // ========================================
            // Validation
            // ========================================
        //    if (hole.getTee_course_idcourse() == null || hole.getTee_course_idcourse() == 0) {
         //       throw new IllegalArgumentException("Course ID is required for hole creation");
        //    }
            
            if (hole.getHoleNumber() == null || hole.getHoleNumber() < 1 || hole.getHoleNumber() > 18) {
                throw new IllegalArgumentException("Hole number must be between 1 and 18");
            }
            
            // ========================================
            // Valeurs par défaut (si null)
            // ========================================
            Short holeNumber = (hole.getHoleNumber() != null) ? hole.getHoleNumber() : 1;
            Short holePar = (hole.getHolePar() != null) ? hole.getHolePar() : 4;
            
            // ========================================
            // Mapping
            // ========================================
      /*      ps.setInt(++index, hole.getIdhole());                 // idhole (peut être 0 pour auto-increment)
            ps.setShort(++index, holeNumber);                     // holeNumber
            ps.setShort(++index, holePar);                        // holePar
            ps.setShort(++index, hole.getHoleDistance());         // holeDistance
            ps.setShort(++index, hole.getHoleStrokeIndex());      // holeStrokeIndex
            ps.setInt(++index, hole.getTee_idtee());              // tee_idtee (FK)
            ps.setInt(++index, hole.getTee_course_idcourse());    // tee_course_idcourse (FK)
            ps.setTimestamp(++index, Timestamp.from(Instant.now())); // creationDate (si la table l'a)
        */    
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setShort(2, hole.getHoleNumber());
            ps.setShort(3, hole.getHolePar());
            ps.setInt(4, hole.getHoleDistance());
            ps.setShort(5, hole.getHoleStrokeIndex());
          //  ps.setInt(6, tee.getIdtee());
            ps.setInt(6, hole.getTee_idtee()); // mod 15-02-026
          //  ps.setInt(7, course.getIdcourse());
            ps.setInt(7, hole.getTee_course_idcourse()); // mod 15-02-2026;
            
            ps.setTimestamp(8,Timestamp.from(Instant.now())); // mod 18-02-2020
             //    String p = ps.toString();

            LOG.debug("PreparedStatement for hole creation completed with {} parameters", index);
            
        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }
}