package read;

import entite.CompetitionData;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
// ne fonctionne pas ,,
public class LoadCompetitionData{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public CompetitionData load(CompetitionData competition,Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
    PreparedStatement ps = null;
    ResultSet rs = null;
try{
        LOG.debug("entering " + methodName);
        LOG.debug(" with competition = " + competition); 
 //   String ro = utils.DBMeta.listMetaColumnsLoad(conn, "competition_data");

final String query = """
        SELECT *
        FROM competition_data
        WHERE CmpDataId = ?
 """;
     ps = conn.prepareStatement(query);
     ps.setInt(1, competition.getCmpDataId());
     utils.LCUtil.logps(ps); 
     rs =  ps.executeQuery();
     CompetitionData cd = new CompetitionData(); 
     while(rs.next()){
           cd = CompetitionData.map(rs);
      }  //end while
    return cd;
}catch (SQLException e){
    String msg = "SQLException in LoadRound() = " + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in LoadRound = " + ex.toString();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
} //end method

void main() throws SQLException, Exception{ // testing purposes
   Connection conn = new DBConnection().getConnection(); // main
   CompetitionData competition = new CompetitionData();
   competition.setCmpDataId(25);
   competition  = new read.LoadCompetitionData().load(competition,conn);
      LOG.debug(" loaded Competition Data = " + competition);
   DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class