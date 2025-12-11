package Controllers;

import entite.composite.EClubPro;
import entite.composite.ESubscription;
import entite.composite.ECourseList;
import entite.composite.ECotisation;
import entite.composite.EPaymentPro;
import entite.composite.EGreenfee;
import entite.composite.EUnavailable;
import entite.composite.ECompetition;
import entite.composite.EMatchplayResult;
import entite.composite.EPlayerPassword;
import entite.composite.EClubCourse;
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
import jakarta.enterprise.context.SessionScoped;
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
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
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
import org.primefaces.model.FilterMeta;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.map.Overlay;
import security.LoginBeanSecurity;
import services.CountryService;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;
import static utils.LCUtil.prepareMessageBean;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
import static java.lang.System.out;
import java.net.URI;
import java.net.http.HttpHeaders;

@Named("courseC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped  // fondamental !!
@jakarta.ws.rs.Path("courseController") // link avec server python !! projet rest-api moved from CreditcardController on 12-08-2025
public class CourseController implements Serializable, interfaces.GolfInterface, interfaces.Log{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
  //https://dzone.com/articles/javaserver-faces-23-1
@Inject @SessionMap
  private Map<String, Object> sessionMap;
@Inject @ApplicationMap
  private Map<String, Object> applicationMap;
    
     @Inject private Club club;
     @Inject private Car car;
     @Inject private Course course;
     @Inject private Round round; 
     @Inject private Tee tee; 
     @Inject private Hole hole;
     @Inject private Player player;
     @Inject private Player player2;
     @Inject private Player playerPro;
     @Inject private Player localAdmin;
     @Inject private Handicap handicap;
     @Inject private HandicapIndex handicapIndex;
     @Inject PlayingHandicap playingHcp;
     @Inject private ScoreStableford scoreStableford;
     @Inject private ScoreMatchplay scoreMatchplay; 
     @Inject private ScoreScramble scoreScramble; 
     @Inject private Inscription inscription;
     @Inject private Matchplay matchplay;
     @Inject private Subscription subscription;
     @Inject private Cotisation cotisation;
     @Inject private HolesGlobal holesGlobal;
     @Inject private Flight flight;
     @Inject private TarifGreenfee tarifGreenfee;
     @Inject private Creditcard creditcard;
     @Inject private LoginBeanSecurity login;
     @Inject private TarifMember tarifMember;
     @Inject private Greenfee greenfee;
     @Inject private Country country;
   //  @Inject private UnavailablePeriod unavailablePeriod;
     @Inject private UnavailableStructure unavailablestructure;
     @Inject private EUnavailable unavailable;
     @Inject private CountryService countryService;
     @Inject private Blocking blocking;
     @Inject private Activation activation;
     @Inject private Password password;
     @Inject private ECompetition competition;
//     @Inject private TreeBeanController treeBean;
   //  @Inject entite.Lesson eventPro; // new 25/05/2021 
 //    @Inject private Lesson lesson; // new 26-01-2023 
 //    @Inject private Address address; // new 26-01-2023
     private Lesson selectedLesson;
     @Inject private Professional professional; // new 25/05/2021 
     private final static List<Integer> STROKEINDEX = new ArrayList<>();
    private final static List<Integer> NUMBERS = new ArrayList<>();
    private ECourseList selectedPlayedRound; // mod 11/03/2018
    private static int[] parArray = null;
    private String parArrayString = "";
    private static String otherGame = null; // new 15/06/2014
    private SelectItem[] gameOptions; // new 01/12/2013
    private static String[] games = null; 
    private static String[] seasons = null; 
    private SelectItem[] seasonOptions; // new 01/12/2013
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
    private List <ECourseList>filteredPlayedRounds = null; // new 03/08/2014
 
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
    private Connection conn = null;
    private Connection connPool = null;
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
    private Map<String, String> availableHoles; // +getter (no setter necessary)
    private Map<String, String> members;
    private Map<Integer, String> data = new HashMap<>();
    private Integer progress1 = 0;  // mod 30-07-2025 was null
    
    private UploadedFile uploadedFile;
    private List<FilterMeta> filterBy;
 // fields for schedule lessons with pro
 //@Inject org.primefaces.model.ScheduleEvent<?> eventSelected; // new 03/06/2021 
    private List<Professional> listProfessional  = new ArrayList<>();
    private Professional selectedPro = null;
    private List<Lesson> listLessons = new ArrayList<>(); // new 27-01-2023
    //private Integer cpt = 0; 
    private String savedType;
    public CourseController(){  // constructor
        this.listavg = null;
//        this.setSunRiseSet(null);
    }

 @PostConstruct
 public void init(){ // attention !! ne peut absolument pas avoir : throws SQLException
  try{
          LOG.debug("entering init" );
  //    ???    LOG.debug("entering init without org.wilfly.plugin !" );
// 1. WITHOUT connection pool
        conn = new DBConnection().getConnection();
        if(conn != null){
              LOG.debug("cette connection database sera utilisée pour les RUN (main) = " + conn);
        }else{
             LOG.error("Connection database is null = " + conn);
        }
// 2. WITH Datasource and connection pool
      javax.sql.DataSource datasource = new utils.DBConnection().setDataSource();
          //  LOG.debug("Datasource is now = " + datasource.toString());
        connPool = new DBConnection().getPooledConnection(datasource);
        conn = connPool;
          LOG.debug("cette connection pooled database sera réutilisée pour toute la session = "+ conn);
// 3. Settings
     //  LOG.debug("** Webbrowser url = " + utils.LCUtil.firstPartUrl());
// 4. loading 
    availableQualifying = new LinkedHashMap<>();
    availableQualifying.put("Non Qualifying", "N");
    availableQualifying.put("Qualifying", "Y");
 //   availableQualifying.put("Counting", "C");
    
    availableHoles = new LinkedHashMap<>();
    availableHoles.put("18 Holes", "01-18");
    availableHoles.put("Holes 1-9", "01-09");
    availableHoles.put("Holes 10-18", "10-18");
  
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
    LOG.debug("we start a reset for all the working fields of the new session "); // c'est bien utile ?
//    LOG.debug("line 03");
  reset("from init in CourseController");
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
   
//    InfoController.printManifestAttributes();
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

 }catch (Exception e){
            String msg = "££ Exception in creating Connection or init in courseC = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
     }
  } //end method init

    public String getSavedType() {
        return savedType;
    }

    public void setSavedType(String savedType) {
        this.savedType = savedType;
    }

    public String getLineModelCourse() {
      //  LOG.debug("getLineModelCourse reached");
        return lineModelCourse;
    }

    public void setLineModelCourse(String lineModelCourse) {
      //  LOG.debug("setLineModelCourse reached");
        this.lineModelCourse = lineModelCourse;
    }

 
public void qualifyingListener(ValueChangeEvent e) {
        LOG.debug("qualifyingListener OldValue = " + e.getOldValue());
        LOG.debug("qualifying NewValue = " + e.getNewValue());
        if(e.getNewValue().equals("Y")){
            round.setShowQualifying(true);
      //      LOG.debug("showQualifying is true = " + round.isShowQualifying());
        }else{
            round.setShowQualifying(false);
        }
            LOG.debug("showQualifying is " + round.isShowQualifying());
        PrimeFaces.current().executeScript("window.location.reload(true);"); 
}
  
    public Lesson getSelectedLesson() {
        return selectedLesson;
    }

    public void setSelectedLesson(Lesson selectedLesson) {
        this.selectedLesson = selectedLesson;
    }
    public void deleteLesson() { /// used in price_pro.xhtml
        this.listLessons.remove(this.selectedLesson);
 //       this.selectedProducts.remove(this.selectedProduct);
    //    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product Removed"));
        String msg = "Lesson removed = = " + this.selectedLesson;
        this.selectedLesson = null;
        LOG.info(msg);
        showMessageInfo(msg);
       creditcard.setTotalPrice(listLessons.size() * professional.getProAmount());
       msg = "recalculated totalPrice is now " + creditcard.getTotalPrice();
       LOG.info(msg);
        showMessageInfo(msg);
        PrimeFaces.current().ajax().update("form_price_pro:growl-msg", "form_price_pro:listLessons","form_price_pro:messages");
    }
    
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

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public void setFilterBy(List<FilterMeta> filterBy) {
        this.filterBy = filterBy;
    }

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

    public Map<String, String> getAvailableHoles() {
        return availableHoles;
    }

    @PreDestroy
    public void exit() {
        LOG.debug(" ------------------ from CourseController PreDestroy exit ()...");
    }
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

    public ECompetition getCompetition() {
        return competition;
    }

    public void setCompetition(ECompetition competition) {
        this.competition = competition;
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

    public Professional getProfessional() {
        return professional;
    }

    public void setProfessional(Professional professional) {
        this.professional = professional;
    }

    public Map<String, String> getAvailableQualifying() {
        return availableQualifying;
    }

  public List<String> getGameList() {
       return gameList;
   }
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

    public Blocking getBlocking() {
        return blocking;
    }

    public void setBlocking(Blocking blocking) {
        this.blocking = blocking;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public Player getLocalAdmin() {
        return localAdmin;
    }

    public void setLocalAdmin(Player localAdmin) {
        this.localAdmin = localAdmin;
    }
    public LoginBeanSecurity getLogin() {
        return login;
    }

    public void setLogin(LoginBeanSecurity login) {
        this.login = login;
    }

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
         parArray =  new read.ReadParArray().read(conn, player, course);  //mod 31/07/2014
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

    public HolesGlobal getHolesGlobal() {
        return holesGlobal;
    }

    public void setHolesGlobal(HolesGlobal holesGlobal) {
        this.holesGlobal = holesGlobal;
    }

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
        return unavailable;
    }

    public void setUnavailable(EUnavailable unavailable) {
        this.unavailable = unavailable;
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

 public void selectFlightFromDialog(Flight flight) throws IOException{
      LOG.debug("entering selectflightfromdialog");
      LOG.debug("entering selectflightfromdialog with source = " + flight.toString() );
      this.flight = flight;
        LOG.debug("flightStart format LocalDateTime = " + flight.getFlightStart() );
  //      LOG.debug("after setflight, flight HHMM = " + flight.getFlightHourStart()) ;
////     round.setWorkHour(flight.getFlightHourStart());  // mod 22-10-2018
     round.setRoundDate(flight.getFlightStart()); // sera écrasé par input de l'écran
        LOG.debug("getRoundDate = " + round.getRoundDate());
     round.setRoundDateTrf(flight.getFlightStart()); // new 21-02-2019 on utilise une zone de transfert
        LOG.debug("getRoundDateTRF = " + round.getRoundDateTrf());
   //  boolean b = new DialogController().closeDialog("dialogFlight.xhtml"); // mod 03-04-2020
     boolean b = Controllers.DialogController.closeDialog("dialogFlight.xhtml"); // mod 03-04-2020
  }

    public Car getCar() {
        return car;
    }

    public String getIntroductionTxt() {
        return introductionTxt;
    }

    public void setIntroductionTxt(String introductionTxt) {
        this.introductionTxt = introductionTxt;
    }

    public void setCar(Car car) {
        this.car = car;
    }

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
            lists.PlayersList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
        }
    }

    public Professional getSelectedPro() {
        return selectedPro;
    }

    public void setSelectedPro(Professional selectedPro) {
        this.selectedPro = selectedPro;
    }

    public List<Professional> getListProfessional() {
        LOG.debug("listprofessional = " + listProfessional);
       return listProfessional;
   }

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
            lists.InscriptionList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
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
            lists.HandicapList.setListe(null);  //lazy loading forced !!
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
// enlevé 29-11-2018 encore utilisé ?
    public void setInputPlayedRounds(String inputPlayedRounds)    {
        this.inputPlayedRounds = inputPlayedRounds;
            LOG.debug("setInput (new played list) = " + inputPlayedRounds);
        if (inputPlayedRounds.equals("ini")) {
            filteredCars = null; // new 15/12/2013
        }
    }

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

    public void setInputClub(String inputClub){
        LOG.debug("setInput (new club !) = " + inputClub);
        this.inputClub = inputClub;
        if(inputClub.equals("ini")) {
            club = new Club();
            //        setNextCourse(false);
            course = new Course();
            tee = new Tee();
            hole = new Hole();
        }
    }

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
            course = new Course();
            tee = new Tee();
            hole = new Hole();
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
            lists.ScoreCardList3.setListe(null);  //lazy loading !!
        }
    }

    public String getInputScore() {
        return inputScore;
    }
    public String getInputTeeStart() {
        return inputScore;
    }
    
  public String LastSession(int idplayer) throws SQLException, Exception {
        LOG.debug("entering LastSession with idplayer = " + idplayer); //from welcome.xhtml sourceC.lastSession
     Audit audit = new find.FindLastLogin().find(player, conn);
     if(audit != null){
         LOG.debug("date last connection for this player = " + audit.getAuditStartDate().format(ZDF_TIME_HHmm));
         return audit.getAuditStartDate().format(ZDF_TIME_HHmm);
     }else{
         return("First Connection for this player");
     }
 } // end method

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
        return Connected;
    }

    public void setConnected(boolean Connected) {
        this.Connected = Connected;
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

    public List<ECourseList> getFilteredPlayedRounds() {
        return filteredPlayedRounds;
    }

    public void setFilteredPlayedRounds(List<ECourseList> filteredPlayedRounds) {
        this.filteredPlayedRounds = filteredPlayedRounds;
    }

    

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

    public HandicapIndex getHandicapIndex() {
        return handicapIndex;
    }

    public void setHandicapIndex(HandicapIndex handicapIndex) {
        this.handicapIndex = handicapIndex;
    }

    public String getInputcmdParticipants() {
        return inputcmdParticipants;
    }

    public void setInputcmdParticipants(String inputcmdParticipants) {
        this.inputcmdParticipants = inputcmdParticipants;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }
// new 27-12-2024 for p:dialog in selectPlayer.xhtml
 public EPlayerPassword getSelectedPlayerEPP() {
     return selectedPlayerEPP;
  }

 public void setSelectedPlayerEPP(EPlayerPassword selectedPlayerEPP) {
      this.selectedPlayerEPP = selectedPlayerEPP;
 }

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



    public ECourseList getSelectedPlayedRound() {
        return selectedPlayedRound;
    }

    public void setSelectedPlayedRound(ECourseList selectedPlayedRound) {
        this.selectedPlayedRound = selectedPlayedRound;
    }

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
        return NextPlayer;
    }

    public void setNextPlayer(boolean NextPlayer) {
        this.NextPlayer = NextPlayer;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

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

    public void setRound(Round round) {
        this.round = round;
    }


    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Tee getTee() {
        return tee;
    }

    public Cotisation getCotisation() {
        return cotisation;
    }

    public void setCotisation(Cotisation cotisation) {
        this.cotisation = cotisation;
    }

    public void setTee(Tee tee) {
        this.tee = tee;
    }

    public Hole getHole() {
        return hole;
    }

    public void setHole(Hole hole) {
        this.hole = hole;
    }

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

    public Connection getConn() {
        return conn;
    }

    public int louis() {
        return course.getIdcourse();
    }

public static String fileUpload() throws SQLException{
     return "player_upload_file.xhtml?faces-redirect=true";
}

// http://adfpractice-fedor.blogspot.be/2012/02/understanding-immediate-attribute.html
// http://balusc.omnifaces.org/2006/09/debug-jsf-lifecycle.html
 // new 25/03/2017

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

 // mod 18-02-2024
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
} //end method completePlayerAddress

@Inject  private ExternalContext externalContext;
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

public String processAction(){
   //  http://memorynotfound.com/jsf-how-to-find-component-programatically/
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        UIComponent component = view.findComponent("uf:name"); // form:field
        component.getAttributes().put("value", "John");
        return null;
    }

public void playerPasswordListener(ValueChangeEvent e) {
            LOG.debug("playerPasswordListener OldValue = " + e.getOldValue());
            LOG.debug("playerPasswordListener NewValue = " + e.getNewValue());
        password.setCurrentPassword(e.getNewValue().toString() );
            LOG.debug("playerPassword setted = " + password.getCurrentPassword());
}

public void playerConfirmPasswordListener(ValueChangeEvent e) {
        LOG.debug("playerConfirmPasswordListener OldValue = " + e.getOldValue());
        LOG.debug("playerConfirmPasswordListener NewValue = " + e.getNewValue());
           password.setWrkconfirmpassword(e.getNewValue().toString() );
  //  player.password.setWrkconfirmpassword(e.getNewValue().toString() );
}

public void creditCardNumberListener(ValueChangeEvent e) {
        LOG.debug("creditcardNumber OldValue = " + e.getOldValue());
    //    LOG.debug("creditcardNumber newvaluee = " + e.getOldValue());
       LOG.debug("creditcardNumber NewValue = " + e.getNewValue());
    creditcard.setCreditcardNumber(e.getNewValue().toString() );
}

public void creditCardTypeListener(ValueChangeEvent e) {
       LOG.debug("creditcardType OldValue = " + e.getOldValue());
       LOG.debug("creditcardType NewValue = " + e.getNewValue());
    creditcard.setCreditcardType(e.getNewValue().toString() );
    if( !creditcard.getCreditcardIssuer().equals(creditcard.getCreditcardType())){   // issuer = calculated  cardType = input data
        String msg = "WARNING !!! "
                + " <br/> Issuer detected = " + creditcard.getCreditcardIssuer()
                + " <br/> Card Type data in = " + creditcard.getCreditcardType();
        LOG.debug(msg);
        showMessageInfo(msg);
    }
}

