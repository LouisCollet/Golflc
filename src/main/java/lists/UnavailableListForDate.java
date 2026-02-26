package lists;

import entite.Club;
import entite.composite.EUnavailable;
import entite.UnavailablePeriod;
import entite.UnavailableStructure;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.RowMapper;
import rowmappers.UnavailablePeriodRowMapper;
import rowmappers.UnavailableStructureRowMapper;
import utils.LCUtil;

@ApplicationScoped
public class UnavailableListForDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<EUnavailable> liste = null;

    public UnavailableListForDate() { }

    public EUnavailable list(final LocalDateTime ldt, final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, club.getIdclub());
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(ldt));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<UnavailableStructure> structureMapper = new UnavailableStructureRowMapper();
                RowMapper<UnavailablePeriod> periodMapper = new UnavailablePeriodRowMapper();

                while (rs.next()) {
                    var structure = structureMapper.map(rs);
                    var period = periodMapper.map(rs);
                    EUnavailable u = new EUnavailable(structure, period);
                    liste.add(u);
                }

                if (liste.isEmpty()) {
                    String msg = "Il n'y a pas aujourd'hui un état du terrain pour ce club : " + club.getIdclub();
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
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<EUnavailable> getListe()              { return liste; }
    public void setListe(List<EUnavailable> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
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
