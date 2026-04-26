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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.TextStyle;
import java.util.Locale;
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
        LOG.debug("entering {}", methodName);
        LOG.debug("with Professional  = {}", professional);

        try (Connection conn = dao.getConnection()) {
            String conflict = findOverlappingDays(conn, professional);
            if (conflict != null) {
                String msg = "Cannot create professional: day overlap with " + conflict;
                LOG.warn(msg);
                showMessageFatal(msg);
                return false;
            }
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
                    LOG.error("insert professional failed for = {}", professional);
                    showMessageFatal("ERROR: could not save professional");
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
     * Returns a human-readable description of conflicting days if another professional record
     * for the same player overlaps in period AND in working days, null otherwise.
     * Periods overlap when: existingStart < newEnd AND existingEnd > newStart.
     */
    private String findOverlappingDays(Connection conn, Professional p) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                SELECT ProWorkDays, ProClubId
                FROM professional
                WHERE ProPlayerId = ?
                AND ProClubId != ?
                AND ProClubStartDate < ?
                AND ProClubEndDate > ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, p.getProPlayerId());
            ps.setInt(2, p.getProClubId());
            ps.setTimestamp(3, Timestamp.valueOf(p.getProEndDate()));
            ps.setTimestamp(4, Timestamp.valueOf(p.getProStartDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int existingMask = rs.getInt("ProWorkDays");
                    LOG.debug("bits printing = {}", enumeration.WorkingDay.printWorkingDays(existingMask));
                    
                    int overlap = existingMask & p.getProWorkDays();
                    if (overlap != 0) {
                        int clubId = rs.getInt("ProClubId");
                        return "club #" + clubId + " — conflicting days: " + maskToDayNames(overlap);
                    }
                }
            }
        }
        return null;
    } // end method

    private String maskToDayNames(int mask) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        enumeration.WorkingDay.printWorkingDays(mask);
        StringBuilder sb = new StringBuilder();
        for (enumeration.WorkingDay wd : enumeration.WorkingDay.values()) {
            if ((mask & wd.mask()) != 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(wd.dayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            }
        }
        return sb.toString();
    } // end method

} // end class
