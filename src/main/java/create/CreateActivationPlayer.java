package create;

import Controllers.LanguageController;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import utils.LCUtil;
import static utils.LCUtil.printSQLException;

@ApplicationScoped
public class CreateActivationPlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    @Inject private mail.ActivationMail activationMail; // migrated 2026-02-26

    public CreateActivationPlayer() { }

    public boolean create(final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("-- Inserting initial Activation for new player = " + player.getIdplayer());

        try (Connection conn = dataSource.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "activation");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                String uuid = UUID.randomUUID().toString();
                LOG.debug("Universally Unique Identifier uuid = " + uuid);
                ps.setString(1, uuid); // ActivationKey
                ps.setInt(2, player.getIdplayer());
                ps.setString(3, player.getPlayerLanguage());
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    String href = utils.LCUtil.firstPartUrl()
                            + "/activation_check.xhtml?uuid="
                            + uuid
                            + "&firstname=" + player.getPlayerFirstName().replaceAll(" ", "%20")
                            + "&lastname=" + player.getPlayerLastName().replaceAll(" ", "%20")
                            + "&language=" + player.getPlayerLanguage();
                    LOG.debug("** href for activation new player = " + href);
                    LanguageController.setLanguage(player.getPlayerLanguage());
                    // new mail.ActivationMail().sendMailAccountCreated(player, href)
                    if (activationMail.sendMailAccountCreated(player, href)) { // migrated 2026-02-26
                        String msg = LCUtil.prepareMessageBean("create.registration.mail");
                        LOG.debug(msg);
                        utils.LCUtil.showMessageInfo(msg);
                        return true;
                    } else {
                        String msg = "ERROR mail not started";
                        LOG.error(msg);
                        utils.LCUtil.showMessageFatal(msg);
                        return false;
                    }
                } else {
                    String msg = "!! NOT NOT successful insert Activation : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException e) {
            printSQLException(e);
            handleSQLException(e, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Player player = new Player();
        player.setIdplayer(111111);
        player.setPlayerFirstName("first test activation");
        player.setPlayerLastName("last test activation");
        boolean b = new create.CreateActivationPlayer().create(player);
        LOG.debug("from main, CreateActivationPlayer = " + b);
    } // end main
    */

} // end class
