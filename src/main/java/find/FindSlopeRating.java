package find;

import entite.Club;
import entite.ECourseList;
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
final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 

  public List<ECourseList> find(final Player player, final Round round, final Connection conn) throws SQLException {
    if(liste == null){
            LOG.debug("starting FindSlopeRating(), Player = {}", player.toString());
            LOG.debug("starting FindSlopeRating(), Round = {}", round.toString());

       PreparedStatement ps = null;
       ResultSet rs = null;
       try {
                LOG.info("starting getSlopeRating.. = ");
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
                           + ph + "," + te + "," + cl + "," + co + "," + ro + "," + pl //+ "," + ha
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
         rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet FindSlopeRating has " + rs.getRow() + " lines.");
         if(rs.getRow() == 0){
            String msg = "-- Empty Result Table for FindSlopeRating !! ";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
         }

          if (rs.getRow() != 1) {
                    String msg = "-- Empty or too much Result(s) in Table for FindSlopeRating !! ";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new Exception(msg);
                }
                rs.beforeFirst(); //on replace le curseur avant la première ligne
                liste = new ArrayList<>();
                //LOG.info("just before while ! ");
                
     while(rs.next()) {
          ECourseList ecl = new ECourseList();

      //    Player p = new Player();
      //    p = );
          ecl.setPlayer(entite.Player.mapPlayer(rs));
 
    //      Club c = new Club();
       //   c = entite.Club.mapClub(rs);
          Club c = entite.Club.mapClub(rs);
          ecl.setClub(c);
 
   //       Course o = new Course();
    //      o = entite.Course.mapCourse(rs);
          ecl.setCourse(entite.Course.mapCourse(rs));
 
     //     Round r = new Round();
      //    r = entite.Round.mapRound(rs);
     //     r = new entite.Round().mapRound(rs,c); // mod 19-02-2020 pour générer ZonedDateTime
          ecl.setRound(new entite.Round().mapRound(rs,c));
 
     //     Inscription i = new Inscription();
     //     i = entite.Inscription.mapInscription(rs);  
          ecl.setInscriptionNew(entite.Inscription.mapInscription(rs));

     //     Tee t = new Tee();
     //     t = entite.Tee.mapTee(rs);
          ecl.setTee(entite.Tee.mapTee(rs));
          
      liste.add(ecl);
                }
      LOG.info("exiting FindSlopeRating with liste = " + liste.toString());
      return liste;
            } catch (SQLException e) {
                String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            } catch (Exception ex) {
                LOG.error("Exception ! " + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return null;
            } finally {
                //   DBConnection.closeQuietly(conn, null, rs, ps);
                DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
            }
        } else {
            //     LOG.debug("escaped to listPlayed repetition with lazy loading");
            return liste;  //plusieurs fois ??
        }
    } //end method

    public static List<ECourseList> getListe() {
        return liste;
    }
    public static void setListe(List<ECourseList> liste) {
        FindSlopeRating.liste = liste;
    }

    public static void main(String[] args) throws SQLException, Exception{

        Connection conn = new DBConnection().getConnection();
        Player player = new Player();
        player.setIdplayer(324713);
        Round round = new Round();
        round.setIdround(449);
        List<ECourseList> res = new FindSlopeRating().find(player, round, conn);
            LOG.info("main - after res = " + res.toString());
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main
} //end Class