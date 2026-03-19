package create;

import entite.Address;
import entite.Club;
import entite.Country;
import entite.LatLng;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;

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
    public boolean create(final Club club) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        LOG.debug("dao = {}", dao);

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
                LCUtil.logps(ps);
                
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
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = "SQLException in " + methodName + ": " + sqle.getMessage();
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
 
    public static void main(String[] args) {
        try {
            Country country = new Country();
            country.setCode("BE");
            
            LatLng latlng = new LatLng();
            latlng.setLat(50.8262290);
            latlng.setLng(4.3571460);
            
            Address address = new Address();
            address.setCity("Brussels");
            address.setZipCode("1060");
            address.setCountry(country);
            address.setStreet("Rue de l'Amazone 55");
            address.setLatLng(latlng);
            
            Club club = new Club();
            club.setClubName("Club de test");
            club.setAddress(address);
            club.setClubLocalAdmin(324713);
            club.setClubWebsite("https://golf-empereur.com/");
            
            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test club: {}", club);
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
            LCUtil.showMessageFatal("Exception in main: " + e.getMessage());
        }
    }
    * */
} // end class

/*package create;

import entite.Address;
import entite.Club;
import entite.Country;
import entite.LatLng;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Arrays;
import connection_package.DBConnection;
import utils.LCUtil;


public class CreateClub {
    // 
private String[] status = new String [2];


    public boolean create(final Club club, final Connection conn) throws SQLException, Exception{ 
         final String methodName = utils.LCUtil.getCurrentMethodName();
     PreparedStatement ps = null;
  //   status[0] = "false";
     try{
               LOG.debug("entering Createclub.create with club  = " + club.toString());
            final String query = LCUtil.generateInsertQuery(conn, "club");
       // try{ PreparedStatement ps = conn.prepareStatement(query)) {   
            ps = conn.prepareStatement(query);
            ps.getWarnings(); // new 27-04-2025
            SQLWarning warning = ps.getWarnings();
            while (warning != null) {
               LOG.warn("SQLWarning: {}", warning.getMessage());
               warning = warning.getNextWarning();
            }
            ps = sql.preparedstatement.psCreateUpdateClub.psMapCreate(ps,club);
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                club.setIdclub(LCUtil.generatedKey(conn));
                String msg = "Club Created  = " + club;
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
            //    status[0] = "true";
             //   status[1] = Integer.toString(club.getIdclub());
             //   LOG.debug("status = " + Arrays.toString(status));
               // return true;
                return true;
            }else{
                String msg = "<br/><br/>NOT NOT Successful insert for club = " + club.getIdclub();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                //status[0] = "false";
                return false;
            }
   //     } // end try preparedStatement
  }catch (SQLException e){
        handleSQLException(e, methodName);
        return false;
  }catch (Exception e){
        handleGenericException(e, methodName);
        return false;
  } finally {
         connection_package.DBConnection.closeQuietly(conn, null, null, ps); // not used because of try-with-resources
        }
    } // end method createClub
    
    void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try {
       Country country = new Country();
        country.setCode("BE");
       LatLng latlng = new LatLng();
        latlng.setLat(50.8262290);
        latlng.setLng(4.3571460);
       Address address = new Address();
        address.setCity("Brussels");
        address.setZipCode("B-1060");
        address.setCountry(country);
        address.setStreet("Rue de l'Amazone 55");
        address.setLatLng(latlng);
        address.setZipCode("1060");
       Club club = new Club();
        club.setClubName("Club de test");
        club.setAddress(address);
        club.setClubLocalAdmin(324713);
        club.setClubWebsite("https://golf-empereur.com/");
 
       var v = new CreateClub().create(club,conn);
            LOG.debug("from main, CreateAudit = " + v);
        } catch (Exception e) {
            String msg = "££ Exception in main CreateAudit = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main//
} //end Class
*/