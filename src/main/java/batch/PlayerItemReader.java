
package batch;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
//import entite.Settings;
import static interfaces.Log.LOG;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://www.baeldung.com/java-ee-7-batch-processing
// https://sqli.developpez.com/tutoriels/javaee/decouverte-batch-processing/
// https://opencsv.sourceforge.net/
@Named // CDI name used in GolfPlayers.xml
@ApplicationScoped // mod 11/01
public class PlayerItemReader extends AbstractItemReader {
    CSVReader reader = null;
    private int count = 0;
    private static int errorsCSV = 0;
    
    @Inject private JobContext jobCtx; // new 25-08-2023
    @Inject private entite.Settings settings;        // ✅ injection CDI
    
  //  static final int NORMAL_INPUT_FIELDS = 8;
/** 
29 	 * Override this method if the ItemReader requires 
30 	 * any open time processing.31 	 * The default implementation does nothing.32 	 *  
33 	 * @param checkpoint for this ItemReader - may be null. 
34 	 * @throws Exception (or subclass) if an error occurs.  
35 
     * @param checkpoint 
     * @throws java.lang.Exception */ 

@Override
//http://opencsv.sourceforge.net/#general
public void open(final Serializable checkpoint) throws Exception{
        LOG.debug("ItemReader - open method started");
     // va lire le csv et retourne une array avec les records
     //   Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
     //   String fileList = jobParameters.getProperty("fileList");
 try{
     LOG.debug("property log_file_name from Job of GolfPlayers.xml = " + jobCtx.getProperties().getProperty("log_file_name"));
    String fileName = settings.getProperty("BATCH") + "importPlayers.csv";
       LOG.debug("fileName for import players csv = " + fileName);
       Path path = Paths.get(fileName);   //      LOG.debug("myPath = " + myPath.toString());
   final CSVParser parser = new CSVParserBuilder()
           .withSeparator(';')
           .withIgnoreQuotations(true)
           .withIgnoreLeadingWhiteSpace(true)
           .build();
    reader = new CSVReaderBuilder(new BufferedReader (new FileReader(path.toFile())))
//           .withSkipLines(1) //// Skip the header
           .withCSVParser(parser)
           .build();
      LOG.debug("CSV reader = " + reader);

} catch (Exception ex){ 
        String msg = " -- Exception in open ItemReader !" + ex;
        LOG.error(msg);
  }
 } //end method 
/** 
52 	 * The readItem method returns the next item 
53 	 * for chunk54 	 * It returns null to indicate no more items, which 
55      * also means the current chunk will be committed and  
56      * the step will end. p57 	 * @return next item or null 
58 	 * @throws Exception is thrown for any errors. 
59 e
     * @return errors. 
59 	 */ 
/* not used
public List<String[]> readAllLines(Path filePath) throws Exception {
    try (Reader reader = Files.newBufferedReader(filePath)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll();
        }
    }
}
*/

@Override
public Object readItem() { 
 try{
       LOG.debug("ItemReader - readItem method started");
      String[] lineInArray;
 //        LOG.debug("ItemReader - readItem method startedreder in readitem = " + reader);
      //lineinarray  = [2021001, Patrick, Cantlay, San Antonio , Texas, US, 17-03-1992, M, EN] 
      while((lineInArray = reader.readNext()) != null) {
          LOG.debug("lineinarray  = " + Arrays.toString(lineInArray)); 
          return lineInArray;// chaque call concerne une ligne
      }
      return null;  // end of input file
}catch(Exception ex){
    LOG.debug("Exception in readItem = " + ex);
    return null;
}    
    
 } // end method

// not used
 public List<String[]> readLineByLine(Path filePath) throws Exception {
    List<String[]> list = new ArrayList<>();
    try (Reader reader = Files.newBufferedReader(filePath)) {
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                list.add(line);
            }
        }
    }
    return list;
}
 

    @Override
    public void close() throws Exception {
        LOG.debug("ItemReader - close method started");
        LOG.debug("reader = " + reader);
        reader.close();
        LOG.debug("reader closed !");
//        LOG.debug("ItemReader - records input = " + values2D.length );
           Controllers.BatchController.setRecordReaded(count);
        LOG.debug("ItemReader - errors = " + errorsCSV);
    //    DBConnection.closeQuietly(conn, null, null, null);
    }
/** 
57 	 * Override this method if the ItemReader supports  
58 	 * checkpoints. 
59 	 * The default implementation returns null.   
60 	 *  
61 	 * @return checkpoint data  
62 	 * @throws Exception (or subclass) if an error occurs. 
63 
     * @return 
     * @throws java.lang.Exception */ 
	@Override 
 	public Serializable checkpointInfo() throws Exception {  
            LOG.debug("checkpoint info");
 		return null;  
 	} 
} //end class 