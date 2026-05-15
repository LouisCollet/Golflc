package read;

import entite.Club;
import entite.UnavailableStructure;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import rowmappers.UnavailableStructureRowMapper;

@ApplicationScoped
public class ReadUnavailableStructure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadUnavailableStructure() { }

    public UnavailableStructure read(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for club = {}", club);

        final String query = """
                SELECT *
                FROM club
                WHERE idclub = ?
                """;

        UnavailableStructure structure = dao.querySingle(query, new UnavailableStructureRowMapper(), club.getIdclub());
        if (structure == null) {
            LOG.error("No Structure found for club = {}", club.getIdclub());
            utils.LCUtil.showMessageFatal("No Structure found for club = " + club.getIdclub());
            return null;
        }
        LOG.debug("found structure, items size = {}", structure.getStructureList().size());
        return structure;
    } // end method

    // Variante silencieuse — pas de showMessageFatal, utilisée dans les checks automatiques
    public UnavailableStructure readSilent(final Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                SELECT *
                FROM club
                WHERE idclub = ?
                """;

        UnavailableStructure structure = dao.querySingle(query, new UnavailableStructureRowMapper(), club.getIdclub());
        LOG.debug("structure = {}", structure != null ? structure.getStructureList().size() + " items" : "null");
        return structure;
    } // end method

} // end class
