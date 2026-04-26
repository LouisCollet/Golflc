package update;

import entite.HolesGlobal;
import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

@ApplicationScoped
public class UpdateHole implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UpdateHole() { }

    public boolean update(final HolesGlobal holesGlobal, final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        try (Connection conn = dao.getConnection()) {

            conn.setAutoCommit(false);

            if (holesGlobal == null || holesGlobal.getDataHoles() == null) {
                LOG.error("holesGlobal data cannot be null");
                throw new IllegalArgumentException("HolesGlobal data cannot be null");
            }

            if (tee == null || tee.getIdtee() == null || tee.getIdtee() == 0) {
                LOG.error("valid tee is required");
                throw new IllegalArgumentException("Valid tee is required");
            }

            int totalHoles = holesGlobal.getDataHoles().length;
            LOG.debug("holesGlobal = {}", holesGlobal);
            LOG.debug("tee = {}", tee);
            LOG.debug("total holes = {}", totalHoles);

            final String updateQuery = """
                UPDATE hole
                SET HolePar = ?,
                    HoleDistance = ?,
                    HoleStrokeIndex = ?
                WHERE tee_idtee = ?
                  AND HoleNumber = ?
                """;

            final String insertQuery = """
                INSERT INTO hole (idhole, HoleNumber, HolePar, HoleDistance, HoleStrokeIndex,
                                  tee_idtee, tee_course_idcourse, HoleModificationDate)
                VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)
                """;

            int updateCount = 0;
            int insertCount = 0;

            try (PreparedStatement psUpdate = conn.prepareStatement(updateQuery);
                 PreparedStatement psInsert = conn.prepareStatement(insertQuery)) {

                for (int i = 0; i < totalHoles; i++) {
                    int[] holeData = holesGlobal.getDataHoles()[i];
                    int holeNumber  = holeData[0];
                    byte par        = (byte) holeData[1];
                    byte strokeIndex = (byte) holeData[2];

                    LOG.debug("processing hole #{}: par={} index={} distance={}",
                             holeNumber, par, strokeIndex, holeData[3]);

                    sql.preparedstatement.psUpdateHole.psMapUpdate(psUpdate, par, strokeIndex, tee.getIdtee(), holeNumber);
                    LCUtil.logps(psUpdate);
                    int row = psUpdate.executeUpdate();

                    if (row != 0) {
                        updateCount++;
                        LOG.debug("UPDATE hole #{} tee={} rows={}", holeNumber, tee.getIdtee(), row);
                    } else {
                        LOG.info("hole #{} not found for tee {} — inserting", holeNumber, tee.getIdtee());
                        sql.preparedstatement.psUpdateHole.psMapInsert(psInsert, holeNumber, par, strokeIndex,
                                tee.getIdtee(), tee.getCourse_idcourse());
                        LCUtil.logps(psInsert);
                        int inserted = psInsert.executeUpdate();

                        if (inserted != 0) {
                            insertCount++;
                            LOG.debug("INSERT hole #{} tee={}", holeNumber, tee.getIdtee());
                        } else {
                            LOG.error("INSERT failed for hole #{}", holeNumber);
                            LCUtil.showMessageFatal("ERROR: could not insert hole #" + holeNumber);
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            LOG.debug("batch upsert: {} updated, {} inserted ({} total) tee={}", updateCount, insertCount, totalHoles, tee.getIdtee());
            String msg = LCUtil.prepareMessageBean("hole.global.modify");
            LCUtil.showMessageInfo(msg);
            conn.commit();

            return true;

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
