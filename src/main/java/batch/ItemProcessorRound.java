
package batch;

//import entite.Player;CSV;
import entite.Round;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("ItemProcessorRound")
public class ItemProcessorRound implements ItemProcessor, interfaces.Log 
{
    @Inject private JobContext jobContext;
    @Inject private Round round;
    
    @Override

public Object processItem(Object item) throws Exception {
try{
          LOG.debug("ProcessItem - Round = " + item.toString());
       Round inputRecord = (Round) item;
          LOG.debug("inputRecord =  " + inputRecord.toString() );
   //       Round outputRecord = new Round(); // momentanément pas utilisé
   //       outputRecord = inputRecord;
   //       outputRecord.setRoundCBA(0);
   //       outputRecord.setRoundQualifying(NEW_LINE);
   //       outputRecord
 //         out
        return inputRecord;
 }catch(Exception ioe)  {
          LOG.debug("Itemprocessor IOException :" + ioe);
          return null;
    }
    } //end method
} //end class