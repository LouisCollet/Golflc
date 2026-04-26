package find;

import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Cherche le round existant à un créneau (course + date exacte).
 * Retourne null si aucun round n'existe encore à ce slot.
 *
 * Note timezone : DB stocke {@code RoundDate} en UTC naïf. Le paramètre {@code slotDateLocal}
 * est exprimé dans la timezone du club — converti en UTC pour la clause WHERE.
 * Le {@code Round} retourné a son {@code RoundDate} converti en local club.
 */
@ApplicationScoped
public class FindRoundBySlot implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Inject private dao.GenericDAO dao;

    public FindRoundBySlot() { }

    public Round find(int idcourse, LocalDateTime slotDateLocal, ZoneId clubZone) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("idcourse={}, slotDateLocal={}, clubZone={}", idcourse, slotDateLocal, clubZone);

        LocalDateTime slotUtc = slotDateLocal.atZone(clubZone).withZoneSameInstant(UTC).toLocalDateTime();
        LOG.debug("slotDateUtc (for WHERE) = {}", slotUtc);

        final String query = """
            SELECT idround, RoundName, RoundGame, RoundQualifying,
                   RoundHoles, RoundStart, RoundDate, course_idcourse
            FROM round
            WHERE course_idcourse = ?
              AND RoundDate = ?
            LIMIT 1
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idcourse);
            ps.setObject(2, slotUtc, JDBCType.TIMESTAMP);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    LOG.debug("no round found for slot");
                    return null;
                }
                Round r = new Round();
                r.setIdround(rs.getInt("idround"));
                r.setRoundName(rs.getString("RoundName"));
                r.setRoundGame(rs.getString("RoundGame"));
                r.setRoundQualifying(rs.getString("RoundQualifying"));
                r.setRoundHoles(rs.getShort("RoundHoles"));
                r.setRoundStart(rs.getShort("RoundStart"));
                LocalDateTime ldtUtc = rs.getObject("RoundDate", LocalDateTime.class);
                if (ldtUtc != null) {
                    // retourne le roundDate en local club (cohérent avec le reste du code applicatif)
                    r.setRoundDate(ldtUtc.atZone(UTC).withZoneSameInstant(clubZone).toLocalDateTime());
                }
                r.setCourseIdcourse(rs.getInt("course_idcourse"));
                LOG.debug("round found idround={} roundDate(local)={}", r.getIdround(), r.getRoundDate());
                return r;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // Signature utilitaire — suffix silencieux sans SQLException. Pratique pour les callers qui ne veulent pas wrap.
    @SuppressWarnings("unused")
    private Timestamp asTimestamp(LocalDateTime ldt) { return Timestamp.valueOf(ldt); }

} // end class
