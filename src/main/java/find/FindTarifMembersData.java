package find;

import static interfaces.Log.LOG;
import entite.Club;
import entite.Round;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.LCUtil;

@ApplicationScoped
public class FindTarifMembersData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public FindTarifMembersData() { }

    public TarifMember find(final Club club, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - for club = " + club);
        LOG.debug(methodName + " - for round = " + round);

        final String query = """
            SELECT *
            FROM tarif_members
            WHERE tarif_members.TarifMemberIdClub = ?
            AND ? BETWEEN TarifMemberStartDate AND TarifMemberEndDate
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, club.getIdclub());
            ps.setTimestamp(2, Timestamp.valueOf(round.getRoundDate()));
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                TarifMember tarif = null;
                int i = 0;
                rowmappers.TarifMemberRowMapper mapper = new rowmappers.TarifMemberRowMapper();
                while (rs.next()) {
                    tarif = mapper.map(rs);
                    i++;
                }
                if (i == 0) {
                    String err = LCUtil.prepareMessageBean("tarif.notfound") + " findTarif "
                            + club.getClubName() + " / " + club.getIdclub();
                    LOG.error(err);
                    LCUtil.showMessageFatal(err);
                    return null;
                } else {
                    LOG.debug(methodName + " - tarif found, rows = " + i);
                }
                return tarif;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /** @deprecated use {@link #find(Club, Round)} via CDI injection */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Club club = new Club();
        club.setIdclub(101);
        Round round = new Round();
        round.setIdround(699);
        TarifMember tarifMember = find(club, round);
        LOG.debug("Tarif extracted from database = " + tarifMember);
    } // end main
    */

} // end class
