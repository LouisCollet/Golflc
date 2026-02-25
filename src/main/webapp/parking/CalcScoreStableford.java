package calc;
import Controllers.LoggingUserController;
import Controllers.StablefordController;
import entite.Course;
import entite.LoggingUser;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import utils.DBConnection;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

// calclu à partir 1/10/2024 voir document Belgium WHS-update ...
public class CalcScoreStableford implements interfaces.GolfInterface {
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
  
public CalcScoreStableford(){ }
  
 public static ScoreStableford calc(final Player player, ScoreStableford score, final Round round, final Course course, final Tee tee,final Connection conn){
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    LOG.debug("... entering " + methodName);
    LOG.debug(" with ScoreStableford = " + score);
    LOG.debug(" with Round = " + round);
    LOG.debug(" with Player = " + player);
try{

    LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
    LoggingUserController.write(LocalDateTime.now().format(ZDF_TIME), "i");
    LoggingUserController.write(player.getPlayerFirstName() + " - " + player.getPlayerLastName(), "i");
    LoggingUserController.write(round.getRoundName() + " - " + round.getRoundDate().format(ZDF_TIME), "t");
       
  // new 30-06-2022 
  // input se trouve dans scoreList : on complete strokeArray from scoreList car on garde l'ancienne méthode de calcul on arrays ....
  // les calculs terminés, on transfère le résultat dans scoreList pour faire les mises à jours des tables DB
  
      int[] arr = score.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray();
           LOG.debug("arr from score = " + Arrays.toString(arr));
      // validations si un résulat = 0    
      if(utils.LCUtil.isArrayAllZeroes(arr)){
          String msg = "scoreArray is all zeroes= " + Arrays.toString(arr);
          LOG.error(msg);
          showMessageFatal(msg);
      }else{
          String msg = "scoreArray is NOT all zeroes= " + Arrays.toString(arr);
          LOG.info(msg);
 //         showMessageInfo(msg);
      }

     if(utils.LCUtil.isArrayOneZero(arr)){
        String msg = "There is (at least...) one result = 0 - correction needed !!";
          LOG.error(msg);
          showMessageFatal(msg);
          return score;
        }
// LOG.debug("satert from score = " + score.getStart()); LIST travaille en coups réels et strike array a toujours 18 de longueur
      int[] a = new int[9]; // int always has initial value of 0.
      if(round.getRoundStart() == 10){ // pour avoir une array longueur 18
          arr = IntStream.concat(IntStream.of(a), IntStream.of(arr)).toArray();
      }
      if(round.getRoundStart() == 1 && round.getRoundHoles() == 9){
          arr = IntStream.concat(IntStream.of(arr),IntStream.of(a)).toArray();
      }
      score.setStrokeArray(arr);
         LOG.debug(" array Strokes completed from scoreList for legacy calculations = "+ Arrays.toString(score.getStrokeArray()));
         LoggingUserController.write("Score Card =  " + Arrays.toString(score.getStrokeArray()));

      if(utils.LCUtil.isArrayAllZeroes(score.getParArray())){ // pris une array au hazard // tester si array = tous des 0
                 LOG.debug("handling isArrayAllZeroes");
             score = new read.ReadParAndStrokeIndex().read(conn, course, score); // from hole
 //               LOG.debug("score with par and index = " + score);
             score = new calc.CalcStablefordCourseHandicap().calc(score, player, round, tee, conn);
                LOG.debug("courseHandicap = " + score.getCourseHandicap());
                LOG.debug("HandicapIndex player = " + score.getPlayerHandicapWHS());   
             int i = new calc.CalcStablefordPlayingHandicap().calc(score, player, round);
             score.setPlayingHandicap(i);
                LOG.debug(" playingHandicap = " + score.getPlayingHandicap());   
        // changed 22-08-2023     score.setExtraArray(Controllers.StablefordController.completeWithExtraStrokes(score, round));
             score.setExtraArray(Controllers.StablefordController.completeWithExtraStrokesNew(score, round));
                LOG.debug("ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray()));
    }
// new 14-04-2025 à partir 1/10/2024
    score.setHolesNotPlayed(countHolesNotPlayed(score.getStrokeArray())); // par convention on encode -1 pour les HolesNotPlayed
          String msg = "There are holes not played = " + score.getHolesNotPlayed();
          LOG.info(msg);
          showMessageInfo(msg);
          LoggingUserController.write(msg);
    if(score.getHolesNotPlayed() > 0){
      // on a des -1 dans les strokes du trou : il faut les remplacer
         score.setStrokeArray(completeHolesNotPlayed(score));
        
          msg = "StrokeArray completed with HolesNotPlay = " + Arrays.toString(score.getStrokeArray());
          LOG.debug(msg);
          LoggingUserController.write(msg);
    }
    
    score.setStart(round.getRoundStart());
        LOG.debug("start is now = " + score.getStart());
    score.setHoles(round.getRoundHoles());
        LOG.debug("holes is now = " + score.getHoles());
    
 //new 14-04-2025 à partir 1/10/2024
    score.setStrokeArray(ajustedGrossScore(score)); // application nddb netDoubleBogeyAdjustment
       msg = "ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray());
       LOG.debug(msg);
       LoggingUserController.write(msg);
 // unchanged
 
 
 
    score.setAdjustedGrossScore(Arrays.stream(score.getStrokeArray()).sum());
        msg = " Adjusted Score Total = " + score.getAdjustedGrossScore();
        LOG.debug(msg);
        LoggingUserController.write(msg);
 // unchanged
    score.setPointsArray(calculatePoints(score));
        msg = "Calculated points are in score.getPointsArray :" + Arrays.toString(score.getPointsArray());
        LOG.debug(msg);
        LoggingUserController.write(msg);
// 09-07-2025 mettre à jour scoreList
   //     var v2 = new StablefordController().completeScoreListWithPoints(score);
   //     LOG.debug("var v2 correction 09-07-2025 = " + v2);

        
    score.setStablefordResult(Arrays.stream(score.getPointsArray()).sum());
        msg = "Stableford total points : " + score.getStablefordResult();
        LOG.debug(msg);
        LoggingUserController.write(msg);

    score.setTotalStrokes(Arrays.stream(score.getStrokeArray()).sum());
        msg = "Total Strokes (calculated) = " + score.getTotalStrokes();
        LOG.debug(msg);
        LoggingUserController.write(msg);
        
  /* si avant 1/10/2024
    if(round.getRoundHoles() == 9){  // new 15-09-2024
       int expectedScore = 17; // pour le moment forfait = 17; mais il est prévu d'individualiser le calcul voir "expected score" dans la doc 
       LoggingUserController.write("expected score","t");
       LoggingUserController.write("Expected Score = " + expectedScore);
       score.setAdjustedScore(score.getAdjustedScore() + expectedScore);
       msg = " Adjusted Score with Expected Score over 9 holes (forfait of 17) = " + score.getAdjustedScore();
       LOG.debug(msg);
     }  
     LoggingUserController.write("Adjusted Score = " + score.getAdjustedScore());
    */   

 //      LOG.debug(msg);
      score.setCourseRating(tee.getTeeRating().doubleValue()); // mod 16-09-224
      score.setSlopeRating(tee.getTeeSlope());
  //    score.setScoreParCourse(course.getCoursePar()); ?? encore utilse ??
      score.setScoreParCourse(tee.getTeePar()); 
      double sd = new calc.CalcStablefordScoreDifferential().calc(score, player, round, conn);
      score.setScoreDifferential(sd);
      score.setShowCreate(true); // afficher button create (pour obliger à faire calculate avant)
      score.setShowCalculate(false); // ne plus afficher button calculate
      score.setShowLineDifferential(true); // ?? utilisé ? afficher ligne Points   
 
      score.setStart(round.getRoundStart()); // new 09-07-2025
      score.setHoles(round.getRoundHoles()); // new 09-07-2025
      
    //    var v = new StablefordController().completeScoreListWithPointsAndStrokes(score);
    //  LOG.debug("new solution 09-07-2025= " + v.toString());
      
      score.setScoreList(new StablefordController().completeScoreListWithPoints(score));
      score.setScoreList(new StablefordController().completeScoreListWithStrokes(score));
      
        LOG.debug("CalcScoreStableford final = " + score);
      LoggingUser logging = new LoggingUser();
      logging.setLoggingIdPlayer(player.getIdplayer());
      logging.setLoggingIdRound(round.getIdround());
      logging.setLoggingType("R"); // for Round, of "H" for Handicap
      
      boolean b = new Controllers.LoggingUserController().createUpdateLoggingUser(logging);
       
      // tester ici
  return score;
 }catch (Exception e){
     LOG.debug("Exception in " + methodName + e);
     return null;
 }finally{
    //LOG.debug(" -- array = " + Arrays.deepToString(points) );
 }
} // end method calc
  public static int countHolesNotPlayed(int[] arr){
    int count = 0;
    for(int i=0;i<arr.length;i++){
  //      LOG.debug("array one zero ? " + arr[i] + " for index = " + i);
     //   LOG.debug("array one zero index =  " + i );
      if(arr[i] == -1){
          count++;
      }
    }
  return count;
} // end method 
  
  // new 15-05-2024 WHS revison 10/2024
  public static int[] ajustedGrossScore(ScoreStableford score){
        // voir Rules of Handicaping rule 3.1b page 26
    LOG.debug(" entering ajustedGrossScore with stroke = " + Arrays.toString(score.getStrokeArray()));
    LOG.debug("  with par = " + Arrays.toString(score.getParArray()));
    LOG.debug("  with extra = " + Arrays.toString(score.getExtraArray()));
try{
    LoggingUserController.write("ajustedGrossScore", "t");
    int [] adj = new int[18];
    for (int i=0; i<adj.length; i++){
        int maximumHoleScore = score.getParArray()[i] + score.getExtraArray()[1] + 2;  // 2 = double bogey
        if(score.getStrokeArray()[i] > maximumHoleScore) {
            LOG.debug("AdjustedGrossScore pour hole = " + (i+1) + " , maximumHoleScore = " + maximumHoleScore);
            adj[i] = maximumHoleScore;
            String msg = "Adjusted Gross Score adjustment applied on hole = " + (i+1)
                     + " adjusted score = " + adj[i] + " for par = " + score.getParArray()[i];
            LOG.debug(msg);
            LoggingUserController.write(msg);
        }else{
            adj[i] = score.getStrokeArray()[i];
        }
   } //end for
       String msg = NEW_LINE + "After ajustedGrossScore, strokes = " + Arrays.toString(adj);
       LOG.debug(msg);
       LoggingUserController.write(msg + NEW_LINE);
       return adj;
  //  return Arrays.stream(adj).sum();

 }catch (Exception e){
     LOG.debug("Exception in ajustedGrossScore ! = "+ e);
    return null;
 }finally{
//    LOG.debug(NEW_LINE + "after adjustement, points = " + Arrays.deepToString(adj) );
  }
} //
   /*
 public static int[] netDoubleBogeyAdjustment(ScoreStableford score){
        // nouveau avec WHS
        // voir Rules of Handicaping rule 3.1b page 26
 //   LOG.debug(" -- Start netdoubleBogeyAdjustment with points = " + Arrays.deepToString(points));
try{
    LoggingUserController.write("Net Double Bogey Adjustment", "t");
 //   WriteTextFile.write("Array points = "+ Arrays.deepToString(points));
    // points [i][0] = hole
    // points [i][1] = par
    // points [i][2] = index
    // points [i][3] = strokes brut
    // points [i][4] = extra
    // points [i][5] = points stableford
 /*   
    for (int i=0; i<points.length; i++){
       WriteTextFile.write(
           "<b>Hole = </b>" + points[i][0]
         + "  <b>Par = </b>" + points[i][1]
         + "  <b>Difficulty = </b>" + points[i][2]
         + "  <b>Brut = </b>" + points[i][3]
         + "  <b>Strokes = </b>" + points[i][4]
                 );
    }
;
    int [] adj = new int[18];
    for (int i=0; i<adj.length; i++){
        int max = score.getParArray()[i] + score.getExtraArray()[1] + 2;
        if(score.getPointsArray()[i] > max) {
            LOG.debug("NDB pour " + i + " max = " + max);
            adj[i] = max;
            String msg = "Net double bogey adjustment applied on hole = " + (i+1)
                     + " adjusted gross score = " + score.getPointsArray()[i] + " for par = " + score.getParArray()[i];
            LOG.debug(msg);
            LoggingUserController.write(msg);
        }else{
            adj[i] = score.getPointsArray()[i];
        }
   } //end for
       String msg = NEW_LINE + "After adjustement, points = " + Arrays.toString(adj);
       LOG.debug(msg);
       LoggingUserController.write(msg + NEW_LINE);
    return adj;

 }catch (Exception e){
     LOG.debug("Exception in NDBA ! = "+ e);
    return null;
 }finally{
//    LOG.debug(NEW_LINE + "after adjustement, points = " + Arrays.deepToString(adj) );
  }
} // end method netDoubleBogeyAdjustment
*/
public static int[] calculatePoints(ScoreStableford score){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug(" entering calculatePoints " + methodName);
        LOG.debug("with scoreStableford = " + score);
  //   LOG.debug("strokeArray = " + Arrays.toString(score.getStrokeArray()));
  //   LOG.debug("extraArray  = " + Arrays.toString(score.getExtraArray()));
  //   LOG.debug("parArray    = " + Arrays.toString(score.getParArray()));
  //   LOG.debug("scoreList = " + score.getScoreList().toString());
try{
 //  calcul du net et des points  03-09-2023
     int[] points = new int[18];
     for (int i=0; i<points.length; i++){    
      //     net[i] = score.getStrokeArray()[i] - score.getExtraArray()[i]; 
      //     points[i] = pointsStableford(net[i], score.getParArray()[i]);
      
           points[i] = 
                   pointsStableford( // method
                   (score.getStrokeArray()[i] - score.getExtraArray()[i]) // net
                   , score.getParArray()[i]);
    } //end for
      LOG.debug("points calculated method = " + Arrays.toString(points));
  return points;
}catch (Exception e){
      LOG.error(" -- Exception in " + methodName + " /" + e );
      return null;
}finally{
//     LOG.debug(NEW_LINE + Arrays.deepToString(points) );
 }
} // end method calculatePoints

// new 16-04-2025 -- WHS revision 10/2024
// dans score_stableford.xhtml pour les trous non joués on encode -1 comme strokes
public static int[] completeHolesNotPlayed(ScoreStableford score){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
     LOG.debug("  " + methodName);
     LOG.debug("strokeArray = " + Arrays.toString(score.getStrokeArray()));
     LOG.debug("parArray    = " + Arrays.toString(score.getParArray()));
     int[] strokes = new int[18];
try{
 // on remplace les -1 
    for (int i=0; i<strokes.length; i++){
        if(score.getStrokeArray()[i] == -1){
            strokes[i] = score.getParArray()[i]; // net par
               LOG.debug("hole " + (i+1) + ", strokes completed = " + strokes[i]);
        }else{ // strokes altready completed
            strokes[i] = score.getStrokeArray()[i];  // recopy
        }
    } //end for
       LOG.debug("strokes completed after for loop = " + Arrays.toString(strokes));
    if(score.getHolesNotPlayed() == 5){ // joué 13 holes
       strokes[13] = strokes[13] + 1; //net bogey
       LOG.debug("not played 5 holes, correction net bogey hole 14 " + strokes[13]);
    }
    if(score.getHolesNotPlayed() == 6){ // joué 12 holes
       strokes[12] = strokes[12] + 1; //net bogey
       LOG.debug("not played 6 holes, correction net bogey hole 13 = " + strokes[12]);
    }
   return strokes;
}catch (Exception e){
      LOG.error(" -- Exception in " + methodName + " /" + e );
      return null;
}finally{
      LOG.debug("strokes completed final = " + Arrays.toString(strokes));
 }
} // end method calculatePoints

private static int pointsStableford(int net, final int par){ 
      LOG.debug("entering pointsStableford");
 //      LOG.debug(" with net =     " + net);
 //      LOG.debug(" with par =     " + par);
     if(net == 0){ // dans les parcours 9 holes, les 9 autres holes ne sont pas utilisés dans l'array
        return 0;
    }
    switch (par - net){
        // mod 09-04-2025
        case -5, -4, -3, -2 -> {return 0;} // rien du tout ex 7 strokes sur par 3
        case -1 -> {return 1;} // bogey 
        case 0 -> { return 2;} // par
        case 1 -> { return 3;} // birdie
        case 2 -> { return 4;} // eagle
        case 3 -> { return 5;} // albatros
        default -> {
            String msg = " -- Falling in Default in pointStableford - 0 points, par = " + par + " net = " + net;
            LOG.error(msg) ;
            showMessageFatal(msg);
            return 0;
          }
    } // end switch
} // end method

void main() throws SQLException, Exception{
    Connection conn = new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
 //   player= new read.LoadPlayer().load(player, conn);
    Round round = new Round();
    round.setIdround(631);
 //   round = new read.ReadRound().read(round, conn);
    ScoreStableford scoremain = new ScoreStableford();
    scoremain.setHandicapType("WHS");
   int strokes[] ={4,5,3,4,4,4,5,3,5,6,5,7,6,7,5,7,4,5};
 //   int strokes[] ={0,0,0,0,0,0,0,0,0,6,5,7,6,7,5,7,4,5};
//      int strokes[] ={6,5,3,4,4,4,5,3,5,0,0,0,0,0,0,0,0,0};
 //   LOG.debug("length = " + arrnum.length);
    scoremain.setStrokeArray(strokes);
//    scoremain.setScoreAction("filling");  // or calculating
//    LOG.debug("score before = " + score);
// ???   ScoreStableford stb = calc(player, scoremain, round, conn);
 /////    LOG.debug("final result = " + stb);
   // LocalDateTime ldt = LocalDateTime.of(2017,Month.AUGUST,26,0,0);
    DBConnection.closeQuietly(conn, null, null, null);
}// end main
} // end class