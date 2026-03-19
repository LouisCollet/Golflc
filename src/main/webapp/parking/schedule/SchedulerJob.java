
package schedule;
import static interfaces.Log.LOG;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
//https://mkyong.com/jsf2/jsf-2-quartz-2-example/

// started from quartz-config.xml
// quartz.properties : org.quartz.plugin.jobInitializer.fileNames = quartz-config.xml
public class SchedulerJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("toutes les heures - from SchedulerJob.java JSF 2.3 + Quartz 2.32 example");
    }
}