package Controllers;

import context.ApplicationContext;
import entite.*;
import enumeration.eTypePayment;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.DTF_DAY_HHMM;
import static interfaces.GolfInterface.DTF_DAY_MONTH;
import static interfaces.GolfInterface.DTF_DAY_SLASH;
import static interfaces.GolfInterface.ZDF_HOURS;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
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
// servlet imports removed 2026-03-21 - no longer needed after JAX-RS extraction
// JAX-RS server annotations removed 2026-03-21 - moved to rest.PaymentRestResource
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
import static enumeration.eTypePayment.COTISATION;
import static enumeration.eTypePayment.GREENFEE;
import static enumeration.eTypePayment.LESSON;
import static enumeration.eTypePayment.SUBSCRIPTION;
import java.util.Objects;
// PaymentOrchestrator/PaymentTarget imports removed 2026-03-21 - moved to PaymentRestResource
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
    @Inject private cache.CacheInvalidator                       cacheInvalidator;
    @Inject private read.ReadCreditcard                          readCreditcard;       // migrated 2026-02-26 - was CreditcardController
    @Inject private payment.PaymentSubscriptionController       paymentSubscriptionController;
    @Inject private payment.PaymentCotisationController         paymentCotisationController;
    @Inject private payment.PaymentGreenfeeController           paymentGreenfeeController;
    @Inject private payment.PaymentLessonController             paymentLessonController;
    @Inject private Controllers.TarifMemberController           tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController         tarifGreenfeeController;
    @Inject private Controllers.MemberController                memberController;
    @Inject private Controllers.CartController                  cartController;       // cart state extracted 2026-05-14
    // SchedulerProController removed - @ViewScoped can't be injected in @SessionScoped. Data via appContext. // 2026-03-22
    @Inject private create.CreateLesson                         createLesson;
    @Inject private find.FindRoundBySlot                        findRoundBySlot;
    @Inject private find.FindInscriptionRound                   findInscriptionRound;
    @Inject private lists.ParticipantsRoundList                 participantsRoundList;
    @Inject private manager.RoundManager                        roundManager;
    @Inject private manager.PlayerManager                       playerManager;
    @Inject private read.ReadClub                               readClubService;
    @Inject private Controllers.HttpController                  httpController;
    // @Inject @SessionMap sessionMap - removed 2026-02-28, migrated to appContext
    @Inject private mail.CreditcardMail                         creditcardMail;  // migrated 2026-02-26
    @Inject private mail.LessonMail                             lessonMail;
    @Inject private entite.Settings                             settings;        // security audit 2026-03-18
    @Inject private payment.PaymentStateStore                   paymentStateStore; // architecture REST/JSF separation 2026-03-21
    @Inject private find.FindCart                               findCartService;        // cart persistence 2026-05-07
    @Inject private delete.DeleteCart                           deleteCartService;      // cart persistence 2026-05-07
    @Inject private find.FindGreenfeePaid                       findGreenfeePaid;

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private Creditcard    creditcard;
    private String        savedType;
    private Integer       progress1 = 0;
    private Player        playerPro;
    private boolean       running = false;
    private String        completionNonce;   // architecture REST/JSF separation 2026-03-21
    private boolean       lessonMailsSent = false; // guard - prevents double-send on page refresh

    public PaymentController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        creditcard = new Creditcard();
        progress1  = 0;
        LOG.debug("PaymentController initialized");
    } // end method

    // ========================================
    // CDI EVENT - ResetEvent observer - 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        creditcard      = new Creditcard();
        savedType       = null;
        progress1       = 0;
        playerPro       = null;
        completionNonce = null;
        lessonMailsSent = false;
        LOG.debug("PaymentController reset done");
    } // end method

    // ========================================
    // METHODES D'ACTION - migrées depuis CourseController 2026-02-25
    // ========================================

    public String manageCotisation() throws SQLException { // called from cotisation.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("tarifMember = {}", memberController.getTarifMember());
            LOG.debug("cotisation = {}", appContext.getCotisation());
            LOG.debug("round = {}", appContext.getRound());
            appContext.setCreditcardType(COTISATION());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());
            Cotisation cotisation = tarifMemberController.completeCotisation(memberController.getTarifMember(), appContext.getPlayer(), java.time.LocalDate.now());
            if (cotisation == null) {
                String msg = "cotisation not found !! is null";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } // end if
            cotisation.setIdplayer(appContext.getPlayer().getIdplayer());
            cotisation.setIdclub(appContext.getClub().getIdclub());
            cotisation.setCommunication(java.util.Objects.toString(appContext.getClub().getClubName(), "") + " : " + cotisation.getCommunication());
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

            if ("PaymentCotisationSpontaneous".equals(appContext.getInputSelectClub())) {
                cotisation.setType("spontaneous");
                LOG.debug("Paiement spontané - NO inscription");
            } else {
                cotisation.setType("round"); // inscription à un round
            } // end if
            LOG.debug("cotisation type : spontaneous ou round ? {}", cotisation.getType());
            LOG.debug("amount non ZERO payment COTISATION needed !");
            appContext.setCotisation(cotisation);
            creditcard = completeWithCotisation(cotisation, appContext.getPlayer()); // migrated 2026-02-26 - was creditcardController
            if (creditcard != null) {
                String msg = "creditcard completed with Cotisation ! ";
                LOG.info(msg);
                cartController.upsertCart();
                return "cart.xhtml?faces-redirect=true";
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
            appContext.setCreditcardType(LESSON());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            cartController.setProfessional(appContext.getProfessional());
            Professional professional = cartController.getProfessional();
            LOG.debug("professional coming from appContext = {}", professional);

            Club club = appContext.getClub();
            club.setIdclub(professional.getProClubId());
            club = readClubService.read(club);
            appContext.setClub(club);

            Player p = new Player();
            p.setIdplayer(professional.getProPlayerId());
            playerPro = playerManager.readPlayer(p.getIdplayer());

            // Lesson amounts already set per-lesson by SchedulerProController.createLesson() via CalcLessonPrice.calc()
            // Both pros must be active: teaching pro guaranteed by ProfessionalListForClub filter;
            // student pro guaranteed by FindCountListProfessional date filter (ProClubStartDate/EndDate)
            if (!playerManager.findProfessionals(appContext.getPlayer()).isEmpty()) {
                for (Lesson lesson : cartController.getListLessons()) {
                    if (!createLesson.create(lesson, appContext.getPlayer())) {
                        showMessageFatal("error: free lesson not registered");
                        return null;
                    }
                    LOG.info("free lesson created: {}", lesson.getEventTitle());
                } // end for
                cartController.getListLessons().clear();
                cacheInvalidator.invalidateProfessionalCaches();
                showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.pro.free"));
                return null; // stay on schedule_pro.xhtml
            } // end if Professional

            cartController.getListLessons().forEach(item -> LOG.debug("lesson start date: {}", item.getEventStartDate()));
            boolean anyPriceConfigured = cartController.getListLessons().stream()
                    .anyMatch(l -> l.getLessonAmount() != null && l.getLessonAmount() > 0);
            if (!anyPriceConfigured) {
                LOG.warn("lesson tarif not configured for proId={}", professional.getProId());
                showMessageFatal(utils.LCUtil.prepareMessageBean("lesson.tarif.unknown"));
                cartController.getListLessons().clear();
                return null;
            } // end if
            creditcard = completeWithLesson(professional, cartController.getListLessons(), appContext.getPlayer()); // migrated 2026-02-26 - was creditcardController
            if (creditcard != null) {
                LOG.info("creditcard with lesson = {}", creditcard);
                cartController.upsertCart();
                return "cart.xhtml?faces-redirect=true";
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
     * Called from cart.xhtml when proFree == true.
     */
    public String confirmProFreeLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return cartController.confirmProFreeLesson();
    } // end method

    public boolean isProFree() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return cartController.isProFree();
    } // end method

    public String cancelCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return cartController.cancelCart();
    } // end method

    public String manageGreenfee() { // called from price_round_greenfee.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with creditcard = {}", creditcard);
            LOG.debug("with greenfee = {}", cartController.getGreenfee());

            appContext.setCreditcardType(GREENFEE());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            // 1. complete greenfee with price - use memC.tarifGreenfee (loaded by findTarifGreenfee)
            Greenfee gf = tarifGreenfeeController.completeGreenfee(memberController.getTarifGreenfee(), appContext.getClub(), appContext.getRound(), appContext.getPlayer());
            LOG.debug("Greenfee completed with tarif data = {}", gf);
            if (gf.getPrice() == 0) {
                String msg = "amount ZERO,  no payment needed !!";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            } // end if
            // 2. enrich with display + session fields and add to list
            gf.setClubName(appContext.getClub()     != null ? appContext.getClub().getClubName()      : "");
            gf.setCourseName(appContext.getCourse() != null ? appContext.getCourse().getCourseName()  : "");
            gf.setRoundHoles(appContext.getRound()  != null ? appContext.getRound().getRoundHoles()   : null);
            gf.setCourseId(appContext.getCourse()   != null ? appContext.getCourse().getIdcourse()    : null);
            gf.setRoundStart(appContext.getRound()  != null ? appContext.getRound().getRoundStart()   : null);
            gf.setRoundGame(appContext.getRound()   != null ? appContext.getRound().getRoundGame()    : null);
            cartController.getListGreenfees().add(gf);
            cartController.setGreenfee(gf);
            LOG.debug("listGreenfees size={}", cartController.getListGreenfees().size());
            // 3. complete creditcard with running total
            creditcard = completeWithGreenfee(gf, appContext.getPlayer()); // migrated 2026-02-26 - was creditcardController
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("creditcard Greenfee total = {}", creditcard.getTotalPrice());
            cartController.upsertCart();
            return "cart.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method manageGreenfee

    /**
     * Ajoute un greenfee au panier et reste sur schedule_round.xhtml.
     * Appelé depuis ScheduleRoundController.confirmRound() pour les non-membres.
     * La navigation vers cart.xhtml se fait séparément via goToCart().
     */
    public void addGreenfeeToCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setCreditcardType(GREENFEE());
            Greenfee gf = tarifGreenfeeController.completeGreenfee(memberController.getTarifGreenfee(), appContext.getClub(), appContext.getRound(), appContext.getPlayer());
            LOG.debug("Greenfee completed = {}", gf);
            if (gf.getPrice() == 0) {
                showMessageInfo("amount ZERO, no payment needed");
                return;
            }
            gf.setClubName(appContext.getClub()     != null ? appContext.getClub().getClubName()     : "");
            gf.setCourseName(appContext.getCourse() != null ? appContext.getCourse().getCourseName() : "");
            gf.setRoundHoles(appContext.getRound()  != null ? appContext.getRound().getRoundHoles()  : null);
            gf.setCourseId(appContext.getCourse()   != null ? appContext.getCourse().getIdcourse()   : null);
            gf.setRoundStart(appContext.getRound()  != null ? appContext.getRound().getRoundStart()  : null);
            gf.setRoundGame(appContext.getRound()   != null ? appContext.getRound().getRoundGame()   : null);
            cartController.getListGreenfees().add(gf);
            cartController.setGreenfee(gf);
            if (creditcard == null) creditcard = new Creditcard();
            creditcard = completeWithGreenfee(gf, appContext.getPlayer());
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("listGreenfees size={}, total={}", cartController.getListGreenfees().size(), creditcard.getTotalPrice());
            cartController.upsertCart();
            String dateStr = gf.getRoundDate() != null ? gf.getRoundDate().format(ZDF_TIME_HHmm) : "?";
            showMessageInfo("�o. Créneau du " + dateStr + " ajouté - "
                    + cartController.getListGreenfees().size() + " créneau(x) dans le panier. Cliquez « Paiement en ligne » pour régler.");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method addGreenfeeToCart

    /** Navigue vers cart.xhtml si le panier contient au moins un greenfee. */
    public String goToCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (!hasMixedCart()) {
            showMessageFatal("Panier vide - sélectionnez d'abord un créneau.");
            return null;
        }
        return "cart.xhtml?faces-redirect=true";
    } // end method goToCart

    public String manageSubscription() { // called from subscription.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            Subscription subscription = appContext.getSubscription();
            subscription.setIdplayer(appContext.getPlayer().getIdplayer());
            LOG.debug("subscription = {}", subscription);

            creditcard.setTypePayment(SUBSCRIPTION());
            LOG.debug("creditcard = {}", creditcard);
            appContext.setCreditcardType(SUBSCRIPTION());
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
                    creditcard = completeWithSubscription(subscription, appContext.getPlayer()); // migrated 2026-02-26 - was creditcardController
                    LOG.debug("creditcard completed with subscription = {}", creditcard);
                    cartController.upsertCart();
                    return "cart.xhtml?faces-redirect=true"; // goes to cart for MIXED support
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
            if (GREENFEE().equals(creditcard.getTypePayment())) {
                if (!creditcardMail.sendMailGreenfee(appContext.getPlayer(), creditcard, memberController.getTarifGreenfee(), appContext.getRound(), appContext.getInscription())) {
                    LOG.warn("sendMailGreenfee returned false");
                }
            } // end if
            if (SUBSCRIPTION().equals(creditcard.getTypePayment())) {
                if (!creditcardMail.sendMailSubscription(appContext.getPlayer(), creditcard, appContext.getSubscription())) {
                    LOG.warn("sendMailSubscription returned false");
                }
            } // end if
            if (COTISATION().equals(creditcard.getTypePayment())) {
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
        progress1 = 0;  // �?� reset pour la prochaine visite
        running = false; // new 09-03-2026
        try {
            creditcard.setTypePayment(appContext.getCreditcardType());
            savedType = creditcard.getTypePayment();
            if (!cartController.validateCartBeforePayment()) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().getExternalContext().redirect("cart.xhtml");
                FacesContext.getCurrentInstance().responseComplete();
                return;
            }
            LOG.debug("before payment creditcard = {}", creditcard);
            String v = httpController.sendPaymentServer(creditcard);
            LOG.debug("var v returned = {}", v);
            LOG.debug("creditcard returned = {}", creditcard);
            if (v.equals("200")) {
                String msg = "Payment validé par Amazone Payments Inc !";
                LOG.info(msg);
                LOG.debug("creditcardType in onCompletePayment = {}", appContext.getCreditcardType());

                // Store transaction snapshot in PaymentStateStore - architecture REST/JSF separation 2026-03-21
                String nonce = creditcard.getPaymentNonce(); // set by HttpController.sendPaymentServer
                payment.PaymentTransaction tx = new payment.PaymentTransaction(nonce);
                tx.setCreditcard(creditcard);
                tx.setPlayerId(appContext.getPlayer().getIdplayer());
                tx.setPlayer(appContext.getPlayer());
                tx.setSavedType(savedType);
                tx.setCreditcardType(appContext.getCreditcardType());
                tx.setSubscription(appContext.getSubscription());
                tx.setCotisation(appContext.getCotisation());
                tx.setGreenfee(cartController.getGreenfee());
                tx.setRound(appContext.getRound());
                // Reload club from DB to guarantee ZoneId is present (cache may be stale)
                Club txClub = appContext.getClub();
                if (txClub != null && txClub.getIdclub() != null && txClub.getIdclub() > 0
                        && (txClub.getAddress() == null || txClub.getAddress().getZoneId() == null || txClub.getAddress().getZoneId().isBlank())) {
                    try {
                        Club reloaded = readClubService.read(txClub);
                        if (reloaded != null) {
                            txClub = reloaded;
                            appContext.setClub(txClub);
                            LOG.debug("club reloaded for ZoneId: id={} zoneId={}", txClub.getIdclub(),
                                    txClub.getAddress() != null ? txClub.getAddress().getZoneId() : "null");
                        }
                    } catch (Exception reloadEx) {
                        LOG.warn("club reload failed, using cached club: {}", reloadEx.getMessage());
                    }
                }
                tx.setClub(txClub);
                tx.setCourse(appContext.getCourse());
                tx.setInscription(appContext.getInscription());
                tx.setListLessons(cartController.getListLessons());
                tx.setListGreenfees(cartController.getListGreenfees());
                tx.setProfessional(cartController.getProfessional());
                paymentStateStore.store(nonce, tx);
                LOG.debug("PaymentTransaction stored with nonce={}", nonce);

                String paymentServiceUrl = settings.getProperty("PAYMENT_SERVICE_URL");
                if (paymentServiceUrl == null || paymentServiceUrl.isBlank()) {
                    msg = "FATAL - PAYMENT_SERVICE_URL environment variable is not set - cannot redirect to payment server /about. "
                               + "Set PAYMENT_SERVICE_URL (e.g. https://127.0.0.1:5000) and restart WildFly.";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    return;
                }
                LOG.debug("before going with context to 5000/about");
                FacesContext context = FacesContext.getCurrentInstance();
                context.getExternalContext().redirect(paymentServiceUrl + "/about");
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

    // handlePayments - moved to rest.PaymentRestResource 2026-03-21

    //  utilisé dans creditcard_accepted.xhtml
    public void onStart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
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

    // ========================================
    // CART - generic helpers (cotisation + lesson branches) - 2026-04-22
    // ========================================

    public boolean isCotisation() {
     //   return etypePayment.COTISATION.toString().equals(appContext.getCreditcardType());
        return eTypePayment.COTISATION.equals(appContext.getCreditcardType());
    } // end method

    public boolean isLesson() {
        return LESSON().equals(appContext.getCreditcardType());
    } // end method

    public boolean isGreenfee() {
        return GREENFEE().equals(appContext.getCreditcardType());
    } // end method

    public boolean isSubscription() {
        return SUBSCRIPTION().equals(appContext.getCreditcardType());
    } // end method

    public boolean isMixed() {
        return "MIXED".equals(appContext.getCreditcardType());
    } // end method

    public double getGreenfeePrice() { return cartController.getGreenfeePrice(); } // end method

    public double getLessonPrice() { return cartController.getLessonPrice(); } // end method

    // ---- has*() - basés sur le contenu des listes (utilisés par cart.xhtml MIXED) ----
    public boolean hasGreenfees()    { return cartController.hasGreenfees(); }    // end method
    public boolean hasLessons()      { return cartController.hasLessons(); }      // end method
    public boolean hasCotisation()   { return cartController.hasCotisation(); }   // end method
    public boolean hasSubscription() { return cartController.hasSubscription(); } // end method
    public boolean hasMixedCart()    { return cartController.hasMixedCart(); }    // end method

    public double getTotalCartPrice() { return cartController.getTotalCartPrice(); } // end method

    public String payMixedCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setCreditcardType("MIXED");
            creditcard = prefilling(appContext.getPlayer());
            creditcard.setPaymentOK(false);
            creditcard.setTotalPrice(cartController.getTotalCartPrice());
            creditcard.setCommunication(cartController.buildMixedCommunication());
            creditcard.setTypePayment("MIXED");
            LOG.debug("MIXED creditcard total={}", creditcard.getTotalPrice());
            return "creditcard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public List<Greenfee> getListGreenfees() { return cartController.getListGreenfees(); } // end method

    public void removeSubscriptionItem() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.removeSubscriptionItem();
    } // end method

    public void removeGreenfeeItem(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.getListGreenfees().remove(gf);
        if (creditcard != null) {
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
        }
        cartController.upsertCart();
        LOG.debug("greenfee removed, list size={}", cartController.getListGreenfees().size());
    } // end method

    public java.util.List<entite.EquipmentsAndBasicAndRange> getCotisationBasicCart() {
        return cartController.getCotisationBasicCart();
    } // end method

    public java.util.List<entite.EquipmentsAndBasic> getCotisationEquipCart() {
        return cartController.getCotisationEquipCart();
    } // end method

    public void removeCotisationBasicItem(entite.EquipmentsAndBasicAndRange item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.removeCotisationBasicItem(item);
    } // end method

    public void removeCotisationEquipmentItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.removeCotisationEquipmentItem(item);
    } // end method

    public void deleteLesson() { // used in cart.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.getListLessons().remove(cartController.getSelectedLesson());
        String msg = "Lesson removed = " + cartController.getSelectedLesson();
        cartController.setSelectedLesson(null);
        LOG.info(msg);
        showMessageInfo(msg);
        creditcard.setTotalPrice(cartController.getListLessons().stream()
                .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                .sum());
        msg = "recalculated totalPrice is now " + creditcard.getTotalPrice();
        LOG.info(msg);
        showMessageInfo(msg);
        cartController.upsertCart();
        org.primefaces.PrimeFaces.current().ajax().update("form_cart:growl-msg", "form_cart:listLessons", "form_cart:messages");
    } // end method

    /** Called from SchedulerProController - adds a pending lesson to the cart. */
    public void addLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.addLesson(lesson);
    } // end method

    /** Called from SchedulerProController - removes a pending lesson from the cart. */
    public void removeLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.removeLesson(lesson);
    } // end method

    /** Called from SchedulerProController on drag-drop - updates a pending lesson in the cart. */
    public void updatePendingLesson(Lesson before, Lesson after) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.updatePendingLesson(before, after);
    } // end method

    public String to_creditcard_test_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}",methodName, s);
        creditcard.setTotalPrice(155.6);
        creditcard.setCommunication(" prepared creditcard communication");
        creditcard.setCreditcardType(enumeration.CreditcardBrand.VISA);
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
        creditcard.setCreditcardType((enumeration.CreditcardBrand) e.getNewValue());
        if (!Objects.equals(creditcard.getCreditcardIssuer(), creditcard.getCreditcardType())) {
            String msg = "WARNING !!! "
                    + " <br/> Issuer detected = " + creditcard.getCreditcardIssuer()
                    + " <br/> Card Type data in = " + creditcard.getCreditcardType();
            LOG.debug(msg);
            showMessageInfo(msg);
        } // end if
    } // end method

    // ========================================
    // TEST METHODS - migrées depuis CourseController 2026-02-25
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
            creditcard.setCreditcardType(enumeration.CreditcardBrand.VISA);
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
            c.setCreditcardType(enumeration.CreditcardBrand.VISA);
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
    // PROGRESS BAR - migrée depuis CourseController 2026-02-25
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
    // METHODES METIER PRIVEES - migrées depuis CreditcardController 2026-02-26
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
         //   cc.setTypePayment(eTypePayment.GREENFEE.toString());
            cc.setTypePayment(eTypePayment.GREENFEE());
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
            double total = lessons.stream()
                    .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                    .sum();
            if (total == 0) {
                LOG.debug("Amount ZERO no payment Lesson needed !");
                return null;
            } // end if
            Creditcard cc = prefilling(player);
            LOG.debug("creditcard after prefilling = {}", cc);
            cc.setPaymentOK(false);
            cc.setTotalPrice(total);
            java.time.format.DateTimeFormatter fmtDate = DTF_DAY_MONTH;
            java.time.format.DateTimeFormatter fmtTime = ZDF_HOURS;
            StringBuilder s = new StringBuilder("Lesson ");
            for (int i = 0; i < lessons.size(); i++) {
                if (i > 0) s.append(",");
                s.append(lessons.get(i).getEventStartDate().format(fmtDate))
                 .append(" ")
                 .append(lessons.get(i).getEventStartDate().format(fmtTime))
                 .append("�?'")
                 .append(lessons.get(i).getEventEndDate().format(fmtTime));
            } // end for
            s.append(" #").append(professional.getProPlayerId());
            String comm = s.length() <= 140 ? s.toString() : s.substring(0, 137) + "...";
            cc.setCommunication(comm);
            cc.setTypePayment(LESSON());
            cc.setCreditcardCurrency(resolveClubCurrency());
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
            cc.setTypePayment(COTISATION());
            cc.setCreditcardCurrency(resolveClubCurrency());
            LOG.debug("creditcard completed with cotisation = {}", cc);
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithCotisation

    private String resolveClubCurrency() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            entite.Club club = appContext.getClub();
            if (club == null || club.getAddress() == null || club.getAddress().getCountry() == null)
                return "EUR";
            return club.getAddress().getCountry().getCurrency();
        } catch (Exception e) {
            LOG.warn("resolveClubCurrency failed, defaulting to EUR: {}", e.getMessage());
            return "EUR";
        }
    } // end method resolveClubCurrency

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
            cc.setTypePayment(SUBSCRIPTION());
            LOG.debug("creditcard completed with subscription = {}", cc);
            return cc;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method completeWithSubscription

    // needsUpdate - moved to PaymentManager.needsUpdate 2026-03-21
    // handlePaymentSubscription - moved to PaymentOrchestrator/SubscriptionRegistrar 2026-03-21

    // ========================================
    // JAX-RS ENDPOINTS - moved to rest.PaymentRestResource 2026-03-21
    // Architecture separation: REST callbacks are now @RequestScoped
    // and use PaymentStateStore instead of @SessionScoped state.
    // ========================================

    // ========================================
    // PAYMENT COMPLETION - sync REST result back to JSF session
    // Called via f:viewParam/preRenderView on creditcard_payment_executed.xhtml
    // ========================================

    /**
     * Called by preRenderView on creditcard_payment_executed.xhtml and
     * creditcard_payment_canceled.xhtml. Retrieves the completed PaymentTransaction
     * from PaymentStateStore and syncs creditcard data back to the JSF session.
     */
    public void onPaymentCompleted() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("completionNonce={}", completionNonce);
        if (completionNonce == null || completionNonce.isEmpty()) {
            LOG.debug("no nonce, skipping sync (direct JSF navigation)");
            return;
        }
        // Vérifie d'abord l'éventuel message d'erreur propagé par PaymentRestResource
        payment.PaymentTransaction errorTx = paymentStateStore.get(completionNonce);
        if (errorTx != null && errorTx.getErrorMessage() != null) {
            LOG.error("payment error propagated to JSF: {}", errorTx.getErrorMessage());
            showMessageFatal(errorTx.getErrorMessage());
            errorTx.setErrorMessage(null);   // one-shot

            // Items traités avec succès AVANT l'erreur : afficher leurs messages + envoyer les mails
            if (!errorTx.getPendingInfoMessages().isEmpty()) {
                LOG.debug("partial success: {} item(s) processed before error - notifying user", errorTx.getPendingInfoMessages().size());
                for (String msg : errorTx.getPendingInfoMessages()) {
                    showMessageInfo(msg);
                }
                this.creditcard = errorTx.getCreditcard();
                this.savedType  = errorTx.getSavedType();
                if (errorTx.getListGreenfees() != null && !errorTx.getListGreenfees().isEmpty()) {
                    cartController.setListGreenfees(errorTx.getListGreenfees());
                }
                if (errorTx.getListLessons() != null && !errorTx.getListLessons().isEmpty()) {
                    cartController.setListLessons(errorTx.getListLessons());
                }
                sendPaymentMails(errorTx);
                sendLessonMails();
            }

            paymentStateStore.remove(completionNonce);
            return;
        }
        payment.PaymentTransaction tx = paymentStateStore.consume(completionNonce);
        if (tx == null) {
            LOG.warn("transaction not found for nonce={} - syncing skipped, sending mails from session", completionNonce);
            cartController.markCartCompleted(savedType);
            // Afficher les messages success stockés par REST dans la tx (encore accessible via get())
            if (errorTx != null) {
                for (String msg : errorTx.getPendingInfoMessages()) {
                    showMessageInfo(msg);
                }
            }
            sendPaymentMails(errorTx);
            sendLessonMails();
            // Inscription auto faite côté REST - confirme N fois (une par greenfee)
            if (this.creditcard != null) {
                String t = this.creditcard.getTypePayment();
                if ("GREENFEE".equals(t) || ("MIXED".equals(t) && !cartController.getListGreenfees().isEmpty())) {
                    int count = cartController.getListGreenfees().isEmpty() ? 1 : cartController.getListGreenfees().size();
                    for (int i = 0; i < count; i++) {
                        showMessageInfo(utils.LCUtil.prepareMessageBean("inscription.confirmation.mail"));
                    }
                }
            }
            return;
        }
        // Sync creditcard data from the transaction back to the JSF session
        this.creditcard = tx.getCreditcard();
        this.savedType = tx.getSavedType();
        // Restore menu visibility - may have been lost if session was recreated during payment flow
        if (appContext.getPlayer() != null) {
            appContext.getPlayer().setShowMenu(true);
            LOG.debug("showMenu restored to true");
        }
        LOG.debug("creditcard synced from PaymentTransaction, nonce={}", completionNonce);
        // ========================================
        // Process PENDING cart rows - inserts into payments_xxx + delete cart rows
        // ========================================
        try {
            int playerId = tx.getPlayerId();
            int clubId   = tx.getClub() != null ? tx.getClub().getIdclub() : 0;
            entite.Player txPlayer = tx.getPlayer() != null ? tx.getPlayer() : appContext.getPlayer();

            if (playerId > 0 && clubId > 0 && txPlayer != null) {
                java.util.List<entite.Cart> carts = findCartService.findAllPending(playerId, clubId);
                LOG.debug("cart rows PENDING count={}", carts.size());

                payment.PaymentOrchestrator sharedOrchestrator = new payment.PaymentOrchestrator(
                    this.creditcard, txPlayer,
                    tx.getRound(), tx.getClub(), tx.getCourse(), tx.getInscription(),
                    paymentSubscriptionController, paymentGreenfeeController,
                    paymentCotisationController, paymentLessonController);

                for (entite.Cart cart : carts) {
                    String cartType = cart.getCartType().name();
                    String json = cart.getCartItemsJson();
                    LOG.debug("processing cart type={}", cartType);
                    try {
                        switch (cartType) {
                        case "COTISATION" -> {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                            entite.Cotisation cot = new entite.Cotisation();
                            if (m.get("idplayer")      instanceof Number n) cot.setIdplayer(n.intValue());
                            if (m.get("idclub")        instanceof Number n) cot.setIdclub(n.intValue());
                            if (m.get("total")         instanceof Number n) cot.setPrice(n.doubleValue());
                            if (m.get("type")          instanceof String s) cot.setType(s);
                            if (m.get("communication") instanceof String s) cot.setCommunication(s);
                            if (m.get("items")         instanceof String s) cot.setItems(s);
                            if (m.get("startDate")     instanceof String s) cot.setCotisationStartDate(java.time.LocalDateTime.parse(s));
                            if (m.get("endDate")       instanceof String s) cot.setCotisationEndDate(java.time.LocalDateTime.parse(s));
                            cot.setStatus(m.get("status") instanceof String s ? s : "Y");
                            sharedOrchestrator.handle(new payment.CotisationPayment(cot));
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                            LOG.debug("COTISATION payment done and cart row deleted");
                        }
                        case "SUBSCRIPTION" -> {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                            entite.Subscription sub = new entite.Subscription();
                            if (m.get("subCode")       instanceof String s) sub.setSubCode(s);
                            if (m.get("amount")        instanceof Number n) sub.setSubscriptionAmount(n.doubleValue());
                            if (m.get("communication") instanceof String s) sub.setCommunication(s);
                            sub.setIdplayer(playerId);
                            entite.Subscription subComplete = paymentSubscriptionController.complete(sub);
                            if (subComplete != null) sub = subComplete;
                            sharedOrchestrator.handle(new payment.SubscriptionPayment(sub));
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                            showMessageInfo(utils.LCUtil.prepareMessageBean("subscription.success") + sub);
                            LOG.debug("SUBSCRIPTION payment done and cart row deleted");
                        }
                        case "LESSON" -> {
                            com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Lesson>> lessonRef =
                                new com.fasterxml.jackson.core.type.TypeReference<>() {};
                            java.util.List<entite.Lesson> lessons = OBJECT_MAPPER.readValue(json, lessonRef);
                            sharedOrchestrator.handle(new payment.LessonPayment(lessons, tx.getProfessional()));
                            tx.setListLessons(lessons);
                            cartController.setListLessons(lessons);
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                            showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.success"));
                            LOG.debug("LESSON payment done and cart row deleted");
                        }
                        case "GREENFEE" -> {
                            com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Greenfee>> gfRef =
                                new com.fasterxml.jackson.core.type.TypeReference<>() {};
                            java.util.List<entite.Greenfee> greenfees = OBJECT_MAPPER.readValue(json, gfRef);
                            java.util.List<entite.Greenfee> gfDone = new java.util.ArrayList<>();
                            for (entite.Greenfee gf : greenfees) {
                                if (findGreenfeePaid.findByCartKeys(gf.getIdplayer(), gf.getRoundDate(), gf.getIdclub())) {
                                    LOG.warn("GREENFEE idempotency - already in DB player={} club={} date={}",
                                            gf.getIdplayer(), gf.getIdclub(), gf.getRoundDate());
                                    gfDone.add(gf);
                                    continue;
                                }
                                entite.Round rc = new entite.Round();
                                rc.setRoundDate(gf.getRoundDate());
                                rc.setRoundHoles(gf.getRoundHoles());
                                rc.setRoundStart(gf.getRoundStart());
                                rc.setRoundGame(gf.getRoundGame());
                                entite.Course gfCourse = new entite.Course();
                                gfCourse.setIdcourse(gf.getCourseId());
                                payment.PaymentOrchestrator gfOrchestrator = new payment.PaymentOrchestrator(
                                    this.creditcard, txPlayer, rc, tx.getClub(), gfCourse, tx.getInscription(),
                                    paymentSubscriptionController, paymentGreenfeeController,
                                    paymentCotisationController, paymentLessonController);
                                gfOrchestrator.handle(new payment.GreenfeePayment(gf));
                                gfDone.add(gf);
                                showMessageInfo(utils.LCUtil.prepareMessageBean("greenfee.success") + gf);
                            }
                            tx.setListGreenfees(gfDone);
                            cartController.setListGreenfees(gfDone);
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                            LOG.debug("GREENFEE payment done count={} and cart row deleted", gfDone.size());
                        }
                        default -> LOG.warn("unknown cart type={} - skipped", cartType);
                        }
                    } catch (Exception itemEx) {
                        String errMsg = "[" + cartType + "] "
                            + (itemEx.getMessage() != null ? itemEx.getMessage() : itemEx.getClass().getSimpleName());
                        LOG.error("item processing failed type={} - continuing to next item", cartType, itemEx);
                        showMessageFatal(errMsg);
                    }
                }
            } else {
                LOG.warn("cart processing skipped playerId={} clubId={} playerNull={}", playerId, clubId, txPlayer == null);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }

        cartController.markCartCompleted(savedType);  // safety net: no-op if rows already deleted above
        sendPaymentMails(tx);
        sendLessonMails();

        // GREENFEE - find-or-create round pour chaque greenfee après paiement
        if ("GREENFEE".equals(this.savedType)) {
            Club club = tx.getClub() != null ? tx.getClub() : appContext.getClub();
            java.util.List<Greenfee> gfList = tx.getListGreenfees();
            if (gfList != null && !gfList.isEmpty()) {
                for (Greenfee gf : gfList) {
                    Round rc = new Round();
                    rc.setRoundDate(gf.getRoundDate());
                    rc.setRoundHoles(gf.getRoundHoles());
                    rc.setRoundStart(gf.getRoundStart());
                    rc.setRoundGame(gf.getRoundGame());
                    Course course = new Course();
                    course.setIdcourse(gf.getCourseId());
                    processOneGreenfeePostPayment(rc, course, club);
                }
            } else {
                processOneGreenfeePostPayment(
                    tx.getRound()  != null ? tx.getRound()  : appContext.getRound(),
                    tx.getCourse() != null ? tx.getCourse() : appContext.getCourse(),
                    club
                );
            }
        }

        // MIXED - post-payment pour chaque greenfee de la liste
        if ("MIXED".equals(this.savedType) && tx.getListGreenfees() != null) {
            Club club = tx.getClub() != null ? tx.getClub() : appContext.getClub();
            for (Greenfee gf : tx.getListGreenfees()) {
                Round rc = new Round();
                rc.setRoundDate(gf.getRoundDate());
                rc.setRoundHoles(gf.getRoundHoles());
                rc.setRoundStart(gf.getRoundStart());
                rc.setRoundGame(gf.getRoundGame());
                Course course = new Course();
                course.setIdcourse(gf.getCourseId());
                processOneGreenfeePostPayment(rc, course, club);
            }
        }

        // COTISATION - message détaillé avec player, période, club depuis snapshot tx
        entite.Cotisation txCot  = tx.getCotisation() != null ? tx.getCotisation() : appContext.getCotisation();
        entite.Player     cotPl  = tx.getPlayer()     != null ? tx.getPlayer()     : appContext.getPlayer();
        Club              cotClub = tx.getClub()      != null ? tx.getClub()       : appContext.getClub();
        if (("COTISATION".equals(this.savedType) || "MIXED".equals(this.savedType))
                && txCot != null && cotPl != null && cotClub != null) {
            String startStr = txCot.getCotisationStartDate() != null ? txCot.getCotisationStartDate().format(DTF_DAY_SLASH) : "?";
            String endStr   = txCot.getCotisationEndDate()   != null ? txCot.getCotisationEndDate().format(DTF_DAY_SLASH)   : "?";
            showMessageInfo(utils.LCUtil.prepareMessageBean("cotisation.confirmed") + " "
                    + cotPl.getPlayerFirstName() + " " + cotPl.getPlayerLastName()
                    + " — " + cotClub.getClubName()
                    + " — " + startStr + " → " + endStr);
        }

        // LESSON - message détaillé avec player, pro, club depuis snapshot tx
        java.util.List<entite.Lesson> txLessons = tx.getListLessons() != null ? tx.getListLessons() : cartController.getListLessons();
        entite.Player lesPl = tx.getPlayer() != null ? tx.getPlayer() : appContext.getPlayer();
        if (("LESSON".equals(this.savedType) || "MIXED".equals(this.savedType))
                && txLessons != null && !txLessons.isEmpty() && lesPl != null) {
            entite.Lesson first = txLessons.get(0);
            String proName  = first.getProName()       != null ? first.getProName()       : "";
            String clubName = first.getEventClubName() != null ? first.getEventClubName() : "";
            String count    = txLessons.size() > 1 ? " (" + txLessons.size() + "x)" : "";
            showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.confirmed") + " "
                    + lesPl.getPlayerFirstName() + " " + lesPl.getPlayerLastName()
                    + " — " + proName
                    + " — " + clubName + count);
        }
    } // end method

    /** Find-or-create round + auto-inscription for one greenfee after successful payment. */
    private void processOneGreenfeePostPayment(Round roundCandidate, Course course, Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (roundCandidate == null || course == null || club == null) {
            LOG.warn("GREENFEE post-payment: round/course/club missing - cannot create round");
            return;
        }
        try {
            Round existing = findRoundBySlot.find(course.getIdcourse(), roundCandidate.getRoundDate(),
                    java.time.ZoneId.of(club.getAddress().getZoneId()));
            Round ready;
            if (existing != null) {
                cacheInvalidator.invalidateParticipantsRound();
                int inscrits = participantsRoundList.list(existing).size();
                if (inscrits >= 4) {
                    LOG.error("GREENFEE post-payment: slot already full (inscrits={})", inscrits);
                    showMessageFatal("Créneau complet après paiement - contactez le support.");
                    return;
                }
                ready = existing;
                LOG.info("GREENFEE: reusing existing round idround={}", ready.getIdround());
            } else {
                manager.RoundManager.SaveResult res = roundManager.createRound(
                        roundCandidate, course, club, new UnavailablePeriod());
                if (!res.isSuccess()) {
                    LOG.error("GREENFEE post-payment: round creation failed - {}", res.getMessage());
                    showMessageFatal("Round creation failed: " + res.getMessage());
                    return;
                }
                ready = roundCandidate;
                LOG.info("GREENFEE: round created idround={}", ready.getIdround());
            }
            appContext.setRound(ready);
            Player currentPlayer = appContext.getPlayer();
            if (currentPlayer != null && !findInscriptionRound.find(ready, currentPlayer)) {
                Inscription auto = new Inscription();
                auto.setInscriptionTeeStart(null); // tee différé
                Inscription r = roundManager.createInscription(ready, currentPlayer, currentPlayer, auto, club, course, "A");
                if (r == null || r.isInscriptionError()) {
                    LOG.warn("GREENFEE auto-inscription failed - user can retry via Register Score");
                } else {
                    LOG.info("GREENFEE auto-inscription created (no tee) player={} round={}", currentPlayer.getIdplayer(), ready.getIdround());
                    showMessageInfo(utils.LCUtil.prepareMessageBean("inscription.confirmation.mail"));
                }
            } else {
                LOG.debug("GREENFEE: player already inscribed - skip auto-inscription");
                showMessageInfo(utils.LCUtil.prepareMessageBean("inscription.confirmation.mail"));
            }
        } catch (SQLException e) {
            handleSQLException(e, methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /**
     * Sends lesson payment confirmation mails using session data.
     * Called by preRenderView on creditcard_payment_executed.xhtml.
     * Independent of PaymentStateStore - uses session fields directly.
     * Guard lessonMailsSent prevents double-send on page refresh.
     */
    public void sendLessonMails() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering");
        if (lessonMailsSent) {
            LOG.debug("mails already sent - skipping");
            return;
        }
        if (cartController.getListLessons() == null || cartController.getListLessons().isEmpty()) {
            LOG.debug("no lessons in session - not a lesson payment");
            return;
        }
        if (cartController.getProfessional() == null) {
            LOG.warn("professional is null - lesson mails not sent");
            return;
        }
        try {
            lessonMail.sendPaymentConfirmation(appContext.getPlayer(), cartController.getProfessional(), cartController.getListLessons(), creditcard);
            lessonMail.sendProNotification(appContext.getPlayer(), cartController.getProfessional(), cartController.getListLessons(), creditcard);
            lessonMailsSent = true;
            LOG.info("lesson mails enqueued for player={}", appContext.getPlayer().getIdplayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /**
     * Sends confirmation mails for GREENFEE, SUBSCRIPTION and COTISATION payments.
     * LESSON mails are handled separately by sendLessonMails().
     * Non-fatal - errors are logged and swallowed.
     */
    private void sendPaymentMails(payment.PaymentTransaction tx) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (creditcard == null || appContext.getPlayer() == null) return;
        String type = savedType != null ? savedType : creditcard.getTypePayment();
        if (type == null) return;
        boolean mixed = "MIXED".equals(type);
        entite.Player player = (tx != null && tx.getPlayer() != null) ? tx.getPlayer() : appContext.getPlayer();
        Club          club   = (tx != null && tx.getClub()   != null) ? tx.getClub()   : appContext.getClub();
        try {
            if ("COTISATION".equals(type) || (mixed && hasCotisation())) {
                entite.Cotisation cot = (tx != null && tx.getCotisation() != null) ? tx.getCotisation() : appContext.getCotisation();
                if (cot != null) {
                    creditcardMail.sendMailCotisation(player, creditcard, cot, club, memberController.getTarifMember());
                    LOG.info("cotisation mail enqueued player={}", player.getIdplayer());
                }
            }
            if ("SUBSCRIPTION".equals(type) || (mixed && hasSubscription())) {
                entite.Subscription sub = (tx != null && tx.getSubscription() != null) ? tx.getSubscription() : appContext.getSubscription();
                if (sub != null) {
                    creditcardMail.sendMailSubscription(player, creditcard, sub);
                    LOG.info("subscription mail enqueued player={}", player.getIdplayer());
                }
            }
            if ("GREENFEE".equals(type) || (mixed && hasGreenfees())) {
                java.util.List<Greenfee> gfList = (tx != null && tx.getListGreenfees() != null) ? tx.getListGreenfees() : cartController.getListGreenfees();
                for (Greenfee gf : gfList) {
                    creditcardMail.sendMailGreenfee(player, creditcard, gf, club);
                }
                LOG.info("greenfee mail(s) enqueued player={} count={}", player.getIdplayer(), gfList.size());
            }
        } catch (Exception e) {
            LOG.warn("sendPaymentMails non-fatal error", e);
        }
    } // end method

    public String getCompletionNonce() { return completionNonce; } // end method
    public void setCompletionNonce(String completionNonce) { this.completionNonce = completionNonce; } // end method

    // ========================================
    // CART PERSISTENCE - 2026-05-07
    // ========================================

    /**
     * Compteur badge panier - délégué à CartController.
     */
    public int getCartBadgeCount() {
        return cartController.getCartBadgeCount();
    } // end method

    /**
     * Restaure le panier depuis la DB au moment de l'identification du joueur.
     */
    public void initCartOnLogin() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.initCartOnLogin();
    } // end method

    public void onCartLoad() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.onCartLoad();
    } // end method

    public String restoreCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return cartController.restoreCartFromDb();
    } // end method

    public void clearCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.clearCartFromDb();
    } // end method

    public String clearCartAndExit() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return cartController.clearCartAndExit();
    } // end method

    // ========================================
    // PAYMENT TYPE - titre et include résolu côté bean
    // ========================================

    public String getPaymentTitle() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        String type = appContext.getCreditcardType();
        if (type == null) return "";
        return switch (type) {
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
        String type = appContext.getCreditcardType();
        if (type == null) return "include/include_empty.xhtml";
        return switch (type) {
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

    public Player getPlayerPro() {
        return playerPro;
    } // end method

    public void setPlayerPro(Player playerPro) {
        this.playerPro = playerPro;
    } // end method

} // end class

