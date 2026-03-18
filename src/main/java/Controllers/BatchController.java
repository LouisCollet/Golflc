package Controllers;

import entite.Batch;
import java.text.SimpleDateFormat;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import utils.LCUtil;
import static exceptions.LCException.handleGenericException;
import static utils.LCUtil.showMessageFatal;

@Named("batchC")
@SessionScoped //mod 25-08-2023 nécessaire pour jobSubmitter.xhtml ou request ?? à tester !
//@RequestScoped  enlevé 25-08-2023
public class BatchController implements Serializable{
    
    private static JobOperator jobOperator = BatchRuntime.getJobOperator();
    private long executionId;
    private List<Batch> listeBatch;// = new ArrayList<>();
    @Inject private Batch batch;
    @Inject private entite.Settings settings;        // ✅ injection CDI
    @Inject private create.CreatePlayer createPlayer;  // migrated 2026-02-24
    @Inject private Controller.refact.NavigationController navigationController; // migrated 2026-02-28
    private static int recordWritten;
    private static int recordReaded;

 public BatchController(){  // constructor
   //  pas d'instance ??'
    }
    
 @PostConstruct
    public void init(){ // attention !! ne peut absolument pas avoir : throws SQLException
       listeBatch = new ArrayList<>();
       LOG.debug("init executed");
    }
// 16-11-2023 copied from Ryder
// public static void thumbs(String s){// throws IOException{
    public void thumbs(String s){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
try{
    String msg = "starting thumbs ...";
    LOG.info( msg);
    LCUtil.showMessageInfo(msg);
    File source = new File(settings.getProperty("PHOTOS_LIBRARY"));
        LOG.debug("source directory = " + source);
    File destinationDir = new File(settings.getProperty("THUMBNAILS_LIBRARY"));
    Path path = destinationDir.toPath();
//        LOG.debug("destination directory = " + destinationDir + " length = " + destinationDir.length());  // size in bytes
        LOG.debug("destination directory = " + destinationDir + " contains files = " + destinationDir.list().length); // number of files
    Files.walk(path)
                .map(Path::toFile)
                .forEach(File::delete);
      LOG.debug("destination directory after delete = " + destinationDir + " contains files = " + destinationDir.list().length);

    Thumbnails.of(source.listFiles())
        .scale(0.30)
     // enlevé 29-09-2023   .outputFormat("jpg")
        .toFiles(destinationDir, Rename.PREFIX_DOT_THUMBNAIL); // ajoute "thumbnail." au début du file name
    msg = "finishing thumbs ...number of thumbnails generated 2 = " + destinationDir.list().length;
    LOG.info( msg);
    LCUtil.showMessageInfo(msg);
} catch (Exception e) {
    handleGenericException(e, methodName);
}
} //end method

public void startBatchJobPlayers(){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
 try{
    
    //JobOperator jo = BatchRuntime.getJobOperator();
   // long id = jo.start("GolfPlayers", null);
   // LOG.debug("jobsubmitted ..." + id);
  
  String JOB_NAME = "GolfPlayers"; // The jobname is the job JSL XML file name (minus the .xml extension) 
  // voir dans dir WEB-INF/META-INF/batch-jobs     
  jobOperator = BatchRuntime.getJobOperator();
    LOG.debug("jobOperator = " + jobOperator);
  Properties props = new Properties();
  String fileName = settings.getProperty("BATCH") + "importPlayers.csv";
      LOG.debug("using file  = " + fileName);
  props.setProperty("input_file", fileName);
  // start 
  executionId = jobOperator.start(JOB_NAME, props);
  // start
  
  LOG.debug("Job started = " + JOB_NAME);
  /*
  JobExecution jobExecution = jobOperator.getJobExecution(executionId);
    LOG.debug("JobExecution = " + jobExecution.toString());
    LOG.debug("JobStatus = " + getJobStatus(jobOperator));
   var v1 = jobOperator.getJobInstance(executionId);
  Batch b= new Batch();
  batch.setExecID(executionId);
    LOG.debug("execID long setted = " + b.getExecID());
    LOG.debug("execID String setted = " + b.getStringID());
    LOG.debug("batch b  is now " + b.toString());
  listeBatch.add(b);
 // long lon = jobOperator.getJobInstance(executionId),
  
// https://jakarta.ee/specifications/batch/2.1/jakarta-batch-spec-2.1.html#batch-exception-classes
   var v = jobOperator.getJobExecutions(v1); 
   // public List<JobExecution> getJobExecutions(JobInstance instance)
   LOG.debug("liste job executions = " + v);
   LOG.debug("liste executionId = " + Arrays.toString(listeBatch.toArray()));
      //   LOG.debug("exiting startBatchJobPlayers with execID = " + execID) + listeBatch.;
       LOG.debug("liste executionId = " + Arrays.toString(listeBatch.toArray()));
 //  return null;  // next page ici = retour a la page de depart
   //  return "jobstarted"; //mod 15-09-2021
 */
 } catch (Exception e) {
     handleGenericException(e, methodName);
 }
} // end method

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

public String startBatchJobRounds(){ // throws Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
  try{
        String JOB_NAME = "GolfRounds"; // The jobname is nothing but the job JSL XML file name (minus the .xml extension)       
        jobOperator = BatchRuntime.getJobOperator();
        Properties props = new Properties();
     //   props.setProperty("input_file", "C:/Users/collet/Documents/NetBeansProjects/GolfNew/importPlayers.txt");
 //       props.setProperty("input_file", Settings.getBATCH() + "importPlayers.csv");
        props.setProperty("input_file", settings.getProperty("BATCH") + "importPlayers.csv");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
 //       String s = Settings.getBATCH() + "importPlayers.txt";
        executionId = jobOperator.start(JOB_NAME, props);
        Batch batch = new Batch();
        batch.setExecID(executionId);
        listeBatch.add(batch);
          LOG.debug("exiting startBatchJobRounds with execID = " + executionId);
          LOG.debug("liste execID = " + Arrays.toString(listeBatch.toArray()));
        return null;  // next page ici = retour a la page de depart
  } catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} //end method
public String startBatchJobInscriptions(){ // throws Exception{
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
 try{
        String JOB_NAME = "GolfInscriptions"; // The jobname is nothing but the job JSL XML file name (minus the .xml extension)       
        jobOperator = BatchRuntime.getJobOperator();
        Properties props = new Properties();
   //     props.setProperty("input_file", Settings.getBATCH() + "importPlayers.txt");
        props.setProperty("input_file", settings.getProperty("BATCH") + "importPlayers.txt");
     //   props.setProperty("payrollInputDataFileName", payrollInputDataFileName);
        executionId = jobOperator.start(JOB_NAME, props);
        
        Batch batch = new Batch();
        batch.setExecID(executionId);
        listeBatch.add(batch);
         LOG.debug("liste executionId = " + Arrays.toString(listeBatch.toArray()));
         LOG.debug("exiting startBatchJobInscriptions with execID = " + executionId);
        return null;  // next page ici = retour a la page de depart
   } catch (Exception e) {
    handleGenericException(e, methodName);
    return null;
}
} //end method


public String getJobStatus(){ // used in jobSubmitter.xhtml
    return getJobStatus(jobOperator);
}
///* Get the status of the job from the batch runtime */
//public String getJobStatus() {
//    return jobOperator.getJobExecution(execID).getBatchStatus().toString();
//}

/* Get the status of the job from the batch runtime */
public String getJobStatus(JobOperator jobOp){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName + " with executionId = " + executionId);
  try{
    //if(executionId = 0){
    //    return "execID = 0 - No batch Status available at the moment !!";
   // }
        BatchStatus status = jobOperator
                .getJobExecution(executionId)
                .getBatchStatus();
            LOG.debug("Batchstatus = " + status.toString());
        JobInstance jobInstance = jobOperator.getJobInstance(executionId);
        jobInstance.getJobName();

  //  return BatchStatus.valueOf(status);
     //   return jo.getJobExecution(execID).getBatchStatus().toString();
     LOG.debug("status =  " + status);
     if(status != BatchStatus.STARTED){
        return status.toString() + " " + jobInstance.getJobName() ; //+ " / write = " + getRecordWritten() + " ,read = " + getRecordReaded();
     }else{
        return status.toString() + " - " + jobInstance.getJobName(); //  + " write = " + getRecordWritten() + " read = " + getRecordReaded();
     }
 //   }else{ // execID = 0
        
 //   }
    } catch (Exception e) {
     handleGenericException(e, methodName);
     return "nothing exception";
}
} // end method

