package charts;

import entite.Handicap;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class HandicapDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public HandicapDetail() { }

    public List<Handicap> getStatHcp(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT idhandicap, HandicapPlayerEGA
            FROM handicap
            WHERE player_idplayer = ?
            """;

        try {
            List<Handicap> liste = dao.queryList(query, new rowmappers.HandicapRowMapper(), player.getIdplayer());
            LOG.debug("list size = {}", liste.size());
            return liste;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

} // end class
