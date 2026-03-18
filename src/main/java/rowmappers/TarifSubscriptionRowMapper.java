package rowmappers;

import entite.TarifSubscription;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TarifSubscriptionRowMapper extends AbstractRowMapper<TarifSubscription> {

    @Override
    public TarifSubscription map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            var t = new TarifSubscription();

            t.setId(getInteger(rs, "TarifSubscriptionId"));
            t.setCode(getString(rs, "TarifSubscriptionCode"));
            t.setPrice(getDouble(rs, "TarifSubscriptionPrice"));
            t.setStartDate(getLocalDateTime(rs, "TarifSubscriptionStartDate"));
            t.setEndDate(getLocalDateTime(rs, "TarifSubscriptionEndDate"));
            t.setCreationDate(getLocalDateTime(rs, "TarifSubscriptionCreationDate"));

            return t;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
