package delete;

import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class DeleteTee implements interfaces.Log, interfaces.GolfInterface{
    public boolean delete(final Tee tee, final Connection conn) throws SQLException {
    PreparedStatement ps = null;
try{
       LOG.debug("starting Delete Tee ... = " );
       LOG.debug(" with tee = "  + tee);
     // question : que faire si on delete un MasterTee ? donner un message !!
     // question : que faire si on delete un DistanceTee - fait mais ps correct si pas de distance tee !
       
    String query =  """
       DELETE from tee
       WHERE tee.idtee = ?
       """ ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getIdtee());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Tee = " + row_deleted);
    String msg = "<br/> <h2>There are " + row_deleted + " Tee deleted = " + tee;
        LOG.debug(msg);
        showMessageInfo(msg);
    if(row_deleted != 0){
           msg = "Tee Deleted = " + tee;
           LOG.info(msg);
           showMessageInfo(msg);
        // new 16-08-2023  non testé   
           query = """
             DELETE from distances
             WHERE DistanceIdTee = ?
          """;
            ps = conn.prepareStatement(query); 
            ps.setInt(1, tee.getIdtee());
            LCUtil.logps(ps); 
            int row_inscription = ps.executeUpdate();
            LOG.debug("deleted DistanceTee = " + row_inscription);
           return true;
    } else {
           msg = "ERROR tee NOT Deleted !!: " + tee;
           LOG.debug(msg);
           showMessageFatal(msg);
           return false;
    }   
}catch (SQLException e){
    String msg = "SQL Exception in DeleteTee = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode() + "<br/>for tee = " + tee;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteTee() " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 void main() throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
    Tee tee = new Tee();
    tee.setIdtee(339);
    boolean b = new DeleteTee().delete(tee, conn);
        LOG.debug("from main - resultat deleteTee = " + b);
 } catch (Exception e) {
        String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class