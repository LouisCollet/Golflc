package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.sql.SQLException;
import javax.sql.DataSource;
import utils.LCUtil;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class UpdateClubStructure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public UpdateClubStructure() { }

    public void update(Club club, UnavailableStructure structure) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
                UPDATE club
                SET GroundCondition = ?
                WHERE idclub = ?
                """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(query)) {
            String json = OBJECT_MAPPER.writeValueAsString(structure);
            LOG.debug("json = {}", json);
            ps.setString(1, json);
            ps.setInt(2, club.getIdclub());
            utils.LCUtil.logps(ps);
            int rows = ps.executeUpdate();
            String msg = String.format(
               "rows updated = %d, structure json = %s",
               rows,
               json
            );
            LOG.debug(msg);
            showMessageInfo(msg);

        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

} // end class
