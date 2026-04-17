package create;

import entite.Course;
import entite.Distance;
import entite.HolesGlobal;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import utils.LCUtil;

@ApplicationScoped
public class CreateOrUpdateHolesGlobal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private create.CreateDistances createDistances;

    @Inject
    private find.FindCountHoles findCountHoles;

    @Inject
    private create.CreateHolesGlobal createHolesGlobal;

    @Inject
    private update.UpdateHole updateHole;

    public CreateOrUpdateHolesGlobal() { }

    public boolean status(final HolesGlobal holesGlobal, final Tee tee, final Course course) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for type = {}", holesGlobal.getType());

        try {
            if (holesGlobal.getType().equals("distance")) {
                LOG.debug("handling distance");
                return handleDistance(tee, holesGlobal);
            } else {
                handleDistance(tee, holesGlobal);
                int rows = findCountHoles.find(tee);
                LOG.info("numbers of rows = {}", rows);
                if (rows == 0) {
                    LOG.info("This is an Insert {}", rows);
                    return createHolesGlobal.create(holesGlobal, tee, course);
                } else {
                    LOG.info("This is a Modify {}", rows);
                    return updateHole.update(holesGlobal, tee);
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

    private boolean handleDistance(final Tee tee, final HolesGlobal holesGlobal) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Distance distance = new Distance();
            distance.setIdTee(tee.getIdtee());
            LOG.debug("input dataHoles for distance = {}", Arrays.deepToString(holesGlobal.getDataHoles()));
            var v = utils.LCUtil.extractFrom2D(holesGlobal.getDataHoles(), 3);
            LOG.debug("input extracted from dataholes = {}", Arrays.toString(v));
            distance.setDistanceArray(v);
            return createDistances.create(distance);
        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
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
