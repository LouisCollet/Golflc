package entite.composite;

import entite.*;

import java.io.Serializable;
import java.util.Optional;

/**
 * DTO unique pour regrouper toutes les données liées à un parcours / joueur / transaction
 * Remplace tous les anciens DTO et est prêt pour JSF
 */
public record ECourseList(
        Club club,
        Course course,
        Tee tee,
        Round round,
        Handicap handicap,
        HandicapIndex handicapIndex,
        Inscription inscription,
        Player player,
        Classment classment,
        ScoreStableford scoreStableford,
        Hole hole,
        Subscription subscription,
        Greenfee greenfee,
        Professional professional,
        LessonPayment lessonPayment,
        Password password,
        MatchplayPlayerResult player1,
        MatchplayPlayerResult player2,
        Cotisation cotisation
) implements Serializable {

    // Constructeur compact
    public ECourseList {
        // Ici tu peux ajouter des validations métier
    }

    // =======================
    // Getters explicites pour JSF
    // =======================
    public Club getClub() { return club != null ? club : new Club(); }
    public Course getCourse() { return course != null ? course : new Course(); }
    public Tee getTee() { return tee != null ? tee : new Tee(); }
    public Round getRound() { return round != null ? round : new Round(); }
    public Handicap getHandicap() { return handicap != null ? handicap : new Handicap(); }
    public HandicapIndex getHandicapIndex() { return handicapIndex != null ? handicapIndex : new HandicapIndex(); }
    public Inscription getInscription() { return inscription != null ? inscription : new Inscription(); }
    public Player getPlayer() { return player != null ? player : new Player(); }
    public Classment getClassment() { return classment != null ? classment : new Classment(); }
    public ScoreStableford getScoreStableford() { return scoreStableford != null ? scoreStableford : new ScoreStableford(); }
    public Hole getHole() { return hole != null ? hole : new Hole(); }
    public Subscription getSubscription() { return subscription != null ? subscription : new Subscription(); }
    public Greenfee getGreenfee() { return greenfee != null ? greenfee : new Greenfee(); }
    public Professional getProfessional() { return professional != null ? professional : new Professional(); }
    public LessonPayment getLessonPayment() { return lessonPayment != null ? lessonPayment : new LessonPayment(); }
    public Password getPassword() { return password != null ? password : new Password(); }
    public MatchplayPlayerResult getPlayer1() { return player1 != null ? player1 : new MatchplayPlayerResult(); }
    public MatchplayPlayerResult getPlayer2() { return player2 != null ? player2 : new MatchplayPlayerResult(); }
 //   public Cotisation getCotisation() { return cotisation != null ? cotisation : new Cotisation(); } // new 25/01/2026

    public Optional<Cotisation> getCotisation() { return Optional.ofNullable(cotisation); // mod 25/0/2026
}
    
    
    // =======================
    // Factory method pour construction progressive (builder)
    // =======================
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Club club;
        private Course course;
        private Tee tee;
        private Round round;
        private Handicap handicap;
        private HandicapIndex handicapIndex;
        private Inscription inscription;
        private Player player;
        private Classment classment;
        private ScoreStableford scoreStableford;
        private Hole hole;
        private Subscription subscription;
        private Greenfee greenfee;
        private Professional professional;
        private LessonPayment lessonPayment;
        private Password password;
        private MatchplayPlayerResult player1;
        private MatchplayPlayerResult player2;
        private Cotisation cotisation;

        public Builder club(Club club) { this.club = club; return this; }
        public Builder course(Course course) { this.course = course; return this; }
        public Builder tee(Tee tee) { this.tee = tee; return this; }
        public Builder round(Round round) { this.round = round; return this; }
        public Builder handicap(Handicap handicap) { this.handicap = handicap; return this; }
        public Builder handicapIndex(HandicapIndex hi) { this.handicapIndex = hi; return this; }
        public Builder inscription(Inscription inscription) { this.inscription = inscription; return this; }
        public Builder player(Player player) { this.player = player; return this; }
        public Builder classment(Classment classment) { this.classment = classment; return this; }
        public Builder scoreStableford(ScoreStableford s) { this.scoreStableford = s; return this; }
        public Builder hole(Hole hole) { this.hole = hole; return this; }
        public Builder subscription(Subscription s) { this.subscription = s; return this; }
        public Builder greenfee(Greenfee g) { this.greenfee = g; return this; }
        public Builder professional(Professional p) { this.professional = p; return this; }
        public Builder lessonPayment(LessonPayment l) { this.lessonPayment = l; return this; }
        public Builder password(Password p) { this.password = p; return this; }
        public Builder player1(MatchplayPlayerResult p) { this.player1 = p; return this; }
        public Builder player2(MatchplayPlayerResult p) { this.player2 = p; return this; }
        public Builder cotisation(Cotisation cotisation) { this.cotisation = cotisation; return this; }
        public ECourseList build() {
            return new ECourseList(
                club, course, tee, round, handicap, handicapIndex,
                inscription, player, classment, scoreStableford, hole, subscription,
                greenfee, professional, lessonPayment, password, player1, player2, cotisation
            );
        }
    }

    // =======================
    // Formatage pour affichage / debug
    // =======================
    public String toDisplayString() {
        return """
            FROM ENTITE : ECOURSELIST2
            %s %s %s %s %s %s %s %s %s %s
            %s %s %s %s %s %s %s %s
            """.formatted(
                club, course, tee, round, handicap, handicapIndex, inscription, player, classment, scoreStableford,
                hole, subscription, greenfee, professional, lessonPayment, password, player1, player2, cotisation
        ).replaceAll("(?m)^\\s*$\n", "");
    }

    // =======================
    // Méthodes utilitaires
    // =======================
    public boolean hasEssentialData() {
        return club != null && course != null && tee != null;
    }

    public boolean isComplete() {
        return club != null && course != null && tee != null && player != null;
    }
}
