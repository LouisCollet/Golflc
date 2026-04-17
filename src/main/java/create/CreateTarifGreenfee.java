package create;

import entite.Club;
import entite.TarifGreenfee;
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
public class CreateTarifGreenfee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO                    dao;
    @Inject private find.FindTarifGreenfeeData        findTarifGreenfeeData;
    @Inject private update.UpdateTarifGreenfee    updateJson;
    @Inject private lists.CourseListForClub           courseListForClub;

    public CreateTarifGreenfee() { }

    public boolean create(final TarifGreenfee tarif, final Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tarif = {}", tarif);
        LOG.debug("for club = {}", club);

        if (tarif.getDatesSeasonsList().isEmpty()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.season.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }
        // removed 2026-04-13 — equipments list can legitimately be empty (BA/DA/HO tarif without equipment options)
        // if (tarif.getEquipmentsList().isEmpty()) {
        //     String msgerr = LCUtil.prepareMessageBean("create.greenfee.equipments.empty");
        //     LOG.error(msgerr);
        //     LCUtil.showMessageFatal(msgerr);
        //     return false;
        // }
        if (tarif.getTwilightList().isEmpty() && tarif.isTwilightReady()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.twilight.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            return false;
        }
        if (tarif.getDatesSeasonsList().isEmpty()
                && tarif.getDaysList().isEmpty()
                && tarif.getTeeTimesList().isEmpty()
                && tarif.getEquipmentsList().isEmpty()
                && tarif.getBasicList().isEmpty()) {
            String msgerr = LCUtil.prepareMessageBean("create.greenfee.empty");
            LOG.error(msgerr);
            LCUtil.showMessageFatal(msgerr);
            throw new Exception(msgerr);
        }

        java.util.List<entite.Course> courses = java.util.Collections.emptyList();
        try { courses = courseListForClub.list(club); } catch (Exception ignored) { }

        boolean allOk = true;
        for (Integer courseId : tarif.getTarifCourseId()) {
            for (Integer holes : tarif.getTarifHolesList()) {
                try (Connection conn = dao.getConnection()) {
                    final String query = LCUtil.generateInsertQuery(conn, "tarif_greenfee");
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        sql.preparedstatement.psCreateTarifGreenfee.mapCreate(ps, tarif, club, courseId, holes);
                        int row = ps.executeUpdate();
                        LOG.debug("row = {} for courseId={}, holes={}", row, courseId, holes);
                        if (row != 0) {
                            String courseName = courses.stream()
                                    .filter(c -> courseId.equals(c.getIdcourse()))
                                    .map(entite.Course::getCourseName)
                                    .findFirst().orElse("");
                            String label = courseName.isEmpty() ? String.valueOf(courseId)
                                    : courseName + " (" + courseId + ")";
                            String msg = "Tarif Created for Course = " + label + " — " + holes + "T";
                            LOG.debug(msg);
                            LCUtil.showMessageInfo(msg);
                        } else {
                            String msg = "NOT Successful TarifGreenfee inserted for courseId=" + courseId + ", holes=" + holes;
                            LOG.error(msg);
                            LCUtil.showMessageFatal(msg);
                            allOk = false;
                        }
                    }
                } catch (SQLException e) {
                    // Duplicate key (courseId + year + holes) → fall back to UPDATE
                    if (e.getErrorCode() == 1062) {
                        LOG.warn("duplicate key for courseId={}, holes={} — falling back to UPDATE", courseId, holes);
                        entite.TarifGreenfee existing = findTarifGreenfeeData.findSilent(courseId, holes);
                        if (existing != null) {
                            tarif.setTarifId(existing.getTarifId());
                            if (!updateJson.update(tarif)) {
                                allOk = false;
                            }
                        } else {
                            LOG.error("duplicate key but existing tarif not found — courseId={}, holes={}", courseId, holes);
                            allOk = false;
                        }
                    } else {
                        handleSQLException(e, methodName);
                        allOk = false;
                    }
                } catch (Exception e) {
                    handleGenericException(e, methodName);
                    allOk = false;
                }
            }   // end for holes
        }   // end for courseId
        return allOk;
    } // end method
} // end class
