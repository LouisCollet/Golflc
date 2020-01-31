package test_instruction;

import static interfaces.Log.LOG;
import java.io.File;
import java.io.FileInputStream;
import javax.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class Tika {
 @Inject
private MyTimer myTimer;
 
public static void main(String args[]) throws Exception {
   FileInputStream is = null;
 try {      
LOG.info(" main starting initTimer 00");

    new MyTimer().initTimer("message MyTimer form LC");
        LOG.info(" main starting execute 01");
    new MyTimer().execute();
        LOG.info(" main starting 02");
    
        
        
        
   
   
  //       Class.forName("com.github.jaiimageio.jpeg2000.impl.J2KImageReader");
   //   File f = new File("C:/Users/Collet/Pictures/modify.png");
   String s = "C:/Users/Collet/Pictures/modify.jpg"; // est en réalité png !!!
      File f = new File(s);
      LOG.info("yes f = " + f.toString());
      LOG.info("extension = " + FilenameUtils.getExtension(s)); 
      ContentHandler contenthandler = new BodyContentHandler();
      Metadata metadata = new Metadata();
      metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
      
      Parser parser = new AutoDetectParser();
      // OOXMLParser parser = new OOXMLParser();
      is = new FileInputStream(f);
      parser.parse(is, contenthandler, metadata,new ParseContext());
      LOG.info("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
      LOG.info("Title: " + metadata.get(Metadata.TIKA_MIME_FILE));
      LOG.info("Author: " + metadata.get(Metadata.LAST_MODIFIED));
      LOG.info("Content: " + contenthandler.toString());
    }
    catch (Exception ex) {
        LOG.info("exception " + ex);
  //    e.printStackTrace();
    }
    finally {
        if (is != null) is.close();
    }
  }
} // end