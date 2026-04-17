package create;

import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class CreateTarifMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private find.FindTarifMembersOverlapping findTarifMembersOverlapping;

    public CreateTarifMember() { }

    public boolean create(final TarifMember tarif) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tarif = {}", tarif);

        if (findTarifMembersOverlapping.find(tarif)) {
            return false; // rejected for dates overlapping
        }
        if (tarif.getBasicList().get(0).getEndDate().isBefore(tarif.getBasicList().get(0).getStartDate())) {
            String msgerr = LCUtil.prepareMessageBean("tarif.member.endbeforestart");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.SqlFactory.generateInsertQuery(conn, "tarif_members"))) {
            sql.preparedstatement.psCreateTarifMember.mapCreate(ps, tarif);
            int row = ps.executeUpdate();
            if (row != 0) {
                String msg = "Tarif Member Created  = <br/>" + tarif;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>ERROR insert for tarif : " + tarif;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
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

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class
