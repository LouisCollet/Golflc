package entite.composite;

import entite.CompetitionData;
import entite.CompetitionDescription;

/**
 * DTO immutable pour regrouper CompetitionDescription et CompetitionData
 * Représente les informations complètes d'une compétition
 * Compatible JSF grâce aux getters explicites
 */
public record ECompetition(
        CompetitionDescription competitionDescription,
        CompetitionData competitionData
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public ECompetition {
        // Validation selon vos règles métier si nécessaire
    }

    /* =======================
       Withers (remplacement des setters)
       ======================= */

    public ECompetition withCompetitionDescription(CompetitionDescription competitionDescription) {
        return new ECompetition(competitionDescription, this.competitionData);
    }

    public ECompetition withCompetitionData(CompetitionData competitionData) {
        return new ECompetition(this.competitionDescription, competitionData);
    }

    /* =======================
       Getters explicites pour JSF
       ======================= */

    public CompetitionDescription getCompetitionDescription() {
        // Si null, retourne un objet vide pour éviter NullPointerException en JSF
        return competitionDescription != null ? competitionDescription : new CompetitionDescription();
    }

    public CompetitionData getCompetitionData() {
        return competitionData != null ? competitionData : new CompetitionData();
    }

    /**
     * Formatage pour affichage/debug
     */
    public String toDisplayString() {
        return """
            FROM ENTITE : ECOMPETITION
            %s %s
            """.formatted(
                competitionDescription != null ? competitionDescription : "",
                competitionData != null ? competitionData : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /**
     * Vérifie si toutes les données sont présentes
     */
    public boolean isComplete() {
        return competitionDescription != null && competitionData != null;
    }

    /**
     * Vérifie si au moins une donnée est présente
     */
    public boolean hasAnyData() {
        return competitionDescription != null || competitionData != null;
    }

    @Override
    public String toString() {
        return "ECompetition{" +
                "competitionDescription=" + competitionDescription +
                ", competitionData=" + competitionData +
                '}';
    }
}
