package enumeration;
import java.util.Arrays;

public enum SelectionPurpose {
    CREATE_TARIF_MEMBER("CreateTarifMember", "selectClubCourse.xhtml", "tarif_member_wizard.xhtml", "message.for.tarif.members"), // migrated 2026-03-23
    CREATE_TARIF_GREENFEE("CreateTarifGreenfee", "selectClubCourse.xhtml", "tarif_greenfee_wizard.xhtml", "message.for.tarif.greenfee"), // migrated 2026-03-23
    CHART_ROUND("ChartRound", "selectClubCourse.xhtml", "statChartCourse.xhtml", "message.for.detail"), // new 2026-03-23
    CHART_COURSE("ChartCourse", "selectClubCourse.xhtml", "statChartCourse.xhtml", "message.for.average"), // new 2026-03-23
    CREATE_PLAYER("CreatePlayer", "selectPlayer.xhtml", "??player.xhtml"),
    SHOW_PLAYER("ShowHandicap", "show_handicap_whs.xhtml", "??player.xhtml"),
    SHOW_ROUND("ShowRound", "show_played_rounds.xhtml", "??player.xhtml"),
    PAYMENT_COTISATION("PaymentCotisation", "selectClubDialog.xhtml", "cotisation.xhtml", "message.selectclub.cotisaton"),
    PAYMENT_SUBSCRIPTION("PaymentSubscription", "subscription.xhtml", "not used", "subscription.new"),
   // CREATE_COMPETITION("CreateCompetition", "selectCompetition.xhtml", "??competition.xhtml"),
    LOCAL_ADMIN("LocalAdministrator", "local_administrator.xhtml", "??local_administrator_final.xhtml"),
    CREATE_PRO("CreatePro", "professional.xhtml", "not used"),
    COMPETITION_DESCRIPTION("Competition Description", "competition_create_description.xhtml", "not used"),
    
    MENU_UNAVAILABLE("MenuUnavailable", "unavailable_wizard.xhtml", "unavailable_wizard.xhtml", "title.unavailable.structure"),
    GROUND_CONDITION_WIZARD("GroundConditionWizard", "ground_condition_wizard.xhtml", "ground_condition_wizard.xhtml", "menu.ground.condition"),
    GROUND_CONDITION_UPDATE("GroundConditionUpdate", "ground_condition_update.xhtml", "ground_condition_update.xhtml", "menu.ground.condition.update"),
    GROUND_CONDITION_DISPLAY("GroundConditionDisplay", "ground_condition_display.xhtml", "ground_condition_display.xhtml", "menu.ground.condition"),
    SIMULATION_GREENFEE("SimulationGreenfee", "greenfee_simulation.xhtml", "greenfee_simulation.xhtml", "menu.simulation.greenfee"),
  //  CREATE_ROUND("CreateRound", "selectClubCourse.xhtml", "round.xhtml");  // OK 04-02-2026
    CREATE_ROUND("CreateRound", "round.xhtml", "??round.xhtml", "message.for.round"),  // OK 13-02-2026
    UPDATE_CLUB("clubUpdate", "selectClubModify.xhtml", "??round.xhtml"),  // OK 14-02-2026
    CREATE_CLUB("clubCreate", "club.xhtml", "??round.xhtml"),  // OK 14-02-2026
    DELETE_CLUB("clubDelete", "selectClubDelete.xhtml", "??round.xhtml"),  // OK 14-02-2026
    CREATE_TARIF_SUBSCRIPTION("CreateTarifSubscription", "tarif_subscription_wizard.xhtml", "tarif_subscription_wizard.xhtml"),

    // Round selections — consolidated 2026-03-23
    ROUND_INSCRIPTION("INSCRIPTION", "selectRound.xhtml", "not used", "message.for.inscription"),
    ROUND_STABLEFORD("STABLEFORD", "selectRound.xhtml", "not used", "message.for.score"),
    ROUND_SCORECARD("SCORECARD", "selectRound.xhtml", "not used", "message.for.scorecard"),
    ROUND_PARTICIPANTS("PARTICIPANTS", "selectRound.xhtml", "not used", "message.for.participants"),
    ROUND_CHART("CHART", "selectRound.xhtml", "not used", "message.for.stat"),

