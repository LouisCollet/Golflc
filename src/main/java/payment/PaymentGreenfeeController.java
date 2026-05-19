package payment;

import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.io.Serializable;
import java.sql.SQLException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentGreenfeeController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private cache.CacheInvalidator cacheInvalidator;
    @Inject private create.CreateInscription createInscriptionService;
    @Inject private create.CreatePaymentGreenfee createPaymentGreenfeeService;
    @Inject private manager.RoundManager roundManager;
    @Inject private find.FindRoundBySlot findRoundBySlot;
    @Inject private find.FindGreenfeePaid findGreenfeePaid;
    @Inject private find.FindInscriptionRound findInscriptionRound;
    @Inject private find.FindTeeStart findTeeStart;
    @Inject private lists.ParticipantsRoundList participantsRoundList;
    @Inject private read.ReadCourse readCourse;

    private static final int MAX_PLAYERS_PER_SLOT = 4;

    public PaymentGreenfeeController() { }

    public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Greenfee greenfee, final Player player, final Round round, final Club club,
            final Course course, Inscription inscription) throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("greenfee={}", greenfee);
        try {
            // Step 1: Check duplicate payment by cart keys (idplayer + roundDate + idclub)
            boolean alreadyPaid = findGreenfeePaid.findByCartKeys(greenfee.getIdplayer(), greenfee.getRoundDate(), greenfee.getIdclub());
            if (alreadyPaid) {
                LOG.info("greenfee already registered player={} club={} date={} — skipping payment insert",
                        player.getIdplayer(), greenfee.getIdclub(), greenfee.getRoundDate());
            }

            // Step 2: Load full course from DB if stub (courseBeginDate null = stub built in REST)
            Course fullCourse = course;
            if (course.getCourseBeginDate() == null && course.getIdcourse() != null) {
                Course loaded = readCourse.read(course);
                if (loaded != null) {
                    fullCourse = loaded;
                    LOG.debug("course loaded from DB idcourse={}", fullCourse.getIdcourse());
                } else {
                    LOG.warn("course not found in DB idcourse={} — proceeding with stub", course.getIdcourse());
                }
            }

            // Step 3: Find-or-create round (card already charged — round created here, after payment confirmation)
            if (round.getIdround() == null) {
                String zoneIdStr = (club.getAddress() != null) ? club.getAddress().getZoneId() : null;
                java.time.ZoneId zoneId = (zoneIdStr != null && !zoneIdStr.isBlank())
                        ? java.time.ZoneId.of(zoneIdStr)
                        : java.time.ZoneId.of("Europe/Brussels");
                Round existing = findRoundBySlot.find(fullCourse.getIdcourse(), round.getRoundDate(), zoneId);
                if (existing != null) {
                    round.setIdround(existing.getIdround());
                    LOG.debug("round found by slot — reusing idround={}", existing.getIdround());
                } else {
                    LOG.debug("no round at slot — creating");
                    manager.RoundManager.SaveResult result =
                        roundManager.createRound(round, fullCourse, club, new entite.UnavailablePeriod());
                    if (!result.isSuccess()) {
                        String msg = "Round creation failed after greenfee payment confirmation: " + result.getMessage();
                        LOG.error(msg);
                        throw new Exception(msg);
                    }
                    LOG.debug("round created idround={}", round.getIdround());
                }
            }

            // Step 4: Sync greenfee.idround (required by payments_greenfee INSERT)
            greenfee.setIdround(round.getIdround());

            // Step 5: Insert payment (skip if already paid)
            if (!alreadyPaid) {
                if (!payment(player, greenfee)) {
                    String msg = "Create Payment Greenfee FAILED";
                    LOG.error(msg);
                    throw new Exception(msg);
                }
                LOG.debug("payment greenfee registered");
            }

            // Step 6: Auto-inscription (idempotent) — 1er tee valide via FindTeeStart
            if (findInscriptionRound.find(round, player)) {
                LOG.debug("player {} already inscribed — skip auto-inscription", player.getIdplayer());
                return true;
            }
            // Filet de sécurité max 4 joueurs — race condition avec un autre payeur concurrent
            cacheInvalidator.invalidateParticipantsRound();
            int inscrits = participantsRoundList.list(round).size();
            if (inscrits >= MAX_PLAYERS_PER_SLOT) {
                LOG.error("créneau complet {}/{} — inscription refusée après paiement. Contacter l'administrateur.",
                        inscrits, MAX_PLAYERS_PER_SLOT);
                return true;   // paiement enregistré, mais inscription non créée
            }
            cacheInvalidator.invalidateFindTeeStart();
            java.util.List<String> teeStarts = findTeeStart.find(fullCourse, player, round);
            if (teeStarts == null || teeStarts.isEmpty() || !teeStarts.get(0).contains("/")) {
                LOG.warn("no tee found for gender {} — inscription deferred to Register Score", player.getPlayerGender());
                return true;
            }
            Inscription auto = new Inscription();
            auto.setInscriptionTeeStart(teeStarts.get(0));
            LOG.debug("auto-inscription with tee={}", teeStarts.get(0));
            Inscription result = createInscriptionService.create(round, player, player, auto, club, fullCourse, "A");
            if (result == null || result.isInscriptionError()) {
                LOG.warn("greenfee auto-inscription failed — user can retry via Register Score");
            } else {
                LOG.info("greenfee auto-inscription created player={} round={}", player.getIdplayer(), round.getIdround());
            }
            return true;
        } catch (Exception e) {
            LOG.error("exception in RegisterPaymentandInscription", e);
            return false;
        }
    } // end method

    private boolean payment(Player player, Greenfee greenfee) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering {}", methodName);
        LOG.debug("greenfee={}", greenfee);
        try {
            int size = player.getDroppedPlayers().size();
            LOG.debug("dropped players count={}", size);
            if (size != 0) {
                for (int i = 0; i < size; i++) {
                    Player p = player.getDroppedPlayers().get(i);
                    LOG.debug("creating greenfee for player={}", p);
                    createPaymentGreenfeeService.create(p, greenfee);
                }
            }

            if (createPaymentGreenfeeService.create(player, greenfee)) {
                LOG.info("greenfee paid date={} round={}", greenfee.getRoundDate().format(ZDF_DAY), greenfee.getIdround());
                return true;
            } else {
                LOG.error("greenfee NOT paid round={}", greenfee.getIdround());
                return false;
            }
        } catch (SQLException e) {
            LOG.error("SQL exception SQLState={} code={}", e.getSQLState(), e.getErrorCode());
            return false;
        } catch (Exception ex) {
            LOG.error("exception in payment", ex);
            return false;
        }
    } // end method

} // end class
