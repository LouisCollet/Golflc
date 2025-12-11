package batch;

import static interfaces.GolfInterface.ZDF_TIME;
import static interfaces.Log.LOG;
import jakarta.batch.api.listener.JobListener;
import jakarta.inject.Named;
import java.time.LocalDateTime;

@Named("InfoJobListener")
public class InfoJobListener implements JobListener {
// The beforeJob method receives control before the job execution begins.
    @Override
    public void beforeJob() throws Exception {
        LOG.debug("The job is starting at " + LocalDateTime.now().format(ZDF_TIME));
    }
// The afterJob method receives control after the job execution ends.
    @Override
    public void afterJob() throws Exception {
        LOG.debug("The job is finished at " + LocalDateTime.now().format(ZDF_TIME));
    }
}