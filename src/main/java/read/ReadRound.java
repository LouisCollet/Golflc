package read;

import entite.Round;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import rowmappers.RoundRowMapper;
import rowmappers.RowMapperRound;

@ApplicationScoped
public class ReadRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadRound() { }

    public Round read(Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (round == null || round.getIdround() == null) {
            throw new IllegalArgumentException("Round.idround must not be null");
        }

        final String query = """
            SELECT *
            FROM Round
            WHERE idround = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                if (rs.next()) {
                    Round mapped = roundMapper.map(rs, null);
                    mapped.setIdround(round.getIdround()); // garantie
                    return mapped;
                }
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
     * Retourne les heures de départ déjà réservées (rounds existants) pour un course+date donnés.
     * Utilisé pour filtrer les flights disponibles sans passer par la table flight.
     *
     * @param courseId   identifiant du course
     * @param date       date du round sélectionné
     * @param clubZoneId timezone du club (ex. "Europe/Paris") — utilisée pour convertir UTC→local
     * @return liste des LocalDateTime réservés en heure locale du club (tronqués à la minute)
     */
    public List<LocalDateTime> readBookedTimesForCourseDate(int courseId, LocalDate date, String clubZoneId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT RoundDate
            FROM round
            WHERE course_idcourse = ?
            AND DATE(RoundDate) = ?
            """;

        ZoneId clubZone = ZoneId.of(clubZoneId);

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, courseId);
            // La date passée est en heure locale — la comparaison DATE() en DB est en UTC,
            // donc on passe la date UTC correspondant à minuit local du club.
            ps.setDate(2, Date.valueOf(date));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                List<LocalDateTime> booked = new ArrayList<>();
                while (rs.next()) {
                    // DB stocke en UTC — convertir vers timezone du club avant comparaison
                    LocalDateTime utcLdt = rs.getTimestamp(1).toLocalDateTime();
                    LocalDateTime clubLdt = utcLdt.atZone(ZoneId.of("UTC"))
                                                  .withZoneSameInstant(clubZone)
                                                  .toLocalDateTime()
                                                  .truncatedTo(ChronoUnit.MINUTES);
                    booked.add(clubLdt);
                }
                LOG.debug("{} booked times for course={} date={} zone={}", booked.size(), courseId, date, clubZoneId);
                return booked;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new ArrayList<>();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new ArrayList<>();
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new ReadRound().read(round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #read(Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Round round = new Round();
        round.setIdround(630);
        round = read(round);
        LOG.debug("loaded round = {}", round);
    } // end main
    */

} // end class
