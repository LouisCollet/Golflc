package Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Creditcard;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.ZDF_YEAR_MONTH_DAY;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import io.mikael.urlbuilder.UrlBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import jakarta.inject.Inject;
import static utils.LCUtil.showMessageFatal;

@Named("httpC")
@SessionScoped
public class HttpController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private entite.Settings settings;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private transient CookieManager cookieManager;
    private transient CookieStore   cookieStore;
    private transient HttpClient    httpClient;
    private Creditcard    creditcard;

    public HttpController() { } // end constructor

    public Creditcard getCreditcard() {
        return creditcard;
    } // end method

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    } // end method

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        LOG.debug("cookieManager created");
        cookieStore = cookieManager.getCookieStore();
        LOG.debug("cookieStore created");
        httpClient = getHttpClient();
        LOG.debug("httpClient created (immutable)");
    } // end method

    public HttpClient getHttpClient() { // pas private car classe de test
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            SSLContext sslContext = buildSSLContext(); // new 09/03/2026
            HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(50))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieManager);
            if (sslContext != null) {
                builder.sslContext(sslContext);
                LOG.debug("custom SSLContext applied (ca.pem)");
            } else {
                LOG.warn("custom SSLContext not available, using JVM default");
            }
            return builder.build();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String sendPaymentServer(Creditcard creditc) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("creditcard = {}", creditc);
        // guard: re-init after session deserialization (transient fields)
        if (httpClient == null) {
            LOG.warn("httpClient is null — reinitializing (post-activation or init failure)");
            init();
        }
        if (httpClient == null) {
            LOG.error("httpClient could not be initialized");
            showMessageFatal("Payment client not available");
            return "ClientNotInitialized";
        }
        HttpResponse<String> response = null;
        try {
            creditc.setCreditCardExpirationDate(
                creditc.getCreditCardExpirationDateLdt().format(ZDF_YEAR_MONTH_DAY));
            LOG.debug("expiration date formatted = {}", creditc.getCreditCardExpirationDate());

            String strJson = OBJECT_MAPPER.writeValueAsString(creditc);
            LOG.debug("creditcard JSON = {}", NEW_LINE + strJson);

            // migrated 2026-03-28 — return_url as query param instead of ReturnDirectory header
            URI uri = UrlBuilder.empty()
                .withScheme("https")
                .withHost("localhost")
                .withPort(5000)
                .withPath("/creditcard")
                .addParameter("return_url", utils.LCUtil.firstPartUrl() + "/rest/payment/") // was: header "ReturnDirectory"
                .toUri();
            LOG.debug(" - uri = {}", uri);

            // security audit 2026-03-19 — unique nonce per transaction (P2)
            // moved before HMAC: nonce is now part of the canonical payload (migrated 2026-03-28)
            String nonce = java.util.UUID.randomUUID().toString().replace("-", "");
            creditc.setPaymentNonce(nonce);

            // security audit 2026-03-20 — unique request ID for anti-replay protection
            // moved before HMAC: requestId is now part of the canonical payload (migrated 2026-03-28)
            String requestId = java.util.UUID.randomUUID().toString();

            // HMAC-SHA256 authentication — migrated 2026-03-28 — AWS Signature v4 canonical request
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String method    = "POST";
            String path      = "/creditcard";
            String query     = uri.getRawQuery() != null ? uri.getRawQuery() : ""; // URL-encoded — matches Python request.query_string.decode('utf-8')
            // migrated 2026-03-28 — SHA-256(body) replaces raw body: PCI-friendly + fixed-length component
            String bodyHash  = HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(strJson.getBytes(StandardCharsets.UTF_8))
            );
            // String payload = timestamp + strJson;                                               // migrated 2026-03-28
            // String payload = String.join("\n", method, path, query, timestamp, nonce, strJson); // migrated 2026-03-28
            String payload = String.join("\n", method, path, query, timestamp, nonce, requestId, bodyHash);
            String hmacSecret = settings.getProperty("PAYMENT_HMAC_SECRET");
            if (hmacSecret == null) throw new IllegalStateException("PAYMENT_HMAC_SECRET env var not set");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            LOG.debug(" - nonce = {}", nonce);
            LOG.debug(" - requestId = {}", requestId);

            HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(strJson))
                .uri(uri) // migrated 2026-03-28 — URI now carries return_url query param
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(15))
                // .header("ReturnDirectory", utils.LCUtil.firstPartUrl() + "/rest/payment/") // migrated 2026-03-28 — moved to return_url query param
                .header("MerchantSite", "GolfLC Merchant Site")
                .header("X-Timestamp", timestamp)
                .header("X-Signature", signature)
                .header("X-Payment-Nonce", nonce)
                .header("X-Request-ID", requestId)
                .build();

            LOG.debug(" - request = {}", request);
           // request.headers().map().forEach((k, v) ->
           //     LOG.debug("response header: {}", k + " = " + v));
          //  LOG.debug("request headers = {}", request.headers());
            request.headers().map().forEach((k, v) ->
                LOG.debug(" - request header: {} = {}", k, v));

            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (ConnectException e) {
                String msg = "ConnectException - Payment Server not available - run creditCardService.py";
                LOG.error(msg);
                showMessageFatal(msg);
                return "ConnectException";
            } catch (HttpTimeoutException e) {
                String msg = "Payment server request timed out - check creditcardService.py and Memurai (net start Memurai)";
                LOG.error(msg);
                showMessageFatal(msg);
                return e.getMessage();
            }

            printResponse(response);
            LOG.debug("statusCode = {}", response.statusCode());

            if (response.statusCode() != 200) {
                LOG.debug("error code = {}", response.statusCode());
                String body = response.body();
                String msg;
                if (body != null && body.trim().startsWith("{")) {
                    java.util.Map<?, ?> map = OBJECT_MAPPER.readValue(body, java.util.Map.class);
                    LOG.error("payment server error response: {}", body);
                    Object message = map.get("message");
                    if (message == null) message = map.get("name");        // Python flask-limiter style
                    Object details = map.get("details");
                    if (details == null) details = map.get("description"); // Python flask-limiter style
                    msg = "HTTP " + response.statusCode() + " — "
                        + (message != null ? message : "Unknown error")
                        + (details != null ? " : " + details : "");
                } else {
                    msg = "Error from payment server (HTTP " + response.statusCode() + "): " + body;
                    LOG.error(msg);
                }
                showMessageFatal(msg);
                return Integer.toString(response.statusCode());
            }

            // statusCode == 200
            LOG.info(" - handling statusCode 200");
            LOG.debug(" - response sslSession present = {}", response.sslSession().isPresent());
            response.headers().map().forEach((k, v) ->
                LOG.debug(" - response header: {} = {}", k, v));

            

            String currency = response.headers().firstValue("Currency").orElse("");
            LOG.debug(" - currency = {}", currency);
            LOG.debug(" - response version = {}", response.version());

            List<HttpCookie> listCookies = cookieStore.getCookies();
        //    LOG.debug(" - cookieStore size = {}", listCookies.size());
            for (HttpCookie cookie : listCookies) {
                LOG.debug(" - cookie: {} = {}", cookie.getName(), cookie.getValue());
            }

        //    String cookieValue = getCookie(response.headers().map().get("set-cookie").get(2));  // returndirectory
        //    LOG.debug(" - cookie value get (2) = {}", cookieValue);
            
            List<URI> listUris = cookieStore.getURIs();
            listUris.forEach(item -> LOG.debug(" - URI fom cookieStore : {}", item));

            return Integer.toString(response.statusCode());

        } catch (Exception e) {
            if (e.getMessage() == null) {
                String msg = "Payment Server not available - run creditCardService.py on the python server";
                LOG.error(msg);
                showMessageFatal(msg);
                return "ServerUnavailable";
            }
            if ("request timed out".equals(e.getMessage())) {
                String msg = "Payment server request timed out - check creditcardService.py and Memurai (net start Memurai)";
                LOG.error(msg);
                showMessageFatal(msg);
                return e.getMessage();
            }
            LOG.error("Exception: {} response={}", e.getMessage(), response);
            showMessageFatal("Exception: " + e.getMessage());
            return e.getMessage();
        }
    } // end method

    public String getCookieValue(List<HttpCookie> listCookies, String name) { // migrated from static 2026-03-22
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("name = {}", name);
        try {
            for (HttpCookie cookie : listCookies) {
                if (name.equals(cookie.getName())) {
                    LOG.debug("found cookie {} = {}", name, cookie.getValue());
                    return cookie.getValue();
                }
            }
            LOG.debug("cookie {} not found", name);
            return "not found";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "not found";
        }
    } // end method

    public String getCookie(String header) { // migrated from static 2026-03-22
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("header = {}", header);
        try {
            int firstIndex = header.indexOf("=");
            if (firstIndex == -1) {
                LOG.debug("'=' not found");
                return "not found";
            }
            int lastIndex = header.indexOf(";");
            if (lastIndex == -1) {
                LOG.debug("';' not found");
                return "not found";
            }
            return header.substring(firstIndex + 1, lastIndex);
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "not found";
        }
    } // end method

    private static void printResponse(HttpResponse<?> response) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug(" - URI     : {}", response.uri());
        LOG.debug(" - Version : {}", response.version());
        LOG.debug(" - Status  : {}", response.statusCode());
        
      //  LOG.debug(" - Headers : {}", response.headers());
        response.headers().map().forEach((k, v) ->
                LOG.debug("response header: {} = {}", k, v));
   //     LOG.debug("Body    : {}", response.body());
    } // end method

    private SSLContext buildSSLContext() { // new 09/03/2026 pour ne plus utiliser cacerts et chipoter à chaque nouvelle version de JDK
        // code généré claude code
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Path caPemPath = Path.of(System.getProperty("user.home"), ".ssl", "creditcard", "ca.pem");
            LOG.debug("ca.pem path = {}", caPemPath);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert;
            try (InputStream is = new FileInputStream(caPemPath.toFile())) {
                caCert = (X509Certificate) cf.generateCertificate(is);
            }
            LOG.debug("CA subject = {}", caCert.getSubjectX500Principal());
            LOG.debug("CA valid until = {}", caCert.getNotAfter());

        //    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
            KeyStore trustStore = KeyStore.getInstance("PKCS12");// mod 21/04/2026 suite jdk 26
            
            trustStore.load(null, null);
            trustStore.setCertificateEntry("GolfLCCreditcardCertificate", caCert);
            LOG.debug("CA certificate loaded into trustStore");

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            LOG.debug("SSLContext initialized with custom trustStore");
            return sslContext;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

} // end class
