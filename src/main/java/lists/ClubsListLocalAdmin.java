package lists;

import entite.Club;
import entite.Player;
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

public class ClubsListLocalAdmin {
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
private static List<Club> liste = null;

public List<Club> list(final Player localAdmin, final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
  final String query =  """
        SELECT *
        FROM club
        WHERE club.ClubLocalAdmin = ?
      """ ;
     ps = conn.prepareStatement(query);
     ps.setInt(1, localAdmin.getIdplayer());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
     liste = new ArrayList<>();
	while(rs.next()){
            Club c = entite.Club.dtoMapper(rs);
	    liste.add(c);
	} // end while
  //     LOG.debug(" -- before forEach " );
   //    liste.forEach(item -> LOG.debug("Course list for pro" + item + "/"));  // java 8 lambda
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
    //LOG.debug("escaped to " + methodName + " repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
}
} //end method

 public static List<Club> getListe(){
        return liste;
   }

    public static void setListe(List<Club> liste) {
        ClubsListLocalAdmin.liste = liste;
    }

    void main() throws SQLException, Exception {
        Connection conn = new DBConnection().getConnection();
        try{
            Player localAdmin = new Player();
            localAdmin.setIdplayer(324715);
            List<Club> lp = new ClubsListLocalAdmin().list(localAdmin, conn);
            LOG.debug("from main, after lp = " + lp);
        } catch (Exception e) {
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
        }finally{
            DBConnection.closeQuietly(conn, null, null , null);
        }
    } // end main//
} //end class