package context;

import entite.*;
import entite.composite.ECompetition;
import entite.composite.ECourseList;
import entite.composite.EPlayerPassword;
import entite.composite.EUnavailable;
import java.util.Collections;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

import static interfaces.Log.LOG;
import java.util.List;

/**
 * Contexte applicatif de session
 * Contient UNIQUEMENT les données PARTAGÉES entre controllers
 * 
 * @author GolfLC
 * @version 3.1 - Suppression préfixes "current"
 */
@Named("appContext")
@SessionScoped
public class ApplicationContext implements Serializable {
    
    private static final long serialVersionUID = 1L;

    
    
    /** Partagé entre : PlayerController, RoundController, SubscriptionController */
    private Player player;
    
    /** Partagé entre : ClubController, RoundController, SubscriptionController */
    private Club club;
    
    /** Partagé entre : ClubController, RoundController */
    private Course course;
    
    /** Partagé entre : RoundController, PlayerController */
    private Round round;
    
    /** Partagé entre : PlayerController, RoundController */
    private HandicapIndex handicapIndex;

    /** Partagé entre : RoundController, CourseController */
    private Inscription inscription;

    /** Partagé entre : RoundController, CourseController */
    private ScoreStableford scoreStableford;

    // ========================================
    // 🎯 DONNÉES PARTAGÉES - Selected
    // ========================================
    private ECourseList selectedHandicap = null;
    private ECourseList selectedPlayedRound;
    private List<ECourseList> playedRounds = null;
    
    
    // ========================================
    // 🔧 DONNÉES DE TRAVAIL PARTAGÉES
    // ========================================
    
    /** Partagé entre : SubscriptionController, PaymentController */
    private Subscription subscription;
    
    /** Partagé entre : SubscriptionController, PaymentController */
    private Cotisation cotisation;

    /** Partagé entre : PaymentController, CreditcardController */
    private Creditcard creditcard;

    /** Partagé entre : RoundController, CourseController, ClubController */
    private ECompetition competition;

    /** Partagé entre : CourseController (unavailable flow), ClubController */
    private EUnavailable unavailable;

    // ========================================
    // 👥 PLAYERS SPÉCIAUX
    // ========================================
    
    private Player playerPro;
    private Player localAdmin;
    private Player playerTemp;
    private EPlayerPassword playerWithPassword;

    // ========================================
    // 🚦 FLAGS DE NAVIGATION
    // ========================================
    
    private boolean nextPlayer      = false;
    private boolean nextInscription = false;
    private boolean nextScorecard   = false;
    private boolean nextRound       = false;
    private boolean connected       = false;

    // ========================================
    // 🎨 ÉTAT DE L'INTERFACE
    // ========================================
    
    private String selectedMenuItem;
    private String lastVisitedPage;
    private String currentLanguage = "fr";

    // ========================================
    // ⚙️ INITIALISATION
    // ========================================
    
    @PostConstruct
    public void init() {
        LOG.debug("ApplicationContext @PostConstruct - initializing session");
        initializeAll();
    }
    
    private void initializeAll() {  // à synchroniser plus tard
        // Entités principales
    //    player          = new Player();  enlevé 19-02-2026
        club            = new Club();
        course          = new Course();
        round           = new Round();
        handicapIndex   = new HandicapIndex();
        inscription     = new Inscription();
        scoreStableford = new ScoreStableford();

        // Compétition
        competition     = new ECompetition(new CompetitionDescription(), new CompetitionData());

        // Données de travail
        subscription    = new Subscription();
        cotisation      = new Cotisation();
        creditcard      = new Creditcard();
        
        // Players spéciaux
        playerPro           = new Player();
        localAdmin          = new Player();
        playerTemp          = new Player();
        playerWithPassword  = null;
        
        // Flags
        nextPlayer      = false;
        nextInscription = false;
        nextRound       = false;
        connected       = false;
        
        // État UI
        selectedMenuItem = null;
        lastVisitedPage  = null;
        currentLanguage  = "fr";
        
        LOG.debug("ApplicationContext initialized");
    }
    
