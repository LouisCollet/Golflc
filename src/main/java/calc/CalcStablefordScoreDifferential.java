package calc;

import Controllers.LoggingUserController;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.Connection;
import static utils.LCUtil.myDoubleRound;

/**
 * Calcul du Score Differential Stableford (WHS).
 * Migrated to CDI — 2026-02-25
 */
@ApplicationScoped
public class CalcStablefordScoreDifferential implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String CLASSNAME = utils.LCUtil.getCurrentClassName();

    public CalcStablefordScoreDifferential() { }

    /**
     * Calcule le score differential.
     * @param score le ScoreStableford (avec slopeRating, courseRating, adjustedGrossScore, playerHandicapWHS)
     * @param player le joueur
     * @param round le round (pour roundHoles)
     * @return le score differential arrondi à 1 décimale
     */
    public double calc(final ScoreStableford score, final Player player, final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
 try{
       LOG.debug(" ...entering " + methodName);
       LOG.debug(" with score = " + score);
  //     LOG.debug(" with round = " + round.toString());
      LoggingUserController.write(CLASSNAME + "." + methodName,"i");   
      LoggingUserController.write("score differential", "t");

      
   // formule   (113/slope)*(ags-cr)
      LOG.debug("slopeRating = " + score.getSlopeRating());
      LoggingUserController.write("slopeRating = " + score.getSlopeRating());
      LOG.debug("adjustedScore (AGS) = " + score.getAdjustedGrossScore());
      LoggingUserController.write("adjustedScore (AGS) = " + score.getAdjustedGrossScore());
      LOG.debug("CourseRating = " + score.getCourseRating());
      LoggingUserController.write("CourseRating = " + score.getCourseRating());
      LOG.debug("Player Handicap = " + score.getPlayerHandicapWHS());
      LoggingUserController.write("Player Handicap = " + score.getPlayerHandicapWHS());
    if(round.getRoundHoles() == 18){
        double scoreDifferential = (113.0/score.getSlopeRating().doubleValue())
              *
              (score.getAdjustedGrossScore() - score.getCourseRating());
            LOG.debug("score differential 18 holes from 1/10/2024  = " + scoreDifferential) ;
        scoreDifferential = myDoubleRound(scoreDifferential,1);
        String msg = ("score differential 18 holes rounded with 1 decimale = " + scoreDifferential);
            LOG.debug(msg);
        return scoreDifferential;
    }else{        // round of 9 holes
        double scoreDifferential = (113.0/score.getSlopeRating().doubleValue()) // IMPORTANT !!slopeRating is short mod 15-04-2025
              *
              (score.getAdjustedGrossScore() - score.getCourseRating());
        String msg = "score differential 9 holes from 1/10/2024 " + scoreDifferential;
        LOG.debug(msg) ;
        LoggingUserController.write(msg, "b");    
        LOG.debug("playerHandicapWHS  = " +  score.getPlayerHandicapWHS()) ;
        score.setExpectedSD9Holes((score.getPlayerHandicapWHS() / 2) + 1.5); // formule approximative et non publiée !!
        msg = "expectedSD9Holes - PlayerHandicapWHS() / 2) + 1.5 " + score.getExpectedSD9Holes();
        LOG.debug(msg);
        LoggingUserController.write(msg, "b");
        
        msg = "expectedSD9Holes - à partir 1/10/2024 formula confidentielle et approximative non implémentée fin avril 2025 != " + score.getExpectedSD9Holes();
        LOG.debug(msg);
        LoggingUserController.write(msg, "b");
      
        scoreDifferential = scoreDifferential + score.getExpectedSD9Holes();
        scoreDifferential = myDoubleRound(scoreDifferential,1);
        msg = ("score differential 9 holes rounded with 1 decimale = " + scoreDifferential);
        LOG.debug(msg);
        LoggingUserController.write(msg);
        
        msg = "Score Differential = (113/SlopeRating) X (Adjusted Gross Score - Course Rating)";
        LOG.debug(msg);
        LoggingUserController.write(msg, "b"); // bold
   //     LoggingUserController.write("Score Differential = (113/SlopeRating) X (Adjusted Gross Score - Course Rating)"); 
   
        StringBuilder sb = new StringBuilder();
        sb.append("Score Differential = (113/");
        sb.append(score.getSlopeRating().doubleValue());
        sb.append(") X (");
        sb.append(score.getAdjustedGrossScore());
        sb.append(" - ");
        sb.append(score.getCourseRating());
        sb.append(")");
        LoggingUserController.write(sb.toString(), "b");
           LOG.debug("sb = " + sb);

        return scoreDifferential;
    }
    
  /*    
 //   BigDecimal slopeRating = new BigDecimal(score.getScoreSlope()); // new 19-10-2021
     BigDecimal slopeRating = new BigDecimal(score.getSlopeRating()); // new 19-10-2021
        LOG.debug("Slope Rating = " + slopeRating);
   //     Controllers.LoggingUserController.write("slope Rating = " + slopeRating);
     
        LOG.debug("Handicap Index player = " + score.getPlayerHandicapWHS());
    BigDecimal courseRating = new BigDecimal(score.getCourseRating());  // froam Doublenew 19-10-2021BigDecimal bigDecimalValue = new BigDecimal(value);
        LOG.debug("Course Rating = " + courseRating);
 //       Controllers.LoggingUserController.write("course Rating = " + courseRating);  
    BigDecimal courseHandicap = BigDecimal.valueOf(score.getCourseHandicap());
        LOG.debug("Course Handicap = " + courseHandicap);
//        Controllers.LoggingUserController.write("course Handicap = " + courseHandicap);  

    BigDecimal par = BigDecimal.valueOf(score.getScoreParCourse());
       LOG.debug("Par = " + par);
 //      Controllers.LoggingUserController.write("par = " + par);
//    int holes = round.getRoundHoles();
        LOG.debug("holes = " + round.getRoundHoles());
    BigDecimal adjustedScore = new BigDecimal(score.getAdjustedScore());
  // 18 holes //////////////////////////
   if(round.getRoundHoles() == 18){
        LOG.debug("calculating scoreDifferential for 18 holes");
    //    LOG.debug("113 divide sloperating  = " + BigDecimal.valueOf(113.0).divide(slopeRating,MathContext.DECIMAL32));
    //    LOG.debug("par substract course = " + par.subtract(courseRating));
 /// mod 15-09-2024       BigDecimal adjustedScore = new BigDecimal(score.getAdjustedScore()); 
    //    LOG.debug("aj36 = " + adjustedScore.subtract(BigDecimal.valueOf(36)));
       LoggingUserController.write("Score Differential = (113/SlopeRating) X (Par + Course Handicap - (Adjusted Stableford Score -36) - Course Rating)", "b");   
      StringBuilder sb = new StringBuilder();
      sb.append("Score Differential = (113/");
      sb.append(slopeRating);
      sb.append(") X (");
      sb.append(par);
      sb.append(" + ");
      sb.append(courseHandicap);
      sb.append(" - (");
      sb.append(adjustedScore);
      sb.append(" - 36) - ");
      sb.append(courseRating);
      sb.append(")");
    LoggingUserController.write(sb.toString(), "b");
       BigDecimal scoreDifferential = 
               (BigDecimal.valueOf(113.0)
                    .divide(slopeRating,MathContext.DECIMAL32)
               .multiply
                    (par.subtract(courseRating)
                    .add(courseHandicap)
                    .subtract
                        (adjustedScore.subtract(BigDecimal.valueOf(36)))
                    )
               );
       
          String msg = "Score Differential for 18 holes = " + scoreDifferential;
          LOG.debug(msg);
          LoggingUserController.write(msg);
          scoreDifferential = scoreDifferential.setScale(1, RoundingMode.HALF_UP); // une décimale
          msg = "ScoreDifferential rounded = " + scoreDifferential;
          LOG.debug(msg);
          LoggingUserController.write(msg + NEW_LINE);
// new 19-07-2020 normalement pas nécessaire !!
          if(scoreDifferential.compareTo(BigDecimal.ZERO) < 0){
              scoreDifferential = scoreDifferential.negate();
              LOG.debug("scoreDifferential negated = " + scoreDifferential);
          }
       return scoreDifferential.doubleValue();
  } // end holes = 18;
  //////////////////////////////////////
   if(round.getRoundHoles() == 9){
        LOG.debug("calculating scoreDifferential for 9 holes");
     //   LOG.debug("113 divide sloperating  = " + BigDecimal.valueOf(113.0).divide(slopeRating,MathContext.DECIMAL32));
     //   LOG.debug("par substract course = " + par.subtract(courseRating));
  //      LOG.debug("aj36 = " + adjustedScore.subtract(BigDecimal.valueOf(36)));

      BigDecimal strokesNotReceived = BigDecimal.ZERO;  // not yet implemented
  //    BigDecimal adjustedScore = new BigDecimal(score.getAdjustedScore()).add(ADDITIONAL_STABLEFORD_POINTS);  // plus 17
      double CourseHandicap18holes = (score.getPlayerHandicapWHS() * (slopeRating.doubleValue() / 113)) + courseRating.doubleValue() - (par.doubleValue() * 2);
         LOG.debug(" courseHcp18holes = " +  CourseHandicap18holes);
      CourseHandicap18holes = myDoubleRound(CourseHandicap18holes,0);
         LOG.debug(" courseHcp18holes rounded 0 decimale = " +  CourseHandicap18holes);
         
   //   BigDecimal courseHcp18holes = BigDecimal.valueOf(CourseHandicap18holes);
      LoggingUserController.
           write("Course Handicap 18 holes = Handicap Index x Slope Rating/113 + Course Rating - (Par x2)");
  //    LoggingUserController.write(" = " + courseHcp18holes);
   //       LOG.debug(" courseHcp18holes = " +  courseHcp18holes);
      LoggingUserController.
           write("Score Differential = (113/Slope Rating) x (Par x 2 + Course Handicap 18 Holes - (Adjusted Stablefordscore + Strokes not received -36) - Course Rating)");
      StringBuilder sb = new StringBuilder();
   //   sb.append("Score Differential = (113/Slope Rating) x (Par x 2 + Course Handicap 18 Holes - (Adjusted Stablefordscore + Strokes not received -36) - Course Rating)");
    //      LOG.debug("slopeRating = " +  slopeRating);
      
      sb.append("(113/").append(slopeRating).append(") X (");
      sb.append(par).append(" + ");
      sb.append(courseHandicap).append(" - (");
      sb.append(adjustedScore).append(" - 36) - ");
      sb.append(courseRating).append(")");
    LoggingUserController.write(sb.toString(), "b");
     calculations
      BigDecimal scoreDifferential =
           (BigDecimal.valueOf(113.0).divide(slopeRating, MathContext.DECIMAL32)
                .multiply(
                    (par.multiply(BigDecimal.valueOf(2),MathContext.DECIMAL32)
                        .add((courseHcp18holes)
                        .subtract(adjustedScore.add(strokesNotReceived).subtract(BigDecimal.valueOf(36)))
                        .subtract(courseRating))
                    )
                )
           );
       String s = "Score Differential for 9 holes = " + scoreDifferential;
          LOG.debug(s);

    //calcul en Double   
     LOG.debug("slopeRating = " + score.getSlopeRating());
    var v = Double.valueOf("113") / score.getSlopeRating();
     LOG.debug("var 113/slopeRating = " + v);
     LOG.debug("CourseRating = " + score.getCourseRating());
          double scoreDifferentialDouble = 
                  Double.valueOf("113")/score.getSlopeRating()
                  * (par.doubleValue()*2
                        + CourseHandicap18holes
                        - (score.getAdjustedScore() + strokesNotReceived.doubleValue() - 36)
                        - score.getCourseRating()
                    );
          String msg = ("score differential 9 holes Double calculation = " + scoreDifferentialDouble);
             LOG.debug(msg);
          scoreDifferentialDouble = myDoubleRound(scoreDifferentialDouble,1);
          msg = ("score differential rounded with 1 decimale = " + scoreDifferentialDouble);
             LOG.debug(msg);
          LoggingUserController.write(msg);
     //     scoreDifferential = scoreDifferential.setScale(1, RoundingMode.HALF_UP); // une décimale
     //     String msg = "ScoreDifferential rounded 1 décimale = " + scoreDifferential;
      //    LOG.debug(msg);
       //   LoggingUserController.write(msg + NEW_LINE);
// new 19-07-2020 normalement pas nécessaire !!
  //        if(scoreDifferential.compareTo(BigDecimal.ZERO) < 0){
   //           scoreDifferential = scoreDifferential.negate();
  //            LOG.debug("scoreDifferential negated = " + scoreDifferential);
   //       }
   */
//       return scoreDifferentialDouble;
   // end holes = 9;
  
 } catch (Exception e) {
      handleGenericException(e, methodName);
      return 999;
  }
 } // end method

    // @Deprecated bridge removed 2026-02-28 — no callers with Connection conn

/*
void main() {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    // tests locaux
} // end main
*/
} // end class