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
    SCHEDULE_ROUND("ScheduleRound", "selectClubCourse.xhtml", "schedule_round.xhtml", "message.for.schedule.round"),

    // Playing Hcp — direct navigation, no selector page — 2026-03-23
    PLAYING_HCP_STABLEFORD("PlayingHcpStableford", "stableford_playing_hcp.xhtml", "not used"),
    PLAYING_HCP_SCRAMBLE("PlayingHcpScramble", "scramble_playing_hcp.xhtml", "not used"),
    PLAYING_HCP_OTHER("PlayingHcpOther", "othergames_playing_hcp.xhtml", "not used");
    
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