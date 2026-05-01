
package sql;
import static interfaces.Log.LOG;
import java.sql.SQLWarning;

public final class PrintWarnings {
    private PrintWarnings() { }

    public static void print(SQLWarning warning, String context) {
        if (warning == null) return;
        LOG.debug("SQL warnings for context = {}", context);
        while (warning != null) {
            LOG.debug("SQLWarning: message={}, SQLState={}, code={}",
                    warning.getMessage(), warning.getSQLState(), warning.getErrorCode());
            warning = warning.getNextWarning();
        }
    }
} // end class