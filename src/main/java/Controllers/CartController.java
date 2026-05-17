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
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
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
    // ETAT PANIER
    // ========================================

    private Greenfee       greenfee;
    private List<Greenfee> listGreenfees = new ArrayList<>();
    private Professional   professional;
    private List<Lesson>   listLessons   = new ArrayList<>();
    private Lesson         selectedLesson;
    private boolean        proFree         = false;
    private boolean        sessionRestored = false;
    private List<entite.EquipmentsAndBasicAndRange> restoredBasicCart = new ArrayList<>();
    private List<entite.EquipmentsAndBasic>         restoredEquipCart = new ArrayList<>();

    public CartController() { }

    // ========================================
    // CDI EVENT — ResetEvent observer
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        greenfee          = new Greenfee();
        listGreenfees     = new ArrayList<>();
        professional      = null;
        listLessons       = new ArrayList<>();
        selectedLesson    = null;
        proFree           = false;
        sessionRestored   = false;
        restoredBasicCart = new ArrayList<>();
        restoredEquipCart = new ArrayList<>();
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
    // HAS*() — cart content checks (used by cart.xhtml + PaymentController)
    // ========================================

    public boolean hasGreenfees()   { return !listGreenfees.isEmpty(); }  // end method
    public boolean hasLessons()     { return !listLessons.isEmpty(); }    // end method

    public boolean hasCotisation() {
        return !getCotisationBasicCart().isEmpty() || !getCotisationEquipCart().isEmpty()
            || (appContext.getCotisation() != null && appContext.getCotisation().getPrice() > 0);
    } // end method

    public boolean hasSubscription() {
        return appContext.getSubscription() != null
            && appContext.getSubscription().getSubCode() != null
            && appContext.getSubscription().getSubscriptionAmount() > 0;
    } // end method

    public boolean hasMixedCart() {
        return hasGreenfees() || hasLessons() || hasCotisation() || hasSubscription();
    } // end method

    // ========================================
    // PRICE GETTERS
    // ========================================

    public double getGreenfeePrice() {
        return listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum();
    } // end method

    public double getLessonPrice() {
        return listLessons.stream()
                .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                .sum();
    } // end method

    public double getTotalCartPrice() {
        double total = listGreenfees.stream().mapToDouble(Greenfee::getPrice).sum();
        total += listLessons.stream()
                .mapToDouble(l -> l.getLessonAmount() != null ? l.getLessonAmount() : 0.0)
                .sum();
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
        if (!listLessons.isEmpty()) {
            java.time.format.DateTimeFormatter fmt = DTF_DAY_HHMM;
            StringBuilder ls = new StringBuilder("Lesson ");
            for (int i = 0; i < listLessons.size(); i++) {
                if (i > 0) ls.append(",");
                ls.append(listLessons.get(i).getEventStartDate().format(fmt));
            }
            if (sb.length() > 0) sb.append(" | ");
            sb.append(ls);
        }
        if (!listGreenfees.isEmpty()) {
            java.time.format.DateTimeFormatter fmt = DTF_DAY_HHMM;
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
        return restoredBasicCart;
    } // end method

    public List<entite.EquipmentsAndBasic> getCotisationEquipCart() {
        TarifMember t = memberController.getTarifMember();
        if (t != null && t.getEquipmentsList() != null) {
            return t.getEquipmentsList().stream()
                    .filter(eq -> eq.getQuantity() != null && eq.getQuantity() > 0)
                    .toList();
        }
        return restoredEquipCart;
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
                LOG.debug("cotisation cancelled — basic cart empty after remove");
            } else {
                refreshCotisationCart();
            }
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
            if (getCotisationBasicCart().isEmpty()) {
                TarifMember t = memberController.getTarifMember();
                if (t != null) {
                    t.setBasicList(new ArrayList<>());
                    t.setEquipmentsList(new ArrayList<>());
                }
                appContext.setCotisation(null);
                LOG.debug("cotisation cancelled — no basic item left after equipment remove");
            } else {
                refreshCotisationCart();
            }
            upsertCart();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // CART ITEM MANIPULATION
    // ========================================

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
        upsertCart();
        LOG.debug("greenfee removed, list size={}", listGreenfees.size());
    } // end method

    public void deleteLesson() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        this.listLessons.remove(this.selectedLesson);
        String msg = "Lesson removed = " + this.selectedLesson;
        this.selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
        upsertCart();
        org.primefaces.PrimeFaces.current().ajax().update("form_cart:growl-msg", "form_cart:listLessons", "form_cart:messages");
    } // end method

    public void addLesson(Lesson lesson) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        listLessons.add(lesson);
        LOG.info("lesson added to cart, cart size={}", listLessons.size());
        showMessageInfo("Lesson added, cart size=" + listLessons.size());
        appContext.setCreditcardType(LESSON());
        upsertCart();
    } // end method

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

    // ========================================
    // CART NAVIGATION ACTIONS
    // ========================================

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
    public void setProFree(boolean proFree) { this.proFree = proFree; }

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

    // ========================================
    // CART BADGE
    // ========================================

    public int getCartBadgeCount() {
        return listGreenfees.size() + listLessons.size()
                + (hasCotisation() ? 1 : 0)
                + (hasSubscription() ? 1 : 0);
    } // end method

    // ========================================
    // CART PERSISTENCE — DB operations
    // ========================================

    public void initCartOnLogin() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (sessionRestored) return;
        try { restoreSessionFromDb(); sessionRestored = true; }
        catch (Exception e) { LOG.warn("initCartOnLogin failed (non-fatal)", e); }
    } // end method

    public void onCartLoad() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (appContext.getPlayer() == null || appContext.getClub() == null) return;
            if (jakarta.faces.context.FacesContext.getCurrentInstance()
                    .getPartialViewContext().isAjaxRequest()) {
                LOG.debug("onCartLoad: skipping restore — AJAX request, session already current");
                return;
            }
            restoreSessionFromDb();
        } catch (Exception e) {
            LOG.warn("onCartLoad failed (non-fatal)", e);
        }
    } // end method

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

    public void markCartCompleted(String savedType) {
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

    public boolean validateCartBeforePayment() {
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
                                            ? lesson.getEventStartDate().format(ZDF_TIME_HHmm) : "?");
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
                            if (findGreenfeePaid.findByCartKeys(gf.getIdplayer(), gf.getRoundDate(), gf.getIdclub())) {
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

    public void upsertCart() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            String type = appContext.getCreditcardType();
            if (type == null || appContext.getPlayer() == null || appContext.getClub() == null || !resolveClubId()) return;
            int playerId = appContext.getPlayer().getIdplayer();
            int clubId   = appContext.getClub().getIdclub();
            String json = buildCartJson(type);
            if (json != null) {
                double total = getTotalCartPrice();
                entite.Cart cart = new entite.Cart();
                cart.setCartPlayerId(playerId);
                cart.setCartClubId(clubId);
                cart.setCartType(enumeration.eTypePayment.valueOf(type));
                cart.setCartItemsJson(json);
                cart.setCartTotal(total);
                createCartService.upsert(cart);
            }
            cleanStaleCartRows(playerId, clubId);
        } catch (Exception e) {
            LOG.warn("upsertCart failed (non-fatal) type={}", appContext.getCreditcardType(), e);
        }
    } // end method

    // ========================================
    // PRIVATE HELPERS
    // ========================================

    private int restoreSessionFromDb() throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getPlayer() == null || appContext.getPlayer().getIdplayer() == null) return 0;
        java.util.List<entite.Cart> carts = findCartService.findAllPendingByPlayer(
            appContext.getPlayer().getIdplayer());
        for (entite.Cart cart : carts) {
            String type = cart.getCartType().name();
            String json = cart.getCartItemsJson();
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
                entite.Club cotClub = new entite.Club();
                cotClub.setIdclub(cart.getCartClubId());
                appContext.setClub(readClubService.read(cotClub));
                LOG.debug("cotisation restore: club loaded from cartClubId={}", cart.getCartClubId());
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                String basicJson = OBJECT_MAPPER.writeValueAsString(m.get("basic"));
                String equipJson = OBJECT_MAPPER.writeValueAsString(m.get("equipment"));
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.EquipmentsAndBasicAndRange>> basicRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                com.fasterxml.jackson.core.type.TypeReference<java.util.List<entite.EquipmentsAndBasic>> equipRef =
                    new com.fasterxml.jackson.core.type.TypeReference<>() {};
                restoredBasicCart = basicJson.equals("null") ? new ArrayList<>() : OBJECT_MAPPER.readValue(basicJson, basicRef);
                restoredEquipCart = equipJson.equals("null") ? new ArrayList<>() : OBJECT_MAPPER.readValue(equipJson, equipRef);
                memberController.getTarifMember().setBasicList(new ArrayList<>(restoredBasicCart));
                memberController.getTarifMember().setEquipmentsList(new ArrayList<>(restoredEquipCart));
                if (!restoredBasicCart.isEmpty() && restoredBasicCart.get(0).getStartDate() != null) {
                    memberController.getTarifMember().setStartDate(restoredBasicCart.get(0).getStartDate());
                    memberController.getTarifMember().setEndDate(restoredBasicCart.get(0).getEndDate());
                }
                // Rebuild Cotisation directly from cart JSON (authoritative source — avoids fragile TarifMember recalc)
                entite.Cotisation cot = new entite.Cotisation();
                cot.setIdplayer(appContext.getPlayer().getIdplayer());
                cot.setIdclub(cart.getCartClubId());
                if (m.get("startDate")     instanceof String s) cot.setCotisationStartDate(java.time.LocalDateTime.parse(s));
                if (m.get("endDate")       instanceof String s) cot.setCotisationEndDate(java.time.LocalDateTime.parse(s));
                if (m.get("total")         instanceof Number n) cot.setPrice(n.doubleValue());
                if (m.get("type")          instanceof String s) cot.setType(s);
                if (m.get("communication") instanceof String s) cot.setCommunication(s);
                if (m.get("items")         instanceof String s) cot.setItems(s);
                if (m.get("status")        instanceof String s) cot.setStatus(s);
                appContext.setCotisation(cot);
                LOG.debug("cotisation restored basic={} equip={} startDate={} endDate={}",
                    restoredBasicCart.size(), restoredEquipCart.size(),
                    cot.getCotisationStartDate(), cot.getCotisationEndDate());
            } else if ("SUBSCRIPTION".equals(type)) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> m = OBJECT_MAPPER.readValue(json, java.util.Map.class);
                entite.Subscription sub = new entite.Subscription();
                if (m.get("subCode")       instanceof String s) sub.setSubCode(s);
                if (m.get("amount")        instanceof Number n) sub.setSubscriptionAmount(n.doubleValue());
                if (m.get("communication") instanceof String s) sub.setCommunication(s);
                sub.setIdplayer(appContext.getPlayer().getIdplayer());
                appContext.setSubscription(sub);
                LOG.debug("subscription restored code={}", sub.getSubCode());
            }
        }
        if (!carts.isEmpty()) {
            appContext.setCreditcardType(carts.size() == 1 ? carts.get(0).getCartType().name() : "MIXED");
        }
        LOG.debug("restoreSessionFromDb: {} row(s) processed", carts.size());
        return carts.size();
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
        LOG.debug("cotisation cart refreshed — new total = {}", c.getPrice());
    } // end method

    private void cleanStaleCartRows(int playerId, int clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (listGreenfees.isEmpty()) {
                deleteCartService.deleteByPlayerClubType(playerId, clubId, "GREENFEE");
                LOG.debug("cleanStaleCartRows: GREENFEE row deleted");
            }
            if (appContext.getCotisation() == null) {
                deleteCartService.deleteByPlayerClubType(playerId, clubId, "COTISATION");
                LOG.debug("cleanStaleCartRows: COTISATION row deleted");
            }
            if (listLessons.isEmpty()) {
                deleteCartService.deleteByPlayerClubType(playerId, clubId, "LESSON");
                LOG.debug("cleanStaleCartRows: LESSON row deleted");
            }
            if (appContext.getSubscription() == null) {
                deleteCartService.deleteByPlayerClubType(playerId, clubId, "SUBSCRIPTION");
                LOG.debug("cleanStaleCartRows: SUBSCRIPTION row deleted");
            }
        } catch (Exception e) {
            LOG.warn("cleanStaleCartRows failed (non-fatal)", e);
        }
    } // end method

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
            case "GREENFEE"      -> OBJECT_MAPPER.writeValueAsString(listGreenfees);
            case "LESSON"        -> OBJECT_MAPPER.writeValueAsString(listLessons);
            case "SUBSCRIPTION"  -> {
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
            default      -> null;
        };
    } // end method

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

    private boolean resolveClubId() {
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

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public Greenfee getGreenfee() { return greenfee; }
    public void setGreenfee(Greenfee greenfee) { this.greenfee = greenfee; }

    public List<Greenfee> getListGreenfees() { return listGreenfees; }
    public void setListGreenfees(List<Greenfee> listGreenfees) { this.listGreenfees = listGreenfees; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    public List<Lesson> getListLessons() { return listLessons; }
    public void setListLessons(List<Lesson> listLessons) { this.listLessons = listLessons; }

    public Lesson getSelectedLesson() { return selectedLesson; }
    public void setSelectedLesson(Lesson selectedLesson) { this.selectedLesson = selectedLesson; }

} // end class
