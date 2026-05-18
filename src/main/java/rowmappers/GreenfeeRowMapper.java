package rowmappers;

import entite.Greenfee;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            String cur = getString(rs, "GreenfeeCurrency");
            greenfee.setCurrency(cur != null ? cur.toUpperCase(java.util.Locale.ROOT) : "EUR"); // currency 28-04-2025

            return greenfee;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }
} //end class
