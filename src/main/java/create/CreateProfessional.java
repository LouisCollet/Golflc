package create;

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
import java.time.LocalDateTime;
import static utils.LCUtil.generatedKey;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateProfessional implements Serializable, interfaces.Log, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public CreateProfessional() { }

    public boolean create(final Professional professional) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with Professional  = " + professional);

        try (Connection conn = dao.getConnection()) {
            final String query = utils.LCUtil.generateInsertQuery(conn, "professional");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                sql.preparedstatement.psCreateProfessional.psMapCreate(ps, professional);
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    professional.setProId(generatedKey(conn));
                    String msg = "Professional Created = " + professional;
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "<br/>ERROR insert Professional : " + professional;
                    LOG.debug(msg);
                    showMessageFatal(msg);
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

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Professional pro = new Professional();
        pro.setProStartDate(LocalDateTime.parse("2021-01-01T00:00:00"));
        pro.setProEndDate(LocalDateTime.parse("2050-12-31T23:59:59"));
        pro.setProClubId(1186);
        pro.setProPlayerId(324720);
        boolean lp = new CreateProfessional().create(pro);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
