package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteHoles implements interfaces.Log, interfaces.GolfInterface
{
    public boolean delete(final int idtee, final Connection conn) throws Exception
    {
    PreparedStatement ps = null;
try
{       LOG.info("starting Delete Holes ... = " );
        LOG.info("Delete Holes for idtee "  + idtee);
    String query = 
       " DELETE from hole" +
       " WHERE hole.tee_idtee = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, idtee);
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.info("deleted Holes = " + row_deleted);
    String msg = "<br/> <h1> There are " + row_deleted + " Holes deleted for tee = " + idtee;
        LOG.info(msg);
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
   
 public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
 try{
    int idtee = 339;
    boolean b = new DeleteHoles().delete(idtee, conn);
        LOG.info("from main - resultat deleteRound = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class