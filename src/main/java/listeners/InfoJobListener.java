
package listeners;

import java.io.Serializable;
import javax.batch.api.listener.JobListener;
import javax.batch.api.listener.StepListener;
import javax.batch.runtime.context.JobContext;
import javax.batch.runtime.context.StepContext;
import javax.enterprise.context.Dependent;
//import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.annotation.WebListener;

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
        LOG.info("Batch Job Starting " + jobCtx.getJobName());
    }
 
    @Override
    public void afterJob()
    { LOG.info("Batch Job Finished");}

    @Override
    public void beforeStep() throws Exception
    {
        LOG.info("Batch Start Step " + stepCtx.getStepName());
    }

    @Override
    public void afterStep() throws Exception {
        LOG.info("Batch End Step");
    }
    
  
    
    
}