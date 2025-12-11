package utils;

import entite.Settings;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FilesOfDirectoryHelp {

  public static List<Path> files() throws IOException{
//   String dir = Settings.getProperty("HELP");
 //    String dir = "C:/Users/Collet/Documents/NetBeansProjects/GolfWfly/src/main/webapp/help";
 //   LOG.debug("directory HELP = " + dir);
   
 List<Path> fileNames = Files.list(Paths.get(Settings.getProperty("HELP")))
                             .filter(Files::isRegularFile)
                             .map(Path::getFileName)  // uniquement le nom de fichier !!
                             .collect(Collectors.toList());
 // fileNames.forEach(item -> LOG.debug("name only =" + item));  // java 8 lambda
    return fileNames;
}
  
 void main() throws Exception {
  try{
      List<Path> list = files();
List<String> li = new ArrayList<>();
      for(int i=0; i < list.size() ; i++){
         li.add(list.get(i).getFileName().toString());
      }
       li.forEach(item -> LOG.debug("li - filenameonly =" + item));  // java 8 lambda
      Object[] array = li.toArray();
      LOG.debug("print array = " + Arrays.deepToString(array));
  //    list.forEach(item -> LOG.debug("FilesOfDirectory =" + item));  // java 8 lambda

 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         
   }
  } // end main//
} // end class