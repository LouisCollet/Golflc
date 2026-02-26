package Controllers;

import calc.CalcScoreStableford;
import payment.PaymentSubscriptionController;
import entite.composite.EUnavailable;
import entite.composite.ECompetition;
import entite.composite.EMatchplayResult;
import entite.composite.EPlayerPassword;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.*;
import entite.Creditcard.etypePayment;
import exceptions.LCException;
import static interfaces.ConsoleColors.BLUE_UNDERLINED;
import static interfaces.ConsoleColors.CYAN_BACKGROUND_BRIGHT;
import static interfaces.ConsoleColors.GREEN;
import static interfaces.ConsoleColors.RED;
import static interfaces.ConsoleColors.RED_BACKGROUND;
import static interfaces.ConsoleColors.RED_BOLD;
import static interfaces.ConsoleColors.RESET;
import static interfaces.GolfInterface.ZDF_TIME_HHmm;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.ApplicationMap;
import jakarta.faces.annotation.SessionMap;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.model.SelectItem;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.primefaces.PrimeFaces;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.map.Overlay;
import security.LoginBeanSecurity;
import service.CountryService;
// import connection_package.DBConnection; // removed 2026-02-26 — CDI migration
import context.ApplicationContext;
import dialog.DialogResult;
import entite.composite.ECourseList;
import enumeration.ClubSelectionPurpose;
import exceptions.InvalidRoundException;
import static exceptions.LCException.handleGenericException;
import static interfaces.GolfInterface.EMPTY_STRING;
import static interfaces.GolfInterface.START_DATE_WHS;
import static interfaces.GolfInterface.ZDF_DAY;
import static interfaces.GolfInterface.ZDF_TIME;
import jakarta.enterprise.context.SessionScoped;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import static java.lang.System.out;
import java.net.http.HttpHeaders;
import java.time.Month;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import manager.PlayerManager;
import payment.PaymentOrchestrator;
import payment.PaymentTarget;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showDialogInfo;

