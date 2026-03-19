package delete;

import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

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

        int rowDeleted = dao.execute(query,
                tarif.getTarifMemberIdClub(),
                Timestamp.valueOf(tarif.getStartDate()));
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
