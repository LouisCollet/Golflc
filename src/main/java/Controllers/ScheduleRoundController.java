
package Controllers;

import context.ApplicationContext;
import entite.Club;
import entite.Cotisation;
import entite.Course;
import entite.EquipmentsAndBasic;
import entite.Flight;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import entite.UnavailablePeriod;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import manager.RoundManager;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.Log.LOG;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

/**
 * Calendrier des créneaux de départ (flights) par semaine.
 * LazyScheduleModel — PrimeFaces appelle loadEvents(start, end) automatiquement
 * à chaque navigation prev/next/today et changement de vue.
 */
@Named("schedRoundC")
@ViewScoped
public class ScheduleRoundController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS
    // ========================================

    @Inject private ApplicationContext                    appContext;
    @Inject private lists.SunriseSunsetList               sunriseSunsetList;
    @Inject private find.FindCotisationAtRoundDate        findCotisationAtRoundDate;
    @Inject private find.FindRoundBySlot                  findRoundBySlot;
    @Inject private find.FindRoundCountInRange            findRoundCountInRange;
    @Inject private find.FindTeeStart                     findTeeStart;
    @Inject private find.FindInscriptionRound             findInscriptionRound;
    @Inject private find.FindGreenfeePaid                 findGreenfeePaid;
    @Inject private lists.ParticipantsRoundList           participantsRoundList;
    @Inject private manager.RoundManager                  roundManager;
    @Inject private find.FindTarifGreenfeeData            findTarifGreenfeeData;
    @Inject private Controller.refact.MemberController    memberController;
    @Inject private Controller.refact.PaymentController   payC;
    @Inject private read.ReadUnavailablePeriod            readUnavailablePeriod;

    // ========================================
    // CHAMPS
    // ========================================

    private static final int MAX_PLAYERS_PER_SLOT = 4;

    private ScheduleModel scheduleModel;

    // Dialog — créneau sélectionné
    private LocalDateTime     selectedFlightStart   = null;
    private Integer           roundHoles            = 18;
    private Integer           roundStart            = 1;
    private String            roundGame             = "STABLEFORD";
    private boolean           slotHasExistingRound  = false;   // true → holes/start/game en lecture seule
    private boolean           slotAlreadyPaid       = false;   // greenfee déjà payé par le joueur courant
    private boolean           slotAlreadyInscribed  = false;   // déjà inscrit au round
    private boolean           slotUnavailable       = false;   // période d'indisponibilité active
    private String            slotUnavailableTypeKey = null;  // message bundle key du type (ex: unavailable.type.maintenance)
    private String            slotUnavailableLabel   = null;  // détail libre saisi par l'admin
    private java.util.List<String> selectedFlightPlayers = java.util.Collections.emptyList();   // ADMIN uniquement

    public ScheduleRoundController() { }

    // ========================================
    // CYCLE DE VIE
    // ========================================

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        scheduleModel = new LazyScheduleModel() {
            @Override
            public void loadEvents(LocalDateTime start, LocalDateTime end) {
                loadFlightsForRange(start.toLocalDate(), end.toLocalDate());
            }
        };
    } // end method

    // ========================================
    // ACTIONS CALENDRIER
    // ========================================

    /**
     * Clic sur un créneau — stocke l'heure et ouvre le dialog via oncomplete JS.
     */
    public void onEventSelect(SelectEvent<ScheduleEvent<Object>> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            ScheduleEvent<Object> evt = event.getObject();
            Object fullObj = evt.getDynamicProperties() != null ? evt.getDynamicProperties().get("full") : null;
            boolean full = Boolean.TRUE.equals(fullObj);
            selectedFlightStart = evt.getStartDate();
            selectedFlightPlayers = java.util.Collections.emptyList();
            slotHasExistingRound = false;
            slotAlreadyPaid = false;
            slotAlreadyInscribed = false;
            slotUnavailable = false;
            slotUnavailableTypeKey = null;
            slotUnavailableLabel   = null;
            LOG.debug("selectedFlightStart = {}, full = {}", selectedFlightStart, full);

            if (full) {
                showMessageFatal("Créneau complet : "
                        + MAX_PLAYERS_PER_SLOT + " joueurs déjà inscrits à "
                        + selectedFlightStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        + ". Merci de choisir un autre créneau.");
                selectedFlightStart = null;
                org.primefaces.PrimeFaces.current().ajax().addCallbackParam("full", true);
                return;
            }

            // Round existant au slot → holes/start/game figés, chargés depuis la DB
            Club   club   = appContext.getClub();
            Course course = appContext.getCourse();
            Player currentPlayer = appContext.getPlayer();

            // Vérification indisponibilité en priorité
            if (club != null) {
                try {
                    Round dummyRound = new Round();
                    dummyRound.setRoundDate(selectedFlightStart);
                    UnavailablePeriod unavailPeriod = readUnavailablePeriod.read(club, dummyRound);
                    if (unavailPeriod != null) {
                        Integer upCourseId = unavailPeriod.getCourseId();
                        boolean affectsCourse = unavailPeriod.isAllCourses()
                                || upCourseId == null
                                || Integer.valueOf(9999).equals(upCourseId)
                                || (course != null && upCourseId != null && upCourseId.equals(course.getIdcourse()));
                        if (affectsCourse) {
                            slotUnavailable = true;
                            String type = unavailPeriod.getUnavailabilityType();
                            slotUnavailableTypeKey = (type != null && !type.isBlank()) ? type : "unavailable.type.placeholder";
                            slotUnavailableLabel   = unavailPeriod.getUnavailabilityLabel();
                            LOG.debug("slot unavailable: typeKey={}, label={}", slotUnavailableTypeKey, slotUnavailableLabel);
                        }
                    }
                } catch (Exception ex) {
                    LOG.warn("unavailability check failed: {}", ex.getMessage());
                }
            }

            if (club != null && course != null) {
                Round existing = findRoundBySlot.find(course.getIdcourse(), selectedFlightStart,
                        java.time.ZoneId.of(club.getAddress().getZoneId()));
                if (existing != null) {
                    slotHasExistingRound = true;
                    roundHoles = (int) existing.getRoundHoles();
                    roundStart = (int) existing.getRoundStart();
                    roundGame  = existing.getRoundGame();
                    LOG.debug("existing round at slot: idround={}, holes={}, start={}, game={}",
                            existing.getIdround(), roundHoles, roundStart, roundGame);

                    // Check immédiat — paiement et inscription déjà effectués pour ce joueur ?
                    if (currentPlayer != null && currentPlayer.getIdplayer() != null) {
                        try {
                            slotAlreadyPaid      = findGreenfeePaid.find(currentPlayer, existing);
                            slotAlreadyInscribed = findInscriptionRound.find(existing, currentPlayer);
                        } catch (Exception checkEx) {
                            LOG.warn("payment/inscription status check failed: {}", checkEx.getMessage());
                        }
                        LOG.debug("slotAlreadyPaid={}, slotAlreadyInscribed={}",
                                slotAlreadyPaid, slotAlreadyInscribed);
                        if (slotAlreadyInscribed) {
                            showMessageInfo("✅ Vous êtes déjà inscrit à ce créneau — rien à faire.");
                        } else if (slotAlreadyPaid) {
                            showMessageInfo("💳 Paiement déjà effectué pour ce créneau — confirmez pour finaliser l'inscription.");
                        }
                    }

                    // ADMIN (strict, pas localadmin) — noms des joueurs déjà inscrits pour affichage
                    if (isAdminStrict()) {
                        participantsRoundList.invalidateCache();
                        java.util.List<entite.composite.ECourseList> parts = participantsRoundList.list(existing);
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (entite.composite.ECourseList e : parts) {
                            entite.Player p = e.player();
                            if (p == null) continue;
                            String fn = p.getPlayerFirstName() != null ? p.getPlayerFirstName() : "";
                            String ln = p.getPlayerLastName()  != null ? p.getPlayerLastName()  : "";
                            names.add((fn + " " + ln).trim());
                        }
                        selectedFlightPlayers = names;
                        LOG.debug("ADMIN — selectedFlightPlayers size = {}", names.size());
                    }
                }
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Rôle strict ADMIN (pas "admin" minuscule = local administrator). */
    public boolean isAdminStrict() {
        Player p = appContext.getPlayer();
        return p != null && "ADMIN".equals(p.getPlayerRole());
    } // end method

    /**
     * Bouton "Confirmer" du dialog.
     * Flux :
     *   1. Paiement (non-membre) — membre skip
     *   2. Find-or-create round au créneau sélectionné (après paiement pour non-membre)
     *   3. L'inscription (choix du tee, mail) est reportée au menu Register Score
     *
     * Pour un non-membre la création du round a lieu dans
     * PaymentController.onPaymentCompleted() après succès du paiement.
     * L'inscription proprement dite se fait ensuite via le menu Register Score.
     */
    public String confirmRound() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        if (selectedFlightStart == null) {
            LOG.warn("selectedFlightStart is null — action ignored");
            return null;
        }
        if (slotUnavailable) {
            String msg = slotUnavailableTypeKey != null ? slotUnavailableTypeKey : "Indisponible";
            if (slotUnavailableLabel != null && !slotUnavailableLabel.isBlank()) {
                msg += " — " + slotUnavailableLabel;
            }
            showMessageFatal(msg);
            return null;
        }

        try {
            Player player = appContext.getPlayer();
            Club   club   = appContext.getClub();
            Course course = appContext.getCourse();

            Round round = buildRound(selectedFlightStart, roundHoles, roundStart, roundGame, course);
            LOG.debug("round built: date={}, holes={}, start={}, game={}",
                    round.getRoundDate(), round.getRoundHoles(), round.getRoundStart(), round.getRoundGame());

            // Vérification membership
            Cotisation cotisation = findCotisationAtRoundDate.find(player, club, round);
            LOG.debug("cotisation status = {}", cotisation.getStatus());

            if ("Y".equals(cotisation.getStatus())) {
                // ── MEMBRE : pas de paiement — find/create round + inscription automatique
                LOG.debug("MEMBER path — no greenfee, auto-inscription only");
                Round persisted = findOrCreateRound(round, course, club);
                if (persisted == null) return null;
                appContext.setRound(persisted);
                InscribeResult r = autoInscribe(persisted, player, club, course);
                reportInscribeResult(r);
                selectedFlightStart = null;
                scheduleModel.clear();   // force le reload pour afficher le count mis à jour
                return null;
            }

            // Si déjà inscrit : rien à faire — message déjà affiché dans onEventSelect
            if (slotAlreadyInscribed) {
                LOG.debug("already inscribed — nothing to do");
                selectedFlightStart = null;
                return null;
            }

            // Si déjà payé : skip payment, directement find-or-create + auto-inscription
            if (slotAlreadyPaid) {
                LOG.debug("already paid — skip payment, route to auto-inscription");
                Round persisted = findOrCreateRound(round, course, club);
                if (persisted == null) return null;
                appContext.setRound(persisted);
                InscribeResult r = autoInscribe(persisted, player, club, course);
                reportInscribeResult(r);
                selectedFlightStart = null;
                scheduleModel.clear();
                return null;
            }

            // ── NON-MEMBRE : paiement d'abord ; round créé après succès dans onPaymentCompleted
            LOG.debug("player is not a member (status={}) — greenfee payment flow", cotisation.getStatus());

            // Garde de capacité AVANT paiement — si un round existe déjà au slot et est plein, on refuse
            Round existingSlot = findRoundBySlot.find(course.getIdcourse(), round.getRoundDate(),
                    java.time.ZoneId.of(club.getAddress().getZoneId()));
            if (existingSlot != null) {
                participantsRoundList.invalidateCache();
                int inscrits = participantsRoundList.list(existingSlot).size();
                if (inscrits >= MAX_PLAYERS_PER_SLOT) {
                    showMessageFatal("Créneau complet : " + inscrits + "/" + MAX_PLAYERS_PER_SLOT
                            + " joueurs déjà inscrits. Paiement annulé.");
                    selectedFlightStart = null;
                    return null;
                }
            }

            appContext.setRound(round);   // sans idround — créé après paiement
            TarifGreenfee tarif = findTarifGreenfeeData.find(round);
            pickChoosenFromSlot(tarif, selectedFlightStart);
            memberController.setTarifGreenfee(tarif);
            return payC.manageGreenfee(); // → creditcard.xhtml

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Inscription automatique au round (avec 1er tee valide pour le genre du joueur).
     * Idempotent — skip si le joueur est déjà inscrit pour ce round.
     */
    public enum InscribeResult { CREATED, ALREADY_INSCRIBED, FAILED }

    private void reportInscribeResult(InscribeResult r) {
        switch (r) {
            case CREATED           -> showMessageInfo(utils.LCUtil.prepareMessageBean("inscription.confirmation.mail"));
            case ALREADY_INSCRIBED -> showMessageInfo("✅ Vous êtes déjà inscrit à ce créneau.");
            case FAILED            -> showMessageFatal("❌ L'inscription n'a pas pu être créée.");
        }
    } // end method

    private InscribeResult autoInscribe(Round round, Player player, Club club, Course course) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (findInscriptionRound.find(round, player)) {
            LOG.debug("player {} déjà inscrit au round {} — skip", player.getIdplayer(), round.getIdround());
            return InscribeResult.ALREADY_INSCRIBED;
        }
        findTeeStart.invalidateCache();
        java.util.List<String> teeStarts = findTeeStart.find(course, player, round);
        if (teeStarts == null || teeStarts.isEmpty() || !teeStarts.get(0).contains("/")) {
            showMessageFatal("Aucun tee trouvé pour le genre " + player.getPlayerGender()
                    + " — inscription non créée. Complétez via GOLF > Register Score.");
            return InscribeResult.FAILED;
        }
        Inscription inscription = new Inscription();
        inscription.setInscriptionTeeStart(teeStarts.get(0));
        LOG.debug("auto-inscription with tee = {}", teeStarts.get(0));
        Inscription result = roundManager.createInscription(round, player, player, inscription, club, course, "A");
        if (result == null || result.isInscriptionError()) {
            LOG.warn("auto-inscription failed — user can retry via Register Score");
            return InscribeResult.FAILED;
        }
        LOG.info("auto-inscription created for player={} round={}", player.getIdplayer(), round.getIdround());
        return InscribeResult.CREATED;
    } // end method

    /**
     * Cherche un round existant au slot (course + RoundDate exacte) ; le crée si absent.
     * Vérifie aussi la contrainte {@value #MAX_PLAYERS_PER_SLOT}.
     * @return le round avec idround renseigné, ou null en cas d'échec.
     */
    private Round findOrCreateRound(Round candidate, Course course, Club club) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);

        Round existing = findRoundBySlot.find(course.getIdcourse(), candidate.getRoundDate(),
                java.time.ZoneId.of(club.getAddress().getZoneId()));
        if (existing != null) {
            // Vérifie la capacité via ParticipantsRoundList (source d'inscriptions par round)
            participantsRoundList.invalidateCache();   // données fraîches — concurrence
            int inscrits = participantsRoundList.list(existing).size();
            if (inscrits >= MAX_PLAYERS_PER_SLOT) {
                showMessageFatal("Créneau complet : " + inscrits + "/" + MAX_PLAYERS_PER_SLOT + " joueurs déjà inscrits.");
                return null;
            }
            LOG.debug("reusing existing round idround={} ({}/{} inscrits)", existing.getIdround(), inscrits, MAX_PLAYERS_PER_SLOT);
            return existing;
        }

        RoundManager.SaveResult result = roundManager.createRound(candidate, course, club, new UnavailablePeriod());
        if (!result.isSuccess()) {
            showMessageFatal("Round creation failed: " + result.getMessage());
            return null;
        }
        LOG.debug("round created idround={}", candidate.getIdround());
        return candidate;
    } // end method

    // ========================================
    // MÉTHODES PRIVÉES
    // ========================================

    /**
     * Appelé par LazyScheduleModel.loadEvents() — génère les slots pour la plage visible.
     * Utilise sunrise/sunset du lundi de la plage comme référence pour toute la semaine.
     */
    private static final DateTimeFormatter ID_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter HHMM   = DateTimeFormatter.ofPattern("HH:mm");

    private void loadFlightsForRange(LocalDate start, LocalDate end) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        scheduleModel.clear();   // évite l'accumulation lors des rappels AJAX successifs
        try {
            Club   club   = appContext.getClub();
            Course course = appContext.getCourse();
            if (club == null) {
                LOG.warn("club is null in appContext — schedule not loaded");
                return;
            }

            // Chargement du tarif 18T une seule fois pour toute la plage (pas N appels DB)
            TarifGreenfee tarif18 = null;
            if (course != null) {
                try { tarif18 = findTarifGreenfeeData.findSilent(course.getIdcourse(), 18); }
                catch (SQLException ex) { LOG.warn("tarif18 lookup failed: {}", ex.getMessage()); }
            }
            LOG.debug("tarif18 found = {}", tarif18 != null);

            String tz = club.getAddress().getZoneId();
            LOG.debug("club={}, tz={}, range=[{}, {})", club.getClubName(), tz, start, end);

            // Un seul appel API : sunrise/sunset du lundi de la plage
            LocalDate monday = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            Round dummyRound = new Round();
            dummyRound.setRoundDate(monday.atStartOfDay());

            sunriseSunsetList.invalidateCache();
            Flight baseFlight = sunriseSunsetList.list(dummyRound, club);
            if (baseFlight == null) {
                LOG.warn("baseFlight is null — sunrise/sunset API unavailable");
                return;
            }

            ZonedDateTime baseSunrise = baseFlight.getSunrise();
            ZonedDateTime baseSunset  = baseFlight.getSunset();
            LOG.debug("baseSunrise={}, baseSunset={}", baseSunrise, baseSunset);

            // Compte le nombre d'inscrits par RoundDate pour la plage visible (1 seule requête)
            // Pour ADMIN strict : on récupère aussi les noms pour les tooltips hover.
            boolean admin = isAdminStrict();
            java.util.Map<LocalDateTime, Integer> roundCounts = java.util.Collections.emptyMap();
            java.util.Map<LocalDateTime, find.FindRoundCountInRange.SlotInfo> roundSlots = java.util.Collections.emptyMap();
            if (course != null) {
                try {
                    if (admin) {
                        roundSlots = findRoundCountInRange.findWithNames(course.getIdcourse(),
                                start.atStartOfDay(), end.atStartOfDay(),
                                java.time.ZoneId.of(club.getAddress().getZoneId()));
                    } else {
                        roundCounts = findRoundCountInRange.find(course.getIdcourse(),
                                start.atStartOfDay(), end.atStartOfDay(),
                                java.time.ZoneId.of(club.getAddress().getZoneId()));
                    }
                } catch (SQLException ex) { LOG.warn("roundCount lookup failed: {}", ex.getMessage()); }
            }
            LOG.debug("admin={} / roundCounts size={} / roundSlots size={}",
                    admin, roundCounts.size(), roundSlots.size());

            // Les heures de slot sont calculées UNE SEULE FOIS à partir du sunrise/sunset du lundi
            // et réutilisées telles quelles pour tous les jours de la semaine (même HH:mm chaque jour).
            java.util.List<LocalTime> slotTimes = computeSlotTimes(baseSunrise.toLocalTime(), baseSunset.toLocalTime());
            LOG.debug("slot times computed from Monday sunrise {}: {} slots",
                    baseSunrise.toLocalTime(), slotTimes.size());

            // Génère les slots pour chaque jour de la plage visible — skip jours passés
            LocalDateTime nowTruncated = LocalDateTime.now();
            LocalDate today = nowTruncated.toLocalDate();
            LocalDate day = start.isBefore(today) ? today : start;
            while (day.isBefore(end)) {
                for (LocalTime t : slotTimes) {
                    LocalDateTime slotStart = LocalDateTime.of(day, t);
                    if (slotStart.isBefore(nowTruncated)) continue;   // skip heures passées
                    String period = computePeriod(t);
                    String priceLabel = extractPrice(tarif18, slotStart);
                    find.FindRoundCountInRange.SlotInfo slotInfo = admin ? roundSlots.get(slotStart) : null;
                    int inscrits = admin
                            ? (slotInfo != null ? slotInfo.count : 0)
                            : roundCounts.getOrDefault(slotStart, 0);
                    boolean full = inscrits >= MAX_PLAYERS_PER_SLOT;
                    String title = HHMM.format(t) + " — " + priceLabel
                            + " (" + inscrits + "/" + MAX_PLAYERS_PER_SLOT + ")";
                    // Pour ADMIN : noms concaténés (séparés par saut de ligne) pour tooltip hover natif
                    String namesTooltip = (admin && slotInfo != null && !slotInfo.names.isEmpty())
                            ? String.join("\n", slotInfo.names) : "";
                    scheduleModel.addEvent(
                            DefaultScheduleEvent.builder()
                                    .id(slotStart.format(ID_FMT))
                                    .title(title)
                                    .startDate(slotStart)
                                    .endDate(slotStart.plusMinutes(12))
                                    .dynamicProperty("period", period)
                                    .dynamicProperty("inscrits", inscrits)
                                    .dynamicProperty("full", full)
                                    .dynamicProperty("names", namesTooltip)
                                    .backgroundColor(full ? "#6c757d" : periodColor(period))
                                    .textColor("white")
                                    .draggable(false)
                                    .resizable(false)
                                    .build()
                    );
                } // end for slot times

                day = day.plusDays(1);
            } // end while

            LOG.debug("scheduleModel totalEvents={}", scheduleModel.getEventCount());

        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /**
     * Flux « 1-clic » depuis le calendrier — pré-remplit le tableau Choosen
     * approprié avec quantité=1 pour que calcGreenfeePrice() retourne le prix
     * du slot (sinon il retourne 0 → "amount ZERO no payment needed").
     *
     * BA → quantity=1 sur le premier basicList
     * HO → ajoute le TeeTimes qui contient slotStart dans teeTimeChoosen
     * DA → crée un DaysWeek avec le prix du jour-de-semaine dans dayChoosen
     */
    private void pickChoosenFromSlot(TarifGreenfee tarif, LocalDateTime slotStart) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null || slotStart == null) {
            LOG.warn("tarif or slotStart null — skipped");
            return;
        }
        String type = tarif.getGreenfeeType();
        LOG.debug("greenfeeType = {}", type);

        // BA — prix fixe : quantity=1 sur le premier item du basicList
        if ("BA".equals(type)) {
            ArrayList<EquipmentsAndBasic> basicList = tarif.getBasicList();
            if (basicList != null && !basicList.isEmpty()) {
                basicList.get(0).setQuantity(1);
                LOG.debug("BA basicList[0] quantity=1 price={}", basicList.get(0).getPrice());
            } else {
                LOG.warn("BA basicList is empty");
            }
            return;
        }

        // HO — match sur la plage horaire
        if ("HO".equals(type)) {
            LocalTime t = slotStart.toLocalTime();
            ArrayList<TarifGreenfee.TeeTimes> teeList = tarif.getTeeTimesList();
            if (teeList == null) { LOG.warn("HO teeTimesList null"); return; }
            for (TarifGreenfee.TeeTimes tt : teeList) {
                if (tt.getStartTime() == null || tt.getEndTime() == null || tt.getPrice() == null) continue;
                if (!t.isBefore(tt.getStartTime()) && t.isBefore(tt.getEndTime())) {
                    tt.setQuantity(1);
                    tarif.getTeeTimeChoosen().add(tt);
                    LOG.debug("HO teeTime matched {}-{} price={}", tt.getStartTime(), tt.getEndTime(), tt.getPrice());
                    return;
                }
            }
            LOG.warn("HO no matching teeTime for {}", t);
            return;
        }

        // DA — prix selon jour de la semaine (0=monday/week, 2=friday, 3=weekend)
        if ("DA".equals(type)) {
            ArrayList<TarifGreenfee.DaysGreenfee> daysList = tarif.getDaysList();
            if (daysList == null || daysList.isEmpty()) { LOG.warn("DA daysList empty"); return; }
            TarifGreenfee.DaysGreenfee dg = daysList.get(0);
            Double[] prices = dg.getPrice();
            if (prices == null) { LOG.warn("DA price array null"); return; }
            int idx = switch (slotStart.getDayOfWeek()) {
                case FRIDAY           -> 2;
                case SATURDAY, SUNDAY -> 3;
                default               -> 0;
            };
            if (idx >= prices.length || prices[idx] == null) {
                LOG.warn("DA no price at index {}", idx);
                return;
            }
            TarifGreenfee.DaysWeek dw = tarif.new DaysWeek();
            dw.setCategory(dg.getCategory());
            dw.setSeason(dg.getSeason());
            dw.setPrice(prices[idx]);
            dw.setQuantity(1);
            tarif.getDayChoosen().add(dw);
            LOG.debug("DA dayChoosen added : idx={} price={}", idx, prices[idx]);
            return;
        }

        LOG.warn("unknown greenfeeType = {}", type);
    } // end method

    private String extractPrice(TarifGreenfee tarif, LocalDateTime slotStart) {
        if (tarif == null) return "unknown";
        LocalTime t   = slotStart.toLocalTime();
        DayOfWeek dow = slotStart.getDayOfWeek();

        // Type HO : teeTimesList avec plages horaires
        ArrayList<TarifGreenfee.TeeTimes> teeList = tarif.getTeeTimesList();
        if (teeList != null) {
            for (TarifGreenfee.TeeTimes tt : teeList) {
                if (tt.getStartTime() != null && tt.getEndTime() != null && tt.getPrice() != null) {
                    if (!t.isBefore(tt.getStartTime()) && t.isBefore(tt.getEndTime())) {
                        return String.format("%.0f€", tt.getPrice());
                    }
                }
            }
        }

        // Type BA : prix fixe dans basicList
        ArrayList<EquipmentsAndBasic> basicList = tarif.getBasicList();
        if (basicList != null && !basicList.isEmpty()) {
            Double p = basicList.get(0).getPrice();
            if (p != null) return String.format("%.0f€", p);
        }

        // Type DA : daysList — prix selon jour de la semaine
        ArrayList<TarifGreenfee.DaysGreenfee> daysList = tarif.getDaysList();
        if (daysList != null && !daysList.isEmpty()) {
            Double[] prices = daysList.get(0).getPrice();
            if (prices != null) {
                int idx = switch (dow) {
                    case FRIDAY   -> 2;
                    case SATURDAY, SUNDAY -> 3;
                    default       -> 0;   // lundi/semaine
                };
                if (idx < prices.length && prices[idx] != null) {
                    return String.format("%.0f€", prices[idx]);
                }
            }
        }

        return "unknown";
    } // end method

    private Round buildRound(LocalDateTime start, Integer holes, Integer roundStartHole, String game, Course course) {
        Round round = new Round();
        round.setRoundDate(start);
        round.setRoundHoles((short)(holes != null ? holes : 18));
        round.setRoundGame(game);
        round.setRoundName("Round " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        round.setRoundQualifying("N");
        round.setRoundStart((short)(roundStartHole != null ? roundStartHole : 1));
        if (course != null) round.setCourseIdcourse(course.getIdcourse());
        return round;
    } // end method

    private String periodColor(String period) {
        return switch (period) {
            case "A" -> "#28a745";   // matin — vert
            case "C" -> "#0d6efd";   // après-midi — bleu
            default  -> "#fd7e14";   // midi B — orange
        };
    } // end method

    /**
     * Calcule les heures (LocalTime) des slots de la journée à partir du sunrise/sunset
     * du LUNDI de la semaine. Ces heures sont utilisées telles quelles pour chaque jour
     * de la plage visible, garantissant que 9h24 signifie 9h24 tous les jours.
     *
     * Règles conservées du comportement d'origine :
     *   - premier slot = sunrise + 32 min (20 min de warmup + 1er incrément de 12 min)
     *   - incrément entre slots = 12 min
     *   - dernier slot accepté tant que (slot − 12 min) < sunset − 2h30
     */
    private java.util.List<LocalTime> computeSlotTimes(LocalTime sunrise, LocalTime sunset) {
        java.util.List<LocalTime> out = new java.util.ArrayList<>();
        LocalTime cursor    = sunrise.plusMinutes(20);
        LocalTime threshold = sunset.minusHours(2).minusMinutes(30);
        while (cursor.isBefore(threshold)) {
            cursor = cursor.plusMinutes(12);
            out.add(cursor);
        }
        return out;
    } // end method

    /** Détermine la période de la journée selon l'heure. A=matin, B=midi, C=après-midi. */
    private String computePeriod(LocalTime t) {
        if (t.isBefore(LocalTime.of(12, 0))) return "A";
        if (t.isAfter(LocalTime.of(14, 0)))  return "C";
        return "B";
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public ScheduleModel getScheduleModel()                       { return scheduleModel; }
    public LocalDateTime getSelectedFlightStart()                 { return selectedFlightStart; }
    public void          setSelectedFlightStart(LocalDateTime v)  { this.selectedFlightStart = v; }
    public Integer       getRoundHoles()                          { return roundHoles; }
    public void          setRoundHoles(Integer v)                 { this.roundHoles = v; }
    public Integer       getRoundStart()                          { return roundStart; }
    public void          setRoundStart(Integer v)                 { this.roundStart = v; }
    public String        getRoundGame()                           { return roundGame; }
    public void          setRoundGame(String v)                   { this.roundGame = v; }
    public boolean       isSlotHasExistingRound()                 { return slotHasExistingRound; }
    public boolean       isSlotAlreadyPaid()                      { return slotAlreadyPaid; }
    public boolean       isSlotAlreadyInscribed()                 { return slotAlreadyInscribed; }
    public boolean       isSlotUnavailable()                      { return slotUnavailable; }
    public String        getSlotUnavailableTypeKey()              { return slotUnavailableTypeKey; }
    public String        getSlotUnavailableLabel()                { return slotUnavailableLabel; }
    public java.util.List<String> getSelectedFlightPlayers()      { return selectedFlightPlayers; }

} // end class
