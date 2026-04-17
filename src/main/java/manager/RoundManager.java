package manager;

import entite.*;
import entite.composite.ECourseList;
import static exceptions.LCException.handleGenericException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import static interfaces.Log.LOG;

/**
 * Service métier COMPLET pour la gestion des rounds, inscriptions et scores
 * ✅ @ApplicationScoped - Stateless, partagé entre tous
 * ✅ Pattern SaveResult uniforme (même que ClubManager)
 * ✅ Gère toute la hiérarchie : Round → Inscription → Score Stableford
 */
@ApplicationScoped
public class RoundManager implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI - Services CRUD
    // ========================================

    // Services Round
    @Inject private create.CreateRound                      createRoundService;
    @Inject private read.ReadRound                          readRoundService;
    @Inject private delete.DeleteRound                      deleteRoundService;

    // Services Inscription
    @Inject private create.CreateInscription                createInscriptionService;
    @Inject private delete.DeleteInscription                deleteInscriptionService;

    // Services Score Stableford
    @Inject private create.CreateOrUpdateScoreStableford    createOrUpdateScoreStablefordService;

    // Services Lists — pour invalidation de cache
    @Inject private lists.InscriptionList                   inscriptionList;
    @Inject private lists.InscriptionListForOneRound        inscriptionListForOneRound;
    @Inject private lists.ParticipantsRoundList             participantsRoundList;
    @Inject private lists.RecentRoundList                   recentRoundList;
    @Inject private lists.RoundPlayersList                  roundPlayersList;
    @Inject private lists.PlayedList                        playedList;
    @Inject private lists.UnavailableListForDate            unavailableListForDate;

    public RoundManager() { }

    // ========================================
    // ROUND - CRUD
    // ========================================

    public SaveResult createRound(final Round round, final Course course,
                                  final Club club, final UnavailablePeriod unavailable) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with round = " + round);
        LOG.debug("with course = " + course);
        LOG.debug("with club = " + club);

        if (round == null) {
            return SaveResult.failure("Round cannot be null");
        }
        if (course == null) {
            return SaveResult.failure("Course cannot be null");
        }
        if (club == null) {
            return SaveResult.failure("Club cannot be null");
        }

        try {
            boolean success = createRoundService.create(round, course, club, unavailable);

            if (success) {
                inscriptionList.invalidateCache();
                inscriptionListForOneRound.invalidateCache();
                participantsRoundList.invalidateCache();
                recentRoundList.invalidateCache();

                String msg = "Round created: id=" + round.getIdround()
                        + " game=" + round.getRoundGame()
                        + " date=" + round.getRoundDate();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Round creation failed");
            }

        } catch (Exception e) {
            LOG.error("Exception creating round", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    public Round readRound(final int idround) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with idround = " + idround);

        if (idround <= 0) {
            LOG.warn(methodName + " - invalid round ID: " + idround);
            return null;
        }

        try {
            Round round = new Round();
            round.setIdround(idround);
            return readRoundService.read(round);
        } catch (Exception e) {
            LOG.error("Exception reading round " + idround, e);
            return null;
        }
    } // end method

    public SaveResult deleteRound(final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with round = " + round);

        if (round == null || round.getIdround() == null || round.getIdround() <= 0) {
            return SaveResult.failure("Invalid round - ID required");
        }

        try {
            boolean success = deleteRoundService.delete(round);

            if (success) {
                inscriptionList.invalidateCache();
                inscriptionListForOneRound.invalidateCache();
                participantsRoundList.invalidateCache();
                recentRoundList.invalidateCache();
                roundPlayersList.invalidateCache();
                playedList.invalidateCache();

                String msg = "Round deleted: " + round.getIdround();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Round deletion failed");
            }

        } catch (Exception e) {
            LOG.error("Exception deleting round " + round.getIdround(), e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    /**
     * Supprime un round et toutes ses données enfant (scores, inscriptions, paiements).
     * ATTENTION — uniquement pour les rounds de test.
     */
    public SaveResult deleteRoundAndChilds(final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with round = " + round);

        if (round == null || round.getIdround() == null || round.getIdround() <= 0) {
            return SaveResult.failure("Invalid round - ID required");
        }

        try {
            LOG.warn(methodName + " - cascading delete requested for round " + round.getIdround());
            boolean success = deleteRoundService.deleteRoundAndChilds(round);

            if (success) {
                inscriptionList.invalidateCache();
                inscriptionListForOneRound.invalidateCache();
                participantsRoundList.invalidateCache();
                recentRoundList.invalidateCache();
                roundPlayersList.invalidateCache();
                playedList.invalidateCache();
                unavailableListForDate.invalidateCache();

                String msg = "Round and all children deleted: id=" + round.getIdround();
                LOG.info(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Round cascade deletion failed");
            }

        } catch (Exception e) {
            LOG.error("Exception in cascade delete for round " + round.getIdround(), e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // INSCRIPTION - CRUD
    // ========================================

    public Inscription createInscription(final Round round, final Player player,
            final Player invitedBy, final Inscription inscription,
            final Club club, final Course course, final String batch) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with round = " + round);
        LOG.debug("with player = " + player);
        LOG.debug("with inscription = " + inscription);

        if (round == null || player == null || inscription == null) {
            LOG.error(methodName + " - null parameter (round, player or inscription)");
            return inscription;
        }

        try {
            Inscription result = createInscriptionService.create(
                    round, player, invitedBy, inscription, club, course, batch);

            if (result != null && !result.isInscriptionError()) {
                inscriptionList.invalidateCache();
                inscriptionListForOneRound.invalidateCache();
                participantsRoundList.invalidateCache();
                roundPlayersList.invalidateCache();
                playedList.invalidateCache();
                LOG.debug(methodName + " - caches invalidated after inscription created");
            }
            return result;

        } catch (Exception e) {
            LOG.error("Exception creating inscription", e);
            if (inscription != null) {
                inscription.setInscriptionError(true);
            }
            return inscription;
        }
    } // end method

    public SaveResult deleteInscription(final Player player, final Round round,
                                        final Club club, final Course course) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with player = " + player);
        LOG.debug("with round = " + round);

        if (player == null || round == null) {
            return SaveResult.failure("Player and round cannot be null");
        }

        try {
            boolean success = deleteInscriptionService.delete(player, round, club, course);

            if (success) {
                inscriptionList.invalidateCache();
                inscriptionListForOneRound.invalidateCache();
                participantsRoundList.invalidateCache();
                roundPlayersList.invalidateCache();
                playedList.invalidateCache();

                String msg = "Inscription deleted: player=" + player.getIdplayer()
                        + " round=" + round.getIdround();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Inscription deletion failed");
            }

        } catch (Exception e) {
            LOG.error("Exception deleting inscription", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // SCORE STABLEFORD
    // ========================================

    public SaveResult saveScoreStableford(final ScoreStableford score,
                                          final Round round, final Player player) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("with score = " + score);
        LOG.debug("with round = " + round);
        LOG.debug("with player = " + player);

        if (score == null || round == null || player == null) {
            return SaveResult.failure("Score, round and player cannot be null");
        }

        try {
            boolean success = createOrUpdateScoreStablefordService.status(score, round, player);

            if (success) {
                playedList.invalidateCache();
                String msg = "Score Stableford saved: player=" + player.getIdplayer()
                        + " round=" + round.getIdround();
                LOG.debug(msg);
                return SaveResult.success(msg);
            } else {
                return SaveResult.failure("Score Stableford save failed");
            }

        } catch (Exception e) {
            LOG.error("Exception saving score stableford", e);
            return SaveResult.failure("Error: " + e.getMessage());
        }
    } // end method

    // ========================================
    // LISTES — exposées au Controller (pattern PlayerManager)
    // ========================================

    public List<ECourseList> listParticipantsForRound(final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (round == null) {
            LOG.warn(methodName + " - round is null");
            return Collections.emptyList();
        }
        try {
            List<ECourseList> result = participantsRoundList.list(round);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    public List<ECourseList> listInscriptionsForRound(final Round round) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        if (round == null) {
            LOG.warn(methodName + " - round is null");
            return Collections.emptyList();
        }
        try {
            List<ECourseList> result = inscriptionListForOneRound.list(round);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // UTILITAIRES
    // ========================================

    public boolean roundExists(int idround) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        return readRound(idround) != null;
    } // end method

    // ========================================
    // SAVE RESULT PATTERN
    // ========================================

    public static class SaveResult implements Serializable {

        private static final long serialVersionUID = 1L;

        private final boolean success;
        private final String message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult success(String message) {
            return new SaveResult(true, message);
        }

        public static SaveResult failure(String message) {
            return new SaveResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return String.format("SaveResult{success=%s, message='%s'}", success, message);
        }
    } // end class SaveResult

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        Round round = new Round();
        round.setIdround(630);
        Round loaded = readRound(630);
        LOG.debug("from main, loaded = " + loaded);
    } // end main
    */

} // end class
