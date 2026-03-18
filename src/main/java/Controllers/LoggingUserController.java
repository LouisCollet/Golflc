package Controllers;

import entite.LoggingUser;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import utils.LCUtil;
import static utils.LCUtil.showMessageInfo;

/**
 * fix multi-user 2026-03-07 — replaced static String with ThreadLocal
 * Each user/request gets its own text buffer (no cross-user data leakage)
 */
@Named("loggingUserC")
@ApplicationScoped
public class LoggingUserController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private Controllers.MongoCalculationsController mongoCalculationsController;

    // fix multi-user 2026-03-07 — ThreadLocal instead of shared static String
    private static final ThreadLocal<StringBuilder> textBuffer =
            ThreadLocal.withInitial(() -> new StringBuilder("start text"));

    public LoggingUserController() { }

    public static void write(String text) {
        writeText("<p"
                + "style='fontsize:1.5em;color:black;>'"
                + text
                + "</p>");
    } // end method

    public static void write(String text, String param) {
        if (param.equalsIgnoreCase("b")) {
            text = "<b>" + text + "</b>";
        }
        if (param.equalsIgnoreCase("i")) {
            text = "<i>" + text + "</i>";
        }
        if (param.equalsIgnoreCase("u")) {
            text = "<u>" + text + "</u>";
        }
        if (param.equalsIgnoreCase("t")) {
            text = "<p>" + "<h1>" + "<b>" + text.toUpperCase() + "</b>" + "</h1>" + "</p>";
        }
        if (param.equalsIgnoreCase("c")) {
            text = "<h1>" + "<p style='color:red;'>" + "<b>" + text.toUpperCase() + "</b>" + "</h1>" + "</p>";
        }
        writeText("<br/>" + text);
    } // end method

    public static void writeText(String newText) {
        textBuffer.get().append(newText);
    } // end method

    public boolean createUpdateLoggingUser(LoggingUser logging) {
        try {
            boolean b = mongoCalculationsController.create(logging);
            return false;
        } catch (Exception e) {
            String msg = "exception in read !!" + e + "No calculations available !";
            LOG.info(msg);
            write(msg);
            showMessageInfo(msg);
            return false;
        }
    } // end method

    public static String getText() {
        return textBuffer.get().toString();
    } // end method

    public static void setText(String text) {
        textBuffer.get().setLength(0);
        textBuffer.get().append(text);
    } // end method

    /**
     * Call at the end of each request to prevent ThreadLocal memory leak
     */
    public static void clearText() {
        textBuffer.remove();
    } // end method

    /*
    void main() throws Exception {
        LOG.debug("line 0");
        LOG.debug("line 1");
    } // end main
    */

} // end class
