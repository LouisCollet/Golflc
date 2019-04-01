
package create;

import entite.Club;
import entite.Course;
import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;


public class CreateTee implements interfaces.Log{
    
    public boolean create(final Club club, final Course course, final Tee tee,
            final Connection conn) throws SQLException    {
  //      Connection conn = null;
        PreparedStatement ps = null;
   try {
                LOG.info("starting createTee() ... = ");
                LOG.info("with tee = " + tee.toString());
            final String query = LCUtil.generateInsertQuery(conn, "tee");
            //String query = "INSERT INTO tee VALUES (?,?,?,?,?,?,?)";
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, tee.getTeeGender());
            ps.setString(3, tee.getTeeStart());
            ps.setInt(4, tee.getTeeSlope());
            ps.setBigDecimal(5, tee.getTeeRating());
            ps.setInt(6, tee.getTeeClubHandicap());
            ps.setString(7, tee.getTeeHolesPlayed()); // new 29-03-2019
            ps.setInt(8, course.getIdcourse());
            ps.setTimestamp(9, LCUtil.getCurrentTimeStamp());
            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            if (row != 0) {
                int key = LCUtil.generatedKey(conn);
                LOG.info("Tee created = {}" ,key);
                tee.setIdtee(key);
 //               hole.setNextHole(true); // affiche le bouton next(Hole) bas ecran à droite
                String msg = "Tee created = " + tee.getIdtee()
                        + " <br/> </h1> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> gender = " + tee.getTeeGender()
                        + " <br/> Start = " + tee.getTeeStart()
                        + " <br/> Slope = " + tee.getTeeSlope()
                        + " <br/> Rating = " + tee.getTeeRating()
                        + " <br/> HolesPlayed = " + tee.getTeeHolesPlayed()
                                ;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>Succesful insert for tee = " + tee.getIdtee()
                        + " <br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName()
                        + " <br/> gender = " + tee.getTeeGender()
                        + " <br/> Start = " + tee.getTeeStart()
                        + " <br/> Slope = " + tee.getTeeSlope()
                        + " <br/> Rating = " + tee.getTeeRating();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return false;
            }
//        } catch (MySQLIntegrityConstraintViolationException cv) {
//            String msg = "£££ MySQLIntegrityConstraintViolationException in insert Tee = " + cv.getMessage();
//            LOG.error(msg);
//            LCUtil.showMessageFatal(msg);
//            return false;
        } catch (SQLException sqle) {
            //LOG.error("-- £££ exception in Insert tee " + sqle.toString());
            String msg = "£££ SQLexception in Insert Tee = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception nfe) {
            String msg = "£££ Exception in Insert Tee = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
    } //end createTee
} //end class