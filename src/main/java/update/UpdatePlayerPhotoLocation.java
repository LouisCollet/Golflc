package update;

import entite.Player;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import utils.LCUtil;
import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;

/**
 * ✅ CDI migration — Connection remplacée par DataSource
 * ✅ try-with-resources — plus de finally/closeQuietly
 * ✅ @ApplicationScoped + implements Serializable
 */
@Named
@ApplicationScoped
public class UpdatePlayerPhotoLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    // ✅ DataSource injecté — plus de Connection en paramètre
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Met à jour la photo location d'un joueur
     * ✅ Connection supprimée de la signature
     */
    public boolean updateRecordFromPlayer(Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - idplayer       = " + player.getIdplayer());
            LOG.debug(methodName + " - photoLocation  = " + player.getPlayerPhotoLocation());

            final String query = "UPDATE player "
                    + "SET PlayerPhotoLocation = ? "
                    + "WHERE idplayer = ?";

            // ✅ try-with-resources — plus de finally/closeQuietly
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, player.getPlayerPhotoLocation());
                ps.setInt(2, player.getIdplayer());
                LCUtil.logps(ps);

                int rows = ps.executeUpdate();

                if (rows != 0) {
                    String msg = "Successful UPDATE PhotoLocation = "
                            + player.getPlayerPhotoLocation()
                            + " for idplayer = " + player.getIdplayer();
                    LOG.debug(methodName + " - " + msg);
                    LCUtil.showMessageInfo(msg);
                    return true;
                } else {
                    String msg = "Unsuccessful UPDATE PhotoLocation for idplayer = "
                            + player.getIdplayer();
                    LOG.error(methodName + " - " + msg);
                    LCUtil.showMessageFatal(msg);
                    return false;
                }
            }

        } catch (SQLException sqle) {
            handleSQLException(sqle, methodName);
            return false;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

} // end class

/*
import entite.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import connection_package.DBConnection;
import utils.LCUtil;
import static interfaces.Log.LOG;

public class UpdatePlayerPhotoLocation {
    
  public boolean updateRecordFromPlayer(Player player, Connection conn) throws SQLException{
    PreparedStatement ps = null;
try{
        LOG.debug("starting update photolocation for. = " + player.getIdplayer() + " photolocation = " 
                                            + player.getPlayerPhotoLocation() );
    final String query = "UPDATE player "
            + " SET PlayerPhotoLocation = ? "
            + " WHERE idplayer = ?";
    ps = conn.prepareStatement(query);
    ps.setString(1,player.getPlayerPhotoLocation());
    ps.setInt(2,player.getIdplayer());
         utils.LCUtil.logps(ps);
   int row = ps.executeUpdate();
      if (row!=0){ 
//LOG.debug("-- successful UPDATE player " + upload.getIdplayer());
        String msg = "<br/>Successful UPDATE file = " + player.getPlayerPhotoLocation()
                + "<br/> for player = " + player.getIdplayer();
            LOG.debug(msg);
            LCUtil.showMessageInfo(msg);
       //   return "updated" + row ;
          return true;
        }else{
             String msg = "-- UNsuccessful result in UPDATE for player : " + player.getIdplayer();
             LOG.error(msg);
             LCUtil.showMessageFatal(msg);
   //          throw new Exception(msg);
             return false;
        }
}catch (SQLException e){
    String msg = "SQL Exception in update player = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
}catch (Exception ex){
    String msg = "Exception in updatePlayer() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
} //end class
*/