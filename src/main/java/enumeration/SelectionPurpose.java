package enumeration;
import java.util.Arrays;

// Lien entre le menu et les sélections d'un club
public enum SelectionPurpose {
    CREATE_TARIF_MEMBER("CreateTarifMember", "selectClubLocalAdmin.xhtml", "tarif_member_wizard.xhtml"),
    CREATE_TARIF_GREENFEE("CreateTarifGreenfee", "selectCourseLocalAdmin.xhtml", "tarif_greenfee_wizard.xhtml"),
    CREATE_PLAYER("CreatePlayer", "selectPlayer.xhtml", "??player.xhtml"),
    SHOW_PLAYER("ShowHandicap", "show_handicap_whs.xhtml", "??player.xhtml"),
    SHOW_ROUND("ShowRound", "show_played_rounds.xhtml", "??player.xhtml"),
    PAYMENT_COTISATION("PaymentCotisation", "selectClubDialog.xhtml", "??paymentCotisation.xhtml"),
   // CREATE_COMPETITION("CreateCompetition", "selectCompetition.xhtml", "??competition.xhtml"),
    LOCAL_ADMIN("LocalAdministrator", "local_administrator.xhtml", "??local_administrator_final.xhtml"),
    CREATE_PRO("CreatePro", "professional.xhtml", "not used"),
    COMPETITION_DESCRIPTION("Competition Description", "competition_create_description.xhtml", "not used"),
    
    MENU_UNAVAILABLE("MenuUnavailable", "unavailable_menu.xhtml", "??unavailable_final.xhtml"),
  //  CREATE_ROUND("CreateRound", "selectClubCourse.xhtml", "round.xhtml");  // OK 04-02-2026
    CREATE_ROUND("CreateRound", "round.xhtml", "??round.xhtml"),  // OK 13-02-2026
    UPDATE_CLUB("clubUpdate", "selectClubModify.xhtml", "??round.xhtml"),  // OK 14-02-2026
    CREATE_CLUB("clubCreate", "club.xhtml", "??round.xhtml"),  // OK 14-02-2026
    DELETE_CLUB("clubDelete", "selectClubDelete.xhtml", "??round.xhtml"),  // OK 14-02-2026
    CREATE_TARIF_SUBSCRIPTION("CreateTarifSubscription", "tarif_subscription_wizard.xhtml", "tarif_subscription_wizard.xhtml");
    
    // First field = menu String code - index
    // Second field = selection facelet
    // Third field = final destination facelet
    
    private final String code;
    private final String targetPage;
    private final String finalPage;
    
    SelectionPurpose(String code, String targetPage, String finalPage) {
        this.code = code;
        this.targetPage = targetPage;
        this.finalPage = finalPage;
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