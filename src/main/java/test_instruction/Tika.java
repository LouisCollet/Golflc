package test_instruction;

import static interfaces.Log.LOG;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class Tika {
 //@Inject
//private MyTimer myTimer;
 
 public boolean contentControl(String file) throws SQLException, IOException{
   //     PreparedStatement ps = null;
        FileInputStream is = null;
   try {
/*     
LOG.info(" main starting initTimer 00");
    new MyTimer().initTimer("message MyTimer form LC");
        LOG.info(" main starting execute 01");
    new MyTimer().execute();
        LOG.info(" main starting 02");
    */
 //  String s = "C:/Users/Collet/Pictures/modify.jpg"; // est en réalité png !!!
      File f = new File(file);
        LOG.info("File f = " + f.toString());
        LOG.info("extension = " + FilenameUtils.getExtension(file)); 
      is = new FileInputStream(f);
      Metadata metadata = new Metadata();
      metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
      Parser parser = new AutoDetectParser();
      // OOXMLParser parser = new OOXMLParser();
      ContentHandler contenthandler = new BodyContentHandler();
      parser.parse(is, contenthandler, metadata,new ParseContext());
   
  //    LOG.info("Title: " + metadata.get(Metadata.TIKA_MIME_FILE));
  //    LOG.info("Last Modified : " + metadata.get(Metadata.LAST_MODIFIED));
  //    LOG.info("Content: " + contenthandler.toString());
      LOG.info("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
      String mime = metadata.get(Metadata.CONTENT_TYPE);
      mime = mime.substring(mime.length() - 3);
        LOG.info("mime 3 dernières positions = " + mime);
      file = file.substring(file.length() - 3);
         LOG.info("file 3 dernières positions = " + file);
      if(!file.equals(mime)){
          LOG.info("error content");
          return false;
      }else{
          return true;
      }
          
   //   metadata.get(Metadata.CONTENT_TYPE)
  //    .substring(mime.length() - 3);

//      return true;
    } catch (Exception ex) {
        LOG.info("exception " + ex);
        return false;

    }finally{
        if (is != null) is.close();
    }
  } // end method
  public static void main(String args[]) throws Exception {
      boolean b = new Tika().contentControl("C:/Users/Collet/Pictures/page1.jpg"); // est en réalité pdf png !!!");
  }

} //end class