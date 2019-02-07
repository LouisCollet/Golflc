
package batch;

import create.CreateInscription;
import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.InscriptionCSV;
import entite.Player;
import entite.Round;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import utils.DBConnection;

@Named("ItemWriterInscription")
public class ItemWriterInscription extends AbstractItemWriter implements interfaces.Log
{    
  //  @Inject
    private Round round;
    private Player player;
    private Club club;
    private Course course;
//    private PlayerHasRound playerhasround;
    private Inscription inscriptionNew;
    private InscriptionCSV inscription;
    private int recordsOK = 0;
    private int recordsKO = 0;
    private Connection conn; // = null;
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
 //   public void writeItems(List items) throws Exception
    public void writeItems(List<Object> items) throws Exception      // à valider 
 //   public void writeItems(List<PlayersCSV> items) throws Exception;
    { LOG.info("ItemWriter - writeItems method started");
    //// ÃƒÂ  modifier ici !!! prendre l'objet en input ???
    try{
        LOG.info("items to be treated =  " + items.size() );
        LOG.info("Print List items = " + Arrays.deepToString(items.toArray()));
        //for (Object obj : items)
         for(int i = 0; i < items.size(); i++)
        {
            InscriptionCSV inputRecord = (InscriptionCSV) items.get(i);
                LOG.info("input Record " + items.get(i));
            boolean bo = insertDB(inputRecord);
                LOG.info("resultat input Record " + bo + " for " + i);
            if(bo == true)
            {
                recordsOK++;
                LOG.info("records written = " + inputRecord.getIdround() );
            }else{
                recordsKO++;
                LOG.info("records NOT written = " + inputRecord.getIdround() );
            }
        }
    
    } //end try
    catch (Exception ex)
    { LOG.error(" -- Exception in ItemWriter !");
   	     LOG.error(ex);
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
        LOG.info("ItemWriter - records OK = " + recordsOK);
        lc.golfnew.BatchController.setRecordWritten(recordsOK);
        LOG.info("ItemWriter - records errors = " + recordsKO);
        DBConnection.closeQuietly(conn, null, null, null);
    }
    
    public boolean insertDB(InscriptionCSV inputRecord) throws Exception
  //    public boolean insertDB(Object inputRecord) throws Exception      
    {
    try{
        LOG.info("step 00");
        LOG.info("entering insertDB with inputRecord = " + inputRecord.toString());
      round = new Round();
      player = new Player();
  //    playerhasround = new PlayerHasRound();
      inscriptionNew = new Inscription();

      player.setIdplayer(inputRecord.getIdplayer() );
        LOG.info("step 01");
      round.setIdround(inputRecord.getIdround() );
        LOG.info("step 02");
//**//      playerhasround.setInscriptionTeam(inputRecord.getInscriptionTeam() );
        LOG.info("step 03");
   //       LOG.info("inscription = " + inscription.toString());
           LOG.info("step 10");
    //  CreateInscription ci = new CreateInscription();
      int b = new CreateInscription().create(round, player, player, inscriptionNew, club, course, conn);
         LOG.info("boolean returned from create inscription = " + b);
      if(b == 00) // new 20/10/2014
             {
                  LOG.error("inscription not created !!");
             } 
      return false;
    } //end try
 catch (Exception ex)
    { LOG.error(" -- Exception in insertDB !");
    	     LOG.error(ex);
           return false;   
    }


    }
} // end Class