package payment;

import entite.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Snapshot of all session state needed by REST payment callbacks.
 * Created in JSF context (PaymentController), consumed in REST context (PaymentRestResource).
 * This is a POJO — NOT a CDI bean.
 */
public class PaymentTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    // Transaction identity
    private final String nonce;
    private final long createdAt;

    // Creditcard snapshot
    private Creditcard creditcard;

    // Player ID
    private int playerId;

    // Full Player loaded by REST (for orchestrator calls in JSF context)
    private Player player;

    // Payment type
    private String savedType;
    private String creditcardType;

    // Context entities needed by PaymentOrchestrator
    private Subscription subscription;
    private Cotisation cotisation;
    private Greenfee greenfee;
    private Round round;
    private Club club;
    private Course course;
    private Inscription inscription;
    private List<Lesson>  listLessons;
    private List<Greenfee> listGreenfees;
    private Professional professional;

    // State
    private boolean completed;
    private boolean canceled;
    private String errorMessage;                                  // set by REST on exception — read by JSF
    private final List<String> pendingInfoMessages = new ArrayList<>();  // set by REST on success — read by JSF

    public PaymentTransaction(String nonce) {
        this.nonce = nonce;
        this.createdAt = System.currentTimeMillis();
        this.completed = false;
        this.canceled = false;
    } // end constructor

    // --- Identity ---

    public String getNonce() { return nonce; }
    public long getCreatedAt() { return createdAt; }

    // --- Creditcard ---

    public Creditcard getCreditcard() { return creditcard; }
    public void setCreditcard(Creditcard creditcard) { this.creditcard = creditcard; }

    // --- Player ---

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    // --- Payment type ---

    public String getSavedType() { return savedType; }
    public void setSavedType(String savedType) { this.savedType = savedType; }

    public String getCreditcardType() { return creditcardType; }
    public void setCreditcardType(String creditcardType) { this.creditcardType = creditcardType; }

    // --- Context entities ---

    public Subscription getSubscription() { return subscription; }
    public void setSubscription(Subscription subscription) { this.subscription = subscription; }

    public Cotisation getCotisation() { return cotisation; }
    public void setCotisation(Cotisation cotisation) { this.cotisation = cotisation; }

    public Greenfee getGreenfee() { return greenfee; }
    public void setGreenfee(Greenfee greenfee) { this.greenfee = greenfee; }

    public Round getRound() { return round; }
    public void setRound(Round round) { this.round = round; }

    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }

    public List<Lesson> getListLessons() { return listLessons; }
    public void setListLessons(List<Lesson> listLessons) { this.listLessons = listLessons; }

    public List<Greenfee> getListGreenfees() { return listGreenfees; }
    public void setListGreenfees(List<Greenfee> listGreenfees) { this.listGreenfees = listGreenfees; }

    public Professional getProfessional() { return professional; }
    public void setProfessional(Professional professional) { this.professional = professional; }

    // --- State ---

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<String> getPendingInfoMessages() { return Collections.unmodifiableList(pendingInfoMessages); }
    public void addInfoMessage(String msg) { if (msg != null) pendingInfoMessages.add(msg); }

    /**
     * Check if this transaction has expired.
     * @param ttlMillis TTL in milliseconds
     * @return true if the transaction is older than ttlMillis
     */
    public boolean isExpired(long ttlMillis) {
        return (System.currentTimeMillis() - createdAt) > ttlMillis;
    } // end method

} // end class
