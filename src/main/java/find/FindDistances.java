package find;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Distance;
import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

/**
 * Service pour trouver les distances d'un tee
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ Migré GenericDAO — 2026-03-18
 * ✅ Basé sur la table 'distances' avec champ JSON
 *
 * Structure de la table distances :
 * - DistanceIdTee INT (FK vers tee.TeeDistanceTee)
 * - DistanceArray JSON (tableau des 18 distances)
 *
 * IMPORTANT : Cherche par tee.getTeeDistanceTee() (pas tee.getIdtee())
 *
 * @author GolfLC
 * @version 5.0 - Migration GenericDAO
 */
@ApplicationScoped
public class FindDistances implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * ObjectMapper Jackson (réutilisable)
     * Configuré avec INDENT_OUTPUT pour le debug
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public FindDistances() {} // end constructor

    /**
     * Trouve les distances pour un tee
     *
     * Cherche dans la table 'distances' par tee.getTeeDistanceTee()
     * Le champ DistanceArray est au format JSON et doit être désérialisé
     *
     * Si aucune distance n'est trouvée, retourne un Distance avec tableau de zéros
     *
     * @param tee Le tee dont on veut les distances
     * @return Distance avec le tableau int[18] (ou zéros si non trouvé)
     */
    public Distance find(final Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(methodName + " - for tee = " + tee);

        // Validation
        if (tee == null) {
            LOG.error(methodName + " - Tee cannot be null");
            throw new IllegalArgumentException("Tee cannot be null");
        }

        // ⚠️ IMPORTANT : On cherche par TeeDistanceTee (pas idtee)
        if (tee.getTeeDistanceTee() == null) {
            LOG.warn(methodName + " - TeeDistanceTee is null for tee " + tee.getIdtee() + ", returning zeros");
            return createZeroDistance();
        }

        final String query = """
                SELECT distances.DistanceArray
                FROM distances
                WHERE distances.DistanceIdTee = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // ⚠️ On utilise getTeeDistanceTee() (pas getIdtee())
            ps.setInt(1, tee.getTeeDistanceTee());
            LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                int rowCount = 0;
                String json = null;

                while (rs.next()) {
                    rowCount++;
                    json = rs.getString("DistanceArray");
                }

                // Cas 1 : Aucune distance trouvée
                if (rowCount == 0) {
                    String msg = LCUtil.prepareMessageBean("distances.notfound") + "<br>" + tee;
                    LOG.debug(methodName + " - " + msg);
                    LCUtil.showMessageInfo(msg);
                    return createZeroDistance();
                }

                // Cas 2 : Distances trouvées (JSON)
                LOG.debug(methodName + " - ResultSet has " + rowCount + " line(s)");
                LOG.debug(methodName + " - Distance format json = " + json);

                Distance distance = objectMapper.readValue(json, Distance.class);
                LOG.debug(methodName + " - Distance extracted from database = " + distance);
                return distance;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return createZeroDistance();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return createZeroDistance();
        }
    } // end method

    /**
     * Crée un Distance avec un tableau de zéros
     * Utilisé quand aucune distance n'est trouvée en base
     */
    private Distance createZeroDistance() {
        Distance distance = new Distance();
        int[] array = new int[18];
        Arrays.fill(array, 0);
        distance.setDistanceArray(array);
        LOG.debug("Created zero distance array");
        return distance;
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class
