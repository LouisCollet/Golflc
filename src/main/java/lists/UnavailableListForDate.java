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

    // Cache d'instance — @ApplicationScoped garantit le singleton
    private List<EUnavailable> liste = null;

    public UnavailableListForDate() { }

    public EUnavailable list(final LocalDateTime ldt, final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
            return liste.getFirst();
        }

        final String query = """
            SELECT *
            FROM club, unavailable_periods
            WHERE club.idclub = ?
            AND UnavailableIdClub = club.idclub
            AND ? BETWEEN UnavailableStartDate AND UnavailableEndDate
            ORDER BY unavailable_periods.UnavailableModificationDate desc
            LIMIT 1
            """;

        RowMapper<UnavailableStructure> structureMapper = new UnavailableStructureRowMapper();
        RowMapper<UnavailablePeriod> periodMapper = new UnavailablePeriodRowMapper();

        try {
            liste = dao.queryList(query, rs -> {
                var structure = structureMapper.map(rs);
                var period = periodMapper.map(rs);
                return new EUnavailable(structure, period);
            }, club.getIdclub(), java.sql.Timestamp.valueOf(ldt));

            if (liste.isEmpty()) {
                String msg = "Il n'y a pas aujourd'hui un etat du terrain pour ce club : " + club.getIdclub();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return null;
            } else {
                LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
                String msg = "Etat du terrain pour ce club : " + liste;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return liste.getFirst();
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // Getters/setters d'instance
    public List<EUnavailable> getListe()              { return liste; }
    public void setListe(List<EUnavailable> liste)    { this.liste = liste; }

    // Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Club club = new Club();
        club.setIdclub(1075);
        EUnavailable eu = new UnavailableListForDate().list(LocalDateTime.now(), club);
        LOG.debug("from main, is Unavailable = " + eu);
    } // end main
    */

} // end class
