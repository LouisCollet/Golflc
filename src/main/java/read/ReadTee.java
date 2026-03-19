package read;

import entite.Tee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import utils.LCUtil;

/**
 * Service de lecture de Tee
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Inject GenericDAO - Connection pooling
 * ✅ Pattern RowMapper conservé
 */
@ApplicationScoped
public class ReadTee implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Lit un Tee par ID
     *
     * @param tee Tee avec l'ID à rechercher
     * @return Tee complet (avec flag notFound si non trouvé)
     * @throws SQLException en cas d'erreur SQL
     * @throws Exception en cas d'autre erreur
     */
    public Tee read(final Tee tee) throws SQLException, Exception {

        final String methodName = LCUtil.getCurrentMethodName();

        try (Connection conn = dao.getConnection()) {

            LOG.debug("entering ReadTee.read");
            LOG.debug(" for tee = " + tee);

            final String query = """
                SELECT *
                FROM Tee
                WHERE idtee = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, tee.getIdtee()); // where
                LCUtil.logps(ps);

                try (ResultSet rs = ps.executeQuery()) {

                    // ✅ PARTIE NON MODIFIÉE (comme demandé) - DÉBUT
                    int i = 0;
                    int t = 0;
                    Tee teef = new Tee();
                    RowMapper<Tee> teeMapper = new TeeRowMapper();
                    while(rs.next()){
                        i++;
                        //  teef = teeMapper.map(rs);
                        teef = teeMapper.map(rs);
                    }  //end while

                    if(i == 0){
                        teef.setNotFound(true);
                        String msg = LCUtil.prepareMessageBean("distancetee.notfound") + " for tee = " + tee + " / " + tee.getTeeStart();
                        LOG.debug(msg);
                        LCUtil.showMessageFatal(msg);
                    }
                    if(i == 1){
                        String msg = "distancetee.found" + " = " + teef.getTeeDistanceTee() + " tee = " + teef.getTeeStart();
                        LOG.info(msg);
                    }
                    // ✅ PARTIE NON MODIFIÉE - FIN

                    return teef;
                }
            }

        } catch (SQLException e) {
            String msg = "SQLException in " + methodName + ": " + e.getMessage()
                + ", SQLState = " + e.getSQLState()
                + ", ErrorCode = " + e.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;

        } catch (Exception e) {
            String msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw e;
        }
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Tee tee = new Tee();
            tee.setIdtee(3000); // existe pas !!

            LOG.debug("Main ready (CDI required for execution)");
            LOG.debug("Test tee ID: {}", tee.getIdtee());

        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}
/*
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import rowmappers.RowMapper;
import rowmappers.TeeRowMapper;
import connection_package.DBConnection;
import utils.LCUtil;

public class ReadTee{

public Tee read(final Tee tee,Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering ReadTee.read");
        LOG.debug(" for tee = " + tee);
     final String query = """
        SELECT *
        FROM Tee
        WHERE idtee = ?
      """;
   //     LOG.debug("Tee loaded = " + tee.getIdtee());
     ps = conn.prepareStatement(query);
     ps.setInt(1, tee.getIdtee()); // where
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     int i = 0;
     int t = 0;
     Tee teef = new Tee();
     RowMapper<Tee> teeMapper = new TeeRowMapper();
     while(rs.next()){
           i++;
         //  teef = teeMapper.map(rs);
           teef = teeMapper.map(rs);
      }  //end while

     if(i == 0){
            teef.setNotFound(true);
            String msg = LCUtil.prepareMessageBean("distancetee.notfound") + " for tee = " + tee + " / " + tee.getTeeStart();
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
     }
     if(i == 1){
          //  String msg = LCUtil.prepareMessageBean("distancetee.found") + " = " + distanceTee + " for course = " + course.getIdcourse() + " / " + tee.getTeeStart();
                 //   player.getPlayerLastName() + " / " + player.getIdplayer()
                 //   + " for round : " + round.getRoundName();
            String msg = "distancetee.found" + " = " + teef.getTeeDistanceTee() + " tee = " + teef.getTeeStart();
            LOG.info(msg);
        //    LCUtil.showMessageInfo(msg);
        }
    return teef;
}catch (SQLException e){
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{ // testing purposes
   Connection conn = new DBConnection().getConnection(); // main
   Tee tee = new Tee();
   tee.setIdtee(3000); // existe pas !!
   Tee t = new ReadTee().read(tee,conn);
   if(t.isNotFound()){
       LOG.debug(" Tee not found ! = " + t.toString());
   }else{
       LOG.debug(" loaded tee = " + t.toString());
   }

   DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
*/