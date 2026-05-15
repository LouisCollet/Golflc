package read;

import entite.HolesGlobal;
import entite.Tee;
import find.FindDistances;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import utils.LCUtil;

@ApplicationScoped
public class ReadHole implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * FindDistances injecté par CDI
     */
    @Inject
    private FindDistances findDistances;

    public HolesGlobal read(Tee tee) throws SQLException {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with tee = {}", tee);

        HolesGlobal holesGlobal = new HolesGlobal();

        try (Connection conn = dao.getConnection()) {

            String query = """
                SELECT *
                FROM hole, tee
                WHERE tee.idtee = ?
                    AND hole.tee_idtee = tee.TeeMasterTee
                ORDER by holenumber
                """;
            //AND hole.tee_idtee = tee.idtee // mod 09-08-2023 pour 01-09 et 10-18

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, tee.getIdtee());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    // ✅ PARTIE NON MODIFIÉE (comme demandé) - DÉBUT
                    int i = 0;
                    var v = findDistances.find(tee).getDistanceArray();
                    LOG.debug("line 00");
                    if(v == null){
                        LOG.debug("array distance = null, filled with 0");
                        v = new int[18];
                    }
                    LOG.debug("array distance = {}", Arrays.toString(v));
                    while(rs.next()){
                        holesGlobal.getDataHoles()[i][0] = (rs.getInt("HoleNumber") );
                        holesGlobal.getDataHoles()[i][1] = (rs.getInt("HolePar") );
                        holesGlobal.getDataHoles()[i][2] = (rs.getInt("HoleStrokeIndex"));
                        holesGlobal.getDataHoles()[i][3] = v[i];
                        i++;
                    } // end while
                    LOG.debug("there are rows = {}", i);
                    // ✅ PARTIE NON MODIFIÉE - FIN

                    return holesGlobal;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new HolesGlobal();
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return new HolesGlobal();
        }
    }

} // end class