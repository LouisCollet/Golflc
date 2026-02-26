
package lists;

import entite.Club;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import rowmappers.ClubRowMapper;
import rowmappers.RowMapper;

@ApplicationScoped
public class ClubList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Resource(lookup = "java:jboss/datasources/golflc")
    private DataSource dataSource;

    // ✅ Cache d'instance — @ApplicationScoped garantit le singleton
    private List<Club> liste = null;

    /**
     * Liste tous les clubs
     * @return liste de tous les clubs
     */
    public List<Club> list() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();

        // ✅ EARLY RETURN - Guard clause
        if (liste != null) {
            LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
            return liste;
        }

        // Chargement depuis la base de données
        LOG.debug("... entering " + methodName);

        final String query = """
            SELECT *
            FROM club
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                liste = new ArrayList<>();
                RowMapper<Club> clubMapper = new ClubRowMapper();

                while (rs.next()) {
                    Club c = clubMapper.map(rs);
                    liste.add(c);
                }

                if (liste.isEmpty()) {
                    String msg = "Empty Result List ClubList in " + methodName;
                    LOG.warn(msg); // ✅ warn au lieu de error
                } else {
                    LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
                }

                return liste;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ✅ Getters/setters d'instance
    public List<Club> getListe()               { return liste; }
    public void setListe(List<Club> liste)     { this.liste = liste; }

    // ✅ Invalidation explicite
    public void invalidateCache() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.liste = null;
        LOG.debug(methodName + " - cache invalidated");
    } // end method

    /*
    void main() throws SQLException {
        try {
            List<Club> lp = new ClubList().list();
            LOG.debug("from main, after lp = " + lp);
            LOG.debug("nombre de clubs dans la liste = " + lp.size());
        } catch (Exception e) {
            String msg = "Exception in main: " + e.getMessage();
            LOG.error(msg, e);
        }
    } // end main
    */

} // end class

/*
import entite.Club;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import jakarta.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rowmappers.ClubRowMapper;
import rowmappers.RowMapper;
import connection_package.DBConnection;
import utils.LCUtil;

public class ClubList{


 private static List<Club> liste = null;

public List<Club> list(final @NotNull Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
final String query = """
        SELECT *
        FROM club
      """;
    ps = conn.prepareStatement(query);
    utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
//   utils.LCUtil.logRs(rs); // testing 
    liste = new ArrayList<>();
    RowMapper<Club> clubMapper = new ClubRowMapper();
	while(rs.next()){
            Club c = clubMapper.map(rs);
	    liste.add(c);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
      //if(liste == null){ // mod 22-04-2025
       if(liste.isEmpty()){   
         String msg = "££ Empty Result List ClubList in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
    handleSQLException(e, methodName);
    return null;
}catch (Exception e){
    handleGenericException(e, methodName);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  //  LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<Club> getListe() {
        return liste;
    }

    public static void setListe(List<Club> liste) {
        ClubList.liste = liste;
    }
    
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
    List<Club> lp = new ClubList().list(conn);
        LOG.debug("from main, after lp = " + lp);
        LOG.debug("nombre de clubs dans la liste = " + lp.size());
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class
*/