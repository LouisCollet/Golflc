package lists;

import entite.Club;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Named
@ApplicationScoped
public class MatchplayClassmentList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<Round> liste = null;

    public MatchplayClassmentList() { }

    // ne fonctionne pas !!
    public List<Round> list(final Round round, final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - with round = " + round);
        LOG.debug(methodName + " - with club = " + club);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
            SELECT *
            FROM round
            WHERE round.RoundCompetition = ?
            ORDER BY idround DESC
            """;

        liste = new ArrayList<>(dao.queryList(query, new rowmappers.RoundRowMapper(),
                round.getRoundCompetition()));

        if (liste.isEmpty()) {
            LOG.warn(methodName + " - empty result list");
        } else {
            LOG.debug(methodName + " - list size = " + liste.size());
        }
        return liste;
    } // end method

    public List<Round> getListe()                    { return liste; }
    public void        setListe(List<Round> liste)   { this.liste = liste; }

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
        // Club club = new Club();
        // club.setIdclub(1159);
        // Round round = new Round();
        // round.setIdround(608);
        // var tees = list(round, club);
        // LOG.debug("from main, list size = " + tees.size());
        LOG.debug("from main, MatchplayClassmentList = ");
    } // end main
    */

} // end class
