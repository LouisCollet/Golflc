
package batch;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import entite.Round;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;

import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import static utils.LCUtil.DatetoLocalDateTime;

/**
 * ItemReader pour le batch de chargement des Rounds
 * ✅ Round — plus @Inject, instancié localement dans readItem()
 * ✅ JobContext doublon supprimé
 * ✅ values2D et errorsCSV — champs d'instance (plus static)
 * ✅ Standards CDI : methodName + handleGenericException
 */
@Named("ItemReaderRound")
@ApplicationScoped
public class ItemReaderRound extends AbstractItemReader implements interfaces.GolfInterface {

    // ✅ JobContext doublon supprimé — un seul suffit
    @Inject private JobContext    jobContext;
    @Inject private StepContext   stepContext;
    @Inject private entite.Settings settings;

    private CSVReader reader   = null;

    // ✅ Champs d'instance — plus static
    private String[][] values2D;
    private int rows      = 0;
    private int columns   = 0;
    private int count     = 0;
    private int errorsCSV = 0;

    static final int NORMAL_INPUT_FIELDS = 3;

    // ========================================
    // OPEN
    // ========================================

    @Override
    public void open(final Serializable checkpoint) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            // ✅ Settings injecté — plus de Settings.getProperty() statique
            String fileName = settings.getProperty("BATCH") + "ryder cup rounds.txt";
            LOG.debug(methodName + " - fileName = " + fileName);

            String readString = Files.readString(Path.of(fileName), StandardCharsets.UTF_8);
            LOG.debug(methodName + " - readString = " + NEW_LINE + readString);

            Path path = Paths.get(fileName);

