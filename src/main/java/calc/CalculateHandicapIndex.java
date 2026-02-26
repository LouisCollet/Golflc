package calc;

import Controllers.LoggingUserController;
import entite.HandicapIndex;
import entite.LoggingUser;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import manager.PlayerManager;
import utils.LCUtil;
import static utils.LCUtil.roundDouble;

@ApplicationScoped
public class CalculateHandicapIndex implements Serializable, interfaces.GolfInterface {

    private static final long serialVersionUID = 1L;

    @Inject
    private PlayerManager playerManager;
    @Inject
    private Controllers.LoggingUserController loggingUserController; // migrated 2026-02-26
    @Inject
    private update.UpdateExceptionalScoreReduction updateExceptionalScoreReduction;   // migrated 2026-02-24
    @Inject
    private find.FindCountScoreDifferential findCountScoreDifferential;               // migrated 2026-02-24
    @Inject
    private find.FindLowHandicapIndex findLowHandicapIndex;                           // migrated 2026-02-24
    @Inject
    private lists.ScoreDifferentialList scoreDifferentialList;                        // migrated 2026-02-24
    @Inject
    private find.FindHandicapIndexAtDate findHandicapIndexAtDateService;              // migrated 2026-02-26

    public CalculateHandicapIndex() { }

    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
public HandicapIndex calc (HandicapIndex handicapIndex){
    final String methodName = utils.LCUtil.getCurrentMethodName();
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" with HandicapIndex = " + handicapIndex);
try {
/*
    1. calculer le nombre de lignes find.FindCountScoreDifferentialWHS    returns int
    2. calcul du lowest handicap 
    3. application du score exceptionnel
    4. cas standard = supérieur à 20, alors prendre les 8 meilleurs find 8 bests returns double
    5. application des deux caps 
*/
     LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
     LoggingUserController.write("Calculate new handicap whs", "t");
       // new find.FindHandicapIndexAtDate().find(handicapIndex)
       double currentIndex = findHandicapIndexAtDateService.find(handicapIndex).getHandicapWHS().doubleValue(); // migrated 2026-02-26 — bug fix
    // error returns null
       String msg = "currentIndex when the round was played = " + currentIndex;
       LOG.debug(msg);
       LoggingUserController.write(msg); 
       
       msg = "currentScoreDifferential = " + handicapIndex.getHandicapScoreDifferential();
       LOG.debug(msg);   
       LoggingUserController.write(msg); 

  // à faire : distingue autres Type : EDS, COR etc ...
   //    handicapIndex.setHandicapComment("COMPET - Application introduction");
    //   msg = "initial Handicap Comment = " + handicapIndex.getHandicapComment(); // est null 
    //   LOG.debug(msg);  
    //   LoggingUserController.write(msg); 
       
       double esr = calcESR(currentIndex,handicapIndex.getHandicapScoreDifferential().doubleValue());
     // a faire dans les 2 cas > 20 et < 20 page 48, 4e bullet
          LOG.debug("ESR calculated = " + esr);
  
     Player p = new Player();
     p.setIdplayer(handicapIndex.getHandicapPlayerId());
     p = playerManager.readPlayer(p.getIdplayer());

  //   p = new read.ReadPlayer().read(p, conn);
     
  //   esr = 0;
 //       LOG.debug(" !!!!   ESR invalidated too complicated for test !!!!");
     if(esr < 0){
         // non testé !
            LOG.debug(" we modify the SD ! because esr is < 0 : " + esr);
         boolean b = updateExceptionalScoreReduction.update(p, esr);
         // tester return
           LOG.debug(" resultat modifyExceptionalScoreReduction " + b);
         Double d = esr;
         handicapIndex.setHandicapExceptionalScoreReduction(d.shortValue());
            LOG.debug(" esr setted " + handicapIndex.getHandicapExceptionalScoreReduction());
         handicapIndex.setHandicapComment(handicapIndex.getHandicapComment() + " ESR - ExceptionalScoreReduction (" + d + ")");
            LOG.debug(" comment esr setted " + handicapIndex.getHandicapComment());
     }else{
         LOG.debug(" there is NO esr modification = " + esr);
  //       Short s = 0;
         handicapIndex.setHandicapExceptionalScoreReduction((short) 0);
     }
     // à modifier : faire extrait et compter les éléments de la liste
    
   
    int countSD = findCountScoreDifferential.find(p);
//       LOG.debug(" Number of existing Score Differentials (SD) = " + countSD);
    if(countSD < 20){
          msg = "Number of Score Differentials (SD) < 20 = " + countSD;
          LOG.debug(msg);
          LoggingUserController.write(msg); 
          handicapIndex = SDLess20(p, handicapIndex);
          LOG.debug(" handicapWHS is now " + handicapIndex.getHandicapWHS());
    }else{ // sd >= 20 
          msg = " Number of Score Differentials (SD) > 20 = " + countSD;
          LOG.debug(msg);
          LoggingUserController.write(msg); 
          handicapIndex = SDMore20(p, handicapIndex);
          LOG.debug(" handicapWHS is now " + handicapIndex.getHandicapWHS());
    }
    
    msg = "At the end of " + methodName + " handicapindex is = " + handicapIndex;
       LOG.debug(msg);
    LoggingUserController.write(msg); 
    LCUtil.showMessageInfo(msg);
    LoggingUserController.write("the new handicap whs is : " + handicapIndex.getHandicapWHS(), "c"); // 12/09/2022
        
    // new 20/07/2022);
    handicapIndex.setLowHandicapIndex(lowHandicapIndexForFutureUse(handicapIndex));

  // new 14/07/2022  
      LoggingUser logging = new LoggingUser();
      logging.setLoggingIdPlayer(p.getIdplayer());
      logging.setLoggingIdRound(handicapIndex.getHandicapRoundId());
      logging.setLoggingType("H");
      loggingUserController.createUpdateLoggingUser(logging); // migrated 2026-02-26
    return handicapIndex;
 } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
 } finally { }
} // end method


