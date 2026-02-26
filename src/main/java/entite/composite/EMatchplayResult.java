package entite.composite;

import entite.MatchplayPlayerResult;
import java.io.Serializable;

/**
 * DTO immutable pour regrouper les résultats d'un match play
 * Représente le résultat entre deux joueurs
 *
 * Version refactorisée : Record sans CDI
 */
public record EMatchplayResult(
    MatchplayPlayerResult player1,
    MatchplayPlayerResult player2
) implements Serializable {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EMatchplayResult {
        // Validation métier possible
        // ex : Objects.requireNonNull(player1, "player1 obligatoire");
    }

    /* =======================
       Getters explicites (JSF / EL)
       ======================= */
    public MatchplayPlayerResult getPlayer1() { return player1; }
    public MatchplayPlayerResult getPlayer2() { return player2; }

    /* =======================
       Withers (immutabilité)
       ======================= */
    public EMatchplayResult withPlayer1(MatchplayPlayerResult newPlayer1) {
        return new EMatchplayResult(newPlayer1, this.player2);
    }

    public EMatchplayResult withPlayer2(MatchplayPlayerResult newPlayer2) {
        return new EMatchplayResult(this.player1, newPlayer2);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EMATCHPLAYRESULT
             ,vers Player1 : %s
             ,vers Player2 : %s
            """.formatted(
                player1 != null ? player1 : "",
                player2 != null ? player2 : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Vérifications rapides
       ======================= */
    public boolean isComplete() {
        return player1 != null && player2 != null;
    }

    public boolean hasAnyData() {
        return player1 != null || player2 != null;
    }
}
