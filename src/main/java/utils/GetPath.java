/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import static interfaces.Log.LOG;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**   not working !!!
 *
 * @author Collet
 */
public class GetPath {
    static String path;
public static String getpath()throws UnsupportedEncodingException {
 
//    path = this.getClass().getClassLoader().getResource("").getPath();
String fullPath = URLDecoder.decode(path, "UTF-8");
String pathArr[] = fullPath.split("/WEB-INF/classes/");
LOG.info("fullpath = " + fullPath);
LOG.info("patharr[0] = " + pathArr[0]);

fullPath = pathArr[0];
String reponsePath = "";
// to read a file from webcontent
reponsePath = new File(fullPath).getPath() + File.separatorChar + "newfile.txt";
return fullPath;
}
public void main(String[] args) throws IOException // testing purposes
{
    String s = getpath();
//int numberOfEntries = zipFile.size();
LOG.info("path = " + s);


}// end main

} //end class
