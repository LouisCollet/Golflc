package read;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import rowmappers.ClubRowMapper;
import utils.LCUtil;

/**
 * Service de lecture de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Pattern RowMapper conservé
 */
@ApplicationScoped
public class ReadClub implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Lit un Club par ID
     *
     * @param club Club avec l'ID à rechercher
     * @return Club complet
     * @throws Exception en cas d'erreur
     */
    public Club read(Club club) throws Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;

        // Validation
        if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
            msg = "Valid club ID is required";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        LOG.debug("Reading club with ID: {}", club.getIdclub());

        String query = """
            SELECT * FROM club
            WHERE club.idclub = ?
            """;

        return dao.querySingle(query, new ClubRowMapper(), club.getIdclub());
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Club club = new Club();
            club.setIdclub(101);

            LOG.debug("Main ready (CDI required for execution)");

        } catch (Exception e) {
            LOG.error("Exception in main: {}", e.getMessage(), e);
        }
    }
}
