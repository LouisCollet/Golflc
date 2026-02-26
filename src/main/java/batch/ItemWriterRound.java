
package batch;
import static interfaces.Log.LOG;
import entite.Club;
import entite.Course;
import entite.Round;
import entite.UnavailablePeriod;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named("ItemWriterRound")
public class ItemWriterRound extends AbstractItemWriter {
  //  @Inject
    private Round round;
    private Course course;
 //   private Handicap handicap;
    private int recordsOK = 0;
    private int recordsKO = 0;
    // private Connection conn; // removed 2026-02-26 — CDI migration

    @Inject private create.CreateRound createRoundService; // migrated 2026-02-26

    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOG.debug("ItemWriter - Open method started");
        // DBConnection removed 2026-02-26 — CDI services use @Resource DataSource
    }

    @Override
    public void writeItems(List<Object> items) throws Exception
    { LOG.debug("ItemWriter - writeItems method started for " + items.size());
    try{
        LOG.debug("items to be treated =  " + items.size() );
        LOG.debug("Print List items = " + Arrays.deepToString(items.toArray()));
         for(int i = 0; i < items.size(); i++) {
            Round inputRecord = (Round) items.get(i);
                LOG.debug("input Record " + items.get(i));
            boolean bo = insertDB(inputRecord);
                LOG.debug("resultat input Record " + bo + " for " + i);
            if(bo == true)
            {
                recordsOK++;
                LOG.debug("records written = " + inputRecord.getRoundDate() );
            }else{
                recordsKO++;
                LOG.debug("records NOT written = " + inputRecord.getRoundDate() );
            }
        }

    } //end try
    catch (Exception ex)
    { LOG.error(" -- Exception in ItemWriter !");
   	     LOG.error(ex);
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

    public boolean insertDB(Round inputRecord) throws Exception {
    try{
        LOG.debug("step 00");
        LOG.debug("entering insertDB with inputRecord = " + inputRecord.toString());
      round = new Round();
      course = new Course();

      round.setRoundDate(inputRecord.getRoundDate());
      round.setRoundGame(inputRecord.getRoundGame() );
      round.setRoundCBA(Short.parseShort("0") );
      round.setRoundName(inputRecord.getRoundName() );
      round.setRoundQualifying("N");
      round.setRoundHoles(Short.parseShort("18") );
      round.setRoundStart(Short.parseShort("1") );
          LOG.debug("round = " + round.toString());
      course.setIdcourse(94); // course gleneagles pour ryder cup 2014
          LOG.debug("course = " + course.toString());
    UnavailablePeriod unavailable = new UnavailablePeriod();
    Club club = null; // fake !!
      // boolean b = new CreateRound().create(round, course, club, unavailable);
      boolean b = createRoundService.create(round, course, club, unavailable); // migrated 2026-02-26
         LOG.debug("boolean returned from create round = " + b);
      if(b == false)
             {
                  LOG.error("Round NOT created ! ");
             }
      return b;
    } //end try
 catch (Exception ex){
     LOG.error(" -- Exception in insertDB !");
    	     LOG.error(ex);
           return false;
    }
    }
} // end Class