            final CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .withIgnoreQuotations(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            reader = new CSVReaderBuilder(
                    new BufferedReader(new FileReader(path.toFile())))
                    .withCSVParser(parser)
                    .build();
            LOG.debug(methodName + " - CSV reader = " + reader);

            columns = NORMAL_INPUT_FIELDS;
            LOG.debug(methodName + " - columns = " + columns);

            // ✅ Vérification intégrité des records
            errorsCSV = 0;
            for (int i = 0; i < values2D.length; i++) {
                if (values2D[i].length != columns) {
                    errorsCSV++;
                    LOG.debug(methodName + " - incomplete record["
                            + (i + 1) + "] — fields = " + values2D[i].length
                            + " / expected = " + columns
                            + " / errors = " + errorsCSV);
                } else {
                    LOG.debug(methodName + " - complete record[" + (i + 1) + "]");
                }
            }

            if (errorsCSV != 0) {
                LOG.error(methodName + " - batch stopped — CSV errors = " + errorsCSV);
                close();
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // READ ITEM
    // ========================================

    @Override
    public Object readItem() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (count < rows) {
                LOG.debug(methodName + " - parsing elem = "
                        + Arrays.deepToString(values2D[count]));

                // ✅ Nouvelle instance Round par item — plus @Inject Round
                Round round = new Round();
                java.util.Date d = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(values2D[count][0]);
                round.setRoundDate(DatetoLocalDateTime(d));
                round.setRoundGame(values2D[count][1]);
                round.setRoundName(values2D[count][2]);

                LOG.debug(methodName + " - round = " + round.toString());
                count++;
                LOG.debug(methodName + " - count = " + count);
                return round;

            } else {
                LOG.debug(methodName + " - finished — total items = " + count);
                return null;
            }

        } catch (ParseException e) {
            handleGenericException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // CLOSE
    // ========================================

    @Override
    public void close() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - records read  = " + (values2D != null ? values2D.length : 0));
            LOG.debug(methodName + " - errors CSV    = " + errorsCSV);
            Controllers.BatchController.setRecordReaded(count);

            if (reader != null) {
                reader.close();                                // ✅ fermeture explicite du reader
                LOG.debug(methodName + " - CSV reader closed");
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // CHECKPOINT
    // ========================================

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    } // end method

} // end class
/*
// à modifier : ne fonctionne plus !! voir example dans public class ItemReaderPlayer 
//import com.Ostermiller.util.CSVParser;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import entite.Round;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import static utils.LCUtil.DatetoLocalDateTime;

@Named("ItemReaderRound")
@ApplicationScoped // mod 11/01
public class ItemReaderRound extends AbstractItemReader implements interfaces.GolfInterface{
    @Inject    private JobContext jobContext;
    @Inject    private Round round;
    @Inject    private StepContext stepContext;
    CSVReader reader = null;
    @Inject JobContext jobCtx;
    @Inject private entite.Settings settings;        // ✅ injection CDI
//    private static BufferedReader br = null;
//    private static final Charset cs = Charset.forName("UTF-8");
    private static String  [][] values2D;
    private int rows = 0;
    private int columns = 0;
    private int count = 0;
    private static int errorsCSV = 0;
    static final int NORMAL_INPUT_FIELDS = 3;


@Override
//@Open
public void open(final Serializable checkpoint) throws Exception {
        LOG.debug("ItemReader - open method started");
     // va lire le csv et retourne une array avec les records
     //   Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
     //   String fileList = jobParameters.getProperty("fileList");
     //   fileURLArray = fileList.split(";");
//        counter=0;
 try {

 /*    File f = new File("C:/Users/collet/Documents/NetBeansProjects/GolfNew/ryder cup rounds.txt");
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);  
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);  
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f),decoder));
       
    //   String fileName = Settings.getBATCH() + "ryder cup rounds.txt";
        String fileName = settings.getProperty("BATCH") + "ryder cup rounds.txt";
          LOG.debug("file to handle = " + fileName);
       String readString = Files.readString(Path.of(fileName), StandardCharsets.UTF_8);
         LOG.debug("String readString = " + NEW_LINE +readString);
         Path path = Paths.get(fileName);   //      LOG.debug("myPath = " + myPath.toString());
            final CSVParser parser = new CSVParserBuilder()
           .withSeparator(';')
           .withIgnoreQuotations(true)
           .withIgnoreLeadingWhiteSpace(true)
           .build();
    reader = new CSVReaderBuilder(new BufferedReader
        //(new FileReader(fileName)))
            (new FileReader(path.toFile())))
//           .withSkipLines(1) //// Skip the header
           .withCSVParser(parser)
           .build();
      LOG.debug("CSV reader = " + reader);
       
    //    final char delimiter = ';' ;		// attention char mandatory, single quote !!
            LOG.debug("normal input fields must be : " + NORMAL_INPUT_FIELDS);
 //       values2D = CSVParser.parse(readString, delimiter);
    //        LOG.debug("after Ostermiller : values2D = " + Arrays.deepToString(values2D) );
    //        LOG.debug("after Ostermiller : nombre de joueurs = " + values2D.length );
   //         br.close();
 //       rows = values2D.length;
 //           LOG.debug("rows = " + rows);
        columns = NORMAL_INPUT_FIELDS;
            LOG.debug("columns = " + columns); 
            
     for(int i = 0; i < values2D.length; i++){ // verification si toutes données présentes par joueur
            if (values2D[i].length != columns) {
                 errorsCSV ++;
                 LOG.debug("parsing elem = " + Arrays.deepToString(values2D[i]) );
		 LOG.debug("Uncomplete record = " + columns + " / fields = " + values2D[i].length + " / record = " + (i+1) + "/ errors = "+errorsCSV );
             }else{
                  LOG.debug("Complete record = " + (i+1));
             } //end if
        } //enf for   
       if(errorsCSV != 0) {
           LOG.debug("batch execution stopped because of errors in fields");
           close();
       }

  }catch (Exception ex) { 
        LOG.error(" -- Exception in open ItemReader !" + ex);
    }
  } //end method 
/* 
52 	 * The readItem method returns the next item 
53 	 * for chunk processing. 
54 	 * It returns null to indicate no more items, which 
55      * also means the current chunk will be committed and  
56      * the step will end. 
57 	 * @return next item or null 
58 	 * @throws Exception is thrown for any errors. 
59 	 


    @Override
public Object readItem() throws Exception {     // à modifier voir example dans ItemReaderPlayer
 //   public Round readItem() throws Exception
 try{
       LOG.debug("ItemReader - readItem method started");
  if(count < rows)  {
                   LOG.debug("length elem = " + values2D[count].length);
                   LOG.debug("parsing elem = " + Arrays.deepToString(values2D[count]) );
       //          round.setRoundDate(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(values2D[count][0]) );
                 
                 java.util.Date d = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(values2D[count][0]);
                 round.setRoundDate(DatetoLocalDateTime(d));
                 round.setRoundGame(values2D[count][1] );
                  round.setRoundName(values2D[count][2]);
                   LOG.debug("round = " + round.toString());
                   LOG.debug("current item = " + Arrays.deepToString(values2D[count]) );
                 count ++;
                    LOG.debug("Number rounds treated = " + count);
                 return round;
        }else{
            LOG.debug("finished ! = ");
            return null;  //stop input by Reader
        }
}catch(ParseException ex){
    LOG.debug("Parse Exception = " + ex);
    return null;
}catch(Exception ex){
    LOG.debug("Exception = " + ex);
    return null;
}    
    
} // end method
/* java.text.ParseException
39 	 * Override this method if the ItemReader requires 
40 	 * any close time processing.    
41 	 * The default implementation does nothing.  
42 	 *  
43 	 * @throws Exception (or subclass) if an error occurs. 
45 	 * The close method marks the end of use of the  
46 	 * ItemReader. The reader is free to do any cleanup  
47 	 * necessary. 
48 	 * @throws Exception is thrown for any errors. 
45 	 * The close method marks the end of use of the  
46 	 * ItemReader.The reader is free to do any cleanup  
47 	 * necessary. 
48 	 * @throws Exception is thrown for any errors. 
49
     * @throws java.lang.Exception
     
    @Override
 public void close() throws Exception {
        LOG.debug("ItemReader - close method started");
        LOG.debug("ItemReader - records input = " + values2D.length );
        LOG.debug("ItemReader - errors = " + errorsCSV);
      Controllers.BatchController.setRecordReaded(count);
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
     * @throws java.lang.Exception 
	@Override 
 	public Serializable checkpointInfo() throws Exception {  
 		return null;  
 	} 

} //end class 
*/