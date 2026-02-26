package cache;

import static interfaces.Log.LOG;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

/**
 * Service centralisé d'invalidation de caches.
 * Remplace les appels massifs à invalidateCache() dispersés dans CourseController.reset().
 *
 * Propose :
 *  - invalidateAll()              → reset total (login, menu Home)
 *  - invalidateRoundCaches()      → rounds, inscriptions, scores, flights
 *  - invalidateClubCaches()       → clubs, courses, tees
 *  - invalidateCompetitionCaches()→ compétitions, matchplay, scramble
 *  - invalidatePlayerCaches()     → players, handicaps
 *  - invalidateSubscriptionCaches()→ subscriptions, cotisations, greenfees
 *  - invalidateProfessionalCaches()→ professionnels, lessons
 */
@Named
@ApplicationScoped
public class CacheInvalidator implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // Lists — Players & Handicaps
    // ========================================
    @Inject private lists.PlayersList                          playersList;
    @Inject private lists.HandicapList                         handicapList;
    @Inject private lists.HandicapIndexList                    handicapIndexList;

    // ========================================
    // Lists — Rounds & Inscriptions
    // ========================================
    @Inject private lists.InscriptionList                      inscriptionList;
    @Inject private lists.InscriptionListForOneRound           inscriptionListForOneRound;
    @Inject private lists.ParticipantsRoundList                participantsRoundList;
    @Inject private lists.RecentRoundList                      recentRoundList;
    @Inject private lists.RoundPlayersList                     roundPlayersList;
    @Inject private lists.PlayedList                           playedList;
    @Inject private lists.UnavailableListForDate               unavailableListForDate;

    // ========================================
    // Lists — Scores & Flights
    // ========================================
    @Inject private lists.ScoreCardList1EGA                    scoreCardList1EGA;
    @Inject private lists.ScoreCardList3                       scoreCardList3;
    @Inject private lists.AllFlightsList                       allFlightsList;
    @Inject private lists.FlightAvailableList                  flightAvailableList;

    // ========================================
    // Lists — Competitions
    // ========================================
    @Inject private lists.CompetitionDescriptionList           competitionDescriptionList;
    @Inject private lists.CompetitionInscriptionsList          competitionInscriptionsList;
    @Inject private lists.CompetitionRoundsList                competitionRoundsList;
    @Inject private lists.CompetitionStartList                 competitionStartList;
    @Inject private lists.ParticipantsStablefordCompetitionList participantsStablefordCompetitionList;
    @Inject private lists.MatchplayList                        matchplayList;
    @Inject private lists.RegisterResultList                   registerResultList;
    @Inject private calc.CalcMatchplayResult                   calcMatchplayResult;

    // ========================================
    // Lists — Clubs & Courses
    // ========================================
    @Inject private lists.ClubList                             clubList;
    @Inject private lists.ClubDetailList                       clubDetailList;
    @Inject private lists.ClubsListLocalAdmin                  clubsListLocalAdmin;
    @Inject private lists.ClubCourseTeeListOne                 clubCourseTeeListOne;
    @Inject private lists.CourseList                           courseList;
    @Inject private lists.CourseListForClub                    courseListForClub;
    @Inject private lists.CoursesListLocalAdmin                coursesListLocalAdmin;
    @Inject private lists.TeesCourseList                       teesCourseList;
    @Inject private lists.HoleList                             holeList;

    // ========================================
    // Lists — Subscriptions & Payments
    // ========================================
    @Inject private lists.SubscriptionRenewalList              subscriptionRenewalList;
    @Inject private lists.LocalAdminCotisationList             localAdminCotisationList;
    @Inject private lists.LocalAdminGreenfeeList               localAdminGreenfeeList;
    @Inject private lists.SystemAdminSubscriptionList          systemAdminSubscriptionList;

    // ========================================
    // Lists — Professionals
    // ========================================
    @Inject private lists.ProfessionalClubList                 professionalClubList;
    @Inject private lists.LessonProList                        lessonProList;
    @Inject private lists.ProfessionalListForClub              professionalListForClub;
    @Inject private lists.ProfessionalListForPayments          professionalListForPayments;
    @Inject private lists.FindCountListProfessional            findCountListProfessional;

    // ========================================
    // Find services (avec cache)
    // ========================================
    @Inject private find.FindSlopeRating                       findSlopeRating;
    @Inject private find.FindInfoStableford                    findInfoStableford;
    @Inject private find.FindCurrentSubscription               findCurrentSubscription;
    @Inject private find.FindTeeStart                          findTeeStart;

    // ========================================
    // Other
    // ========================================
    @Inject private lists.SunriseSunsetList                    sunriseSunsetList;

    public CacheInvalidator() { } // end constructor

    // ========================================
    // 🔥 INVALIDATION TOTALE
    // ========================================

    /**
     * Invalide TOUS les caches — équivalent de l'ancien reset() dans CourseController.
     * À utiliser au login et depuis le menu Home (reset total).
     */
    public void invalidateAll() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        invalidatePlayerCaches();
        invalidateRoundCaches();
        invalidateScoreCaches();
        invalidateCompetitionCaches();
        invalidateClubCaches();
        invalidateSubscriptionCaches();
        invalidateProfessionalCaches();
        invalidateFindCaches();

        // Other
        sunriseSunsetList.invalidateCache();

        LOG.debug(methodName + " - all caches invalidated");
    } // end method

    // ========================================
    // 🎯 INVALIDATIONS CIBLÉES
    // ========================================

    /**
     * Players & Handicaps
     */
    public void invalidatePlayerCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        playersList.invalidateCache();
        handicapList.invalidateCache();
        handicapIndexList.invalidateCache();
    } // end method

    /**
     * Rounds & Inscriptions
     */
    public void invalidateRoundCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        inscriptionList.invalidateCache();
        inscriptionListForOneRound.invalidateCache();
        participantsRoundList.invalidateCache();
        recentRoundList.invalidateCache();
        roundPlayersList.invalidateCache();
        playedList.invalidateCache();
        unavailableListForDate.invalidateCache();
    } // end method

    /**
     * Scores & Flights
     */
    public void invalidateScoreCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        scoreCardList1EGA.invalidateCache();
        scoreCardList3.invalidateCache();
        allFlightsList.invalidateCache();
        flightAvailableList.invalidateCache();
    } // end method

    /**
     * Competitions (Stableford, Matchplay, Scramble)
     */
    public void invalidateCompetitionCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        competitionDescriptionList.invalidateCache();
        competitionInscriptionsList.invalidateCache();
        competitionRoundsList.invalidateCache();
        competitionStartList.invalidateCache();
        participantsStablefordCompetitionList.invalidateCache();
        matchplayList.invalidateCache();
        registerResultList.invalidateCache();
        calcMatchplayResult.invalidateCache();
    } // end method

    /**
     * Clubs & Courses
     */
    public void invalidateClubCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        clubList.invalidateCache();
        clubDetailList.invalidateCache();
        clubsListLocalAdmin.invalidateCache();
        clubCourseTeeListOne.invalidateCache();
        courseList.invalidateCache();
        courseListForClub.invalidateCache();
        coursesListLocalAdmin.invalidateCache();
        teesCourseList.invalidateCache();
        holeList.invalidateCache();
    } // end method

    /**
     * Subscriptions, Cotisations, Greenfees
     */
    public void invalidateSubscriptionCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        subscriptionRenewalList.invalidateCache();
        localAdminCotisationList.invalidateCache();
        localAdminGreenfeeList.invalidateCache();
        systemAdminSubscriptionList.invalidateCache();
    } // end method

    /**
     * Professionals & Lessons
     */
    public void invalidateProfessionalCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        professionalClubList.invalidateCache();
        lessonProList.invalidateCache();
        professionalListForClub.invalidateCache();
        professionalListForPayments.invalidateCache();
        findCountListProfessional.invalidateCache();
    } // end method

    /**
     * Find services (caches de résultats de recherche)
     */
    public void invalidateFindCaches() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);

        findSlopeRating.invalidateCache();
        findInfoStableford.invalidateCache();
        findCurrentSubscription.invalidateCache();
        findTeeStart.invalidateCache();
    } // end method

} // end class
