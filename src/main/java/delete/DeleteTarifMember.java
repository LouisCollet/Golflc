package delete;

import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public DeleteTarifMember() { }

    public boolean delete(final TarifMember tarif) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        final String query = """
                DELETE
                FROM tarif_members
                WHERE tarif_members.TarifMemberIdClub = ?
                AND DATE(TarifMemberStartDate) = DATE(?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, tarif.getTarifMemberIdClub());
            ps.setTimestamp(2, Timestamp.valueOf(tarif.getStartDate()));
            LCUtil.logps(ps);
            int rowDeleted = ps.executeUpdate();
            String msg = "There are " + rowDeleted + " Tarifmembers deleted = " + tarif;
            if (rowDeleted != 0) {
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
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

    /** @deprecated Use {@link #delete(TarifMember)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        TarifMember tarif = new TarifMember();
        tarif.setTarifMemberIdClub(1104);
        boolean b = new DeleteTarifMember().delete(tarif);
        LOG.debug("from main - resultat deleted TarifMember = " + b);
    } // end main
    */

} // end class
