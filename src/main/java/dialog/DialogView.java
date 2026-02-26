
package dialog;

// ========================================
// DialogView - Enumération des vues
// ========================================

/**
 * Énumération des différentes vues de dialogue disponibles.
 * <p>
 * Chaque vue correspond à un fichier XHTML dans l'application.
 * </p>
 */
public enum DialogView {
    
    UNAVAILABLE("dialogUnavailable.xhtml"),
    HANDICAP_INDEX("dialog_handicap_index.xhtml"),
    FLIGHT("dialogFlight.xhtml"),
    MATCHPLAY_CLASSMENT("dialog_matchplay_classment.xhtml"),
    CLUB_DETAIL("dialogClubDetail.xhtml"),
    CLUB_SELECT("dialogClub.xhtml"),
    COURSE_SELECT("dialogCourse.xhtml"),
    PLAYER_SELECT("dialogPlayers.xhtml"),
    WEATHER("dialogWeather.xhtml"),
    ROUND_SELECT("dialogRound.xhtml"),
    PLAYED_ROUNDS("dialog_played_rounds.xhtml");
    
    private final String viewName;
    
    DialogView(String viewName) {
        this.viewName = viewName;
    }
    
    /**
     * Retourne le nom du fichier de vue XHTML.
     *
     * @return le nom de la vue
     */
    public String getViewName() {
        return viewName;
    }
    
    @Override
    public String toString() {
        return viewName;
    }
} // end


