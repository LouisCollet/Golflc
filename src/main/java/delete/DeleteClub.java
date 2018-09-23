package delete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteClub implements interfaces.Log, interfaces.GolfInterface
{
  public String deleteClub(final int idclub, final Connection conn) throws Exception {
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
    String msg = "<br/> <h1>There are " + row_delete + " Club deleted = " + idclub;
        LOG.info(msg);
  //      LCUtil.showMessageInfo(msg);
        return "Club deleted ! ";
}catch (SQLException e){
    String msg = "SQL Exception in DeleteClub = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;
}catch (Exception ex){
    String msg = "Exception in DeleteClub() " + ex;
    LOG.error(msg);
  //  LCUtil.showMessageFatal(msg);
    return null;
}finally{
        utils.DBConnection.closeQuietly(null, null, null, ps);
}
} //end method
   
 public static void main(String[] args) throws SQLException, Exception 
 {
     DBConnection dbc = new DBConnection();
     Connection conn = dbc.getConnection();
 try{
       LOG.info("Input main = ");
    int idclub = 113;
 //  Date date =SDF.parse("01/01/2000");
    DeleteClub dc = new DeleteClub();
    dc.deleteClub(idclub, conn);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class