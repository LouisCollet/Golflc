package read;

import entite.Club;
import entite.Round;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import rowmappers.RowMapper;
import rowmappers.UnavailablePeriodRowMapper;

@ApplicationScoped
public class ReadUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadUnavailablePeriod() { }

    public UnavailablePeriod read(final Club club, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for round = " + round.toString());
        LOG.debug(methodName + " - for club = " + club.toString());

        final String query = """
                SELECT *
                FROM unavailable_periods
                WHERE UnavailableIdClub = ?
                AND DATE(UnavailableStartDate) <= DATE(?)
                AND DATE(UnavailableEndDate) >= DATE(?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, club.getIdclub());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            ps.setTimestamp(3, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapper<UnavailablePeriod> periodMapper = new UnavailablePeriodRowMapper();
                UnavailablePeriod period = new UnavailablePeriod();
                while (rs.next()) {
                    period = periodMapper.map(rs);
                }
                LOG.debug(methodName + " - unavailable period = " + period);
                if (period.getIdclub() == null) {
                    LOG.debug(methodName + " - no unavailable period found for club = " + club.getIdclub());
                    return null;
                }
                LOG.debug(methodName + " - found period = " + period);
                return period;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Club club = new Club(); club.setIdclub(1122);
        // Round round = new Round(); round.setIdround(102);
        // round.setRoundDate(LocalDateTime.of(2020, Month.FEBRUARY, 17, 12, 15));
        // UnavailablePeriod period = new ReadUnavailablePeriod().read(club, round);
        // LOG.debug("unavailable found = " + period);
    } // end main
    */

} // end class
