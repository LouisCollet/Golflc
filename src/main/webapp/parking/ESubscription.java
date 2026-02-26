package entite.composite;

import entite.Club;
import entite.Player;
import entite.Subscription;

/**
 * DTO immutable pour regrouper Subscription, Player et Club
 * Représente une souscription d'un joueur dans un club
 *
 * Version refactorisée : Record sans CDI
 */
public record ESubscription(
    Subscription subscription,
    Player player,
    Club club
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public ESubscription {
        // Validation métier possible
        // ex : Objects.requireNonNull(subscription, "subscription obligatoire");
    }

    /**
     * Constructeur secondaire : Subscription + Player uniquement
     */
    public ESubscription(Subscription subscription, Player player) {
        this(subscription, player, null);
    }

    /* =======================
       Getters explicites (JSF / EL)
       ======================= */
    public Subscription getSubscription() { return subscription; }
    public Player getPlayer() { return player; }
    public Club getClub() { return club; }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public ESubscription withSubscription(Subscription subscription) {
        return new ESubscription(subscription, this.player, this.club);
    }

    public ESubscription withPlayer(Player player) {
        return new ESubscription(this.subscription, player, this.club);
    }

    public ESubscription withClub(Club club) {
        return new ESubscription(this.subscription, this.player, club);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : ESUBSCRIPTION
            %s %s %s
            """.formatted(
                subscription != null ? subscription : "",
                player != null ? player : "",
                club != null ? club : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Vérifications métier
       ======================= */
    public boolean isComplete() {
        return subscription != null && player != null && club != null;
    }

    public boolean hasEssentialData() {
        return subscription != null && player != null;
    }

    public boolean hasAnyData() {
        return subscription != null || player != null || club != null;
    }
}
