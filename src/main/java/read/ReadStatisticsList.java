package read;

import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class ReadStatisticsList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private dao.GenericDAO dao;

    /**
     * Charge les statistiques d'un score pour un joueur et un round
     * Utilisé dans include_statistics.xhtml pour compléter la dataTable
     */
    public ArrayList<ScoreStableford.Statistics> load(final Player player, final Round round) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        final String query = """
            SELECT *
            FROM score, round
            WHERE score.player_has_round_player_idplayer = ?
              AND round.idround = ?
              AND score.player_has_round_round_idround = round.idround
              AND round.idround = score.player_has_round_round_idround
            """;

        try (Connection conn = dao.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, player.getIdplayer());
            ps.setInt(2, round.getIdround());
            utils.LCUtil.logps(ps);

            try (ResultSet rs = ps.executeQuery()) {
                ArrayList<ScoreStableford.Statistics> statisticsList = new ArrayList<>();

                while (rs.next()) {
                    ScoreStableford.Statistics sta = new ScoreStableford().new Statistics();
                    sta.setHole(rs.getInt("ScoreHole"));
                    sta.setPar(rs.getInt("ScorePar"));
                    sta.setStroke(rs.getInt("ScoreStroke"));
                    sta.setFairway(rs.getInt("ScoreFairway"));
                    sta.setGreen(rs.getInt("ScoreGreen"));
                    sta.setPutt(rs.getInt("ScorePutts"));
                    sta.setBunker(rs.getInt("ScoreBunker"));
                    sta.setPenalty(rs.getInt("ScorePenalty"));
                    statisticsList.add(sta);
                }

                LOG.debug("ReadStatisticsList returned {} statistics", statisticsList.size());
                return statisticsList;
            }

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return new ArrayList<>();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return new ArrayList<>();
        }
    } // end method

    /*
    void main() throws SQLException {
        Player player = new Player();
        Round round = new Round();
        player.setIdplayer(324713);
        round.setIdround(676);

        var v = new read.ReadStatisticsList().load(player, round);
        LOG.debug("result main size = {}", v.size());
        LOG.debug("result main = {}", v.toString());
    } // end main
    */

} // end class
/*
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import connection_package.DBConnection;
import utils.LCUtil;
import static interfaces.Log.LOG;
public class ReadStatisticsList {
     public ArrayList<ScoreStableford.Statistics> load(Connection conn, final Player player, final Round round) throws SQLException{
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<ScoreStableford.Statistics> statisticsList = new ArrayList<>();
try{
    LOG.debug("starting ReadStatisticsList.load ");
  //  with player = " + player.getIdplayer()
  //          + " round = " + round.getIdround());
     final String query = """
          SELECT *
          FROM score, round
          WHERE score.player_has_round_player_idplayer = ?
            and round.idround = ?
            and score.player_has_round_round_idround = round.idround
            and round.idround = score.player_has_round_round_idround
  """;

     ps = conn.prepareStatement(query);
     ps.setInt(1, player.getIdplayer());
     ps.setInt(2, round.getIdround());
     utils.LCUtil.logps(ps);
     rs =  ps.executeQuery();
      while(rs.next()){  // sera utilisé dans include_statistics.xhtml pour complétér la dataTable
          ScoreStableford.Statistics sta = new ScoreStableford().new Statistics();
          sta.setHole(rs.getInt("ScoreHole"));
          sta.setPar(rs.getInt("ScorePar"));
          sta.setStroke(rs.getInt("ScoreStroke"));
          sta.setFairway(rs.getInt("ScoreFairway"));
          sta.setGreen(rs.getInt("ScoreGreen"));
          sta.setPutt(rs.getInt("ScorePutts"));
          sta.setBunker(rs.getInt("ScoreBunker"));
          sta.setPenalty(rs.getInt("ScorePenalty"));
          statisticsList.add(sta);
        } // end while
   //   LOG.debug(" -- returned statisticsList = {}", statisticsList.toString());
return statisticsList;
}catch (SQLException e){
    String msg = "SQLException in readStatisticsList = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    LOG.error("Exception ! {}", ex);
    LCUtil.showMessageFatal("Exception in readStatisticsList = " + ex.toString() );
     return null;
}finally{
    DBConnection.closeQuietly(null, null, rs, ps); //mod 14/08/2014
}
} //end method

void main() throws SQLException, Exception {
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    Round round = new Round();
    player.setIdplayer(324713);
    round.setIdround(676);
    var v = new read.ReadStatisticsList().load(conn, player, round);
    //   LOG.debug("result main = {}", Arrays.deepToString(v));
       LOG.debug("result main size = {}", v.size());
       LOG.debug("result main = {}", v.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class
*/