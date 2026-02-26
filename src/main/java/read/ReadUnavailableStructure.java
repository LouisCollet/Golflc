package read;

import entite.Club;
import entite.UnavailableStructure;
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
import javax.sql.DataSource;
import rowmappers.RowMapper;
import rowmappers.UnavailableStructureRowMapper;

@ApplicationScoped
public class ReadUnavailableStructure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    public ReadUnavailableStructure() { }

    public UnavailableStructure read(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for club = " + club);

        final String query = """
                SELECT *
                FROM club
                WHERE idclub = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, club.getIdclub());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                UnavailableStructure structure = new UnavailableStructure();
                RowMapper<UnavailableStructure> structureMapper = new UnavailableStructureRowMapper();
                while (rs.next()) {
                    structure = structureMapper.map(rs);
                }
                if (structure == null) {
                    String msg = "No Structure found for club = " + club.getIdclub();
                    LOG.error(methodName + " - " + msg);
                    utils.LCUtil.showMessageFatal(msg);
                    return null;
                }
                LOG.debug(methodName + " - found structure, items = " + structure.getStructureList().size());
                return structure;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Club club = new Club(); club.setIdclub(101);
        // LOG.debug("UnavailableStructure = " + new ReadUnavailableStructure().read(club));
    } // end main
    */

} // end class
