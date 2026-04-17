package find;

import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class FindCountHoles implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindCountHoles() { }

    public int find(final Tee tee) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("tee = " + tee);

        final String query = """
                SELECT count(*)
                FROM hole
                WHERE hole.tee_idtee = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, tee.getIdtee());
            utils.LCUtil.logps(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 99; // error code
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return 0;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return 0;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Tee tee = new Tee();
        tee.setIdtee(118);
        int i = new FindCountHoles().find(tee);
        LOG.debug("main - after holes = " + i);
    } // end main
    */

} // end class
