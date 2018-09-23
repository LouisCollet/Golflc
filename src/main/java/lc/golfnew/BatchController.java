
package lc.golfnew;

import entite.Batch;
import static interfaces.GolfInterface.sdf_timeHHmmss;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.JobInstance;
import javax.batch.runtime.StepExecution;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("batchC")
@SessionScoped
public class BatchController implements Serializable, interfaces.Log
{
    
    private static JobOperator jo = BatchRuntime.getJobOperator();
    private long execID;
    private List<Batch> listeBatch;// = new ArrayList<>();
    @Inject private Batch batch;
    private static int recordWritten;
    private static int recordReaded;
    
    /*
public long startNewBatchJob() throws Exception 
    {
            LOG.info("starting startNewBatchJob ...");
        String JOB_NAME = "SimplePayrollJob";
        // The jobname is nothing but the job JSL XML file name (minus the .xml extension)       
        jo = BatchRuntime.getJobOperator();
        
        Properties props = new Properties();
        props.setProperty("payrollInputDataFileName", "c:/log/work_golflc.log");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
            LOG.info("exiting startnewbatchjob ...");
        return jo.start(JOB_NAME, props);
}
*/
// public long startBatchJobPLayers() throws Exception
/* Submit the batch job to the batch runtime.
 * JSF Navigation method (return the name of the next page) */
    
        public BatchController()  // constructor
    {
      //  this.listavg = null;
      //  this.setSunRiseSet(null);
    }
    
    @PostConstruct
    public void init() // attention !! ne peut absolument pas avoir : throws SQLException
    {
    listeBatch = new ArrayList<>();
    }
    
public String startBatchJobPlayers() throws Exception
{
    LOG.info("starting startBatchJobPlayers ...");
  String JOB_NAME = "GolfPlayers"; // The jobname is the job JSL XML file name (minus the .xml extension)       
  jo = BatchRuntime.getJobOperator();
  Properties props = new Properties();
  
//  props.setProperty("input_file", "C:/Users/collet/Documents/NetBeansProjects/GolfWfly/importPlayers.txt"); Arrays.deepToString(executedSteps.toArray()))
  props.setProperty("input_file", "Constants.USER_DIR" + "importPlayers.txt");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
  execID = jo.start(JOB_NAME, props);
  
  Batch ba = new Batch();
  ba.setExecID(execID);
 //   LOG.info("execID long setted = " + ba.getExecID());
 //   LOG.info("execID String setted = " + ba.getStringID());
    LOG.info("ba is now " + ba.toString());
  listeBatch.add(ba);
      //   LOG.info("exiting startBatchJobPlayers with execID = " + execID) + listeBatch.;
         LOG.info("liste execID = " + Arrays.toString(listeBatch.toArray()));
    return null;  // next page ici = retour a la page de depart
}

    public static int getRecordWritten() {
        return recordWritten;
    }

    public static void setRecordWritten(int recordWritten) {
        BatchController.recordWritten = recordWritten;
    }

    public static int getRecordReaded() {
        return recordReaded;
    }

    public static void setRecordReaded(int recordReaded) {
        BatchController.recordReaded = recordReaded;
    }




    public List<Batch> getListeBatch() {
        return listeBatch;
    }

