package lists;

import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rowmappers.TarifGreenfeeRowMapper;
import utils.LCUtil;

/**
 * Liste de tous les TarifGreenfee (toutes années, tous parcours).
 * Utilisée dans la page d'administration tarif_greenfee_admin.xhtml.
 * Cache d'instance invalidé à chaque CREATE / UPDATE / DELETE.
 */
@ApplicationScoped
public class TarifGreenfeeList implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String QUERY = """
            SELECT TarifId, TarifJson, TarifCourseId, TarifHoles,
                   TarifYear, TarifStartDate, TarifEndDate, TarifCurrency
            FROM tarif_greenfee
            WHERE TarifYear >= YEAR(CURDATE())
            ORDER BY TarifYear DESC, TarifCourseId, TarifHoles
            """;

    @Inject private dao.GenericDAO dao;

    private List<TarifGreenfee> liste = null;

    public TarifGreenfeeList() { }

    public List<TarifGreenfee> list() throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (liste != null) {
            LOG.debug("returning cached list size={}", liste.size());
            return liste;
        }

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(QUERY)) {

            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                TarifGreenfeeRowMapper mapper = new TarifGreenfeeRowMapper();
                while (rs.next()) {
                    TarifGreenfee t = mapper.map(rs);
                    if (t != null) liste.add(t);
                }
                if (liste.isEmpty()) {
                    LOG.warn("empty tarif greenfee list");
                } else {
                    LOG.debug("list size={}", liste.size());
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

    public void invalidateCache() {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug("cache invalidated");
    } // end method

} // end class
