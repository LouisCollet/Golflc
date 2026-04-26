package rowmappers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifGreenfee;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps a tarif_greenfee ResultSet row to a TarifGreenfee entity.
 * Mandatory column : TarifJson (Jackson deserialization).
 * Optional columns : TarifCourseId, TarifYear, TarifCurrencyCode — set when present in the ResultSet.
 */
public class TarifGreenfeeRowMapper extends AbstractRowMapper<TarifGreenfee> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // backward compat with old JSON
    }

    @Override
    public TarifGreenfee map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            String json = getString(rs, "TarifJson");
            if (json == null || json.isBlank()) {
                LOG.warn(methodName + " - TarifJson is null or blank");
                return null;
            }

            TarifGreenfee t = OBJECT_MAPPER.readValue(json, TarifGreenfee.class);

            if (hasColumn(rs, "TarifId")) {
                t.setTarifId(getInteger(rs, "TarifId"));
            }
            if (hasColumn(rs, "TarifYear")) {
                t.setTarifYear(getInteger(rs, "TarifYear"));
            }
            if (hasColumn(rs, "TarifCurrency")) {
                t.setCurrency(getString(rs, "TarifCurrency"));
            }
            if (hasColumn(rs, "TarifCourseId")) {
                Integer courseId = getInteger(rs, "TarifCourseId");
                t.setTarifCourseId(courseId != null ? new ArrayList<>(List.of(courseId)) : new ArrayList<>());
            }
            if (hasColumn(rs, "TarifHoles")) {
                Integer h = getInteger(rs, "TarifHoles");
                t.setTarifHoles(h != null ? h : 18);
            }
            if (hasColumn(rs, "TarifCurrencyCode")) {
                t.setCurrency(getString(rs, "TarifCurrencyCode"));
            }

            return t;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
