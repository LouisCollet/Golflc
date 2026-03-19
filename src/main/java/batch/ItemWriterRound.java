
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
    public void writeItems(List<Object> items) throws Exception {
        final String methodName = "ItemWriterRound.writeItems";
        LOG.debug("entering " + methodName + " - items to process: " + items.size());
        try {
            for (Object item : items) {
                Round inputRecord = (Round) item;
                LOG.debug(methodName + " - processing round: " + inputRecord.getRoundDate());
                if (insertDB(inputRecord)) {
                    recordsOK++;
                    LOG.debug(methodName + " - record written: " + inputRecord.getRoundDate());
                } else {
                    recordsKO++;
                    LOG.debug(methodName + " - record NOT written: " + inputRecord.getRoundDate());
                }
            }
            LOG.debug(methodName + " - batch complete: OK=" + recordsOK + ", KO=" + recordsKO);
        } catch (Exception ex) {
            LOG.error(methodName + " - Exception in writeItems: " + ex.getMessage());
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
