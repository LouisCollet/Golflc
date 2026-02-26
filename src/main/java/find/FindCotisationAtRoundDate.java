package find;

import entite.Club;
import entite.Cotisation;
import entite.Player;
import entite.Round;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import rowmappers.CotisationRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

@ApplicationScoped
public class FindCotisationAtRoundDate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public FindCotisationAtRoundDate() { }

    public Cotisation find(final Player player, final Club club, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);
        LOG.debug("for club = " + club);

        Cotisation cotisation = new Cotisation();
        // new 15-09-2021
        if (club.getIdclub().equals(1159)) {  // Whistling Straits Ryder Cup 2021 — accès non payant
            cotisation.setStatus("Y");
            return cotisation;
        }

        final String query = """
            SELECT *
            FROM payments_cotisation
            WHERE CotisationIdPlayer = ?
              AND CotisationIdClub = ?
              AND ? BETWEEN cotisationStartDate AND cotisationEndDate
              AND CotisationStatus = 'Y'
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, club.getIdclub());
            ps.setTimestamp(3, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int i = 0;
                RowMapper<Cotisation> cotisationMapper = new CotisationRowMapper();
                while (rs.next()) {
                    i++;
                    cotisation = cotisationMapper.map(rs);
                }
                LOG.debug(methodName + " - i = " + i);
                if (i == 0) {
                    String msg = LCUtil.prepareMessageBean("cotisation.notfound");
                    LOG.info(msg);
                    cotisation.setStatus("NF");
                } else {
                    LOG.debug(methodName + " - cotisation found");
                    cotisation.setStatus("Y");
                }
                return cotisation;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public List<Cotisation> findAll(final Player player, final Club club, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for player = " + player);
        LOG.debug("for round = " + round);
        LOG.debug("for club = " + club);

        try (Connection conn = dataSource.getConnection()) {

            String c = utils.DBMeta.listMetaColumnsLoad(conn, "payments_cotisation");
            final String query =
                " SELECT " + c
                + " FROM payments_cotisation"
                + " WHERE CotisationIdPlayer = ?"
                + "   AND DATE(?) >= cotisationStartDate"
                + "   AND DATE(?) <= cotisationEndDate"
                + "   AND CotisationStatus = 'Y'";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, player.getIdplayer());
                ps.setDate(2, java.sql.Date.valueOf(round.getRoundDate().toLocalDate()));
                ps.setDate(3, java.sql.Date.valueOf(round.getRoundDate().toLocalDate()));
                utils.LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    List<Cotisation> liste = new ArrayList<>();
                    RowMapper<Cotisation> cotisationMapper = new CotisationRowMapper();
                    while (rs.next()) {
                        liste.add(cotisationMapper.map(rs));
                    }
                    if (liste.isEmpty()) {
                        String msg = "£ Empty Result List in " + methodName;
                        LOG.error(msg);
                        LCUtil.showMessageFatal(msg);
                    } else {
                        LOG.debug(methodName + " - list size = " + liste.size());
                    }
                    return liste;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new FindCotisationAtRoundDate().find(..., conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #find(Player, Club, Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(324715);
        Round round = new Round();
        round.setIdround(699);
        Club club = new Club();
        club.setIdclub(1006);
        Cotisation cotisation = find(player, club, round);
        LOG.debug("cotisation found = " + cotisation);
    } // end main
    */

} // end class
