package utils;
// à modifier : ne fonctionne plus !! voir example dans public class ItemReaderPlayer 
import entite.Handicap;
import entite.HandicapIndex;
import entite.Player;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.Connection;
import java.sql.SQLException;

@Named("golfCSV")
@SessionScoped
 public class GolfCSVFile implements Serializable, interfaces.GolfInterface{
/**
 * CSV in action: extract fields from a CSV File using Ostermiller CSVParser */

      private static String  [][] values2D;
      private static String [] array_return;
      private static BufferedReader br = null;
 //     private static int nb = 0;
      private static int errorsCSV = 0;
      private static Player player = new Player();
      private static Handicap handicap ; 
      private static final Charset cs = Charset.forName("UTF-8");
static final int NORMAL_INPUT_FIELDS = 13;
public GolfCSVFile()	
{
// constructor
}

public static  String [] getCSVExtract(final Connection conn, final File fi) throws SQLException {
//  nb++;
        LOG.debug("starting getExtract");
  try 
  {         //set a Savepoint
 //       savepoint1 = conn.setSavepoint("Savepoint1");
            //input n'est pas UTF-8 , mais ISO 8859 = new FileReader (fi);
      // The FileReader class always use the system's default character encoding !!! bad idea
    CharsetDecoder decoder = cs.newDecoder();
      //  CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();  
        decoder.onMalformedInput(CodingErrorAction.REPORT);  
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);  
    br = new BufferedReader(
             new InputStreamReader(
                 new FileInputStream(fi),decoder));  //en rÃƒÂ©alitÃƒÂ©, decoder inutile on est natif en UTF-8 !!!
//     LOG.debug("still here 01");
/*Charset Description

US-ASCII     Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
ISO-8859-1   ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
UTF-8        Eight-bit UCS Transformation Format
UTF-16BE     Sixteen-bit UCS Transformation Format, big-endian byte order
UTF-16LE     Sixteen-bit UCS Transformation Format, little-endian byte order
UTF-16       Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
*/

  //   final char delimiter = ';'; // attention char mandatory, single quote !!
      LOG.debug("normal input fields must be : " + NORMAL_INPUT_FIELDS);

     br.close();
     
     int rows=values2D.length;
        LOG.debug("rows = " + rows);
     int columns = NORMAL_INPUT_FIELDS;
       LOG.debug("columns = " + columns);
     
     
     for(int i = 0; i < rows; i++) {
            if (values2D[i].length != columns){
                 errorsCSV ++;
		 LOG.debug("Uncomplete record = " + columns + " / fields = " + values2D[i].length + " / record = " + (i+1) + "/ errors = "+errorsCSV );
        //         LOG.error(NEW_LINE + "Too much fields = {0}" + Arrays.deepToString(values2D));  //fine pas sur console
                }else{
                  LOG.debug("Complete record = " + (i+1));
                }
                //end if
        } //enf for
     if(errorsCSV != 0) {
         LOG.debug("there are {} errors - treatment stopped, no insert", errorsCSV);
         array_return[0]= "ERROR";
         array_return[1]= "FIELDS";
         return array_return;
     }

conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
conn.setAutoCommit(false);  //permettre le traitement en batch     
///sp = conn.setSavepoint("Savepoint1");
///LOG.debug("Savepoint taken !");
boolean b = false;
      for(int i=0;i<rows;i++)
        { LOG.debug("in the loop : row i = " + i);
          player = new Player();
          handicap = new Handicap();
                    LOG.debug("inner elem = " + values2D[i][0]);
                 player.setIdplayer(Integer.parseInt(values2D[i][0]) );
                   LOG.debug("idplayer inserted");
                 player.setPlayerFirstName(values2D[i][1]);
                 player.setPlayerLastName(values2D[i][2]);
                 player.getAddress().setCity(values2D[i][3]);
              //   player.getAddress().setCountry(values2D[i][4]);
                 //mod 22-12-2022
                 player.getAddress().getCountry().setCode(values2D[i][4]);
   //   à corriger           player.setPlayerBirthDate(SDF.parse(values2D[i][5]) );
  //               LocalDateTime.parse("2018-11-03T12:45:30"
                 player.setPlayerGender(values2D[i][6]);
                 player.setPlayerHomeClub(Integer.parseInt(values2D[i][7]) );
                 player.setPlayerPhotoLocation(values2D[i][8]);
                 player.setPlayerLanguage(values2D[i][9]);
                 player.setPlayerEmail(values2D[i][10]);
                     LOG.debug("player = " + player.toString());
                 handicap.setHandicapStart(SDF.parse(values2D[i][11]) );
      //        ps.setBigDecimal(7, club.getClubLongitude().setScale(6,RoundingMode.CEILING) );
                 BigDecimal bd = new BigDecimal(values2D[i][12]);
                 handicap.setHandicapPlayerEGA(bd);
                  LOG.debug("handicap = " + handicap.toString());
  //           }  
             LOG.debug("write player and handicap for record = " + i);
       //       create.CreatePlayer cp = new create.CreatePlayer();
       //      b = new create.CreatePlayer().create(player, handicap, conn, "B");
       //      LOG.debug("boolean returned from create player = " + b);
   // à modidier ici !!
        HandicapIndex handicapIndex = new HandicapIndex();
             if( ! new create.CreatePlayer().create(player, handicapIndex, conn, "B")){ // new 20/10/2014
                  LOG.debug("boolean returned from create player is false ==> rollback ");
                  array_return[0]= "ERROR";
                  array_return[1]= "ROLLBACK started";
                  conn.rollback();
                  conn.setAutoCommit(true);  //reset to normal state !!!!!
                  return array_return;
             }
             // si b est false, provoquer le rollback !!!!!!!!!!!!!!!
        }
LOG.debug("commit executed ");
conn.commit();
 //   return array_return;
	   }catch (SQLException ase){
               LOG.error(" -- SQLException > " + ase.getMessage()+ " / " + ase);	// error in arraycopy
                array_return[0]= "ERROR";
                array_return[1]= "SQL  ";
                conn.rollback();
                
            }catch (ArrayStoreException ase){
                LOG.error(" -- ArrayStoreException > " + ase.getMessage()+ " / " + ase);	// error in arraycopy
                array_return[0]= "ERROR";
                array_return[1]= "STORE";
                conn.rollback();
  //              return array_return;

            }catch(NullPointerException npe) // #2
            { 
              String msg = " -- NullPointerException = " + npe.getMessage() + " for = " + fi.toString();
              array_return[0]= "ERROR";
              array_return[1]= "NULLP";
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
                conn.rollback();
    //          return array_return;
            }catch (ArrayIndexOutOfBoundsException e) // # 3
	    { LOG.error(" -- ArrayIndexOutOfBounds Exception = " + e.getMessage());
              LOG.error("  for = " + fi.toString());
              LOG.error("  length array re = " + array_return.length + " / " +  NORMAL_INPUT_FIELDS);
              LOG.error("  length values2D = " + values2D[0].length);
              array_return[0]= "ERROR";
              array_return[1]= "OUTOF";
                  conn.rollback();
     //         return array_return;
	    }catch (IOException ex)  // # 4
            { LOG.error(" -- Input File not found !!! exception catched here !");
  	      //ex.printStackTrace();
  	     LOG.error(ex.getMessage());
              array_return[0]= "ERROR";
              array_return[1]= "IOEXC";
                  conn.rollback();
  //            return array_return;
  	    }catch (Exception e)  // # 5
            { LOG.error(" -- Error in reading CSV File: ");
              //e.printStackTrace();
              array_return[0]= "ERROR";
              array_return[1]= "EXCEP";
                  conn.rollback();
     //         return array_return;
	}finally{
          // empty
        conn.setAutoCommit(true);  //reset to normal state !!!!!
   //           return array_return;
	  }
	return array_return;

}   // end method setExtract

//public static int errorsCount ()//
//{
//    return errorsCSV;//
//}
} // end class GolfCSVFile