
package startup;

import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Properties;
import jakarta.annotation.PreDestroy;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import schedule.MonJob;

//@Startup   //enlevé 15-08-2021
//@Singleton
//@DependsOn("StartupBean")

// pas utilisé ??
public class SchedulerBean {
    private Scheduler scheduler;

@PostConstruct
public void scheduleJobs() {
   try {
            LOG.debug("entering schedulejobs");
            LOG.debug("starting quartz Scheduler");   // new 15-06-2021

    ClassLoader clo = Thread.currentThread().getContextClassLoader();
    Properties p;
       try (InputStream is = clo.getResourceAsStream("quartz.properties")) {
           p = new Properties();
           p.load(is);
        }
    utils.LCUtil.printProperties("quartz.properties");
    utils.LCUtil.printProperties("c3p0.properties");
//    String db_connection = p.getProperty("jdbc.mysql");
//    prop.load(AnyClassUsedByJVM.class.getClassLoader().getResourceAsStream("quartz.properties"));
   String js = p.getProperty("org.quartz.jobStore.class");
    LOG.debug("jobStore = " + js);
/*   
    LOG.debug("starting SchedulereController.run = " + js);
   Controllers.SchedulerController sc = new Controllers.SchedulerController();
   sc.run();
   sc.list();
     */       
            scheduler = new StdSchedulerFactory().getScheduler();
  //          scheduler.setJobFactory(cdiJobFactory);

            JobKey job1Key = JobKey.jobKey("job1", "my-jobs");
            JobDetail job1 = JobBuilder
                    .newJob(MonJob.class)
                    .withIdentity(job1Key)
                    .build();

            TriggerKey tk1 = TriggerKey.triggerKey("trigger1", "my-jobs");
            Trigger trigger1 = TriggerBuilder
                    .newTrigger()
                    .withIdentity(tk1)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(10))
                    .build();

            scheduler.start(); // start before scheduling jobs
            scheduler.scheduleJob(job1, trigger1);

            printJobsAndTriggers(scheduler);

        } catch (SchedulerException e) {
            LOG.error("Error while creating scheduler", e);
   } catch (Exception e) {
            LOG.error("Error while creating scheduler", e);
        }
   
   
    }
 //       }
    private void printJobsAndTriggers(Scheduler scheduler) throws SchedulerException {
        LOG.info("Quartz Scheduler: {}", scheduler.getSchedulerName());
        for(String group: scheduler.getJobGroupNames()) {
            for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                LOG.debug("Found job identified by {}", jobKey);
            }
        }
        for(String group: scheduler.getTriggerGroupNames()) {
            for(TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(group))) {
                LOG.debug("Found trigger identified by {}", triggerKey);
            }
        }
    }

    @PreDestroy
    public void stopJobs() {
        LOG.debug("entering stopJobs");
        if (scheduler != null) {
            try {
      //          SchedulerController.main("stop");  // string []
                scheduler.shutdown(false);
            } catch (SchedulerException e) {
                LOG.error("Error while closing scheduler", e);
            }
        }
    }
} // end class