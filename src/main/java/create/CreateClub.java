package create;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Types;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service de création de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class CreateClub implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Crée un Club dans la base de données
     * 
     * @param club Le club à créer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean create(final Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;

        try (Connection conn = dao.getConnection()) {
            
            // ========================================
            // Configuration transaction
            // ========================================
            conn.setAutoCommit(false);
            msg = "AutoCommit set to false";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
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
            
            if (club.getClubName() == null || club.getClubName().trim().isEmpty()) {
                msg = "Club name is required";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Creating club: {}", club.toString());
            
            // ========================================
            // Insert Club
            // ========================================
            String query = LCUtil.generateInsertQuery(conn, "club");
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                // Vérifier les warnings SQL
                SQLWarning warning = ps.getWarnings();
                while (warning != null) {
                    LOG.warn("SQLWarning: {}", warning.getMessage());
                    warning = warning.getNextWarning();
                }
                
                // Mapper les données
                sql.preparedstatement.psCreateUpdateClub.psMapCreate(ps, club);

                // Exécuter l'insert
                int row = ps.executeUpdate();
                
                if (row == 0) {
                    msg = "Fatal Error: No row inserted in " + methodName;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }
            
            // ========================================
            // Récupération de l'ID généré
            // ========================================
            int generatedId = LCUtil.generatedKey(conn);
            club.setIdclub(generatedId);
            
            msg = String.format("Club created: %s (ID: %d)", 
                               club.getClubName(), 
                               club.getIdclub());
            LOG.debug(msg);
           // LCUtil.showMessageInfo(msg);
            
            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            msg = "Club creation committed successfully";
            LOG.debug(msg);
            
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


    public boolean upsert(final Club club) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            INSERT INTO club (idclub, ClubName, clubAddress, clubCity, clubCountry, ClubLatitude, ClubLongitude, ClubWebsite, ClubZoneId, ClubLocalAdmin, GroundCondition, ClubModificationDate)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            AS vals
            ON DUPLICATE KEY UPDATE
                ClubName             = vals.ClubName,
                clubAddress          = vals.clubAddress,
                clubCity             = vals.clubCity,
                clubCountry          = vals.clubCountry,
                ClubLatitude         = vals.ClubLatitude,
                ClubLongitude        = vals.ClubLongitude,
                ClubWebsite          = vals.ClubWebsite,
                ClubZoneId           = vals.ClubZoneId,
                ClubLocalAdmin       = vals.ClubLocalAdmin,
                GroundCondition      = vals.GroundCondition,
                ClubModificationDate = CURRENT_TIMESTAMP
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (club.getIdclub() != null && club.getIdclub() > 0) {
                ps.setInt(1, club.getIdclub());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            sql.preparedstatement.psCreateUpdateClub.psMapUpsert(ps, club);

            int rows = ps.executeUpdate();
            LOG.debug("club upserted id = {} name = {} rows = {}", club.getIdclub(), club.getClubName(), rows);
            return rows > 0;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class