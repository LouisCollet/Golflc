
package batch;

import java.util.Properties;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("RemoveDirectoryBatchlet")
public class RemoveDirectoryBatchlet implements Batchlet,interfaces.Log {

    @Inject
    JobContext jobCtx;

    @Override
    public String process() throws Exception {
        Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        String downloadDirectoryRoot = jobCtx.getProperties().getProperty("downloadDirectory");
            LOG.debug("Remove directory " + downloadDirectoryRoot + "/job" + jobCtx.getExecutionId());

        return "done";
    }

    @Override
    public void stop() throws Exception {
        
    }
  
    public RemoveDirectoryBatchlet() {}
    
    
}
