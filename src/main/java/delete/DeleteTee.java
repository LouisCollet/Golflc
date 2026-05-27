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
