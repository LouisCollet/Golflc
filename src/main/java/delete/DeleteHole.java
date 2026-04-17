package delete;

import entite.Hole;
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
 * Service de suppression de Hole
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Gestion transactionnelle avec commit/rollback
 */
@ApplicationScoped
public class DeleteHole implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteHole() { }

    /**
     * Supprime un Hole (simple delete)
     *
     * @param holeId Le trou à supprimer
     * @return true si succès, false sinon
     * @throws Exception en cas d'erreur
     */
    public boolean delete(final int holeId) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg;

         try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);
            LOG.info("AutoCommit set to false");

            // Validation
        //    if (holeId == 0) {
         //       msg = "Hole cannot be null";
         ///       LOG.error(msg);
         //       LCUtil.showMessageFatal(msg);
         //       throw new IllegalArgumentException(msg);
         //   }

            if (holeId== 0) {
                msg = "Hole ID is required for deletion";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                throw new IllegalArgumentException(msg);
            }

            LOG.debug("Deleting hole #{} (ID: {})", holeId);

            // Delete Hole
            String query = """
                DELETE FROM hole
                WHERE hole.idhole = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, holeId);
                LCUtil.logps(ps);

                int rowsDeleted = ps.executeUpdate();
                LOG.debug("Rows deleted: {}", rowsDeleted);

                if (rowsDeleted == 0) {
                    msg = "No hole deleted - Hole may not exist: ID " + holeId;
                    LOG.warn(msg);
                    LCUtil.showMessageInfo(msg);
                    return false;
                }
            }

            msg = String.format("Hole #%d deleted (ID: %d)", holeId);
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);

            conn.commit();
            LOG.debug("Hole deletion committed successfully");

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
            Hole hole = new Hole();
            hole.setIdhole(200);
            LOG.debug("Main ready (CDI required for execution)");
        } catch (Exception e) {
            LOG.error("Exception in main: {}", e.getMessage(), e);
        }
    } // end main
*/
} // end class
/*
import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import static interfaces.Log.LOG;
import utils.LCUtil;

public class DeleteHoles {
    public boolean delete(final Tee tee, final Connection conn) throws Exception    {
    PreparedStatement ps = null;
try
{       LOG.debug("starting Delete Holes ... = " );
        LOG.debug("Delete Holes for idtee "  + tee);
    String query =
       " DELETE from hole" +
       " WHERE hole.tee_idtee = ?"
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getIdtee());
    LCUtil.logps(ps);
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Holes = {}", row_deleted);
    String msg = "<br/> <h1> There are " + row_deleted + " Holes deleted for tee = " + tee;
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteHoles = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteHoles() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
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
    boolean b = new DeleteHoles().delete(tee, conn);
        LOG.debug("from main - resultat deleteRound = {}", b);
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