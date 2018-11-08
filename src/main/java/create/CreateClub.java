package create;

import entite.Club;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.validation.Valid;
import utils.LCUtil;

public class CreateClub implements interfaces.Log
{
   public  boolean createClub(final @Valid Club club, final Connection conn) throws SQLException
   { 
     //   PreparedStatement ps = null;
//        try (PreparedStatement ps = null;)
//        {
            LOG.info("club = " + club.toString());
    //        LOG.info("club Name       = " + club.getClubName());
    //        LOG.info("club Address    = " + club.getClubAddress());
    //        LOG.info("club City       = " + club.getClubCity());
    //        LOG.info("club Country    = " + club.getClubCountry());
    //        LOG.info("club Latitude   = " + club.getClubLatitude());
    //        LOG.info("club Longitude  = " + String.format("%.6f", club.getClubLongitude() ) );
    //        LOG.info("club Web site   = " + club.getClubWebsite());
            LOG.info("club Zone ID   = " + club.getClubTimeZone().getTimeZoneId());

            final String query = LCUtil.generateInsertQuery(conn, "club"); // new 15/11/2012
            try (PreparedStatement ps = conn.prepareStatement(query) ) // mod 21/04/2014 try-with-resources jdk1.7
        {
    //        ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, club.getClubName());
            ps.setString(3, club.getClubAddress());
            ps.setString(4, club.getClubCity());
            ps.setString(5, club.getClubCountry());
            ps.setBigDecimal(6, club.getClubLatitude());
       //     ps.setBigDecimal(7, club.getClubLongitude());
            ps.setBigDecimal(7, club.getClubLongitude().setScale(6,RoundingMode.CEILING) ); // 6 positions décimales
            // format : 4.5555550000000000210320649784989655017852783203125 
            ps.setString(8, club.getClubWebsite());
            ps.setString(9, club.getClubTimeZone().getTimeZoneId() ); // new 01/08/2017 using GoogleTimeZone
            ps.setTimestamp(10, LCUtil.getCurrentTimeStamp());
             //    String p = ps.toString();
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            String s = "<br/>" + " name = " + club.getClubName()
                        + "<br/>" + " address = " + club.getClubAddress()
                        + "<br/>" + " city = " + club.getClubCity()
                        + "<br/>" + " country = " + club.getClubCountry()
                        + "<br/>" + " latit  = " + club.getClubLatitude()
                        + "<br/>" + " longit = " + club.getClubLongitude()
                  //      + "<br/>" + " longit = " + String.format("%.6f", club.getClubLongitude())
                        + "<br/>" + " web = " + club.getClubWebsite();
            
            if (row != 0)
            {
                int key = LCUtil.generatedKey(conn);
                club.setIdclub(key);
                LOG.info("Club created = " + club.getIdclub());
  //          CourseController.setShowButtonCreateCourse(false); 
//class. .course.setNextCourse(true); // affiche le bouton next(Course) bas ecran à droite
//                course.setNextCourse(true); // affiche le bouton next(Course) bas ecran à droite
                //  LOG.info("-- club after generatedKey = " + club.getIdclub() );
                String msg = "<br/><br/><h1>Club Created  = " + club.getIdclub() + "</h1>"
                        + " " + s;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/><br/>NOT NOT Successful insert for club = " + club.getIdclub()
                    + " " + s;
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
        }
  //          catch (MySQLIntegrityConstraintViolationException cv) {
  //          String msg = "£££ MySQLIntegrityConstraintViolationException in insert Club = " + cv.getMessage();
  //          LOG.error(msg);
   //         LCUtil.showMessageFatal(msg);
   //         return false;
   //     }
            catch (SQLException sqle) {
            String msg = "£££ exception in Insert Club = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Insert Club = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } catch (Exception e) {
            String msg = "£££ Exception in Insert Club = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
         // utils.DBConnection.closeQuietly(conn, null, null, ps); // not used because of try-with-resources
        }
    } // end method createClub

} //end Class
