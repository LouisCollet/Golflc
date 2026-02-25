package test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Louis Collet
 */
public class Savetofile {
 
    // Main driver method
    public static void main(String[] args){
        writeString("SUBSCRIPTION");
        readString();
    }
    
    public static void writeString(String in){
        Path path = Paths.get("C:/log/savetofile.txt");
        String strW = in;
        // Try block to check for exceptions
        try {
            // Now calling Files.writeString() method with path , content & standard charsets
            Files.writeString(path, strW, StandardCharsets.UTF_8, StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING);
     //?       Files.write(path, in.getBytes());
        }
        // Catch block to handle the exception
        catch (IOException ex) {
            // Print messqage exception occurred as
            // invalid. directory local path is passed
            System.out.print("Invalid Path");
        }
    }   // end method write
     public static void readString(){    
        try{
                // Creating a path choosing file from local
        // directory by creating an object of Path class
      //  Path fileName = Path.of("C:/log/savetofile.txt"");
        Path fileName = Path.of("C:/log/savetofile.txt");;

        // Now calling Files.readString() method to read the file
        String strR = Files.readString(fileName);

        // Printing the string
        System.out.println("String readed = " + strR);
        
        }catch (IOException ex) {
            // Print messqage exception occurred as
            // invalid. directory local path is passed
            System.out.print("Invalid Path");
        }
        
    } //end method read
} // end class