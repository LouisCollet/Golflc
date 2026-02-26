package entite.composite;

import entite.Club;
import entite.Player;
import entite.Professional;

/**
 * DTO immutable pour regrouper Club, Professional et Player
 * Représente la relation entre un club et son professionnel
 * Compatible JSF grâce aux getters explicites
 */
public record EClubPro(
        Club club,
        Professional professional,
        Player player
)  {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EClubPro {
        // Validation selon vos règles métier
    }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EClubPro withClub(Club newClub) {
        return new EClubPro(newClub, this.professional, this.player);
    }

    public EClubPro withProfessional(Professional newProfessional) {
        return new EClubPro(this.club, newProfessional, this.player);
    }

    public EClubPro withPlayer(Player newPlayer) {
        return new EClubPro(this.club, this.professional, newPlayer);
    }

    /* =======================
       Getters explicites pour JSF
       ======================= */
    public Club getClub() {
        return club != null ? club : new Club();
    }

    public Professional getProfessional() {
        return professional != null ? professional : new Professional();
    }

    public Player getPlayer() {
        return player != null ? player : new Player();
    }

    /**
     * Formatage pour affichage/debug
     */
    public String toDisplayString() {
        return """
            FROM ENTITE : ECLUBPRO
            %s %s %s
            """.formatted(
                club != null ? club : "",
                professional != null ? professional : "",
                player != null ? player : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /**
     * Vérifie si toutes les données sont présentes
     */
    public boolean isComplete() {
        return club != null && professional != null && player != null;
    }

    /**
     * Vérifie si au moins une donnée est présente
     */
    public boolean hasAnyData() {
        return club != null || professional != null || player != null;
    }

    @Override
    public String toString() {
        return "EClubPro{" +
                "club=" + club +
                ", professional=" + professional +
                ", player=" + player +
                '}';
    }
}
