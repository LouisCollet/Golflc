package update;

import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import sql.SqlFactory;
import utils.LCUtil;

/**
 * Service de mise à jour de Tee
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class UpdateTee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    /**
     * Met à jour un Tee dans la base de données
     * 
     * @param tee Le tee à mettre à jour
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean update(final Tee tee) throws SQLException {
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
            
            // ✅ PARTIE NON MODIFIÉE - DÉBUT
            
            // ========================================
            // Validation
            // ========================================
            if (tee == null) {
                msg = "Tee cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            if (tee.getIdtee() == null || tee.getIdtee() == 0) {
                msg = "Tee ID is required for update";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }
            
            // Validation et normalisation des données
            if (tee.getTeeDistanceTee() == null) {
                tee.setTeeDistanceTee(0);
            }
            
            LOG.debug("Updating tee: {} (ID: {})", tee.getIdtee());
            
            // ========================================
            // Update Tee
            // ========================================
            String query = new SqlFactory().generateQueryUpdate(conn, "tee");
            LOG.debug("Update query: {}", query);
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                
                // Mapper les données
                sql.preparedstatement.psCreateUpdateTee.mapUpdate(ps, tee);
                LCUtil.logps(ps);
                
                // Exécuter l'update
                int rowsAffected = ps.executeUpdate();
                LOG.debug("Rows affected: {}", rowsAffected);
                
                if (rowsAffected == 0) {
                    msg = "No rows updated - Tee may not exist: ID " + tee.getIdtee();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }
            
            msg = String.format("Tee updated: %s (ID: %d, Gender: %s, Slope: %d)", 
                             //  tee.getTeeName(),
                               tee.getIdtee(),
                               tee.getTeeGender(),
                               tee.getTeeSlope());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            
            // ========================================
            // Commit transaction
            // ========================================
            conn.commit();
            msg = "Tee update committed successfully";
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

/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Tee tee = new Tee();
            tee.setIdtee(140);
            LOG.debug("Main ready (CDI required for execution)");
        } catch (Exception e) {
            LOG.error("Exception in main: {}", e.getMessage(), e);
        }
    } // end main
*/
} // end class
/*
import entite.Tee;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import utils.LCUtil;

public class UpdateTee implements Serializable, interfaces.Log, interfaces.GolfInterface {
    
    
 public boolean update(final Tee tee, final Connection conn) throws Exception {
  final String methodName = utils.LCUtil.getCurrentMethodName();   
        PreparedStatement ps = null;
  try {
                LOG.debug("entering {}", methodName);
                LOG.debug("with tee  = {}", tee);
            String te = utils.DBMeta.listMetaColumnsUpdate(conn, "tee");
                // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
                // ne fonctionne pas
                //£££ SQLException in update.UpdateTee.update - Parameter index out of range (11 > number of parameters, which is 10). ,SQLState = S1009 ,ErrorCode = 0 
        final String query = """
            UPDATE tee
            SET %s
            WHERE tee.idtee = ?;
           """.formatted(te);
   
       LOG.debug("query formatted = {}", NEW_LINE +query);
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setString(1, tee.getTeeGender());
            ps.setString(2, tee.getTeeStart());
            ps.setInt(3, tee.getTeeSlope());
            ps.setBigDecimal(4, tee.getTeeRating());
            ps.setInt(5, tee.getTeeClubHandicap());
            ps.setString(6, tee.getTeeHolesPlayed());
            ps.setShort(7, tee.getTeePar());
            ps.setInt(8, tee.getTeeMasterTee());
            if(tee.getTeeDistanceTee() == null){  // 12-08-2023
                tee.setTeeDistanceTee(0);
            }
            ps.setInt(9, tee.getTeeDistanceTee());  // new 12-08-2023
    // search key where
            ps.setInt(10, tee.getIdtee());  // ne pas oublier = where
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
      //         LOG.debug("row = {}", row);
            if(row != 0) {
                String msg = LCUtil.prepareMessageBean("tee.modify")
                        + "</h1> <br/>ID = " + tee
                        + " <br/>Start position = " + tee.getTeeStart()
                        + " <br/>Gender = " + tee.getTeeGender()
                        + " <br/>Master Tee = " + tee.getTeeMasterTee();
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "row = 0 - Could not modify tee";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
// new 28/12/2014 - à tester                    
             //   throw (new SQLException(msg));
                return false; // pas compatible avec throw
            }
     //       return true;
  }catch (SQLException sqle) {
            String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  } catch (Exception e) {
            String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyTee

 void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
            Tee tee = new Tee();
            tee.setIdtee(140);
            Tee t = new read.ReadTee().read(tee, conn);
            boolean b = new UpdateTee().update(t, conn);
            LOG.debug("from main, teemodified = {}", b);
        } catch (Exception e) {
            String msg = "££ Exception in main Modify tee= " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class
*/