
package enumeration;

import java.util.Arrays;
// lien entre le menu et les selections d'un club
public enum ClubSelectionPurpose {

    CREATE_TARIF_MEMBER("TarifMember", "selectTarifMember.xhtml"),
    CREATE_PLAYER("CreatePlayer", "selectPlayer.xhtml"),
    PAYMENT_COTISATION("PaymentCotisation", "selectClubDialog.xhtml"),
    CREATE_COMPETITION("CreateCompetition", "selectCompetition.xhtml"),
    LOCAL_ADMIN("LocalAdministrator", "local_administrator2.xhtml"), // ok 01-02-2026
    CREATE_PRO("CreatePro", "professional.xhtml"),
    MENU_UNAVAILABLE("MenuUnavailable", "unavailable_menu.xhtml"),
    CREATE_ROUND("CreateRound", "selectClubCourse.xhtml");
//    VOLUNTARY_PAYMENT("PaymentCotisationSpontaneous", "selectClubDialog.xhtml");
    // first field = menu String code
    // second field = facelet
    
    private final String code;
    private final String targetPage;

    ClubSelectionPurpose(String code, String targetPage) {
        this.code = code;
        this.targetPage = targetPage;
    }

    public String getCode() {
        return code;
    }

    public String navigation() {
        return targetPage + "?faces-redirect=true";  // yes !!
    }

    public static ClubSelectionPurpose fromCode(String code) {
        return Arrays.stream(values())
                .filter(p -> p.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(CREATE_PLAYER);
    }
}
/* utilisation :



*/
