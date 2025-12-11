
package pem;

//import com.google.api.client.util.PemReader;
import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DEREncodable;
import java.security.PEMDecoder;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
//mport java.security.cert.


//https://www.baeldung.com/java-25-features

public class PemExample {
    
        // non utilisé !
//https://www.baeldung.com/java-read-pem-file-keys
    public static RSAPublicKey readX509PublicKey(File file) throws Exception {
    String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

    String publicKeyPEM = key
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replaceAll(System.lineSeparator(), "")
      .replace("-----END PUBLIC KEY-----", "");

    byte[] encoded = org.apache.hc.client5.http.utils.Base64.decodeBase64(publicKeyPEM);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
}
    
    
    
    
    
    @SuppressWarnings("preview")
    public static void main(String[] args) throws IOException {
        
 //   Path path = Paths.get("cert.pem");
 //   X509Certificate cert;
 //   try (Reader reader = Files.newBufferedReader(path)) {
 //      cert = PemReader.readCertificate(reader);
 //   }
 //    System.out.println(cert.getSubjectX500Principal());
        
      String pem = """
        -----BEGIN PUBLIC KEY-----
        MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgjDohS0RHP395oJxciVaeks9N
        KNY5m9V1IkBBwYsMGyxskrW5sapgi9qlGSYOma9kkko1xlBs17qG8TTg38faxgGJ
        sLT2BAmdVFwuWdRtzq6ONn2YPHYj5s5pqx6vU5baz58/STQXNIhn21QoPjXgQCnj
        Pp0OxnacWeRSnAIOmQIDAQAB
        -----END PUBLIC KEY-----
        """;

        try {
            String base64 = pem.replaceAll("-----.*-----", "").replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(base64);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey key = factory.generatePublic(spec);

            System.out.println("Loaded key algorithm: " + key.getAlgorithm());
            System.out.println("Loaded key format: " + key.getFormat());
            System.out.println("Loaded key toString: " + key.toString());
            
            System.out.printf("Hello JEP-470! OpenSSL decoding public PEM file%n%n");
    
    System.out.printf("Read the OpenSSL test-public.pem file into a String%n");    
    InputStream is = PemExample.class.getClassLoader().getResourceAsStream("C:/Users/Louis Collet/cacert.pem"); // is null ...
    byte[] bytes = is.readAllBytes();
    String publicPem = new String(bytes, StandardCharsets.UTF_8);
    System.out.printf("%s%n", publicPem);
    
    System.out.printf("Create PEMDecoder%n");
    PEMDecoder pd = PEMDecoder.of();

    System.out.printf("Decode the OpenSSL test-public.pem file%n");
    DEREncodable derEncodable = pd.decode(publicPem);
    switch (derEncodable) {
      case PublicKey publicKey -> System.out.printf("Successfully decoded PublicKey!%n");  
      default -> System.out.printf("What is \"%s\"%n", derEncodable.getClass().getName());
    }      
 // }
           } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }
}
