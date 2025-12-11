package lists;

import entite.Club;
import static interfaces.Log.LOG;
import jakarta.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class ClubList{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 private static List<Club> liste = null;

public List<Club> list(final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
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
	while(rs.next()){
            Club c = entite.Club.dtoMapper(rs);
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
}catch (SQLException sqle){ 
       String msg = " -- SQL Exception in " + methodName + " -- ErrorCode = " + sqle.getErrorCode() + " -- SQLSTATE =  " + sqle.getSQLState();
       LCUtil.showMessageFatal(msg);
       return null;
}catch (Exception ex){
    String error = "Exception in " + methodName + " / " + ex;
    LOG.error(error);
    LCUtil.showMessageFatal(error);
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