package lists;

import entite.Club;
import entite.composite.EUnavailable;
import entite.UnavailablePeriod;
import entite.UnavailableStructure;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import rowmappers.RowMapper;
import rowmappers.UnavailablePeriodRowMapper;
import rowmappers.UnavailableStructureRowMapper;
import utils.LCUtil;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;

@ApplicationScoped
public class UnavailableListForDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public UnavailableListForDate() { }

    public EUnavailable list(final LocalDateTime ldt, final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (club == null) {
            LOG.warn(methodName + " - club is null, returning null");
            return null;
        }

        final String query = """
            SELECT *
            FROM club, unavailable_periods
            WHERE club.idclub = ?
            AND unavailable_periods.UnavailableIdClub = club.idclub
            AND ? BETWEEN UnavailableStartDate AND UnavailableEndDate
            ORDER BY unavailable_periods.UnavailableModificationDate desc
            LIMIT 1
            """;

        RowMapper<UnavailableStructure> structureMapper = new UnavailableStructureRowMapper();
        RowMapper<UnavailablePeriod> periodMapper = new UnavailablePeriodRowMapper();

        try {
            List<EUnavailable> result = dao.queryList(query, rs -> {
                var structure = structureMapper.map(rs);
                var period = periodMapper.map(rs);
                return new EUnavailable(structure, period);
            }, club.getIdclub(), java.sql.Timestamp.valueOf(ldt));

            if (result.isEmpty()) {
                String msg = "Il n'y a pas aujourd'hui un etat du terrain pour ce club : " + club.getIdclub();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return null;
            } else {
                LOG.debug("ResultSet " + methodName + " has " + result.size() + " lines.");
                String msg = "Etat du terrain pour ce club : " + result;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return result.getFirst();
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // invalidateCache() conservé pour compatibilité CacheInvalidator
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - no-op (cache removed — result depends on (ldt, club) params)");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Club club = new Club();
        club.setIdclub(1075);
        EUnavailable eu = new UnavailableListForDate().list(LocalDateTime.now(), club);
        LOG.debug("from main, is Unavailable = " + eu);
    } // end main
    */

} // end class
