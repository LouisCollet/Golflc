package find;

import entite.Player;
import entite.Round;
import entite.StablefordResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

/**
 *
 * @author collet
 */
public class FindSlopeRating implements interfaces.Log {

    private static List<StablefordResult> liste = null;
final private static String ClassName = Thread.currentThread().getStackTrace()[1].getClassName(); 
    
    public List<StablefordResult> getSlopeRating(final Player player, final Round round, final Connection conn) throws SQLException // pour un joueur particulier !!!
    {
        if (liste == null) {
            LOG.debug("starting getSlopeRating(), Player = {}", player.getIdplayer());
            LOG.debug("starting getSlopeRating(), Round = {}", round.getIdround());
            LOG.debug("starting getSlopeRating(), liste = {}", liste);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                LOG.info("starting getSlopeRating.. = ");
                String query
                        = // attention faut un espace en fin de ligne avant le " !!!!
                        " SELECT RoundDate, idround,  idplayer, "
                        + "          idcourse, CourseHoles, idclub, idtee, tee.TeeClubHandicap, player.PlayerGender, "
                        + "          round.RoundCSA, round.RoundQualifying, round.RoundHoles, roundstart, roundcompetition, "
                        + "              player_has_round.InscriptionTeeStart, teegender, coursepar,  teeslope, teerating, teestart, "
                        + "              roundgame,CourseName,ClubName "
                        + " FROM round "
                        + " JOIN player "
                        + "	ON player.idplayer = ? "
                        + " JOIN course "
                        + "	ON round.course_idcourse = course.idcourse "
                        + "	AND round.idround = ? "
                        + " JOIN club  "
                        + "	ON club.idclub = course.club_idclub "
                        + " JOIN player_has_round "
                        + "   ON player_has_round.round_idround = round.idround "
                        + "   AND player_has_round.player_idplayer = player.idplayer "
                        + " JOIN tee "
                        + " 	ON tee.course_idcourse = course.idcourse "
                        + "  	 AND tee.TeeGender = player.PlayerGender  "
                        + " 	 AND tee.TeeStart = player_has_round.InscriptionTeeStart ";

                ps = conn.prepareStatement(query);
                ps.setInt(1, player.getIdplayer());
                ps.setInt(2, round.getIdround());
                utils.LCUtil.logps(ps);
                rs = ps.executeQuery();
                rs.last(); //on récupère le numéro de la ligne
                LOG.info("ResultSet getPlayedList has " + rs.getRow() + " lines.");
                if (rs.getRow() != 1) {
                    String msg = "-- Empty or too much Result(s) in Table for FindSlopeRating !! ";
                    LOG.error(msg);
                    LCUtil.showMessageFatal(msg);
                    throw new Exception(msg);
                }
                rs.beforeFirst(); //on replace le curseur avant la première ligne
                liste = new ArrayList<>();
                //LOG.info("just before while ! ");
                while (rs.next()) {
                    //LOG.info("just after while ! ");
                    StablefordResult sr = new StablefordResult(); // liste pour sélectionner un round

                    sr.setTeeSlope(rs.getShort("teeslope"));
                    sr.setTeeRating(rs.getBigDecimal("teerating"));
                    sr.setIdtee(rs.getInt("idtee"));
                    sr.setTeeClubHandicap(rs.getShort("TeeClubHandicap"));  //new 05/07/2016
                    sr.setTeeStart(rs.getString("teestart"));
                    sr.setIdcourse(rs.getInt("idcourse"));
                    sr.setCoursePar(rs.getShort("coursepar"));
                    sr.setCourseName(rs.getString("coursename"));
                    sr.setCourseHoles(rs.getShort("CourseHoles"));
                    sr.setPlayerGender(rs.getString("playergender"));
                    sr.setRoundCBA(rs.getShort("roundcsa"));
                    sr.setRoundQualifying(rs.getString("roundqualifying"));
                    sr.setRoundHoles(rs.getShort("roundholes"));
                    sr.setIdround(rs.getInt("idround")); // new 22/06/2017
                    //   sr.setRoundDate(rs.getDate("rounddate"));
         //           sr.setRoundDate(rs.getTimestamp("roundDate")); // mod 02/08/2015 avec minutes
                    
                    java.util.Date d = rs.getTimestamp("roundDate");
                    LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    sr.setRoundDate(date);
                    
                    
                    sr.setRoundStart(rs.getShort("roundstart"));
                    sr.setRoundCompetition(rs.getString("roundcompetition"));
                    sr.setRoundGame(rs.getString("roundgame"));
                    sr.setIdclub(rs.getInt("idclub"));
                    sr.setClubName(rs.getString("clubname"));
                    sr.setInscriptionTeeStart(rs.getString("InscriptionTeeStart"));
                    liste.add(sr);
                }
                LOG.info("liste = " + liste.toString());
                return liste;
            } catch (SQLException e) {
                String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
                        + ", ErrorCode = " + e.getErrorCode();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            } catch (NullPointerException npe) {
                LOG.error("NullPointerException in FindSlopeRating) " + npe);
                LCUtil.showMessageFatal("Exception = " + npe.toString());
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

    public static List<StablefordResult> getListe() {
        return liste;
    }

    public static void setListe(List<StablefordResult> liste) {
        FindSlopeRating.liste = liste;
    }

    public static void main(String[] args) throws SQLException, Exception // testing purposes
    {
 //       LOG.info("Input main = " + s);
        DBConnection dbc = new DBConnection();
        Connection conn = dbc.getConnection();
        Player player = new Player();
        Round round = new Round();
        player.setIdplayer(324713);
        round.setIdround(260);
        FindSlopeRating fsr = new FindSlopeRating();
        List<StablefordResult> res = fsr.getSlopeRating(player, round, conn);
        LOG.info("main - after");
//for (int x: par )
//        LOG.info(x + ",");
        DBConnection.closeQuietly(conn, null, null, null);

    }// end main

} //end Class
