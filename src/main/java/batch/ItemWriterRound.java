
package batch;

import create.CreateRound;
import entite.Club;
import entite.Course;
import entite.Round;
import entite.UnavailablePeriod;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import utils.DBConnection;

@Named("ItemWriterRound")
public class ItemWriterRound extends AbstractItemWriter implements interfaces.Log{    
  //  @Inject
    private Round round;
    private Course course;
 //   private Handicap handicap;
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
     * 56 	 * The writeItems method writes a list of item for the current chunk.58 	 * @param items specifies the list of items to write. 
59 	 * @throws Exception is thrown for any errors. 
60
     * @throws java.lang.Exception
     */
    @Override
 //   public void writeItems(List items) throws Exception
    public void writeItems(List<Object> items) throws Exception      // à valider 
 //   public void writeItems(List<PlayersCSV> items) throws Exception;
    { LOG.debug("ItemWriter - writeItems method started for " + items.size());
    //// ÃƒÂ  modifier ici !!! prendre l'objet en input ???
    try{
        LOG.debug("items to be treated =  " + items.size() );
        LOG.debug("Print List items = " + Arrays.deepToString(items.toArray()));
        //for (Object obj : items)
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
    
    public boolean insertDB(Round inputRecord) throws Exception {
    try{
        LOG.debug("step 00");
        LOG.debug("entering insertDB with inputRecord = " + inputRecord.toString());
      round = new Round();
      course = new Course();

      round.setRoundDate(inputRecord.getRoundDate());
 //       LOG.debug("step 01");
      round.setRoundGame(inputRecord.getRoundGame() );
//        LOG.debug("step 02");
      round.setRoundCBA(Short.parseShort("0") );
 //       LOG.debug("step 03");
      round.setRoundName(inputRecord.getRoundName() );
 //      LOG.debug("step 04");
      round.setRoundQualifying("N");
//       LOG.debug("step 05");
      round.setRoundHoles(Short.parseShort("18") );
//       LOG.debug("step 06");
      round.setRoundStart(Short.parseShort("1") );
//       LOG.debug("step 07");
          LOG.debug("round = " + round.toString());
//           LOG.debug("step 08");
      course.setIdcourse(94); // course gleneagles pour ryder cup 2014
//       LOG.debug("step 09");
          LOG.debug("course = " + course.toString());
 //          LOG.debug("step 10");
    //  CreateRound cr = new CreateRound();
    UnavailablePeriod unavailable = new UnavailablePeriod();
    // à modifier !!
    Club club = null; // fake !!
      boolean b = new CreateRound().create(round, course, club, unavailable, conn);
         LOG.debug("boolean returned from create round = " + b);
      if(b == false) // new 20/10/2014
             {
                  LOG.error("Round NOT created ! ");
             } 
      return b;
    } //end try //end try
 catch (Exception ex){
     LOG.error(" -- Exception in insertDB !");
    	     LOG.error(ex);
           return false;   
    }
    }
} // end Class