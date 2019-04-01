package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteRound implements interfaces.Log, interfaces.GolfInterface
{
    public boolean delete(final int idround, final Connection conn) throws Exception
    {
    PreparedStatement ps = null;
try
{       LOG.info("starting Delete Round ... = " );
        LOG.info("Delete round for idround "  + idround);
    String query = 
       " DELETE from round" +
       " WHERE round.idround = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, idround);
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
        LOG.info("deleted Round = " + row_deleted);
    String msg = "<br/> <h1>Round deleted = " + idround;
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteRond = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteRound() " + ex;
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
    int idround = 339;
    DeleteRound dr = new DeleteRound();
    boolean b = new DeleteRound().delete(idround, conn);
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