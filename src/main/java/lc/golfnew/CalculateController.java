
package lc.golfnew;

import calc.CalcHandicap;
import calc.CalcScramblePlayingHandicap;
import calc.GolfCalc;
import create.CreateHandicap;
import create.CreateScoreSqueleton;
import entite.Course;
import entite.Handicap;
import entite.Player;
import entite.PlayingHcp;
import entite.Round;
import find.FindHandicap;
import java.io.Serializable;
import static java.lang.Integer.parseInt;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import modify.ModifyScore;
import utils.*;

@Named("calcC")
@SessionScoped
public class CalculateController implements Serializable, interfaces.GolfInterface, interfaces.Log{
    private static String list[];
    private static Player pl;
    private static List<Player> listPlayers = null;
        
public CalculateController(){
    // empty constructor
};

 public String[] calculate(final Player player, final Round round, final Course course
      //   final PlayerHasRound phr
         , final Connection conn)
        throws SQLException, Exception{
 
    LOG.info(" -- Start of calculate Controller");
    LOG.info(" -- Player : " + player.toString() );
    LOG.info(" -- Round : " + round.toString() );
    LOG.info(" -- Course : " + course.toString() );
     
    String operation_card = null;
    int holes = round.getRoundHoles();
      LOG.info(" -- Calculate Controller for Holes = " + holes);
try{
  //  int player.getIdplayer() = player.getIdplayer();
        LOG.info(" -- Calculate Controller : Player = " + player.getIdplayer());
 //   intround.getIdround() = round.getIdround();
        LOG.info(" -- Calculate Controller : Round = " + round.getIdround());
  //  String game = round.getRoundGame();
        LOG.info(" -- Calculate Controller : Game = " + round.getRoundGame());
 //   String round.getRoundCompetition() = round.getRoundCompetition();
        LOG.info(" -- Calculate Controller : Competition = " + round.getRoundCompetition());
 //   String qualifying = round.getRoundQualifying() ;
        LOG.info(" -- Calculate Controller : qualifying = " + round.getRoundQualifying());
    int in_totalpar = course.getCoursePar();
        LOG.info(" -- Calculate Controller : Par = " + in_totalpar);

  //      find.FindCountScore fcs = new find.FindCountScore();
       int rows = new find.FindCountScore().getCountScore(conn, player, round, "rows");
       if (rows == 99){
           LOG.error("Fatal error in getcountscore/count rows");
           throw new Exception(" -- Fatal error in getCountStore, score = " + rows);
       }
       if (rows == 0){ // le score n'est pas encore enregistré
           operation_card = "empty";
           LOG.info(" -- Calculate Controller : empty forced  ! ");
       }else{
           operation_card = "complete";
           LOG.info(" -- Calculate Controller : complete forced ! ");
       }

//call # 2  execute stored pro : getParameter

    LOG.info("game = " + round.getRoundGame()); 
int handicap_strokes = 0;
if("STABLEFORD".equals(round.getRoundGame()) )
    {       LOG.info("gameType is STABLEFORD");
//        calc.CalcStablefordPlayingHandicap csph = new calc.CalcStablefordPlayingHandicap();
         //   LOG.info("before getPlayingHcp player = " + player.toString());
         //   LOG.info("before getPlayingHcp round = " + round.toString());
        list = new calc.CalcStablefordPlayingHandicap().getPlayingHcp(conn, player, round);
            LOG.info(" -- Result PlayingHandicap = " + Arrays.deepToString(list) );
        handicap_strokes = parseInt(list[1]);
            LOG.info(" -- PlayingHandicap Stableford = " + handicap_strokes );
    }
if(Round.GameType.SCRAMBLE.toString().equals(round.getRoundGame()))
// old if("SCRAMBLE".equals(round.getRoundGame()) )
{   LOG.info("gameType is SCRAMBLE");
         LOG.info("on cherche le nombre de joueurs déjà inscrits"); 
 //        lists.RoundPlayersList spl = new lists.RoundPlayersList();
     listPlayers =  new lists.RoundPlayersList().listAllParticipants(round, conn);
        LOG.info("after lists.RoundPlayersList, lp =  "); // + Arrays.deepToString(lp));
  if(listPlayers != null){
        LOG.info("nombre de players stableford = lp size = " + listPlayers.size());
      PlayingHcp phcp = new PlayingHcp();
     double[] ad = {0.00, 0.00, 0.00, 0.00};
     // on cherche les handicaps des joueurs, stockage dans array double
     for(int i=0; i < listPlayers.size() ; i++)
     {  int idp = listPlayers.get(i).getIdplayer();
        LOG.debug(" -- item in for idplayer = " + idp );
        pl = new Player();
        pl.setIdplayer(idp);
//            LOG.info("line 11, id player = " + pl.getIdplayer() );
        find.FindHandicap fh = new FindHandicap (); //new 14-08-2018
        double player_hcp = fh.findPlayerHandicap(pl, round, conn);
   //     double player_hcp = find.FindHandicap.findPlayerHandicap(pl, round, conn);
            LOG.info("handicap from find.FindHandicap = " + player_hcp);
        ad[i] = player_hcp; 
 //         LOG.info("line 023 + player hcp = " + ad[i]);
 //    for(double speed : ad) {
 //           LOG.info("Print ad element = " + speed);
 //       }
     } // end for 
     
     // ici il faut transformer l'array double en array Double et faire le set
    phcp.setHcpScr(utils.LCUtil.doubleArrayToDoubleArray(ad));
//    LOG.info("line B");
     LOG.info("PlayingHcp.HCPSCCR = " + Arrays.deepToString(phcp.getHcpScr()));
 // calcul du handicap de l'équipe    
 //    calc.CalcScramblePlayingHandicap csph = new CalcScramblePlayingHandicap();
     handicap_strokes = new CalcScramblePlayingHandicap().getScramblePlayingHcp(phcp, listPlayers.size());
       LOG.info(" handicap_strokes  = " + handicap_strokes);

  } //end if
} //end SCRAMBLE
    
//    if(list[0].equals("ERROR") )
 //       { LOG.error(" -- Fatal error in stored List = " + Arrays.deepToString(list) );
 //            throw new Exception(" -- Fatal error in getStoredList, round = " + round + " - " + list[1]);
 //       }
//LOG.info("line 00");
    // new 11/01/2012
    // on adapte la longueur de l'array points aux nombre de holes (9 ou 18)
    // points.length sera utilisé dans la suite pour connaître le nombre de holes
     GolfMySQL gmsp = new GolfMySQL();
     int [][] points = gmsp.createArrayPoints(holes);// create array length = 9 or 18
       LOG.info(" -- array points in calculatecontroller = " + Arrays.deepToString(points) );

//call # 3 modified 04 05 2013// load array points from table myPoints
  //  LOG.info("line 01");
 //   load.LoadPointsArray lpa= new load.LoadPointsArray();
    points = new load.LoadPointsArray().LoadPointsArray(conn, points, player, round); // mod 22/06/2014
        // complete array points à partir database
        LOG.info   (" -- we are back from getPointsArray with filled points ! = " + Arrays.deepToString(points));
    // ici il faut recopier points vers golfcalc et golfmysql !!
        gmsp.setPoints(points);
        calc.GolfCalc.setPoints(points);
        
   // complete l'array par les strokes
if(operation_card.equals("complete") ){
  //  load.LoadStrokesArray lsa = new load.LoadStrokesArray();
    points =  new load.LoadStrokesArray().LoadStrokesArray(conn, points, player, round);  // mod 22/06/2014
}
        LOG.info   (" -- we are back from LoadStrokesArray with strokes filled in = " + Arrays.deepToString(points));
        LOG.info   (" -- # Holes = " + holes);

 //call # 6 // complète array points with extra strokes
   
    list = calc.GolfCalc.setArrayExtraStrokes(points, handicap_strokes); //, holes);
        LOG.info(" -- Result setArrayExtrasStroke = OK");
    if(list[0].equals("ERROR") )
        { LOG.error(" -- Fatal error in setExtraStrokes = " + Arrays.deepToString(list) );
          throw new Exception(" -- Fatal error in setExtraStrokes = " + list[1]);
        }

//call # 7  complète Table score ExtraStrokes pour faire une EMPTY scorecard (empty only !!
// modifié 24/11/2012

    points = calc.GolfCalc.trfPoints(); // transfère l'array point
        LOG.info(" -- we are back from setArrayExtraStrokes with filled points ! = "
                + Arrays.deepToString(points));

    if(operation_card.equals("empty") ){
            LOG.info(" -- getCountScore/sum scorestroke = 0 - we generate score squeleton");
        CreateScoreSqueleton css = new CreateScoreSqueleton();
        list = css.setScoreSqueleton(conn, points, player.getIdplayer(),round.getIdround(), holes);
        if(list[0].equals("ERROR") )
            { LOG.error(" -- Fatal error in setScoreSqueleton = " + Arrays.deepToString(list) );
               throw new Exception(" -- Fatal error in setScoreSqueleton = " + list[1]); //stoppe exécution
            }
    }
//call # 8  // complete table points - calcul des points comme pros/hcp=0
    find.FindCountScore siup = new find.FindCountScore();
    int i = siup.getCountScore(conn, player,round, "sum");
// si i > 0 : les strokes sont introduits
if(operation_card.equals("complete") && (i > 0)){
    list = calc.GolfCalc.setArrayPoints(points); // a besoin des strokes dans l'array points !!
        LOG.info(" -- Result setPoints=  OK");
    if(list[0].equals("ERROR") )
        {  LOG.error(" -- Fatal error in " + list[1] + Arrays.deepToString(list) );
           throw new Exception(" -- Fatal error in " + list[1]); //stoppe exécution, génial
        }
}
    points = calc.GolfCalc.trfPoints(); // transfère l'array point
        LOG.info(" -- we are back from setArrayPoints with filled points ! = " + NEWLINE 
                + Arrays.deepToString(points));
        
 //* tout se fait à partir de setScore !!
//call # 9 complète la table Score à partir array Points
if(operation_card.equals("complete") ){    // only for complete card
 //   modify.ModifyScore ms = new ModifyScore();
    list = new ModifyScore().updateScore(conn, points, player.getIdplayer(),round.getIdround());  // mod 21/06/20115
    // update de la table score à partir array points
    if(list[0].equals("ERROR") )
        {  LOG.error(" -- Fatal error in updateScore = " + Arrays.deepToString(list) );
           throw new Exception(" -- Fatal error in updateScore = " + list[1] );
    }else{
            LOG.info(" -- result updateScore Stored = " + Arrays.deepToString(list));
        }
}

//call # 10    totalise les points du round, hole par hole, only for complete card
int round_result_stableford = 0;
if(operation_card.equals("complete") )
{
        round_result_stableford = calc.GolfCalc.getRoundStablefordResult(points); //(holes);
        LOG.info   (" -- round_result_stableford = " + round_result_stableford);
 
//call # 14    getCSA : points supplémentaires en fonction difficulté round
 //   GolfMySQL gmsq = new GolfMySQL();
    int csa = new GolfMySQL().getCSA(); // get CSA
        LOG.info(" -- CSA = " + csa);
    round_result_stableford = round_result_stableford - csa;   // mod 5/10/2013  CSA peut -être négatif, alors addition !
        LOG.info(" -- round result stableford with CSA = " + round_result_stableford);
}

int round_result_zwanzeurs = 0;
int round_result_greenshirt = 0;
//LOG.info(" type of game = " + CourseController.getRoundGame() );
if(operation_card.equals("complete") && (round.getRoundGame().equals("ZWANZEURS"))){ // et si 
  round_result_zwanzeurs = GolfCalc.getRoundZwanzeursResult(points, in_totalpar, handicap_strokes); //(holes); aussi le handicap
     LOG.info   (" -- round_result_zwanzeurs = " + round_result_zwanzeurs);
     LOG.info   (" -- round.getRoundCompetition() = " + round.getRoundCompetition());
  round_result_greenshirt = GolfCalc.getRoundGreenshirtResult(points, handicap_strokes, round.getRoundCompetition()); //(holes); aussi le handicap
     LOG.info   (" -- round_result_greenshirt = " + round_result_greenshirt);
}

//call # 11     insert result in table=Round, only for complete card
if(operation_card.equals("complete") ){
    LOG.info("before setStoredResult");
  //  create.CreateResult cr = new create.CreateResult();
    list = new create.CreateResult().setStoredResult (conn, player.getIdplayer(),round.getIdround(), round_result_stableford,
            round_result_zwanzeurs, round_result_greenshirt );
            LOG.info("list setStoreResult = " + Arrays.toString(list));
    
    if(list[0].equals("ERROR") )
        {  LOG.error(" -- Error inserting Round result : " + Arrays.deepToString(list) );
           throw new Exception(" -- throw new Exception : Error inserting Round result : " + list[1]);
    }else{
            LOG.info(" -- Round Stableford result Stored = " + round_result_stableford);
            LOG.info(" -- Round Zwanzeurs  result Stored = " + round_result_zwanzeurs);
            LOG.info(" -- Round greenshirt result Stored = " + round_result_greenshirt);
        }
}
//call # 12    Calcul du handicap après Round, only for complete card
   double calc_handicap = 0;
   double exact_handicap = 0;
if(operation_card.equals("complete") )
{
//        LOG.info(" -- Current player Handicap = " + exact_handicap + "\n");
 //   find.FindHandicap fh = new FindHandicap (); //new 14-08-2018
    exact_handicap = new FindHandicap().findPlayerHandicap(player, round, conn);
  //  exact_handicap = find.FindHandicap.findPlayerHandicap(player, round, conn);
        LOG.info(" -- Current player Handicap = " + exact_handicap + NEWLINE);
        LOG.info(" -- Qualifying = " + round.getRoundQualifying() );
  //  CalcHandicap ch = new CalcHandicap();
    calc_handicap = new CalcHandicap().getNewHandicap(round_result_stableford, exact_handicap, round); 
        String msg = " -- Calculated New Handicap = " + String.format( "%.1f", calc_handicap);
       // 
        LOG.info(msg);
   //  LCUtil.showMessageInfo(msg);
}
LOG.info(" -- Current player Handicap = " + exact_handicap + NEWLINE);
LOG.info(" -- Calculated Handicap = " + String.format("%.1f",calc_handicap));

//if( (operation_card.equals("complete"))      // pas empty scorecard
// && (calc_handicap != exact_handicap) // différence entre hcp old et calculé
// && (qualifying.equals("Y")) )        // si tour qualifying
    // ajouter ici le traitement du round counting = "equals.C" :
    // Counting: la modification des handicaps se fait uniquement à la baisse (bons scores)
    //, mais pas à la hausse (mauvais scores). 
//        {LOG.info(" -- NEW Handicap = " + calc_handicap + "\n");}

//call # 17   si nouveau handicap : lancer une stored pro

LOG.info(" -- Call 17 - Operation_card = " + operation_card); //Call 17 - new Handicap = ");
LOG.info(" -- Call 17 - calc_handicap = " + String.format( "%.1f",calc_handicap));
LOG.info(" -- Call 17 - exact_handicap = " + exact_handicap);
//    if(round.getIdround() == 274)
//        {round.setRoundQualifying("Y");
//         LOG.info(" -- round 274, qualifying forced to 'Y' = " + round.getRoundQualifying() );
//        }
LOG.info(" -- Call 17 - Qualifying() = " + round.getRoundQualifying());
// load.LoadHandicap lh = new load.LoadHandicap();
Handicap h = new load.LoadHandicap().LoadHandicap(player, round, conn);
LOG.info(" -- result loadHandicap = " + h.toString());
LOG.info(" -- handicapend = = " + h.getHandicapEnd());
LOG.info(" -- round date = " + round.getRoundDate());

//Instant instant = Instant.ofEpochMilli(h.getHandicapEnd().getTime()); // mod 18/02/2019
 //   LOG.info("instant of getHandicapEnd  = " + instant);
//LocalDateTime ldtHandicapEnd = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
 //   LOG.info("ldt HandicapEnd " + ldtHandicapEnd); 
////LocalDate ldHandicapEnd = ldtHandicapEnd.toLocalDate();
LocalDate ldHandicapEnd = utils.LCUtil.DatetoLocalDate(h.getHandicapEnd());
    LOG.info("local date HandicapEnd " + ldHandicapEnd);
LocalDate ldRoundDate = round.getRoundDate().toLocalDate();
    LOG.info("locla date RoundDate = " + ldRoundDate);

 if (!ldHandicapEnd.isEqual(ldRoundDate)) {// date de fin à la date du round = le nouveau handicap a déjà été attribué précédemment!
            LOG.info("Handicapend is NOT equal rounddate --> new handicap !!!!");  
       }else{
     LOG.info("Handicapend is equal rounddate --> no NEW handicap !!!!");
 }
//LOG.info("line 222");
    if( (operation_card.equals("complete"))
            && (calc_handicap != exact_handicap)
            && (round.getRoundQualifying().equals("Y"))
            && (!ldHandicapEnd.isEqual(ldRoundDate)) // new 27/08/2017
            ) 
    {
        LOG.info(" -- Call 17 - new Handicap = " + round.getRoundQualifying() + " exact = "
                + exact_handicap + " calculated = " + String.format( "%.1f",calc_handicap));  // Double avec 1 décimale
           create.CreateHandicap ch = new CreateHandicap();
           list = ch.create(conn, player, round, calc_handicap); // new 24/06/2014
        
        if (list[0].equals("ERROR") )
        {    LOG.error(" -- Error storing new Handicap = " + Arrays.deepToString(list) );
             throw new Exception(" -- Error storing new Handicap = " + list[1]);
        }else{ // there is a new handicap
            String msg = (" -- Congratulations/Sorry for your new handicap = " + String.format( "%.1f",calc_handicap));
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }
    }else{
        String msg = "no new handicap";
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
    }
 // end calls 16 and 17
return list;

 // end try
}catch(SQLException e){
    String msg = "-- SQLException in calculate Controller !!! " + e.toString();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
         String msg = "-- Exception in calculate Controller !!! " + e.toString();
         LOG.error(msg + NEWLINE + NEWLINE);
         LCUtil.showMessageFatal(msg);
        return null;
}finally{
      //  LOG.info("-- calculate controller finally = !!! ");
    DBConnection.closeQuietly(null, null, null, null); // new 10/12/2011
  //  return list;
}
} // end of method
} // end of class