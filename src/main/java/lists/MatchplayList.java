package lists;

import entite.Matchplay;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;
/**
 *
 * @author collet
 */
public class MatchplayList implements interfaces.Log{
      private static List<Matchplay> liste = null;

public List<Matchplay> getList(final Connection conn, final String formula) throws SQLException
//public List<Matchplay> getMatchplayList(final Connection conn) throws SQLException
{
  //  

if(liste == null)
{
        LOG.debug("starting MatchplayScrambleList with formula = " + formula);
    PreparedStatement ps = null;
    ResultSet rs = null;
try
{
   final String query =     // attention faut un espace en fin de ligne avant le " !!!!
   " SELECT" +
"          idround, roundgame, club.idclub, club.ClubName,  course.idcourse, course.CourseName, round.RoundDate, RoundName" +
"          FROM round" +
"          JOIN course" +
"               ON course.idcourse = round.course_idcourse" +
"           JOIN club" +
"               ON club.idclub = course.club_idclub" +
"           WHERE substring(roundgame,1,3)= ? " + // was 'MP_'
"           ORDER by rounddate desc"
     ;
 ///       LOG.debug("round = " + in_round); //.getIdround()) ;
    ps = conn.prepareStatement(query);
    ps.setString(1, formula.toUpperCase() );
        utils.LCUtil.logps(ps);
    rs =  ps.executeQuery();
        rs.last(); //on récupère le numéro de la ligne
            LOG.debug("ResultSet MatchplayScrambleList has " + rs.getRow() + " lines.");
     // a faire : valider qu'il y a 2 ou 4 lignes        

        liste = new ArrayList<>();
          //LOG.debug("just before while ! ");
	while(rs.next())
        {
		//LOG.debug("just after while ! ");
           Matchplay mp = new Matchplay(); // liste pour sélectionner un round
           mp.setIdround(rs.getInt("idround") );
           mp.setRoundDate(rs.getTimestamp("roundDate") ); // mod 10/11/2014 avec minutes
   //        mp.setIdplayer(rs.getInt("idplayer") );
   //        mp.setPlayerLastName(rs.getString("playerLastname") );
   //        mp.setPlayerFirstName(rs.getString("playerfirstname") );
           mp.setIdclub(rs.getInt("idclub") );
           mp.setClubName(rs.getString("clubName") );
    //       mp.setPlayerhasroundTeam(rs.getString("team"));
    //       mp.setPlayerhasroundPlayerNumber(rs.getString("playernumber"));
           mp.setIdcourse(rs.getInt("idcourse"));
           mp.setCourseName(rs.getString("CourseName") );
           mp.setRoundName(rs.getString("RoundName") );
           mp.setRoundGame(rs.getString("roundgame"));
    
 //          mp.setRoundName(rs.getString("CourseName"));
 //          mp.setRoundNameName(rs.getString("compet"));
  //         mp.setRoundNameDay(rs.getString("day"));
  //         mp.setRoundNameMatch(rs.getString("_match"));
                  //LOG.debug("inside while : " + mp.toString());
			//store all data into a List
	liste.add(mp);
        
	}
        LOG.debug("liste {} = {} ", formula, Arrays.deepToString(liste.toArray()) );
        
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

    public static List<Matchplay> getListe() {
        return liste;
    }

    public static void setListe(List<Matchplay> liste) {
        MatchplayList.liste = liste;
    }


   // void main() throws SQLException // testing purposes
     public static void main(Connection c) throws SQLException // testing purposes
{
//Connection con = DBConnection.getConnection();
 LOG.debug("entering main of MatchplayList1");
////    Round round = new Round(); 
//player.setIdplayer(324713);
   LOG.debug("entering main of MatchplayList2");
////  round.setIdround(220);
  LOG.debug("entering main of MatchplayList3");
/////List<Matchplay> li = MatchplayList.getMatchplayList(220, c);
//LOG.debug("liste matchplay from main = " + li.toString());
//for (int x: par )
//        LOG.debug(x + ",");
//DBConnection.closeQuietly(con, null, null, null);
}// end main

} // end Class
