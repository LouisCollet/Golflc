package calc;

import entite.ECourseList;
import entite.Player;
import entite.Round;
import find.FindHandicap;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CalcStablefordPlayingHandicap implements interfaces.Log 
{
//private static int round; modifié 15-0-8-2018 supprimé variables de classe inutiles

    int handicap_strokes;
  //  double par;
  //  int csa;
  //  int holes;
    
 public String [] getPlayingHcp(final Connection conn, final Player player, final Round round) throws SQLException, Exception
{
     LOG.info("very first entering getPlayingHcp !");
    
  //  int starthole;
  //  String qualifying = "";
   // Short teeClubHandicap = 0;
  //  List<StablefordResult> stb = null;
    String[] array_return_error = new String [3];  
try{   //  List<StablefordResult> listeStb;
   
    LOG.info("entering getPlayingHcp !");
    LOG.info("player = " + player.toString());
    LOG.info("round = " + round.toString());
 
    LOG.info("game " + round.getRoundGame() );
    find.FindSlopeRating fsr = new find.FindSlopeRating();
    List<ECourseList> stb = fsr.getSlopeRating(player, round, conn);
        LOG.info("List StablefordResult = " + Arrays.toString(stb.toArray() ) );
 //    listeStb = stb.subList(0, 1);
     
    FindHandicap fh = new FindHandicap (); // mod 14-08-2018
    double player_hcp = fh.findPlayerHandicap(player, round, conn);
 //   double player_hcp = find.FindHandicap.findPlayerHandicap(player, round, conn);
        LOG.info("OKOK player_hcp = " + player_hcp);
    double slope = stb.get(0).Etee.getTeeSlope(); // new
        LOG.info("OKOK slope = " + slope);
    BigDecimal bd_rating = stb.get(0).Etee.getTeeRating(); //new
    double rating = bd_rating.doubleValue(); //turn the BigDecimal object into a double
        LOG.info("OKOK rating = " + rating);
    double par = stb.get(0).Ecourse.getCoursePar();
        LOG.info("OKOK par = " + par);
    int csa = stb.get(0).Eround.getRoundCBA();
        LOG.info("OKOK csa = " + csa);
    int holes = stb.get(0).Eround.getRoundHoles();
        LOG.info("OKOK holes = " + holes);
    String qualifying = stb.get(0).Eround.getRoundQualifying();
        LOG.info("OKOK qualifying = " + qualifying);
        /// bug !!
    Short starthole = stb.get(0).Eround.getRoundStart();    // hole de départ : 1 ou 10 (pour les parcours 9 trous)
        LOG.info("OKOK starthole = " + starthole);
 
 //   Short teeClubHandicap = stb.get(0).Etee.getTeeClubHandicap();     
    int teeClubHandicap = stb.get(0).Etee.getTeeClubHandicap();
        LOG.info("teeClubHandicap = " +  teeClubHandicap);

    
      LOG.info("Elements de calcul du handicap : slope = " + slope +
              ", rating = " + rating + ", player hcp = " + player_hcp +
              ", par = " + par + ", csa = " + csa + ", holes = " + holes
      + ", teeclubhandicap = " + teeClubHandicap);
// -----------------
  //    handicap_strokes = (int) Math.round( (player_hcp * (slope/113.0) ) + rating - par);
// -----------------
   //     LOG.info("-- old system full (18 holes) playing hcp  = " + handicap_strokes );
 //     int playing_hcp_calculated = calculatePlayingHcp(player_hcp, slope, rating, par);
      handicap_strokes = calculatePlayingHcp(player_hcp, slope, rating, par, teeClubHandicap);
      
        LOG.info("-- calculated  (18 holes) playing hcp  = " + handicap_strokes );
      
    if (holes == 9)
       {
            handicap_strokes = Math.round(handicap_strokes / 2);
           LOG.info("-- reduced (9 holes) playing hcp  = " + handicap_strokes );
       }
      
    array_return_error[0]= "NO ERROR";
    array_return_error[1]= Integer.toString(handicap_strokes);
    array_return_error[2]= "";
    return array_return_error;
    
} catch(final SQLException sqle){
       LOG.error(" -- SQL Exception by LC mod = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " + sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
       array_return_error[0]= "ERROR";
       array_return_error[1]= "GetStoredListPoints" + sqle.getMessage();
       array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " SQLState =  " + sqle.getSQLState();
               LOG.error("-- Â£Â£Â£ Exception stored pro : " + sqle.toString());
       //throw(sqle);
       return array_return_error;

        //LOG.error("sql error return msg =  = " + session.getAttribute("return_msg") );
} // end catch
catch(final Exception e)
{       LOG.error(" -- Exception by LC = " + e.getMessage());
       array_return_error[0]= "ERROR";
       array_return_error[1]= e.getMessage();
       array_return_error[2]= "" ;
       //throw(e);
       return array_return_error;
} // end catch

finally
{

 // LOG.info("finally : end of getStoredList ");

}
} //end getPlayingHandicap
    
//public int calculatePlayingHcp (double exact_hcp, double slope, double rating, double par, short clubhandicap)
        public int calculatePlayingHcp (double exact_hcp, double slope, double rating, double par, int clubhandicap)
{
    int addhcp = 0;
     if(exact_hcp == 54) //new 05/07/2016
        { LOG.info("Player handicap = 54 - reduced to 36 for calculations");
            exact_hcp = 36;
            addhcp = clubhandicap;
        }

int playing_hcp = (int) Math.round( (exact_hcp * (slope/113.0) ) + (rating-par) );
    playing_hcp = playing_hcp + addhcp;  // pour les handicap 54 only
    LOG.info("new system calculated playing hcp = " + playing_hcp);
    return playing_hcp;
}
} // end class