package delete;

import entite.composite.ECompetition;
import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteInscriptionCompetition implements interfaces.Log, interfaces.GolfInterface{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
 public boolean delete(final ECompetition competition, final Connection conn) throws Exception{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);     
    PreparedStatement ps = null;
try
{       LOG.debug("starting " + methodName);
        LOG.debug(" for idtee "  + competition);
    final String query = 
       " DELETE from competition_data" +
       " WHERE CmpDataId = ?" +
       " AND CmpDataPlayerId = ?"
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, competition.getCompetitionData().getCmpDataId());
    ps.setInt(2, competition.getCompetitionData().getCmpDataPlayerId());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
 //       LOG.debug("deleted CompetitionData = " + row_deleted);
    String msg = "There is " + row_deleted + " Competition Data deleted ! ";// + competition;
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in " + methodName + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
     ECompetition ec = new ECompetition();
  //  ec.
 //   boolean b = new DeleteInscriptionCompetition().delete(tee, conn);
 //       LOG.debug("from main - resultat deleteTee = " + b);
 } catch (Exception e) {
        String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class