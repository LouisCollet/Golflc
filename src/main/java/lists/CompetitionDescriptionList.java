package lists;

import entite.CompetitionDescription;
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

public class CompetitionDescriptionList implements interfaces.Log{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 //   private static List<CompetitionDescription> liste = null;
  //  private static List<ECompetition> liste = null;
     private static List<CompetitionDescription> liste = null;
    
 public List<CompetitionDescription> list(final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
    // eliminer les compétitions archivées ??
    
     final String query = """
        SELECT *
        FROM competition_description
      """;
 
     ps = conn.prepareStatement(query);
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
//    ECompetition ec = new ECompetition();
    CompetitionDescription cde = new CompetitionDescription();
	while(rs.next()){
           cde = entite.CompetitionDescription.map(rs);
           liste.add(cde);
	} // end while

 //      liste.forEach(item -> LOG.debug("Course list " + item + "/"));  // java 8 lambda                   
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

    public static List<CompetitionDescription> getListe() {
        return liste;
    }
    public static void setListe(List<CompetitionDescription> liste) {
        CompetitionDescriptionList.liste = liste;
    }

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
    var lp = new CompetitionDescriptionList().list(conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//
} //end class