
package batch;

import entite.PlayerCSV;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named("ItemProcessorPlayer")
public class ItemProcessorPlayer implements javax.batch.api.chunk.ItemProcessor, interfaces.Log 
{
    @Inject  private JobContext jobContext;
    @Inject  private PlayerCSV playerCSV;
    
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
//To filter a record, one simply returns "null" from the ItemProcessor.
            //The framework will detect that the result is "null"
            //and avoid adding that item to the list of records delivered to the ItemWriter.
            //As usual, an exception thrown from the ItemProcessor will result in a skip.
public Object processItem(Object item) throws Exception 
    {
try{
       LOG.info("ItemProcessorPlayer-ProcessItem - playerCSV = " + item.toString());
          PlayerCSV inputRecord = (PlayerCSV) item;
          // pas de manipulation ni filtrage
       //   PlayerCSV outputRecord = new PlayerCSV(); // momentanément pas utilisé
          PlayerCSV outputRecord = inputRecord; // momentanément pas utilisé
        return outputRecord;
}catch(Exception e){
          LOG.info("Itemprocessor Exception :" + e);
          return null;
    }
    } //end method
} //end class
