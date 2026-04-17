package create;

import entite.Course;
import entite.HolesGlobal;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import utils.LCUtil;

@ApplicationScoped
public class CreateHolesGlobal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateHolesGlobal() { }

    public boolean create(final HolesGlobal holesGlobal, final Tee tee, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("new holes values = {}", NEW_LINE + holesGlobal);
        LOG.debug("course = {}", course);
        LOG.debug("tee = {}", tee);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "hole");
            for (int i = 0; i < holesGlobal.getDataHoles().length; i++) {
                LOG.debug("handling index i = {}", i);
                LOG.debug("handling holesGlobal = {}", Arrays.toString(holesGlobal.getDataHoles()[i]));
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setNull(1, java.sql.Types.INTEGER); // idhole
                    ps.setShort(2, (short) holesGlobal.getDataHoles()[i][0]); // holenumber
                    ps.setShort(3, (short) holesGlobal.getDataHoles()[i][1]); // Par
                    ps.setInt(4, 0); // distance
                    ps.setShort(5, (short) holesGlobal.getDataHoles()[i][2]); // stroke index
                    ps.setInt(6, tee.getIdtee());
                    ps.setInt(7, course.getIdcourse());
                    ps.setTimestamp(8, Timestamp.from(Instant.now()));
                    utils.LCUtil.logps(ps);
                    int row = ps.executeUpdate();
                    if (row != 0) {
                        LOG.debug("Successfull update Hole for hole={} tee={} row={}", holesGlobal.getDataHoles()[i][0], tee.getIdtee(), row);
                    } else {
                        LOG.debug("-- ERROR update Hole for hole : {}", holesGlobal.getDataHoles()[i][0]);
                        return false;
                    }
                }
            } // end for
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
