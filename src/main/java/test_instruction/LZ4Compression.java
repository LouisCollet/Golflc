
package test_instruction;

//import static interfaces.Log.LOG;
import java.io.IOException;
import java.util.Arrays;
import net.jpountz.lz4.LZ4Compressor; 
import net.jpountz.lz4.LZ4FastDecompressor; 
//import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.*;
/**
 *
 * @author collet
 */
public class LZ4Compression 
{
    // Depending on your platform and your JVM, this method will pick the
// fastest LZ4Factory instance available
// Depending on your platform and your JVM, this method will pick the
// fastest LZ4Factory instance available
public static void main(String args[]) throws IOException
{  
try{
    
LZ4Factory factory = LZ4Factory.fastestInstance();
String[][] A_D2 = {{ "4",  "4",  "5",  "3",  "4",  "2",  "4",  "4",  "4",  "3",  "4",  "5",  "4",  "3", "4" ,  "4", "0" , "0" }, { "0", "0" ,"0" ,"0" ,"0"  ,"0"  , "0" ,"0"  , "0" ,"0"  ,"0"  , "0" , "0" ,"0"  , "0" , "0" ,"0" , "0" }, { "5",  "6",  "4",  "3",  "4",  "2",  "4",  "4",  "4",  "3",  "4",  "5", "4",  "4",  "4",  "6", "0" ,"0"  }, {"0" , "0" ,"0"  ,"0"  ,"0"  ,"0" , "0" ,"0"  , "0" , "0" , "0" ,"0"  ,"0"  , "0" ,"0" ,"0"  ,"0"  , "0" }, {"0" ,"0"  , "0" ,"0"  ,"0" , "0" ,"0" ,  "0", "0" ,"0" ,"0" ,"0"  , "0" , "0" ,"0"  ,"0"  ,"0"  ,"0"  }, { "0", "0" , "0" ,"0"  , "0" , "0" , "0" , "0" ,"0"  , "0" , "0" , "0" , "0" ,"0" , "0" , "0" , "0" ,"0"  }};     
String s = Arrays.deepToString(A_D2);

//byte[] data = "12345345234572".getBytes("UTF-8");
byte[] data = s.getBytes("ISO-8859-1");
 int decompressedLength = data.length;
 System.out.println("decompressedLength = " + decompressedLength);
// compress data
//LZ4Compressor compressor = factory.fastCompressor();
LZ4Compressor compressor = factory.highCompressor();
// or factory.highCompressor() for slower compression but better compression ratio
int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
byte[] compressed = new byte[maxCompressedLength];
int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);

 System.out.println("compressedLength = " + compressedLength);

// decompress data
// - method 1: when the decompressed length is known
LZ4FastDecompressor decompressor = factory.fastDecompressor();
byte[] restored = new byte[decompressedLength];
int compressedLength2 = decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
 System.out.println("compressedLength2 = " + compressedLength2);
// compressedLength == compressedLength2
// - method 2: when only the compressed length is known (a little slower)
// the destination buffer needs to be over-sized

LZ4SafeDecompressor decompressor2 = factory.safeDecompressor();
int decompressedLength2 = decompressor2.decompress(compressed, 0, compressedLength,restored, 0);
// decompressedLength == decompressedLength2

//byte[] data = "12345345234572".getBytes("UTF-8");
data = "12345345234572".getBytes("UTF-8");
decompressedLength = data.length;
// compress data
////LZ4Compressor compressor = factory.fastCompressor();
// or factory.highCompressor() for slower compression but better compression ratio
maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
compressed = new byte[maxCompressedLength];
compressedLength = compressor.compress(
  data, 0, decompressedLength,
  compressed, 0, maxCompressedLength);
// decompress data

// - method 1: when the decompressed length is known
//LZ4FastDecompressor decompressor = factory.fastDecompressor();
restored = new byte[decompressedLength];
 compressedLength2 = decompressor.decompress(
  compressed, 0,
  restored, 0, decompressedLength);
// compressedLength == compressedLength2
// - method 2: when only the compressed length is known (a little slower)
// the destination buffer needs to be over-sized
  decompressor2 = factory.safeDecompressor();
decompressedLength2 = decompressor2.decompress(
  compressed, 0, compressedLength,
  restored, 0);
// decompressedLength == decompressedLength2
}catch(Exception ex){
    String msg = "Exception in thumbs() : " + ex;
    System.out.println(" error = " + msg);            
   
} finally {    }
} //end method
} //end class