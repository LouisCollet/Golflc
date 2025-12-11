package lists;

import entite.CompetitionData;
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

public class CompetitionDataList implements interfaces.Log{
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
  private static List<CompetitionData> liste = null;

 public List<CompetitionData> list(CompetitionDescription cd, final @NotNull Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
if(liste == null){
        LOG.debug(" ... entering " + methodName);
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
     final String query = """
        SELECT *
        FROM competition_data
        WHERE CmpDataCompetitionId = ?
        ORDER BY CmpDataFlightNumber
      """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, cd.getCompetitionId());
     utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
    liste = new ArrayList<>();
    CompetitionData ec = new CompetitionData();
	while(rs.next()){
           ec = entite.CompetitionData.map(rs);
           liste.add(ec);
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

    public static List<CompetitionData> getListe() {
        return liste;
    }
    public static void setListe(List<CompetitionData> liste) {
        CompetitionDataList.liste = liste;
    }

 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
      CompetitionDescription cde= new CompetitionDescription();
      cde.setCompetitionId(24);
    var lp = new CompetitionDataList().list(cde,conn);
        LOG.debug("from main, after lp = " + lp);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main//
} //end class