package read;

import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;

@ApplicationScoped
public class ReadTee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    public Tee read(final Tee tee) throws SQLException, Exception {
        final String methodName = LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("for tee = {}", tee);

        try (Connection conn = dao.getConnection()) {

            final String query = """
                SELECT *
                FROM Tee
                WHERE idtee = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, tee.getIdtee());
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {
                    int i = 0;
                    Tee teef = new Tee();
                    RowMapper<Tee> teeMapper = new TeeRowMapper();
                    while(rs.next()){
                        i++;
                        teef = teeMapper.map(rs);
                    }  //end while

                    if(i == 0){
                        teef.setNotFound(true);
                        String msg = LCUtil.prepareMessageBean("distancetee.notfound") + " for tee = " + tee + " / " + tee.getTeeStart();
                        LOG.debug(msg);
                        LCUtil.showMessageFatal(msg);
                    }
                    if(i == 1){
                        LOG.info("distancetee.found = {} tee = {}", teef.getTeeDistanceTee(), teef.getTeeStart());
                    }

                    return teef;
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    }

} // end class