public HandicapIndex SDLess20 (final Player player, final HandicapIndex handicapIndex){
    final String methodName = utils.LCUtil.getCurrentMethodName();
try{ 
        LOG.debug(" -- Start of " + methodName);
// Tableau Rules of Handicapping page 39
    List<HandicapIndex> listeSD = scoreDifferentialList.list(player,"<20");
 // on a une liste avec les Score Differentials 
        LOG.debug("SDLess20 - returned list = " );
       listeSD.forEach(item -> LOG.debug("Less 20 - Round Date " + item.getHandicapDate().format(ZDF_TIME_DAY)
                                  + " - SD = " + item.getHandicapScoreDifferential()
                                  )
               ); 
    int sd = listeSD.size();
       LOG.debug("SDLess20 - number of SD = " + sd);

    int lowest = 0; 
    int adjustment = 0;
      for(int i=0; i<FEWER_THAN_20SD.length; i++){
           if(FEWER_THAN_20SD[i][0] == sd){
                    LOG.debug("SDLess - selected = " + Arrays.toString(FEWER_THAN_20SD[i]));
               lowest = FEWER_THAN_20SD[i][1];
               adjustment = FEWER_THAN_20SD[i][2];
                    LOG.debug("SDLess - item i = " + i);
           }
       } // end for

     LOG.debug("lowest = " + lowest);
     LOG.debug("adjustment = " + adjustment);
     double tot = 0;
  // selection des SD retenus en vue de calculer l'average :col 2 "Average of lowest x"
     for(int i=0; i<lowest; i++){
        tot = Double.sum(tot, listeSD.get(i).getHandicapScoreDifferential().doubleValue());
          LOG.debug("i = " + (i+1) + " SD Less - SD element = " + listeSD.get(i).getHandicapScoreDifferential()
                   + " / tot = " + tot+ " date= " + listeSD.get(i).getHandicapDate());
     }
     double avg = roundDouble(tot/lowest,1);  // nearest tenth
        LOG.debug("average SD rounded = " + avg);
     double handicapWHS = avg + adjustment;  // adjustments are negative values !!
        LOG.debug("handicapIndex = average adjusted = " + handicapWHS);
     handicapIndex.setHandicapWHS(BigDecimal.valueOf(handicapWHS));
  // compléter colunm Comment avec détails du calcul !!
     handicapIndex.setHandicapComment(handicapIndex.getHandicapComment() 
             + " : sd=" + sd +",avg=" + avg + ",lowest=" + lowest + ",adjustment=" + adjustment);

     return handicapIndex;
 } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
}
} // end method

