package create;

import entite.Club;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utils.LCUtil;

public class CreateClub implements interfaces.Log{
   public  boolean create(final Club club, final Connection conn) throws SQLException{ 
     PreparedStatement ps = null;
     try{
            LOG.info("club = " + club.toString());
            LOG.info("line 00");
            final String query = LCUtil.generateInsertQuery(conn, "club"); // new 15/11/2012
             LOG.info("line 01");
            ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
            ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, club.getClubName());
            ps.setString(3, club.getClubAddress());
            ps.setString(4, club.getClubCity());
            ps.setString(5, club.getClubCountry());
                 LOG.info("line 01");
            ps.setBigDecimal(6, club.getClubLatitude());
           LOG.info("line 02");
            ps.setBigDecimal(7, club.getClubLongitude().setScale(6,RoundingMode.CEILING) ); // 6 positions décimales
           LOG.info("line 03");
            ps.setString(8, club.getClubWebsite());
           LOG.info("line 04");
     //       if(club.getClubTimeZone().getTimeZoneId() == null){
     //           LOG.info("getClubTimeZone().getTimeZoneId() == null");
            ps.setString(9, "Europe/Brussels");
     //       }else{
      //          ps.setString(9, club.getClubTimeZone().getTimeZoneId() ); // new 01/08/2017 using GoogleTimeZone
      //      }
            LOG.info("line 05");
     //       ps.setInt(11,club.getClubLocalAdmin());  // new 14-02-2019
            ps.setInt(10,324713);  // mod 29-03-2019
              LOG.info("line 06");
            ps.setTimestamp(11, LCUtil.getCurrentTimeStamp());
              
              
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
            
            if (row != 0){
                int key = LCUtil.generatedKey(conn);
                club.setIdclub(key);
                LOG.info("Club created = " + club.getIdclub());
  //          CourseController.setShowButtonCreateCourse(false); 
//class. .course.setNextCourse(true); // affiche le bouton next(Course) bas ecran à droite
//                course.setNextCourse(true); // affiche le bouton next(Course) bas ecran à droite
                //  LOG.info("-- club after generatedKey = " + club.getIdclub() );
                String msg = "Club Created  = " + club.getIdclub() 
                        + " " + s;
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            }else{
                String msg = "<br/><br/>NOT NOT Successful insert for club = " + club.getIdclub()
                    + " " + s;
                LOG.info(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
  }catch (SQLException sqle){
            String msg = "£££ exception in Insert Club = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
  }catch (Exception e){
            String msg = "£££ Exception in Insert Club = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
        } finally {
         // utils.DBConnection.closeQuietly(conn, null, null, ps); // not used because of try-with-resources
        }
    } // end method createClub
} //end Class