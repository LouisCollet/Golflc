
package batch;

//import entite.PlayerCSV;
import entite.Round;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named("ItemProcessorRound")
public class ItemProcessorRound implements javax.batch.api.chunk.ItemProcessor, interfaces.Log 
{
    @Inject
    private JobContext jobContext;
    @Inject
    private Round round;
    
    @Override
 //   public Object processItem(Object PlayerCSV) throws Exception   // petit p ?
    /** 
29 	 * The processItem method is part of a chunk 
30 	 * step. It accepts an input item from an 
31 	 * item reader and returns an item that gets 
32 	 * passed onto the item writer. Returning null  
33      * indicates that the item should not be continued  
34      * to be processed.  This effectively enables processItem  
35 	 * to filter out unwanted input items. 
36 	 * @param item specifies the input item to process. 
37 	 * @return output item to write. 
38 	 * @throws Exception thrown for any errors.public
* Object processItem(Object item) throws Exception  
39 	 */ 

public Object processItem(Object item) throws Exception 
    {
try{
          LOG.info("ProcessItem - Round = " + item.toString());
       Round inputRecord = (Round) item;
          LOG.info("inputRecord =  " + inputRecord.toString() );
   //       Round outputRecord = new Round(); // momentanément pas utilisé
   //       outputRecord = inputRecord;
   //       outputRecord.setRoundCBA(0);
   //       outputRecord.setRoundQualifying(NEW_LINE);
   //       outputRecord
 //         out
        return inputRecord;
        }
catch(Exception ioe)
    {
          LOG.info("Itemprocessor IOException :" + ioe);
          return null;
    }
    } //end method
} //end class
