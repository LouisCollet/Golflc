/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ByeJob implements Job{ 
	
	private ByeService bs = new ByeService();
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		bs.sayGoodbye();
	} 
}
