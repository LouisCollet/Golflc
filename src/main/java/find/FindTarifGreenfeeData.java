package find;

import entite.Round;
import entite.TarifGreenfee;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static interfaces.Log.LOG;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import rowmappers.TarifGreenfeeRowMapper;
import utils.LCUtil;

@ApplicationScoped
public class FindTarifGreenfeeData implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private final TarifGreenfeeRowMapper mapper = new TarifGreenfeeRowMapper();

    public FindTarifGreenfeeData() { }

    public TarifGreenfee find(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - for round = " + round);

        if (round == null || round.getCourseIdcourse() == null) {
            String msg = LCUtil.prepareMessageBean("tarif.greenfee.notfound") + " (no course selected)";
            LOG.warn(methodName + " - " + msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }

        final String query = """
            SELECT TarifJson, TarifCourseId, TarifHoles, TarifCurrency
            FROM tarif_greenfee
            WHERE tarif_greenfee.TarifCourseId = ?
            AND ? BETWEEN tarif_greenfee.TarifStartDate AND tarif_greenfee.TarifEndDate
            AND TarifHoles = ?
            """;

        short holes = round.getRoundHoles() != null ? round.getRoundHoles() : 18;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getCourseIdcourse());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            ps.setShort(3, holes);
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                TarifGreenfee result = null;
                int i = 0;
                while (rs.next()) {
                    i++;
                    result = mapper.map(rs);
                }
                if (i > 0) {
                    LOG.debug(methodName + " - ResultSet has " + i + " lines.");
                    LOG.debug("Tarif Greenfee extracted from database = " + result);
                    return result;
                }
            }

            String msg = LCUtil.prepareMessageBean("tarif.greenfee.notfound.holes") + " (" + holes + "T)";
            LOG.warn(msg);
            LCUtil.showMessageFatal(msg);
            return null;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Silent variant — returns null without FacesMessage if no tarif found.
     * Used for display-only purposes (e.g. Show Tarif dialog). Defaults to 18T.
     */
    public TarifGreenfee findSilent(int courseId) throws SQLException {
        return findSilent(courseId, 18);
    } // end method

    /**
     * Silent variant with explicit holes filter (9 or 18).
     */
    public TarifGreenfee findSilent(int courseId, int holes) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - courseId={}, holes={}", methodName, courseId, holes);

        final String query = """
            SELECT TarifId, TarifJson, TarifCourseId, TarifHoles
            FROM tarif_greenfee
            WHERE TarifCourseId = ?
            AND TarifHoles = ?
            ORDER BY TarifYear DESC
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, courseId);
            ps.setInt(2, holes);
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TarifGreenfee result = mapper.map(rs);
                    LOG.debug(methodName + " - found tarif for courseId={}, holes={}", courseId, holes);
                    return result;
                }
                LOG.debug(methodName + " - no DB tarif found for courseId={}, holes={}", courseId, holes);
                return null;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Loads a TarifGreenfee by its primary key (TarifId).
     * Returns null without FacesMessage if not found.
     */
    public TarifGreenfee findById(int tarifId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - tarifId={}", methodName, tarifId);

        final String query = """
            SELECT TarifId, TarifJson, TarifCourseId, TarifHoles
            FROM tarif_greenfee
            WHERE TarifId = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, tarifId);
            LCUtil.logps(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TarifGreenfee result = mapper.map(rs);
                    LOG.debug("{} - found tarif id={}", methodName, tarifId);
                    return result;
                }
                LOG.warn("{} - no tarif found for id={}", methodName, tarifId);
                return null;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Returns the distinct club IDs that have at least one tarif greenfee valid for the current year.
     * Used to filter the club selector in greenfee simulation.
     */
    public List<Integer> findClubIdsWithTarifForCurrentYear() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT DISTINCT c.club_idclub
            FROM tarif_greenfee tg
            JOIN course c ON c.idcourse = tg.TarifCourseId
            WHERE tg.TarifYear = YEAR(CURDATE())
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            LCUtil.logps(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                LOG.debug("club ids with tarif for current year = {}", ids);
                return ids;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /** @deprecated use {@link #find(Round)} via CDI injection */
    /*
    void main() throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Round round = new Round();
        round.setIdround(755);
        TarifGreenfee tarifGreenfee = find(round);
        LOG.debug("TarifGreenfee in main = " + tarifGreenfee);
    } // end main
    */

} // end class
