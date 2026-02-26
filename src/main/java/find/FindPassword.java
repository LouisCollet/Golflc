package find;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import entite.composite.EPlayerPassword;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;

/**
 * ✅ @ApplicationScoped — Stateless, partagé
 * Migré CDI 2026-02-25
 */
@ApplicationScoped
public class FindPassword implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindPassword() { }

    /**
     * ✅ CDI — nouvelle signature sans Connection
     */
    public boolean passwordMatch(final EPlayerPassword epp) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for EPlayerPassword = " + epp);

        final String query = """
            SELECT *
            FROM player
            WHERE player.idplayer = ?
            AND player.PlayerPassword = SHA2(?,256)
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, epp.player().getIdplayer());
            ps.setString(2, epp.password().getCurrentPassword());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LOG.debug(methodName + " - password match OK");
                    String msg = LCUtil.prepareMessageBean("password.match") + epp.password().getCurrentPassword();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String err = LCUtil.prepareMessageBean("password.notmatch");
                    LCUtil.showMessageFatal(err);
                    LOG.error(err);
                    return false;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /**
     * @deprecated Bridge legacy — appelants non-CDI qui fournissent leur propre Connection
     */
/*
void main() throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/

} // end class
