package update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.composite.EPlayerPassword;
import entite.Password;
import entite.Player;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import manager.PlayerManager;
import utils.LCUtil;

/**
 * Service de mise à jour du mot de passe joueur
 * ✅ @ApplicationScoped - Stateless, partagé
 */
@ApplicationScoped
public class UpdatePassword implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Inject
    private dao.GenericDAO dao;

    @Inject
    private PlayerManager playerManager;

    public UpdatePassword() { }

    public boolean update(final EPlayerPassword epp) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(" with epp = {}", epp);

        Player player = epp.player();
        Password password = epp.password();

        if (password == null) {
            LOG.debug("entite Password = null {}", password);
        }

        LOG.debug("entite Password = {}", password);
        LOG.debug("Previous Passwords = {}", password.getPreviousPasswords());

        if (password.getWrkpassword() == null) {
            String err = "wrkpassword is null !!";
            LOG.debug(err);
        }

        if (password.getWrkpassword().equals("RESET PASSWORD")) {
            LOG.debug("case reset password");
        } else {
            if (!password.getWrkpassword().equals(password.getWrkconfirmpassword())) {
                String err = LCUtil.prepareMessageBean("player.password.notmatch") + " : " + password.getWrkpassword()
                        + " / " + password.getWrkconfirmpassword();
                LOG.error(err);
                LCUtil.showMessageFatal(err);
                return false;
            } else {
                LOG.debug("we continue because password.getWrkpassword().equals(password.getWrkconfirmpassword() !! ");
            }
        }

        LOG.debug("control : getPreviousPasswords = {}", password.getPreviousPasswords().toString());
        List<String> listPreviousPasswords = password.getPreviousPasswords();
        LOG.debug("list previousPasswords = {}", listPreviousPasswords);

        if (listPreviousPasswords.contains(password.getWrkpassword())) {
            String err = LCUtil.prepareMessageBean("player.password.reuse") + " : "
                    + password.getWrkpassword() + " / " + password.getPreviousPasswords().toString();
            LOG.error(err);
            LCUtil.showMessageFatal(err);
            return false;
        } else {
            LOG.debug("ce password n'a jamais été utilisé");
        }

        if (password.getCurrentPassword() != null) {
            listPreviousPasswords.add(password.getCurrentPassword());
            LOG.debug("added to listPreviousPasswords = {}", password.getCurrentPassword());
        } else {
            listPreviousPasswords.add(password.getWrkpassword());
            LOG.debug("added to listPreviousPasswords = {}", password.getWrkpassword());
        }

        LOG.debug("list avec old password ajouté = {}", listPreviousPasswords);
        password.setPreviousPasswords(listPreviousPasswords);

        String[] array = listPreviousPasswords.toArray(String[]::new);
        LOG.debug("array = {}", Arrays.toString(array));
        password.setArrayPasswords(array);

        String json = OBJECT_MAPPER.writeValueAsString(password);
        LOG.debug("Previouspasswords converted in json format= {}", json);

        final String query = """
                UPDATE Player
                SET player.PlayerPassword = SHA2(?,256),
                    player.PlayerPreviousPasswords = ?
                WHERE player.idplayer = ?
                """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (password.getWrkpassword().equals("RESET PASSWORD")) {
                ps.setNull(1, java.sql.Types.CHAR);
            } else {
                ps.setString(1, password.getWrkpassword());
            }
            ps.setString(2, json);
            ps.setInt(3, player.getIdplayer());
            utils.LCUtil.logps(ps);

            int row = ps.executeUpdate();
            LOG.debug("row = {}", row);
            if (row == 1) {
                LOG.debug("PlayerPassword created or modified");
                String msg = LCUtil.prepareMessageBean("player.password.modified")
                        + " <br/>ID = " + player.getIdplayer()
                        + " <br/>password = " + password.getWrkpassword();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "NOT NOT successful modify Player row = 0 !!! ";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
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
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Player player = new Player();
        player.setIdplayer(324720);
        Password password = null;
        EPlayerPassword epp = new EPlayerPassword(player, password);
        epp = playerManager.readPlayerWithPassword(epp.getPlayer().getIdplayer());
        Password pa = epp.password();
        LOG.debug("previousPasswords liste 01 = {}", pa.getPreviousPasswords());
        pa.setWrkpassword("****");
        pa.setWrkconfirmpassword("****");
        LOG.debug("liste 02 = {}", pa.getPreviousPasswords());
        boolean b = new UpdatePassword().update(epp);
        LOG.debug("from main, result = {}", b);
    } // end main
    */

} // end class
