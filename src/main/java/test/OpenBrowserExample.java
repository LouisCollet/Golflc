package test;
import static interfaces.Log.LOG;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenBrowserExample {
    public static void main(String[] args) {
        try {
            // Check if Desktop API is supported
            LOG.debug("entering main of OpenBrowerExample");
        //    var proc = new String[]{"notepad.exe","null"};
        //    Runtime runtime = Runtime.getRuntime();
        //    Process process = runtime.exec(proc);
        //    proc = new String[]{"calc.exe","null"};
        //    process = runtime.exec(proc);
             // create a process and execute calc.exe and currect environment
        //    proc = new String[]{"explorer.exe","null"};
         //   process = runtime.exec(proc);
          //   String url = "https://stackoverflow.com/questions"; 
              URL url = new URI("https://stackoverflow.com/questions").toURL();
             LOG.debug("port = " + url.getPort());
             LOG.debug("host = " + url.getHost());
             LOG.debug("file = " + url.getFile());
             // solution alternative à Desktop
             Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()});  // was url/String
        //    Desktop.getDesktop().open(new File("c:\\a.doc"));
            // was http://stackoverflow.com"
            
        //    runtime.exec( "rundll32 url.dll,FileProtocolHandler " + url); // deprecated
      //  Runtime.getRuntime().exec(new String[]{"rundll32.exe", "url.dll", "FileProtocolHandler", url});
       //     var proc = new String[]{"rundll32.exe", "url.dll", "FileProtocolHandler", url}; // was ok cherché longtemps !!
       //     Process process = Runtime.getRuntime().exec(proc);
       //       LOG.debug("process : " + process.toString());
       //       LOG.debug("process info : " + process.info());
            if (Desktop.isDesktopSupported()) {
                LOG.debug("Desktop is supported");
                Desktop desktop = Desktop.getDesktop();
               // LOG.debug("is supported Browse : " + desktop.isSupported(java.awt.Desktop.Action.BROWSE));
                LOG.debug("is supported email : " + desktop.isSupported(Desktop.Action.MAIL));
             //   desktop.setAboutHandler(aboutHandler);
                if(desktop.isSupported(Desktop.Action.BROWSE)) { // Open the URI in the default browser
                    LOG.debug("Browse is supported" );
                    desktop.browse(new URI("https://forum.inductiveautomation.com/t/expose-files-to-the-system/48008/6"));
                    desktop.browse(URI.create("chrome"));  // will open browser automatically with empty URL
                    desktop.browse(URI.create("firefox")); 
                    String baseUrl = "http://example.com/search";
                    String queryParam = "java";
                   String encodedQuery = URLEncoder.encode("q=" + queryParam, "UTF-8");
                    URI uri = new URI(baseUrl + "?" + encodedQuery);
                  Desktop.getDesktop().browse(uri);  // https://example.com/search?q%3Djava
                }else{
                     LOG.debug("browse is NOT supported on this platform !! " );
                }
            } else {
                LOG.debug("Desktop API is not supported on this system.");
            }
        } catch (Exception e) {
            LOG.debug("exception in main of OpenBrowserExample " + e);
        }
    } //end main

public void readAndOpenHTML() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("nameOftheResource.html");
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        Path tmpPath= Files.createTempFile("temporalName",".html");
        File html = tmpPath.toFile();
        OutputStream os = new FileOutputStream(html);
        os.write(buffer);
        if (Desktop.isDesktopSupported()) {
            new Thread(()-> {
                try {
                    Desktop.getDesktop().browse(html.toURI());
                } catch (IOException e) {
                    //e.printStackTrace();
                    LOG.info("IOEX in readAndOpenHTML ");
                    LOG.info(e.toString());
                }catch (Exception e){
                    LOG.info("exception in readAndOpenHTML ");    
                }
            }).start();
        } else {
            LOG.info("Desktop browser not suported");
        }
        os.close();
    }

} //end class
