package create;

import entite.Course;
import entite.HolesGlobal;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class CreateHolesGlobal // implements // interfaces.GolfInterface{
    {
 public boolean createHoles(HolesGlobal hg, Tee tee, Course course, Connection conn) throws SQLException
{
        LOG.info("entering createHolesGlobal - updateHoles "); 
        LOG.info("holesGlobal - new holes values = " + hg.toString());;
        LOG.info("tee = " + tee.toString());
    PreparedStatement ps = null;
try
{
  //  String s = utils.DBMeta.listMetaColumnsUpdate(conn, "hole"); // MAJ blacklist !!
  //      LOG.info("String for updateHoles from listMetaColumns = " + s);

 //   String query = "UPDATE hole SET "
 //                  + s
 //                  + "  WHERE tee_idtee=?"
 //                  + "  AND Hole.holenumber=?";
    final String query = LCUtil.generateInsertQuery(conn, "hole"); 
    
 for (int i=0; i<hg.getDataHoles().length; i++)
    {
        ps = conn.prepareStatement(query);
   // updated fields
    ps.setNull(1, java.sql.Types.INTEGER); // idhole
    ps.setShort(2, (short) hg.getDataHoles()[i][0]); // holenumber
          ps.setShort(3, (short) hg.getDataHoles()[i][1]); // Par
          ps.setInt(4,hg.getDataHoles()[i][3]); // distance
          ps.setShort(5, (short) hg.getDataHoles()[i][2]); // stroke index
          ps.setInt(6,tee.getIdtee());
    ps.setInt(7, course.getIdcourse());
          ps.setTimestamp(8, LCUtil.getCurrentTimeStamp());
           utils.LCUtil.logps(ps);
          
        int row = ps.executeUpdate(); // write into database
        if(row!=0)
            {
                LOG.info("-- Successfull update Hole for hole : " + hg.getDataHoles()[i][0] + " for tee = " 
                        + tee.getIdtee() + " row = " + row); 
            }else{
                LOG.info("-- ERROR update Hole for hole : " + hg.getDataHoles()[i][0]); 
                return false;
            }
     } // end for
   String msg =  LCUtil.prepareMessageBean("hole.modify");
   LOG.info(msg); 
   LCUtil.showMessageInfo(msg);
return true;
} catch(SQLException sqle) {
       String msg = "£££ SQLException in Modify Holes Global = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
       LOG.error(msg);
       LCUtil.showMessageFatal(msg);
       return false;
    } catch(Exception e) {
       LOG.error(" -- Exception in Modify Holes Global = " + e.getMessage());
       return false;
    }finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
 //       return false;
    }
} //end updateHoles

} // end class