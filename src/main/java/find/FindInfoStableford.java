package find;

import entite.composite.ECourseList;
import entite.Player;
import entite.Round;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class FindInfoStableford implements interfaces.Log {

private static List<ECourseList> liste = null;
 private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();

 public ECourseList find(final Player player, final Round round, final Connection conn) throws SQLException {
      final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
   if(liste == null){
            LOG.debug("... starting " + methodName);
            LOG.debug(" for player " + player);
            LOG.debug(" for round " + round);
       PreparedStatement ps = null;
       ResultSet rs = null;
   try {

   final String query = """
  SELECT *
  FROM round
  JOIN player
     ON player.idplayer = ?
  JOIN course
   ON round.course_idcourse = course.idcourse
   AND round.idround = ?
  JOIN club
   ON club.idclub = course.club_idclub
  JOIN player_has_round
   ON InscriptionIdRound = round.idround
   AND InscriptionIdPlayer = player.idplayer
  JOIN tee
   ON tee.course_idcourse = course.idcourse
   AND player_has_round.InscriptionIdTee = tee.idtee
""";
      ps = conn.prepareStatement(query);
  // search fields
      ps.setInt(1, player.getIdplayer());
      ps.setInt(2, round.getIdround());
      utils.LCUtil.logps(ps);
      rs = ps.executeQuery();
     liste = new ArrayList<>();
     while(rs.next()) {
          ECourseList ecl = new ECourseList();
          ecl.setPlayer(entite.Player.map(rs));
          ecl.setClub(entite.Club.dtoMapper(rs));
          ecl.setCourse(entite.Course.dtoMapper(rs));
          ecl.setRound(new entite.Round().dtoMapper(rs,ecl.getClub()));// mod 19-02-2020 pour générer ZonedDateTime
          ecl.setInscription(entite.Inscription.map(rs));
          ecl.setTee(entite.Tee.dtoMapper(rs));
      liste.add(ecl);
      } //end while
       if(liste.isEmpty()){
         String msg = "££ Empty Result Table in " + methodName + " for player = " + player.getIdplayer();
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
       }
       if(liste.size() > 1){
         String msg = "££ Result Table > 1 = " + liste.size() + methodName + " for player = " + player.getIdplayer();
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
   //      return null;
       }
       
      LOG.debug("ResultSet "  + methodName + " has " + liste.size() + " lines.");
 //  }
   ECourseList ecl = liste.get(0);
   //   LOG.debug("exiting FindSlopeRating with liste = " + liste.toString());
      LOG.debug("exiting FindInfoStableford with ECourseList = " + ecl.toString());
  //    return liste;
    return ecl;
  } catch (SQLException e) {
                String msg = "SQL Exception in  " + methodName + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
  } catch (Exception ex) {
                LOG.error("Exception in " + methodName + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return null;
  } finally {
                //   DBConnection.closeQuietly(conn, null, rs, ps);
                DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
            }
  } else {
 //            LOG.debug("escaped to FindSlopeRating repetition with lazy loading");
         //   return ecl;  //plusieurs fois ??
           return liste.get(0);
   }
    } //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        FindInfoStableford.liste = liste;
    }

    void main() throws SQLException, Exception{
        Connection conn = new DBConnection().getConnection();
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(636); // test competition
        round = new read.ReadRound().read(round, conn);
        ECourseList ecl = new FindInfoStableford().find(player, round, conn);
           LOG.debug("main - after res = " + ecl);
        DBConnection.closeQuietly(conn, null, null, null);
    }// end main
} //end Class