
package lists;

import entite.Player;
import entite.Professional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static interfaces.Log.LOG;

/**
 * Liste les professionnels pour un joueur donne
 * Migre vers GenericDAO (2026-03-18)
 */
@ApplicationScoped
public class FindCountListProfessional implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // Pas de cache — résultat spécifique à chaque joueur (@ApplicationScoped partagé entre tous les utilisateurs)
    private static final String QUERY = """
            SELECT *
            FROM professional
            WHERE professional.ProPlayerId = ?
            AND NOW() BETWEEN ProClubStartDate AND ProClubEndDate
            LIMIT 500
            """;

    public List<Professional> list(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (player == null || player.getIdplayer() == null || player.getIdplayer() <= 0) {
            LOG.warn("player is null or has no ID");
            return Collections.emptyList();
        }

        LOG.debug("querying database for player ID={}", player.getIdplayer());

        RowMapper<Professional> professionalMapper = new ProfessionalRowMapper();
        List<Professional> result = dao.queryList(QUERY, professionalMapper, player.getIdplayer());

        if (result.isEmpty()) {
            LOG.debug("no professionals found for player ID={}", player.getIdplayer());
        } else {
            LOG.debug("found {} professional(s)", result.size());
            result.forEach(item ->
                    LOG.debug("Professional: proId={}, playerId={}, club={}",
                            item.getProId(), item.getProPlayerId(), item.getProClubId()));
        }
        return result;
    } // end method

    public void invalidateCache() {
        // No-op — pas de cache, résultat player-specific
    } // end method

    // ========================================
    // MAIN DE TEST - conserve commente
    // ========================================

    /*
    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        try {
            conn = new DBConnection().getConnection();

            Player player = new Player();
            player.setIdplayer(324713);

            FindCountListProfessional finder = new FindCountListProfessional();

            LOG.debug("TEST 1: Premier appel - devrait faire la requete SQL");
            LOG.debug("isCached() avant = " + finder.isCached());
            List<Professional> lp1 = finder.list(player, conn);
            LOG.debug("isCached() apres = " + finder.isCached());
            LOG.debug("Nombre de professionals = " + lp1.size());

            LOG.debug("TEST 2: Deuxieme appel - devrait utiliser le cache");
            List<Professional> lp2 = finder.list(player, conn);
            LOG.debug("Meme liste ? " + (lp1 == lp2));

            LOG.debug("TEST 3: Apres invalidation");
            FindCountListProfessional.setListe(null);
            List<Professional> lp3 = finder.list(player, conn);
            LOG.debug("Nouvelle liste ? " + (lp1 != lp3));

            LOG.debug("TEST 4: player null");
            List<Professional> lpNull = finder.list(null, conn);
            LOG.debug("Liste avec player null = " + lpNull.size());

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        } finally {
            DBConnection.closeQuietly(conn, null, null, null);
        }
    } // end main
    */

} // end class
