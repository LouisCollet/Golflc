package create;

import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import jakarta.inject.Inject;
import utils.LCUtil;

@ApplicationScoped
public class CreateActivationPassword implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    @Inject private mail.ResetPasswordMail resetPasswordMail;  // migrated 2026-02-26

    public CreateActivationPassword() { }

    public boolean create(final Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try (Connection conn = dao.getConnection()) {
            final String query = LCUtil.generateInsertQuery(conn, "activation");
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                String uuid = UUID.randomUUID().toString();
                ps.setString(1, uuid); // ActivationKey
                ps.setInt(2, player.getIdplayer());
                ps.setString(3, player.getPlayerLanguage());
                ps.setTimestamp(4, Timestamp.from(Instant.now()));
                utils.LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row != 0) {
                    LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(10);
                    Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    LOG.debug("date plus 10 = " + date);
                    String href = utils.LCUtil.firstPartUrl() + "/password_check.xhtml"
                            + "?uuid=" + uuid
                            + "&firstname=" + player.getPlayerFirstName()
                            + "&lastname=" + player.getPlayerLastName()
                            + "&language=" + player.getPlayerLanguage()
                            + "&time=" + date
                            + "&millis=" + date.getTime();
                    href = href.replaceAll(" ", "%20");
                    LOG.debug("** href for activation password = " + href);
                    // new mail.ResetPasswordMail().send(...)
                    if (resetPasswordMail.send(player, href)) { // migrated 2026-02-26
                        String msg = LCUtil.prepareMessageBean("create.reset.mail");
                        LOG.debug(msg);
                        utils.LCUtil.showMessageInfo(msg);
                    }
                    String msg = "!! successful insert Activation for Password : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.debug(msg);
                    return true;
                } else {
                    String msg = "!! NOT NOT successful insert Activation for Password : "
                            + " <br/>ID = " + player.getIdplayer()
                            + " <br/>first = " + player.getPlayerFirstName()
                            + " <br/>last = " + player.getPlayerLastName();
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }
        } catch (SQLException e) {
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
        player.setIdplayer(324713);
        player.setPlayerFirstName("Jon");
        player.setPlayerLastName("Rahm");
        boolean b = new create.CreateActivationPassword().create(player);
        LOG.debug("from main, CreateActivationPassword = " + b);
    } // end main
    */

} // end class
