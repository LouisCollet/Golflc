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
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static utils.LCUtil.showMessageFatal;

@Named("httpC")
@SessionScoped
public class HttpController implements Serializable {

    private static final long serialVersionUID = 1L;

    private CookieManager cookieManager = null;
    private CookieStore   cookieStore   = null;
    private HttpClient    httpClient    = null;
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
        LOG.debug("entering " + methodName);
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        LOG.debug(methodName + " - cookieManager created");
        cookieStore = cookieManager.getCookieStore();
        LOG.debug(methodName + " - cookieStore created");
        httpClient = getHttpClient();
        LOG.debug(methodName + " - httpClient created (immutable)");
    } // end method

    public HttpClient getHttpClient() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(50))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieManager)
                .build();
            return client;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String sendPaymentServer(Creditcard creditc) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - creditcard = " + creditc);
        HttpResponse<String> response = null;
        try {
            creditc.setCreditCardExpirationDate(
                creditc.getCreditCardExpirationDateLdt().format(ZDF_YEAR_MONTH_DAY));
            LOG.debug(methodName + " - expiration date formatted = " + creditc.getCreditCardExpirationDate());

            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());
            om.configure(SerializationFeature.INDENT_OUTPUT, true);
            String strJson = om.writeValueAsString(creditc);
            LOG.debug(methodName + " - creditcard JSON = " + NEW_LINE + strJson);

            URI uri = UrlBuilder.empty()
                .withScheme("https")
                .withHost("localhost")
                .withPort(5000)
                .withPath("creditcard/")
                .toUri();
            LOG.debug(methodName + " - uri = " + uri);

            // HMAC-SHA256 authentication
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            String payload = timestamp + strJson;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                System.getenv("PAYMENT_HMAC_SECRET").getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String signature = HexFormat.of().formatHex(
                mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

            HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(strJson))
                .uri(URI.create("https://localhost:5000/creditcard"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(5))
                .header("ReturnDirectory", utils.LCUtil.firstPartUrl() + "/rest/paymentController/")
                .header("MerchantSite", "GolfLC Merchant Site")
                .header("X-Timestamp", timestamp)
                .header("X-Signature", signature)
                .build();

            LOG.debug(methodName + " - request = " + request);
            LOG.debug(methodName + " - request headers = " + request.headers());

            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (ConnectException e) {
                String msg = "ConnectException - Payment Server not available - run creditCardService.py";
                LOG.error(msg);
                showMessageFatal(msg);
                return "ConnectException";
            } catch (HttpTimeoutException e) {
                String msg = "Redis Server not available (request timed out) - start it: wsl sudo systemctl start redis";
                LOG.error(msg);
                showMessageFatal(msg);
                return e.getMessage();
            }

            printResponse(response);
            LOG.debug(methodName + " - statusCode = " + response.statusCode());
            LOG.debug(methodName + " - response uri = " + response.uri());

            if (response.statusCode() != 200) {
                LOG.debug(methodName + " - error code = " + response.statusCode());
                ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                Object jsonObject = mapper.readValue(response.body(), Object.class);
                String msg = "Error from payment server: " + mapper.writeValueAsString(jsonObject);
                LOG.error(msg);
                showMessageFatal(msg);
                return Integer.toString(response.statusCode());
            }

            // statusCode == 200
            LOG.info(methodName + " - handling statusCode 200");
            LOG.debug(methodName + " - response sslSession present = " + response.sslSession().isPresent());
            response.headers().map().forEach((k, v) ->
                LOG.debug(methodName + " - response header: " + k + " = " + v));

            String cookieValue = getCookie(response.headers().map().get("set-cookie").get(2));
            LOG.debug(methodName + " - cookie value = " + cookieValue);

            String currency = response.headers().firstValue("Currency").orElse("");
            LOG.debug(methodName + " - currency = " + currency);
            LOG.debug(methodName + " - response version = " + response.version());

            List<HttpCookie> listCookies = cookieStore.getCookies();
            LOG.debug(methodName + " - cookieStore size = " + listCookies.size());
            for (HttpCookie cookie : listCookies) {
                LOG.debug(methodName + " - cookie: " + cookie.getName() + " = " + cookie.getValue());
            }

            List<URI> listUris = cookieStore.getURIs();
            listUris.forEach(item -> LOG.debug(methodName + " - URI: " + item));

            return Integer.toString(response.statusCode());

        } catch (HttpConnectTimeoutException e) {
            String msg = "HttpConnectTimeoutException in " + methodName + " = " + e.getMessage()
                + " , response = " + response;
            LOG.error(msg);
            showMessageFatal(msg);
            return e.getMessage();
        } catch (Exception e) {
            if (e.getMessage() == null) {
                String msg = "Payment Server not available - run creditCardService.py on the python server";
                LOG.error(msg);
                showMessageFatal(msg);
                return "ServerUnavailable";
            }
            if ("request timed out".equals(e.getMessage())) {
                String msg = "Redis Server not available (request timed out) - start it: wsl sudo systemctl start redis";
                LOG.error(msg);
                showMessageFatal(msg);
                return e.getMessage();
            }
            String msg = "Exception in " + methodName + " = " + e.getMessage() + " , response = " + response;
            LOG.error(msg);
            showMessageFatal(msg);
            return e.getMessage();
        }
    } // end method

    public static String getCookieValue(List<HttpCookie> listCookies, String name) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - name = " + name);
        try {
            for (HttpCookie cookie : listCookies) {
                if (name.equals(cookie.getName())) {
                    LOG.debug(methodName + " - found cookie " + name + " = " + cookie.getValue());
                    return cookie.getValue();
                }
            }
            LOG.debug(methodName + " - cookie " + name + " not found");
            return "-1";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return "not found";
        }
    } // end method

    public static String getCookie(String header) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " - header = " + header);
        try {
            int firstIndex = header.indexOf("=");
            if (firstIndex == -1) {
                LOG.debug(methodName + " - '=' not found");
                return "not found";
            }
            int lastIndex = header.indexOf(";");
            if (lastIndex == -1) {
                LOG.debug(methodName + " - ';' not found");
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
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - URI     : " + response.uri());
        LOG.debug(methodName + " - Version : " + response.version());
        LOG.debug(methodName + " - Status  : " + response.statusCode());
        LOG.debug(methodName + " - Headers : " + response.headers());
        LOG.debug(methodName + " - Body    : " + response.body());
    } // end method

    private boolean loadKeyStore() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            LOG.debug(methodName + " - keyStore type = " + keyStore.getType());
            LOG.debug(methodName + " - keyStore provider = " + keyStore.getProvider());

            String relativeCacertsPath = "/lib/security/cacerts".replace("/", File.separator);
            String filename = System.getProperty("java.home") + relativeCacertsPath;
            LOG.debug(methodName + " - cacerts path = " + filename);

            try (FileInputStream fis = new FileInputStream(filename)) {
                keyStore.load(fis, "changeit".toCharArray());
            }

            Date date = keyStore.getCreationDate("GolfLCCreditcardCertificate");
            LOG.debug(methodName + " - certificate creation date = " + date);

            boolean found = (date != null);
            LOG.debug(methodName + " - certificate found = " + found);
            return found;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end main
    */

} // end class
