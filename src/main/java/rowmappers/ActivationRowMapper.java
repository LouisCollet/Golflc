package rowmappers;

import entite.Activation;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class ActivationRowMapper extends AbstractRowMapper<Activation> {

    @Override
    public Activation map(ResultSet rs) throws SQLException {
        try {
            Activation a = new Activation();
            a.setActivationKey(getString(rs, "ActivationKey"));
            a.setActivationPlayerId(getInteger(rs, "ActivationPlayerId"));
            a.setActivationLanguage(getString(rs, "ActivationPlayerLanguage"));
            a.setActivationCreationDate(getLocalDateTime(rs, "activationCreationDate"));
            return a;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
