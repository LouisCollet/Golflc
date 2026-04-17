package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Distance;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import utils.LCUtil;

/**
 * Service de mise à jour des distances (JSON column)
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdateDistances implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Inject
    private dao.GenericDAO dao;

    public UpdateDistances() { }

    public boolean update(Distance distance) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(" with distances = {}", distance);

        try (Connection conn = dao.getConnection()) {

            String distances = utils.DBMeta.listMetaColumnsUpdate(conn, "distances");
            final String query = """
                    UPDATE distances
                    SET %s
                    WHERE DistanceIdTee = ?
                    """.formatted(distances);

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                String json = OBJECT_MAPPER.writeValueAsString(distance);
                LOG.debug("distances converted in json format = {}", NEW_LINE + json);
                ps.setString(1, json);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setInt(3, distance.getIdTee());
                utils.LCUtil.logps(ps);

                int row = ps.executeUpdate();
                if (row != 0) {
                    LOG.debug("Successfull updateDistances : {}", distance);
                    var v = distance.getDistanceArray();
                    Arrays.stream(v).forEach(e -> LOG.debug("{},", e));
                    String msg = LCUtil.prepareMessageBean("distance.update") + distance;
                    msg = msg + "<br>Vérification : total = " + Arrays.stream(v).sum();
                    msg = msg + " ,out = " + Arrays.stream(v, 0, 9).sum();
                    msg = msg + " ,in = " + Arrays.stream(v, 9, 18).sum();
                    LOG.debug(msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "FATAL ERROR updateDistances : " + distance;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new Exception("Exception in " + methodName + ": update failed for distance idTee=" + distance.getIdTee());
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Distance distance = new Distance();
        distance.setIdTee(2);
        int ar[] = {200,210,220,333,273,442,318,171,407,355,307,180,398,365,472,138,337,399};
        distance.setDistanceArray(ar);
        boolean lp = new UpdateDistances().update(distance);
        LOG.debug("from main, after lp = {}", lp);
    } // end main
    */

} // end class
