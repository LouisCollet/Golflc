package delete;

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
import utils.LCUtil;

/**
 * Service de suppression de Tee
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class DeleteTee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;



    /**
     * Supprime un Tee (simple delete)
     *
     * @param tee Le tee à supprimer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean delete(final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false");

            // Validation
            if (tee == null) {
                msg = "Tee cannot be null";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            if (tee.getIdtee() == null || tee.getIdtee() == 0) {
                msg = "Tee ID is required for deletion";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.debug("Deleting tee: {} (ID: {})", tee.getIdtee());

            // Delete Tee
            String query = """
                DELETE FROM tee
                WHERE tee.idtee = ?
                """;
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, tee.getIdtee());
                LCUtil.logps(ps);

                int rowsDeleted = ps.executeUpdate();
                LOG.debug("Rows deleted: {}", rowsDeleted);

                if (rowsDeleted == 0) {
                    msg = "No tee deleted - Tee may not exist: ID " + tee.getIdtee();
                    LOG.warn(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }

            msg = String.format("Tee deleted: %s %s (ID: %d)",
                               tee.getTeeStart(), tee.getTeeGender(), tee.getIdtee());
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            conn.commit();
            LOG.debug("Tee deletion committed successfully");

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
            tee.setIdtee(100);
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class DeleteTee {
    public boolean delete(final Tee tee, final Connection conn) throws SQLException {
    PreparedStatement ps = null;
try{
       LOG.debug("starting Delete Tee ... = " );
       LOG.debug(" with tee = "  + tee);
     // question : que faire si on delete un MasterTee ? donner un message !!
     // question : que faire si on delete un DistanceTee - fait mais ps correct si pas de distance tee !

    String query =  """
       DELETE from tee
       WHERE tee.idtee = ?
       """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getIdtee());
    LCUtil.logps(ps);
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Tee = {}", row_deleted);
    String msg = "<br/> <h2>There are " + row_deleted + " Tee deleted = " + tee;
        LOG.debug(msg);
        showMessageInfo(msg);
    if(row_deleted != 0){
           msg = "Tee Deleted = " + tee;
           LOG.info(msg);
           showMessageInfo(msg);
        // new 16-08-2023  non testé
           query = """
             DELETE from distances
             WHERE DistanceIdTee = ?
          """;
            ps = conn.prepareStatement(query);
            ps.setInt(1, tee.getIdtee());
            LCUtil.logps(ps);
            int row_inscription = ps.executeUpdate();
            LOG.debug("deleted DistanceTee = {}", row_inscription);
           return true;
    } else {
           msg = "ERROR tee NOT Deleted !!: " + tee;
           LOG.debug(msg);
           showMessageFatal(msg);
           return false;
    }
}catch (SQLException e){
    String msg = "SQL Exception in DeleteTee = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode() + "<br/>for tee = " + tee;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteTee() " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{
        connection_package.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
    Tee tee = new Tee();
    tee.setIdtee(339);
    boolean b = new DeleteTee().delete(tee, conn);
        LOG.debug("from main - resultat deleteTee = {}", b);
 } catch (Exception e) {
        String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null);
          }
} // end method main
} //end class
*/