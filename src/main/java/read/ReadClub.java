package read;

import entite.Club;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static interfaces.Log.LOG;
import rowmappers.ClubRowMapper;
import rowmappers.RowMapper;
import utils.LCUtil;

/**
 * Service de lecture de Club
 * ✅ @ApplicationScoped - Stateless, partagé
 * ✅ @Resource DataSource - Connection pooling
 * ✅ Pattern RowMapper conservé
 */
@ApplicationScoped
public class ReadClub implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    /**
     * DataSource injecté par WildFly (connection pooling)
     */
    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    /**
     * Lit un Club par ID
     * 
     * @param club Club avec l'ID à rechercher
     * @return Club complet
     * @throws Exception en cas d'erreur
     */
    public Club read(Club club) throws Exception {
        
        final String methodName = LCUtil.getCurrentMethodName();
        String msg;
        
        try (Connection conn = dataSource.getConnection()) {
            
            // Validation
            if (club == null || club.getIdclub() == null || club.getIdclub() == 0) {
                msg = "Valid club ID is required";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
            }
            
            LOG.debug("Reading club with ID: {}", club.getIdclub());
            
            // Read Club
            String query = """
                SELECT * FROM club
                WHERE club.idclub = ?
                """;
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, club.getIdclub());
                LCUtil.logps(ps);
                
                // ✅ PARTIE NON MODIFIÉE (comme demandé)
                try (ResultSet rs = ps.executeQuery()) {
                   // if (rs.next()) {
                    RowMapper<Club> clubMapper = new ClubRowMapper();
                    while(rs.next()){
                        club = clubMapper.map(rs);
                    }  //end while
 //    LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
                    return club;
                }
            }
            
        } catch (SQLException sqle) {
            LCUtil.printSQLException(sqle);
            msg = String.format("SQLException in %s: %s", methodName, sqle.getMessage());
            LOG.error(msg);
            throw sqle;
            
        } catch (Exception e) {
            msg = "Exception in " + methodName + ": " + e.getMessage();
            LOG.error(msg);
            throw e;
        }
    }

    /**
     * Main pour tests
     */
    public static void main(String[] args) {
        try {
            Club club = new Club();
            club.setIdclub(101);
            
            LOG.debug("Main ready (CDI required for execution)");
            
        } catch (Exception e) {
            LOG.error("Exception in main: " + e.getMessage(), e);
        }
    }
}
/*
import entite.Club;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import rowmappers.ClubRowMapper;
import rowmappers.RowMapper;
import connection_package.DBConnection;
import utils.LCUtil;

public class ReadClub{
  
public Club read(Club club,Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);
        LOG.debug("with Club = " + club);
final String query = """
        SELECT *
        FROM Club
        WHERE idclub = ?
       """ ;

    ps = conn.prepareStatement(query);
    ps.setInt(1, club.getIdclub());
    utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    RowMapper<Club> clubMapper = new ClubRowMapper();
    while(rs.next()){
          club = clubMapper.map(rs);
    }  //end while
 //    LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
    return club;
}catch (SQLException e){
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}
finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

public static void main(String[] args) throws Exception, SQLException{
    Connection conn = new DBConnection().getConnection();
    
    // Affiche la classe principale passée par Maven
        String className = System.getProperty("printClassName");
        if (className != null) {
            LOG.debug("Classe principale passée par Maven : " + className);
        }
    
    Club club = new Club();
    club.setIdclub(154);
    Club c = new ReadClub().read(club, conn);
       LOG.debug(" club loaded = " + c.toString());
    DBConnection.closeQuietly(conn, null, null, null);

}// end main
} // end class
*/