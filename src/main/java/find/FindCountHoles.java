package find;

import entite.Tee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCountHoles implements interfaces.Log {

 public int find(final Tee tee, final Connection conn) throws SQLException{
          LOG.debug("starting findCountHoles, tee = {}", tee);
            PreparedStatement ps = null;
            ResultSet rs = null;
       try{
      //          LOG.debug("starting findCountHoles.. = ");
                final String query = """
                       SELECT count(*)
                       FROM hole
                       WHERE hole.tee_idtee = ?
                     """;
    ps = conn.prepareStatement(query);
    ps.setInt(1,tee.getIdtee());
  //  ps.setInt(2,round.getIdround());
    utils.LCUtil.logps(ps);
    rs = ps.executeQuery();
    if(rs.next()){ 
        // LOG.debug("resultat : getCountHoles = " + rs.getInt(1) );
       return rs.getInt(1);
    }else{
      //  LOG.debug("no next : getCountScore = " + rs.getInt(1) );
        return 99;  //error code
    }
            } catch (SQLException e) {
                String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return 0;
            } catch (Exception ex) {
                LOG.error("Exception ! " + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return 0;
            } finally {
                //   DBConnection.closeQuietly(conn, null, rs, ps);
                DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
            }
    } //end method

    void main() throws SQLException, Exception {// testing purposes
        Connection conn = new DBConnection().getConnection();
        Tee tee = new Tee();
        tee.setIdtee(118);
        int i = new FindCountHoles().find(tee, conn);  // teeid
           LOG.debug("main - after holes = " + i);
        DBConnection.closeQuietly(conn, null, null, null);
    }// end main
} //end Class