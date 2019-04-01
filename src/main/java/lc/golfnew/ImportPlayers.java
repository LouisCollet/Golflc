
package lc.golfnew;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import org.primefaces.PrimeFaces;
import utils.DBConnection;


// @Named("importP")
// @SessionScoped
@NamedSessionScoped  // new 25/12/2016, jus t for the fun see Stereotype
public class ImportPlayers implements Serializable, interfaces.GolfInterface, interfaces.Log  // on y trouve les constantes
{
   //  private static String []array_fields = new String [NORMAL_INPUT_FIELDS];
    private static String [] array_return;

public ImportPlayers() 
{
    // constructor
}

    public static void main(String s) throws IOException, SQLException, Exception
{
    LOG.info("starting main with " + s);
    DBConnection dbc = new DBConnection();
    Connection conn = dbc.getConnection();
    try{

     //   File f = new File("C:/Users/collet/Documents/NetBeansProjects/GolfWfly/importPlayers.txt");
        File f = new File(Constants.USER_DIR + "importPlayers.txt");
            LOG.info("using file = " + f.toString());
        array_return = utils.GolfCSVFile.getCSVExtract(conn, f);
            //  extraire les fields dans string array
         LOG.info("after golfCSVFile call");
   ////     if (array_return[0].equals("ERROR") )     // erreur détectée dans GolfCSVFile
   ////       { LOG.info("ERROR CSV = " + array_return[1]);
//             throw new RuntimeException("error in CSV - rollback initiated");
        //          continue;    // remonte boucle et traite fichier suivant
  ////        }else{
  ////          LOG.info("OK for = " );
    ////      }
        PrimeFaces.current().executeScript("alert('msg from ImportPlayers')");
        
 //      conn.commit();
 }catch (Exception ex){
    LOG.info("Fatal Exception in importPlayers" + ex );

} finally {
            DBConnection.closeQuietly(conn, null, null, null); // new 14/08/2014
        }
} // end of main

} //end class ImportPlayers