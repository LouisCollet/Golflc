package Controllers;

import context.ApplicationContext;

import enumeration.eTypePayment;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.DTF_DAY_MONTH;
import static interfaces.GolfInterface.DTF_DAY_SLASH;
import static interfaces.GolfInterface.ZDF_HOURS;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Lesson;
import entite.Player;
import entite.Professional;
import entite.Round;
import entite.Subscription;
import entite.UnavailablePeriod;
import static enumeration.eTypePayment.COTISATION;
import static enumeration.eTypePayment.GREENFEE;
import static enumeration.eTypePayment.LESSON;
import static enumeration.eTypePayment.SUBSCRIPTION;
import java.util.Objects;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

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
    @Inject private cache.CacheInvalidator                      cacheInvalidator;
    @Inject private read.ReadCreditcard                         readCreditcard;
    @Inject private payment.PaymentSubscriptionController       paymentSubscriptionController;
    @Inject private payment.PaymentCotisationController         paymentCotisationController;
    @Inject private payment.PaymentGreenfeeController           paymentGreenfeeController;
    @Inject private payment.PaymentLessonController             paymentLessonController;
    @Inject private Controllers.TarifMemberController           tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController         tarifGreenfeeController;
    @Inject private Controllers.MemberController                memberController;
    @Inject private Controllers.CartController                  cartController;
    @Inject private create.CreateLesson                         createLesson;
    @Inject private find.FindRoundBySlot                        findRoundBySlot;
    @Inject private find.FindInscriptionRound                   findInscriptionRound;
    @Inject private lists.ParticipantsRoundList                 participantsRoundList;
    @Inject private manager.RoundManager                        roundManager;
    @Inject private manager.PlayerManager                       playerManager;
    @Inject private read.ReadClub                               readClubService;
    @Inject private Controllers.HttpController                  httpController;
    @Inject private mail.LessonMail                             lessonMail;
    @Inject private mail.PaymentConfirmationMail                paymentConfirmationMail;
    @Inject private entite.Settings                             settings;        // security audit 2026-03-18
    @Inject private payment.PaymentStateStore                   paymentStateStore; // architecture REST/JSF separation 2026-03-21
    @Inject private find.FindCart                               findCartService;
    @Inject private delete.DeleteCart                           deleteCartService;
    @Inject private update.UpdateCartStatus                     updateCartStatusService;
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
            Cotisation cotisation = tarifMemberController.completeCotisation(memberController.getTarifMember(),
                    appContext.getPlayer(), java.time.LocalDate.now());
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
            creditcard = completeWithCotisation(cotisation, appContext.getPlayer());
            if (creditcard != null) {
                String msg = "creditcard completed with Cotisation ! ";
                LOG.info(msg);
                cartController.upsertCotisation();
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
                cartController.clearLessonsFromCart();
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
                cartController.clearLessonsFromCart();
                return null;
            } // end if
            creditcard = completeWithLesson(professional, cartController.getListLessons(), appContext.getPlayer());
            if (creditcard != null) {
                LOG.info("creditcard with lesson = {}", creditcard);
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

    public String manageGreenfee() { // called from price_round_greenfee.xhtml
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with creditcard = {}", creditcard);
            LOG.debug("with greenfee = {}", cartController.getGreenfee());

            appContext.setCreditcardType(GREENFEE());
            LOG.debug("creditcardType = {}", appContext.getCreditcardType());

            // 1. complete greenfee with price - use memC.tarifGreenfee (loaded by findTarifGreenfee)
            Greenfee gf = tarifGreenfeeController.completeGreenfee(memberController.getTarifGreenfee(), appContext.getClub(),
                    appContext.getRound(), appContext.getPlayer());
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
            cartController.addGreenfeeToCart(gf);
            cartController.setGreenfee(gf);
            LOG.debug("listGreenfees size={}", cartController.getListGreenfees().size());
            // 3. complete creditcard with running total
            creditcard = completeWithGreenfee(gf, appContext.getPlayer()); // migrated 2026-02-26 - was creditcardController
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("creditcard Greenfee total = {}", creditcard.getTotalPrice());
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
            Greenfee gf = tarifGreenfeeController.completeGreenfee(memberController.getTarifGreenfee(), appContext.getClub(),
                    appContext.getRound(), appContext.getPlayer());
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
            cartController.addGreenfeeToCart(gf);
            cartController.setGreenfee(gf);
            if (creditcard == null) creditcard = new Creditcard();
            creditcard = completeWithGreenfee(gf, appContext.getPlayer());
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
            LOG.debug("listGreenfees size={}, total={}", cartController.getListGreenfees().size(), creditcard.getTotalPrice());
            String dateStr = gf.getRoundDate() != null ? gf.getRoundDate().format(ZDF_TIME_HHmm) : "?";
            showMessageInfo("Créneau du " + dateStr + " ajouté - "
                    + cartController.getListGreenfees().size() + " créneau(x) dans le panier. Cliquez « Paiement en ligne » pour régler.");
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method addGreenfeeToCart

    /** Depuis greenfee_equipment.xhtml : valide les équipements et retourne sur schedule_round. */
    public String confirmGreenfeeEquipments() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        addGreenfeeToCart();
        return "schedule_round.xhtml?faces-redirect=true";
    } // end method

    /** Navigue vers cart.xhtml si le panier contient au moins un greenfee. */
    public String goToCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (!cartController.hasMixedCart()) {
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
                    creditcard = completeWithSubscription(subscription, appContext.getPlayer());
                    LOG.debug("creditcard completed with subscription = {}", creditcard);
                    cartController.upsertSubscription();
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

    public void onCompletePayment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("coming from creditcard_accepted.xhtml");
        progress1 = 0;
        running = false;
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

    public void onStart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        running = true;
        progress1 = 0;
    } // end method

    public void cancelProgress() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("Payment canceled by User");
        progress1 = 0;  // était null
        running = false;
        creditcard.setPaymentOK(false);
        String msg = "Creditcard payment canceled by user";
        LOG.error(msg);
        showMessageFatal(msg);
    } // end method

    public String payCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            int playerId = appContext.getPlayer().getIdplayer();
            java.util.List<entite.Cart> carts = playerId > 0
                ? findCartService.findAllPendingByPlayer(playerId)
                : java.util.Collections.emptyList();
            java.util.Set<String> typeSet = carts.stream()
                .map(c -> c.getCartType().name())
                .collect(java.util.stream.Collectors.toSet());
            String type = typeSet.size() == 1 ? typeSet.iterator().next()
                        : typeSet.size()  > 1 ? "MIXED"
                        : appContext.getCreditcardType();  // fallback if DB empty
            LOG.debug("cart rows={} → type={}", carts.size(), type);
            appContext.setCreditcardType(type);
            creditcard = prefilling(appContext.getPlayer());
            creditcard.setPaymentOK(false);
            creditcard.setTotalPrice(cartController.getTotalCartPrice());
            creditcard.setCommunication(cartController.buildMixedCommunication());
            creditcard.setTypePayment(type);
            LOG.debug("creditcard total={}", creditcard.getTotalPrice());
            return "creditcard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String editGreenfeeEquipments(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        removeGreenfeeItem(gf);
        entite.TarifGreenfee tarif = memberController.getTarifGreenfee();
        if (tarif != null && tarif.getEquipmentsList() != null) {
            tarif.getEquipmentsList().forEach(e -> e.setQuantity(0));
        }
        return "greenfee_equipment.xhtml?faces-redirect=true";
    } // end method

    public void removeGreenfeeItem(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        cartController.removeGreenfeeFromCart(gf);
        if (creditcard != null) {
            creditcard.setTotalPrice(cartController.getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum());
        }
        LOG.debug("greenfee removed, list size={}", cartController.getListGreenfees().size());
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
            LOG.warn("issuer/card type mismatch issuer={} cardType={}", creditcard.getCreditcardIssuer(), creditcard.getCreditcardType());
            showMessageInfo("WARNING !!! <br/> Issuer detected = " + creditcard.getCreditcardIssuer()
                    + " <br/> Card Type data in = " + creditcard.getCreditcardType());
        } // end if
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
        LOG.debug("entering {} greenfee={}", methodName, greenfee);
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
                 .append(" -> ")
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

    // ========================================
    // PAYMENT COMPLETION - sync REST result back to JSF session
    // Called via f:viewParam/preRenderView on creditcard_payment_executed.xhtml
    // ========================================

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
                dispatchMails(errorTx.getPlayer() != null ? errorTx.getPlayer() : appContext.getPlayer(),
                              errorTx.getClub()   != null ? errorTx.getClub()   : appContext.getClub(),
                              errorTx.getListGreenfees() != null ? errorTx.getListGreenfees() : java.util.Collections.emptyList(),
                              errorTx.getListLessons()   != null ? errorTx.getListLessons()   : java.util.Collections.emptyList(),
                              errorTx.getCotisation(), errorTx.getSubscription(),
                              errorTx.getProfessional() != null ? errorTx.getProfessional() : cartController.getProfessional());
            }

            paymentStateStore.remove(completionNonce);
            return;
        }
        payment.PaymentTransaction tx = paymentStateStore.consume(completionNonce);
        if (tx == null) {
            LOG.warn("transaction not found for nonce={} - syncing skipped, sending mails from session", completionNonce);
            cartController.markCartCompleted(savedType);
            if (errorTx != null) {
                for (String msg : errorTx.getPendingInfoMessages()) {
                    showMessageInfo(msg);
                }
                dispatchMails(errorTx.getPlayer() != null ? errorTx.getPlayer() : appContext.getPlayer(),
                              errorTx.getClub()   != null ? errorTx.getClub()   : appContext.getClub(),
                              errorTx.getListGreenfees() != null ? errorTx.getListGreenfees() : java.util.Collections.emptyList(),
                              errorTx.getListLessons()   != null ? errorTx.getListLessons()   : java.util.Collections.emptyList(),
                              errorTx.getCotisation(), errorTx.getSubscription(),
                              errorTx.getProfessional() != null ? errorTx.getProfessional() : cartController.getProfessional());
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
        // Process PENDING cart rows — chaque type traité indépendamment
        // ========================================
        java.util.List<entite.Greenfee> processedGreenfees  = new java.util.ArrayList<>();
        java.util.List<entite.Lesson>   processedLessons    = new java.util.ArrayList<>();
        entite.Cotisation               processedCotisation = null;
        entite.Subscription             processedSub        = null;
        try {
            int playerId = tx.getPlayerId();
            entite.Player txPlayer = tx.getPlayer() != null ? tx.getPlayer() : appContext.getPlayer();

            if (playerId > 0 && txPlayer != null) {
                java.util.List<entite.Cart> carts = findCartService.findAllPendingByPlayer(playerId);
                LOG.debug("cart rows PENDING count={}", carts.size());

                payment.PaymentOrchestrator sharedOrchestrator = new payment.PaymentOrchestrator(
                    this.creditcard, txPlayer,
                    tx.getRound(), tx.getClub(), tx.getCourse(), tx.getInscription(),
                    paymentSubscriptionController, paymentGreenfeeController,
                    paymentCotisationController, paymentLessonController);

                // Accumulate lesson rows — processed as a batch after the loop
                java.util.List<entite.Lesson> pendingLessons = new java.util.ArrayList<>();
                java.util.List<Integer>       lessonCartIds  = new java.util.ArrayList<>();

                for (entite.Cart cart : carts) {
                    String cartType = cart.getCartType().name();
                    String json = cart.getCartItemsJson();
                    LOG.debug("processing cart idCart={} type={}", cart.getIdCart(), cartType);
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
                            tx.setCotisation(cot);
                            sharedOrchestrator.handle(new payment.CotisationPayment(cot));
                            deleteCartService.deleteById(cart.getIdCart());
                            String cotStart = cot.getCotisationStartDate() != null ? cot.getCotisationStartDate().format(DTF_DAY_SLASH) : "?";
                            String cotEnd   = cot.getCotisationEndDate()   != null ? cot.getCotisationEndDate().format(DTF_DAY_SLASH)   : "?";
                            showMessageInfo(utils.LCUtil.prepareMessageBean("cotisation.confirmed") + " "
                                    + txPlayer.getPlayerFirstName() + " " + txPlayer.getPlayerLastName()
                                    + " — " + (tx.getClub() != null ? tx.getClub().getClubName() : "")
                                    + " — " + cotStart + " → " + cotEnd);
                            processedCotisation = cot;
                            LOG.debug("COTISATION payment done idCart={}", cart.getIdCart());
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
                            deleteCartService.deleteById(cart.getIdCart());
                            showMessageInfo(utils.LCUtil.prepareMessageBean("subscription.success") + sub);
                            processedSub = sub;
                            LOG.debug("SUBSCRIPTION payment done idCart={}", cart.getIdCart());
                        }
                        case "LESSON" -> {
                            entite.Lesson lesson = OBJECT_MAPPER.readValue(json, entite.Lesson.class);
                            pendingLessons.add(lesson);
                            lessonCartIds.add(cart.getIdCart());
                            LOG.debug("LESSON row accumulated idCart={}", cart.getIdCart());
                        }
                        case "GREENFEE" -> {
                            entite.Greenfee gf = OBJECT_MAPPER.readValue(json, entite.Greenfee.class);
                            if (findGreenfeePaid.findByCartKeys(gf.getIdplayer(), gf.getRoundDate(), gf.getIdclub())) {
                                LOG.warn("GREENFEE idempotency - already in DB player={} club={} date={}",
                                        gf.getIdplayer(), gf.getIdclub(), gf.getRoundDate());
                                processedGreenfees.add(gf);
                                deleteCartService.deleteById(cart.getIdCart());
                            } else {
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
                                processOneGreenfeePostPayment(rc, gfCourse, tx.getClub());
                                processedGreenfees.add(gf);
                                deleteCartService.deleteById(cart.getIdCart());
                                showMessageInfo(utils.LCUtil.prepareMessageBean("greenfee.success") + gf);
                                LOG.debug("GREENFEE payment done idCart={}", cart.getIdCart());
                            }
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

                // Process all accumulated lessons as a batch
                if (!pendingLessons.isEmpty()) {
                    try {
                        sharedOrchestrator.handle(new payment.LessonPayment(pendingLessons, tx.getProfessional()));
                        showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.success"));
                        entite.Lesson first = pendingLessons.get(0);
                        String count = pendingLessons.size() > 1 ? " (" + pendingLessons.size() + "x)" : "";
                        showMessageInfo(utils.LCUtil.prepareMessageBean("lesson.confirmed") + " "
                                + txPlayer.getPlayerFirstName() + " " + txPlayer.getPlayerLastName()
                                + " — " + (first.getProName()       != null ? first.getProName()       : "")
                                + " — " + (first.getEventClubName() != null ? first.getEventClubName() : "") + count);
                        LOG.debug("LESSON batch payment done count={}", pendingLessons.size());
                    } catch (Exception lessonEx) {
                        String errMsg = "[LESSON] " + (lessonEx.getMessage() != null ? lessonEx.getMessage() : lessonEx.getClass().getSimpleName());
                        LOG.error("LESSON batch processing failed", lessonEx);
                        showMessageFatal(errMsg);
                    } finally {
                        // Payment already charged by gateway — always mark COMPLETED and always send mail
                        for (int lcId : lessonCartIds) {
                            try { deleteCartService.deleteById(lcId); }
                            catch (Exception e) { LOG.warn("deleteById lesson idCart={} failed", lcId, e); }
                        }
                        processedLessons = pendingLessons;
                        tx.setListLessons(pendingLessons);
                    }
                }
            } else {
                LOG.warn("cart processing skipped playerId={} playerNull={}", playerId, txPlayer == null);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }

        dispatchMails(tx.getPlayer() != null ? tx.getPlayer() : appContext.getPlayer(),
                      tx.getClub()   != null ? tx.getClub()   : appContext.getClub(),
                      processedGreenfees, processedLessons, processedCotisation, processedSub,
                      tx.getProfessional() != null ? tx.getProfessional() : cartController.getProfessional());
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
                Inscription r = roundManager.createInscription(ready, currentPlayer, currentPlayer, auto, club, course, "P");
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

    private void dispatchMails(entite.Player player, Club club,
            java.util.List<entite.Greenfee> greenfees, java.util.List<entite.Lesson> lessons,
            entite.Cotisation cotisation, entite.Subscription subscription,
            Professional professional) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (player == null) return;
        try {
            paymentConfirmationMail.send(player, this.creditcard, club,
                greenfees, lessons, professional,
                cotisation, memberController.getTarifMember(),
                subscription);
            LOG.info("payment confirmation mail enqueued");
            if (!lessons.isEmpty() && professional != null) {
                lessonMail.sendProNotification(player, professional, lessons, this.creditcard, club);
                LOG.info("pro notification mail enqueued count={}", lessons.size());
            } else if (!lessons.isEmpty()) {
                LOG.warn("pro notification skipped — professional is null, lessons count={}", lessons.size());
            }
        } catch (Exception e) {
            LOG.warn("dispatchMails non-fatal error", e);
        }
    } // end method

    public String getCompletionNonce() { return completionNonce; } // end method
    public void setCompletionNonce(String completionNonce) { this.completionNonce = completionNonce; } // end method

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

