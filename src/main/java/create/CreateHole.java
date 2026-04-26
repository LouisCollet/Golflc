package create;

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

@ApplicationScoped
public class CreateHole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateHole() { }

    public boolean create(final Hole hole) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (hole == null) {
                LOG.error("hole cannot be null");
                throw new IllegalArgumentException("Hole cannot be null");
            }

            if (hole.getTee_course_idcourse() == 0) {
                LOG.error("hole must be associated with a course");
                throw new IllegalArgumentException("Hole must be associated with a course");
            }

            if (hole.getHoleNumber() == null || hole.getHoleNumber() < 1 || hole.getHoleNumber() > 18) {
                LOG.error("hole number must be between 1 and 18");
                throw new IllegalArgumentException("Hole number must be between 1 and 18");
            }

            LOG.debug("creating hole #{} = {}", hole.getHoleNumber(), hole);

            String query = LCUtil.generateInsertQuery(conn, "hole");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateHole.mapCreate(ps, hole);
                LCUtil.logps(ps);

                int row = ps.executeUpdate();

                if (row == 0) {
                    String msg = "Fatal Error: No row inserted";
                    LOG.error(msg);
                    throw new SQLException(msg);
                }
            }

            int generatedId = LCUtil.generatedKey(conn);
            hole.setIdhole(generatedId);
            LOG.debug("hole #{} created id = {}", hole.getHoleNumber(), hole.getIdhole());
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
