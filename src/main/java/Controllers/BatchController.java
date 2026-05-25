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

@Named("batchC")
@SessionScoped
public class BatchController implements Serializable {

    private static final long serialVersionUID = 1L;

    private JobOperator jobOperator;
    private long executionId;
    private List<Batch> listeBatch;
    private Batch batch = new Batch();   // POJO — not injectable
    private static int recordWritten;   // static: updated by batch item readers/writers running in a separate thread context
    private static int recordReaded;

    @Inject private entite.Settings settings;
    @Inject private create.CreatePlayer createPlayer;

    public BatchController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listeBatch = new ArrayList<>();
        jobOperator = BatchRuntime.getJobOperator();
    } // end method

    public void thumbs(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String msg = "starting thumbs ...";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
            File source = new File(settings.getProperty("PHOTOS_LIBRARY"));
            LOG.debug("source directory = {}", source);
            File destinationDir = new File(settings.getProperty("THUMBNAILS_LIBRARY"));
            Path path = destinationDir.toPath();
            String[] preList = destinationDir.list();
            LOG.debug("destination directory = {} contains files = {}", destinationDir, preList != null ? preList.length : 0);
            Files.walk(path).map(Path::toFile).forEach(File::delete);
            String[] postList = destinationDir.list();
            LOG.debug("destination directory after delete = {} contains files = {}", destinationDir, postList != null ? postList.length : 0);
            Thumbnails.of(source.listFiles())
                .scale(0.30)
                .toFiles(destinationDir, Rename.PREFIX_DOT_THUMBNAIL);
            String[] finalList = destinationDir.list();
            msg = "finishing thumbs — thumbnails generated = " + (finalList != null ? finalList.length : 0);
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void startBatchJobPlayers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
          //  String jobName = "GolfPlayers";
            String jobName = "golfPlayers"; // mod LC 25-05-2026
            Properties props = new Properties();
            String fileName = settings.getProperty("BATCH") + "importPlayers.csv";
            LOG.debug("using file = {}", fileName);
            props.setProperty("input_file", fileName);
            executionId = jobOperator.start(jobName, props);
            LOG.debug("job started = {}", jobName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String startBatchJobRounds() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String jobName = "GolfRounds";
            Properties props = new Properties();
            props.setProperty("input_file", settings.getProperty("BATCH") + "importPlayers.csv");
            executionId = jobOperator.start(jobName, props);
            Batch b = new Batch();
            b.setExecID(executionId);
            listeBatch.add(b);
            LOG.debug("exiting with execID = {}", executionId);
            LOG.debug("liste execID = {}", Arrays.toString(listeBatch.toArray()));
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String startBatchJobInscriptions() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String jobName = "GolfInscriptions";
            Properties props = new Properties();
            props.setProperty("input_file", settings.getProperty("BATCH") + "importPlayers.txt");
            executionId = jobOperator.start(jobName, props);
            Batch b = new Batch();
            b.setExecID(executionId);
            listeBatch.add(b);
            LOG.debug("exiting with execID = {}", executionId);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String getJobStatus() {
        return getJobStatus(jobOperator);
    } // end method

    public String getJobStatus(JobOperator jobOp) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            BatchStatus status = jobOp.getJobExecution(executionId).getBatchStatus();
            JobInstance jobInstance = jobOp.getJobInstance(executionId);
            LOG.debug("status = {}", status);
            if (status != BatchStatus.STARTED) {
                return status.toString() + " " + jobInstance.getJobName();
            } else {
                return status.toString() + " - " + jobInstance.getJobName();
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "unknown";
        }
    } // end method

    public String getJobExecutionDetails(long executionId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        long duration = 0;
        try {
            JobInstance jobInstance = jobOperator.getJobInstance(executionId);
            LOG.debug("jobInstance job name = {}", jobInstance.getJobName());
            LOG.debug("jobInstance id = {}", jobInstance.getInstanceId());
            JobExecution je = jobOperator.getJobExecution(executionId);
            String startd = new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS").format(je.getCreateTime());
            LOG.debug("jobExecution create time = {}", startd);
            LOG.debug("jobExecution name = {}", je.getJobName());
            LOG.debug("jobExecution batch status = {}", je.getBatchStatus());
            LOG.debug("jobExecution executionId = {}", je.getExecutionId());
            LOG.debug("jobExecution exitStatus = {}", je.getExitStatus());
            if (je.getEndTime() != null && je.getStartTime() != null) {
                duration = je.getEndTime().getTime() - je.getStartTime().getTime();
            }
            Set<String> jna = jobOperator.getJobNames();
            LOG.debug("job names = {}", Arrays.deepToString(jna.toArray()));
            List<String> executedSteps = new ArrayList<>();
            List<StepExecution> se = jobOperator.getStepExecutions(executionId);
            for (StepExecution stepExecution : se) {
                executedSteps.add(stepExecution.getStepName());
            }
            LOG.debug("executed steps = {}", Arrays.deepToString(executedSteps.toArray()));
            return je.getExecutionId() + "-" + je.getJobName() + " = " + je.getExitStatus() + " "
                    + new SimpleDateFormat("dd/MM/yyyy HH:mm:SSS").format(je.getCreateTime())
                    + " / " + duration + " millisec";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "";
        }
    } // end method

    public long restartJob(long executionId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            long newExecutionId = BatchRuntime.getJobOperator().restart(executionId, new Properties());
            LOG.debug("restarted executionId={} → newExecutionId={}", executionId, newExecutionId);
            return newExecutionId;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return -1L;
        }
    } // end method

    private BatchStatus waitForJobComplete(JobOperator jobOp, long execId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        JobExecution execution = jobOp.getJobExecution(execId);
        BatchStatus curBatchStatus = execution.getBatchStatus();
        while (true) {
            if (curBatchStatus == BatchStatus.STOPPED
                    || curBatchStatus == BatchStatus.COMPLETED
                    || curBatchStatus == BatchStatus.FAILED) {
                break;
            }
            execution = jobOp.getJobExecution(execId);
            curBatchStatus = execution.getBatchStatus();
        }
        return curBatchStatus;
    } // end method

    // ---- Getters / Setters ----

    public long getExecID()                        { return executionId; }
    public List<Batch> getListeBatch()             { return listeBatch; }
    public void setListeBatch(List<Batch> l)       { this.listeBatch = l; }
    public Batch getBatch()                        { return batch; }
    public void setBatch(Batch batch)              { this.batch = batch; }
    public static int getRecordWritten()            { return recordWritten; }
    public static void setRecordWritten(int n)     { recordWritten = n; }
    public static int getRecordReaded()            { return recordReaded; }
    public static void setRecordReaded(int n)      { recordReaded = n; }

} // end class

/*
[old code preserved in git history]
*/
