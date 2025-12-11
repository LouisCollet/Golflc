package read;

import entite.CompetitionDescription;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class LoadCompetitionDescription{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public CompetitionDescription load(CompetitionDescription competition,Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);
        LOG.debug(" with competition = " + competition); 
 //   String cde = utils.DBMeta.listMetaColumnsLoad(conn, "competition_description");
 //    LOG.debug(" string cde = " + cde); 
final String query = """
        SELECT *
        FROM competition_description
        WHERE CompetitionId = ?
      """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, competition.getCompetitionId());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     CompetitionDescription cd = new CompetitionDescription(); 
     int i = 0;
     while(rs.next()){
         i++;
           cd = CompetitionDescription.map(rs);
      }  //end while
     if(i == 0){
         String msg = "Nothing found in LoadCompetition !!!";
         LOG.debug(msg);
         LCUtil.showMessageFatal(msg);
         cd = null;
     }
    return cd;
}catch (SQLException e){
    String msg = "SQLException in " + methodName + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        
        return null;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex.toString();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{
   Connection conn = new DBConnection().getConnection();
   CompetitionDescription competition = new CompetitionDescription();
   competition.setCompetitionId(24);
   competition  = new LoadCompetitionDescription().load(competition,conn);
     LOG.debug(" loaded competition description = " + competition);
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class