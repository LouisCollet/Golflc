
package batch;

import entite.Club;
import entite.Course;
import entite.Inscription;
import entite.InscriptionCSV;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named("ItemWriterInscription")
public class ItemWriterInscription extends AbstractItemWriter {
    @Inject private create.CreateInscription createInscriptionService; // migrated 2026-02-25
    private Round round;
    private Player player;
    private Club club;
    private Course course;
    private Inscription inscriptionNew;
    private InscriptionCSV inscription;
    private int recordsOK = 0;
    private int recordsKO = 0;
    // private Connection conn; // removed 2026-02-26 — CDI migration

    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOG.debug("ItemWriter - Open method started");
        // DBConnection removed 2026-02-26 — CDI services use @Resource DataSource
    }

    @Override
    public void writeItems(List<Object> items) throws Exception{
     LOG.debug("ItemWriter - writeItems method started");
    try{
        LOG.debug("items to be treated =  " + items.size() );
        LOG.debug("Print List items = " + Arrays.deepToString(items.toArray()));
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
    }
    } // end method

@Override
    public void close() throws Exception {
           LOG.debug("ItemWriter - close method started");
           LOG.debug("ItemWriter - records OK = " + recordsOK);
        Controllers.BatchController.setRecordWritten(recordsOK);
           LOG.debug("ItemWriter - records errors = " + recordsKO);
        // DBConnection.closeQuietly(conn, null, null, null); // removed 2026-02-26
    }

    public boolean insertDB(InscriptionCSV inputRecord) throws Exception{
  try{
        LOG.debug("step 00");
        LOG.debug("entering insertDB with inputRecord = " + inputRecord.toString());
      round = new Round();
      player = new Player();
      inscriptionNew = new Inscription();

      player.setIdplayer(inputRecord.getIdplayer() );
        LOG.debug("step 01");
      round.setIdround(inputRecord.getIdround() );
        LOG.debug("step 02");
        LOG.debug("step 03");
           LOG.debug("step 10");

   var b = createInscriptionService.create(round, player, player, inscriptionNew, club, course, "B"); // migrated 2026-02-25
      LOG.debug("boolean returned from create inscription = " + b);
      if(b.isInscriptionError()){
                  LOG.error("inscription not created !!");
      }
      return false;
    } //end try
 catch (Exception ex)
    { LOG.error(" -- Exception in insertDB !");
    	     LOG.error(ex);
           return false;
    }
 } //end method
} // end Class
