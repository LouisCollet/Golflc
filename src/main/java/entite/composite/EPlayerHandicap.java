package entite.composite;

import entite.HandicapIndex;
import entite.Player;

/**
 * DTO immutable pour regrouper Player et HandicapIndex
 * Représente le handicap d'un joueur
 *
 * Version refactorisée : Record sans CDI
 */
public record EPlayerHandicap(
    HandicapIndex handicapIndex,
    Player player
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EPlayerHandicap {
        // Validation métier optionnelle
        // ex: Objects.requireNonNull(player, "player obligatoire");
    }

    /* =======================
       Getters explicites (JSF)
       ======================= */
    public HandicapIndex getHandicapIndex() { return handicapIndex; }
    public Player getPlayer() { return player; }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EPlayerHandicap withPlayer(Player newPlayer) {
        return new EPlayerHandicap(this.handicapIndex, newPlayer);
    }

    public EPlayerHandicap withHandicapIndex(HandicapIndex newHandicapIndex) {
        return new EPlayerHandicap(newHandicapIndex, this.player);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EPLAYERHANDICAP
            %s %s
            """.formatted(
                handicapIndex != null ? handicapIndex : "",
                player != null ? player : ""
            ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Vérifications métier
       ======================= */
    public boolean isComplete() {
        return handicapIndex != null && player != null;
    }

    public boolean hasAnyData() {
        return handicapIndex != null || player != null;
    }
}
