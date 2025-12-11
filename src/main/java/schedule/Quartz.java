
package schedule;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
/*https://examples.javacodegeeks.com/enterprise-java/quartz/quartz-helloworld-example/
Run the application
If you run MyApp.java class, the result will be something like the output below:
As you can see, the HelloJob.java is executed every second for 30 seconds,
whereas the ByeJob.java is executed every 5 seconds forever.
*/

public class Quartz {
void main() {
try {
		JobDetail job1 = JobBuilder.newJob(HelloJob.class).withIdentity("helloJob", "group1").build();

		Trigger trigger1 = TriggerBuilder.newTrigger()
                        .withIdentity("simpleTrigger", "group1")
			.withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(30))
                        .build();   
		Scheduler scheduler1 = new StdSchedulerFactory().getScheduler(); 
		scheduler1.start(); 
		scheduler1.scheduleJob(job1, trigger1); 
			
		JobDetail job2 = JobBuilder.newJob(ByeJob.class)
                                .withIdentity("byeJob", "group2")
                                .build();
		Trigger trigger2 = TriggerBuilder.newTrigger()
                                .withIdentity("cronTrigger", "group2")
				.withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                                .build();
		Scheduler scheduler2 = new StdSchedulerFactory().getScheduler();
		scheduler2.start(); 
		scheduler2.scheduleJob(job2, trigger2); 
}catch(Exception e){ 
	e.printStackTrace();
	}
}

} //end Class