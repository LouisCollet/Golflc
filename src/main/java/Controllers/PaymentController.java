package Controllers;

import context.ApplicationContext;
import entite.*;
import enumeration.eTypePayment;
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
import static enumeration.eTypePayment.COTISATION;
import static enumeration.eTypePayment.GREENFEE;
import static enumeration.eTypePayment.LESSON;
import static enumeration.eTypePayment.SUBSCRIPTION;
import java.util.Objects;
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
    @Inject private cache.CacheInvalidator                       cacheInvalidator;
    @Inject private read.ReadCreditcard                          readCreditcard;       // migrated 2026-02-26 — was CreditcardController
    @Inject private payment.PaymentSubscriptionController       paymentSubscriptionController;
    @Inject private Controllers.TarifMemberController           tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController         tarifGreenfeeController;
    @Inject private Controllers.MemberController          memberController;
    // SchedulerProController removed — @ViewScoped can't be injected in @SessionScoped. Data via appContext. // 2026-03-22
    @Inject private create.CreateLesson                         createLesson;
    @Inject private create.CreatePaymentLesson                  createPaymentLesson; // payments_lesson — 2026-03-29
    @Inject private find.FindRoundBySlot                        findRoundBySlot;
    @Inject private find.FindInscriptionRound                   findInscriptionRound;
    @Inject private lists.ParticipantsRoundList                 participantsRoundList;
    @Inject private manager.RoundManager                        roundManager;
    @Inject private manager.PlayerManager                       playerManager;
    @Inject private read.ReadClub                               readClubService;
    @Inject private Controllers.HttpController                  httpController;
    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext
    @Inject private mail.CreditcardMail                         creditcardMail;  // migrated 2026-02-26
    @Inject private mail.LessonMail                             lessonMail;
    @Inject private entite.Settings                             settings;        // security audit 2026-03-18
    @Inject private payment.PaymentStateStore                   paymentStateStore; // architecture REST/JSF separation 2026-03-21
    @Inject private create.CreateCart                           createCartService;      // cart persistence 2026-05-07
    @Inject private find.FindCart                               findCartService;        // cart persistence 2026-05-07
    @Inject private update.UpdateCartStatus                     updateCartStatusService; // cart persistence 2026-05-07
    @Inject private delete.DeleteCart                           deleteCartService;      // cart persistence 2026-05-07
    @Inject private find.FindCotisationOverlapping              findCotisationOverlapping;
    @Inject private find.FindSubscriptionOverlapping            findSubscriptionOverlapping;
    @Inject private find.FindGreenfeePaid                       findGreenfeePaid;
    @Inject private find.FindLessonBooked                       findLessonBooked;

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private Creditcard    creditcard;
    private String        savedType;
    private Integer       progress1 = 0;
    private Greenfee        greenfee;
    private List<Greenfee>  listGreenfees = new ArrayList<>();
    private Professional    professional;
    private List<Lesson>    listLessons = new ArrayList<>();
    private Lesson        selectedLesson;
    private Player        playerPro;
    private boolean running = false;  // new 09-03-2026
    private boolean proFree = false;  // true when student is also an active pro → free lesson
    private String completionNonce;   // architecture REST/JSF separation 2026-03-21
    private boolean lessonMailsSent = false; // guard — prevents double-send on page refresh
    private boolean sessionRestored  = false; // true once restoreSessionFromDb() ran
    private java.util.List<entite.EquipmentsAndBasicAndRange> restoredBasicCart = new ArrayList<>();
    private java.util.List<entite.EquipmentsAndBasic>         restoredEquipCart = new ArrayList<>();

    public PaymentController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        creditcard    = new Creditcard();
        greenfee      = new Greenfee();
        listGreenfees = new ArrayList<>();
        progress1     = 0;
        LOG.debug("PaymentController initialized");
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        creditcard    = new Creditcard();
        savedType     = null;
        progress1     = 0;
        greenfee      = new Greenfee();
        listGreenfees = new ArrayList<>();
        professional  = null;
        listLessons   = new ArrayList<>();
        selectedLesson  = null;
        playerPro       = null;
        completionNonce   = null;
        restoredBasicCart = new ArrayList<>();
        restoredEquipCart = new ArrayList<>();
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
            creditcard = completeWithCotisation(cotisation, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            if (creditcard != null) {
                String msg = "creditcard completed with Cotisation ! ";
                LOG.info(msg);
                upsertCart();
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

            professional = appContext.getProfessional();
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
                for (Lesson lesson : listLessons) {
                    if (!createLesson.create(lesson, appContext.getPlayer())) {
                        showMessageFatal("error: free lesson not registered");
                        return null;
                    }
                    LOG.info("free lesson created: {}", lesson.getEventTitle());
                } // end for
                listLessons.clear();
                cacheInvalidator.invalidateProfessionalCaches();
                showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.pro.free"));
                return null; // stay on schedule_pro.xhtml
            } // end if Professional

            listLessons.forEach(item -> LOG.debug("lesson start date: {}", item.getEventStartDate()));
            boolean anyPriceConfigured = listLessons.stream()
                    .anyMatch(l -> l.getLessonAmount() != null && l.getLessonAmount() > 0);
            if (!anyPriceConfigured) {
                LOG.warn("lesson tarif not configured for proId={}", professional.getProId());
                showMessageFatal(utils.LCUtil.prepareMessageBean("lesson.tarif.unknown"));
                listLessons.clear();
                return null;
            } // end if
            creditcard = completeWithLesson(professional, listLessons, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            if (creditcard != null) {
                LOG.info("creditcard with lesson = {}", creditcard);
                upsertCart();
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
            clearCartFromDb();
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
        setCanceledCart();
        listLessons.clear();
        proFree = false;
        if (isGreenfee()) {
            return "schedule_round.xhtml?faces-redirect=true";
        }
        return "schedule_pro.xhtml?faces-redirect=true";
    } // end method

    public String manageGreenfee() { // called from price_round_greenfee.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with creditcard = {}", creditcard);
            LOG.debug("with greenfee = {}", greenfee);

            appContext.setCreditcardType(GREENFEE());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            // 1. complete greenfee with price — use memC.tarifGreenfee (loaded by findTarifGreenfee)
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
            listGreenfees.add(gf);
            greenfee = gf; // kept for backward compat
            LOG.debug("listGreenfees size={}", listGreenfees.size());
            // 3. complete creditcard with running total
            creditcard = completeWithGreenfee(gf, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
            creditcard.setTotalPrice(listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("creditcard Greenfee total = {}", creditcard.getTotalPrice());
            upsertCart();
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
            listGreenfees.add(gf);
            greenfee = gf;
            if (creditcard == null) creditcard = new Creditcard();
            creditcard = completeWithGreenfee(gf, appContext.getPlayer());
            creditcard.setTotalPrice(listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("listGreenfees size={}, total={}", listGreenfees.size(), creditcard.getTotalPrice());
            upsertCart();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String dateStr = gf.getRoundDate() != null ? gf.getRoundDate().format(fmt) : "?";
            showMessageInfo("✅ Créneau du " + dateStr + " ajouté — "
                    + listGreenfees.size() + " créneau(x) dans le panier. Cliquez « Paiement en ligne » pour régler.");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method addGreenfeeToCart

    /** Navigue vers cart.xhtml si le panier contient au moins un greenfee. */
    public String goToCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (!hasMixedCart()) {
            showMessageFatal("Panier vide — sélectionnez d'abord un créneau.");
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
                    creditcard = completeWithSubscription(subscription, appContext.getPlayer()); // migrated 2026-02-26 — was creditcardController
                    LOG.debug("creditcard completed with subscription = {}", creditcard);
                    upsertCart();
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
        progress1 = 0;  // ← reset pour la prochaine visite
        running = false; // new 09-03-2026
        try {
            creditcard.setTypePayment(appContext.getCreditcardType());
            savedType = creditcard.getTypePayment();
            if (!validateCartBeforePayment()) {
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
                tx.setListLessons(listLessons);
                tx.setListGreenfees(listGreenfees);
                tx.setProfessional(professional);
                paymentStateStore.store(nonce, tx);
                LOG.debug("PaymentTransaction stored with nonce={}", nonce);

                String paymentServiceUrl = settings.getProperty("PAYMENT_SERVICE_URL");
                if (paymentServiceUrl == null || paymentServiceUrl.isBlank()) {
                    msg = "FATAL — PAYMENT_SERVICE_URL environment variable is not set — cannot redirect to payment server /about. "
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

    // handlePayments — moved to rest.PaymentRestResource 2026-03-21

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
    // CART — generic helpers (cotisation + lesson branches) — 2026-04-22
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

    public double getGreenfeePrice() {
        return listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum();
    } // end method

    // ---- has*() — basés sur le contenu des listes (utilisés par cart.xhtml MIXED) ----
    public boolean hasGreenfees()    { return !listGreenfees.isEmpty(); } // end method
    public boolean hasLessons()      { return !listLessons.isEmpty(); }   // end method
    public boolean hasCotisation() {
        String type = appContext.getCreditcardType();
        if (!"COTISATION".equals(type) && !"MIXED".equals(type)) return false;
        return !getCotisationBasicCart().isEmpty() || !getCotisationEquipCart().isEmpty()
            || (appContext.getCotisation() != null && appContext.getCotisation().getPrice() > 0);
    } // end method
    public boolean hasSubscription() {
        // subCode null = subscription du login (accès), pas un item du panier
        return appContext.getSubscription() != null
            && appContext.getSubscription().getSubCode() != null
            && appContext.getSubscription().getSubscriptionAmount() > 0;
    } // end method
    public boolean hasMixedCart()    { return hasGreenfees() || hasLessons() || hasCotisation() || hasSubscription(); } // end method

    public double getTotalCartPrice() {
        double total = listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum();
        total += listLessons.stream().mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0).sum();
        if (hasCotisation() && appContext.getCotisation() != null)
            total += appContext.getCotisation().getPrice();
        if (hasSubscription() && appContext.getSubscription() != null)
            total += appContext.getSubscription().getSubscriptionAmount();
        return total;
    } // end method

    private String buildMixedCommunication() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        StringBuilder sb = new StringBuilder();
        if (hasCotisation() && appContext.getCotisation() != null) {
            String c = appContext.getCotisation().getCommunication();
            if (c != null && !c.isBlank()) sb.append(c);
        }
        if (hasSubscription() && appContext.getSubscription() != null) {
            String c = appContext.getSubscription().getCommunication();
            if (c != null && !c.isBlank()) { if (sb.length() > 0) sb.append(" | "); sb.append(c); }
        }
        if (!listLessons.isEmpty()) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm");
            StringBuilder ls = new StringBuilder("Lesson ");
            for (int i = 0; i < listLessons.size(); i++) {
                if (i > 0) ls.append(",");
                ls.append(listLessons.get(i).getEventStartDate().format(fmt));
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(ls);
        }
        if (!listGreenfees.isEmpty()) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm");
            StringBuilder gs = new StringBuilder("GF ");
            for (int i = 0; i < listGreenfees.size(); i++) {
                if (i > 0) gs.append(",");
                gs.append(listGreenfees.get(i).getRoundDate() != null
                        ? listGreenfees.get(i).getRoundDate().format(fmt) : "?");
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(gs);
        }
        String comm = sb.toString();
        if (comm.length() > 140) comm = comm.substring(0, 137) + "...";
        LOG.debug("mixed communication built, length={}", comm.length());
        return comm;
    } // end method

    public String payMixedCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setCreditcardType("MIXED");
            creditcard = prefilling(appContext.getPlayer());
            creditcard.setPaymentOK(false);
            creditcard.setTotalPrice(getTotalCartPrice());
            creditcard.setCommunication(buildMixedCommunication());
            creditcard.setTypePayment("MIXED");
            LOG.debug("MIXED creditcard total={}", creditcard.getTotalPrice());
            return "creditcard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public List<Greenfee> getListGreenfees() { return listGreenfees; } // end method

    public void removeSubscriptionItem() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setSubscription(null);
            if (appContext.getPlayer() != null && appContext.getClub() != null) {
                deleteCartService.deleteByPlayerClubType(
                    appContext.getPlayer().getIdplayer(),
                    appContext.getClub().getIdclub(),
                    "SUBSCRIPTION");
            }
            LOG.info("subscription removed from cart");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeGreenfeeItem(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listGreenfees.remove(gf);
        if (creditcard != null) {
            creditcard.setTotalPrice(listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum());
        }
        upsertCart();
        LOG.debug("greenfee removed, list size={}", listGreenfees.size());
    } // end method

    public java.util.List<entite.EquipmentsAndBasicAndRange> getCotisationBasicCart() {
        TarifMember t = memberController.getTarifMember();
        if (t != null && t.getBasicList() != null) {
            java.util.List<entite.EquipmentsAndBasicAndRange> filtered = t.getBasicList().stream()
                    .filter(b -> b.getQuantity() != null && b.getQuantity() > 0)
                    .toList();
            if (!filtered.isEmpty()) return filtered;
        }
        return restoredBasicCart;
    } // end method

    public java.util.List<entite.EquipmentsAndBasic> getCotisationEquipCart() {
        TarifMember t = memberController.getTarifMember();
        if (t != null && t.getEquipmentsList() != null) {
            java.util.List<entite.EquipmentsAndBasic> filtered = t.getEquipmentsList().stream()
                    .filter(eq -> eq.getQuantity() != null && eq.getQuantity() > 0)
                    .toList();
            if (!filtered.isEmpty()) return filtered;
        }
        return restoredEquipCart;
    } // end method

    public void removeCotisationBasicItem(entite.EquipmentsAndBasicAndRange item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            item.setQuantity(0);
            refreshCotisationCart();
            upsertCart();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeCotisationEquipmentItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            item.setQuantity(0);
            refreshCotisationCart();
            upsertCart();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    private void refreshCotisationCart() throws Exception {
        Cotisation c = tarifMemberController.completeCotisation(
                memberController.getTarifMember(), appContext.getPlayer(), java.time.LocalDate.now());
        if (c == null) return;
        c.setIdplayer(appContext.getPlayer().getIdplayer());
        c.setIdclub(appContext.getClub().getIdclub());
        c.setCommunication(appContext.getClub().getClubName() + " : " + c.getCommunication());
        c.setType(appContext.getCotisation() != null ? appContext.getCotisation().getType() : "spontaneous");
        appContext.setCotisation(c);
        if (c.getPrice() > 0.0) {
            creditcard = completeWithCotisation(c, appContext.getPlayer());
        }
        LOG.debug("cotisation cart refreshed — new total = {}", c.getPrice());
    } // end method

    public void deleteLesson() { // used in cart.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.listLessons.remove(this.selectedLesson);
        String msg = "Lesson removed = = " + this.selectedLesson;
        this.selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
        creditcard.setTotalPrice(listLessons.stream()
                .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                .sum());
        msg = "recalculated totalPrice is now " + creditcard.getTotalPrice();
        LOG.info(msg);
        showMessageInfo(msg);
        upsertCart();
        org.primefaces.PrimeFaces.current().ajax().update("form_cart:growl-msg", "form_cart:listLessons", "form_cart:messages");
    } // end method

    /** Called from SchedulerProController — adds a pending lesson to the cart. */
    public void addLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.add(lesson);
        LOG.info("lesson added to cart, cart size={}", listLessons.size());
        showMessageInfo("Lesson added, cart size=" + listLessons.size());
        appContext.setCreditcardType(LESSON());
        upsertCart();
    } // end method

    /** Called from SchedulerProController — removes a pending lesson from the cart. */
    public void removeLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.removeIf(l -> Objects.equals(l.getEventProId(), lesson.getEventProId())
                && l.getEventStartDate() != null
                && l.getEventStartDate().equals(lesson.getEventStartDate()));
        LOG.info("lesson removed from cart, cart size={}", listLessons.size());
        appContext.setCreditcardType(LESSON());
        upsertCart();
    } // end method

    /** Called from SchedulerProController on drag-drop — updates a pending lesson in the cart. */
    public void updatePendingLesson(Lesson before, Lesson after) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.replaceAll(l -> {
            if (Objects.equals(l.getEventProId(), before.getEventProId())
                    && before.getEventStartDate() != null
                    && before.getEventStartDate().equals(l.getEventStartDate())) {
                l.setEventStartDate(after.getEventStartDate());
                l.setEventEndDate(after.getEventEndDate());
                l.setEventTitle(after.getEventTitle());
            }
            return l;
        });
        LOG.debug("pending lesson updated in cart");
        appContext.setCreditcardType(LESSON());
        upsertCart();
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
            cc.setTypePayment(LESSON());
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
            cc.setTypePayment(SUBSCRIPTION());
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
                LOG.debug("partial success: {} item(s) processed before error — notifying user", errorTx.getPendingInfoMessages().size());
                for (String msg : errorTx.getPendingInfoMessages()) {
                    showMessageInfo(msg);
                }
                this.creditcard = errorTx.getCreditcard();
                this.savedType  = errorTx.getSavedType();
                if (errorTx.getListGreenfees() != null && !errorTx.getListGreenfees().isEmpty()) {
                    this.listGreenfees = errorTx.getListGreenfees();
                }
                if (errorTx.getListLessons() != null && !errorTx.getListLessons().isEmpty()) {
                    this.listLessons = errorTx.getListLessons();
                }
                sendPaymentMails();
                sendLessonMails();
            }

            paymentStateStore.remove(completionNonce);
            return;
        }
        payment.PaymentTransaction tx = paymentStateStore.consume(completionNonce);
        if (tx == null) {
            LOG.warn("transaction not found for nonce={} — syncing skipped, sending mails from session", completionNonce);
            markCartCompleted();
            // Afficher les messages success stockés par REST dans la tx (encore accessible via get())
            if (errorTx != null) {
                for (String msg : errorTx.getPendingInfoMessages()) {
                    showMessageInfo(msg);
                }
            }
            sendPaymentMails();
            sendLessonMails();
            // Inscription auto faite côté REST — confirme N fois (une par greenfee)
            if (this.creditcard != null) {
                String t = this.creditcard.getTypePayment();
                if ("GREENFEE".equals(t) || ("MIXED".equals(t) && !listGreenfees.isEmpty())) {
                    int count = listGreenfees.isEmpty() ? 1 : listGreenfees.size();
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
        // Restore menu visibility — may have been lost if session was recreated during payment flow
        if (appContext.getPlayer() != null) {
            appContext.getPlayer().setShowMenu(true);
            LOG.debug("showMenu restored to true");
        }
        LOG.debug("creditcard synced from PaymentTransaction, nonce={}", completionNonce);
        markCartCompleted();
        sendPaymentMails();

        // GREENFEE — find-or-create round après paiement ; appContext prêt pour inscription.xhtml
        if ("GREENFEE".equals(this.savedType)) {
            processOneGreenfeePostPayment(
                tx.getRound()  != null ? tx.getRound()  : appContext.getRound(),
                tx.getCourse() != null ? tx.getCourse() : appContext.getCourse(),
                tx.getClub()   != null ? tx.getClub()   : appContext.getClub()
            );
        }

        // MIXED — post-payment pour chaque greenfee de la liste
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

        // Persist lessons + 1 payments_lesson groupé — JSF context garanti (preRenderView)
        if (("LESSON".equals(this.savedType) || "MIXED".equals(this.savedType))
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

    /** Find-or-create round + auto-inscription for one greenfee after successful payment. */
    private void processOneGreenfeePostPayment(Round roundCandidate, Course course, Club club) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (roundCandidate == null || course == null || club == null) {
            LOG.warn("GREENFEE post-payment: round/course/club missing — cannot create round");
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
                    showMessageFatal("Créneau complet après paiement — contactez le support.");
                    return;
                }
                ready = existing;
                LOG.info("GREENFEE: reusing existing round idround={}", ready.getIdround());
            } else {
                manager.RoundManager.SaveResult res = roundManager.createRound(
                        roundCandidate, course, club, new UnavailablePeriod());
                if (!res.isSuccess()) {
                    LOG.error("GREENFEE post-payment: round creation failed — {}", res.getMessage());
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
                    LOG.warn("GREENFEE auto-inscription failed — user can retry via Register Score");
                } else {
                    LOG.info("GREENFEE auto-inscription created (no tee) player={} round={}", currentPlayer.getIdplayer(), ready.getIdround());
                    showMessageInfo(utils.LCUtil.prepareMessageBean("inscription.confirmation.mail"));
                }
            } else {
                LOG.debug("GREENFEE: player already inscribed — skip auto-inscription");
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

    /**
     * Sends confirmation mails for GREENFEE, SUBSCRIPTION and COTISATION payments.
     * LESSON mails are handled separately by sendLessonMails().
     * Non-fatal — errors are logged and swallowed.
     */
    private void sendPaymentMails() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (creditcard == null || appContext.getPlayer() == null) return;
        String type = savedType != null ? savedType : creditcard.getTypePayment();
        if (type == null) return;
        boolean mixed = "MIXED".equals(type);
        try {
            if ("COTISATION".equals(type) || (mixed && hasCotisation())) {
                if (appContext.getCotisation() != null) {
                    creditcardMail.sendMailCotisation(appContext.getPlayer(), creditcard,
                        appContext.getCotisation(), appContext.getClub(), memberController.getTarifMember());
                    LOG.info("cotisation mail enqueued player={}", appContext.getPlayer().getIdplayer());
                }
            }
            if ("SUBSCRIPTION".equals(type) || (mixed && hasSubscription())) {
                if (appContext.getSubscription() != null) {
                    creditcardMail.sendMailSubscription(appContext.getPlayer(), creditcard,
                        appContext.getSubscription());
                    LOG.info("subscription mail enqueued player={}", appContext.getPlayer().getIdplayer());
                }
            }
            if ("GREENFEE".equals(type) || (mixed && hasGreenfees())) {
                for (Greenfee gf : listGreenfees) {
                    creditcardMail.sendMailGreenfee(appContext.getPlayer(), creditcard, gf, appContext.getClub());
                }
                LOG.info("greenfee mail(s) enqueued player={} count={}", appContext.getPlayer().getIdplayer(), listGreenfees.size());
            }
        } catch (Exception e) {
            LOG.warn("sendPaymentMails non-fatal error", e);
        }
    } // end method

    public String getCompletionNonce() { return completionNonce; } // end method
    public void setCompletionNonce(String completionNonce) { this.completionNonce = completionNonce; } // end method

    // ========================================
    // CART PERSISTENCE — 2026-05-07
    // ========================================

    /**
     * Compteur badge panier — calculé depuis les listes en mémoire (chargées à la connexion).
     */
    public int getCartBadgeCount() {
        return listGreenfees.size() + listLessons.size()
                + (hasCotisation() ? 1 : 0)
                + (hasSubscription() ? 1 : 0);
    } // end method

    /**
     * Restaure le panier depuis la DB au moment de l'identification du joueur.
     * Appelé par LoginController.selectPlayer() dès que idplayer est connu.
     */
    public void initCartOnLogin() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (sessionRestored) return;
        try { restoreSessionFromDb(); sessionRestored = true; }
        catch (Exception e) { LOG.warn("initCartOnLogin failed (non-fatal)", e); }
    } // end method

    /**
     * Core restore — loads ALL pending DB rows into session. No navigation, no message.
     * @return number of rows restored
     */
    private int restoreSessionFromDb() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getPlayer() == null || appContext.getPlayer().getIdplayer() == null) return 0;
        java.util.List<entite.Cart> carts = findCartService.findAllPendingByPlayer(
            appContext.getPlayer().getIdplayer());
        for (entite.Cart cart : carts) {
            String type = cart.getCartType().name();
            String json  = cart.getCartItemsJson();
            if (json == null) continue;
            if ("LESSON".equals(type)) {
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Lesson>> ref =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                listLessons = OBJECT_MAPPER.readValue(json, ref);
                LOG.debug("lessons restored size={}", listLessons.size());
            } else if ("GREENFEE".equals(type)) {
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Greenfee>> ref =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                listGreenfees = OBJECT_MAPPER.readValue(json, ref);
                if (!listGreenfees.isEmpty()) greenfee = listGreenfees.get(0);
                LOG.debug("greenfees restored size={}", listGreenfees.size());
            } else if ("COTISATION".equals(type)) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                entite.Cotisation c = appContext.getCotisation() != null
                    ? appContext.getCotisation() : new entite.Cotisation();
                // 1. Restaurer les items depuis le JSON
                String basicJson = OBJECT_MAPPER.writeValueAsString(m.get("basic"));
                String equipJson = OBJECT_MAPPER.writeValueAsString(m.get("equipment"));
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.EquipmentsAndBasicAndRange>> basicRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.EquipmentsAndBasic>> equipRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                restoredBasicCart = basicJson.equals("null") ? new ArrayList<>() : OBJECT_MAPPER.readValue(basicJson, basicRef);
                restoredEquipCart = equipJson.equals("null") ? new ArrayList<>() : OBJECT_MAPPER.readValue(equipJson, equipRef);
                // 2. Injecter les items sur TarifMember — source de vérité pour completeCotisation()
                memberController.getTarifMember().setBasicList(new java.util.ArrayList<>(restoredBasicCart));
                memberController.getTarifMember().setEquipmentsList(new java.util.ArrayList<>(restoredEquipCart));
                // 3. Restaurer les dates pour include_summary_cotisation
                if (!restoredBasicCart.isEmpty() && restoredBasicCart.get(0).getStartDate() != null) {
                    memberController.getTarifMember().setStartDate(restoredBasicCart.get(0).getStartDate());
                    memberController.getTarifMember().setEndDate(restoredBasicCart.get(0).getEndDate());
                }
                // 4. Pré-positionner le type (spontaneous/round) pour refreshCotisationCart()
                if (m.get("type") instanceof String typeVal) {
                    entite.Cotisation temp = new entite.Cotisation();
                    temp.setType(typeVal);
                    appContext.setCotisation(temp);
                }
                // 5. Recompute depuis la source (dates, paymentRef, items, communication, status)
                refreshCotisationCart();
                LOG.debug("cotisation restored via refreshCotisationCart basic={} equip={}", restoredBasicCart.size(), restoredEquipCart.size());
            } else if ("SUBSCRIPTION".equals(type)) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                entite.Subscription sub = new entite.Subscription();
                if (m.get("subCode") instanceof String s) sub.setSubCode(s);
                if (m.get("amount")  instanceof Number n) sub.setSubscriptionAmount(n.doubleValue());
                if (m.get("communication") instanceof String s) sub.setCommunication(s);
                sub.setIdplayer(appContext.getPlayer().getIdplayer());
                // complete() est appelé par createPayment() au moment du paiement — pas ici
                appContext.setSubscription(sub);
                LOG.debug("subscription restored code={}", sub.getSubCode());
            }
        }
        if (!carts.isEmpty()) {
            if (creditcard == null) creditcard = new Creditcard();
            creditcard.setTotalPrice(getTotalCartPrice());
            appContext.setCreditcardType(carts.size() == 1 ? carts.get(0).getCartType().name() : "MIXED");
        }
        LOG.debug("restoreSessionFromDb: {} row(s) processed", carts.size());
        return carts.size();
    } // end method

    /**
     * preRenderView listener on cart.xhtml — rebuilds session from DB on every load.
     * Ensures all PENDING rows are visible regardless of session state.
     */
    public void onCartLoad() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (appContext.getPlayer() == null || appContext.getClub() == null) return;
            restoreSessionFromDb();
        } catch (Exception e) {
            LOG.warn("onCartLoad failed (non-fatal)", e);
        }
    } // end method

    /**
     * Restaure tous les paniers PENDING depuis la DB et navigue vers cart.xhtml.
     * Appelé depuis le badge header ou le bouton "Restaurer" de cart.xhtml.
     */
    public String restoreCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            int restored = restoreSessionFromDb();
            if (restored == 0) {
                showMessageInfo(utils.LCUtil.prepareMessageBean("cart.empty.message"));
                return null;
            }
            showMessageInfo(utils.LCUtil.prepareMessageBean("cart.restore.message"));
            LOG.info("cart restored {} type(s)", restored);
            return "cart.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Garantit que appContext.getClub().getIdclub() est non-null.
     * Si le club n'est pas encore positionné, utilise le club home du joueur.
     * @return true si l'idclub est disponible après résolution, false sinon.
     */
    private boolean resolveClubId() {
        if (appContext.getClub().getIdclub() != null) return true;
        Integer homeClub = (appContext.getPlayer() != null) ? appContext.getPlayer().getPlayerHomeClub() : null;
        if (homeClub == null) return false;
        appContext.getClub().setIdclub(homeClub);
        LOG.debug("club id resolved from player home club = {}", homeClub);
        return true;
    } // end method

    /** Supprime le panier DB du type courant. Pour MIXED, supprime tous les types. */
    public void clearCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (appContext.getPlayer() == null || appContext.getClub() == null || !resolveClubId()) return;
            String type = appContext.getCreditcardType();
            if (type == null) return;
            int playerId = appContext.getPlayer().getIdplayer();
            int clubId   = appContext.getClub().getIdclub();
            if ("MIXED".equals(type)) {
                deleteCartService.deleteAllByPlayerClub(playerId, clubId);
            } else {
                deleteCartService.deleteByPlayerClubType(playerId, clubId, type);
            }

            LOG.info("cart cleared from DB type={}", type);
        } catch (Exception e) {
            LOG.warn("clearCartFromDb failed (non-fatal)", e);
        }
    } // end method

    /*
     * Vérifie les overlaps COTISATION et SUBSCRIPTION avant d'envoyer au serveur de paiement.
     * En contexte JSF : OverlapChecker.showMessageFatal() fonctionne — le message s'affiche sur cart.xhtml.
     * @return true si le panier est valide, false si au moins un overlap a été détecté et supprimé.
     */
    /** Lit la table cart, vérifie les doublons/chevauchements, supprime la ligne en cas de problème.
     *  @return true si le panier est valide, false si au moins une ligne a été supprimée. */
    private boolean validateCartBeforePayment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getPlayer() == null || appContext.getClub() == null || !resolveClubId()) return true;
        int playerId = appContext.getPlayer().getIdplayer();
        int clubId   = appContext.getClub().getIdclub();
        boolean valid = true;
        try {
            java.util.List<entite.Cart> carts = findCartService.findAllPending(playerId, clubId);
            for (entite.Cart cart : carts) {
                String cartType = cart.getCartType().name();
                String json = cart.getCartItemsJson();
                switch (cartType) {
                    case "COTISATION" -> {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                        entite.Cotisation cot = new entite.Cotisation();
                        if (m.get("idplayer")  instanceof Number n) cot.setIdplayer(n.intValue());
                        if (m.get("idclub")    instanceof Number n) cot.setIdclub(n.intValue());
                        if (m.get("startDate") instanceof String s) cot.setCotisationStartDate(java.time.LocalDateTime.parse(s));
                        if (m.get("endDate")   instanceof String s) cot.setCotisationEndDate(java.time.LocalDateTime.parse(s));
                        if (cot.getIdplayer() != null && cot.getIdclub() != null && cot.getCotisationStartDate() != null) {
                            if (findCotisationOverlapping.find(cot)) {
                                String msg = "[COTISATION] " + utils.LCUtil.prepareMessageBean("create.cotisation.duplicate")
                                        + " player=" + cot.getIdplayer() + " club=" + cot.getIdclub();
                                LOG.warn(msg);
                                showMessageFatal(msg);
                                showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                                deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                    
                                valid = false;
                            }
                        }
                    }
                    case "SUBSCRIPTION" -> {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                        entite.Subscription sub = new entite.Subscription();
                        if (m.get("subCode") instanceof String s) sub.setSubCode(s);
                        if (m.get("amount")  instanceof Number n) sub.setSubscriptionAmount(n.doubleValue());
                        sub.setIdplayer(playerId);
                        if (sub.getSubCode() != null) {
                            entite.Subscription subComplete = paymentSubscriptionController.complete(sub);
                            if (subComplete != null && subComplete.getStartDate() != null) {
                                if (findSubscriptionOverlapping.find(subComplete)) {
                                    String msg = "[SUBSCRIPTION] " + utils.LCUtil.prepareMessageBean("create.subscription.duplicate")
                                            + " player=" + playerId + " code=" + sub.getSubCode();
                                    LOG.warn(msg);
                                    showMessageFatal(msg);
                                    showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                                    deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                        
                                    valid = false;
                                }
                            }
                        }
                    }
                    case "LESSON" -> {
                        com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Lesson>> lessonRef =
                            new com.fasterxml.jackson.core.type.TypeReference<>() {};
                        java.util.List<entite.Lesson> lessons = OBJECT_MAPPER.readValue(json, lessonRef);
                        boolean anyBooked = false;
                        for (entite.Lesson lesson : lessons) {
                            if (findLessonBooked.find(lesson)) {
                                String msg = "[LESSON] " + utils.LCUtil.prepareMessageBean("lesson.already.booked")
                                        + " " + (lesson.getEventStartDate() != null
                                            ? lesson.getEventStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                                            : "?");
                                LOG.warn(msg);
                                showMessageFatal(msg);
                                anyBooked = true;
                                valid = false;
                            }
                        }
                        if (anyBooked) {
                            showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                
                        }
                    }
                    case "GREENFEE" -> {
                        com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.Greenfee>> ref =
                            new com.fasterxml.jackson.core.type.TypeReference<>() {};
                        java.util.List<entite.Greenfee> greenfees = OBJECT_MAPPER.readValue(json, ref);
                        boolean anyDuplicate = false;
                        for (entite.Greenfee gf : greenfees) {
                            if (gf.getRoundDate() == null || gf.getIdclub() == null || gf.getIdplayer() == null) continue;
                            boolean duplicate = findGreenfeePaid.findByCartKeys(gf.getIdplayer(), gf.getRoundDate(), gf.getIdclub());
                            if (duplicate) {
                                String msg = "[GREENFEE] " + utils.LCUtil.prepareMessageBean("create.greenfee.duplicate")
                                        + " " + gf.getRoundDate().toLocalDate() + " club=" + gf.getIdclub();
                                LOG.warn(msg);
                                showMessageFatal(msg);
                                anyDuplicate = true;
                                valid = false;
                            }
                        }
                        if (anyDuplicate) {
                            showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                            deleteCartService.deleteByPlayerClubType(playerId, clubId, cartType);
                
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
        LOG.debug("validateCartBeforePayment result={}", valid);
        return valid;
    } // end method

    /** Persiste ou met à jour le panier courant en DB (non-fatal si erreur). */
    private void upsertCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String type = appContext.getCreditcardType();
            if (type == null || appContext.getPlayer() == null || appContext.getClub() == null || !resolveClubId()) return;
            String json = buildCartJson(type);
            if (json == null) return;
            Double rawTotal = (creditcard != null) ? creditcard.getTotalPrice() : null;
            double total = (rawTotal != null) ? rawTotal : 0.0;
            entite.Cart cart = new entite.Cart();
            cart.setCartPlayerId(appContext.getPlayer().getIdplayer());
            cart.setCartClubId(appContext.getClub().getIdclub());
            cart.setCartType(eTypePayment.valueOf(type));
            cart.setCartItemsJson(json);
            cart.setCartTotal(total);
            createCartService.upsert(cart);

        } catch (Exception e) {
            LOG.warn("upsertCart failed (non-fatal) type={}", appContext.getCreditcardType(), e);
        }
    } // end method

    /** Sérialise les items du panier selon le type. Retourne null si type inconnu. */
    private String buildCartJson(String type) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("type={}", type);
        return switch (type) {
            case "COTISATION" -> {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("basic",     getCotisationBasicCart());
                m.put("equipment", getCotisationEquipCart());
                entite.Cotisation cot = appContext.getCotisation();
                m.put("total",    cot != null ? cot.getPrice() : 0.0);
                m.put("type",     cot != null ? cot.getType() : "spontaneous");
                m.put("idplayer", appContext.getPlayer() != null ? appContext.getPlayer().getIdplayer() : null);
                m.put("idclub",   appContext.getClub() != null ? appContext.getClub().getIdclub() : null);
                if (cot != null) {
                    m.put("startDate",     cot.getCotisationStartDate());
                    m.put("endDate",       cot.getCotisationEndDate());
                    m.put("communication", cot.getCommunication() != null ? cot.getCommunication() : "");
                    m.put("items",         cot.getItems() != null ? cot.getItems() : "");
                    m.put("status",        cot.getStatus() != null ? cot.getStatus() : "Y");
                }
                yield OBJECT_MAPPER.writeValueAsString(m);
            }
            case "GREENFEE" -> OBJECT_MAPPER.writeValueAsString(listGreenfees);
            case "LESSON" -> OBJECT_MAPPER.writeValueAsString(listLessons);
            case "SUBSCRIPTION" -> {
                entite.Subscription sub = appContext.getSubscription();
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                if (sub != null) {
                    m.put("subCode",       sub.getSubCode());
                    m.put("amount",        sub.getSubscriptionAmount());
                    m.put("communication", sub.getCommunication() != null ? sub.getCommunication() : "");
                }
                yield OBJECT_MAPPER.writeValueAsString(m);
            }
            case "MIXED" -> null; // individual type rows already in DB — no dedicated MIXED row
            default -> null;
        };
    } // end method

    /** Marque le panier PENDING comme COMPLETED après paiement réussi (non-fatal). */
    private void markCartCompleted() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (savedType == null || appContext.getPlayer() == null || appContext.getClub() == null) return;
            int playerId = appContext.getPlayer().getIdplayer();
            int clubId   = appContext.getClub().getIdclub();
            if ("MIXED".equals(savedType)) {
                for (String t : new String[]{"GREENFEE", "LESSON", "COTISATION", "SUBSCRIPTION"}) {
                    updateCartStatusService.setCompletedByPlayerClubType(playerId, clubId, t);
                }
            } else {
                updateCartStatusService.setCompletedByPlayerClubType(playerId, clubId, savedType);
            }

            LOG.info("cart marked COMPLETED type={}", savedType);
        } catch (Exception e) {
            LOG.warn("markCartCompleted failed (non-fatal)", e);
        }
    } // end method

    /** Supprime définitivement le panier DB et navigue hors du panier. */
    public String clearCartAndExit() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        boolean hadGreenfees = hasGreenfees();
        clearCartFromDb();
        listGreenfees.clear();
        listLessons.clear();
        proFree = false;
        if (hadGreenfees || isGreenfee()) return "schedule_round.xhtml?faces-redirect=true";
        return "schedule_pro.xhtml?faces-redirect=true";
    } // end method

    /** Marque le panier PENDING comme CANCELED (appelé depuis cancelCart). */
    private void setCanceledCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String type = appContext.getCreditcardType();
            if (type == null || appContext.getPlayer() == null || appContext.getClub() == null || !resolveClubId()) return;
            updateCartStatusService.setCanceledByPlayerClubType(
                appContext.getPlayer().getIdplayer(),
                appContext.getClub().getIdclub(),
                type);

            LOG.info("cart marked CANCELED type={}", type);
        } catch (Exception e) {
            LOG.warn("setCanceledCart failed (non-fatal)", e);
        }
    } // end method

    // ========================================
    // PAYMENT TYPE — titre et include résolu côté bean
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

} // end class
