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
// servlet imports removed 2026-03-21 — no longer needed after JAX-RS extraction
// JAX-RS server annotations removed 2026-03-21 — moved to rest.PaymentRestResource
// JAX-RS client imports kept for testWebService()
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// PaymentOrchestrator/PaymentTarget imports removed 2026-03-21 — moved to PaymentRestResource
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
public class PaymentController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    // ========================================
    // INJECTIONS CDI
    // ========================================

    @Inject private ApplicationContext                          appContext;
    @Inject private read.ReadCreditcard                          readCreditcard;       // migrated 2026-02-26 — was CreditcardController
    @Inject private payment.PaymentSubscriptionController       paymentSubscriptionController;
    @Inject private Controllers.TarifMemberController           tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController         tarifGreenfeeController;
    @Inject private Controller.refact.MemberController          memberController;
    // SchedulerProController removed — @ViewScoped can't be injected in @SessionScoped. Data via appContext. // 2026-03-22
    @Inject private create.CreateLesson                         createLesson;
    @Inject private create.CreatePaymentLesson                  createPaymentLesson; // payments_lesson — 2026-03-29
    @Inject private manager.PlayerManager                       playerManager;
    @Inject private read.ReadClub                               readClubService;
    @Inject private Controllers.HttpController                  httpController;
    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext
    @Inject private mail.CreditcardMail                         creditcardMail;  // migrated 2026-02-26
    @Inject private mail.LessonMail                             lessonMail;
    @Inject private entite.Settings                             settings;        // security audit 2026-03-18
    @Inject private payment.PaymentStateStore                   paymentStateStore; // architecture REST/JSF separation 2026-03-21

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private Creditcard    creditcard;
    private String        savedType;
    private Integer       progress1 = 0;
    private Greenfee      greenfee;
    private Professional  professional;
    private List<Lesson>  listLessons = new ArrayList<>();
    private Lesson        selectedLesson;
    private Player        playerPro;
    private boolean running = false;  // new 09-03-2026
    private boolean proFree = false;  // true when student is also an active pro → free lesson
    private String completionNonce;   // architecture REST/JSF separation 2026-03-21
    private boolean lessonMailsSent = false; // guard — prevents double-send on page refresh

    public PaymentController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        creditcard = new Creditcard();
        greenfee = new Greenfee();
        progress1 = 0;
        LOG.debug("PaymentController initialized");
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("entering {} — source: {}", event.getSource());
        creditcard     = new Creditcard();
        savedType      = null;
        progress1      = 0;
        greenfee       = new Greenfee();
        professional   = null;
        listLessons    = new ArrayList<>();
        selectedLesson  = null;
        playerPro       = null;
        completionNonce = null;
        LOG.debug("PaymentController reset done");
    } // end method

    // ========================================
    // METHODES D'ACTION — migrées depuis CourseController 2026-02-25
    // ========================================

    public String manageCotisation() throws SQLException { // called from cotisation.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("tarifMember = {}", memberController.getTarifMember());
            LOG.debug("cotisation = {}", appContext.getCotisation());
            LOG.debug("round = {}", appContext.getRound());
            appContext.setCreditcardType(etypePayment.COTISATION.toString());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());
            Cotisation cotisation = tarifMemberController.completeCotisation(memberController.getTarifMember(), appContext.getPlayer(), appContext.getRound());
            if (cotisation == null) {
                String msg = "cotisation not found !! is null";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } // end if
            cotisation.setIdplayer(appContext.getPlayer().getIdplayer());
            cotisation.setIdclub(appContext.getClub().getIdclub());
            cotisation.setCommunication(appContext.getClub().getClubName() + " : " + cotisation.getCommunication());
            LOG.debug("Cotisation loaded = {}", cotisation);

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
            LOG.debug("cotisation type : spontaneous ou round ? {}", cotisation.getType());
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
                return null;
            } // end if
        } catch (SQLException ex) {
            handleSQLException(ex, methodName);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageCotisation

    public String manageLesson() throws SQLException { // called from schedule_pro.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setCreditcardType(etypePayment.LESSON.toString());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            professional = appContext.getProfessional();
            LOG.debug("professional coming from appContext = {}", professional);

            Club club = appContext.getClub();
            club.setIdclub(professional.getProClubId());
            club = readClubService.read(club);
            appContext.setClub(club);

            Player p = new Player();
            p.setIdplayer(professional.getProPlayerId());
            playerPro = playerManager.readPlayer(p.getIdplayer());

            // listLessons already populated via addLesson() calls from SchedulerProController (panier)
            listLessons.forEach(l -> l.setLessonAmount(professional.getProAmount()));

            // Both pros must be active: teaching pro guaranteed by ProfessionalListForClub filter;
            // student pro guaranteed by FindCountListProfessional date filter (ProClubStartDate/EndDate)
            if (!playerManager.findProfessionals(appContext.getPlayer()).isEmpty()) {
                listLessons.forEach(l -> l.setLessonAmount(0.0));
                proFree = true;
                return "cart_pro.xhtml?faces-redirect=true";
            } // end if Professional

            listLessons.forEach(item -> LOG.debug("lesson start date: {}", item.getEventStartDate()));
            if (professional.getProAmount().equals(0.0)) {
                String msg = "amount ZERO no payment Lesson needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if
            creditcard = completeWithLesson(professional, listLessons, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            if (creditcard != null) {
                LOG.info("creditcard with lesson = {}", creditcard);
                return "cart_pro.xhtml?faces-redirect=true";
            } else {
                String msg = "paiement par creditcard KO : quelle conclusion ?";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } // end if
        } catch (SQLException ex) {
            handleSQLException(ex, methodName);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageLesson

    /**
     * Confirms free lessons for a student who is also an active professional.
     * Called from cart_pro.xhtml when proFree == true.
     */
    public String confirmProFreeLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            for (Lesson lesson : listLessons) {
                if (createLesson.create(lesson, appContext.getPlayer())) {
                    LOG.info("free lesson created: {}", lesson.getEventTitle());
                } else {
                    showMessageFatal("error: free lesson not registered");
                    return null;
                }
            }
            proFree = false;
            listLessons.clear();
            return "welcome.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public boolean isProFree() { return proFree; }

    public String cancelCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.clear();
        proFree = false;
        return "schedule_pro.xhtml?faces-redirect=true";
    } // end method

    public String manageGreenfee() { // called from price_round_greenfee.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with creditcard = {}", creditcard);
            LOG.debug("with greenfee = {}", greenfee);

            appContext.setCreditcardType(etypePayment.GREENFEE.toString());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            // 1. complete greenfee with price — use memC.tarifGreenfee (loaded by findTarifGreenfee)
            greenfee = tarifGreenfeeController.completeGreenfee(memberController.getTarifGreenfee(), appContext.getClub(), appContext.getRound(), appContext.getPlayer());
            LOG.debug("Greenfee completed with tarif data = {}", greenfee);
            if (greenfee.getPrice() == 0) {
                String msg = "amount ZERO,  no payment needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if
            // 2. complete creditcard
            creditcard = completeWithGreenfee(greenfee, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            LOG.debug("creditcard Greenfee completed = {}", creditcard);
            return("creditcard.xhtml?faces-redirect=true");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageGreenfee

    public String manageSubscription() { // called from subscription.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Subscription subscription = appContext.getSubscription();
            subscription.setIdplayer(appContext.getPlayer().getIdplayer());
            LOG.debug("subscription = {}", subscription);

            creditcard.setTypePayment(etypePayment.SUBSCRIPTION.toString());
            LOG.debug("creditcard = {}", creditcard);
            appContext.setCreditcardType(etypePayment.SUBSCRIPTION.toString());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());
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
                    LOG.debug("creditcard completed with subscription = {}", creditcard);
                    return "creditcard.xhtml?faces-redirect=true";
                }
                default -> {
                    LOG.debug("getSubCode() UNKNOWN = {}", subscription.getSubCode());
                    return null;
                }
            } // end switch
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageSubscription

    public String creditCardMail() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with creditcard typePayment = {}", creditcard.getTypePayment());
        try {
            if (Creditcard.etypePayment.GREENFEE.toString().equals(creditcard.getTypePayment())) {
                if (!creditcardMail.sendMailGreenfee(appContext.getPlayer(), creditcard, memberController.getTarifGreenfee(), appContext.getRound(), appContext.getInscription())) {
                    LOG.warn("sendMailGreenfee returned false");
                }
            } // end if
            if (Creditcard.etypePayment.SUBSCRIPTION.toString().equals(creditcard.getTypePayment())) {
                if (!creditcardMail.sendMailSubscription(appContext.getPlayer(), creditcard, appContext.getSubscription())) {
                    LOG.warn("sendMailSubscription returned false");
                }
            } // end if
            if (Creditcard.etypePayment.COTISATION.toString().equals(creditcard.getTypePayment())) {
                if (!creditcardMail.sendMailCotisation(appContext.getPlayer(), creditcard, appContext.getCotisation(), appContext.getClub(), memberController.getTarifMember())) {
                    LOG.warn("sendMailCotisation returned false");
                }
            } // end if
            return "creditcard_payment_executed.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    //  !! ne pas toucher ..String typePayment.
    public void onCompletePayment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("coming from creditcard_accepted.xhtml");
        progress1 = 0;  // ← reset pour la prochaine visite
        running = false; // new 09-03-2026
        try {
            creditcard.setTypePayment(appContext.getCreditcardType());
            savedType = creditcard.getTypePayment();
            LOG.debug("before payment creditcard = {}", creditcard);
            String v = httpController.sendPaymentServer(creditcard); // migrated 2026-02-26 — was creditcardController.getCC2()
            LOG.debug("var v returned = {}", v);
            LOG.debug("creditcard returned = {}", creditcard);
            if (v.equals("200")) {
                String msg = "Payment validé par Amazone Payments Inc !";
                LOG.info(msg);
                LOG.debug("creditcardType in onCompletePayment = {}", appContext.getCreditcardType());

                // Store transaction snapshot in PaymentStateStore — architecture REST/JSF separation 2026-03-21
                String nonce = creditcard.getPaymentNonce(); // set by HttpController.sendPaymentServer
                payment.PaymentTransaction tx = new payment.PaymentTransaction(nonce);
                tx.setCreditcard(creditcard);
                tx.setPlayerId(appContext.getPlayer().getIdplayer());
                tx.setSavedType(savedType);
                tx.setCreditcardType(appContext.getCreditcardType());
                tx.setSubscription(appContext.getSubscription());
                tx.setCotisation(appContext.getCotisation());
                tx.setGreenfee(greenfee);
                tx.setRound(appContext.getRound());
                tx.setClub(appContext.getClub());
                tx.setCourse(appContext.getCourse());
                tx.setInscription(appContext.getInscription());
                tx.setListLessons(listLessons);
                paymentStateStore.store(nonce, tx);
                LOG.debug("PaymentTransaction stored with nonce={}", nonce);

                LOG.debug("before going with context to 5000/about");
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().redirect(settings.getProperty("PAYMENT_SERVICE_URL") + "/about");
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

    // handlePayments — moved to rest.PaymentRestResource 2026-03-21

    //  utilisé dans creditcard_accepted.xhtml
    public void onStart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        running = true;
        progress1 = 0;
        LOG.debug("entering {}, progress1 = {}", progress1);
    //    showMessageInfo("entering onStart, progress1 = " + progress1);
    } // end method

    public void onProgress() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String msg = "Progress Updated " + progress1;
        showMessageInfo(msg);
    } // end method

    public void cancelProgress() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("Payment canceled by User");
        progress1 = 0;  // était null
        running = false; // new 09-03-2026
        creditcard.setPaymentOK(false);
        String msg = "Creditcard payment canceled by user";
        LOG.error(msg);
        showMessageFatal(msg);
    } // end method

    public void deleteLesson() { // used in cart_pro.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.listLessons.remove(this.selectedLesson);
        String msg = "Lesson removed = = " + this.selectedLesson;
        this.selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
        creditcard.setTotalPrice(listLessons.size() * professional.getProAmount());
        msg = "recalculated totalPrice is now " + creditcard.getTotalPrice();
        LOG.info(msg);
        showMessageInfo(msg);
        org.primefaces.PrimeFaces.current().ajax().update("form_cart_pro:growl-msg", "form_cart_pro:listLessons", "form_cart_pro:messages");
    } // end method

    /** Called from SchedulerProController — adds a pending lesson to the cart (no DB). */
    public void addLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.add(lesson);
        LOG.info("lesson added to cart: {}, cart size={}", lesson.getEventTitle(), listLessons.size());
    } // end method

    /** Called from SchedulerProController — removes a pending lesson from the cart (no DB). */
    public void removeLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.removeIf(l -> l.getEventProId() == lesson.getEventProId()
                && l.getEventStartDate() != null
                && l.getEventStartDate().equals(lesson.getEventStartDate()));
        LOG.info("lesson removed from cart, cart size={}", listLessons.size());
    } // end method

    /** Called from SchedulerProController on drag-drop — updates a pending lesson in the cart (no DB). */
    public void updatePendingLesson(Lesson before, Lesson after) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.replaceAll(l -> {
            if (l.getEventProId() == before.getEventProId()
                    && before.getEventStartDate() != null
                    && before.getEventStartDate().equals(l.getEventStartDate())) {
                l.setEventStartDate(after.getEventStartDate());
                l.setEventEndDate(after.getEventEndDate());
                l.setEventTitle(after.getEventTitle());
            }
            return l;
        });
        LOG.debug("pending lesson updated in cart");
    } // end method

    public String to_creditcard_test_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        creditcard.setTotalPrice(155.6);
        creditcard.setCommunication(" prepared creditcard communication");
        creditcard.setCreditcardType("GREENFEE");
        return "creditcard_test.xhtml?faces-redirect=true";
    } // end method

    public void creditCardNumberListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("creditcardNumber OldValue = {}", e.getOldValue());
        LOG.debug("creditcardNumber NewValue = {}", e.getNewValue());
        creditcard.setCreditcardNumber(e.getNewValue().toString());
    } // end method

    public void creditCardTypeListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("creditcardType OldValue = {}", e.getOldValue());
        LOG.debug("creditcardType NewValue = {}", e.getNewValue());
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
        LOG.debug("entering {} for creditcard, server = python");
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
            LOG.debug("just before send payment to python server, creditcard = {}", creditcard);
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
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method testWebServiceHttp

    public void testWebService() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String ws = null;
        Response response = null;
        try {
            Creditcard c = new Creditcard();
            c.setCreditCardHolder("LOUIS COLLET");
            c.setCreditCardIdPlayer(324713);
            c.setCommunication("creditcard communication");
            c.setCreditcardNumber("1111222233334444");
            c.setTotalPrice(35.0);
            c.setTypePayment("LESSON");
            c.setCreditcardType("VISA");
            c.setCreditcardVerificationCode((short) 567);
            String strJson = OBJECT_MAPPER.writeValueAsString(c);
            LOG.debug("creditcard data in json format:\n{}", strJson);

            jakarta.ws.rs.client.Client client = ClientBuilder.newClient();
            ws = settings.getProperty("CREDITCARD_SERVICE_URL") + "/creditcard/" + URLEncoder.encode(strJson, "utf-8");
            LOG.debug("going to Webservice creditcard escaped:\n{}", ws);
            WebTarget webTarget = client.target(ws);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            response = invocationBuilder.get();
            response.bufferEntity();
            String s = response.readEntity(String.class);
            LOG.debug("readEntity s = {}", s);
            final Cookie sessionId = response.getCookies().get("JSESSIONID");
            LOG.debug("sessionId = {}", sessionId);

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
            LOG.debug("viewhandler = {}", viewHandler);
            UIViewRoot viewRoot = viewHandler.createView(facesContext, facesContext.getViewRoot().getViewId());
            LOG.debug("viewRoot = {}", viewRoot);
            LOG.debug("viewId = {}", viewRoot.getViewId());
            String actionUrl = viewHandler.getActionURL(facesContext, viewRoot.getViewId());
            LOG.debug("actionUrl = {}", actionUrl);

            creditcard = c; //test only
            facesContext.getExternalContext().redirect("creditcard_payment_executed.xhtml?faces-redirect=true");

        } catch (Exception e) {
            handleGenericException(e, methodName);
        } finally {
            if (response != null) { response.close(); }
            LOG.debug("response closed");
        }
    } // end method

    // ========================================
    // PROGRESS BAR — migrée depuis CourseController 2026-02-25
    // ========================================

    private Integer updateProgress(Integer progress) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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

    private Creditcard prefilling(Player player) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Creditcard c = readCreditcard.read(player);
        LOG.debug("creditcard loaded = {}", c);
        if (c.getCreditcardNumber() == null) {
            LOG.debug("first utilisation of a creditcard for user = {}", player.getPlayerLastName());
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
            LOG.debug("creditcard completed with db info = {}", c);
        } // end if
        return c;
    } // end method

    private Creditcard completeWithGreenfee(Greenfee greenfee, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with Greenfee {}", greenfee);
        try {
            if (greenfee.getPrice() == 0) {
                LOG.debug("amount ZERO no payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard prefilled with player's data = {}", cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(greenfee.getPrice());
            cc.setCommunication(greenfee.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.GREENFEE.toString());
            cc.setCreditcardCurrency(greenfee.getCurrency());
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithGreenfee

    private Creditcard completeWithLesson(Professional professional, List<Lesson> lessons,
            Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("listLessons = {}", lessons);
            LOG.debug("professional = {}", professional);
            LOG.debug("player = {}", player);
            if (professional.getProAmount() == 0) {
                LOG.debug("Amount ZERO no payment Lesson needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard after prefilling = {}", cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(professional.getProAmount() * lessons.size());
            java.time.format.DateTimeFormatter fmtDate = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            java.time.format.DateTimeFormatter fmtTime = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            StringBuilder s = new StringBuilder("Lesson ");
            for (int i = 0; i < lessons.size(); i++) {
                if (i > 0) s.append(",");
                s.append(lessons.get(i).getEventStartDate().format(fmtDate))
                 .append(" ")
                 .append(lessons.get(i).getEventStartDate().format(fmtTime))
                 .append("→")
                 .append(lessons.get(i).getEventEndDate().format(fmtTime));
            } // end for
            s.append(" #").append(professional.getProPlayerId());
            String comm = s.length() <= 140 ? s.toString() : s.substring(0, 137) + "...";
            cc.setCommunication(comm);
            cc.setTypePayment(Creditcard.etypePayment.LESSON.toString());
            LOG.debug("exiting completeWithLesson with creditcard = {}", cc);
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithLesson

    private Creditcard completeWithCotisation(Cotisation cotisation, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("cotisation = {}", cotisation);
            if (cotisation.getPrice() == 0) {
                LOG.debug("amount ZERO no payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard prefilled = {}", cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(cotisation.getPrice());
            cc.setCommunication(cotisation.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.COTISATION.toString());
            LOG.debug("creditcard completed with cotisation = {}", cc);
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithCotisation

    private Creditcard completeWithSubscription(Subscription subscription, Player player) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("subscription = {}", subscription);
            if (subscription.getSubscriptionAmount() == 0) {
                LOG.debug("amount ZERO -- No payment needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            cc.setPaymentOK(false);
            cc.setTotalPrice(subscription.getSubscriptionAmount());
            cc.setCommunication(subscription.getCommunication());
            cc.setTypePayment(Creditcard.etypePayment.SUBSCRIPTION.toString());
            LOG.debug("creditcard completed with subscription = {}", cc);
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithSubscription

    // needsUpdate — moved to PaymentManager.needsUpdate 2026-03-21
    // handlePaymentSubscription — moved to PaymentOrchestrator/SubscriptionRegistrar 2026-03-21

    // ========================================
    // JAX-RS ENDPOINTS — moved to rest.PaymentRestResource 2026-03-21
    // Architecture separation: REST callbacks are now @RequestScoped
    // and use PaymentStateStore instead of @SessionScoped state.
    // ========================================

    // ========================================
    // PAYMENT COMPLETION — sync REST result back to JSF session
    // Called via f:viewParam/preRenderView on creditcard_payment_executed.xhtml
    // ========================================

    /**
     * Called by preRenderView on creditcard_payment_executed.xhtml and
     * creditcard_payment_canceled.xhtml. Retrieves the completed PaymentTransaction
     * from PaymentStateStore and syncs creditcard data back to the JSF session.
     */
    public void onPaymentCompleted() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - completionNonce={}", completionNonce);
        if (completionNonce == null || completionNonce.isEmpty()) {
            LOG.debug("no nonce, skipping sync (direct JSF navigation)");
            return;
        }
        payment.PaymentTransaction tx = paymentStateStore.consume(completionNonce);
        if (tx == null) {
            LOG.warn("transaction not found for nonce={} — syncing skipped, sending mails from session", completionNonce);
            sendLessonMails();
            return;
        }
        // Sync creditcard data from the transaction back to the JSF session
        this.creditcard = tx.getCreditcard();
        this.savedType = tx.getSavedType();
        // Restore menu visibility — may have been lost if session was recreated during payment flow
        if (appContext.getPlayer() != null) {
            appContext.getPlayer().setShowMenu(true);
            LOG.debug("showMenu restored to true");
        }
        LOG.debug("creditcard synced from PaymentTransaction, nonce={}", completionNonce);

        // Persist lessons + 1 payments_lesson groupé — JSF context garanti (preRenderView)
        if ("LESSON".equals(this.savedType)
                && tx.getListLessons() != null
                && !tx.getListLessons().isEmpty()) {
            try {
                java.time.format.DateTimeFormatter fmt     = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                java.time.format.DateTimeFormatter fmtTime = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                int studentId = appContext.getPlayer().getIdplayer();
                List<entite.Lesson> lessons = tx.getListLessons();

                // 1. Persister chaque leçon dans la table lesson (réservation créneau)
                boolean allOk = true;
                for (entite.Lesson lesson : lessons) {
                    boolean ok = createLesson.create(lesson, appContext.getPlayer());
                    if (ok) {
                        LOG.info("lesson persisted: {} for player={}", lesson.getEventTitle(), studentId);
                    } else {
                        LOG.error("failed to persist lesson: {}", lesson);
                        allOk = false;
                    }
                }

                // 2. UN seul enregistrement dans payments_lesson pour tout le paiement
                StringBuilder comm = new StringBuilder();
                comm.append("Student #").append(studentId).append("\n");
                for (entite.Lesson lesson : lessons) {
                    comm.append(lesson.getEventStartDate().format(fmt))
                        .append("→").append(lesson.getEventEndDate().format(fmtTime))
                        .append("\n");
                }
                comm.append("Ref: ").append(this.creditcard.getCreditcardPaymentReference()).append("\n");
                if (this.professional == null) {
                    LOG.warn("professional is null, skipping payments_lesson");
                } else {
                    createPaymentLesson.create(lessons, this.creditcard, this.professional, comm.toString());
                }
                LOG.info("payments_lesson persisted: {}", comm);

                if (allOk) {
                    utils.LCUtil.showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.success"));
                    sendLessonMails();
                }

            } catch (java.sql.SQLException e) {
                handleSQLException(e, methodName);
            }
        }
    } // end method

    /**
     * Sends lesson payment confirmation mails using session data.
     * Called by preRenderView on creditcard_payment_executed.xhtml.
     * Independent of PaymentStateStore — uses session fields directly.
     * Guard lessonMailsSent prevents double-send on page refresh.
     */
    public void sendLessonMails() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering");
        if (lessonMailsSent) {
            LOG.debug("mails already sent — skipping");
            return;
        }
        if (listLessons == null || listLessons.isEmpty()) {
            LOG.debug("no lessons in session — not a lesson payment");
            return;
        }
        if (professional == null) {
            LOG.warn("professional is null — lesson mails not sent");
            return;
        }
        try {
            lessonMail.sendPaymentConfirmation(appContext.getPlayer(), professional, listLessons, creditcard);
            lessonMail.sendProNotification(appContext.getPlayer(), professional, listLessons, creditcard);
            lessonMailsSent = true;
            LOG.info("lesson mails enqueued for player={}", appContext.getPlayer().getIdplayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String getCompletionNonce() { return completionNonce; } // end method
    public void setCompletionNonce(String completionNonce) { this.completionNonce = completionNonce; } // end method

    // ========================================
    // PAYMENT TYPE — titre et include résolu côté bean
    // ========================================

    public String getPaymentTitle() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return switch (appContext.getCreditcardType()) {
            case "SUBSCRIPTION" -> utils.LCUtil.prepareMessageBean("title.payment.subscription");
            case "GREENFEE"     -> utils.LCUtil.prepareMessageBean("title.payment.round");
            case "COTISATION"   -> utils.LCUtil.prepareMessageBean("title.payment.cotisation");
            case "LESSON"       -> utils.LCUtil.prepareMessageBean("title.payment.lesson");
            default             -> "";
        };
    } // end method

    public String getPaymentIncludeSrc() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return switch (appContext.getCreditcardType()) {
            case "GREENFEE"     -> "include/include_summary_round.xhtml";
            case "COTISATION"   -> "include/include_summary_cotisation.xhtml";
            case "SUBSCRIPTION" -> "include/include_subscription.xhtml";
            case "LESSON"       -> "include/include_lesson.xhtml";
            default             -> "include/include_empty.xhtml";
        };
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
        if (running) {
            progress1 = updateProgress(progress1);
        }
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
        LOG.debug("entering {}", methodName);
        // tests locaux
    } // end main
    */

} // end class
