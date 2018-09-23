package lc.golfnew;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
//import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
 
public class MonitorDirectory implements interfaces.Log
{
 private static final String basePath = "C:/aa (LC Data)/LC (User)/Golf/Ryder Cup";
 private static Path propDir;
 
  public static void main(String[] args) throws IOException, InterruptedException 
  {
 int count = 0;
      LOG.info("new version starting monitoring");
    FileSystem fs = FileSystems.getDefault();
    final WatchService ws = fs.newWatchService();  
    propDir = fs.getPath(basePath);
    propDir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
           StandardWatchEventKinds.ENTRY_DELETE,
           StandardWatchEventKinds.ENTRY_MODIFY,
           StandardWatchEventKinds.OVERFLOW);
while(true)
{
    WatchKey key = null;
    try 
    {
         key = ws.take();
    } catch(InterruptedException ie)
    {
              ie.printStackTrace();
    }

            for(WatchEvent<?> event: key.pollEvents())
            {
                switch(event.kind().name())
                {
                    case "OVERFLOW":
                        LOG.info(++count + ": OVERFLOW");
                        break;
                    case "ENTRY_MODIFY":
                        LOG.info(++count + ": File " + event.context() + " is changed!");
                        break;
                    case "ENTRY_CREATE":
                        LOG.info(++count + ": File " + event.context() + " is created!");
                        break;
                    case "ENTRY_DELETE":
                        LOG.info(++count + ": File " + event.context() + " is deleted!");
                        break;
                    default:
                        LOG.info(++count + ": UNKNOWN EVENT!");
                } //end switch
            } //end for
            key.reset();
  LOG.info("ending watch event");
}    

  } // end main
  
} //end class