
package batch;

import entite.composite.EPlayerHandicap;
import entite.HandicapIndex;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;

@Named("PlayerItemProcessor")
public class PlayerItemProcessor implements ItemProcessor{
    @Inject  private JobContext jobContext;
    @Inject  private Player player;
    @Inject  private HandicapIndex handicapIndex;
    @Inject  private EPlayerHandicap ePlayerHandicap;

     /** 
29 	 * The processItem method is part of a chunk 
30 	 * step.It accepts an input item from an 
31 	 * item reader and returns an item that gets 
32 	 * passed onto the item writer.Returning null  
33      * indicates that the item should not be continued  
34      * to be processed.This effectively enables processItem  
35 	 * to filter out unwanted input items. 36 	 * @param item specifies the input item to process. 
37 	 * @return output item to write. 
38 	 * @throws Exception thrown for any errors.public
 Object processItem(Object item) throws Exception  
39 
     * @param item 
     * @return  
     * @throws java.lang.Exception */ 
//To filter a record, one simply returns "null" from the ItemProcessor.
            //The framework will detect that the result is "null"
            //and avoid adding that item to the list of records delivered to the ItemWriter.
            //As usual, an exception thrown from the ItemProcessor will result in a skip.


@Override    
public Object processItem(Object item) throws Exception{
try{
      // you need to cast items to their specific type before processing!!!
      String[] line = (String[]) item; 
         LOG.debug("line = " + Arrays.toString(line));
      //lineinarray format  = [2021001, Patrick, Cantlay, San Antonio , Texas, US, 17-03-1992, M, EN] 
      player.setIdplayer(Integer.parseInt(line[0]));
      player.setPlayerFirstName(line[1]);
      player.setPlayerLastName(line[2]);
      player.getAddress().setCity(line[3]);
    //  player.getAddress().setCountry(line[4]);
      // mod 22-12-2022
      player.getAddress().getCountry().setCode(line[4]);
 //     player.setPlayerBirthDate(SDF.parse(line[5]));
  // à modifier   le contenu du fichier  
      player.setPlayerBirthDate(LocalDateTime.parse(line[5]));
 //     LocalDateTime.parse("2018-11-03T12:45:30"
      player.setPlayerGender(line[6]);
      player.setPlayerHomeClub(104); // tous les joueurs ont le même home Club !
   //   player.setPlayerPhotoLocation("fotoLocation");
      player.setPlayerPhotoLocation(Integer.toString(player.getIdplayer()) + ".png"); // mod 25-08-2023
      player.setPlayerLanguage(line[7]);
      String s = player.getPlayerFirstName() + "." + player.getPlayerLastName() + "@skynet.be";
      player.setPlayerEmail(s);
   //   player.getAddress().setZoneId("America/Los_Angeles");
      player.getAddress().setZoneId("America/Los_Angeles");
      double latitude = Double.parseDouble("34.086282"); // hollywood !!
      double longitude = Double.parseDouble("-118.318582");
   ///   player.setPlayerLatLng(new LatLng(latitude, longitude));
   /// à modifier   player.getAddress().setLatLng(latLng);
          LOG.debug("player to be inserted = " + player);
      handicapIndex.setHandicapDate(LocalDateTime.of(2021,Month.JANUARY,01,0,0));
      handicapIndex.setHandicapWHS(BigDecimal.valueOf(0.0));
         LOG.debug("handicap to be inserted = " + handicapIndex);
 //        LOG.debug("just before create player");
 
  //    ePlayerHandicap.setPlayer(player);
      ePlayerHandicap.withPlayer(player); // passage à record withplayer à créer manuellement !!
    //  ePlayerHandicap.setHandicapIndex(handicapIndex);
      ePlayerHandicap.withHandicapIndex(handicapIndex);
   return ePlayerHandicap;
}catch(Exception e){
          LOG.debug("PlayerItemprocessor Exception :" + e);
          return null;
 }
    } //end method
} //end class