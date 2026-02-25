package update;

import entite.HolesGlobal;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class UpdateHolesGlobal{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
 public boolean update(HolesGlobal holesGlobal, Tee tee, Connection conn) throws SQLException{
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("entering " + methodName); 
        LOG.debug("holesGlobal - new holes values = " + holesGlobal);
        LOG.debug("tee = " + tee.toString());
    PreparedStatement ps = null;
try{
    String ho = utils.DBMeta.listMetaColumnsUpdate(conn, "hole"); // MAJ blacklist !!
        LOG.debug("String for updateHoles from listMetaColumns = " + ho);
     // %s indique qu'il s'agit d'un string dans est le même pour toutes les query
    final String query = """
            UPDATE hole
            SET %s
            WHERE tee_idtee=?
               AND Hole.holenumber=?
           """.formatted(ho);

    LOG.debug("longueur = " + holesGlobal.getDataHoles().length);
 for (int i=0; i<holesGlobal.getDataHoles().length; i++) {
  //      var v = holesGlobal.getDataHoles()[i];
  //      LOG.debug(" v = "+ v);
        ps = conn.prepareStatement(query);
        LOG.debug(" i = " + i);
   // updated fields
          ps.setShort(1, (short) holesGlobal.getDataHoles()[i][1]); // Par
          // modified 19-08-2023 tansfered to table distances
          ps.setInt(2,holesGlobal.getDataHoles()[i][3]);            // distance
          ps.setInt(2,0);            // distance
          ps.setShort(3, (short) holesGlobal.getDataHoles()[i][2]); // stroke index
    // find keys
          ps.setInt(4,tee.getIdtee());
          ps.setInt(5,holesGlobal.getDataHoles()[i][0]);
             utils.LCUtil.logps(ps);
        int row = ps.executeUpdate(); // write into database
        if(row!=0){
                LOG.debug("-- Successfull update Hole for hole : " + holesGlobal.getDataHoles()[i][0] + " for tee = " 
                        + tee.getIdtee() + " row = " + row); 
            }else{
                String msg = "-- ERROR update Hole for hole : " + holesGlobal.getDataHoles()[i][0]; 
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
     } // end for

return true;
} catch(SQLException sqle) {
       String msg = "£££ SQLException in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
} catch(Exception e) {
       LOG.error(" -- Exception in  " +methodName + e.getMessage());
       return false;
}finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
      //  return false;
    }
} //end updateHoles
} // end class