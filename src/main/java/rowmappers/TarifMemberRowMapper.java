package rowmappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.TarifMember;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TarifMemberRowMapper extends AbstractRowMapper<TarifMember> {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public TarifMember map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        try {
            String json = getString(rs, "TarifMemberJson");
            if (json == null || json.isBlank()) {
                LOG.warn(methodName + " - TarifMemberJson is null or blank");
                return null;
            }
            TarifMember t = OBJECT_MAPPER.readValue(json, TarifMember.class);
            if (hasColumn(rs, "TarifMemberStartDate")) t.setStartDate(getLocalDateTime(rs, "TarifMemberStartDate"));
            if (hasColumn(rs, "TarifMemberEndDate"))   t.setEndDate(getLocalDateTime(rs, "TarifMemberEndDate"));
            if (hasColumn(rs, "TarifMemberIdClub"))    t.setTarifMemberIdClub(getInteger(rs, "TarifMemberIdClub"));
            return t;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
