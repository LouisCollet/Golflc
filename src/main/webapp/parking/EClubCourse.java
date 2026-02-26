package entite.composite;

import entite.Club;
import entite.Course;

/**
 * DTO immutable pour regrouper Club et Course
 * Représente un parcours dans un club
 * Compatible JSF grâce aux getters explicites
 */
public record EClubCourse(
        Club club,
        Course course
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EClubCourse {
        // Validation selon vos règles métier
    }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EClubCourse withClub(Club newClub) {
        return new EClubCourse(newClub, this.course);
    }

    public EClubCourse withCourse(Course newCourse) {
        return new EClubCourse(this.club, newCourse);
    }

    /* =======================
       Getters explicites pour JSF
       ======================= */
    public Club getClub() {
        return club != null ? club : new Club();
    }

    public Course getCourse() {
        return course != null ? course : new Course();
    }

    /**
     * Formatage pour affichage/debug
     */
    public String toDisplayString() {
        return """
            FROM ENTITE : ECLUBCOURSE
            %s %s
            """.formatted(
                club != null ? club : "",
                course != null ? course : ""
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    /**
     * Vérifie si toutes les données sont présentes
     */
    public boolean isComplete() {
        return club != null && course != null;
    }

    /**
     * Vérifie si au moins une donnée est présente
     */
    public boolean hasAnyData() {
        return club != null || course != null;
    }

    @Override
    public String toString() {
        return "EClubCourse{" +
                "club=" + club +
                ", course=" + course +
                '}';
    }
}
