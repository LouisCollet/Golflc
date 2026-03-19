package delete;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import java.text.SimpleDateFormat;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import utils.LCUtil;

@ApplicationScoped
public class DeleteHandicap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public DeleteHandicap() { }

    public String delete(final int idplayer, final Date date) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("Delete Handicap for idplayer = " + idplayer);
        LOG.debug("Delete Handicap for date     = " + new SimpleDateFormat("dd/MM/yyyy").format(date));

        final String deleteQuery = """
                DELETE from handicap
                WHERE handicap.player_idplayer = ?
                  and handicap.idhandicap = ?
                """;
        final String updateQuery = """
                UPDATE handicap
                SET handicap.HandicapEnd = '2099-12-31'
                WHERE handicap.player_idplayer = ?
                  and handicap.idhandicap < ?
                ORDER BY idhandicap DESC LIMIT 1
                """;

        try (Connection conn = dao.getConnection()) {

            int rowDelete = 0;
            int rowUpdate = 0;

            try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
                ps.setInt(1, idplayer);
                ps.setDate(2, LCUtil.getSqlDate(date));
                LCUtil.logps(ps);
                rowDelete = ps.executeUpdate();
                LOG.debug(methodName + " - deleted Handicap = " + rowDelete);
            }

            if (rowDelete > 0) {
                try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                    ps.setInt(1, idplayer);
                    ps.setDate(2, LCUtil.getSqlDate(date));
                    LCUtil.logps(ps);
                    rowUpdate = ps.executeUpdate();
                    LOG.debug(methodName + " - Updated HandicapEnd = " + rowUpdate);
                }
            }

            String msg = "<br/> <h1>Handicap deleted = "
                    + " <br/></h1>player = " + idplayer
                    + " <br/>date = " + new SimpleDateFormat("dd/MM/yyyy").format(date)
                    + " <br/>deleted  = " + rowDelete
                    + " <br/>updated = " + rowUpdate;
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
            return "Player deleted ! ";

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /** @deprecated Use {@link #delete(int, Date)} instead — migrated 2026-02-24 */
    /*
    void main() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        int idplayer = 2014102;
        Date date = SDF.parse("01/01/2000");
        String b = new DeleteHandicap().delete(idplayer, date);
        LOG.debug("from main - resultat deleteHandicap = " + b);
    } // end main
    */

} // end class
