package entite.composite;

import entite.Club;
import entite.Greenfee;
import entite.Player;

/**
 * DTO immutable pour regrouper Greenfee, Player et Club
 * Représente un green fee payé par un joueur dans un club
 *
 * Version refactorisée : Record sans CDI
 */
public record EGreenfee(
    Greenfee greenfee,
    Player player,
    Club club
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EGreenfee {
        // Validation selon vos règles métier
    }

    /* =======================
       Getters explicites pour JSF
       ======================= */
    public Greenfee getGreenfee() { return greenfee; }
    public Player getPlayer() { return player; }
    public Club getClub() { return club; }

    /* =======================
       Withers pour immutabilité
       ======================= */
    public EGreenfee withGreenfee(Greenfee newGreenfee) {
        return new EGreenfee(newGreenfee, this.player, this.club);
    }

    public EGreenfee withPlayer(Player newPlayer) {
        return new EGreenfee(this.greenfee, newPlayer, this.club);
    }

    public EGreenfee withClub(Club newClub) {
        return new EGreenfee(this.greenfee, this.player, newClub);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EGREENFEE
            %s %s %s
            """.formatted(
                greenfee != null ? greenfee : "",
                player != null ? player : "",
                club != null ? club : ""
            ).replaceAll("(?m)^\\s*$\n", ""); // supprime les lignes vides
    }

    /* =======================
       Vérifications rapides
       ======================= */
    public boolean isComplete() {
        return greenfee != null && player != null && club != null;
    }

    public boolean hasAnyData() {
        return greenfee != null || player != null || club != null;
    }
}
