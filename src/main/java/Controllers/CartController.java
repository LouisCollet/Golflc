package Controllers;

import context.ApplicationContext;
import entite.*;
import static enumeration.eTypePayment.GREENFEE;
import static enumeration.eTypePayment.LESSON;
import static enumeration.eTypePayment.SUBSCRIPTION;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.DTF_DAY_HHMM;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

@Named("cartC")
@SessionScoped
public class CartController implements Serializable {

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

    @Inject private ApplicationContext                           appContext;
    @Inject private Controllers.TarifMemberController            tarifMemberController;
    @Inject private Controllers.MemberController                 memberController;
    @Inject private create.CreateLesson                          createLesson;
    @Inject private create.CreateCart                            createCartService;
    @Inject private find.FindCart                                findCartService;
    @Inject private update.UpdateCartStatus                      updateCartStatusService;
    @Inject private delete.DeleteCart                            deleteCartService;
    @Inject private find.FindCotisationOverlapping               findCotisationOverlapping;
    @Inject private find.FindSubscriptionOverlapping             findSubscriptionOverlapping;
    @Inject private find.FindGreenfeePaid                        findGreenfeePaid;
    @Inject private find.FindLessonBooked                        findLessonBooked;
    @Inject private payment.PaymentSubscriptionController        paymentSubscriptionController;
    @Inject private read.ReadClub                                readClubService;

    // ========================================
    // SESSION STATE — minimal: only what cannot live in DB
    // ========================================

    private Greenfee     greenfee;         // current greenfee during selection flow
    private Professional professional;     // bridge lesson → payment
    private Lesson       selectedLesson;   // for delete confirmation dialog
    private boolean      proFree = false;

    // Request-scope read cache — transient: never serialized, one DB hit per render cycle
    private transient List<entite.Cart> _pendingRows;
    private transient List<Greenfee>    _cachedGf;
    private transient List<Lesson>      _cachedLs;

    public CartController() { }

