package utils;

/**
 * inspiration : http://ostermiller.org/utils/CSV.html
 * Comma Separated Values (CSV) - com.Ostermiller.util Java Utilities
 * Version 1.07.00
 * @author Louis Collet
 */

import com.Ostermiller.util.*;
import entite.Handicap;
import entite.Player;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("golfCSV")
@SessionScoped
 public class GolfCSVFile implements Serializable, interfaces.Log, interfaces.GolfInterface
/**
 * CSV in action: extract fields from a CSV File using Ostermiller CSVParser */
{
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
/**
 * @return nombre total des fichiers traitÃƒÆ’Ã‚Â©s
 */
//public static int getTallyFiles() // static new 22 11 2009
//    {return nb - errorsCSV ;}  // mod 13/11/2009

/**
 * @param fi fichier input, format .txt
 * @return array_return = String array with fields
 * si error, le premier ÃƒÆ’Ã‚Â©lÃƒÆ’Ã‚Â©ment de l'array contient "ERROR"
 * 
 * handles CSV files
 */
public static  String [] getCSVExtract(final Connection conn, final File fi) throws SQLException 
{
//  nb++;
        LOG.info("starting getExtract");
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
//     LOG.info("still here 01");
/*Charset Description

US-ASCII     Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
ISO-8859-1   ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
UTF-8        Eight-bit UCS Transformation Format
UTF-16BE     Sixteen-bit UCS Transformation Format, big-endian byte order
UTF-16LE     Sixteen-bit UCS Transformation Format, little-endian byte order
UTF-16       Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark
*/

     final char delimiter = ';' ;		// attention char mandatory, single quote !!
      LOG.info("normal input fields must be : " + NORMAL_INPUT_FIELDS);
     values2D = com.Ostermiller.util.CSVParser.parse(br, delimiter); // was in
            // see http://ostermiller.org/utils/doc/com/Ostermiller/util/CSVParser.html
//             values2D contient les records, normalement il ne doit y en avoir qu'un
     LOG.info("after Ostermiller : values2D = " + Arrays.deepToString(values2D) );
     LOG.info("after Ostermiller : nombre de joueurs = " + values2D.length );
     br.close();
     
     int rows=values2D.length;
        LOG.info("rows = " + rows);
     int columns = NORMAL_INPUT_FIELDS;
       LOG.info("columns = " + columns);
     
     
     for(int i = 0; i < rows; i++) // verification si toutes données présentes par joueur
        {
            if (values2D[i].length != columns)  //
                {
                 errorsCSV ++;
		 LOG.info("Uncomplete record = " + columns + " / fields = " + values2D[i].length + " / record = " + (i+1) + "/ errors = "+errorsCSV );
        //         LOG.error(NEWLINE + "Too much fields = {0}" + Arrays.deepToString(values2D));  //fine pas sur console
                }else{
                  LOG.info("Complete record = " + (i+1));
                }
                //end if
        } //enf for
     if(errorsCSV != 0)
     {
         LOG.info("there are {} errors - treatment stopped, no insert", errorsCSV);
         array_return[0]= "ERROR";
         array_return[1]= "FIELDS";
         return array_return;
     }
 //     int rows=values2D.length;
 //       LOG.info("rows = " + rows);
 //     int columns = NORMAL_INPUT_FIELDS;
 //      LOG.info("columns = " + columns);
  //    int i;
 // Set transaction isolation to SERIALIZABLE

conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
conn.setAutoCommit(false);  //permettre le traitement en batch     
///sp = conn.setSavepoint("Savepoint1");
///LOG.info("Savepoint taken !");
boolean b = false;
      for(int i=0;i<rows;i++)
        { LOG.info("in the loop : row i = " + i);
          player = new Player();
          handicap = new Handicap();
                    LOG.info("inner elem = " + values2D[i][0]);
                 player.setIdplayer(Integer.parseInt(values2D[i][0]) );
                   LOG.info("idplayer inserted");
                 player.setPlayerFirstName(values2D[i][1]);
                 player.setPlayerLastName(values2D[i][2]);
                 player.setPlayerCity(values2D[i][3]);
                 player.setPlayerCountry(values2D[i][4]);
                 player.setPlayerBirthDate(SDF.parse(values2D[i][5]) );
                 player.setPlayerGender(values2D[i][6]);
                 player.setPlayerHomeClub(Integer.parseInt(values2D[i][7]) );
                 player.setPlayerPhotoLocation(values2D[i][8]);
                 player.setPlayerLanguage(values2D[i][9]);
                 player.setPlayerEmail(values2D[i][10]);
                     LOG.info("player = " + player.toString());
                 handicap.setHandicapStart(SDF.parse(values2D[i][11]) );
      //        ps.setBigDecimal(7, club.getClubLongitude().setScale(6,RoundingMode.CEILING) );
                 BigDecimal bd = new BigDecimal(values2D[i][12]);
                 handicap.setHandicapPlayer(bd);
                  LOG.info("handicap = " + handicap.toString());
  //           }  
             LOG.info("write player and handicap for record = " + i);
              create.CreatePlayer cp = new create.CreatePlayer();
             b = cp.createPlayer(player, handicap, conn, "B");
             LOG.info("boolean returned from create player = " + b);
             if(b == false) // new 20/10/2014
             {
                  LOG.info("boolean returned from create player is false ==> rollback ");
                  array_return[0]= "ERROR";
                  array_return[1]= "ROLLBACK started";
                  conn.rollback();
                  conn.setAutoCommit(true);  //reset to normal state !!!!!
                  return array_return;
             }
             // si b est false, provoquer le rollback !!!!!!!!!!!!!!!
        }
LOG.info("commit executed ");
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
            }catch (BadDelimeterException bde) // #1 bis
	    {	LOG.error(" -- BadDelimiterException > " +
                        bde.getMessage()+ " / " + bde);	// error in arraycopy
                array_return[0]= "ERROR";
                array_return[1]= "DELIM";
                    conn.rollback();
    //            return array_return;
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
              return array_return;
	  }
	//return array_return;

}   // end method setExtract

//public static int errorsCount ()//
//{
//    return errorsCSV;//
//}
} // end class GolfCSVFile