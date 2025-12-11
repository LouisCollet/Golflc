
package schedule;
import static interfaces.Log.LOG;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import static utils.LCUtil.showMessageFatal;

public class MonJob implements Job {

  @Override
 public void execute(final JobExecutionContext context) throws JobExecutionException {
//       LOG.debug("MonJob : entering execute");
try{
    final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    
    final String monParam = jobDataMap.getString("param");
    LOG.debug("MonJob Execution pour param  " + monParam); 
    final JobKey keyParam = context.getJobDetail().getKey();
     LOG.debug("MonJob Execution de key " + keyParam);
     
    final String subject = jobDataMap.getString("subject");
    LOG.debug(" et pour subject  =   " + subject);
    
    final JobKey keySubject = context.getJobDetail().getKey();
    LOG.debug(" et pour keySubject = " + keySubject);

    String jobSays = jobDataMap.getString("jobSays");
    LOG.debug("MonJob Execution pour jobSays  " + jobSays); 
    final JobKey keyjobSays = context.getJobDetail().getKey();
    LOG.debug(" et pour keySubject = " + keyjobSays);
    
    float myFloatValue = jobDataMap.getFloat("myFloatValue");
    LOG.debug("MonJob Execution pour myFloatValue  " + myFloatValue); 
    
    final JobKey keyFloatValue = context.getJobDetail().getKey();
       LOG.debug("MonJob Execution pour keyFloatValue  " + keyFloatValue); 
 //        LOG.debug("exiting monJob.execute()");
  }catch (Exception e){
      String msg = "Fatal Exception in monJob execute : "  + e;
	LOG.error(msg);
        showMessageFatal(msg);
  }
  } // end method execute  
} //end Class