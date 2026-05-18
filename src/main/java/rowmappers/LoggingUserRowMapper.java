package rowmappers;

import entite.LoggingUser;
import static exceptions.LCException.handleGenericException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import static utils.LCUtil.getCurrentMethodName;

public class LoggingUserRowMapper extends AbstractRowMapper<LoggingUser> {

    @Override
    public LoggingUser map(ResultSet rs) throws SQLException {
        try {
            LoggingUser logging = new LoggingUser();
            logging.setLoggingIdPlayer(getInteger(rs, "LoggingIdPlayer"));
            logging.setLoggingIdRound(getInteger(rs, "LoggingIdRound"));
            logging.setLoggingType(getString(rs, "LoggingType"));
            logging.setLoggingCalculations(getString(rs, "LoggingCalculations"));
            logging.setLoggingModificationDate(LocalDateTime.now());
            return logging;
        } catch (Exception e) {
            handleGenericException(e, getCurrentMethodName());
            return null;
        }
    } // end map

} // end class