    /**
     * Reset complet (pour login/logout)
     */
    public void reset() {
        initializeAll();
        LOG.info("ApplicationContext.reset() - full reset executed");
    }
    /**
     * Reset partiel - seulement le contexte de travail
     */
    public void resetWorkContext() {
        LOG.debug("ApplicationContext.resetWorkContext() - partial reset");
        round           = new Round();
        handicapIndex   = new HandicapIndex();
        inscription     = new Inscription();
        scoreStableford = new ScoreStableford();
        nextInscription = false;
        nextScorecard   = false;
        nextRound       = false;
    }

    // ========================================
    // 📥 GETTERS / SETTERS - Entités principales
    // ========================================
    
    public Player getPlayer() {
        if (player == null) player = new Player();
        return player;
    }
    
    public void setPlayer(Player player) {
        this.player = player;
        LOG.debug("ApplicationContext: Player set to ID {}", 
                  player != null ? player.getIdplayer() : null);
    }
    
    public Club getClub() {
        if (club == null) club = new Club();
        return club;
    }
    
    public void setClub(Club club) {
        this.club = club;
        LOG.debug("ApplicationContext: Club set to ID {}", 
                  club != null ? club.getIdclub() : null);
    }
    
    public Course getCourse() {
        if (course == null) course = new Course();
        return course;
    }
    
    public void setCourse(Course course) {
        this.course = course;
        LOG.debug("ApplicationContext: Course set to ID {}", 
                  course != null ? course.getIdcourse() : null);
    }
    
    public Round getRound() {
        if (round == null) round = new Round();
        return round;
    }
    
    public void setRound(Round round) {
        this.round = round;
        LOG.debug("ApplicationContext: Round set to ID {}", 
                  round != null ? round.getIdround() : null);
    }
    
    public HandicapIndex getHandicapIndex() {
        if (handicapIndex == null) handicapIndex = new HandicapIndex();
        return handicapIndex;
    }
    
    public void setHandicapIndex(HandicapIndex handicapIndex) {
        this.handicapIndex = handicapIndex;
    }

    public Inscription getInscription() {
        if (inscription == null) inscription = new Inscription();
        return inscription;
    }

    public void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    public ScoreStableford getScoreStableford() {
        if (scoreStableford == null) scoreStableford = new ScoreStableford();
        return scoreStableford;
    }

    public void setScoreStableford(ScoreStableford scoreStableford) {
        this.scoreStableford = scoreStableford;
    }

    public Subscription getSubscription() {
        if (subscription == null) subscription = new Subscription();
        return subscription;
    }
    
    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
    public Cotisation getCotisation() {
        if (cotisation == null) cotisation = new Cotisation();
        return cotisation;
    }
    
    public void setCotisation(Cotisation cotisation) {
        this.cotisation = cotisation;
    }

