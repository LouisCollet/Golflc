package calc;

import entite.ECourseList;
import entite.Handicap;
import entite.Player;
import entite.Round;
import entite.Tee;
import find.FindHandicap;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;

public class CalcStablefordPlayingHandicap implements interfaces.Log{

 public int calc(final Connection conn, final Player player, final Round round) throws SQLException, Exception{

try{
    LOG.info("entering CalcStablefordPlayingHandicap.calc !");
    LOG.info("with player = " + player.toString());
    LOG.info("with round = " + round.toString());
    int handicap_strokes;
    int holes;
  //  LOG.info("game " + round.getRoundGame() );
    List<ECourseList> stb = new find.FindSlopeRating().find(player, round, conn);
    // tester erreur
        LOG.info("List FindSlopeRating = " + Arrays.toString(stb.toArray() ) );
        LOG.info("ECourseList FindSlopeRating = " + stb.toString() );
    double player_hcp = new FindHandicap ().find(player, round, conn);

        LOG.info("OKOK player_hcp = " + player_hcp);
    double slope = stb.get(0).Etee.getTeeSlope();
        LOG.info("OKOK slope = " + slope);
    BigDecimal bd_rating = stb.get(0).Etee.getTeeRating();
    double rating = bd_rating.doubleValue(); //turn the BigDecimal object into a double
        LOG.info("OKOK rating = " + rating);
        
        // mod 07-04-2019
 //   double par = stb.get(0).Ecourse.getCoursePar();
    double par = stb.get(0).Etee.getTeePar(); // new 07-04-2019
        LOG.info("OKOK par = " + par);
    int csa = stb.get(0).Eround.getRoundCBA(); // obsolete, maintenu pour historique
        LOG.info("OKOK csa = " + csa);
    // à modifier = utiliser playedholes 01-18    
    holes = stb.get(0).Eround.getRoundHoles();
        LOG.info("OKOK holes = " + holes);
        
        
    String qualifying = stb.get(0).Eround.getRoundQualifying();
        LOG.info("OKOK qualifying = " + qualifying);
    Short starthole = stb.get(0).Eround.getRoundStart();    // hole de départ : 1 ou 10 (pour les parcours 9 trous)
        LOG.info("OKOK starthole = " + starthole);
 //   Short teeClubHandicap = stb.get(0).Etee.getTeeClubHandicap();     
    int teeClubHandicap = stb.get(0).Etee.getTeeClubHandicap();
        LOG.info("teeClubHandicap = " +  teeClubHandicap);
      LOG.info("Elements de calcul du handicap : slope = " + slope +
              ", rating = " + rating + ", player hcp = " + player_hcp +
              ", par = " + par + ", csa = " + csa + ", holes = " + holes
      + ", teeclubhandicap = " + teeClubHandicap);
  //     handicap_strokes = calculatePlayingHcp(player_hcp, slope, rating, par, teeClubHandicap, round);
        Handicap handicap = new Handicap ();
        handicap.setHandicapPlayer(BigDecimal.valueOf(player_hcp));
        Tee tee = stb.get(0).Etee;
    LOG.info("handicap = " + handicap.toString());
    LOG.info("tee = " + tee.toString());
    LOG.info("round = " + round.toString());

      handicap_strokes = calculatePlayingHcp(conn, handicap ,tee, round);

        LOG.info("-- calculated playing hcp  = " + handicap_strokes );
 /*   modified 06-04-2019  
    if (holes == 9){
            handicap_strokes = Math.round(handicap_strokes / 2);
           LOG.info("-- reduced (9 holes) playing hcp  = " + handicap_strokes );
       }
*/      
  //  array_return_error[0]= "NO ERROR";
 //   array_return_error[1]= Integer.toString(handicap_strokes);
 //   array_return_error[2]= "";
  //  return array_return_error;
  return handicap_strokes;
    
} catch(final SQLException sqle){
       LOG.error(" -- SQL Exception by LC mod = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " + sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
   //    array_return_error[0]= "ERROR";
   //    array_return_error[1]= "CalcStablefordPlayingHandicap" + sqle.getMessage();
   //    array_return_error[2]= "ErrorCode = " + sqle.getErrorCode() + " SQLState =  " + sqle.getSQLState();
               LOG.error("-- Â£Â£Â£ Exception CalcStablefordPlayingHandicap : " + sqle.toString());
       //throw(sqle);
       return 999;

        //LOG.error("sql error return msg =  = " + session.getAttribute("return_msg") );
} // end catch
catch(final Exception e)
{       LOG.error(" -- Exception by LC = " + e.getMessage());
   //    array_return_error[0]= "ERROR";
   //    array_return_error[1]= e.getMessage();
   //    array_return_error[2]= "" ;
       //throw(e);
       return 999;
} // end catch

finally{}
} //end getPlayingHandicap

 // public int calculatePlayingHcp (double exact_hcp, double slope, double rating, double par,
 //               int clubhandicap, Round round){
   public int calculatePlayingHcp (Connection conn, Handicap handicap, Tee tee, Round round) throws Exception{
        LOG.info("entering calculatePlayingHcp");
    double exact_hcp = handicap.getHandicapPlayer().doubleValue();//turn the BigDecimal object into a double
        LOG.info("exact handicap = " + exact_hcp);
    double slope = tee.getTeeSlope();
        LOG.info("slope = " + slope);
    double rating = tee.getTeeRating().doubleValue(); //turn the BigDecimal object into a double
        LOG.info("rating = " + tee.getTeeRating());
    double par = tee.getTeePar();
        LOG.info("par = " + tee.getTeePar());
    int category = 0;   // de 1 à 5 soit 1-5
    int nholes = round.getRoundHoles();
        LOG.info("holes = " + nholes);
    int playing_hcp = 0;
    
 //   if(exact_hcp == 54) //new 05/07/2016
    if(exact_hcp > 36 && exact_hcp < 55){ // new 01/05/2019 Club Handicap
         LOG.info("Player category 6 - Club Handicap");
         category = 6;
    }else{
          LOG.info("Player category 1-5");
          category = 15;
    }
  //  LOG.info("clubhandicap = " + clubhandicap);
  if(category == 15 ){ // catégories de 1 à 5
      LOG.info("calculating playing hcp for categories 1 to 5 = "); // + playing_hcp);
      
    if(nholes == 18){
        playing_hcp = (int) Math.round( (exact_hcp * (slope/113.0) ) + (rating-par) );
        LOG.info("calculated new playing hcp for categories 1 to 5, 18 holes = "); // + playing_hcp);
    }else{ // 9 holes
        playing_hcp = (int) Math.round( (exact_hcp*(slope/113.0))/2 + ((rating/2) - par) );
        LOG.info("calculated new playing hcp for categories 1 to 5, 18 holes = "); // + playing_hcp);
    }
        
    return playing_hcp;
  } // end category 1-5

   if(category == 6 ){ // catégory 6
    // il faut calculer le playing handicap differential
    // strokes d'un hcp 36 du tee moins 36 pour 18 trous
    // pour 9 trous c'est moins 18
    int c6 = calcHandicapDifferential(conn, tee, round);
    LOG.info(" base differential = " + c6);
    if(nholes == 18){
        int differential = c6 - 36;
        LOG.info("differential 18 holes = " + c6 + " - 36 = " + differential);
        playing_hcp = (int) (exact_hcp + differential);
        LOG.info("playing_hcp = exact_hcp + differential = " + exact_hcp + "" + differential);
    }else{ // 9 holes
        int differential = c6 - 18; // à modifier
        playing_hcp = (int) Math.round( (exact_hcp*(slope/113.0))/2 + ((rating/2) - par) );
    }
    LOG.info("calculated new playing hcp for category 6 = " + playing_hcp);
} //end category 6



     LOG.info("hcp = " + playing_hcp);
//LOG.info("first part = " + Math.round( (exact_hcp*(slope/113.0))/2) );
//LOG.info("second part = " + Math.round( rating/2 - par));

//    playing_hcp = playing_hcp + addhcp;  // pour les handicap 54 only
        LOG.info("new system calculated playing hcp = " + playing_hcp);
    return playing_hcp;
} // end method
  
   public int calcHandicapDifferential(Connection conn, Tee tee, Round round) throws Exception{
try{
// chercher le gender dans tee
// on utilise un player fictif qui a 36 de hcp
// 363636 pour les M
// 363637 pour les L
// on &appelle
   LOG.info("entering CalcHandicapDifferential.calc !");
   LOG.info("with tee = " + tee);
   LOG.info("with round = " + round);
   Player p = new Player();
   p.setIdplayer(363636); // for M !!
   p = new load.LoadPlayer().load(p, conn);
 //  Round r = new Round();
 //  r = new load.LoadRound().load(round, conn);
    int hcp = new calc.CalcStablefordPlayingHandicap().calc(conn, p, round);
       LOG.info("Playing Hcp calculated pour player handicap 36 !! = " + hcp);
  // double exact_hcp = handicap.getHandicapPlayer().doubleValue();//turn the BigDecimal object into a double
   //     LOG.info("exact handicap = " + exact_hcp);
    return hcp;
   
   } catch(final SQLException sqle){
       LOG.error(" -- SQL Exception by LC mod = " + sqle.getMessage());
       LOG.error(" -- ErrorCode = " + sqle.getErrorCode() );
       LOG.error(" -- SQLSTATE =  " + sqle.getSQLState());
               LOG.error("-- Â£Â£Â£ Exception CalcStablefordPlayingHandicap : " + sqle.toString());
       return 999;
} // end catch
catch(final Exception e){
    LOG.error(" -- Exception calcHandicapDifferential = " + e.getMessage());
       return 999;
} // end catch
} //end getPlayingHandicap
   
 public static void main(String[] args) throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
        Handicap handicap = new Handicap ();
        handicap.setHandicapPlayer(BigDecimal.valueOf(54.0));
        Tee tee = new Tee();
        tee.setTeePar((short)73);
        tee.setTeeRating(BigDecimal.valueOf(70.2));
        tee.setTeeSlope((short)125);
    //    tee.setTeePar(teePar);
    
      Round round = new Round();
      round.setIdround(439);
      round = new load.LoadRound().load(round, conn);
     //   round.setRoundHoles(Short.valueOf("18"));
     //   round.setIdround(443);
        
  //      double exact_hcp = 25.9;
 //       double slope = 126.0;
 //       double rating = 69.9;
 //       double par = 36.0;
   //     int clubhandicap = 0;
        int res = new CalcStablefordPlayingHandicap().calculatePlayingHcp(conn, handicap, tee,round);

            LOG.info("main - playing handicap calculated = " + res);
        res = new CalcStablefordPlayingHandicap().calcHandicapDifferential(conn, tee, round);
            LOG.info("main - playing handicap differential calculated = " + res);
     DBConnection.closeQuietly(conn, null, null, null);

    }// end main      
        
        
} // end class