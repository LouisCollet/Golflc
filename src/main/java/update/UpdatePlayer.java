package update;

import entite.Player;
import static interfaces.Log.LOG;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import sql.SqlFactory;

import sql.preparedstatement.psCreateUpdatePlayer;
import utils.LCUtil;

@ApplicationScoped
public class UpdatePlayer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private dao.GenericDAO dao;

    /**
     * Mise à jour d’un Player
     */
    public boolean update(final Player player) throws Exception {

        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        try (Connection conn = dao.getConnection()) {
            conn.setAutoCommit(false);
            LOG.debug("AutoCommit set to false");
          // String query = utils.DBMeta.listMetaColumnsUpdate(conn, "player"); // enlève playerpassword
         //   final String query = new SqlFactory().generateQueryUpdate(conn, "player");  // le where est inclus !!
            final String co = new SqlFactory().listMetaColumnsUpdate(conn,"player"); // 
            //   String co = utils.DBMeta.listMetaColumnsUpdate(conn, "course");
            LOG.debug("String from listMetaColumns = {}", co);
          final String query = """
          UPDATE player
          SET %s
          WHERE player.idplayer=?;
         """.formatted(co) ;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                psCreateUpdatePlayer.mapUpdate(ps, player); //MapUpdate(ps, player);
                LCUtil.logps(ps);
                int row = ps.executeUpdate();
                if (row == 0) {
                    msg = "Fatal Error executeUpdate in " + methodName;
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    conn.rollback();
                    throw new SQLException(msg);
                }
            }

            conn.commit();
            msg = "Player updated successfully";
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return true;

        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = "SQLException in " + methodName + ": "
                    + sqle.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw sqle;

        } catch (Exception e) {
            msg = "Exception in " + methodName + ": "
                    + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }
}
