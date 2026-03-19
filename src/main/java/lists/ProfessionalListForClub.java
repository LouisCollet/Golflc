package lists;

import entite.composite.ECourseList;
import static interfaces.Log.LOG;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro

@Named()
@ViewScoped // ?? nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml
public class ProfessionalListForClub implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    // ✅ Cache d'instance — @ViewScoped resets per view automatically
    private List<ECourseList> liste = null;

    public ProfessionalListForClub() { }

    public List<ECourseList> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        // ✅ Early return — guard clause FIRST
        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        // liste de tous les pro
        final String query = """
            SELECT *
            FROM professional, club, player
            WHERE professional.ProClubId = club.idclub
             AND player.idplayer = ProplayerId
             AND NOW() BETWEEN ProClubStartDate AND ProClubEndDate
            ORDER BY club.ClubName, player.PlayerLastName
            """;

        RowMapper<ECourseList> compositeMapper = rs -> ECourseList.builder()
                .club(new ClubRowMapper().map(rs))
                .professional(new ProfessionalRowMapper().map(rs))
                .player(new PlayerRowMapper().map(rs))
                .build();

        liste = dao.queryList(query, compositeMapper);
        return liste;
    } // end method

    // ✅ Getters/setters d'instance
    public List<ECourseList> getListe()              { return liste; }
    public void setListe(List<ECourseList> liste)    { this.liste = liste; }

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
        List<ECourseList> prof = new ProfessionalListForClub().list();
        LOG.debug("list Pro for Clubs = " + prof.size());
    } // end main
    */

} // end class
