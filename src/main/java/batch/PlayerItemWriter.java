
package batch;

import entite.composite.EPlayerHandicap;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

@Named("PlayerItemWriter")
public class PlayerItemWriter extends AbstractItemWriter {    
    private int recordsOK; // = 0;
    private int recordsKO; // = 0;
    private Connection conn; // = null;
    private @Inject Player player;
//    private @Inject HandicapIndex handicapIndex;
 //   private @Inject EPlayerHandicap eph;
  //  private static int count = 0;
/** 
33 	 * The open method prepares the writer to write items.34 	 *  
35 	 * The input parameter represents the last checkpoint 
36 	 * for this writer in a given job instance.The   
37 	 * checkpoint data is defined by this writer and is  
38 	 * provided by the checkpointInfo method. 
The checkpoint 
39 	 * data provides the writer whatever information it needs  
40 	 * to resume writing items upon restart. A checkpoint value  
41 	 * of null is passed upon initial start. 
42 	 *  
43 	 * @param checkpoint specifies the last checkpoint  
44 	 * @throws Exception is thrown for any errors. 
45 
     * @param checkpoint 
     * @throws java.lang.Exception */ 
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
         LOG.debug("ItemWriter - Open method started");
       conn = new DBConnection().getConnection();
       recordsOK = 0;
       recordsKO = 0;
    }
/* 
56 	 * The writeItems method writes a list of item for the current chunk.  
58 	 * @param items specifies the list of items to write. 
59 	 * @throws Exception is thrown for any errors. 
60 	 */ 


    @Override
 public void writeItems(List<Object> eph) throws Exception {
 //   LOG.debug("ItemWriter - writeItems method started for " + players.size());
    LOG.debug("ItemWriter - writeItems method started for " + eph.size());
    try{
        LOG.debug("number of items to be treated =  " + eph.size() );
  //      LOG.debug("Print List items = " + Arrays.deepToString(eph.toArray()));
        for(int i=0; i < eph.size(); i++) {
             EPlayerHandicap EPH = (EPlayerHandicap) eph.get(i);
                LOG.debug("input Player = " + EPH.getPlayer());
                LOG.debug("input HandicapIndex =  " + EPH.getHandicapIndex());
           if(new create.CreatePlayer().create(EPH.getPlayer(), EPH.getHandicapIndex(), conn, "B")){
              LOG.debug("Player created !! ");
           }else{  // batch indicator
             LOG.debug("boolean returned from create player = " + false);
             LOG.error("Player NOT created !! ");
           }
          } //end for   
    } //end try
  catch (Exception ex){
     String msg = " -- Exception in ItemWriter !" + ex;
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
        LOG.debug("ItemWriter - close method started");
     //   LOG.debug("ItemWriter - records OK = " + recordsOK);
        String msg = "ItemWriter - records OK = " + recordsOK;
        Controllers.BatchController.setRecordWritten(recordsOK);
        LOG.debug(msg);
        msg = "ItemWriter - records errors = " + recordsKO;
        LOG.debug(msg);
        DBConnection.closeQuietly(conn, null, null, null);
    } // end close
} // end class ItemWriterPlayers