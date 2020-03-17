package lists;

import edu.emory.mathcs.backport.java.util.Arrays;
import entite.Old_Matchplay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
/**
 *
 * @author collet
 */
public class MatchplayList implements interfaces.Log
{
      private static List<Old_Matchplay> liste = null;

public List<Old_Matchplay> getList(final Connection conn, final String formula) throws SQLException
//public List<Matchplay> getMatchplayList(final Connection conn) throws SQLException
{
  //  

if(liste == null)
{
        LOG.info("starting MatchplayScrambleList with formula = " + formula);
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
  String query =     // attention faut un espace en fin de ligne avant le " !!!!
   " SELECT" +
"          idround, roundgame, club.idclub, club.ClubName,  course.idcourse, course.CourseName, round.RoundDate, RoundCompetition" +
"          FROM round" +
"          JOIN course" +
"               ON course.idcourse = round.course_idcourse" +
"           JOIN club" +
"               ON club.idclub = course.club_idclub" +
"           WHERE substring(roundgame,1,3)= ? " + // was 'MP_'
"           ORDER by rounddate desc"
     ;
 ///       LOG.info("round = " + in_round); //.getIdround()) ;
    ps = conn.prepareStatement(query);
    ps.setString(1, formula.toUpperCase() );
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.info("ResultSet MatchplayScrambleList has " + rs.getRow() + " lines.");
     // a faire : valider qu'il y a 2 ou 4 lignes        

        rs.beforeFirst(); //on replace le curseur avant la première ligne
        liste = new ArrayList<>();
          //LOG.info("just before while ! ");
	while(rs.next())
        {
		//LOG.info("just after while ! ");
           Old_Matchplay mp = new Old_Matchplay(); // liste pour sélectionner un round
           mp.setIdround(rs.getInt("idround") );
           mp.setRoundDate(rs.getTimestamp("roundDate") ); // mod 10/11/2014 avec minutes
   //        mp.setIdplayer(rs.getInt("idplayer") );
   //        mp.setPlayerLastName(rs.getString("playerlastname") );
   //        mp.setPlayerFirstName(rs.getString("playerfirstname") );
           mp.setIdclub(rs.getInt("idclub") );
           mp.setClubName(rs.getString("clubName") );
    //       mp.setPlayerhasroundTeam(rs.getString("team"));
    //       mp.setPlayerhasroundPlayerNumber(rs.getString("playernumber"));
           mp.setIdcourse(rs.getInt("idcourse"));
           mp.setCourseName(rs.getString("CourseName") );
           mp.setRoundCompetition(rs.getString("RoundCompetition") );
           mp.setRoundGame(rs.getString("roundgame"));
     //      CourseController.round.setRoundGame(mp.getRoundGame() );
 //          mp.setRoundCompetition(rs.getString("CourseName"));
 //          mp.setRoundCompetitionName(rs.getString("compet"));
  //         mp.setRoundCompetitionDay(rs.getString("day"));
  //         mp.setRoundCompetitionMatch(rs.getString("_match"));
                  //LOG.info("inside while : " + mp.toString());
			//store all data into a List
	liste.add(mp);
        
	}
        LOG.info("liste {} = {} ", formula, Arrays.deepToString(liste.toArray()) );
        
    return liste;
}catch (SQLException e){
    String msg = "SQL Exception = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (NullPointerException npe){
    String msg = "NullPointerException in getMatchplayList()" + npe;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}catch (Exception ex){
    String msg = "Exception in getMatchplayList()" + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
     return null;
}finally{
  //  DBConnection.closeQuietly(conn, null, rs, ps);
    DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
   //      LOG.debug("escaped to getMatchplayList repetition thanks to lazy loading");
    return liste;  //not null, donc pas d'acces
}
}//end method

    public static List<Old_Matchplay> getListe() {
        return liste;
    }

    public static void setListe(List<Old_Matchplay> liste) {
        MatchplayList.liste = liste;
    }


   // public static void main(String[] args) throws SQLException // testing purposes
     public static void main(Connection c) throws SQLException // testing purposes
{
//Connection con = DBConnection.getConnection();
 LOG.info("entering main of MatchplayList1");
////    Round round = new Round(); 
//player.setIdplayer(324713);
   LOG.info("entering main of MatchplayList2");
////  round.setIdround(220);
  LOG.info("entering main of MatchplayList3");
/////List<Matchplay> li = MatchplayList.getMatchplayList(220, c);
//LOG.info("liste matchplay from main = " + li.toString());
//for (int x: par )
//        LOG.info(x + ",");
//DBConnection.closeQuietly(con, null, null, null);
}// end main

} // end Class
