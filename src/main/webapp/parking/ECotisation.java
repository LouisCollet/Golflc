package entite.composite;

import entite.Club;
import entite.Cotisation;
import entite.Player;

/**
 * DTO immutable pour regrouper Cotisation, Player et Club
 * Représente une cotisation d'un joueur dans un club
 * Compatible JSF grâce aux getters explicites
 */
public record ECotisation(
        Cotisation cotisation,
        Player player,
        Club club
) {

    // Constructeur compact avec validation optionnelle
    public ECotisation {
        // Validation selon vos règles métier
    }

    /* =======================
       Withers (remplacement des setters)
       ======================= */

    public ECotisation withCotisation(Cotisation newCotisation) {
        return new ECotisation(newCotisation, this.player, this.club);
    }

    public ECotisation withPlayer(Player newPlayer) {
        return new ECotisation(this.cotisation, newPlayer, this.club);
    }

    public ECotisation withClub(Club newClub) {
        return new ECotisation(this.cotisation, this.player, newClub);
    }

    /* =======================
       Getters explicites pour JSF
       ======================= */

    public Cotisation getCotisation() {
        // Si null, retourne un objet vide pour éviter NullPointerException en JSF
        return cotisation != null ? cotisation : new Cotisation();
    }

    public Player getPlayer() {
        return player != null ? player : new Player();
    }

    public Club getClub() {
        return club != null ? club : new Club();
    }

    /**
     * Formatage pour affichage/debug
     */
    public String toDisplayString() {
        return """
            FROM ENTITE : ECOTISATION
            %s %s %s
            """.formatted(
                cotisation != null ? cotisation : "",
                player != null ? player : "",
                club != null ? club : ""
            ).replaceAll("(?m)^\\s*$\n", "");
    }

    /**
     * Vérifie si toutes les données sont présentes
     */
    public boolean isComplete() {
        return cotisation != null && player != null && club != null;
    }

    /**
     * Vérifie si au moins une donnée est présente
     */
    public boolean hasAnyData() {
        return cotisation != null || player != null || club != null;
    }
}
