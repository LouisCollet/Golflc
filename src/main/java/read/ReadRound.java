package read;

import entite.Round;
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
import rowmappers.RoundRowMapper;
import rowmappers.RowMapperRound;

@ApplicationScoped
public class ReadRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadRound() { }

    public Round read(Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (round == null || round.getIdround() == null) {
            throw new IllegalArgumentException("Round.idround must not be null");
        }

        final String query = """
            SELECT *
            FROM Round
            WHERE idround = ?
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                RowMapperRound<Round> roundMapper = new RoundRowMapper();
                if (rs.next()) {
                    Round mapped = roundMapper.map(rs, null);
                    mapped.setIdround(round.getIdround()); // garantie
                    return mapped;
                }
                return null;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ===========================================================================================
    // BRIDGE — @Deprecated — pour les appelants legacy (new ReadRound().read(round, conn))
    // À supprimer quand tous les appelants seront migrés en CDI
    // ===========================================================================================
    /** @deprecated Utiliser {@link #read(Round)} via injection CDI */
    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Round round = new Round();
        round.setIdround(630);
        round = read(round);
        LOG.debug("loaded round = " + round);
    } // end main
    */

} // end class
