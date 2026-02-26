package Controller.refact;

import context.ApplicationContext;
import entite.*;
import entite.composite.ECourseList;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.SessionMap;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import manager.MemberManager;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    @Inject private Controllers.CourseController         courseController;       // pour reset() — migrated 2026-02-25
    @Inject private lists.ClubsListLocalAdmin            clubsListLocalAdmin;   // migrated 2026-02-25
    @Inject private lists.CoursesListLocalAdmin          coursesListLocalAdmin; // migrated 2026-02-25
    @Inject private lists.SubscriptionRenewalList        subscriptionRenewalList; // migrated 2026-02-25
    @Inject @SessionMap private Map<String, Object>      sessionMap;

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
        LOG.debug("MemberController initialized");
    } // end method

    // ========================================
    // TARIF LOOKUP / FIND (5 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String findTarifGreenfee() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("player = " + appContext.getPlayer().toString());
            LOG.debug("course = " + course.toString());
            sessionMap.put("inputSelectCourse", "createTarifGreenfee");
            tarifGreenfee = findTarifGreenfeeData.find(round);
            if (tarifGreenfee == null) {
                String err = "Tarif returned from findTarifdata is null ";
                LOG.debug(err);
                return null;
            }
            LOG.debug("now tarifGreenfee found is = " + tarifGreenfee);
            tarifGreenfee = new calc.CalcTarifGreenfee().calc(tarifGreenfee, round, club, appContext.getPlayer());
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
            LOG.debug(" for round = " + round);
            LOG.debug(" for club = " + club);
            tarifMember = findTarifMembersData.find(club, round);
            LOG.debug("TarifMember found = " + tarifMember);
            sessionMap.put("inputSelectCourse", "createTarifMember");
            sessionMap.put("inputSelectClub", "createTarifMember");
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
            club = ecl.club();
            course = ecl.course();
            round = ecl.round();
            tarifGreenfee = findTarifGreenfeeData.find(round);
            if (tarifGreenfee == null) {
                String msg = "No Tarif available for this course";
                LOG.error(msg);
                showMessageFatal(msg);
            } else {
                String msg = "Tarif returned = " + tarifGreenfee.toString();
                LOG.info(msg);
                showMessageInfo(msg);
            }
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";
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
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    public String showTarifGreenfee(String idcourse) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            tarifGreenfee = findTarifGreenfeeData.find(round);
            String msg = prepareMessageBean("tarif.greenfee.show") + tarifGreenfee.showTarifGreenfee();
            LOG.info(msg);
            showMessageInfo(msg);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
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
                    tarifGreenfee.setTarifCourseId(course.getIdcourse());
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

    public String createTarifMembers() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("with tarifMember = " + tarifMember);
            LOG.debug("for club = " + club);
            tarifMember.setTarifMemberIdClub(club.getIdclub());
            if (createTarifMemberService.create(tarifMember)) {
                String msg = "Tarif is created ";
                LOG.info(msg);
                showMessageInfo(msg);
                return "tarif_members_menu.xhtml?faces-redirect=true";
            } else {
                String msg = "Tarif is NOT created ";
                LOG.error(msg);
                showMessageFatal(msg);
                return "tarif_members_menu.xhtml?faces-redirect=true";
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
            LOG.debug("with club = " + club);
            if (createTarifGreenfeeService.create(tarifGreenfee, club)) {
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
            LOG.debug("with course = " + course);
            LOG.debug("with TarifGreenfee = " + tarifGreenfee);
            tarifGreenfee.setTarifCourseId(course.getIdcourse());
            if (deleteTarifGreenfeeService.delete(tarifGreenfee, year)) {
                String msg = "TarifGreenfee deleted = " + tarifGreenfee;
                LOG.info(msg);
            } else {
                String msg = "Result of deleteTarifGreenfee is NOT OK = " + tarifGreenfee;
                LOG.error(msg);
            }
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";
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
            LOG.debug("with club = " + club);
            LOG.debug("with Tarifmember = " + tarifMember);
            tarifMember.setTarifMemberIdClub(club.getIdclub());
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
            if (sessionMap.get("inputSelectPaiement").equals("Greenfees")) {
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
            if (sessionMap.get("inputSelectPaiement").equals("Greenfees")) {
                return localAdminCotisationList.list(appContext.getPlayer());
            }
            if (sessionMap.get("inputSelectPaiement").equals("Members")) {
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
    // NAVIGATION to_* (3 methodes)
    // migrated from CourseController 2026-02-25
    // ========================================

    public String to_selectLocalAdmin_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        try {
            courseController.reset("Reset to_selectLocalAdmin" + s);
            sessionMap.put("inputSelectPaiement", s);
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
            courseController.reset("Reset to_selectSystemAdmin" + s);
            sessionMap.put("inputSelectSubscriptions", s);
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
            courseController.reset("Reset to_selectPro" + s);
            sessionMap.put("inputSelectPaiement", s);
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
        LOG.debug("MemberController reset complete");
    } // end method

} // end class
