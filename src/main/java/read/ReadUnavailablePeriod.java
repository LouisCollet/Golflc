package read;

import entite.Club;
import entite.Round;
import entite.UnavailablePeriod;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import rowmappers.UnavailablePeriodRowMapper;

@ApplicationScoped
public class ReadUnavailablePeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadUnavailablePeriod() { }

    public UnavailablePeriod read(final Club club, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for round = {}", round.toString());
        LOG.debug("for club = {}", club.toString());

        final String query = """
                SELECT *
                FROM unavailable_periods
                WHERE UnavailableIdClub = ?
                AND DATE(UnavailableStartDate) <= DATE(?)
                AND DATE(UnavailableEndDate) >= DATE(?)
                """;

        UnavailablePeriod period = dao.querySingle(query, new UnavailablePeriodRowMapper(),
                club.getIdclub(), Timestamp.valueOf(round.getRoundDate()), Timestamp.valueOf(round.getRoundDate()));
        if (period == null || period.getIdclub() == null) {
            LOG.debug("no unavailable period found for club = {}", club.getIdclub());
            return null;
        }
        LOG.debug("found period = {}", period);
        return period;
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Club club = new Club(); club.setIdclub(1122);
        // Round round = new Round(); round.setIdround(102);
        // round.setRoundDate(LocalDateTime.of(2020, Month.FEBRUARY, 17, 12, 15));
        // UnavailablePeriod period = new ReadUnavailablePeriod().read(club, round);
        // LOG.debug("unavailable found = {}", period);
    } // end main
    */

} // end class
