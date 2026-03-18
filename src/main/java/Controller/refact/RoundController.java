package Controller.refact;

import context.ApplicationContext;
import Controllers.DialogController;
import Controllers.StablefordController;
import entite.*;
import entite.composite.ECompetition;
import entite.composite.ECourseList;
import entite.composite.EMatchplayResult;
import exceptions.InvalidRoundException;
import static exceptions.LCException.handleGenericException;
import static exceptions.LCException.handleSQLException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.annotation.SessionMap;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import exceptions.LCException;
import static interfaces.GolfInterface.START_DATE_WHS;
import org.primefaces.event.ToggleEvent;
import static interfaces.Log.LOG;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DualListModel;
import manager.PlayerManager;
import manager.RoundManager;
import static utils.LCUtil.DatetoLocalDateTime;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import utils.LCUtil;

/**
 * Controller JSF pour la gestion des rounds, inscriptions et scores
 * ✅ @SessionScoped — état mutable lié à la session (listStableford, flags)
 * ✅ Délègue à RoundManager — controller ne connaît pas les services directement
 * ✅ inscription et scoreStableford centralisés dans ApplicationContext (partagés)
 * ✅ Navigation JSF via String return (faces-redirect=true)
 */
@Named("roundC")
@SessionScoped
public class RoundController implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========================================
    // INJECTIONS CDI
    // ========================================

    @Inject private RoundManager                        roundManager;
    @Inject private PlayerManager                       playerManager;
    @Inject private ApplicationContext                  appContext;
    @Inject private DialogController                    dialogController;

    // @Inject @SessionMap sessionMap — removed 2026-02-28, migrated to appContext

    // ✅ Injections compétition — migrated 2026-02-25
    @Inject private lists.MatchplayList                 matchplayList;
    @Inject private lists.ScrambleList                  scrambleList;
    @Inject private lists.RegisterResultList            registerResultList;
    @Inject private lists.ParticipantsRoundList         participantsRoundList;
    @Inject private read.ReadRound                      readRoundService;
    @Inject private read.ReadClub                       readClubService;
    @Inject private read.ReadCourse                     readCourseService;
    @Inject private read.ReadParArray                   readParArray;
    @Inject private find.FindCountScore                 findCountScoreService;
    @Inject private StablefordController                stablefordController;

    // ✅ Injections Round/Inscription — migrated 2026-02-25
    @Inject private find.FindTeeStart                  findTeeStart;
    @Inject private find.FindOpenWeather               findOpenWeather;
    @Inject private Controllers.LanguageController     languageController; // fix multi-user 2026-03-07
    @Inject private find.FindTarifGreenfeeData         findTarifGreenfeeData;
    @Inject private lists.RoundPlayersList             roundPlayersListService;

    // ✅ Injections Score/Stableford — migrated 2026-02-25
    @Inject private calc.CalcScoreStableford           calcScoreStableford;
    @Inject private create.CreateStatisticsStableford  createStatisticsStableford;
    @Inject private read.ReadStatisticsList            readStatisticsListService;
    @Inject private Controllers.HandicapController     handicapController;

    // ✅ Injections Scorecard — migrated 2026-02-25
    @Inject private utils.ShowScore                    showScoreList;
    @Inject private lists.ScoreCardList1EGA            scoreCardList1EGA;
    @Inject private lists.ScoreCardList3               scoreCardList3;
    @Inject private find.FindSlopeRating               findSlopeRating;
    @Inject private find.FindHandicapIndexAtDate       findHandicapIndexAtDate;
    @Inject private read.ReadScoreList                 readScoreListService;

    // ✅ Injections Matchplay/Participants — migrated 2026-02-25
    @Inject private lists.ParticipantsStablefordCompetitionList participantsStablefordCompetitionList;
    @Inject private calc.CalcMatchplayResult           calcMatchplayResult;

    // ✅ Injections Competition/Remaining — migrated 2026-02-25
    @Inject private create.CreateCompetitionData       createCompetitionData;
    @Inject private create.CreateCompetitionRounds     createCompetitionRounds;
    @Inject private create.CreateCompetitionInscriptions createCompetitionInscriptions;
    @Inject private update.UpdateCompetitionDescription updateCompetitionDescription;
    @Inject private lists.CompetitionRoundsList         competitionRoundsList;
    @Inject private delete.DeleteInscriptionCompetition deleteInscriptionCompetition;
    @Inject private lists.CompetitionInscriptionsList   competitionInscriptionsList;
    @Inject private lists.RecentRoundList               recentRoundList;

    // ✅ Injection MongoDB — migrated 2026-02-26
    @Inject private Controllers.MongoCalculationsController mongoCalculationsController;

    // ✅ Injection NavigationController — renamed from CourseController 2026-02-28
    @Inject private Controller.refact.NavigationController        navigationController;

    // ✅ Injections Phase 3A — Competition management — migrated 2026-02-25
    @Inject private create.CreateCompetitionDescription createCompetitionDescriptionService; // Phase 3A
    @Inject private lists.CompetitionDescriptionList    competitionDescriptionList;            // Phase 3A
    @Inject private lists.CompetitionStartList          competitionStartList;                  // Phase 3A
    @Inject private calc.CalcCompetitionTimeStartList   calcCompetitionTimeStartList;          // Phase 3A
    @Inject private lists.InscriptionList               inscriptionListService;                // Phase 3A

    // ========================================
    // STATE mutable — justifie @SessionScoped
    // inscription / scoreStableford → ApplicationContext (partagés)
    // ========================================

    private List<ECourseList>   listStableford;
    private boolean             nextInscription = false;
    private boolean             nextScorecard   = false;
    private int                 cptFlight       = 0;
    private List<Player>        lp              = null;
    private TarifGreenfee       tarifGreenfee;
    private List<?>             filteredCars;     // PrimeFaces dataTable requirement

    // ✅ State PlayingHcp — migrated 2026-02-26
    private PlayingHandicap        playingHcp;
    private int                    inputPlayingHcp = 0; // migrated 2026-02-27 from CourseController

    // ✅ State compétition — migrated 2026-02-25
    private Matchplay              matchplay;
    private Flight                 flight;
    private ScoreMatchplay         scoreMatchplay;
    private List<Matchplay>        listmatchplay;
    private List<ScoreScramble>    listscr;
    private Tee                    tee;

    // ✅ State Phase 3A — Competition fields — migrated 2026-02-25
    private Map<String, String>    availableQualifying;
    private List<String>           gameList;

    // ✅ State migrated 2026-02-28 from NavigationController
    private ArrayList<Flight>              filteredFlights;
    private DualListModel<Player>          dlPlayers;
    private String                         inputClubOperation = null;
    private String                         inputcmdParticipants = null;
    private String                         inputScorecard = null;
    private int[]                          parArray = null;
    private String                         otherGame = null;
    private boolean                        skip;

    public RoundController() { }

    @PostConstruct
    public void init() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        listStableford  = Collections.emptyList();
        nextInscription = false;
        nextScorecard   = false;
        matchplay       = new Matchplay();
        flight          = new Flight();
        scoreMatchplay  = new ScoreMatchplay();
        listmatchplay   = Collections.emptyList();
        listscr         = Collections.emptyList();
        tee             = new Tee();
        // Phase 3A — Competition fields
        availableQualifying = new java.util.LinkedHashMap<>();
        availableQualifying.put("Non Qualifying", "N");
        availableQualifying.put("Qualifying", "Y");
        gameList = Arrays.asList("STABLEFORD","SCRAMBLE","CHAPMAN","STROKEPLAY","MP_FOURBALL","MP_FOURSOME","MP_SINGLE");
        LOG.debug("RoundController initialized");
    } // end method

    // ========================================
    // CDI EVENT — ResetEvent observer — 2026-02-26
    // ========================================

    public void onReset(@Observes events.ResetEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " — source: " + event.getSource());
        listStableford      = Collections.emptyList();
        nextInscription     = false;
        nextScorecard       = false;
        cptFlight           = 0;
        lp                  = null;
        tarifGreenfee       = null;
        filteredCars        = null;
        matchplay           = new Matchplay();
        flight              = new Flight();
        scoreMatchplay      = new ScoreMatchplay();
        listmatchplay       = Collections.emptyList();
        listscr             = Collections.emptyList();
        tee                 = new Tee();
        availableQualifying = new java.util.LinkedHashMap<>();
        availableQualifying.put("Non Qualifying", "N");
        availableQualifying.put("Qualifying", "Y");
        gameList = Arrays.asList("STABLEFORD","SCRAMBLE","CHAPMAN","STROKEPLAY","MP_FOURBALL","MP_FOURSOME","MP_SINGLE");
        // migrated 2026-02-28 from NavigationController
        filteredFlights      = null;
        dlPlayers            = null;
        inputClubOperation   = null;
        inputcmdParticipants = null;
        inputScorecard       = null;
        parArray             = null;
        otherGame            = null;
        skip                 = false;
        LOG.debug(methodName + " — RoundController reset done");
    } // end method

    // ========================================
    // Délégation Round ← ApplicationContext
    // ========================================

    public Round getRound() {
        return appContext.getRound();
    }

    public void setRound(final Round round) {
        appContext.setRound(round);
    }

    // ========================================
    // CREATE - Round
    // ========================================

    public void createRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            round.setRoundDate(round.getRoundDateTrf()); // transfert date depuis Flight
            LOG.debug("round after TRF Date = " + round);

            RoundManager.SaveResult result = roundManager.createRound(
                    round,
                    appContext.getCourse(),
                    appContext.getClub(),
                    new UnavailablePeriod()
            );

            if (result.isSuccess()) {
                LOG.debug("Round created successfully");
                appContext.setRound(round);
                setNextInscription(true);
            } else {
                LOG.error("Round creation failed: " + result.getMessage());
                setNextInscription(false);
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            setNextInscription(false);
        }
    } // end method

    // ========================================
    // CREATE - Inscription
    // ========================================

    public String createInscription() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round  round  = appContext.getRound();
            Player player = appContext.getPlayer();
            Club   club   = appContext.getClub();
            Course course = appContext.getCourse();

            LOG.debug("with round = " + round);
            LOG.debug("with player = " + player);
            LOG.debug("with inscription = " + appContext.getInscription());

            Inscription inscription = roundManager.createInscription(
                    round, player, player, appContext.getInscription(), club, course, "A");
            appContext.setInscription(inscription);
            LOG.debug("inscription returned = " + inscription);

            if (!inscription.isInscriptionError()) {
                String msg = LCUtil.prepareMessageBean("inscription.ok") + round + inscription
                        + " <br/> player name = " + player.getPlayerLastName()
                        + " <br/> club name = " + club.getClubName()
                        + " <br/> course name = " + course.getCourseName();
                LOG.info(msg);
                showMessageInfo(msg);
                inscription.setInscriptionOK(true);
                return "inscription.xhtml?faces-redirect=true";
            }

            LOG.debug("inscription error status = " + inscription.getErrorStatus());

            return switch (inscription.getErrorStatus()) {
                case "01" -> {
                    inscription.setInscriptionOK(false);
                    yield "inscription.xhtml?faces-redirect=true";
                }
                case "02" -> {
                    showMessageFatal(LCUtil.prepareMessageBean("cotisation.notfound"));
                    inscription.setInscriptionOK(false);
                    yield "greenfee_cotisation_round.xhtml?faces-redirect=true";
                }
                case "03" -> {
                    inscription.setInscriptionOK(false);
                    yield "greenfee_cotisation_round.xhtml?faces-redirect=true";
                }
                case "04" -> {
                    inscription.setInscriptionOK(false);
                    String msg = inscription.getWeather(); // erreur stockée dans weather (usage provisoire)
                    LOG.error(methodName + " - error 04 : " + msg);
                    yield null;
                }
                case "05" -> {
                    inscription.setInscriptionOK(false);
                    yield "inscription.xhtml"; // forward simple — pas de redirect
                }
                default -> null;
            };

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Inscription des joueurs droppés (multi-inscription depuis pickList)
     */
    public String createInscriptionOtherPlayers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round  round      = appContext.getRound();
            Player invitedBy  = appContext.getPlayer();
            Club   club       = appContext.getClub();
            Course course     = appContext.getCourse();
            List<Player> droppedPlayers = invitedBy.getDroppedPlayers();

            LOG.debug("number of inscriptions to be created = " + droppedPlayers.size());
            droppedPlayers.forEach(item -> LOG.debug("dropped player = " + item.getIdplayer()));

            List<Player> copy = List.copyOf(droppedPlayers); // immutable snapshot
            for (Player p : copy) {
                LOG.debug("creating inscription for player = " + p);
                appContext.getInscription().setRound_idround(round.getIdround());
                appContext.getInscription().setInscriptionInvitedBy(
                        invitedBy.getPlayerFirstName() + "," + invitedBy.getPlayerLastName());

                Inscription result = roundManager.createInscription(
                        round, p, invitedBy, appContext.getInscription(), club, course, "B");
                appContext.setInscription(result);

                if (result.isInscriptionError()) {
                    String msg = "Inscription other players NOT OK for player = "
                            + p.getIdplayer() + " / " + p.getPlayerLastName();
                    LOG.error(msg);
                    showMessageFatal(msg);
                } else {
                    droppedPlayers.removeIf(item -> item.getIdplayer().equals(p.getIdplayer()));
                    LOG.debug("inscription OK for player = " + p);
                }
            }
            return "inscriptions_other_players.xhtml";

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Inscription
    // ========================================

    /**
     * Annulation d'une inscription depuis la vue participants.
     * Seuls l'ADMIN ou le joueur concerné peuvent annuler.
     */
    public String cancelInscription(final ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with ecl = " + ecl);
        try {
            Player currentPlayer   = appContext.getPlayer();
            Player inscribedPlayer = ecl.getPlayer();

            LOG.debug("current player = " + currentPlayer.getIdplayer()
                    + " role = " + currentPlayer.getPlayerRole());
            LOG.debug("inscribed player = " + inscribedPlayer.getIdplayer());

            // Permission check
            if (!currentPlayer.getPlayerRole().equals("ADMIN")
                    && !currentPlayer.getIdplayer().equals(inscribedPlayer.getIdplayer())) {
                String msgerr = LCUtil.prepareMessageBean("cancel.inscription")
                        + currentPlayer.getIdplayer() + " /\\ "
                        + inscribedPlayer.getIdplayer() + " /\\ "
                        + currentPlayer.getPlayerRole();
                LOG.error(msgerr);
                showMessageFatal(msgerr);
                return null;
            }

            appContext.setRound(ecl.round());

            RoundManager.SaveResult result = roundManager.deleteInscription(
                    inscribedPlayer, ecl.round(), ecl.club(), ecl.course());

            if (result.isSuccess()) {
                listStableford = roundManager.listParticipantsForRound(ecl.round()); // ✅ via manager
                String s = Round.fillRoundPlayersStringEcl(listStableford);
                appContext.getRound().setPlayersString(s);
                LOG.debug("PlayersString updated = " + appContext.getRound().getPlayersString());
                return "show_participants_stableford.xhtml?faces-redirect=true";
            } else {
                showMessageFatal(result.getMessage());
                return null;
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // DELETE - Round
    // ========================================

    public String cancelRound(final ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with ecl = " + ecl);
        try {
            RoundManager.SaveResult result = roundManager.deleteRound(ecl.round());

            if (result.isSuccess()) {
                showMessageInfo(result.getMessage());
                return "selectInscription.xhtml?faces-redirect=true";
            } else {
                showMessageFatal(result.getMessage());
                return null;
            }

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // SELECT - Round (depuis dialog)
    // ========================================

    public String selectRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            dialogController.closeDialog(null);

            Object mode = appContext.getInputSelectRound();
            if ("INSCRIPTION".equals(mode)) {
                Round round = appContext.getRound();
                List<ECourseList> li = roundManager.listInscriptionsForRound(round); // ✅ via manager
                if (!li.isEmpty()) {
                    LOG.debug("with Club = " + li.getFirst().club());
                    LOG.debug("with Course = " + li.getFirst().course());
                    LOG.debug("with Round = " + li.getFirst().round());
                }
                return "inscription.xhtml?faces-redirect=true";
            }
            return null;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Sélection d'une inscription récente depuis la liste (selectInscription.xhtml)
     */
    public String selectRecentInscription(final ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("ecl = " + ecl);
        try {
            dialogController.closeDialog(null);

            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());
            appContext.setRound(ecl.round());

            List<ECourseList> participants = roundManager.listParticipantsForRound(ecl.round()); // ✅ via manager
            if (participants != null && !participants.isEmpty()) {
                appContext.getInscription().setInscriptionOK(true);
                String s = Round.fillRoundPlayersStringEcl(participants);
                appContext.getRound().setPlayersString(s);
                LOG.debug("joueurs déjà inscrits = " + appContext.getRound().getPlayersString());
            } else {
                appContext.getRound().setPlayersString("no players reservation");
            }

            String msg = "Select RecentInscription Successful"
                    + " <br/> Club name = " + ecl.club().getClubName()
                    + " <br/> course name = " + ecl.course().getCourseName()
                    + " <br/> round = " + ecl.round().getIdround();
            LOG.info(msg);
            return "inscription.xhtml?faces-redirect=true";

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // SCORE STABLEFORD
    // ========================================

    public String createScoreStableford() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            ScoreStableford score = appContext.getScoreStableford();
            LOG.debug("with round = " + round);
            LOG.debug("with scoreStableford = " + score);

            Object scoreType = appContext.getScoreType();
            LOG.debug("with scoreType = " + scoreType);

            Player p;
            if ("COMPETITION".equals(scoreType)) {
                LOG.debug("handling COMPETITION scoreType");
                int playerid = appContext.getCompetitionPlayerId();
                p = playerManager.readPlayer(playerid);
            } else {
                LOG.debug("handling INDIVIDUAL scoreType");
                p = appContext.getPlayer();
            }

            RoundManager.SaveResult result = roundManager.saveScoreStableford(score, round, p);

            if (result.isSuccess()) {
                LOG.debug("ScoreStableford created or modified");
                if ("INDIVIDUAL".equals(scoreType)) {
                    score.setShowButtonStatistics(true);
                }
                if ("Y".equals(round.getRoundQualifying())) {
                    score.setShowCreateHandicapIndex(true);
                }
            } else {
                LOG.error("ERROR Creation/Modification scoreStableford");
                showMessageFatal(result.getMessage());
                return null;
            }
            return null;

        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // FLAGS
    // ========================================

    public boolean isNextInscription() { return nextInscription; }
    public void setNextInscription(final boolean nextInscription) {
        this.nextInscription = nextInscription;
        appContext.setNextInscription(nextInscription); // sync ApplicationContext
    }

    public boolean isNextScorecard() { return nextScorecard; }
    public void setNextScorecard(final boolean nextScorecard) {
        this.nextScorecard = nextScorecard;
        appContext.setNextScorecard(nextScorecard); // sync ApplicationContext
    }

    // ========================================
    // GETTERS / SETTERS — délèguent à ApplicationContext
    // ========================================

    public Inscription getInscription() {
        return appContext.getInscription();
    }
    public void setInscription(final Inscription inscription) {
        appContext.setInscription(inscription);
    }

    public ScoreStableford getScoreStableford() {
        return appContext.getScoreStableford();
    }
    public void setScoreStableford(final ScoreStableford scoreStableford) {
        appContext.setScoreStableford(scoreStableford);
    }

    public List<ECourseList> getListStableford() { return listStableford; }
    public void setListStableford(final List<ECourseList> listStableford) {
        this.listStableford = listStableford;
    }

    // ========================================
    // COMPETITION — Flight selection (migrated 2026-02-25)
    // ========================================

    public void selectFlightFromDialog(final Flight flight) throws IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with flight = " + flight);
        this.flight = flight;
        LOG.debug("flightStart format LocalDateTime = " + flight.getFlightStart());
        Round round = appContext.getRound();
        round.setRoundDate(flight.getFlightStart());
        LOG.debug("getRoundDate = " + round.getRoundDate());
        round.setRoundDateTrf(flight.getFlightStart());
        LOG.debug("getRoundDateTRF = " + round.getRoundDateTrf());
        dialogController.closeDialog(null);
    } // end method

    public void processChecked(final AjaxBehaviorEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end method

    // ========================================
    // COMPETITION — Matchplay rounds (migrated 2026-02-25)
    // ========================================

    public List<Matchplay> listMatchplayRounds(final String formula) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for formula = " + formula);
        try {
            listmatchplay = matchplayList.getList("MP_");
            Course course = appContext.getCourse();
            Club club = appContext.getClub();
            Round round = appContext.getRound();
            course.setIdcourse(listmatchplay.get(1).getIdcourse());
            LOG.debug("setted idcourse on = " + course.getIdcourse());
            course.setCourseName(listmatchplay.get(1).getCourseName());
            club.setIdclub(listmatchplay.get(1).getIdclub());
            club.setClubName(listmatchplay.get(1).getClubName());
            round.setRoundGame(listmatchplay.get(1).getRoundGame());
            round.setIdround(listmatchplay.get(1).getIdround());
            LOG.debug("from listmp, round = " + round.getIdround());
            round.setRoundName(listmatchplay.get(1).getRoundName());
            java.util.Date d = listmatchplay.get(1).getRoundDate();
            LocalDateTime date = DatetoLocalDateTime(d);
            round.setRoundDate(date);
            matchplay.setRoundNameName(listmatchplay.get(1).getRoundNameName());
            return listmatchplay;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // COMPETITION — Scramble rounds (migrated 2026-02-25)
    // ========================================

    public List<ScoreScramble> listScrambleRounds(final String formula) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with formula = " + formula);
        try {
            listscr = scrambleList.getList(formula);
            LOG.debug("listscr = " + listscr);
            LOG.debug("listscr rounds = " + listscr.size());
            Course course = appContext.getCourse();
            Club club = appContext.getClub();
            Round round = appContext.getRound();
            course.setIdcourse(listscr.get(0).getIdcourse());
            LOG.debug("setted idcourse on = " + course.getIdcourse());
            course.setCourseName(listscr.get(0).getCourseName());
            LOG.debug("setted course name on = " + course.getCourseName());
            club.setIdclub(listscr.get(0).getIdclub());
            club.setClubName(listscr.get(0).getClubName());
            round.setRoundGame(listscr.get(0).getRoundGame());
            round.setIdround(listscr.get(0).getIdround());
            LOG.debug("from listscr, round = " + round.getIdround());
            round.setRoundName(listscr.get(0).getRoundName());
            round.setRoundDate(listscr.get(0).getRoundDate());
            return listscr;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // COMPETITION — Stableford result (migrated 2026-02-25)
    // ========================================

    public List<ECourseList> registerStablefordResult() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug("for player = " + appContext.getPlayer());
            return registerResultList.list(appContext.getPlayer());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // COMPETITION — Score Stableford compétition (migrated 2026-02-25)
    // ========================================

    public String selectedCompetitionScoreStableford(final ECompetition ec) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with CompetitionData = " + ec.competitionData());
        LOG.debug("with CompetitionDescription = " + ec.competitionDescription());
        try {
            Round round = appContext.getRound();
            round.setIdround(ec.competitionData().getCmpDataRoundId());
            round = readRoundService.read(round);
            appContext.setRound(round);

            Player competitionPlayer = new Player();
            competitionPlayer.setIdplayer(ec.competitionData().getCmpDataPlayerId());
            LOG.debug("id player for registration competition = " + competitionPlayer.getIdplayer());
            competitionPlayer = playerManager.readPlayer(competitionPlayer.getIdplayer());

            Club club = appContext.getClub();
            club.setIdclub(ec.competitionDescription().getCompetitionClubId());
            club = readClubService.read(club);
            appContext.setClub(club);

            Course course = appContext.getCourse();
            course.setIdcourse(ec.competitionDescription().getCompetitionCourseId());
            course = readCourseService.read(course);
            appContext.setCourse(course);

            // UPDATE or INSERT ?
            int rows = findCountScoreService.find(competitionPlayer, round, "rows");
            LOG.debug("we are back with FindCountScore = " + rows);

            ScoreStableford scoreStableford = appContext.getScoreStableford();
            if (rows != 0) {
                LOG.debug("there are : " + rows + " ==> this is a UPDATE, thus we are prefilling the score !");
                scoreStableford = stablefordController.completeScoreStableford(appContext.getPlayer(), round, tee);
                appContext.setScoreStableford(scoreStableford);
                LOG.debug("Score is NOW prefilled with = " + scoreStableford);
            } else {
                LOG.debug("this is a CREATION! no prefilling");
            }

            appContext.setCompetitionPlayerId(competitionPlayer.getIdplayer());
            appContext.setScoreType("COMPETITION");
            LOG.debug("competitionPlayerId = " + appContext.getCompetitionPlayerId());

            String s = "score_stableford.xhtml?faces-redirect=true"
                    + "&cmd=COMPETITION"
                    + "&playerId=" + competitionPlayer.getIdplayer()
                    + "&playerName=" + competitionPlayer.getPlayerLastName()
                    .replaceAll(" ", "%20").replaceAll("&", "&amp;");
            LOG.debug("string redirect to score_stableford = " + s);
            return s;

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // COMPETITION — Score Matchplay (migrated 2026-02-25)
    // ========================================

    public String registerScoreMatchplay(final ECourseList ecl, final String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with scoreMatchplay = " + scoreMatchplay);
        LOG.debug("with type = " + type);
        LOG.debug("with ecl = " + ecl);
        try {
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());
            appContext.setRound(ecl.round());
            tee = ecl.tee();

            var vv = readParArray.read(appContext.getPlayer(), appContext.getCourse());
            scoreMatchplay.setParArray(vv);

            Round round = appContext.getRound();
            if (round.getScoreMatchplay() != null) {
                scoreMatchplay.setstrokesEur(round.getScoreMatchplay().getstrokesEur());
                scoreMatchplay.setstrokesUsa(round.getScoreMatchplay().getstrokesUsa());
                scoreMatchplay.setResult(round.getScoreMatchplay().getResult());
            }

            var v = participantsRoundList.list(round);
            if (v.size() == 4) {
                String A1 = v.get(0).getInscription().getInscriptionMatchplayTeam()
                        + " - " + v.get(0).getPlayer().getPlayerLastName();
                String A2 = ", " + v.get(1).getPlayer().getPlayerLastName();
                scoreMatchplay.setPlayersA(A1 + A2);
                String B1 = v.get(2).getInscription().getInscriptionMatchplayTeam()
                        + " - " + v.get(2).getPlayer().getPlayerLastName();
                String B2 = ", " + v.get(3).getPlayer().getPlayerLastName();
                scoreMatchplay.setPlayersB(B1 + B2);
            }
            if (v.size() == 2) {
                String A1 = v.get(0).getInscription().getInscriptionMatchplayTeam()
                        + " - " + v.get(0).getPlayer().getPlayerLastName();
                scoreMatchplay.setPlayersA(A1);
                String B1 = v.get(1).getInscription().getInscriptionMatchplayTeam()
                        + " - " + v.get(1).getPlayer().getPlayerLastName();
                scoreMatchplay.setPlayersB(B1);
            }
            return "score_matchplay.xhtml?faces-redirect=true";

        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // GETTERS / SETTERS — Compétition (migrated 2026-02-25)
    // ========================================

    public Matchplay getMatchplay()                         { return matchplay; }
    public void setMatchplay(final Matchplay matchplay)     { this.matchplay = matchplay; }

    public Flight getFlight()                               { return flight; }
    public void setFlight(final Flight flight)              { this.flight = flight; }

    public ScoreMatchplay getScoreMatchplay()               { return scoreMatchplay; }
    public void setScoreMatchplay(final ScoreMatchplay sm)  { this.scoreMatchplay = sm; }

    public List<Matchplay> getListmatchplay()               { return listmatchplay; }
    public void setListmatchplay(final List<Matchplay> lm)  { this.listmatchplay = lm; }

    public List<ScoreScramble> getListscr()                 { return listscr; }
    public void setListscr(final List<ScoreScramble> ls)    { this.listscr = ls; }

    public Tee getTee()                                     { return tee; }
    public void setTee(final Tee tee)                       { this.tee = tee; }

    // ========================================
    // Délégation Club, Course ← ApplicationContext
    // ========================================

    public Club getClub()                                   { return appContext.getClub(); }
    public void setClub(final Club club)                    { appContext.setClub(club); }

    public Course getCourse()                               { return appContext.getCourse(); }
    public void setCourse(final Course course)              { appContext.setCourse(course); }

    public TarifGreenfee getTarifGreenfee()                 { return tarifGreenfee; }
    public void setTarifGreenfee(TarifGreenfee t)           { this.tarifGreenfee = t; }

    public List<Player> getLp()                             { return lp; }
    public void setLp(List<Player> lp)                      { this.lp = lp; }

    public int getCptFlight()                               { return cptFlight; }
    public void setCptFlight(int cptFlight)                  { this.cptFlight = cptFlight; }

    public List<?> getFilteredCars()                         { return filteredCars; }
    public void setFilteredCars(List<?> filteredCars)         { this.filteredCars = filteredCars; }

    // ✅ Getters/setters migrated 2026-02-28 from NavigationController
    public ArrayList<Flight> getFilteredFlights()                         { return filteredFlights; }
    public void setFilteredFlights(ArrayList<Flight> filteredFlights)     { this.filteredFlights = filteredFlights; }

    public DualListModel<Player> getDlPlayers()                          { return dlPlayers; }
    public void setDlPlayers(DualListModel<Player> dlPlayers)            { this.dlPlayers = dlPlayers; }

    public String getInputClubOperation()                                { return inputClubOperation; }
    public void setInputClubOperation(String inputClubOperation)         { this.inputClubOperation = inputClubOperation; }

    public String getInputcmdParticipants()                              { return inputcmdParticipants; }
    public void setInputcmdParticipants(String inputcmdParticipants)     { this.inputcmdParticipants = inputcmdParticipants; }

    public String getInputScorecard()                                    { return inputScorecard; }
    public void setInputScorecard(String inputScorecard) {
        LOG.debug("setInputScorecard = " + inputScorecard);
        this.inputScorecard = inputScorecard;
        if ("ini".equals(inputScorecard)) {
            scoreCardList3.invalidateCache();
        }
    } // end method

    public int[] getParArray()                                           { return parArray; }
    public void setParArray(int[] parArray)                              { this.parArray = parArray; }

    public String getOtherGame()                                         { return otherGame; }
    public void setOtherGame(String otherGame)                           { this.otherGame = otherGame; }

    public boolean isSkip()                                              { return skip; }
    public void setSkip(boolean skip)                                    { this.skip = skip; }

    public String onFlowProcess(org.primefaces.event.FlowEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " — old=" + event.getOldStep() + ", new=" + event.getNewStep());
        if (skip) {
            skip = false;
            return "confirm";
        } else {
            return event.getNewStep();
        }
    } // end method

    // ========================================
    // MÉTHODES MIGRÉES depuis CourseController — Groupe A Round/Inscription — 2026-02-25
    // ========================================

    /**
     * ValueChangeListener appelé quand la date du round change.
     * Migré depuis CourseController — 2026-02-25
     */
    public void roundWorkDate(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            LOG.debug(methodName + " - course = " + appContext.getCourse());
            LOG.debug(methodName + " - roundDate = " + round.getRoundDate());
            cptFlight = 0;
            appContext.setRound(round);
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
        }
    } // end method

    /**
     * Prépare la page d'inscription d'autres joueurs.
     * Migré depuis CourseController — 2026-02-25
     */
    public String otherPlayers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            Course course = appContext.getCourse();
            LOG.debug(methodName + " - round = " + round);
            LOG.debug(methodName + " - course = " + course);

            tarifGreenfee = findTarifGreenfeeData.find(round);
            if (tarifGreenfee == null) {
                String err = "Tarif returned from findTarifdata is null";
                LOG.error(err);
                showMessageFatal(err);
                return null;
            }
            LOG.debug(methodName + " - tarifGreenfee = " + tarifGreenfee);
            LOG.debug(methodName + " - roundPlayersList size = " + roundPlayersList().size());
            LOG.debug(methodName + " - draggedPlayers size = " + appContext.getPlayer().getDraggedPlayers().size());
            LOG.debug(methodName + " - roundGame = " + round.getRoundGame());

            int max = round.getRoundGame().equals(Round.GameType.MP_SINGLE.toString()) ? 2 : 4;
            int tot = appContext.getPlayer().getDraggedPlayers().size() + roundPlayersList().size();
            LOG.debug(methodName + " - total inscrits + candidats = " + tot);

            if (tot > max) {
                String msg = LCUtil.prepareMessageBean("inscription.toomuchplayers") + " max = " + max + " / total = " + tot;
                LOG.error(msg);
                showMessageFatal(msg);
                return null;  // stay on current page to display the error message
            }
            return "inscriptions_other_players.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Crée les inscriptions pour les autres joueurs (via pickList — non opérationnel).
     * Migré depuis CourseController — 2026-02-25
     */
    public String createOtherPlayers() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            Inscription inscription = appContext.getInscription();
            LOG.debug(methodName + " - round = " + round);
            LOG.debug(methodName + " - inscription = " + inscription);
            // boucler sur createInscription — non opérationnel
            return "inscription.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Drag-drop d'un joueur dans une inscription (inscriptions_other_players.xhtml).
     * Migré depuis CourseController — 2026-02-25
     */
    public String playerDrop(org.primefaces.event.DragDropEvent<?> event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - event = " + event.getData());
            LOG.debug(methodName + " - DragId = " + event.getDragId());
            LOG.debug(methodName + " - DropId = " + event.getDropId());

            entite.composite.EPlayerPassword epp = ((entite.composite.EPlayerPassword) event.getData());
            Player playerDropped = epp.player();
            LOG.debug(methodName + " - player dropped = " + playerDropped);

            if (appContext.getPlayer().getDroppedPlayers().contains(playerDropped)) {
                String err = LCUtil.prepareMessageBean("déjà dans DroppedPlayers");
                LOG.error(err);
                showMessageFatal(err);
                return null;
            }

            if (appContext.getPlayer().getDroppedPlayers().size() > 4) {
                String msg = "There are more than 4 dropped players";
                LOG.debug(msg);
                showMessageFatal(msg);
                return null;
            }

            findTeeStart.invalidateCache();
            List<String> ls = teeStartList(playerDropped);
            LOG.debug(methodName + " - teeStartList = " + ls);

            appContext.getPlayer().getDroppedPlayers().add(playerDropped);
            LOG.debug(methodName + " - droppedPlayers size = " + appContext.getPlayer().getDroppedPlayers().size());

            appContext.getPlayer().getDraggedPlayers().remove(epp);
            LOG.debug(methodName + " - draggedPlayers size = " + appContext.getPlayer().getDraggedPlayers().size());

            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Retire un joueur de la liste des joueurs droppés.
     * Migré depuis CourseController — 2026-02-25
     */
    public String playerRemove(Player p) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - player to remove = " + p);
            appContext.getPlayer().getDroppedPlayers().remove(p);
            appContext.getPlayer().getDroppedPlayers(); // refresh screen
            return "inscriptions_other_players.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Retourne la liste des joueurs inscrits au round courant.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<Player> roundPlayersList() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Round round = appContext.getRound();
            lp = roundPlayersListService.list(round);
            if (lp != null) {
                round.setPlayersString(Round.fillRoundPlayersString(lp));
            }
            return lp;
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Retourne la liste des tee starts disponibles pour un joueur dans le round courant.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<String> teeStartList(Player otherPlayer) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug(methodName + " - otherPlayer = " + otherPlayer);
        LOG.debug(methodName + " - currentPlayer = " + appContext.getPlayer());
        try {
            return findTeeStart.find(appContext.getCourse(), otherPlayer, appContext.getRound());
        } catch (SQLException e) {
            handleSQLException(e, methodName);
            return Collections.emptyList();
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Cherche la météo pour le club du round courant.
     * Migré depuis CourseController — 2026-02-25
     */
    public String findWeather() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            Club club = appContext.getClub();
            Inscription inscription = appContext.getInscription();
            LOG.debug(methodName + " - club = " + club);
            LOG.debug(methodName + " - player = " + appContext.getPlayer());
            LOG.debug(methodName + " - round = " + appContext.getRound());

            String weather = findOpenWeather.find(club, languageController.getLanguage()); // fix multi-user 2026-03-07
            if (weather == null) {
                LOG.debug(methodName + " - weather is null");
                inscription.setWeather("Weather returned from findWeather is null");
            } else {
                LOG.debug(methodName + " - weather OK = " + weather);
                inscription.setWeather(weather);
                inscription.setShowWeather(true);
            }
            return null;
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    // ========================================
    // MÉTHODES MIGRÉES depuis CourseController — Groupe B Score/Stableford — 2026-02-25
    // ========================================

    /**
     * Routes vers le bon enregistrement de score selon le type de jeu.
     * Migré depuis CourseController — 2026-02-25
     */
    public String selectedScoreToRegister(ECourseList ecl, String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with player = " + appContext.getPlayer().getIdplayer());
        LOG.debug("with ecl = " + ecl);
        LOG.debug("with type = " + type);
        try {
            Round round = ecl.round();
            appContext.setRound(round);
            if (Round.GameType.STABLEFORD.toString().equals(round.getRoundGame())) {
                return loadScoreStableford(ecl, type);
            }
            LOG.debug("!!! No register score for = " + round.getRoundGame());
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Charge et complète un score Stableford pour affichage/saisie.
     * Migré depuis CourseController — 2026-02-25
     */
    public String loadScoreStableford(ECourseList ecl, String type) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with type = " + type);
        LOG.debug("with ecl = " + ecl);
        try {
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());
            appContext.setRound(ecl.round());
            tee = ecl.tee();

            if ("competition".equals(type)) {
                appContext.getPlayer().setIdplayer(ecl.player().getIdplayer());
                appContext.getPlayer().setPlayerLastName(ecl.player().getPlayerLastName());
                appContext.getPlayer().setPlayerFirstName("???");
                appContext.getPlayer().setPlayerGender(ecl.player().getPlayerGender());
            }

            ScoreStableford score = stablefordController.completeScoreStableford(
                    appContext.getPlayer(), appContext.getRound(), ecl.tee());
            appContext.setScoreStableford(score);

            appContext.setScoreType("INDIVIDUAL");
            return "score_stableford.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Calcule le score Stableford (WHS uniquement).
     * Migré depuis CourseController — 2026-02-25
     */
    public String calculateScoreStableford() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for Round = " + appContext.getRound());
        LOG.debug("for Course = " + appContext.getCourse());
        LOG.debug("for current Player = " + appContext.getPlayer());
        LOG.debug("with scoreType = " + appContext.getScoreType());
        LOG.debug("with competitionPlayerId = " + appContext.getCompetitionPlayerId());
        try {
            Player p;
            if ("COMPETITION".equals(appContext.getScoreType())) {
                LOG.debug("handling COMPETITION scoreType");
                int playerid = appContext.getCompetitionPlayerId();
                p = playerManager.readPlayer(playerid);
            } else {
                LOG.debug("handling INDIVIDUAL scoreType");
                p = appContext.getPlayer();
            }

            Round round = appContext.getRound();
            if (round.getRoundDate().isBefore(START_DATE_WHS)) {
                String msg = "EGA Handicapping ! - THIS part is invalidated !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }

            LOG.debug("going to calculations !");
            ScoreStableford score = calcScoreStableford.calc(
                    p, appContext.getScoreStableford(), round, appContext.getCourse(), tee);
            appContext.setScoreStableford(score);
            LOG.debug("scoreStableford completed = " + score);

            return "score_stableford.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Charge la table des statistiques avant affichage.
     * Migré depuis CourseController — 2026-02-25
     */
    public String loadStatisticsTable() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            ScoreStableford score = appContext.getScoreStableford();
            LOG.debug("scoreStableford = " + score);

            var v = readStatisticsListService.load(appContext.getPlayer(), appContext.getRound());
            score.setStatisticsList(v);

            if (utils.LCUtil.isArrayAllZeroes(score.getStrokeArray())) {
                int[] arr = score.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray();
                score.setStrokeArray(arr);
            }

            var v1 = stablefordController.completeStatisticsListWithStrokes(
                    score.getStatisticsList(), score.getStrokeArray());
            score.setStatisticsList(v1);

            return "score_statistics.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Crée les statistiques Stableford et active le bouton scorecard.
     * Migré depuis CourseController — 2026-02-25
     */
    public String createStatisticsStableford() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            ScoreStableford score = appContext.getScoreStableford();
            Round round = appContext.getRound();
            LOG.debug("round = " + round);
            LOG.debug("scoreStableford = " + score);
            LOG.debug("List statistics = " + score.getStatisticsList().toString());

            // Validation métier avant SQL — règles du trigger update_score_trigger
            for (ScoreStableford.Statistics stt : score.getStatisticsList()) {
                if (stt.getPar() != null && stt.getPar() == 3
                        && stt.getFairway() != null && stt.getFairway() > 0) {
                    String msg = "Hole " + stt.getHole() + " : Par 3 — Fairway must be zero";
                    LOG.error(methodName + " - " + msg);
                    showMessageFatal(msg);
                    stt.setFairway(0);
                    return null;
                }
            } // end for validation

            if (createStatisticsStableford.create(appContext.getPlayer(), round, score)) {
                String msg = "statistics created !!";
                LOG.debug(msg);
                showMessageInfo(msg);
                setNextScorecard(true);
            } else {
                LOG.debug("statistics NOT created : error !!");
            }
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Crée un HandicapIndex pour un round qualifiant.
     * Migré depuis CourseController — 2026-02-25
     */
    public String createHandicapIndex() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("for ScoreStableford = " + appContext.getScoreStableford());
        LOG.debug("for HandicapIndex = " + appContext.getHandicapIndex());
        try {
            Round round = appContext.getRound();
            if (!"Y".equals(round.getRoundQualifying())) {
                String msg = "No HandicapIndex creation because Round is not Qualifying !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }

            appContext.setHandicapIndex(handicapController.create(
                    appContext.getScoreStableford(), appContext.getPlayer(), round));

            if (appContext.getHandicapIndex() == null) {
                LOG.debug("handicapIndex is null in RoundController");
            }
            return "score_stableford.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    // ========================================
    // MÉTHODES MIGRÉES depuis CourseController — Groupe D Matchplay/Participants — 2026-02-25
    // ========================================

    /**
     * Liste des participants Matchplay.
     * Migré depuis CourseController — 2026-02-25
     */
    public String listParticipants_mp(Matchplay mp) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with round = " + mp.getIdround());
        try {
            Round round = appContext.getRound();
            round.setIdround(mp.getIdround());
            LOG.debug("liste participants matchplay = " + Arrays.deepToString(listmatchplay.toArray()));
            round.setRoundGame(listmatchplay.get(0).getRoundGame());
            return "show_participants.xhtml?faces-redirect=true&cmd=MP_";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Liste des participants Scramble.
     * Créé 2026-02-28 — navC.listParticipants_scramble n'existait pas, calqué sur listParticipants_mp
     */
    public String listParticipants_scramble(ScoreScramble scr) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with round = " + scr.getIdround());
        try {
            Round round = appContext.getRound();
            round.setIdround(scr.getIdround());
            round.setRoundGame(scr.getRoundGame());
            return "show_participants.xhtml?faces-redirect=true&cmd=SCR";
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Liste des participants Stableford pour un round.
     * Migré depuis CourseController — 2026-02-25
     */
    public String listParticipantsStablefordRound(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with round = " + ecl.round().getIdround());
        try {
            appContext.setRound(ecl.round());
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());

            listStableford = participantsRoundList.list(appContext.getRound());
            if (listStableford.isEmpty()) {
                String msg = "Aucun participant inscrit pour ce round";
                LOG.info(msg);
                showMessageInfo(msg);
                return null;
            }

            LOG.info("Traitement de {} participants", listStableford.size());
            LOG.debug("liste participants stableford = " + Arrays.deepToString(listStableford.toArray()));

            String playersString = Round.fillRoundPlayersStringEcl(listStableford);
            appContext.getRound().setPlayersString(playersString);
            LOG.debug("PlayersString is now " + appContext.getRound().getPlayersString());
            appContext.getRound().setRoundGame(listStableford.get(0).round().getRoundGame());

            return "show_participants_stableford.xhtml?faces-redirect=true&cmd=ROUND";
        } catch (InvalidRoundException e) {
            String msg = "Round invalide : Ce parcours doit avoir 18 trous. "
                    + "Veuillez vérifier la configuration du round." + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return null;
        }
    } // end method

    /**
     * Liste des participants Stableford pour une compétition.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<ECourseList> listParticipantsStablefordCompetition(CompetitionDescription cde) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " for CompetitionDescription = " + cde);
        try {
            var li = participantsStablefordCompetitionList.list(cde);
            LOG.debug("liste participants stableford = " + Arrays.deepToString(li.toArray()));
            return li;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * Calcule les résultats Matchplay entre 2 joueurs.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<EMatchplayResult> calcMatchplayResult() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            if (listStableford.size() != 2) {
                String msg = "Pour matchplay il faut deux joueurs !!";
                LOG.error(msg);
                showMessageFatal(msg);
                return Collections.emptyList();
            }
            var p1 = listStableford.get(0).player();
            LOG.debug("listStableford player 1 = " + p1.getIdplayer());
            var p2 = listStableford.get(1).player();
            LOG.debug("listStableford player 2 = " + p2.getIdplayer());

            var li = calcMatchplayResult.calc(p1, p2, appContext.getRound());
            LOG.debug("liste participants stableford = " + Arrays.deepToString(li.toArray()));
            return li;
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // MÉTHODES MIGRÉES depuis CourseController — Groupe C Scorecard — 2026-02-25
    // ========================================

    /**
     * Entry point pour affichage scorecard. Extrait le contexte depuis ECourseList.
     * Migré depuis CourseController — 2026-02-25
     */
    public String scorecard(ECourseList ecl) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with ecl = " + ecl.toDisplayString());
        try {
            appContext.setClub(ecl.club());
            appContext.setCourse(ecl.course());
            appContext.setRound(ecl.round());
            appContext.setInscription(ecl.inscription());
            tee = ecl.tee();
            LOG.debug("Tee is now = " + tee.toString());
            String msg = "Select EcourseList Successful"
                    + " <br/> Club name = " + ecl.club().getClubName()
                    + " <br/> course name = " + ecl.course().getCourseName()
                    + " <br/> round = " + ecl.round().getIdround();
            LOG.debug(msg);
            return "scorecard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Affiche le scorecard — route vers WHS ou erreur si EGA.
     * Migré depuis CourseController — 2026-02-25
     */
    public String show_scorecard() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with round = " + appContext.getRound());
        try {
            Round round = appContext.getRound();
            if (round.getRoundDate().isBefore(START_DATE_WHS)) {
                LOG.info("this is an EGA round - no more supported !!!");
                String msg = "EGA round — no longer supported";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            } else {
                String msg = "this is a WHS round : " + round.getIdround()
                        + " on date = " + round.getRoundDate();
                LOG.info(msg);
                showMessageInfo(msg);
                return "show_scorecard_whs.xhtml?faces-redirect=true";
            }
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * Affiche un scorecard vide pour un cours sélectionné.
     * Migré depuis CourseController — 2026-02-25
     */
    public String show_scorecard_empty(ECourseList ecl) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with ecl = " + ecl);
        try {
            Club club = appContext.getClub();
            club.setIdclub(ecl.club().getIdclub());
            Course course = appContext.getCourse();
            course.setIdcourse(ecl.course().getIdcourse());
            return showScoreList.show_empty(appContext.getPlayer(), club, course,
                    appContext.getRound(), appContext.getInscription());
        } catch (Exception e) {
            handleGenericException(e, methodName);
            return null;
        }
    } // end method

    /**
     * ScoreCardList1 pour rounds EGA (avant START_DATE_WHS).
     * Migré depuis CourseController — 2026-02-25
     */
    public List<ECourseList> ScoreCardList1EGA() throws SQLException, LCException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return scoreCardList1EGA.list(appContext.getPlayer(), appContext.getRound());
        } catch (NullPointerException | SQLException ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * ScoreCardList1 WHS — retourne le HandicapIndex à la date du round.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<HandicapIndex> ScoreCardList1WHS() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            appContext.getHandicapIndex().setHandicapPlayerId(appContext.getPlayer().getIdplayer());
            appContext.getHandicapIndex().setHandicapDate(appContext.getRound().getRoundDate());
            appContext.setHandicapIndex(findHandicapIndexAtDate.find(appContext.getHandicapIndex()));

            List<HandicapIndex> lhi = new ArrayList<>();
            lhi.add(appContext.getHandicapIndex());
            return lhi;
        } catch (NullPointerException | SQLException ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * ScoreCardList2 — slope rating et données du cours.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<ECourseList> ScoreCardList2() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return findSlopeRating.find(appContext.getPlayer(), appContext.getRound());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * ScoreCardList3 — données de score détaillées (putts, bunker, penalty).
     * Migré depuis CourseController — 2026-02-25
     */
    public List<ECourseList> ScoreCardList3() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return scoreCardList3.list(appContext.getPlayer(), appContext.getRound());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    /**
     * ScoreCardList4 — liste détaillée des scores par trou.
     * Migré depuis CourseController — 2026-02-25
     */
    public List<ScoreStableford.Score> ScoreCardList4() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return readScoreListService.read(appContext.getPlayer(), appContext.getRound(), tee);
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // COMPETITION — migrated 2026-02-25
    // ========================================

    /**
     * Crée une inscription à une compétition pour le joueur courant.
     * Appelé depuis competition_list_inscriptions.xhtml
     */
    public String createInscriptionCompetition(ECompetition ec) throws SQLException, IOException, InstantiationException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("entering CreateInscriptionCompetition with Competition param = " + ec);
        LOG.debug("entering CreateInscriptionCompetition with Competition Data= " + ec.competitionData());
        LOG.debug("for player = " + appContext.getPlayer());
        ec.competitionData().setCmpDataCompetitionId(ec.competitionDescription().getCompetitionId());
        ec.competitionData().setCmpDataPlayerId(appContext.getPlayer().getIdplayer());
        ec.competitionData().setCmpDataPlayerGender(appContext.getPlayer().getPlayerGender());
        ec.competitionData().setCmpDataPlayerFirstLastName(appContext.getPlayer().getPlayerLastName() + ", " + appContext.getPlayer().getPlayerFirstName());
        LOG.debug("with competitionData = " + ec.competitionData());
        if (createCompetitionData.create(ec.competitionData())) {
            String msg = "Inscription data competition OK for " + String.valueOf(ec.competitionData().getCmpDataCompetitionId());
            LOG.info(msg);
            showMessageInfo(msg);
        } else {
            String msg = "Failure Inscription competition NOT OK";
            LOG.error(msg);
            showMessageFatal(msg);
        }
        return "competition_list_inscriptions.xhtml?faces-redirect=true";
    } // end method

    /**
     * Crée les rounds et inscriptions d'une compétition.
     * Appelé depuis competition_admin_menu.xhtml
     */
    public String createRoundsCompetition(CompetitionDescription cd) throws SQLException, IOException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with CompetitionDescription = " + cd);
        if (!createCompetitionRounds.create(cd)) {
            String msg = "Create Rounds competition NOT OK for competition = " + cd.getCompetitionId();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } else {
            String msg = "Create Rounds competition OK for competition = " + cd.getCompetitionId();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        cd.setCompetitionStatus("2");
        if (!updateCompetitionDescription.update(cd)) {
            String msg = "NOT modify Competition Description Status !! ";
            LOG.debug(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } else {
            String msg = "competitionStatus = " + cd.getCompetitionStatus();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        if (!createCompetitionInscriptions.create(cd)) {
            String msg = "Create competitionInscriptions NOT OK " + cd.getCompetitionId();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } else {
            String msg = "Create CompetitionInscriptions OK " + cd.getCompetitionId();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        cd.setCompetitionStatus("3");
        if (!updateCompetitionDescription.update(cd)) {
            String msg = "NOT modify Competition Description !! ";
            LOG.info(msg);
            showMessageInfo(msg);
            return null;
        } else {
            String msg = "competitionStatus = " + cd.getCompetitionStatus();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        competitionRoundsList.invalidateCache(); // migrated 2026-02-25
        return "competition_admin_menu.xhtml?faces-redirect=true";
    } // end method

    /**
     * Annule l'inscription d'un joueur à une compétition.
     * Appelé depuis competition_list_inscriptions.xhtml
     */
    public String cancelInscriptionCompetition(ECompetition ec) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with ec = " + ec.toString());
        if (deleteInscriptionCompetition.delete(ec)) {
            String msg = "inscription deleted = " + ec.competitionData().getCmpDataId()
                    + " for player = " + ec.competitionData().getCmpDataPlayerId()
                    + " for competition = " + ec.competitionDescription().getCompetitionName();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        competitionInscriptionsList.invalidateCache(); // migrated 2026-02-25
        competitionInscriptionsList.list(ec.competitionDescription()); // refresh without deleted item
        return "competition_list_inscriptions.xhtml?faces-redirect=true";
    } // end method

    /**
     * Gère l'événement de toggle d'une ligne de compétition (PrimeFaces).
     */
    public void onRowToggleCompetition(ToggleEvent event) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("event data = " + event.getData().toString());
        LOG.debug("event visibility = " + event.getVisibility());
        String msg = "Row State " + event.getVisibility() + " / " + event.getData().toString();
        LOG.debug(msg);
        showMessageInfo(msg);
    } // end method

    // ========================================
    // LIST — migrated 2026-02-25
    // ========================================

    /**
     * Liste les rounds récents du joueur courant.
     * Appelé depuis select_participants_round.xhtml et autres.
     */
    public List<ECourseList> listRecentRounds() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return recentRoundList.list(appContext.getPlayer());
        } catch (Exception ex) {
            handleGenericException(ex, methodName);
            return Collections.emptyList();
        }
    } // end method

    // ========================================
    // VALIDATION Matchplay — migrated 2026-02-25
    // ========================================

    /**
     * Stub validation matchplay 2.
     */
    public void validateScoreHoleMatchplay2() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
    } // end method

    /**
     * Validation JSF pour le score matchplay.
     */
    public void validateScoreHoleMatchplay(FacesContext context, UIComponent toValidate, Object value)
            throws ValidatorException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("toValidate ClientId = " + toValidate.getClientId());
        LOG.debug("toValidate Id = " + toValidate.getId());
        LOG.debug("value = " + value.toString());
        LOG.debug("UIcomponent, getFamily = " + toValidate.getFamily());
        LOG.debug("UIcomponent, context = " + context.toString());
        LOG.debug("UIcomponent, message = " + toValidate.getClientId(context));
        String confirm = (String) value;

        String field1Id = (String) toValidate.getAttributes().get("scorePlayer11");
        LOG.debug("field1Id = " + field1Id);

        UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
        LOG.debug("passComponent = " + passComponent);
        String pass = (String) passComponent.getSubmittedValue();
        LOG.debug("pass1 = " + pass);
        if (pass == null) {
            pass = (String) passComponent.getValue();
            LOG.debug("pass2 = " + pass);
        }

        if (!pass.equals(confirm)) {
            LOG.debug("pass not equal confirm");
            String field1Id_copy = toValidate.getClientId(context);
            showMessageFatal(field1Id_copy);
        }
    } // end method

    // ========== COMPETITION MANAGEMENT — migrated 2026-02-25 (Phase 3A) ==========

    /** Convenience getter — delegates to ApplicationContext */
    public ECompetition getCompetition() {
        return appContext.getCompetition();
    } // end method

    /** Convenience setter — delegates to ApplicationContext */
    public void setCompetition(ECompetition competition) {
        appContext.setCompetition(competition);
    } // end method

    public Map<String, String> getAvailableQualifying() {
        return availableQualifying;
    } // end method

    public List<String> getGameList() {
        return gameList;
    } // end method

    // --- Method 1: createCompetitionDescription ---
    public String createCompetitionDescription() throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        ECompetition competition = appContext.getCompetition();
        LOG.debug("for competition = " + competition);
        LOG.debug("for club = " + appContext.getClub());
        LOG.debug("for course = " + appContext.getCourse());
        competition.competitionDescription().setCompetitionClubId(appContext.getClub().getIdclub());
        competition.competitionDescription().setCompetitionCourseId(appContext.getCourse().getIdcourse());
        if (createCompetitionDescriptionService.create(competition.competitionDescription())) {
            String msg = "competition Description created = " + competition;
            LOG.info(msg);
            showMessageInfo(msg);
        } else {
            String msg = "ERROR Inscription competition KO for "
                    + competition.competitionDescription().getCompetitionName();
            LOG.error(msg);
            showMessageFatal(msg);
        }
        return null;
    } // end method

    // --- Method 2: beforeInscriptionCompetition ---
    public String beforeInscriptionCompetition(CompetitionDescription ec) throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with competition Description = " + ec);
        appContext.setCompetition(appContext.getCompetition().withCompetitionDescription(ec));
        return "competition_inscription.xhtml?faces-redirect=true&operation=add";
    } // end method

    // --- Method 3: competitionTimeStartList ---
    public List<String> competitionTimeStartList(String s) throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        ECompetition competition = appContext.getCompetition();
        LOG.debug("competition = " + competition);
        try {
            return calcCompetitionTimeStartList.calc(competition);
        } catch (Exception e) {
            String msg = "Exception in competitionTimeStartList = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // --- Method 4: listCompetitions ---
    public List<CompetitionDescription> listCompetitions() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return competitionDescriptionList.list();
        } catch (Exception ex) {
            String msg = "Exception in listCompetitions() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // --- Method 5: beforeListInscriptionsCompetition ---
    public String beforeListInscriptionsCompetition(CompetitionDescription cd) throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with competition description = " + cd);
        appContext.setCompetition(appContext.getCompetition().withCompetitionDescription(cd));
        competitionInscriptionsList.invalidateCache(); // migrated 2026-02-25
        return "competition_list_inscriptions.xhtml?faces-redirect=true&operation=add";
    } // end method

    // --- Method 6: beforeCompetitionMenu ---
    public String beforeCompetitionMenu(CompetitionDescription ec) throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with competition Description = " + ec);
        appContext.setCompetition(appContext.getCompetition().withCompetitionDescription(ec));
        return "competition_admin_menu.xhtml?faces-redirect=true&operation=menu";
    } // end method

    // --- Method 7: listInscriptionsCompetition ---
    public List<ECompetition> listInscriptionsCompetition() throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        ECompetition competition = appContext.getCompetition();
        LOG.debug("with competition Description = " + competition.competitionDescription());
        var lp1 = competitionInscriptionsList.list(competition.competitionDescription());
        LOG.debug("var lp1 = " + lp1);
        return lp1;
    } // end method

    // --- Method 8: beforeListStartCompetition ---
    public String beforeListStartCompetition(CompetitionDescription cd, String type_exec) throws SQLException, java.io.IOException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("with competition description = " + cd);
        LOG.debug("with type = " + type_exec);
        if (type_exec.equals(entite.CompetitionDescription.StatusExecution.PROVISIONAL.toString())
                || type_exec.equals(entite.CompetitionDescription.StatusExecution.FINAL.toString())) {
            LOG.debug("good execution type - PROVISIONAL or FINAL");
        }
        cd.setCompetitionExecution(type_exec);
        appContext.setCompetition(appContext.getCompetition().withCompetitionDescription(cd));
        LOG.debug("competition modified = " + appContext.getCompetition());
        return "competition_list_start.xhtml?faces-redirect=true&operation=add";
    } // end method

    // --- Method 9: listStartCompetition ---
    public List<ECompetition> listStartCompetition() throws SQLException, java.io.IOException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        ECompetition competition = appContext.getCompetition();
        LOG.debug("with competition Description = " + competition.competitionDescription());
        LOG.debug("CompetitionStatus = " + competition.competitionDescription().getCompetitionStatus());
        try {
            String execution = competition.competitionDescription().getCompetitionExecution();
            LOG.debug("CompetitionExecution = " + execution);
            String msg = "ListStartCompetition OK";
            LOG.debug(msg);
            // 1. d'abord
            List<ECompetition> li = competitionInscriptionsList.list(competition.competitionDescription());
            LOG.debug("line 01 after call competitionInscriptionsList li size = " + li.size());
            if (li == null) {
                msg = "there are no inscriptions : we do nothing for competition Id  = "
                        + competition.competitionDescription().getCompetitionId();
                LOG.debug(msg);
                LCUtil.showMessageFatal(msg);
                return li;
            }
            // 2. ensuite
            LOG.debug("we go to CompetitionStartlist with Execution = " + execution);
            li.get(0).competitionDescription().setCompetitionExecution(execution);
            var csl = competitionStartList.list(li);
            if (csl == null) {
                msg = "Empty CompetitionStartList !! ";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            } else {
                msg = "Completed CompetitionStartList = " + csl.size()
                        + " for competition Id  = " + competition.competitionDescription().getCompetitionId();
                LOG.info(msg);
            }
            // mod status
            competition.competitionDescription().setCompetitionStatus("1");
            if (updateCompetitionDescription.update(competition.competitionDescription())) {
                msg = "OK result of modify Competition Description";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
            } else {
                msg = "KO KO  result of modify Competition Description";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return null;
            }
            return csl;
        } catch (Exception ex) {
            String msg = "Exception in listStartCompetition() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // --- Method 10: qualifyingListener ---
    public void qualifyingListener(ValueChangeEvent e) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        LOG.debug("OldValue = " + e.getOldValue());
        LOG.debug("NewValue = " + e.getNewValue());
        Round round = appContext.getRound();
        if (e.getNewValue().equals("Y")) {
            round.setShowQualifying(true);
        } else {
            round.setShowQualifying(false);
        }
        LOG.debug("showQualifying is " + round.isShowQualifying());
        PrimeFaces.current().executeScript("window.location.reload(true);");
    } // end method

    // --- Method 11: listInscriptions ---
    public List<ECourseList> listInscriptions() throws SQLException {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            return inscriptionListService.list();
        } catch (Exception ex) {
            String msg = "Exception in listInscriptions() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } // end method

    // ========================================
    // NAVIGATION — migrated 2026-02-25 (Phase 3B)
    // ========================================

    /**
     * Navigation vers inscription (STB/SCR/MP/COMPETITION)
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_select_inscription_xhtml(String s) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        try {
            // setFilteredInscriptions(null) removed 2026-02-28 — dead field, reset() handles cleanup
            navigationController.reset(s);
            if (s.equals("TEST")) {
                return "selectRound.xhtml?faces-redirect=true&cmd=" + s;
            }
            if (s.equals(Round.GameType.STABLEFORD.toString())) {
                return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;
            }
            if (s.equals(Round.GameType.SCRAMBLE.toString())) {
                return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;
            }
            if (s.equals("MATCHPLAY")) {
                return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;
            }
            if (s.equals("ADMIN COMPETITION")) {
                return "select_competition_admin.xhtml?faces-redirect=true&cmd=" + s;
            }
            if (s.equals("PLAYER COMPETITION")) {
                return "select_competition_player.xhtml?faces-redirect=true&cmd=" + s;
            }
            return "playing formule not found";
        } catch (Exception ex) {
            LOG.error("Exception in " + methodName + " " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } // end method

    /**
     * Navigation vers selectRound (INSCRIPTION)
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_select_round_xhtml(String s) throws Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        try {
            // setFilteredInscriptions(null) removed 2026-02-28 — dead field, reset() handles cleanup
            navigationController.reset(s);
            if (s.equals("INSCRIPTION")) {
                LOG.debug("handling inscription !");
                appContext.setInputSelectRound(s);
                LOG.debug("sessionMap : inputSelectRound = " + appContext.getInputSelectRound());
                return "selectRound.xhtml?faces-redirect=true";
            }
            return "playing formule not found";
        } catch (Exception ex) {
            LOG.error("Exception in " + methodName + " " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } // end method

    /**
     * Navigation vers select_participants_round
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_selectParticipantsRound_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "select_participants_round.xhtml?faces-redirect=true&cmd=" + s;
    } // end method

    /**
     * Navigation vers selectStablefordRounds
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_selectStablefordRounds_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "selectStablefordRounds.xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers selectRegisteredRounds
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_selectRegisteredRounds_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "selectRegisteredRounds.xhtml?faces-redirect=true&cmd=" + s;
    } // end method

    /**
     * Navigation vers stableford_playing_hcp
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_stableford_playing_hcp_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        this.playingHcp = new PlayingHandicap(); // migrated 2026-02-26 — was navigationController.setPlayingHcp()
        return "stableford_playing_hcp.xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers scramble_playing_hcp
     * Migré depuis menu.xhtml url= — 2026-02-28
     */
    public String to_scramble_playing_hcp_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        this.playingHcp = new PlayingHandicap();
        return "scramble_playing_hcp.xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers othergames_playing_hcp
     * Migré depuis menu.xhtml url= — 2026-02-28
     */
    public String to_othergames_playing_hcp_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        this.playingHcp = new PlayingHandicap();
        return "othergames_playing_hcp.xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers selectMatchplayRounds
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_selectMatchplayRounds_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "selectMatchplayRounds.xhtml?faces-redirect=true";
    } // end method

    /**
     * Navigation vers selectScrambleRounds
     * Migré depuis CourseController — 2026-02-25
     */
    public String to_selectScrambleRounds_xhtml(String s) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName + " with string = " + s);
        navigationController.reset(s);
        return "selectScrambleRounds.xhtml?faces-redirect=true";
    } // end method

    // ========================================
    // MONGO CALCULATIONS — migrated 2026-02-26 from CourseController
    // ========================================

    /**
     * Charge le texte des calculs MongoDB pour le round sélectionné.
     * Appelé depuis dialog_played_rounds.xhtml via f:viewAction.
     */
    public void textCalculationRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            ECourseList selectedRound = appContext.getSelectedPlayedRound();
            LOG.debug(methodName + " - selected roundid = " + selectedRound.round().getIdround());
            LOG.debug(methodName + " - current playerid = " + appContext.getPlayer().getIdplayer());
            LoggingUser logging = new LoggingUser();
            logging.setLoggingIdPlayer(appContext.getPlayer().getIdplayer());
            logging.setLoggingIdRound(selectedRound.round().getIdround());
            logging.setLoggingType("R"); // Round
            LOG.debug(methodName + " - logging_user = " + logging);
            selectedRound.round().setCalculations(mongoCalculationsController.read(logging));
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    // ========================================
    // PLAYING HCP — migrated 2026-02-26 from CourseController
    // ========================================

    public PlayingHandicap getPlayingHcp() {
        return playingHcp;
    } // end method

    public void setPlayingHcp(PlayingHandicap playingHcp) {
        this.playingHcp = playingHcp;
    } // end method

    public void simulateHcpStb() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - PlayingHcp = " + playingHcp);
            LOG.debug(methodName + " - array input data " + Arrays.toString(playingHcp.getHcpScr()));
            Handicap h = new Handicap();
            h.setHandicapPlayerEGA(java.math.BigDecimal.valueOf(playingHcp.getHandicapPlayerEGA()));

            Tee t = new Tee();
            t.setTeeSlope(playingHcp.getTeeSlope().shortValue());
            LOG.debug(methodName + " - slope = " + t.getTeeSlope());
            t.setTeeRating(java.math.BigDecimal.valueOf(playingHcp.getTeeRating()));
            LOG.debug(methodName + " - rating = " + t.getTeeRating());
            t.setTeePar(playingHcp.getCoursePar().shortValue());
            LOG.debug(methodName + " - par = " + t.getTeePar());

            Round r = new Round();
            r.setRoundHoles(playingHcp.getRoundHoles());
            int i = 0;
            // int i = new calc.CalcStablefordPlayingHandicapEGA().calculatePlayingHcp(conn, h, t, r);
            playingHcp.setPlayingHandicap(i);
            LOG.debug(methodName + " - Playing Hcp calculated = " + playingHcp.getPlayingHandicap());
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    public void calculateHcpScramble() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        try {
            LOG.debug(methodName + " - input array data = " + Arrays.toString(playingHcp.getHcpScr()));
        } catch (Exception e) {
            handleGenericException(e, methodName);
        }
    } // end method

    /*
    void main() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        // Tests locaux — nécessite WildFly + CDI
        LOG.debug("RoundController main() — non applicable sans contexte CDI");
    } // end main
    */

    // ========================================
    // viewChartPlayedRound — migrated 2026-02-27 from CourseController
    // ========================================

    public void viewChartPlayedRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        Map<String, Object> options = new HashMap<>();
        options.put("modal", true);
        options.put("draggable", false);
        options.put("resizable", true);
        options.put("contentHeight", 320);
        options.put("contentWidth", 640);
        options.put("closable", true);
        options.put("header", "header by LC");
        PrimeFaces.current().dialog().openDynamic("viewChartRounds", options, null);
    } // end method

    // ========================================
    // initInputPlayingHcp — migrated 2026-02-27 from CourseController
    // ========================================

    public void initInputPlayingHcp(int inputPlayingHcp) {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
        this.inputPlayingHcp = inputPlayingHcp;
        LOG.debug(methodName + " - inputPlayingHcp = " + inputPlayingHcp);
        PlayingHandicap ph = new PlayingHandicap();
        ph.setPlayingHandicap(this.inputPlayingHcp);
    } // end method

    public int getInputPlayingHcp() {
        return inputPlayingHcp;
    } // end method

    public void setInputPlayingHcp(int inputPlayingHcp) {
        this.inputPlayingHcp = inputPlayingHcp;
    } // end method

} // end class
