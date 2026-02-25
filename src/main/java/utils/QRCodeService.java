package utils;

// Java code to generate QR code

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import static interfaces.Log.LOG;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
//@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class QRCodeGenerator {
// Function to create the QR code
public static boolean createQR(String data,
                        Path path,
                        Map<EncodeHintType,Object> hashMap, // mod 05-05-2025
			int height, int width){
try{
	BitMatrix matrix = new MultiFormatWriter().encode(
                data,
		BarcodeFormat.QR_CODE,
                width, 
                height);

        MatrixToImageWriter.writeToPath(matrix, "png",path);
        LOG.debug("path.getFileName() = " + path.getFileName());
     return true;

}catch (Exception ex){
    String msg = "Exception in createQR" +  ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return false;
}
} //end method

public static Path manageQR(String qrCodeText){ //, Path path){
try{
        Map<EncodeHintType, Object> map = new EnumMap<>(EncodeHintType.class);
  //   https://zxing.github.io/zxing/apidocs/com/google/zxing/EncodeHintType.html
    
	map.put(EncodeHintType.ERROR_CORRECTION, "H");
        map.put(EncodeHintType.CHARACTER_SET, "utf-8");
        map.put(EncodeHintType.MARGIN, "0");
   // Create an temporary file
        Path temp = Files.createTempFile("Qrc", ".png"); // prefix, randomnumber, suffix
  //         LOG.debug("QRC Temp file : " + temp);
        if(createQR(qrCodeText, temp, map, 200, 200)){
  //           LOG.debug("we are OK");
             LOG.debug("size Path temp = " + Files.size(temp));
	     LOG.debug("QR Code Generated!!! + " + temp);
             return temp;
        }else{
             LOG.debug("error");
             return null;
        }
}catch (Exception ex){
    String msg = "Exception in manageQR" +  ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}
} //end method

void main()	throws WriterException, IOException,NotFoundException{
	// The data that the QR code will contain
        String data = "Hello ! This is golfLC, the famous application v2";
 //       String strPath = "c:/log/demo.png";// The path where the image will get saved
 //       Path path = Paths.get(strPath);// converts string to path
  //      path = manageQR(data, path);
        Path path = manageQR(data);
	LOG.debug("QR Code Generated on Path = " + path);

} //end method
} //end class