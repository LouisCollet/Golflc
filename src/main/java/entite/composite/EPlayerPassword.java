package entite.composite;

import entite.Password;
import entite.Player;

/**
 * DTO immutable pour regrouper Player et Password
 * Utilisé pour l'authentification et la gestion des mots de passe
 *
 * Version refactorisée : Record sans CDI
 */
public record EPlayerPassword(
    Player player,
    Password password
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EPlayerPassword {
        // Validation métier possible
        // ex : Objects.requireNonNull(player, "player obligatoire");
        // ex : Objects.requireNonNull(password, "password obligatoire");
    }

    /* =======================
       Getters explicites (JSF / EL)
       ======================= */
    public Player getPlayer() { return player; }
    public Password getPassword() { return password; }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EPlayerPassword withPlayer(Player newPlayer) {
        return new EPlayerPassword(newPlayer, this.password);
    }

    public EPlayerPassword withPassword(Password newPassword) {
        return new EPlayerPassword(this.player, newPassword);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EPLAYERPASSWORD
            %s %s
            """.formatted(
                player != null ? player : "",
                password != null ? password : ""
            ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Vérifications métier
       ======================= */
    public boolean isComplete() {
        return player != null && password != null;
    }

    public boolean hasAnyData() {
        return player != null || password != null;
    }
}
