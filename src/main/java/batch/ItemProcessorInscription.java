
package batch;

import entite.InscriptionCSV;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("ItemProcessorInscription")
public class ItemProcessorInscription implements ItemProcessor{
    @Inject private JobContext jobContext;
    @Inject private InscriptionCSV inscription;
    @Override


public Object processItem(Object item) throws Exception{
try{
          LOG.debug("ProcessItem - Round = " + item.toString());
       InscriptionCSV inputRecord = (InscriptionCSV) item;
          LOG.debug("inputRecord =  " + inputRecord.toString() );
   //       Round outputRecord = new Round(); // momentanément pas utilisé
   //       outputRecord = inputRecord;
   //       outputRecord.setRoundCBA(0);
   //       outputRecord.setRoundQualifying(NEW_LINE);
   //       outputRecord
 //         out
        return inputRecord;
}catch(Exception ioe){
          LOG.debug("Itemprocessor IOException :" + ioe);
          return null;
    }
} //end method
} //end class