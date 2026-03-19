package create;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class CreateDistances implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject
    private update.UpdateDistances updateDistances;

    public CreateDistances() { }

    public boolean create(final Distance distance) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with distance = " + distance);

        if (distance.getDistanceArray() == null) {
            LOG.debug("distancearray is null - skipped");
            return false;
        }

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "distances");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, distance.getIdTee());
                String json = new ObjectMapper().writeValueAsString(distance);
                LOG.debug("distances converted in json format = " + NEW_LINE + json);
                ps.setString(2, json);
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String msg = LCUtil.prepareMessageBean("distance.create") + distance;
                    var v = distance.getDistanceArray();
                    Arrays.stream(v).forEach(e -> LOG.debug(e + ","));
                    msg = msg + "<br>Vérification : total = " + Arrays.stream(v).sum();
                    msg = msg + " ,out = " + Arrays.stream(v, 0, 9).sum();
                    msg = msg + " ,in = " + Arrays.stream(v, 9, 18).sum();
                    LOG.debug(msg);
                    showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "-- ERROR update Distances : " + distance;
                    LOG.debug(msg);
                    showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equals("23000") && sqle.getErrorCode() == 1062) {
                LOG.info("distances already exists - going to update");
                boolean b = updateDistances.update(distance);
                LOG.debug("back from update = " + b);
                return true;
            }
            utils.LCUtil.printSQLException(sqle);
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Distance distance = new Distance();
        distance.setIdTee(218);
        int ar[] = {335,511,140,333,273,442,318,171,407,355,307,180,398,365,472,138,337,399};
        distance.setDistanceArray(ar);
        LOG.debug("array to insert json = " + Arrays.toString(distance.getDistanceArray()));
        boolean lp = new CreateDistances().create(distance);
        LOG.debug("from main, after lp = " + lp);
    } // end main
    */

} // end class
