
package calc;

import Controllers.LoggingUserController;
import Controllers.StablefordController;
import entite.Course;
import entite.LoggingUser;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.sql.Connection; // kept for @Deprecated bridge
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Calcul des scores Stableford.
 * Version CDI-compliant. le 16/01/2026
 * on pourrait aussi faire pour StablefordController
 * 
 */
@ApplicationScoped  // changed from @RequestScoped — no mutable state 2026-02-26
public class CalcScoreStableford implements interfaces.GolfInterface, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    // ========================================
    // INJECTIONS CDI — migrated 2026-02-25
    // ========================================
    @Inject private StablefordController stablefordController;
    @Inject private read.ReadParAndStrokeIndex readParAndStrokeIndex;
    @Inject private calc.CalcStablefordCourseHandicap calcCourseHandicap;
    @Inject private calc.CalcStablefordPlayingHandicap calcPlayingHandicap;
    @Inject private calc.CalcStablefordScoreDifferential calcScoreDifferential;
    @Inject private Controllers.LoggingUserController loggingUserController;

    public CalcScoreStableford() { }
    
    // ========================================
    // CHANGEMENT PRINCIPAL : Supprimer "static"
    // ========================================
    
    /** @deprecated Use {@link #calc(Player, ScoreStableford, Round, Course, Tee)} without Connection */
    @Deprecated
    public ScoreStableford calc(final Player player, ScoreStableford score,
            final Round round, final Course course, final Tee tee,
            final Connection conn) {
        return calc(player, score, round, course, tee);
    } // end method

    public ScoreStableford calc(
            final Player player,
            ScoreStableford score,
            final Round round,
            final Course course,
            final Tee tee) {
        
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("... entering " + methodName);
        LOG.debug(" with ScoreStableford = " + score);
        LOG.debug(" with Round = " + round);
        LOG.debug(" with Player = " + player);
        
        try {
            LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
            LoggingUserController.write(LocalDateTime.now().format(ZDF_TIME), "i");
            LoggingUserController.write(player.getPlayerFirstName() + " - " + player.getPlayerLastName(), "i");
            LoggingUserController.write(round.getRoundName() + " - " + round.getRoundDate().format(ZDF_TIME), "t");
            
            int[] arr = score.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray();
            LOG.debug("arr from score = " + Arrays.toString(arr));
            
            if(utils.LCUtil.isArrayAllZeroes(arr)){
                String msg = "scoreArray is all zeroes= " + Arrays.toString(arr);
                LOG.error(msg);
                showMessageFatal(msg);
            } else {
                String msg = "scoreArray is NOT all zeroes= " + Arrays.toString(arr);
                LOG.info(msg);
            }

        //    if(utils.LCUtil.isArrayOneZero(arr)){ // mod 31-01-2026
            if(utils.LCUtil.containsZero(arr)){    
                String msg = "There is (at least...) one result = 0 - correction needed !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return score;
            }
            
            int[] a = new int[9];
            if(round.getRoundStart() == 10){
                arr = IntStream.concat(IntStream.of(a), IntStream.of(arr)).toArray();
            }
            if(round.getRoundStart() == 1 && round.getRoundHoles() == 9){
                arr = IntStream.concat(IntStream.of(arr),IntStream.of(a)).toArray();
            }
            score.setStrokeArray(arr);
            LOG.debug(" array Strokes completed from scoreList for legacy calculations = "+ Arrays.toString(score.getStrokeArray()));
            LoggingUserController.write("Score Card =  " + Arrays.toString(score.getStrokeArray()));

            if(utils.LCUtil.isArrayAllZeroes(score.getParArray())){
                LOG.debug("handling isArrayAllZeroes");
                // score = new read.ReadParAndStrokeIndex().read(course, score);
                score = readParAndStrokeIndex.read(course, score); // migrated 2026-02-25
                // score = new calc.CalcStablefordCourseHandicap().calc(score, player, round, tee);
                score = calcCourseHandicap.calc(score, player, round, tee); // migrated 2026-02-25
                LOG.debug("courseHandicap = " + score.getCourseHandicap());
                LOG.debug("HandicapIndex player = " + score.getPlayerHandicapWHS());
                // int i = new calc.CalcStablefordPlayingHandicap().calc(score, player, round);
                int i = calcPlayingHandicap.calc(score, player, round); // migrated 2026-02-25
                score.setPlayingHandicap(i);
                LOG.debug(" playingHandicap = " + score.getPlayingHandicap());   
                
                // ========================================
                // CHANGEMENT : Utiliser injection au lieu d'appel statique
                // ========================================
          //      score.setExtraArray(stablefordController.completeWithExtraStrokesNew(score, round));
                score.setExtraArray(Controllers.StablefordController.completeWithExtraStrokesNew(score, round)); //avant
                
                LOG.debug("ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray()));
            }
            
            score.setHolesNotPlayed(countHolesNotPlayed(score.getStrokeArray()));
            String msg = "There are holes not played = " + score.getHolesNotPlayed();
            LOG.info(msg);
            showMessageInfo(msg);
            LoggingUserController.write(msg);
            
            if(score.getHolesNotPlayed() > 0){
                score.setStrokeArray(completeHolesNotPlayed(score));
                msg = "StrokeArray completed with HolesNotPlay = " + Arrays.toString(score.getStrokeArray());
                LOG.debug(msg);
                LoggingUserController.write(msg);
            }
            
            score.setStart(round.getRoundStart());
            LOG.debug("start is now = " + score.getStart());
            score.setHoles(round.getRoundHoles());
            LOG.debug("holes is now = " + score.getHoles());
            
            score.setStrokeArray(ajustedGrossScore(score));
            msg = "ExtraArray completed with Extra strokes = " + Arrays.toString(score.getExtraArray());
            LOG.debug(msg);
            LoggingUserController.write(msg);
            
            score.setAdjustedGrossScore(Arrays.stream(score.getStrokeArray()).sum());
            msg = " Adjusted Score Total = " + score.getAdjustedGrossScore();
            LOG.debug(msg);
            LoggingUserController.write(msg);
            
            score.setPointsArray(calculatePoints(score));
            msg = "Calculated points are in score.getPointsArray :" + Arrays.toString(score.getPointsArray());
            LOG.debug(msg);
            LoggingUserController.write(msg);
            
            score.setStablefordResult(Arrays.stream(score.getPointsArray()).sum());
            msg = "Stableford total points : " + score.getStablefordResult();
            LOG.debug(msg);
            LoggingUserController.write(msg);

            score.setTotalStrokes(Arrays.stream(score.getStrokeArray()).sum());
            msg = "Total Strokes (calculated) = " + score.getTotalStrokes();
            LOG.debug(msg);
            LoggingUserController.write(msg);
            
            score.setCourseRating(tee.getTeeRating().doubleValue());
            score.setSlopeRating(tee.getTeeSlope());
            score.setScoreParCourse(tee.getTeePar()); 
            // double sd = new calc.CalcStablefordScoreDifferential().calc(score, player, round, conn);
            double sd = calcScoreDifferential.calc(score, player, round); // migrated 2026-02-25
            score.setScoreDifferential(sd);
            score.setShowCreate(true);
            score.setShowCalculate(false);
            score.setShowLineDifferential(true);
            
            score.setStart(round.getRoundStart());
            score.setHoles(round.getRoundHoles());
            
            // ========================================
            // CHANGEMENT : Utiliser injection
            // ========================================
   //         score.setScoreList(stablefordController.completeScoreListWithPoints(score));
            // score.setScoreList(new StablefordController().completeScoreListWithStrokes(score));
            score.setScoreList(stablefordController.completeScoreListWithStrokes(score)); // migrated 2026-02-25
            // score.setScoreList(new StablefordController().completeScoreListWithPoints(score));
            score.setScoreList(stablefordController.completeScoreListWithPoints(score)); // migrated 2026-02-25
            
            LOG.debug("CalcScoreStableford final = " + score);
            
            LoggingUser logging = new LoggingUser();
            logging.setLoggingIdPlayer(player.getIdplayer());
            logging.setLoggingIdRound(round.getIdround());
            logging.setLoggingType("R");
            
            // boolean b = new Controllers.LoggingUserController().createUpdateLoggingUser(logging);
            boolean b = loggingUserController.createUpdateLoggingUser(logging); // migrated 2026-02-25
            
            return score;
            
        } catch (Exception e){
            LOG.debug("Exception in " + methodName + e);
            return null;
        }
    }
    
    // ========================================
    // MÉTHODES UTILITAIRES : Peuvent rester static
    // ========================================
    // Ces méthodes sont appelées uniquement depuis calc()
    // dans cette classe, donc elles peuvent rester static
    
   private static int countHolesNotPlayed(int[] arr){
        int count = 0;
        for(int i=0; i<arr.length; i++){
            if(arr[i] == -1){
                count++;
            }
        }
        return count;
    }
    
    private static int[] ajustedGrossScore(ScoreStableford score){
        LOG.debug(" entering ajustedGrossScore with stroke = " + Arrays.toString(score.getStrokeArray()));
        LOG.debug("  with par = " + Arrays.toString(score.getParArray()));
        LOG.debug("  with extra = " + Arrays.toString(score.getExtraArray()));
        
        try {
            LoggingUserController.write("ajustedGrossScore", "t");
            int [] adj = new int[18];
            for (int i=0; i<adj.length; i++){
                int maximumHoleScore = score.getParArray()[i] + score.getExtraArray()[1] + 2;
                if(score.getStrokeArray()[i] > maximumHoleScore) {
                    LOG.debug("AdjustedGrossScore pour hole = " + (i+1) + " , maximumHoleScore = " + maximumHoleScore);
                    adj[i] = maximumHoleScore;
                    String msg = "Adjusted Gross Score adjustment applied on hole = " + (i+1)
                             + " adjusted score = " + adj[i] + " for par = " + score.getParArray()[i];
                    LOG.debug(msg);
                    LoggingUserController.write(msg);
                } else {
                    adj[i] = score.getStrokeArray()[i];
                }
            }
            String msg = NEW_LINE + "After ajustedGrossScore, strokes = " + Arrays.toString(adj);
            LOG.debug(msg);
            LoggingUserController.write(msg + NEW_LINE);
            return adj;
        } catch (Exception e){
            LOG.debug("Exception in ajustedGrossScore ! = "+ e);
            return null;
        }
    }

    private static int[] calculatePoints(ScoreStableford score){
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug(" entering calculatePoints " + methodName);
        LOG.debug("with scoreStableford = " + score);
        
        try {
            int[] points = new int[18];
            for (int i=0; i<points.length; i++){    
                points[i] = pointsStableford(
                    (score.getStrokeArray()[i] - score.getExtraArray()[i]),
                    score.getParArray()[i]
                );
            }
            LOG.debug("points calculated method = " + Arrays.toString(points));
            return points;
        } catch (Exception e){
            LOG.error(" -- Exception in " + methodName + " /" + e );
            return null;
        }
    }
// version non testée
private static int[] calculatePoints2(ScoreStableford score) {
    if (score == null) return new int[0];

    final int[] s = score.getStrokeArray();
    final int[] e = score.getExtraArray();
    final int[] p = score.getParArray();

    if (s == null || e == null || p == null
            || s.length < 18 || e.length < 18 || p.length < 18) {
        return new int[0];
    }

    int[] points = new int[18];

    // Boucle for compacte avec variable intermédiaire pour clarté
    for (int i = 0; i < 18; i++) {
        points[i] = pointsStableford(s[i] - e[i], p[i]);
    }

    return points;
}

    
    
    
    
    
   private static int[] completeHolesNotPlayed(ScoreStableford score){
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("  " + methodName);
        LOG.debug("strokeArray = " + Arrays.toString(score.getStrokeArray()));
        LOG.debug("parArray    = " + Arrays.toString(score.getParArray()));
        int[] strokes = new int[18];
        
        try {
            for (int i=0; i<strokes.length; i++){
                if(score.getStrokeArray()[i] == -1){
                    strokes[i] = score.getParArray()[i];
                    LOG.debug("hole " + (i+1) + ", strokes completed = " + strokes[i]);
                } else {
                    strokes[i] = score.getStrokeArray()[i];
                }
            }
            LOG.debug("strokes completed after for loop = " + Arrays.toString(strokes));
            
            if(score.getHolesNotPlayed() == 5){
                strokes[13] = strokes[13] + 1;
                LOG.debug("not played 5 holes, correction net bogey hole 14 " + strokes[13]);
            }
            if(score.getHolesNotPlayed() == 6){
                strokes[12] = strokes[12] + 1;
                LOG.debug("not played 6 holes, correction net bogey hole 13 = " + strokes[12]);
            }
            return strokes;
        } catch (Exception e){
            LOG.error(" -- Exception in " + methodName + " /" + e );
            return null;
        } finally {
            LOG.debug("strokes completed final = " + Arrays.toString(strokes));
        }
    } //end method
   
   // non testée
   private static int[] completeHolesNotPlayed2(ScoreStableford score) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("Entering " + methodName);

    if (score == null || score.getStrokeArray() == null || score.getParArray() == null) {
        LOG.error("Score or arrays are null");
        return new int[0];
    }

    final int[] originalStrokes = score.getStrokeArray();
    final int[] pars = score.getParArray();
    int[] strokes = IntStream.range(0, 18)
            .map(i -> originalStrokes[i] == -1 ? pars[i] : originalStrokes[i])
            .toArray();

    // Correction spéciale pour trous non joués
    int holesNotPlayed = score.getHolesNotPlayed();
    if (holesNotPlayed == 5 && strokes.length > 13) {
        strokes[13]++;
        LOG.debug("Holes not played = 5, corrected hole 14 to " + strokes[13]);
    } else if (holesNotPlayed == 6 && strokes.length > 12) {
        strokes[12]++;
        LOG.debug("Holes not played = 6, corrected hole 13 to " + strokes[12]);
    }

    LOG.debug("Final strokes array: " + Arrays.toString(strokes));
    return strokes;
}

  //@SuppressWarnings("preview")
    private static int pointsStableford(int net, final int par){ 
        LOG.debug("entering pointsStableford");
        
        if(net == 0){
            return 0;
        }
   // mod 16/01/2026 
 
   return switch (par - net) {
      //  case int d when d <= -2 -> 0; // preview en java 25
        case -5, -4, -3, -2 -> 0;    
        case -1 -> 1;
        case 0  -> 2;
        case 1  -> 3;
        case 2  -> 4;
        case 3  -> 5;
        default -> {
        String msg = " -- Falling in Default in pointStableford - 0 points, par = "
                   + par + " net = " + net;
        LOG.error(msg);
        showMessageFatal(msg);
        yield 0;
    }
}; 
        
    /*   
        switch (par - net){
            case -5, -4, -3, -2 -> {return 0;}
            case -1 -> {return 1;}
            case 0 -> { return 2;}
            case 1 -> { return 3;}
            case 2 -> { return 4;}
            case 3 -> { return 5;}
            default -> {
                String msg = " -- Falling in Default in pointStableford - 0 points, par = " + par + " net = " + net;
                LOG.error(msg) ;
                showMessageFatal(msg);
                return 0;
            }
        }
   */
    }

    /* from claude : semble intéressant : à tester
    
private static final int POINTS_DOUBLE_BOGEY_OR_WORSE = 0;
private static final int POINTS_BOGEY = 1;
private static final int POINTS_PAR = 2;
private static final int POINTS_BIRDIE = 3;
private static final int POINTS_EAGLE = 4;
private static final int POINTS_ALBATROSS = 5;
private static final int POINTS_MAX = 8;

private static int pointsStableford(int net, final int par) {
    LOG.debug("Calcul points Stableford - par: {}, net: {}", par, net);
    
    if (net == 0) {
        return 0; // Trou non joué
    }
    
    int diff = par - net;
    
    return switch (diff) {
        case -2, -1 -> POINTS_BOGEY;
        case 0 -> POINTS_PAR;
        case 1 -> POINTS_BIRDIE;
        case 2 -> POINTS_EAGLE;
        case 3 -> POINTS_ALBATROSS;
        default -> {
            if (diff < -2) yield POINTS_DOUBLE_BOGEY_OR_WORSE;
            yield Math.min(POINTS_ALBATROSS + (diff - 3), POINTS_MAX);
        }
    };
}
    */
    
    // ========================================
    // MAIN pour tests : Peut rester static
    // ========================================
/*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // tests locaux
    } // end main
*/
} // end class