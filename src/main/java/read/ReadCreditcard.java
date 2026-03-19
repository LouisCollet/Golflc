package read;

import entite.Creditcard;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import rowmappers.CreditcardRowMapper;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageInfo;

@ApplicationScoped
public class ReadCreditcard implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public ReadCreditcard() { }

    public Creditcard read(final Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - for idplayer = " + player.getIdplayer());

        final String query = """
                SELECT *
                FROM creditcard
                WHERE CreditcardIdPlayer = ?
                """;

        Creditcard creditcard = dao.querySingle(query, new CreditcardRowMapper(), player.getIdplayer());
        if (creditcard == null) {
            creditcard = new Creditcard();
        }
        if (creditcard.getCreditcardNumber() == null) {
            String msg = prepareMessageBean("creditcard.notfound");
            LOG.debug(methodName + " - " + msg);
            showMessageInfo(msg);
        } else {
            String msg = utils.LCUtil.prepareMessageBean("creditcard.found") + creditcard;
            LOG.debug(methodName + " - " + msg);
        }
        return creditcard;
    } // end method

    /*
    void main() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // nécessite contexte CDI — DataSource injecté par WildFly
        // Player player = new Player(); player.setIdplayer(324733);
        // Creditcard cc = new ReadCreditcard().read(player);
        // LOG.debug("creditcard found = " + cc.toString());
    } // end main
    */

} // end class
