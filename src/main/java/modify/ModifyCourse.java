package modify;

//import create.*;
//import entite.Handicap;
import entite.Course;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class ModifyCourse implements Serializable, interfaces.Log, interfaces.GolfInterface{

public boolean modifyCourse(final Course course, final Connection conn) throws Exception{
        PreparedStatement ps = null;
    //    int row = 0;
        boolean b = false;
        try {
        ///    LOG.info("club ID  = " + club.getIdclub());
            //LOG.info("club City  = " + club.getCourseCity() );
            LOG.info("course Name  = " + course.getCourseName());
            LOG.info("course Holes  = " + course.getCourseHoles());
            LOG.info("course Par  = " + course.getCoursePar());
            
    String co = utils.DBMeta.listMetaColumnsUpdate(conn, "course");
        LOG.info("String from listMetaColumns = " + co);
    String query = "UPDATE course SET "
                   + co
                   + "  WHERE course.idcourse=?";
        
             ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
       //     ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(1, course.getCourseName());
      //      ps.setShort(2, course.getCourseHoles());
            ps.setShort(2, (short) 18);// mod 12-11-2018 toujour 18 holes  enlevé dans blaclist de columns update
            ps.setShort(3, course.getCoursePar()); 
///            ps.setInt(6, club.getIdclub());
            ps.setDate(4, LCUtil.getSqlDate(course.getCourseBegin()));
            ps.setDate(5, LCUtil.getSqlDate(course.getCourseEnd()));
            ps.setInt(6, course.getIdcourse());  // ne pas oublier
////            ps.setTimestamp(9, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg =  LCUtil.prepareMessageBean("course.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + course.getIdcourse()
                            + " <br/>Name = " + course.getCourseName()
                            + " <br/>Par = " + course.getCoursePar();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful modify Course row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not modify club"));
                //    return false; pas compatible avec throw
            }
return true;
        } // end try
catch (SQLException sqle) {
            String msg = "£££ SQLException in Modify Course = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Modify Course = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyCourse
} //end Class