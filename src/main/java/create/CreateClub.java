package create;

import entite.Club;
import jakarta.faces.annotation.ApplicationMap;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import utils.LCUtil;



public class CreateClub implements interfaces.Log{
private String[] status = new String [2];

 //  public boolean create(final Club club, final Connection conn) throws SQLException{ 
    public String[] create(final Club club, final Connection conn) throws SQLException{     
     PreparedStatement ps = null;
     status[0] = "false";
     try{
               LOG.debug("entering Createclub.create with club  = " + club.toString());
            final String query = LCUtil.generateInsertQuery(conn, "club");
            ps = conn.prepareStatement(query);
            ps.getWarnings(); // new 27-04-2025
            ps = Club.psClubCreate(ps,club);
            utils.LCUtil.logps(ps);
            int row = ps.executeUpdate(); // write into database
            if (row != 0){
                club.setIdclub(LCUtil.generatedKey(conn));
                String msg = "Club Created  = " + club;
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                status[0] = "true";
                status[1] = Integer.toString(club.getIdclub());
                LOG.debug("status = " + Arrays.toString(status));
               // return true;
                return status;
            }else{
                String msg = "<br/><br/>NOT NOT Successful insert for club = " + club.getIdclub();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                //status[0] = "false";
                return status;
            }
  }catch (SQLException sqle){
            String msg = "£££ exception in Insert Club = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            //status[0] = "false";
            return status;
  }catch (Exception e){
            String msg = "£££ Exception in Insert Club = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            //status[0] = "false";
            return status;
        } finally {
         // utils.DBConnection.closeQuietly(conn, null, null, ps); // not used because of try-with-resources
        }
    } // end method createClub
} //end Class