    // ========================================
    // CDI EVENT — ResetEvent observer
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        greenfee       = new Greenfee();
        selectedLesson = null;
        proFree        = false;
        invalidateCache();
        LOG.debug("CartController reset done");
    } // end method

    // ========================================
    // TYPE CHECKS — used by cart.xhtml rendered attributes
    // ========================================

    public boolean isCotisation() {
        return enumeration.eTypePayment.COTISATION.equals(appContext.getCreditcardType());
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

    // ========================================
    // HAS*() — DB-backed content checks
    // ========================================

    public boolean hasGreenfees()   { return !getListGreenfees().isEmpty(); }  // end method
    public boolean hasLessons()     { return !getListLessons().isEmpty(); }    // end method

    public boolean hasCotisation() {
        return getPendingRows().stream().anyMatch(c -> c.getCartType() == enumeration.eTypePayment.COTISATION);
    } // end method

    public boolean hasSubscription() {
        return getPendingRows().stream().anyMatch(c -> c.getCartType() == enumeration.eTypePayment.SUBSCRIPTION);
    } // end method

    public boolean hasMixedCart() {
        return hasGreenfees() || hasLessons() || hasCotisation() || hasSubscription();
    } // end method

    // ========================================
    // LIST GETTERS — DB-backed, request-scoped cache
    // ========================================

    public List<Greenfee> getListGreenfees() {
        if (_cachedGf == null) {
            _cachedGf = new ArrayList<>();
            for (entite.Cart row : getPendingRows()) {
                if (row.getCartType() == enumeration.eTypePayment.GREENFEE) {
                    Greenfee gf = parseGreenfee(row.getCartItemsJson());
                    if (gf != null) _cachedGf.add(gf);
                }
            }
        }
        return _cachedGf;
    } // end method

    public List<Lesson> getListLessons() {
        if (_cachedLs == null) {
            _cachedLs = new ArrayList<>();
            for (entite.Cart row : getPendingRows()) {
                if (row.getCartType() == enumeration.eTypePayment.LESSON) {
                    Lesson ls = parseLesson(row.getCartItemsJson());
                    if (ls != null) _cachedLs.add(ls);
                }
            }
        }
        return _cachedLs;
    } // end method

    // ========================================
    // PRICE GETTERS
    // ========================================

    public double getGreenfeePrice() {
        return getListGreenfees().stream().mapToDouble(Greenfee::getPrice).sum();
    } // end method

    public double getLessonPrice() {
        return getListLessons().stream()
                .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                .sum();
    } // end method

    public double getTotalCartPrice() {
        double total = getGreenfeePrice() + getLessonPrice();
        if (hasCotisation() && appContext.getCotisation() != null)
            total += appContext.getCotisation().getPrice();
        if (hasSubscription() && appContext.getSubscription() != null)
            total += appContext.getSubscription().getSubscriptionAmount();
        return total;
    } // end method

    // ========================================
    // MIXED COMMUNICATION — called by PaymentController.payCart()
    // ========================================

    public String buildMixedCommunication() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        StringBuilder sb = new StringBuilder();
        if (hasCotisation() && appContext.getCotisation() != null) {
            String c = appContext.getCotisation().getCommunication();
            if (c != null && !c.isBlank()) sb.append(c);
        }
        if (hasSubscription() && appContext.getSubscription() != null) {
            String c = appContext.getSubscription().getCommunication();
            if (c != null && !c.isBlank()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(c);
            }
        }
        List<Lesson> lessons = getListLessons();
        if (!lessons.isEmpty()) {
            StringBuilder ls = new StringBuilder("Lesson ");
            for (int i = 0; i < lessons.size(); i++) {
                if (i > 0) ls.append(",");
                ls.append(lessons.get(i).getEventStartDate().format(DTF_DAY_HHMM));
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(ls);
        }
        List<Greenfee> gfs = getListGreenfees();
        if (!gfs.isEmpty()) {
            StringBuilder gs = new StringBuilder("GF ");
            for (int i = 0; i < gfs.size(); i++) {
                if (i > 0) gs.append(",");
                gs.append(gfs.get(i).getRoundDate() != null
                        ? gfs.get(i).getRoundDate().format(DTF_DAY_HHMM) : "?");
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(gs);
        }
        String comm = sb.toString();
        if (comm.length() > 140) comm = comm.substring(0, 137) + "...";
        LOG.debug("mixed communication built length={}", comm.length());
        return comm;
    } // end method

    // ========================================
    // COTISATION CART ITEMS
    // ========================================

    public List<entite.EquipmentsAndBasicAndRange> getCotisationBasicCart() {
        TarifMember t = memberController.getTarifMember();
        if (t != null && t.getBasicList() != null) {
            return t.getBasicList().stream()
                    .filter(b -> b.getQuantity() != null && b.getQuantity() > 0)
                    .toList();
        }
        return Collections.emptyList();
    } // end method

    public List<entite.EquipmentsAndBasic> getCotisationEquipCart() {
        TarifMember t = memberController.getTarifMember();
        if (t != null && t.getEquipmentsList() != null) {
            return t.getEquipmentsList().stream()
                    .filter(eq -> eq.getQuantity() != null && eq.getQuantity() > 0)
                    .toList();
        }
        return Collections.emptyList();
    } // end method

    public void removeCotisationBasicItem(entite.EquipmentsAndBasicAndRange item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            item.setQuantity(0);
            if (getCotisationBasicCart().isEmpty()) {
                TarifMember t = memberController.getTarifMember();
                if (t != null) {
                    t.setBasicList(new ArrayList<>());
                    t.setEquipmentsList(new ArrayList<>());
                }
                appContext.setCotisation(null);
                deleteCartService.deleteByPlayerClubType(playerId(), clubId(), "COTISATION");
                LOG.debug("cotisation cancelled — basic cart empty after remove");
            } else {
                refreshCotisationCart();
                upsertCotisation();
            }
            invalidateCache();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeCotisationEquipmentItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            item.setQuantity(0);
            if (getCotisationBasicCart().isEmpty()) {
                TarifMember t = memberController.getTarifMember();
                if (t != null) {
                    t.setBasicList(new ArrayList<>());
                    t.setEquipmentsList(new ArrayList<>());
                }
                appContext.setCotisation(null);
                deleteCartService.deleteByPlayerClubType(playerId(), clubId(), "COTISATION");
                LOG.debug("cotisation cancelled — no basic item left after equipment remove");
            } else {
                refreshCotisationCart();
                upsertCotisation();
            }
            invalidateCache();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // CART MUTATIONS — all DB-direct
    // ========================================

    public void removeSubscriptionItem() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            appContext.setSubscription(null);
            deleteCartService.deleteByPlayerClubType(playerId(), clubId(), "SUBSCRIPTION");
            invalidateCache();
            LOG.info("subscription removed from cart");
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void addGreenfeeToCart(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (gf.getRoundDate() == null) return;
            createCartService.upsert(buildCartRow(playerId(), clubId(), gf.getRoundDate(),
                enumeration.eTypePayment.GREENFEE, OBJECT_MAPPER.writeValueAsString(gf), gf.getPrice()));
            greenfee = gf;
            invalidateCache();
            LOG.debug("greenfee added to cart roundDate={}", gf.getRoundDate());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeGreenfeeFromCart(Greenfee gf) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (gf.getRoundDate() == null) return;
            findCartService.findByPlayerClubTypeStartDate(playerId(), clubId(), "GREENFEE", gf.getRoundDate())
                .ifPresent(c -> {
                    try { deleteCartService.deleteById(c.getIdCart()); }
                    catch (Exception ex) { LOG.warn("deleteById failed idCart={}", c.getIdCart(), ex); }
                });
            invalidateCache();
            LOG.debug("greenfee removed from cart roundDate={}", gf.getRoundDate());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void addLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (lesson.getEventStartDate() == null) return;
            double amount = lesson.getLessonAmount() != null ? lesson.getLessonAmount() : 0.0;
            createCartService.upsert(buildCartRow(playerId(), clubId(), lesson.getEventStartDate(),
                enumeration.eTypePayment.LESSON, OBJECT_MAPPER.writeValueAsString(lesson), amount));
            appContext.setCreditcardType(LESSON());
            invalidateCache();
            LOG.info("lesson added to cart startDate={}", lesson.getEventStartDate());
         //   showMessageInfo("Lesson added to cart"); // mod 18-05-2026 
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (lesson.getEventStartDate() == null) return;
            findCartService.findByPlayerClubTypeStartDate(playerId(), clubId(), "LESSON", lesson.getEventStartDate())
                .ifPresent(c -> {
                    try { deleteCartService.deleteById(c.getIdCart()); }
                    catch (Exception ex) { LOG.warn("deleteById failed idCart={}", c.getIdCart(), ex); }
                });
            appContext.setCreditcardType(LESSON());
            invalidateCache();
            LOG.info("lesson removed from cart startDate={}", lesson.getEventStartDate());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void deleteLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (selectedLesson == null) return;
        removeLesson(selectedLesson);
        String msg = "Lesson removed = " + selectedLesson;
        selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
        org.primefaces.PrimeFaces.current().ajax().update("form_cart:growl-msg", "form_cart:listLessons", "form_cart:messages");
    } // end method

    public void updatePendingLesson(Lesson before, Lesson after) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        removeLesson(before);
        addLesson(after);
        appContext.setCreditcardType(LESSON());
        LOG.debug("pending lesson updated in cart");
    } // end method

    public void clearLessonsFromCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            deleteCartService.deleteByPlayerClubType(playerId(), clubId(), "LESSON");
            invalidateCache();
        } catch (Exception e) {
            LOG.warn("clearLessonsFromCart failed (non-fatal)", e);
        }
    } // end method

    public void upsertCotisation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            entite.Cotisation cot = appContext.getCotisation();
            if (cot == null || cot.getCotisationStartDate() == null) return;
            createCartService.upsert(buildCartRow(playerId(), clubId(), cot.getCotisationStartDate(),
                enumeration.eTypePayment.COTISATION, buildCotisationJson(cot), cot.getPrice()));
            invalidateCache();
            LOG.debug("cotisation upserted startDate={}", cot.getCotisationStartDate());
        } catch (Exception e) {
            LOG.warn("upsertCotisation failed (non-fatal)", e);
        }
    } // end method

    public void upsertSubscription() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            entite.Subscription sub = appContext.getSubscription();
            if (sub == null) return;
            java.time.LocalDateTime subStart = sub.getStartDate() != null
                ? sub.getStartDate()
                : java.time.LocalDateTime.now().withSecond(0).withNano(0);
            createCartService.upsert(buildCartRow(playerId(), clubId(), subStart,
                enumeration.eTypePayment.SUBSCRIPTION, buildSubscriptionJson(sub), sub.getSubscriptionAmount()));
            invalidateCache();
            LOG.debug("subscription upserted code={}", sub.getSubCode());
        } catch (Exception e) {
            LOG.warn("upsertSubscription failed (non-fatal)", e);
        }
    } // end method

    // ========================================
    // CART NAVIGATION ACTIONS
    // ========================================

    public String confirmProFreeLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            for (Lesson lesson : getListLessons()) {
                if (createLesson.create(lesson, appContext.getPlayer())) {
                    LOG.info("free lesson created: {}", lesson.getEventTitle());
                } else {
                    showMessageFatal("error: free lesson not registered");
                    return null;
                }
            }
            proFree = false;
            clearCartFromDb();
            return "welcome.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public boolean isProFree() { return proFree; }
    public void setProFree(boolean proFree) { this.proFree = proFree; }

    public String cancelCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        setCanceledCart();
        proFree = false;
        invalidateCache();
        return "welcome.xhtml?faces-redirect=true";
    } // end method

    public String clearCartAndExit() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        clearCartFromDb();
        proFree = false;
        return "welcome.xhtml?faces-redirect=true";
    } // end method

    // ========================================
    // CART BADGE
    // ========================================

    public int getCartBadgeCount() {
        List<entite.Cart> rows = getPendingRows();
        // Keep creditcardType in sync with DB so cart.xhtml renders correctly at session start
        if (!rows.isEmpty()) {
            java.util.Set<String> types = rows.stream()
                .map(c -> c.getCartType().name())
                .collect(java.util.stream.Collectors.toSet());
            appContext.setCreditcardType(types.size() == 1 ? types.iterator().next() : "MIXED");
        }
        return rows.size();
    } // end method

    // ========================================
    // CART PERSISTENCE — DB operations
    // ========================================

    public void onCartLoad() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (appContext.getPlayer() == null) return;
            if (FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest()) return;
            invalidateCache();
            refreshAppContextFromCart();
            java.util.Set<String> types = getPendingRows().stream()
                .map(c -> c.getCartType().name())
                .collect(java.util.stream.Collectors.toSet());
            if (!types.isEmpty()) {
                appContext.setCreditcardType(types.size() == 1 ? types.iterator().next() : "MIXED");
            }
        } catch (Exception e) {
            LOG.warn("onCartLoad failed (non-fatal)", e);
        }
    } // end method

    public String restoreCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            invalidateCache();
            int count = getPendingRows().size();
            if (count == 0) {
                showMessageInfo(utils.LCUtil.prepareMessageBean("cart.empty.message"));
                return null;
            }
            refreshAppContextFromCart();
            showMessageInfo(utils.LCUtil.prepareMessageBean("cart.restore.message"));
            LOG.info("cart restored {} row(s)", count);
            return "cart.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void clearCartFromDb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (playerId() == 0) return;
            deleteCartService.deleteAllByPlayer(playerId());
            invalidateCache();
            LOG.info("cart cleared from DB playerId={}", playerId());
        } catch (Exception e) {
            LOG.warn("clearCartFromDb failed (non-fatal)", e);
        }
    } // end method

    public void markCartCompleted(String savedType) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (playerId() == 0) return;
            updateCartStatusService.setCompletedByPlayer(playerId());
            invalidateCache();
            LOG.info("cart marked COMPLETED type={}", savedType);
        } catch (Exception e) {
            LOG.warn("markCartCompleted failed (non-fatal)", e);
        }
    } // end method

    public boolean validateCartBeforePayment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (playerId() == 0 || clubId() == 0) return true;
        boolean valid = true;
        try {
            for (entite.Cart cart : getPendingRows()) {
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
                                deleteCartService.deleteById(cart.getIdCart());
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
                        sub.setIdplayer(playerId());
                        if (sub.getSubCode() != null) {
                            entite.Subscription subComplete = paymentSubscriptionController.complete(sub);
                            if (subComplete != null && subComplete.getStartDate() != null) {
                                if (findSubscriptionOverlapping.find(subComplete)) {
                                    String msg = "[SUBSCRIPTION] " + utils.LCUtil.prepareMessageBean("create.subscription.duplicate")
                                            + " player=" + playerId() + " code=" + sub.getSubCode();
                                    LOG.warn(msg);
                                    showMessageFatal(msg);
                                    showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                                    deleteCartService.deleteById(cart.getIdCart());
                                    valid = false;
                                }
                            }
                        }
                    }
                    case "LESSON" -> {
                        Lesson lesson = OBJECT_MAPPER.readValue(json, Lesson.class);
                        if (findLessonBooked.find(lesson)) {
                            String msg = "[LESSON] " + utils.LCUtil.prepareMessageBean("lesson.already.booked")
                                    + " " + (lesson.getEventStartDate() != null
                                        ? lesson.getEventStartDate().format(ZDF_TIME_HHmm) : "?");
                            LOG.warn(msg);
                            showMessageFatal(msg);
                            showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                            deleteCartService.deleteById(cart.getIdCart());
                            valid = false;
                        }
                    }
                    case "GREENFEE" -> {
                        Greenfee gf = OBJECT_MAPPER.readValue(json, Greenfee.class);
                        if (gf.getRoundDate() != null && gf.getIdclub() != null && gf.getIdplayer() != null) {
                            if (findGreenfeePaid.findByCartKeys(gf.getIdplayer(), gf.getRoundDate(), gf.getIdclub())) {
                                String msg = "[GREENFEE] " + utils.LCUtil.prepareMessageBean("create.greenfee.duplicate")
                                        + " " + gf.getRoundDate().toLocalDate() + " club=" + gf.getIdclub();
                                LOG.warn(msg);
                                showMessageFatal(msg);
                                showMessageInfo(utils.LCUtil.prepareMessageBean("cart.duplicate.suggestion"));
                                deleteCartService.deleteById(cart.getIdCart());
                                valid = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
        invalidateCache();
        LOG.debug("validateCartBeforePayment result={}", valid);
        return valid;
    } // end method

    // ========================================
    // PRIVATE HELPERS
    // ========================================

    private int playerId() {
        return (appContext.getPlayer() != null && appContext.getPlayer().getIdplayer() != null)
            ? appContext.getPlayer().getIdplayer() : 0;
    } // end method

    private int clubId() {
        if (!resolveClubId()) return 0;
        return (appContext.getClub() != null && appContext.getClub().getIdclub() != null)
            ? appContext.getClub().getIdclub() : 0;
    } // end method

    private List<entite.Cart> getPendingRows() {
        if (_pendingRows == null) {
            try {
                int pid = playerId();
                _pendingRows = pid > 0
                    ? findCartService.findAllPendingByPlayer(pid)
                    : Collections.emptyList();
            } catch (Exception e) {
                LOG.warn("getPendingRows failed (non-fatal)", e);
                _pendingRows = Collections.emptyList();
            }
        }
        return _pendingRows;
    } // end method

    private void invalidateCache() {
        _pendingRows = null;
        _cachedGf    = null;
        _cachedLs    = null;
    } // end method

    private Greenfee parseGreenfee(String json) {
        try { return OBJECT_MAPPER.readValue(json, Greenfee.class); }
        catch (Exception e) { LOG.warn("parseGreenfee failed: {}", e.getMessage()); return null; }
    } // end method

    private Lesson parseLesson(String json) {
        try { return OBJECT_MAPPER.readValue(json, Lesson.class); }
        catch (Exception e) { LOG.warn("parseLesson failed: {}", e.getMessage()); return null; }
    } // end method

    private void refreshAppContextFromCart() {
        // Restore club from first cart row that carries a clubId (all types store it)
        if (appContext.getClub() == null) {
            getPendingRows().stream()
                .filter(c -> c.getCartClubId() > 0)
                .findFirst()
                .ifPresent(c -> {
                    try {
                        entite.Club club = new entite.Club();
                        club.setIdclub(c.getCartClubId());
                        appContext.setClub(readClubService.read(club));
                        LOG.debug("club restored from cart clubId={}", c.getCartClubId());
                    } catch (Exception e) {
                        LOG.warn("club restore from cart failed", e);
                    }
                });
        }
        for (entite.Cart cart : getPendingRows()) {
            try {
                String json = cart.getCartItemsJson();
                if (json == null) continue;
                if (cart.getCartType() == enumeration.eTypePayment.COTISATION) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                    // Restore TarifMember lists
                    String basicJson = OBJECT_MAPPER.writeValueAsString(m.get("basic"));
                    String equipJson = OBJECT_MAPPER.writeValueAsString(m.get("equipment"));
                    com.fasterxml.jackson.core.type.TypeReference<List<entite.EquipmentsAndBasicAndRange>> basicRef =
                        new com.fasterxml.jackson.core.type.TypeReference<>() {};
                    com.fasterxml.jackson.core.type.TypeReference<List<entite.EquipmentsAndBasic>> equipRef =
                        new com.fasterxml.jackson.core.type.TypeReference<>() {};
                    List<entite.EquipmentsAndBasicAndRange> basicList = basicJson.equals("null")
                        ? new ArrayList<>() : OBJECT_MAPPER.readValue(basicJson, basicRef);
                    List<entite.EquipmentsAndBasic> equipList = equipJson.equals("null")
                        ? new ArrayList<>() : OBJECT_MAPPER.readValue(equipJson, equipRef);
                    memberController.getTarifMember().setBasicList(new ArrayList<>(basicList));
                    memberController.getTarifMember().setEquipmentsList(new ArrayList<>(equipList));
                    if (!basicList.isEmpty() && basicList.get(0).getStartDate() != null) {
                        memberController.getTarifMember().setStartDate(basicList.get(0).getStartDate());
                        memberController.getTarifMember().setEndDate(basicList.get(0).getEndDate());
                    }
                    // Rebuild Cotisation from DB JSON
                    entite.Cotisation cot = new entite.Cotisation();
                    cot.setIdplayer(playerId());
                    cot.setIdclub(cart.getCartClubId());
                    if (m.get("startDate")     instanceof String s) cot.setCotisationStartDate(java.time.LocalDateTime.parse(s));
                    if (m.get("endDate")       instanceof String s) cot.setCotisationEndDate(java.time.LocalDateTime.parse(s));
                    if (m.get("total")         instanceof Number n) cot.setPrice(n.doubleValue());
                    if (m.get("type")          instanceof String s) cot.setType(s);
                    if (m.get("communication") instanceof String s) cot.setCommunication(s);
                    if (m.get("items")         instanceof String s) cot.setItems(s);
                    if (m.get("status")        instanceof String s) cot.setStatus(s);
                    appContext.setCotisation(cot);
                    LOG.debug("cotisation refreshed from DB startDate={}", cot.getCotisationStartDate());
                } else if (cart.getCartType() == enumeration.eTypePayment.SUBSCRIPTION) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                    entite.Subscription sub = new entite.Subscription();
                    if (m.get("subCode")       instanceof String s) sub.setSubCode(s);
                    if (m.get("amount")        instanceof Number n) sub.setSubscriptionAmount(n.doubleValue());
                    if (m.get("communication") instanceof String s) sub.setCommunication(s);
                    sub.setIdplayer(playerId());
                    appContext.setSubscription(sub);
                    LOG.debug("subscription refreshed from DB code={}", sub.getSubCode());
                }
            } catch (Exception e) {
                LOG.warn("refreshAppContextFromCart failed for type={}", cart.getCartType(), e);
            }
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
        LOG.debug("cotisation cart refreshed total={}", c.getPrice());
    } // end method

    private void setCanceledCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (playerId() == 0) return;
            updateCartStatusService.setCanceledByPlayer(playerId());
            invalidateCache();
            LOG.info("cart marked CANCELED playerId={}", playerId());
        } catch (Exception e) {
            LOG.warn("setCanceledCart failed (non-fatal)", e);
        }
    } // end method

    private boolean resolveClubId() {
        if (appContext.getClub() == null) return false;
        if (appContext.getClub().getIdclub() != null) return true;
        Integer homeClubId = (appContext.getPlayer() != null) ? appContext.getPlayer().getPlayerHomeClub() : null;
        if (homeClubId == null) return false;
        try {
            entite.Club c = new entite.Club();
            c.setIdclub(homeClubId);
            appContext.setClub(readClubService.read(c));
        } catch (Exception e) {
            LOG.warn("resolveClubId: could not load home club id={}", homeClubId, e);
            return false;
        }
        LOG.debug("club resolved from player home club id={}", homeClubId);
        return true;
    } // end method

    private entite.Cart buildCartRow(int pid, int cid, java.time.LocalDateTime startDate,
            enumeration.eTypePayment type, String json, double total) {
        entite.Cart cart = new entite.Cart();
        cart.setCartPlayerId(pid);
        cart.setCartClubId(cid);
        cart.setCartStartDate(startDate);
        cart.setCartType(type);
        cart.setCartItemsJson(json);
        cart.setCartTotal(total);
        return cart;
    } // end method

    private String buildCotisationJson(entite.Cotisation cot) throws Exception {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("basic",         getCotisationBasicCart());
        m.put("equipment",     getCotisationEquipCart());
        m.put("total",         cot.getPrice());
        m.put("type",          cot.getType() != null ? cot.getType() : "spontaneous");
        m.put("idplayer",      appContext.getPlayer() != null ? appContext.getPlayer().getIdplayer() : null);
        m.put("idclub",        appContext.getClub()   != null ? appContext.getClub().getIdclub()     : null);
        m.put("startDate",     cot.getCotisationStartDate());
        m.put("endDate",       cot.getCotisationEndDate());
        m.put("communication", cot.getCommunication()  != null ? cot.getCommunication()  : "");
        m.put("items",         cot.getItems()          != null ? cot.getItems()          : "");
        m.put("status",        cot.getStatus()         != null ? cot.getStatus()         : "Y");
        return OBJECT_MAPPER.writeValueAsString(m);
    } // end method

    private String buildSubscriptionJson(entite.Subscription sub) throws Exception {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("subCode",       sub.getSubCode());
        m.put("amount",        sub.getSubscriptionAmount());
        m.put("communication", sub.getCommunication() != null ? sub.getCommunication() : "");
        return OBJECT_MAPPER.writeValueAsString(m);
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public Greenfee getGreenfee() { return greenfee; }
    public void setGreenfee(Greenfee greenfee) { this.greenfee = greenfee; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public Lesson getSelectedLesson() { return selectedLesson; }
    public void setSelectedLesson(Lesson selectedLesson) { this.selectedLesson = selectedLesson; }

} // end class