    // Round calendar — selectClubCourse → schedule_round.xhtml — 2026-04-19
    SCHEDULE_ROUND("ScheduleRound", "selectClubCourse.xhtml", "schedule_round.xhtml", "menu.schedule.round"),

    // Direct-navigation pages — no selector — 2026-05-04
    JOB_SUBMITTER("JobSubmitter", "jobSubmitter.xhtml", "jobSubmitter.xhtml"),
    TECHNICAL_INFO("TechnicalInfo", "technical_info.xhtml", "technical_info.xhtml"),
    ADMIN_ADVANCED("AdminAdvanced", "admin_advanced.xhtml", "admin_advanced.xhtml"),
    AUDIT_CONNECTIONS("AuditConnections", "audit_connections.xhtml", "audit_connections.xhtml"),
    CALCULATOR_CSS("CalculatorCss", "calculator_css.xhtml", "calculator_css.xhtml"),
    SEARCH_GOLF("SearchGolf", "search_golf.xhtml", "search_golf.xhtml"),
    DOWNLOAD("Download", "download.xhtml", "download.xhtml"),

    // Playing Hcp — direct navigation, no selector page — 2026-03-23
    PLAYING_HCP_STABLEFORD("PlayingHcpStableford", "stableford_playing_hcp.xhtml", "not used"),
    PLAYING_HCP_SCRAMBLE("PlayingHcpScramble", "scramble_playing_hcp.xhtml", "not used"),
    PLAYING_HCP_OTHER("PlayingHcpOther", "othergames_playing_hcp.xhtml", "not used"),

    // Professional & Local/System Admin — direct navigation, no selector — 2026-05-04
    PRO_INSCRIPTION("ProInscription", "selectPro.xhtml", "selectPro.xhtml"),
    PRO_LESSONS("ProLessons", "professional_lessons_paid.xhtml", "professional_lessons_paid.xhtml"),
    LOCAL_ADMIN_PROFESSIONALS("LocalAdminProfessionals", "local_administrator_professionals.xhtml", "local_administrator_professionals.xhtml"),
    LOCAL_ADMIN_MEMBERS("LocalAdminMembers", "local_administrator_cotisations.xhtml", "local_administrator_cotisations.xhtml"),
    LOCAL_ADMIN_GREENFEES("LocalAdminGreenfees", "local_administrator_greenfees.xhtml", "local_administrator_greenfees.xhtml"),
    SYSTEM_ADMIN_SUBSCRIPTIONS("SystemAdminSubscriptions", "system_administrator_subscriptions.xhtml", "system_administrator_subscriptions.xhtml"),
    TARIF_PRO("TarifPro", "selectPro.xhtml", "tarif_professional_wizard.xhtml", "menu.tarif.pro");
    
    // First field = menu String code - index
    // Second field = selection facelet
    // Third field = final destination facelet
    
    private final String code;
    private final String targetPage;
    private final String finalPage;
    private final String dialogTitleKey;

    SelectionPurpose(String code, String targetPage, String finalPage) {
        this(code, targetPage, finalPage, "message.selectclub.delete");
    }

    SelectionPurpose(String code, String targetPage, String finalPage, String dialogTitleKey) {
        this.code = code;
        this.targetPage = targetPage;
        this.finalPage = finalPage;
        this.dialogTitleKey = dialogTitleKey;
    }

    public String getCode() {
        return code;
    }

    public String getTargetPage() {
        return targetPage;
    }

    public String getFinalPage() {
        return finalPage;
    }

    public String getDialogTitleKey() {
        return dialogTitleKey;
    }
    
    public String navigationToFirst() {
        return targetPage + "?faces-redirect=true";
    }
    
    public String navigationToFinal() {
        return finalPage + "?faces-redirect=true";
    }
    
    public static SelectionPurpose fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(CREATE_PLAYER);
    }
}