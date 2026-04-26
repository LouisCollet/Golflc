package find;

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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compte le nombre d'inscriptions (inscription) par RoundDate pour un course
 * donné entre deux dates. Utilisé par le calendrier des créneaux pour afficher "n/4"
 * sur chaque slot.
 *
 * Note timezone : le DB stocke {@code round.RoundDate} en UTC naïf
 * (cf. {@code create.CreateRound}). Les paramètres et les clés du map sont exprimés
 * dans la timezone du club — la conversion est faite ici, localisée et symétrique.
 */
@ApplicationScoped
public class FindRoundCountInRange implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Inject private dao.GenericDAO dao;

    public FindRoundCountInRange() { }

    public Map<LocalDateTime, Integer> find(int idcourse, LocalDateTime fromLocal,
                                            LocalDateTime toLocal, ZoneId clubZone) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("idcourse={}, from={}, to={}, clubZone={}", idcourse, fromLocal, toLocal, clubZone);

        // Convertit les bornes de la recherche (local club) → UTC pour matcher le stockage DB
        LocalDateTime fromUtc = fromLocal.atZone(clubZone).withZoneSameInstant(UTC).toLocalDateTime();
        LocalDateTime toUtc   = toLocal.atZone(clubZone).withZoneSameInstant(UTC).toLocalDateTime();

        final String query = """
            SELECT r.RoundDate AS RoundDate, COUNT(phr.InscriptionIdPlayer) AS cnt
            FROM round r
            LEFT JOIN inscription phr ON phr.InscriptionIdRound = r.idround
            WHERE r.course_idcourse = ?
              AND r.RoundDate >= ?
              AND r.RoundDate <  ?
            GROUP BY r.RoundDate
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idcourse);
            ps.setObject(2, fromUtc, JDBCType.TIMESTAMP);
            ps.setObject(3, toUtc,   JDBCType.TIMESTAMP);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDateTime, Integer> map = new HashMap<>();
                while (rs.next()) {
                    LocalDateTime ldtUtc = rs.getObject("RoundDate", LocalDateTime.class);
                    if (ldtUtc != null) {
                        LocalDateTime ldtLocal = ldtUtc.atZone(UTC).withZoneSameInstant(clubZone).toLocalDateTime();
                        map.put(ldtLocal, rs.getInt("cnt"));
                    }
                }
                LOG.debug("result size = {}", map.size());
                return map;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyMap();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyMap();
        }
    } // end method

    /**
     * Variante admin — retourne aussi les noms des joueurs inscrits par slot.
     */
    public Map<LocalDateTime, SlotInfo> findWithNames(int idcourse, LocalDateTime fromLocal,
                                                      LocalDateTime toLocal, ZoneId clubZone) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        LocalDateTime fromUtc = fromLocal.atZone(clubZone).withZoneSameInstant(UTC).toLocalDateTime();
        LocalDateTime toUtc   = toLocal.atZone(clubZone).withZoneSameInstant(UTC).toLocalDateTime();

        final String query = """
            SELECT r.RoundDate AS RoundDate,
                   p.PlayerFirstName AS firstName,
                   p.PlayerLastName  AS lastName
            FROM round r
            LEFT JOIN inscription phr ON phr.InscriptionIdRound = r.idround
            LEFT JOIN player p ON p.idplayer = phr.InscriptionIdPlayer
            WHERE r.course_idcourse = ?
              AND r.RoundDate >= ?
              AND r.RoundDate <  ?
            ORDER BY r.RoundDate
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idcourse);
            ps.setObject(2, fromUtc, JDBCType.TIMESTAMP);
            ps.setObject(3, toUtc,   JDBCType.TIMESTAMP);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                Map<LocalDateTime, SlotInfo> map = new HashMap<>();
                while (rs.next()) {
                    LocalDateTime ldtUtc = rs.getObject("RoundDate", LocalDateTime.class);
                    if (ldtUtc == null) continue;
                    LocalDateTime ldtLocal = ldtUtc.atZone(UTC).withZoneSameInstant(clubZone).toLocalDateTime();
                    SlotInfo slot = map.computeIfAbsent(ldtLocal, k -> new SlotInfo(0, new ArrayList<>()));
                    String first = rs.getString("firstName");
                    String last  = rs.getString("lastName");
                    if (first != null || last != null) {
                        String display = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                        slot.names.add(display);
                        slot.count++;
                    }
                }
                LOG.debug("result size = {}", map.size());
                return map;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyMap();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyMap();
        }
    } // end method

    /** Slot data for admin display — count + inscribed player names. */
    public static final class SlotInfo {
        public int count;
        public final List<String> names;
        public SlotInfo(int count, List<String> names) {
            this.count = count;
            this.names = names;
        }
    } // end class SlotInfo

} // end class
