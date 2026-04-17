
package Controllers;

import static interfaces.Log.LOG;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

// @Named("schedulerC")
// @SessionScoped 
//@ApplicationScoped  // ?? voir solution avec config files ??
public class SchedulerQuartzController implements Serializable{
   Scheduler scheduler = null;
    
 public void stop() throws Exception {
        LOG.debug("entering stop scheduler");
        LOG.debug("with scheduler = {}", scheduler.getSchedulerName());
    for (JobExecutionContext currentlyExecutingJob : scheduler.getCurrentlyExecutingJobs()) {
       if (InterruptableJob.class.isAssignableFrom(currentlyExecutingJob.getJobDetail().getJobClass())) // Otherwise it will throw an exception
        scheduler.interrupt(currentlyExecutingJob.getFireInstanceId());
       
    }
         LOG.debug("exiting stop scheduler");
}
    
  public void list() throws Exception {
        LOG.debug("entering list scheduler");
          LOG.debug("with scheduler = {}", scheduler.getSchedulerName());
       for(String group: scheduler.getJobGroupNames()) {
         for(JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
           LOG.debug("list Jobkeys = {}", jobKey);
         }
      }
      for(String group: scheduler.getTriggerGroupNames()) {
         for(TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
           LOG.debug("list Triggerkeys = {}", triggerKey);
         }
      }
         LOG.debug("exiting list scheduler");
}
 
public void run() throws Exception {
  try {
/*			Trigger trigger = TriggerBuilder.newTrigger()
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInSeconds(5)
                                        .repeatForever())
					.build();
			
 */
LOG.debug("entering run scheduler");
       JobDataMap jobDataMap = new JobDataMap();
       jobDataMap.put("param", "12345 param ");
       jobDataMap.put("subject", "12345 subject");
   // define the job and tie it to MonJob class
      final JobDetail jobDetail = JobBuilder
          .newJob(schedule.MonJob.class)
          .withIdentity("monJob", "groupe_1")
          .withDescription("By LC Send Email Job") 
          .usingJobData(jobDataMap)
          .usingJobData("jobSays", "Hello World!")
          .usingJobData("myFloatValue", 3.141f)
          .build();
// specify job trigger
      final Trigger cronTrigger = TriggerBuilder
          .newTrigger()
          .withIdentity("monTrigger", "groupe_1")
          .withSchedule(
              CronScheduleBuilder.cronSchedule("0/5 * * * * ?")  // toutes les 5 secondes 
                .inTimeZone(TimeZone.getTimeZone("Europe/Paris"))
                .withMisfireHandlingInstructionFireAndProceed())
          .withDescription("cronTrigger Description 2")
          .build();
// overinding properties in quatz.properties
// https://medium.com/viithiisys/quartz-scheduler-with-mysql-database-506a608cf7a8

ClassLoader clo = Thread.currentThread().getContextClassLoader();
    Properties p;
       try (InputStream is = clo.getResourceAsStream("quartz.properties")) {
           p = new Properties();
           p.load(is);
        }
    utils.LCUtil.printProperties("quartz.properties");
    // essai de modifiction des properties
 //     Properties prop = new Properties();
 
      p.replace("org.quartz.scheduler.skipUpdateCheck", "true");
      p.replace("org.quartz.dataSource.quartzDataSource.maxConnections", "2");
      p.replace("org.quartz.threadPool.threadCount", "10");
      LOG.debug(" properties modified = ");
  
      utils.LCUtil.printProperties("quartz.properties");
         LOG.debug(" line 00");
      scheduler = new StdSchedulerFactory(p).getScheduler();
         LOG.debug(" line 01");
 //        LOG.debug(" line 03");
      scheduler.start();
   // Tell quartz to schedule the job using our trigger         
      scheduler.scheduleJob(jobDetail, cronTrigger);
  //    LOG.debug(" line 04");
      LOG.debug("line 05 ");  // boucle !!
 //       char letter = (char) System.in.read();
//        LOG.debug("You typed the letter {}", letter);
   //   if (scheduler != null) {
////        scheduler.shutdown();
   //   }
    } catch (final SchedulerException e) {
      LOG.debug("Scheduler Exception {}", e);
    } catch (final Exception e) {
      LOG.debug("Exception {}", e);
    }
  
// catch (final IOException e) {
//      LOG.debug("IOException Exception {}", e);;
 //   }
} //end run

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
    } // end main
    */

} // end class