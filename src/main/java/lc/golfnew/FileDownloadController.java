/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package lc.golfnew;

import java.io.*;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

public class FileDownloadController implements interfaces.Log
{

private StreamedContent file = null;

public FileDownloadController() throws IOException
{
    InputStream is = null;
try
{
    LOG.info("entering 01");
        is= new FileInputStream(new File("/images/sofa.png"));
	file = new DefaultStreamedContent(is, "image/jpg", "downloaded_optimus.jpg");
        LOG.info("file downloaded = " + file);
}
catch(IOException e)
        {
            LOG.info("IOException upload Exception LC = " + e.getMessage() );
        }
finally
{
    is.close();
}
}

public StreamedContent getFile()
{
    LOG.info("entering getFile");
        return file;
}

         public void setFile(StreamedContent file)
         {                 this.file = file;
         }


} //end class
