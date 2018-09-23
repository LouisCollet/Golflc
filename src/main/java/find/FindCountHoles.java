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
            try {
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
            } catch (NullPointerException npe) {
                LOG.error("NullPointerException in FindCountHoles() " + npe);
                LCUtil.showMessageFatal("Exception = " + npe.toString());
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

    public static void main(String s) throws SQLException, Exception // testing purposes
    {
        LOG.info("Input main = " + s);
        DBConnection dbc = new DBConnection();
        Connection conn = dbc.getConnection();
  //      Player player = new Player();
  //      Round round = new Round();
  //      player.setIdplayer(324713);
   //     round.setIdround(260);
//        List<StablefordResult> res = getSlopeRating(player, round, conn);
        LOG.info("main - after");
//for (int x: par )
//        LOG.info(x + ",");
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main
} //end Class