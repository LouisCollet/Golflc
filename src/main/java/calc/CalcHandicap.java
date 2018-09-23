
package calc;

import entite.Round;
import static interfaces.Log.LOG;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import utils.LCUtil;
public class CalcHandicap implements interfaces.GolfInterface, interfaces.Log{

public double getNewHandicap (int in_result, double in_exact_handicap, Round round)
{
     LOG.info(" -- Start of calcNewHandicap with result = " + in_result);
     LOG.info(" -- Start of calcNewHandicap with exact hcp = " + in_exact_handicap);
     LOG.info(" -- Start of calcNewHandicap with round = " + round.toString());

 double hcp = in_exact_handicap;
if(round.getRoundQualifying() == null || round.getRoundQualifying().equals("N"))
{
    LOG.info(" -- !! No New Handicap Calculated because Qualifying = N or null");
    return hcp;
}
 try
 {
  double[][]BUFFER;
//  Date date = SDF.parse("01/05/2014");
  LocalDateTime d = LocalDateTime.of(2014,Month.MAY,01,0,0);
  if(round.getRoundDate().isBefore(d))
  {
    LOG.info(" -- Date for BUFFER = before 01.05.2014 " );
    BUFFER = BUFFER_ZONE;
  }else{
    LOG.info(" -- Date for BUFFER = after 01.05.2014 " );
    BUFFER = BUFFER_ZONE_2014;
  }

    LOG.info(" -- BUFFER = " + Arrays.deepToString(BUFFER) );

// il faut aussi avoir le roundQualifying !! s'il est Counting, révision uniquement à la baisse !!

    int loops = 0; // une boucle par point stableford > 36 ou < 30 pour catégorie 5 par exemple
    // il faut compter point par point, pour gérer les passages d'une autre catégorie de handicap à une autre !!
   
    LOG.info("hcp = " + hcp);
    
    String add_substract = "";
    if(in_result > 36)  // la borne supérieure de la bufferzone est égale à 36 dans tous les cas
    {
        loops = in_result - 36;
        add_substract = "S";
        LOG.info(" -- number of loops > borne supérieure 36 = " + loops);
    }else{  //trouver la catégorie de handicap et la borne inférieure de la bufferzone
          for (double[] BUFFER1 : BUFFER)
          {
          if (hcp >= BUFFER1[1] && hcp <= BUFFER1[2])
          {
              loops = (int) BUFFER1[3];
              loops = loops - in_result;
              add_substract = "A";
              LOG.info(" -- Number of loops < borne inférieure    =   " + loops);
              LOG.info("BUFFER trouvé = " + Arrays.toString(BUFFER[3]));
          }//end  loop
          }  //end for 
    }
//  LOG.info(" -- number of loops = " + loops);
//LOG.info("on ajoute : " + BUFFER1[3]);
//outerloop:
 for(int j = 0; j < loops; j++)
 {    //   LOG.info(" -- outer loop = " + j);
      for (double[] BUFFER1 : BUFFER) {
       //       LOG.info(" -- inner loop = " + j);
       //     LOG.info(" -- hcp = " + hcp);
       //     LOG.info(" -- HCP Debut = " + BUFFER[j][1]);
       //     LOG.info(" -- HCP Fin =   " + BUFFER[j][2]);
          if (hcp >= BUFFER1[1] && hcp <= BUFFER1[2]) {
       //             LOG.info(" -- Category =  " + BUFFER[j][0]);
       //             LOG.info(" -- HCP Debut selected  = " + BUFFER[j][1]);
       //             LOG.info(" -- HCP Fin selected =   " + BUFFER[j][2]);
              if(add_substract.equals("S")){
                    LOG.info("add_subtract = S - diminution handicap DOWN");
                  hcp = hcp - BUFFER1[6];  // diminution handicap
              }else if(round.getRoundQualifying().equals("C")){   //Counting competition
                  LOG.info("Counting round : pas d'augmentation du handicap !");
                    return hcp; //LOG.info("je sors de la boucle !!!");
              }else if(add_substract.equals("A")){ 
                    LOG.info("add_subtract = A - augmentation handicap UP " + add_substract);
                  hcp = hcp + BUFFER1[5];  // augmentation handicap une seule fois !!
                 return hcp; //LOG.info("je sors de la boucle !!!");
             }
            
              hcp = utils.LCUtil.myRound(hcp,1);
              LOG.info(" -- New intermediate HCP     =   " + hcp);
          }
      } //end inner loop
 } //end outer loop
 LOG.info(" -- Final NEW HCP     =   " + hcp);
 return hcp;

 } catch (Exception e) {
     
     // LOG.info(" -- Error in calcNewHandicap" + e);
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

public static void main(String[] args) throws ParseException //throws SQLException // testing purposes
{
   Round round = new Round(); //////à modifier pour tester !!!!
//round.setIdround(274); //round avec bernard cornez
///   round.setRoundDate.valueOf(SDF.parse("07/08/2016") );
  round.setRoundQualifying("C");  // on peut tester avec "C" de counting, N de non qualifying et Y de qualifying
  CalcHandicap ch = new CalcHandicap();
  double hcp = ch.getNewHandicap (37, 25.9 ,round);
     LOG.info(" -- Bravo ! Voici votre nouveau Handicap : = " + hcp );

}// end main    

} //end class