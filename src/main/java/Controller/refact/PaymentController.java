package Controller.refact;

import context.ApplicationContext;
import entite.*;
import entite.Creditcard.etypePayment;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.annotation.SessionMap;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import manager.PaymentManager;
import payment.PaymentOrchestrator;
import payment.PaymentTarget;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Controller JSF pour la gestion des paiements et cartes de credit.
 * Migre depuis CourseController le 2026-02-25.
 *
 * @author GolfLC
 */
@Named("payC")
@SessionScoped
@jakarta.ws.rs.Path("paymentController")
public class PaymentController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI
    // ========================================

    @Inject private ApplicationContext                          appContext;
    @Inject private PaymentManager                              paymentManager;
    @Inject private read.ReadCreditcard                          readCreditcard;       // migrated 2026-02-26 — was CreditcardController
    @Inject private create.CreateCreditcard                       createCreditcard;     // migrated 2026-02-26
    @Inject private update.ModifyCreditcard                       modifyCreditcard;     // migrated 2026-02-26
    @Inject private payment.PaymentSubscriptionController       paymentSubscriptionController;
    @Inject private payment.PaymentGreenfeeController           paymentGreenfeeController;
    @Inject private Controllers.TarifMemberController           tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController         tarifGreenfeeController;
    @Inject private Controllers.SchedulerProController          schedulerProController;
    @Inject private create.CreateLesson                         createLesson;
    @Inject private manager.PlayerManager                       playerManager;
    @Inject private read.ReadClub                               readClubService;
    @Inject private Controllers.HttpController                  httpController;
    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext
    @Inject private mail.CreditcardMail                         creditcardMail;  // migrated 2026-02-26

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private Creditcard    creditcard;
    private String        savedType;
    private Integer       progress1 = 0;
    private Greenfee      greenfee;
    private TarifGreenfee tarifGreenfee;
    private TarifMember   tarifMember;
    private Professional  professional;
    private List<Lesson>  listLessons = new ArrayList<>();
    private Lesson        selectedLesson;
    private Player        playerPro;

    public PaymentController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        creditcard = new Creditcard();
        greenfee = new Greenfee();
        tarifGreenfee = new TarifGreenfee();
        tarifMember = new TarifMember();
        LOG.debug("PaymentController initialized");
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " — source: " + event.getSource());
        creditcard     = new Creditcard();
        savedType      = null;
        progress1      = 0;
        greenfee       = new Greenfee();
        tarifGreenfee  = new TarifGreenfee();
        tarifMember    = new TarifMember();
        professional   = null;
        listLessons    = new ArrayList<>();
        selectedLesson = null;
        playerPro      = null;
        LOG.debug(methodName + " — PaymentController reset done");
    } // end method

    // ========================================
    // METHODES D'ACTION — migrées depuis CourseController 2026-02-25
    // ========================================

    public String manageCotisation() throws Exception { // called from cotisation.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("tarifMember = " + tarifMember);
            LOG.debug("cotisation = " + appContext.getCotisation()); // est null
            LOG.debug("round = " + appContext.getRound());
            appContext.setCreditcardType(etypePayment.COTISATION.toString());
            LOG.debug("creditcardType = " + appContext.getCreditcardType());
            Cotisation cotisation = tarifMemberController.completeCotisation(tarifMember, appContext.getPlayer(), appContext.getRound());
            if (cotisation == null) {
                String msg = "cotisation not found !! is null";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } // end if
            cotisation.setIdplayer(appContext.getPlayer().getIdplayer());
            cotisation.setIdclub(appContext.getClub().getIdclub());
            cotisation.setCommunication(appContext.getClub().getClubName() + " : " + cotisation.getCommunication());
            LOG.debug("Cotisation loaded = " + cotisation);

            if (cotisation.getPrice() == 0.0) {
                String msg = "amount ZERO no payment needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if

            if (cotisation.isCotisationError()) {
                String msg = "cotisation error !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } // end if

            if (appContext.getInputSelectClub().equals("PaymentCotisationSpontaneous")) {
                cotisation.setType("spontaneous");
                LOG.debug("Paiement spontané - NO inscription");
            } else {
                cotisation.setType("round"); // inscription à un round
            } // end if
            LOG.debug("cotisation type : spontaneous ou round ? " + cotisation.getType());

            LOG.debug("amount non ZERO payment COTISATION needed !");
            appContext.setCotisation(cotisation);
            creditcard = completeWithCotisation(cotisation, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            if (creditcard != null) {
                String msg = "creditcard completed with Cotisation ! ";
                LOG.info(msg);
                return "creditcard.xhtml?faces-redirect=true";
            } else {
                String msg = "paiement par creditcard KO : quelle conclusion ?";
                LOG.error(msg);
                showMessageFatal(msg);
                throw new Exception(msg);
            } // end if
        } catch (SQLException ex) {
            handleSQLException(ex, methodName);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageCotisation

    public String manageLesson() throws Exception { // called from schedule_pro.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            appContext.setCreditcardType(etypePayment.LESSON.toString());
            LOG.debug("creditcardType = " + appContext.getCreditcardType());

            professional = schedulerProController.getProfessional();
            LOG.debug("professional coming from SchedulerProController = " + professional);

            Club club = appContext.getClub();
            club.setIdclub(professional.getProClubId());
            club = readClubService.read(club);
            appContext.setClub(club);

            Player p = new Player();
            p.setIdplayer(professional.getProPlayerId());
            playerPro = playerManager.readPlayer(p.getIdplayer());

            listLessons = schedulerProController.getListLessons();

            for (Lesson lesson2 : listLessons) {
                lesson2.setLessonAmount(professional.getProAmount());
            } // end for

            if (!playerManager.findProfessionals(appContext.getPlayer()).isEmpty()) {
                for (Lesson lesson2 : listLessons) {
                    lesson2.setLessonAmount(0.0); // new 30-01-2023 16:27
                    if (createLesson.create(lesson2, appContext.getPlayer())) {
                        String msg = "Lesson pro created = " + lesson2;
                        LOG.info(msg);
                        showMessageInfo(msg);
                    } else {
                        String msg = "error : lesson pro not registered !!";
                        LOG.error(msg);
                        showMessageFatal(msg);
                        break;
                    } // end if
                } // end for
                return "welcome.xhtml?faces-redirect=true";
            } // end if Professional

            listLessons.forEach(item -> LOG.debug("listLessons Start Date : " + item.getEventStartDate()));
            if (professional.getProAmount().equals(0.0)) {
                String msg = "amount ZERO no payment Lesson needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if
            creditcard = completeWithLesson(professional, listLessons, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            if (creditcard != null) {
                String msg = "Creditcard with lesson ! " + creditcard;
                LOG.info(msg);
                return "price_pro.xhtml?faces-redirect=true";
            } else {
                String msg = "paiement par creditcard KO : quelle conclusion ?";
                LOG.error(msg);
                showMessageFatal(msg);
                throw new Exception(msg);
            } // end if
        } catch (SQLException ex) {
            handleSQLException(ex, methodName);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageLesson

    public String manageGreenfee() { // called from price_round_greenfee.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with creditcard = " + creditcard);
            LOG.debug("with greenfee = " + greenfee);

            appContext.setCreditcardType(etypePayment.GREENFEE.toString());
            LOG.debug("creditcardType = " + appContext.getCreditcardType());

            // 1. complete greenfee with price
            greenfee = tarifGreenfeeController.completeGreenfee(tarifGreenfee, appContext.getClub(), appContext.getRound(), appContext.getPlayer());
            LOG.debug("Greenfee completed with tarif data = " + greenfee);
            if (greenfee.getPrice() == 0) {
                String msg = "amount ZERO,  no payment needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if
            // 2. complete creditcard
            creditcard = completeWithGreenfee(greenfee, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            LOG.debug("Creditcard Greenfee completed = " + creditcard);
            return("creditcard.xhtml?faces-redirect=true");
        } catch (Exception ex) {
            String msg = "Exception in manageGreenfee " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    public String manageSubscription() throws Exception { // called from subscription.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Subscription subscription = appContext.getSubscription();
            subscription.setIdplayer(appContext.getPlayer().getIdplayer());
            LOG.debug("coming from subscription.xhtml with Subscription = " + subscription);

            creditcard.setTypePayment(etypePayment.SUBSCRIPTION.toString());
            LOG.debug(" with Creditcard = " + creditcard);
            appContext.setCreditcardType(etypePayment.SUBSCRIPTION.toString());
            LOG.debug("creditcardType = " + appContext.getCreditcardType());
            switch (subscription.getSubCode()) {
                case "TRIAL" -> {    // trial one day
                    LOG.debug("SubCode = TRIAL");
                    boolean b = paymentSubscriptionController.createPayment(subscription); // migrated 2026-02-25
                    return "welcome.xhtml?faces-redirect=true";
                }
                case "MONTHLY", "YEARLY" -> {
                    LOG.debug("getSubCode()is MONTHLY or YEARLY");
                    subscription = paymentSubscriptionController.complete(subscription); // migrated 2026-02-25
                    appContext.setSubscription(subscription);
                    creditcard = completeWithSubscription(subscription, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
                    LOG.debug("creditcard completed with subscription = " + creditcard);
                    return "creditcard.xhtml?faces-redirect=true";
                }
                default -> {
                    LOG.debug(": getSubCode() UNKNOWN = " + subscription.getSubCode());
                    return null;
                }
            } // end switch
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageSubscription

    public String creditCardMail() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with creditcard typePayment = " + creditcard.getTypePayment());
        if (Creditcard.etypePayment.GREENFEE.toString().equals(creditcard.getTypePayment())) {
            // new mail.CreditcardMail().sendMailGreenfee(...)
            if (creditcardMail.sendMailGreenfee(appContext.getPlayer(), creditcard, tarifGreenfee, appContext.getRound(), appContext.getInscription())) { // migrated 2026-02-26
                LOG.debug("");
            } // end if
        } // end if
        if (Creditcard.etypePayment.SUBSCRIPTION.toString().equals(creditcard.getTypePayment())) {
            // new mail.CreditcardMail().sendMailSubscription(...)
            boolean ok = creditcardMail.sendMailSubscription(appContext.getPlayer(), creditcard, appContext.getSubscription()); // migrated 2026-02-26
        } // end if
        if (Creditcard.etypePayment.COTISATION.toString().equals(creditcard.getTypePayment())) {
            // new mail.CreditcardMail().sendMailCotisation(...)
            boolean ok = creditcardMail.sendMailCotisation(appContext.getPlayer(), creditcard, appContext.getCotisation(), appContext.getClub(), tarifMember); // migrated 2026-02-26
        } // end if
        return "creditcard_payment_executed.xhtml?faces-redirect=true";
    } // end method

    //  !! ne pas toucher ..String typePayment.
    public void onCompletePayment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " coming from creditcard_accepted.xhtml");
        try {
            creditcard.setTypePayment(appContext.getCreditcardType());
            savedType = creditcard.getTypePayment();
            LOG.debug("before payment creditcard = " + creditcard);
            String v = httpController.sendPaymentServer(creditcard); // migrated 2026-02-26 — was creditcardController.getCC2()
            LOG.debug("var v returned in OnCompletePayment = " + v);
            LOG.debug("creditcard returned in OnCompletePayment = " + creditcard);
            if (v.equals("200")) {
                String msg = "Payment validé par Amazone Payments Inc !";
                LOG.info(msg);
                LOG.debug("creditcardType in onCompletePayment = " + appContext.getCreditcardType());
                LOG.debug("before going with context to 5000/about");
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().redirect("https://localhost:5000/about");
                context.responseComplete();
                LOG.debug("after redirect with context to 5000/about");
            } else {
                String msg = "payment rejected by Amazone Payments Inc ! !";
                showMessageFatal(msg);
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().redirect("creditcard_payment_canceled.xhtml?faces-redirect=true");
                context.responseComplete();
            } // end if
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method onCompletePayment

    // new code 21-01-2026 saved in text editpadlite
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_handle/{isbn}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_HTML)
public jakarta.ws.rs.core.Response handlePayments(
            @PathParam("isbn") String uuid,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context UriInfo context,
            @Context jakarta.ws.rs.core.HttpHeaders hh,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String currency,
            @DefaultValue("2") @QueryParam("step") int step,
            @DefaultValue("true") @QueryParam("min-m") boolean hasMin
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        try {
            LOG.debug("Payment reference = " + reference);
            LOG.debug("Path parameters = " + context.getPathParameters());
            LOG.debug("Absolute URI = " + context.getAbsolutePath());
            LOG.debug("Amount = " + amount);

            creditcard.setCreditcardPaymentReference(reference);
            creditcard.setTypePayment(getSavedType());
            LOG.debug("Creditcard updated: " + creditcard);

            boolean needsUpdate = needsUpdate(creditcard, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            LOG.debug("Creditcard in DB created or modified? " + needsUpdate);

            creditcard.setPaymentOK(true);

            PaymentTarget target = switch (creditcard.getTypePayment()) {
                case "SUBSCRIPTION" -> new payment.SubscriptionPayment(appContext.getSubscription());
                case "COTISATION" -> new payment.CotisationPayment(appContext.getCotisation());
                case "GREENFEE" -> new payment.GreenfeePayment(greenfee);
                default -> throw new IllegalArgumentException(
                        "Unknown payment type: " + creditcard.getTypePayment()
                );
            };

            LOG.debug("before PaymentOrchestrator");
            PaymentOrchestrator orchestrator = new PaymentOrchestrator(
                    creditcard, appContext.getPlayer(), appContext.getRound(), appContext.getClub(), appContext.getCourse(), appContext.getInscription(),
                    paymentSubscriptionController, paymentGreenfeeController // migrated 2026-02-25
            );

            orchestrator.handle(target);

            return jakarta.ws.rs.core.Response
                    .status(Response.Status.FOUND)
                    .location(java.net.URI.create("https://localhost:5000/payment_generator"))
                    .build();

        } catch (Exception e) {
            String msg = "Exception in handlePayments: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method handlePayments

    //  utilisé dans creditcard_accepted.xhtml
    public void onStart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", progress1 = " + progress1);
        showMessageInfo("entering onStart, progress1 = " + progress1);
    } // end method

    public void onProgress() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        String msg = "Progress Updated " + progress1;
        showMessageInfo(msg);
    } // end method

    public void cancelProgress() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("Payment canceled by User");
        progress1 = null;
        creditcard.setPaymentOK(false);
        String msg = "Creditcard payment canceled by user";
        LOG.error(msg);
        showMessageFatal(msg);
    } // end method

    public void deleteLesson() { // used in price_pro.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.listLessons.remove(this.selectedLesson);
        String msg = "Lesson removed = = " + this.selectedLesson;
        this.selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
        creditcard.setTotalPrice(listLessons.size() * professional.getProAmount());
        msg = "recalculated totalPrice is now " + creditcard.getTotalPrice();
        LOG.info(msg);
        showMessageInfo(msg);
        org.primefaces.PrimeFaces.current().ajax().update("form_price_pro:growl-msg", "form_price_pro:listLessons", "form_price_pro:messages");
    } // end method

    public String to_creditcard_test_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        creditcard.setTotalPrice(155.6);
        creditcard.setCommunication(" prepared creditcard communication");
        creditcard.setCreditcardType("GREENFEE");
        return "creditcard_test.xhtml?faces-redirect=true";
    } // end method

    public void creditCardNumberListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("creditcardNumber OldValue = " + e.getOldValue());
        LOG.debug("creditcardNumber NewValue = " + e.getNewValue());
        creditcard.setCreditcardNumber(e.getNewValue().toString());
    } // end method

    public void creditCardTypeListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("creditcardType OldValue = " + e.getOldValue());
        LOG.debug("creditcardType NewValue = " + e.getNewValue());
        creditcard.setCreditcardType(e.getNewValue().toString());
        if (!creditcard.getCreditcardIssuer().equals(creditcard.getCreditcardType())) {
            String msg = "WARNING !!! "
                    + " <br/> Issuer detected = " + creditcard.getCreditcardIssuer()
                    + " <br/> Card Type data in = " + creditcard.getCreditcardType();
            LOG.debug(msg);
            showMessageInfo(msg);
        } // end if
    } // end method

    // ========================================
    // TEST METHODS — migrées depuis CourseController 2026-02-25
    // ========================================

    public String testWebServiceHttp() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for creditcard, server = python");
        try {
            creditcard.setCreditCardHolder("LOUIS COLLET 11");
            creditcard.setCreditCardIdPlayer(324713);
            creditcard.setCommunication("creditcard using Java11HttpClientExample");
            creditcard.setCreditcardNumber("1111222233334444");
            creditcard.setTotalPrice(35.0);
            creditcard.setTypePayment("LESSON");
            creditcard.setCreditcardType("VISA");
            creditcard.setCreditcardVerificationCode((short) 567);
            creditcard.setPaymentOK(false);
            creditcard.setCreditcardCurrency("EUR");
            LOG.debug("just before send payment to python server " + creditcard);
            String s = httpController.sendPaymentServer(creditcard);
            if (creditcard.getCreditCardIdPlayer() != null) { // fake test !!
                String msg = "Payment validé par Amazone Payments Inc !";
                LOG.info(msg);
                showMessageInfo(msg);
                return "creditcard_accepted.xhtml?faces-redirect=true";
            } else {
                String msg = "payment rejected by Amazone Payments Inc ! !";
                LOG.error(msg);
                showMessageFatal(msg);
                return "welcome.xhtml?faces-redirect=true";
            } // end if
        } catch (Exception ex) {
            String msg = "Exception in testWebServiceHttp" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    public void testWebService() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        String ws = null;
        Response response = null;
        try {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JavaTimeModule());

            Creditcard c = new Creditcard();
            c.setCreditCardHolder("LOUIS COLLET");
            c.setCreditCardIdPlayer(324713);
            c.setCommunication("creditcard communication");
            c.setCreditcardNumber("1111222233334444");
            c.setTotalPrice(35.0);
            c.setTypePayment("LESSON");
            c.setCreditcardType("VISA");
            c.setCreditcardVerificationCode((short) 567);
            String strJson = om.writeValueAsString(c);
            LOG.debug("creditcard data converted in json format = " + "\n" + strJson);

            jakarta.ws.rs.client.Client client = ClientBuilder.newClient();
            ws = "http://localhost:8083/creditcard/" + URLEncoder.encode(strJson, "utf-8");
            LOG.debug("going to Webservice creditcard escaped \n" + ws);
            WebTarget webTarget = client.target(ws);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            response = invocationBuilder.get();
            response.bufferEntity();
            String s = response.readEntity(String.class);
            LOG.debug("readEntity s = " + s);
            final Cookie sessionId = response.getCookies().get("JSESSIONID");
            LOG.debug("sessionId = " + sessionId);

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String msg = response.readEntity(String.class);
                LOG.debug(msg);
                showMessageInfo(msg);
            } else {
                String msg = "response - it is !NOT OK!  = " + response.getStatus() + "<br/>\n" + ws;
                LOG.error(msg);
                showMessageFatal(msg);
            } // end if
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            LOG.debug("we have viewhandler = " + viewHandler);
            UIViewRoot viewRoot = viewHandler.createView(facesContext, facesContext.getViewRoot().getViewId());
            LOG.debug("we have viewRoot = " + viewRoot);
            LOG.debug("we have viewId = " + viewRoot.getViewId());
            String actionUrl = viewHandler.getActionURL(facesContext, viewRoot.getViewId());
            LOG.debug("we have actionUrl = " + actionUrl);

            creditcard = c; //test only
            facesContext.getExternalContext().redirect("creditcard_payment_executed.xhtml?faces-redirect=true");

        } catch (Exception e) {
            String msg = "£££ Exception in testWebService = " + e.getMessage() + ws;
            LOG.error(msg);
            showMessageFatal(msg);
        } finally {
            response.close();
            LOG.debug("response closed");
        }
    } // end method

    // ========================================
    // PROGRESS BAR — migrée depuis CourseController 2026-02-25
    // ========================================

    private Integer updateProgress(Integer progress) {
        if (progress == null) {
            progress = 0;
        } else {
            progress = progress + (int) (Math.random() * 35);
            if (progress > 100)
                progress = 100;
        } // end if
        return progress;
    } // end method

    // ========================================
    // METHODES METIER PRIVEES — migrées depuis CreditcardController 2026-02-26
    // ========================================

    private Creditcard prefilling(Player player) throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Creditcard c = readCreditcard.read(player);
        LOG.debug("creditcard loaded = " + c);
        if (c.getCreditcardNumber() == null) {
            LOG.debug("First utilisation of a creditcard for user = " + player.getPlayerLastName());
            c.setCreditCardHolder("first use");
        } else {
            c.setCreditCardIdPlayer(player.getIdplayer());
            c.setCreditCardHolder(c.getCreditcardHolder());
            c.setCreditcardNumber(c.getCreditcardNumber());
            c.setCreditcardType(c.getCreditcardType());
            c.setCreditCardExpirationDateLdt(c.getCreditCardExpirationDateLdt());
            String lastTwo = String.valueOf(c.getCreditCardExpirationDateLdt().getYear()).substring(2);
            String s = String.valueOf(c.getCreditCardExpirationDateLdt().getMonthValue())
                    + "/" + lastTwo;
            c.setCreditCardExpirationDateString(s);
            c.setCreditcardVerificationCode(c.getCreditcardVerificationCode());
            LOG.debug("creditcard completed with db info = " + c);
        } // end if
        return c;
    } // end method

    private Creditcard completeWithGreenfee(Greenfee greenfee, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with Greenfee " + greenfee);
        try {
            if (greenfee.getPrice() == 0) {
                LOG.debug("amount ZERO no payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard prefilled with player's data = " + cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(greenfee.getPrice());
            cc.setCommunication(greenfee.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.GREENFEE.toString());
            cc.setCreditcardCurrency(greenfee.getCurrency());
            return cc;
        } catch (Exception ex) {
            String msg = "Creditcardpayment Greenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    private Creditcard completeWithLesson(Professional professional, List<Lesson> lessons,
            Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with listLessons = " + lessons);
            LOG.debug("Professional = " + professional);
            LOG.debug("Player = " + player);
            if (professional.getProAmount() == 0) {
                LOG.debug("Amount ZERO no payment Lesson needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard after prefilling = " + cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(professional.getProAmount() * lessons.size());
            String s = "";
            for (Lesson lesson : lessons) {
                s = s + lesson.getEventStartDate().toString() + " / ";
            } // end for
            cc.setCommunication("Réservation Lesson : "
                    + s + " ,pro # = " + professional.getProPlayerId());
            cc.setTypePayment(Creditcard.etypePayment.LESSON.toString());
            LOG.debug("exiting completeWithLesson with creditcard = " + cc);
            return cc;
        } catch (Exception ex) {
            String msg = "Exception in completeWithLesson ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    private Creditcard completeWithCotisation(Cotisation cotisation, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("Cotisation = " + cotisation);
            if (cotisation.getPrice() == 0) {
                LOG.debug("amount ZERO no payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard prefilled = " + cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(cotisation.getPrice());
            cc.setCommunication(cotisation.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.COTISATION.toString());
            LOG.debug("creditcard completed with Cotisation = " + cc);
            return cc;
        } catch (Exception ex) {
            String msg = "Creditcardpayment Cotisation Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    private Creditcard completeWithSubscription(Subscription subscription, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("subscription = " + subscription);
            if (subscription.getSubscriptionAmount() == 0) {
                LOG.debug("amount ZERO -- No payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            cc.setPaymentOK(false);
            cc.setTotalPrice(subscription.getSubscriptionAmount());
            cc.setCommunication(subscription.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.SUBSCRIPTION.toString());
            LOG.debug("creditcard completed with subscription = " + cc);
            return cc;
        } catch (Exception ex) {
            String msg = "completeWithSubscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    private boolean needsUpdate(Creditcard c, Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with creditcard input = " + c);
            Creditcard creditc = readCreditcard.read(player);
            if (creditc == null) {
                LOG.debug("CREATION : First utilisation of a creditcard for user = " + player.getPlayerLastName());
                if (createCreditcard.create(c)) {
                    LOG.debug("creditcard is created in DB");
                    return true;
                } else {
                    LOG.debug("ERROR : creditcard NOT created in DB");
                    return false;
                } // end if
            } // end first cc

            if (creditc.getCreditCardExpirationDateLdt().equals(c.getCreditCardExpirationDateLdt())
                    && creditc.getCreditcardNumber().equals(c.getCreditcardNumber())
                    && creditc.getCreditcardHolder().equals(c.getCreditcardHolder())
                    && creditc.getCreditcardVerificationCode().equals(c.getCreditcardVerificationCode())
                    && creditc.getCreditcardType().equals(c.getCreditcardType())) {
                LOG.debug("creditcard already registered and all data are the same - no update DB needed");
                return false;
            } else {
                LOG.debug("a modified creditcard has been introduced !");
                if (modifyCreditcard.modify(creditcard)) {
                    String msg = "creditcard is modified in DB";
                    LOG.info(msg);
                    showMessageInfo(msg);
                    return true;
                } else {
                    LOG.error("ERROR : creditcard is NOT modified in DB");
                    return false;
                } // end if
            } // end equals
        } catch (Exception e) {
            String msg = "Exception in needsUpdate: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        }
    } // end method

    private Boolean handlePaymentSubscription(Creditcard c, Subscription subscription) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("handlePayments for subscription = " + subscription);
            LOG.debug("handlePayments Subscription with creditcard input = " + c);
            subscription.setPaymentReference(c.getCreditcardPaymentReference());
            subscription.setIdplayer(c.getCreditCardIdPlayer());

            if (paymentSubscriptionController.createPayment(subscription)) {
                LOG.debug("after Subscription createPayment : we are OK " + subscription);
                String msg = utils.LCUtil.prepareMessageBean("subscription.success")
                        + " start date = " + subscription.getStartDate().format(interfaces.GolfInterface.ZDF_DAY)
                        + " end date = " + subscription.getEndDate().format(interfaces.GolfInterface.ZDF_DAY);
                LOG.info(msg);
                return true;
            } else {
                String msg = "error : subscription NOT modified !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            } // end if
        } catch (Exception e) {
            String msg = "Exception in handlePaymentSubscription: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        }
    } // end method

    // ========================================
    // ENDPOINTS JAX-RS — migrés depuis CreditcardController 2026-02-26
    // ========================================

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_choice/{isbn}")
    public jakarta.ws.rs.core.Response paymentChoice(
            @PathParam("isbn") String param,
            @Context HttpServletRequest servletRequest,
            @Context ServletContext servletContext,
            @Context jakarta.ws.rs.core.Request request,
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String Currency
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from payment server");
        try {
            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoCache(true);
            cacheControl.setNoStore(true);
            cacheControl.setMustRevalidate(true);
            LOG.debug("with param = " + param);
            LOG.debug("with UriInfo = " + uriInfo.getRequestUri().toString());
            LOG.debug("Amount = " + amount);
            LOG.debug("PaymentReference = " + reference);
            LOG.debug("ServletContext getContextPath = " + servletContext.getContextPath());
            String href = "http://localhost:8080" + servletContext.getContextPath();
            String location = href + "/rest/paymentController";
            LOG.debug("location = " + location);

            if (param.equals("phase1")) {
                LOG.debug("handling param phase1 = " + param);
                return jakarta.ws.rs.core.Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_canceled" + "/101"))
                        .header("type", "payment_cancel")
                        .build();
            } // end phase 1

            if (param.equals("phase2")) {
                LOG.debug("handling param phase2 = " + param);
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_confirmed"))
                        .build();
            } // end phase 2

            if (param.equals("phase3")) {
                LOG.debug("handling param phase3 = " + param);
                return Response
                        .status(Response.Status.FOUND)
                        .location(java.net.URI.create(location + "/payment_handle" + "/101"))
                        .build();
            } // end phase 3

            return Response.status(Response.Status.NOT_FOUND).entity("redirect from payment_choice - unknown param or not yet implemented").build();
        } catch (Exception e) {
            String msg = "Exception in paymentChoice: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_canceled/{isbn}")
    public Response paymentCancel(
            @PathParam("isbn") String id,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("type") String type,
            @CookieParam("Amount") String amount
    ) throws IOException, WebApplicationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from python server");
        try {
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
            creditcard.setCreditcardPaymentReference(null);
            creditcard.setCommunication("Payment refused by User Client");
            LOG.debug("going to /creditcard_payment_canceled.xhtml");
            LOG.debug("request.getContextPath() = " + request.getContextPath());
            response.sendRedirect(request.getContextPath() + "/creditcard_payment_canceled.xhtml?faces-redirect=true&message=Canceled by User");
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity("Payment is canceled")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .cacheControl(cacheControl)
                    .build();
        } catch (Exception e) {
            String msg = "Exception in paymentCancel: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("payment_confirmed")
    public Response paymentConfirmed(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context HttpHeaders headers,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
            @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String Currency
    ) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + ", coming from python server");
        try {
            LOG.debug(NEW_LINE);
            LOG.debug("JSESSIONID = " + sessionid);
            LOG.debug("headers = " + headers);
            LOG.debug("response header payment reference = " + response.getHeader("PaymentReference"));
            LOG.debug("creditcard = " + creditcard);
            LOG.debug("browser = " + whichBrowser);
            LOG.debug("User = " + user);
            LOG.debug("Amount = " + amount);
            creditcard.setTotalPrice(Double.valueOf(amount));
            creditcard.setCreditcardPaymentReference(reference);
            LOG.debug("going to /creditcard_payment_executed.xhtml");
            response.sendRedirect(request.getContextPath() + "/creditcard_payment_executed.xhtml");
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            String msg = "Exception in paymentConfirmed: " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public Creditcard getCreditcard() {
        if (creditcard == null) creditcard = new Creditcard();
        return creditcard;
    } // end method

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    } // end method

    public String getSavedType() {
        return savedType;
    } // end method

    public void setSavedType(String savedType) {
        this.savedType = savedType;
    } // end method

    public Integer getProgress1() {
        progress1 = updateProgress(progress1);
        return progress1;
    } // end method

    public void setProgress1(Integer progress1) {
        this.progress1 = progress1;
    } // end method

    public Greenfee getGreenfee() {
        return greenfee;
    } // end method

    public void setGreenfee(Greenfee greenfee) {
        this.greenfee = greenfee;
    } // end method

    public TarifGreenfee getTarifGreenfee() {
        return tarifGreenfee;
    } // end method

    public void setTarifGreenfee(TarifGreenfee tarifGreenfee) {
        this.tarifGreenfee = tarifGreenfee;
    } // end method

    public TarifMember getTarifMember() {
        return tarifMember;
    } // end method

    public void setTarifMember(TarifMember tarifMember) {
        this.tarifMember = tarifMember;
    } // end method

    public Professional getProfessional() {
        return professional;
    } // end method

    public void setProfessional(Professional professional) {
        this.professional = professional;
    } // end method

    public List<Lesson> getListLessons() {
        return listLessons;
    } // end method

    public void setListLessons(List<Lesson> listLessons) {
        this.listLessons = listLessons;
    } // end method

    public Lesson getSelectedLesson() {
        return selectedLesson;
    } // end method

    public void setSelectedLesson(Lesson selectedLesson) {
        this.selectedLesson = selectedLesson;
    } // end method

    public Player getPlayerPro() {
        return playerPro;
    } // end method

    public void setPlayerPro(Player playerPro) {
        this.playerPro = playerPro;
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // tests locaux
    } // end main
    */

} // end class
