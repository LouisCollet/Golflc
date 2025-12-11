package delete;

import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteHoles implements interfaces.Log, interfaces.GolfInterface{
    public boolean delete(final Tee tee, final Connection conn) throws Exception    {
    PreparedStatement ps = null;
try
{       LOG.debug("starting Delete Holes ... = " );
        LOG.debug("Delete Holes for idtee "  + tee);
    String query = 
       " DELETE from hole" +
       " WHERE hole.tee_idtee = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, tee.getIdtee());
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.debug("deleted Holes = " + row_deleted);
    String msg = "<br/> <h1> There are " + row_deleted + " Holes deleted for tee = " + tee;
        LOG.debug(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteHoles = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteHoles() " + ex;
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
     Tee tee = new Tee();
     tee.setIdtee(339);
    boolean b = new DeleteHoles().delete(tee, conn);
        LOG.debug("from main - resultat deleteRound = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class