package rowmappers;

import entite.Blocking;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import static utils.LCUtil.getCurrentMethodName;

public class BlockingRowMapper extends AbstractRowMapper<Blocking> {

    @Override
    public Blocking map(ResultSet rs) throws SQLException {
        try {
            Blocking b = new Blocking();
            b.setBlockingPlayerId(getInteger(rs, "BlockingPlayerId"));
            b.setBlockingLastAttempt(getLocalDateTime(rs, "BlockingLastAttempt"));
            b.setBlockingAttempts(getShort(rs, "BlockingAttempts"));
            b.setBlockingRetryTime(getLocalDateTime(rs, "BlockingRetryTime"));
            return b;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
