package utils;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import net.glxn.qrgen.javase.QRCode;
import java.io.IOException;
import net.glxn.qrgen.core.image.ImageType;

@ApplicationScoped
public class QRCodeService {
// new 28-12-2025 utilise under de hood : import com.google.zxing.BarcodeFormat;

  //  public Path generateTempQR(String content, int size) throws IOException {
     public byte[] generateQR(String content, int size) throws IOException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("QR content must not be empty");
        }
     //   Path temp = Files.createTempFile("Qrc", ".png");
     //   byte[] png = QRCode.from(content).withSize(size, size);
     //   Files.write(temp, png);
     //       LOG.info("QR code generated at {}", temp);
        return QRCode
            .from(content)
            .to(ImageType.PNG)
            .withSize(size, size) // width, height
            .stream()  // Retourne ByteArrayOutputStream
            .toByteArray(); // // On récupère le tableau d’octets
     } //end method

 /*
public static boolean createQR(String data,
                        Path path,
                        Map<EncodeHintType,Object> hashMap, // mod 05-05-2025
			int height, int width){
// https://www.toomanyrequests.com/generating-branded-qr-codes-with-java-and-zxing/
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
*/
/*
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
*/
void main() throws WriterException, IOException,NotFoundException{
	// The data that the QR code will contain
        String content = "Hello ! This is golfLC, the famous application v2";
 //       String strPath = "c:/log/demo.png";// The path where the image will get saved
 //       Path path = Paths.get(strPath);// converts string to path
  //      path = manageQR(data, path);
        var path = new QRCodeService().generateQR(content, 200);
   //     Path path = manageQR(data);
	LOG.debug("QR Code Generated on Path = " + path);

} //end method
} //end class