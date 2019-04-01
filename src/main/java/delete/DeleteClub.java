package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteClub implements interfaces.Log, interfaces.GolfInterface{
  public boolean delete(final int idclub, final Connection conn) throws Exception {
    PreparedStatement ps = null;
try{ 
    LOG.info("starting Delete Club ... = " );
        LOG.info("Delete club for idclub "  + idclub);
    String query = 
       " DELETE from club" +
       " WHERE club.idclub = ?" 
        ;
    ps = conn.prepareStatement(query);
    ps.setInt(1, idclub);
    LCUtil.logps(ps); 
    int row_delete = ps.executeUpdate();
        LOG.info("deleted Club = " + row_delete);
    String msg = "There are " + row_delete + " Club deleted = " + idclub;
        LOG.info(msg);
  //      LCUtil.showMessageInfo(msg);
        return true;
}catch (SQLException e){
    String msg = "SQL Exception in DeleteClub = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}catch (Exception ex){
    String msg = "Exception in DeleteClub() " + ex;
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return false;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 public static void main(String[] args) throws SQLException, Exception 
 {
     Connection conn = new DBConnection().getConnection();
 try{
    int idclub = 113;
    boolean b = new DeleteClub().delete(idclub, conn);
    LOG.info("from main - resultat deleteclub = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class