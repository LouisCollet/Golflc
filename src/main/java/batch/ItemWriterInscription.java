
package batch;

import create.CreateInscription;
import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.InscriptionCSV;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;

@Named("ItemWriterInscription")
public class ItemWriterInscription extends AbstractItemWriter {    
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
        LOG.debug("ItemWriter - Open method started");
        // here open DB
        DBConnection dbc = new DBConnection();
        conn = dbc.getConnection();
    }
/* 
56 	 * The writeItems method writes a list of item for the current chunk.  
58 	 * @param items specifies the list of items to write. 
59 	 * @throws Exception is thrown for any errors. 
60 	 */ 

    /**
     * 56 	 * The writeItems method writes a list of item for the current chunk.58 	 * @param items specifies the list of items to write.59 	 * @throws Exception is thrown for any errors. 
60
     * @param items
     * @throws java.lang.Exception
     */
    @Override
 //   public void writeItems(List items) throws Exception
    public void writeItems(List<Object> items) throws Exception{
 //   public void writeItems(List<PlayersCSV> items) throws Exception;
     LOG.debug("ItemWriter - writeItems method started");
    //// ÃƒÂ  modifier ici !!! prendre l'objet en input ???
    try{
        LOG.debug("items to be treated =  " + items.size() );
        LOG.debug("Print List items = " + Arrays.deepToString(items.toArray()));
        //for (Object obj : items)
         for(int i = 0; i < items.size(); i++) {
            InscriptionCSV inputRecord = (InscriptionCSV) items.get(i);
                LOG.debug("input Record " + items.get(i));
            boolean bo = insertDB(inputRecord);
                LOG.debug("resultat input Record " + bo + " for " + i);
            if(bo == true) {
                recordsOK++;
                LOG.debug("records written = " + inputRecord.getIdround() );
            }else{
                recordsKO++;
                LOG.debug("records NOT written = " + inputRecord.getIdround() );
            }
        }
    
    } catch (Exception ex) { 
        LOG.error(" -- Exception in ItemWriter !" + ex);
   //	     LOG.error(ex);
    }
    } // end method
/*
48 	 * The close method marks the end of use of the ItemWriter.
*       The writer is free to do any cleanup necessary. 
51 	 * @throws Exception is thrown for any errors. 
52 	 */ 
@Override
    public void close() throws Exception {
           LOG.debug("ItemWriter - close method started");
           LOG.debug("ItemWriter - records OK = " + recordsOK);
        Controllers.BatchController.setRecordWritten(recordsOK);
           LOG.debug("ItemWriter - records errors = " + recordsKO);
        DBConnection.closeQuietly(conn, null, null, null);
    }
    
    public boolean insertDB(InscriptionCSV inputRecord) throws Exception{
  //    public boolean insertDB(Object inputRecord) throws Exception      
  try{
        LOG.debug("step 00");
        LOG.debug("entering insertDB with inputRecord = " + inputRecord.toString());
      round = new Round();
      player = new Player();
  //    playerhasround = new PlayerHasRound();
      inscriptionNew = new Inscription();

      player.setIdplayer(inputRecord.getIdplayer() );
        LOG.debug("step 01");
      round.setIdround(inputRecord.getIdround() );
        LOG.debug("step 02");
//**//      playerhasround.setInscriptionTeam(inputRecord.getInscriptionTeam() );
        LOG.debug("step 03");
   //       LOG.debug("inscription = " + inscription.toString());
           LOG.debug("step 10");
    //  CreateInscription ci = new CreateInscription();
  //    int b = new CreateInscription().create(round, player, player, inscriptionNew, club, course, "B", conn);
   var b = new CreateInscription().create(round, player, player, inscriptionNew, club, course, "B", conn);
      LOG.debug("boolean returned from create inscription = " + b);
      if(b.isInscriptionError()){
                  LOG.error("inscription not created !!");
      } 
      return false;
    } //end try //end try
 catch (Exception ex)
    { LOG.error(" -- Exception in insertDB !");
    	     LOG.error(ex);
           return false;   
    }
 } //end method
} // end Class