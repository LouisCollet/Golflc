
package mongo;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import entite.HelpView;
import static interfaces.Log.LOG;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import static utils.LCUtil.showMessageFatal;

//https://www.baeldung.com/java-list-directory-files

public class MongoHelpLoad {
// initial load from directory with one help file per .xhtml file ...
    // just executed once 
 public void load() throws Exception {
 try (MongoClient mongoClient = MongoClients.create()) {
    MongoDatabase database = mongoClient.getDatabase("golflc");
    database.getCollection("help_view").drop();
       LOG.debug("collection HELP-VIEW dropped ! ");
    database.createCollection("help_view");
       LOG.debug("collection help_view created ! ");
    String dir = "C:/Users/Louis Collet/Documents/NetBeansProjects/GolfWfly/src/main/webapp/help";
    List<Path> fileNames = Files
        .list(Paths.get(dir))
        .filter(Files::isRegularFile)
       // .map(Path::getFileName)  // uniquement le nom de fichier !!
      //  .map(Path::toString)  pas utile dans ce cas
        .collect(Collectors.toList());

    fileNames.forEach((temp) -> {LOG.debug(temp);});
    int count = 0;
    for(Path f : fileNames){
        String name = f.getFileName().toString();
      //      LOG.debug("name = " + name);
        String content = Files.readString(f, StandardCharsets.UTF_8); // reads file content in a String
      //      LOG.debug("content = " + content);
   // file name : enlever prefix help_ et suffixe .xhtml;
        String s = name.substring("help_".length());
      //      LOG.debug("s = " + s);
        name = s.substring(0,s.length()-".xhtml".length());
      //      LOG.debug("t = " + t);
   // utiliser entite
        HelpView helpview = new HelpView();
        helpview.setId(name);  // document key mod 19/11/2022
        helpview.setHelpViewText(content);
        helpview.setHelpViewLanguage("FR");
        helpview.setHelpViewModificationDate(LocalDateTime.now());
     // call MongoHelpController
        boolean b = new Controllers.MongoHelpController().create(helpview);
        if(b){
            count = count + 1;
        }
}
LOG.debug("end of execution, documents created = " + count);
 
  } catch (MongoException e) {
        String msg = "exception in create !!" + e;
        LOG.info(msg);
       // write(msg);
        showMessageFatal(msg);
      //  return null;
    }
 }

void main() throws Exception {
    new MongoHelpLoad().load();
    LOG.debug("end main");
    }
} //end class