/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import java.util.Enumeration;

/**
 *
 * @author collet
 */
public class CreateZipFile implements interfaces.Log
{
  //  enum enu {}
    
    public static String ZipFile(String sourceDirectory, String zipFile)// throws SQLException
    {
    try
    {
   //  String zipFile = "C:/FileIO/zipdemo.zip";
   //  String sourceDirectory = "C:/examples";
 
     //create byte buffer
     byte[] buffer = new byte[1024];
    /*
     * To create a zip file, use
     * ZipOutputStream(OutputStream out)
     * constructor of ZipOutputStream class.
     */
      //create object of FileOutputStream
          FileOutputStream fout = new FileOutputStream(zipFile);

      //create object of ZipOutputStream from FileOutputStream
          ZipOutputStream zout = new ZipOutputStream(fout);

      //create File object from directory name
          File dir = new File(sourceDirectory);

      //check to see if this directory exists
         if(!dir.isDirectory())
         {
            LOG.info(sourceDirectory + " is not a directory");
            return null;
         }else{
             File[] files = dir.listFiles();
             for(int i=0; i < files.length ; i++)
             {
                LOG.info("Adding " + files[i].getName());
              //create object of FileInputStream for source file
                 FileInputStream fin = new FileInputStream(files[i]);
             /*
            * To begin writing ZipEntry in the zip file, use
            * void putNextEntry(ZipEntry entry)
            * method of ZipOutputStream class.
            * 
            * This method begins writing a new Zip entry to 
            * the zip file and positions the stream to the start 
            * of the entry data.
            */
           zout.putNextEntry(new ZipEntry(files[i].getName()));
          /*
          * After creating entry in the zip file, actually 
          * write the file.
          */
            int length;
             while((length = fin.read(buffer)) > 0)
            {
                zout.write(buffer, 0, length);
            }
           /*
           * After writing the file to ZipOutputStream, use
           * void closeEntry() method of ZipOutputStream class to 
           * close the current entry and position the stream to 
           * write the next entry.
           */
           zout.closeEntry();
           //close the InputStream
           fin.close();
           } //end for
      } // end is Directory

    //close the ZipOutputStream
    zout.close();                  
    LOG.info("Zip file created = " + zipFile);
    LOG.info("Zip file size = " + zipFile.length());
    return "OK";
    } // end try
   catch(ZipException z)
    {
          LOG.info("ZipException :" + z);
              return null;
    }
    catch(IOException ioe)
    {
          LOG.info("IOException :" + ioe);
              return null;
    }
//return null;
    }
    
public static void main(String[] args) throws IOException // testing purposes
{
    String source = "c:/tmp/download/job1";
    String zipfile = "c:/tmp/downloadArchives/job1.zip";
ZipFile(source,zipfile);
ZipFile zipFile = new ZipFile(zipfile);
//int numberOfEntries = zipFile.size();
LOG.info("number of entries = " + zipFile.size());
Enumeration enu = zipFile.entries();
while (enu.hasMoreElements())
{
    LOG.info(" elements of zip = " + enu.nextElement()); 
}

}// end main
} // end class
