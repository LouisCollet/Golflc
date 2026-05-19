package rest;

import entite.*;
import static exceptions.LCException.handleGenericException;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.CookieParam;
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
import payment.PaymentStateStore;
import payment.PaymentTransaction;

@jakarta.ws.rs.Path("payment")
@RequestScoped
public class PaymentRestResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PaymentStateStore paymentStateStore;
    @Inject private entite.Settings settings;
    @Inject private manager.PaymentManager paymentManager;
    @Inject private read.ReadPlayer readPlayer;

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
            @CookieParam("Amount") String amount,
            @CookieParam("PaymentReference") String reference,
            @QueryParam("nonce") String nonce,
            @QueryParam("sig") String sig
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // Verify callback signature (HMAC + nonce)
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn("SECURITY: unknown nonce={}", nonce);
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }
            if (!verifyCallbackSignature(tx, nonce, sig, param)) {
                LOG.warn("SECURITY: unauthorized callback rejected phase={}", param);
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid callback signature").build();
            }

            LOG.debug("param={}", param);
            LOG.debug("uriInfo={}", uriInfo.getRequestUri());
            LOG.debug("amount={}", amount);
            LOG.debug("reference={}", reference);
            LOG.debug("contextPath={}", servletContext.getContextPath());

            String appBaseUrl = settings.getProperty("APP_BASE_URL");
            if (appBaseUrl == null || appBaseUrl.isBlank()) {
                String msg = "FATAL — APP_BASE_URL environment variable is not set — cannot build redirect URL. "
                           + "Set APP_BASE_URL (e.g. http://localhost:8080) and restart WildFly.";
                LOG.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            String href = appBaseUrl + servletContext.getContextPath();
            String location = href + "/rest/payment";
            LOG.debug("location={}", location);

            if (param.equals("phase1")) {
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_canceled/101?nonce=" + nonce))
                        .header("type", "payment_cancel")
                        .build();
            } // end phase 1

            if (param.equals("phase2")) {
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_confirmed?nonce=" + nonce))
                        .build();
            } // end phase 2

            if (param.equals("phase3")) {
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_handle/101?nonce=" + nonce))
                        .build();
            } // end phase 3

            return Response.status(Response.Status.NOT_FOUND)
                    .entity("redirect from payment_choice - unknown param or not yet implemented").build();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error").build();
        }
    } // end method

    // ========================================
    // PAYMENT HANDLE — bridge only: record reference, signal JSF, redirect
    // Business logic (DB inserts + cart cleanup) runs in PaymentController.onPaymentCompleted()
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
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String currency,
            @QueryParam("nonce") String nonce
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            // Get transaction (NOT consume — JSF will consume in onPaymentCompleted)
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn("SECURITY: unknown or already consumed nonce={}", nonce);
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown or consumed transaction").build();
            }

            Creditcard cc = tx.getCreditcard();

            // Validate cookie amount against server-side amount
            if (amount != null && cc.getTotalPrice() > 0) {
                double cookieAmt = Double.parseDouble(amount);
                if (Math.abs(cookieAmt - cc.getTotalPrice()) > 0.01) {
                    LOG.warn("SECURITY: amount mismatch! Server={} Cookie={} playerId={}",
                            cc.getTotalPrice(), cookieAmt, tx.getPlayerId());
                }
            }

            // Record payment reference on creditcard
            cc.setCreditcardPaymentReference(reference);
            cc.setTypePayment(tx.getSavedType());
            LOG.debug("creditcard reference set: {}", reference);

            // Load full Player from DB (firstName, lastName, email, language for mails and orchestrator)
            Player stub = new Player();
            stub.setIdplayer(tx.getPlayerId());
            Player player = readPlayer.read(stub);
            if (player == null) {
                LOG.warn("Player not found in DB for idplayer={} — falling back to stub", tx.getPlayerId());
                player = stub;
            }
            tx.setPlayer(player);

            // Record creditcard in DB (create or update)
            boolean updated = paymentManager.needsUpdate(cc, player);
            LOG.debug("Creditcard in DB created or modified? {}", updated);

            cc.setPaymentOK(true);
            LOG.info("payment acknowledged nonce={} reference={} playerId={} type={}",
                    nonce, reference, tx.getPlayerId(), tx.getSavedType());

            // Redirect to Python payment_generator — which redirects user to creditcard_payment_executed.xhtml
            String paymentServiceUrl = settings.getProperty("PAYMENT_SERVICE_URL");
            if (paymentServiceUrl == null || paymentServiceUrl.isBlank()) {
                String msg = "FATAL — PAYMENT_SERVICE_URL not set — cannot redirect to payment_generator.";
                LOG.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response
                    .status(Response.Status.FOUND)
                    .location(java.net.URI.create(paymentServiceUrl + "/payment_generator"))
                    .build();

        } catch (Exception e) {
            handleGenericException(e, methodName);
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            PaymentTransaction errTx = paymentStateStore.get(nonce);
            if (errTx != null) {
                errTx.setErrorMessage("Payment processing error: " + detail);
                errTx.setCanceled(true);
                LOG.debug("errorMessage stored on transaction nonce={}", nonce);
            }

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
            @HeaderParam("type") String type,
            @CookieParam("Amount") String amount,
            @QueryParam("nonce") String nonce
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn("unknown nonce={}", nonce);
                response.sendRedirect(request.getContextPath() + "/creditcard_payment_canceled.xhtml");
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }

            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(3600);
            cacheControl.setNoCache(true);
            cacheControl.setNoStore(true);
            cacheControl.setMustRevalidate(true);
            cacheControl.setPrivate(true);

            LOG.debug("id={}", id);
            LOG.debug("amount={}", amount);
            LOG.debug("type={}", type);

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
            handleGenericException(e, methodName);
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
            @HeaderParam("User-Agent") String whichBrowser,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String currency,
            @QueryParam("nonce") String nonce
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            PaymentTransaction tx = paymentStateStore.get(nonce);
            if (tx == null) {
                LOG.warn("unknown nonce={}", nonce);
                response.sendRedirect(request.getContextPath()
                        + "/creditcard_payment_canceled.xhtml?message=Unknown+transaction");
                return Response.status(Response.Status.FORBIDDEN).entity("Unknown transaction").build();
            }

            Creditcard cc = tx.getCreditcard();
            LOG.debug("browser={}", whichBrowser);
            LOG.debug("user={}", user);
            LOG.debug("amount from cookie={}", amount);

            // Validate amount against server-side value
            if (amount == null || amount.isBlank()) {
                LOG.warn("Amount cookie missing nonce={}", nonce);
                response.sendRedirect(request.getContextPath()
                        + "/creditcard_payment_canceled.xhtml?nonce=" + nonce + "&message=Amount+missing");
                return Response.status(Response.Status.FORBIDDEN).entity("Amount cookie missing").build();
            }
            double cookieAmount = Double.parseDouble(amount);
            double serverAmount = cc.getTotalPrice();
            LOG.debug("server amount={} cookie amount={}", serverAmount, cookieAmount);
            if (Math.abs(cookieAmount - serverAmount) > 0.01) {
                LOG.warn("SECURITY: payment amount mismatch! Server={} Cookie={} playerId={}",
                        serverAmount, cookieAmount, tx.getPlayerId());
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
            handleGenericException(e, methodName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal error").build();
        }
    } // end method

    // ========================================
    // CALLBACK SIGNATURE VERIFICATION
    // ========================================

    private boolean verifyCallbackSignature(PaymentTransaction tx, String nonce, String sig, String phase) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (nonce == null || sig == null || phase == null) {
            LOG.warn("missing callback signature parameters");
            return false;
        }

        // Verify nonce matches the one generated at payment initiation
        String expectedNonce = tx.getCreditcard().getPaymentNonce();
        if (expectedNonce == null || !expectedNonce.equals(nonce)) {
            LOG.warn("SECURITY: nonce mismatch expected={} received={}", expectedNonce, nonce);
            return false;
        }

        // Verify HMAC signature
        try {
            String secret = settings.getProperty("PAYMENT_HMAC_SECRET");
            if (secret == null) {
                LOG.error("PAYMENT_HMAC_SECRET not set");
                return false;
            }
            String payload = nonce + phase;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            boolean valid = expected.equals(sig);
            if (!valid) {
                LOG.warn("SECURITY: invalid callback signature phase={}", phase);
            }
            LOG.debug("callback signature validated={}", valid);
            return valid;
        } catch (Exception e) {
            LOG.error("error verifying callback signature", e);
            return false;
        }
    } // end method

} // end class
