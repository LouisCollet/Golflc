package Controllers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

} // end class
