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

@ApplicationScoped
public class UpdateTee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateTee() { }

    public boolean update(final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (tee == null) {
                LOG.error("tee cannot be null");
                throw new IllegalArgumentException("Tee cannot be null");
            }

            if (tee.getIdtee() == null || tee.getIdtee() == 0) {
                LOG.error("tee id is required for update");
                throw new IllegalArgumentException("Tee ID is required for update");
            }

            if (tee.getTeeDistanceTee() == null) {
                tee.setTeeDistanceTee(0);
            }

            LOG.debug("updating tee id = {}", tee.getIdtee());

            String query = new SqlFactory().generateQueryUpdate(conn, "tee");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateUpdateTee.mapUpdate(ps, tee);
                LCUtil.logps(ps);

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    LOG.error("no rows updated for tee id = {}", tee.getIdtee());
                    throw new SQLException("No rows updated — tee id = " + tee.getIdtee());
                }
            }

            String msg = LCUtil.prepareMessageBean("tee.modify");
            LOG.debug("tee updated id = {} gender = {} slope = {}", tee.getIdtee(), tee.getTeeGender(), tee.getTeeSlope());
            LCUtil.showMessageInfo(msg);
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
