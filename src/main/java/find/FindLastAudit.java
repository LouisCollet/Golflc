package find;

import entite.Audit;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class FindLastAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindLastAudit() { }

    public Audit find(Audit audit) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for audit = " + audit);

        final String query = """
              SELECT *
              FROM audit
              WHERE AuditPlayerId = ?
              ORDER by AuditStartDate
              DESC limit 1
          """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, audit.getAuditPlayerId());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                Audit a = null;
                int i = 0;
                while (rs.next()) {
                    i++;
                    a = entite.Audit.mapAudit(rs);
                }
                if (i == 0) {
                    LOG.debug(methodName + " - no audit record for player = " + audit.getAuditPlayerId());
                } else {
                    LOG.debug("ResultSet FindLastAudit has " + i + " lines.");
                }
                return a;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

/*
void main() throws SQLException, Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/

} // end class
