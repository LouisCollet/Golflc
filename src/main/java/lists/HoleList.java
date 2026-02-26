package lists;

import entite.Hole;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.HoleRowMapper;
import rowmappers.RowMapper;

@Named
@ApplicationScoped
public class HoleList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Hole> liste = null;

    public HoleList() { }

    /**
     * Liste les holes pour un tee donné
     * @param teeId l'ID du tee
     * @return liste des holes (18 holes normalement)
     */
    public List<Hole> listForTee(final int teeId) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for teeId = " + teeId);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT *
                FROM hole
                WHERE hole.tee_idtee = ?
                ORDER BY HoleNumber
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teeId);
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Hole> holeMapper = new HoleRowMapper();

                while (rs.next()) {
                    liste.add(holeMapper.map(rs));
                }

                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list for teeId = " + teeId);
                } else {
                    LOG.debug(methodName + " - list size = " + liste.size());
                }
                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<Hole> getListe()                { return liste; }
    public void       setListe(List<Hole> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        int teeId = 98;
        List<Hole> holes = new HoleList().listForTee(teeId);
        LOG.debug("hole list for tee = " + holes.size());
        holes.forEach(hole -> LOG.debug("Hole: " + hole.getHoleNumber() + " - Par: " + hole.getHolePar()));
    } // end main
    */

} // end class
