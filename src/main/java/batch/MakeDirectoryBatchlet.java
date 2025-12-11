
package batch;

import java.io.File;
import java.util.Properties;
import jakarta.batch.api.Batchlet;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("MakeDirectoryBatchlet")
public class MakeDirectoryBatchlet implements Batchlet, interfaces.Log {

    @Inject
    JobContext jobCtx;

    @Override
    public String process() throws Exception {
        Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        String downloadDirectoryRoot = jobCtx.getProperties().getProperty("downloadDirectory");
        LOG.debug("Create temporary download directory "+downloadDirectoryRoot +"/job"+jobCtx.getExecutionId());
        new File(downloadDirectoryRoot +"/job"+jobCtx.getExecutionId()).mkdirs();
        return "done";
    }

    @Override
    public void stop() throws Exception {
        
    }
  
    public MakeDirectoryBatchlet() {}
    
    
}
