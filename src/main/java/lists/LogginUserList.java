package lists;

import entite.LoggingUser;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import utils.DBConnection;
import utils.LCUtil;

public class LogginUserList{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 private static List<LoggingUser> liste = null;
// pour migration mongoDB 16/08/2022
public List<LoggingUser> list(final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
 final String query ="""
        SELECT *
        FROM logging_user
      """;
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
	while(rs.next()){
            LoggingUser c = entite.LoggingUser.map(rs);
	 liste.add(c);
	} // end while
  //     LOG.debug(" -- before forEach " );
 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda
     // if(liste == null){
      if(liste.isEmpty()){    
         String msg = "££ Empty Result List in " + methodName;
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }
 return liste;
}catch (SQLException e){ 
        String error = "SQL Exception in " + methodName + ": " + e;
	LOG.error(error);
        LCUtil.showMessageFatal(error);
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
 //   LOG.debug("escaped to " + methodName + "repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

    public static List<LoggingUser> getListe() {
        return liste;
    }

    public static void setListe(List<LoggingUser> liste) {
        LogginUserList.liste = liste;
    }
    
    
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
    List<LoggingUser> lp = new LogginUserList().list(conn);
        LOG.debug("from main, after lp = " + lp);
        LOG.debug("nombre de logging users dans la liste = " + lp.size());
 } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class