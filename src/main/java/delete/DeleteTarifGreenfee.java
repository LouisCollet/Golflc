package delete;

import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class DeleteTarifGreenfee implements interfaces.GolfInterface{
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
     
  public boolean delete(final TarifGreenfee tarif, String year, final Connection conn) throws Exception {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
      PreparedStatement ps = null;
try{ 
       LOG.debug("starting " + methodName);
       // à modifier tarifYear n'est pas utile !!
    final String query ="""
              DELETE
              FROM tarif_greenfee
              WHERE tarif_greenfee.TarifCourseId = ?
              AND TarifYear = ?
              """;

    ps = conn.prepareStatement(query);
    ps.setInt(1, tarif.getTarifCourseId());
  /*  attention manipulation à modifier !
    String s = tarif.getStartDate().format(ZDF_DAY);
        LOG.debug("s ZDF_DAY = " + s);
        LOG.debug("s = " + tarif.getStartDate().format(ZDF_YEAR));
    String Syear = s.substring(s.length() - 4);
    int year = Integer.valueOf(Syear);
       LOG.debug("year version 1 = " + year);
       LOG.debug("int year version 2 = " + Integer.valueOf(tarif.getStartDate().format(ZDF_YEAR)));
       
   */    
    ps.setInt(2, Integer.valueOf(year));
    LCUtil.logps(ps); 
    int row_deleted = ps.executeUpdate();
    if(row_deleted != 0){
        String msg = "TarifGreenfee deleted ! for year = " + year + " , for courseId = " + tarif.getTarifCourseId();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return true;
    }else{
        String msg = "Error delete TarifGreenfee for year = " + year + " , for courseId = " + tarif.getTarifCourseId();
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return false;
    }
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
     TarifGreenfee tarif = new TarifGreenfee();
     tarif.setTarifCourseId(23);
     tarif.setStartDate(LocalDateTime.parse("2019-01-01T12:30:30"));
     boolean b = new DeleteTarifGreenfee().delete(tarif, "2022", conn);
       LOG.debug("from main - resultat deleted TarifGreenfee = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
       DBConnection.closeQuietly(conn, null, null, null); 
          }
} // end method main
} //end class