package Controller.refact;

import context.ApplicationContext;
import entite.*;
import entite.composite.ECourseList;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.annotation.SessionMap;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import manager.MemberManager;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import exceptions.AppException;
import static exceptions.LCException.handleGenericException;
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

    @Inject private MemberManager memberManager;
    @Inject private ApplicationContext appContext;
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

    // ========================================
    // ETAT UI LOCAL
    // ========================================

    private TarifMember tarifMember;
    private TarifGreenfee tarifGreenfee;
    private Cotisation cotisation;
    private Greenfee greenfee;
    private Subscription subscription;
    private Club club;
    private Course course;
    private Round round;
    private List<ECourseList> subscriptionRenewal;
    private List<?> filteredCars; // PrimeFaces dataTable filteredValue — migrated from navC 2026-02-28
    private entite.TarifSubscription tarifSubscription; // added 2026-03-06

    public MemberController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        tarifMember   = new TarifMember();
        tarifGreenfee = new TarifGreenfee();
        cotisation    = new Cotisation();
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
        LOG.debug("entering " + methodName + " — source: " + event.getSource());
        tarifMember         = new TarifMember();
        tarifGreenfee       = new TarifGreenfee();
        cotisation          = new Cotisation();
        greenfee            = new Greenfee();
        subscription        = new Subscription();
        club                = null;
        course              = null;
        round               = null;
        subscriptionRenewal = null;
        filteredCars        = null;
        tarifSubscription   = new entite.TarifSubscription();
        LOG.debug(methodName + " — MemberController reset done");
    } // end method

    // ========================================
    // TARIF LOOKUP / FIND (5 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String findTarifGreenfee() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
      //      LOG.debug("player = " + appContext.getPlayer().toString());
      //      LOG.debug("course = " + appContext.getCourse());
       // enlevé : pourquoi ??     appContext.setInputSelectCourse("createTarifGreenfee");
            tarifGreenfee = findTarifGreenfeeData.find(appContext.getRound());
            if (tarifGreenfee == null) {
                String err = "Tarif returned from findTarifdata is null ";
                LOG.debug(err);
                return null;
            }
            LOG.debug("now tarifGreenfee found is = " + tarifGreenfee);
            tarifGreenfee = calcTarifGreenfee.calc(tarifGreenfee, appContext.getRound(), appContext.getClub(), appContext.getPlayer()); // migrated 2026-02-28
            return "price_round_greenfee.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String findTarifCotisation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
        //    LOG.debug(" for round = " + appContext.getRound());
        //    LOG.debug(" for club = " + club);
            tarifMember = findTarifMembersData.find(appContext.getClub(), appContext.getRound());
            LOG.debug("TarifMember found = " + tarifMember);
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
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(" findTarif with ecl = " + ecl.toString());
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
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("  selectTarif, ecl = " + ecl.toString());
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
        LOG.debug("entering " + methodName);
        try {
            tarifGreenfee = findTarifGreenfeeData.find(appContext.getRound());
            if (tarifGreenfee == null) {
                // message already displayed by FindTarifGreenfeeData
                return;
            }
            String msg = prepareMessageBean("tarif.greenfee.show") + tarifGreenfee.showTarifGreenfee();
            LOG.info(msg);
            showMessageInfo(msg);
        } catch (Exception e) {
            LOG.error(methodName + " - " + e.getMessage(), e);
            showMessageFatal(e.getMessage());
        }
    } // end method

    // ========================================
    // TARIF INPUT (3 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String inputTarifMembersCotisation() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with Members tarif = " + tarifMember);
            LOG.debug("for club = " + club.toString());
            tarifMember = tarifMemberController.inputTarifMembersCotisation(tarifMember);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String inputTarifGreenfee(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with param " + param);
            LOG.debug("with tarif = " + tarifGreenfee);
            switch (param) {
                case "PE" -> {
                    tarifGreenfee.setTarifCourseId(appContext.getCourse().getIdcourse()); // mod 06/03/2026
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
                case "TW" -> {
                    showMessageInfo(tarifGreenfee.toString());
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeTwilight(tarifGreenfee);
                    return "tarif_greenfee_twilight.xhtml?faces-redirect=true";
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

    public String inputTarifMembersEquipments() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("for club = " + club);
            LOG.debug("tarifMembers = " + tarifMember);
            tarifMember = tarifMemberController.inputTarifMembersEquipments(tarifMember);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // TARIF CREATE / DELETE / SHOW (5 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String createTarifMember() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with tarifMember = " + tarifMember);
         //   LOG.debug("for club = " + club);
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

    public String createTarifGreenfee() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with tarifGreenfee = " + tarifGreenfee);
        //    LOG.debug("with club = " + club); // devrait être null
            if (createTarifGreenfeeService.create(tarifGreenfee, appContext.getClub())) { // mod LC 06/03/2026
                String msg = " TarifGreenfee Created ! ";
                LOG.info(msg);
                showMessageInfo(msg);
                return "welcome.xhtml?faces-redirect=true";
            } else {
                String msg = "Fatal Error creation tarif Greenfee";
                LOG.debug(msg);
                showMessageFatal(msg);
                return "welcome.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String deleteTarifGreenfee(String year) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
        //    LOG.debug("with course = " + appContext.getCourse());
            LOG.debug("with TarifGreenfee = " + tarifGreenfee);
            tarifGreenfee.setTarifCourseId(appContext.getCourse().getIdcourse());
            if (deleteTarifGreenfeeService.delete(tarifGreenfee, year)) {
                String msg = "TarifGreenfee deleted = " + tarifGreenfee;
                LOG.info(msg);
            } else {
                String msg = "Result of deleteTarifGreenfee is NOT OK = " + tarifGreenfee;
                LOG.error(msg);
            }
            return "tarif_greenfee_wizard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String showTarifMembers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("for club = " + club);
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
        LOG.debug("entering " + methodName);
        try {
        //    LOG.debug("with club = " + club);
            LOG.debug("with Tarifmember = " + tarifMember);
         //   tarifMember.setTarifMemberIdClub(club.getIdclub());
            tarifMember.setTarifMemberIdClub(appContext.getClub().getIdclub()); // mod LC 06/03/2026
            if (deleteTarifMemberService.delete(tarifMember)) {
                String msg = "TarifMember deleted = " + tarifMember;
                LOG.info(msg);
                showMessageInfo(msg);
            } else {
                String msg = "Result of deleteTarifMember is NOT OK = " + tarifGreenfee.getTarifCourseId();
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
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("for professional = " + appContext.getPlayer());
            return professionalListForPayments.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminGreenfee(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with param = " + s);
        try {
            LOG.debug("for local admin = " + appContext.getPlayer());
            LOG.debug("with role = " + appContext.getPlayer().getPlayerRole());
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
        LOG.debug("entering " + methodName);
        LOG.debug("with param = " + s);
        try {
            return systemAdminSubscriptionList.list();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminCotisation(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with param = " + s);
        try {
            LOG.debug("for local admin = " + appContext.getPlayer());
            LOG.debug("with role = " + appContext.getPlayer().getPlayerRole());
            if (appContext.getInputSelectPaiement().equals("Greenfees")) {
                return localAdminCotisationList.list(appContext.getPlayer());
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
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("for subscription = " + subscription);
            if (updateSubscription.modify(subscription)) {
                LOG.debug("after modifySubscription : we are OK " + subscription);
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
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("player = " + appContext.getPlayer());
            return clubsListLocalAdmin.list(appContext.getPlayer());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listLocalAdminCoursesList(String select, String admin) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("player = " + appContext.getPlayer());
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
        LOG.debug("entering " + methodName);
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
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "subscription.xhtml?faces-redirect=true";
    } // end method

    public String to_selectLocalAdmin_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        try {
            navigationController.reset("Reset to_selectLocalAdmin" + s);
            appContext.setInputSelectPaiement(s);
            if (s.equals("Members")) {
                return "local_administrator_cotisations.xhtml?faces-redirect=true";
            }
            if (s.equals("Greenfees")) {
                return "local_administrator_greenfees.xhtml?faces-redirect=true";
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String to_selectSystemAdmin_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
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
        LOG.debug("entering " + methodName + " with string = " + s);
        try {
            navigationController.reset("Reset to_selectPro" + s);
            appContext.setInputSelectPaiement(s);
            if (s.equals("Lessons")) {
                return "professional_lessons_paid.xhtml?faces-redirect=true";
            }
            if (s.equals("Inscription")) {
                return "selectProForClub.xhtml?faces-redirect=true&cmd=" + s;
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
        LOG.debug("entering " + methodName);
        if (tarifGreenfee == null) return 0;
        if (tarifGreenfee.getDatesSeasonsList() == null || tarifGreenfee.getDatesSeasonsList().isEmpty()) return 0;
        if (!tarifGreenfee.isEquipmentsReady()) return 1;
        if (!tarifGreenfee.isUpdateReady()) return 2;
        if (!tarifGreenfee.isTwilightDone()) return 3;
        return 4;
    } // end method

    // ========================================
    // WIZARD — version ajax (retourne void, reste sur la page)
    // ========================================

    public void inputTarifGreenfeeWizard(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with param " + param);
            LOG.debug("with tarif = " + tarifGreenfee);
            switch (param) {
                case "PE" -> {
                    tarifGreenfee.setTarifCourseId(appContext.getCourse().getIdcourse());
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeePeriods(tarifGreenfee);
                }
                case "BA" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeBasic(tarifGreenfee);
                case "DA" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeDays(tarifGreenfee);
                case "EQ" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeEquipments(tarifGreenfee);
                case "HO" -> tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeHours(tarifGreenfee);
                case "TW" -> {
                    showMessageInfo(tarifGreenfee.toString());
                    tarifGreenfee = tarifGreenfeeController.inputTarifGreenfeeTwilight(tarifGreenfee);
                }
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
    // WIZARD FLOW LISTENER — GREENFEE
    // ========================================

    public String onTarifGreenfeeFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - oldStep = " + event.getOldStep());
        LOG.debug(methodName + " - newStep = " + event.getNewStep());
        return event.getNewStep();
    } // end method

    // ========================================
    // WIZARD — TARIF MEMBER (version ajax, reste sur la page)
    // ========================================

    public void inputTarifMemberWizard(String param) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with param " + param);
            LOG.debug("with tarifMember = " + tarifMember);
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
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - oldStep = " + event.getOldStep());
        LOG.debug(methodName + " - newStep = " + event.getNewStep());
        return event.getNewStep();
    } // end method

    // ========================================
    // RESET
    // ========================================

    public void resetAll() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        tarifMember   = new TarifMember();
        tarifGreenfee = new TarifGreenfee();
        cotisation    = new Cotisation();
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
        LOG.debug("entering " + methodName);
        LOG.debug("with code = " + code);
        try {
            LOG.debug("with tarifSubscription = " + tarifSubscription);

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
                tarifSubscriptionList.invalidateCache();
                tarifSubscription.setTarifList(tarifSubscriptionList.list());
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
        LOG.debug("entering " + methodName);
        try {
            if (deleteTarifSubscriptionService.deleteAll()) {
                tarifSubscriptionList.invalidateCache();
                tarifSubscription.setTarifList(tarifSubscriptionList.list());
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public String onTarifSubscriptionFlow(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - oldStep = " + event.getOldStep());
        LOG.debug(methodName + " - newStep = " + event.getNewStep());
        try {
            // reset work fields when switching between Monthly and Yearly tabs
            String oldStep = event.getOldStep();
            String newStep = event.getNewStep();
            if ((oldStep.contains("Monthly") && newStep.contains("Yearly"))
                    || (oldStep.contains("Yearly") && newStep.contains("Monthly"))) {
                tarifSubscription.setWorkPrice(null);
                tarifSubscription.setWorkStartDate(null);
                tarifSubscription.setWorkEndDate(null);
                LOG.debug(methodName + " - work fields reset for tab switch");
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
        LOG.debug("entering " + methodName);
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
    }

    public void setTarifSubscription(entite.TarifSubscription tarifSubscription) {
        this.tarifSubscription = tarifSubscription;
    }

} // end class