public HandicapIndex SDMore20 (final Player player, HandicapIndex handicapIndex){
    final String methodName = utils.LCUtil.getCurrentMethodName();
try{ 
       LOG.debug("entering " + methodName);
       LOG.debug(" with HandicapIndex = " + handicapIndex);
        LoggingUserController.write(CLASSNAME + "." + methodName,"i");  
        
    List<HandicapIndex> listeSD = scoreDifferentialList.list(player,">20");
    String msg = "Calculated Handicap Index = selection of lowest 8 SD's = ";
    LOG.debug(msg);
    LoggingUserController.write(msg); 
    
    listeSD.forEach(new Consumer<HandicapIndex>() {
           @Override
           public void accept(HandicapIndex item) {
               String s = "Round Date : " + item.getHandicapDate().format(ZDF_TIME_DAY) + " - SD = " + item.getHandicapScoreDifferential();
               LOG.debug(s);
               Controllers.LoggingUserController.write(s);
           }
       });
  //https://mkyong.com/java8/java-8-how-to-sum-bigdecimal-using-stream/   
     BigDecimal sum = listeSD
                .stream()
                .map(x -> x.getHandicapScoreDifferential())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
     msg = "SUM of lowest 8 SD's = " + sum;
        LOG.debug(msg);
     Controllers.LoggingUserController.write(msg);
  // moyenne des 8 meilleurs SD    
     BigDecimal handicapWHS = sum.divide(BigDecimal.valueOf(8));
     msg = "AVERAGE lowest 8 SD = " + handicapWHS;
        LOG.debug(msg);
     LoggingUserController.write(msg);
     
     handicapWHS = handicapWHS.setScale(1, RoundingMode.HALF_EVEN); // 1 = une décimale /
     msg = "Average lowest 8 SD ROUNDED = " + handicapWHS;
     LOG.debug(msg);
     LoggingUserController.write(msg + NEW_LINE);
     
     handicapIndex.setHandicapWHS(handicapWHS);
     handicapIndex.setHandicapComment(handicapIndex.getHandicapComment() + " : sd=8" + ",tot=" + sum);
//+",avg=" + avg + ",lowest=" + lowest + ",adjustment=" + adjustment);
       LOG.debug("before caps reduction, handicapWHS  = " + handicapIndex.getHandicapWHS());
     handicapIndex = this.calcSoftcapHardcap(handicapIndex);
       LOG.debug("after caps reduction, index = " + handicapIndex.getHandicapWHS());
     return handicapIndex;

 } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
}
} // end method

 public static double calcESR (double currentIndex, double currentScoreDifferential){
// Exceptional Score Reduction page 48
final String methodName = utils.LCUtil.getCurrentMethodName();
try{
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" -- currentIndex = " + currentIndex);
     LOG.debug(" -- currentScoreDifferential = " + currentScoreDifferential);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i");
     LoggingUserController.write("Exceptional score reduction","t"); 
 
     double diff = roundDouble(currentIndex - currentScoreDifferential, 1); // une décimale
     String s = "ESR difference currentIndex - currentScoreDifferential = " + diff;  
     LOG.debug(s);
     LoggingUserController.write(s); 
     double esr = 0.0;
     if(diff >= 7.0 && diff <= 9.9){
         LOG.debug(" ESR difference = > 7.0 and < 9.9 = " + diff);
         esr = -1.0;
         LoggingUserController.write("ESR difference = > 7.0 and < 9.9  ==> esr = " + esr); 
     }    
     if(diff >= 10.0){
         LOG.debug(" ESR difference => 10 " + diff);
         esr = -2.0;
         LoggingUserController.write("ESR difference = >10 ==> esr = " + esr); 
     } 
      LOG.debug(" ESR difference at end " + esr);
  //    Controllers.LoggingUserController.write("??? esr is not handled at this moment ! " + esr); 
 return esr;
  } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99;
}
} // end method

  public HandicapIndex calcSoftcapHardcap (HandicapIndex handicapIndex){
  final String methodName = utils.LCUtil.getCurrentMethodName();
      try{
// Limit on upward movement of a Handicap Index -  page 47
     LOG.debug(" -- Start of "  + methodName);
     LOG.debug(" -- handicapIndex = " + handicapIndex);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
     LoggingUserController.write("previous low handicap index" , "t"); 
  // à adapter !!
  // déplacé vers method
     double lowHandicapIndex = findLowHandicapIndex.find(handicapIndex);
     if(lowHandicapIndex == 0.0){
         return null; // cata !!
     }
     handicapIndex.setLowHandicapIndex(lowHandicapIndex);
     String s = "Previous Low Handicap Index = " + handicapIndex.getLowHandicapIndex();  
     LOG.debug(s);  
     LoggingUserController.write(s);
     LoggingUserController.write("Dates Period Low Handicap Index = " 
            + handicapIndex.getHandicapDate().toLocalDate().format(ZDF_YEAR)
            + " - "
            + handicapIndex.getHandicapDate().minusYears(1).toLocalDate().format(ZDF_DAY));
     double handicapWHS = handicapIndex.getHandicapWHS().doubleValue();
        LOG.debug("handicapWHS at the begining = " + handicapWHS);
        
     LoggingUserController.write("soft cap" ,"t");    
     double diff1 = roundDouble(handicapWHS - handicapIndex.getLowHandicapIndex(),1);
      LOG.debug("softcap handicapWHS - lowHandicapIndex = " + diff1);
     LoggingUserController.write("softcap handicapWHS - lowHandicapIndex = " + diff1);
     double reduction = 0;
    
    if(diff1 > 3){
        double diff2 = diff1 - 3.0;
            LOG.debug("softcap step 2 - diff 2 = " + diff2);
        reduction = roundDouble(diff2 /2,1);
            LOG.debug("reduction soft cap = " + reduction);
            Controllers.LoggingUserController.write("reduction soft cap =" + reduction);
     //   handicapWHS = round(average - diff2 / 2,1);
  //      handicapWHS = roundDouble(average - reduction,1);
        handicapWHS = roundDouble(handicapWHS - reduction,1);
            LOG.debug("application du SoftCap : handicapWHS = " + handicapWHS);
        handicapIndex.setHandicapWHS(BigDecimal.valueOf(handicapWHS));
        handicapIndex.setHandicapSoftHardCap(String.valueOf(reduction));
        handicapIndex.setHandicapComment(handicapIndex.getHandicapComment() + " Soft Cap (" + reduction + ")");
         //   LOG.debug("step 3 - hcp = " + round(avg - diff2 / 2,1));  // nearest tenth
    }else{
        LOG.debug("No application of Soft Cap :  handicapIndex = " + handicapWHS);
        LoggingUserController.write("No Soft Cap triggered : diff <= 3");
        handicapIndex.setHandicapSoftHardCap("-NO-");
    }
    
     LoggingUserController.write("hard cap", "t");
       LOG.debug("handicapWHS = " + handicapWHS);
       LoggingUserController.write("handicapWHS = " + handicapWHS);
       LOG.debug("low Handicap Index = " + handicapIndex.getLowHandicapIndex());
       LoggingUserController.write("low Handicap Index = " + handicapIndex.getLowHandicapIndex());
      
     double diff3 = roundDouble(handicapWHS - handicapIndex.getLowHandicapIndex(),1);
       LOG.debug("diff3  = " + diff3);
       LoggingUserController.write("handicapWHS - lowHandicapIndex = " + diff3);
     if(diff3 > 5.0){
  //         LOG.debug("application du HardCap car = " + diff3);
        handicapWHS = handicapIndex.getLowHandicapIndex() + 5;
        handicapIndex.setHandicapWHS(BigDecimal.valueOf(handicapWHS));
        handicapIndex.setHandicapSoftHardCap(String.valueOf(reduction + 5));
        LoggingUserController.write("reduction hard cap =" + reduction + 5);
        handicapIndex.setHandicapComment(handicapIndex.getHandicapComment() + " ,Hard Cap (" + 5 + ")");
    }else{
        LOG.debug("No application of Hard Cap : diff <= 5");
        LoggingUserController.write("No Hard Cap triggered: diff <= 5");
    }
    String msg = "handicapWHS after caps reduction = " + handicapWHS;
    LOG.info(msg);
    LoggingUserController.write(msg);
    LCUtil.showMessageInfo(msg);
 return handicapIndex;
  } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return null;
}
} // end method
  
 public double lowHandicapIndexForFutureUse (HandicapIndex handicapIndex){
// point 5.7 page 79
final String methodName = utils.LCUtil.getCurrentMethodName();
try{
     LOG.debug(" -- Start of " + methodName);
     LOG.debug(" -- handicapIndex = " + handicapIndex);
 //   LOG.debug(" -- currentScoreDifferential = " + currentScoreDifferential);
     LoggingUserController.write(CLASSNAME + "." + methodName,"i"); 
     LoggingUserController.write("Low HandicapIndex For Future Use","t"); 
     LoggingUserController.write("Scores between " + handicapIndex.getHandicapDate().minusYears(1).format(ZDF_YEAR) 
             + " and " + handicapIndex.getHandicapDate().format(ZDF_YEAR)); 
 //    double lowHandicapIndex = 0.0;
     double lowHandicapIndex = findLowHandicapIndex.find(handicapIndex);
     String msg = " Low handicap index is " + lowHandicapIndex;
      LOG.debug(msg);
      LoggingUserController.write(msg); 
 return lowHandicapIndex;
  } catch (Exception e) {
      String msg = " -- Error in " + methodName + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
      return 99;
}
} // end method
/*
void main() throws Exception, SQLException{
    Connection conn = null; // new DBConnection().getConnection();
    Player player = new Player();
    player.setIdplayer(324713);
    Round round = new Round();
    round.setIdround(487);
    round = new read.ReadRound().read(round, conn);
 //   round.setRoundDate(LocalDateTime.of(2019,Month.APRIL,01,0,0));
 //   round.setRoundQualifying("C");  // "C" = counting, N = non qualifying et Y = qualifying
    HandicapIndex handicapIndex = new HandicapIndex();
     // à compléter : handicapId, playerid, roundid, handicapdate
    handicapIndex.setHandicapPlayerId(player.getIdplayer());
    handicapIndex.setHandicapRoundId(round.getIdround());
    handicapIndex.setHandicapDate(round.getRoundDate());
    handicapIndex.setHandicapWHS(BigDecimal.valueOf(15.9));
    handicapIndex.setHandicapScoreDifferential(BigDecimal.valueOf(26.9));
 //   index.setHandicapScoreDifferential(BigDecimal.valueOf(15.9));
 //   index = new CalculateHandicapWHS().calc(index,player, round, conn);
    handicapIndex = new CalculateHandicapIndex().calc(handicapIndex, conn);
     LOG.debug(" Voici votre nouveau Handicap : = " + handicapIndex);
    // DBConnection.closeQuietly(conn, null, null, null);
}// end main
*/
} //end class
/*    Collections.sort(liste, new HandicapIndex.sortByScoreDifferential());  // static inner class !!!
     double tot = 0;
     for(int i=0; i<liste.size(); i++){ // 8 meilleurs résultats = les plus petits    
        tot = Double.sum(tot, liste.get(i).getHandicapScoreDifferential().doubleValue());
          LOG.debug("SD = " + (i+1)
                   + " / element = " + liste.get(i).getHandicapScoreDifferential()
                   + " / date= " + liste.get(i).getHandicapDate().format(ZDF_TIME_DAY)
                   + " / tot = " + tot);
     }
     LOG.debug("Double total = " + tot);
*/ 