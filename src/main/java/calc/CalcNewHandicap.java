
package calc;

import entite.Round;
import static interfaces.Log.LOG;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import utils.LCUtil;
public class CalcNewHandicap implements interfaces.GolfInterface{

public double calc (int in_result, double in_exact_handicap, Round round){
     LOG.info(" -- Start of calcNewHandicap with result = " + in_result);
     LOG.info(" -- Start of calcNewHandicap with exact hcp = " + in_exact_handicap);
     LOG.info(" -- Start of calcNewHandicap with round = " + round.toString());
try {
  double hcp = in_exact_handicap;
  if(round.getRoundQualifying() == null || round.getRoundQualifying().equals("N")){
    LOG.info(" -- !! No New Handicap Calculated because Qualifying = N or null");
    return hcp;
  }

  double[][]BUFFER;
  if(round.getRoundDate().isBefore(LocalDateTime.of(2014,Month.MAY,01,0,0))){
    LOG.info(" -- Date for BUFFER = before 01.05.2014 " );
    BUFFER = BUFFER_ZONE;
  }else{
    LOG.info(" -- Date for BUFFER = after 01.05.2014 " );
    BUFFER = BUFFER_ZONE_2014;
  }
    LOG.info(" -- BUFFER = " + Arrays.deepToString(BUFFER) );
    int loops = 0; // une boucle par point stableford > 36 ou < 30 pour catégorie 5 par exemple
    // il faut compter point par point, pour gérer les passages d'une autre catégorie de handicap à une autre !!
   int category = 0;
    LOG.info("exact handicap = " + hcp);
    if(in_exact_handicap > 36){
        category = 6;
        LOG.info("category = " + category);
    }
    String add_substract = "";
    if(in_result > 36){ // la borne supérieure de la bufferzone est égale à 36 dans tous les cas
        loops = in_result - 36;
        add_substract = "S";
        LOG.info(" -- number of loops > borne supérieure 36 = " + loops);
    }else{  //trouver la catégorie de handicap et la borne inférieure de la bufferzone
          for (double[] BUFFER1 : BUFFER){
            if(hcp >= BUFFER1[1] && hcp <= BUFFER1[2]){
              loops = (int) BUFFER1[4]; // was 3
              LOG.info("loops = " + BUFFER1[4]);
              loops = loops - in_result;
              add_substract = "A";
                LOG.info(" -- Number of loops < borne inférieure = " + loops);
                LOG.info("BUFFER 1 trouvé = " + Arrays.toString(BUFFER1)) ; //BUFFER[3]));
                LOG.info("because hcp " + hcp + " >= " + BUFFER1[1] + " and hcp <= " + BUFFER1[2]);
                LOG.info(" -- borne inférieure buffer = " + BUFFER1[3]);
              if(in_result > BUFFER1[3]){
                   LOG.info(" no modification because score " + in_result + " > borne inférieure buffer = " + BUFFER1[3]);
                   return hcp;
              }
              
            }//end if
          }//end for 
    }
//outerloop:
LOG.info("Operation ont the hcp = " + add_substract);
 for(int j = 0; j < loops; j++){ 
      for(double[] BUFFER1 : BUFFER) {
          if(hcp >= BUFFER1[1] && hcp <= BUFFER1[2]) {
                    LOG.info(" -- Category array = " + Arrays.toString(BUFFER1));
                    LOG.info(" -- Category number = " + BUFFER1[0]);
                    LOG.info(" -- Borne inférieure buffer = " + BUFFER1[3]);
                    LOG.info(" -- Borne supérieure buffer = " + BUFFER1[4]);
                    LOG.info(" -- Handicap UP = " + BUFFER1[5]);
                    LOG.info(" -- Handicap DOWN = " + BUFFER1[6]);
              if(add_substract.equals("S")){
                    LOG.info("operation = S - handicap DOWN de " + BUFFER1[6]);
                  hcp = hcp - BUFFER1[6];  // diminution handicap
                  LOG.info("new hcp = " + hcp);
                  LOG.info("loop = " + j);
              }else if(round.getRoundQualifying().equals("C") || category == 6){  //Counting competition
                    LOG.info("Counting round or Category 6 : pas d'augmentation du handicap !");
                  return hcp;
              }else if(add_substract.equals("A")){ 
                    LOG.info("operation = A - handicap UP de " + BUFFER1[5]);
                  hcp = hcp + BUFFER1[5];  // augmentation handicap une seule fois !!
                  if(hcp > 36.0){  // hcp ne remonte pas au-delà de 36 !
                      LOG.info("new hcp reduced to 36, calculation was: " + hcp);
                      hcp = 36.0;
                  }
                 return hcp;
             }
            
              hcp = utils.LCUtil.myRound(hcp,1);
              LOG.info(" -- New intermediate HCP = " + hcp);
          } // end if
      } //end inner loop
 } //end outer loop
 LOG.info(" ** FINAL HCP =   " + hcp);
 return hcp;

 } catch (Exception e) {
      String msg = " -- Error in calcNewHandicap" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
    return 0.0;
 }
 finally
 {
   // LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
} // end method setNewHandicap

public static void main(String[] args) throws ParseException{
   Round round = new Round();
   round.setRoundDate(LocalDateTime.of(2019,Month.APRIL,01,0,0));
   round.setRoundQualifying("C");  // "C" = counting, N = non qualifying et Y = qualifying
  double hcp = new CalcNewHandicap().calc(49, 37 ,round); // resultat, exact hcp, round 
     LOG.info(" Voici votre nouveau Handicap : = " + hcp );
}// end main
} //end class