    public Creditcard getCreditcard() {
        if (creditcard == null) creditcard = new Creditcard();
        return creditcard;
    }

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    }

    public ECompetition getCompetition() {
        if (competition == null) competition = new ECompetition(new CompetitionDescription(), new CompetitionData());
        return competition;
    }

    public void setCompetition(ECompetition competition) {
        this.competition = competition;
        LOG.debug("ApplicationContext: competition set");
    }

    public EUnavailable getUnavailable() {
        return unavailable;
    }

    public void setUnavailable(EUnavailable unavailable) {
        this.unavailable = unavailable;
        LOG.debug("ApplicationContext: unavailable = {}", unavailable);
    }

    // ========================================
    // 👥 GETTERS / SETTERS - Players spéciaux
    // ========================================
    
    public Player getPlayerPro() {
        if (playerPro == null) playerPro = new Player();
        return playerPro;
    }
    
    public void setPlayerPro(Player player) {
        this.playerPro = player;
    }
    
    public Player getLocalAdmin() {
        if (localAdmin == null) localAdmin = new Player();
        return localAdmin;
    }
    
    public void setLocalAdmin(Player player) {
        this.localAdmin = player;
    }
    
    public Player getPlayerTemp() {
        if (playerTemp == null) playerTemp = new Player();
        return playerTemp;
    }
    
    public void setPlayerTemp(Player player) {
        this.playerTemp = player;
    }
    
    public EPlayerPassword getPlayerWithPassword() {
        return playerWithPassword;
    }
    
    public void setPlayerWithPassword(EPlayerPassword epp) {
        this.playerWithPassword = epp;
    }
    public ECourseList getSelectedHandicap()  { return selectedHandicap; }
    public void        setSelectedHandicap(ECourseList selected) { this.selectedHandicap = selected; }
    
    public ECourseList getSelectedPlayedRound() {
        return selectedPlayedRound;
    }

    public void setSelectedPlayedRound(ECourseList selectedPlayedRound) {
        this.selectedPlayedRound = selectedPlayedRound;
    }

    public List<ECourseList> getPlayedRounds() {
        return playedRounds;
    }

    public void setPlayedRounds(List<ECourseList> playedRounds) {
        this.playedRounds = playedRounds;
    }
   
    // ========================================
    // 🚦 GETTERS / SETTERS - Flags
    // ========================================
    
    public boolean isNextPlayer() { return nextPlayer; }
    public void setNextPlayer(boolean nextPlayer) {
        this.nextPlayer = nextPlayer;
        LOG.debug("ApplicationContext: nextPlayer = {}", nextPlayer);
    }
    
    public boolean isNextInscription() { return nextInscription; }
    public void setNextInscription(boolean nextInscription) {
        this.nextInscription = nextInscription;
        LOG.debug("ApplicationContext: nextInscription = {}", nextInscription);
    }

    public boolean isNextScorecard() { return nextScorecard; }
    public void setNextScorecard(boolean nextScorecard) {
        this.nextScorecard = nextScorecard;
        LOG.debug("ApplicationContext: nextScorecard = {}", nextScorecard);
    }

    public boolean isNextRound() { return nextRound; }
    public void setNextRound(boolean nextRound) {
        this.nextRound = nextRound;
    }
    
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) {
        this.connected = connected;
        LOG.debug("ApplicationContext: connected = {}", connected);
    }

    // ========================================
    // 🎨 GETTERS / SETTERS - État UI
    // ========================================
    
    public String getSelectedMenuItem() { return selectedMenuItem; }
    public void setSelectedMenuItem(String menuItem) { this.selectedMenuItem = menuItem; }
    
    public String getLastVisitedPage() { return lastVisitedPage; }
    public void setLastVisitedPage(String page) { this.lastVisitedPage = page; }
    
    public String getCurrentLanguage() { return currentLanguage; }
    public void setCurrentLanguage(String language) { this.currentLanguage = language; }

    // ========================================
    // 🔍 MÉTHODES UTILITAIRES
    // ========================================
    
    public boolean isPlayerLoggedIn() {
        return player != null && 
               player.getIdplayer() != null &&
               player.getIdplayer() > 0;
    }
    
    public boolean isClubSelected() {
        return club != null && 
               club.getIdclub() != null &&
               club.getIdclub() > 0;
    }
    
    public boolean isCourseSelected() {
        return course != null && 
               course.getIdcourse() != null &&
               course.getIdcourse() > 0;
    }
    
    public boolean isRoundActive() {
        return round != null && 
               round.getIdround() != null &&
               round.getIdround() > 0;
    }
    
    public String getCurrentPlayerFullName() {
        if (player == null || player.getPlayerFirstName() == null) return "Guest";
        return player.getPlayerFirstName() + " " + player.getPlayerLastName();
    }
    
    public String getCurrentPlayerRole() {
        if (player == null || player.getPlayerRole() == null) return "GUEST";
        return player.getPlayerRole();
    }
    
    @Override
    public String toString() {
        return "ApplicationContext{" +
                "player="    + (player != null ? player.getIdplayer()   : null) +
                ", club="    + (club   != null ? club.getIdclub()        : null) +
                ", course="  + (course != null ? course.getIdcourse()    : null) +
                ", round="   + (round  != null ? round.getIdround()      : null) +
                ", connected="    + connected  +
                ", nextPlayer="   + nextPlayer +
                '}';
    }
}