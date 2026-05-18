package rowmappers;

import entite.Audit;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class AuditRowMapper extends AbstractRowMapper<Audit> {

    @Override
    public Audit map(ResultSet rs) throws SQLException {
        try {
            Audit a = new Audit();
            a.setIdaudit(getInteger(rs, "AuditId"));
            a.setAuditPlayerId(getInteger(rs, "AuditPlayerId"));
            a.setAuditStartDate(getLocalDateTime(rs, "auditStartDate"));
            a.setAuditEndDate(getLocalDateTime(rs, "auditEndDate"));
            a.setPlayerName(getString(rs, "playerName")); // colonne JOIN optionnelle
            return a;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
