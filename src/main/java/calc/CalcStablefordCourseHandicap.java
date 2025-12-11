package calc;

import Controllers.LoggingUserController;
import entite.HandicapIndex;
import entite.Player;
import entite.Round;
import entite.ScoreStableford;
import entite.Tee;
import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import utils.DBConnection;
import utils.LCUtil;

public class CalcStablefordCourseHandicap{
  private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
 public ScoreStableford calc(final ScoreStableford score, final Player player, final Round round, final Tee tee, final Connection conn) throws SQLException, Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
try{
    LOG.debug("... entering " + methodName );
 //   LOG.debug("with player = " + player);
 //   LOG.debug("with round = " + round);
 //   LOG.debug("with tee = " + tee);
        LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
        HandicapIndex handicapIndex = new HandicapIndex();
            LOG.debug("start calculations");
        handicapIndex.setHandicapPlayerId(player.getIdplayer());
        handicapIndex.setHandicapDate(round.getRoundDate());
        var hi = new find.FindHandicapIndexAtDate().find(handicapIndex, conn);
        double handicapWHS = hi.getHandicapWHS().doubleValue();
            LOG.debug("Player HandicapIndex WHS = " + handicapWHS);
        LoggingUserController.write(LocalDateTime.now().format(ZDF_TIME), "i");
        LoggingUserController.write(player.getPlayerFirstName() + " - " + player.getPlayerLastName(), "i");
        LoggingUserController.write("round name = " + round.getRoundName() + " - " + round.getRoundDate().format(ZDF_TIME), "t");    
        LoggingUserController.write("handicapWHS =  " + handicapWHS);
        handicapIndex.setHandicapWHS(BigDecimal.valueOf(handicapWHS));
        score.setPlayerHandicapWHS(handicapIndex.getHandicapWHS().doubleValue()); 
        score.setHandicapType("WHS");
//     }
       BigDecimal courseHandicap = courseHandicap(conn, handicapIndex ,tee, round);
       LoggingUserController.write("courseHandicap =  " + courseHandicap);
        LOG.debug("-- courseHandicap = " + courseHandicap);
     score.setCourseHandicap(courseHandicap.intValue());
   return score;
} catch(final SQLException sqle){
       String msg = " -- SQL Exception in " + methodName + 
       " -- ErrorCode = " + sqle.getErrorCode() +
       " -- SQLSTATE =  " + sqle.getSQLState();
       LCUtil.showMessageFatal(msg);
       return null;
}catch (Exception ex) {
                LOG.error("Exception in " + methodName + ex);
                LCUtil.showMessageFatal("Exception = " + ex.toString());
                return null;
}finally{
    
}
 } // end method

 public BigDecimal courseHandicap (Connection conn, HandicapIndex handicapIndex, Tee tee, Round round) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
 try{
        LOG.debug(" ...entering " + methodName);
        LOG.debug(" with handicapIndex = " + handicapIndex);
        LOG.debug(" with tee = " + tee.toString());
        LOG.debug(" with round = " + round.toString());
       LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
       LoggingUserController.write("Course Handicap", "t");
       LoggingUserController.write("Course Id" + round.getCourseIdcourse());
       BigDecimal handicapWHS = handicapIndex.getHandicapWHS();//turn the BigDecimal object into a double
       String msg = "HandicapWHS from database = " + handicapWHS;
          LOG.debug(msg);
       BigDecimal slopeRating = new BigDecimal(tee.getTeeSlope());
         msg = "slopeRating =  " + slopeRating;
     //    LoggingUserController.write(msg);
          LOG.debug(msg);
    BigDecimal courseRating = tee.getTeeRating();
         msg = "Course Rating = " + courseRating;
         LOG.debug(msg);
    //     LoggingUserController.write(msg);
    BigDecimal par = new BigDecimal(tee.getTeePar().intValue()); 
         msg = "Par = " + par;
         LOG.debug(msg);
     //    LoggingUserController.write(msg);
    int nholes = round.getRoundHoles();
        LOG.debug("holes = " + nholes);

    if(nholes == 9){ // new 08-12-2020
           LOG.debug(" 9 holes paragraph");
      //       BigDecimal bd113 = slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32);
          LOG.debug("slope rating / 113 = " + slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32));
          LOG.debug("courseRating - par  = " + courseRating.subtract(par));
          LOG.debug("Handicap Index / 2  = " + handicapWHS.divide(new BigDecimal("2.0"),MathContext.DECIMAL32));
  // enlevé 14-10-2021
//          par = par.multiply(new BigDecimal("2.0"),MathContext.DECIMAL32);
 //            LOG.debug("Correction !! Par * 2  = " + par);
        BigDecimal courseHandicap = 
               (handicapWHS.divide(new BigDecimal("2.0"),MathContext.DECIMAL32)
               .multiply(slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32))
               .add(courseRating.subtract(par))
                );
          LOG.debug("courseHandicap for 9 holes = " + courseHandicap);
       courseHandicap = courseHandicap.setScale(0, RoundingMode.HALF_EVEN); // 0 = pas de décimale /precision est different de scale
          LOG.debug("courseHandicap 9 holes rounded = " + courseHandicap);
    //   courseHandicapInt = courseHandicap.intValue();
    //      LOG.debug("courseHandicap int value = " + courseHandicap);
    
    LoggingUserController.write("Course handicap 9 holes = HandicapIndex X SlopeRating/113 + Course Rating - Par)", "b");   
      StringBuilder sb = new StringBuilder();
   //   sb.append("Course Handicap 9 holes = Handicap Index / 2");
      sb.append(handicapIndex.getHandicapWHS()).append("/2 "); // mod 16-09-2024
      sb.append(" X ");
      sb.append(slopeRating).append("/113");
      sb.append(" + ").append(courseRating).append(" - ").append(par);
    LoggingUserController.write(sb.toString(), "b");
      return courseHandicap;
    } // end 9 holes
    if(nholes == 18){
 //       BigDecimal bd113 = slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32);
         
          LOG.debug("slope rating / 113 = " + slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32));
          LOG.debug("courseRating - par  = " + courseRating.subtract(par));
        BigDecimal courseHandicap = 
               (handicapWHS
               .multiply(slopeRating.divide(new BigDecimal("113.0"),MathContext.DECIMAL32))
               .add(courseRating.subtract(par))
                );
             LOG.debug("courseHandicap for 18 holes = " + courseHandicap);
    LoggingUserController.write("CourseHandicap 18 holes =  " + "Handicap Index X Slope Rating/113 + CourseRating - Par", "b");
    StringBuilder sb = new StringBuilder();
      sb.append("Course Handicap 18 holes = Handicap Index");
      sb.append(handicapIndex);
      sb.append(" X ");
      sb.append(slopeRating);
      sb.append("/113 + ");
      sb.append(courseRating);
      sb.append(" - ");
      sb.append(par);
    LoggingUserController.write(sb.toString(), "b");        
          LoggingUserController.write("courseHandicap 18 holes =  " + courseHandicap);
          courseHandicap = courseHandicap.setScale(0, RoundingMode.HALF_EVEN); // 0 = pas de décimale /precision est different de scale
             LOG.debug("courseHandicap 18 holes rounded = " + courseHandicap);
           LoggingUserController.write("Course handicap = HandicapIndex X SlopeRating/113 + Course Rating - Par)", "b");   

    LoggingUserController.write("courseHandicap 18 holes rounded =  " + courseHandicap);
    //      LOG.debug("courseHandicap int value = " + courseHandicap);
       return courseHandicap;
    }

 }catch(Exception ex){
     String error = "Exception in " + methodName + ex;
      LOG.error(error);
      LCUtil.showMessageFatal(error);
      return null;
  } finally {
  }    
    return null;
 } // end method   
    

 void main() throws SQLException, Exception{
      Connection conn = new DBConnection().getConnection();
      Player player = new Player();
      player.setIdplayer(324713);
 //     player = new read.ReadPlayer().read(player, conn);
        
      Tee tee = new Tee();
      tee.setIdtee(157);

      
      tee = new read.ReadTee().read(tee, conn);
   //   tee.setTeeRating(BigDecimal.valueOf(70.3));
  //    tee.setTeePar ((short) 72);
   //   tee.setTeeSlope((short)125);
      Round round = new Round();
      round.setIdround(630);
      round = new read.ReadRound().read(round, conn); // holes loaded !
      HandicapIndex handicapIndex = new HandicapIndex();
      handicapIndex.setHandicapWHS(BigDecimal.valueOf(27.3));
      BigDecimal res = new calc.CalcStablefordCourseHandicap().courseHandicap(conn, handicapIndex, tee, round);
         LOG.debug("main - course Handicap calculated = " + res);
     DBConnection.closeQuietly(conn, null, null, null);
    }// end main
} // end class