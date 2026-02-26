package sql.preparedstatement;

import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import utils.LCUtil;
import static utils.LCUtil.getCurrentMethodName;

/**
 * PreparedStatement mapper pour Tee
 * Gère la création et la mise à jour de tees
 * 
 * ⚠️ Basé sur la structure RÉELLE de l'entité Tee
 * Champs disponibles : teeGender, teeStart, teeSlope, teeRating, teeClubHandicap,
 *                      teeHolesPlayed, teePar, teeMasterTee, teeDistanceTee
 */
public class psCreateUpdateTee {

    /**
     * Prépare le PreparedStatement pour un update de Tee
     * 
     * @param ps PreparedStatement à remplir
     * @param tee Tee à mettre à jour
     * @throws Exception en cas d'erreur de mapping
     */
    public static void mapUpdate(PreparedStatement ps, Tee tee) throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapUpdate for tee = {}", tee);
            
            // ========================================
            // Validation
            // ========================================
            if (tee.getIdtee() == null || tee.getIdtee() == 0) {
                throw new IllegalArgumentException("Tee ID is required for update");
            }
            
            // ========================================
            // Normalisation des valeurs nullable
            // ========================================
            Integer distance = (tee.getTeeDistanceTee() != null) ? tee.getTeeDistanceTee() : 0;
            
            // ========================================
            // Mapping (dans l'ordre de la table SQL)
            // ========================================
            ps.setString(++index, tee.getTeeGender());            // TeeGender (M/F)
            ps.setString(++index, tee.getTeeStart());             // TeeStart (YELLOW/WHITE/etc)
            ps.setShort(++index, tee.getTeeSlope());              // TeeSlope (88-152)
            ps.setBigDecimal(++index, tee.getTeeRating());        // TeeRating (28.0-77.4)
            ps.setInt(++index, tee.getTeeClubHandicap());         // TeeClubHandicap (0-10)
            ps.setString(++index, tee.getTeeHolesPlayed());       // TeeHolesPlayed (ex: "01-18")
            ps.setShort(++index, tee.getTeePar());                // TeePar
            ps.setInt(++index, tee.getTeeMasterTee());            // TeeMasterTee
            if(tee.getTeeDistanceTee() == null){  // 12-08-2023
                tee.setTeeDistanceTee(0);
            }
            ps.setInt(++index, distance);                         // TeeDistanceTee (normalisé)
            
            // ========================================
            // WHERE clause (toujours en dernier)
            // ========================================
            ps.setInt(++index, tee.getIdtee());                   // WHERE idtee = ?
            
            LOG.debug("PreparedStatement for tee update completed with {} parameters", index);
            
        } catch (Exception e) {
            String msg = "Exception in mapUpdate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    /**
     * Prépare le PreparedStatement pour un insert de Tee
     * 
     * @param ps PreparedStatement à remplir
     * @param tee Tee à créer
     * @throws Exception en cas d'erreur de mapping
     */
    public static void mapCreate(PreparedStatement ps, Tee tee) throws Exception {
        int index = 0;
        try {
            LOG.debug("entering mapCreate with tee = {}", tee);
            
            // ========================================
            // Validation
            // ========================================
        //    if (tee.getCourse_idcourse() == null || tee.getCourse_idcourse() == 0) {
        //        throw new IllegalArgumentException("Course ID is required for tee creation");
        //    }
            
            // ========================================
            // Normalisation et valeurs par défaut
            // ========================================
      /*      Integer distance = (tee.getTeeDistanceTee() != null) ? tee.getTeeDistanceTee() : 0;
            
            // Valeurs par défaut si null (depuis le constructeur)
            String gender = (tee.getTeeGender() != null) ? tee.getTeeGender() : "M";
            String start = (tee.getTeeStart() != null) ? tee.getTeeStart() : "YELLOW";
            Integer clubHandicap = (tee.getTeeClubHandicap() != null) ? tee.getTeeClubHandicap() : 0;
            String holesPlayed = (tee.getTeeHolesPlayed() != null) ? tee.getTeeHolesPlayed() : "01-18";
            
            // ========================================
            // Mapping (dans l'ordre de la table SQL)
            // ========================================
            ps.setInt(++index, tee.getIdtee());                   // idtee (peut être 0 pour auto-increment)
            ps.setString(++index, gender);                        // TeeGender
            ps.setString(++index, start);                         // TeeStart
            ps.setShort(++index, tee.getTeeSlope());              // TeeSlope
            ps.setBigDecimal(++index, tee.getTeeRating());        // TeeRating
            ps.setInt(++index, clubHandicap);                     // TeeClubHandicap
            ps.setString(++index, holesPlayed);                   // TeeHolesPlayed
            ps.setShort(++index, tee.getTeePar());                // TeePar
            ps.setInt(++index, tee.getCourse_idcourse());         // course_idcourse (FK)
            ps.setInt(++index, tee.getTeeMasterTee());            // TeeMasterTee
            ps.setInt(++index, distance);                         // TeeDistanceTee
            ps.setTimestamp(++index, Timestamp.from(Instant.now())); // creationDate (si la table l'a)
*/
    ps.setNull(1, java.sql.Types.INTEGER);
    ps.setString(2, tee.getTeeGender());
    ps.setString(3, tee.getTeeStart());
    ps.setInt(4, tee.getTeeSlope());
    ps.setBigDecimal(5, tee.getTeeRating());
    ps.setInt(6, tee.getTeeClubHandicap());   // quelle est son utilité ??
    ps.setString(7, tee.getTeeHolesPlayed());
    ps.setShort(8,tee.getTeePar());
    ps.setInt(9, 9999);  // MASTER TEE ! mod 15-08-2020 provisoire, sera modifié plus loin !
    ps.setInt(10, 9999); // Distance TEE ! mod 12-08-2023 provisoire, sera updated plus loin !
  //  ps.setInt(9, tee.getTeeMasterTee());// mod 23-08-2023    
  //  ps.setInt(10,tee.getTeeDistanceTee());
 //   ps.setInt(11, course.getIdcourse());
    ps.setInt(11, tee.getCourse_idcourse()); //mod 15-02-2026
    
    ps.setTimestamp(12, Timestamp.from(Instant.now()));
            
             LOG.debug("PreparedStatement for tee creation completed with {} parameters", index);
            
            
         //--------   
            
            
        } catch (Exception e) {
            String msg = "Exception in mapCreate = " + getCurrentMethodName() + " / " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }
}