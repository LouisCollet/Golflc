package batch;
// à modifier : ne fonctionne plus !! voir example dans public class ItemReaderPlayer 
//import com.Ostermiller.util.CSVParser;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import entite.InscriptionCSV;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.batch.api.chunk.AbstractItemReader;
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
import java.util.Arrays;

@Named("ItemReaderInscription") // pas updaté !!
@ApplicationScoped // mod 11/01
public class ItemReaderInscription extends AbstractItemReader implements interfaces.GolfInterface{
  //  @Inject private JobContext jobContext;
  //  @Inject // enlevé LC 24-02-2026
    private InscriptionCSV inscription; //  ??
    @Inject private entite.Settings settings;        // ✅ injection CDI
   // @Inject private StepContext stepContext;
  //  private static BufferedReader br = null;
 //   private static final Charset CS = Charset.forName("UTF-8");
    private static String  [][] values2D;
    CSVReader reader = null;
    private int rows = 0;
    private int columns = 0;
    private int count = 0;
    private static int errorsCSV = 0;
    static final int NORMAL_INPUT_FIELDS = 3;
/** 
29 	 * Override this method if the ItemReader requires 
30 	 * any open time processing.   
31 	 * The default implementation does nothing.  
32 	 *  
33 	 * @param checkpoint for this ItemReader - may be null. 
34 	 * @throws Exception (or subclass) if an error occurs.  
35 	 */ 

@Override
//@Open
public void open(final Serializable checkpoint) throws Exception{
        LOG.debug("ItemReader - open method started");
     // va lire le csv et retourne une array avec les records
        
     //   Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
     //   String fileList = jobParameters.getProperty("fileList");
     //   fileURLArray = fileList.split(";");
//        counter=0;
 try{
   /*     File f = new File("C:/Users/collet/Documents/NetBeansProjects/GolfNew/ryder cup inscriptions.txt");
        CharsetDecoder decoder = CS.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);  
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);  
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f),decoder));
    */    
  //     String fileName = Settings.getBATCH() + "ryder cup inscriptions.txt";
       String fileName = settings.getProperty("BATCH") + "ryder cup inscriptions.txt";
          LOG.debug("file to handle = " + fileName);
          
       String readString = Files.readString(Path.of(fileName), StandardCharsets.UTF_8);
         LOG.debug("String readString = " + NEW_LINE +readString);
       //        Path path = Paths.get(fileName);   //      LOG.debug("myPath = " + myPath.toString());
   final CSVParser parser = new CSVParserBuilder()
           .withSeparator(';')
           .withIgnoreQuotations(true)
           .withIgnoreLeadingWhiteSpace(true)
           .build();
    reader = new CSVReaderBuilder(new BufferedReader(new FileReader(Paths.get(fileName).toFile())))
//           .withSkipLines(1) //// Skip the header
           .withCSVParser(parser)
           .build();
      LOG.debug("CSV reader = " + reader);
 //       final char delimiter = ';' ;		// attention char mandatory, single quote !!
            LOG.debug("normal input fields must be : " + NORMAL_INPUT_FIELDS);
   //     values2D = CSVParser.parse(readString, delimiter);
     //       LOG.debug("after Ostermiller : values2D = " + Arrays.deepToString(values2D) );
      //      LOG.debug("after Ostermiller : nombre de joueurs = " + values2D.length );
    //        br.close();
 //       rows = values2D.length;
  //          LOG.debug("rows = " + rows);
        columns = NORMAL_INPUT_FIELDS;
            LOG.debug("columns = " + columns); 
            
     for(int i = 0; i < values2D.length; i++){ // verification si toutes données présentes par joueur
            if (values2D[i].length != columns){
                 errorsCSV ++;
                 LOG.debug("parsing elem = " + Arrays.deepToString(values2D[i]) );
		 LOG.debug("Uncomplete record = " + columns + " / fields = " + values2D[i].length + " / record = " + (i+1) + "/ errors = "+errorsCSV );
                 }else{
                  LOG.debug("Complete record = " + (i+1));
                } //end if
        } //enf for   
       if(errorsCSV != 0){
           LOG.debug("batch execution stopped because of errors in fields");
           close();
       }
}catch (Exception ex){ 
        LOG.error(" -- Exception in open ItemReader !" + ex);
    }
} //end method 
/** 
52 	 * The readItem method returns the next item 
53 	 * for chunk processing. 
54 	 * It returns null to indicate no more items, which 
55      * also means the current chunk will be committed and  
56      * the step will end. 
57 	 * @return next item or null 
58 	 * @throws Exception is thrown for any errors. 
59 	 */ 

@Override
public Object readItem() throws Exception{      // à modifier : voir ItemReaderPlayer !!
 //   public Round readItem() throws Exception
 try{
       LOG.debug("ItemReader - readItem method started");
  if(count < rows){
       LOG.debug("length elem = " + values2D[count].length);
       LOG.debug("parsing elem = " + Arrays.deepToString(values2D[count]) );
       inscription.setIdplayer(Integer.valueOf(values2D[count][0]) );
       inscription.setIdround(Integer.valueOf(values2D[count][1]) );
       inscription.setInscriptionTeam(values2D[count][2]);
          LOG.debug("inscription = " + inscription.toString());
          LOG.debug("current item = " + Arrays.deepToString(values2D[count]) );
              count ++;
          LOG.debug("Number inscritions treated = " + count);
        return inscription;
        }else{
            LOG.debug("finished ! = ");
            return null;  //stop input by Reader
        }
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
44 	 */ 
/** 
45 	 * The close method marks the end of use of the  
46 	 * ItemReader. The reader is free to do any cleanup  
47 	 * necessary. 
48 	 * @throws Exception is thrown for any errors. 
49 	 */ 

@Override
public void close() throws Exception {
           LOG.debug("ItemReader - close method started");
           LOG.debug("ItemReader - records input = " + values2D.length );
        Controllers.BatchController.setRecordReaded(values2D.length);  // was count
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
63 	 */ 
@Override 
public Serializable checkpointInfo() throws Exception {  
 		return null;  
 	} 
} //end class 