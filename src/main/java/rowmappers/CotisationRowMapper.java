package rowmappers;

import entite.Cotisation;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class CotisationRowMapper extends AbstractRowMapper<Cotisation> {

    @Override
    public Cotisation map(ResultSet rs) throws SQLException {
   //     final String methodName = utils.LCUtil.getCurrentMethodName();
     try {
        Cotisation c = new Cotisation();
        c.setIdclub(getInteger(rs,"CotisationIdClub"));
        c.setIdplayer(getInteger(rs,"CotisationIdPlayer"));
        c.setCotisationStartDate(getTimestamp(rs,"CotisationStartDate").toLocalDateTime());
        c.setCotisationEndDate(getTimestamp(rs,"CotisationEndDate").toLocalDateTime());
        c.setPaymentReference(getString(rs,"CotisationPaymentReference"));
        c.setCommunication(getString(rs,"CotisationCommunication"));
        c.setPrice(getDouble(rs,"CotisationAmount"));
        c.setItems(getString(rs,"CotisationItems"));
        c.setStatus(getString(rs,"CotisationStatus"));
        c.setPaymentDate(getTimestamp(rs,"CotisationModificationDate").toLocalDateTime());
        return c;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    }
} // end class