public void roundWorkDate(ValueChangeEvent e) throws ParseException, SQLException{ // throws ParseException {
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
      //      return null;
        }      
   //  return "round.xhtml?faces-redirect=true";
} // end method
// affiche la box, pas le Messagefatal et continue ...
// que faire ? forcer work?? à null ?
// forcer un dispatch ?? vers une view

 public String otherPlayers() throws SQLException, Exception {  //mod 30-11-2018
  try{
          LOG.debug("entering otherPlayers ... ");
          LOG.debug("otherPlayers round = ... " + round);
          LOG.debug("otherPlayers course = ... " + course);
          
       tarifGreenfee = new find.FindTarifGreenfeeData().find(round, conn);
       if(tarifGreenfee == null){
           String err = "Tarif returned from findTarifdata is null ";
            LOG.debug(err);
           showMessageFatal(err);
           return "inscriptions_other_players.xhtml?faces-redirect=true";
        }
            LOG.debug("greenfee = " + greenfee);
            LOG.debug("Tarif greenfee = " + tarifGreenfee);
     //  LOG.debug("new inscriptions = " + getSelectedOtherPlayers().size());

            LOG.debug("nombre déjà inscrits à ce round = " + roundPlayersList().size());
            LOG.debug("number new inscriptions = " + player.getDraggedPlayers().size());
            LOG.debug("roundGame = " + round.getRoundGame());
            int max = 0;
            if(round.getRoundGame().equals(Round.GameType.MP_SINGLE.toString())){
                max = 2;
            }else{
                max = 4;
            }
            LOG.debug("here are the draggedPlayers  = " + player.getDraggedPlayers().toString());
   //      LOG.debug("déjà inscrits à ce round 1 = " + round.getPlayers().size());
     //       LOG.debug("nom des joueurs déjà inscrits à ce round = " + round.getPlayersList());
    
            int tot = player.getDraggedPlayers().size() + roundPlayersList().size();
               LOG.debug("total déjà inscrits plus nouveaux candidats = " + tot);
        if(tot > max){
            // with messages placeholders
            String msg = prepareMessageBean("inscription.toomuchplayers") + " {max} = " + max + " /tot " + tot; 
            LOG.error(msg);
            showMessageFatal(msg);
        }
      return "inscriptions_other_players.xhtml?faces-redirect=true";
   }catch(Exception e){
            String msg = "££ Exception in otherPlayers = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
          }     
 // return null;      
 //   } // end catch
   } // end method
   
   public String createOtherPlayers() throws SQLException {  //new 03/09/2014, mod 30/06/2017 used in pickListPlayers not operational
        LOG.debug("starting createOtherPlayers = ");
  //      LOG.debug("List for players = " + playersTarget.toString());
        LOG.debug("dlPlayers getSource 2 = " + dlPlayers.getSource().toString()); // colonne de gauche
        LOG.debug("dlPlayers getTarget 2 = " + dlPlayers.getTarget().toString()); // colonne de droite
        LOG.debug("there are ?? new inscriptions = " + dlPlayers.getTarget().size()); // joueurs sélectionnés
        // à faire = vérifier que 3 joueurs maximum !
        
        for(int i=0; i < dlPlayers.getTarget().size() ; i++){
 //           LOG.debug("line 01");
     //       LOG.debug(" -- item in for idplayer # = " + dlPlayers.getTarget().get(i).getIdplayer() );
        }
        LOG.debug("fulllist = " + fullList.toString());
        LOG.debug("for round = " + round.toString());
        LOG.debug("for player_has_round = " + inscription.toString());
        LOG.debug("after createOtherPlayers");
    //    boucler sur createInscription 
        return "inscription.xhtml?faces-redirect=true";
    }
   
    public int getInputPlayingHcp() {
        return inputPlayingHcp;
    }

    public void setInputPlayingHcp(int inputPlayingHcp) {
        this.inputPlayingHcp = inputPlayingHcp;
    }

   public void findCourseListForClub() throws SQLException{ // mod 18/06/2022
      LOG.debug("entering findCourseListForClub");
   courseListForClub = new lists.CourseListForClub().list(club, conn);
  // return null; 
}

  public String selectorClubNextView(){  // coming from selectClubDialog.xhtml
  try{
       LOG.debug("entering selectorClub");
       LOG.debug("with inputSelectClub = " + sessionMap.get("inputSelectClub"));  
       
    if(sessionMap.get("inputSelectClub").equals("PaymentCotisationSpontaneous")){
          LOG.debug("sessionmap is PaymentCotisationSpontaneous");
          LOG.debug("pour le club = " + club.getIdclub());
          LOG.debug("pour le round = " + round.getRoundDate());
        return "cotisation.xhtml?faces-redirect=true";
    }
  } catch (Exception ex) {
            String msg = "Exception in selectorClub() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
       return null;
   } // end method
  
 public String selectedClub(Club c) {   // coming from dialogClub.xhtml
  try{
      LOG.debug(" entering selectedClub with club c = " + c);
      LOG.debug(" entering selectedClub with club club = " + club);
        if(player == null) {
               LOG.debug(" current player is null - a examiner : pas normal !"); 
          //  player = new Player();
        }
    //       LOG.debug("sessionMap : inputSelectCourse = " + sessionMap.get("inputSelectCourse")); // est null
           LOG.debug("sessionMap : inputSelectClub = " + sessionMap.get("inputSelectClub"));
        club = c; // IMPORTANT
            LOG.debug(" for club = " + club);

        if(sessionMap.get("inputSelectClub") == null){
               LOG.debug("inputSelectClub = null - cas create_player");
               LOG.debug(" current player initialized !"); // cas create player from player.xhtml    
         //   player = new Player();
            player.setPlayerHomeClub(club.getIdclub());
                LOG.debug("player = " + player);
            Controllers.DialogController.closeDialog("dialogClub.xhtml");
            return null; // player.xhtml
         }

        if(sessionMap.get("inputSelectClub").equals("LocalAdministrator"))  {
            localAdmin.setPlayerHomeClub(club.getIdclub());
                 LOG.debug("local administrator with HomeClub setted = " + localAdmin);
            Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
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
           tarifMember = new find.FindTarifMembersData().find(club, round, conn);
           Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
           return null; //"cotisation.xhtml?faces-redirect=true";
        }
 
        if(sessionMap.get("inputSelectClub").equals("CreatePro")){
            localAdmin.setPlayerHomeClub(club.getIdclub());
                 LOG.debug("local administrator with HomeClub setted = " + localAdmin);
            Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
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
         competition.getCompetitionDescription().setCompetitionClubId(club.getIdclub());
             LOG.debug(" competition updated ClubId= " + competition.getCompetitionDescription()); 
       LOG.debug("line 01");
    //  boolean b = new DialogController().closeDialog("dialogClub.xhtml"); 
      boolean b = Controllers.DialogController.closeDialog("dialogClub.xhtml"); // mod 03-04-2020
          LOG.debug("DialogClub closed ! = " + b);
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
 
  public String selectedCourseForClub(Course c){   // coming from dialogCourse.xhtml  
      // faut une 2e paramètre !!
  try{
           LOG.debug(" ... entering selectedCourseForClub ");
           LOG.debug(" with course input parameter = " + c);
     //      LOG.debug(" for player = " + player);
           LOG.debug(" for club = " + club);
        course = c;
           LOG.debug(" course is now = " + course);
     //              LOG.debug("course = " + course);
        String msg = "Select Course Successfull = " + " <br/> CourseName = " + course.getCourseName();
        LOG.debug(msg);
        showMessageInfo(msg);
        LOG.debug("selectedCourseForClub : inputSelectClub = " + sessionMap.get("inputSelectClub"));
        
        boolean b = Controllers.DialogController.closeDialog("dialogCourse.xhtml"); // mod 02/10/2024
        LOG.debug("DialogCourse closed !! = " + b);
   //      LOG.debug("selectedCourseForClub, inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
   
        if(sessionMap.get("inputSelectClub").equals("MenuUnavailable")) {  // positionné from menu
           LOG.debug("handling menu unavailable");
           LOG.debug("for unavailable = " + unavailable);
           // chercher si structure existe
           // à deplacer vers selectclub ??
           unavailable.getStructure().setMenuLaunched(true); // provoque affichage menu interne dans unavailable_menu.xhtml
           var v = new read.ReadUnavailableStructure().read(club, conn);
              LOG.debug("variable v found = ENTITE UnavailableStructure " + v);
           // positionner switch
           
           if (v != null && !v.getStructureList().isEmpty()){
               unavailable.getStructure().setStructureList(v.getStructureList());
               unavailable.getStructure().setStructureExists(true); // provoque affichage create periods
               LOG.debug("structure exists " + v);
           }else{
               LOG.debug("NO structure exists " + v);
           }
           //return null;
     //   boolean b = new DialogController().closeDialog("dialogCourse.xhtml"); // back to normal screen
         b = Controllers.DialogController.closeDialog("dialogCourse.xhtml"); // mod 03-04-2020
          LOG.debug("DialogCourse closed !! = " + b);
        return null;
      }
      
    if(sessionMap.get("inputSelectClub").equals("CREATE COMPETITION")) {  // positionné dans menu
        LOG.debug(" selectedCourseForClub - competition =  " + competition);
      competition.getCompetitionDescription().setCompetitionCourseId(course.getIdcourse());
      competition.getCompetitionDescription().setCompetitionCourseIdName(
                         Integer.toString(course.getIdcourse()) + " - " + course.getCourseName());
         LOG.debug(" competition updated CourseId= " + competition.getCompetitionDescription()); 
      Controllers.DialogController.closeDialog("dialogCourse.xhtml"); // mod 03-04-2020
      return null;//  return "competition_create_description.xhtml?faces-redirect=true";
       }
  } catch (Exception e) {
            String msg = "££ Exception in selectedCourseForClub = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    return null;
    } // end class selectcours
public String to_selectCourse_xhtml(String s){
            LOG.debug("entering to_selectCourse_xhtml ... with string = " + s);
       reset("Reset to_selectCourse " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectCourse", s);
            LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
       return "selectCourse.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
   }

public String to_selectCourse2_xhtml(String s){
            LOG.debug("entering to_selectCourse2_xhtml ... with string = " + s);
       reset("Reset to_selectCourse " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectCourse", s);
       sessionMap.put("inputSelectClub", s);
          LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
          LOG.debug("club selected for :  = " + sessionMap.get("inputSelectClub"));
       return "selectClubCourse.xhtml?faces-redirect=true"; // + sessionMap.get("inputSelectCourse");
   }

public String to_selectGrpc_xhtml(String s){
            LOG.debug("entering to_selectGprc_xhtml ... with string = " + s);
       reset("Reset to_selectGrpc " + s);
  //     setFilteredCourses(null);
  //     sessionMap.put("inputSelectCourse", s);
  //          LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
       return "grpc_server.xhtml?faces-redirect=true"; // + sessionMap.get("inputSelectCourse");
   }
public String to_selectClubLA_xhtml(String s){
            LOG.debug("entering to_selectClubLA_xhtml ... with string = " + s);
       reset("Reset to_selectClubLA " + s);
       setFilteredClubs(null); // new 15-12-2023
       if(s.equals("CreatePro"))  {
           sessionMap.put("inputSelectClub", s);
           return "professional.xhtml?faces-redirect=true";
       }
       sessionMap.put("inputSelectCourse", s);
            LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
      return "selectClubLocalAdmin.xhtml?faces-redirect=true"; //&cmd=" + sessionMap.get("inputSelectCourse"); 
  //     
   }

public String to_selectClubSYS_xhtml(String s){
            LOG.debug("entering to_selectClubSYS_xhtml ... with string = " + s);
       reset("Reset to_selectClubSYS " + s);
       setFilteredClubs(null); // new 15-12-2023
       if(s.equals("CreatePro"))  {
           sessionMap.put("inputSelectClub", s);
           return "professional.xhtml?faces-redirect=true";
       }
       sessionMap.put("inputSelectCourse", s);
            LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
      return "selectClubLocalAdmin.xhtml?faces-redirect=true"; //&cmd=" + sessionMap.get("inputSelectCourse"); 
  //     
   }

// new 12/06/2022
public String to_selectMenuUnavailable_xhtml(String s){ 
            LOG.debug("entering to_selectMenuUnavailable_xhtml ... with string = " + s);
       reset("Reset to_selectClubLA " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectClub", s);
  //          LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
      return "unavailable_menu.xhtml?faces-redirect=true";
   }

public String to_selectDesignationLA_xhtml(String s){  // LocalAdministrator
            LOG.debug("entering to_selectClubLA_xhtml ... with string = " + s);
       reset("Reset to_selectClubLA " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectClub", s);
            LOG.debug("club selected for :  = " + sessionMap.get("inputSelectClub"));
     return "local_administrator.xhtml?faces-redirect=true";
   }


public String to_selectCourseLA_xhtml(String s){
            LOG.debug("entering to_selectCourseLA_xhtml ... with string = " + s);
       reset("Reset to_selectClub2 " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectCourse", s);
       sessionMap.put("adminType","admin");
            LOG.debug("club selected for :  = " + sessionMap.get("inputSelectCourse"));
       return "selectCourseLocalAdmin.xhtml?faces-redirect=true"; //&cmd=" + sessionMap.get("inputSelectCourse");
   }






public String to_update_help(String s){
            LOG.debug("entering to_update_help ... with string = " + s);
       reset("Reset to_selectCourse " + s);
    //   setFilteredCourses(null);
    //   sessionMap.put("inputSelectCourse", s);
   //         LOG.debug("course selected for :  = " + sessionMap.get("inputSelectCourse"));
       return "treenode.xhtml?faces-redirect=true";
   }

public String to_selectLocalAdmin_xhtml(String s){
            LOG.debug("entering to_selectLocalAdmin_xhtml ... with string = " + s);
       reset("Reset to_selectLocalAdmin" + s);
       sessionMap.put("inputSelectPaiement", s);
       
     if(s.equals("Members")){
        return "local_administrator_cotisations.xhtml?faces-redirect=true"; //&cmd=" + s;
     }
     if(s.equals("Greenfees")){
        return "local_administrator_greenfees.xhtml?faces-redirect=true"; //&cmd=" + s;
     }
     return null;
}

public String to_selectSystemAdmin_xhtml(String s){
            LOG.debug("entering to_selectSystemAdmin_xhtml ... with string = " + s);
       reset("Reset to_selectSystemAdmin" + s);
       sessionMap.put("inputSelectSubscriptions", s);
       return "system_administrator_subscriptions.xhtml?faces-redirect=true"; //&cmd=" + s;
   /*      
     if(s.equals("Members")){
        return "local_administrator_cotisations.xhtml?faces-redirect=true"; //&cmd=" + s;
     }
     if(s.equals("Greenfees")){
        return "local_administrator_greenfees.xhtml?faces-redirect=true"; //&cmd=" + s;
     }
     return null;
*/
}


public String to_selectPro_xhtml(String s){
         LOG.debug("entering to_selectPro_xhtml ... with string = " + s);
       reset("Reset to_selectPro" + s);
       sessionMap.put("inputSelectPaiement", s);  // ?? utile
     if(s.equals("Lessons")){
        return "professional_lessons_paid.xhtml?faces-redirect=true"; //&cmd=" + s;
     }
     if(s.equals("Inscription")){
        return "selectProForClub.xhtml?faces-redirect=true&cmd=" + s;}
     return null;
}
public String to_selectClub_xhtml(String s){
            LOG.debug("entering to_selectClub_xhtml ... with string = " + s);
       reset("Reset to_selectClub" + s);
       sessionMap.put("inputSelectCourse", s);
       sessionMap.put("inputSelectClub", s); // fake 22-02-2024
       return "selectClub.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
   }

// 
// new 24-08-2025
public String to_selectClubDialog_xhtml(String s){
            LOG.debug("entering to_selectClubDialog_xhtml ... with string = " + s);// PaymentCotisationSpontaneous
       reset("Reset to_selectClubDialog" + s);
    //   sessionMap.put("inputSelectCourse", s);
       sessionMap.put("inputSelectClub", s);
       return "selectClubDialog.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectClub");
   }



public String selectClub(Club c, String select){
  try {
            LOG.debug(" entering selectClub(Club) ... = ");
            LOG.debug(" with select = " + select);
            LOG.debug(" with in_club = " + c);
        club = c;

        String msg = "Select Club Successfull l 1731 = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> inputSelectCourse = " + select;
        LOG.debug(msg);

             LOG.debug("selectClub : inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
         if(sessionMap.get("inputSelectCourse") == null){
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
         }
 
         // ou remplacer par 
         //  if(sessionMap.get("inputSelectClub").equals(CreatePro) ??
         
          if(select.equals("CreatePro")) {// new 26-05-2021
            return "professional.xhtml?faces-redirect=true";} 

    //  enlevé 19/06/2022    if(select.equals("CreateUnavailableStructure")) {
    //        return "unavailable_structure.xhtml?faces-redirect=true";} // mod 22-03-2020
  
       // mod 01-04-2020
            if(select.equals("CreateUnavailablePeriod")) {
            // compléter l'entité UnavailableStructure sessionMap.get("inputSelectCourse")
            unavailable.setStructure(new read.ReadUnavailableStructure().read(club, conn)); // mod 20-04-2021
      // tester si erreur 
               LOG.debug("returned with unavailable structure = " + unavailablestructure);
            if(unavailable.getStructure() == null){
                   // LOG.debug("error we go to createstructure");
                     String msgerr =  LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr); 
                     LCUtil.showMessageInfo(msgerr);
                return "unavailable_structure.xhtml?faces-redirect=true";
            }else{
                club.setUnavailableStructure(unavailablestructure);
                    LOG.debug("structure length = " + club.getUnavailableStructure().getStructureList().size());
                return "unavailable_period.xhtml?faces-redirect=true"; // new 23-03-2020 22-03-2020
            }
        } // end if
             
             
        if (select.equals("CreateTarifGreenfee")) {
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";} // mod 16-09-2018
        
        if (select.equals("CreateTarifMember")) {
            return "tarif_members_menu.xhtml?faces-redirect=true";} // new 05-01-2019     
             
        if(select.equals("PaymentCotisationSpontaneous")) {
               LOG.debug("entering PaymentCotisationSpontaneous");
               LOG.debug("club = "+ club);
               LOG.debug("round = "+ round); // normalement est null ?? payment pour quelle année ?
               // round utilisé pour 
            tarifMember = new find.FindTarifMembersData().find(club, round, conn);  // mod 10/05/2022
            if(tarifMember == null){
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return null;
            }else{
                return "cotisation.xhtml?faces-redirect=true";
            }
           
       }

  } catch (Exception e) {
            String msg = "££ Exception in selectCourse = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    return null;
    } // end class selectcourse

public String selectCourseLA(EClubCourse in_club, String select){
  try {
            LOG.debug(" entering selectCourseLA ... = ");
            LOG.debug(" with select = " + select);
            LOG.debug(" with in_club = " + in_club);
        club = in_club.getClub();    
        course = in_club.getCourse();
        String msg = "Select Club Successfull l 1731 = "
                + " <br/> Club name = " + club.getClubName()
       //         + " <br/> Course name = " + course.getCourseName()
        //        + " <br/> inputSelectCourse = " + sessionMap.get("inputSelectCourse");
                + " <br/> inputSelectCourse = " + select;
        LOG.debug(msg);
             LOG.debug("selectClub, inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
         if(sessionMap.get("inputSelectCourse") == null){
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
         }

          if (select.equals("CreateTarifGreenfee")) {
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";}

          if (sessionMap.get("inputSelectCourse").equals("CreateTarifMember")) {
            return "tarif_members_menu.xhtml?faces-redirect=true";} // new 05-01-2019     
             
            if(sessionMap.get("inputSelectCourse").equals("CreateUnavailablePeriod")) {
            // compléter l'entité UnavailableStructure sessionMap.get("inputSelectCourse")
            unavailable.setStructure(new read.ReadUnavailableStructure().read(club, conn)); // mod 20-04-2021
      // tester si erreur 
               LOG.debug("returned with unavailble structure = " + unavailablestructure);
        if(unavailable.getStructure() == null){
                   // LOG.debug("error we go to createstructure");
                     String msgerr =  LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr); 
                     LCUtil.showMessageInfo(msgerr);
                return "unavailable_structure.xhtml?faces-redirect=true";
            }else{
                club.setUnavailableStructure(unavailablestructure);
                    LOG.debug("structure length = " + club.getUnavailableStructure().getStructureList().size());
                return "unavailable_period.xhtml?faces-redirect=true"; // new 23-03-2020 22-03-2020
            }
        } // end if      
        if(select.equals("PaymentTarifMember")) {
           tarifMember = new find.FindTarifMembersData().find(club, round, conn);
            if(tarifMember == null){
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return null;
            }else{
                 return "cotisation.xhtml?faces-redirect=true";
            }
           
       }
  // la suite n'est pas développée car doit rester dans selectCourse
    
        if (sessionMap.get("inputSelectCourse").equals("CreateRound")) {
            return "round.xhtml?faces-redirect=true&cmd=round";} // mod 30/07/2014
        
        if (sessionMap.get("inputSelectCourse").equals("ini")) {
            return "round.xhtml?faces-redirect=true&cmd=ini";} // mod 30/07/2014

     // 08-12-2024 à modifier !!!   
        if (sessionMap.get("inputSelectCourse").equals("ChartCourse")) {
            return "statChartCourse.xhtml?faces-redirect=true";} // mod 01/04/2016
     //   }
  } catch (Exception e) {
            String msg = "££ Exception in selectCourse = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    return null;
    } // end class selectcourse

public String selectClubCourse(){ // new 02/10/2024
 try {
     LOG.debug(" entering selectClubCourse ! ");
     LOG.debug(" with club = " + club);
     LOG.debug(" with course = " + course);
     LOG.debug(" with sessionMap inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
   return null;
// to be completed

 } catch (Exception e) {
            String msg = "££ Exception in selectClubCourse = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
  //  return null;
    } // end class selectClubCourse

public String selectCourse(){ // new 02/10/2024
 try {
        LOG.debug(" entering selectCourse ");
        LOG.debug("club = " + club);
        LOG.debug("course = " + course);
        LOG.debug("sessionMap inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
     if(sessionMap.get("inputSelectCourse").equals("CreateRound")) {
            return "round.xhtml?faces-redirect=true&cmd=round";} // mod 30/07/2014   
     //  à compléter
     return null;

 } catch (Exception e) {
            String msg = "££ Exception in selectCourse = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
}


public String selectCourse(ECourseList ecl){
 try {
            LOG.debug(" entering selectCourse(ECourseList) ... = ");
 //           LOG.debug(" with ecl = " + ecl.toString());
        club = ecl.getClub();
            LOG.debug("club = " + club);
        course = ecl.getCourse(); // on le perd ici ? je crois que oui !!!
            LOG.debug("course = " + course);
        String msg = "Select Course Successfull l 1731 = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName()
                + " <br/> inputSelectCourse = " + sessionMap.get("inputSelectCourse");
        LOG.debug(msg);
   //     showMessageInfo(msg); // enlevé 17-11-2018
 //  printMap(sessionMap);
             LOG.debug("selectCourse, inputSelectCourse = " + sessionMap.get("inputSelectCourse"));
         if(sessionMap.get("inputSelectCourse") == null){
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
         }
  //       LOG.debug("selectCourse, inputSelectCourse !! = " + sessionMap.get("inputSelectCourse"));
         
        if (sessionMap.get("inputSelectCourse").equals("CreateRound")) {
            return "round.xhtml?faces-redirect=true&cmd=round";} // mod 30/07/2014
        
        if (sessionMap.get("inputSelectCourse").equals("ini")) {
            return "round.xhtml?faces-redirect=true&cmd=ini";} // mod 30/07/2014
        
        if (sessionMap.get("inputSelectCourse").equals("CreateTarifGreenfee")) {
            return "tarif_greenfee_menu.xhtml?faces-redirect=true";} // mod 16-09-2018
        
        if (sessionMap.get("inputSelectCourse").equals("CreateTarifMember")) {
   //         tarifMember = new TarifMember();
            return "tarif_members_menu.xhtml?faces-redirect=true";} // new 05-01-2019
       

  //      if(sessionMap.get("inputSelectCourse").equals("CreateUnavailableStructure")) {
   //         return "unavailable_structure.xhtml?faces-redirect=true";
   //     } // mod 22-03-2020

      //  if(sessionMap.get("inputSelectCourse").equals("CreateUnavailablePeriod")) {
      // mod 01-04-2020
        if(sessionMap.get("inputSelectCourse").equals("CreateUnavailablePeriod")) {
            // compléter l'entité UnavailableStructure sessionMap.get("inputSelectCourse")
            unavailable.setStructure(new read.ReadUnavailableStructure().read(ecl.getClub(),conn)); // mod 31-03
            // tester si erreur 
            LOG.debug("returned with unavailble strucure = " + unavailablestructure);
            if(unavailable.getStructure() == null){
                   // LOG.debug("error we go to createstructure");
                     String msgerr =  LCUtil.prepareMessageBean("unavailable.structure.notfound");
                    LOG.error(msgerr); 
                     LCUtil.showMessageInfo(msgerr);
                return "unavailable_structure.xhtml?faces-redirect=true";
            }else{
                club.setUnavailableStructure(unavailablestructure);
                    LOG.debug("structure length = " + club.getUnavailableStructure().getStructureList().size());
                return "unavailable_period.xhtml?faces-redirect=true"; // new 23-03-2020 22-03-2020
            }
        } // end if
    /////    
        if(sessionMap.get("inputSelectCourse").equals("PaymentTarifMember")) {
            LOG.debug("we are in paymentTarifMember");
            LOG.debug("round = " + round);
   //         loadTarifMember();  // mod 14-02-2019  used in ??.xhtml
   /* distinguer le paiement cotisation pour un greenfee : round existe
   // et le paiement spontané d'une cotisation : round existe pas 
           if(round == null){  // paiement spontané
               round.setRoundDate(LocalDateTime.now()); // date du jour 
               tarifMember = new find.FindTarifMembersData().find(club, round.getRoundDate(), conn);
               tarifMember.setType("S"); 
           }else{
               tarifMember = new find.FindTarifMembersData().find(club, round.getRoundDate(), conn);
               tarifMember.setType("R"); // paiement pour participer à un round
           }
 */
            if(tarifMember == null){
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return null;
            }else{
                 return "cotisation.xhtml?faces-redirect=true";
            }
           
            }
        // PF 15 à modifier 
        if (sessionMap.get("inputSelectCourse").equals("ChartCourse")) {
            return "statChartCourse.xhtml?faces-redirect=true";
        } // mod 01/04/2016
  } catch (Exception e) {
            String msg = "££ Exception in selectCourse = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    return null;
    } // end class selectcourse

public String findTarifGreenfee(){  // tarif pour l'inscription (green fee)
    // used in greenfee_cotisation_round.xhtml 
try{
       LOG.debug("starting findTarifGreenfee");
       LOG.debug("player = " + player.toString());
       LOG.debug("course = " + course.toString());
    sessionMap.put("inputSelectCourse", "createTarifGreenfee");  // new 02-04-2020  ???
    tarifGreenfee = new find.FindTarifGreenfeeData().find(round, conn);
    if(tarifGreenfee == null){
         String err = "Tarif returned from findTarifdata is null ";
         LOG.debug(err);
    //     showMessageFatal(err);
         return null;  // donc inscription.xhtml ??
    }
       LOG.debug("now tarifGreenfee found is = " + tarifGreenfee);
    tarifGreenfee = new calc.CalcTarifGreenfee().calc(tarifGreenfee, round, club, player);
    return "price_round_greenfee.xhtml?faces-redirect=true";
}catch(Exception ex){
    String msg = "findTarifGreenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
}  // end method  


 public String findTarifCotisation(){ // used in greenfee_cotisation_round.xhtml 
     try{
            LOG.debug("entering findTarifCotisation");
            LOG.debug(" for found = " + round);  // nécessaire pour date
            LOG.debug(" for club = " + club);
        tarifMember = new find.FindTarifMembersData().find(club, round, conn);
            LOG.debug("TarifMember found = " + tarifMember);
        sessionMap.put("inputSelectCourse", "createTarifMember");
        sessionMap.put("inputSelectClub", "createTarifMember");
        if(tarifMember == null){
                String msgerr = LCUtil.prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return "greenfee_cotisation_round.xhtml?faces-redirect=true";
        }else{
                return "cotisation.xhtml?faces-redirect=true";
        } 
 } catch (Exception e) {
        String msg = "££ Exception in findTarifCotisation = " + e.getMessage();
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
 }
 
public String selectTravel(ECourseList ecl){ 
 try {
        LOG.debug(" entering select Travel ...");
            LOG.debug(" select Travel with Ecourselist = " + ecl.toString());
            club = ecl.getClub();
            course = ecl.getCourse();

        String msg = "Select Course Successfull l 1796 = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
        LOG.debug(msg);
        showMessageInfo(msg);
        // à vérifier 
          LOG.debug("getInputSelectCourse = " + sessionMap.get("inputSelectCourse") ); // à vérifer
            return "maps_home_club.xhtml?faces-redirect=true"; //?cmd=Rnd"; // mod 30/07/2014

    } catch (Exception e) {
            String msg = "££ Exception in selectTravel = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
 } // end class selectTravel


// solution PF15 utiliser p:chart basé sur XDEV !!!
 public String selectChart(ECourseList ecl){ //used in selectCourse.xhtml line 269
  try {
            LOG.debug("starting selectChart with ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        String msg = "Select Course Successfull = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName() + " / " + course.getIdcourse();
            LOG.debug(msg);
            showMessageInfo(msg);
          LOG.debug("getInputSelectCourse = " + sessionMap.get("inputSelectCourse") ); // à vérifer
       //PF 15 à modifier   
       String v = new Controllers.ChartController().lineModelCourse(conn, player, course);
       setLineModelCourse(v); // new 10-12-2024
          LOG.debug("Chart returned from ChartController = " + getLineModelCourse());
       return "statChartCourse.xhtml?faces-redirect=true"; 
     // return null;
    } catch (Exception e) {
            String msg = "££ Exception in selectChart = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
} // end class selectTravel


public String gRpcServer(String param){
  try{
            LOG.debug("entering gRpcServer with param = " + param);
            
            var v = new Controllers.GrpcController().startServer();
             LOG.debug("gRpcServer started = " + v);
            
      /*  io.grpc.Server server = io.grpc.ServerBuilder
                .forPort(8083)
                .addService(new grpc.OrderServiceImpl()).build();
           LOG.debug("gRpcServer implemented ! " + server.getClass().toString());
        server.start();
           , listening op port 8081");
        */   
           String s = new grpc.__ApplicationClient().sendMessage();   
           LOG.debug("retour send message = " + s);
           
      //     server.awaitTermination();
           LOG.debug("gRpcServer awaiting termination = " + param);
           
   //        var w = new Controllers.GrpcController().stopServer();
   //         LOG.debug("result stopServer = " + w);
           
           
        return null;
   }catch (Exception e){
            String msg = "CourseController - grpcServer : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } 
} // end method HandicapModel

public void checkMail(String ini){
        LOG.debug("starting checkMail with : " + ini);
///    utils.CheckingMails.main(ini); // argument bidon !!
        LOG.debug("ending checkMail with : " + ini);
}
public void newMessageFatal(String ini){
        LOG.debug("starting newMessageFatal with : " + ini);
///    utils.CheckingMails.main(ini); // argument bidon !!
        LOG.debug(ini);
     //   utils.LCUtil lcu = new utils.LCUtil();
 //       utils.LCUtil.showMessageFatalOld(ini);
        LOG.debug("ending newMessageFatal with : " + ini);
}
public void to_reset_menu(String ini){ // workaround : vient de menu et n'accepte pas le String en retour 
    reset(ini);
    player = new Player();
    sessionMap.put("playerid", 0);
    sessionMap.put("playerlastname", "");
    sessionMap.put("playerage", 0);
    String msg = "Reset PLAYER done = " + ini;
    LOG.debug(msg);
    showMessageInfo(msg);
    
}

// new 14/08/2014, called from menu
public String reset(String ini){
  try{
        LOG.debug("starting reset with : " + ini);
//08/08/2022    charts.ChartsBarModel.setBarModel(null);
   // charts.ChartsBarModel.
//    charts.ChartsLineModel.setLineModel(null);
//    charts.HandicapModel.setHandicapModel(null);
    lists.AllFlightsList.setListe(null);  // new 22/04/2017
    
    lists.HandicapList.setListe(null);
    lists.InscriptionList.setListe(null);
    lists.InscriptionListForOneRound.setListe(null); // new 06/10/2024
    lists.PlayersList.setListe(null);
    lists.ParticipantsRoundList.setListe(null);
    lists.ParticipantsStablefordCompetitionList.setListe(null);
    lists.MatchplayList.setListe(null);
    lists.PlayedList.setListe(null);
    lists.PlayersList.setListe(null);
    lists.RecentRoundList.setListe(null);
    lists.ScoreCardList1EGA.setListe(null);
    lists.CourseList.setListe(null);
    lists.ScoreCardList3.setListe(null);
    lists.UnavailableListForDate.setListe(null);
    lists.RoundPlayersList.setListe(null);
    lists.SubscriptionRenewalList.setListe(null);
    lists.FlightAvailableList.setListe(null);
    lists.ClubDetailList.setListe(null);
//    lists.ClubCourseTeeListAll.setListe(null);
    lists.ClubCourseTeeListOne.setListe(null);
    lists.CompetitionInscriptionsList.setListe(null);
    lists.CompetitionDescriptionList.setListe(null);
    lists.CompetitionRoundsList.setListe(null);
    lists.CompetitionStartList.setListe(null);
 //      LOG.debug("CompetitionStartList setted to null");
    lists.HandicapIndexList.setListe(null);
    lists.CourseListForClub.setListe(null);
    lists.ClubList.setListe(null);
    lists.ClubsListLocalAdmin.setListe(null);
    lists.LocalAdminCotisationList.setListe(null);
    lists.LocalAdminGreenfeeList.setListe(null); // new 26/09/2022
    lists.SystemAdminSubscriptionList.setListe(null); // new 22-02-2024
    lists.CoursesListLocalAdmin.setListe(null);
    lists.ProfessionalClubList.setListe(null);
    lists.LessonProList.setListe(null);
    lists.ProfessionalListForClub.setListe(null); // new 31-05-2021
    lists.RegisterResultList.setListe(null);
    lists.ProfessionalListForPayments.setListe(null); // new 02-02-2023
    lists.FindCountListProfessional.setListe(null); // new 10-02-2023
  //  lists.UnavailableListForDate.setListe(null);
//        LOG.debug("ending  initialize lists = " + ini);
    find.FindSlopeRating.setListe(null);  // new 22/6/2015
    find.FindInfoStableford.setListe(null);
    find.FindCurrentSubscription.setListe(null); // new 04/02/2017
    find.FindTeeStart.setListe(null);
    lists.SunriseSunsetList.setListe(null); // new 09/05/2017
    Controllers.LoggingUserController.setText("first start"); // new 16-09-2024
    calc.CalcMatchplayResult.setFinalResult(null); // new 02/09/2022
    
 //2. entites
    if(ini.equals("non_belgian")){ 
        LOG.debug("starting reset with non_belgian : " + ini);
            player = new Player();
            LOG.debug("new player : " + player);
        }
    if(ini.equals("from login")){ 
//           LOG.debug("starting reset from login : " + ini);
        player = new Player();
           LOG.debug("login : player set to new() : " + player);
        }

    // reset droppedplayers new 27/02/2022
         if(player != null){
      //       LOG.debug("before clearDroppedPlayers (null)");
             player.clearDroppedPlayers();
      //       LOG.debug("after clearDroppedPlayers (null)");
             player.clearDraggedPlayers();
//             LOG.debug("after player.setDroppedPlayers (null)");
         }
    player2 = new Player();
    club = new Club();
    // club.setUnavailableStructure(null) ;
    course = new Course();
    handicap = new Handicap();
    handicapIndex = new HandicapIndex();
    hole = new Hole();
    matchplay = new Matchplay();
    // player = no
    inscription = new Inscription();
    round = new Round();
//    scorecard = new EScoreCardList();
    scoreMatchplay = new ScoreMatchplay();
//    scoreStablefordOld = new ScoreStableford();
    scoreStableford = new ScoreStableford(); // new 19-10-2021
    scoreScramble = new ScoreScramble(); // new 26/07/2015
    tee = new Tee();
    subscription = new Subscription(); // new 01/02/2017
    cotisation = new Cotisation(); // new 10-01-2019
    playingHcp = new PlayingHandicap(); // new 16/06/2017
    tarifGreenfee = new TarifGreenfee(); // new 18/03/2018
    greenfee = new Greenfee();
    tarifMember = new TarifMember(); // new 101-01-2018
    creditcard = new Creditcard();
    creditcard.setPaymentOK(false);
    holesGlobal = new HolesGlobal();
  //  audit = new Audit(); // new 13-06-2019
    login = new LoginBeanSecurity();
    localAdmin = new Player(); // new 26-03-2019
    password = new Password();
    cptFlight = 0;  // iterations sur 
    unavailable = new EUnavailable();
    setProgress1(0); // mod 30-07-2025 was null
    competition = new ECompetition();
    setFilteredCourses(null);
    setFilteredClubs(null);
 //   setFilteredPlayers(null);
  //  treeBean = new TreeBeanController();
    
 //      LOG.debug("ending initialize entites , param = " + ini);
 //   return "reset OK "; // on retourne d'où on vient
    return "welcome.xhtml?faces-redirect=true";
}catch (Exception ex){
            LOG.error("error in reset ! " + ex);
            showMessageFatal("Exception reset  = " + ex.toString());
            return "error";
        }
}

//public void createCourse(){
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

// public void createTee(){ mod 24-08-2023
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

public void createHole(){
  try{ // mod 28/02/2022
    if(new create.CreateHole().create(club, course, tee, hole, STROKEINDEX, conn)){
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

    public void processChecked(AjaxBehaviorEvent e) {
        // doing some stuff here
        LOG.debug("processChecked !!");
    }
    public void convertYtoM() {  // mod 04-12-2017 
        hole = utils.ConvertYardsToMeters.convertYtoM(hole);
    }
    
  public void createRound() throws SQLException{
       LOG.debug("entering createRound ");
  //     LOG.debug("round = " + round.toString());
       round.setRoundDate(round.getRoundDateTrf()); // zone de transfert provenant de Flight, avec heure de départ
       LOG.debug("round after TRF Date = " + round.toString());

    if(new create.CreateRound().create(round, course, club, unavailable.getPeriod(),conn)){
        LOG.debug("round created : we go to inscription !!");
         setNextInscription(true); // affiche le bouton next(Inscription) bas ecran à droite
    }else{
        LOG.debug("round NOT NOT created !!");
    }
} // end method

 public String PlayerDrop(DragDropEvent<?> event) {  // used in inscriptions_other_players.xhtml
  try{
          LOG.debug("Entering PlayerDrop");
          LOG.debug("event PlayerDrop = " + event.getData().toString());
          LOG.debug("DragId = " + event.getDragId());
          LOG.debug("DropId = " + event.getDropId());
    //event is EplayerPassword !!
    EPlayerPassword epp = ((EPlayerPassword) event.getData());
    Player playerDropped = epp.getPlayer();
 //        LOG.debug("PlayerLastName dropped = " + playerDropped.getPlayerLastName());
         LOG.debug("Player dropped = " + playerDropped);
   //  if(droppedPlayers.contains(playerDropped)){
   LOG.debug("size = " + player.getDroppedPlayers().size());
      if(player.getDroppedPlayers().contains(playerDropped)){    
            String err = LCUtil.prepareMessageBean("déjà dans DroppedPlayers"); // pas possible ??
            LOG.error(err); 
            LCUtil.showMessageFatal(err);
          return null;
         }    

      if(player.getDroppedPlayers().size() > 4){   
           String msg = "There are more than 4 dropped players";
            LOG.debug(msg);
          LCUtil.showMessageFatal(msg);
          return null;
       }    
         
    find.FindTeeStart.setListe(null); // new new !!! cherché longtemps !!! utilisait les data du current player !
    List<String> ls = teeStartList(playerDropped); // new 01-04-2019
       LOG.debug("teeStartList =  " + ls.toString());
    player.getDroppedPlayers().forEach(item -> LOG.debug("before add ,list of dropped Players = " + item.getIdplayer())); 
    player.getDroppedPlayers().add(playerDropped);
      //    LOG.debug("After add, droppedPlayers = " +  Arrays.toString(player.getDroppedPlayers().toArray()));
    player.getDroppedPlayers().forEach(item -> LOG.debug("after add, list of dropped Players =" + item.getIdplayer())); 
          LOG.debug("After add, number of dropped players = " + player.getDroppedPlayers().size());
    // le remove est adapté !!  éviter de dragger le player une seconde fois !!
    
     player.getDraggedPlayers().remove(epp);
        LOG.debug("After remove, number of draggedPlayers = " + player.getDraggedPlayers().size());
        LOG.debug("After remove, draggedPlayers = " + player.getDraggedPlayers().toString());
    return null;
 } catch (Exception e) {
            String msg = "£££ Exception in PlayerDrop = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
 }
} // end method

public String PlayerRemove(Player p) {  // used in inscriptions_other_players.xhtml
 try{
           LOG.debug("entering PlayerRemove");
           LOG.debug("Player to remove from droppedPlayers = " + p);
  //      player.getDroppedPlayers().forEach(item -> LOG.debug("before remove list of dropped Players =" + item.getIdplayer())); 
  //        LOG.debug("Before remove, number of dropped players = " + player.getDroppedPlayers().size());
        player.getDroppedPlayers().remove(p);
   //         LOG.debug("After remove, droppedPlayers = " + droppedPlayers.toString());
   //       LOG.debug("After remove, number of dropped players = " + player.getDroppedPlayers().size());
        player.getDroppedPlayers(); // refresh screen
   //     player.getDroppedPlayers().forEach(item -> LOG.debug("after remove list of dropped Players =" + item.getIdplayer())); 
        return "inscriptions_other_players.xhtml?faces-redirect=true";
 }catch(Exception e){
            String msg = "£££ Exception in PlayerRemove = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method PlayerRemove
    
    
//new 16-09-201 remplace field round.roundPlayers

public List<Player> roundPlayersList() throws SQLException{
 //   LOG.debug("entering roundPlayersList");
 //   LOG.debug("with round =" + round);
   lp = new lists.RoundPlayersList().list(round, conn);
   if(lp != null){
       round.setPlayersString(Round.fillRoundPlayersString(lp));
   }
   return lp;
}

public String createInscriptionOtherPlayers(){
try{
    LOG.debug("entering CourseController.createInscriptionOtherPlayers");
    LOG.debug("round = " + round.toString());
 // a faire tester ici prélablement les rejets pour non membre et non paiement greenfee
     player.getDroppedPlayers().forEach(item -> LOG.debug("list of dropped Players =" + item.getIdplayer()));  // java 8 lambda
     LOG.debug("number of inscriptions to be created= " + player.getDroppedPlayers().size() );
  // new l
     List<Player> copy = List.copyOf(player.getDroppedPlayers()); // immutable
     for(int i=0; i < copy.size(); i++){
        LOG.debug("starting with i = "+ i);
        Player p = copy.get(i);
            LOG.debug("traitement de l'inscription pour player = " + p);
        inscription.setRound_idround(round.getIdround());
        inscription.setInscriptionInvitedBy(player.getPlayerFirstName() + "," + player.getPlayerLastName());
    // mod 09-11-2021
    inscription = new create.CreateInscription().create(round, p, player, inscription, club, course, "B", conn);
        if(inscription.isInscriptionError()){
            String msg = "Inscription other players NOT OK for player = " + p.getIdplayer() + " / " + p.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
//            continue; //The continue statement breaks one iteration (in the loop), if a specified condition occurs, and continues with the next iteration in the loop.
        }else{ // success
            LOG.debug("Joueurs inscription OK = " + p);
 //           LOG.debug("i = " + i);
            player.getDroppedPlayers().removeIf(item -> item.getIdplayer().equals(p.getIdplayer()));
            LOG.debug("after remove length droped = " + player.getDroppedPlayers().size());
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

public String createInscription(){
try{
        LOG.debug("entering createInscription");
        LOG.debug("with round = " + round);
    Player invitedBy = player;
        LOG.debug("round inscription = "+  round);
        LOG.debug(" inscription = "+  inscription);
 //   int ret = new create.CreateInscription().create(round, player, invitedBy, inscription, club, course, "A" ,conn); // mod 10/11/2014
    inscription = new create.CreateInscription().create(round, player, invitedBy, inscription, club, course, "A" ,conn); // mod 10/11/2014
    LOG.debug("inscription returned  createInscription = " + inscription);
  
    if( !inscription.isInscriptionError()){ // no errors !!!
          String msg =  LCUtil.prepareMessageBean("inscription.ok") + round + inscription
     //        + " <br/> player = " + player.getIdplayer()
             + " <br/> player name = " + player.getPlayerLastName()
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
    
    
 /*     if(ret == 00){
          String msg =  LCUtil.prepareMessageBean("inscription.ok") + round + inscription
     //        + " <br/> player = " + player.getIdplayer()
             + " <br/> player name = " + player.getPlayerLastName()
             + " <br/> club name = " + club.getClubName()
             + " <br/> course name = " + course.getCourseName()
           ;
          LOG.info(msg);
          inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
      //    return "welcome.xhtml?faces-redirect=true";
          return "inscription.xhtml?faces-redirect=true";
      }
         */
  }catch(Exception ex){
    String msg = "CreateInscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}  
return null;  
}   //end method CreateInscription


  
public void existCountry(){
    LOG.debug("country name = " + country.getName());
    LOG.debug("player id = " + player.getIdplayer());
}

public String forgetPassword() throws Exception{
try{
    LOG.debug("entering forgetPassword");
    new create.CreateActivationPassword().create(conn, player); // y compris envoi du mail
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
      LOG.debug("a examiner current player = " + player); // null 
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
                player.setIdplayer(null);
                String msg = "You are " + differenceInMinutes + " minutes too late for the reset of your Password " 
                        + activation.getActivationPlayerId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return "login.xhtml?faces-redirect=true";
           }  
   // on récupère le playerid dans activation
   // transférer activation dans 
   //player.setIdplayer(activation.getActivationPlayerId());
 //  EPlayerPassword epp = new EPlayerPassword();
 //  epp.setPlayer(player);
 //  epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
 //  player= epp.getPlayer(); // partial
 //  password = epp.getPassword();
   // checkpassword param = activation seulement
  // var v  = new PasswordController().resetPassword(epp, activation, conn); // delete activation row, update password
   var v  = new PasswordController().resetPassword(activation, conn); // delete activation row, update password
   player = v.getPlayer(); // complete
  
    if(player != null) { 
        String msg = ("The password reset was asked by " + player.getIdplayer());
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
       activation = new read.ReadActivation().read(conn, activation);
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
                player.setIdplayer(null);
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
   String s = new ActivationController().check(activation, conn);
   LOG.debug("string s = " + s);
  //  if(new PasswordController().checkPassword(uuid, conn)){  //true
    if(player != null) { 
  //      String msg = ("The activation is a success -  Welcome new  player : " + );
        String msg =  LCUtil.prepareMessageBean("player.welcome" + player.getPlayerFirstName());
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
     EPlayerPassword epp = new EPlayerPassword();
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
  public String validateExistingPassword() throws SQLException { // coming from password_modify
      // used in modify_password_.xhtml pour afficher 2e panelGrid
try{
        LOG.debug("entering validateExistingPassword");
        LOG.debug(" with player = " + player);
        LOG.debug(" with password = " + password);
        LOG.debug(" with currentPassword = " + password.getCurrentPassword());  // user input to be validated
        Password passwordtrf = password;
     //   LOG.debug("password Player = " + player.getPlayerPassword());
      EPlayerPassword epp = new EPlayerPassword();
      epp.setPlayer(player);
      epp.setPassword(password); // utile ??
        LOG.debug("password transfered to ReadPlayer= " + epp.getPassword());
      epp = new read.ReadPlayer().read(epp, conn); // 2e version, la première reste valable output = player only
        LOG.debug("epp returned from LoadPlayer = " + epp);
      password = epp.getPassword();
      password.setCurrentPassword(passwordtrf.getCurrentPassword());
      epp.setPassword(password);
        if(new find.FindPassword().passwordMatch(epp, conn)){   // is true
                String msg = "existing password correct ! ";
                  LOG.debug(msg);
  ///                password.setShowCurrentPassword(true);
//                  player.setWrkpassword(""); //fonctionne mais inutile
// aller vers create-password directement
                  passwordVerification("OK");
                  return "password_create.xhtml?faces-redirect=true";
        }else{
                  String msg = "validateExistingPassword : old password NOT correct ! ";
                  LOG.error(msg);
           //       LCUtil.showMessageFatal(msg);
                  passwordVerification("KO");  // blocage 15 min après 3 erreurs
                  return null;
             } 
   }catch(Exception ex){
    String msg = "validateExistingPassword Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method
  
public String modifyPassword() throws SQLException, Exception{
 try{
        LOG.debug("entering modifyPassword");
        LOG.debug("with entite Password = " + password);
    EPlayerPassword epp = new EPlayerPassword();
    epp.setPassword(password);
    epp.setPlayer(player);
    if(new update.UpdatePassword().update(epp, conn)){ // true
        LOG.debug("boolean returned from modifyPassword is 'true' ");
        return "login.xhtml?faces-redirect=true";
    }else{
        LOG.debug("boolean returned from modifyPassword is 'false' ");
        return null;
    } 
  }catch(Exception ex){
        String msg = "modify Password Exception ! " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
}    
} // end method

public String findTarifGreenfeeEcl(ECourseList ecl){ // utilisé ??
try{
            LOG.debug("entering findTarif");
            LOG.debug(" findTarif with ecl = " + ecl.toString());
        club = ecl.getClub(); //.getClub();
        course = ecl.getCourse(); //getCourse();
        round = ecl.getRound(); //.getRoun; //d();

        tarifGreenfee = new find.FindTarifGreenfeeData().find(round, conn);
        if(tarifGreenfee == null){
            String msg = "No Tarif available for this course";
            LOG.error(msg) ;
            showMessageFatal(msg);
        }else{
            String msg = "Tarif returned = " + tarifGreenfee.toString();
            LOG.info(msg);
            showMessageInfo(msg);
        }
        // a faire : extraire le prix de la partie !!!
      
   // return null;
    return "tarif_greenfee_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in findTarifGreenfeeEcl ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} //end method



public String findWeather(){
 try{
    LOG.debug("starting findWeather");
    LOG.debug("club = " + club.toString());
    LOG.debug("player = " + player.toString());
    LOG.debug("round = " + round.toString());
        LOG.debug("just before findWeather");
 //   String weather = new find.FindWeather().currentWeatherByCityName(club);
    String weather = new find.FindOpenWeather().find(club, conn); // new 03-11-2021 openweather
    if(weather == null){
          LOG.debug("Weather returned from findWeather is null ");
       inscription.setWeather("Weather returned from findWeather is null");
    } else{
          LOG.debug("weather data is  OK = " + weather);
         //        DialogController.showWeather();
      inscription.setWeather(weather);
      inscription.setShowWeather(true);
    } 
     return null; // to inscription.xhtml
  }catch(Exception ex){
    String msg = "finWeather Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  //end method   

public String selectTarif(ECourseList ecl){
try{
    LOG.debug("entering selectTarif");
           LOG.debug("  selectTarif, ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
    return "tarif_greenfee_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in selectTarif ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} //end method

public String selectRound(){
 try{
         LOG.debug("entering selectRound");
         LOG.debug("  with Round = " + round);
      var b = Controllers.DialogController.closeDialog("dialogRound.xhtml"); // new 05/10/2024
          LOG.debug("  with dialogRound() closed = " + b);
      if(sessionMap.get("inputSelectRound").equals("INSCRIPTION")){
          
          List<ECourseList> li = new lists.InscriptionListForOneRound().list(round,conn);
          LOG.debug("  with Club = " + li.getFirst().getClub());
          LOG.debug("  with Course = " + li.getFirst().getCourse());
          LOG.debug("  with Round = " + li.getFirst().getRound());
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

public String selectRecentInscription(ECourseList ecl){
 try{
           LOG.debug("entering selectRecentInscrition");
           LOG.debug("  selectRecentInscription, ecl = " + ecl);
           Controllers.DialogController.closeDialog("dialogRound.xhtml"); // new 05/10/2024
 //          LOG.debug("filteredInscriptions = " + getFilteredInscriptions());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
    //       LOG.debug("on cherche le nombre de joueurs  déjà inscrits et leur nom"); 
     LOG.debug("  at this crucial moment round = = " + round);
    
 //       lp = new lists.RoundPlayersList().list(round, conn);
        lp = new lists.RoundPlayersList().list(round, conn);
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
    

public String inputTarifMembersCotisation(){  // used in tarif_members.xhtml
try{
        LOG.debug("entering inputTarifMembersCotisation with Members tarif = " + tarifMember);
        LOG.debug("entering tarif members for club = " + club.toString());
     tarifMember = new Controllers.TarifMemberController().inputTarifMembersCotisation(tarifMember);
     return null; // retourne d'où il vient
}catch(Exception ex){
    String msg = "inputTarifMembers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method
public String showTarifGreenfee(String idcourse){
try{
//     LOG.debug("entering showTarifgreenfee !");
//     LOG.debug("entering showTarifGreenfee for round = " + round);
//     LOG.debug("entering showTarifGreenfee for course = " + course);
//     LOG.debug("entering showTarifGreenfee for idcourse = " + idcourse); // en réalité pas nécessaire !
     tarifGreenfee = new find.FindTarifGreenfeeData().find(round, conn);
     String msg = LCUtil.prepareMessageBean("tarif.greenfee.show") + tarifGreenfee.showTarifGreenfee();
        LOG.info(msg);
        showMessageInfo(msg);
        return null; // plus general
}catch(Exception ex){
    String msg = "Exception on showTarifGreenfee! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method     

 public String inputTarifGreenfee(String param){  // new 28/04/2022
try{
        LOG.debug("courseC : entering inputTarifGreenfee with param " + param);
        LOG.debug(" with tarif =  " + tarifGreenfee);
     switch(param){
            case "PE" -> {
                tarifGreenfee.setTarifCourseId(course.getIdcourse());
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeePeriods(tarifGreenfee);
                return "tarif_greenfee_periods.xhtml?faces-redirect=true";
            }
            case "BA" -> {
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeeBasic(tarifGreenfee);
                return "tarif_greenfee_basic.xhtml?faces-redirect=true";
            }
            case "DA" -> {
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeeDays(tarifGreenfee);
                return "tarif_greenfee_days.xhtml?faces-redirect=true";
            }
            case "EQ" -> {
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeeEquipments(tarifGreenfee);
                return "tarif_greenfee_equipments.xhtml?faces-redirect=true";
            }
            case "HO" -> {
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeeHours(tarifGreenfee);
                return "tarif_greenfee_hours.xhtml?faces-redirect=true";    
            }
            case "TW" -> {  // months twilight
                showMessageInfo(tarifGreenfee.toString());
                tarifGreenfee = new Controllers.TarifGreenfeeController().inputTarifGreenfeeTwilight(tarifGreenfee);
                return "tarif_greenfee_twilight.xhtml?faces-redirect=true";    
            }
            case "PR" -> {
                showMessageInfo(tarifGreenfee.toString());
                return null;
            }
            default -> {
                String msg = "failed in default switch";
                LOG.error(msg);
                showMessageFatal(msg);
                return null;
            }
       } // end switch
 
}catch(Exception ex){
    String msg = "inputTarifGreenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

 public String createUnavailablePeriod(){
try{   // from unavailable_period.xhtml
        LOG.debug("entering createUnavailablePeriod!");
       LOG.debug("entering createUnavailablePeriod for club = " + club);
   //   unavailablePeriod.setIdclub(club.getIdclub()); 
      unavailable.getPeriod().setIdclub(club.getIdclub()); 
        String msg = "Indisponibilité to be created = " + unavailable.getPeriod();
        LOG.info(msg);
    //    showMessageInfo(msg);
     
    if(new create.CreateUnavailablePeriod().create(unavailable.getPeriod(), conn)){
        unavailable.getPeriod().setStartDate(null);  // presentation screen
        unavailable.getPeriod().setEndDate(null);    // presentation screen
        msg = "Unavailable Period created = " + unavailable.getPeriod();
        LOG.info(msg);
        showMessageInfo(msg);
        return null;
    }else{
        msg = "Unavailable is NOT created !";
        LOG.debug(msg);
        showMessageFatal(msg);
        return null;
    }
}catch(Exception ex){
    String msg = "Exception in createUnavailable! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
//return null; // à modifier
} // end method
 
public String createTarifMembers() throws SQLException, Exception{
try{
       LOG.debug("entering createTarifMembers with tarifMember = " + tarifMember);
       LOG.debug("for club = " + club);
    tarifMember.setTarifMemberIdClub(club.getIdclub()); // new 17-05-2021
 //   if(new create.CreateTarifMember().create(tarifMember, club, conn)){
    if(new create.CreateTarifMember().create(tarifMember, conn)){
        String msg = "Tarif is created ";
        LOG.info(msg);
        showMessageInfo(msg);
      //  return null; // retourne d'où il vient
        return "tarif_members_menu.xhtml?faces-redirect=true";
    }else{
         String msg = "Tarif is NOT created ";
         LOG.error(msg);
         showMessageFatal(msg);
      //  return null;
        return "tarif_members_menu.xhtml?faces-redirect=true";
    }
}catch(Exception ex){
    String msg = "Exception in createTarifMembers! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

public String createTarifGreenfee() throws SQLException, Exception{
try{
       LOG.debug("entering createTarifGreenfee !");
       LOG.debug("with tarifGreenfee = " + tarifGreenfee);
       LOG.debug("with club = " + club);
    if(new create.CreateTarifGreenfee().create(tarifGreenfee, club, conn)){
    //    String msg = " TarifGreenfee Created = " + tarifGreenfee;
        String msg = " TarifGreenfee Created ! ";
        LOG.info(msg);
        showMessageInfo(msg);
        return "welcome.xhtml?faces-redirect=true"; // retour vers menu = crash !!
  //      return null;
    }else{
        String msg = "Fatal Error creation tarif Greenfee";
        LOG.debug(msg);
        showMessageFatal(msg);
        LOG.debug("after show message");
        return "welcome.xhtml?faces-redirect=true";
    //    return "tarif_greenfee_menu.xhtml?faces-redirect=true";
   //     return "welcome.xhtml";
    }
}catch(Exception ex){
    String msg = "Exception in createTarifGreenfee! " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return null;
}    
} // end method

public String deleteTarifGreenfee(String year) throws SQLException, Exception{
 try{
            LOG.debug("entering deleteTarifGreenfee");
            LOG.debug("with course = " + course);
            LOG.debug("with TarifGreenfee = " + tarifGreenfee);
   // TODO  remplacer hard coded year par dateSeasons [0][0] et retrouver YYYY        
        tarifGreenfee.setTarifCourseId(course.getIdcourse());
        
  //      String s = tarifGreenfee.getStartDate().format(ZDF_DAY);
        if(new delete.DeleteTarifGreenfee().delete(tarifGreenfee, year, conn)){
            String msg = "TarifGreenfee deleted = " + tarifGreenfee; 
            LOG.info(msg);
  //          showMessageInfo(msg);
        }else{
            String msg = "Result of deleteTarifGreenfee is NOT OK = " + tarifGreenfee;
            LOG.error(msg);
  //          showMessageFatal(msg);
        }
         return "tarif_greenfee_menu.xhtml?faces-redirect=true";
   //    
 }catch (Exception ex){
            String msg = "Exception in deleteTarifGreenfee" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteTarifGreenfee

 public String inputTarifMembersEquipments() throws SQLException, Exception{  // used in tarif_equipments.xhtml
try{
    LOG.debug("entering inputTarifMembersEquipments !");
        LOG.debug("entering for club = " + club);
   //     LOG.debug("Members priceEquipments = "  + Arrays.deepToString(tarifMember.getPriceEquipments()));
        LOG.debug(" tarifMembers  = "  + tarifMember);
        
        tarifMember = new Controllers.TarifMemberController().inputTarifMembersEquipments(tarifMember);
        return null; // retourne d'où il vient
}catch(Exception ex){
    String msg = "inputTarifMembersEquipments Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
 } // end method

  public String inputUnvailableStructure() throws SQLException, Exception{  // used in unavailable_structure.xhtml
try{
    LOG.debug("entering inputUnvailableStructure !");
        LOG.debug("entering inputUnvailableStructure for club = " + club);
        LOG.debug("entering inputUnvailableStructure for unavailable = " + unavailable);
         // TODO aller vers UnavailableController
        unavailable = new UnavailableController().inputUnvailableStructure(unavailable);
        LOG.debug("back from inputUnvailableStructure with unavailable = " + unavailable);
 //       LOG.debug("Unavailable itemStructure = "  + Arrays.deepToString(club.getUnavailableStructure().getItemStructure()));
   return null; // retourne d'où il vient

}catch(Exception ex){
    String msg = "inputUnvailableStructure Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method   
 

public String showTarifMembers() throws SQLException, Exception{
try{
//     LOG.debug("entering showTarifMembers !");
     LOG.debug("entering showTarifMembers for club = " + club);
//     tarifMember.RemoveNull(); // remove null from arrays
     String msg = LCUtil.prepareMessageBean("tarif.member.show") + "<br/" + tarifMember;
   //     + "<br/> equipment = " +  Arrays.deepToString(tarifMember.getPriceEquipments())
   //     + "<br/> comment = " + tarifMember.getComment();
        LOG.info(msg);
        showMessageInfo(msg);
  //    return "tarif_members_menu.xhtml?faces-redirect=true";
        return null;
}catch(Exception ex){
    String msg = "Exception in ShowTarifMember! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

public String showUnavailableStructure() throws SQLException{
try{
  //   LOG.debug("entering showUnavailableStructure !");
     LOG.debug("entering showUnavailableStructure for unvailable = " + unavailable);
  //   club.getUnavailableStructure().RemoveNull(); // remove null from arrays
      String msg = LCUtil.prepareMessageBean("unavailable.structure.show")
        + "<br/> Unavailable Structure = " 
   //           + Arrays.deepToString(club.getUnavailableStructure().getItemStructure())
              + unavailable.getStructure().getStructureList().toString()
//        + "<br/> date debut = " +  unavailable.getStructure().getStartDate()
 //       + "<br/> date fin = " +  unavailable.getStructure().getEndDate()
        ;

        LOG.info(msg);
        showMessageInfo(msg);
   //      return "tarif_members_menu.xhtml?faces-redirect=true";
        return null; // fonctionne pas j'essaie
        
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method    

public String showUnavailablePeriod() {   // version 1 method overloading sans paramètre from unavailable_show.xhtml
try{
      LOG.debug("entering showUnavailablePeriods for club = " + club);
  //    LOG.debug("entering showUnavailablePeriods for ecl = " + ecl);

      EUnavailable lun = new lists.UnavailableListForDate().list(LocalDateTime.now(),club, conn);
  // 3 linges suivantes le 06-06-2020
   //  if(lun.isEmpty()){   //? works ??
   LOG.debug("line 01");
     if(lun == null){  
         LOG.debug("pas de période d'indisponibilité");
         unavailable = null;
     }else{
         unavailable = lun; //.get(0);   // first element of the list
         String msg = "showUnavailablePeriods - first element of list is = " + lun.toString();
         LOG.debug(msg);
         showMessageInfo(msg);
  //       DialogController.showUnavailable(); // new 22-01-2021 fonctionne pas !
         return "unavailable_show.xhtml?faces-redirect=true";
     }
 return null;
  
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method    

public EUnavailable showUnavailablePeriods() throws SQLException{   // version 2 method overloading sans paramètre from unavailable_show.xhtml
try{
      LOG.debug("entering showUnavailablePeriods for club = " + club);
  //    LOG.debug("entering showUnavailablePeriods for ecl = " + ecl);
     unavailable = new lists.UnavailableListForDate().list(LocalDateTime.now(),club, conn);
//        LOG.debug("showUnavailablePeriods - first element of list is = " + lun.get(0).toString());
   return unavailable;
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method    

public String showUnavailablePeriods(Club c) throws SQLException{   // version 1 method overloading sans paramètre from unavailable_show.xhtml
try{
        LOG.debug("entering showUnavailablePeriods for club input = " + c);
     EUnavailable lun = new lists.UnavailableListForDate().list(LocalDateTime.now(),c, conn);
     if(lun == null){
         LOG.debug("lun is null");
         LOG.debug("no unavailabilities known");
         unavailable = null;
         return "unavailable_show.xhtml?faces-redirect=true";
     }else{
         unavailable = lun;
            LOG.debug("showUnavailablePeriods -  element is = " + lun);
         return "unavailable_show.xhtml?faces-redirect=true";
     }
}catch(Exception ex){
    String msg = "Exception in showUnavailablePeriods!! " + ex;
    LOG.error(msg);
    showMessageFatal(msg);
    return null;
}    
} // end method    

public String showUnavailablePeriods(ECourseList ecl) throws SQLException{
    // version 2 method overloading from selectInscription.xhtml
try{
  //    LOG.debug("entering showUnavailablePeriods for ecl = " + ecl);
    club = ecl.getClub();
    EUnavailable lun = new lists.UnavailableListForDate().list(LocalDateTime.now(),ecl.getClub(), conn);
       LOG.debug("showUnavailablePeriods -  element of list is = " + lun);
    return "unavailable_show.xhtml?faces-redirect=true";
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method    


 public String selectedCompetitionScoreStableford(ECompetition ec) throws SQLException {
   // public String selectedCompetitionScoreStableford(CompetitionDescription cd) throws SQLException {
    // dans le cadre d'un compétition
    try{
          LOG.debug(" ... entering selectedCompetitionScoreStableford "); 
          LOG.debug("   with CompetitionData = " + ec.getCompetitionData());
          LOG.debug("   with CompetitionDescription = " + ec.getCompetitionDescription());
      round.setIdround(ec.getCompetitionData().getCmpDataRoundId());
      round = new read.ReadRound().read(round, conn);
      Player competitionPlayer = new Player();
      competitionPlayer.setIdplayer(ec.getCompetitionData().getCmpDataPlayerId());
         LOG.debug("id player for registration competition = " + competitionPlayer.getIdplayer());
      competitionPlayer = new read.ReadPlayer().read(competitionPlayer, conn);
      club.setIdclub(ec.getCompetitionDescription().getCompetitionClubId());
      club = new read.ReadClub().read(club, conn);
      course.setIdcourse(ec.getCompetitionDescription().getCompetitionCourseId()); 
      course = new read.ReadCourse().read(course, conn);
 /// UPDATE or INSERT ??     
      int rows = new find.FindCountScore().find(conn, competitionPlayer, round, "rows");
           LOG.debug("we are back with FindCountScore = " + rows);
      if(rows != 0){ // UPDATE thus prefilling
               LOG.debug("there are : " + rows + " ==> this is a UPDATE, thus we are prefilling the score !");
       //        scoreStableford = read.ReadScoreStablefordWHS.read(competitionPlayer, round, conn);
       // mod 28/06/2022        
       // mod 17-08-2023 non testé ajouté tee !!
       // attention !!
               scoreStableford = new StablefordController().completeScoreStableford(player, round, tee, conn);
                  LOG.debug("Score is NOW prefilled with = !! " + scoreStableford);
        }else{
               LOG.debug("this is a CREATION! no prefilling "); // + Arrays.toString(scoreStableford.getParArray()));
//             scoreStableford.setShowLinePoints(false); // ne PAS afficher la ligne des points
        }
    sessionMap.put("competitionPlayer", String.valueOf(competitionPlayer.getIdplayer()));
    sessionMap.put("scoreType", "COMPETITION");
    LOG.debug("sessionMap - competitionPlayer = " + sessionMap.get("competitionPlayer"));
    
    String s = "score_stableford.xhtml?faces-redirect=true"
         + "&cmd=COMPETITION"
         + "&playerId=" + competitionPlayer.getIdplayer()
         + "&playerName=" + competitionPlayer.getPlayerLastName()
        .replaceAll(" ", "%20").replaceAll("&","&amp;");
      LOG.debug("string redirect to score_stableford = " + s);
return s;
  }catch(Exception ex){
     String msg = "ERROR in selectedCompetitionScoreStableford ! " + ex;
     LOG.error(msg);
     showMessageFatal(msg);
     return null;
}
}  //end method
public String selectedScoreToRegister(ECourseList ecl, String type) throws SQLException { // used in selectStablefordRounds.xhtml
    LOG.debug(" ... entering selectedScoreToRegister with CURRENT player = " + player.getIdplayer()); 
    LOG.debug("with ecl = " + ecl);
    LOG.debug("with type = " + type);
 try{
     round = ecl.getRound();
     if(Round.GameType.STABLEFORD.toString().equals(round.getRoundGame())){
           return LoadScoreStableford(ecl, type);
     }

     LOG.debug("!!! No register score for = " + round.getRoundGame());
        return null;  // fake
}catch(Exception ex){
    String msg = "scoreStableford Exception ! " + ex;
   LOG.error(msg);
   showMessageFatal(msg);
   return null;  
}
}
/*
public String RegisterScoreMatchplay(ECourseList ecl, String type) throws SQLException {       
   try{
          LOG.debug(" ... entering RegisterScoreMatchplay "); 
          LOG.debug("   with scoreMatchplay = " + scoreMatchplay);
          LOG.debug("   with type = " + type); // pas utilisé ??
          LOG.debug(" ..with ecl = " + ecl);
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        tee = ecl.getTee(); 
        var vv = new read.ReadParArray().read(conn, player, course);
        scoreMatchplay.setParArray(vv);
   // rechercher infos db
      if(round.getScoreMatchplay() != null){ // score enregistré précedemment en db
         scoreMatchplay.setstrokesEur(round.getScoreMatchplay().getstrokesEur());
         scoreMatchplay.setstrokesUsa(round.getScoreMatchplay().getstrokesUsa());
         scoreMatchplay.setResult(round.getScoreMatchplay().getResult());
      }
 // new 24-09-2021
    // compléter nom des teams et des joueurs ou les recupérer de db !!??
        var v = new lists.ParticipantsRoundList().list(round, conn);
        if(v.size() == 4){
            String A1 = v.get(0).getInscription().getInscriptionMatchplayTeam()
                    + " - " + v.get(0).getPlayer().getPlayerLastName();
            String A2 = ", " + v.get(1).getPlayer().getPlayerLastName();
            scoreMatchplay.setPlayersA(A1 + A2);
            String B1 = v.get(2).getInscription().getInscriptionMatchplayTeam()
                    + " - " + v.get(2).getPlayer().getPlayerLastName();
            String B2 = ", " + v.get(3).getPlayer().getPlayerLastName();
            scoreMatchplay.setPlayersB(B1 + B2);
        }
        if(v.size() == 2){
            String A1 = v.get(0).getInscription().getInscriptionMatchplayTeam() + " - " + v.get(0).getPlayer().getPlayerLastName();
            scoreMatchplay.setPlayersA(A1);
            String B1 = v.get(1).getInscription().getInscriptionMatchplayTeam() + " - " + v.get(1).getPlayer().getPlayerLastName();
            scoreMatchplay.setPlayersB(B1);
         }
   return "score_matchplay.xhtml?faces-redirect=true";
 }catch(Exception ex){
    String msg = "RegisterScoreMatchplay exception" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
 }
} 
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
 public String LoadScoreStableford(ECourseList ecl, String type) throws SQLException {    // used in selectStablefordRounds.xhtml
   try{
          LOG.debug(" ... entering LoadScoreStableford "); 
          LOG.debug("   with type = " + type);
          LOG.debug(" ..with ecl = " + ecl);
     //     LOG.debug(" ..with player = " + player);
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        
     //       LOG.debug("round = " + round);
    //        LOG.debug("RoundGame = " + round.getRoundGame());
        tee = ecl.getTee();
        if(type.equals("competition")){
            player.setIdplayer(ecl.getPlayer().getIdplayer());
            player.setPlayerLastName(ecl.getPlayer().getPlayerLastName());
            player.setPlayerFirstName("???");
            player.setPlayerGender(ecl.getPlayer().getPlayerGender());
            round.setIdround(ecl.getRound().getIdround());
            course.setIdcourse(ecl.getCourse().getIdcourse());
            club.setIdclub(ecl.getClub().getIdclub());
        }
       scoreStableford = new StablefordController().completeScoreStableford(player, round, ecl.getTee(),conn);
    //      LOG.debug("we use this completed scoreStableford = " + scoreStableford); 
   sessionMap.put("scoreType", "INDIVIDUAL");
 return "score_stableford.xhtml?faces-redirect=true";
  }catch(Exception ex){
    String msg = "LoadScoreStableford Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
} finally{
    //    player = player_save;
           LOG.debug("player restored to : " + player.getIdplayer());
//return 0;
}    
} // end method

public String calculateScoreStableford() throws SQLException, Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
   LOG.debug("entering " + methodName);
 //  LOG.debug("with param = " + param);
   LOG.debug(" for Round = " + round);
   LOG.debug(" for Course = " + course);
   LOG.debug(" for current Player = " + player);
   LOG.debug(" with scoreType = " + sessionMap.get("scoreType")); // COMPETITION or INDIVIDUAL
   LOG.debug(" with competition Player = " + sessionMap.get("competitionPlayer"));
   LOG.debug(" for competition = " + competition);
try{
//       LOG.debug(" entering with score = " + scoreStableford);
        Player p;
  if(sessionMap.get("scoreType").equals("COMPETITION")){
         LOG.debug("handling with scoreType = " + sessionMap.get("scoreType"));
      int playerid = Integer.parseInt(sessionMap.get("competitionPlayer").toString());
      p = new Player();
      p.setIdplayer(playerid);
      p = new read.ReadPlayer().read(p, conn);
  }else{ // is INDIVIDUAL
       LOG.debug("handling with scoreType = " + sessionMap.get("scoreType"));
      p = player;
  }
  
      if(round.getRoundDate().isBefore(START_DATE_WHS) ){
          String msg = "EGA Handicapping ! - THIS part is invalidated !";
          LOG.error(msg);
          showMessageFatal(msg);
          return null;
      }//else{
           LOG.debug(" going to calculations ! " );
         scoreStableford = calc.CalcScoreStableford.calc(p, scoreStableford, round, course, tee, conn);
         
        // setScoreList(scoreStableford.getScoreList());
         LOG.debug("scoreStableford completed = " + scoreStableford);
         
    // } //end else

//   return null; mod 01/07/2022
     return "score_stableford.xhtml?faces-redirect=true";
}catch(Exception ex){
    String msg = "Exception in calculateScoreStableford() " + ex;
   LOG.error(msg);
   showMessageFatal(msg);
   return null;
}    
} // end method
/*
public String calculateScoreMatchplay() throws SQLException, Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
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
public String loadStatisticsTable() throws SQLException, Exception{  // executed before include_statistics.xhtml
   LOG.debug("entering loadStatisticsTable() !");
try{
          LOG.debug("scoreStableford = " + scoreStableford);
       var v = new read.ReadStatisticsList().load(conn, player, round);
       scoreStableford.setStatisticsList(v);
       if(utils.LCUtil.isArrayAllZeroes(scoreStableford.getStrokeArray())){
           int[] arr = scoreStableford.getScoreList().stream().mapToInt(i -> i.getStrokes()).toArray(); // create array from list
           scoreStableford.setStrokeArray(arr);
       }
   var v1 = new StablefordController().completeStatisticsListWithStrokes(scoreStableford.getStatisticsList(), scoreStableford.getStrokeArray());
   //    var v1 = new StablefordController().completeStatisticsListWithStrokes(scoreStableford.getStatisticsList(), arr);
       scoreStableford.setStatisticsList(v1);
    return "score_statistics.xhtml?faces-redirect=true";
}catch(Exception ex){
    String msg = "Exception in loadStatisticsTable()! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method

public String createStatisticsStableford() throws SQLException{
        LOG.debug(".. entering createStatisticsStableford");
        LOG.debug("round = " + round);
        LOG.debug("scoreStableford = " + scoreStableford);
   //     LOG.debug("array statistics [][] = " + Arrays.deepToString(scoreStableford.getStatistics()));
        LOG.debug("List statistics = " + scoreStableford.getStatisticsList().toString());
        
    if(new create.CreateStatisticsStableford().create(player, round, scoreStableford, conn)){
      //  String msg = "statistics created !! " + Arrays.deepToString(scoreStableford.getStatistics());
         String msg = "statistics created !! " ; //+ scoreStableford.getStatisticsList().toString();
  //       scoreStableford.setStatisticsList(null); // new 28/06/2022
        LOG.debug(msg);
        showMessageInfo(msg);
        setNextScorecard(true); // affiche le bouton next(Scorecard) bas ecran à  droite
    }else{
        LOG.debug("statistics NOT created : error !!");
    }
  return null;
}

public String createScoreStableford() throws SQLException, Exception{
   LOG.debug("entering createScoreStableford !");
try{
    LOG.debug("with round game = " + round.getRoundGame());
    LOG.debug("with competition Player = " + sessionMap.get("competitionPlayer"));
    LOG.debug("with current player = " + player.getIdplayer());
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
      p = new read.ReadPlayer().read(p, conn);
  }else{ // is INDIVIDUAL
       LOG.debug("handling with scoreType = " + sessionMap.get("scoreType"));
      p = player;
  }
   if(new create.CreateOrUpdateScoreStableford().status(scoreStableford, round, p, conn)){  // was Old
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
        competition.getCompetitionData().setCmpDataScoreDifferential(scoreStableford.getScoreDifferential());
        competition.getCompetitionData().setCmpDataScorePoints((short)scoreStableford.getAdjustedGrossScore());
           LOG.info("competition is at this moment 2 = " + competition);
        if( ! new update.UpdateCompetitionData().update(competition.getCompetitionData(), conn)){
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

public String createHandicapIndex(){
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
   LOG.debug("entering " + methodName);
   LOG.debug(" for ScoreStableford = " + scoreStableford);
   LOG.debug("for HandicapIndex = " + handicapIndex); // est non complété
try{
      if(! round.getRoundQualifying().equals("Y")){
          String msg ="No HandicapIndex creation because Round is not Qualifying !";
          LOG.error(msg);
          showMessageFatal(msg);
          return null;
      } //else{
 /*/    mod 18-04-2025   }
      handicapIndex.setHandicapScoreDifferential(BigDecimal.valueOf(scoreStableford.getScoreDifferential()));
      handicapIndex.setHandicapPlayerId(player.getIdplayer());
      handicapIndex.setHandicapRoundId(round.getIdround());
      handicapIndex.setHandicapDate(round.getRoundDate());
      handicapIndex.setHandicapPlayedStrokes((short)scoreStableford.getTotalStrokes());
      handicapIndex.setHandicapHolesNotPlayed((short)scoreStableford.getHolesNotPlayed());
      handicapIndex.setHandicapExpectedSD9Holes(scoreStableford.getExpectedSD9Holes());
         LOG.debug("for HandicapIndex completed = " + handicapIndex);
      if(! new Controllers.HandicapController().create(handicapIndex, conn)){
          String msg = "fatal error in createHandicapIndex !";
          LOG.error(msg);
          showMessageFatal(msg);
          return null;
      }
      */
      handicapIndex = new Controllers.HandicapController().create(scoreStableford,player,round, conn);
      if(handicapIndex == null){
          LOG.debug("handicapIndex is null in CourseController");
      }
  return "score_stableford.xhtml?faces-redirect=true";
}catch(Exception ex){
    String msg = "Exception in createHandicapIndex " + ex;
   LOG.error(msg);
   showMessageFatal(msg);
   return null;
}    
} // end method    
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
public void validateScoreHoleMatchplay2(){
    LOG.debug("entering validateScoreHoleMatchplay2 for hole = " );
}
        
public void validateScoreHoleMatchplay(FacesContext context, UIComponent toValidate, Object value)
      throws ValidatorException
{
LOG.debug("entering validateScoreHoleMatchplay");
    LOG.debug("entering validateScoreMatchPlay - toValidate ClientId = " + toValidate.getClientId() );
    LOG.debug("entering validateMP4 - toValidate Id = " + toValidate.getId() );
    LOG.debug("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.debug("entering validateMP4 - value = " + value.toString());
    LOG.debug("UIcomponent, getFamily = " + toValidate.getFamily());
    LOG.debug("UIcomponent, context = " + context.toString());
    LOG.debug("UIcomponent,message = " + toValidate.getClientId(context)); 
    String confirm = (String)value;
    
    String field1Id = (String) toValidate.getAttributes().get("scorePlayer11");
         LOG.debug(" validateMP4 - field1Id = " + field1Id);
         
    UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
         LOG.debug(" validateMP4 - passComponent = " + passComponent);
    String pass = (String) passComponent.getSubmittedValue();
        LOG.debug(" validateMP4 - pass1 = " + pass);
    if (pass == null){
        pass = (String) passComponent.getValue();
         LOG.debug(" validateMP4 - pass2 = " + pass);
    }
    
    if (!pass.equals(confirm)){
        LOG.debug(" validateMP4 - pass not equal confirm = " );
        String error = toValidate.getClientId(context);
        showMessageFatal(error);
   //     String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
     //   throw new ValidatorException(new FacesMessage(err));
    }
}
    


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

public String show_scorecard_empty(ECourseList ecl) throws SQLException{
        LOG.debug("entering show_scoreCard_empty with :!" + ecl.toString() );
    club.setIdclub(ecl.getClub().getIdclub());
    course.setIdcourse(ecl.getCourse().getIdcourse());
    // à verifier
    return new lists.ShowScoreList().show_empty(player, club, course, round, inscription, conn);
}

  public List<ECourseList> listRecentRounds() {
        LOG.debug("...  entering listRecentRounds " );
   try {
        return new lists.RecentRoundList().list(player, conn);
 
   } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
    } //end method
  
    public List<EClubPro> listProfessionalForClub() {
        LOG.debug("...  entering listProfessionalForClub " );
   try {
        return new lists.ProfessionalListForClub().list(conn);

   } catch (Exception ex) {
       String msg = "Exception in listProfessionalforClub! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
     }
    } //end method
  
public List<Professional> isProfessional() { // new 14-12-2021
      //  LOG.debug("...  entering isProfessional" ); loop
     //   LOG.debug("with player = " + player);
   try {
     //  int count = new find.FindCountProfessional().find(player, conn);
        var listPro = new lists.FindCountListProfessional().list(player, conn);
        if(!listPro.isEmpty()){
            playerPro = player;
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
  
//  public long getTotalCount(String name) {
 //       return customers.stream().filter(customers -> name.equals(customers.representative.name)).count();
 //   }
   public List<EPaymentPro> listProfessionalPayments() {
        LOG.debug("...entering listProfessionalPayments " );
   try {
       LOG.debug("for professional = " + player);

      return new lists.ProfessionalListForPayments().list(player, conn);
  //     return null;
   } catch (Exception ex) {
            LOG.error("Exception in listProfessionalPayments()! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
  } //end method listProfessionalPayments
   
     public List<EGreenfee> listLocalAdminGreenfee(String s) {
        LOG.debug("...entering listLocalAdminGreenfee " );
        LOG.debug("...with param = " + s );
   try {
       LOG.debug("for local admin = " + player);
       LOG.debug("with role = " + player.getPlayerRole());
       
       // param = Greenfees ==>
       // param = Members ==>
       if(sessionMap.get("inputSelectPaiement").equals("Greenfees")){
     //      return new lists.LocalAdminGreenfeeList().list(player, conn);
        // mod 23-01-2023 à vérifier
           return new lists.LocalAdminGreenfeeList().list(player, conn);
       }
   //    if(sessionMap.get("inputSelectPaiement").equals("Members")){
    //       return new lists.LocalAdminCotisationList().list(player, conn);
   //    }
       LOG.debug("error in inputSelectPaiement : INVALID");
       return null;
   } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
    } //end method
     
  public List<ESubscription> listSystemAdminSubscription(String s) {
        LOG.debug("...entering listSystemAdminSubscription " );
        LOG.debug("...with param = " + s );
        // param n'est pas utilisé !!
   try {
 //      LOG.debug("for local admin = " + player);
 //      LOG.debug("with role = " + player.getPlayerRole());
        return new lists.SystemAdminSubscriptionList().list(conn);
   } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
 } //end method
     
     
     public List<ECotisation> listLocalAdminCotisation(String s) {
        LOG.debug("...entering listLocalAdminCotisation " );
        LOG.debug("...with param = " + s );
        // param n'est pas utilisé !!
   try {
       LOG.debug("for local admin = " + player);
       LOG.debug("with role = " + player.getPlayerRole());
       
       // param = Greenfees ==>
       // param = Members ==>
       if(sessionMap.get("inputSelectPaiement").equals("Greenfees")){
     //      return new lists.LocalAdminGreenfeeList().list(player, conn);
        // mod 23-01-2023 à vérifier
           return new lists.LocalAdminCotisationList().list(player, conn);
       }
       if(sessionMap.get("inputSelectPaiement").equals("Members")){
           return new lists.LocalAdminCotisationList().list(player, conn);
       }
       LOG.debug("error in inputSelectPaiement : INVALID");
       return null;
   } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
    } //end method
  
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
      Flight flight2 = new lists.SunriseSunsetList().list(round, club, conn);
        LOG.debug("flight2 = " + flight2);
      if(flight2 == null){
          LOG.debug("flight is null !! cata");
          return null;
      }
          LOG.debug("step 1-Flight f = " + flight2.toString());
 // 2 ------------------  creation tableFlights : 1 record toutes les 12 min en partant de sunrise jusque sunset
    //    LOG.debug("timeZone tz = " + club.getClubZoneId());
  //  liste avec tousl les flights de la journée
      flightList = new lists.AllFlightsList().createTableFlights(flight2, club.getAddress().getZoneId(), conn);  // à changer  dans address.
 
      //   flightList.forEach(item -> LOG.debug("FlightList list " + item));
        LOG.debug("step 2-list created , size = " + flightList.size());
    //  boolean OK = new create.CreateTableFlights().create(flightList, course.getIdcourse(), conn);  // fake = courseid
    
       if(new create.CreateTableFlights().create(flightList, course, conn)){
            LOG.debug("boolean result create.CreateFlights = OK");
            // elimination des flights déjà réservés    
         //   flightList.forEach(item -> LOG.debug("FlightList list before " + item));
            flightList = new lists.FlightAvailableList().listAllFlights(conn);
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

public List<Matchplay> listMatchplayRounds(String formula) {
        try {
            LOG.debug("from CourseController : listMatchplayRounds for formula = " + formula);
            lists.MatchplayList mpl = new lists.MatchplayList();
            listmatchplay = mpl.getList(conn,"MP_");
       //        String [][] s = listmp.toArray(sc1);
 //             Matchplay [] countries = listmp.toArray(new Matchplay[listmp.size()]);
 //               LOG.debug("countries = " + Arrays.deepToString(countries));
 //              LOG.debug("from CourseController : listmp = " + listmp.toString());
         course.setIdcourse(listmatchplay.get(1).getIdcourse() );
                LOG.debug("setted idcourse on = " + course.getIdcourse());
         course.setCourseName(listmatchplay.get(1).getCourseName() );
         club.setIdclub(listmatchplay.get(1).getIdclub() );
         club.setClubName(listmatchplay.get(1).getClubName() );
         round.setRoundGame(listmatchplay.get(1).getRoundGame() );
         round.setIdround(listmatchplay.get(1).getIdround() );
              LOG.debug("from listmp, round = = " + round.getIdround() );
         round.setRoundName(listmatchplay.get(1).getRoundName() );
         java.util.Date d = listmatchplay.get(1).getRoundDate();
         LocalDateTime date = DatetoLocalDateTime(d);
         round.setRoundDate(date);
     //          inscription.setPlayerhasroundTeam(listmp.get(1).getPlayerhasroundTeam() );
         matchplay.setRoundNameName(listmatchplay.get(1).getRoundNameName() );
     //          matchplay.setPlayerhasroundPlayerNumber(listmp.get(1).getPlayerhasroundPlayerNumber() );
               // et d'autres ...
         return listmatchplay;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    
public List<ScoreScramble> listScrambleRounds(String formula){
 try {
        LOG.debug("entering  CourseController : listScrambleRounds with formula =  " + formula);
            lists.ScrambleList sl = new lists.ScrambleList();
            listscr = sl.getList(conn, formula);
       //        String [][] s = listmp.toArray(sc1);
 //             Matchplay [] countries = listmp.toArray(new Matchplay[listmp.size()]);
 //               LOG.debug("countries = " + Arrays.deepToString(countries));
         LOG.debug("from CourseController : listscr = " + listscr.toString());
         LOG.debug("from CourseController : listscr rounds = " + listscr.size() );
         course.setIdcourse(listscr.get(0).getIdcourse() );
                LOG.debug("setted idcourse on = " + course.getIdcourse());
         course.setCourseName(listscr.get(0).getCourseName() );
                LOG.debug("setted course name on = " + course.getCourseName() );
         club.setIdclub(listscr.get(0).getIdclub() );
         club.setClubName(listscr.get(0).getClubName() );
         round.setRoundGame(listscr.get(0).getRoundGame() );
         round.setIdround(listscr.get(0).getIdround() );
              LOG.debug("from listscr, round = " + round.getIdround() );
         round.setRoundName(listscr.get(0).getRoundName() );
         round.setRoundDate(listscr.get(0).getRoundDate());
         return listscr;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    

    public List<Matchplay> getListmatchplay() {
        return listmatchplay;
    }

    public void setListmatchplay(List<Matchplay> listmatchplay) {
        this.listmatchplay = listmatchplay;
    }

public List<ECourseList> listPlayedRounds(String formula) throws SQLException{
     LOG.debug(" ... entering listPlayedRounds WITH formula = " + formula);
    return new lists.PlayedList().list(player, conn);
 //return new lists.RecentRoundList().list(player, conn);
}

public List<ECourseList> registerStablefordResult(){// from selectStablefordRounds.xhtml
  try {
      //    LOG.debug(" ... entering registerResult ! " );
     //     LOG.debug(" ...for player =  " + player);
         return new lists.RegisterResultList().list(player,conn);
         
  } catch (Exception ex) {
            String err = "Exception in registerResult  " + ex;
            LOG.error(err);
            showMessageFatal(err);
            return null;
  } finally {  }
 } //end method

public String to_selectMatchplayRounds_xhtml(String s) {
            LOG.debug("entering to_ ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectMatchplayRounds.xhtml?faces-redirect=true";
   }

public String to_creditcard_test_xhtml(String s) {
            LOG.debug("entering to_ ... with string = " + s);
            reset(s);
            creditcard.setTotalPrice(155.6);
            creditcard.setCommunication(" prepared creditcard communication");
            creditcard.setCreditcardType("GREENFEE");
       return "creditcard_test.xhtml?faces-redirect=true";
   }
public String to_stableford_playing_hcp_xhtml(String s) {
            LOG.debug("entering to_ ... with string = " + s);
            reset(s);
            playingHcp = new PlayingHandicap();
       return "stableford_playing_hcp.xhtml?faces-redirect=true";
   }
public String to_selectPlayer_xhtml(String s) {
            LOG.debug("entering to selectPlayer_xhtml... with string = " + s);
            sessionMap.put("inputSelectHomeClub", s); 
            reset(s);
       return "selectPlayer.xhtml?faces-redirect=true";
   }

public String to_selectScrambleRounds_xhtml(String s){
            LOG.debug("entering to_selectScrambleRounds_xhtml ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectScrambleRounds.xhtml?faces-redirect=true";
   }
public String to_selectRegisteredRounds_xhtml(String s){
            LOG.debug("entering to_selectRegisteredRounds_xhtml with String = " + s);
       reset(s);
       return "selectRegisteredRounds.xhtml?faces-redirect=true&cmd=" + s;
   }

public String to_selectParticipantsRound_xhtml(String s){
            LOG.debug("entering to_selectParticpantsRound_xhtml with String = " + s);
       reset(s);
       return "select_participants_round.xhtml?faces-redirect=true&cmd=" + s;
   }

/* just for testing from menu TEST
public String to_select_tarifMembers_xhtml(Integer i) throws SQLException {
            club.setIdclub(i);
            tarifMember = new find.FindTarifMembersData().find(club,conn);
            reset(String.valueOf(i));
        return "cotisation.xhtml?faces-redirect=true";
   }
*/



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
   /*    if(s.equals(Round.GameType.STABLEFORD.toString()))
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
*/
 return "playing formule not found";   
 } catch (Exception ex) {
            LOG.error("Exception in to_select_inscription_xhtml! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
    }
}



public String to_selectStablefordRounds_xhtml(String s) {
            LOG.debug("entering to_selectStablefordRounds_xhtml with string = " + s);
            reset(s);
  //          scoreStableford.setStatisticsList(null);
       return "selectStablefordRounds.xhtml?faces-redirect=true";
   }

public String to_show_played_rounds_xhtml(String s){
       LOG.debug("entering to_show_played_rounds_xhtml with string s = " + s);
            reset(s);
            setInputPlayedRounds(s); // new 29/03/2016
       return "show_played_rounds.xhtml?faces-redirect=true&cmd=" + s; // mod 28/03/2016
   }
public String to_competition_create_description_xhtml(String s){
        LOG.debug("entering to_competition ... with string = " + s);
     reset(s);
     sessionMap.put("inputSelectClub", s);
     return "competition_create_description.xhtml?faces-redirect=true&operation=" + s;
   }
public String to_club_xhtml(String s){
        LOG.debug("entering to_club_hxtml ... with string = " + s);
            reset(s);
       club.setCreateModify(true);  // gestion button dans club.xhtml
       return "club.xhtml?faces-redirect=true&operation=" + s;
   }

public void to_selenium_createClub(String s) throws IOException{
      LOG.debug("entering to_selenium_createClub ... with string = " + s);
    reset(s);
    applicationMap.put("clubCreated", "not yet implemented");
    LOG.debug("applicationMap clubCreated = " + applicationMap.get("clubCreated"));
    new selenium.SeleniumClubTest().testing("club.xhtml");
 //   selenium.SeleniumClub().testing("club.xhtml"); // ne trouve pas si dans Test Packages
   // à modifier    
    }

public String to_course_xhtml(String s) {
        LOG.debug("entering to_course_xthml ... with string = " + s);
            reset(s);
       course.setCreateModify(true);  // gestion button dans ccourse.xhtml
       return "course.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_tee_xhtml(String s) {
        LOG.debug("entering to_tee_xthml ... with string = " + s);
            reset(s);
       tee.setCreateModify(true);  // gestion button dans tee.xhtml
       return "tee.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_clubModify_xhtml(String s){
        LOG.debug("entering to_clubModify_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       tee.setModifyClubCourseTee(true);
       sessionMap.put("inputSelectClub", s);
       if (s.equals("modify_club_improved")){
           return "selectClubModify.xhtml?faces-redirect=true";
       }else{
           sessionMap.put("inputSelectCourse", s);
           return "modify_ClubCourseTee.xhtml?faces-redirect=true";
       }
  // 02-04     course.setInputSelectCourse(s);
       
   }
public String to_clubDelete_xhtml(String s){
        LOG.debug("entering to_clubDelete_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       sessionMap.put("inputSelectClub", s);
       if(s.equals("delete_club_improved")){
           return "selectClubDelete.xhtml?faces-redirect=true";
       }
  // 02-04     course.setInputSelectCourse(s);
       
       return "deleteClubCourseTee.xhtml?faces-redirect=true";
   }

public String to_player_xhtml(String s){
         LOG.debug("entering to_player_xthml ... with string = " + s);
       reset(s);
       return "player.xhtml?faces-redirect=true";
   }

public void to_player_modify(String s){
         LOG.debug("entering to_player_modify ... with string = " + s);
  //     reset(s);
        createModifyPlayer = s;
 //      return "player_modify.xhtml?faces-redirect=true";
   }

public String to_show_handicap_whs_xhtml(String s){
        LOG.debug("entering to_show_handicap_whs ... with string = " + s);
            reset(s);
      
         return "show_handicap_whs.xhtml?faces-redirect=true";
    //    }
      
   }
public String to_delete_player_xhtml(String s) throws Exception {
        LOG.debug("entering to_delete_player ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   //         return "";
   }
public String to_delete_club_xhtml(String s) throws Exception {    // not used
            LOG.debug("entering to_delete_club ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_club.xhtml?faces-redirect=true";
   //         return "";
   }
  public void deleteCascadingPlayer() throws SQLException, Exception{
 try{  // fonctionne ?

//    new delete.DeletePlayer().deletePlayerAndChilds(getDeletePlayer(),conn );
    // ajouter boolean = correct insert !!!
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

public String scorecard(ECourseList ecl) {
  //  LOG.debug("Entering scorecard");// with ecl = " + ecl.toString());
        LOG.debug("Entering scorecard with ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        inscription = ecl.getInscription();
   //        LOG.debug("TeeStart from scorecard = " + inscription.getInscriptionTeeStart());
        tee = ecl.getTee();
           LOG.debug("Tee is now = " + tee.toString());
        String msg = "Select EcourseList Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> course name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.debug(msg);
 //       LOG.debug(" lists.ScoreCard3List.getListe() called = " + lists.ScoreCard3List.getListe());
 //       LOG.debug(" lists.ScoreCard2List.getListe() called = " + find.FindSlopeRating.getListe());

        return "scorecard.xhtml?faces-redirect=true";
 } //end scorecard

public String show_scorecard() throws SQLException, Exception{
        LOG.debug("entering show_scoreCard with round :!" + round); // 24-04-2020 is !N
    // à faire différencier EGA et WHS    
    // a faire = ne conserver que pour EGA
       String[] list = null;
       if(round.getRoundDate().isBefore(START_DATE_WHS) ){
           LOG.info("this is an EGA round - nmore supported !!!");
//           list = new Controllers.CalculateControllerEGA().calculate(player, round, course, tee, inscription, conn); // mod 27/05/2017
           LOG.debug("after Calculate controller EGA = " + Arrays.deepToString(list));
           if(list[0].equals("ERROR") ){
             String msg = "Fatal error in CalculateController() " + Arrays.deepToString(list);
             LOG.error(msg);
             showMessageFatal(msg);
             return null;
          }else{
              String msg = "CalculateControllerEGA is OK";
              LOG.info(msg);
    //          showMessageInfo(msg);
              return "show_scorecard_ega.xhtml?faces-redirect=true";  // mod 05-04-2019
          }
       }else{
           String msg = "this is a WHS round : "+ round.getIdround() + " on date = " + round.getRoundDate();
           LOG.info(msg);
           showMessageInfo(msg);
           return "show_scorecard_whs.xhtml?faces-redirect=true";  // mod 05-04-2019
       }    
} // end show_scorecard

public List<ECourseList> ScoreCardList1EGA() throws SQLException, LCException {
 try{
        return new lists.ScoreCardList1EGA().list(player, round, conn);
 }catch (NullPointerException | SQLException ex){
            String msg = "Exception in ScoreCardList1() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {
 }
} //end method

public List<HandicapIndex> ScoreCardList1WHS() throws SQLException{
 try{
     LOG.debug("entering ScoreCardList1WHS");
//     LOG.debug("for handicapIndex = " + handicapIndex);
//     LOG.debug("round = " + round);
     handicapIndex.setHandicapPlayerId(player.getIdplayer());
     handicapIndex.setHandicapDate(round.getRoundDate());
     handicapIndex = new find.FindHandicapIndexAtDate().find(handicapIndex, conn); //.find(player, round, conn);
    List<HandicapIndex> lhi = new ArrayList<>(); // faut une liste pour dataTable
    lhi.add(handicapIndex);
    return lhi;
 }catch (NullPointerException | SQLException ex){
            String msg = "Exception in ScoreCardList1() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {
 }
} //end method


public List<ECourseList> ScoreCardList2() throws SQLException {
 try{  
//   LOG.debug("entering ScoreCardList2");
        // le nom est trompeur : fait beaucoup plus que son nom l'indique !
      return new find.FindSlopeRating().find(player, round, conn);
    }catch (Exception ex){
            String msg = "Exception in getScoreCardList2() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

public List<ECourseList> ScoreCardList3() throws SQLException {
   try{ 
      LOG.debug("entering CourseC.ScoreCardList3");
            return new lists.ScoreCardList3().list(player, round, conn);
            
  }catch (Exception ex){
            String msg = "Exception in getScoreCardList3() " + ex;
                LOG.error(msg);
            showMessageFatal(msg);
            return null;
      } finally {
      }
    } //end method

public List<ScoreStableford.Score> ScoreCardList4() throws SQLException {
   try{ 
      LOG.debug("entering CourseC.ScoreCardList4");
     ArrayList<ScoreStableford.Score> v1 = new read.ReadScoreList().read(player,round,tee, conn);
   //    LOG.debug("result of readScoreList = " + v1);
       return v1;
  }catch (Exception ex){
        String msg = "Exception in getScoreCardList3() " + ex;
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
  } finally { }
 } //end method

    public static boolean isShowButtonCreditCard() {
        return ShowButtonCreditCard;
    }

    public static void setShowButtonCreditCard(boolean ShowButtonCreditCard) { // sert à quoi ??
        CourseController.ShowButtonCreditCard = ShowButtonCreditCard;
    }

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

public void listSubscriptionRenewal(String s) throws SQLException {
try {
     LOG.debug("... entering listSubscriptionReneval " + s); // a quoi sert le s ? paramètre ?
             subscriptionRenewal = new lists.SubscriptionRenewalList().list(conn);
    //         subscriptionReneval.forEach(item -> LOG.debug("liste " + item));  // java 8 lambda
            String msg = "We send subscription Renewal Mails = " + subscriptionRenewal.size();
            LOG.debug(msg);
            LCUtil.showDialogInfo(msg);
   } catch (Exception ex) {
            String msg = "Exception in listSubscriptionRenewal " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
       //     return null;
        } finally { }
    } //end method        
        
 public List<Car> listCars() throws SQLException { //testing primefaces
 try {
            return new lists.CarList().getListAllCars(conn);
 } catch (Exception ex) {
            String msg = "Exception in listCars() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
 public void validatePlayer(){
     setNextPanelPlayer(true);  //affiche le 2e panelGrid
}

 public String creditCardMail() throws Exception{
     LOG.debug("entering creditCardMail with creditcard typePayment = " + creditcard.getTypePayment());
     // à faire : renvoyer le mail status dans l'écran 
        if(Creditcard.etypePayment.GREENFEE.toString().equals(creditcard.getTypePayment())){
  //         boolean ok = new mail.CreditcardMail().sendMailInscription(creditcard,tarifGreenfee, round, inscription);
           if(new mail.CreditcardMail().sendMailGreenfee(player, creditcard,tarifGreenfee, round, inscription)){
               LOG.debug("");
           }
        }
        if(Creditcard.etypePayment.SUBSCRIPTION.toString().equals(creditcard.getTypePayment())){
            //creditCardSubscriptionMail();
            boolean ok = new mail.CreditcardMail().sendMailSubscription(player,creditcard, subscription);
        }
        if(Creditcard.etypePayment.COTISATION.toString().equals(creditcard.getTypePayment())){
          //  creditCardCotisationMail();
          boolean ok = new mail.CreditcardMail().sendMailCotisation(player,creditcard, cotisation, club, tarifMember);
        }
     return "creditcard_payment_executed.xhtml?faces-redirect=true";
  }
 
public void createPlayer() throws SQLException, Exception{
 try{
     LOG.debug("entering createPlayer");
     LOG.debug("entering createPlayer with HandicapIndex = " + handicapIndex);
  // mod 11-04-2022
// a faire tester la valeur du résultat !!
//  completePlayerAddress();

    if(new create.CreatePlayer().create(player, handicapIndex, conn, "A")){// new 23-11-2020 "A" signifie avec Activation (non en batch)
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

public String selectDialogPlayer(EPlayerPassword epp) {  // coming from dialog_players.xhtml
try{
        LOG.debug(" entering selectDialogPlayer with EplayerPassword = " + epp);
   //     LOG.debug("with second param = " + p);  // p is null ??
           LOG.debug("sessionMap : inputSelectClub = " + sessionMap.get("inputSelectClub"));
       //    LOG.debug("sessionMap : inputSelectCourse = " + sessionMap.get("inputSelectCourse"));  
           
            if(sessionMap.get("inputSelectClub").equals("LocalAdministrator"))  {
                localAdmin = epp.getPlayer();
                   LOG.debug("selected local administrator = " + localAdmin);
             //   boolean b = new DialogController().closeDialog("dialog_players.xhtml");
                boolean b = Controllers.DialogController.closeDialog("dialogPlayers.xhtml"); // mod 03-04-2020
                   LOG.debug(" exiting selectLocalAdministrator with dialogController closed , b = " + b);
                return null; // local_administator.xhtml
            }
            if(sessionMap.get("inputSelectClub").equals("CreatePro")){
                localAdmin = epp.getPlayer();
              //  localAdmin.setIdplayer(epp.getPlayer().getIdplayer());
              //  localAdmin.setPlayerLastName(epp.getPlayer().getPlayerLastName());
              //    LOG.debug("professional before = " + professional);
                professional.setProPlayerId(epp.getPlayer().getIdplayer());
                   LOG.debug("professional with playerid updated = " + professional);
//                   LOG.debug("selected local administrator = " + localAdmin);
              //  boolean b = new DialogController().closeDialog("dialog_players.xhtml");
                boolean b = Controllers.DialogController.closeDialog("dialogPlayers.xhtml"); // mod 03-04-2020
                   LOG.debug(" exiting selectLocalAdministrator with dialogController closed , b = " + b);
                return null; // professional.xhtml
            }
     return null;
  } catch (Exception e) {
            String msg = "££ Exception selectDialogPlayer = " + e.getMessage() + " for player = " + player.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
  }
} // end method

public String createLocalAdministrator() throws SQLException, Exception{
 try{
     LOG.debug("entering createLocalAdministrator");
     LOG.debug(" for club = " + club);
     LOG.debug(" with localAdmin = " + localAdmin);
     
     Player p = new read.ReadPlayer().read(localAdmin,conn);
        LOG.debug("player role = " + p.getPlayerRole());
     if(p.getPlayerRole().equals("PLAYER")){  // pas de modif si admin ou ADMIN donc on ne peut être localadmin que dans un club !!
         p.setPlayerRole("admin"); // Local Administrator
        if(new update.UpdatePlayer().update(p, conn)){
           String msg = "Update Player with localAdmin = OK " + p;
           LOG.info(msg);
           showMessageInfo(msg);
        }else{
           String msg = "FAILURE Update player for localAdmin ! = " + p;
           LOG.error(msg);
           showMessageFatal(msg);
           return null;
        }
      } else{
         String msg="you are already admin or ADMIN = " + p.getPlayerRole();
         LOG.info(msg);
         showMessageInfo(msg);
        // return null;
      }
// end if 1
  //   Club c = new load.LoadClub().load(club,conn); // pas nécessaire
     club.setClubLocalAdmin(p.getIdplayer());
     if(new update.UpdateClub().update(club, conn)){
        String msg = "Club updated local administrator created <br> = " 
                + localAdmin.getIdplayer() + " / " + localAdmin.getPlayerLastName()
                + "<br> for club = " + club.getIdclub() + " / " + club.getClubName();
        LOG.info(msg);
        showMessageInfo(msg);
     }else{
         String msg = "FAILURE modify club localAdmin ! = " + club;
         LOG.error(msg);
         showMessageFatal(msg);
  //       return null;
     }
return null;

 }catch (Exception ex){
            String msg = "Exception in createLocalAdministrator" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method createLocalAdministrator

public String createProfessional() throws SQLException, Exception{
 try{
     LOG.debug("entering createProfessional");
     LOG.debug(" for club = " + club);
     LOG.debug(" with localAdmin = " + localAdmin);
     LOG.debug(" with player = " + player);
  // compléter par ??
  professional.setProClubId(club.getIdclub());
  if(new create.CreateProfessional().create(professional, conn)){// new 23-11-2020
        String msg = " professional created !! = "; //+ professional;
        LOG.info(msg);
        showMessageInfo(msg);
    }else{
        String msg = "FATAL error : professional NOT created !!" + professional;
        LOG.error(msg);
        showMessageFatal(msg);
    }
  return null;
}catch (Exception ex){
            String msg = "Exception in createLocalAdministrator" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method createLocalAdministrator

public String createCompetitionDescription() throws SQLException, IOException{
        LOG.debug("entering CreateCompetitionDescription");
        LOG.debug("for competition = " + competition);
   if(new create.CreateCompetitionDescription().create(competition.getCompetitionDescription(), conn)){
       String msg = "competition Description created = "
               + competition;
            //   + competition.getCompetitionDescription().getCompetitionName()
           //    + competition.getCompetitionDescription().getCompetitionId();
       LOG.info(msg);
       showMessageInfo(msg);
   }else{
       String msg = "ERROR Inscription competition KO for "
               + competition.getCompetitionDescription().getCompetitionName();
       LOG.error(msg);
       showMessageFatal(msg);
   }
  return null;
} // end method createCompetition

// public String beforeInscriptionCompetition(ECompetition ec) throws SQLException, IOException{
    public String beforeInscriptionCompetition(CompetitionDescription ec) throws SQLException, IOException{ // mod 18-03-2022
  LOG.debug("entering inscriptionCompetition");
  LOG.debug("with competition Description = " + ec); //.getCompetitionDescription());
  // this.competition = ec;
  competition.setCompetitionDescription(ec);
  return "competition_inscription.xhtml?faces-redirect=true&operation=add";
}

 public List<String> competitionTimeStartList(String s) throws SQLException {
       LOG.debug("entering timeStartListe = ..." );
       LOG.debug("competition = " + competition);
  try{ 
      return new calc.CalcCompetitionTimeStartList().calc(competition);
  } catch (Exception e) {
            String msg = "££ Exception in timeStartListe ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
         return null; // indicates that the same view should be redisplayed
   } finally { }
} //end method


 public String createInscriptionCompetition(ECompetition ec) throws SQLException, IOException, InstantiationException{  
       LOG.debug("entering CreateInscriptionCompetition with Competition param = " + ec);
       LOG.debug("entering CreateInscriptionCompetition with Competition Data= " + competition.getCompetitionData());
       LOG.debug("for player = " + player);
    competition.getCompetitionData().setCmpDataCompetitionId(ec.getCompetitionDescription().getCompetitionId());
    competition.getCompetitionData().setCmpDataPlayerId(player.getIdplayer());
    competition.getCompetitionData().setCmpDataPlayerGender(player.getPlayerGender());
    competition.getCompetitionData().setCmpDataPlayerFirstLastName(player.getPlayerLastName() + ", " + player.getPlayerFirstName());
       LOG.debug("with competitionData = " + competition.getCompetitionData());
  if(new create.CreateCompetitionData().create(competition.getCompetitionData(), conn)){
        String msg = "Inscription data competition OK for " + String.valueOf(competition.getCompetitionData().getCmpDataCompetitionId());
        LOG.info(msg);
        showMessageInfo(msg);
  }else{
      String msg = "Failure Inscription competition NOT OK";
        LOG.error(msg);
        showMessageFatal(msg);
  }
    return "competition_list_inscriptions.xhtml?faces-redirect=true";
}
 
//public String createRoundsCompetition(ECompetition ec) throws SQLException, IOException, Exception{
    public String createRoundsCompetition(CompetitionDescription cd) throws SQLException, IOException, Exception{
       LOG.debug("entering CreateRounds with CompetitionDescription = " + cd);
 //   competition = ec;
  //     LOG.debug("with competition = " + competition);
 //   var cd = competition.getCompetitionDescription();
  if( ! new create.CreateCompetitionRounds().create(cd, conn)){
        String msg = "Create Rounds competition NOT OK for competition = " + cd.getCompetitionId();
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
  }else{
      String msg = "Create Rounds competition OK for competition = " + cd.getCompetitionId();
      LOG.info(msg);
      showMessageInfo(msg);
  }
     cd.setCompetitionStatus("2");
     if( ! new update.UpdateCompetitionDescription().update(cd, conn)){
         String msg = "NOT modifiy Competition Description Status !! ";
         LOG.debug(msg);
         LCUtil.showMessageFatal(msg);
         return null;
     }else{
         String msg = "competitionStatus = " + cd.getCompetitionStatus();
         LOG.info(msg);
         showMessageInfo(msg);
     }
  
  
  
  // ici on a besoin des rounds créés !!
  //var lp = new lists.CompetitionInscriptionsList().list(competition.getCompetitionDescription(),conn);
  //lp.forEach(item -> LOG.debug("CompetitionInscriptionsList = " + "playerId = " + item.getCompetitionData().getCmpDataPlayerId()
   //       +"roundId = "+ item.getCompetitionData().getCmpDataRoundId()));
  
    if( ! new create.CreateCompetitionInscriptions().create(cd, conn)){
        String msg = "Create competitionInscriptions  NOT OK " + cd.getCompetitionId();
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
  }else{
        String msg = "Create CompetitionInscriptionsOK " + cd.getCompetitionId();
        LOG.info(msg);
        showMessageInfo(msg);
  }
    
//        String msg = "Created Rounds and Inscriptions competition OK = " + cd.getCompetitionId();
 //       LOG.info(msg);
//        showMessageInfo(msg);
 
     cd.setCompetitionStatus("3");
     if( ! new update.UpdateCompetitionDescription().update(cd, conn)){
         String msg = "NOT modifiy Competition Description !! ";
         LOG.info(msg);
         showMessageInfo(msg);
         return null;
     }else{
         String msg = "competitionStatus = " + cd.getCompetitionStatus();
         LOG.info(msg);
         showMessageInfo(msg);
     }
  lists.CompetitionRoundsList.setListe(null); // new 03-11-2020 permettre des 'Creation Rounds' successives dans select_competition_admin
   return "competition_admin_menu.xhtml?faces-redirect=true";
}

  public String cancelInscriptionCompetition(ECompetition ec) throws Exception{
        LOG.debug(" starting cancelInscriptionCompetition");
        LOG.debug(" with ecl = " + ec.toString());
    if(new delete.DeleteInscriptionCompetition().delete(ec, conn)){
        String msg = "inscription deleted = " + ec.getCompetitionData().getCmpDataId()
                + " for player = " + ec.getCompetitionData().getCmpDataPlayerId()
                + " for competition = " + ec.getCompetitionDescription().getCompetitionName() ;
        LOG.info(msg);
        showMessageInfo(msg);
    }
 //     LOG.debug(" result of cancelInscritionCompetition = " + OK);
      // afficher message de 
      lists.CompetitionInscriptionsList.setListe(null);  // reset
      new lists.CompetitionInscriptionsList().list(ec.getCompetitionDescription(), conn); //refresh list without the deleted item
      return "competition_list_inscriptions.xhtml?faces-redirect=true";  // refresh view without message !

  }

public String createClub(){
        LOG.debug("entering CreateClub");
try{
 // LOG.debug("with club = " + club.toString());
 //   if(new create.CreateClub().create(club, conn)){
        // changement : returns array with 1. true or false 2. the clubid created
    String[] status = new create.CreateClub().create(club, conn);
  //  Boolean b = Boolean.valueOf(status[0]);
  //  LOG.debug("booleab b = " + b);
    if (Boolean.valueOf(status[0])){  // if true , club created
        LOG.debug("club created : " + status[1]);
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

public String addCourse(ECourseList ecl){
 try{
      LOG.debug("entering addCourse");
         club = new read.ReadClub().read(ecl.getClub(),conn);  // pour avoir clubname, etc...
         LOG.debug("adding a course for idclub = " + ecl.getClub().getIdclub() + club.getClubName());
        course.setCreateModify(true); // mod 12/122017 was false gestion button dans course.xhtml
         course = new Course(); // added 2018-11-08 pour nettoyer data d'une précédente modification
        return "course.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method addCourse

public String addTee(ECourseList ecl){
 try{
      LOG.debug("entering addTee");
         club = new read.ReadClub().read(ecl.getClub(), conn);  // pour avoir clubname, etc...
            LOG.debug("club handled is " + club.getIdclub() + " : " + club.getClubName());
         course = new read.ReadCourse().read(ecl.getCourse(),conn);
            LOG.debug("course handled is " + course.getIdcourse() + " : " + course.getCourseName());
            
         LOG.debug("tee to be added = " + ecl.getTee());
         tee.setCreateModify(true); // gestion button dans tee.xhtml
         tee = new Tee();  // new 2018-11-08  nettoyer les data d'une prédédente modification
         
        return "tee.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addTee" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method addTee

public String deleteClub(ECourseList ecl){ 
 try{
     LOG.debug("entering deleteClub for " + ecl.getClub()); //Eclub.getIdclub());
       boolean OK = new delete.DeleteClub().delete(ecl.getClub(), conn);
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

public String deleteCourse(ECourseList ecl){
 try{
        LOG.debug("entering deleteCourse for " + ecl.getCourse());
     boolean OK = new delete.DeleteCourse().delete(ecl.getCourse(), conn); //.deleteHoles(ecl.Etee.getIdtee(), conn);
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

public String deleteTee(ECourseList ecl){
 try{
            LOG.debug("entering deleteTee for " + ecl.getTee());
    //    delete.DeleteTee dt = new delete.DeleteTee();
        boolean OK = new delete.DeleteTee().delete(ecl.getTee(), conn);
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

public String deleteTarifMember(){
    // new 19-05-2021
 try{
            LOG.debug("entering deleteTarifMember");
            LOG.debug("with club = " + club);
            LOG.debug("with Tarifmember = " + tarifMember);
        tarifMember.setTarifMemberIdClub(club.getIdclub());
  //      String s = tarifGreenfee.getStartDate().format(ZDF_DAY);
  //      String Syear = s.substring(s.length() - 4);
        if(new delete.DeleteTarifMember().delete(tarifMember, conn)){
            String msg = "TarifMember deleted = " + tarifMember; //.getTarifMemberIdClub(); //+ " /" + Syear;
            LOG.info(msg);
            showMessageInfo(msg);
        }else{
            String msg = "Result of deleteTarifMember is NOT OK = " + tarifGreenfee.getTarifCourseId(); // + " / " + Syear;
            LOG.error(msg);
            showMessageFatal(msg);
        }
        return null;
   //     return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteTarifMember" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteTarifGreenfe

public String deleteHoles(ECourseList ecl){ 
 try{
        LOG.debug("entering deleteHoles for Tee = " + ecl.getTee());
      boolean OK = new delete.DeleteHoles().delete(ecl.getTee(), conn);
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

    public Creditcard getCreditcard() {
        return creditcard;
    }

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    }

public String loadCourse(ECourseList ecl) throws SQLException, Exception{ 
 try{
       LOG.debug("entering loadCourse");
       LOG.debug("with ecl = " + ecl.toString());
       LOG.debug("idcourse in loadCourse = " + ecl.getCourse().getIdcourse() );
     course = new read.ReadCourse().read(ecl.getCourse(),conn);
     club = new read.ReadClub().read(ecl.getClub(), conn);  // pour avoir clubname, etc...
        LOG.debug("idclub after loadCourse= " + club.getIdclub());  // si est null faut complémenter
     if(club.getIdclub() == null){
         club.setIdclub(course.getClub_idclub());
         String msg = "Idclub forced because it was null ";
         LOG.error(msg);
         showMessageFatal(msg);
     }
     
    if(course != null){
        course.setCreateModify(false); // gestion button dans course.xhtml
        return "course.xhtml?faces-redirect=true&operation=modify";
    }else{
        LOG.debug("error : course not retreaved !!");
        return null;
    }
 }catch (SQLException ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

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

public String loadTee(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadTee");
        LOG.debug("loadTee ecl = " + ecl.toString());
     tee = new read.ReadTee().read(ecl.getTee(),conn);
     course = new read.ReadCourse().read(ecl.getCourse(),conn); // pour avoir coursename, etc...
     club = new read.ReadClub().read(ecl.getClub(), conn);  // pour avoir clubname, etc...
        LOG.debug("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.debug("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null) {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.debug("idcourse forced");
     }
    if(tee != null){
        tee.setCreateModify(false); // gestion button dans club.xhtml
        return "tee.xhtml?faces-redirect=true&operation=modify";
    }else{
        // error in create player
        LOG.debug("error : tee not retreaved !!");
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

public String loadHoles(ECourseList ecl, String type) throws SQLException, Exception{  // multiple Hole
 try{
        LOG.debug("entering loadHoles - multiple");
        LOG.debug("with type = " + type);
     tee = new read.ReadTee().read(ecl.getTee(),conn);
     holesGlobal = new read.ReadHoles().read(tee, conn); // mod 15-08-2023
        LOG.debug("holesGlobal dataHoles = " + Arrays.deepToString(holesGlobal.getDataHoles()));
     course = new read.ReadCourse().read(ecl.getCourse(),conn); // pour avoir coursename, etc...
     club = new read.ReadClub().read(ecl.getClub(), conn );  // pour avoir clubname, etc...
        LOG.debug("course after loadHoles = " + course.toString()); 
        LOG.debug("tee after loadHoles = " + tee.toString());
  //       course.setIdcourse(tee.getCourse_idcourse());
  //       LOG.debug("idcourse forced");
      hole.setCreateModify(false); // gestion button dans club.xhtml
      if (type.equals("global")){
          return "modify_holes_global.xhtml?faces-redirect=true&operation=modify holes Global";
      }else{  // distance
          return "modify_holes_distance.xhtml?faces-redirect=true&operation=modify holes Distance";
      }
      
 }catch (Exception ex){
            String msg = "Exception in loadHoles " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadHolee

 public String createHolesGlobal(String param) throws Exception{
   try{
        LOG.debug("entering createHolesGlobal  "); 
        LOG.debug("with param = " + param); // global or distance
        LOG.debug("for club = " + club);
        holesGlobal.setType(param);
     if(new create.CreateOrUpdateHolesGlobal().status(holesGlobal, tee, course, player, conn)){
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
 
  public String viewHolesGlobal() throws Exception{
        LOG.debug("entering viewHolesGlobal  "); 
        LOG.debug("for holesGlobal = " + holesGlobal);
        LOG.debug("for club = " + club);
        LOG.debug("for course = " + course);
        LOG.debug("for tee = " + tee);
  //     holesGlobal = new load.LoadHoles().LoadHolesArray(conn, tee);
       tee.setTeeHolesPlayed("01-18");
 return "modify_holes_global.xhtml?faces-redirect=true&cmd=" + sessionMap.get("inputSelectCourse");
    } // end modifyHolesGlobal
 
public String loadHole(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadHole");
     tee = new read.ReadTee().read(ecl.getTee(),conn);
     course = new read.ReadCourse().read(ecl.getCourse(),conn); // pour avoir coursename, etc...
     club = new read.ReadClub().read(ecl.getClub(), conn);  // pour avoir clubname, etc...
        LOG.debug("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.debug("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null) {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.debug("idcourse forced");
     }
    if(tee != null){
        tee.setCreateModify(false); // gestion button dans club.xhtml
        return "hole.xhtml?faces-redirect=true&operation=modify hole";
    }else{
        // error in create player
        LOG.debug("error : tee not retreaved !!");
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadHolee


 public String modifyTee() throws Exception { 
        LOG.debug("entering modifyTee  "); 
        LOG.debug("tee to be modified = " + tee.toString());
    if(new update.UpdateTee().update(tee, conn)){
        tee.setNextTee(true); // affiche le bouton next(Course) bas ecran a droite
        LOG.debug("tee Modified !!");
    }
 return null;
    } // end modifyTee

public String loadClub(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.debug("entering loadClub");
    club = new read.ReadClub().read(ecl.getClub(), conn);
    // ajouter boolean = correct insert !!!
    if(club != null){
        club.setCreateModify(false); // gestion button dans club.xhtml
        return "club.xhtml?faces-redirect=true&operation=modify";
    }else{
        // error in create player
        String msg = "error : club not retreaved !! ";
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

public String modifyClubUnavailableStructure(String type) throws Exception { //modify club from unavailable_structure.xhtml
   LOG.debug("entering modifyClubUnavailableStructure  for club = " + club);
   LOG.debug("  for type = " + type);
 try{
     if(type.equals("modify")){
         // normal case
     }
     if(type.equals("delete")){
         unavailable.getStructure().setStructureList(null);
     }
     if(new UnavailableController().updateClub(unavailable, club, conn)){
         unavailable.getStructure().setStructureExists(true);  // menu management
     }
      return null; // returns to calling screen

  }catch (Exception ex){
            String msg = "Exception in modifyClubUnavailableStructure " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} // end modifyClub

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
// enlevé 29-11-2018 à réécrire
 public String listParticipants_mp(Matchplay mp) throws SQLException {
    try {
                LOG.debug(" -- entering listParticipants_mp = " + mp.getIdround());
                round.setIdround(mp.getIdround());
  ////          listmatchplay = lists.ScamblePLayersList.listAllParticipants(round.getIdround(), conn);
                LOG.debug(" -- exiting listParticipants_mp = ");
                LOG.debug("liste participants matchplay = " + Arrays.deepToString(listmatchplay.toArray()) );
  //            course.setIdcourse(inputPlayingHcp);
                round.setRoundGame(listmatchplay.get(0).getRoundGame());
           return "show_participants.xhtml?faces-redirect=true&cmd=MP_";
        } catch (Exception ex) {
            String msg = "Exception in getParticipantsList_mp() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

 public String listParticipantsStablefordRound(ECourseList ecl) throws SQLException {
  try {
          LOG.debug(" -- entering listParticipantsStablefordRound = " + ecl.getRound().getIdround() );
          round = ecl.getRound();
          club = ecl.getClub();
          course = ecl.getCourse();
          listStableford = new lists.ParticipantsRoundList().list(round, conn);
             LOG.debug(" -- exiting listParticipants_stableford = ");
             LOG.debug("liste participants stableford = " + Arrays.deepToString(listStableford.toArray()) );
   //          LOG.debug("PlayersString was " + round.getPlayersString());
          String playersString = Round.fillRoundPlayersStringEcl(listStableford);
          round.setPlayersString(playersString);
             LOG.debug("PlayersString is now " + round.getPlayersString());
          round.setRoundGame(listStableford.get(0).getRound().getRoundGame());
           return "show_participants_stableford.xhtml?faces-redirect=true&cmd=ROUND";
   } catch (Exception ex) {
            String msg = "Exception in listParticipantsStablefordRound" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {  }
    } //end method

 
  public List<ECourseList> listParticipantsStablefordCompetition(CompetitionDescription cde) throws SQLException {
  try {
          LOG.debug(" -- entering listParticipantsStablefordCompetition for CompetitionDescription = " + cde);
  //     round.setRoundDate(ec.getCompetitionDescription().getCompetitionDate());
   //    round.setIdround(ec.getCompetitionData().getCmpDataRoundId());
   //    round.setRoundName(ec.getCompetitionDescription().getCompetitionName());
   //    round.setRoundGame(ec.getCompetitionDescription().getCompetitionGame());

     var li = new lists.ParticipantsStablefordCompetitionList().list(cde, conn);
       LOG.debug(" -- exiting listParticipantsStablefordCompetition = ");
       LOG.debug(" with liste participants stableford = " + Arrays.deepToString(li.toArray()) );
  //     LOG.debug("PlayersString was " + round.getPlayersString());
//return listStableford;
return li;

 //     return "competition_show_participants_stableford.xhtml?faces-redirect=true&cmd=COMPETITION";
   } catch (Exception ex) {
            String msg = "Exception in listParticipants_scramble" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {  }
    } //end method
 
  
 public List<EMatchplayResult> calcMatchplayResult() throws SQLException { // button in show_participants_stableford.xhtml
  try {
          LOG.debug(" -- entering calcMatchplayResult");
          
      //TODO modifier : on prend les deux premiers    
         if(listStableford.size() != 2){
             String msg = "Pour matchplay il faut deux joueurs  !!";
             LOG.error(msg);
             showMessageFatal(msg);
             return null;
         }
         var p1 = listStableford.get(0).getPlayer(); //.getIdplayer();
           LOG.debug("listStableford player 1 = " + p1.getIdplayer());
         var p2 = listStableford.get(1).getPlayer(); //.getIdplayer(); 
           LOG.debug("listStableford player 2 = " + p2.getIdplayer());
          
     var li = new calc.CalcMatchplayResult().calc(p1, p2, round, conn);
  //     LOG.debug(" result calcMatchplay =  " + li);
       LOG.debug(" with liste participants stableford = " + Arrays.deepToString(li.toArray()) );
  //     LOG.debug("PlayersString was " + round.getPlayersString());
     return li;

 //     return "competition_show_participants_stableford.xhtml?faces-redirect=true&cmd=COMPETITION";
   } catch (Exception ex) {
            String msg = "Exception in calcMatchplayResult " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {  }
    } //end method

 public List<ECourseList> listCourses() throws SQLException{
 try {
     LOG.debug("listCourseOnly ? sessionMap = "+ sessionMap.get("inputSelectCourse"));

     if(sessionMap.get("inputSelectCourse").equals("ChartCourse")
      || sessionMap.get("inputSelectCourse").equals("CreateRound")
      || sessionMap.get("inputSelectCourse").equals("createTarifGreenfee"))   {
         return new lists.CourseListOnly().list(conn); // club + course
    }else{
        return new lists.CourseList().list(conn); // club + course + tee
   }
  } catch (Exception ex) {
            String msg = "Exception in listCourses() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } //end method
 }
 
  public List<Club> listLocalAdminClubsList() throws SQLException{
 try {
     LOG.debug("listCourseOnly ? sessionMap = "+ sessionMap.get("inputSelectCourse"));

  //   if(sessionMap.get("inputSelectCourse").equals("ChartCourse")
  //    || sessionMap.get("inputSelectCourse").equals("CreateRound")
  //    || sessionMap.get("inputSelectCourse").equals("createTarifGreenfee"))   {
  LOG.debug("player = " + player);
         return new lists.ClubsListLocalAdmin().list(player, conn);
  //  }else{
  //      return new lists.CourseList().list(conn);
  // }
  } catch (Exception ex) {
            String msg = "Exception in listCourses() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } //end method
 }
 
   public List<EClubCourse> listLocalAdminCoursesList(String select, String admin) throws SQLException{
 try {
//     LOG.debug(" entering listLocalAdminCoursesList");
//     LOG.debug("with sessionMap = "+ sessionMap.get("inputSelectCourse"));
//     LOG.debug("with select = "+ select);
//     LOG.debug("with admin = "+ admin);

  //  if(sessionMap.get("inputSelectCourse").equals("ChartCourse")
  //    || sessionMap.get("inputSelectCourse").equals("CreateRound")
 // if(select.equals("createTarifGreenfee")){
  LOG.debug("player = " + player);
         return new lists.CoursesListLocalAdmin().list(player, conn);
  //  }else{
  //      return new lists.CourseList().list(conn);
  // }
  } catch (Exception ex) {
            String msg = "Exception in listCourses() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } //end method
 }

   
   
   
   
   
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
 
 public List<Course> listCoursesForClub(String clubid) throws SQLException{
 try {
     LOG.debug(" starting listCoursesForClub with param clubid = " + clubid);
     if(clubid == null || clubid.equals(EMPTY_STRING)){
         LOG.debug("param clubid == null or empty");
     }else{
         club.setIdclub(Integer.parseInt(clubid));
     }
        LOG.debug("listCoursesForClub - Club = "+ club);
     return new lists.CourseListForClub().list(club,conn);
 } catch (Exception ex) {
            String msg = "Exception in listClubs() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } //end method
  }
 
      public List<ECourseList> listDetailClub(String id) throws SQLException{ // used in dialogClubDetail.xhtml
 try { // mod 21-04-2019
          LOG.debug(" -- entering listDetailClub ");
          LOG.debug("with id club = " + id);
          Club c = new Club();
          c.setIdclub(Integer.valueOf(id));
          lists.ClubDetailList.setListe(null);  //rei

       return new lists.ClubDetailList().list(c, conn);
    } catch (Exception ex) {
            String msg = "Exception in listDetailClub() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } //end method
     
 public List<ECourseList> listClubsCoursesTees(String param) throws SQLException {
   try {
       LOG.debug(" -- entering listClubsCoursesTee with param = " + param);
       LOG.debug(" with club = " + club.getIdclub());
 //      if(param.equals("all_clubs")){
 //          return new lists.ClubCourseTeeListAll().list(conn);
 //      }
       if(param.equals("one_club")){
           return new lists.ClubCourseTeeListOne().list(club, conn);
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
 
  public List<ECourseList> listInscriptions() throws SQLException{
   try {
        //    LOG.debug(" -- entering listInscriptions with inputInscription = " + getInputInscription());
        //    LOG.debug(" -- entering listInscriptions ");
        return new lists.InscriptionList().list(conn);
   } catch (Exception ex) {
            String msg = "Exception in getInscriptionList() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
  

  public List<CompetitionDescription> listCompetitions(){  //throws SQLException{  // mod 17-03-2022
   try {
      LOG.debug(" -- entering listCompetitions ");

        return new lists.CompetitionDescriptionList().list(conn);
    } catch (Exception ex) {
            String msg = "Exception in listCompetitions() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {        }
    } //end method
       
 
    public String beforeListInscriptionsCompetition(CompetitionDescription cd) throws SQLException, IOException{ // mod 17-03-2022
  LOG.debug("entering beforeListInscriptionCompetition");
  LOG.debug("with competition description = " + cd); //.getCompetitionDescription());
 // LOG.debug("with competition data = " + ec.getCompetitionData());
//  LOG.debug("for competition = " + ec.getCompetitionDescription().getCompetitionId());
 /// this.competition = ec;
  competition.setCompetitionDescription(cd);
  lists.CompetitionInscriptionsList.setListe(null); // new 31-10-2020
  return "competition_list_inscriptions.xhtml?faces-redirect=true&operation=add";
} 

    public String beforeCompetitionMenu(CompetitionDescription ec) throws SQLException, IOException{  // mod 17/03/2022
  LOG.debug("entering beforeCompetitionMenu");
  LOG.debug("with competition Description = " + ec);
 // LOG.debug("for competition = " + ec.getCompetitionDescription().getCompetitionId());
 /// this.competition = ec;
  competition.setCompetitionDescription(ec);
  // rei liste !!
//  CompetitionInscriptionsList.setListe(null); // new 31-10-2020
  return "competition_admin_menu.xhtml?faces-redirect=true&operation=menu";
} 

public List<ECompetition> listInscriptionsCompetition() throws SQLException, IOException{
     LOG.debug("entering listInscriptionsCompetition");
 //    LOG.debug("with competition Data = " + competition.getCompetitionData());
     LOG.debug("with competition Description = " + competition.getCompetitionDescription());
//  LOG.debug("for competition ID = " + ec.getCompetitionDescription().getCompetitionId());
 // this.competition = ec;
  //  String msg = "Data List OK";
    //    LOG.debug(msg);
    //    showMessageInfo(msg);
    var lp1 = new lists.CompetitionInscriptionsList().list(competition.getCompetitionDescription(),conn);
    LOG.debug("var lp louis = " + lp1);
   return lp1;
}

  public String beforeListStartCompetition(CompetitionDescription cd, String type_exec) throws SQLException, IOException{
        LOG.debug("entering beforeListStartCompetition");
        LOG.debug("with competition description = " + cd); //.getCompetitionDescription());
        LOG.debug("with type = " + type_exec);
    if(type_exec.equals(entite.CompetitionDescription.StatusExecution.PROVISIONAL.toString()) 
       || type_exec.equals(entite.CompetitionDescription.StatusExecution.FINAL.toString())){
        LOG.debug("good execution type - PROVISIONAL or FINAL");
    }
    // que faire avec FINAL ??
    cd.setCompetitionExecution(type_exec);
    competition.setCompetitionDescription(cd);
      LOG.debug("competition modified = " + competition);
    return "competition_list_start.xhtml?faces-redirect=true&operation=add";
} 

public List<ECompetition> listStartCompetition() throws SQLException, IOException, Exception{
 try{
      LOG.debug("entering listStartCompetition");
      LOG.debug("with competition Description = " + competition.getCompetitionDescription());
      LOG.debug("CompetitionStatus = " + competition.getCompetitionDescription().getCompetitionStatus());
 //  if(competition.getCompetitionDescription().getCompetitionStatus().equals("1")){
 //          LOG.debug("Status is already 1 - nothing to do");
           
      String execution = competition.getCompetitionDescription().getCompetitionExecution();
          LOG.debug("CompetitionExecution = " + execution);
      String msg = "ListStartCompetition OK";
        LOG.debug(msg);
 // 1. d'abord '
      List<ECompetition> li = new lists.CompetitionInscriptionsList().list(competition.getCompetitionDescription(), conn);
        LOG.debug("line 01 after call competitionInscriptionsList li size = " + li.size());
     if(li == null){
        msg = "there are no inscriptions : we do nothing for competition Id  = "
                + competition.getCompetitionDescription().getCompetitionId();
        LOG.debug(msg);
        LCUtil.showMessageFatal(msg);
        return li;
    }
 // 2. ensuite
       LOG.debug("we go to CompetitionStartlist with Execution = " + execution);
     li.get(0).getCompetitionDescription().setCompetitionExecution(execution);
    var csl = new lists.CompetitionStartList().list(li,conn);
//       LOG.debug("we are back from competitionstartlist with csl = " + csl.toString());
    if(csl == null){
        msg = "Empty CompetitionStartList !! ";
         LOG.error(msg);
         LCUtil.showMessageFatal(msg);
         return null;
    }else{
         msg = "Completed CompetitionStartList = " + csl.size() +
                 " for competition Id  = " + competition.getCompetitionDescription().getCompetitionId();
         LOG.info(msg);
   //      LCUtil.showMessageInfo(msg);        
     }   
    // mod status
         competition.getCompetitionDescription().setCompetitionStatus("1");
  //         LOG.debug("we go to update description with status = " + cde.getCompetitionStatus());
         if(new update.UpdateCompetitionDescription().update(competition.getCompetitionDescription(), conn)){
                msg = "OK result of modify Competition Description";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);        
 //            return true;
         }else{
                msg = "KO KO  result of modify Competition Description";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg); 
                return null;
         }    
     return csl;

 }catch (Exception ex){
            String msg = "Exception in listStartCompetition() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
} // end method

  public String cancelInscription(ECourseList ecl) throws Exception {
  try{
      LOG.debug(" starting cancelInscription ");
      LOG.debug(" with ecl = " + ecl.toString());
      setPlayer2(ecl.getPlayer());
      setRound(ecl.getRound());
      /// controle ne devait canceler que ses propres rounds !!!
      LOG.debug("current player = " + player.getIdplayer());
      LOG.debug("current player ROLE = " + player.getPlayerRole());
      LOG.debug("cancellation for player = " + player2.getIdplayer());
      LOG.debug("cancellation for player ROLE = " + player2.getPlayerRole());
      // new 04-11-2018
      if((! player.getPlayerRole().equals("ADMIN")) && (player.getIdplayer().intValue() != player2.getIdplayer().intValue())){  // mod 01-12-018 player 2 et ||
          String msgerr =  LCUtil.prepareMessageBean("cancel.inscription");
          msgerr = msgerr + player.getIdplayer() + " /\\ " + player2.getIdplayer() + " /\\ " + player.getPlayerRole();
          LOG.error(msgerr);
          showMessageFatal(msgerr);
          throw new Exception(msgerr);
      }
            
   //   delete.DeleteInscription di = new delete.DeleteInscription();
      new delete.DeleteInscription().delete(player2, round, ecl.getClub(),ecl.getCourse(), conn); // similaire
      lists.ParticipantsRoundList.setListe(null);  // reset
      listStableford = new lists.ParticipantsRoundList().list(round, conn); // refresh list without the deleted item
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
  
  public List<ECourseList> listHandicaps() throws SQLException{
   try {
 //           LOG.debug(" -- entering getHandicapList = " + player.getIdplayer() );
         return new lists.HandicapList().list(player, conn);
   } catch (Exception ex) {
            String msg = "Exception in listHandicaps " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {  }
    } //end method
  
 public List<ECourseList> listHandicapsWHS() throws SQLException{
   try {
 //           LOG.debug(" -- entering getHandicapList = " + player.getIdplayer() );
         return new lists.HandicapIndexList().list(player, conn);
      } catch (Exception ex) {
            String msg = "Exception in listHandicapsWHS " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {  }
    } //end method
  
  
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
  
  
  public String cancelRound(ECourseList ecl) throws Exception{
        LOG.debug(" starting cancelRound ");
        LOG.debug(" with ecl = " + ecl.toString());
      boolean OK = new delete.DeleteRound().delete(ecl.getRound(), conn);
        LOG.debug(" result of deleteHoles = " + OK);
      lists.InscriptionList.setListe(null);  // reset
      listInscriptions();  // refresh list without the deleted item
    return "selectInscription.xhtml?faces-redirect=true";  // refresh view without message !
  }

    public Map<String, Object> getApplicationMap() {
        return applicationMap;
    }

 public String selectPlayer(EPlayerPassword epp) throws SQLException{
try{
    LOG.debug(" starting selectPlayer ");
    player = epp.getPlayer();
       
     if(player.getIdplayer() == null){
         String err = "player is null = " + player;
         LOG.debug(err);
         showMessageFatal(err);
         return null;
     }else{
         LOG.debug("selected current Player = " + player);
     }
      LanguageController.setLocale(Locale.of(player.getPlayerLanguage()));
         LOG.debug("Language set = " + player.getPlayerLanguage());  
         LOG.debug("Language is now = " +  LanguageController.getLanguage());  
LOG.debug("1. verifying if there is an existing password");
      Password p = new Controllers.PasswordController().isExists(epp, conn);
      if (p == null){
          LOG.debug("password is null ==> going to password_create");
          return "password_create.xhtml?faces-redirect=true";
      }
 LOG.debug("2. verifying if there is the is a blocking password too many trials");
      if(new Controllers.PasswordController().isBlocking(player, conn)){
          return "selectPlayer.xhtml?faces-redirect=true";
      }

 LOG.debug("3. verifying if there is a valid subscription");
     subscription = new PaymentsSubscriptionController().isExists(player, conn);
        LOG.debug("subscription valid ? = " + subscription);
    if(subscription.isErrorStatus()){
          LOG.debug("subscription is null ==> going to subscription.xhtml");
          return "subscription.xhtml?faces-redirect=true";
    }else{
         // we continue
    }
 LOG.debug("4. initialisations diverses");
       localAdmin = new Player(); // bien le bon endroit ?
       sessionMap.put("playerid", player.getIdplayer());
       if(sessionMap.get("playerid") == null){
           LOG.debug("sessionMap playerid = null");
       }else{
           sessionMap.put("playerlastname", player.getPlayerLastName());
           int yourAge = utils.LCUtil.calculateAgeFirstJanuary(player.getPlayerBirthDate());
           sessionMap.put("playerage", yourAge);
           setConnected(true); // affiche le bouton Logout via rendered dans header.xhtml
           applicationMap.put("Connection",conn);  // utilisé dans ScheduleProController !! pour récupérer la connection
              LOG.debug("applicationMap Connection is now for ScheduleProController = " + applicationMap.get("Connection"));
           club.setIdclub(epp.getPlayer().getPlayerHomeClub());
           player.setShowMenu(true);  // 17-04-2024 affiche le menu principal 
       }
 LOG.debug("5. create auadi log");
      if(new create.CreateAudit().create(player, conn)){
            String msg = "createdAudit in selectPlayer ";
            LOG.debug(msg);
      }
 // everything controlled and initialized  //
       return "welcome.xhtml?faces-redirect=true";
  } catch (Exception e) {
            String msg = "££ Exception selectPlayer = " + e.getMessage() + " for player = " + player;
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {
        }  
} // end method
 
  public List<Professional> listProfessional() throws SQLException{
   try {
            LOG.debug(" -- entering listProfessional");
    // liste des clubs ou il est pro
         listProfessional = new lists.ProfessionalClubList().list(player, conn);
         if(!listProfessional.isEmpty()){
            String msg = "is a pro !!" + listProfessional;
            LOG.debug(msg);
            showMessageInfo(msg);
            return listProfessional;
        }else{
            String msg = "is NOT  pro !!";
            LOG.debug(msg);
            return null;
        }
  } catch (Exception ex) {
            String msg = "Exception in listProfessional " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } //end method
  } //end 
public String passwordVerification(String OK_KO){
try{ // coming from welcome.xhtml provisoirement
    LOG.debug(" starting passwordVerification with = " + OK_KO);
    LOG.debug("for player player= " + player);
    // non nil faut vérifier si blocage !!
    if("OK".equals(OK_KO)){
        String msg = "Password Correct";
        LOG.debug(msg);
 //       showMessageInfo(msg);
        return null;
    }
    if("KO".equals(OK_KO)){
        String msg = LCUtil.prepareMessageBean("connection.failed");
    //    String msg = "wrong password for this connection";
        LOG.info(msg);
        showMessageInfo(msg);
        blocking = new read.LoadBlocking().load(player,conn);
        LOG.debug("returned blocking = " + blocking);
        if(blocking == null){
           LOG.debug("il n'existe pas de record blocage");
           // faut en créer un !
            boolean b = new create.CreateBlocking().create(player,conn);
                LOG.debug("record bloking written ? = " + blocking);
            return "selectPlayer.xhtml?faces-redirect=true";
        }
        if(blocking != null){ // blocking not null
            if(blocking.getBlockingAttempts() > 2){
                msg = LCUtil.prepareMessageBean("connection.blocked");
    //    String msg = "wrong password for this connection";
                LOG.info(msg);
                showMessageInfo(msg);
            }else{// si blocking < 2, rien faire
                short s = blocking.getBlockingAttempts();
                blocking.setBlockingAttempts(s+=1);
                boolean b = new update.UpdateBlocking().update(blocking, conn);
                return "selectPlayer.xhtml?faces-redirect=true";
            }
        } //end blocking nomt null
    }
    
    return null;
  } catch (Exception e) {
            String msg = "££ Exception in passwordVerification = " + e.getMessage() + " for player = " + player.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }  
} // end method passwordVerification

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

public boolean modifySubscription() throws Exception{ 
 try{
      LOG.debug("entering modifySubscription for subscription = " + subscription);
    
    if(new update.UpdateSubscription().modify(subscription, conn)){
        LOG.debug("after modifySubscription : we are OK " + subscription);
        String msg = LCUtil.prepareMessageBean("subscription.success") + " end date = " + subscription.getEndDate().format(ZDF_DAY);
        LOG.debug(msg);
        showMessageInfo(msg);
        return true;
    }else{
        String msg = "error : subscription NOT modified !!";
        LOG.error(msg);
        showMessageFatal(msg);
        return false; // retourne d'ou il vient
    }
 }catch (Exception ex){
            String msg = "Exception in handleSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method modifySubscription
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
 public String manageCotisation() throws Exception{ // called from cotisation.xhtml
 try{
      LOG.debug("entering manageCotisation ");
      LOG.debug("tarifMember = " + tarifMember);
      LOG.debug("cotisation = " + cotisation); // est null
      LOG.debug("round = " + round);
    sessionMap.put("creditcardType", etypePayment.COTISATION);
     // enlevé 25-08-2025(etypePayment.COTISATION.toString());  // new 15-08-2025 solution provisoire suite utilisation REST (pas le même context)!!
      LOG.debug("sessionMap creditcardType created = " + sessionMap.get("creditcardType"));
    cotisation = new TarifMemberController().completeCotisation(tarifMember, player, round);
    if(cotisation == null){
        String msg = "cotisation not found !! is null";
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
    cotisation.setIdplayer(player.getIdplayer());
    cotisation.setIdclub(club.getIdclub());
    cotisation.setCommunication(club.getClubName() + " : " + cotisation.getCommunication());
       LOG.debug("Cotisation loaded = " + cotisation);
 //   boolean OK = false;

     if(cotisation.getPrice() == 0.0){
  //  if(cotisation.getPrice().equals(0.0)){    
          String msg = "amount ZERO no payment needed !!";
          LOG.info(msg);
          showMessageInfo(msg);
          return null;
       }

    if(cotisation.isCotisationError()){
        String msg = "cotisation error !!";
        LOG.error(msg);
        showMessageFatal(msg);
        return null;
    }
//new 22-08-2025   
   // if(sessionMap.get("inputSelectCourse").equals("PaymentCotisationSpontaneous")){
    if(sessionMap.get("inputSelectClub").equals("PaymentCotisationSpontaneous")){    // mod 24-08-2025
        cotisation.setType("spontaneous");
            LOG.debug("Paiement spontané - NO inscription");
    }else{
        cotisation.setType("round"); // inscription à un round
    }
    LOG.debug("cotisation type : spontaneous ou round ? " + cotisation.getType());
    
    LOG.debug("amount non ZERO payment COTISATION needed !");
    creditcard = new Controllers.CreditcardController().completeWithCotisation(cotisation, player, conn);
    if(creditcard != null){ 
        String msg = "creditcard completed with Cotisation ! ";
        LOG.info(msg);
    //          showMessageInfo(msg);
        return "creditcard.xhtml?faces-redirect=true";
    }else{
        String msg = "paiement par creditcard KO : quelle conclusion ?";
        LOG.error(msg);
        showMessageFatal(msg);
        throw new Exception(msg);
      //        return null;
    }
 }catch (SQLException ex){
            String msg = "SQLException in manageCotisation " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }catch (Exception ex){
            String msg = "Exception in manageCotisation " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
 } //end method manageCotisation
  
 
 public String manageLesson() throws Exception{ // called from schedule_pro.xhtml
 try{
      LOG.debug("entering manageLesson");
   // sessionMap.put("creditcardType", "LESSON");
    sessionMap.put("creditcardType", etypePayment.LESSON); 
       LOG.debug("sessionMap creditcardType created = " + sessionMap.get("creditcardType"));
 // va chercher dans autre controller
    professional = Controllers.SchedulerProController.getProfessional();
       LOG.debug("professional coming from SchedulerProController = " + professional);
    club.setIdclub(professional.getProClubId());
    club = new read.ReadClub().read(club, conn);
    Player p = new Player();
    p.setIdplayer(professional.getProPlayerId());
    playerPro = new read.ReadPlayer().read(p, conn);
    // 27-01-2023 erreur toutours dernière lesson !
 // va récupérer dans autre Controller !!
    listLessons = Controllers.SchedulerProController.getListLessons();
    // complete lesson with price
    
       LOG.debug("we manage a professional ?" + isProfessional().size() );
       // alors faire create lesson only et pas de payment ?
       // vérifier si le student n'est pas aussi pro !! ex louuis prend une lesson chez olivier
    for(Lesson lesson2 : listLessons){
         //    if(isProfessional() > 0){ // nombre de clubs ou il est pro
         //        lesson2.setLessonAmount(0.0);
         //    }else{
                 lesson2.setLessonAmount(professional.getProAmount());
         //    }
             // si all-day mettre à zéro
       //      LOG.debug("proAmount added = " + d);
    }
    
    // qunad le pro bloque une journée parce qu'il donne leçon dans un autre club
    if( !isProfessional().isEmpty()){ // new 08-02-2023
        for(Lesson lesson2 : listLessons){
        // à faire : ajouter la référence du payement (generated key)
        lesson2.setLessonAmount(0.0); // new 30-01-2023 16:27
         if(new create.CreateLesson().create(lesson2, player, conn)){
            String msg = "Lesson pro created = " + lesson2;
            LOG.info(msg);
            showMessageInfo(msg);
         }else{
            String msg = "error : lesson pro not registered !!";
            LOG.error(msg);
            showMessageFatal(msg);
            break;
         }
    } // end for
    return "welcome.xhtml?faces-redirect=true";
} // end if Professional

    listLessons.forEach(item -> LOG.debug("listLessons Start Date : " + item.getEventStartDate()));
   // if(professional.getProAmount() == 0.0){ // mod 26-11-2025
    if(professional.getProAmount().equals(0.0)){    
        String msg = "amount ZERO no payment Lesson needed !!";
        LOG.info(msg);
        showMessageInfo(msg);
        return null;
    }
    creditcard = new Controllers.CreditcardController().completeWithLesson(professional, listLessons, player, conn);
    if(creditcard != null){ 
        String msg = "Creditcard with lesson ! " +  creditcard;
        LOG.info(msg);
    //  showMessageInfo(msg);
        return "price_pro.xhtml?faces-redirect=true";
    }else{
        String msg = "paiement par creditcard KO : quelle conclusion ?";
        LOG.error(msg);
        showMessageFatal(msg);
        throw new Exception(msg);
      //        return null;
    }
 }catch (SQLException ex){
            String msg = "SQLException in manageLesson " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }catch (Exception ex){
            String msg = "Exception in manageLesson " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
 } //end method manageLesson
 
 public String manageGreenfee() { // called from price_round_greenfee.xhtml
 try{
      LOG.debug("entering manageGreenfee");
      LOG.debug("with creditcard = " + creditcard);
      LOG.debug("with greenfee = " + greenfee);

      //  enlevé 25-08-2025 (etypePayment.GREENFEE.toString());  // new 15-08-2025 solution provisoire suite utilisation REST (pas le même context)!!

       sessionMap.put("creditcardType", etypePayment.GREENFEE); 
         LOG.debug("sessionMap creditcardType created = " + sessionMap.get("creditcardType"));

  // 1.  complete greenfee with price   
      greenfee = new TarifGreenfeeController().completeGreenfee(tarifGreenfee, club, round, player); 
          LOG.debug("Greenfee completed with tarif data = " + greenfee);
      if(greenfee.getPrice() == 0){
    //  if(greenfee.getPrice().equals(0)){    
          String msg = "amount ZERO,  no payment needed !!";
          LOG.info(msg);
          showMessageInfo(msg);
          return null;
       }
 // 2. complete creditcard 
      creditcard = new CreditcardController().completeWithGreenfee(greenfee,player, conn) ;
          LOG.debug("Creditcard Greenfee completed = " + creditcard);
     return("creditcard.xhtml?faces-redirect=true");
 }catch (Exception ex){
            String msg = "Exception in manageGreenfee " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
 } // end method

public String testWebServiceHttp() { // from menu TEST Webservice Creditcard Python
//public void testWebServiceHttp() { // from menu TEST Webservice Creditcard Python    
try{
       LOG.debug("entering testWebserviceHttp for creditcard, server = python  ");
    creditcard.setCreditCardHolder("LOUIS COLLET 11");
    creditcard.setCreditCardIdPlayer(324713); // mod 31-01-2023
    creditcard.setCommunication("creditcard using Java11HttpClientExample");
    //  LOG.debug("testLC = " + ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
 ///   String ldString = LocalDate.now().plusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // toujours valide !!
   //     LOG.debug("ldString = " + ldString);
 //      LOG.debug("contenu CreditCardExpirationDate = " + creditcard.getCreditCardExpirationDate());
    creditcard.setCreditcardNumber("1111222233334444");
    creditcard.setTotalPrice(35.0);
    creditcard.setTypePayment("LESSON");
    creditcard.setCreditcardType("VISA");
    creditcard.setCreditcardVerificationCode((short)567);
    creditcard.setPaymentOK(false);
    creditcard.setCreditcardCurrency("EUR"); 
      LOG.debug("just before send payment to python server " + creditcard);
     String s = new HttpController().sendPaymentServer(creditcard);
  //  if(creditcard.getErrorMessage() != null){
     if(creditcard.getCreditCardIdPlayer() != null){    // fake test !!
       String msg = "Payment validé par Amazone Payments Inc !";
       LOG.info(msg);
       showMessageInfo(msg);
      // return "creditcard_payment_executed.xhtml?faces-redirect=true";
       return "creditcard_accepted.xhtml?faces-redirect=true";
   }else{
       String msg = "payment rejected by Amazone Payments Inc ! !";
       LOG.error(msg); // + creditcard.getErrorMessage());
       showMessageFatal(msg);
       return "welcome.xhtml?faces-redirect=true"; 
  }
  }catch (Exception ex){
            String msg = "Exception in testWebServiceHttp" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} // end method

 public void testWebService() {
     String ws = null;
     Response response = null;
try{
       LOG.debug("entering testWebservice ");
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());  // traiter LocalDateTime format 

    Creditcard c = new Creditcard();
    c.setCreditCardHolder("LOUIS COLLET");
    c.setCreditCardIdPlayer(324713);
    c.setCommunication("creditcard communication");
  ///  c.setCreditCardExpirationDate(LocalDateTime.now());
    c.setCreditcardNumber("1111222233334444");
    c.setTotalPrice(35.0);
    c.setTypePayment("LESSON");
    c.setCreditcardType("VISA");
    c.setCreditcardVerificationCode((short)567);
    String strJson = om.writeValueAsString(c);
       LOG.debug("creditcard data converted in json format = " + NEW_LINE + strJson);
 //      https://github.com/networknt/json-schema-validator PENDING
       
  // fake mod     
    jakarta.ws.rs.client.Client client = ClientBuilder.newClient();
    ws = "http://localhost:8083/creditcard/" + URLEncoder.encode(strJson,"utf-8");
       LOG.debug("going to Webservice creditcard escaped \n" + ws);
    WebTarget webTarget = client.target(ws);
    Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
    // going to Python !!
    
    response = invocationBuilder.get();
    response.bufferEntity(); // plusieurs calls de response possibles
    String s = response.readEntity(String.class);
      LOG.debug("readEntity s = " + s);
    final Cookie sessionId = response.getCookies().get("JSESSIONID");  
      LOG.debug("sessionId = " + sessionId);
      
      
    if(response.getStatus() == Response.Status.OK.getStatusCode()){
  //         String msg = "response - it is OK  = " + response.getStatus() + "<br/>\n" + ws;
           String msg = response.readEntity(String.class); // + "<br/>\n" + ws;
      //      String msg = "response - it is OK  = " + response ; //.readEntity(String.class); // + "<br/>\n" + ws;
            LOG.debug(msg);
   //         msg = response.readEntity(String.class);
            showMessageInfo(msg);
    }else{
            String msg= "response - it is !NOT OK!  = " + response.getStatus() + "<br/>\n" + ws;
            LOG.error(msg);
            showMessageFatal(msg);
    }
   FacesContext facesContext = FacesContext.getCurrentInstance();
    // This is the proper way to get the view's url
   ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
      LOG.debug("we have viewhandler = " + viewHandler);
   UIViewRoot viewRoot = viewHandler.createView(facesContext, facesContext.getViewRoot().getViewId());
      LOG.debug("we have viewRoot = " + viewRoot);
      LOG.debug("we have viewId = " + viewRoot.getViewId());
  String actionUrl = viewHandler.getActionURL(facesContext, viewRoot.getViewId());
      LOG.debug("we have actionUrl = " + actionUrl);
    
       creditcard = c;  //test only 
       externalContext.redirect("creditcard_payment_executed.xhtml?faces-redirect=true");
      
      
   } catch (Exception e) {
            String msg = "£££ Exception in testWebService = " + e.getMessage() + ws;
            LOG.error(msg);
            showMessageFatal(msg);
   } finally{
        response.close();
        LOG.debug("response closed");
}  
   }   


 
 public String manageSubscription() throws Exception{ // called from subscription.xhtml
 try{
       LOG.debug("entering manageSubscription, coming from subscription.xhtml ");
       LOG.debug(" with Subscription = " + subscription);
       
  //  enlevé 25-08-225   writeString(etypePayment.SUBSCRIPTION.toString());  // new 15-08-2025 solution provisoire !!
       
       
  //  sessionMap.put("creditcardType", "SUBSCRIPTION");
     // creditcard.setCreditcardType(etypePayment.SUBSCRIPTION.toString()); // new 15-08-2025
      creditcard.setTypePayment(etypePayment.SUBSCRIPTION.toString()); // new 15-08-2025
        LOG.debug(" with Creditcard = " + creditcard);
      sessionMap.put("creditcardType", etypePayment.SUBSCRIPTION); 
    // mod 30-07-2025
    
    // comment sauver pour utiliser en methode rest ?
       LOG.debug("sessionMap creditcardType created = " + sessionMap.get("creditcardType"));
    switch(subscription.getSubCode()){
        case "TRIAL" -> {    // trial one day
           LOG.debug("SubCode = TRIAL");
         //  subscription = new PaymentsSubscriptionController().complete(subscription, conn); // new 24-08-2025
           boolean b = new Controllers.PaymentsSubscriptionController().createPayment(subscription, conn);
           // à compléter
           return "welcome.xhtml?faces-redirect=true";
        }
        case "MONTHLY","YEARLY" ->{ 
            LOG.debug("getSubCode()is MONTHLY or YEARLY");
            // mod 16-04-2024
          //  subscription = new PaymentsSubscriptionController().completePriceAndCommunication(subscription);
            subscription = new PaymentsSubscriptionController().complete(subscription, conn);
            creditcard = new CreditcardController().completeWithSubscription(subscription,player,conn) ;
              LOG.debug("creditcard completed with subscription = " + creditcard);
            return "creditcard.xhtml?faces-redirect=true";
        }
        default -> {LOG.debug(": getSubCode() UNKNOWN = " + subscription.getSubCode() );
            return null;}
    } //end switch
    
  }catch (Exception ex){
            String msg = "Exception in manageSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
//         return null;
 } //end method manageSubscription

public String registereIDPlayer(){ // throws javax.smartcardio.CardException  // mod 03-12-2017
    try{
            LOG.debug("entering register eID Player");
        player = new smartCard.SmartcardBelgium().initClient();  // call webservice
        // http://localhost:8080/rest-demo-1.0/rest/tutorial/pojoJson"
      //  Player player = new Player();
      LOG.debug("back from external resource with cardBelgium = " + player);
      if(player == null){
          String msg = "eid Card Belgium not found";
          LOG.error(msg);
          showMessageFatal(msg);
          return null;
      }
    
       LOG.debug("from registeeIDPlayer = ");// + p);
 //   player = p;
 return "player.xhtml?faces-redirect=true";
  // return null; // provisoire
    }catch (Exception ex){
            String msg = "Exception in registereIDPLayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
}  // end method    

public void onRowToggleCompetition(ToggleEvent event) { // competition
    LOG.debug("event data = " + event.getData().toString());
    LOG.debug("event visibility = " + event.getVisibility());
  //  event.getComponent().
     String msg =  "Row State " + event.getVisibility() + " / " + event.getData().toString();
     LOG.debug(msg);
     showMessageInfo(msg);
    }

public void rowPlayerSelect(SelectEvent<Object> event) { // used in selectOtherPlayers.xhtml
        LOG.debug("entering rowPlayerSelect");
        LOG.debug("event = " + event.getObject().toString());
        String msg = "size selected players = " + player.getDraggedPlayers().size();
        LOG.debug(msg);
     //   showMessageInfo(msg);
        if (player.getDraggedPlayers().size() > 4) {
            msg = "You can't select more than 4 players";
            LOG.error(msg);
            showMessageFatal(msg);
        }
    }

public void loginAPI(){
    LOG.debug("entering loginAPI coming from login_securityAPI.xhtml");
    LOG.debug("username = " + login.getUsername());
    LOG.debug("password = " + login.getPassword());
  }


public String login() throws IOException, SQLException { // executed via actionView in login.xhtml
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
     return null;
  //      return "login.xhtml?faces-redirect=true";  // old - doesn't work
    } // end method

public String logout(String lgt){
    try{
        LOG.debug("entering logout() for player = " + player.getIdplayer());
        LOG.debug("entering logout with parameter = " + lgt);
        if(player.getIdplayer()!= null){
            Audit a = new Audit();
            a.setAuditPlayerId(player.getIdplayer());
            a = new find.FindLastAudit().find(a, conn);
            if(a != null){
                String msg = "ending an audit which started at : " + a.getAuditStartDate().format(ZDF_TIME);
                LOG.debug(msg);
                showMessageInfo(msg);
                boolean ok = new update.UpdateAudit().stop(a, conn);
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
          String msg = "Time-out for inactivity from masterTemplate!";
          LOG.debug(msg);
          showMessageInfo(msg);
      //    return "login.xhtml?faces-redirect=true"; // mod 31-08-2025
         return "session_expired.xhtml?faces-redirect=true";
    }else{
        LOG.debug("unknown logout message : " + lgt);
        return null;
    }
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

public void savePlayer(ActionEvent actionEvent) {
        //Persist user  
        LOG.debug("entering savePlayer !!");
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + player.getPlayerFirstName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public static boolean isPostback() {
        return FacesContext.getCurrentInstance().isPostback();
    }

    public void preRenderClub() throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        LOG.debug("preRenderClub called");
        LOG.debug("preRenderView  : idclub = " + club.getIdclub());
    //    PhaseId currentPhaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
    //    LOG.debug("currentPhaseId 1 = " + currentPhaseId);
        LOG.debug("currentPhaseId 1 = " + FacesContext.getCurrentInstance().getCurrentPhaseId());
        LOG.debug("isPostBack ? = " + isPostback());
        //    if(club.getIdclub()!= null)
        if ((!isPostback()) && (club.getIdclub() != null))
        //    postback = false
        {
            club = new Club();
            LOG.debug("preRenderClub : club forced to null ");
        }
    }

    public void preRenderCourse() {
        LOG.debug("preRenderCourse called");
    }

  public List<String> teeStartList(Player otherPlayer) throws SQLException {  // used in inscription.xhtml and inscriptions_other_players.xhtml
       LOG.debug("entering teeStartList = ..." );
       LOG.debug("with inscription player = " + otherPlayer.toString()); // si inscription d'autres players
       LOG.debug("with current player = " + player.toString());
     try{
            return new find.FindTeeStart().find(course, otherPlayer, round, conn);
      } catch (Exception e) {
            String msg = "££ Exception in teeStartList ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally { }
    } //end method
 
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

//  !! ne pas toucher ..String typePayment.
public void onCompletePayment() {
    LOG.debug("entering onCompletePayment coming from creditcard_accepted.xhtml"); // line 150
  try{
     creditcard.setTypePayment(sessionMap.get("creditcardType").toString());
     savedType = creditcard.getTypePayment();
        LOG.debug("before payment creditcard = " + creditcard); // completed
    // IMPORTANT 
   // new 29-07-2025 va chercher dans HttpController ... qui va dans webservice server python Amazone Payments Inc
        // qui reviendra vers 
 ///    creditcard = new CreditcardController().getCC1(creditcard); 
      String v = new CreditcardController().getCC2(creditcard);
   //   String v = new HttpController().sendPaymentServer(creditcard); // plus besoinde passer par creditcardController !!
        LOG.debug("var v returned in OnCompletePayment = " + v);
        LOG.debug("creditcard returned in OnCompletePayment = " + creditcard);
  //  if(v.equals("OK")){
    if(v.equals("200")){    
       String msg = "Payment validé par Amazone Payments Inc !";
       LOG.info(msg);
  ///     showMessageInfo(msg);
          LOG.debug("sessionMap creditcardType in onCompletePayment = " + sessionMap.get("creditcardType").toString());
       // new 17-11-2025 enfin une solution propre et pas un hack !!!
           LOG.debug("before going with context to 5000/about");
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().redirect("https://localhost:5000/about");
        context.responseComplete(); // termine proprement le cycle JSF
// Use FacesContext#responseComplete() to signal JSF that you've already handled the response
// yourself and that JSF thus doesn't need to render the response
// FacesContext.responseComplete() does not send any response to the client.
// It merely sets a flag in the context to tell the JSF framework that no further 
// lifecycle phases should be processed after the current one completes.    
          LOG.debug("after redirect with context to 5000/about");  // passe la main à python   
    }else{
        String msg = "payment rejected by Amazone Payments Inc ! !";
        showMessageFatal(msg);
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().redirect("creditcard_payment_canceled.xhtml?faces-redirect=true");
        context.responseComplete(); // termine proprement le cycle JSF
    }
 }catch (Exception e) {
            String msg = "££ Exception in onCompletePayment  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           //return null; // indicates that the same view should be redisplayed
 }

} //end method onCompletePayment

@jakarta.ws.rs.GET // coming from CreditcardController
@jakarta.ws.rs.Path("payment_handle/{isbn}") 
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_HTML)
public jakarta.ws.rs.core.Response handlePayments(
            @PathParam("isbn") String uuid,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Context UriInfo context,
            @Context HttpHeaders hh,
            @CookieParam("JSESSIONID") String sessionid,
            @HeaderParam("User-Agent") String whichBrowser,
            @HeaderParam("From") String from,
        //    @HeaderParam("Accept") String accept,
        //    @CookieParam("User") String user,
            @CookieParam("PaymentReference") String reference,
            @CookieParam("Amount") String amount,
            @CookieParam("Currency") String Currency,
            @DefaultValue("2") @QueryParam("step") int step,
            @DefaultValue("true") @QueryParam("min-m") boolean hasMin
    ) throws IOException { // coming from paymentChoice direct from python server from onCompletePayment() mod 12-08-2025
try{
        LOG.debug("payement reference = " + reference);
      //  LOG.debug("UriInfo/context = " + context);
      //  MultivaluedMap<String, String> queryParams = context.getQueryParameters();
        MultivaluedMap<String, String> pathParams = context.getPathParameters();
      //  LOG.debug("queryParams = " + queryParams);
        LOG.debug("pathParams = " + pathParams);
        URI uri =  context.getAbsolutePath();
        LOG.debug("uri AbsolutePath = " + uri);
        LOG.debug("amount = " + amount);
        
    creditcard.setCreditcardPaymentReference(reference); // new 14-08-2025
          LOG.debug("handle savedType = " + getSavedType()); // et toujours GREENFEE le grand mystère !!
    creditcard.setTypePayment(getSavedType()); // new 25-08-2025 work around !! 
          LOG.debug("handlePayments with CourseController Creditcard = " + creditcard); 
// common part all payments
        LOG.debug("before needsUpdate, creditcard = " + creditcard);
    boolean b = new CreditcardController().needsUpdate(creditcard,player, conn);
        LOG.debug("creditcard in db created ou modified ? " + b);
    creditcard.setPaymentOK(true); // le paiement est exécuté! bien au bon endroit ??
        LOG.debug("creditcard.setPaymentOK(true)");
    var typePayment = creditcard.getTypePayment();

// subscription payment
    if(typePayment.equals(etypePayment.SUBSCRIPTION.toString())){
            LOG.debug("handlePayments for subscription = " + subscription);
        b = new Controllers.CreditcardController().handlePaymentSubscription(creditcard, subscription, conn);
            LOG.debug("returned payment subscription = " + b); // devenu inutile
    try{
  // old solution      response.sendRedirect(request.getContextPath() + "/creditcard_generator.xhtml?");  // new 21-08-2025
    } catch (Exception e) {
        String error = "error in subscription payment : creditcard_generator failed ! : " + e;
        LOG.debug(error);
    }
}  // end subscription Payment 

 // Cotisation payment
    if(typePayment.equals(etypePayment.COTISATION.toString())){ // club member
            LOG.debug("handlePayments with cotisation = " + cotisation); // dates non complétées !
            LOG.debug("handlePayments with creditcard = " + creditcard);
        cotisation.setPaymentReference(creditcard.getCreditcardPaymentReference());
        if(new Controllers.PaymentsCotisationController().RegisterPaymentandInscription(creditcard, cotisation,
                   player, round, club, course, inscription, conn)){
               LOG.debug("inscription now definitively done");
        }else{
                // a compléter
        }
        //   fonctionne response.sendRedirect(request.getContextPath() + "/creditcard_generator.xhtml?");  // new 21-08-2025
        //    return Response.status(Response.Status.ACCEPTED).entity("Payment cotisation is OK, louis").build();
       } // end Cotisation  
    
 //  Greenfee payment 
         if(typePayment.equals(etypePayment.GREENFEE.toString())){ //  donne erreur if(creditcard.getTypePayment().equals(etypePayment.GREENFEE.toString())){ 
              LOG.debug("handlePayments with cotisation = " + greenfee); // dates non complétées !
              LOG.debug("handlePayments with creditcard = " + creditcard);
            greenfee.setPaymentReference(creditcard.getCreditcardPaymentReference());
            if(new Controllers.PaymentsGreenfeeController().RegisterPaymentandInscription(creditcard, greenfee, player, round, club, course, inscription, conn)){
               LOG.debug("greenfee paid and inscription now definitively done");
            }else{
                // complété 12-05-2025
                String msg = "payment déjà enregistré pour round = " + round;
                LOG.error(msg);
                showMessageFatal(msg);
                //return null;
            }
       //     response.sendRedirect(request.getContextPath() + "/creditcard_generator.xhtml?");  // new 21-08-2025
       //     return Response.status(Response.Status.ACCEPTED).entity("Payment greenfee is OK, louis").build();
    } // end greenfee payment
         // common part all payments
 // IMPORTANT !! 23-10-2025 cherché longtemps et c'était si simple !!
 // retourne à python server Amazone Payments Inc et envoye un écran de confirmation de 5 secs!!
  ////   ligne remplacée le 24-10-2025 par ci-après     response.sendRedirect("https://localhost:5000/payment_generator"); // HttpServletResponse
         return jakarta.ws.rs.core.Response
                .status(Response.Status.FOUND) // 302
                .location(java.net.URI.create("https://localhost:5000/payment_generator"))
                .build();
/*/ lesson payment
    if(typePayment.equals(etypePayment.LESSON.toString())){
         LOG.debug("handlePayments lesson with professional = " + professional);
         LOG.debug("handlePayments lesson with creditcard = " + creditcard);
         LOG.debug("handlePayments lesson with listlessons = " + listLessons.toString());
        

 // une ligne paiement par leçon
   for(Lesson lesson2 : listLessons){
      if(new create.CreatePaymentLesson().create(lesson2, creditcard, professional, conn)){ //true
           LOG.debug("after createPaymentLesson : payment OK");
           String msg = LCUtil.prepareMessageBean("lesson.success") + "<br/>"
                  + lesson2 + "<br/>" + "<br/>" + creditcard + "<br/>" + "<br/>" + professional
                  + " <br>for club = " + club.getClubName();
           LOG.info(msg);
           showMessageInfo(msg);
      }else{
            String msg = "error : payment lesson not done !!";
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // retourne d'ou il vient    ?? à vérifier    
      }
    } // end for
        // récupérer la generated key = pour faire référence au paiement ??
         int generatedKey = create.CreatePaymentLesson.getGeneratedKey();
         LOG.debug("key payment lessons = " + generatedKey);
  //   loop sur listLessons
    LOG.debug("there are lessons :  " + listLessons.size());
  // create Lesson
    for(Lesson lesson : listLessons){
        // à faire : ajouter la référence du payement (generated key)
        lesson.setLessonAmount(professional.getProAmount()); // new 30-01-2023 16:27
         if(new create.CreateLesson().create(lesson, player, conn)){
            String msg = "Lesson student created = " + lesson;
            LOG.info(msg);
            showMessageInfo(msg);
         }else{
            String msg = "error : lesson not registered !!";
            LOG.error(msg);
            showMessageFatal(msg);
            break;
      //      return null; // retourne d'ou il vient    ?? à vérifier         
         }
    } // end for
      return "login.xhtml?faces-redirect=true";
    } // end Lesson payment 
    

       
       */
 }catch (Exception e) {
            String msg = "££ Exception in handlePayments  = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return null; // indicates that the same view should be redisplayed
        } finally {}
//return null;
} //end method

//  utilisé dans creditcard_accepted.xhtml
public void onStart() {
        String msg = "entering onStart, progress1 = " + progress1;
        LOG.debug(msg);
        showMessageInfo(msg);
      //  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Started"));
    }

public void onProgress() {
    String msg = "Progress Updated " + progress1;
    showMessageInfo(msg);
}


 public Integer getProgress1() {
        progress1 = updateProgress(progress1);
        return progress1;
    }
 public void setProgress1(Integer progress1) {
//     LOG.debug("progress 1 " + progress1);
        this.progress1 = progress1;
    }
 
 private Integer updateProgress(Integer progress) { // important used in creditcard_accepted.xhtml de façon NON visible !!
   //  LOG.debug("entering updateProgress with param = " + progress);
        if(progress == null) {
            progress = 0;
        }else {
            progress = progress + (int)(Math.random() * 35);
            if(progress > 100)
                progress = 100;
        }
   //      LOG.debug("progress returned = " + progress);
        return progress;
    }

 public void cancelProgress() {
          LOG.debug("Payment canceled by User");
        progress1 = null;
        creditcard.setPaymentOK(false); // le paiement n'est pas exécuté!
        String msg = "Creditcard payment canceled by user";
        LOG.error(msg);
        showMessageFatal(msg);
    }

// used in schedule_pro
     public List<Lesson> getListLessons() {
        return listLessons;
    }

    public void setListLessons(List<Lesson> listLessons) {
        this.listLessons = listLessons;
    }
  
  public void textCalculationIndex() throws Exception { 
  try{  
           LOG.debug("entering textCalculationIndex"); // called from dialog_handicap_index.xhtml
        //       <f:viewAction action="#{courseC.textCalculationIndex()}" />
           LOG.debug("selected roundid = " + handicapIndex.getSelectedHandicap().getRound().getIdround());
           LOG.debug("current playerid = "+ player.getIdplayer());
        LoggingUser logging = new LoggingUser();
        logging.setLoggingIdPlayer(player.getIdplayer());
        logging.setLoggingIdRound(handicapIndex.getSelectedHandicap().getRound().getIdround());
        logging.setLoggingType("H"); // Handicap
     // new 16/08/2022
         handicapIndex.setCalculations(new Controllers.MongoCalculationsController().read(logging));
   //           LOG.debug("Handicap Calculations text = " + handicapIndex.getCalculations());
  }catch (Exception ex){
            String msg = "Exception in textCalculationIndex " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  }
 } //end method  
 
  public void textCalculationRound() throws Exception { 
  try{  
        LOG.debug("entering textCalculationRound"); // called from dialog_played_rounds.xhtml
        //       <f:viewAction action="#{courseC.textCalculationRound()}" />
        LOG.debug("selected roundid = " + selectedPlayedRound.getRound().getIdround());
        LOG.debug("current playerid = "+ player.getIdplayer());
    LoggingUser logging = new LoggingUser();
    logging.setLoggingIdPlayer(player.getIdplayer());
    logging.setLoggingIdRound(selectedPlayedRound.getRound().getIdround());
    logging.setLoggingType("R"); // Round
       LOG.debug("logging_user = " + logging);
// mod 16/08/2022 migration vers mongoDB
     round.setCalculations(new Controllers.MongoCalculationsController().read(logging));
    
 //          LOG.debug("Calculations text = " + round.getCalculations());
  }catch (Exception ex){
            String msg = "Exception in textCalculationRound " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  }  
 } //end method  
 
 public void SendEmailTest(){
 try{
     String to = "louis.collet@skynet.be";
     String data2 = LocalDateTime.now() + "<br/> <bold> from courseController - sendmailtest Hello ! This is golfLC, the famous application v2";
     Path pathQRC = utils.QRCodeGenerator.manageQR(data2);
     // tester si null ?
    // Player p = player;
     Path pathICS = utils.VcardGenerator.create(player);
     boolean b = new mail.SendEmail().sendHtmlMail("<bold>test CourseController </bold>Renouvellement de votre souscription à GolfLC",
             "message du mail",to,
             pathICS,
             pathQRC, // QRCode
             "es");
     
     String msg = "boolean result send mail = " + b;
     LOG.info(msg);
     showMessageInfo(msg);
 }catch (Exception ex){
            String msg = "SendEmailTest" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
     //       return null;
 }
 } // end method  
  public static void numberText(String s){
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
  
   public void main(String args[]) throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        //    not used
        LOG.debug(" -- main terminated");
    } // end main
} // end class