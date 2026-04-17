package delete;

import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import utils.LCUtil;

@ApplicationScoped
public class DeleteTarifGreenfee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteTarifGreenfee() { }

    public boolean deleteById(int tarifId) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                DELETE FROM tarif_greenfee
                WHERE TarifId = ?
                """;

        int rowDeleted = dao.execute(query, tarifId);
        if (rowDeleted != 0) {
            LOG.info("TarifGreenfee deleted by id = {}", tarifId);
            LCUtil.showMessageInfo("TarifGreenfee deleted (id=" + tarifId + ")");
            return true;
        } else {
            String msg = "Error deleteById — no record found for TarifId = " + tarifId;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    public boolean delete(final TarifGreenfee tarif, String year) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                DELETE
                FROM tarif_greenfee
                WHERE tarif_greenfee.TarifCourseId = ?
                AND TarifYear = ?
                AND TarifHoles = ?
                """;

        int rowDeleted = dao.execute(query, tarif.getTarifCourseId(), Integer.valueOf(year), tarif.getTarifHoles());
        if (rowDeleted != 0) {
            String msg = "TarifGreenfee deleted ! for year = " + year
                    + " , courseId = " + tarif.getTarifCourseId()
                    + " , holes = " + tarif.getTarifHoles();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            return true;
        } else {
            String msg = "Error delete TarifGreenfee for year = " + year
                    + " , courseId = " + tarif.getTarifCourseId()
                    + " , holes = " + tarif.getTarifHoles();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        }
    } // end method

    /** @deprecated Use {@link #delete(TarifGreenfee, String)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        TarifGreenfee tarif = new TarifGreenfee();
        tarif.setTarifCourseId(23);
        boolean b = new DeleteTarifGreenfee().delete(tarif, "2022");
        LOG.debug("from main - resultat deleted TarifGreenfee = {}", b);
    } // end main
    */

} // end class