    public void setListeBatch(List<Batch> listeBatch) {
        this.listeBatch = listeBatch;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

public String startBatchJobRounds() throws Exception
{
    LOG.info("starting startBatchJobRounds ...");
        String JOB_NAME = "GolfRounds"; // The jobname is nothing but the job JSL XML file name (minus the .xml extension)       
        jo = BatchRuntime.getJobOperator();
        Properties props = new Properties();
 ////       props.setProperty("input_file", "C:/Users/collet/Documents/NetBeansProjects/GolfNew/importPlayers.txt");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
        execID = jo.start(JOB_NAME, props);
        Batch ba = new Batch();
        ba.setExecID(execID);
        listeBatch.add(ba);
         LOG.info("exiting startBatchJobRounds with execID = " + execID);
         LOG.info("liste execID = " + Arrays.toString(listeBatch.toArray()));
        return null;  // next page ici = retour a la page de depart
}
public String startBatchJobInscriptions() throws Exception
{
    LOG.info("starting startBatchJobInscriptions ...");
        String JOB_NAME = "GolfInscriptions"; // The jobname is nothing but the job JSL XML file name (minus the .xml extension)       
        jo = BatchRuntime.getJobOperator();
        Properties props = new Properties();
 ////       props.setProperty("input_file", "C:/Users/collet/Documents/NetBeansProjects/GolfNew/importPlayers.txt");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
        execID = jo.start(JOB_NAME, props);
        Batch ba = new Batch();
        ba.setExecID(execID);
        listeBatch.add(ba);

         LOG.info("liste execID = " + Arrays.toString(listeBatch.toArray()));
         LOG.info("exiting startBatchJobInscriptions with execID = " + execID);
         
        return null;  // next page ici = retour a la page de depart
}

/* Get the status of the job from the batch runtime */
public String getJobStatus()
{
    LOG.info("execID = " + execID);
    if(execID != 0)
    {
        BatchStatus status = jo
                .getJobExecution(execID)
                .getBatchStatus();
            LOG.info("Batchstatus = " + status.toString());
        JobInstance ji = jo.getJobInstance(execID);
        ji.getJobName();

  //  return BatchStatus.valueOf(status);
     //   return jo.getJobExecution(execID).getBatchStatus().toString();
     if(status != BatchStatus.STARTED){
        return status.toString() + " " + ji.getJobName() + " / write = " + getRecordWritten() + " ,read = " + getRecordReaded();
     }else{
        return status.toString() + " - " + ji.getJobName();//;  + " write = " + getRecordWritten() + " read = " + getRecordReaded();
     }
     
    }else{ // execID = 0
        return "No batch Status available at the moment";
    }
}

public String getJobExecutionDetails(long executionId)
//https://docs.oracle.com/javaee/7/api/javax/batch/operations/JobOperator.html
//  https://jaxenter.com/java-ee-7-introduction-to-batch-jsr-352-106192.html      
{
    long duration = 0;
    try{
        jo = BatchRuntime.getJobOperator();
        JobInstance ji = jo.getJobInstance(executionId);
        String jn = ji.getJobName();
        long iid = ji.getInstanceId();
        LOG.info("jobInstance job name = " + jn);
        LOG.info("jobInstance id = " + iid );
        JobExecution je = jo.getJobExecution(executionId);
        String startd = sdf_timeHHmmss.format(je.getCreateTime());
            LOG.info("jobExecution create time = " + startd );
            LOG.info("jobExecution name = " + je.getJobName());
            LOG.info("jobExecution end time = " + sdf_timeHHmmss.format(je.getCreateTime()));
            LOG.info("jobExecution batch status = " + je.getBatchStatus());
            LOG.info("jobExecution batch executionId = " + je.getExecutionId());
            LOG.info("jobExecution batch ExitStatus = " + je.getExitStatus());
            
      //      LOG.info("jobExecutions = " + jo.getJobExecutions(ji));
      //      LOG.info("jobInstance = " + jo.getJobInstance(executionId));
            
   //         List<JobExecution> ljo = jo.getJobExecutions(ji);
       //     ljo.stream().forEach(System.out::println);

            if (je.getEndTime() != null
            && je.getStartTime() != null)
            {
                LOG.info("Job Duration:  ");
             //   + TimeUnit.MILLISECONDS.toSeconds(je.getEndTime().getTime()
          duration = (je.getEndTime().getTime() - je.getStartTime().getTime()); // + " milliseconds");
            }
  //      LOG.info("Step Execution = " + Arrays.deepToString(se.toArray()));
        Set<String> jna = jo.getJobNames();
        LOG.info("Job Names = " + Arrays.deepToString(jna.toArray()));
        
        List<String> executedSteps = new ArrayList<>();
        List<StepExecution> se = jo.getStepExecutions(executionId);
        for (StepExecution stepExecution : se) {
            executedSteps.add(stepExecution.getStepName());
        }
            LOG.info("Executed Steps 2 = " + Arrays.deepToString(executedSteps.toArray()));
      //  return je;
        return je.getExecutionId() + "-" + je.getJobName() + " = " + je.getExitStatus() + " " 
                + sdf_timeHHmmss.format(je.getCreateTime()) + " / " + duration + " millisec "
                ;

    }
    catch(Exception ex)
{
    LOG.info("Exception in getJobExecutionDetails= " + ex);
    return null; 
}    
    } // end method

 private BatchStatus waitForJobComplete(JobOperator jobOperator, long executionId) {  // not in use
    JobExecution execution = jobOperator.getJobExecution(executionId);

    BatchStatus curBatchStatus = execution.getBatchStatus();

    while (true) {
        if (curBatchStatus == BatchStatus.STOPPED
                || curBatchStatus == BatchStatus.COMPLETED
                || curBatchStatus == BatchStatus.FAILED) {
            break;
        }

        execution = jobOperator.getJobExecution(executionId);
        curBatchStatus = execution.getBatchStatus();
    }

    return curBatchStatus;
}
public long restartJob(long executionId) {
        Properties jobProperties = new Properties();
        long newExecutionId = BatchRuntime.getJobOperator().restart(executionId, jobProperties);
        return newExecutionId;
    }

public long getExecID() {
        return execID;
    }
public boolean isCompleted()
    {
        return (getJobStatus().compareTo("COMPLETED") == 0);
    }
 //   public static void setExecID(long execID) {
 //       BatchController.execID = execID;
 //   }
    /* Show the results 
    public String showResults() throws IOException {
        if (isCompleted()) {
            String returnStr;
            /* open file name for output, split comas 
            BufferedReader breader;
            breader = new BufferedReader(new FileReader("result1.txt"));
            String[] results = breader.readLine().split(", ");
            /* create output string 
            returnStr = String.format("%s purchases of %s tablet page views, (%s percent)", 
                         results[0], results[1], results[2]);
            return returnStr;
        } else {
            return "";
        } 
    }*/
    
    } //end class

