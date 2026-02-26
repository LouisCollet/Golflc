package entite.composite;

import entite.Club;
import entite.LessonPayment;
import entite.Player;
import entite.Professional;

/**
 * DTO immutable pour regrouper Club, Professional, LessonPayment et Student
 * Représente un paiement de leçon d'un élève à un professionnel dans un club
 *
 * Version refactorisée : Record sans CDI
 */
public record EPaymentPro(
    Club club,
    Professional professional,
    LessonPayment lessonPayment,
    Player student
) {

    /**
     * Constructeur compact avec validation optionnelle
     */
    public EPaymentPro {
        // Validation métier optionnelle
        // ex: Objects.requireNonNull(professional, "professional obligatoire");
    }

    /* =======================
       Getters explicites (JSF)
       ======================= */
    public Club getClub() { return club; }
    public Professional getProfessional() { return professional; }
    public LessonPayment getLessonPayment() { return lessonPayment; }
    public Player getStudent() { return student; }

    /* =======================
       Withers (remplacement des setters)
       ======================= */
    public EPaymentPro withClub(Club newClub) {
        return new EPaymentPro(newClub, this.professional, this.lessonPayment, this.student);
    }

    public EPaymentPro withProfessional(Professional newProfessional) {
        return new EPaymentPro(this.club, newProfessional, this.lessonPayment, this.student);
    }

    public EPaymentPro withLessonPayment(LessonPayment newLessonPayment) {
        return new EPaymentPro(this.club, this.professional, newLessonPayment, this.student);
    }

    public EPaymentPro withStudent(Player newStudent) {
        return new EPaymentPro(this.club, this.professional, this.lessonPayment, newStudent);
    }

    /* =======================
       Formatage pour affichage/debug
       ======================= */
    public String toDisplayString() {
        return """
            FROM ENTITE : EPAYMENTPRO
            %s %s %s %s
            """.formatted(
                club != null ? club : "",
                professional != null ? professional : "",
                lessonPayment != null ? lessonPayment : "",
                student != null ? student : ""
            ).replaceAll("(?m)^\\s*$\n", "");
    }

    /* =======================
       Vérifications métier
       ======================= */
    public boolean isComplete() {
        return club != null
            && professional != null
            && lessonPayment != null
            && student != null;
    }

    /**
     * Données essentielles (avant paiement)
     */
    public boolean hasEssentialData() {
        return club != null
            && professional != null
            && student != null;
    }

    public boolean hasAnyData() {
        return club != null
            || professional != null
            || lessonPayment != null
            || student != null;
    }
}
