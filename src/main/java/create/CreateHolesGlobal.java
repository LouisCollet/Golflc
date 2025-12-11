package create;

import entite.Course;
import entite.HolesGlobal;
import entite.Tee;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

public class CreateHolesGlobal {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
 public boolean create(final HolesGlobal holesGlobal, final Tee tee, final Course course, final Connection conn) throws SQLException{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        LOG.debug("... entering in " + methodName); 
        LOG.debug("CreateHolesGlobal - new holes values = " + NEW_LINE + holesGlobal);
        LOG.debug("course = " + course);
        LOG.debug("tee = " + tee);
    PreparedStatement ps = null;
try{
    final String query = LCUtil.generateInsertQuery(conn, "hole"); 
  //  LOG.debug("verification : total od IndexArra must be = 171 " + holesGlobal.getDataHoles().)
 for (int i=0; i<holesGlobal.getDataHoles().length; i++){
        ps = conn.prepareStatement(query);
        LOG.debug("handling index i = " + i);
        LOG.debug("handling holesGlobal = " + Arrays.toString(holesGlobal.getDataHoles()[i]));
   // updated fields
    ps.setNull(1, java.sql.Types.INTEGER); // idhole
    ps.setShort(2, (short) holesGlobal.getDataHoles()[i][0]); // holenumber
    ps.setShort(3, (short) holesGlobal.getDataHoles()[i][1]); // Par
    // mod 19-08-203 distances transfered to table distances
 //   ps.setInt(4,holesGlobal.getDataHoles()[i][3]); // distance
    ps.setInt(4, 0); // distance
    ps.setShort(5, (short) holesGlobal.getDataHoles()[i][2]); // stroke index
    ps.setInt(6,tee.getIdtee());
    ps.setInt(7, course.getIdcourse());
    ps.setTimestamp(8, Timestamp.from(Instant.now()));
    
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
    if(row!=0){
                LOG.debug("-- Successfull update Hole for hole : " + holesGlobal.getDataHoles()[i][0] + " for tee = " 
                        + tee.getIdtee() + " row inserted = " + row); 
     }else{
                LOG.debug("-- ERROR update Hole for hole : " + holesGlobal.getDataHoles()[i][0]); 
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
       LOG.error(" -- Exception in " + methodName + e.getMessage());
       return false;
    }finally{
        DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
 //       return false;
    }
} //end updateHoles
 
  void main() throws SQLException, Exception {
   Connection conn = new DBConnection().getConnection();
   try{
  //          Player player = new Player();
    //        player.setIdplayer(324713);

    }catch (Exception e){
            String msg = "££ Exception in main CreateBlocking = " + e.getMessage();
            LOG.error(msg);
            //      LCUtil.showMessageFatal(msg);
    }finally{
            DBConnection.closeQuietly(conn, null, null, null);
    }
    } // end main//
 } // end class