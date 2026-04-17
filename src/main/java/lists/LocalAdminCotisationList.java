package lists;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import manager.PlayerManager;
import rowmappers.ClubRowMapper;
import rowmappers.CotisationRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.RowMapper;

@Named("LACotisation")
@ApplicationScoped // migrated from @ViewScoped 2026-03-22
public class LocalAdminCotisationList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private PlayerManager playerManager;

    // ✅ Cache d'instance — @ApplicationScoped singleton, invalidated via CacheInvalidator
    private List<ECourseList> liste = null;

    public LocalAdminCotisationList() { }

    public List<ECourseList> list(final Player localAdmin) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with Local Admin = " + localAdmin);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM payments_cotisation, club, player
            WHERE club.ClubLocalAdmin = ?
              AND payments_cotisation.CotisationIdClub = club.idclub
              AND player.idplayer = payments_cotisation.CotisationIdPlayer
            ORDER BY cotisationIdclub, playerlastname
            """;

        RowMapper<Club> clubMapper = new ClubRowMapper();
        RowMapper<Player> playerMapper = new PlayerRowMapper();
        RowMapper<Cotisation> cotisationMapper = new CotisationRowMapper();

        liste = new ArrayList<>(dao.queryList(query, rs -> ECourseList.builder()
                .club(clubMapper.map(rs))
                .player(playerMapper.map(rs))
                .cotisation(cotisationMapper.map(rs))
                .build(),
                localAdmin.getIdplayer()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public double getTotalForClub(int clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (liste == null) return 0.0;
        return liste.stream()
                .filter(o -> o.getClub().getIdclub() == clubId)
                .mapToDouble(o -> o.getCotisation().map(Cotisation::getPrice).orElse(0.0))
                .sum();
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324715);
        player = playerManager.readPlayer(player.getIdplayer());
        var lp = new LocalAdminCotisationList().list(player);
        LOG.debug("from main, result = " + lp);
    } // end main
    */

} // end class
