package rowmappers;

import entite.Greenfee;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class GreenfeeRowMapper extends AbstractRowMapper<Greenfee> {

    @Override
    public Greenfee map(ResultSet rs) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        try {
            var greenfee = new Greenfee();

            greenfee.setIdclub(getInteger(rs,"GreenfeeIdClub"));
            greenfee.setIdplayer(getInteger(rs,"GreenfeeIdPlayer"));
            greenfee.setIdround(rs.getInt("GreenfeeIdRound"));

            // Conversion Timestamp → LocalDateTime avec helper
            greenfee.setRoundDate(getLocalDateTime(rs, "GreenfeeRoundDate"));
            greenfee.setPaymentDate(getLocalDateTime(rs, "GreenfeeModificationDate"));

            // Helper String pour éviter null
            greenfee.setPaymentReference(getString(rs, "GreenfeePaymentReference"));
            greenfee.setCommunication(getString(rs, "GreenfeeCommunication"));
            greenfee.setItems(getString(rs, "GreenfeeItems"));
            greenfee.setPrice(getDouble(rs, "GreenfeeAmount"));
            greenfee.setStatus(getString(rs, "GreenfeeStatus"));
            greenfee.setCurrency(getString(rs, "GreenfeeCurrency")); // currency 28-04-2025

            return greenfee;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
}
