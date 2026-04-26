package update;

import entite.Professional;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class UpdateProfessional implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateProfessional() { }

    public boolean updateAmount(final Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Professional = {}", professional);

        final String query = """
                UPDATE professional
                SET ProAmount = ?
                WHERE ProId = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            sql.preparedstatement.psCreateProfessional.psMapUpdate(ps, professional);
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate();
            if (row != 0) {
                LOG.info("ProAmount updated for ProId={} new amount={}", professional.getProId(), professional.getProAmount());
                return true;
            } else {
                LOG.warn("no row updated for ProId={}", professional.getProId());
                return false;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    public boolean updateFull(final Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with Professional = {}", professional);

        final String query = """
                UPDATE professional
                SET ProAmount = ?, ProClubStartDate = ?, ProClubEndDate = ?, ProWorkDays = ?
                WHERE ProId = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            sql.preparedstatement.psCreateProfessional.psMapUpdateFull(ps, professional);
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate();
            if (row != 0) {
                LOG.info("Professional updated for ProId={}", professional.getProId());
                return true;
            } else {
                LOG.warn("no row updated for ProId={}", professional.getProId());
                return false;
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class
