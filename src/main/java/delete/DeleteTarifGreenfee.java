package delete;

import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifGreenfee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteTarifGreenfee() { }

    public boolean delete(final TarifGreenfee tarif, String year) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                DELETE
                FROM tarif_greenfee
                WHERE tarif_greenfee.TarifCourseId = ?
                AND TarifYear = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, tarif.getTarifCourseId());
            ps.setInt(2, Integer.valueOf(year));
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            if (rowDeleted != 0) {
                String msg = "TarifGreenfee deleted ! for year = " + year + " , for courseId = " + tarif.getTarifCourseId();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "Error delete TarifGreenfee for year = " + year + " , for courseId = " + tarif.getTarifCourseId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(TarifGreenfee, String)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        TarifGreenfee tarif = new TarifGreenfee();
        tarif.setTarifCourseId(23);
        boolean b = new DeleteTarifGreenfee().delete(tarif, "2022");
        LOG.debug("from main - resultat deleted TarifGreenfee = " + b);
    } // end main
    */

} // end class
