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

public class FindSlopeRating implements interfaces.Log {

private static List<ECourseList> liste = null;
private final static String CLASSNAME = utils.LCUtil.getCurrentClassName(); 

  public List<ECourseList> find(final Player player, final Round round, final Connection conn) throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 
   if(liste == null){
            LOG.debug("starting " + methodName + "with Player = ", player);
            LOG.debug(" Round = ", round);

       PreparedStatement ps = null;
       ResultSet rs = null;
   
   try {
 //      LOG.debug("starting getSlopeRating.. = ");
     String ph = utils.DBMeta.listMetaColumnsLoad(conn, "player_has_round");
         //           String sc = utils.DBMeta.listMetaColumnsLoad(conn, "score");
     String te = utils.DBMeta.listMetaColumnsLoad(conn, "tee");
     String cl = utils.DBMeta.listMetaColumnsLoad(conn, "club");
     String co = utils.DBMeta.listMetaColumnsLoad(conn, "course");
     String ro = utils.DBMeta.listMetaColumnsLoad(conn, "round");
     String pl = utils.DBMeta.listMetaColumnsLoad(conn, "player");
     String query
                        = // attention faut un espace en fin de ligne avant le " !!!!
         " SELECT"
         + ph + "," + te + "," + cl + "," + co + "," + ro + "," + pl
         + " FROM round "
         + " JOIN player "
         + "	ON player.idplayer = ? "
         + " JOIN course "
         + "	ON round.course_idcourse = course.idcourse "
         + "	AND round.idround = ? "
         + " JOIN club  "
         + "	ON club.idclub = course.club_idclub "
         + " JOIN player_has_round "
         + "   ON InscriptionIdRound = round.idround "
         + "   AND InscriptionIdPlayer = player.idplayer "
         + " JOIN tee "
         + " 	ON tee.course_idcourse = course.idcourse "
         + "  	  AND player_has_round.InscriptionIdTee = tee.idtee"
               //         + " 	 AND tee.TeeStart = player_has_round.InscriptionTeeStart "
           ;
         ps = conn.prepareStatement(query);
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
  //       return null;
     }else{
         LOG.debug("ResultSet "  + methodName + " has " + liste.size() + " lines.");
     }
         LOG.debug("exiting FindSlopeRating with liste = " + liste.toString());
  return liste;
            } catch (SQLException e) {
                String msg = "SQL Exception in = "  + methodName + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            } catch (Exception ex) {
                LOG.error("Exception ! " + ex);
                LCUtil.showMessageFatal("Exception in " + methodName + ex.toString());
                return null;
            } finally {
                //   DBConnection.closeQuietly(conn, null, rs, ps);
                DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
            }
        } else {
 //            LOG.debug("escaped to FindSlopeRating repetition with lazy loading");
            return liste;  //plusieurs fois ??
        }
    } //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        FindSlopeRating.liste = liste;
    }

    void main() throws SQLException, Exception{

        Connection conn = new DBConnection().getConnection();
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(487);
        round = new read.ReadRound().read(round, conn);
        List<ECourseList> res = new FindSlopeRating().find(player, round, conn);
            LOG.debug("main - after res = " + res.toString());
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main
} //end Class