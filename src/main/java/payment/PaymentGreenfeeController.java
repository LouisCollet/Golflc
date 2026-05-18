package payment;

// import Controllers.CreditcardController; // removed 2026-02-25
// import Controllers.TarifGreenfeeController; // removed 2026-02-25
import entite.Club;
import entite.Course;
import entite.Creditcard;
import entite.Greenfee;
import entite.Inscription;
import entite.Player;
import entite.Round;
import entite.TarifGreenfee;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.Log.LOG;
import java.io.*;
// import java.sql.Connection; // removed 2026-02-25
import java.sql.SQLException;
import java.util.Arrays;
import static utils.LCUtil.prepareMessageBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentGreenfeeController implements Serializable{

    private static final long serialVersionUID = 1L;

    @Inject private cache.CacheInvalidator        cacheInvalidator;
    @Inject private create.CreateInscription      createInscriptionService;      // migrated 2026-02-25
    @Inject private create.CreatePaymentGreenfee  createPaymentGreenfeeService;  // migrated 2026-02-25
    @Inject private manager.RoundManager          roundManager;                  // added 2026-04-19 flight booking
    @Inject private find.FindRoundBySlot          findRoundBySlot;               // find-or-create au slot — 2026-04-21
    @Inject private find.FindGreenfeePaid         findGreenfeePaid;              // skip insert si déjà payé — 2026-04-21
    @Inject private find.FindInscriptionRound     findInscriptionRound;          // idempotence auto-inscription — 2026-04-21
    @Inject private find.FindTeeStart             findTeeStart;                  // 1er tee valide — 2026-04-21
    @Inject private lists.ParticipantsRoundList   participantsRoundList;         // max 4 joueurs — 2026-04-21
    @Inject private read.ReadCourse               readCourse;                    // load full course from stub — 2026-05-10

    private static final int MAX_PLAYERS_PER_SLOT = 4;

public PaymentGreenfeeController(){ // constructor
    //
}
// new 21-01-2023 vient de coursecontroller
public boolean RegisterPaymentandInscription(final Creditcard creditcard, final Greenfee greenfee, final Player player, final Round round, final Club club,
        final Course course,
        Inscription inscription) throws SQLException, Exception { // conn removed 2026-02-25
try{
           LOG.debug("entering RegisterPaymentandInscriptionGreenfee");
           LOG.debug("with greenfee = " + greenfee);

  // Step 1: Check duplicate payment by cart keys (idplayer + roundDate + idclub) — no round needed
           boolean alreadyPaid = findGreenfeePaid.findByCartKeys(greenfee.getIdplayer(), greenfee.getRoundDate(), greenfee.getIdclub());
           if (alreadyPaid) {
               LOG.info("PaymentGreenfee already registered player={} club={} date={} — skipping payment insert",
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
               LOG.debug("PaymentGreenfee registered");
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
               String msg = "Créneau complet (" + inscrits + "/" + MAX_PLAYERS_PER_SLOT
                       + ") — inscription refusée après paiement. Contacter l'administrateur pour un remboursement.";
               LOG.error(msg);
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
           LOG.debug("auto-inscription with tee = {}", teeStarts.get(0));
           Inscription result = createInscriptionService.create(round, player, player, auto, club, fullCourse, "A");
           if (result == null || result.isInscriptionError()) {
               LOG.warn("GREENFEE auto-inscription failed — user can retry via Register Score");
           } else {
               LOG.info("GREENFEE auto-inscription created player={} round={}", player.getIdplayer(), round.getIdround());
           }
           return true;
}catch (Exception e) {
            LOG.error("Exception in RegisterPaymentandInscriptionGreenfee = {}", e.getMessage());
           return false;
   }
} //end method

/* legacy — not called by registrars — uses new TarifGreenfeeController() and new CreditcardController()
public String manageGreenfee(TarifGreenfee tarifGreenfee, Club club, Round round, Player player) throws Exception{
    // commented out 2026-02-25
} //end method manageGreenfee
*/





private boolean payment(Player player, Greenfee greenfee) { // static removed + conn removed 2026-02-25
try{
        LOG.debug("entering payment");
        LOG.debug("with greenfee = " + greenfee);
        LOG.debug("droppedPlayers is before : " + Arrays.toString(player.getDroppedPlayers().toArray()));
        int size = player.getDroppedPlayers().size();
        LOG.debug("size/number of iterations players = " + size );
        if(size != 0){
   //         Player p = new Player();
            for(int i=0; i < size; i++){
    //        LOG.debug(" -- treated idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
                Player p = player.getDroppedPlayers().get(i);
                   LOG.debug("we have to CreateGreenfee for :" + p.toString());
                // boolean b = new create.CreatePaymentGreenfee().create(p, greenfee);
                boolean b = createPaymentGreenfeeService.create(p, greenfee); // migrated 2026-02-25
                   LOG.debug("create other players OK");
            } //end for

        } //end if

      // if(new create.CreatePaymentGreenfee().create(player, greenfee)){ // true
      if(createPaymentGreenfeeService.create(player, greenfee)){ // migrated 2026-02-25
  //        LOG.debug("after createGreenfee : we are OK");
          LOG.info("greenfee paid date={} round={}", greenfee.getRoundDate().format(ZDF_DAY), greenfee.getIdround());
      return true;
     }else{
        LOG.error("Greenfee NOT paid for round={}", greenfee.getIdround());
        return false;
    }
 }catch (SQLException e){
            LOG.error("SQL Exception in PaymentsGreenfeeController SQLState={} code={}", e.getSQLState(), e.getErrorCode());
            return false;
  }catch (Exception ex){
            LOG.error("Exception in PaymentsGreenfeeController = {}", ex.getMessage());
            return false;
  }
 } //end method
} // end class
