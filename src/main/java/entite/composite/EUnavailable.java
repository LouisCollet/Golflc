package entite.composite;

import entite.UnavailablePeriod;
import entite.UnavailableStructure;

/**
 * DTO immutable pour regrouper UnavailableStructure et UnavailablePeriod
 * Représente une période d'indisponibilité d'une structure
 *
 * Version refactorisée : Record sans CDI
 */
public record EUnavailable(
    UnavailableStructure structure,
    UnavailablePeriod period
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EUnavailable {
        // Validation métier possible
        // ex : Objects.requireNonNull(structure, "structure obligatoire");
    }

    /* =======================
       Getters explicites (JSF / EL)
       ======================= */
    public UnavailableStructure getStructure() { return structure; }
    public UnavailablePeriod getPeriod() { return period; }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EUnavailable withStructure(UnavailableStructure structure) {
        return new EUnavailable(structure, this.period);
    }

    public EUnavailable withPeriod(UnavailablePeriod period) {
        return new EUnavailable(this.structure, period);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EUNAVAILABLE
            %s %s
            """.formatted(
                structure != null ? structure : "",
                period != null ? period : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Formatage HTML (compatibilité legacy)
       ======================= */
    public String toHtmlString() {
        return "FROM ENTITE : EUNAVAILABLE<br/>"
             + (structure != null ? structure : "")
             + "<br/>"
             + (period != null ? period : "");
    }

    /* =======================
       Vérifications métier
       ======================= */
    public boolean isComplete() {
        return structure != null && period != null;
    }

    public boolean hasAnyData() {
        return structure != null || period != null;
    }
}
