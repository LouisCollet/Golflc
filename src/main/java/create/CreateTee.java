package create;

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
import static utils.LCUtil.showMessageFatal;

@ApplicationScoped
public class CreateTee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateTee() { }

    public boolean create(final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (tee == null) {
                LOG.error("tee cannot be null");
                throw new IllegalArgumentException("Tee cannot be null");
            }

            if (tee.getCourse_idcourse() == 0) {
                LOG.error("tee must be associated with a course");
                throw new IllegalArgumentException("Tee must be associated with a course");
            }

            LOG.debug("creating tee = {}", tee);

            String query = LCUtil.generateInsertQuery(conn, "tee");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateTee.mapCreate(ps, tee);
                LCUtil.logps(ps);

                int row = ps.executeUpdate();

                if (row == 0) {
                    String msg = "Fatal Error: No TEE created";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    throw new SQLException(msg);
                }
            }

            int generatedId = LCUtil.generatedKey(conn);
            tee.setIdtee(generatedId);
            LOG.debug("tee created id = {}", tee.getIdtee());
            conn.commit();

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
    } // end main
    */

} // end class
