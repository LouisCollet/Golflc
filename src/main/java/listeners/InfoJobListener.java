
package listeners;

import java.io.Serializable;
import jakarta.batch.api.listener.JobListener;
import jakarta.batch.api.listener.StepListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
//import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.PhaseListener;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.annotation.WebListener;

@Dependent
@Named("InfoJobListener")
public class InfoJobListener implements Serializable, JobListener, StepListener, interfaces.Log
{

//@Dependent
//@Named("InfoJobListener")
//public class InfoJobListener Serializable, JobListener, StepListener, interfaces.Log//
//{

     @Inject
    JobContext jobCtx;

      @Inject
    StepContext stepCtx;

    
    @Override
    public void beforeJob() throws Exception {
        LOG.debug("Batch Job Starting " + jobCtx.getJobName());
    }
 
    @Override
    public void afterJob()
    { LOG.debug("Batch Job Finished");}

    @Override
    public void beforeStep() throws Exception
    {
        LOG.debug("Batch Start Step " + stepCtx.getStepName());
    }

    @Override
    public void afterStep() throws Exception {
        LOG.debug("Batch End Step");
    }
    
  
    
    
}