public String getJobExecutionDetails(long executionId){
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    long duration = 0;
    try{
        //jobOperator = BatchRuntime.getJobOperator();  25-08-2023
        JobInstance jobInstance = jobOperator.getJobInstance(executionId);
        String jn = jobInstance.getJobName();
        long iid = jobInstance.getInstanceId();
        LOG.debug("jobInstance job name = " + jn);
        LOG.debug("jobInstance id = " + iid );
        JobExecution je = jobOperator.getJobExecution(executionId);
        String startd = new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS").format(je.getCreateTime());
            LOG.debug("jobExecution create time = " + startd );
            LOG.debug("jobExecution name = " + je.getJobName());
            LOG.debug("jobExecution end time = " + new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS").format(je.getCreateTime()));
            LOG.debug("jobExecution batch status = " + je.getBatchStatus());
            LOG.debug("jobExecution batch executionId = " + je.getExecutionId());
            LOG.debug("jobExecution batch ExitStatus = " + je.getExitStatus());
            
      //      LOG.debug("jobExecutions = " + jo.getJobExecutions(ji));
      //      LOG.debug("jobInstance = " + jo.getJobInstance(executionId));
            
   //         List<JobExecution> ljo = jo.getJobExecutions(ji);
       //     ljo.stream().forEach(System.out::println);

            if (je.getEndTime() != null
            && je.getStartTime() != null)
            {
                LOG.debug("Job Duration:  ");
             //   + TimeUnit.MILLISECONDS.toSeconds(je.getEndTime().getTime()
          duration = (je.getEndTime().getTime() - je.getStartTime().getTime()); // + " milliseconds");
            }
  //      LOG.debug("Step Execution = " + Arrays.deepToString(se.toArray()));
        Set<String> jna = jobOperator.getJobNames();
        LOG.debug("Job Names = " + Arrays.deepToString(jna.toArray()));
        
        List<String> executedSteps = new ArrayList<>();
        List<StepExecution> se = jobOperator.getStepExecutions(executionId);
        for (StepExecution stepExecution : se) {
            executedSteps.add(stepExecution.getStepName());
        }
            LOG.debug("Executed Steps 2 = " + Arrays.deepToString(executedSteps.toArray()));
      //  return je;
        return je.getExecutionId() + "-" + je.getJobName() + " = " + je.getExitStatus() + " " 
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS").format(je.getCreateTime()) + " / " + duration + " millisec "
                ;
    }
 catch (Exception e) {
    handleGenericException(e, methodName);
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
    } // end while

    return curBatchStatus;
}
public long restartJob(long executionId) {
        Properties jobProperties = new Properties();
        long newExecutionId = BatchRuntime.getJobOperator().restart(executionId, jobProperties);
        return newExecutionId;
    }

public long getExecID() {
        return executionId;
    }
//public boolean isCompleted(){
 //       return (getJobStatus().compareTo("COMPLETED") == 0);
 //   }
 //   public static void setExecID(long execID) {
 //       BatchController.execID = execID;
 //   }

/*/ lancé en test à partir du menu !
 public static void importPlayers() throws SQLException, Exception{
 try{
   // https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
   //https://www.baeldung.com/opencsv

    String fileName = Settings.getProperty("BATCH") + "importPlayers.csv";
       LOG.debug("fileName for import players csv = " + fileName);
    final CSVParser parser = new CSVParserBuilder()
           .withSeparator(';')
           .withIgnoreQuotations(true)
           .withIgnoreLeadingWhiteSpace(true)
           .build();
   try(final CSVReader reader = new CSVReaderBuilder(new BufferedReader(new FileReader(fileName)))
//           .withSkipLines(1) //// Skip the header
           .withCSVParser(parser)
           .build())
      {
         LOG.debug(" Read line by line and turns line into a String[]");
      String[] lineInArray;
      //lineinarray format = [2021001, Patrick, Cantlay, San Antonio , Texas, US, 17-03-1992, M, EN] 
       Connection conn = new DBConnection().getConnection();
      while((lineInArray = reader.readNext()) != null) {
   //       LOG.debug("lineinarray  = " + Arrays.toString(lineInArray)); // chaque call concerne une ligne
          parsePlayers(lineInArray, conn);
          // return vers processsItem
      }
      LOG.debug(reader.getLinesRead());
      LOG.debug(reader.getRecordsRead());
      LOG.debug(reader.getSkipLines());
      String msg = "Finished with success !!= ";
      DBConnection.closeQuietly(conn, null, null, null);
      LOG.info(msg);
      showMessageInfo(msg);
      
   }catch (Exception ex){
  //  LOG.debug("Fatal Exception in CSV reader " + ex )
      String msg = "Fatal Exception in CSV reader = " + ex;
      LOG.debug(msg);
      showMessageFatal(msg);
    }
 }catch (Exception ex){
     String msg = "Fatal Exception in importPlayers = " + ex;
     LOG.debug(msg);
     showMessageFatal(msg);
} finally {
} 
 // à faire dans processiItem !!
 } // end method
*/
 /*
 public static void parsePlayers(String[] line, Connection conn){
     // one player
 try{
     HandicapIndex handicapIndex = new HandicapIndex();
     Player player = new Player();
 //    EPlayerHandicap ePlayerHandicap = new EPlayerHandicap();
         LOG.debug("line = " + Arrays.toString(line));
      //line format  = [2021001, Patrick, Cantlay, San Antonio , Texas, US, 17-03-1992, M, EN] 
      player.setIdplayer(Integer.valueOf(line[0]));
      player.setPlayerFirstName(line[1]);
      player.setPlayerLastName(line[2]);
      player.getAddress().setCity(line[3]);
      player.getAddress().setStreet("Ryder street"); // cannot be null
      // mod 22-12-2022
      player.getAddress().getCountry().setCode(line[4]);
// mod 26-08-2023
      player.setPlayerBirthDate(LocalDateTime.of(LocalDate.parse(line[5], ZDF_DAY),LocalTime.of(9,57)));  // même heure de naissance pour tous !!
      player.setPlayerGender(line[6]);
      player.setPlayerHomeClub(104); // tous les joueurs ont le même home Club !
   //   player.setPlayerPhotoLocation("fotoLocation");
      player.setPlayerPhotoLocation(String.valueOf(player.getIdplayer()) + ".png"); // mod 25-08-2023
      player.setPlayerLanguage(line[7]); //String.valueOf(0)
      String s = player.getPlayerFirstName() + "." + player.getPlayerLastName() + "@skynet.be";
      player.setPlayerEmail(s);
      player.getAddress().setZoneId("America/Los_Angeles");
      double latitude = Double.parseDouble("34.086282"); // hollywood !!
      double longitude = Double.parseDouble("-118.318582");
      player.setPlayerLatLng(new com.google.maps.model.LatLng(latitude, longitude));
          LOG.debug("player to be inserted = " + player);
      handicapIndex.setHandicapDate(LocalDateTime.of(2021,Month.JANUARY,01,0,0));
      handicapIndex.setHandicapWHS(BigDecimal.valueOf(0.0));
         LOG.debug("handicapIndex to be inserted = " + handicapIndex);
 //        LOG.debug("just before create player");
   //   ePlayerHandicap.setPlayer(player);
   //   ePlayerHandicap.setHandicapIndex(handicapIndex);
     LOG.debug("parsed player = player");
     if(createPlayer.create(player, handicapIndex, "B")){   // B= creation Batch
        String msg = "player created !!";
        LOG.info(msg);
        showMessageInfo(msg);
     }else{
        String msg = "FATAL error : player not created !!";
        LOG.error(msg);
        showMessageFatal(msg);
    }
     
     
 }catch (Exception ex){
     String msg = "Fatal Exception in parsePlayers = " + ex;
     LOG.debug(msg);
     showMessageFatal(msg); 
  //   
  //  for(int i=0; i<lineInArray.length; i++){
  //        LOG.debug("lineinarray " + i + "/" + lineInArray[i]);
  //        LOG.debug("lineinarray 1 = " + lineInArray[1]);
  //        LOG.debug("lineinarray 2 = " + lineInArray[2]); //  options[i + 1] = new SelectItem(data[i], data[i]);
        }
 }
*/
    // ========================================
    // NAVIGATION — migrated 2026-02-28
    // ========================================

    /**
     * Navigation vers jobSubmitter.xhtml
     */
    public String to_jobSubmitter_xhtml() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        navigationController.reset("Reset to_jobSubmitter");
        return "jobSubmitter.xhtml?faces-redirect=true";
    } // end method

} //end class