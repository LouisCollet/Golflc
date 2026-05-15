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

} // end class
