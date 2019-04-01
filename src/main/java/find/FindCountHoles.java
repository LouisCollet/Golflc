package find;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.DBConnection;
import utils.LCUtil;

public class FindCountHoles implements interfaces.Log {

    public int findCountHoles(final int tee, final Connection conn) throws SQLException 
    {
          LOG.debug("starting findCountHoles, tee = {}", tee);
            PreparedStatement ps = null;
            ResultSet rs = null;
       try{
      //          LOG.info("starting findCountHoles.. = ");
                String query = " SELECT * from hole where hole.tee_idtee = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, tee);
                utils.LCUtil.logps(ps);
                rs = ps.executeQuery();
                rs.last(); //on récupère le numéro de la ligne
                    LOG.info("ResultSet findCountHoles " + rs.getRow() + " lines.");
                return rs.getRow();
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

    public static void main(String[] args) throws SQLException, Exception {// testing purposes
        
        Connection conn = new DBConnection().getConnection();
        int i = new FindCountHoles().findCountHoles(147, conn);  // teeid
        LOG.info("main - after holes = " + i);
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main
} //end Class