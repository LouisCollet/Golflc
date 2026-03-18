package Controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpControllerTest {

    private HttpController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new HttpController();

        // Injecter cookieManager via réflexion (normalement fait par @PostConstruct init())
        CookieManager cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        Field cmField = HttpController.class.getDeclaredField("cookieManager");
        cmField.setAccessible(true);
        cmField.set(controller, cm);

        Field csField = HttpController.class.getDeclaredField("cookieStore");
        csField.setAccessible(true);
        csField.set(controller, cm.getCookieStore());
    } // end setUp

    // ===== getCookie(String header) =====

    @Nested
    class GetCookieTests {

        @Test
        void normalHeader_returnsValue() {
            assertEquals("135.0", HttpController.getCookie("Amount=135.0; Path=/"));
        } // end test

        @Test
        void multipleEquals_returnsFirstSegment() {
            // "Key=val=ue; Path=/" → doit retourner "val=ue" (entre premier = et premier ;)
            assertEquals("val=ue", HttpController.getCookie("Key=val=ue; Path=/"));
        } // end test

        @Test
        void noEquals_returnsNotFound() {
            assertEquals("not found", HttpController.getCookie("NoEqualsHere;something"));
        } // end test

        @Test
        void noSemicolon_returnsNotFound() {
            assertEquals("not found", HttpController.getCookie("Amount=135.0"));
        } // end test

        @Test
        void emptyValue_returnsEmpty() {
            assertEquals("", HttpController.getCookie("Amount=; Path=/"));
        } // end test

        @Test
        void semicolonBeforeEquals_throwsAppException() {
            // "No;val=ue" → indexOf("=")=6, indexOf(";")=2 → substring(7, 2) → StringIndexOutOfBoundsException
            // handleGenericException wraps it in AppException
            assertThrows(exceptions.AppException.class, () -> HttpController.getCookie("No;val=ue"));
        } // end test

    } // end nested class

    // ===== getCookieValue(List<HttpCookie>, String name) =====

    @Nested
    class GetCookieValueTests {

        @Test
        void found_returnsValue() {
            HttpCookie cookie = new HttpCookie("PaymentReference", "REF-123");
            List<HttpCookie> cookies = List.of(cookie);
            assertEquals("REF-123", HttpController.getCookieValue(cookies, "PaymentReference"));
        } // end test

        @Test
        void notFound_returnsMinus1() {
            HttpCookie cookie = new HttpCookie("Amount", "50.0");
            List<HttpCookie> cookies = List.of(cookie);
            assertEquals("-1", HttpController.getCookieValue(cookies, "NonExistent"));
        } // end test

        @Test
        void multipleCookies_returnsCorrectOne() {
            HttpCookie c1 = new HttpCookie("Amount", "50.0");
            HttpCookie c2 = new HttpCookie("PaymentReference", "REF-456");
            HttpCookie c3 = new HttpCookie("Currency", "EUR");
            List<HttpCookie> cookies = List.of(c1, c2, c3);
            assertEquals("REF-456", HttpController.getCookieValue(cookies, "PaymentReference"));
            assertEquals("EUR", HttpController.getCookieValue(cookies, "Currency"));
            assertEquals("50.0", HttpController.getCookieValue(cookies, "Amount"));
        } // end test

        @Test
        void emptyList_returnsMinus1() {
            assertEquals("-1", HttpController.getCookieValue(Collections.emptyList(), "Anything"));
        } // end test

        @Test
        void duplicateName_returnsFirst() {
            HttpCookie c1 = new HttpCookie("Key", "first");
            HttpCookie c2 = new HttpCookie("Key", "second");
            List<HttpCookie> cookies = List.of(c1, c2);
            assertEquals("first", HttpController.getCookieValue(cookies, "Key"));
        } // end test

    } // end nested class

    // ===== getHttpClient() =====

    @Nested
    class GetHttpClientTests {

        @Test
        void returnsNonNull() {
            HttpClient client = controller.getHttpClient();
            assertNotNull(client);
        } // end test

        @Test
        void returnsHttp11() {
            HttpClient client = controller.getHttpClient();
            assertEquals(HttpClient.Version.HTTP_1_1, client.version());
        } // end test

        @Test
        void hasCorrectTimeout() {
            HttpClient client = controller.getHttpClient();
            assertTrue(client.connectTimeout().isPresent());
            assertEquals(50, client.connectTimeout().get().getSeconds());
        } // end test

        @Test
        void followsRedirects() {
            HttpClient client = controller.getHttpClient();
            assertEquals(HttpClient.Redirect.NORMAL, client.followRedirects());
        } // end test

        @Test
        void hasCookieHandler() {
            HttpClient client = controller.getHttpClient();
            assertTrue(client.cookieHandler().isPresent());
        } // end test

    } // end nested class

    // ===== init() =====

    @Nested
    class InitTests {

        @Test
        void setsAllFields() throws Exception {
            HttpController fresh = new HttpController();
            fresh.init();

            Field cmField = HttpController.class.getDeclaredField("cookieManager");
            cmField.setAccessible(true);
            assertNotNull(cmField.get(fresh));

            Field csField = HttpController.class.getDeclaredField("cookieStore");
            csField.setAccessible(true);
            assertNotNull(csField.get(fresh));

            Field hcField = HttpController.class.getDeclaredField("httpClient");
            hcField.setAccessible(true);
            assertNotNull(hcField.get(fresh));
        } // end test

    } // end nested class

    // ===== creditcard getter/setter =====

    @Nested
    class CreditcardAccessorTests {

        @Test
        void getterSetterRoundTrip() {
            assertNull(controller.getCreditcard());
            entite.Creditcard cc = new entite.Creditcard();
            controller.setCreditcard(cc);
            assertSame(cc, controller.getCreditcard());
        } // end test

    } // end nested class

    // ===== loadKeyStore() — private, via réflexion =====

    @Nested
    class LoadKeyStoreTests {

        @Test
        void loadsDefaultCacerts() throws Exception {
            Method method = HttpController.class.getDeclaredMethod("loadKeyStore");
            method.setAccessible(true);
            // Le cacerts du JDK existe toujours — la méthode ne doit pas throw
            // Le certificat "GolfLCCreditcardCertificate" peut ou non être présent
            Object result = method.invoke(controller);
            assertNotNull(result);
            assertInstanceOf(Boolean.class, result);
        } // end test

    } // end nested class

    // ===== sendPaymentServer(Creditcard) =====

    @Nested
    class SendPaymentServerTests {

        private HttpClient mockHttpClient;

        @BeforeEach
        void setUpSendPayment() throws Exception {
            // Injecter un mock HttpClient via réflexion
            mockHttpClient = mock(HttpClient.class);
            Field hcField = HttpController.class.getDeclaredField("httpClient");
            hcField.setAccessible(true);
            hcField.set(controller, mockHttpClient);
            // Pas de mockStatic — showMessageFatal() et firstPartUrl()
            // gèrent l'absence de FacesContext en interne (try-catch)
        } // end setUp

        private entite.Creditcard createValidCreditcard() {
            entite.Creditcard cc = new entite.Creditcard();
            cc.setCreditCardExpirationDateLdt(LocalDateTime.of(2027, 6, 1, 0, 0));
            cc.setCreditcardHolder("TEST HOLDER");
            cc.setCreditcardType("VISA");
            cc.setCreditcardVerificationCode((short) 123);
            cc.setTotalPrice(100.0);
            cc.setCommunication("test-comm");
            cc.setCreditCardIdPlayer(1);
            return cc;
        } // end method

        private void assumeHmacSecretSet() {
            assumeTrue(System.getenv("PAYMENT_HMAC_SECRET") != null,
                "Skipped: PAYMENT_HMAC_SECRET environment variable required");
        } // end method

        // --- Tests ne nécessitant PAS PAYMENT_HMAC_SECRET ---

        @Test
        void nullExpirationDate_returnsExceptionMessage() throws Exception {
            entite.Creditcard cc = createValidCreditcard();
            cc.setCreditCardExpirationDateLdt(null);

            String result = controller.sendPaymentServer(cc);
            // NPE avec message enrichi Java 25 → outer catch → retourne e.getMessage()
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } // end test

        // --- Tests nécessitant PAYMENT_HMAC_SECRET ---

        @Test
        void connectException_returnsConnectException() throws Exception {
            assumeHmacSecretSet();
            when(mockHttpClient.send(any(), any()))
                .thenThrow(new ConnectException("Connection refused"));

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("ConnectException", result);
        } // end test

        @Test
        void httpTimeoutException_returnsMessage() throws Exception {
            assumeHmacSecretSet();
            when(mockHttpClient.send(any(), any()))
                .thenThrow(new HttpTimeoutException("request timed out"));

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("request timed out", result);
        } // end test

        @SuppressWarnings("unchecked")
        @Test
        void nonOkStatus_returnsStatusCode() throws Exception {
            assumeHmacSecretSet();
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(400);
            when(mockResponse.body()).thenReturn("{\"error\":\"bad request\"}");
            when(mockResponse.uri()).thenReturn(URI.create("https://localhost:5000/creditcard"));
            when(mockResponse.version()).thenReturn(HttpClient.Version.HTTP_1_1);
            when(mockResponse.headers()).thenReturn(
                HttpHeaders.of(Map.of(), (k, v) -> true));
            doReturn(mockResponse).when(mockHttpClient).send(any(), any());

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("400", result);
        } // end test

        @SuppressWarnings("unchecked")
        @Test
        void okStatus_returns200() throws Exception {
            assumeHmacSecretSet();
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            when(mockResponse.statusCode()).thenReturn(200);
            when(mockResponse.body()).thenReturn("{\"status\":\"ok\"}");
            when(mockResponse.uri()).thenReturn(URI.create("https://localhost:5000/creditcard"));
            when(mockResponse.version()).thenReturn(HttpClient.Version.HTTP_1_1);
            when(mockResponse.sslSession()).thenReturn(Optional.empty());
            HttpHeaders headers = HttpHeaders.of(
                Map.of(
                    "set-cookie", List.of("c0=v0; Path=/", "c1=v1; Path=/", "Amount=135.0; Path=/"),
                    "Currency", List.of("EUR")
                ),
                (k, v) -> true);
            when(mockResponse.headers()).thenReturn(headers);
            doReturn(mockResponse).when(mockHttpClient).send(any(), any());

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("200", result);
        } // end test

        @Test
        void exceptionNullMessage_returnsServerUnavailable() throws Exception {
            assumeHmacSecretSet();
            // RuntimeException non captée par le inner catch → outer catch (Exception e)
            when(mockHttpClient.send(any(), any()))
                .thenThrow(new RuntimeException((String) null));

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("ServerUnavailable", result);
        } // end test

        @Test
        void exceptionRequestTimedOut_returnsTimedOutMessage() throws Exception {
            assumeHmacSecretSet();
            when(mockHttpClient.send(any(), any()))
                .thenThrow(new RuntimeException("request timed out"));

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("request timed out", result);
        } // end test

        @Test
        void generalException_returnsExceptionMessage() throws Exception {
            assumeHmacSecretSet();
            when(mockHttpClient.send(any(), any()))
                .thenThrow(new RuntimeException("unexpected error"));

            String result = controller.sendPaymentServer(createValidCreditcard());
            assertEquals("unexpected error", result);
        } // end test

    } // end nested class

} // end class
