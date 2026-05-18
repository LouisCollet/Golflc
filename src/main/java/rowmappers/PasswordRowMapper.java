package rowmappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Password;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import static utils.LCUtil.getCurrentMethodName;

public class PasswordRowMapper extends AbstractRowMapper<Password> {

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public Password map(ResultSet rs) throws SQLException {
        try {
            Password password = new Password();
            String s = rs.getString("PlayerPreviousPasswords");
            if (s != null) {
                password = OBJECT_MAPPER.readValue(s, Password.class);
                password.setPreviousPasswords(new ArrayList<>(Arrays.asList(password.getArrayPasswords())));
            }
            password.setPlayerPassword(rs.getString("PlayerPassword"));
            return password;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
