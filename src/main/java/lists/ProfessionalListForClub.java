package lists;

import entite.Club;
import entite.Greenfee;
import entite.Player;
import entite.Professional;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.faces.view.ViewScoped;
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
import rowmappers.ClubRowMapper;
import rowmappers.GreenfeeRowMapper;
import rowmappers.PlayerRowMapper;
import rowmappers.ProfessionalRowMapper;
import rowmappers.RowMapper;

// vérifier si un player est aussi un pro : retourner les liste des clubs où il est pro

@Named()
@ViewScoped // ?? nécessaire !! pour faire le total dans local_administrator_cotisations.xhtml
public class ProfessionalListForClub implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();
                RowMapper<Player> playerMapper = new PlayerRowMapper();
                RowMapper<Greenfee> greenfeeMapper = new GreenfeeRowMapper();
                RowMapper<Professional> professionalMapper = new ProfessionalRowMapper();

                while (rs.next()) {
                    ECourseList ecl = ECourseList.builder()
                            .club(clubMapper.map(rs))
                            .professional(professionalMapper.map(rs))
                            .player(playerMapper.map(rs))
                            .build();
                    liste.add(ecl);
                }
                if (liste.isEmpty()) {
                    LOG.warn(methodName + " - empty result list");
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
