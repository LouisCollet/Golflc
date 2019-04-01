package modify;

//import create.*;
//import entite.Handicap;
import entite.Club;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import load.LoadClub;
import utils.DBConnection;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class ModifyClub implements Serializable, interfaces.Log, interfaces.GolfInterface{

     
public boolean modify(final Club club, final Connection conn) throws Exception{
        PreparedStatement ps = null;
        boolean b = false;
        try {
            LOG.info("Entering modifyClub with ...");
            LOG.info("club = "+ club.toString());
    String s = utils.DBMeta.listMetaColumnsUpdate(conn, "club");
        LOG.info("String from listMetaColumns = " + s);
    String query = "UPDATE club SET "
                   + s
                   + "  WHERE club.idclub=?"
            ;
            ps = conn.prepareStatement(query);
            ps.setString(1, club.getClubName());
            ps.setString(2, club.getClubAddress());
            ps.setString(3, club.getClubCity());
            ps.setString(4, club.getClubCountry());
            ps.setBigDecimal(5, club.getClubLatitude());
            ps.setBigDecimal(6, club.getClubLongitude().setScale(6,RoundingMode.CEILING) ); // 6 positions décimales
            ps.setString(7, club.getClubWebsite());
            ps.setString(8, club.getClubTimeZone().getTimeZoneId() ); // new 01/08/2017 using GoogleTimeZone
            ps.setInt(9, club.getClubLocalAdmin());
            ps.setInt(10, club.getIdclub());  // ne pas oublier
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
                LOG.info("row = " + row);
            if (row != 0) 
            {
                String msg =  LCUtil.prepareMessageBean("club.modify");
                msg = msg // + "<h1> successful modify Player : "
                            + " <br/>ID = " + club.getIdclub()
                            + " <br/>Name = " + club.getClubName()
                            + " <br/>Address = " + club.getClubAddress();
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
            }else{
                    String msg = "-- NOT NOT successful modify Club row = 0 !!! ";
                    LOG.info(msg);
                    LCUtil.showMessageInfo(msg);
// new 28/12/2014 - à tester                    
                    throw (new SQLException("row = 0 - Could not modify club"));
                //    return false; pas compatible avec throw
            }
return true;
        } // end try
catch(SQLException sqle) {
            String msg = "£££ SQLException in Modify Club = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } catch (NumberFormatException nfe) {
            String msg = "£££ NumberFormatException in Modify Club = " + nfe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   } finally {
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
//         return false;
    } //end ModifyClub

         public static void main(String[] args) throws SQLException, Exception{
     Connection conn = new DBConnection().getConnection();
  try{
     //   Player player = new Player();
     //   player.setIdplayer(324713);
     //   Round round = new Round(); 
     //   round.setIdround(300);
     Club club = new Club();
     club.setIdclub(1104);
  //   load.LoadClub(club,conn);
     Club c = new LoadClub().load(club, conn);
     c.setClubName(club.getIdclub() + "modified");
     boolean b = new ModifyClub().modify(c,conn);
         LOG.info("from main, resultat = " + b);
 }catch (Exception e){
            String msg = "££ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
          }
   } // end main//



} //end Class