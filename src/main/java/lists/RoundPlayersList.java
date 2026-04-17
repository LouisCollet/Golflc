package lists;

import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rowmappers.PlayerRowMapper;

@Named
@ApplicationScoped
public class RoundPlayersList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<Player> liste = null;

    public RoundPlayersList() { }

    public List<Player> list(final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for round = " + round);

        if (round.getIdround() == null) {
            LOG.warn(methodName + " - round.idround is null, returning empty list");
            return Collections.emptyList();
        }

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM player_has_round, round, player
            WHERE round.idround = ?
              AND InscriptionIdRound = round.idround
              AND player.idplayer = InscriptionIdPlayer
            """;

        liste = new ArrayList<>(dao.queryList(query, new PlayerRowMapper(), round.getIdround()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - no inscriptions yet for round=" + round.getIdround());
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<Player> getListe()                { return liste; }
    public void         setListe(List<Player> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new RoundPlayersList().list(round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #list(Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Round round = new Round();
        round.setIdround(628);
        List<Player> p1 = list(round);
        LOG.debug("inscription list = " + p1.toString());
    } // end main
    */

} // end class
