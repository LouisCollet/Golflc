package lists;

import entite.LoggingUser;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

@Named
@ApplicationScoped
public class LogginUserList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    private List<LoggingUser> liste = null;

    public LogginUserList() { }

    public List<LoggingUser> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        if (liste != null) {
            LOG.debug(methodName + " - returning cached list size = " + liste.size());
            return liste;
        }

        final String query = """
                SELECT *
                FROM logging_user
                """;

        liste = dao.queryList(query, rs -> LoggingUser.map(rs));
        return liste;
    } // end method

    public List<LoggingUser> getListe()                       { return liste; }
    public void              setListe(List<LoggingUser> liste) { this.liste = liste; }

    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        List<LoggingUser> lp = new LogginUserList().list();
        LOG.debug("from main, after lp = " + lp);
        LOG.debug("nombre de logging users dans la liste = " + lp.size());
    } // end main
    */

} // end class
