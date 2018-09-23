/*
 * To change this template, choose Tools | Templates



 * and open the template in the editor.
 */
package batch;

import static interfaces.Log.LOG;
import java.util.Properties;
import javax.batch.api.Batchlet;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import utils.*;

@Named("FileArchiver")
public class FileArchiver implements Batchlet , interfaces.Log{

    @Inject
    JobContext jobCtx;

    @Override
public String process() throws Exception  // test comment lines
{
    try{
        Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
            LOG.info("Archive files in directory for job " + jobCtx.getExecutionId());
        String downloadDirectory = jobCtx.getProperties().getProperty("downloadDirectory") 
                + File.separator + "job" + jobCtx.getExecutionId();
            LOG.info("DownloadDirectory =  " + downloadDirectory);
        String downloadDirectory2 = jobCtx.getProperties().getProperty("downloadDirectory") 
                + "/" + "job" + jobCtx.getExecutionId();
        LOG.info("DownloadDirectory2 =  " + downloadDirectory2);

        String outputZipFile = jobCtx.getProperties().getProperty("archivesDirectory") 
                + File.separator + "job" + jobCtx.getExecutionId() + ".zip";
            LOG.info("outputZipFile =  " + outputZipFile);
        List<String> fileList = generateFileList(new File(downloadDirectory), downloadDirectory);
            LOG.info(" before zipIt");
        zipIt(outputZipFile, downloadDirectory, fileList);
  //      String s = utils.CreateZipFile(downloadDirectory,outputZipFile);
            LOG.info(" after zipIt");
        return "done";
      }

catch (Exception e)
{
            String msg = "£££ Exception process files archiver = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}
} // end mathod

    @Override
    public void stop() throws Exception {
    }
 
    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile, String sourceFolder, List<String> fileList)
    {
         try {
            LOG.info(" ... entering zipIt with : " + zipFile + "  " + fileList.toString());
        byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
                LOG.info("Output to Zip : " + zipFile);
            for (String file : fileList) {
                LOG.info("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);
                FileInputStream in = new FileInputStream(sourceFolder + File.separator + file);
                int len;
                while ((len = in.read(buffer)) > 0)
                {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();
                LOG.info("Done with creating the file archive");
        } catch (IOException ex)
        {
            String msg = "£££ Exception zipIt files archiver = " + ex.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);;
        }
    }

    /**
     * Traverse a directory and get all files, and add the file into fileList
     */
    public List<String> generateFileList(File node, String sourceFolder) {
    List<String> fileList = new ArrayList<String>();
        //add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString(), sourceFolder));
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename), sourceFolder);
            }
        }
        return fileList;
    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file, String sourceFolder) {
        return file.substring(sourceFolder.length() + 1, file.length());
    }
}