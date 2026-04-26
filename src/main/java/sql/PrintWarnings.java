
package sql;
import static interfaces.Log.LOG;
import java.sql.SQLWarning;

public final class PrintWarnings {
    private PrintWarnings() {
        // empêche l'instanciation
    }
    public static void print(SQLWarning warning, String context) {
        while (warning != null) {
            LOG.debug("warnings for context = " + context);
            LOG.debug("SQLWarning:");
            LOG.debug("Message  : " + warning.getMessage());
            LOG.debug("SQLState : " + warning.getSQLState());
            LOG.debug("Code     : " + warning.getErrorCode());
            warning = warning.getNextWarning();
        }
    }
}