@Named("courseC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped  // modifié 02-02-2026 fondamental !!
//@ApplicationScoped
@jakarta.ws.rs.Path("courseController") // link avec server python !! projet rest-api moved from CreditcardController on 12-08-2025
public class CourseController implements Serializable{
    
    
    // Services (⚠️ pas des entités)// injection CDI
           // ✅ SERVICES - avec @Inject
    /* ===============================
     * 1️⃣ Injections CDI
     * =============================== */
    // ✅ Injection du contexte de session
    @Inject private ApplicationContext appContext;
    @Inject private ExternalContext externalContext;
    @Inject private PlayerManager playerManager;  // oui !!!
    @Inject @SessionMap
    private Map<String, Object> sessionMap;

    @Inject @ApplicationMap
    private Map<String, Object> applicationMap;

    @Inject private DialogController dialogController;
    @Inject private CountryService countryService;
    @Inject private CalcScoreStableford calcScoreStableford;
    @Inject private contexte.ClubSelectionContextBean clubSelectionContext;
    /// enlevé 14-02-2026@Inject private Controller.refact.PlayerController playerC;  
        @Inject private service.CoordinatesService coordinatesService; 
    @Inject private SchedulerProController schedulerProController;
    // ✅ AJOUTÉ : injection CDI ReadClub
    @Inject private read.ReadClub readClubService;
    @Inject private read.ReadCourse readCourseService;
    @Inject private read.ReadTee readTeeService;
    @Inject private read.ReadHole readHoleService;
    @Inject private find.FindCountScore findCountScoreService;
    @Inject private create.CreateRound createRoundService;  // ✅ injection CDI provisoire
    // ✅ Ajouter l'injection
    @Inject private numbertext.Numbertext numbertextService;
    @Inject private lists.HandicapIndexList handicapIndexList;
    @Inject private lists.PlayedList playedList;
    @Inject private lists.InscriptionList inscriptionList;
    @Inject private lists.ClubsListLocalAdmin clubsListLocalAdmin;
    @Inject private lists.LessonProList lessonProList; // migrated 2026-02-23
    @Inject private mail.EmailService mailService;
    @Inject private ical.IcalService icalService;
    @Inject private mail.MailSender mailSender;
    @Inject private lists.CourseListOnly             courseListOnly;             // migrated 2026-02-24
    @Inject private lists.InscriptionListForOneRound inscriptionListForOneRound; // migrated 2026-02-24
    @Inject private lists.ParticipantsRoundList      participantsRoundList;      // migrated 2026-02-24
    @Inject private lists.RecentRoundList            recentRoundList;            // migrated 2026-02-24
    @Inject private lists.RoundPlayersList           roundPlayersList;           // migrated 2026-02-24
    @Inject private delete.DeleteRound               deleteRound;                // migrated 2026-02-24
    @Inject private create.CreateInscription         createInscription;          // migrated 2026-02-24
    @Inject private delete.DeleteInscription         deleteInscription;          // migrated 2026-02-24
    @Inject private delete.DeleteInscriptionCompetition deleteInscriptionCompetition; // migrated 2026-02-24
    @Inject private create.CreateOrUpdateScoreStableford createOrUpdateScoreStableford; // migrated 2026-02-24
    @Inject private create.CreateStatisticsStableford    createStatisticsStableford;    // migrated 2026-02-24
    @Inject private utils.ShowScore                  showScoreList;                 // migrated 2026-02-24
    @Inject private lists.ScoreCardList1EGA              scoreCardList1EGA;             // migrated 2026-02-24
    @Inject private lists.ScoreCardList3                 scoreCardList3;                // migrated 2026-02-24
    @Inject private update.UpdateCompetitionData                    updateCompetitionData;                    // migrated 2026-02-24
    @Inject private update.UpdateCompetitionDescription             updateCompetitionDescription;             // migrated 2026-02-24
    @Inject private create.CreateCompetitionDescription             createCompetitionDescription;             // migrated 2026-02-24
    @Inject private create.CreateCompetitionData                    createCompetitionData;                    // migrated 2026-02-24
    @Inject private create.CreateCompetitionRounds                  createCompetitionRounds;                  // migrated 2026-02-24
    @Inject private create.CreateCompetitionInscriptions            createCompetitionInscriptions;            // migrated 2026-02-24
    @Inject private lists.CompetitionDescriptionList                competitionDescriptionList;               // migrated 2026-02-24
    @Inject private lists.CompetitionInscriptionsList               competitionInscriptionsList;              // migrated 2026-02-24
    @Inject private lists.CompetitionRoundsList                     competitionRoundsList;                    // migrated 2026-02-24
    @Inject private lists.CompetitionStartList                      competitionStartList;                     // migrated 2026-02-24
    @Inject private lists.MatchplayList                             matchplayList;                            // migrated 2026-02-24
    @Inject private lists.ScrambleList                              scrambleList;                             // migrated 2026-02-24
    @Inject private lists.RegisterResultList                        registerResultList;                       // migrated 2026-02-24
    @Inject private lists.ParticipantsStablefordCompetitionList     participantsStablefordCompetitionList;    // migrated 2026-02-24
    @Inject private calc.CalcMatchplayResult                        calcMatchplayResult;                      // migrated 2026-02-24
    @Inject private find.FindInfoStableford                         findInfoStableford;                       // migrated 2026-02-24
    @Inject private find.FindSlopeRating                            findSlopeRating;                          // migrated 2026-02-24
    @Inject private find.FindCurrentSubscription                    findCurrentSubscription;                  // migrated 2026-02-24
    @Inject private find.FindTeeStart                               findTeeStart;                             // migrated 2026-02-24
    @Inject private lists.PlayersList                               playersList;                              // migrated 2026-02-24
    @Inject private lists.AllFlightsList                            allFlightsList;                           // migrated 2026-02-24
    @Inject private lists.CourseList                                courseList;                               // migrated 2026-02-24
    @Inject private lists.FlightAvailableList                       flightAvailableList;                      // migrated 2026-02-24
    @Inject private lists.ClubCourseTeeListOne                      clubCourseTeeListOne;                     // migrated 2026-02-24
    @Inject private lists.ClubList                                  clubList;                                 // migrated 2026-02-24
    @Inject private lists.SunriseSunsetList                         sunriseSunsetList;                        // migrated 2026-02-24
    @Inject private lists.HandicapList                              handicapList;                             // migrated 2026-02-24
    @Inject private lists.UnavailableListForDate                    unavailableListForDate;                   // migrated 2026-02-24
    @Inject private lists.SubscriptionRenewalList                   subscriptionRenewalList;                  // migrated 2026-02-24
    @Inject private read.LoadBlocking                               loadBlocking;                             // migrated 2026-02-24
    @Inject private read.ReadActivation                             readActivation;                           // migrated 2026-02-24
    @Inject private read.ReadParArray                               readParArray;                             // migrated 2026-02-24
    @Inject private read.ReadUnavailableStructure                   readUnavailableStructure;                 // migrated 2026-02-24
    @Inject private lists.ClubDetailList                            clubDetailList;                           // migrated 2026-02-24
    @Inject private lists.CourseListForClub                         courseListForClubService;                 // migrated 2026-02-24
    @Inject private lists.CoursesListLocalAdmin                     coursesListLocalAdmin;                    // migrated 2026-02-24
    @Inject private lists.ProfessionalClubList                      professionalClubList;                     // migrated 2026-02-24
    @Inject private lists.FindCountListProfessional                 findCountListProfessional;                // migrated 2026-02-24
    @Inject private lists.LocalAdminGreenfeeList                    localAdminGreenfeeList;                   // migrated 2026-02-24
    @Inject private lists.SystemAdminSubscriptionList               systemAdminSubscriptionList;              // migrated 2026-02-24
    @Inject private lists.LocalAdminCotisationList                  localAdminCotisationList;                 // migrated 2026-02-24
    @Inject private Controllers.UnavailableController               unavailableController;                    // migrated 2026-02-24
    @Inject private lists.ProfessionalListForClub                   professionalListForClub;                  // migrated 2026-02-24
    @Inject private lists.ProfessionalListForPayments               professionalListForPayments;              // migrated 2026-02-24
    @Inject private update.UpdateBlocking                           updateBlocking;                           // migrated 2026-02-24
    @Inject private update.UpdateAudit                              updateAudit;                              // migrated 2026-02-24
    @Inject private update.UpdateSubscription                       updateSubscription;                       // migrated 2026-02-24
    @Inject private update.UpdatePassword                           updatePassword;                           // migrated 2026-02-24
    @Inject private update.UpdatePlayer                             updatePlayer;                             // migrated 2026-02-24
    @Inject private create.CreateActivationPassword                createActivationPassword;                 // migrated 2026-02-24
    @Inject private create.CreateUnavailablePeriod                 createUnavailablePeriod;                  // migrated 2026-02-24
    @Inject private create.CreateTarifMember                       createTarifMember;                        // migrated 2026-02-24
    @Inject private create.CreateTarifGreenfee                     createTarifGreenfee;                      // migrated 2026-02-24
    @Inject private create.CreateProfessional                      createProfessional;                       // migrated 2026-02-24
    @Inject private find.FindOpenWeather                             findOpenWeather;                          // migrated 2026-02-25
    @Inject private Controllers.StablefordController                 stablefordController;                     // migrated 2026-02-25
    @Inject private read.ReadStatisticsList                          readStatisticsListService;                // migrated 2026-02-25
    @Inject private find.FindHandicapIndexAtDate                     findHandicapIndexAtDate;                  // migrated 2026-02-25
    @Inject private read.ReadScoreList                               readScoreListService;                     // migrated 2026-02-25
    @Inject private calc.CalcCompetitionTimeStartList                calcCompetitionTimeStartList;             // migrated 2026-02-25
    @Inject private create.CreateOrUpdateHolesGlobal               createOrUpdateHolesGlobal;                // migrated 2026-02-24
    @Inject private create.CreateAudit                             createAudit;                              // migrated 2026-02-24
    @Inject private create.CreateBlocking                          createBlocking;                           // migrated 2026-02-24
    @Inject private create.CreateLesson                            createLesson;                             // migrated 2026-02-24
    @Inject private create.CreatePlayer                            createPlayer;                             // migrated 2026-02-24
    @Inject private Controllers.HandicapController                 handicapController;                       // migrated 2026-02-24
    @Inject private Controllers.PasswordController                 passwordController;                       // migrated 2026-02-24
    @Inject private Controllers.ActivationController               activationController;                     // migrated 2026-02-24
    @Inject private Controllers.TarifGreenfeeController            tarifGreenfeeController;                  // migrated 2026-02-24
    @Inject private Controllers.TarifMemberController              tarifMemberController;                    // migrated 2026-02-24
    @Inject private payment.PaymentSubscriptionController          paymentSubscriptionController;            // migrated 2026-02-25
    @Inject private Controllers.CreditcardController               creditcardController;                     // migrated 2026-02-25
    @Inject private payment.PaymentGreenfeeController              paymentGreenfeeController;                // migrated 2026-02-25
    @Inject private find.FindTarifMembersData                      findTarifMembersData;                     // migrated 2026-02-25
    @Inject private find.FindTarifGreenfeeData                     findTarifGreenfeeData;                    // migrated 2026-02-25
    @Inject private delete.DeleteTarifGreenfee                     deleteTarifGreenfee;                      // migrated 2026-02-25
    @Inject private delete.DeleteTarifMember                       deleteTarifMember;                        // migrated 2026-02-25
    @Inject private cache.CacheInvalidator                         cacheInvalidator;                         // migrated 2026-02-25
    @Inject private Controllers.MongoCalculationsController        mongoCalculationsController;               // migrated 2026-02-26
    @Inject private create.CreateTableFlights                    createTableFlights;                        // migrated 2026-02-26
    @Inject private find.FindLastAudit                           findLastAudit;                             // migrated 2026-02-26


    private Player playerPro;

       
    // ✅ ENTITÉS - sans @Inject
   // debut enlever les Inject des entités qui suivent le 14-02-2026  
      // private Club club; — removed 2026-02-25 (delegates to appContext)
      // private Course course; — removed 2026-02-25 (delegates to appContext)
      // private Tee tee; — removed 2026-02-25 (migrated to ClubController)
      // private Hole hole; — removed 2026-02-25 (migrated to ClubController)
      
      private Round round; 

//       private Car car;
      private Handicap handicap;
    //  private HandicapIndex handicapIndex;
      private PlayingHandicap playingHcp;
      private ScoreStableford scoreStableford;
      private ScoreMatchplay scoreMatchplay; 
      private ScoreScramble scoreScramble; 
      private Inscription inscription;
      private Matchplay matchplay;
      private Subscription subscription;
      private Cotisation cotisation;
      // private HolesGlobal holesGlobal; — removed 2026-02-25 (migrated to ClubController)
      private Flight flight;
      private TarifGreenfee tarifGreenfee;
      private Creditcard creditcard;
      private LoginBeanSecurity login;
      private TarifMember tarifMember;
      private Greenfee greenfee;
      private Country country;
      private Blocking blocking;
      private Activation activation;
      private Password password;
      private UnavailableStructure unavailablestructure;
      private UnavailablePeriod unavailableperiod;
      private Professional professional; // new 25/05/2021 
// fin de enlever @Inject 14-02-2026
     private EUnavailable unavailable;
     // private ECompetition competition; — removed 2026-02-25 (now in appContext, bridge via getCompetition/setCompetition)
     // private Lesson selectedLesson; // migrated 2026-02-25 to PaymentController
   
 // new 15/01/2026   refactoring CDI
    //@Inject
    //private DialogController3 dialogController2; // utilisé dans DialogController version CDI compatible utilisé ici pour le close 
 // new 16/01/2026 refactoring CDI   
    //@Inject
  //  private calc.CalcScoreStableford calcScoreStableford; 
    private final static List<Integer> STROKEINDEX = new ArrayList<>();
    private final static List<Integer> NUMBERS = new ArrayList<>();
    private ECourseList selectedPlayedRound;
    private static int[] parArray = null;
    private String parArrayString = "";
    private static String otherGame = null;
    private SelectItem[] gameOptions;
    private static String[] games = null; 
    private static String[] seasons = null; 
    private SelectItem[] seasonOptions; 
//    private final static List<SelectItem> LANGUAGES = new ArrayList<>();
    private final static List<Integer> VALUES = new ArrayList<>();

    private List<Average> listavg;
    
    private List<Matchplay> listmatchplay;  // new 30/09/2014  Ã  mofifier certainement !!
    private List<ScoreScramble> listscr;  // new 30/09/2014


    private boolean NextStep; // 15/01/2013
    private boolean NextInscription; // 15/01/2013
    private boolean NextScorecard; // 15/01/2013
    private boolean NextPlayer; // 10/02/2013
    private boolean Connected; // 09/05/2013 // used for Logout button in header.xhtml, in selectPlayer
    private boolean skip;
    private boolean ShowButtonCreateScore; // 27/10/2013
 //   private static boolean ShowButtonCreateStatistics; // 20-10-2021 transféré vers ScoreStableford
    private static boolean ShowButtonCreditCard; // 27/10
    private boolean ShowButtonCreateCourse; // 27/10/2013
    private boolean NextPanelPlayer = false;  // 16/11//2013
    private List<?>filteredCars; // ne pas supprimer :nécessaire depuis Primefaces 3.4 (faut une List) 
    
    private List<ECourseList>filteredCourses = null; // mod 09-02-2021
    private List<Club>filteredClubs = null; // new 15-12-2023
    private ArrayList<Flight> filteredFlights;
    private List<EPlayerPassword>filteredPlayers = null; // 13-02-2021 ne pas utiliser List<?>
   // private List <ECourseList>filteredPlayedRounds = null; // 23-02-2026 migré vers PlayerController
 
    private List<?>filteredInscriptions; // new 03/08/2014 est ECourseList !
    private List<ECourseList> listStableford;
    private Player selectedPlayer;
    private EPlayerPassword selectedPlayerEPP; // selectPlayer.xhtml new 26-12-2024
    private List<Player> selectedPlayersMatchPlay; // new 31/08/2014
    private DualListModel<Player> dlPlayers; // new 03/09/2014
   // List<Player> playersTarget = new ArrayList<>();
    List<ECourseList> subscriptionRenewal;
    private List<Player> fullList;
    private int deletePlayer = 0;
    private String inputClub = null;
    private String inputClubOperation = null;
    private String inputcmdParticipants = null;
    private String inputCourse = null;
    private String inputCourseOperation = null;
    private String inputScorecard = null;
    private String inputScore = null;
  //  private String inputStat = null;
    private String inputParticipants = null;
    private String inputInscription = null;
    private String inputHandicap = null;
    private int inputPlayingHcp = 0;
    private String inputPlayedRounds = null;
    private final String htmlBr = "<br />";
    private final String htmlH1 = "<h1>";
    private String introductionTxt;
 //   private MapModel mapModel = null;  // new 26/08/2014
    private Overlay<Object> overlay; // mod 05-05-2024
    private List<Player> lp = null;
//private static BeID eID = null;
    // private Connection conn = null; // removed 2026-02-26 — CDI migration
    // private Connection connPool = null; // removed 2026-02-26 — CDI migration
    private String radioButtonJSF;
    private String createModifyPlayer = "C";  // utilisé pour choisir player.xhtml ou player_modify.xhtml
    private String SunRiseSet; // = null; 
    private String uuid;
    private String emoji;
    private int cptFlight = 0;
    private String lineModelCourse;
    ArrayList<Flight> flightList = null; // doit être ici idiot !!
    List<Course> courseListForClub = null;
    List<String> gameList = Arrays.asList("STABLEFORD","SCRAMBLE","CHAPMAN","STROKEPLAY","MP_FOURBALL","MP_FOURSOME","MP_SINGLE"); // used only in competition-create_description.xhtml
 //   List<String> parList = Arrays.asList("73","72","71","70","69","62","36","35","34");
 //   List<Integer> parList = Arrays.asList(73, 72, 71, 70, 69);
    private Map<String, String> availableQualifying; // +getter (no setter necessary)
  //  private Map<String, String> availableHoles; // +getter (no setter necessary)
    private Map<String, String> members;
    private Map<Integer, String> data = new HashMap<>();
    // private Integer progress1 = 0;  // migrated 2026-02-25 to PaymentController
    
    private UploadedFile uploadedFile;
 //   private List<FilterMeta> filterBy;
 // fields for schedule lessons with pro
 //@Inject org.primefaces.model.ScheduleEvent<?> eventSelected; // new 03/06/2021 
    private List<Professional> listProfessional  = new ArrayList<>();
    private Professional selectedPro = null;
    // private List<Lesson> listLessons = new ArrayList<>(); // migrated 2026-02-25 to PaymentController
    //private Integer cpt = 0; 
    // private String savedType; // migrated 2026-02-25 to PaymentController
    private static final boolean MIGRATION_PLAYER = true;
    
    
    
    
    
    public CourseController(){  // constructor
        this.listavg = null;
//        this.setSunRiseSet(null);
    }

 @PostConstruct
 public void init(){ // attention !! ne peut absolument pas avoir : throws SQLException
  //   public String init(){ // attention !! ne peut absolument pas avoir : throws SQLException
  try{
          LOG.debug("Entering CourseController @PostConstruct");
          LOG.debug("we start a reset for all the working fields of the new session "); // pas déjà fait ??c'est bien utile ?
       reset("from init in CourseController");  // 
   //// enlevé 12-02-2026    player = new Player();
   //LOG.debug("player instanciated in CourseController");
   /// enlevé 09-02-2026player = new Player();
    /* Initialise le player uniquement si null dans playerController   */
    
    /* à vérifier !!
    if (appContext.getPlayer() == null) {
        LOG.debug("à vérifier ici");
        Player player = new Player();
        playerC.setPlayer(getPlayer());
        LOG.debug("Initialized new Player in playerController: {}", player);
    } else {
        LOG.debug("Player already initialized in playerController: {}"); //, appContext.getPlayer()); // est null
    }
    */
        //  LOG.debug("playerController.setPlayer(null)");
  //    ???    LOG.debug("entering init without org.wilfly.plugin !" );
// 1. WITHOUT connection pool — removed 2026-02-26 (CDI migration — all services now use @Resource DataSource)
// 2. WITH Datasource and connection pool — removed 2026-02-26
        LOG.debug("CDI migration complete — Connection/DBConnection removed from CourseController");
// 3. Settings
     //  LOG.debug("** Webbrowser url = " + utils.LCUtil.firstPartUrl());
// 4. loading 
    init2();  // déplacé 14-02-2026 pour visibilité
    // ✅ Si appelé depuis login, ne pas rediriger
      //  if ("from login".equals(ini)) {
      //      LOG.debug("Reste sur login.xhtml for " + ini);
       //     return null; // reste sur login.xhtml
      //  }
        
        // ✅ Sinon, rediriger vers welcome
        LOG.debug("va vers welcome.xhtml ");
     //   return "welcome.xhtml?faces-redirect=true";
            
 }catch (Exception e){
            String msg = "££ Exception in creating Connection or init in courseC = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
          //  return null;
     }
  } //end method init
 
 public void init2(){ 
  try{
      availableQualifying = new LinkedHashMap<>();
    availableQualifying.put("Non Qualifying", "N");
    availableQualifying.put("Qualifying", "Y");
 //   availableQualifying.put("Counting", "C");
    
 //   availableHoles = new LinkedHashMap<>();
 //   availableHoles.put("18 Holes", "01-18");
 //   availableHoles.put("Holes 1-9", "01-09");
 //   availableHoles.put("Holes 10-18", "10-18");
  
    seasons = new String[2];
    seasons[0]="2013-2014";
    seasons[1]="2012-2013";
    seasonOptions = createFilterOptions(seasons); // new 01/12/2013

        setNextStep(false);
        setNextInscription(false);
        setNextScorecard(false);
        setNextPlayer(false);
        setConnected(false); // used for Logout button in header.xhtml, in selectPlayer
  //      setZwanzeur(false); //16/11/2013 supprimé 
        setShowButtonCreateScore(true);
 //       setShowButtonCreateStatistics(false);
  ///  LOG.debug("we start a reset for all the working fields of the new session "); // c'est bien utile ?
  /// reset("from init in CourseController");
 //  LOG.debug("leaving " + this.getClass().getSimpleName() + " Postconstruct init()");
   LOG.debug("NEW session just started !! " + NEW_LINE);
   LOG.debug("System property file encoding = " + System.getProperty("file.encoding") );
   LOG.debug("System property native encoding = " + System.getProperty("native.encoding") );
   LOG.debug("Charset.defaultCharset(): " + Charset.defaultCharset());
   LOG.debug("file.encoding property:" + System.getProperty("file.encoding"));
   LOG.debug("native.encoding property:" + System.getProperty("native.encoding"));
 //  LOG.debug("sun.jnu.encoding property:" + System.getProperty("sun.jnu.encoding"));
   LOG.debug("stdout.encoding property:" + System.getProperty("stdout.encoding"));
   LOG.debug("session timeout (from web.xml) = " + externalContext.getSessionMaxInactiveInterval() ); // in seconds
  // LOG.debug("sun.stdout.encoding property:" + System.getProperty("sun.stdout.encoding"));
 //  System.setOut(new PrintStream(System.out, true, "UTF8"));
   System.setOut(new PrintStream(System.out, true, "UTF8"));
   out.println("1=héhé hàhà");
    PrintStream stream = new PrintStream(System.out, true, "UTF8");
// new 19-03-2023 pom.xml :  <exec.inheritIo>true</exec.inheritIo>  <!--  mod 19-03-2023  https://github.com/apache/netbeans/issues/5552 
//Support ANSI colours when printing to output window
//https://www.w3schools.blog/ansi-colors-java
        stream.println("2=±héhé hàhà");
  //  String ANSI_RESET = "\u001B[0m";
  //  String ANSI_RED = "\u001B[31m";
  //  String RED_BOLD = "\u001B[1;31m"; // 1; = bold 
  //  String RED_BOLD_UNDERLINED = "\u001B[1;4;31m"; // 1; = bold 4;=underline
  //  String RED_BACKGROUND = "\u001B[41m"; // [4 au lieu de [3
    out.println(RED + "This text should be red!" + RESET);
    out.println(RED_BOLD + "This text should be red bold !" + RESET);
    out.println(RED_BACKGROUND + "This text should be red background !" + RESET);
    out.println(BLUE_UNDERLINED + "This text should be blue underlined !" + RESET);  // not correctCYAN_BACKGROUND_BRIGHT
    out.println(CYAN_BACKGROUND_BRIGHT + "This text should be cyan backgrouf bright !" + RESET);
    out.println(String.format("%sThis is green text%s", GREEN, RESET));
    
   LOG.debug("Here are the ManifestAttributes - not printed at the moment " + NEW_LINE); 
   LOG.debug("windows-1252 = pas d'accents Charset JVM = {}", java.nio.charset.Charset.defaultCharset());
   //  enleve trop verbose  utils.LCUtil.printManifestAttributes();
    LOG.debug("end of the method @Postconstruct init " + NEW_LINE);
    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        LOG.debug("rootPath = " + rootPath);
    String appConfigPath = rootPath + "app.properties";
        LOG.debug("appConfigPath = " + appConfigPath);
    String catalogConfigPath = rootPath + "catalog";
        LOG.debug("catalogConfigPath = " + catalogConfigPath);
    LOG.debug("Here are the MetaData ! (not printed at the moment)" + NEW_LINE);
  //  DBMeta.listMetaData(conn);
    LOG.debug("classLoader of this class : " + CourseController.class.getClassLoader());
    LOG.debug("DriverManager of this class : " + DriverManager.class.getClassLoader());

    // enlevé 14-02-023206 utils.WildFlyEnvironmentInfo.report(); // donne print
     //   LOG.debug("utils.WildFlyEnvironmentInfo.report() = " + report);      
  // alternative test version CdiAuditScanner d'avor via main !

  }catch (Exception e){
            String msg = "££ Exception in init2 = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
     }
  } 
 
 
    @PreDestroy
    public void exit() {
        LOG.debug(" ------------------ from CourseController PreDestroy exit ()...");
    } 
 // new 07-02-2026
private void logLegacyAccess(String method) {
    String viewId = "no-view";
    try {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getViewRoot() != null) {
            viewId = fc.getViewRoot().getViewId();
        }
    } catch (Exception e) {
        // ignore any issue accessing FacesContext
    }
  
    LOG.warn("[MIGRATION] Legacy access detected - Method: {} | View: {}", method, viewId);
}

 
 //new 02/02/2026 utilisé dans selectClubCourse.xhtml
public void resetCourseSelection() {
    // Réinitialiser le parcours quand le club change

    LOG.debug("=== RESET COURSE CALLED ===");
    Club club = appContext.getClub(); // migrated 2026-02-25
    LOG.debug("Club ID: " + (club != null ? club.getIdclub() : "null"));
    Course course = appContext.getCourse(); // migrated 2026-02-25
    if (course != null) {
        course.setIdcourse(0);
        course.setCourseName("");
    }

    // Log pour debug
    LOG.debug("Club changed, course reset. New Club ID: " +
        (club != null ? club.getIdclub() : "null"));
}

  //  public Player getPlayerTemp() {
  //      return playerTemp;
  //  }

   // public void setPlayerTemp(Player playerTemp) {
   //     this.playerTemp = playerTemp;
   // }


/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
    public String getSavedType() { ... }
    public void setSavedType(String savedType) { ... }
*/

    public String getLineModelCourse() {
      //  LOG.debug("getLineModelCourse reached");
        return lineModelCourse;
    }

    public void setLineModelCourse(String lineModelCourse) {
      //  LOG.debug("setLineModelCourse reached");
        this.lineModelCourse = lineModelCourse;
    }

 
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public void qualifyingListener(ValueChangeEvent e) { ... }
*/
  
/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public Lesson getSelectedLesson() { ... }
   public void setSelectedLesson(Lesson selectedLesson) { ... }
   public void deleteLesson() { ... }
*/
    
    public List<ECourseList> getListStableford() {
        return listStableford;
    }

    public Player getPlayerPro() {
        return playerPro;
    }

    public void setPlayerPro(Player playerPro) {
        this.playerPro = playerPro;
    }

    public List<Club> getFilteredClubs() {
        return filteredClubs;
    }

    public void setFilteredClubs(List<Club> filteredClubs) {
        this.filteredClubs = filteredClubs;
    }

    public void setListStableford(List<ECourseList> listStableford) {
        this.listStableford = listStableford;
    }

    public ScoreStableford getScoreStableford() {
        return scoreStableford;
    }

    public void setScoreStableford(ScoreStableford scoreStableford) {
        this.scoreStableford = scoreStableford;
    }

// ✅ MIGRÉ vers ClubController (clubC) - completeCountry devrait aller vers countrycontroller
/*
public List<Country> completeCountry(String query) {  
    LOG.debug("entering CourseController completeCountry with query = " + query);// selection pour une lettre
       String queryLowerCase = query.toLowerCase();
      //  LOG.debug("countryService = " + countryService);
           if(countryService == null){
                countryService = new CountryService();
               LOG.debug("new countryService = " + countryService);
          }
         List<Country> countries = countryService.getCountries();
 //          LOG.debug("countries found = " + countries.toString());
        return countries.stream()
                .filter(t -> t.getName()
                        .toLowerCase()
                        .contains(queryLowerCase))
                        .collect(Collectors.toList());
    }
*/ // end ✅ MIGRÉ - completeCountry



// ✅ MIGRÉ vers ClubController (clubC) - onCountrySelect
/*
  public void onCountrySelect(SelectEvent<String> event) {  // for club.xhtml and NOT for player.xhtml  !!  mod 21.02.2024 pour récupérer code country
      try{
          LOG.debug("entering on CountrySelect");
        String countrySelected = event.getObject();
        String msg = "country selected = " + countrySelected;
        LOG.debug(msg);
        showMessageInfo(msg);
        club.getAddress().getCountry().setCode(countrySelected); // new 21.02.2024
      // et pour player ??
  //    if (player.getAddress().getCountry().getCode() == null){ // new 15-10-2024
  //        LOG.debug("code for player is null, code  = " );
  //        player.getAddress().getCountry().setName(countrySelected);
  //    }
           LOG.debug("club address is now = " + club.getAddress());
        //   LOG.debug("player address is now = " + player.getAddress());
      }catch (Exception e){
            String msg = "Error in onCountrySelect= " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
         } 
     } // end method
*/ // end ✅ MIGRÉ - onCountrySelect

  
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

private SelectItem[] createFilterOptions(String[] data) {
        SelectItem[] options = new SelectItem[data.length + 1];
        options[0] = new SelectItem("", "Select All Games");
        for(int i = 0; i < data.length; i++){
            options[i + 1] = new SelectItem(data[i], data[i]);
        }
     return options;
    }
/* transféré 20-02-2026 vers TechnicalController techC
    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(List<FilterMeta> filterBy) {
        this.filterBy = filterBy;
    }
*/
    public List<Course> getCourseListForClub() {
        return courseListForClub;
    }

    public void setCourseListForClub(List<Course> courseListForClub) {
        this.courseListForClub = courseListForClub;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

 //   public Map<String, String> getAvailableHoles() {
 //       return availableHoles;
 //   }


public Map<Integer, String> getData() {
        LOG.debug("called MapBean.getData");
        data.put(1, "java ee 8");
        data.put(2, "jsf 2.3");
        return data;
    }

    public Activation getActivation() {
        return activation;
    }

    public Password getPassword() {
        return password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    /** Bridge → appContext.getCompetition() — Phase 3A migration 2026-02-25 */
    public ECompetition getCompetition() {
        return appContext.getCompetition();
    }

    /** Bridge → appContext.setCompetition() — Phase 3A migration 2026-02-25 */
    public void setCompetition(ECompetition competition) {
        appContext.setCompetition(competition);
    }

    public void setActivation(Activation activation) {
        
        this.activation = activation;
        LOG.debug("activation setted to " + getActivation());
    }

    public TarifMember getTarifMember() {
        return tarifMember;
    }

    public void setTarifMember(TarifMember member) {
        this.tarifMember = member;
    }

 //   public Professional getProfessional() {
 //       return professional;
 //   }

 //   public void setProfessional(Professional professional) {
 //       this.professional = professional;
 //   }

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
    public Map<String, String> getAvailableQualifying() { ... }
    public List<String> getGameList() { ... }
*/
/*
    public List<String> getParList() {
        return parList;
    }

    public void setParList(List<String> parList) {
        this.parList = parList;
    }
*/
    public Greenfee getGreenfee() {
        return greenfee;
    }

    
    public void setGreenfee(Greenfee greenfee) {
        this.greenfee = greenfee;
    }

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
    public Blocking getBlocking() { ... }
    public void setBlocking(Blocking blocking) { ... }
*/

 //   public Player getPlayer2() {
 //       return player2;
 //   }

  //  public void setPlayer2(Player player2) {
  //      this.player2 = player2;
   // }

  //  public Player getLocalAdmin() {
  //      return localAdmin;
  //  }

  //  public void setLocalAdmin(Player localAdmin) {
  //      this.localAdmin = localAdmin;
  //  }
    //public LoginBeanSecurity getLogin() {
    //    return login;
   // }

   // public void setLogin(LoginBeanSecurity login) {
   //     this.login = login;
   // }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
    
    public US[] getStates () {
        return US.values();
    }
    
    public enum US {
        ALABAMA("Alabama", "AL"),
        ALASKA("Alaska", "AK"),
        ARIZONA("Arizona", "AZ");
        String unabbreviated;
        String abbreviation;
        US(String unabbreviated, String abbreviation) {
            this.unabbreviated = unabbreviated;
            this.abbreviation = abbreviation;
        }
    }

 //   public java.util.Date getNow() {
   //     return now;
   // }

    public List<Player> getLp() {
        return lp;
    }

    public void setLp(List<Player> lp) {
        this.lp = lp;
    }

public void validateMP4(FacesContext context, UIComponent toValidate, Object value)
      throws ValidatorException
{
 try{
  //  LOG.debug("entering validateMP4");
    LOG.debug("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.debug("entering validateMP4 - toValidate Id = " + toValidate.getId() );
   // LOG.debug("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.debug("entering validateMP4 - value = " + value.toString());
  //  LOG.debug("UIcomponent, getFamily = " + toValidate.getFamily());
  //  LOG.debug("UIcomponent, context = " + context.toString());
  //  LOG.debug("UIcomponent,message = " + toValidate.getClientId(context)); 
    String confirm = (String)value;
    

if ((context == null) || (toValidate == null)) 
{ 
     LOG.debug("UIcomponent,null context or toValidate = "); 
    throw new NullPointerException(); 
} 
if (!(toValidate instanceof UIInput))
    { LOG.debug("UIcomponent,not instanceof UIInput = "); 
    return;
}
    String field1Id = (String) toValidate.getAttributes().get("scorePlayer11");
         LOG.debug(" validateMP4 - field1Id = " + field1Id);
         
 //   UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
 //        LOG.debug(" validateMP4 - passComponent = " + passComponent);
 //   String pass = (String) passComponent.getSubmittedValue();
 //       LOG.debug(" validateMP4 - pass1 = " + pass);
       String  pass = null;
    if (pass == null)
    {
 //       pass = (String) passComponent.getValue();
         LOG.debug(" validateMP4 - pass2 = " + pass);
    }
    
    if (!pass.equals(confirm)){
        LOG.debug(" validateMP4 - pass not equal confirm = " );
        String msg = toValidate.getClientId(context);
        utils.LCUtil.showMessageFatal(msg);
  //      String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
        throw new ValidatorException(new FacesMessage(msg));
    }
 }  //end try ValidatorException
 catch (ValidatorException ve) {
            String msg = "ValidatorException = " + ve.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
 } 
 catch (Exception npe) {
            String msg = "NullPointerException = " + npe.getMessage();
            LOG.error(msg);
            utils.LCUtil.showMessageFatal(msg);
 } 
}

    public TarifGreenfee getTarifGreenfee() {
        return tarifGreenfee;
    }

    public void setTarifGreenfee(TarifGreenfee tarifGreenfee) {
        this.tarifGreenfee = tarifGreenfee;
    }

     public String getParArrayString() throws SQLException {
    //     load.LoadParArray lpa = ;
         parArray =  readParArray.read(appContext.getPlayer(), appContext.getCourse());  // migrated 2026-02-25
 //           LOG.debug("parArray  = " + parArray);
     parArrayString = Arrays.toString(parArray); // it will return String like [1, 2, 3, 4, 5]
 //    LOG.debug("parArrayString = " + parArrayString);
        String strSeparator = "|";
        // //you can use replaceAll method to replace brackets and commas
        parArrayString = parArrayString.replaceAll(", ", strSeparator).replace("[", "").replace("]", "");
//          LOG.debug("String strNumbers2 = " + parArrayString);
        
        return parArrayString;
    }

    public Map<String, String> getMembers() {
            LOG.debug("getMembers returned = " + members);
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

    public boolean isNextPanelPlayer() {
        return NextPanelPlayer;
    }

    public void setNextPanelPlayer(boolean NextPanelPlayer) {
        this.NextPanelPlayer = NextPanelPlayer;
    }

    // getHolesGlobal() / setHolesGlobal() — removed 2026-02-25 (migrated to ClubController)

    public int[] getParArray() {
        return parArray;
    }

    public void setParArrayString(String parArrayString) {
        this.parArrayString = parArrayString;
    }

       public void setParArray(int[] parArray) {
           CourseController.parArray = parArray;
      }

   public Inscription getInscription() {
      return inscription;
    }

    public void setInscription(Inscription inscription) {
       this.inscription = inscription;
   }

    public EUnavailable getUnavailable() {
        return appContext.getUnavailable();
    }

    public void setUnavailable(EUnavailable unavailable) {
        this.unavailable = unavailable;
        appContext.setUnavailable(unavailable); // sync appContext — migrated 2026-02-25
    }

 //   public Inscription getInscriptionNew() {
 //       return inscriptionNew;
 //   }

 //   public void setInscriptionNew(Inscription inscriptionNew) {
 //       this.inscriptionNew = inscriptionNew;
 //   }

    public  Flight getFlight() {
        return flight;
    }

 /* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public void selectFlightFromDialog(Flight flight) throws IOException { ... }
 */

 //   public Car getCar() {
 //       return car;
  //  }

    public String getIntroductionTxt() {
        return introductionTxt;
    }

    public void setIntroductionTxt(String introductionTxt) {
        this.introductionTxt = introductionTxt;
    }

 //   public void setCar(Car car) {
 //       this.car = car;
  //  }

 //   public PlayerHasRound getPlayerhasround() {
 //       return inscription;
 //   }

 //   public void setPlayerhasround(PlayerHasRound inscription) {
 //       this.inscription = inscription;
 //   }

    
public boolean isShowButtonCreateCourse() {
        return ShowButtonCreateCourse;
    }

    public void setShowButtonCreateCourse(boolean ShowButtonCreateCourse) {
        this.ShowButtonCreateCourse = ShowButtonCreateCourse;
    }

    public String getSunRiseSet() {
        return SunRiseSet;
    }

    public List<Player> getFullList() {
        return fullList;
    }

    public void setFullList(List<Player> fullList) {
        this.fullList = fullList;
    }

    public void setSunRiseSet(String SunRiseSet) {
        this.SunRiseSet = SunRiseSet;
    }

    public SelectItem[] getGameOptions() {
        return gameOptions;
    }

    public void setGameOptions(SelectItem[] gamesOptions) {
        this.gameOptions = gamesOptions;
    }

    public SelectItem[] getSeasonOptions()
    {
        LOG.debug(" passing thru getSeasonOptions");
        return seasonOptions;
    }

    public void setSeasonOptions(SelectItem[] seasonOptions)
    {   LOG.debug(" passing thru SetSeasonOptions");
        this.seasonOptions = seasonOptions;
    }

    public String getHtmlBr() {
        return htmlBr;
    }

    public String getHtmlH1() {
        return htmlH1;
    }

    public String getInputClub() {
        LOG.debug("getInput = " + inputClub);
        return inputClub;
    }

    public String getInputParticipants() {
        return inputParticipants;
    }

    public void setInputParticipants(String inputParticipants) {
        this.inputParticipants = inputParticipants;
        LOG.debug("setInputParticipants (new round !) = " + inputParticipants);
        if (inputParticipants.equals("ini") ){
            LOG.debug(" -- Coursecontroller/listcourse, filteredCars  set to null ! ");
            // was: lists.PlayersList.setListe(null);  //lazy loading forced !!
            playersList.invalidateCache();              // migrated 2026-02-24
            filteredCars = null; // new 15/12/2013
        }
    }

    public Professional getSelectedPro() {
        return selectedPro;
    }

    public void setSelectedPro(Professional selectedPro) {
        this.selectedPro = selectedPro;
    }

//    public List<Professional> getListProfessional() {
//        LOG.debug("listprofessional = " + listProfessional);
//       return listProfessional;
//   }

 //   public void setListProfessional(List<Professional> listProfessional) {
 //       this.listProfessional = listProfessional;
 //   }
    
    public String getInputInscription() {
        return inputInscription;
    }

    public void setInputInscription(String inputInscription) {
        this.inputInscription = inputInscription;
        LOG.debug("setInputInscription (new round !) = " + inputInscription);
        if (inputInscription.equals("ini") )
        {  LOG.debug(" -- Coursecontroller/listInscription, filteredCars  set to null ! ");
           // lists.InscriptionList.setListe(null);  //lazy loading forced !!
            inscriptionList.invalidateCache(); // mod 23-02-2026
            filteredCars = null;
        }
    } // end method

    public String getInputHandicap() {
        return inputHandicap;
    }

    public void setInputHandicap(String inputHandicap) {
        this.inputHandicap = inputHandicap;
        LOG.debug("setInputHandicap (new round !) = " + inputHandicap);
        if (inputHandicap.equals("ini")){
            LOG.debug(" -- Coursecontroller/listhandicap, filteredCars  set to null ! ");
            // was: lists.HandicapList.setListe(null);  //lazy loading forced !!
            handicapList.invalidateCache();             // migrated 2026-02-24
            filteredCars = null; // new 15/12/2013
        }
    }

    public void initInputPlayingHcp(int inputPlayingHcp){
        this.inputPlayingHcp = inputPlayingHcp;
        LOG.debug("setInputPlayingHcp (new calcul !) = " + inputPlayingHcp);
   //     if (inputPlayingHcp.equals("ini") )
   //     {  
        entite.PlayingHandicap ph = new entite.PlayingHandicap();
            ph.setPlayingHandicap(this.inputPlayingHcp);  // reset to zero
            LOG.debug(" -- Coursecontroller/Playinghandicap set to null ! ");
    }    
    
    
    public String getInputPlayedRounds() {
        return inputPlayedRounds;
    }
/* enlevé 22-02-026 encore utilisé ?
    public void setInputPlayedRounds(String inputPlayedRounds)    {
        this.inputPlayedRounds = inputPlayedRounds;
            LOG.debug("setInput (new played list) = " + inputPlayedRounds);
        if (inputPlayedRounds.equals("ini")) {
            filteredCars = null; // new 15/12/2013
        }
    }
*/
    public String getInputClubOperation() {
        return inputClubOperation;
    }

    public void setInputClubOperation(String inputClubOperation) {
        this.inputClubOperation = inputClubOperation;
    }

    public String getInputCourseOperation() {
        return inputCourseOperation;
    }

    public void setInputCourseOperation(String inputCourseOperation) {
        this.inputCourseOperation = inputCourseOperation;
    }

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public void setInputClub(String inputClub) { ... }
*/

    public List<Average> getListavg() {
        return listavg;
    }

    public void setListavg(List<Average> listavg) {
        this.listavg = listavg;
    }

    public String getInputCourse() {
        return inputCourse;
    }

    public void setInputCourse(String inputCourse) {
        LOG.debug("setInput (new course !) = " + inputCourse);
        this.inputCourse = inputCourse;
        if (inputCourse.equals("ini")) {
            appContext.setCourse(new Course());
            // tee = new Tee(); — removed 2026-02-25 (migrated to ClubController)
            // hole = new Hole(); — removed 2026-02-25
        }
    }

    public String getInputScorecard() {
        return inputScorecard;
    }

    public void setInputScorecard(String inputScorecard){
        LOG.debug("setInput (new scorecard !) = " + inputScorecard);
        this.inputScorecard = inputScorecard;
        if (inputScorecard.equals("ini")) {
            LOG.debug(" -- ScoreCard3List.setListe to null ");
            scoreCardList3.invalidateCache();      // migrated 2026-02-24
        }
    }

    public String getInputScore() {
        return inputScore;
    }
    public String getInputTeeStart() {
        return inputScore;
    }
    
// ✅ MIGRÉ vers PlayerController (playerC) - LastSession → lastSession — 2026-02-25
/*
  public String LastSession(int idplayer) throws SQLException, Exception {
     Audit audit = new find.FindLastLogin().find(appContext.getPlayer(), conn);
     if(audit != null){
         return audit.getAuditStartDate().format(ZDF_TIME_HHmm);
     }else{
         return("First Connection for this player");
     }
 } // end method
*/ // end ✅ MIGRÉ - LastSession

    public int getDeletePlayer() {
        return deletePlayer;
    }

    public void setDeletePlayer(int deletePlayer) {
        this.deletePlayer = deletePlayer;
    }

    public boolean isNextStep() {
        return NextStep;
    }

    public void setNextStep(boolean NextStep) {
        this.NextStep = NextStep;
    }

    public boolean isNextInscription() {
        return NextInscription;
    }

    public void setNextInscription(boolean NextInscription) {
        this.NextInscription = NextInscription;
    }

    public ScoreScramble getScoreScramble() {
        return scoreScramble;
    }

    public void setScoreScramble(ScoreScramble scoreScramble) {
        this.scoreScramble = scoreScramble;
    }

    public boolean isNextScorecard() {
        return NextScorecard;
    }

    public void setNextScorecard(boolean NextScorecard) {
        this.NextScorecard = NextScorecard;
    }

    public boolean isConnected() {
        return appContext.isConnected(); // migrated 2026-02-25 — delegates to appContext
    }

    public void setConnected(boolean Connected) {
        this.Connected = Connected;
        appContext.setConnected(Connected); // migrated 2026-02-25 — sync with appContext
    }

  //  public TreeBeanController getTreeBean() {
 //       return treeBean;
 //   }

 //   public void setTreeBean(TreeBeanController treeBean) {
 //       this.treeBean = treeBean;
 //   }

//    public EScoreCardList getScorecard() {
//        return scorecard;
//    }

    public boolean isShowButtonCreateScore() {
        return ShowButtonCreateScore;
    }

    public void setShowButtonCreateScore(boolean ShowButtonCreateScore) {
        this.ShowButtonCreateScore = ShowButtonCreateScore;
     //   LOG.debug("ShowButtonCreateScore setted to : " + ShowButtonCreateScore);
    }

    public static void setButtonCreditCard(boolean b) {
        ShowButtonCreditCard = b;
    }
    public String getOtherGame() {
        return otherGame;
    }

    public void setOtherGame(String otherGame)
    {
        CourseController.otherGame = otherGame;
        LOG.debug("otherGame setted to : " + CourseController.otherGame);
    }

    public ArrayList<Flight> getFilteredFlights() {
        return filteredFlights;
    }

    public void setFilteredFlights(ArrayList<Flight> filteredFlights) {
        this.filteredFlights = filteredFlights;
    }

    public List<?> getFilteredInscriptions() {
        return filteredInscriptions;
    }

    public void setFilteredInscriptions(List<?> filteredInscriptions) {
        this.filteredInscriptions = filteredInscriptions;
    }

    public List<?> getFilteredCourses() {
        return filteredCourses;
    }

    public void setFilteredCourses(List<ECourseList> filteredCourses) {
        this.filteredCourses = filteredCourses;
    }

 //   public List<ECourseList> getFilteredPlayedRounds() {
 //       return filteredPlayedRounds;
 //   }

  //  public void setFilteredPlayedRounds(List<ECourseList> filteredPlayedRounds) {
  //      this.filteredPlayedRounds = filteredPlayedRounds;
  //  }

    

    public List<?> getFilteredCars() {
     //   LOG.debug("from getFilteredCars = " + filteredCars);
        return filteredCars;
    }



    public List<EPlayerPassword> getFilteredPlayers() {
        LOG.debug("getfilteredPlayers " + filteredPlayers.toString());
        return filteredPlayers;
    }

    public void setFilteredPlayers(List <EPlayerPassword>filteredPlayers) {
        LOG.debug("getfilteredPlayers " + filteredPlayers.toString());
        this.filteredPlayers = filteredPlayers;
    }

 //   public HandicapIndex getHandicapIndex() {
 //       return handicapIndex;
 //   }

 //   public void setHandicapIndex(HandicapIndex handicapIndex) {
 //       this.handicapIndex = handicapIndex;
  //  }

    public String getInputcmdParticipants() {
        return inputcmdParticipants;
    }

    public void setInputcmdParticipants(String inputcmdParticipants) {
        this.inputcmdParticipants = inputcmdParticipants;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }
/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
 public EPlayerPassword getSelectedPlayerEPP() { ... }
 public void setSelectedPlayerEPP(EPlayerPassword selectedPlayerEPP) { ... }
*/

    public void setSelectedPlayer(Player selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }
// new 03/09/2014
    public DualListModel<Player> getDlPlayers() {
        return dlPlayers;
    }

    public void setDlPlayers(DualListModel<Player> dlPlayers) {
        this.dlPlayers = dlPlayers;
    }

    public List<Player> getSelectedPlayersMatchPlay() {
        LOG.debug("getSelectedPlayersMatchPlay = " + selectedPlayersMatchPlay );
        if(selectedPlayersMatchPlay != null) {
 //           String msg = "#Players in Match Play = " + String.valueOf(selectedPlayersMatchPlay.size());
        }
        return selectedPlayersMatchPlay;
    }

    public void setSelectedPlayersMatchPlay(List<Player> selectedPlayersMatchPlay) {
        LOG.debug("setSelectedPlayersMatchPlay = " +  Arrays.toString(selectedPlayersMatchPlay.toArray()));
   //      LOG.debug("listsc3 after while = " + Arrays.toString(listsc3.toArray() ) );
        this.selectedPlayersMatchPlay = selectedPlayersMatchPlay;
        
    }

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   getSelectedPlayedRound/setSelectedPlayedRound — delegates to appContext
    public ECourseList getSelectedPlayedRound() { ... }
    public void setSelectedPlayedRound(ECourseList selectedPlayedRound) { ... }
*/

    public void setFilteredCars(List<?> filteredCars) {
        LOG.debug("from setFilteredCars = " + filteredCars);
        this.filteredCars = filteredCars;
    }

    public Matchplay getMatchplay() {
        return matchplay;
    }

    public void setMatchplay(Matchplay matchplay) {
        this.matchplay = matchplay;
    }

    public boolean isNextPlayer() {
        return appContext.isNextPlayer(); // migrated 2026-02-25 — delegates to appContext
    }

    public void setNextPlayer(boolean NextPlayer) {
        this.NextPlayer = NextPlayer;
        appContext.setNextPlayer(NextPlayer); // migrated 2026-02-25 — sync with appContext
    }

 //   public Player getPlayer() {
 //       return player;
 //   }
// new 07-02-2026
    public Player getPlayer() {
    if (MIGRATION_PLAYER) {   //    private static final boolean MIGRATION_PLAYER = true;
        logLegacyAccess("getPlayer()");
    }
    LOG.debug("getPlayer ");
    return appContext.getPlayer();  // magic here !!
}
  //  public void setPlayer(Player player) {
  //      this.player = player;
  //  }

//public void setPlayer(Player player) {
//    this.player = player;
//    playerController.setPlayer(player); // new 09-02-2026
//}
    
    
// Le Player est maintenant délégué à PlayerController
 //   public Player getPlayer() {
 //       return playerController.getPlayer();
 //   }

 //   public void setPlayer(Player player) {
  //      playerC.setPlayer(player);
  //  }




    
    
    public Handicap getHandicap() {
        return handicap;
    }

    public void setHandicap(Handicap handicap) {
        this.handicap = handicap;
    }

    public PlayingHandicap getPlayingHcp() {
        return playingHcp;
    }

    public void setPlayingHcp(PlayingHandicap playingHcp) {
        this.playingHcp = playingHcp;
    }

    public Round getRound() {
        return round;
    }

 //   public void setRound(Round round) {
 //       this.round = round;
 //   }
    
    // modifié 20-02-2026 pour listFlights
    public void setRound(Round round) {
    this.round = round;
    appContext.setRound(round);   // ✅ synchronise
}

    public Course getCourse() {
        return appContext.getCourse(); // migrated 2026-02-25 — delegates to appContext
    }

    public void setCourse(Course course) {
        appContext.setCourse(course); // migrated 2026-02-25 — delegates to appContext
    }

    public Club getClub() {
        return appContext.getClub(); // migrated 2026-02-25 — delegates to appContext
    }

    public void setClub(Club club) {
        appContext.setClub(club); // migrated 2026-02-25 — delegates to appContext
    }

    // getTee() — removed 2026-02-25 (migrated to ClubController)

    public Cotisation getCotisation() {
        return cotisation;
    }

    public void setCotisation(Cotisation cotisation) {
        this.cotisation = cotisation;
    }

    // setTee() — removed 2026-02-25 (migrated to ClubController)

    // getHole() / setHole() — removed 2026-02-25 (migrated to ClubController)

    public ScoreMatchplay getScoreMatchplay() {
        return scoreMatchplay;
    }

    public void setScoreMatchplay(ScoreMatchplay scoreMatchplay) {
        this.scoreMatchplay = scoreMatchplay;
    }
   public Overlay<Object> getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay<Object> overlay) {
        this.overlay = overlay;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getRadioButtonJSF() {
        return radioButtonJSF;
    }

    public void setRadioButtonJSF(String radioButtonJSF) {
        this.radioButtonJSF = radioButtonJSF;
    }

    // public Connection getConn() { return conn; } // removed 2026-02-26 — CDI migration

    public int louis() {
        return appContext.getCourse().getIdcourse(); // migrated 2026-02-25
    }

public static String fileUpload() throws SQLException{
     return "player_upload_file.xhtml?faces-redirect=true";
}

// http://adfpractice-fedor.blogspot.be/2012/02/understanding-immediate-attribute.html
// http://balusc.omnifaces.org/2006/09/debug-jsf-lifecycle.html
 // new 25/03/2017
/*
public String findClubCoordinates() { //used in club.xhtml
      LOG.debug("entering findClubCoordinates " );
   try{
       club = new find.FindCoordinates().clubCoordinates(club);
          LOG.debug("club with coordinates integrated = " + club);
       return null;// return "club.xhtml?faces-redirect=true";
  }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubCoordinates " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
} // end method
*/
// new 31-01-2026
/*/private final service.CoordinatesService coordinatesService = new service.CoordinatesService();

    public String findClubCoordinates() {
        try {
            club = new service.CoordinatesService().updateCoordinates(club);
            showMessageInfo("Coordinates updated for club " + club.getClubName());
        } catch (Exception e) {
            showMessageFatal("Error updating club coordinates: " + e.getMessage());
        }
        return null;
    }
*/


    public String findClubCoordinates() {
        try {
            appContext.setClub(coordinatesService.updateCoordinates(appContext.getClub())); // migrated 2026-02-25
            showMessageInfo("Coordinates found for club " + appContext.getClub().getClubName());
        } catch (Exception e) {
            showMessageFatal("Error find club coordinates: " + e.getMessage());
        }
        return null;
    }


 /* mod 18-02-2024
 public String findPlayerCoordinates(){ // used in player.xhtml
 try{
    player = new find.FindCoordinates().playerCoordinates(player);
    return null; //   affiche player.xhtml
  }catch (Exception ex){
            String msg = "Exception in findPlayerCoordinates " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method
*/
/* vers clubcontroller 20-02-026
public String findClubWebsite(){ //used in player.xhtml
       LOG.debug("entering findClubWebsite " );
 try{ 
                LOG.debug("club Website = " + club.getClubWebsite() );  // a été complété par clubWebsiteListener, 
            if(club.getClubWebsite() == null){
                club.setClubWebsite("Website must be completed !");
                return null;
            }
            externalContext.redirect("http://" + club.getClubWebsite());  // https ???
            return null;
  }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubWebsite = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
} // end method
*/
public String processAction(){
   //  http://memorynotfound.com/jsf-how-to-find-component-programatically/
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        UIComponent component = view.findComponent("uf:name"); // form:field
        component.getAttributes().put("value", "John");
        return null;
    }

// ✅ MIGRÉ vers PlayerController (playerC) - playerPasswordListener — 2026-02-25
/*
public void playerPasswordListener(ValueChangeEvent e) {
        password.setCurrentPassword(e.getNewValue().toString() );
}

public void playerConfirmPasswordListener(ValueChangeEvent e) {
           password.setWrkconfirmpassword(e.getNewValue().toString() );
}
*/ // end ✅ MIGRÉ - playerPasswordListener + playerConfirmPasswordListener

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public void creditCardNumberListener(ValueChangeEvent e) { ... }
   public void creditCardTypeListener(ValueChangeEvent e) { ... }
*/
/*
public void roundWorkDate(ValueChangeEvent e) throws ParseException, SQLException{
    try{
        LOG.debug("entering roundWorkDate");
        LOG.debug("course is = " + course.toString());
        LOG.debug("roundDate format LocalDatetime = " + round.getRoundDate());
        cptFlight = 0;
//        LOG.debug("cptFlight = " + cptFlight);
      } catch (Exception ex) {
            String msg = "££ Exception in roundWorkDate = " + ex.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
        }      
   //  return "round.xhtml?faces-redirect=true";
} // end method
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public void roundWorkDate(ValueChangeEvent e) {
    cptFlight = 0;
    appContext.setRound(round);
} // end method
*/ // end ✅ MIGRÉ - roundWorkDate




/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
 public String otherPlayers() throws SQLException, Exception {
      return "inscriptions_other_players.xhtml?faces-redirect=true";
   } // end method
*/ // end ✅ MIGRÉ - otherPlayers
   
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String createOtherPlayers() throws SQLException {
        return "inscription.xhtml?faces-redirect=true";
    }
*/ // end ✅ MIGRÉ - createOtherPlayers
   
    public int getInputPlayingHcp() {
        return inputPlayingHcp;
    }

    public void setInputPlayingHcp(int inputPlayingHcp) {
        this.inputPlayingHcp = inputPlayingHcp;
    }

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
   public void findCourseListForClub() throws SQLException, Exception { ... }
*/
// ce code n'a pas de sens ?
// ✅ MIGRÉ vers ClubController (clubC) - selectorClubNextView — 2026-02-25
/*
  public String selectorClubNextView(){  // coming from selectClubDialog.xhtml
  try{
       LOG.debug("entering selectorClubNextView");
       LOG.debug("with inputSelectClub = " + sessionMap.get("inputSelectClub"));

    if(sessionMap.get("inputSelectClub").equals("PaymentCotisationSpontaneous")){
          LOG.debug("sessionmap is PaymentCotisationSpontaneous");
          LOG.debug("pour le club = " + club.getIdclub());
          LOG.debug("pour le round = " + round.getRoundDate());
        return "cotisation.xhtml?faces-redirect=true";
    }else{
        return null; // new
    }
  } catch (Exception ex) {
            String msg = "Exception in selectorClub() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
   } // end method
*/ // end ✅ MIGRÉ - selectorClubNextView
  


 //   public void fermerDialog() throws IOException {
 //       dialogController.closeDialog("dialogClub.xhtml");
 //   }
  
  //public String selectDialogPlayer(EPlayerPassword epp) {  // coming from dialogPlayer.xhtml
       public String selectedPlayerFromDialog(EPlayerPassword epp) {  // coming from dialogPlayer.xhtml
   try{
        LOG.debug(" entering selectedPlayerFromDialog with player = " + epp.getPlayer());
        LOG.debug(" entering selectedPlayerFromDialog with playerTemp = " + appContext.getPlayerTemp());
   //     LOG.debug("sessionMap : inputSelectClub = " + sessionMap.get("inputSelectClub"));
       //    LOG.debug("sessionMap : inputSelectCourse = " + sessionMap.get("inputSelectCourse"));  
        
        enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                                                     .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
         LOG.debug("withselectDialogPlayer with purpose = " + purpose);
      ///  if(sessionMap.get("inputSelectClub").equals("LocalAdministrator"))  {
        if (purpose == ClubSelectionPurpose.LOCAL_ADMIN) {
            LOG.debug("we handle LA");
        //    localAdmin = epp.player();
         //   playerTemp = epp.getPlayer(); // mod 18:44
            appContext.setPlayerTemp(epp.getPlayer()); // mod 19-02-2026
            // ou set localadmin ??
            
             //   LOG.debug("selected local administrator = " + localAdmin);
                LOG.debug("selected local administrator = " + appContext.getPlayerTemp());
                LOG.debug("select pour localadministrateur - closeDialog version CDI");
            dialogController.closeDialog(null);  // new 01/02/2026
                
                  // LOG.debug(" exiting selectLocalAdministrator with dialogController closed , b = " + b);
            return null; // local_administator.xhtml // stay on current view (dialog closed)
        }
            //   if(sessionMap.get("inputSelectClub").equals("CreatePro")){
        if (purpose == ClubSelectionPurpose.CREATE_PRO) {
            // localAdmin = epp.player();
        //    playerTemp = epp.getPlayer(); // super important sinon pas d'affichage dans 
            appContext.setPlayerTemp(epp.getPlayer());  // 12-02-2026
               professional.setProPlayerId(epp.player().getIdplayer()); // mod 05-02-2026
                   LOG.debug("professional with playerid updated = " + professional);
            dialogController.closeDialog(null);
         //          LOG.debug(" exiting selectLocalAdministrator with dialogController closed , b = " + b);
            return null; // professional.xhtml // stay on current view (dialog closed)
        }
     return null;
  } catch (Exception e) {
            String msg = "££ Exception selectDialogPlayer = " + e.getMessage() + " for player = " + appContext.getPlayer().getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
  }
} // end method

  /* migré vers clubcontroller 19-02-206
  public String selectedClubFromDialog(Club c) { // coming from 
        LOG.debug("Entering selectedClubFromDialog with param club = " + c);
        LOG.debug("original/old club value = " + club);
    club = c;
    course = new Course();
    // was: lists.CourseListForClub.setListe(null);  // si changement de club (2e essai)
    courseListForClubService.invalidateCache();  // migrated 2026-02-24
        LOG.debug("Exiting selectedClubFromDialog with club = " + club);
    dialogController.closeDialog(null);
    return null;
  }
  
  */
       /* migrée 20-02-2026
  public String selectedCourseFromDialog(Course c) { // coming from 
      LOG.debug("Entering selectedCourseFromDialog with param club = " + c);
      LOG.debug("original club value = " + club);
    course = c;
      LOG.debug("Exiting selectedCourseFromDialog with club = " + club);
      LOG.debug("Exiting selectedCourseFromDialog with course = " + course);
    dialogController.closeDialog(null);
      return null;
  }
  */
// ✅ MIGRÉ vers ClubController (clubC) - clubAndCourseAction — 2026-02-25
/*
  public String clubAndCourseAction(){  // coming from selectClubCourse.xhtml
      LOG.debug("Entering clubAndCourseAction");
          enumeration.ClubSelectionPurpose purpose = Optional.ofNullable(clubSelectionContext.getPurpose())
                                                     .orElse(enumeration.ClubSelectionPurpose.CREATE_PLAYER);
         LOG.debug("Entering clubAndCourseAction with purpose = " + purpose);
      return purpose.navigationToFinal();
  }
*/ // end ✅ MIGRÉ - clubAndCourseAction
  
/* migrated on 2026-02-25 — now in Controller.refact.ClubController
  public String selectedClub(Club c) { ... }
*/
 
  /*
 public String selectedClub(Club c) {   // coming from dialogClub.xhtml
  try{
      LOG.debug(" entering selectedClub with club c = " + c);
      LOG.debug(" entering selectedClub with club club = " + club);
        if(player == null) {
               LOG.debug(" current player is null - a examiner : pas normal !"); 
          //  player = new Player();
        }
             LOG.debug("sessionMap : inputSelectClub = " + sessionMap.get("inputSelectClub"));
        club = c; // IMPORTANT
            LOG.debug(" for club = " + club);

        if(sessionMap.get("inputSelectClub") == null){
               LOG.debug("inputSelectClub = null - cas create_player");
               LOG.debug(" current player initialized !"); // cas create player from player.xhtml    
            player.setPlayerHomeClub(club.getIdclub());
                LOG.debug("player = " + player);
       //     dialogController2.closeDialog("dialogClub.xhtml");  // new 15/01/2026  
   //         Controllers.DialogController.closeDialog("dialogClub.xhtml");
             dialogController.closeDialog(null);
            return null; // player.xhtml
         }

        if(sessionMap.get("inputSelectClub").equals("LocalAdministrator"))  {
            localAdmin.setPlayerHomeClub(club.getIdclub());
                 LOG.debug("local administrator with new dialogController HomeClub setted = " + localAdmin);
            dialogController2.closeDialog("dialogClub.xhtml");  // new 15/01/2026    
            LOG.debug("select club pour localadministrateur - closeDialog version CDI");
         //   Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
            return null; // local_administator.xhtml
        }
 // new 24-08-2025       
        if(sessionMap.get("inputSelectClub").equals("PaymentCotisationSpontaneous"))  {
          //  tarifMember.setTarifMemberIdClub(c.getIdclub());
          //       LOG.debug("tarifMember setted = " + tarifMember);
            LOG.debug("sessionmap is PaymentCotisationSpontaneous");
            LOG.debug("pour le club = " + club);
          // lancer findTarifCotisation pour le club
           round.setRoundDate(LocalDateTime.now());
              LOG.debug("pour le round = " + round);
        // tarifMember = new find.FindTarifMembersData().find(club, round, conn);
           tarifMember = findTarifMembersData.find(club, round); // migrated 2026-02-25
        //   dialogController2.closeDialog("dialogClub.xhtml");  // new 15/01/2026  
       ///    Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020/
            dialogController.closeDialog(null);
           return null; //"cotisation.xhtml?faces-redirect=true";
        }
 
        if(sessionMap.get("inputSelectClub").equals("CreatePro")){
            localAdmin.setPlayerHomeClub(club.getIdclub());
                 LOG.debug("local administrator with HomeClub setted = " + localAdmin);
       //     Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
             dialogController.closeDialog(null);
            return null; // professional.xhtml
        }  

        if(sessionMap.get("inputSelectClub").equals("MenuUnavailable")) {  // positionné from menu
              LOG.debug("selectHomeClub - handling menu unavailable");
              LOG.debug("for unavailable = " + unavailable);
        }

      player.setPlayerHomeClub(club.getIdclub());
            LOG.debug("setted HomeClub = " + player.getPlayerHomeClub());
            LOG.debug("inputSelectHomeClub = " + sessionMap.get("inputSelectHomeClub")); 
             LOG.debug(" selectHomeClub competition = " + competition); 
         competition.competitionDescription().setCompetitionClubId(club.getIdclub());
             LOG.debug(" competition updated ClubId= " + competition.competitionDescription()); 
       LOG.debug("line 01");
  ///    boolean b = Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
       dialogController.closeDialog(null);
 //         LOG.debug("DialogClub closed ! = " + b);
      if(sessionMap.get("inputSelectHomeClub").equals("Create Competition Description")){
            return "competition_create_description.xhtml?faces-redirect=true";
      }
 //      LOG.debug("after test on sessionMap ");
        if(createModifyPlayer.equals("M")){
            LOG.debug("This is a modification player = " + createModifyPlayer);
            return "player_modify.xhtml?faces-redirect=true";
        }else{
            LOG.debug("This is a creation player = " + createModifyPlayer);
            return "player.xhtml?faces-redirect=true";} 
    //    return null; // mod 06-04-2017 aussi player-modify est possible
  } catch (Exception e) {
            String msg = "££ Exception in selectHomeClub = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
 //   return null;
    } // end class selectcourse
 */
/* migrated on 2026-02-25 — now in Controller.refact.ClubController
  public String selectedCourseForClub(Course c) { ... }
*/
  
/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectCourse_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectCourse2_xhtml(String s){
            LOG.debug("entering to_selectCourse2_xhtml ... with string = " + s);
       reset("Reset to_selectCourse " + s);
       sessionMap.put("inputSelectCourse", s);
       sessionMap.put("inputSelectClub", s);
       enumeration.ClubSelectionPurpose purpose = enumeration.ClubSelectionPurpose.CREATE_PRO;
       
       
          LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
          LOG.debug("club selected for :  = " + sessionMap.get("inputSelectClub"));
       return "selectClubCourse.xhtml?faces-redirect=true";
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectGrpc_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectClubLA_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectClubSYS_xhtml(String s) { ... }
*/

/*/ enlevé 03-02-2026
public String to_selectMenuUnavailable_xhtml(String s){ 
            LOG.debug("entering to_selectMenuUnavailable_xhtml ... with string = " + s);
       reset("Reset to_selectClubLA " + s);
       sessionMap.put("inputSelectClub", s);
  //          LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
      return "unavailable_menu.xhtml?faces-redirect=true";
   }
*/
/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectPurpose_xhtml(String menuSelection) { ... }
*/


/*
public String to_selectDesignationLA_xhtml(String s){  // LocalAdministrator
            LOG.debug("entering to_selectDesignationLA_xhtml ... with string = " + s);
        reset("Reset to_selectClubLA " + s);
        sessionMap.put("inputSelectClub", s);
        enums.ClubSelectionPurpose purpose = enums.ClubSelectionPurpose.LOCAL_ADMIN; // new 01-02-2026
          LOG.debug("purpose setted to = " + purpose);
        enums.ClubSelectionPurpose purpose2 = enums.ClubSelectionPurpose.fromCode(s);
            LOG.debug("purpose searched and setted to = " + purpose2);
            LOG.debug("club selected for :  = " + sessionMap.get("inputSelectClub"));
       return "local_administrator2.xhtml?faces-redirect=true";
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController
public String to_selectCourseLA_xhtml(String s) { ... }
*/






/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String to_update_help(String s){
            LOG.debug("entering to_update_help ... with string = " + s);
       reset("Reset to_selectCourse " + s);
       return "treenode.xhtml?faces-redirect=true";
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public String to_selectLocalAdmin_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public String to_selectSystemAdmin_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public String to_selectPro_xhtml(String s) { ... }
*/
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String to_selectClub_xhtml(String s){ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String to_selectClubDialog_xhtml(String s){ ... }
*/



/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String selectClub(Club c, String select){ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String selectCourseLA(ECourseList in_club, String select){ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String selectClubCourse(){ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String selectedCourse(){ ... }
*/


/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String selectCourse(ECourseList ecl) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String findTarifGreenfee(){
    // tarif pour l'inscription (green fee)
    // used in greenfee_cotisation_round.xhtml
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String findTarifCotisation(){
    // used in greenfee_cotisation_round.xhtml
}
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String selectTravel(ECourseList ecl) { ... }
*/


/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String selectChart(ECourseList ecl) { ... }
*/

/*
public String gRpcServer(String param){
  try{
            LOG.debug("entering gRpcServer with param = " + param);
            
            var v = new Controllers.GrpcController().startServer();
             LOG.debug("gRpcServer started = " + v);
 
           String s = new grpc.__ApplicationClient().sendMessage();   
           LOG.debug("retour send message = " + s);
           
      //     server.awaitTermination();
           LOG.debug("gRpcServer awaiting termination = " + param);
        return null;
   }catch (Exception e){
            String msg = "CourseController - grpcServer : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } 
} // end method HandicapModel
*/
/* migrated on 2026-02-25 — now in Controller.refact.TechnicalController (techC)
   public void checkMail(String ini) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.TechnicalController (techC)
   public void newMessageFatal(String ini) { ... }
*/
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public void to_reset_menu(String ini){ ... }
*/

// new 12-02-2026, called from LoginBean : supprimé !!
  //public String reset(String ini) {
    public void reset(String ini) {
    try {
        LOG.debug("starting CourseController reset with: " + ini);
    /*    
        // --- Reset PlayerController objects only on login ---
        if ("from login".equals(ini)) {
            if (appContext.getPlayer() == null) {
                playerC.setPlayer(new Player());
                LOG.debug("Player initialized in PlayerController");
            }
            playerC.setPlayerPro(new Player());
            playerC.setLocalAdmin(new Player());
            LOG.debug("PlayerController reset done");
            // Ne pas rediriger, reste sur login.xhtml
            return null;
        }
        // faire si vient de postconstruct //
        
        if ("from init in CourseController".equals(ini)) {
            if (appContext.getPlayer() == null) {
                playerC.setPlayer(new Player());
                LOG.debug("Player initialized in from init in CourseController");
            }
            playerC.setPlayerPro(new Player());
            playerC.setLocalAdmin(new Player());
            LOG.debug("PlayerController reset done");
            // Ne pas rediriger, reste sur login.xhtml
            return null;
        }
        */
    // new suite migration vers Controlleurs externes    ////////////////

        appContext.reset();  // sauf Player
    // --- Reset global lists --- délégué à CacheInvalidator — migrated 2026-02-25
        cacheInvalidator.invalidateAll();
        Controllers.LoggingUserController.setText("first start");
        // --- Clear dropped/dragged players if Player exists ---
        if (appContext.getPlayer() != null) {
            appContext.getPlayer().clearDroppedPlayers();
            appContext.getPlayer().clearDraggedPlayers();
        }

    // --- Reset other entities ---
    //    player2 = new Player(); enlevé 14-02-2026 15:11
    //    playerTemp = new Player(); enlevé 12-02-2026
        // club = new Club(); — removed 2026-02-25 (appContext.reset() s'en charge)
        // course = new Course(); — removed 2026-02-25 (appContext.reset() s'en charge)
        handicap = new Handicap();
    //    handicapIndex = new HandicapIndex();
        // hole = new Hole(); — removed 2026-02-25 (migrated to ClubController)
        matchplay = new Matchplay();
        inscription = new Inscription();
        round = new Round();
        scoreMatchplay = new ScoreMatchplay();
        scoreStableford = new ScoreStableford();
        scoreScramble = new ScoreScramble();
        // tee = new Tee(); — removed 2026-02-25 (migrated to ClubController)
        subscription = new Subscription();
        cotisation = new Cotisation();
        playingHcp = new PlayingHandicap();
        tarifGreenfee = new TarifGreenfee();
        greenfee = new Greenfee();
        tarifMember = new TarifMember();
        creditcard = new Creditcard();
        creditcard.setPaymentOK(false);
        // holesGlobal = new HolesGlobal(); — removed 2026-02-25 (migrated to ClubController)
        login = new LoginBeanSecurity();
        password = new Password();
        cptFlight = 0;
        unavailable = new EUnavailable(new UnavailableStructure(), new UnavailablePeriod());
        // setProgress1(0); // migrated 2026-02-25 to PaymentController
        // competition = new ECompetition(...) — removed 2026-02-25 (now in appContext.reset())
        unavailablestructure = new UnavailableStructure();
        unavailableperiod = new UnavailablePeriod();

        setFilteredCourses(null);
        setFilteredClubs(null);

        /*/ ✅ Si appelé depuis login, ne pas rediriger
        if ("from login".equals(ini)) {
            LOG.debug("Reste sur login.xhtml for " + ini);
            return null; // reste sur login.xhtml
        }
        
        // ✅ Sinon, rediriger vers welcome
        LOG.debug("va vers welcome.xhtml for " + ini);
        return "welcome.xhtml?faces-redirect=true";
        */
     } catch (Exception ex) {
        LOG.error("Error in reset! " + ex);
        showMessageFatal("Exception reset = " + ex.toString());
       // return "error";
    }
}



//public void createCourse(){

// ✅ MIGRÉ vers ClubController (clubC) - createCourse
/*
public String createCourse(){    
  try{
        LOG.debug("start to create course, clubID = " + club.getIdclub() );
  //  create.CreateCourse cc = new create.CreateCourse();
 //   boolean ok = cc.createCourse(club, course, conn);
    if(new create.CreateCourse().create(club, course, conn)){  // ok
        LOG.debug("course created, next step = tee");
        tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
        return "tee.xhtml?faces-redirect=true";  //return "modify_holes_distance.xhtml?faces-redirect=true";
    }else{
        return null;
    }
 }catch (Exception ex){
     String msg = "Fatal error in CreateCourse";
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
 }
} // end method
*/ // end ✅ MIGRÉ - createCourse




// ✅ MIGRÉ vers ClubController (clubC) - createTee
/*
 public String createTee(){   
  try{
        LOG.debug("Starting createTee !");
 //   create.CreateTee ct = new create.CreateTee();
 //   boolean ok = ct.createTee(club, course, tee, conn);
    if(new create.CreateTee().create(course, tee, conn)){
        LOG.debug("tee created : we go to hole !!");
        hole.setNextHole(true); // affiche le bouton next(Hole) bas ecran à  droite
        Tee t = new read.ReadTee().read(tee,conn);
        if(t.isNotFound()){
            LOG.debug(" Tee not found ! = " + t.toString());
        }else{
            tee = t;
        LOG.debug(" loaded tee = " + t.toString());
        }
        if(tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
               LOG.debug("master tee  ==> 3 lignes avec par, index et distances : la totale ");
             return "modify_holes_global.xhtml?faces-redirect=true";
        }else if (!tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
               LOG.debug("distance tee  ==> 1 ligne avec les distances");
             return "modify_holes_distance.xhtml?faces-redirect=true";
        } else {
            String msg = "No holes registration needed : we already have all the information with MasterTee and DistanceTee";
            LOG.info(msg);
            showMessageInfo(msg);
            return null; //retourne d'où il vient donc tee.xhtml
        }
    }else{
       
       return null;
    }
 }catch (Exception ex){
     String msg = "Fatal error in CreateTee";
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
 }   
} // end create Tee
*/ // end ✅ MIGRÉ - createTee


public void simulateHcpStb() throws Exception, Exception{
    LOG.debug("Starting Stableford Hcp calculated !");
    LOG.debug("with PlayingHcp = " + playingHcp);
    LOG.debug("array input data " + Arrays.toString(playingHcp.getHcpScr()));
    Handicap h = new Handicap();
    h.setHandicapPlayerEGA(java.math.BigDecimal.valueOf(playingHcp.getHandicapPlayerEGA()));

    Tee t = new Tee();
    t.setTeeSlope(playingHcp.getTeeSlope().shortValue()); 
       LOG.debug("with slope = " + t.getTeeSlope());
    t.setTeeRating(java.math.BigDecimal.valueOf(playingHcp.getTeeRating()));
       LOG.debug("with rating = " + t.getTeeRating());
    t.setTeePar(playingHcp.getCoursePar().shortValue());
      LOG.debug("with par = " + t.getTeePar());

    Round r = new Round();
    r.setRoundHoles(playingHcp.getRoundHoles());
    int i = 0;
//    int i = new calc.CalcStablefordPlayingHandicapEGA().calculatePlayingHcp(conn, h, t ,r);
    playingHcp.setPlayingHandicap(i);
      LOG.debug("Playing Hcp calculated !! = " + playingHcp.getPlayingHandicap() );
}

public void calculateHcpScramble() throws Exception{
    LOG.debug("calculate handicap scramble");
    LOG.debug("with input array data = " + Arrays.toString(playingHcp.getHcpScr()));
}


// ✅ MIGRÉ vers ClubController (clubC) - createHole
/*
public void createHole(){
  try{ // mod 28/02/2022
   // if(new create.CreateHole().create(club, course, tee, hole, STROKEINDEX, conn)){
     if(new create.CreateHole().create(club, course, tee, hole, STROKEINDEX)){    
        // ajouter boolean = correct insert !!!
        LOG.debug("hole created : we go to hole !!");
     setNextStep(true);  // affiche le bouton next(Step) bas ecran à droite}
    }
  }catch (Exception ex){
     String msg = "Fatal error in CreateHole";
     LOG.error(msg);
     showMessageFatal(msg);
 }
} // end method
*/ // end ✅ MIGRÉ - createHole

/* enlevé 21-02-26 utilisé dans hole.xhtml qui est deprec
    public List<Integer> indexNumbers(final int max) {
        // list avec les stroke index (de 1 à 18)
        // si un stroke index est utilisé, il est removed ! dans createHole
        if (STROKEINDEX.isEmpty()) {   //LOG.debug("values is empty !");
            for (int i = 0; i < max; i++) {
                STROKEINDEX.add(i + 1);
            }
        }
//LOG.debug("values array = " + values.toString() );
        return STROKEINDEX;
    }
*/
    public List<Integer> holeNumbers(final int max) {   // used for score.xhtml : numéro des trous,
        // list avec les stroke index (de 1 à  18)
        if (NUMBERS.isEmpty()) {   //LOG.debug("values is empty !");
            for (int i = 0; i < max; i++) {
                NUMBERS.add(i + 1);
            }
        }
        LOG.debug("holeNumbers returned  =" + NUMBERS.toString());
        return NUMBERS;
    }

    /* migrated on 2026-02-25 — now in Controller.refact.RoundController
        public void processChecked(AjaxBehaviorEvent e) { ... }
    */
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public void convertYtoM() { ... }
*/
    /*
  public void createRound_old() throws SQLException{
       LOG.debug("entering createRound ");
  //     LOG.debug("round = " + round.toString());
       round.setRoundDate(round.getRoundDateTrf()); // zone de transfert provenant de Flight, avec heure de départ
       LOG.debug("round after TRF Date = " + round.toString());

    if(new create.CreateRound().create(round, course, club, unavailable.period())){
        LOG.debug("round created : we go to inscription !!");
         setNextInscription(true); // affiche le bouton next(Inscription) bas ecran à droite
    }else{
        LOG.debug("round NOT NOT created !!");
    }
} // end method
*/
  
  /**
     * Crée un round
     * ✅ Version corrigée
     */
  /* migrated on 2026-02-24 — now in Controller.refact.RoundController
    public void createRound() {
        final String methodName = utils.LCUtil.getCurrentMethodName();
        LOG.debug("entering " + methodName);
          try {
           // Transfert de la date depuis Flight
            round.setRoundDate(round.getRoundDateTrf());
                LOG.debug("round after TRF Date = " + round);
            Club   club   = appContext.getClub();       // ✅ depuis ApplicationContext
            Course course = appContext.getCourse();      // ✅ depuis ApplicationContext
            // ✅ Appel de la méthode de création
          //  boolean success = new create.CreateRound().create(
            // ✅ plus de new, injection directe en attendant RoundController
            boolean success = createRoundService.create(
                round, 
                course, 
                club, 
                unavailable.period() 
            );
            if (success) {
                LOG.debug("✅ Round created successfully!");
                setNextInscription(true); // Affiche le bouton "Inscription"
                // ✅ Optionnel : Réinitialiser le formulaire pour un nouveau round
                // round = new Round();
                appContext.setRound(round);   // new 20-02-2026 synchronise ApplicationContext
            } else {
                LOG.error("❌ Round creation failed");
                setNextInscription(false); // ✅ Cache le bouton si échec
            }
            
         } catch (Exception e) {
            LOG.error("Unexpected exception in createRound", e);
            showMessageFatal("Unexpected error: " + e.getMessage());
            setNextInscription(false);
        }
    } // end method
  */

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
 public String PlayerDrop(DragDropEvent<?> event) {
    return null;
} // end method
*/ // end ✅ MIGRÉ - PlayerDrop

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public String PlayerRemove(Player p) {
        return "inscriptions_other_players.xhtml?faces-redirect=true";
} //end method PlayerRemove
*/ // end ✅ MIGRÉ - PlayerRemove
    
    
//new 16-09-201 remplace field round.roundPlayers

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public List<Player> roundPlayersList() throws SQLException, Exception{
   lp = roundPlayersList.list(round);
   if(lp != null) round.setPlayersString(Round.fillRoundPlayersString(lp));
   return lp;
}
*/ // end ✅ MIGRÉ - roundPlayersList

/* migrated on 2026-02-24 — now in Controller.refact.RoundController
public String createInscriptionOtherPlayers(){
try{
    LOG.debug("entering CourseController.createInscriptionOtherPlayers");
    LOG.debug("round = " + round.toString());
 // a faire tester ici prélablement les rejets pour non membre et non paiement greenfee
     appContext.getPlayer().getDroppedPlayers().forEach(item -> LOG.debug("list of dropped Players =" + item.getIdplayer()));  // java 8 lambda
     LOG.debug("number of inscriptions to be created= " + appContext.getPlayer().getDroppedPlayers().size() );
  // new l
     List<Player> copy = List.copyOf(appContext.getPlayer().getDroppedPlayers()); // immutable
     for(int i=0; i < copy.size(); i++){
        LOG.debug("starting with i = "+ i);
        Player p = copy.get(i);
            LOG.debug("traitement de l'inscription pour player = " + p);
        inscription.setRound_idround(round.getIdround());
        inscription.setInscriptionInvitedBy(appContext.getPlayer().getPlayerFirstName() + "," + appContext.getPlayer().getPlayerLastName());
    // mod 09-11-2021
    inscription = createInscription.create(round, p, appContext.getPlayer(), inscription, club, course, "B"); // migrated 2026-02-24
        if(inscription.isInscriptionError()){
            String msg = "Inscription other players NOT OK for player = " + p.getIdplayer() + " / " + p.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
//            continue; //The continue statement breaks one iteration (in the loop), if a specified condition occurs, and continues with the next iteration in the loop.
        }else{ // success
            LOG.debug("Joueurs inscription OK = " + p);
 //           LOG.debug("i = " + i);
            appContext.getPlayer().getDroppedPlayers().removeIf(item -> item.getIdplayer().equals(p.getIdplayer()));
            LOG.debug("after remove length droped = " + appContext.getPlayer().getDroppedPlayers().size());
            LOG.debug("after remove length copy = " + copy.size());
//             LOG.debug("after remove, i = " + i);
        } //end if
       } //end for
      return "inscriptions_other_players.xhtml";
   }catch(Exception ex){
    String msg = "CourseC. creatInscriptionOtherPlayers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }
}  // end method
*/

/* migrated on 2026-02-24 — now in Controller.refact.RoundController
public String createInscription(){
try{
        LOG.debug("entering createInscription");
        LOG.debug("with round = " + round);
    Player invitedBy = appContext.getPlayer();
        LOG.debug("round inscription = "+  round);
        LOG.debug(" inscription = "+  inscription);
 //   int ret = new create.CreateInscription().create(round, player, invitedBy, inscription, club, course, "A" ,conn); // mod 10/11/2014
    inscription = createInscription.create(round, appContext.getPlayer(), invitedBy, inscription, club, course, "A"); // migrated 2026-02-24
    LOG.debug("inscription returned  createInscription = " + inscription);
  
    if( !inscription.isInscriptionError()){ // no errors !!!
          String msg =  LCUtil.prepareMessageBean("inscription.ok") + round + inscription
     //        + " <br/> player = " + player.getIdplayer()
             + " <br/> player name = " + appContext.getPlayer().getPlayerLastName()
             + " <br/> club name = " + club.getClubName()
             + " <br/> course name = " + course.getCourseName() ;
          LOG.info(msg);
          showMessageInfo(msg); // new 01-09-2025
          inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
      //    return "welcome.xhtml?faces-redirect=true";
          return "inscription.xhtml?faces-redirect=true";
      }     
        
    LOG.debug("inscription error status = " + inscription.getErrorStatus());
    
     if(inscription.getErrorStatus().equals("01")){
           inscription.setInscriptionOK(false);
           //  used in inscription.xhtml
           return "inscription.xhtml?faces-redirect=true";
        }
    if(inscription.getErrorStatus().equals("02")){
        String err = LCUtil.prepareMessageBean("cotisation.notfound"); 
        LOG.debug(err);
        showMessageFatal(err);
        inscription.setInscriptionOK(false); // used in inscription.xhtml
        return "greenfee_cotisation_round.xhtml?faces-redirect=true";
    }
    if(inscription.getErrorStatus().equals("03")){
        inscription.setInscriptionOK(false); //  used in inscription.xhtml
        return "greenfee_cotisation_round.xhtml?faces-redirect=true";
    }
    if(inscription.getErrorStatus().equals("04")){
        inscription.setInscriptionOK(false);
        String msg = inscription.getWeather(); // mauvaise utilisation !!
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
      //  return "inscription.xhtml?faces-redirect=true";
     //   return "inscription.xhtml"; // simple forward : pourquoi ??
        
    }
     if(inscription.getErrorStatus().equals("05")){ // course unavailable new 20/06/2022
        inscription.setInscriptionOK(false);
   //     return "inscription.xhtml?faces-redirect=true";
        return "inscription.xhtml"; // simple forward : pourquoi ??
    }
    
    
 //     if(ret == 00){
 //          String msg =  LCUtil.prepareMessageBean("inscription.ok") + round + inscription
 //     //        + " <br/> player = " + player.getIdplayer()
 //              + " <br/> player name = " + player.getPlayerLastName()
 //              + " <br/> club name = " + club.getClubName()
 //              + " <br/> course name = " + course.getCourseName()
 //            ;
 //          LOG.info(msg);
 //          inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
 //      //    return "welcome.xhtml?faces-redirect=true";
 //          return "inscription.xhtml?faces-redirect=true";
 //      }
  }catch(Exception ex){
    String msg = "CreateInscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
return null;
}   //end method CreateInscription
*/


  
public void existCountry(){
    LOG.debug("country name = " + country.getName());
    LOG.debug("player id = " + appContext.getPlayer().getIdplayer());
}

public String forgetPassword() throws Exception{
try{
    LOG.debug("entering forgetPassword");
    createActivationPassword.create(appContext.getPlayer()); // y compris envoi du mail
 //   LOG.debug("line 02");
   utils.LCUtil.showDialogInfo("Nous venons de vous envoyer un mail", "vous devez y répondre dans les 10 minutes !");
   return null;
  }catch(Exception ex){
    String msg = "forget password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
}

    public String getUuid() { // from password_check.xhtml
        LOG.debug("uuid transfered from password_check = " + uuid);
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

 public String resetPassword() throws SQLException, Exception, Throwable{  // called from password_check.xhtml
    // créer une nouvelle session ?
    // mail envoyé par user pour réinitialiser son password
  try{
      LOG.debug("entering resetPassword ");
      LOG.debug("with uuid = " + uuid); //voir  <f:viewAction action="#{courseC.activateNewPlayer(param.uuid)}"/>
      LOG.debug("a examiner current player = " + appContext.getPlayer()); // null 
/*      LOG.debug("entering resetPassword with activation = " + activation.getActivationKey());  // à mon avis c'est pas bon c'est null !
      
      player = new Player(); // new 23-02-2020
            //https://www.baeldung.com/java-request-getsession 
          https://www.digitalocean.com/community/tutorials/java-session-management-servlet-httpsession-url-rewriting
        LOG.debug("current session =  " + externalContext.getSessionId(true));
      externalContext.invalidateSession();
       String msg = "session invalidated !!";
           LOG.info(msg);
           LCUtil.showMessageInfo(msg);
           
         LOG.debug("after new session activationkey = " + activation.getActivationKey() );
  // on récupère activation à partir de sa key
      */
 ///       activation.setActivationKey(uuid); // new 29-02-2024
 ///       activation = new read.ReadActivation().read(conn, activation);
        LOG.debug("Activation resetPassword = " + activation); // on a le id du player
  // controle sur la durée
       Duration difference = Duration.between(activation.getActivationCreationDate(),LocalDateTime.now());
        long differenceInMinutes = difference.toMinutes();
                LOG.debug("difference in minutes = " + differenceInMinutes);
         if(differenceInMinutes < 10){
                String msg = LCUtil.prepareMessageBean("password.reset.ok") + (10 - differenceInMinutes) + " minutes";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
           }else{
             LOG.debug("too late for reinitialisation password");
                appContext.getPlayer().setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for the reset of your Password " 
                        + activation.getActivationPlayerId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "login.xhtml?faces-redirect=true";
           }  
   // on récupère le playerid dans activation
   // transférer activation dans 
   //player.setIdplayer(activation.getActivationPlayerId());

 //  epp.setPlayer(player);
 //  epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
 //  player= epp.getPlayer(); // partial
 //  password = epp.getPassword();
   // checkpassword param = activation seulement
  // var v  = new PasswordController().resetPassword(epp, activation, conn); // delete activation row, update password
   var v  = passwordController.resetPassword(activation); // delete activation row, update password
   
   ///  appContext.getPlayer() = v.player(); // complete
   ///// modifié 12-02-2026 non testé !!
   appContext.setPlayer(v.getPlayer());
  
    if(appContext.getPlayer() != null) { 
        String msg = ("The password reset was asked by " + appContext.getPlayer().getIdplayer());
        LOG.info(msg);
        showMessageInfo(msg);
        return "login.xhtml?faces-redirect=true";
    }else{ // false
        String msg = "Activation record not found : you already had done this work in a recent past ! " ;
        LOG.error(msg);
        showMessageFatal(msg);
     return null;  
    //     return "activation_failure.xhtml?faces-redirect=true"; // mod 03-12-2018
     }
   }catch(Exception ex){
        String msg = "Course controller : resetPassword Exception ! " + ex;
     //   System.err.print(msg);
        LOG.error(msg);
    //    System.err.print("system error LC" + ex);
        showMessageFatal(msg);
        return null;
}    
}
    
  //  mod 01-03-2024 public void activateNewPlayer(String UUID){
     public void completeActivation(String UUID) throws Exception{
        //   <f:viewAction action="#{courseC.activateNewPlayer(param.uuid)}"/>
    //used in activation_check.xhtml dans View Action donc exécuté AVANT affichage écran
    //used in password_check.xhtml dans View Action donc exécuté AVANT affichage écran
          LOG.debug("entering completeActivation with UUID = " + UUID);
     //  uuid = UUID;
     //  activation.setActivationKey(uuid); // new 29-02-2024, 01-03-2024
       activation.setActivationKey(UUID); // new 29-02-2024, 01-03-2024
       activation = readActivation.read(activation);
       if(activation == null){
           LOG.debug("Activation is null, not found ! = " + activation);
       }else{
            LOG.debug("Activation found = " + activation); // on a le id du player
       }
}
    
public String activateNewPlayer() throws Throwable{ 
    // voir method ci-dessus qui est exécutée d'abord
    // used in from activation_check.xhtml }"/>
    //used in activation_check.xhtml par commandButton action="#{courseC.activateNewPlayer(),donc exécuté APRES affichage écran
  try{
        LOG.debug("entering ActivateNewPlayer with activation = " + activation);
   // est null     LOG.debug("entering ActivateNewPlayer with activation = " + activation);
   // à modifier !!
   //   activation.setActivationKey(uuid);
   //   activation = new read.ReadActivation().read(conn, activation);
   //     LOG.debug("after LoadActivation we detected the new Player = " + activation.getActivationPlayerId()); // on a le id du player
      if(activation == null){  // new 01-01-2023
          LOG.debug("activation is null - return");
          return null;
      }  
       Duration difference = Duration.between(activation.getActivationCreationDate(),LocalDateTime.now());
        long differenceInMinutes = difference.toMinutes();
                LOG.debug("difference in minutes = " + differenceInMinutes);
         if(differenceInMinutes < 10){
                String msg = "Votre Enregistrement à GolfLC est activé !!! <br/> You Respected the deadline of 10 minutes :" + " it was remaining = " + (10 - differenceInMinutes);
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
           }else{
                appContext.getPlayer().setIdplayer(null);
             /// changer le msg pour la langue !
                String msg = "You are " + differenceInMinutes + " minutes too late for your Registration activation or the reset of your Password " 
                        + activation;
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "login.xhtml?faces-redirect=true";
           }  
  //  player.setIdplayer(activation.getActivationPlayerId());
  //     LOG.debug("searching playerid = " + player.getIdplayer());
  // mod 29-02-2024 player = new find.FindPlayer().find(player, conn);
  //  player = new read.ReadPlayer().read(player, conn);
  //      LOG.debug("player found from activation new Player = " + player); // c'est OK
 
  // String s = new ActivationController().check(player, activation, conn);
   String s = activationController.check(activation);
   LOG.debug("string s = " + s);
  //  if(new PasswordController().checkPassword(uuid, conn)){  //true
    if(appContext.getPlayer() != null) { 
  //      String msg = ("The activation is a success -  Welcome new  player : " + );
        String msg =  LCUtil.prepareMessageBean("player.welcome" + appContext.getPlayer().getPlayerFirstName());
        LOG.info(msg);
        showMessageInfo(msg);
        return "login.xhtml?faces-redirect=true";
    }else{ // false
        String msg = "Activation record not found : you already had done this work in a recent past ! " ;
        LOG.error(msg);
        showMessageFatal(msg);
     return null;  
    //     return "activation_failure.xhtml?faces-redirect=true"; // mod 03-12-2018
     }
   }catch(Exception ex){
        String msg = "Course controller : activateNewPlayer Exception ! " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
}    
}
/* enlevé 01-03-2024 not used ??
public String createPassword() throws Exception{     // used in password_create.xhtml
 try{
     LOG.debug("entering createPassword");
     LOG.debug("player for password = " + player);
     epp.setPlayer(player);
     epp.setPassword(password);
     if(new update.UpdatePassword().update(epp, conn)){  // true
          LOG.debug("boolean returned from modifyPassword is 'true' ");
                  String msg = "<br> <br> <h1>Password created/modified 2493 !! ";
                   LOG.info(msg + password.getWrkpassword());
                 password.setWrkpassword("***********");
                 password.setWrkconfirmpassword("***********");
                 return "login.xhtml?faces-redirect=true";
      }else{ 
                 String msg = "The password is not modified !!";
                 LOG.error(msg);
                 showMessageFatal(msg);
                 return null;
      } 
  }catch(Exception ex){
    String msg = "create password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}// end method
*/
/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String validateExistingPassword() throws SQLException { ... }
*/
  
/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String modifyPassword() throws SQLException, Exception { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String findTarifGreenfeeEcl(ECourseList ecl){
}
*/



/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public String findWeather(){
    String weather = findOpenWeather.find(club);
    inscription.setWeather(weather);
     return null;
}  //end method
*/ // end ✅ MIGRÉ - findWeather   

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String selectTarif(ECourseList ecl){
}
*/

/* migrated on 2026-02-24 — now in Controller.refact.RoundController
public String selectRound(){
 try{
         LOG.debug("entering selectRound");
         LOG.debug("  with Round = " + round);
    ///  var b = Controllers.DialogController.closeDialog("dialogRound.xhtml"); // new 05/10/2024
       dialogController.closeDialog(null);
    //      LOG.debug("  with dialogRound() closed = " + b);
      if(sessionMap.get("inputSelectRound").equals("INSCRIPTION")){
          
          List<ECourseList> li = inscriptionListForOneRound.list(round); // migrated 2026-02-24
          LOG.debug("  with Club = " + li.getFirst().club());
          LOG.debug("  with Course = " + li.getFirst().course());
          LOG.debug("  with Round = " + li.getFirst().round());
          // transférer  ??? vers club et course ?
          return "inscription.xhtml?faces-redirect=true";
      }
return null;
 }catch(Exception ex){
    String msg = "courseC.selectRound() Exception ! " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return null;
}
    } // end selectRound
*/

/* migrated on 2026-02-24 — now in Controller.refact.RoundController
public String selectRecentInscription(ECourseList ecl){
 try{
           LOG.debug("entering selectRecentInscrition");
           LOG.debug("  selectRecentInscription, ecl = " + ecl);
     ///      Controllers.DialogController.closeDialog("dialogRound.xhtml"); // new 05/10/2024
            dialogController.closeDialog(null);
 //          LOG.debug("filteredInscriptions = " + getFilteredInscriptions());
        club = ecl.club();
        course = ecl.course();
        round = ecl.round();
    //       LOG.debug("on cherche le nombre de joueurs  déjà inscrits et leur nom"); 
     LOG.debug("  at this crucial moment round = = " + round);
    
 //       lp = roundPlayersList.list(round); // migrated 2026-02-25
        lp = roundPlayersList.list(round); // migrated 2026-02-25
  //         LOG.debug("after lists.RoundPlayersList on a nombre de situations =  " + lp.size());
  if(lp != null){
         LOG.debug("nombre de players = " + lp.size());
        inscription.setInscriptionOK(true); // new 12/7/2017 used in inscription.xhtml
    //    String s = utils.LCUtil.fillRoundPlayersString(lp);
        String s = Round.fillRoundPlayersString(lp);
        round.setPlayersString(s);
         LOG.debug("joueurs déjà inscrits = " + round.getPlayersString() );
  }else{
         LOG.debug("nombre de players stableford =  zero" );
       round.setPlayersString("no players reservation");
   }

   String msg = "Select RecentInscription EcourseList Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> course name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
   //     showMessageInfo(msg);
        return "inscription.xhtml?faces-redirect=true";
   }catch(Exception ex){
    String msg = "selectRecentInscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
    } // end method
*/
    

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String inputTarifMembersCotisation(){
}
*/
/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String showTarifGreenfee(String idcourse){
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String inputTarifGreenfee(String param){
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
 public String createUnavailablePeriod(){ ... }
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String createTarifMembers() throws SQLException, Exception{
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String createTarifGreenfee() throws SQLException, Exception{
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String deleteTarifGreenfee(String year) throws SQLException, Exception{
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String inputTarifMembersEquipments() throws SQLException, Exception{
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
  public String inputUnvailableStructure() throws SQLException, Exception{ ... }
*/
 

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String showTarifMembers() throws SQLException, Exception{
}
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String showUnavailableStructure() throws SQLException{ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String showUnavailablePeriod(){ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public EUnavailable showUnavailablePeriods() throws SQLException{ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String showUnavailablePeriods(Club c) throws SQLException{ ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
public String showUnavailablePeriods(ECourseList ecl) throws SQLException{ ... }
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public String selectedCompetitionScoreStableford(ECompetition ec) throws SQLException { ... }
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String selectedScoreToRegister(ECourseList ecl, String type) throws SQLException { ... }
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public String RegisterScoreMatchplay(ECourseList ecl, String type) throws SQLException { ... }
*/
/*
public List<Round> classementMatchplay() throws SQLException{
try{
     LOG.debug("entering classement MatchPlay with round = " + round);
     LOG.debug("entering classement MatchPlay with club = " + club);
    double totA = 0;
    double totB = 0;
    var li = new lists.MatchplayClassmentList().list(round,club, conn);
    LOG.debug("found size li = " + li.size());
   // calcul du total pour la competition 
    for(int i=0; i < li.size() ; i++){
        LOG.debug("i = " + i);
        LOG.debug("roundid = " + li.get(i).getIdround()); 
      if(li.get(i).getScoreMatchplay() != null){ // il y a un résultat enregistré
          String[] s = li.get(i).getScoreMatchplay().getResult();
        // var data = li.get(i).getCompetitionData();
  //       LOG.debug("v = " + v.toString());
         LOG.debug("v result = " + Arrays.toString(s)); 
         
//v.getScoreMatchplay().getResult().toString());
         if(s[0].contains("&") || s[0].contains("U")){ // Up
             totA = totA + 1;
             LOG.debug("totA = " + totA);
         }
  //       if(s[0].contains("&|U")){ // regular expression
  //           totA++;
 //            LOG.debug("regular totA = " + totA);
 //        }
 //        if(Pattern.matches("&|U",s[0])){ // regular expression
 ///            totA++;
  //           LOG.debug("pattern totA = " + totA);
  //       }
                  
         if(s[0].contains("H")) {  // HALVED
             totA = totA + 0.5;
             LOG.debug("totA = " + totA);
         }
         if(s[1].contains("&") || s[1].contains("U")){ // Up
             totB = totB + 1;
             LOG.debug("totB = " + totA);
         }
         if(s[1].contains("H")) { // HALVED
             totB = totB + 0.5;
             LOG.debug("totB = " + totB);
         }
        } // end if
    } //end for
      sessionMap.put("PointsTeamA", totA); // lus dans dialog_matchplay_classment
      sessionMap.put("PointsTeamB", totB);
      sessionMap.put("Competition", li.get(0).getRoundCompetition());
    return li; //new lists.MatchplayClassmentList().list(round,club, conn);

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method    
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String LoadScoreStableford(ECourseList ecl, String type) throws SQLException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String calculateScoreStableford() throws SQLException, Exception { ... }
*/
/*
public String calculateScoreMatchplay() throws SQLException, Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(); 
   LOG.debug("entering " + methodName);
   LOG.debug(" for Round = " + round);
   LOG.debug(" for current Player = " + player);
   LOG.debug("with scoreType = " + sessionMap.get("scoreType")); // COMPETITION or INDIVIDUAL
   LOG.debug("with competition Player = " + sessionMap.get("competitionPlayer"));
   LOG.debug(" for competition = " + competition);
   LOG.debug("for ScorematchPlay = " + scoreMatchplay);
try{
    
    // pour matchplay, la competition est enregistrée dans round.getRoundCompetition()

           LOG.debug(" going to Calculate scorematchplay ! " );
          scoreMatchplay = calc.CalculateScoreMatchplay.calc(player,scoreMatchplay, round, conn);
 //    return "score_matchplay.xhtml?faces-redirect=true";
     return null;  // même résultat, mais evite reaffichage !
}catch(Exception ex){
    String msg = "Exception in calculateScoreStableford() " + ex;
   LOG.error(msg);
   showMessageFatal(msg);
   return null;
}    
} // end method
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String loadStatisticsTable() throws SQLException, Exception { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String createStatisticsStableford() throws SQLException { ... }
*/

/* migrated on 2026-02-24 — now in Controller.refact.RoundController
public String createScoreStableford() throws SQLException, Exception{
   LOG.debug("entering createScoreStableford !");
try{
    LOG.debug("with round game = " + round.getRoundGame());
    LOG.debug("with competition Player = " + sessionMap.get("competitionPlayer"));
    LOG.debug("with current player = " + appContext.getPlayer().getIdplayer());
    LOG.debug("with scoreType = " + sessionMap.get("scoreType")); // COMPETITION or INDIVIDUAL
    LOG.debug("with roundid = " + round.getIdround());
    LOG.debug("with scoreStableford = " + scoreStableford);
    LOG.debug("Create with competition = " + competition);
    
//if(Round.GameType.STABLEFORD.toString().equals(round.getRoundGame())){


   
   
  Player p;// = null;
  if(sessionMap.get("scoreType").equals("COMPETITION")){
         LOG.debug("handling with scoreType = " + sessionMap.get("scoreType"));
  //    Object s = sessionMap.get("competitionPlayer");
      int playerid = Integer.parseInt((String) sessionMap.get("competitionPlayer"));
      p = new Player();
      p.setIdplayer(playerid);
     // p = new read.ReadPlayer().read(p, conn);
      p = playerManager.readPlayer(p.getIdplayer());
  }else{ // is INDIVIDUAL
       LOG.debug("handling with scoreType = " + sessionMap.get("scoreType"));
      p = appContext.getPlayer();
  }
   if(createOrUpdateScoreStableford.status(scoreStableford, round, p)){                      // migrated 2026-02-24
     LOG.debug("ScoreStableford created or modified !");
        if(sessionMap.get("scoreType").equals("INDIVIDUAL")){  // pas de statistics pour COMPETITION
           scoreStableford.setShowButtonStatistics(true);  // afficher l'écran statistiques'
        }
        if(round.getRoundQualifying().equals("Y")){
           scoreStableford.setShowCreateHandicapIndex(true);
        }
   }else{
        String msg = "ERROR Creation/Modification scoreStableford";
        LOG.error(msg);
        showMessageFatal(msg);
        return null; // added 24-12-2020
   }
   // new 18-11-2020
     if(sessionMap.get("scoreType").equals("COMPETITION")){
           LOG.debug("we are back from Calculate with scoreStableford = " + scoreStableford);
           LOG.info("competition is at this moment 1 = " + competition);
        competition.competitionData().setCmpDataScoreDifferential(scoreStableford.getScoreDifferential());
        competition.competitionData().setCmpDataScorePoints((short)scoreStableford.getAdjustedGrossScore());
           LOG.info("competition is at this moment 2 = " + competition);
        if( ! updateCompetitionData.update(competition.competitionData())){                 // migrated 2026-02-24
           String msg = "NOT modify CompetitionData !!";
           LOG.error(msg);
           showMessageFatal(msg);
        }
     } // end equals COMPETITION
return null;
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String createHandicapIndex() { ... }
*/    
/*
public void createScoreMatchplay() throws SQLException{
try{
            LOG.debug("entering createscoreMatchplay !!");
            LOG.debug("scoreMatchplay = " + scoreMatchplay);
 //           LOG.debug("result, winning team 1 = " + scoreMatchplay.getMatchplayResult() );
            if(new create.CreateScoreMatchplay().create(scoreMatchplay, round, conn)){
                String msg = "score matchplay created !!!";
                LOG.info(msg);
                showMessageInfo(msg);
            }else{
                String msg = "fatal error in WHSController !";
                LOG.error(msg);
                showMessageFatal(msg);
              //  return null;
            }
        LOG.debug("after createScoreMatchPlay");
            
            
} catch(Exception ex){
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
     //       return null;
}
} // end method
 */
/* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public void validateScoreHoleMatchplay2(){
*/
        
/* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public void validateScoreHoleMatchplay(FacesContext context, UIComponent toValidate, Object value)
*/
    


public List<Integer> getValues() {
        LOG.debug("entering getValues ...");
      if (VALUES.isEmpty()) {
            for (int i = 1; i < 19; i++) {
                VALUES.add(0);
            }
      }
        LOG.debug("values returned  =" + VALUES.toString());
        return VALUES;
    }

public void setValues(String strokes) {
        LOG.debug("enter setValues with strokes = " + strokes);
        VALUES.set(5, Integer.parseInt(strokes));
    }

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String show_scorecard_empty(ECourseList ecl) throws SQLException { ... }
*/

  /* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public List<ECourseList> listRecentRounds() {
*/
  
    public List<ECourseList> listProfessionalForClub() {
        LOG.debug("...  entering listProfessionalForClub " );
   try {
        // was: return new lists.ProfessionalListForClub().list(conn);
        return professionalListForClub.list(); // migrated 2026-02-24

   } catch (Exception ex) {
       String msg = "Exception in listProfessionalforClub! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
     }
    } //end method
  /*
public List<Professional> isProfessional() { // new 14-12-2021
      //  LOG.debug("...  entering isProfessional" ); loop
     //   LOG.debug("with player = " + player);
   try {
     //  int count = new find.FindCountProfessional().find(player, conn);
        var listPro = new lists.FindCountListProfessional().list(player, conn);
        if(!listPro.isEmpty()){
            playerPro = player;   // pourquoi ??
//            LOG.debug("playerPro forced to current Player");
        }
        return listPro;
   } catch (Exception ex) {
       String msg = "Exception in isProfessional! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }
} //end method
moved to playerecontroller
    
public List<Professional> getProfessionals() {
    try {
        List<Professional> listPro = new lists.FindCountListProfessional().list(player, conn);
        return (listPro != null) ? listPro : Collections.emptyList();
    } catch (Exception ex) {
        String msg = "Exception in getProfessionals: " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return Collections.emptyList();
    }
}
  */




//  public long getTotalCount(String name) {
 //       return customers.stream().filter(customers -> name.equals(customers.representative.name)).count();
 //   }
/* migrated on 2026-02-25 — now in Controller.refact.MemberController
   public List<ECourseList> listProfessionalPayments() {
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
   public List<ECourseList> listLocalAdminGreenfee(String s) {
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
   public List<ECourseList> listSystemAdminSubscription(String s) {
   }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
   public List<ECourseList> listLocalAdminCotisation(String s) {
   }
*/
 /* migrée 19-0-2026 
public List<Flight> listFlights(){
 try {
////     LOG.debug("from CourseController : entering listFlights .for WorkDate = .. " + round.getWorkDate());
  ///   LOG.debug("from CourseController : entering listFlights .for round = .. " + round);
  //   LOG.debug("from CourseController : entering listFlights .for course = .. " + course);
   //  LOG.debug("from CourseController : entering listFlights .for club = .. " + club);
  cptFlight++;
// LOG.debug("cptFlight after ++ = " + cptFlight);
   if(cptFlight == 1){   /// éviter iteration !!!
      LOG.debug("starting cptFlight loop with cptFlight = 1");
      if(round.getRoundDate() == null){
         String msg = "Test : listFlights : round workdate = null";
         LOG.error(msg);
         showMessageFatal(msg);
         return null;
       }else{
          //
         }
     //  if(club.getClubLatLng().getLat() == 0){
       if(club.getAddress().getLatLng().getLat() == 0){    
            String msgerr = "club's latitude is unknown ! stop de l'opération en cours !! ";
            LOG.error(msgerr);
            showMessageFatal(msgerr);
            throw new Exception(msgerr);
       }
 //  1  ---------------- recherche sunrise et sunset 
 
      // Flight flight2 = new lists.SunriseSunsetList().list(round, club);
      Flight flight2 = sunriseSunsetList.list(round, club); // migrated 2026-02-26
        LOG.debug("flight2 = " + flight2);
      if(flight2 == null){
          LOG.debug("flight is null !! cata");
          return null;
      }
          LOG.debug("step 1-Flight f = " + flight2.toString());
 // 2 ------------------  creation tableFlights : 1 record toutes les 12 min en partant de sunrise jusque sunset
    //    LOG.debug("timeZone tz = " + club.getClubZoneId());
  //  liste avec tousl les flights de la journée
      // flightList = new lists.AllFlightsList().createTableFlights(flight2, club.getAddress().getZoneId());
      flightList = allFlightsList.createTableFlights(flight2, club.getAddress().getZoneId()); // migrated 2026-02-26
 
      //   flightList.forEach(item -> LOG.debug("FlightList list " + item));
        LOG.debug("step 2-list created , size = " + flightList.size());
    //  boolean OK = new create.CreateTableFlights().create(flightList, course.getIdcourse(), conn);  // fake = courseid
    
       // if(new create.CreateTableFlights().create(flightList, course)){
       if(createTableFlights.create(flightList, course)){ // migrated 2026-02-26
            LOG.debug("boolean result create.CreateFlights = OK");
            // elimination des flights déjà réservés    
         //   flightList.forEach(item -> LOG.debug("FlightList list before " + item));
            // flightList = new lists.FlightAvailableList().listAllFlights();
            flightList = flightAvailableList.listAllFlights(); // migrated 2026-02-26
       //     flightList.forEach(item -> LOG.debug("FlightList list after " + item));
      //  LOG.debug("flightList returned = " + flightList);
         return flightList;
      }else{
           LOG.debug("error createTableFlights");
           return null;
       }
 }else{  // cptFlight != 1
     LOG.debug(" escaped to repetition with " + cptFlight);
     return flightList;
 }
   }catch (SQLException ex){
            String msg = "SQLException in listFlights " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   } catch (Exception ex) {
            String msg = "Exception in listFlights= " + ex.toString();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }
 } //end method
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public List<Matchplay> listMatchplayRounds(String formula) { ... }
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public List<ScoreScramble> listScrambleRounds(String formula) { ... }
*/

    public List<Matchplay> getListmatchplay() {
        return listmatchplay;
    }

    public void setListmatchplay(List<Matchplay> listmatchplay) {
        this.listmatchplay = listmatchplay;
    }

//public List<ECourseList2> listPlayedRounds() throws Exception{
//    // LOG.debug(" ... entering listPlayedRounds WITH formula = " + formula);
//    return new lists.PlayedList().list(appContext.getPlayer());
//}
    /*
public List<ECourseList> listPlayedRounds() throws SQLException {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    return playedList.list(appContext.getPlayer());
} // end method
*/


/* migrated on 2026-02-25 — now in Controller.refact.RoundController
    public List<ECourseList> registerStablefordResult() { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String to_selectMatchplayRounds_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String to_creditcard_test_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String to_stableford_playing_hcp_xhtml(String s) { ... }
*/
public String to_selectPlayer_xhtml(String s) {
            LOG.debug("entering to selectPlayer_xhtml... with string = " + s);
            sessionMap.put("inputSelectHomeClub", s); 
            reset(s);
       return "selectPlayer.xhtml?faces-redirect=true";
   }

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String to_selectScrambleRounds_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String to_selectRegisteredRounds_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String to_selectParticipantsRound_xhtml(String s) { ... }
*/

/* just for testing from menu TEST
public String to_select_tarifMembers_xhtml(Integer i) throws SQLException {
            club.setIdclub(i);
            tarifMember = new find.FindTarifMembersData().find(club,conn);
            reset(String.valueOf(i));
        return "cotisation.xhtml?faces-redirect=true";
   }
*/



/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public String to_select_inscription_xhtml(String s) throws Exception {
    try{
                LOG.debug("entering to_select_inscription_round ... with string = " + s);
            setFilteredInscriptions(null);
            reset(s);
       if(s.equals("TEST")){ 
            return "selectRound.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals(Round.GameType.STABLEFORD.toString()))
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals(Round.GameType.SCRAMBLE.toString()))   //  Ã  implémenter pu alors autre solution chacun s'inscrit isolÃ©ment et on regroupe ensuite ...
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("MATCHPLAY"))
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("ADMIN COMPETITION")) { 
            return "select_competition_admin.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("PLAYER COMPETITION")){ 
            return "select_competition_player.xhtml?faces-redirect=true&cmd=" + s;}
    //   if(s.equals("TEST")){ 
    //        return "select_competition_player.xhtml?faces-redirect=true&cmd=" + s;}

 return "playing formule not found";   
 } catch (Exception ex) {
            LOG.error("Exception in to_select_inscription_xhtml! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
    }
}
end migrated to_select_inscription_xhtml */

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public String to_select_round_xhtml(String s) throws Exception {
    try{
                LOG.debug("entering to_select_round_xhtml ... with string = " + s);
            setFilteredInscriptions(null);
            reset(s);
       if(s.equals("INSCRIPTION")){
              LOG.debug("handling inscription !");
           sessionMap.put("inputSelectRound", s);
              LOG.debug("sessionMap : inputSelectRound = " + sessionMap.get("inputSelectRound"));
           return "selectRound.xhtml?faces-redirect=true";}
   //    if(s.equals(Round.GameType.STABLEFORD.toString()))
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals(Round.GameType.SCRAMBLE.toString()))   //  Ã  implémenter pu alors autre solution chacun s'inscrit isolÃ©ment et on regroupe ensuite ...
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("MATCHPLAY"))
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("ADMIN COMPETITION")) { 
            return "select_competition_admin.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("PLAYER COMPETITION")){ 
            return "select_competition_player.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("TEST")){ 
            return "select_competition_player.xhtml?faces-redirect=true&cmd=" + s;}

 return "playing formule not found";
 } catch (Exception ex) {
            LOG.error("Exception in to_select_inscription_xhtml! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
    }
}
end migrated to_select_round_xhtml */

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
public String to_selectStablefordRounds_xhtml(String s) {
            LOG.debug("entering to_selectStablefordRounds_xhtml with string = " + s);
            reset(s);
  //          scoreStableford.setStatisticsList(null);
       return "selectStablefordRounds.xhtml?faces-redirect=true";
   }
end migrated to_selectStablefordRounds_xhtml */
/*
public String to_show_played_rounds_xhtml(String s){
       LOG.debug("entering to_show_played_rounds_xhtml with string s = " + s);
            reset(s);
            setInputPlayedRounds(s); // new 29/03/2016// sert à quoi ??
       return "show_played_rounds.xhtml?faces-redirect=true&cmd=" + s; // mod 28/03/2016
   }
*/
/*
public String to_competition_create_description_xhtml(String s){
        LOG.debug("entering to_competition ... with string = " + s);
     reset(s);
     sessionMap.put("inputSelectClub", s);
     return "competition_create_description.xhtml?faces-redirect=true&operation=" + s;
   }
*/
/*
public String to_club_xhtml(String s){
        LOG.debug("entering to_club_hxtml ... with string = " + s);
            reset(s);
       club.setCreateModify(true);  // gestion button dans club.xhtml
       return "club.xhtml?faces-redirect=true&operation=" + s;
   }
*/


/* enlevé 10-01-2026 suite séparation prod-test
public void to_selenium_createClub(String s) throws IOException{
      LOG.debug("entering to_selenium_createClub ... with string = " + s);
    reset(s);
    applicationMap.put("clubCreated", "not yet implemented");
    LOG.debug("applicationMap clubCreated = " + applicationMap.get("clubCreated"));
    new selenium.SeleniumClubTest().testing("club.xhtml");
 //   selenium.SeleniumClub().testing("club.xhtml"); // ne trouve pas si dans Test Packages
   // à modifier    
    }
*/
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String to_course_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String to_tee_xhtml(String s) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public String to_clubModify_xhtml(String s) { ... }
*/

/*
public String to_clubDelete_xhtml(String s){
        LOG.debug("entering to_clubDelete_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       sessionMap.put("inputSelectClub", s);
    //   if(s.equals("delete_club_improved")){
       if(s.equals("clubDelete")){    
           return "selectClubDelete.xhtml?faces-redirect=true";
       }
  // 02-04     course.setInputSelectCourse(s);
       
       return "deleteClubCourseTee.xhtml?faces-redirect=true";
   }
*/
/* dead code — commented Phase 3B 2026-02-25 — not referenced in any active XHTML
public String to_player_xhtml(String s){
         LOG.debug("entering to_player_xthml ... with string = " + s);
       reset(s);
       return "player.xhtml?faces-redirect=true";
   }
*/

// ✅ MIGRÉ vers PlayerController (playerC) - to_player_modify — 2026-02-25
/*
public void to_player_modify(String s){
         LOG.debug("entering to_player_modify ... with string = " + s);
  //     reset(s);
        createModifyPlayer = s;
 //      return "player_modify.xhtml?faces-redirect=true";
   }
*/
/*
public String to_show_handicap_whs_xhtml(String s){
        LOG.debug("entering to_show_handicap_whs ... with string = " + s);
            reset(s);
         return "show_handicap_whs.xhtml?faces-redirect=true";
   }
*/
/* dead code — commented Phase 3B 2026-02-25 — not referenced in any active XHTML
public String to_delete_player_xhtml(String s) throws Exception {
        LOG.debug("entering to_delete_player ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   }
*/

/* dead code — commented Phase 3B 2026-02-25 — not referenced in any active XHTML
public String to_delete_club_xhtml(String s) throws Exception {
            LOG.debug("entering to_delete_club ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_club.xhtml?faces-redirect=true";
   }
*/
// ✅ MIGRÉ vers PlayerController (playerC) - deleteCascadingPlayer — 2026-02-25
/*
  public void deleteCascadingPlayer() throws SQLException, Exception{
 try{

//    new delete.DeletePlayer().deletePlayerAndChilds(getDeletePlayer(),conn );
  //  if(ok)
  //  {
  //          LOG.debug("player created, next step = photo");
  //      setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
  //  }else{
        // error in create player
    // }
 }catch (Exception ex){
            String msg = "Exception in deletePlayer and childs " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player
*/ // end ✅ MIGRÉ - deleteCascadingPlayer

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String scorecard(ECourseList ecl) { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String show_scorecard() throws SQLException, Exception { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECourseList> ScoreCardList1EGA() throws SQLException, LCException, Exception { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<HandicapIndex> ScoreCardList1WHS() throws SQLException { ... }
*/


/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECourseList> ScoreCardList2() throws SQLException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECourseList> ScoreCardList3() throws SQLException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ScoreStableford.Score> ScoreCardList4() throws SQLException { ... }
*/

    public static boolean isShowButtonCreditCard() {
        return ShowButtonCreditCard;
    }

    public static void setShowButtonCreditCard(boolean ShowButtonCreditCard) { // sert à quoi ??
        CourseController.ShowButtonCreditCard = ShowButtonCreditCard;
    }
/*
public List<EPlayerPassword> listPlayers() throws SQLException {
//            LOG.debug("... entering listPlayers with conn = " + conn);
   try {
       return new lists.PlayersList().list(conn);
   } catch (Exception ex) {
            String msg = "Exception in CourseController.listPlayers() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
*/
/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public void listSubscriptionRenewal(String s) throws SQLException { ... }
*/
        
 
// ✅ MIGRÉ vers PlayerController (playerC) - validatePlayer — 2026-02-25
/*
 public void validatePlayer(){
     setNextPanelPlayer(true);
}
*/ // end ✅ MIGRÉ - validatePlayer

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String creditCardMail() throws Exception { ... }
*/
 /*
 public void createPlayer() {
    if (MIGRATION_PLAYER) { // 
        logLegacyAccess("createPlayer()");
    }
  ///  playerController.createPlayer(player, "A");  // "B" = batch
    
}

// appelé depuis la vue JSF
public void modifyPlayer() {
    Player playerToUpdate = appContext.getPlayer();
///    playerController.updatePlayer(playerToUpdate);
}
*/
 /*
public void createPlayer() throws SQLException, Exception{
 try{
     LOG.debug("entering createPlayer");
     LOG.debug("entering createPlayer with HandicapIndex = " + handicapIndex);
  // mod 11-04-2022
// a faire tester la valeur du résultat !!
//  completePlayerAddress();

    if(createPlayer.create(player, handicapIndex, "A")){// new 23-11-2020 "A" signifie avec Activation (non en batch)
            LOG.debug("player created, next step = photo");
        setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
    }else{
        // error in create player
        String msg = "FATAL error : new player not created !!";
        LOG.error(msg);
  //      showMessageFatal(msg);
    }
 }catch (Exception ex){
            String msg = "Exception in createPlayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
 
} //end method create player
*/

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String createLocalAdministrator() throws SQLException, Exception { ... }
*/
/*
public String createProfessional() {  // coming from professional.xhtml
 try{
     LOG.debug("entering createProfessional");
     LOG.debug(" for club = " + club);
   //  LOG.debug(" with localAdmin = " + localAdmin);
    LOG.debug(" with playerTemp = " + appContext.getPlayerTemp());
  //  LOG.debug(" with professional local courseC = " + professional); // normalement pas OK
    LOG.debug(" with professional appContext = " + appContext.getProfessional()); // normalement OK
     LOG.debug(" with player Temp = " + appContext.getPlayerTemp()); 
  // d'où vient le club ? toujours ok car dans courseC de include_club_selector
  // je pourrai supprimer professional version loacale ?? non utilisé ailleurs
  
  //professional.setProClubId(club.getIdclub());
  //professional.setProPlayerId(appContext.getPlayerTemp().getIdplayer()); // new 05-02-2026
  appContext.getProfessional().setProClubId(club.getIdclub());
  appContext.getProfessional().setProPlayerId(appContext.getPlayerTemp().getIdplayer()); // new 05-02-2026
  
  if(createProfessional.create(appContext.getProfessional())){
        String msg = " professional created !! = "; //+ professional;
        LOG.info(msg);
        showMessageInfo(msg);
    }else{
        String msg = "FATAL error : professional NOT created !!" + appContext.getProfessional();
        LOG.error(msg);
        showMessageFatal(msg);
    }
  return null;
}catch (Exception ex){
            String msg = "Exception in createProfessional" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method createLocalAdministrator
*/
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String createCompetitionDescription() throws SQLException, IOException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String beforeInscriptionCompetition(CompetitionDescription ec) throws SQLException, IOException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<String> competitionTimeStartList(String s) throws SQLException { ... }
*/


 /* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public String createInscriptionCompetition(ECompetition ec) throws SQLException, IOException, InstantiationException{
*/
 
/* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public String createRoundsCompetition(CompetitionDescription cd) throws SQLException, IOException, Exception {
*/

  /* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public String cancelInscriptionCompetition(ECompetition ec) throws Exception{
*/


// ✅ MIGRÉ vers ClubController (clubC) - createClub
/*
public String createClub(){
        LOG.debug("entering CreateClub");
try{
 // LOG.debug("with club = " + club.toString());
 //   if(new create.CreateClub().create(club, conn)){
        // changement : returns array with 1. true or false 2. the clubid created
    if(new create.CreateClub().create(club, conn)){

        LOG.debug("club created : we go to course !!");
        course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
        return "course.xhtml?faces-redirect=true";
    }
   return null;
 }catch (Exception ex){
        String msg = "Exception in createClub " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
  }
} // end method createClub// ✅ MIGRÉ vers ClubController (clubC) - createClub
/*
public String createClub(){
        LOG.debug("entering CreateClub");
try{
 // LOG.debug("with club = " + club.toString());
 //   if(new create.CreateClub().create(club, conn)){
        // changement : returns array with 1. true or false 2. the clubid created
    if(new create.CreateClub().create(club, conn)){

        LOG.debug("club created : we go to course !!");
        course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
        return "course.xhtml?faces-redirect=true";
    }
   return null;
 }catch (Exception ex){
        String msg = "Exception in createClub " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
  }
} // end method createClub
*/ // end ✅ MIGRÉ - createClub


// ✅ MIGRÉ vers ClubController (clubC) - addCourse — 2026-02-25
/*
public String addCourse(ECourseList ecl){
 try{
      LOG.debug("entering addCourse");
         club = new read.ReadClub().read(ecl.club());
         LOG.debug("adding a course for idclub = " + ecl.club().getIdclub() + club.getClubName());
        course.setCreateModify(true);
         course = new Course();
        return "course.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method addCourse
*/ // end ✅ MIGRÉ - addCourse

// ✅ MIGRÉ vers ClubController (clubC) - addTee — 2026-02-25
/*
public String addTee(ECourseList ecl){
 try{
      LOG.debug("entering addTee");
         club = new read.ReadClub().read(ecl.club());
            LOG.debug("club handled is " + club.getIdclub() + " : " + club.getClubName());
         course = new read.ReadCourse().read(ecl.course());
            LOG.debug("course handled is " + course.getIdcourse() + " : " + course.getCourseName());
         LOG.debug("tee to be added = " + ecl.tee());
         tee.setCreateModify(true);
         tee = new Tee();
        return "tee.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addTee" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method addTee
*/ // end ✅ MIGRÉ - addTee


// ✅ MIGRÉ vers ClubController (clubC) - deleteClub
/*
public String deleteClub(ECourseList2 ecl){ 
 try{
     LOG.debug("entering deleteClub for " + ecl.club()); //Eclub.getIdclub());
       boolean OK = new delete.DeleteClub().delete(ecl.club(), conn);
            LOG.debug(" result of deleteClub = " + OK);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteClub" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteClub
*/ // end ✅ MIGRÉ - deleteClub



// ✅ MIGRÉ vers ClubController (clubC) - deleteCourse
/*
public String deleteCourse(ECourseList2 ecl){
 try{
        LOG.debug("entering deleteCourse for " + ecl.course());
     boolean OK = new delete.DeleteCourse().delete(ecl.course(), conn); //.deleteHoles(ecl.Etee.getIdtee(), conn);
            LOG.debug(" result of deleteCourse = " + OK);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteCourse" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteCourse
*/ // end ✅ MIGRÉ - deleteCourse



// ✅ MIGRÉ vers ClubController (clubC) - deleteTee
/*
public String deleteTee(ECourseList2 ecl){
 try{
            LOG.debug("entering deleteTee for " + ecl.tee());
    //    delete.DeleteTee dt = new delete.DeleteTee();
        boolean OK = new delete.DeleteTee().delete(ecl.tee(), conn);
            LOG.debug(" result of deleteTee = " + OK);
        lists.CourseList.setListe(null);// reset
        listCourses(); // refresh list without the deleted item    
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (SQLException ex){
            String msg = "Exception in deleteTee" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteTee
*/ // end ✅ MIGRÉ - deleteTee


/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public String deleteTarifMember(){
}
*/


// ✅ MIGRÉ vers ClubController (clubC) - deleteHoles
/*
public String deleteHoles(ECourseList2 ecl){ 
 try{
        LOG.debug("entering deleteHoles for Tee = " + ecl.tee());
      boolean OK = new delete.DeleteHoles().delete(ecl.tee(), conn);
        LOG.debug(" result of deleteHoles = " + OK);
    // refresh list without the deleted item   
      lists.CourseList.setListe(null);// reset
      listCourses(); 
     return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteHoles" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteHoles
*/ // end ✅ MIGRÉ - deleteHoles


    public Creditcard getCreditcard() {
        return creditcard;
    }

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    }

// ✅ MIGRÉ vers ClubController (clubC) - loadCourse — 2026-02-25
/*
public String loadCourse(ECourseList ecl) throws SQLException, Exception{
 try{
       LOG.debug("entering loadCourse");
     course = new read.ReadCourse().read(ecl.course());
     club = new read.ReadClub().read(ecl.club());
    if(course != null){
        course.setCreateModify(false);
        return "course.xhtml?faces-redirect=true&operation=modify";
    }else{
        return null;
    }
 }catch (SQLException ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadCourse
*/ // end ✅ MIGRÉ - loadCourse
/* enlevé 10-01-2026 suite séparation prod-test 
public void screenShot(String view){
try{  
    LOG.debug("we take a screenShot for view = " + view);
 // faut avoir    http://localhost:8080/GolfWfly-1.0-SNAPSHOT/technical_info.xhtml
///     String url = utils.LCUtil.firstPartUrl()+ FacesContext.getCurrentInstance().getViewRoot().getViewId();
 ///       LOG.debug("ScreenShot url = " + url);
//    ScreenShotCaptureSelenium screenShot = new ScreenShotCaptureSelenium(0, null);
//     selenium.ScreenShotCaptureSelenium screenShot = new ScreenShotCaptureSelenium();
 //    SeleniumController screenShot = new SeleniumController(); //.capture(view);
   //  screenShot.TakeRobotScreenshot();
     if(SeleniumController.capture(view)){
         LOG.debug("ScreenShot taken ! OK when back in courseC ");
         // envoi mail
 //        screenShot.tearDown(driver);
     }else{
         LOG.debug("error screenshot when back in courseC ");
     }
     
   
 }catch (Exception ex){
        String msg = "Exception in screenShot " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
  }
} //end method 
*/

// ✅ MIGRÉ vers ClubController (clubC) - modifyCourse
/*
public String modifyCourse() throws Exception {
        LOG.debug("entering modifyCourse  "); 
        LOG.debug("course to be modified = " + course.toString());
    if(new update.UpdateCourse().update(course, conn)) {
        String msg = "course Modified !! ";
        LOG.info(msg);
        showMessageInfo(msg);
    }
 return null;
    } // end modifyCourse
*/ // end ✅ MIGRÉ - modifyCourse


// ✅ MIGRÉ vers ClubController (clubC) - loadTee — 2026-02-25
/*
public String loadTee(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadTee");
     tee = new read.ReadTee().read(ecl.tee());
     course = new read.ReadCourse().read(ecl.course());
     club = new read.ReadClub().read(ecl.club());
    if(tee != null){
        tee.setCreateModify(false);
        return "tee.xhtml?faces-redirect=true&operation=modify";
    }else{
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadTee " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadTee
*/ // end ✅ MIGRÉ - loadTee

// ✅ MIGRÉ vers ClubController (clubC) - loadHoles — 2026-02-25
/*
public String loadHoles(ECourseList ecl, String type) throws SQLException, Exception{
 try{
        LOG.debug("entering loadHoles - multiple");
     tee = new read.ReadTee().read(ecl.tee());
      holesGlobal = new read.ReadHole().read(tee);
       course = new read.ReadCourse().read(ecl.course());
      club = new read.ReadClub().read(ecl.club());
      hole.setCreateModify(false);
      if (type.equals("global")){
          return "modify_holes_global.xhtml?faces-redirect=true&operation=modify holes Global";
      }else{
          return "modify_holes_distance.xhtml?faces-redirect=true&operation=modify holes Distance";
      }
 }catch (Exception ex){
            String msg = "Exception in loadHoles " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadHoles
*/ // end ✅ MIGRÉ - loadHoles


// ✅ MIGRÉ vers ClubController (clubC) - createHolesGlobal
/*
 public String createHolesGlobal(String param) throws Exception{
   try{
        LOG.debug("entering createHolesGlobal  "); 
        LOG.debug("with param = " + param); // global or distance
        LOG.debug("for club = " + club);
        holesGlobal.setType(param);
     if(createOrUpdateHolesGlobal.status(holesGlobal, tee, course)){
         String msg =  LCUtil.prepareMessageBean("hole.global.create");
         LOG.info(msg);
         showMessageInfo(msg);
     }else{
         String msg = "FAILURE Create Holes Global !! ";
         LOG.error(msg);
         showMessageFatal(msg);
     }
 return null;
 
  }catch (Exception ex){
            String msg = "Exception in createHolesGlobal " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
 } // end method createHolesGlobal
*/ // end ✅ MIGRÉ - createHolesGlobal

 
// ✅ MIGRÉ vers ClubController (clubC) - viewHolesGlobal — 2026-02-25
/*
  public String viewHolesGlobal() throws Exception{
        LOG.debug("entering viewHolesGlobal  ");
       tee.setTeeHolesPlayed("01-18");
 return "modify_holes_global.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
    } // end modifyHolesGlobal
*/ // end ✅ MIGRÉ - viewHolesGlobal
 
// ✅ MIGRÉ vers ClubController (clubC) - loadHole — 2026-02-25
/*
public String loadHole(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadHole");
     tee = new read.ReadTee().read(ecl.tee());
     course = new read.ReadCourse().read(ecl.course());
     club = new read.ReadClub().read(ecl.club());
    if(tee != null){
        tee.setCreateModify(false);
        return "hole.xhtml?faces-redirect=true&operation=modify hole";
    }else{
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadHole
*/ // end ✅ MIGRÉ - loadHole



// ✅ MIGRÉ vers ClubController (clubC) - modifyTee
/*
 public String modifyTee() throws Exception { 
        LOG.debug("entering modifyTee  "); 
        LOG.debug("tee to be modified = " + tee.toString());
    if(new update.UpdateTee().update(tee, conn)){
        tee.setNextTee(true); // affiche le bouton next(Course) bas ecran a droite
        LOG.debug("tee Modified !!");
    }
 return null;
    } // end modifyTee
*/ // end ✅ MIGRÉ - modifyTee


// ✅ MIGRÉ vers ClubController (clubC) - loadClub — 2026-02-25
/*
public String loadClub(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadClub");
    club = new read.ReadClub().read(ecl.club());
    if(club != null){
        club.setCreateModify(false);
        return "club.xhtml?faces-redirect=true&operation=modify";
    }else{
        String msg = "error : club not retrieved !!";
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadClub " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadClub
*/ // end ✅ MIGRÉ - loadClub


// ✅ MIGRÉ vers ClubController (clubC) - modifyClub
/*
  public String modifyClub() throws Exception { 
      //modify club from modifyClubCourseTee.xhtml
        LOG.debug("entering modifyClub  ");
        LOG.debug("club to be modified = " + club.toString());
    if(new update.UpdateClub().update(club, conn)){
        course.setNextCourse(false); // n'affiche PAS le bouton next(Course) bas ecran a droite
        LOG.debug("club is Modified !!");
    }
 return null;
    } // end modifyClub
*/ // end ✅ MIGRÉ - modifyClub


// ✅ MIGRÉ vers ClubController (clubC) - modifyClubUnavailableStructure — 2026-02-25
/*
public String modifyClubUnavailableStructure(String type) throws Exception {
   LOG.debug("entering modifyClubUnavailableStructure  for club = " + club);
     if(type.equals("delete")){
         unavailable.structure().setStructureList(null);
     }
     if(unavailableController.updateClub(unavailable, club)){
         unavailable.structure().setStructureExists(true);
     }
      return null;
} // end modifyClubUnavailableStructure
*/ // end ✅ MIGRÉ - modifyClubUnavailableStructure
/*
public void modifyPlayer() throws SQLException, Exception{
 try{
        LOG.debug("entering modifyPlayer");
    if(new update.UpdatePlayer().update(player, conn)){
            LOG.debug("player modified, next step = photo");
        setNextPlayer(true); // affiche le bouton next(photo) bas ecran à  droite
    //    setCreateModifyPlayer("M");
        createModifyPlayer = "M";  // c'est trop tard !
    }else{
        // error in create player
        LOG.debug("FATAL error in modify player ");
        
    }
 }catch (Exception ex){
            String msg = "Exception in modifyPlayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player
*/
// enlevé 29-11-2018 à réécrire
 /* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String listParticipants_mp(Matchplay mp) throws SQLException { ... }
*/

 /* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String listParticipantsStablefordRound(ECourseList ecl) throws SQLException { ... }
*/

 
  /* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECourseList> listParticipantsStablefordCompetition(CompetitionDescription cde) throws SQLException { ... }
*/
 
  
 /* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<EMatchplayResult> calcMatchplayResult() throws SQLException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC) as listCoursesPublic()
 public List<ECourseList> listCourses() throws SQLException{ ... }
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public List<Club> listLocalAdminClubsList() throws SQLException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.MemberController (memC)
   public List<ECourseList> listLocalAdminCoursesList(String select, String admin) throws SQLException { ... }
*/

   /*  migré vers clubController 19-02-206
   public List<Club> listClubsDialog(String type) { // mod 02-02-2026 non testé coming from dialogClub.xhtml
    try {
    
        // Cas par défaut - tous les clubs (fonction ADMIN)
        LOG.debug("Returning all clubs (default case)");
        return new lists.ClubList().list();
        
    } catch (Exception ex) {
        String msg = "Exception in listClubsDialog(): " + ex.getMessage();
        LOG.error(msg, ex);
        showMessageFatal(msg);
        return null;
    }
}
   
   /*   //   LOG.debug("entering listClubsDialog");
     //   LOG.debug("with type = " + type);
        String inputSelectClub = (String) sessionMap.get("inputSelectClub");
      LOG.debug("inputSelectClub = {}", inputSelectClub);
        
        // Cas où inputSelectClub est null ou nécessite tous les clubs
        if (inputSelectClub == null || 
            "PaymentCotisationSpontaneous".equals(inputSelectClub)) {
            LOG.debug("Returning all clubs (inputSelectClub: {})", inputSelectClub);
            return new lists.ClubList().list(conn);
        }
        
        // Cas spécifique CreatePro - clubs gérés par l'administrateur local
        if ("CreatePro".equals(inputSelectClub)) {
            LOG.debug("Returning clubs managed by local admin");
            return new lists.ClubsListLocalAdmin().list(player, conn);
        }
        */
   
   
   
   /*
 public List<Club> listClubsDialog(){ // called by dialogClub.xhtml et aussi selectClub.xhtml
  try {
        LOG.debug("entering listClubsDialog");
        LOG.debug("with inputSelectClub = " + sessionMap.get("inputSelectClub"));
    if(sessionMap.get("inputSelectClub")== null){ // cas si create_player
        LOG.debug("inputSelectClub is null");
     return new lists.ClubList().list(conn); // tous les clubs !
    }
  // new 24-08-2025  
    if(sessionMap.get("inputSelectClub").equals("PaymentCotisationSpontaneous")){
          LOG.debug("sessionmap is PaymentCotisationSpontaneous");
       return new lists.ClubList().list(conn); // tous les clubs !
    }
    
    if(sessionMap.get("inputSelectClub").equals("CreatePro")){
       LOG.debug("sessionmap is CreatePro");
     return new lists.ClubsListLocalAdmin().list(player, conn); // car only clubs gérés LA
    }else{
         LOG.debug("sessionmap is NOT CreatePro");
     return new lists.ClubList().list(conn); // tous les clubs ! car fonction ADMIN
    } 
      
 //   if(sessionMap.get("inputSelectClub").equals("LocalAdministrator")){
 //         LOG.debug("sessionmap is LocalAdministrator");
  //      return new lists.ClubList().list(conn); // tous les clubs ! car fonction ADMIN
 //   }
  
 // old     if(param.equals("local_administrator")){  // mod 12-12-2023
//        return new lists.ClubsListLocalAdmin().list(player, conn);
       // 
       // autre approche !!
      //}
 //   return null;
    } catch (Exception ex) {
            String msg = "Exception in listClubs() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
 } //end method
 */
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public List<Course> listCoursesForClub(String clubid) throws SQLException { ... }
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
      public List<ECourseList> listDetailClub(String id) throws SQLException{ ... }
*/
 /* migrée 19-02-2026    
 public List<ECourseList2> listClubsCoursesTees(String param) throws SQLException {
   try {
       LOG.debug(" -- entering listClubsCoursesTee with param = " + param);
       LOG.debug(" with club = " + club.getIdclub());
 //      if(param.equals("all_clubs")){
 //          return new lists.ClubCourseTeeListAll().list(conn);
 //      }
       if(param.equals("one_club")){
           return new lists.ClubCourseTeeListOne().list(club);
       }
       // refresh screen 
} catch (Exception ex) {
            String msg = "Exception in listClubsCoursesTees() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
} finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
  //    return null;
        }
   return null;
} //end method
 */
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECourseList> listInscriptions() throws SQLException { ... }
*/
  

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<CompetitionDescription> listCompetitions() { ... }
*/
       
 
/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String beforeListInscriptionsCompetition(CompetitionDescription cd) throws SQLException, IOException { ... }
*/ 

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String beforeCompetitionMenu(CompetitionDescription ec) throws SQLException, IOException { ... }
*/ 

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECompetition> listInscriptionsCompetition() throws SQLException, IOException { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public String beforeListStartCompetition(CompetitionDescription cd, String type_exec) throws SQLException, IOException { ... }
*/ 

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
   public List<ECompetition> listStartCompetition() throws SQLException, IOException, Exception { ... }
*/

  /* migrated on 2026-02-24 — now in Controller.refact.RoundController
  public String cancelInscription(ECourseList ecl) throws Exception {
  try{
      LOG.debug(" starting cancelInscription ");
      // à tester
      LOG.debug(" with ecl = " + ecl.toString());
    //  setPlayer2(ecl.player());
    //  appContext.setPlayerTemp(ecl.getPlayer());
      setRound(ecl.round());
      /// controle ne devait canceler que ses propres rounds !!!
      LOG.debug("current player = " + appContext.getPlayer().getIdplayer());
      LOG.debug("current player ROLE = " + appContext.getPlayer().getPlayerRole());
      LOG.debug("cancellation for player = " + ecl.getPlayer().getIdplayer());
      LOG.debug("cancellation for player ROLE = " + ecl.getPlayer().getPlayerRole());
      // new 04-11-2018
      if((! appContext.getPlayer().getPlayerRole().equals("ADMIN"))
           //   && (appContext.getPlayer().getIdplayer().intValue() != player2.getIdplayer().intValue())){  // mod 01-12-018 player 2 et ||
              && (appContext.getPlayer().getIdplayer().intValue() !=ecl.getPlayer().getIdplayer().intValue())){  // mod 01-12-018 player 2 et || 
          
          String msgerr =  LCUtil.prepareMessageBean("cancel.inscription");
        
          msgerr = msgerr + appContext.getPlayer().getIdplayer() + " /\\ " + ecl.getPlayer().getIdplayer() + " /\\ " + appContext.getPlayer().getPlayerRole();
          LOG.error(msgerr);
          showMessageFatal(msgerr);
          throw new Exception(msgerr);
      }

      //   delete.DeleteInscription di = new delete.DeleteInscription();
      deleteInscription.delete(ecl.getPlayer(), round, ecl.club(), ecl.course()); // migrated 2026-02-24
      participantsRoundList.invalidateCache();                          // migrated 2026-02-24
      listStableford = participantsRoundList.list(round);              // migrated 2026-02-24
       //new 01-12-2018/
     //  String s1 = utils.LCUtil.fillRoundPlayersStringEcl(listStableford);
        String s1 = Round.fillRoundPlayersStringEcl(listStableford);
       round.setPlayersString(s1);
         LOG.debug("PlayersString is now " + round.getPlayersString());
       // compteur aussi !!  
  //      short sh8 = round.getRoundPlayers();
   //     round.setRoundPlayers(--sh8);
   //          LOG.debug("RoundPlayers is now " +  round.getRoundPlayers());
  //    return null;
   return "show_participants_stableford.xhtml?faces-redirect=true";  // refresh view without message !
  //    return null;  // back to originating view   ne refresh view but with message !
  
    } catch (Exception ex) {
            String msg = "Exception in cancelInscription() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
  } //end method
  */

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public List<ECourseList> listHandicaps() throws SQLException { ... }
*/
/* dead code — XHTML already uses playerC.listHandicapWHS()
   public List<ECourseList> listHandicapWHS() throws SQLException { ... }
*/
  
  
    /* à réécrire enlevé 29-11-2018
  public String cancelHandicap(EcourseList ccr) throws Exception{
      LOG.debug(" starting cancelHandicap ");
      LOG.debug(" with CCR = " + ccr.toString());
      LOG.debug(" for player Id = " + ccr.getIdplayer());
      LOG.debug(" for player Last Name = " + ccr.getPlayerLastName());
      LOG.debug(" for handicap = " + ccr.getHandicapStart());
      
///      delete.DeleteHandicap.deleteHandicap(ccr.getIdplayer(), ccr.getIdhandicap(), conn);
      
      lists.HandicapList.setListe(null);  // reset
      listHandicaps();  // refresh list without the deleted item
    return "show_handicap_whs.xhtml?faces-redirect=true";  // refresh view without message !
  }
  */
  
  
  /* migrated on 2026-02-24 — now in Controller.refact.RoundController
  public String cancelRound(ECourseList ecl) throws Exception{
        LOG.debug(" starting cancelRound ");
        LOG.debug(" with ecl = " + ecl.toString());
      boolean OK = deleteRound.delete(ecl.round()); // migrated 2026-02-24
        LOG.debug(" result of deleteHoles = " + OK);
   //   lists.InscriptionList.setListe(null);  // reset
      inscriptionList.invalidateCache(); // new 223-02-2026
      listInscriptions();  // refresh list without the deleted item
    return "selectInscription.xhtml?faces-redirect=true";  // refresh view without message !
  }
  */

    public Map<String, Object> getApplicationMap() {
        return applicationMap;
    }

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String selectPlayer(EPlayerPassword epp) throws SQLException { ... }
*/
 
/* dead code — duplicate of playerC.getProfessionals()
   public List<Professional> listProfessional() { ... }
*/
  
  
/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String passwordVerification(String OK_KO) { ... }
*/

public String findSun() throws SQLException, IOException{
    // ajouter boolean = correct insert !!!

    LocalDate today = LocalDate.now();
 //   LOG.debug("string returned in findSun = " + r);
    return "fake";
  //  if(ok)
  //  {
   //     LOG.debug("club created : we go to course !!");
    //    course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
    }

/* migrated on 2026-02-25 — now in Controller.refact.MemberController
public boolean modifySubscription() throws Exception{
}
*/
/*
public static void beforePreparePaymentLesson(ScheduleModel model, Professional pro) throws Exception{ // lesson et professional
    // faire le move vers next
    // vient de ScheduleProController
  LOG.debug("entering beforePreparePaymentLesson");
  LOG.debug("with event = " + model);
  LOG.debug("with Lesson = " + pro);
//  professional = pro;
}
*/
/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String manageCotisation() throws Exception { ... }
*/
  
 
/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String manageLesson() throws Exception { ... }
*/
 
/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String manageGreenfee() { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String testWebServiceHttp() { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public void testWebService() { ... }
*/   


 
/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public String manageSubscription() throws Exception { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public String registereIDPlayer() { ... }
*/

/* migrated on 2026-02-25 -- now in Controller.refact.RoundController (roundC)
   public void onRowToggleCompetition(ToggleEvent event) { // competition
*/

// ✅ MIGRÉ vers PlayerController (playerC) - rowPlayerSelect — 2026-02-25
/*
public void rowPlayerSelect(SelectEvent<Object> event) {
        LOG.debug("entering rowPlayerSelect");
        LOG.debug("event = " + event.getObject().toString());
        String msg = "size selected players = " + appContext.getPlayer().getDraggedPlayers().size();
        LOG.debug(msg);
        if (appContext.getPlayer().getDraggedPlayers().size() > 4) {
            msg = "You can't select more than 4 players";
            LOG.error(msg);
            showMessageFatal(msg);
        }
    }
*/ // end ✅ MIGRÉ - rowPlayerSelect

public void loginAPI(){
    LOG.debug("entering loginAPI coming from login_securityAPI.xhtml");
    LOG.debug("username = " + login.getUsername());
    LOG.debug("password = " + login.getPassword());
  }

/*
public String login() { // n'est plus exécuté ?? executed via actionView in login.xhtml
            LOG.debug("entering login() coming from login.xhtml");
        listeners.ImplHttpSessionListener msc = new listeners.ImplHttpSessionListener(); 
            LOG.debug("active sessions at this moment= " + msc.getActiveSessions());
            LOG.debug("browser language = " + externalContext.getRequestLocale());
            LOG.debug("session buffersize = " + externalContext.getResponseBufferSize());
            LOG.debug("session applicationcontextpath = " + externalContext.getApplicationContextPath());
     //       LOG.debug("session character encoding = " + ec.getRequestCharacterEncoding());
            LOG.debug("session timeout = " + externalContext.getSessionMaxInactiveInterval() + " seconds");
        reset("from login");
  //      player = new Player(); // new 28/09/2014
  //          LOG.debug("from login : new player !!");
 
       sessionMap.put("playerid","");
       sessionMap.put("playerlastname", "");
       sessionMap.put("playerage", 0);
       sessionMap.put("creditcardType", "INITIALIZED");
       sessionMap.put("inputSelectHomeClub", "login"); 
     return null;   // Reste sur login.xhtml (pas de redirect)
    } // end method
*/
public String logout(String lgt){
    try{
            LOG.debug("entering logout() for player = " + appContext.getPlayer().getIdplayer());
            LOG.debug("entering logout with parameter = " + lgt);
    //    player.setShowMenu(false); // ✅ Désactive le menu
        appContext.getPlayer().setShowMenu(true);
        if(appContext.getPlayer().getIdplayer()!= null){
            Audit a = new Audit();
            a.setAuditPlayerId(appContext.getPlayer().getIdplayer());
            // a = new find.FindLastAudit().find(a, conn);
            a = findLastAudit.find(a); // migrated 2026-02-26
            if(a != null){
                String msg = "ending an audit which started at : " + a.getAuditStartDate().format(ZDF_TIME);
                LOG.debug(msg);
                showMessageInfo(msg);
                boolean ok = updateAudit.stop(a);
            }
          }
        reset("from logout");
   //               LOG.debug("new player started !");
  // enlevé 16-03-2020      player = new Player(); // new 28/09/2014
            LOG.debug("this session will be invalidated : " + externalContext.getSessionId(true));
           externalContext.invalidateSession();
  //   FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
  if(lgt != null){
    if(lgt.equals("from button Logout")){
          String msg = "You asked a logout from the Logout button";
          LOG.info(msg);
          showMessageInfo(msg);
          return "login.xhtml?faces-redirect=true";
    }
    if (lgt.equals("Inactive Interval from masterTemplate")){
          String msg = "Inactive Interval from masterTemplate - Time-out for inactivity from masterTemplate!";
          LOG.debug(msg);
          showMessageInfo(msg);
      //    return "login.xhtml?faces-redirect=true"; // mod 31-08-2025
         return "session_expired.xhtml?faces-redirect=true";
    }else{
        LOG.debug("unknown logout message : " + lgt);
        return null;
    }
 //   player = null;
  }else{
      LOG.debug("lgt is null " + lgt);
      return null;
  }
  }catch (Exception ex){
            String msg = "Exception in logout() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  } 
} // end method
    
public String onFlowProcess(FlowEvent event) {
        LOG.debug("Current wizard step:" + event.getOldStep());
        LOG.debug("Next wizard step:" + event.getNewStep());
        if(skip){
            skip = false;//reset in case user goes back
            return "confirm";
        }else{
            return event.getNewStep();
        }
    } //end method

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public void savePlayer(ActionEvent actionEvent) { ... }
*/

    public static boolean isPostback() {
        return FacesContext.getCurrentInstance().isPostback();
    }

/* migrated on 2026-02-25 — now in Controller.refact.ClubController (clubC)
   public void preRenderClub() { ... }
*/

    public void preRenderCourse() {
        LOG.debug("preRenderCourse called");
    }

/* migrated on 2026-02-25 — now in Controller.refact.RoundController (roundC)
  public List<String> teeStartList(Player otherPlayer) throws SQLException {
            return findTeeStart.find(course, otherPlayer, round);
    } //end method
*/ // end ✅ MIGRÉ - teeStartList
 
 public void viewChartPlayedRound() {
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("contentHeight", 320);
    options.put("contentWidth", 640);  //default
    options.put("closable", true); // default
    options.put("header", "header by LC"); // new 30/08/2014 correct ?
//    RequestContext.getCurrentInstance().openDialog("viewChartRounds", options, null); // ie xx.xhtml
    PrimeFaces.current().dialog().openDynamic("viewChartRounds", options, null); 
}

public void checkCaptcha(ActionEvent e){ 
       FacesContext.getCurrentInstance().addMessage(null, 
       new FacesMessage(FacesMessage.SEVERITY_INFO, "Your Captcha Code Is Correct !",null)); 
} 

public static class TeeStart{
	public String teeLabel;
	public String teeValue;
 
public TeeStart(String teeLabel, String teeValue){
            this.teeLabel = teeLabel;
            this.teeValue = teeValue;}
 
public String getTeeLabel(){
	return teeLabel;}
 
public String getTeeValue(){
	return teeValue;}
 	} 
private TeeStart[] teeList;

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public void onCompletePayment() { ... }
*/

/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public jakarta.ws.rs.core.Response handlePayments(...) { ... }
*/



/* migrated on 2026-02-25 — now in Controller.refact.PaymentController (payC)
   public void onStart() { ... }
   public void onProgress() { ... }
   public Integer getProgress1() { ... }
   public void setProgress1(Integer progress1) { ... }
   private Integer updateProgress(Integer progress) { ... }
   public void cancelProgress() { ... }
   public List<Lesson> getListLessons() { ... }
   public void setListLessons(List<Lesson> listLessons) { ... }
*/
  /* migré 23-02-2026
  public void textCalculationIndex() throws Exception {
  try{  
           LOG.debug("entering textCalculationIndex"); // called from dialog_handicap_index.xhtml
           LOG.debug("selected handicapIndex = " + appContext.getHandicapIndex());
       //    LOG.debug("selected roundid = " + appContext.getHandicapIndex().getSelectedHandicap().round().getIdround());
           LOG.debug("selected roundid = " + appContext.getSelectedHandicap().round().getIdround());
           LOG.debug("current playerid = "+ appContext.getPlayer().getIdplayer());
        LoggingUser logging = new LoggingUser();
        logging.setLoggingIdPlayer(appContext.getPlayer().getIdplayer());
        logging.setLoggingIdRound(appContext.getSelectedHandicap().getRound().getIdround());
        logging.setLoggingType("H"); // Handicap
         appContext.getHandicapIndex().setCalculations(new Controllers.MongoCalculationsController().read(logging));
   //           LOG.debug("Handicap Calculations text = " + appContext.getHandicapIndex().getCalculations());
  }catch (Exception ex){
            String msg = "Exception in textCalculationIndex " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  }
 } //end method  
 */
  public void textCalculationRound() throws Exception { 
  try{  
        LOG.debug("entering textCalculationRound"); // called from dialog_played_rounds.xhtml
        //       <f:viewAction action="#{courseC.textCalculationRound()}" />
        ECourseList selectedRound = appContext.getSelectedPlayedRound(); // migrated 2026-02-25 — was local field
        LOG.debug("selected roundid = " + selectedRound.round().getIdround());
        LOG.debug("current playerid = "+ appContext.getPlayer().getIdplayer());
    LoggingUser logging = new LoggingUser();
    logging.setLoggingIdPlayer(appContext.getPlayer().getIdplayer());
    logging.setLoggingIdRound(selectedRound.round().getIdround());
    logging.setLoggingType("R"); // Round
       LOG.debug("logging_user = " + logging);
// mod 16/08/2022 migration vers mongoDB
     // new Controllers.MongoCalculationsController().read(logging)
     round.setCalculations(mongoCalculationsController.read(logging)); // migrated 2026-02-26
    
 //          LOG.debug("Calculations text = " + round.getCalculations());
  }catch (Exception ex){
            String msg = "Exception in textCalculationRound " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  }  
 } //end method  

 public void SendEmailTest(){
 try{
     
  long start = System.nanoTime();
    String content = "Ceci est le texte du mail <b> gras </b>"
        + "</br> really new line ?"
        + "</br> now italic : " 
        + " </br> now <i> italiques </i>";
   String title = "Ceci est le sujet du mail, louis";
   String recipient = "louis.collet@skynet.be,louis.collet.onduty@gmail.com"; // comma = separator
   // a faire : envoi QRC
   
   // sera fait dans l'envoi du mail  byte[] qrContent = qrService.generateQR(content, 200);  // because "this.qrService" is null
     String qrContent = "</br>this is the start of the content" + content + "</br>this is the end of the content";
   //          LOG.debug("reponse de qrService = " + pathQRC.toString()); 
             
   // à faire : envoi 
    ///enlevé 09-02-2026  Player player = new Player();
      appContext.getPlayer().setIdplayer(456783);  // muntingh
    //  appContext.getPlayer() = new read.ReadPlayer().read(appContext.getPlayer(), conn);
    // modifié 12-02-2026 non testé !!!
   //      playerC.setPlayer(new read.ReadPlayer().read(appContext.getPlayer(), conn));
      
         // si tu as juste l'id du player
     // playerC.readPlayer(appContext.getPlayer().getIdplayer());
      playerManager.readPlayer(appContext.getPlayer().getIdplayer());
         // à vérifier 
     // player.setPlayerLastName("Muntingh");
     // player.setPlayerLanguage("fr");
     // player.setPlayerEmail("theo.muntingh@skynet.be");
      Player player2 = new Player();
      player2.setIdplayer(2014101);  // muntingh
      Player player3 = new Player();
      player3.setIdplayer(2014102);  
      ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
      p.add(player2);
      p.add(player3);
      appContext.getPlayer().setDroppedPlayers(p);
 
      Player invitedBy = new Player();
      invitedBy.setIdplayer(324713);
      appContext.getPlayer().setPlayerLastName("Collet");

      Club club = new Club();
      club.setIdclub(101);  //rigenée
      // club = new read.ReadClub().read(club);
      club = readClubService.read(club); // migrated 2026-02-26
         LOG.debug("club = " + club);
      Course course = new Course();
      Round round = new Round(); 
      round.setRoundDate(LocalDateTime.of(2025, Month.NOVEMBER, 17, 12, 15));
      round.setRoundGame("round game : STABLEFORD");
      round.setPlayersString("inscrits précédemment : Corstjens, Bauer");
   
     // byte[] icsAttachment = new utils.IcalService().generateIcs(player, invitedBy, round, club, course, true);
       byte[] icsAttachment = icalService.generateIcs(appContext.getPlayer(), invitedBy, round, club, course, true);  // mod 31-12-2025
      showMessageInfo("after icalService");
         LOG.debug("after generation of icsAttachmentvia icalService = " + icsAttachment);
    //  boolean b = new mail.EmailService().sendHtmlMail(title, content, recipient, icsAttachment ,qrContent, "es");
    
    //  fonctionne boolean b = mailService.sendHtmlMail(title, content, recipient, icsAttachment ,qrContent, "es");
    //    LOG.debug("boolean result = " + b);
    //    showMessageInfo("after mailService");
        
         
        
        // fonctionne  = new mail.MailSender().sendHtmlMail(title, content, recipient, icsAttachment ,null, "es");
        //LOG.debug("boolean result mail.MailSender().sendHtmlMail= " + b);
      //  CompletableFuture cf = new mail.MailSender().sendHtmlMailAsync(title, content, recipient, icsAttachment ,null, "es");
        
      

    //    PrimeFaces.current().ajax().update("growl-msg"); // rafraîchir le composant growl
      //  PrimeFaces.current().executeScript("window.scrollTo(0,0);"); // scroll top remonte la page après la fin de l’envoi.
        showMessageInfo("show MessageInfo - Envoi du mail, Le mail est en cours d’envoi à " + recipient);
        
        CompletableFuture<Void> cf =
           mailSender.sendHtmlMailAsync(
        title,
        content,
        recipient,
        icsAttachment,
        null,
        "es"
    );

    cf.orTimeout(300, TimeUnit.SECONDS)
        .whenComplete((r, ex) -> {
            if (ex == null) {
              LOG.info("Mail envoyé avec succès à {}", recipient);
              // showMessageInfo("show MessageInfo - Envoi du mail, Le mail a été envoyé avec succès à " + recipient);
               PrimeFaces.current().dialog().showMessageDynamic(
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Mail envoyé !")
            );
               showDialogInfo("Mail envoyé avec succès");
            }else{
               Throwable cause = ex instanceof CompletionException && ex.getCause() != null
                            ? ex.getCause()
                            : ex;
               LOG.error("Mail KO pour {} (timeout ou erreur)", recipient, cause);
            }
        });
 
      LOG.debug("Mail submission for {} done (async)", recipient);
      LOG.debug("CompletableFuture result after Async= " + cf);
        
        showMessageInfo("after MailSender");
     long elapsedNanos = System.nanoTime() - start;
      LOG.debug("Elapsed time Nanos: " + elapsedNanos);
    double elapsedMillis = elapsedNanos / 1_000_000.0;
       LOG.debug("Elapsed time Millis: " + elapsedMillis + " ms");
   //    LOG.debug("execution time = " + elapsedMillis);
 }catch (Exception ex){
            String msg = "SendEmailTest" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
     //       return null;
 }
 } // end method  
 /*
  public void numberText_old(String s){
 try{
     LOG.debug("entering numberText");
     LOG.debug("locale language = " + Controllers.LanguageController.getLanguage());
     String[] args = new String[3];
         args[0] = "-l";
         args[1] = Controllers.LanguageController.getLanguage(); //  en be en_US be
         args[2] = "2373,95"; // point des mille non autorisés
     String result = numbertext.Numbertext.kernel(args);
     String msg = ("numbertext = " + args[2] + " /" + result);
        LOG.debug(msg);
        showMessageInfo(msg);
  //   LOG.debug("boolean result send mail = " +b);
 }catch (Exception ex){
            String msg = "numberText" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
 }
 } //end numbertext
  */
  public void numberText(String s) {
    final String methodName = utils.LCUtil.getCurrentMethodName();
    LOG.debug("entering " + methodName);
    try {
        LOG.debug(methodName + " - locale language = " 
                + Controllers.LanguageController.getLanguage());

        String[] args = new String[3];
        args[0] = "-l";
        args[1] = Controllers.LanguageController.getLanguage();
        args[2] = s;  // ✅ paramètre s plutôt que valeur en dur

        // ✅ appel sur instance injectée
        String result = numbertextService.kernel(args);
        LOG.debug(methodName + " - result = " + result);

    } catch (Exception e) {
        handleGenericException(e, methodName);
    }
} // end method
  
  
  
  
// ✅ MIGRÉ vers PlayerController (playerC) - onPlayerIdChanged — 2026-02-25
/*
  public void onPlayerIdChanged() {
      LOG.debug("entering onPlayerIdChanged with localAdmin = " + appContext.getLocalAdmin());
    if (appContext.getLocalAdmin().getIdplayer() == null || appContext.getLocalAdmin().getIdplayer() <= 0) {
        return;
    }
    try {
        Player loadedPlayer = playerManager.readPlayer(appContext.getLocalAdmin().getIdplayer());
        if (loadedPlayer != null) {
            appContext.setLocalAdmin(loadedPlayer);
            utils.LCUtil.showMessageInfo("Player loaded: " + appContext.getLocalAdmin().getPlayerLastName());
        } else {
            utils.LCUtil.showMessageFatal("Player not found");
        }
    } catch (Exception e) {
        LOG.error("Error loading player", e);
        utils.LCUtil.showMessageFatal("Error loading player " + e);
    }
} // end method
*/ // end ✅ MIGRÉ - onPlayerIdChanged
  /*
  public void onPlayerSelected(SelectEvent<DialogResult<Player>> event) {
    try {
           LOG.debug("entering onPlayerSelected with event = " + event.toString());
        DialogResult<Player> result = event.getObject();
           LOG.debug("result message = " + result.message());
           LOG.debug("result data = " + result.data());
        if (result != null && result.success()) {
            localAdmin = result.data();
            LOG.info("Player selected: {}", localAdmin.getPlayerLastName());
            utils.LCUtil.showMessageInfo("Player selected");
        }
    } catch (Exception e) {
        String msg = "Error processing onPlayerSelected " + e;
        LOG.error(msg);
        utils.LCUtil.showMessageFatal(msg);
        throw e;
    }
} // end method
  */
/* migrated on 2026-02-25 — now in Controller.refact.PlayerController (playerC)
   public void onPlayerSelected(SelectEvent<?> event) { ... }
*/
   
  
   public void main(String args[]) throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        //    not used
        LOG.debug(" -- main terminated");
    } // end main
} // end class


