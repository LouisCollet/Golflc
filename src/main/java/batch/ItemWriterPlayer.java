
package batch;

import entite.Handicap;
import entite.Player;
import entite.PlayerCSV;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named("ItemWriterPlayer")
public class ItemWriterPlayer extends AbstractItemWriter implements interfaces.Log
{    
  //  @Inject
    private PlayerCSV playerCSV;
    private Player player;
    private Handicap handicap;
    private int recordsOK; // = 0;
    private int recordsKO; // = 0;
    private Connection conn; // = null;
  //  private static int count = 0;
/** 
33 	 * The open method prepares the writer to write items. 
34 	 *  
35 	 * The input parameter represents the last checkpoint 
36 	 * for this writer in a given job instance. The   
37 	 * checkpoint data is defined by this writer and is  
38 	 * provided by the checkpointInfo method. The checkpoint 
39 	 * data provides the writer whatever information it needs  
40 	 * to resume writing items upon restart. A checkpoint value  
41 	 * of null is passed upon initial start. 
42 	 *  
43 	 * @param checkpoint specifies the last checkpoint  
44 	 * @throws Exception is thrown for any errors. 
45 	 */ 
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOG.info("ItemWriter - Open method started");
        // here open DB
        DBConnection dbc = new DBConnection();
        conn = dbc.getConnection();
       recordsOK = 0;
       recordsKO = 0;
    }
/** 
56 	 * The writeItems method writes a list of item for the current chunk.  
58 	 * @param items specifies the list of items to write. 
59 	 * @throws Exception is thrown for any errors. 
60 	 */ 

    /**
     * 56 	 * The writeItems method writes a list of item for the current chunk.58 	 * @param items specifies the list of items to write. 
59 	 * @throws Exception is thrown for any errors. 
60
     * @throws java.lang.Exception
     */
    @Override
    public void writeItems(List items) throws Exception 
 //   public void writeItems(List<PlayersCSV> items) throws Exception;
    { LOG.info("ItemWriter - writeItems method started for " + items.size());
    //// Ã  modifier ici !!! prendre l'objet en input ???
    try{
        LOG.info("items to be treated =  " + items.size() );
        LOG.info("Print List items = " + Arrays.deepToString(items.toArray()));
        //for (Object obj : items)
         for(int i = 0; i < items.size(); i++)
        {
            PlayerCSV inputRecord = (PlayerCSV) items.get(i);
                LOG.info("input Record " + items.get(i));
            boolean bo = insertDB(inputRecord);
                LOG.info("resultat input Record " + bo + " for " + i);
            if(bo == true)
            {
                recordsOK++;
       //         count ++;
                         LOG.info("records written YES");
            }else{
                recordsKO++;
                LOG.info("records NOT written in database ");
            }
        }
///    PlayerCSV inputRecord = (PlayerCSV) items.get(0);
///        LOG.info("inputRecord 0 =  " + inputRecord.toString());
///        LOG.info("inputRecord 0, id player =  " + inputRecord.getIdplayer());
        
//        inputRecord = (PlayerCSV) items.get(1);
//        LOG.info("inputRecord 1 =  " + inputRecord.toString());
//        LOG.info("inputRecord1 , id player =  " + inputRecord.getIdplayer());
        
    
    } //end try
    catch (Exception ex)
    { String msg = " -- Exception in ItemWriter !" + ex;
   	     LOG.error(msg);
      LCUtil.showMessageFatal(msg);
    }
    } // end method
/*
48 	 * The close method marks the end of use of the ItemWriter.
*       The writer is free to do any cleanup necessary. 
51 	 * @throws Exception is thrown for any errors. 
52 	 */ 
@Override
    public void close() throws Exception {
        LOG.info("ItemWriter - close method started");
     //   LOG.info("ItemWriter - records OK = " + recordsOK);
        String msg = "ItemWriter - records OK = " + recordsOK;
        lc.golfnew.BatchController.setRecordWritten(recordsOK);
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        msg = "ItemWriter - records errors = " + recordsKO;
       
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        DBConnection.closeQuietly(conn, null, null, null);
    }
    public boolean insertDB(PlayerCSV inputRecord) throws Exception
    {
    try{
      player = new Player();
      handicap = new Handicap();

      player.setIdplayer(inputRecord.getIdplayer() );   
  //      LOG.info("idplayer candidate toinsertion = " + player.getIdplayer());
      player.setPlayerFirstName(inputRecord.getPlayerFirstName());
      player.setPlayerLastName(inputRecord.getPlayerLastName());
      player.setPlayerCity(inputRecord.getPlayerCity());
      player.setPlayerCountry(inputRecord.getPlayerCountry());
      player.setPlayerBirthDate(inputRecord.getPlayerBirthDate() );
      player.setPlayerGender(inputRecord.getPlayerGender());
      player.setPlayerHomeClub(inputRecord.getPlayerHomeClub() );
      player.setPlayerPhotoLocation(inputRecord.getPlayerPhotoLocation());
      player.setPlayerLanguage(inputRecord.getPlayerLanguage());
      player.setPlayerEmail(inputRecord.getPlayerEmail());
          LOG.info("player to be inserted = " + player.toString());
      handicap.setHandicapStart(inputRecord.getHandicapStart() );
      handicap.setHandicapPlayer(inputRecord.getHandicapPlayer());
         LOG.info("handicap = " + handicap.toString());
         
        LOG.info("just before create player");
     create.CreatePlayer cp = new create.CreatePlayer();
   //     interfaces.PlayerDao cp = new interfaces.PlayerDao();
      boolean b = cp.createPlayer(player, handicap, conn, "B");
         LOG.info("boolean returned from create player = " + b);
      if(b == false) // new 20/10/2014
             {
                  LOG.error("Player NOT created !! ");
//                  return ExitStatus.COMPLETED;
      } else {
          LOG.error("Player created !! ");
      }
      return b;
    } //end try
 catch (IOException ex)
    { String msg = " -- Exception in insertDB !";
    	     LOG.error(msg);
              LCUtil.showMessageFatal(msg);
           return false;   
    }
    }
} // end ItemWriterPlayers