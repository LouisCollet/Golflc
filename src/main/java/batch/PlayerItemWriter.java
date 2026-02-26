
package batch;

import entite.composite.EPlayerHandicap;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import utils.LCUtil;

@Named("PlayerItemWriter")
public class PlayerItemWriter extends AbstractItemWriter {
    private int recordsOK; // = 0;
    private int recordsKO; // = 0;
    // private Connection conn; // removed 2026-02-26 — CDI migration
    private @Inject Player player;
//    private @Inject HandicapIndex handicapIndex;
 //   private @Inject EPlayerHandicap eph;
  //  private static int count = 0;

    @Inject private create.CreatePlayer createPlayerService; // migrated 2026-02-26

    @Override
    public void open(Serializable checkpoint) throws Exception {
         LOG.debug("ItemWriter - Open method started");
       // conn = new DBConnection().getConnection(); // removed 2026-02-26
       recordsOK = 0;
       recordsKO = 0;
    }

    @Override
 public void writeItems(List<Object> eph) throws Exception {
    LOG.debug("ItemWriter - writeItems method started for " + eph.size());
    try{
        LOG.debug("number of items to be treated =  " + eph.size() );
        for(int i=0; i < eph.size(); i++) {
             EPlayerHandicap EPH = (EPlayerHandicap) eph.get(i);
                LOG.debug("input Player = " + EPH.player());
                LOG.debug("input HandicapIndex =  " + EPH.handicapIndex());
             // if(new create.CreatePlayer().create(EPH.player(), EPH.handicapIndex(),"B")){
             if(createPlayerService.create(EPH.player(), EPH.handicapIndex(),"B")){ // migrated 2026-02-26
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

@Override
    public void close() throws Exception {
        LOG.debug("ItemWriter - close method started");
        String msg = "ItemWriter - records OK = " + recordsOK;
        Controllers.BatchController.setRecordWritten(recordsOK);
        LOG.debug(msg);
        msg = "ItemWriter - records errors = " + recordsKO;
        LOG.debug(msg);
        // DBConnection.closeQuietly(conn, null, null, null); // removed 2026-02-26
    } // end close
} // end class ItemWriterPlayers
