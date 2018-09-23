
package batch;

import com.Ostermiller.util.CSVParser;
import entite.InscriptionCSV;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author collet
 */
@Named("ItemReaderInscription")
//@ItemReader
public class ItemReaderInscription extends AbstractItemReader implements interfaces.Log, interfaces.GolfInterface //, interfaces.GolfInterface
{
  //  @Inject private JobContext jobContext;
    @Inject private InscriptionCSV inscription;
   // @Inject private StepContext stepContext;
    private static BufferedReader br = null;
    private static final Charset CS = Charset.forName("UTF-8");
    private static String  [][] values2D;
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
public void open(final Serializable checkpoint) throws Exception
    {
        LOG.info("ItemReader - open method started");
     // va lire le csv et retourne une array avec les records
        
     //   Properties jobParameters = BatchRuntime.getJobOperator().getParameters(jobContext.getExecutionId());
     //   String fileList = jobParameters.getProperty("fileList");
     //   fileURLArray = fileList.split(";");
//        counter=0;
 try 
  {     File f = new File("C:/Users/collet/Documents/NetBeansProjects/GolfNew/ryder cup inscriptions.txt");
        CharsetDecoder decoder = CS.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);  
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);  
        br = new BufferedReader(new InputStreamReader(new FileInputStream(f),decoder));
        final char delimiter = ';' ;		// attention char mandatory, single quote !!
            LOG.info("normal input fields must be : " + NORMAL_INPUT_FIELDS);
        values2D = CSVParser.parse(br, delimiter);
            LOG.info("after Ostermiller : values2D = " + Arrays.deepToString(values2D) );
            LOG.info("after Ostermiller : nombre de joueurs = " + values2D.length );
            br.close();
        rows = values2D.length;
            LOG.info("rows = " + rows);
        columns = NORMAL_INPUT_FIELDS;
            LOG.info("columns = " + columns); 
            
     for(int i = 0; i < rows; i++) // verification si toutes données présentes par joueur
        {
            if (values2D[i].length != columns)
                {
                 errorsCSV ++;
                 LOG.info("parsing elem = " + Arrays.deepToString(values2D[i]) );
		 LOG.info("Uncomplete record = " + columns + " / fields = " + values2D[i].length + " / record = " + (i+1) + "/ errors = "+errorsCSV );
                 }else{
                  LOG.info("Complete record = " + (i+1));
                } //end if
        } //enf for   
       if(errorsCSV != 0)
       {
           LOG.info("batch execution stopped because of errors in fields");
           close();
       }
            
  } 
catch (Exception ex)
    { 
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
public Object readItem() throws Exception     // à valider   
 //   public Round readItem() throws Exception
{ 
    try
{
       LOG.info("ItemReader - readItem method started");
  if(count < rows)
       {
                   LOG.info("length elem = " + values2D[count].length);
                   LOG.info("parsing elem = " + Arrays.deepToString(values2D[count]) );
                 inscription.setIdplayer(Integer.parseInt(values2D[count][0]) );
                 inscription.setIdround(Integer.parseInt(values2D[count][1]) );
                 inscription.setInscriptionTeam(values2D[count][2]);
                   LOG.info("inscription = " + inscription.toString());
                   LOG.info("current item = " + Arrays.deepToString(values2D[count]) );
                 count ++;
                    LOG.info("Number inscritions treated = " + count);
                 return inscription;
        }else{
            LOG.info("finished ! = ");
            return null;  //stop input by Reader
        }
}
//catch(ParseException ex)/
//{
//    LOG.info("Parse Exception = " + ex);
//    return null;
//}
catch(Exception ex)
{
    LOG.info("Exception = " + ex);
    return null;
}    
    
} // end method
/** java.text.ParseException
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
        LOG.info("ItemReader - close method started");
        LOG.info("ItemReader - records input = " + values2D.length );
        lc.golfnew.BatchController.setRecordReaded(values2D.length);  // was count
        LOG.info("ItemReader - errors = " + errorsCSV);
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