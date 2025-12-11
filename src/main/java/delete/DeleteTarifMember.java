package delete;

import entite.TarifMember;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteTarifMember implements interfaces.Log, interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
  public boolean delete(final TarifMember tarif, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      PreparedStatement ps = null;
try{ 
       LOG.debug("starting " + methodName);
    final String query ="""
              DELETE
              FROM tarif_members
              WHERE tarif_members.TarifMemberIdClub = ?
              AND DATE(TarifMemberStartDate) = DATE(?)
              """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, tarif.getTarifMemberIdClub());
  //  attention manipulation à modifier !
//    String s = tarif.getStartDate().format(ZDF_DAY);
//    String Syear = s.substring(s.length() - 4);
//    int year = Integer.valueOf(Syear);
 //      LOG.debug("year = " + year);

    ps.setTimestamp(2,Timestamp.valueOf(tarif.getStartDate()));
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
    String msg = "There are " + row_deleted + " Tarifmembers deleted = " + tarif;
    if(row_deleted != 0){
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
    }else{
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
    }
    
        
 //       return true;
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
     TarifMember tarif = new TarifMember();
     tarif.setTarifMemberIdClub(1104);
     tarif.setStartDate(LocalDateTime.parse("2021-01-01T12:30:30"));
     boolean b = new DeleteTarifMember().delete(tarif, conn);
       LOG.debug("from main - resultat deleted TarifMember = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class