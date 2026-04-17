package Controller.refact;

import context.ApplicationContext;
import entite.*;
import entite.composite.ECourseList;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import utils.LCUtil;

/**
 * Controller JSF pour la gestion des membres : tarifs, paiements, subscriptions
 * Phase 1 migree depuis CourseController le 2026-02-25
 */
@Named("memC")
@SessionScoped
public class MemberController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI
    // ========================================

 //   @Inject private MemberManager memberManager;
    @Inject private ApplicationContext appContext;
    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private Controllers.TarifMemberController    tarifMemberController;
    @Inject private Controllers.TarifGreenfeeController  tarifGreenfeeController;
    @Inject private find.FindTarifGreenfeeData           findTarifGreenfeeData;
    @Inject private find.FindTarifMembersData            findTarifMembersData;
    @Inject private create.CreateTarifMember             createTarifMemberService;
    @Inject private create.CreateTarifGreenfee           createTarifGreenfeeService;
    @Inject private delete.DeleteTarifGreenfee           deleteTarifGreenfeeService;
    @Inject private delete.DeleteTarifMember             deleteTarifMemberService;
    @Inject private lists.ProfessionalListForPayments    professionalListForPayments;
    @Inject private lists.LocalAdminGreenfeeList         localAdminGreenfeeList;
    @Inject private lists.SystemAdminSubscriptionList    systemAdminSubscriptionList;
    @Inject private lists.LocalAdminCotisationList       localAdminCotisationList;
    @Inject private update.UpdateSubscription            updateSubscription;
    @Inject private Controller.refact.NavigationController navigationController;  // renamed 2026-02-28
    @Inject private lists.ClubsListLocalAdmin            clubsListLocalAdmin;   // migrated 2026-02-25
    @Inject private lists.CoursesListLocalAdmin          coursesListLocalAdmin; // migrated 2026-02-25
    @Inject private lists.SubscriptionRenewalList        subscriptionRenewalList; // migrated 2026-02-25
    @Inject private calc.CalcTarifGreenfee              calcTarifGreenfee; // migrated 2026-02-28
    @Inject private create.CreateTarifSubscription     createTarifSubscriptionService; // added 2026-03-06
    @Inject private delete.DeleteTarifSubscription     deleteTarifSubscriptionService; // added 2026-03-06
    @Inject private lists.TarifSubscriptionList        tarifSubscriptionList;          // added 2026-03-06
    @Inject private lists.CourseListForClub            courseListForClub;              // added for greenfee wizard course selector
    @Inject private lists.TarifGreenfeeList            tarifGreenfeeList;              // added for admin tarif list
    @Inject private update.UpdateTarifGreenfee         updateTarifGreenfeeJsonService; // added for add period to existing tarif
    @Inject private dao.GenericDAO                     dao;                            // added for wizard club lookup

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private TarifMember tarifMember;
    private TarifGreenfee tarifGreenfee;
    private TarifGreenfee tarifGreenfeeDB = null; // loaded lazily from DB for Show Tarif dialog
    private Greenfee greenfee;
    private Subscription subscription;
    private Club club;
    private Course course;
    private Round round;
    private List<ECourseList> subscriptionRenewal;
    private List<?> filteredCars; // PrimeFaces dataTable filteredValue — migrated from navC 2026-02-28
    private entite.TarifSubscription tarifSubscription; // added 2026-03-06
    private entite.TarifGreenfee.DatesSeasons editingPeriod    = null; // non-null = mode édition d'une période existante
    private entite.EquipmentsAndBasic          editingEquipment = null; // non-null = mode édition d'un équipement existant

    public MemberController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifMember   = new TarifMember();
        tarifGreenfee = new TarifGreenfee();
        greenfee      = new Greenfee();
        subscription  = new Subscription();
        tarifSubscription = new entite.TarifSubscription();
        LOG.debug("MemberController initialized");
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} — source: {}", event.getSource());
        tarifMember         = new TarifMember();
        tarifGreenfee       = new TarifGreenfee();
        tarifGreenfeeDB     = null;
        greenfee            = new Greenfee();
        subscription        = new Subscription();
        club                = null;
        course              = null;
        round               = null;
        subscriptionRenewal = null;
        filteredCars        = null;
        tarifSubscription   = new entite.TarifSubscription();
        LOG.debug("MemberController reset done");
    } // end method

    // ========================================
    // TARIF LOOKUP / FIND (5 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String findTarifGreenfee() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
      //      LOG.debug("player = {}", appContext.getPlayer().toString());
      //      LOG.debug("course = {}", appContext.getCourse());
       // enlevé : pourquoi ??     appContext.setInputSelectCourse("createTarifGreenfee");
            tarifGreenfee = findTarifGreenfeeData.find(appContext.getRound());
            if (tarifGreenfee == null) {
                String err = "Tarif returned from findTarifdata is null ";
                LOG.debug(err);
                return null;
            }
            LOG.debug("tarifGreenfee found = {}", tarifGreenfee);
            tarifGreenfee = calcTarifGreenfee.calc(tarifGreenfee, appContext.getRound(), appContext.getClub(), appContext.getPlayer()); // migrated 2026-02-28
            return "price_round_greenfee.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String findTarifCotisation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
        //    LOG.debug(" for round = {}", appContext.getRound());
        //    LOG.debug(" for club = {}", club);
            tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound());
            LOG.debug("tarifMember found = {}", tarifMember);
        //    appContext.setInputSelectCourse("createTarifMember");
        //    appContext.setInputSelectClub("createTarifMember");
            if (tarifMember == null) {
                String msgerr = prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr);
                showMessageFatal(msgerr);
                return "greenfee_cotisation_round.xhtml?faces-redirect=true";
            } else {
                return "cotisation.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String findTarifGreenfeeEcl(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("findTarif with ecl = {}", ecl);
       //    club = ecl.club();
       //     course = ecl.course();
         //   round = ecl.round();
            tarifGreenfee = findTarifGreenfeeData.find(ecl.getRound());
            if (tarifGreenfee == null) {
                String msg = "No Tarif available for this course";
                LOG.error(msg);
                showMessageFatal(msg);
            } else {
                String msg = "Tarif returned = " + tarifGreenfee.toString();
                LOG.info(msg);
                showMessageInfo(msg);
            }
            return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String selectTarif(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("selectTarif, ecl = {}", ecl);
            club = ecl.club();
            course = ecl.course();
            round = ecl.round();
            return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public void showTarifGreenfee(String idcourse) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with idcourse={}", idcourse);
        try {
            // Set courseIdcourse on round if not already set (needed by FindTarifGreenfeeData)
            Round round = appContext.getRound();
            if (round.getCourseIdcourse() == null && idcourse != null && !idcourse.isEmpty()) {
                round.setCourseIdcourse(Integer.parseInt(idcourse));
                LOG.debug("courseIdcourse set to {}", idcourse);
            }
            tarifGreenfee = findTarifGreenfeeData.find(round);
            if (tarifGreenfee == null) {
                // message already displayed by FindTarifGreenfeeData
                return;
            }
            String msg = prepareMessageBean("tarif.greenfee.show") + tarifGreenfee.showTarifGreenfee();
            LOG.info(msg);
            showMessageInfo(msg);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // TARIF INPUT (3 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String inputTarifMembersCotisation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with Members tarif = {}", tarifMember);
            LOG.debug("for club = {}", club);
            tarifMember = tarifMemberController.inputTarifMembersCotisation(tarifMember);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /* obsolete 2026-03-27 — remplacé par inputTarifGreenfeeWizard() — pages multi-step en parking
    public String inputTarifGreenfee(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("with param {}", param);
            LOG.debug("with tarif = {}", tarifGreenfee);
            switch (param) {
                case "PE" -> {
                    // tarifCourseId is now set from the wizard's course selector
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeePeriods(tarifGreenfee);
                    return "tarif_greenfee_periods.xhtml?faces-redirect=true";
                }
                case "BA" -> {
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeBasic(tarifGreenfee);
                    return "tarif_greenfee_basic.xhtml?faces-redirect=true";
                }
                case "DA" -> {
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeDays(tarifGreenfee);
                    return "tarif_greenfee_days.xhtml?faces-redirect=true";
                }
                case "EQ" -> {
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeEquipments(tarifGreenfee);
                    return "tarif_greenfee_equipments.xhtml?faces-redirect=true";
                }
                case "HO" -> {
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeHours(tarifGreenfee);
                    return "tarif_greenfee_hours.xhtml?faces-redirect=true";
                }
                case "PR" -> {
                    showMessageInfo(tarifGreenfee.toString());
                    return null;
                }
                default -> {
                    String msg = "failed in default switch";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    return null;
                }
            } // end switch
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method
    */

    /* obsolete 2026-03-27 — remplacé par inputTarifMemberWizard('EQ') — aucun XHTML actif
    public String inputTarifMembersEquipments() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("for club = {}", club);
            LOG.debug("tarifMembers = {}", tarifMember);
            tarifMember = tarifMemberController.inputTarifMembersEquipments(tarifMember);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method
    */

    // ========================================
    // TARIF CREATE / DELETE / SHOW (5 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String createTarifMember() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("tarifMember = {}", tarifMember);
         //   LOG.debug("for club = {}", club);
          //  tarifMember.setTarifMemberIdClub(club.getIdclub());
            tarifMember.setTarifMemberIdClub(appContext.getClub().getIdclub()); // mod LC 06/03/2026
            if (createTarifMemberService.create(tarifMember)) {
                String msg = "Tarif is created ";
                LOG.info(msg);
                showMessageInfo(msg);
                return "tarif_member_wizard.xhtml?faces-redirect=true";
            } else {
                String msg = "Tarif is NOT created ";
                LOG.error(msg);
                showMessageFatal(msg);
                return "tarif_member_wizard.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String createTarifGreenfee() throws SQLException { // handleSQLException used — throws required
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("tarifGreenfee = {}", tarifGreenfee);
            if (tarifGreenfee.getGreenfeeType() == null) {
                String msg = "Pricing type (BA/HO/DA) must be selected before saving the tarif.";
                LOG.error("{}", msg);
                showMessageFatal(msg);
                return null;
            }
        //    LOG.debug("with club = {}", club); // devrait être null
            if (createTarifGreenfeeService.create(tarifGreenfee, appContext.getClub())) { // mod LC 06/03/2026
                tarifGreenfeeList.invalidateCache(); // invalidate after CREATE
                String msg = " TarifGreenfee Created ! " + tarifGreenfee; // mod LC 16-04-2026
                LOG.info(msg);
                showMessageInfo(msg);
                return "welcome.xhtml?faces-redirect=true";
            } else {
                String msg = "Fatal Error creation tarif Greenfee";
                LOG.debug(msg);
                showMessageFatal(msg);
                return "welcome.xhtml?faces-redirect=true";
            }
        } catch (java.sql.SQLException e) {
            if (isMysqlDuplicate(e)) {
                String msg = prepareMessageBean("tarif.greenfee.duplicate");
                LOG.warn(msg);
                showMessageFatal(msg);
                return null;
            }
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String deleteTarifGreenfee(String year) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            TarifGreenfee db = getTarifGreenfeeDB();
            if (db == null || db.getTarifId() == null) {
                showMessageFatal("No tarif in DB for this course — cannot delete.");
                return null;
            }
            LOG.debug("deleting tarifId = {}", db.getTarifId());
            if (deleteTarifGreenfeeService.deleteById(db.getTarifId())) {
                LOG.info("TarifGreenfee deleted, tarifId = {}", db.getTarifId());
                tarifGreenfeeDB = null; // invalidate cache
                tarifGreenfeeList.invalidateCache(); // invalidate after DELETE
            } else {
                LOG.error("deleteById returned false for tarifId = {}", db.getTarifId());
            }
            return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String showTarifMembers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("club = {}", club);
            String msg = prepareMessageBean("tarif.member.show") + "<br/" + tarifMember;
            LOG.info(msg);
            showMessageInfo(msg);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String deleteTarifMember() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
        //    LOG.debug("with club = {}", club);
            LOG.debug("tarifMember = {}", tarifMember);
         //   tarifMember.setTarifMemberIdClub(club.getIdclub());
            tarifMember.setTarifMemberIdClub(appContext.getClub().getIdclub()); // mod LC 06/03/2026
            if (deleteTarifMemberService.delete(tarifMember)) {
                String msg = "TarifMember deleted = " + tarifMember;
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = "Result of deleteTarifMember is NOT OK = " + tarifMember;
                LOG.error(msg);
                showMessageFatal(msg);
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // LISTES ADMIN (4 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public List<ECourseList> listProfessionalPayments() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("professional = {}", appContext.getPlayer());
            return professionalListForPayments.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminGreenfee(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("param = {}", s);
        try {
            LOG.debug("local admin = {}", appContext.getPlayer());
            LOG.debug("role = {}", appContext.getPlayer().getPlayerRole());
            if (appContext.getInputSelectPaiement().equals("Greenfees")) {
                return localAdminGreenfeeList.list(appContext.getPlayer());
            }
            LOG.debug("error in inputSelectPaiement : INVALID");
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listSystemAdminSubscription(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("param = {}", s);
        try {
            return systemAdminSubscriptionList.list();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminCotisation(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("param = {}", s);
        try {
            LOG.debug("local admin = {}", appContext.getPlayer());
            LOG.debug("role = {}", appContext.getPlayer().getPlayerRole());
            if (appContext.getInputSelectPaiement().equals("Greenfees")) {
                return localAdminGreenfeeList.list(appContext.getPlayer());
            }
            if (appContext.getInputSelectPaiement().equals("Members")) {
                return localAdminCotisationList.list(appContext.getPlayer());
            }
            LOG.debug("error in inputSelectPaiement : INVALID");
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // SUBSCRIPTION (1 methode)
    // migrated from CourseController 2026-02-25
    // ========================================

    public boolean modifySubscription() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("subscription = {}", subscription);
            if (updateSubscription.modify(subscription)) {
                LOG.debug("after modifySubscription : OK, subscription = {}", subscription);
                String msg = prepareMessageBean("subscription.success") + " end date = " + subscription.getEndDate().format(ZDF_DAY);
                LOG.debug(msg);
                showMessageInfo(msg);
                return true;
            } else {
                String msg = "error : subscription NOT modified !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return false;
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return false;
        }
    } // end method

    // ========================================
    // LISTES LOCAL ADMIN (2 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public List<Club> listLocalAdminClubsList() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("player = {}", appContext.getPlayer());
            return clubsListLocalAdmin.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminCoursesList(String select, String admin) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("player = {}", appContext.getPlayer());
            return coursesListLocalAdmin.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // SUBSCRIPTION RENEWAL (1 methode)
    // migrated from CourseController 2026-02-25
    // ========================================

    public void listSubscriptionRenewal(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            subscriptionRenewal = subscriptionRenewalList.list();
            String msg = "We send subscription Renewal Mails = " + subscriptionRenewal.size();
            LOG.debug(msg);
            LCUtil.showDialogInfo(msg);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // NAVIGATION to_* (4 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    /**
     * Navigation vers subscription.xhtml
     * Migré depuis menu.xhtml url= — 2026-02-28
     */
    public String to_subscription_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        try {
            navigationController.reset(s);
            return "subscription.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String to_selectLocalAdmin_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        try {
            navigationController.reset("Reset to_selectLocalAdmin" + s);
            appContext.setInputSelectPaiement(s);
            if (s.equals("Members")) {
                return "local_administrator_cotisations.xhtml?faces-redirect=true";
            }
            if (s.equals("Greenfees")) {
                return "local_administrator_greenfees.xhtml?faces-redirect=true";
            }
            if (s.equals("Professionals")) {
                return "local_administrator_professionals.xhtml?faces-redirect=true";
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String to_selectSystemAdmin_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        try {
            navigationController.reset("Reset to_selectSystemAdmin" + s);
            appContext.setInputSelectSubscriptions(s);
            return "system_administrator_subscriptions.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String to_selectPro_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} with string = {}", s);
        try {
            navigationController.reset("Reset to_selectPro" + s);
            appContext.setInputSelectPaiement(s);
            if (s.equals("Lessons")) {
                return "professional_lessons_paid.xhtml?faces-redirect=true";
            }
            if (s.equals("Inscription")) {
                return "selectPro.xhtml?faces-redirect=true&cmd=" + s; // mod 29-03-2026
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // GETTERS / SETTERS
    // ========================================

    public TarifMember getTarifMember() {
        if (tarifMember == null) tarifMember = new TarifMember();
        return tarifMember;
    } // end method

    public void setTarifMember(TarifMember tarifMember) {
        this.tarifMember = tarifMember;
    } // end method

    public TarifGreenfee getTarifGreenfee() {
        if (tarifGreenfee == null) tarifGreenfee = new TarifGreenfee();
        return tarifGreenfee;
    } // end method

    public void setTarifGreenfee(TarifGreenfee tarifGreenfee) {
        this.tarifGreenfee = tarifGreenfee;
    } // end method

    public double getTarifGreenfeeTotal() {
        if (tarifGreenfee == null) return 0.0;
        return tarifGreenfeeController.calcGreenfeePrice(tarifGreenfee);
    } // end method

    public String getGreenfeeIncludeSrc() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getGreenfeeType() == null) {
            return "include/include_empty.xhtml";
        }
        return switch (tarifGreenfee.getGreenfeeType()) {
            case "BA" -> "include/include_greenfee_basic.xhtml";
            case "DA" -> "include/include_greenfee_days.xhtml";
            case "HO" -> "include/include_greenfee_hours.xhtml";
            default   -> {
                LOG.error("unknown greenfeeType = {}", tarifGreenfee.getGreenfeeType());
                yield "include/include_empty.xhtml";
            }
        };
    } // end method

    /** Lazy load — reads existing DB tarif for current course (today's date). Returns null if none. */
    public TarifGreenfee getTarifGreenfeeDB() {
        if (tarifGreenfeeDB != null) {
            return tarifGreenfeeDB; // cached — called many times per render, no log
        }
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getTarifCourseId().isEmpty()) {
            LOG.debug("tarifCourseId is empty — returning null");
            return null;
        }
        try {
            tarifGreenfeeDB = findTarifGreenfeeData.findSilent(
                    tarifGreenfee.getTarifCourseId().get(0), tarifGreenfee.getTarifHoles());
            LOG.debug("tarifGreenfeeDB loaded = {}", tarifGreenfeeDB != null ? tarifGreenfeeDB.getTarifId() : "null");
        } catch (Exception e) {
            LOG.warn("no DB tarif found: {}", e.getMessage());
        }
        return tarifGreenfeeDB;
    } // end method

    public void resetTarifGreenfeeDB() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifGreenfeeDB = null;
    } // end method

    /** Returns courses for the current club — used by the greenfee wizard course selector. */
    public List<entite.Course> getCoursesForWizard() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (appContext.getClub() == null || appContext.getClub().getIdclub() == null) {
            return Collections.emptyList();
        }
        try {
            List<entite.Course> courses = courseListForClub.list(appContext.getClub());
            // auto-select when only one course exists
            if (courses.size() == 1 && tarifGreenfee != null && tarifGreenfee.getTarifCourseId().isEmpty()) {
                tarifGreenfee.setTarifCourseId(new java.util.ArrayList<>(java.util.List.of(courses.get(0).getIdcourse())));
                LOG.debug("auto-selected single course id={}", courses.get(0).getIdcourse());
            }
            return courses;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public java.util.List<jakarta.faces.model.SelectItem> getHolesItems() {
        return java.util.List.of(
            new jakarta.faces.model.SelectItem(Integer.valueOf(18), "18T"),
            new jakarta.faces.model.SelectItem(Integer.valueOf(9),  "9T")
        );
    } // end method

    public List<entite.Club> getClubsForWizard() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return clubsListLocalAdmin.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public Integer getWizardClubId() {
        return appContext.getClub() != null ? appContext.getClub().getIdclub() : null;
    } // end method

    public void setWizardClubId(Integer clubId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (clubId == null) return;
        try {
            clubsListLocalAdmin.list(appContext.getPlayer()).stream()
                .filter(c -> clubId.equals(c.getIdclub()))
                .findFirst()
                .ifPresent(c -> {
                    appContext.setClub(c);
                    courseListForClub.invalidateCache();
                    if (tarifGreenfee != null) tarifGreenfee.setTarifCourseId(new java.util.ArrayList<>());
                });
            // auto-select after club change (getCoursesForWizard handles initial load)
            getCoursesForWizard();
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Returns "CourseName (id), ..." for all courses selected in the wizard, or empty string. */
    public String getTarifGreenfeeCourseDisplay() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getTarifCourseId().isEmpty()) {
            return "";
        }
        try {
            java.util.List<entite.Course> all = courseListForClub.list(appContext.getClub());
            return tarifGreenfee.getTarifCourseId().stream()
                    .map(id -> all.stream()
                            .filter(c -> id.equals(c.getIdcourse()))
                            .map(c -> c.getCourseName() + " (" + c.getIdcourse() + ")")
                            .findFirst()
                            .orElse("Course #" + id))
                    .collect(java.util.stream.Collectors.joining(", "));
        } catch (Exception e) {
            return tarifGreenfee.getTarifCourseId().toString();
        }
    } // end method

    /** Returns the course name for a given courseId — used in tarif_greenfee_admin.xhtml table rows.
     *  Uses courseListForClub (Java-level cache) to avoid one JDBC roundtrip per table row. */
    public String courseNameFor(Integer courseId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (courseId == null) return "";
        try {
            return courseListForClub.list(appContext.getClub()).stream()
                    .filter(c -> courseId.equals(c.getIdcourse()))
                    .map(entite.Course::getCourseName)
                    .findFirst()
                    .orElse("Course #" + courseId);
        } catch (Exception e) {
            return "Course #" + courseId;
        }
    } // end method

    /** Overload for multi-course tarifs — joins course names with " + ". */
    public String courseNameFor(java.util.List<Integer> courseIds) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (courseIds == null || courseIds.isEmpty()) return "";
        return courseIds.stream()
                .map(this::courseNameFor)
                .collect(java.util.stream.Collectors.joining(" + "));
    } // end method

    public Cotisation getCotisation() {
        return appContext.getCotisation();
    } // end method

    public void setCotisation(Cotisation cotisation) {
        appContext.setCotisation(cotisation);
    } // end method

    public Greenfee getGreenfee() {
        if (greenfee == null) greenfee = new Greenfee();
        return greenfee;
    } // end method

    public void setGreenfee(Greenfee greenfee) {
        this.greenfee = greenfee;
    } // end method

    public Subscription getSubscription() {
        return appContext.getSubscription();
    } // end method

    public void setSubscription(Subscription subscription) {
        appContext.setSubscription(subscription);
    } // end method

    public Club getClub() {
        return club;
    } // end method

    public void setClub(Club club) {
        this.club = club;
    } // end method

    public Course getCourse() {
        return course;
    } // end method

    public void setCourse(Course course) {
        this.course = course;
    } // end method

    public Round getRound() {
        return round;
    } // end method

    public void setRound(Round round) {
        this.round = round;
    } // end method

    public List<ECourseList> getSubscriptionRenewal() {
        return subscriptionRenewal;
    } // end method

    public void setSubscriptionRenewal(List<ECourseList> subscriptionRenewal) {
        this.subscriptionRenewal = subscriptionRenewal;
    } // end method

    public List<?> getFilteredCars()                    { return filteredCars; }
    public void    setFilteredCars(List<?> filteredCars) { this.filteredCars = filteredCars; }

    // ========================================
    // COMPUTED PROPERTIES
    // ========================================

    public int getTarifGreenfeeActiveStep() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null) return 0;
        if (tarifGreenfee.getDatesSeasonsList() == null || tarifGreenfee.getDatesSeasonsList().isEmpty()) return 0;
        if (!tarifGreenfee.isEquipmentsReady()) return 1;
        if (!tarifGreenfee.isUpdateReady()) return 2;
        if (!tarifGreenfee.isTwilightDone()) return 3;
        return 4;
    } // end method

    // ========================================
    // ADMIN — LISTE DES TARIFS GREENFEE
    // ========================================

    public List<entite.TarifGreenfee> getTarifGreenfeeListForClub() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            return tarifGreenfeeList.list();
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Charge un TarifGreenfee existant depuis la DB pour visualisation (dialog).
     */
    public void loadTarifForEdit(int tarifId) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - tarifId={}", methodName, tarifId);
        try {
            tarifGreenfee = findTarifGreenfeeData.findById(tarifId);
            if (tarifGreenfee == null) {
                showMessageFatal("Tarif not found for id=" + tarifId);
            } else {
                LOG.debug("tarif loaded for edit: courseId={} holes={} periods={}",
                        tarifGreenfee.getTarifCourseId(),
                        tarifGreenfee.getTarifHoles(),
                        tarifGreenfee.getDatesSeasonsList().size());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /**
     * Charge un TarifGreenfee existant et ouvre le wizard pour continuer la saisie (ajout de périodes, etc.).
     * Pré-remplit wizardClubId depuis le TarifCourseId pour que le sélecteur de parcours soit correct.
     */
    public String loadTarifForWizard(int tarifId) throws java.sql.SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {} - tarifId={}", methodName, tarifId);
        try {
            tarifGreenfee = findTarifGreenfeeData.findById(tarifId);
            if (tarifGreenfee == null) {
                showMessageFatal("Tarif not found for id=" + tarifId);
                return null;
            }
            // Trouver le club_idclub du parcours et l'injecter dans appContext via setWizardClubId
            if (tarifGreenfee.getTarifCourseId() != null) {
                List<Integer> ids = dao.queryList(
                    "SELECT club_idclub FROM course WHERE idcourse = ?",
                    rs -> rs.getInt("club_idclub"),
                    tarifGreenfee.getTarifCourseId());
                if (!ids.isEmpty()) {
                    setWizardClubId(ids.get(0)); // set appContext.club + reload course list
                }
            }
            LOG.debug("tarif loaded for wizard: courseId={} holes={} periods={}",
                    tarifGreenfee.getTarifCourseId(),
                    tarifGreenfee.getTarifHoles(),
                    tarifGreenfee.getDatesSeasonsList().size());
            return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Ajoute la période (workSeason + startDate + endDate) au TarifGreenfee chargé et sauvegarde en DB.
     * Appelé depuis le dialog "+ Période".
     */
    public void addPeriodToExistingTarif() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (tarifGreenfee == null || tarifGreenfee.getTarifId() == null) {
                showMessageFatal("No tarif loaded for editing");
                return;
            }
            // validation basique
            if (tarifGreenfee.getSeason() == null || tarifGreenfee.getStartDate() == null || tarifGreenfee.getEndDate() == null) {
                showMessageFatal("Season, start date and end date are required");
                return;
            }
            if (!tarifGreenfeeController.validPeriod(tarifGreenfee.getDatesSeasonsList(), tarifGreenfee.getSeason())) {
                // période n'existe pas encore — on l'ajoute
            }
            entite.TarifGreenfee.DatesSeasons period = new entite.TarifGreenfee.DatesSeasons();
            period.setSeason(tarifGreenfee.getSeason());
            period.setStartDate(tarifGreenfee.getStartDate());
            period.setEndDate(tarifGreenfee.getEndDate());
            tarifGreenfee.getDatesSeasonsList().add(period);
            LOG.debug("{} - datesSeasonsList size after add={}", methodName, tarifGreenfee.getDatesSeasonsList().size());

            boolean ok = updateTarifGreenfeeJsonService.update(tarifGreenfee);
            if (ok) {
                tarifGreenfeeList.invalidateCache();
                // reset work fields
                tarifGreenfee.setSeason(null);
                tarifGreenfee.setStartDate(null);
                tarifGreenfee.setEndDate(null);
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // WIZARD — version ajax (retourne void, reste sur la page)
    // ========================================

    public void inputTarifGreenfeeWizard(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("param = {}", param);
            LOG.debug("tarifGreenfee = {}", tarifGreenfee);
            switch (param) {
                case "PE" -> {
                    // tarifCourseId is now set from the wizard's course selector
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeePeriods(tarifGreenfee);
                }
                case "BA" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeBasic(tarifGreenfee);
                case "DA" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeDays(tarifGreenfee);
                case "TW" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeTwilight(tarifGreenfee);
                case "EQ" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeEquipments(tarifGreenfee);
                case "HO" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeHours(tarifGreenfee);
                default -> {
                    String msg = "failed in default switch";
                    LOG.error(msg);
                    showMessageFatal(msg);
                }
            } // end switch
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // WIZARD — REMOVE PRICING ITEMS
    // ========================================

    public void removeBasicItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tarifGreenfee.getBasicList().remove(item);
            LOG.debug("basicList size after remove = {}", tarifGreenfee.getBasicList().size());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Returns the filtered equipment list for the current view:
     *  HO mode → filtered by season + slot ; other modes → filtered by season only. */
    public java.util.List<entite.EquipmentsAndBasic> getEquipmentsForDisplay() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null) return Collections.emptyList();
        return "HO".equals(tarifGreenfee.getGreenfeeType())
                ? getEquipmentsForCurrentSlot()
                : getEquipmentsForCurrentSeason();
    } // end method

    /**
     * Returns a human-readable label for a slot key: "HH:mm–HH:mm (item)" or "-- All slots --" if null.
     * Used in the equipments tab slot dropdown.
     */
    public String slotLabelFor(String slotKey) {
        if (slotKey == null) return "-- All slots --";
        return tarifGreenfee.getTeeTimesList().stream()
                .filter(tt -> slotKey.equals(tt.getSlotKey()))
                .findFirst()
                .map(tt -> tt.getStartTime() + "–" + tt.getEndTime() + " (" + tt.getItem() + ")")
                .orElse(slotKey);
    } // end method

    /** Returns "dd/MM – dd/MM" date range for a season code, or "" if not found.
     *  Used in the hoursTable "Période" column. */
    public String seasonPeriodFor(String season) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (season == null || tarifGreenfee == null) return "";
        return tarifGreenfee.getDatesSeasonsList().stream()
                .filter(ds -> season.equals(ds.getSeason()))
                .findFirst()
                .map(ds -> {
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                    return ds.getStartDate().format(fmt) + " – " + ds.getEndDate().format(fmt);
                })
                .orElse("");
    } // end method

    /** Returns the date range "dd/MM – dd/MM" for a season code in the current tarifGreenfee.
     *  Returns "" for season "A" (all seasons) or if the season is not found. */
    public String periodDatesFor(String season) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (season == null || "A".equals(season) || tarifGreenfee == null
                || tarifGreenfee.getDatesSeasonsList() == null) return "";
        return tarifGreenfee.getDatesSeasonsList().stream()
                .filter(ds -> season.equals(ds.getSeason()))
                .findFirst()
                .map(ds -> {
                    java.time.format.DateTimeFormatter fmt =
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM");
                    return ds.getStartDate().format(fmt) + " – " + ds.getEndDate().format(fmt);
                })
                .orElse("");
    } // end method

    /** Returns only the time range for a slot key: "HH:mm–HH:mm", or "" if null/not found.
     *  Used in the equipments table "Tranche" column. */
    public String slotTimeFor(String slotKey) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (slotKey == null || tarifGreenfee == null) return "";
        return tarifGreenfee.getTeeTimesList().stream()
                .filter(tt -> slotKey.equals(tt.getSlotKey()))
                .findFirst()
                .map(tt -> tt.getStartTime() + "–" + tt.getEndTime())
                .orElse("");
    } // end method

    /** Overload — resolves a slot key against a given TarifGreenfee (used in visualisation detail
     *  where the tarif may differ from the session's current tarifGreenfee, e.g. DB version). */
    public String slotTimeFor(entite.TarifGreenfee tarif, String slotKey) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (slotKey == null || tarif == null || tarif.getTeeTimesList() == null) return "—";
        return tarif.getTeeTimesList().stream()
                .filter(tt -> slotKey.equals(tt.getSlotKey()))
                .findFirst()
                .map(tt -> tt.getStartTime() + "–" + tt.getEndTime())
                .orElse("—");
    } // end method

    /** Returns the descriptive name of a slot (TeeTimes.item) for a given slotKey.
     *  Used in include_greenfee_equipments.xhtml to display the slot name column. */
    public String slotNameFor(String slotKey) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (slotKey == null || tarifGreenfee == null) return "";
        return tarifGreenfee.getTeeTimesList().stream()
                .filter(tt -> slotKey.equals(tt.getSlotKey()))
                .findFirst()
                .map(entite.TarifGreenfee.TeeTimes::getItem)
                .orElse("");
    } // end method

    /** Returns equipments for a given tarif filtered by a specific season.
     *  Used in the Raw ArrayLists dialog to show per-period equipment breakdown.
     *  season "A" or null → returns all equipments. */
    public java.util.List<entite.EquipmentsAndBasic> equipmentsForSeason(entite.TarifGreenfee tarif, String season) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null || tarif.getEquipmentsList() == null) return Collections.emptyList();
        if (season == null || "A".equals(season)) return tarif.getEquipmentsList();
        return tarif.getEquipmentsList().stream()
                .filter(e -> season.equals(e.getSeason()) || "A".equals(e.getSeason()))
                .collect(java.util.stream.Collectors.toList());
    } // end method

    public void removeTeeTimeItem(entite.TarifGreenfee.TeeTimes item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tarifGreenfee.getTeeTimesList().remove(item);
            LOG.debug("teeTimesList size after remove = {}", tarifGreenfee.getTeeTimesList().size());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Returns equipments filtered by the currently selected season (workSeason).
     *  "A" or null → all equipments. Specific season → entries for that season + "A" entries. */
    public java.util.List<entite.EquipmentsAndBasic> getEquipmentsForCurrentSeason() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getEquipmentsList() == null) return Collections.emptyList();
        String season = tarifGreenfee.getWorkSeason();
        if (season == null || season.isBlank() || "A".equals(season)) return tarifGreenfee.getEquipmentsList();
        return tarifGreenfee.getEquipmentsList().stream()
                .filter(e -> season.equals(e.getSeason()) || "A".equals(e.getSeason()))
                .collect(java.util.stream.Collectors.toList());
    } // end method

    /** Returns equipments filtered by selected season AND selected slot (workLinkedSlotKey).
     *  Only active in HO mode. If no slot selected (null) → shows all for the season. */
    public java.util.List<entite.EquipmentsAndBasic> getEquipmentsForCurrentSlot() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getEquipmentsList() == null) return Collections.emptyList();
        String season   = tarifGreenfee.getWorkSeason();
        String slotKey  = tarifGreenfee.getWorkLinkedSlotKey();
        return tarifGreenfee.getEquipmentsList().stream()
                .filter(e -> season == null || "A".equals(season)
                          || season.equals(e.getSeason()) || "A".equals(e.getSeason()))
                .filter(e -> slotKey == null
                          || slotKey.equals(e.getLinkedSlotKey()))
                .collect(java.util.stream.Collectors.toList());
    } // end method

    /** HO mode — when a slot is selected, derive workSeason from that slot's season.
     *  Called via p:ajax listener on equipSlot. */
    public void syncSeasonFromSlot() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null) return;
        String slotKey = tarifGreenfee.getWorkLinkedSlotKey();
        if (slotKey == null) {
            LOG.debug("slot cleared — workSeason unchanged");
            return;
        }
        tarifGreenfee.getTeeTimesList().stream()
                .filter(t -> slotKey.equals(t.getSlotKey()))
                .findFirst()
                .ifPresent(t -> {
                    tarifGreenfee.setWorkSeason(t.getSeason());
                    LOG.debug("workSeason set to {} from slot {}", t.getSeason(), slotKey);
                });
    } // end method

    /** Returns tee times filtered by the currently selected season (workSeason). */
    public java.util.List<entite.TarifGreenfee.TeeTimes> getTeeTimesForCurrentSeason() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee == null || tarifGreenfee.getTeeTimesList() == null) return Collections.emptyList();
        String season = tarifGreenfee.getWorkSeason();
        if (season == null || season.isBlank() || "A".equals(season)) return tarifGreenfee.getTeeTimesList();
        return tarifGreenfee.getTeeTimesList().stream()
                .filter(t -> season.equals(t.getSeason()))
                .collect(java.util.stream.Collectors.toList());
    } // end method

    /** HO mode — when season changes, reset workLinkedSlotKey to null to avoid JSF "value is not valid" error.
     *  The PrimeFaces SelectOneMenu keeps the hidden input value even after the list changes via ajax — the
     *  stale slotKey would fail validation against the new season's list. */
    public void resetSlotKeyOnSeasonChange() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarifGreenfee != null) {
            tarifGreenfee.setWorkLinkedSlotKey(null);
            LOG.debug("workLinkedSlotKey reset on season change");
        }
    } // end method


    public void removeEquipmentItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tarifGreenfee.getEquipmentsList().remove(item);
            LOG.debug("equipmentsList size after remove = {}", tarifGreenfee.getEquipmentsList().size());
            persistIfExists(methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Charge un équipement existant dans les work fields pour édition. */
    public void editEquipmentItem(entite.EquipmentsAndBasic item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifGreenfee.setWorkSeason(item.getSeason());
        tarifGreenfee.setWorkItem(item.getItem());
        tarifGreenfee.setWorkPrice(item.getPrice());
        tarifGreenfee.setWorkLinkedSlotKey(item.getLinkedSlotKey());
        editingEquipment = item;
        LOG.debug("editingEquipment set to item={} season={}", item.getItem(), item.getSeason());
    } // end method

    /** Annule le mode édition sans modifier l'équipement. */
    public void cancelEditEquipment() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifGreenfee.setWorkSeason(null);
        tarifGreenfee.setWorkItem(null);
        tarifGreenfee.setWorkPrice(null);
        tarifGreenfee.setWorkLinkedSlotKey(null);
        editingEquipment = null;
    } // end method

    /** Valide et remplace l'équipement en cours d'édition. */
    public void updateEquipmentItem() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (tarifGreenfee.getWorkItem() == null || tarifGreenfee.getWorkItem().isBlank()) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.equipment.item.required"));
                return;
            }
            if (tarifGreenfee.getWorkPrice() == null) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.equipment.price.required"));
                return;
            }
            editingEquipment.setSeason(tarifGreenfee.getWorkSeason());
            editingEquipment.setItem(tarifGreenfee.getWorkItem());
            editingEquipment.setPrice(tarifGreenfee.getWorkPrice());
            editingEquipment.setLinkedSlotKey(tarifGreenfee.getWorkLinkedSlotKey());
            LOG.debug("equipment updated to item={} season={} price={}", editingEquipment.getItem(),
                    editingEquipment.getSeason(), editingEquipment.getPrice());
            persistIfExists(methodName);
            tarifGreenfee.setWorkSeason(null);
            tarifGreenfee.setWorkItem(null);
            tarifGreenfee.setWorkPrice(null);
            tarifGreenfee.setWorkLinkedSlotKey(null);
            editingEquipment = null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public entite.EquipmentsAndBasic getEditingEquipment() { return editingEquipment; }

    public void removeDaysItem(entite.TarifGreenfee.DaysGreenfee item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tarifGreenfee.getDaysList().remove(item);
            LOG.debug("daysList size after remove = {}", tarifGreenfee.getDaysList().size());
            persistIfExists(methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void removeTwilightItem(entite.TarifGreenfee.Twilight item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            tarifGreenfee.getTwilightList().remove(item);
            LOG.debug("twilightList size after remove = {}", tarifGreenfee.getTwilightList().size());
            persistIfExists(methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Charge une période existante dans les work fields pour édition. */
    public void editPeriodItem(entite.TarifGreenfee.DatesSeasons item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifGreenfee.setSeason(item.getSeason());
        tarifGreenfee.setStartDate(item.getStartDate());
        tarifGreenfee.setEndDate(item.getEndDate());
        editingPeriod = item;
        LOG.debug("editingPeriod set to season={}", item.getSeason());
    } // end method

    /** Annule le mode édition sans modifier la période. */
    public void cancelEditPeriod() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifGreenfee.setSeason(null);
        tarifGreenfee.setStartDate(null);
        tarifGreenfee.setEndDate(null);
        editingPeriod = null;
    } // end method

    /** Valide et remplace la période en cours d'édition. */
    public void updatePeriodItem() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (tarifGreenfee.getStartDate() == null || tarifGreenfee.getEndDate() == null) {
                showMessageFatal("Start date and end date are required.");
                return;
            }
            if (!tarifGreenfee.getStartDate().isBefore(tarifGreenfee.getEndDate())) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.member.endbeforestart"));
                return;
            }
            // overlap check en excluant la période en cours d'édition
            tarifGreenfee.getDatesSeasonsList().remove(editingPeriod);
            boolean overlap = tarifGreenfeeController.overlapCheckPeriods(tarifGreenfee);
            if (overlap) {
                tarifGreenfee.getDatesSeasonsList().add(editingPeriod); // restaurer
                return; // overlapCheckPeriods a déjà affiché le message
            }
            // mise à jour en place
            editingPeriod.setSeason(tarifGreenfee.getSeason());
            editingPeriod.setStartDate(tarifGreenfee.getStartDate());
            editingPeriod.setEndDate(tarifGreenfee.getEndDate());
            tarifGreenfee.getDatesSeasonsList().add(editingPeriod);
            LOG.debug("period updated to season={} start={} end={}", editingPeriod.getSeason(),
                    editingPeriod.getStartDate(), editingPeriod.getEndDate());
            persistIfExists(methodName);
            // housekeeping
            tarifGreenfee.setSeason(null);
            tarifGreenfee.setStartDate(null);
            tarifGreenfee.setEndDate(null);
            editingPeriod = null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public entite.TarifGreenfee.DatesSeasons getEditingPeriod() { return editingPeriod; }

    public void removePeriodItem(entite.TarifGreenfee.DatesSeasons item) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (tarifGreenfee.getDatesSeasonsList().size() <= 1) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.period.last"));
                return;
            }
            tarifGreenfee.getDatesSeasonsList().remove(item);
            LOG.debug("datesSeasonsList size after remove = {}", tarifGreenfee.getDatesSeasonsList().size());
            persistIfExists(methodName);
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /** Persiste le TarifGreenfee en DB si un tarifId existe (tarif déjà sauvegardé). */
    private void persistIfExists(String callerMethodName) throws SQLException {
        if (tarifGreenfee != null && tarifGreenfee.getTarifId() != null) {
            boolean ok = updateTarifGreenfeeJsonService.update(tarifGreenfee);
            if (ok) {
                tarifGreenfeeList.invalidateCache();
                LOG.debug("{} - DB updated and cache invalidated", callerMethodName);
            }
        }
    } // end method

    // ========================================
    // WIZARD FLOW LISTENER — GREENFEE
    // ========================================

    public String onTarifGreenfeeFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldStep = {}", event.getOldStep());
        LOG.debug("newStep = {}", event.getNewStep());

        // Forward: Periods → Pricing — require at least one period
        if ("PeriodsTab".equals(event.getOldStep()) && "PricingTab".equals(event.getNewStep())) {
            if (tarifGreenfee == null || tarifGreenfee.getDatesSeasonsList() == null
                    || tarifGreenfee.getDatesSeasonsList().isEmpty()) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.period.required"));
                return event.getOldStep();
            }
        }

        // Forward: Pricing → Equipments — require at least one pricing entry
        if ("PricingTab".equals(event.getOldStep()) && "EquipmentsTab".equals(event.getNewStep())) {
            if (tarifGreenfee == null
                    || (tarifGreenfee.getTeeTimesList().isEmpty()
                        && tarifGreenfee.getBasicList().isEmpty()
                        && tarifGreenfee.getDaysList().isEmpty())) {
                showMessageFatal(LCUtil.prepareMessageBean("tarif.pricing.required"));
                return event.getOldStep();
            }
            // Reset workSeason to "A" so the slot dropdown shows all slots on arrival
            tarifGreenfee.setWorkSeason("A");
        }

        // Forward: Equipments → Confirm — equipments are optional, no check required

        return event.getNewStep();
    } // end method

    // ========================================
    // WIZARD — TARIF MEMBER (version ajax, reste sur la page)
    // ========================================

    public void inputTarifMemberWizard(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            LOG.debug("param = {}", param);
            LOG.debug("tarifMember = {}", tarifMember);
            switch (param) {
                case "CO" -> tarifMember = tarifMemberController.inputTarifMembersCotisation(tarifMember);
                case "EQ" -> tarifMember = tarifMemberController.inputTarifMembersEquipments(tarifMember);
                default -> {
                    String msg = "failed in default switch";
                    LOG.error(msg);
                    showMessageFatal(msg);
                }
            } // end switch
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // WIZARD FLOW LISTENER — MEMBER
    // ========================================

    public String onTarifMemberFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldStep = {}", event.getOldStep());
        LOG.debug("newStep = {}", event.getNewStep());
        return event.getNewStep();
    } // end method

    // ========================================
    // RESET
    // ========================================

    public void resetAll() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        tarifMember   = new TarifMember();
        tarifGreenfee = new TarifGreenfee();
        greenfee      = new Greenfee();
        subscription  = new Subscription();
        tarifSubscription = new entite.TarifSubscription();
        LOG.debug("MemberController reset complete");
    } // end method

    // ========================================
    // WIZARD — TARIF SUBSCRIPTION
    // ========================================

    public void inputTarifSubscriptionWizard(String code) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("code = {}", code);
        try {
            LOG.debug("tarifSubscription = {}", tarifSubscription);

            if (code == null || code.isBlank()) {
                showMessageFatal("Subscription type is required");
                return;
            }
            if (tarifSubscription.getWorkPrice() == null || tarifSubscription.getWorkPrice() <= 0) {
                showMessageFatal("Price must be greater than 0");
                return;
            }
            if (tarifSubscription.getWorkStartDate() == null) {
                showMessageFatal("Start date is required");
                return;
            }
            if (tarifSubscription.getWorkEndDate() == null) {
                showMessageFatal("End date is required");
                return;
            }
            if (tarifSubscription.getWorkEndDate().isBefore(tarifSubscription.getWorkStartDate())) {
                showMessageFatal("End date must be after start date");
                return;
            }
            if (tarifSubscription.getWorkEndDate().isEqual(tarifSubscription.getWorkStartDate())) {
                showMessageFatal("End date must be different from start date");
                return;
            }

            entite.TarifSubscription newTarif = new entite.TarifSubscription();
            newTarif.setCode(code);
            newTarif.setPrice(tarifSubscription.getWorkPrice());
            newTarif.setStartDate(tarifSubscription.getWorkStartDate());
            newTarif.setEndDate(tarifSubscription.getWorkEndDate());

            if (createTarifSubscriptionService.create(newTarif)) {
                cacheInvalidator.invalidateSubscriptionCaches(); // centralized 2026-03-22
                // update in-memory list instead of re-querying DB
                List<entite.TarifSubscription> current = tarifSubscription.getTarifList();
                if (current == null) {
                    current = new java.util.ArrayList<>();
                }
                current.add(newTarif);
                tarifSubscription.setTarifList(current);
                // reset work fields for next entry
                tarifSubscription.setWorkPrice(null);
                tarifSubscription.setWorkStartDate(null);
                tarifSubscription.setWorkEndDate(null);
                showMessageInfo("Tarif subscription added: " + newTarif.getCode() + " = " + newTarif.getPrice());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void deleteTarifSubscription() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            if (deleteTarifSubscriptionService.deleteAll()) {
                cacheInvalidator.invalidateSubscriptionCaches(); // centralized 2026-03-22
                tarifSubscription.setTarifList(new java.util.ArrayList<>()); // empty — no need to query DB after deleteAll
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String onTarifSubscriptionFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("oldStep = {}", event.getOldStep());
        LOG.debug("newStep = {}", event.getNewStep());
        try {
            // reset work fields when switching between Monthly and Yearly tabs
            String oldStep = event.getOldStep();
            String newStep = event.getNewStep();
            if ((oldStep.contains("Monthly") && newStep.contains("Yearly"))
                    || (oldStep.contains("Yearly") && newStep.contains("Monthly"))) {
                tarifSubscription.setWorkPrice(null);
                tarifSubscription.setWorkStartDate(null);
                tarifSubscription.setWorkEndDate(null);
                LOG.debug("work fields reset for tab switch");
            }
            // load list when entering confirmation tab
            if (newStep.contains("Confirmation")) {
                tarifSubscription.setTarifList(tarifSubscriptionList.list());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
        return event.getNewStep();
    } // end method

    public String confirmTarifSubscription() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        try {
            var list = tarifSubscriptionList.list();
            if (list.isEmpty()) {
                showMessageFatal("No tarif subscription to confirm");
                return null;
            }
            showMessageInfo("Tarif subscriptions confirmed: " + list.size() + " entries");
            tarifSubscription = new entite.TarifSubscription();
            return "welcome.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // === Getter/Setter TarifSubscription ===

    public entite.TarifSubscription getTarifSubscription() {
        if (tarifSubscription == null) tarifSubscription = new entite.TarifSubscription();
        return tarifSubscription;
    } // end method

    public void setTarifSubscription(entite.TarifSubscription tarifSubscription) {
        this.tarifSubscription = tarifSubscription;
    } // end method

    /** Walk the SQLException cause chain looking for MySQL error 1062 (duplicate entry). */
    private static boolean isMysqlDuplicate(java.sql.SQLException e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof java.sql.SQLException sql && sql.getErrorCode() == 1062) return true;
            t = t.getCause();
        }
        return false;
    } // end method

    // =========================================================================
    // EquipmentDisplayRow — flat row DTO for the period → slot → equipment view
    // =========================================================================

    /** Flat row for the equipment hierarchy: period → slot → equipment.
     *  firstOfSeason / firstOfSlot flags drive p:headerRow rendering. */
    public static class EquipmentDisplayRow implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final String  season;
        private final String  slotLabel;     // null = non-HO ; "— all slots —" = unlinked HO
        private final String  item;
        private final Double  price;
        private final boolean firstOfSeason;
        private final boolean firstOfSlot;

        public EquipmentDisplayRow(String season, String slotLabel, String item, Double price,
                                   boolean firstOfSeason, boolean firstOfSlot) {
            this.season        = season;
            this.slotLabel     = slotLabel;
            this.item          = item;
            this.price         = price;
            this.firstOfSeason = firstOfSeason;
            this.firstOfSlot   = firstOfSlot;
        } // end constructor

        public String  getSeason()        { return season; }
        public String  getSlotLabel()     { return slotLabel; }
        public String  getItem()          { return item; }
        public Double  getPrice()         { return price; }
        public boolean isFirstOfSeason()  { return firstOfSeason; }
        public boolean isFirstOfSlot()    { return firstOfSlot; }
    } // end class EquipmentDisplayRow

    /** Builds a flat sorted list of EquipmentDisplayRow for hierarchy rendering.
     *  Order: datesSeasonsList order → teeTimesList order → item.
     *  Works for both HO (with slots) and non-HO (no slot level). */
    public java.util.List<EquipmentDisplayRow> buildEquipmentDisplayRows(entite.TarifGreenfee tarif) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (tarif == null || tarif.getEquipmentsList() == null || tarif.getEquipmentsList().isEmpty()) {
            return Collections.emptyList();
        }
        boolean isHO = "HO".equals(tarif.getGreenfeeType());
        java.util.List<EquipmentDisplayRow> rows = new java.util.ArrayList<>();

        java.util.List<String> seasons;
        if (tarif.getDatesSeasonsList() == null || tarif.getDatesSeasonsList().isEmpty()) {
            seasons = java.util.List.of("A");
        } else {
            seasons = tarif.getDatesSeasonsList().stream()
                    .map(ds -> ds.getSeason()).distinct()
                    .collect(java.util.stream.Collectors.toList());
        }

        String prevSeason    = null;
        String prevSlotLabel = null;

        for (String season : seasons) {
            java.util.List<entite.EquipmentsAndBasic> seasonEquips = tarif.getEquipmentsList().stream()
                    .filter(e -> season.equals(e.getSeason()) || "A".equals(e.getSeason()))
                    .collect(java.util.stream.Collectors.toList());
            if (seasonEquips.isEmpty()) continue;

            if (isHO) {
                java.util.List<entite.TarifGreenfee.TeeTimes> slots =
                        tarif.getTeeTimesList() == null ? Collections.emptyList() :
                        tarif.getTeeTimesList().stream()
                                .filter(tt -> season.equals(tt.getSeason()) || "A".equals(tt.getSeason()))
                                .collect(java.util.stream.Collectors.toList());

                // Linked equipments — in teeTimesList order
                for (entite.TarifGreenfee.TeeTimes slot : slots) {
                    java.util.List<entite.EquipmentsAndBasic> slotEquips = seasonEquips.stream()
                            .filter(e -> slot.getSlotKey().equals(e.getLinkedSlotKey()))
                            .collect(java.util.stream.Collectors.toList());
                    if (slotEquips.isEmpty()) continue;
                    String slotLabel = slot.getStartTime() + "–" + slot.getEndTime()
                            + (slot.getItem() != null && !slot.getItem().isBlank()
                               ? "  (" + slot.getItem() + ")" : "");
                    for (entite.EquipmentsAndBasic e : slotEquips) {
                        boolean fSeason = !season.equals(prevSeason);
                        boolean fSlot   = fSeason || !slotLabel.equals(prevSlotLabel);
                        prevSeason    = season;
                        prevSlotLabel = slotLabel;
                        rows.add(new EquipmentDisplayRow(season, slotLabel, e.getItem(), e.getPrice(), fSeason, fSlot));
                    }
                }

                // Unlinked equipments (no slot)
                java.util.List<entite.EquipmentsAndBasic> unlinked = seasonEquips.stream()
                        .filter(e -> e.getLinkedSlotKey() == null)
                        .collect(java.util.stream.Collectors.toList());
                if (!unlinked.isEmpty()) {
                    String slotLabel = "— all slots —";
                    for (entite.EquipmentsAndBasic e : unlinked) {
                        boolean fSeason = !season.equals(prevSeason);
                        boolean fSlot   = fSeason || !slotLabel.equals(prevSlotLabel);
                        prevSeason    = season;
                        prevSlotLabel = slotLabel;
                        rows.add(new EquipmentDisplayRow(season, slotLabel, e.getItem(), e.getPrice(), fSeason, fSlot));
                    }
                }

            } else {
                // Non-HO: no slot level
                for (entite.EquipmentsAndBasic e : seasonEquips) {
                    boolean fSeason = !season.equals(prevSeason);
                    prevSeason    = season;
                    prevSlotLabel = "";
                    rows.add(new EquipmentDisplayRow(season, "", e.getItem(), e.getPrice(), fSeason, false));
                }
            }
        }

        LOG.debug("buildEquipmentDisplayRows size = {}", rows.size());
        return rows;
    } // end method

} // end class
