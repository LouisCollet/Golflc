package rest;

import entite.*;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import payment.PaymentOrchestrator;
import payment.PaymentStateStore;
import payment.PaymentTarget;
import payment.PaymentTransaction;

/**
 * JAX-RS resource for payment callbacks from the Python payment server.
 * Stateless (@RequestScoped) — all transaction data is retrieved from PaymentStateStore by nonce.
 * This class does NOT inject any @SessionScoped beans.
 *
 * Replaces the JAX-RS endpoints previously in PaymentController.
 * Refactored 2026-03-21 — architecture separation REST/JSF.
 */
@jakarta.ws.rs.Path("payment")
@RequestScoped
public class PaymentRestResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PaymentStateStore paymentStateStore;
    @Inject private entite.Settings settings;
    @Inject private manager.PaymentManager paymentManager;
    @Inject private payment.PaymentSubscriptionController paymentSubscriptionController;
    @Inject private payment.PaymentGreenfeeController paymentGreenfeeController;
    @Inject private payment.PaymentCotisationController paymentCotisationController;
    @Inject private payment.PaymentLessonController paymentLessonController;
    @Inject private read.ReadPlayer readPlayer;   // charger le Player complet pour les mails — 2026-04-21

    public PaymentRestResource() { } // end constructor

    // ========================================
    // PAYMENT CHOICE — router from Python server
    // ========================================

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_choice/{isbn}")
    public Response paymentChoice(
            @PathParam("isbn") String param,
            @Context HttpServletRequest servletRequest,
            @Context ServletContext servletContext,
            @Context UriInfo uriInfo,
            @CookieParam("JSESSIONID") String sessionid,
            @CookieParam("Amount") String amount,
            @CookieParam("PaymentReference") String reference,
            @QueryParam("nonce") String nonce,
            @QueryParam("sig") String sig
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from payment server");
        try {
            // Verify callback signature (HMAC + nonce)
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn(methodName + " - SECURITY: unknown nonce=" + nonce);
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }
            if (!verifyCallbackSignature(tx, nonce, sig, param)) {
                LOG.warn(methodName + " - SECURITY: unauthorized callback rejected for phase=" + param);
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid callback signature").build();
            }

            LOG.debug("with param = " + param);
            LOG.debug("with UriInfo = " + uriInfo.getRequestUri().toString());
            LOG.debug("Amount = " + amount);
            LOG.debug("PaymentReference = " + reference);
            LOG.debug("ServletContext getContextPath = " + servletContext.getContextPath());

            String appBaseUrl = settings.getProperty("APP_BASE_URL");
            if (appBaseUrl == null || appBaseUrl.isBlank()) {
                String msg = "FATAL — APP_BASE_URL environment variable is not set — cannot build redirect URL. "
                           + "Set APP_BASE_URL (e.g. http://localhost:8080) and restart WildFly.";
                LOG.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            String href = appBaseUrl + servletContext.getContextPath();
            String location = href + "/rest/payment";
            LOG.debug("location = " + location);

            if (param.equals("phase1")) {
                LOG.debug("handling param phase1 = " + param);
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_canceled/101?nonce=" + nonce))
                        .header("type", "payment_cancel")
                        .build();
            } // end phase 1

            if (param.equals("phase2")) {
                LOG.debug("handling param phase2 = " + param);
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_confirmed?nonce=" + nonce))
                        .build();
            } // end phase 2

            if (param.equals("phase3")) {
                LOG.debug("handling param phase3 = " + param);
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_handle/101?nonce=" + nonce))
                        .build();
            } // end phase 3

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("redirect from payment_choice - unknown param or not yet implemented").build();
        } catch (Exception e) {
            LOG.error("Exception in paymentChoice: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error").build();
        }
    } // end method

    // ========================================
    // PAYMENT HANDLE — executes PaymentOrchestrator
    // ========================================

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_handle/{isbn}")
    @jakarta.ws.rs.Consumes(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
    @jakarta.ws.rs.Produces(jakarta.ws.rs.core.MediaType.TEXT_HTML)
    public Response handlePayments(
            @PathParam("isbn") String uuid,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context UriInfo context,
            @CookieParam("JSESSIONID") String sessionid,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String currency,
            @QueryParam("nonce") String nonce,
            @DefaultValue("2") @QueryParam("step") int step,
            @DefaultValue("true") @QueryParam("min-m") boolean hasMin
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            PaymentTransaction tx = paymentStateStore.consume(nonce);
            if (tx == null) {
                LOG.warn(methodName + " - SECURITY: unknown or already consumed nonce=" + nonce);
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown or consumed transaction").build();
            }

            Creditcard cc = tx.getCreditcard();
            LOG.debug("Payment reference = " + reference);
            LOG.debug("Path parameters = " + context.getPathParameters());
            LOG.debug("Absolute URI = " + context.getAbsolutePath());
            LOG.debug("Amount from cookie = " + amount);

            // Validate cookie amount against server-side amount
            if (amount != null && cc.getTotalPrice() > 0) {
                double cookieAmt = Double.parseDouble(amount);
                if (Math.abs(cookieAmt - cc.getTotalPrice()) > 0.01) {
                    LOG.warn("SECURITY: amount mismatch in handlePayments! Server=" + cc.getTotalPrice()
                            + " Cookie=" + cookieAmt + " for playerId=" + tx.getPlayerId());
                }
            }

            cc.setCreditcardPaymentReference(reference);
            cc.setTypePayment(tx.getSavedType());
            LOG.debug("Creditcard updated: " + cc);

            // Load FULL Player from DB (firstName, lastName, email, language, etc.)
            // sinon les mails de confirmation affichent "null" sur tous les champs — 2026-04-21
            Player stub = new Player();
            stub.setIdplayer(tx.getPlayerId());
            Player player = readPlayer.read(stub);
            if (player == null) {
                LOG.warn("Player not found in DB for idplayer={} — falling back to stub", tx.getPlayerId());
                player = stub;
            }
            boolean updated = paymentManager.needsUpdate(cc, player);
            LOG.debug("Creditcard in DB created or modified? " + updated);

            cc.setPaymentOK(true);

            PaymentTarget target = switch (cc.getTypePayment()) {
                case "SUBSCRIPTION" -> new payment.SubscriptionPayment(tx.getSubscription());
                case "COTISATION" -> new payment.CotisationPayment(tx.getCotisation());
                case "GREENFEE" -> new payment.GreenfeePayment(tx.getGreenfee());
                case "LESSON" -> new payment.LessonPayment(tx.getListLessons());
                default -> throw new IllegalArgumentException(
                        "Unknown payment type: " + cc.getTypePayment()
                );
            };

            LOG.debug("before PaymentOrchestrator");
            PaymentOrchestrator orchestrator = new PaymentOrchestrator(
                    cc, player, tx.getRound(), tx.getClub(), tx.getCourse(), tx.getInscription(),
                    paymentSubscriptionController, paymentGreenfeeController,
                    paymentCotisationController, paymentLessonController
            );
            orchestrator.handle(target);

            String paymentServiceUrl = settings.getProperty("PAYMENT_SERVICE_URL");
            if (paymentServiceUrl == null || paymentServiceUrl.isBlank()) {
                String msg = "FATAL — PAYMENT_SERVICE_URL environment variable is not set — cannot redirect to payment_generator. "
                           + "Set PAYMENT_SERVICE_URL (e.g. https://127.0.0.1:5000) and restart WildFly.";
                LOG.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response
                    .status(Response.Status.FOUND)
                    .location(java.net.URI.create(paymentServiceUrl + "/payment_generator"))
                    .build();

        } catch (Exception e) {
            LOG.error("Exception in handlePayments: " + e.getMessage(), e);
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            // Propage le message d'erreur vers la session JSF via PaymentStateStore — lu dans onPaymentCompleted
            PaymentTransaction errTx = paymentStateStore.get(nonce);
            if (errTx != null) {
                errTx.setErrorMessage("Payment processing error: " + detail);
                errTx.setCanceled(true);
                LOG.debug("errorMessage stored on transaction nonce={}", nonce);
            }

            // Redirige le user vers la page JSF canceled — le message sera affiché via onPaymentCompleted
            String scheme = request.getScheme();
            String host   = request.getServerName();
            int port      = request.getServerPort();
            String ctx    = request.getContextPath();
            String canceledUrl = scheme + "://" + host + ":" + port + ctx
                    + "/creditcard_payment_canceled.xhtml?nonce=" + (nonce != null ? nonce : "");
            LOG.debug("redirecting to JSF canceled page: {}", canceledUrl);
            return Response.status(Response.Status.FOUND)
                    .location(java.net.URI.create(canceledUrl))
                    .build();
        }
    } // end method

    // ========================================
    // PAYMENT CANCELED
    // ========================================

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_canceled/{isbn}")
    public Response paymentCancel(
            @PathParam("isbn") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("type") String type,
            @CookieParam("Amount") String amount,
            @QueryParam("nonce") String nonce
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from python server");
        try {
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn(methodName + " - unknown nonce=" + nonce);
                response.sendRedirect(request.getContextPath() + "/creditcard_payment_canceled.xhtml");
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }

            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(3600);
            cacheControl.setNoCache(true);
            cacheControl.setNoStore(true);
            cacheControl.setMustRevalidate(true);
            cacheControl.setPrivate(true);

            LOG.debug("PathParam isbn converted to id = " + id);
            LOG.debug("JSESSIONID = " + sessionid);
            LOG.debug("Amount = " + amount);
            LOG.debug("@HeaderParam type = " + type);

            tx.getCreditcard().setCreditcardPaymentReference(null);
            tx.getCreditcard().setCommunication("Payment refused by User Client");
            tx.setCanceled(true);

            LOG.debug("going to /creditcard_payment_canceled.xhtml");
            response.sendRedirect(request.getContextPath()
                    + "/creditcard_payment_canceled.xhtml?nonce=" + nonce);
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity("Payment is canceled")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .cacheControl(cacheControl)
                    .build();
        } catch (Exception e) {
            LOG.error("Exception in paymentCancel: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error").build();
        }
    } // end method

    // ========================================
    // PAYMENT CONFIRMED
    // ========================================

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_confirmed")
    public Response paymentConfirmed(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context HttpHeaders headers,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String currency,
            @QueryParam("nonce") String nonce
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from python server");
        try {
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn(methodName + " - unknown nonce=" + nonce);
                response.sendRedirect(request.getContextPath()
                        + "/creditcard_payment_canceled.xhtml?message=Unknown+transaction");
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }

            Creditcard cc = tx.getCreditcard();
            LOG.debug(NEW_LINE);
            LOG.debug("JSESSIONID = " + sessionid);
            LOG.debug("creditcard = " + cc);
            LOG.debug("browser = " + whichBrowser);
            LOG.debug("User = " + user);
            LOG.debug("Amount from cookie = " + amount);

            // Validate amount against server-side value
            double cookieAmount = Double.parseDouble(amount);
            double serverAmount = cc.getTotalPrice();
            LOG.debug("Server-side amount = " + serverAmount + ", cookie amount = " + cookieAmount);
            if (Math.abs(cookieAmount - serverAmount) > 0.01) {
                LOG.warn("SECURITY: payment amount mismatch! Server=" + serverAmount + " Cookie=" + cookieAmount
                        + " for playerId=" + tx.getPlayerId());
                cc.setPaymentOK(false);
                cc.setCommunication("Payment rejected: amount tampered");
                response.sendRedirect(request.getContextPath()
                        + "/creditcard_payment_canceled.xhtml?nonce=" + nonce + "&message=Amount+mismatch");
                return Response.status(Response.Status.FORBIDDEN).entity("Payment amount mismatch").build();
            }

            // Amount validated — set payment reference
            cc.setCreditcardPaymentReference(reference);
            LOG.debug("going to /creditcard_payment_executed.xhtml");
            response.sendRedirect(request.getContextPath()
                    + "/creditcard_payment_executed.xhtml?nonce=" + nonce);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            LOG.error("Exception in paymentConfirmed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error").build();
        }
    } // end method

    // ========================================
    // CALLBACK SIGNATURE VERIFICATION
    // ========================================

    /**
     * Verifies the HMAC signature on a callback from the Python payment server.
     * The signature is computed as HMAC-SHA256(nonce + phase) using the shared secret.
     * Returns true if the signature is valid AND the nonce matches the stored transaction nonce.
     */
    private boolean verifyCallbackSignature(PaymentTransaction tx, String nonce, String sig, String phase) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (nonce == null || sig == null || phase == null) {
            LOG.warn(methodName + " - missing callback signature parameters");
            return false;
        }

        // Verify nonce matches the one generated at payment initiation
        String expectedNonce = tx.getCreditcard().getPaymentNonce();
        if (expectedNonce == null || !expectedNonce.equals(nonce)) {
            LOG.warn(methodName + " - SECURITY: nonce mismatch! expected=" + expectedNonce + " received=" + nonce);
            return false;
        }

        // Verify HMAC signature
        try {
            String secret = settings.getProperty("PAYMENT_HMAC_SECRET");
            if (secret == null) {
                LOG.error(methodName + " - PAYMENT_HMAC_SECRET not set");
                return false;
            }
            String payload = nonce + phase;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            boolean valid = expected.equals(sig);
            if (!valid) {
                LOG.warn(methodName + " - SECURITY: invalid callback signature for phase=" + phase);
            }
            LOG.debug("callback signature validated = " + expected.toString());
            return valid;
        } catch (Exception e) {
            LOG.error(methodName + " - error verifying callback signature: " + e.getMessage());
            return false;
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class
