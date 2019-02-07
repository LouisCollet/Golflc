package lc.golfnew;

import SmartCard.HandleSmartCard;
import entite.*;
import exceptions.LCCustomException;
import find.*;
import static interfaces.Log.LOG;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.annotation.ApplicationMap;
import javax.faces.annotation.SessionMap;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDateTime;
import static utils.LCUtil.intArraytoStringArray;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;
@Named("courseC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped
public class CourseController implements Serializable, interfaces.GolfInterface, interfaces.Log{
     @Inject private Club club;
     @Inject private Car car;
     @Inject private Course course;
     @Inject private Course course2;
     @Inject private Round round; 
     @Inject private Tee tee; 
     @Inject private Hole hole;
     @Inject private Player player;
     @Inject private Player player2;
     @Inject private Handicap handicap; 
     @Inject PlayingHcp playingHcp;
     @Inject private ScoreStableford scoreStableford; // mod 20/07/2015
     @Inject private ScoreMatchplay scoreMatchplay; 
     @Inject private ScoreScramble scoreScramble; // new 26/07/2015
 //    @Inject private EScoreCardList scorecard;
     @Inject private PlayerHasRound inscription;
     @Inject private Inscription inscriptionNew;
     @Inject private Old_Matchplay matchplay;
     @Inject private Subscription subscription;
     @Inject private Cotisation cotisation;
     @Inject private HolesGlobal holesGlobal;  // new 13/08/2017
     @Inject private Flight flight;
     @Inject private TarifGreenfee tarifGreenfee;  // mod 24-01-2019
     @Inject private Creditcard creditcard;
     @Inject private Login login;
     @Inject private TarifMember tarifMember;
     @Inject private Greenfee greenfee;
     @Inject private Unavailable unavailable;
     private final static List<Integer> STROKEINDEX = new ArrayList<>();
    private final static List<Integer> NUMBERS = new ArrayList<>();
    
    private ECourseList selectedPlayedRound; // mod 11/03/2018
    private final static List<SelectItem> GAMES = new ArrayList<>();
    private final static List<SelectItem> START = new ArrayList<>();
    private static String[] games = null; 
//    private static String[] start = null; 
    private static int[] parArray = null;
    private String parArrayString = "";
    private static String otherGame = null; // new 15/06/2014
    private SelectItem[] gameOptions; // new 01/12/2013
    private static String[] seasons = null; 
    private SelectItem[] seasonOptions; // new 01/12/2013
    private final static List<SelectItem> LANGUAGES = new ArrayList<>();
    private final static List<Integer> VALUES = new ArrayList<>();
    private static String[][] sc2;
    private List<Average> listavg;
    private List<Old_Matchplay> listmp;  // new 30/09/2014  Ã  mofifier certainement !!
    private List<ScoreScramble> listscr;  // new 30/09/2014
    private List<Subscription> subscr;
    private List<String> teeStartListe = null;
//    private final static String PHOTOFILE = "nophoto.jpeg";
//                              private boolean NextCourse; // 13/01/2013
    private boolean NextStep; // 15/01/2013
    private boolean NextInscription; // 15/01/2013
    private boolean NextScorecard; // 15/01/2013
    private boolean NextPlayer; // 10/02/2013

    private boolean Connected; // 09/05/2013 // used for Logout button in header.xhtml, in selectPlayer
    private boolean skip;

    private boolean ShowButtonCreateScore; // 27/10/2013
    private static boolean ShowButtonCreateStatistics; // 27/10/2013
    private static boolean ShowButtonCreditCard; // 27/10
    private boolean ShowButtonCreateCourse; // 27/10/2013
    private boolean NextPanelPlayer = false;  // 16/11//2013
    
    
 //   private CartesianChartModel linearModel;
 //   private CartesianChartModel barModel;
    private List <?>filteredCars; // ne pas supprimer :nécessaire depuis Primefaces 3.4 (faut une List) 
    private List <?>filteredCourses; // new 03/08/2014
    private ArrayList<Flight> filteredFlights;
    private List <?>filteredPlayers; // new 03/08/2014

    private List <?>filteredPlayedRounds; // new 03/08/2014
    private List <?>filteredInscriptions; // new 03/08/2014
    private List<ECourseList> listStableford;
    private Player selectedPlayer;
    
    private List<Player> selectedPlayersMatchPlay; // new 31/08/2014
  //  private List<Player> selectedOtherPlayers; // new 30/06/2017
    private DualListModel<Player> dlPlayers; // new 03/09/2014
//    private List<Player> playersTarget; // new 03/09/2014
    List<Player> playersTarget = new ArrayList<>();
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
    private String inputStat = null;
//    private String inputRound = null;
      
    private String inputParticipants = null;
    private String inputInscription = null;
    private String inputHandicap = null;
    private int inputPlayingHcp = 0;
    private String inputPlayedRounds = null;
    private final String htmlBr = "<br />";
    private final String htmlH1 = "<h1>";
    private String introductionTxt;
    private MapModel mapModel = null;  // new 26/08/2014
    private Overlay overlay; // new 26/08/2014
    private List<Car> row; // new testing rowedit 17/09/2014
    private List<Player> lp = null;
//private static BeID eID = null;
    private Connection conn = null;
    private Connection connPool = null;
    private String radioButtonJSF;
    private String createModifyPlayer = "C";  // utilisé pour choisir player.xhtml ou player_modify.xhtml
    private String SunRiseSet; // = null; 
    private AtomicInteger progressInteger = new AtomicInteger();
    private ExecutorService executorService;
    private String uuid; // new 14/08/2018
    private String emoji;
    private int cptCourse = 0;
    private int cptFlight = 0;
    ArrayList<Flight> fl = null;
    List<String> gameList = Arrays.asList("STABLEFORD","SCRAMBLE","CHAPMAN","STROKEPLAY","MP_FOURBALL","MP_FOURSOME","MP_SINGLE");
    List<String> parList = Arrays.asList("73","72","71","70","69");
 //   List<Integer> parList = Arrays.asList(73, 72, 71, 70, 69);
    private Map<String, String> availableQualifying; // +getter (no setter necessary)
    private Map<String, String> members;
    private Map<Integer, String> data = new HashMap<>();
    
    
    public CourseController()  // constructor
    {
        this.listavg = null;
        this.setSunRiseSet(null);
    }

 @PostConstruct
 public void init(){ // attention !! ne peut absolument pas avoir : throws SQLException

    try{
    //        LOG.info("new");
            conn = utils.DBConnection.getConn();
             LOG.info("cette connection database sera utilisée pendant toute la session = "+ conn);
            
           connPool = utils.DBConnection.getPooledConnection();
             LOG.info("cette connection pooled database sera réutilisée = "+ connPool);
           
             conn = connPool;
             
       LOG.info("** Webbrowser url = " + utils.LCUtil.firstPartUrl());
 /*         
          
  LOG.info("starting shiro ...");         
          IniRealm iniRealm = new IniRealm(USER_DIR + "/src/main/webapp/WEB-INF/shiro.ini");
          DefaultSecurityManager securityManager = new DefaultSecurityManager(iniRealm);
          SecurityUtils.setSecurityManager(securityManager);
          Subject currentUser = SecurityUtils.getSubject();
LOG.info(" shiro 01");    
//print their identifying principal (in this case, a username): 
        LOG.info( "Shiro User [" + currentUser.getPrincipal() + "] logged in successfully." );

          if (!currentUser.isAuthenticated()) {
              LOG.info("not authenticated");
            UsernamePasswordToken token = new UsernamePasswordToken("user", "password");
            token.setRememberMe(true);
            try {
                currentUser.login(token);
            }catch (UnknownAccountException uae) {
                    LOG.error("Username Not Found!", uae);
            }catch (IncorrectCredentialsException ice) {
                 LOG.error("Invalid Credentials!", ice);
            }catch (LockedAccountException lae) {
                 LOG.error("Your Account is Locked!", lae);
            }catch (AuthenticationException ae) {
                 LOG.error("Unexpected Error!", ae); 
            } 
        }  //end if
  LOG.info("end of shiro 1");
  
  if (currentUser.hasRole("admin")) {
        LOG.info("Welcome Admin");
  }else if(currentUser.hasRole("editor")) {
        LOG.info("Welcome, Editor!");
  }else if(currentUser.hasRole("author")) {
        LOG.info("Welcome, Author");
  }else { 
        LOG.info("Welcome, Guest");
}

  if(currentUser.isPermitted("articles:compose")) {
    LOG.info("You can compose an article");
} else { 
    LOG.info("You are NOT permitted to compose an article!");
} 
 
if(currentUser.isPermitted("articles:save")) {
    LOG.info("You can save articles");
} else { 
    LOG.info("You can NOT save articles");
}

if(currentUser.isPermitted("articles:publish")) {
    LOG.info("You can publish articles");
} else { 
    LOG.info("You can NOT publish articles");
}
*/
 /*
 
   // new apache shiro 24-12-2017
   //     String s = utils.GetPath.getpath();
  //      LOG.info("before shiro, path = " + s);
        Subject currentUser = SecurityUtils.getSubject();
            LOG.info("subject current user =  = " + currentUser);
  //      Session session = currentUser.getSession();
  //      session.setAttribute( "someKey", "aValue" );
    if ( !currentUser.isAuthenticated() ) {
        LOG.info(" current user is  n ot authenticated =  = " + currentUser);
    //collect user principals and credentials in a gui specific manner
    //such as username/password html form, X509 certificate, OpenID, etc.
    //We'll use the username/password example here since it is the most common.
    //(do you know what movie this is from? ;)
        
         UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
    //this is all you have to do to support 'remember me' (no config - built in!):
        token.setRememberMe(true);
        currentUser.login(token);
}

        Factory<SecurityManager> factory = new IniSecurityManagerFactory(USER_DIR + "/src/main/webapp/WEB-INF/shiro.ini");
            LOG.info("factory =  = " + factory);
        SecurityManager securityManager = factory.getInstance();
            LOG.info("security manager =  = " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        UsernamePasswordToken token = new UsernamePasswordToken("admin","test123");
        token.setRememberMe(true);
        
        LOG.info("after shiro");
  */        
          
 // https://github.com/vdurmont/emoji-java
//  String str = "An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis! man= :man: - woman = :woman:";
    //      + "An &#128512;awesome &#128515;string with a few &#128521;emojis!"  // html decimal
    //      + "An &#x1f600;awesome &#x1f603;string with a few &#x1f609;emojis!"; // html hexadecimal
//  emoji = EmojiParser.parseToUnicode(str);
//  LOG.info("emoji = " + emoji);
 // Prints:
// "An 😀awesome 😃string 😄with a few 😉emojis!"
// new 23-10-2018 
//https://stackoverflow.com/tags/selectonemenu/info

 //members = ImmutableMap.of(
 //             "Full TarifMember", 2500,
 //             "Semainier", 2100,
 //             "Monday", 1400
 //     );
   

      
      members = new LinkedHashMap<>();
      members.put("Full Member", "2500");
      members.put("Semainier", "2100");
      members.put("Monday", "1400");
  //    for (Map.Entry<String, Integer> entry : members.entrySet()) {
  //      LOG.info("members info : " + entry.getKey() + ":" + entry.getValue().toString());
  //    }
      members.forEach((key, value) -> LOG.info("Members information = " + key + ":" + value));
      
      
    availableQualifying = new LinkedHashMap<String, String>();
    availableQualifying.put("Non Qualifying", "N");
    availableQualifying.put("Qualifying", "Y");
    availableQualifying.put("Counting", "C");

    LOG.info("availableQualyfying initialized ");

        if (LANGUAGES.isEmpty())
        {
            LANGUAGES.add(new SelectItem("en", "English"));  //first field = itemValue, stocke dans DB
            LANGUAGES.add(new SelectItem("de", "German"));
            LANGUAGES.add(new SelectItem("fr", "Français"));
            LANGUAGES.add(new SelectItem("nl", "Nederlands"));
        }
             //   LOG.info("LANGUAGES initialized" + LANGUAGES.toString() );//Arrays.toString(games)/
              //  LOG.info("LANGUAGES initialized" + Arrays.toString(LANGUAGES.toArray()));
 // est en production !!            
        if (GAMES.isEmpty()) {
            GAMES.add(new SelectItem("STABLEFORD", "Stableford"));  //first field = itemValue, stockÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©e dans DB
            GAMES.add(new SelectItem("SCRAMBLE", "Scramble"));
            GAMES.add(new SelectItem("CHAPMAN", "Chapman"));
            GAMES.add(new SelectItem("STROKEPLAY", "Strokeplay"));
            GAMES.add(new SelectItem("ZWANZEURS", "Zwanzeurs"));
            GAMES.add(new SelectItem("MP_FOURBALL", "Fourball"));
            GAMES.add(new SelectItem("MP_FOURSOME", "Foursome"));
            GAMES.add(new SelectItem("MP_SINGLE", "Single"));
  //              LOG.info("GAMES initialized" + GAMES.toString() );
              //   LOG.info("GAMES initialized" + Arrays.toString(GAMES.toArray()));
        }
              
        if (START.isEmpty())
        {
            START.add(new SelectItem("YELLOW", "Yellow"));  //first field = itemValue, stockage dans DB
            START.add(new SelectItem("BLACK", "Black"));
            START.add(new SelectItem("WHITE", "White"));
            START.add(new SelectItem("BLUE", "Blue"));
            START.add(new SelectItem("RED", "Red"));
            START.add(new SelectItem("ORANGE", "Orange"));

        //  LOG.info("tee start position initialized" + START.toString() );
        }
/*               
     if (start == null)  // new 25/02/2017, used in tee.xhtml, teeRestart, inscription.xhtml
     {
         start = new String[6];
         start[0]="YELLOW";
         start[1]="BLACK";
         start[2]="WHITE";
         start[3]="BLUE";
         start[4]="RED";
         start[5]="ORANGE";
            LOG.info("tee start position iinitialized = "+ Arrays.toString(start) );
      }                   
   */             
  if (games == null) {
         games = new String[7];
         // public enum GameType {STABLEFORD,SCRAMBLE,CHAPMAN,STROKEPLAY,ZWANZEURS,MP_FOURBALL,MP_FOURSOME,MP_SINGLE}
         games[0]=Round.GameType.STABLEFORD.toString(); //"STABLEFORD";
         games[1]=Round.GameType.SCRAMBLE.toString();
       //  games[1]="ZWANZEURS";
    //     games[2]="SCRAMBLE";
         games[2]="CHAPMAN";
         games[3]="STROKEPLAY";
         games[4]="MP_FOURBALL";
         games[5]="MP_FOURSOME";
         games[6]="MP_SINGLE";
   //              LOG.info("games initialized = "+ Arrays.toString(games) );
      }

  
       gameOptions = createFilterOptions(games); // used in show_played_rounds.xhtml new 01/12/2013
            //    LOG.info("games Options initialized = "+ Arrays.toString(gameOptions) );
                

         seasons = new String[2];
         seasons[0]="2013-2014";
         seasons[1]="2012-2013";
//            LOG.info("seasons initialized = " + Arrays.toString(seasons));
        seasonOptions = createFilterOptions(seasons); // new 01/12/2013
  //          LOG.info("season Options initialized = "+ Arrays.toString(seasonOptions) );
//setNextCourse(false);
//setNextTee(false);
// setNextHole(false);
        setNextStep(false);
        setNextInscription(false);
        setNextScorecard(false);
        setNextPlayer(false);
        setConnected(false); // used for Logout button in header.xhtml, in selectPlayer
  //      setZwanzeur(false); //16/11/2013 supprimé 
        setShowButtonCreateScore(true);
        setShowButtonCreateStatistics(false);
        
///        utils.LCUtil.startup(); //mod 14/08/2014
           
  ClassLoader clo = Thread.currentThread().getContextClassLoader();
 //   LOG.info("ClassLoader clo = " + clo);
  // files sous /src/main/resources/
  InputStream is = clo.getResourceAsStream("version_components.properties");
  Properties prop = new Properties();
  prop.load(is);
  String value = prop.getProperty("message.test3", "message.test3 not found");
  LOG.info("Value from version_components.properties : " + value);
//  LOG.info("Value from System.getProperty : " + System.getProperty("primefaces.version"));

  is = clo.getResourceAsStream("myPOM.properties");
  Properties prop1 = new Properties();
  prop1.load(is);
  String version = prop1.getProperty("primefaces.version");
//  LOG.info("Value from properties : " + value2);
  LOG.info("Primefaces version from myPOM.properties: " + version); 
  
  FacesContext facesContext = FacesContext.getCurrentInstance();
  HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
  Enumeration<?> e = session.getAttributeNames();
    while (e.hasMoreElements())
    {
     String attr = (String)e.nextElement();
        if(!attr.equals("facelets.ui.DebugOutput")){// continue;  // print view !!
            LOG.info("   session   attr  = "+ attr);
            Object value2 = session.getAttribute(attr);
            LOG.info("   session   value = "+ value2);
        }
    }
 ///        utils.LCUtil.logMap(session);
  HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
  HttpServletResponse res = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
  // ne fonctionne pas
  //utils.SessionSnoop ssno = new utils.SessionSnoop();
  //ssno.doGet(req, res);
    LOG.info("we start a reset for all the working fields of the new session ");
  reset("from init in CourseController");
 //  LOG.info("leaving " + this.getClass().getSimpleName() + " Postconstruct init()");
   LOG.info("NEW session just started !! " + NEW_LINE);
 //  utils.MySessionCounter msc = new utils.MySessionCounter();
 //  LOG.info("The are activeSessions = : " + msc.getActiveSessions()); 
// HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
// HttpServletResponse res = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();

 } catch (Exception e){
            String msg = "££ Exception in creating Connection or init in courseC = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
     }
  } //end method init

    @PreDestroy
    public void exit() {
        LOG.info(" ------------------ from CourseController PreDestroy exit ()...");
    }
public Map<Integer, String> getData() {
        LOG.info("called MapBean.getData");
        data.put(1, "java ee 8");
        data.put(2, "jsf 2.3");
        return data;
    }

    public TarifMember getTarifMember() {
        return tarifMember;
    }

    public void setTarifMember(TarifMember member) {
        this.tarifMember = member;
    }

    public Map<String, String> getAvailableQualifying() {
        return availableQualifying;
    }

    public List<String> getGameList() {
        return gameList;
    }

    public List<String> getParList() {
        return parList;
    }

    public void setParList(List<String> parList) {
        this.parList = parList;
    }

    public Greenfee getGreenfee() {
        return greenfee;
    }

    public Unavailable getUnavailable() {
        return unavailable;
    }

    public void setUnavailable(Unavailable unavailable) {
        this.unavailable = unavailable;
    }

    
    public void setGreenfee(Greenfee greenfee) {
        this.greenfee = greenfee;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
    
    public void setGameList(List<String> gameList) {
        this.gameList = gameList;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
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
  //  LOG.info("entering validateMP4");
    LOG.info("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.info("entering validateMP4 - toValidate Id = " + toValidate.getId() );
   // LOG.info("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.info("entering validateMP4 - value = " + value.toString());
  //  LOG.info("UIcomponent, getFamily = " + toValidate.getFamily());
  //  LOG.info("UIcomponent, context = " + context.toString());
  //  LOG.info("UIcomponent,message = " + toValidate.getClientId(context)); 
    String confirm = (String)value;
    

if ((context == null) || (toValidate == null)) 
{ 
     LOG.info("UIcomponent,null context or toValidate = "); 
    throw new NullPointerException(); 
} 
if (!(toValidate instanceof UIInput))
    { LOG.info("UIcomponent,not instanceof UIInput = "); 
    return;
}
    String field1Id = (String) toValidate.getAttributes().get("scorePlayer11");
         LOG.info(" validateMP4 - field1Id = " + field1Id);
         
 //   UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
 //        LOG.info(" validateMP4 - passComponent = " + passComponent);
 //   String pass = (String) passComponent.getSubmittedValue();
 //       LOG.info(" validateMP4 - pass1 = " + pass);
       String  pass = null;
    if (pass == null)
    {
 //       pass = (String) passComponent.getValue();
         LOG.info(" validateMP4 - pass2 = " + pass);
    }
    
    if (!pass.equals(confirm))
    {
        LOG.info(" validateMP4 - pass not equal confirm = " );
        String msg = toValidate.getClientId(context);
        showMessageFatal(msg);
  //      String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
        throw new ValidatorException(new FacesMessage(msg));
    }
 }  //end try ValidatorException
 catch (ValidatorException ve)
     {
            String msg = "ValidatorException = " + ve.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
         } 
 catch (NullPointerException npe)
     {
            String msg = "NullPointerException = " + npe.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
         } 
}

    public TarifGreenfee getTarifGreenfee() {
        return tarifGreenfee;
    }

    public void setTarifGreenfee(TarifGreenfee tarifGreenfee) {
        this.tarifGreenfee = tarifGreenfee;
    }

     public String getParArrayString() throws SQLException
     {
         load.LoadParArray lpa = new load.LoadParArray();
         parArray =  lpa.LoadParArray(conn, player, course);  //mod 31/07/2014
 //           LOG.info("parArray  = " + parArray);
     parArrayString = Arrays.toString(parArray); // it will return String like [1, 2, 3, 4, 5]
 //    LOG.info("parArrayString = " + parArrayString);
        String strSeparator = "|";
        // //you can use replaceAll method to replace brackets and commas
        parArrayString = parArrayString.replaceAll(", ", strSeparator).replace("[", "").replace("]", "");
//          LOG.info("String strNumbers2 = " + parArrayString);
        
//  lance exÃƒÆ’Ã‚Â©cution d'une fonction Javascript via Primefaces  context.execute("PrimeFaces.info('Hello from the Backing Bean');");
////        RequestContext c = RequestContext.getCurrentInstance();
////         c.execute("PrimeFaces.info('Hello from CourseController');");
////         c.execute("alert('msg from getParArrayString');");
////         c.execute("return confirm('from CourseController : \\nPress a button!\\nEither OK or Cancel');");
        return parArrayString;
    }

    public Map<String, String> getMembers() {
            LOG.info("getMembers returned = " + members);
        return members;
    }

    public void setMembers(Map<String, String> members) {
        this.members = members;
    }

 //   public boolean isNextPanelPassword() {
 //       return NextPanelPassword;
 //   }

 //   public void setNextPanelPassword(boolean NextPanelPassword) {
 //       this.NextPanelPassword = NextPanelPassword;
 //   }

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

    public PlayerHasRound getInscription() {
        return inscription;
    }

    public void setInscription(PlayerHasRound inscription) {
        this.inscription = inscription;
    }

    public Inscription getInscriptionNew() {
        return inscriptionNew;
    }

    public void setInscriptionNew(Inscription inscriptionNew) {
        this.inscriptionNew = inscriptionNew;
    }

    public  Flight getFlight() {
        return flight;
    }

      public void selectFlightFromDialog(Flight flight) throws IOException{
      LOG.info("entering selectflightfromdialog");
      LOG.info("entering selectflightfromdialog with source = " + flight.toString() );
      this.flight = flight;
     
        LOG.info("after setflight, flight = " + flight.getFlightStart() );
//           LOG.info("after setflight, flight HHMM = " + Constants.dtf_HHmm.format(flight.getFlightStart()) );
           LOG.info("after setflight, flight HHMM = " + flight.getFlightHourStart()) ;
  //   round.setWorkHour(Constants.dtf_HHmm.format(flight.getFlightStart() ));
     round.setWorkHour(flight.getFlightHourStart());  // mod 22-10-2018
     round.setRoundDate(flight.getFlightStart());
        LOG.info("getRoundDate = " + round.getRoundDate());
        LOG.info("getWorkHour = " + round.getWorkHour());
        LOG.info("other method getFlightHourStart = " + flight.getFlightHourStart());
     DialogController dc = new DialogController();
     dc.closeDialog("dialogFlight.xhtml");
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

    private SelectItem[] createFilterOptions(String[] data)
    {
        SelectItem[] options = new SelectItem[data.length + 1];
        options[0] = new SelectItem("", "Select All");
        for(int i = 0; i < data.length; i++)
        {
            options[i + 1] = new SelectItem(data[i], data[i]);
        }
     return options;
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
        LOG.info(" passing thru getSeasonOptions");
        return seasonOptions;
    }

    public void setSeasonOptions(SelectItem[] seasonOptions)
    {   LOG.info(" passing thru SetSeasonOptions");
        this.seasonOptions = seasonOptions;
    }

    public List<SelectItem> getLanguages() {
        return LANGUAGES;
    }

    public String getHtmlBr() {
        return htmlBr;
    }

    public String getHtmlH1() {
        return htmlH1;
    }

    public String getInputClub() {
        LOG.info("getInput = " + inputClub);
        return inputClub;
    }

    public String getInputParticipants() {
        return inputParticipants;
    }

    public void setInputParticipants(String inputParticipants)
    {
        this.inputParticipants = inputParticipants;
        LOG.info("setInputParticipants (new round !) = " + inputParticipants);
        if (inputParticipants.equals("ini") )
        {  LOG.info(" -- Coursecontroller/listcourse, filteredCars  set to null ! ");
            lists.PlayersList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
        }
    }
    
    public String getInputInscription() {
        return inputInscription;
    }

    public void setInputInscription(String inputInscription) {
        this.inputInscription = inputInscription;
        LOG.info("setInputInscription (new round !) = " + inputInscription);
        if (inputInscription.equals("ini") )
        {  LOG.info(" -- Coursecontroller/listInscription, filteredCars  set to null ! ");
            lists.InscriptionList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
        }
    } // end method

    public String getInputHandicap() {
        return inputHandicap;
    }

    public void setInputHandicap(String inputHandicap) {
        this.inputHandicap = inputHandicap;
        LOG.info("setInputHandicap (new round !) = " + inputHandicap);
        if (inputHandicap.equals("ini") )
        {  LOG.info(" -- Coursecontroller/listhandicap, filteredCars  set to null ! ");
            lists.HandicapList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
        }
    }

    public void initInputPlayingHcp(int inputPlayingHcp) // new 2014-06-15
    {
        this.inputPlayingHcp = inputPlayingHcp;
        LOG.info("setInputPlayingHcp (new calcul !) = " + inputPlayingHcp);
   //     if (inputPlayingHcp.equals("ini") )
   //     {  
        entite.PlayingHcp ph = new entite.PlayingHcp();
            ph.setPlayingHandicap(this.inputPlayingHcp);  // reset to zero
            ph.initElem();
            LOG.info(" -- Coursecontroller/Playinghandicap set to null ! ");
   //     }
    }    
    
    
    public String getInputPlayedRounds() {
        return inputPlayedRounds;
    }
// enlevé 29-11-2018 encore utilisé ?
    public void setInputPlayedRounds(String inputPlayedRounds)    {
        this.inputPlayedRounds = inputPlayedRounds;
            LOG.info("setInput (new played list) = " + inputPlayedRounds);
        if (inputPlayedRounds.equals("ini"))
        {
                LOG.info(" -- Listcontroller/listround, filteredCars  set to null ! ");
//            lists.RoundList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
          //  gamesOptions = null; // new 15/12/2013
//                LOG.info(" -- Listcontroller/listround = " + lists.RoundList.getListe() );
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

    public void setInputClub(String inputClub)
    {
        LOG.info("setInput (new club !) = " + inputClub);
        this.inputClub = inputClub;
        if (inputClub.equals("ini")) {
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
        LOG.info("setInput (new course !) = " + inputCourse);
        this.inputCourse = inputCourse;
        if (inputCourse.equals("ini")) {
    //        club = new Club();
            //        setNextCourse(false);
            course = new Course();
            tee = new Tee();
            hole = new Hole();
        }
    }

    public String getInputScorecard() {
        return inputScorecard;
    }

    public void setInputScorecard(String inputScorecard)
    {
        LOG.info("setInput (new scorecard !) = " + inputScorecard);
        this.inputScorecard = inputScorecard;
        if (inputScorecard.equals("ini")) {
            LOG.info(" -- Course Controller : Listcontroller/listcc2 set to null = ");
//            lists.ScoreCard2List.setListe(null);  //lazy loading !!
            lists.ScoreCard3List.setListe(null);  //lazy loading !!
        }
    }

    public String getInputScore() {
        return inputScore;
    }
    public String getInputTeeStart() {
        return inputScore;
    }
    
    public String getLastSession() throws SQLException {
        LOG.info("before last session");
        
        find.FindLastLogin fll = new find.FindLastLogin();
        return sdf_timeHHmm.format(fll.lastAuditLogin(player, conn));
     //   LOG.info("last login was : " + )
    }

    public int getDeletePlayer() {
        return deletePlayer;
    }

    public void setDeletePlayer(int deletePlayer) {
        this.deletePlayer = deletePlayer;
    }

    public void setStartSession() throws SQLException {
        create.CreateAudit ca = new create.CreateAudit();
        ca.startAuditLogin(player, conn);
  ////      utils.Audit.startAuditLogin(player, conn); // mod 20/12/2014
    }
/*
    public boolean isZwanzeur() {
        LOG.debug(" ...entering isZwanzeur");
        LOG.info("round game equals : " + round.getRoundGame());
        LOG.info("scorecard game equals : " + scorecard.getRoundGame());
        LOG.info("listsc3 game equals : " + ListController.getListsc3().get(0).getRoundGame());
        if (round.getRoundGame().equals("ZWANZEURS")) {
            return true;
        } else {
            return false;
        }
    }
*/
 //   public void setZwanzeur(boolean Zwanzeur) {
 //       this.Zwanzeur = Zwanzeur;
 //   }

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

//    public EScoreCardList getScorecard() {
//        return scorecard;
//    }

    public boolean isShowButtonCreateScore() {
        return ShowButtonCreateScore;
    }

    public void setShowButtonCreateScore(boolean ShowButtonCreateScore) {
        this.ShowButtonCreateScore = ShowButtonCreateScore;
     //   LOG.info("ShowButtonCreateScore setted to : " + ShowButtonCreateScore);
    }

    public boolean isShowButtonCreateStatistics() {
        return ShowButtonCreateStatistics;
    }

    public static void setShowButtonCreateStatistics(boolean showButtonCreateStatistics) {
        ShowButtonCreateStatistics = showButtonCreateStatistics;
        LOG.info("ShowButtonCreateStatistics setted to : " + ShowButtonCreateStatistics);
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
        LOG.info("otherGame setted to : " + CourseController.otherGame);
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

    public void setFilteredCourses(List<?> filteredCourses) {
        this.filteredCourses = filteredCourses;
    }

    public List<?> getFilteredPlayedRounds() {
        return filteredPlayedRounds;
    }

    public void setFilteredPlayedRounds(List <?>filteredPlayedRounds) {
        this.filteredPlayedRounds = filteredPlayedRounds;
    }

    public List<?> getFilteredCars() {
     //   LOG.info("from getFilteredCars = " + filteredCars);
        return filteredCars;
    }



    public List<?> getFilteredPlayers() {
        return filteredPlayers;
    }

    public void setFilteredPlayers(List <?>filteredPlayers) {
        this.filteredPlayers = filteredPlayers;
    }

 //   public List<?> getFilteredHandicaps() {
 //       return filteredHandicaps;
 //   }

//    public void setFilteredHandicaps(List <?>filteredHandicaps) {
 //       this.filteredHandicaps = filteredHandicaps;
 //   }

 //   public ECourseList getSelectedHandicap() {
 //       LOG.info("getSelectedHandicap = " + selectedHandicap);
 //       return selectedHandicap;
 //   }

//    public void setSelectedHandicap(ECourseList selectedHandicap) {
        
  //      this.selectedHandicap = selectedHandicap;
   //      LOG.info("setSelectedHandicap = " + selectedHandicap);
   // }

    public String getInputcmdParticipants() {
        return inputcmdParticipants;
    }

    public void setInputcmdParticipants(String inputcmdParticipants) {
        this.inputcmdParticipants = inputcmdParticipants;
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
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
        LOG.info("getSelectedPlayersMatchPlay = " + selectedPlayersMatchPlay );
        if(selectedPlayersMatchPlay != null)
        {
            String msg = "#Players in Match Play = " + String.valueOf(selectedPlayersMatchPlay.size());
        }
        return selectedPlayersMatchPlay;
    }

    public void setSelectedPlayersMatchPlay(List<Player> selectedPlayersMatchPlay) {
        LOG.info("setSelectedPlayersMatchPlay = " +  Arrays.toString(selectedPlayersMatchPlay.toArray()));
   //      LOG.info("listsc3 after while = " + Arrays.toString(listsc3.toArray() ) );
        this.selectedPlayersMatchPlay = selectedPlayersMatchPlay;
        
    }



    public ECourseList getSelectedPlayedRound() {
        return selectedPlayedRound;
    }

    public void setSelectedPlayedRound(ECourseList selectedPlayedRound) {
        this.selectedPlayedRound = selectedPlayedRound;
    }

    public void setFilteredCars(List<?> filteredCars) {
        LOG.info("from setFilteredCars = " + filteredCars);
        this.filteredCars = filteredCars;
    }

    public Old_Matchplay getMatchplay() {
        return matchplay;
    }

    public void setMatchplay(Old_Matchplay matchplay) {
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

    public PlayingHcp getPlayingHcp() {
        return playingHcp;
    }

    public void setPlayingHcp(PlayingHcp playingHcp) {
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

    public Course getCourse2() {
        return course2;
    }

    public void setCourse2(Course course2) {
        this.course2 = course2;
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

    public ScoreStableford getScoreStableford() {
        return scoreStableford;
    }

    public void setScoreStableford(ScoreStableford score) {
        this.scoreStableford = score;
    }

    public ScoreMatchplay getScoreMatchplay() {
        return scoreMatchplay;
    }

    public void setScoreMatchplay(ScoreMatchplay scoreMatchplay) {
        this.scoreMatchplay = scoreMatchplay;
    }
   public Overlay getOverlay() {
        return overlay;
    }

    public void setOverlay(Overlay overlay) {
        this.overlay = overlay;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
    
 public MapModel getMapModel()
    {
        LOG.info("entering getMapModel ...");
        
     if(mapModel == null)
     {    
       if(club.getIdclub() == null){
            LOG.info("clubIdclub == null");
            return mapModel;}
         LOG.info("getMapModel is null : we fill it !");
         LOG.info("club = "+ club.toString());
         LOG.info("course = "+ course.toString());
         LOG.info("course.getIdcourse = " + (course.getIdcourse() != null ? "true : id course not null" : "false : idcourse is null")); 
 
        mapModel = new DefaultMapModel();  
       org.primefaces.model.map.LatLng latlng = new org.primefaces.model.map.LatLng(club.getClubLatitude().doubleValue(), club.getClubLongitude().doubleValue() );
       Marker marker = new Marker(latlng, "Golf club : " + club.getClubName() );  // affiche nom du club dans marker
 //      marker.setTitle("This is the marker title");
       mapModel.addOverlay(marker);
       
       Circle circle = new Circle(latlng, 200);
       circle.setFillColor("green");
       circle.setFillOpacity(0.5);
       circle.setStrokeColor("#00ff00");
       circle.setStrokeOpacity(0.7);
       mapModel.addOverlay(circle);
       LOG.info("mapModel OK");
        return mapModel;
     }else{
        LOG.debug("escaped to mapModel repetition thanks to lazy loading");
        return mapModel;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
}
    } // end method getMapModel


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

public CartesianChartModel LineModel() throws SQLException { // new solution 03/08/2014, Chart API
        LOG.info("starting new linearModel");
        charts.ChartsLineModel clm = new charts.ChartsLineModel();
        return clm.createLineModel(conn, player, course, round);
      //  return linearModel;
    }

public static String fileUpload() throws SQLException
{
  //  FileUploadController.setIdplayer(Integer.toString(player.getIdplayer()) );
  //  FileUploadController.setPlayerFirstName(player.getPlayerFirstName() );
  //  FileUploadController.setPlayerLastName(player.getPlayerLastName());
        
     return "player_file.xhtml?faces-redirect=true";
}

// http://adfpractice-fedor.blogspot.be/2012/02/understanding-immediate-attribute.html
// http://balusc.omnifaces.org/2006/09/debug-jsf-lifecycle.html
 // new 25/03/2017
/*
public String findHomeClub() // n'est plus utilisé dans player.xhtml
{       LOG.info("entering findHomeClub " );
   try{     
       // explication !! on profite du button "HomeClub" pour chercher les coordinates du player = chipotage !!!
 //      player = find.FindPlayerCoordinates.findPlayerLatLngTz(player);
     //   return "club.xhtml?faces-redirect=true";
       return "selectHomeClub.xhtml?faces-redirect=true";
  
     }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findHomeClub = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
}
*/
public String findClubLatLng() //used in player.xhtml
{       LOG.info("entering findClubLatLng " );
   try{     
       find.FindClubCoordinates fcc = new find.FindClubCoordinates();
       club = fcc.findClubLatLngTz(club);
       if(club == null){
           LOG.info("club courseC is null");
           club.setClubAddress("?? error => retry");
       }
       
         return "club.xhtml?faces-redirect=true";
    
     }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubLatLng " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
}

public String  findClubWebsite() //used in player.xhtml
{       LOG.info("entering findWebsite " );
   try{ 
            LOG.info("club Website = " + club.getClubWebsite() );  // a été complété par clubWebsiteListener, 
            if(club.getClubWebsite() == null)
            {
                club.setClubWebsite("Website cannot  be null");
                return("club.xhtml?faces-redirect=true");
            }
            String red = "http://" + club.getClubWebsite();
              LOG.info("redirecting to  = " + red);
  //Redirect a request to the specified URL, and cause the responseComplete()
  // method to be called on the FacesContext instance for the current request.
            FacesContext.getCurrentInstance().getExternalContext().redirect(red); // faut redirect car exertnal site
            return null;
   }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubWebsite = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
} // en method

public String processAction(){
   //  http://memorynotfound.com/jsf-how-to-find-component-programatically/
        UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
        UIComponent component = view.findComponent("uf:name"); // form:field
        component.getAttributes().put("value", "John");
        return null;
    }


public void playerLanguageListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("playerLanguage OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("playerLanguage NewValue = " + valueChangeEvent.getNewValue());
 //       String newLanguage = (String) valueChangeEvent.getNewValue();
    player.setPlayerLanguage(valueChangeEvent.getNewValue().toString() );
}

public void playerPasswordListener(ValueChangeEvent valueChangeEvent) {
   //     LOG.info("playerCountryListener ");
        LOG.info("playerPassword OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("playerPassword NewValue = " + valueChangeEvent.getNewValue());
    player.setPlayerPassword(valueChangeEvent.getNewValue().toString() );
}



public void playerCountryListener(ValueChangeEvent valueChangeEvent) {
   //     LOG.info("playerCountryListener ");
        LOG.info("playerCountry OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("playerCountry NewValue = " + valueChangeEvent.getNewValue());
    player.setPlayerCountry(valueChangeEvent.getNewValue().toString() );
}



public void playerCityListener(ValueChangeEvent valueChangeEvent) {
   //     LOG.info("playerCityListener ");
     UIComponent c = valueChangeEvent.getComponent();
     LOG.info("component client Id=  " + c.getClientId());
     UIInput input = (UIInput) valueChangeEvent.getComponent();
      //  LOG.info("playerCity UIInput =  " + input);
      //  PhaseId phaseId = valueChangeEvent.getPhaseId();
      //  LOG.info("playerCity phaseId =  " + phaseId);
    utils.LCUtil.printCurrentPhaseID();
 //   Object source = valueChangeEvent.getSource();
  //  LOG.info("playerCitysource =  " + source);
        LOG.info("playerCity OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("playerCity NewValue = " + valueChangeEvent.getNewValue());
    player.setPlayerCity(valueChangeEvent.getNewValue().toString() );
    
    // new 09-08-2018 à ce moment on a le country et la city !!
     find.FindPlayerCoordinates fpc = new find.FindPlayerCoordinates();
     player = fpc.findPlayerLatLngTz(player);
}
public void clubWebsiteListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("clubWebsite OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("clubWebsite NewValue Website = " + valueChangeEvent.getNewValue());
        club.setClubWebsite(valueChangeEvent.getNewValue().toString() );
}
public void clubCountryListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("clubCountry OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("clubCountry NewValue = " + valueChangeEvent.getNewValue());
    club.setClubCountry(valueChangeEvent.getNewValue().toString() );
}
public void clubNameListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("clubName OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("clubName NewValue = " + valueChangeEvent.getNewValue());
    club.setClubName(valueChangeEvent.getNewValue().toString() );
}
public void clubCityListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("clubCity OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("clubCity NewValue = " + valueChangeEvent.getNewValue());
    club.setClubCity(valueChangeEvent.getNewValue().toString() );
}
public void clubAddressListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("clubAddress OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("clubAddress NewValue = " + valueChangeEvent.getNewValue());
    club.setClubAddress(valueChangeEvent.getNewValue().toString() );
}

public void creditCardNumberListener(ValueChangeEvent valueChangeEvent) {
//        LOG.info("creditcardNumber OldValue = " + valueChangeEvent.getOldValue());
//        LOG.info("creditcardNumber NewValue = " + valueChangeEvent.getNewValue());
    creditcard.setCreditCardNumber(valueChangeEvent.getNewValue().toString() );
}

public void creditCardTypeListener(ValueChangeEvent valueChangeEvent) {
 //       LOG.info("creditcardType OldValue = " + valueChangeEvent.getOldValue());
 //       LOG.info("creditcardType NewValue = " + valueChangeEvent.getNewValue());
    creditcard.setCreditCardType(valueChangeEvent.getNewValue().toString() );
    if( !creditcard.getCreditCardIssuer().equals(creditcard.getCreditCardType())){   // issuer = calculated  cardType = input data
        String msg = "WARNING !!! "
                + " <br/> Issuer detected = " + creditcard.getCreditCardIssuer()
                + " <br/> Card Type data in = " + creditcard.getCreditCardType();

        LOG.info(msg);
        showMessageInfo(msg);
    }
}
public void roundWorkDate(ValueChangeEvent valueChangeEvent) throws ParseException {
        LOG.info("roundWorkDate");
 //   utils.LCUtil.printCurrentPhaseID();
        LOG.info("roundWorkDate OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("roundWorkDate NewValue Website = " + valueChangeEvent.getNewValue());
    String s = SDF.format(valueChangeEvent.getNewValue());
    LOG.info("line 001");
    LOG.info("s == " + s);
    round.setWorkDate(SDF.parse(SDF.format(valueChangeEvent.getNewValue())));
       LOG.info("line 002");
 //   round.setWorkDate(SDF.parse(s));
 //   round.setWorkDate(SDF.parse(s));
        LOG.info("roundworkdate  =  " + round.getWorkDate());
}
   public String otherPlayers() throws SQLException, Exception {  //mod 30-11-2018
        int int1 = 0;
  try{
       LOG.info("starting otherPlayers ");
        LOG.info("there are ?? new inscriptions = " + player.getSelectedOtherPlayers().size());
        LOG.info("here are the selectedOtherPlayers  = " + player.getSelectedOtherPlayers().toString());
   //      LOG.info("déjà inscrits à ce round 1 = " + round.getPlayers().size());
        LOG.info("déjà inscrits à ce round = " + round.getRoundPlayers());
        int1 = player.getSelectedOtherPlayers().size() + round.getRoundPlayers();
        if(int1 > 4){
            throw new Exception();
        }
      return "inscriptions_other_players.xhtml?faces-redirect=true";
   }catch(Exception e){
      if (int1 > 4) {
         String msg =  LCUtil.prepareMessageBean("inscription.toomuchplayers");
         throw new LCCustomException(msg + int1, e);
      }else{
            String msg = "££ Exception in otherPlayers = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
          }     
        
    } // end catch
   } // end method
   
   public String createOtherPlayers() throws SQLException {  //new 03/09/2014, mod 30/06/2017 used in pickListPlayers not operational
        LOG.info("starting createOtherPlayers = ");
  //      LOG.info("List for players = " + playersTarget.toString());
        LOG.info("dlPlayers getSource 2 = " + dlPlayers.getSource().toString()); // colonne de gauche
        LOG.info("dlPlayers getTarget 2 = " + dlPlayers.getTarget().toString()); // colonne de droite
        LOG.info("there are ?? new inscriptions = " + dlPlayers.getTarget().size()); // joueurs sélectionnés
        // à faire = vérifier que 3 joueurs maximum !
        
        for(int i=0; i < dlPlayers.getTarget().size() ; i++)
        {
            LOG.debug("line 01");
     //       LOG.debug(" -- item in for idplayer # = " + dlPlayers.getTarget().get(i).getIdplayer() );
        }
        LOG.info("fulllist = " + fullList.toString());
        LOG.info("for round = " + round.toString());
        LOG.info("for player_has_round = " + inscription.toString());
 //       List<Player> sortedPlayers = Collections.sort(selectedPlayersMatchPlay); // new 04/09/2014
   //     dlPlayers = new DualListModel<>(selectedPlayersMatchPlay, playersTarget);
        LOG.info("after createOtherPlayers");
    //    boucler sur createInscription 
        
        
        return "inscription.xhtml?faces-redirect=true";
    }
   
    public int getInputPlayingHcp() {
        return inputPlayingHcp;
    }

    public void setInputPlayingHcp(int inputPlayingHcp) {
        this.inputPlayingHcp = inputPlayingHcp;
    }

    public void uploadListener(FileUploadEvent e) throws Exception{    
         LOG.info(" entering uploadListener = ");
         FileUploadController.uploadListener(e, player, conn);
         LOG.info(" after FileUploadController");
      // forcer refresh de listplayers !!
       lists.PlayersList.setListe(null);
       lists.PlayersList pl = new lists.PlayersList();
       pl.getListAllPlayers(conn);
  //     return "welcome.xhtml?faces-redirect=true"; retourne quand même player_file.xhtml
    }
    
    public String selectHomeClub(ECourseList ecl) throws IOException { 
        LOG.info(" selected Home Club, ecl = " + ecl.toString());
   LOG.info(" player is at this moment = " + player.toString() );
         
         club = ecl.getClub();  // mod 25-11-2018
         player.setPlayerHomeClub(club.getIdclub()); // new 04/02/2019
   //         LOG.info("setted ClubName as Home Club = " + club.getClubName() + " / " + club.getIdclub());
    //     player.setPlayerHomeClub(club.getIdclub());
    //     LOG.info("setted player = " + player.toString());
            LOG.info("setted HomeClub = " + player.getPlayerHomeClub());

        String msg = "Select Club Successfull = "
                + " <br/> Club name = " + club.getClubName() // + " <br/> Course name = " + course.getCourseName()
                ;
        LOG.info(msg);
        showMessageInfo(msg);
 
        
        DialogController dc = new DialogController();
        dc.closeDialog("dialogHomeClub.xhtml");
        
        if(createModifyPlayer.equals("M"))
        {   LOG.info("This is a modification player = " + createModifyPlayer);
            return "player_modify.xhtml?faces-redirect=true";
        }else{
            LOG.info("This is a creation player = " + createModifyPlayer);
            return "player.xhtml?faces-redirect=true";} 
    //    return null; // mod 06-04-2017 aussi player-modify est possible
    } // end selectClub
    
public String to_selectCourse_xhtml(String s){
            LOG.info("entering to_selectCourse_xhtml ... with string = " + s);
       reset("Reset to_selectCourse " + s);
       setFilteredCourses(null);
  //      LOG.info("s is now = " + s);
       course2.setInputSelectCourse(s); // field de transition restored to course after created from ecl voir selectCourse
            LOG.info(" course selected for : " + course2.getInputSelectCourse());
       return "selectCourse.xhtml?faces-redirect=true&cmd=" + course2.getInputSelectCourse();
   }

 public String selectCourse(ECourseList ecl){ 
  try {
            LOG.info(" entering select Course(ECourseList) ... = ");
            LOG.info(" ecl = " + ecl.toString());
        club = ecl.getClub();
            LOG.info("club = " + club.toString());
        course = ecl.getCourse(); // on le perd ici ? je crois que oui !!!
            LOG.info("course = " + course.toString());
            LOG.info("course2 = " + course2.toString());
        
        course.setInputSelectCourse(course2.getInputSelectCourse() ); // new 24-11-2018 superimportant !!
        
        String msg = "Select Course Successfull l 1731 = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName()
                + " <br/> inputSelectCourse = " + course.getInputSelectCourse();
        LOG.info(msg);
   //     showMessageInfo(msg); // enlevé 17-11-2018
        LOG.info("selectCourse, inputSelectCourse = " + course.getInputSelectCourse());
         if(course.getInputSelectCourse()== null){
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                showMessageFatal(msg);
         }
         LOG.info("selectCourse, inputSelectCourse !! = " + course.getInputSelectCourse());
         
        if (course.getInputSelectCourse().equals("CreateRound")) {
            return "round.xhtml?faces-redirect=true&cmd=round";} // mod 30/07/2014
        
        if (course.getInputSelectCourse().equals("ini")) {
            return "round.xhtml?faces-redirect=true&cmd=ini";} // mod 30/07/2014
        
        if (course.getInputSelectCourse().equals("CreateTarif")) {
            return "tarif_menu.xhtml?faces-redirect=true";} // mod 16-09-2018
        
        if (course.getInputSelectCourse().equals("CreateTarifMember")) {
   //         tarifMember = new TarifMember();
            return "tarif_members.xhtml?faces-redirect=true";} // new 05-01-2019
       
        if (course.getInputSelectCourse().equals("CreateUnavailable")) {
            return "unavailable.xhtml?faces-redirect=true";} // new 05-01-2019
        
        
        
        if (course.getInputSelectCourse().equals("PaymentTarifMember")) {
            tarifMember = new FindTarifMembersData().findTarif(club, conn);
            if(tarifMember == null){
                String msgerr =  LCUtil.prepareMessageBean("tarif.member.notfound");
                LOG.error(msgerr); 
                LCUtil.showMessageFatal(msgerr);
                return null;
            }else{
                 return "cotisation.xhtml?faces-redirect=true";} // new 05-01-2019
            }
           
        
        if (course.getInputSelectCourse().equals("ChartCourse")) {
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

public String selectTravel(ECourseList ecl){ 
 try {
        LOG.info(" entering select Travel ...");
            LOG.info(" select Travel with Ecourselist = " + ecl.toString());
            
            club = ecl.Eclub; // mod 09-12-2018
            course = ecl.Ecourse;

        String msg = "Select Course Successfull l 1796 = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
        LOG.info(msg);
        showMessageInfo(msg);
        // à vérifier 
          LOG.info("getInputSelectCourse = " + course.getInputSelectCourse() ); // à vérifer
//        if (getInputSelectCourse().equals("ChartCourse")) {
            return "maps_home_club.xhtml?faces-redirect=true"; //?cmd=Rnd"; // mod 30/07/2014

    } catch (Exception e) {
            String msg = "££ Exception in selectTravel = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
 } // end class selectTravel

  public String selectChart(ECourseList ecl){ //, String cmd)
  try {
            LOG.info("starting selectChart !! = " );
            LOG.info("starting selectChart with ecl = " + ecl.toString());
         club = ecl.Eclub;
         course = ecl.Ecourse;

        String msg = "Select Course Successfull = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
            LOG.info(msg);
            showMessageInfo(msg);
        // à vérifier 
          LOG.info("getInputSelectCourse = " + course.getInputSelectCourse() ); // à vérifer
//        if (getInputSelectCourse().equals("ChartCourse")) {
         return "statChartCourse.xhtml?faces-redirect=true"; 
//        } else {
//            String s = "statChartRound.xhtml?faces-redirect=true";
//            LOG.info("return = " + s);
//            return s;
//        }
    } catch (Exception e) {
            String msg = "££ Exception in selectChart = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    }
} // end class selectTravel

public BarChartModel CourseBarModel() throws SQLException
 { try {
            LOG.info("starting CourseBarModel");
            LOG.info("idplayer = " + player.getIdplayer() );
            LOG.info("idcourse = " + course.getIdcourse() );
            LOG.info("idround = " + round.getIdround() );
            LOG.info("continuing BarModel");
            LOG.info("continuing CourseBarModel with getInputSelectCourse = " + course.getInputSelectCourse());
            LOG.info("continuing CourseBarModel with getInputPlayedRounds = " + getInputPlayedRounds());
            charts.ChartsBarModel cm = new charts.ChartsBarModel();
          return cm.getBarModel(conn, player, course, round, "course"); // round is not used
   }catch (SQLException e){
            String msg = "CourseController - getBarModel : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } 
} // end method CourseBarModel
  
public LineChartModel HandicapModel() throws SQLException
 { try {
            LOG.info("starting HandicapModel");
            LOG.info("idplayer = " + player.getIdplayer() );
            LOG.info("continuing HandicapModel");
            charts.HandicapModel hm = new charts.HandicapModel();
           return hm.getHandicapModel(conn, player); 
   }catch (SQLException e){
            String msg = "CourseController - HandicapModel : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } 
} // end method HandicapModel

  public BarChartModel RoundBarModel() throws SQLException
 { try        {
            LOG.info("starting RoundBarModel");
            LOG.info("idplayer = " + player.getIdplayer() );
            LOG.info("idcourse = " + course.getIdcourse() );
            LOG.info("idround = " + round.getIdround() );
           LOG.info("continuing BarModel");
           charts.ChartsBarModel cm = new charts.ChartsBarModel();
          return cm.getBarModel(conn, player, course, round, "round"); // round is not used
        } catch (SQLException e) {
            String msg = "CourseController - RoundBarModel : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } 
} // end method RoundBarModel

public void checkMail(String ini)
{
        LOG.info("starting checkMail with : " + ini);
///    utils.CheckingMails.main(ini); // argument bidon !!
        LOG.info("ending checkMail with : " + ini);
}
public void newMessageFatal(String ini)
{
        LOG.info("starting newMessageFatal with : " + ini);
///    utils.CheckingMails.main(ini); // argument bidon !!
        LOG.info(ini);
     //   utils.LCUtil lcu = new utils.LCUtil();
 //       utils.LCUtil.showMessageFatalOld(ini);
        LOG.info("ending newMessageFatal with : " + ini);
}


// new 14/08/2014, called from menu
public String reset(String ini){
  try{
        LOG.info("starting reset with : " + ini);
    charts.ChartsBarModel.setBarModel(null);
    charts.ChartsLineModel.setLineModel(null);
    charts.HandicapModel.setHandicapModel(null);
    create.CreateAllFlights.setListe(null);  // new 22/04/2017
    lists.HandicapList.setListe(null);
    lists.InscriptionList.setListe(null);
    lists.PlayersList.setListe(null);
    lists.ParticipantsStableford.setListe(null); // new 29/06/2016
    lists.MatchplayList.setListe(null);
    lists.PlayedList.setListe(null);
    lists.PlayersList.setListe(null);
    lists.RecentList.setListe(null);
///    lists.RoundList.setListe(null);
    lists.ScoreCard1List.setListe(null);
    lists.CourseList.setListe(null); // new 28/07/2017
    lists.ScoreCard3List.setListe(null);
//        LOG.info("ending  initialize lists = " + ini);
    find.FindSlopeRating.setListe(null);  // new 22/6/2015
    find.FindSubscription.setListe(null); // new 04/02/2017
    find.FindTeeStart.setListe(null);
    find.SunriseSunset.setListe(null); // new 09/05/2017
    lists.RoundPlayersList.setListe(null); // new 16/06/2017
    lists.SubscriptionRenewalList.setListe(null); // new 25-11-2018
    lists.FlightList.setListe(null);
    lists.ClubDetailList.setListe(null);
 //2. entites
    if(ini.equals("non_belgian"))
        {   LOG.info("starting reset with non_belgian : " + ini);
            player = new Player();
            LOG.info("new player : " + player);
        }
    player.setSelectedOtherPlayers(null);  // new 30-11-2018
    player2 = new Player();
    club = new Club();

    course = new Course();
    handicap = new Handicap();
    hole = new Hole();
    matchplay = new Old_Matchplay();
    // player = no
    inscription = new PlayerHasRound();
    round = new Round();
//    scorecard = new EScoreCardList();
    scoreMatchplay = new ScoreMatchplay();
    scoreStableford = new ScoreStableford();
    scoreScramble = new ScoreScramble(); // new 26/07/2015
    tee = new Tee();
    subscription = new Subscription(); // new 01/02/2017
    cotisation = new Cotisation(); // new 10-01-2019
    playingHcp = new PlayingHcp(); // new 16/06/2017
    tarifGreenfee = new TarifGreenfee(); // new 18/03/2018
    greenfee = new Greenfee();
    tarifMember = new TarifMember(); // new 101-01-2018
    creditcard = new Creditcard();
    creditcard.setPaymentOK(false);
    holesGlobal = new HolesGlobal();
    login = new Login();
    
    cptFlight = 0;  // iterations sur 
 //      LOG.info("ending initialize entites , param = " + ini);
    return "reset OK "; // on retourne d'où on vient
     }catch (Exception ex){
            LOG.error("error in reset ! " + ex);
            showMessageFatal("Exception reset  = " + ex.toString());
            return "error";
        }
}

public void createCourse() throws SQLException // 07/06/2014
{
        LOG.info("start to create course, clubID = " + club.getIdclub() );
    create.CreateCourse cc = new create.CreateCourse();
 //   boolean ok = cc.createCourse(club, course, conn);
    if(cc.createCourse(club, course, conn)){  // ok
        LOG.info("course created, next step = tee");
        tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
    }
}

public void createTee() throws SQLException
{
        LOG.info("Starting createTee !");
    create.CreateTee ct = new create.CreateTee();
 //   boolean ok = ct.createTee(club, course, tee, conn);
    if(ct.createTee(club, course, tee, conn)){
        LOG.info("tee created : we go to hole !!");
       hole.setNextHole(true); // affiche le bouton next(Hole) bas ecran à  droite
    }
}

public void calculateHcpStb() // throws SQLException
{
    LOG.info("Starting Stableford Hcp calculated !");
    double exact_handicap = playingHcp.getHandicapPlayer();
        LOG.info("with handicap player = " + exact_handicap );
    double slope = playingHcp.getTeeSlope();
        LOG.info("with slope = " + slope );
    double rating = playingHcp.getTeeRating();
        LOG.info("with rating = " + rating );
    double par = playingHcp.getCoursePar();
        LOG.info("with par = " + par );
    short teeClubHandicap = 0;       
    //    int hcp = utils.GolfMySQL.calculatePlayingHcp(ph, sl, rt, pa);
    calc.CalcStablefordPlayingHandicap cph = new calc.CalcStablefordPlayingHandicap();
    int hcp = cph.calculatePlayingHcp(exact_handicap, slope, rating, par, teeClubHandicap);
    //    LOG.info("Playing Hcp calculated !! = " + hcp);
    entite.PlayingHcp ph = new entite.PlayingHcp();
    ph.setPlayingHandicap(hcp);
   // PlayingHcp.setPlayingHandicap(hcp); // mod 19-08-2018
            LOG.info("Playing Hcp calculated !! = " + playingHcp.getPlayingHandicap() );
}

public void calculateHcpScramble() throws Exception // calculate Handicap Scramble
{
    
}

public void createHole() throws SQLException
{
    create.CreateHole ch = new create.CreateHole();
    ch.createHole(club, course, tee, hole, STROKEINDEX, conn);

    // ajouter boolean = correct insert !!!
        LOG.info("hole created : we go to hole !!");
     setNextStep(true);  // affiche le bouton next(Step) bas ecran Ã Â  droite}
}

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
        // list avec les stroke index (de 1 Ã Â  18)
        if (NUMBERS.isEmpty()) {   //LOG.debug("values is empty !");
            for (int i = 0; i < max; i++) {
                NUMBERS.add(i + 1);
            }
        }
        LOG.info("holeNumbers returned  =" + NUMBERS.toString());
        return NUMBERS;
    }

    public void processChecked(AjaxBehaviorEvent e) {
        // doing some stuff here
        LOG.info("processChecked !!");
    }
    public void convertYtoM() {  // mod 04-12-2017 
        hole = utils.ConvertYardsToMeters.convertYtoM(hole);
    }
    
  public void createRound() throws SQLException{
       LOG.info("entering createRound ");
    create.CreateRound cr = new create.CreateRound();
  //  boolean ok = cr.createRound(round, course, conn);
    if(cr.createRound(round, course, conn)){
        LOG.info("round created : we go to course !!");
         setNextInscription(true); // affiche le bouton next(Inscription) bas ecran Ã  droite
    }
}

    public List<SelectItem> getGames() {
        return GAMES;
    }
    
  public List<SelectItem> getStart() {
  return START;
    }
    
  
public String createInscriptionOtherPlayers() throws SQLException{
  try{
        LOG.info("entering createInscriptionOtherPlayers");
     create.CreateInscriptionOtherPlayers ciop = new create.CreateInscriptionOtherPlayers();
     boolean ok = ciop.createInscriptions(player, round, inscriptionNew, club, course, conn);
        LOG.info("createInscriptionOtherPlayers result is = " + ok);
     return null; // retourne d'où il vient
   }catch(Exception ex){
    String msg = "creatInscriptionOtherPlayers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }    
}  // end method

 
public String createInscription() throws SQLException{
  try{
        LOG.info("entering createInscription");
    
    Player invitedBy = player;
    // new 09-12-2018 complète invitedBy par le nom du player current
  //  ArrayList<Player> p = new ArrayList<>();   // transform player2 in list<player<    
  //  p.add(player);
  //  invitedBy.setDroppedPlayers(p);
    
 //   create.CreateInscription ci = new create.CreateInscription();
    int ret = new create.CreateInscription().create(round, player, invitedBy, inscriptionNew, club, course, conn); // mod 10/11/2014
    LOG.info("coursec, return from createInscription = " + ret);
    if(ret == 01){
        String err =  LCUtil.prepareMessageBean("inscription.too much players"); // + listPlayers.size() ;
         LOG.error(err); 
        LCUtil.showMessageFatal(err);
        inscription.setInscriptionOK(false); // new 16/7/2016 used in inscription.xhtml
        return "inscription.xhtml?faces-redirect=true";
    }
    if(ret == 02){
        String err = " Vous avez payé un accessoire mais vous n'êtes pas membre de ce club - Voulez-vous payer un greenfee ?"; // + cotisation.getIdclub();
        LOG.info(err);
        LCUtil.showMessageFatal(err);
        inscription.setInscriptionOK(false); // new 16/7/2016 used in inscription.xhtml
        return "inscription.xhtml?faces-redirect=true";
    }
      if(ret == 00){
          String msg =  LCUtil.prepareMessageBean("inscription.ok");
          msg = msg
             + "for round = " + round.getIdround()
             + " <br/> player = " + player.getIdplayer()
             + " <br/> player name = " + player.getPlayerLastName()
             + " <br/> round date = " + round.getRoundDate().format(ZDF_TIME_HHmm)
                    ;
          LOG.info(msg);
          LCUtil.showMessageInfo(msg);
            //     mail.InscriptionMail im = new mail.InscriptionMail();
          boolean b = new mail.InscriptionMail().sendMail(player, invitedBy, round, club, course);
          inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
          return "welcome.xhtml?faces-redirect=true";
      }
  }catch(Exception ex){
    String msg = "CreateInscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}  
return null;  
}   //end method CreateInscription

public String forgetPassword() throws SQLException, Exception{
try{
    LOG.info("entering forgetPassword");
// inset into table Activation
    create.CreateActivationPassword cap = new create.CreateActivationPassword();
    cap.createActivation(conn, player); // y compris envoi du mail
 //   LOG.info("line 02");
  //  LOG.info("line03");
   return null;
  }catch(Exception ex){
    String msg = "forget password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}    

    public String getUuid() { // from password_check.xhtml
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

public String resetPassword() throws SQLException, Exception, Throwable{ 
    // called from password.check.xhtml
    // créer une nouvelle session ?
  try{
       LOG.info("entering resetPassword with uuid = " + uuid);  // à mon avis c'est pas bon ???
    PasswordController pc = new PasswordController();
//    LOG.info("line 01");
    boolean ok = pc.checkPassword(uuid, conn);
    LOG.info("back  to courseC with b = " + ok);
    if(ok){  //true
        String msg = ("checkPassword success");
        LOG.info(msg);
    //    showMessageInfo(msg);
         return "login.xhtml?faces-redirect=true"; //&language=" + language + "&id=" + playerid;
    }else{ // false
        String msg = "Activation record not found : you already had done this work in a recent past ! " ;
        LOG.error(msg);
        showMessageFatal(msg);
     return null;  
    //     return "activation_failure.xhtml?faces-redirect=true"; // mod 03-12-2018
     }
   }catch(Exception ex){
        String msg = "Course controller : resetPassword Exception ! " + ex;
        System.err.print(msg);
        LOG.error(msg);
        System.err.print("system error LC" + ex);
        showMessageFatal(msg);
        return null;
}    
}

public String createPassword() throws SQLException, Exception{
 try{   LOG.info("entering createPassword");
    modify.ModifyPassword mp = new modify.ModifyPassword();
  //  boolean ok = mp.modifypassword(player, conn);
    if(mp.modifypassword(player, conn)){  // true
                  LOG.info("boolean returned from modifyPassword is 'true' ");
                  String msg = "<br> <br> <h1>Password Created 2179 !! ";
                   LOG.info(msg + player.getWrkpassword());
           //       showMessageInfo(msg);
                  player.setWrkpassword("***********");
                  player.setWrkconfirmpassword("***********");
             //     return "welcome.xhtml?faces-redirect=true";
                  return "login.xhtml?faces-redirect=true";
      }else{ 
                  String msg = "boolean returned from modifyPassword is 'false' ";
                  LOG.error(msg);
            //     showMessageFatal(msg);
                  return null;
      } 
   //      setNextScorecard(true); // affiche le bouton carte de score ??
  //  }
 // return null;
  }catch(Exception ex){
    String msg = "icreate password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}// end method    

  public String validateExistingPassword() throws SQLException { // used in modify_password_.xhtml pour afficher 2e panelGrid
try{
        LOG.info("entering validateExistingPassword");
         LOG.info("password Wrk= " + player.getWrkpassword()); 
        LOG.info("password Player = " + player.getPlayerPassword());
    //    find.FindPassword fp = new find.FindPassword();
    //    boolean ok = new find.FindPassword().passwordMatch(player, conn);
    //    LOG.info("from validateExistingPassword, after = " + Boolean.toString(ok).toUpperCase());
        if(new find.FindPassword().passwordMatch(player, conn)){   // is true
                String msg = "existing password correct ! ";
                  LOG.info(msg);
          //        utils.LCUtil.showDialogInfo(msg);
                  player.setNextPanelPassword(true);  //affiche le 2e panelGrid
                  return null;
        }else{
                  String msg = "Existing password not correct ! ";
                  LOG.error(msg);
                  LCUtil.showDialogFatal(msg);
                  return null;
             } 
   }catch(Exception ex){
    String msg = "validate existing password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method
  
public String modifyPassword() throws SQLException, Exception{
 try{
    LOG.info("entering modifyPassword");
    // à développer
     LOG.info("password Wrk= " + player.getWrkpassword());
     LOG.info("password Player = " + player.getPlayerPassword());
 //    find.FindPassword fp = new find.FindPassword();
     boolean ok = new find.FindPassword().passwordMatch(player, conn);
  // tester ici si passwrd match ...
     player.setNextPanelPassword(false);  //affiche pas le 2e panelGrid pour prochaine utlisation dans la même session ?
     LOG.info("password match = " + ok); //msg + player.getWrkpassword());
     
    if(new modify.ModifyPassword().modifypassword(player, conn)){ // true
        LOG.info("boolean returned from modifyPassword is 'true' ");
        player.setWrkpassword("**********");
        player.setWrkconfirmpassword("**********");
            LOG.info("passwordwrk set to  = " + "**********");
        String msg = "<br> <h1>Password Modified 2287 !! ";
        LOG.info(msg + player.getWrkpassword());
     //nextPanelPassword   showMessageInfo(msg);
  ////                find.FindSubscriptionStatus fss = new find.FindSubscriptionStatus();
   ////               return fss.subscriptionStatus(subscription, player, conn); //, subscription);
    }else{
        LOG.info("boolean returned from modifyPassword is 'false' ");
        return null;
    } 
  return null;
  }catch(Exception ex){
    String msg = "modify Password Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

public void Testpayment() throws SQLException{
 try{
        LOG.info("starting payment test");
   //     LOG.info("creditcard = " + creditcard.toString());
    creditcard = new Creditcard();
    creditcard.setTotalPrice(12345.6);
    creditcard.setCommunication("ceci est la communication de test");
    creditcard.setTypePayment("COTISATION");
    creditcard.setPaymentOK(false);
////      boolean OK = CreditcardPayment();
////      LOG.info("coming back with this payment information : " + OK);
   //   LOG.info("At the end The payment is " + creditcard.isPaymentOK());
  }catch(Exception ex){
    String msg = "test payment Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
     //      return null;
}    
}  //end method  

public boolean CreditcardPayment(Subscription subscription) throws SQLException{  // entrypoint for credicard payment
 try{
         LOG.info("starting CreditcardPayment real execution");
         LOG.info("subscription = " + subscription.toString());
      if(subscription.getPrice() == 0){
          LOG.info("amount ZERO -- No payment needed !");
          return false;}
      creditcard = new Creditcard();
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(subscription.getPrice());
      creditcard.setCommunication(subscription.getCommunication());
      creditcard.setTypePayment("SUBSCRIPTION");
         LOG.info("going to creditcard.xhtml");
      FacesContext.getCurrentInstance().getExternalContext().dispatch("creditcard.xhtml");
        LOG.info("after dispatch creditcard.xhtml");
  //    LOG.info("after we have CreditCardHolder = " + creditcard.getCreditCardHolder());
         LOG.info("after creditcard.xhtml we have : " + creditcard.toString());
        //       return "creditcard.xhtml?faces-redirect=true";
      return true;// doit retourner si payment OK !! provisoirement !!
  }catch(Exception ex){
    String msg = "Creditcardpayment Subscription Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}    
}  //end method  

//@Inject
//FacesContext facesContext;
//@Inject
//ExternalContext externalContext;

public boolean CreditcardPayment(Cotisation cotisation) throws SQLException{  // entrypoint for credicard payment
 try{
         LOG.info("starting CreditcardPayment real execution");
         LOG.info("Cotisation = " + cotisation.toString());
      if(cotisation.getPrice() == 0){
          LOG.info("amount ZERO nopayment needed !");
          return false;
      }
      creditcard = new Creditcard();
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(cotisation.getPrice());
      creditcard.setCommunication(cotisation.getCommunication());
      creditcard.setTypePayment("COTISATION");
         LOG.info("going to creditcard.xhtml");
     FacesContext fc = FacesContext.getCurrentInstance();
     ExternalContext eco = fc.getExternalContext();
  //  ec.redirect("creditcard.xhtml"); non
  //https://stackoverflow.com/questions/14163457/externalcontext-dispatch-not-working
  // use normal navigation instead :
///    NavigationHandler nh = fc.getApplication().getNavigationHandler();
////    nh.handleNavigation(fc, null, "creditcard.xhtml?faces-redirect=true");
     Flash flash = eco.getFlash();
     flash.setKeepMessages(true);
//https://stackoverflow.com/questions/15474345/using-externalcontext-dispatch-in-jsf-error-handler-causes-corrupt-page-renderin/     
   //  ViewHandler viewHandler = fc.getApplication().getViewHandler();
   //  UIViewRoot viewRoot = viewHandler.createView(fc, "creditcard.xhtml?faces-redirect=true");
   //  fc.setViewRoot(viewRoot);
  //   fc.renderResponse();
     
     
     eco.dispatch("creditcard.xhtml?faces-redirect=true");
  //    FacesContext.getCurrentInstance().getExternalContext().redirect("creditcard.xhtml");
      LOG.info("responseComplete has been called : " + fc.getResponseComplete());
      LOG.info("after redirect to creditcard.xhtml");
      if(creditcard.getCommunication() == null){
          LOG.info("after creditcard.xhtml we have creditcard = null !");
      }else{
           LOG.info("after creditcard.xhtml we have : " + creditcard.toString());
      }
        //       return "creditcard.xhtml?faces-redirect=true";
      return true;// doit retourner si payment OK !! provisoirement !!
  }catch(Exception ex){
    String msg = "Creditcardpayment Cotisation Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}    
}  //end method  

public boolean CreditcardPayment(Greenfee greenfee) throws SQLException{  // entrypoint for credicard payment
 try{
         LOG.info("starting CreditcardPayment real execution");
         LOG.info("Greenfee = " + greenfee.toString());
      if(greenfee.getPrice() == 0){
          LOG.info("amount ZERO nopayment needed !");
          return false;
      }
      creditcard = new Creditcard();
      creditcard.setPaymentOK(false);
      creditcard.setTotalPrice(greenfee.getPrice());
      creditcard.setCommunication(greenfee.getCommunication());
      //"ceci est la communication de test");
      creditcard.setTypePayment("GREENFEE");
         LOG.info("going to creditcard.xhtml");
     FacesContext fc = FacesContext.getCurrentInstance();
     ExternalContext xc = fc.getExternalContext();
     ExternalContext eco = fc.getExternalContext();
        LOG.info("externalcontextPath = " + eco.getRequestContextPath());
     
     Flash flash = eco.getFlash();
     flash.setKeepMessages(true);
     eco.dispatch("creditcard.xhtml?faces-redirect=true");
  //    FacesContext.getCurrentInstance().getExternalContext().redirect("creditcard.xhtml");
      LOG.info("responseComplete has been called : " + fc.getResponseComplete());
      LOG.info("after redirect to creditcard.xhtml");
      if(creditcard.getCommunication() == null){
          LOG.info("after creditcard.xhtml we have creditcard = null !");
      }else{
           LOG.info("after creditcard.xhtml we have : " + creditcard.toString());
      }
        //       return "creditcard.xhtml?faces-redirect=true";
      return true;// doit retourner si payment OK !! provisoirement !!
  }catch(Exception ex){
    String msg = "Creditcardpayment Greenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
}    
}  //end method  

public String findTarifGreenfee() throws SQLException{  // tarif pour l'inscription (green fee)
 try{
    LOG.info("starting findTarifGreenfee for a round");
    LOG.info("course = " + course.toString());
//    LOG.info("just before player");
    LOG.info("player = " + player.toString());
    tarifGreenfee = new find.FindTarifGreenfeeData().findTarif(course, conn);
 //   boolean c = create.CreateInscription
  //  boolean b = create.CreateInscription.createInscription(round, player, inscription, club, course, conn); // mod 10/11/2014
    if(tarifGreenfee == null){
         LOG.info("Tarif returned from findTarifdata is null ");
         //   return "inscription.xhtml?faces-redirect=true";
         return null;  // donc inscription.xhtml
    }else{
             LOG.info("tarif Greenfee OK");
          double dd = new calc.CalcTarifGreenfee().calc(tarifGreenfee, round, club, player);
             LOG.info("price green fee = " + dd);
             // tester sur la valeur 
          tarifGreenfee.setPriceGreenfee(dd);
          // il s'agit du paiment d'une inscription new 09-10-2018
  //        creditcard.setTypePayment(Creditcard.etypePayment.INSCRIPTION.toString()); // à déplacer !!!
          return "tarif_round.xhtml?faces-redirect=true";
             } 
  }catch(Exception ex){
    String msg = "findTarifGreenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
}  // end method  

public void findWeather() throws SQLException, Exception{
 try{
     // ne fonctionne plus à partir de début janvier 2019 - Yahoo a arreté le service ...
    LOG.info("starting findWeather");
    LOG.info("club = " + club.toString());
    LOG.info("player = " + player.toString());
    LOG.info("round = " + round.toString());
        LOG.info("just before findWeather");
    find.FindWeather fw = new find.FindWeather();
    String weather = fw.currentWeatherByCityName(club);
    if(weather == null){
                  LOG.info("Weather returned from findWeather is null ");
               //   return "inscription.xhtml?faces-redirect=true";
      //            return null;  // donc inscription.xhtml
             } else{
                  LOG.info("weather data is  OK = " + weather);
                  DialogController.showWeather();
             } 
  //   return null;
  }catch(Exception ex){
    String msg = "finWeather Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
}    
}  //end method   

public String selectTarif(ECourseList ecl) throws SQLException {
try{
    LOG.info("entering selectTarif");
           LOG.info("  selectTarif, ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
    return "tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in selectTarif ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} //end method

public String printTarifGreenfee() throws SQLException {
try{
    LOG.info("entering printTarifGreenfee");
    if(tarifGreenfee == null){
        LOG.info("tarif Greenfee= null");
    }
    if(tarifGreenfee.getDatesSeason().length == 0 
     && tarifGreenfee.getDays().length == 0
     && tarifGreenfee.getTeeTimes().length == 0
     && tarifGreenfee.getPriceEquipments().length == 0)
     {
       String msgerr =  LCUtil.prepareMessageBean("tarif.greenfee.empty");
       LOG.error(msgerr);
       showMessageFatal(msgerr);
       throw new Exception("from printTarifGreenfee" +  msgerr);
     }else{
        LOG.info("exiting printTarifGreenfee with " + tarifGreenfee.toString());
        showMessageInfo(tarifGreenfee.toString());
    }
    return "tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in printTarifGreenfee ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} //end method printTarifGreenfee

public String findTarifGreenfee(ECourseList ecl) throws SQLException {
try{
            LOG.info("entering findTarif");
            LOG.info(" findTarif with ecl = " + ecl.toString());
        club = ecl.Eclub; //.getClub();
        course = ecl.Ecourse; //getCourse();
        round = ecl.Eround; //.getRoun; //d();
    //    find.FindTarifGreenfeeData ft = new find.FindTarifGreenfeeData();
        tarifGreenfee = new find.FindTarifGreenfeeData().findTarif(course, conn);
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
    return "tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in findTarif ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} //end method

  public String selectRecentInscription(ECourseList ecl) throws SQLException {
 try{       
           LOG.info("entering selectRecentInscrition");
           LOG.info("  selectRecentInscription, ecl = " + ecl);
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
           LOG.info("on cherche le nombre de joueurs  déjà inscrits et leur nom"); 
    //    lists.RoundPlayersList spl  = new lists.RoundPlayersList();
  // lp = List<Player>      
        lp = new lists.RoundPlayersList().listAllParticipants(round, conn);
           LOG.info("after lists.RoundPlayersList ");
  if(lp != null){
         LOG.info("nombre de players stableford = lp size = " + lp.size());
        inscription.setInscriptionOK(true); // new 12/7/2017 used in inscription.xhtml
        String s = utils.LCUtil.fillRoundPlayersString(lp);
        round.setPlayersString(s);
         LOG.info("c'est fait ! joueurs déjà inscrits = " + round.getPlayersString() );
  }else{
         LOG.info("nombre de players stableford =  zero" );
       round.setPlayersString("no players inscrits");
   }
         inscription.setInscriptionTeeStart(ecl.Etee.getTeeStart());
          LOG.info("TeeStart = " + inscription.getInscriptionTeeStart());
           
        String msg = "Select RecentInscription EcourseList Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
   //     showMessageInfo(msg);
        //String s = Integer.toString(player.getIdplayer() );
        return "inscription.xhtml?faces-redirect=true";
   }catch(Exception ex){
    String msg = "lisrecentinscriptions Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}           
    } // end method
    

public String scoreStableford(ECourseList ecl) throws SQLException {
    try{
             LOG.info(" ... entering ScoreStableford "); 
            LOG.info(" ... entering ScoreStableford  with ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();

   //     load.LoadParArray lpa = new load.LoadParArray();
        parArray = new load.LoadParArray().LoadParArray(conn, player, course);  //mod 15/08/2014
        
   /// UPDATE or INSERT ??     
    //   find.FindCountScore sciup = new find.FindCountScore();
        int rows = new find.FindCountScore().getCountScore(conn, player, round, "rows");
        if (rows != 0){ // UPDATE thus prefilling
                LOG.info("there are : " + rows + " ==> this is a UPDATE, thus we are prefilling the score !");
      //      load.LoadScoreArray lsa = new load.LoadScoreArray();
            int[] int01 = new load.LoadScoreArray().LoadScoreArray(conn, player, round);
                LOG.info("back from loadscorearray with int01 = " + Arrays.toString(int01));
            scoreStableford.setHoles(intArraytoStringArray(int01));
               LOG.info("scoreStableford OK");
      //      load.LoadStatisticsArray lstta = new load.LoadStatisticsArray();
            scoreStableford.setStatistics(new load.LoadStatisticsArray().LoadStatisticsArray(conn, player, round) );
                LOG.info("Score is NOW prefilled !!");
        } // end loading
        String msg = "Select ECourseList Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
        showMessageInfo(msg);
//à mofifier : renvoyer en fonction de Stableford ou Matchplay FOURBALL ou FOURSOME ou SINGLE
        //Str.startsWith("_MP") 
     //   return "score_matchplay.xhtml?faces-redirect=true";
        return "score_stableford.xhtml?faces-redirect=true";
  }catch(Exception ex){
    String msg = "scoreStableford Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
    }
public String inputTarifMembers() throws SQLException, Exception{  // used in tarif_members.xhtml
try{
    LOG.info("entering inputTarifMembers !");
        LOG.info("entering tarif members for club = " + club.toString());
        LOG.info("Members tarif Base = "  + Arrays.deepToString(tarifMember.getMembersBase()));
        LOG.info("Members tarif = " + tarifMember.toString());
 //   String msg = " input Tarif Member = " + tarifMember.getTarifMemberIndex() + " / " 
 //               + Arrays.deepToString(tarifMember.getMembersBase()[tarifMember.getTarifMemberIndex()]);
  //  LOG.info(msg);
  //  showMessageInfo(msg);
  // update array
    int i = tarifMember.getTarifMemberIndex();
    String [][] arr = tarifMember.getMembersBase();
    arr[i][0] = tarifMember.getWorkItem();
    arr[i][1] = tarifMember.getWorkPrice();
    tarifMember.setMembersBase(arr);
    tarifMember.setWorkItem(""); // pour leprochain affichage
    tarifMember.setWorkPrice("");
        LOG.info("Members tarif updated = " + tarifMember.toString());
    String msg = "show TarifMembers = "
            + tarifMember.getMemberStartDate().format(ZDF_TIME_DAY)
            + " / " 
            + tarifMember.getMemberEndDate().format(ZDF_TIME_DAY)
            +  Arrays.deepToString(tarifMember.getMembersBase());
        LOG.info(msg);
        showMessageInfo(msg);
    tarifMember.setTarifMemberIndex(i + 1);
        LOG.info("length = " + tarifMember.getMembersBase().length);
    // vérifier si longueur max est atteint avec lengnt ou size !!
        LOG.info("Tarif Index Members is now :" + tarifMember.getTarifMemberIndex());
    if(tarifMember.getTarifMemberIndex() > tarifMember.getMembersBase().length){  // plus de place dans l'array ??
        LOG.info("maximum items reached ! ");
    }
    // à faire : vérifier si on n'a pas changé les dates de début et fin
        LOG.info("");
    
      return null; // retourne d'où il vient
}catch(Exception ex){
    String msg = "inputTarifMembers Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method

 public String inputTarifSeasons() throws SQLException, Exception{
try{
    LOG.info("entering inputTarifSeasons !");
        LOG.info("entering tarif seasons with course = " + course.toString());
        LOG.info("datesSeason = "  + Arrays.deepToString(tarifGreenfee.getDatesSeason()));
    String msg = " input Period Seasons = " + tarifGreenfee.getTarifIndexSeasons() + " / " 
                                            + Arrays.deepToString(tarifGreenfee.getDatesSeason()[tarifGreenfee.getTarifIndexSeasons()]);
    LOG.info(msg);
    showMessageInfo(msg);
    
    tarifGreenfee.setTarifIndexSeasons(tarifGreenfee.getTarifIndexSeasons() + 1);
    LOG.info("Tarif Index Seasons is now :" + tarifGreenfee.getTarifIndexSeasons());
     return null; // retourne d'ouù il vient
    // à modifier
 //    boolean ok = create.CreateTarif.createTarif(club, conn);
}catch(Exception ex){
    String msg = "inputTarifSeasons Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method
 
 public String createUnavailable() throws SQLException, Exception{
try{
        LOG.info("entering createUnavailable!");
        LOG.info("entering createUnavailable for course = " + course.toString());
      unavailable.setIdcourse(course.getIdcourse());
        String msg = " Indisponibilité to be created = " + unavailable.toString();
        LOG.info(msg);
    //    showMessageInfo(msg);

    if(new create.CreateUnavailable().create(unavailable, conn)){
        msg = "Unavailable is created !";
        LOG.info(msg);
        showMessageInfo(msg);
        return null; // retourne d'où il vient
    }else{
        msg = "Unavailable  is NOT created !";
        LOG.info(msg);
        showMessageFatal("tarif is NOT created");
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
    LOG.info("entering createTarif Members!");
        LOG.info("entering createTarifMembers for club = " + club.toString());
    LOG.info("tarifMember to be created = " + Arrays.deepToString(tarifMember.getMembersBase()));
 //   String info = "input TarifMembers info = " + SDF.format(tarifMember.getMemberStartDate()) + SDF.format(tarifMember.getMemberEndDate()) +
  //            Arrays.deepToString(tarifMember.getMembersBase());
  //  LOG.info(info);
//    LOG.info("line 02");
    tarifMember.RemoveNull(); // remove null from arrays
 //    LOG.info("line 02");
    String msg = " Tarif Member to be created = " + tarifMember.toString();
        LOG.info(msg);
        showMessageInfo(msg);
    tarifMember.setTarifMemberIndex(tarifMember.getTarifMemberIndex() + 1);
  //  create.CreateTarifMember ctm = new create.CreateTarifMember();// à modfier
  //  boolean ok = new create.CreateTarifMember().createTarif(tarifMember, club, conn);
    if(new create.CreateTarifMember().createTarif(tarifMember, club, conn)){
        LOG.info("Tarif is created ? = ");
        showMessageInfo("Tarif is created ");
        return null; // retourne d'où il vient
    }else{
        LOG.info(" tarif is NOT created " );
         showMessageInfo("tarif is NOT created");
        return null;
    }
    
    // à modifier
 //    boolean ok = create.CreateTarif.createTarif(club, conn);
}catch(Exception ex){
    String msg = "Exception in createTarifMembers! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

public String createTarifGreenfee() throws SQLException, Exception{
try{
    LOG.info("entering createTarifGreenfee !");
     //   LOG.info("entering tarif seasons with round = " + round.toString());
        LOG.info("entering createTarifGreenfee with course = " + course.toString());
        LOG.info("datesSeason to be created = " + Arrays.deepToString(tarifGreenfee.getDatesSeason()));
        LOG.info("equipments to be created = " + Arrays.deepToString(tarifGreenfee.getPriceEquipments()));
        LOG.info("days to be created = " + Arrays.deepToString(tarifGreenfee.getDays()));
        LOG.info("tarif index = " + tarifGreenfee.getTarifIndexSeasons());
    tarifGreenfee.RemoveNull(); // remove null from arrays
    String msg = " Tarif to be created = " + tarifGreenfee.toString();
        LOG.info(msg);
    //    showMessageInfo(msg);

    if(new create.CreateTarifGreenfee().createTarif(tarifGreenfee, course, conn)){
        msg = " TarifGreenfee Seasons = " + Arrays.deepToString(tarifGreenfee.getDatesSeason());
        msg = msg + " <br/>TarifGreenfee Equipments = " + Arrays.deepToString(tarifGreenfee.getPriceEquipments());
        msg = msg + " <br/>TarifGreenfee Times = " + Arrays.deepToString(tarifGreenfee.getTeeTimes());
        msg = msg + " <br/>TarifGreenfee Days = " + Arrays.deepToString(tarifGreenfee.getDays());
        LOG.info(msg);
        showMessageInfo(msg);
        return "tarif_menu.xhtml?faces-redirect=true";
    }else{
        msg = "Fatal Error creation tarif Greenfee";
        LOG.info(msg);
        showMessageFatal(msg);
        return null;
    }
  //   return null; // retourne d'où il vient = tarif_menu
    // à modifier
 //    boolean ok = create.CreateTarif.createTarif(club, conn);
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method

public String inputTarifHours() throws SQLException, Exception{
try{
    LOG.info("entering inputTarifHours !");
        LOG.info("entering tarif Hours with course = " + course.toString());
        LOG.info("teeTimes = "  + Arrays.deepToString(tarifGreenfee.getTeeTimes()));
    String msg = "Tarif Hours = " + tarifGreenfee.getTarifIndexHours() + " / " 
                                   + Arrays.deepToString(tarifGreenfee.getTeeTimes()); //[tarifGreenfee.getTarifIndexHours()]);
 //+  Arrays.deepToString(tarifGreenfee.getPriceEquipments());
       LOG.info(msg);
    showMessageInfo(msg);
    
    tarifGreenfee.setTarifIndexHours(tarifGreenfee.getTarifIndexHours() + 1);
        LOG.info("tarif index Hours is now :" + tarifGreenfee.getTarifIndexHours());
     return null; // retourne d'ouù il vient
}catch(Exception ex){
    String msg = "Exception in inputTarifHours! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method
 public String inputTarifEquipments() throws SQLException, Exception{  // used in tarif_equipments.xhtml
try{
    LOG.info("entering inputTarifEquipments !");
        LOG.info("entering tarifEquipments for course = " + course.toString());
        LOG.info("Members tarifEquipments = "  + Arrays.deepToString(tarifGreenfee.getPriceEquipments()));
        LOG.info("Members tarifGreenfee = "  + tarifGreenfee.toString());
 //   String msg = " input Tarif Member = " + tarifMember.getTarifMemberIndex() + " / " 
 //               + Arrays.deepToString(tarifMember.getMembersBase()[tarifMember.getTarifMemberIndex()]);
  //  LOG.info(msg);
  //  showMessageInfo(msg);
  // update array
    int i = tarifGreenfee.getTarifIndexEquipments();
    String [][] arr = tarifGreenfee.getPriceEquipments();
    arr[i][0] = tarifGreenfee.getWorkItem();
    arr[i][1] = tarifGreenfee.getWorkPrice();
    tarifGreenfee.setPriceEquipments(arr);
    tarifGreenfee.setWorkItem(""); // init pour le prochain affichage
    tarifGreenfee.setWorkPrice("");
        LOG.info("Members tarif updated = " + tarifGreenfee.toString());
    String msg = "Tarif Equipments = "
         //   + tarifGreenfee.getPriceEquipments().getMemberStartDate().format(ZDF_TIME_DAY)
            + " / " 
         //   + tarifGreenfee.getMemberEndDate().format(ZDF_TIME_DAY)
            +  Arrays.deepToString(tarifGreenfee.getPriceEquipments());
        LOG.info(msg);
        showMessageInfo(msg);
  //  tarifGreenfee.setTarifMemberIndex(i + 1);
    tarifGreenfee.setTarifIndexEquipments(i+1);
        LOG.info("length = " + tarifGreenfee.getPriceEquipments().length);
    // vérifier si longueur max est atteint avec lengnt ou size !!
        LOG.info("Tarif Index Equipment's is now :" + tarifGreenfee.getTarifIndexEquipments());
    if(tarifGreenfee.getTarifIndexEquipments() > tarifGreenfee.getPriceEquipments().length){  // plus de place dans l'array ??
        LOG.error("maximum items reached ! ");
    }
    // à faire : vérifier si on n'a pas changé les dates de début et fin
        LOG.info("");
    
      return null; // retourne d'où il vient
}catch(Exception ex){
    String msg = "inputTarifGreenfee Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}
} // end method   
public String old_inputTarifEquipments() throws SQLException, Exception{
try{
        LOG.info("entering inputTarifEquipments !");
        LOG.info("entering tarif equipments with course = " + course.toString());
    String msg = " input Equipments = "  + Arrays.deepToString(tarifGreenfee.getPriceEquipments());
        LOG.info(msg);
        showMessageInfo(msg);
 //   tarif.setTarifIndexHours(tarif.getTarifIndexHours() + 1);
 //       LOG.info("tarif index Hours is now :" + tarif.getTarifIndex());
     return null; // retourne d'où il vient

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method
        
public String showTarifMembers() throws SQLException, Exception{
try{
     LOG.info("entering showTarifMembers !");
     LOG.info("entering showTarifMembers for club = " + club.toString());
     tarifMember.RemoveNull(); // remove null from arrays
      String msg = LCUtil.prepareMessageBean("tarif.member.show")
        + SDF.format(tarifMember.getMemberStartDate()) + " / "
        + SDF.format(tarifMember.getMemberEndDate())
        +  Arrays.deepToString(tarifMember.getMembersBase());
        LOG.info(msg);
        showMessageInfo(msg);
     return null; // retourne d'ouù il vient
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method        

public String inputTarifDaysOfWeek() throws SQLException, Exception{
try{
     LOG.info("entering inputTarifDaysOfWeek !");
     LOG.info("entering tarif Days with course = " + course.toString());
     LOG.info("days = "  + Arrays.deepToString(tarifGreenfee.getDays()));
     String msg = "days = "  + Arrays.deepToString(tarifGreenfee.getDays());
//        LOG.info("début période days = " + tarif.getStartDays());
//        LOG.info("fin période days = " + tarif.getEndDays());
//    String msg = " input days = "  + Arrays.deepToString(tarif.getDays()) 
//            + " / " + tarif.getStartDays() + " / " + tarif.getEndDays();
    LOG.info(msg);
    showMessageInfo(msg);
    
 //   tarif.setTarifIndexHours(tarif.getTarifIndexHours() + 1);
 //       LOG.info("tarif index Hours is now :" + tarif.getTarifIndex());
     return null; // retourne d'ouù il vient

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
}    
} // end method        
        
public void createScoreStableford() throws SQLException, Exception{
   LOG.info("entering createScoreStableford !");
try{
    LOG.info("entering with round game = " + round.getRoundGame());
  //  List<Player> lp = null;
if(Round.GameType.SCRAMBLE.toString().equals(round.getRoundGame())){
        LOG.info("this is a SCRAMBLE game");
     lists.RoundPlayersList spl = new lists.RoundPlayersList();
     lp = spl.listAllParticipants(round, conn);
 //       LOG.info("lp ");
   LOG.info("nombre de players stableford = lp size = " + lp.size());
// on enregistre le résultat pour CHAQUE joueur = principe de fonctionnement
     for(int i=0; i < lp.size() ; i++){
         LOG.debug(" -- item in for idplayer = " + lp.get(i).getIdplayer() );
        player2.setIdplayer(lp.get(i).getIdplayer()); // mod 04-12-2017 en player2
        player2.setPlayerLastName(lp.get(i).getPlayerLastName());
  //      create.CreateScoreStableford css = new create.CreateScoreStableford();
     //   boolean ok = css.createModifyScore(scoreStableford, round, player2, conn);
        if(new create.CreateScoreStableford().createModifyScore(scoreStableford, round, player2, conn)){
            LOG.info("ScoreStableford created for multiple players in scramble round!");
            setShowButtonCreateStatistics(true);  // affiche le bouton bas ecran
        }else{
          LOG.info( "error creation score scramble");
        }
     }    //end for 

}else{ // single stableford : on enregistrele résultat pour un seul joueur !
    //    create.CreateScoreStableford css = new create.CreateScoreStableford();
    //    boolean OK = new create.CreateScoreStableford().createModifyScore(scoreStableford, round, player, conn);
        if(new create.CreateScoreStableford().createModifyScore(scoreStableford, round, player, conn)){
            LOG.info("ScoreStableford created !");
            setShowButtonCreateStatistics(true);  // affiche le bouton bas ecran
        }else{
          LOG.info( "error creation score stableford");}
    }
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
}    
 //   }
} // end method
    
/*  enlevé 29-11-2018 à réécrire
public void createScoreMatchplay() throws SQLException{
try{
            LOG.info("entering createscoreMatchplay !!");
            LOG.info("result, winning team 1 = " + scoreMatchplay.getMatchplayResult() );
        String result = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("result");
            LOG.info("result, winning team 2 = " + result );
            LOG.info("scoreMP4 = " + Arrays.deepToString(scoreMatchplay.getScoreMP4()) );
            LOG.info("there are {} players", listmatchplay.size() );
            LOG.info("listmatchplay = " + Arrays.deepToString(listmatchplay.toArray()));
        round.setIdround(listmatchplay.get(0).getIdround() );
            LOG.info("roundId = " + round.getIdround() );
        int ii = listmatchplay.size();
        Integer[] p = new Integer[ii];
        for (int i=0; i<ii; i++)
        { 
            p[i] = listmatchplay.get(i).getIdplayer();
                LOG.info("player {} = playerid {}" , i+1 , p[i] );
            for (int j=0; j<18; j++)
            {
                LOG.info(" points IN = " + scoreMatchplay.getScoreMP4()[i][j] );
                String s = scoreMatchplay.getScoreMP4()[i][j];
                if(s == null)
                {   s = "0";  }
                s = s.trim().replace(" ", "0"); // .replace(null, "0"); // mod 28/11/2014
 //               if("  ".equals(s)){
                if(s == null || s.equals("") ) {  
                    s = "0";
                }else{
                    s = s.trim();}
                scoreMatchplay.getScoreMP4()[i][j] = s;
                LOG.info(" points OUT= " + scoreMatchplay.getScoreMP4()[i][j] );
            }
        }
        scoreMatchplay.setPlayers(p);
//        LOG.info("players = " + Arrays.deepToString(scoreMatchplay.getPlayers()) );
//         LOG.info("score = "   + Arrays.deepToString(scoreMatchplay.getScoreMP4()) );
        
        // trim + replace ?
        LOG.info("scorematchplay stripped= " + scoreMatchplay.toString() );
        create.CreateScoreMatchplay csmp = new create.CreateScoreMatchplay();
        csmp.createAllScores(scoreMatchplay, round, conn);
} catch(Exception ex)
{
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
     //       return null;
}
} // end method
 */
public void validateScoreHoleMatchplay2(){
    LOG.info("entering validateScoreHoleMatchplay2 for hole = " );
}
        
public void validateScoreHoleMatchplay(FacesContext context, UIComponent toValidate, Object value)
      throws ValidatorException
{
LOG.info("entering validateScoreHoleMatchplay");
    LOG.info("entering validateScoreMatchPlay - toValidate ClientId = " + toValidate.getClientId() );
    LOG.info("entering validateMP4 - toValidate Id = " + toValidate.getId() );
    LOG.info("entering validateMP4 - toValidate ClientId = " + toValidate.getClientId() );
    LOG.info("entering validateMP4 - value = " + value.toString());
    LOG.info("UIcomponent, getFamily = " + toValidate.getFamily());
    LOG.info("UIcomponent, context = " + context.toString());
    LOG.info("UIcomponent,message = " + toValidate.getClientId(context)); 
    String confirm = (String)value;
    
    String field1Id = (String) toValidate.getAttributes().get("scorePlayer11");
         LOG.info(" validateMP4 - field1Id = " + field1Id);
         
    UIInput passComponent = (UIInput) context.getViewRoot().findComponent(field1Id);
         LOG.info(" validateMP4 - passComponent = " + passComponent);
    String pass = (String) passComponent.getSubmittedValue();
        LOG.info(" validateMP4 - pass1 = " + pass);
    if (pass == null){
        pass = (String) passComponent.getValue();
         LOG.info(" validateMP4 - pass2 = " + pass);
    }
    
    if (!pass.equals(confirm)){
        LOG.info(" validateMP4 - pass not equal confirm = " );
        String msg = toValidate.getClientId(context);
        showMessageFatal(msg);
   //     String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
     //   throw new ValidatorException(new FacesMessage(err));
    }
}
    
public void createStatistics() throws SQLException{
    sc2 = scoreStableford.getStatistics();
 //   create.CreateStatistics cs = new create.CreateStatistics();
 //   boolean ok = cs.createStatistics(player, round, sc2, conn);
    // ajouter boolean = correct insert !!!
    if(new create.CreateStatistics().createStatistics(player, round, sc2, conn)){
        LOG.info("statistics created : we go to XXX !!");
         setNextScorecard(true); // affiche le bouton next(Scorecard) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  droite
   }
}
    
public List<Integer> getValues() {
        LOG.info("entering getValues ...");
      if (VALUES.isEmpty()) {
            for (int i = 1; i < 19; i++) {
                VALUES.add(0);
            }
      }
        LOG.info("values returned  =" + VALUES.toString());
        return VALUES;
    }

public void setValues(String strokes) {
        LOG.info("enter setValues with strokes = " + strokes);
        VALUES.set(5, Integer.parseInt(strokes));
    }

public String show_scorecard() throws SQLException{
        LOG.info("entering show_scoreCard with :!" + round.getRoundQualifying() );
 //   show.ShowScoreCard scc = new show.ShowScoreCard();
    return new show.ShowScoreCard().show_scorecard(player, club, course, round, inscription, conn);
}

public String show_scorecard_empty(ECourseList ecl) throws SQLException{
        LOG.info("entering show_scoreCard_empty with :!" + ecl.toString() );
    club.setIdclub(ecl.Eclub.getIdclub());
    course.setIdcourse(ecl.Ecourse.getIdcourse());
    // à verifier
    return show.ShowScoreCard.show_scorecard_empty(player, club, course, round, inscription, conn);
}

  public List<ECourseList> listRounds() {
        LOG.info("from CourseController : listRounds = " );
   try {
      //   lists.RecentList rl = new lists.RecentList();
         return new lists.RecentList().getRecentRoundList(player, conn);
   } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
     }
    } //end method

public List<Flight> listFlights(){
 try {
      LOG.info("from CourseController : entering listFlights ... " );
      if(round.getWorkDate() == null){
         String msg = "Test : listFlights : round workdate = null";
         LOG.error(msg);
         showMessageInfo(msg);
         }else{
     ///                LOG.info("round workdate = " + round.getWorkDate());
         }
 //               LOG.info("line 222 = ");
     if(club.getClubLatitude().compareTo(BigDecimal.ZERO) == 0){
            String msgerr = "La Latitude du club n'est pas connue ! stop de l'opération en cours !! "; // + club.getClubLatitude();
            LOG.error(msgerr);
            showMessageFatal(msgerr);
            throw new Exception(msgerr);
      }

     // new 28-11-2018
        cptFlight++;
    //    LOG.info("cptFlight = " + cptFlight);
 if(cptFlight == 1)   /// éviter iteration !!!
 {
          LOG.info("we do it");
      String tz = "Europe/Brussels";
 //  1  ---------------- cherche sunrise et sunset 
      find.SunriseSunset fss = new find.SunriseSunset();
      Flight f = fss.findSunriseSunset(round.getWorkDate(),club.getClubLatitude(),club.getClubLongitude(),tz,conn);
         LOG.info("1-Flight f = " + f.toString());
 // 2 ------------------  creation tableFlights : 1 record toutes les 12 min en partant de sunrise jusque sunset
      create.CreateAllFlights caf = new create.CreateAllFlights();
       fl = caf.createTableFlights(f.getSunrise(), f.getSunset(), tz, course.getIdcourse(), conn);
        LOG.info("2-table created !");
      // new 28/11/2018 déplacé de CreateAllFlights
//3      
      create.CreateFlights cf = new create.CreateFlights();
      boolean OK = cf.createFlight(fl, course.getIdcourse(), conn);  // fake = courseid
        LOG.info("boolean result create.CreateFlights = " + OK);
 
  // elimination des flights déjà réservés    
      lists.FlightList lfl = new lists.FlightList();
   //   ArrayList<Flight> alf = lfl.listAllFlights(conn);
      fl = lfl.listAllFlights(conn);
//      LOG.info("boolean result lists.FlightList =  = " + OK);
 //        LOG.info("ArrayList<Flight> = " + lfl);
   //      LOG.info("from CourseController : exiting listFlights() ... " );
   flight = fl.get(0);
   return fl;
 }else{
   //  LOG.info(" escaped to repetition ... " + cptFlight);
     return fl;
 }
   } catch (Exception ex) {
            String msg = "Exception in listFlights= " + ex.toString();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
   }
 } //end method

public List<Old_Matchplay> listMatchplayRounds(String formula) {
        try {
            LOG.info("from CourseController : listMatchplayRounds for formula = " + formula);
            lists.MatchplayList mpl = new lists.MatchplayList();
            listmp = mpl.getList(conn,"MP_");
       //        String [][] s = listmp.toArray(sc1);
 //             Matchplay [] countries = listmp.toArray(new Matchplay[listmp.size()]);
 //               LOG.info("countries = " + Arrays.deepToString(countries));
 //              LOG.info("from CourseController : listmp = " + listmp.toString());
         course.setIdcourse(listmp.get(1).getIdcourse() );
                LOG.info("setted idcourse on = " + course.getIdcourse());
         course.setCourseName(listmp.get(1).getCourseName() );
         club.setIdclub(listmp.get(1).getIdclub() );
         club.setClubName(listmp.get(1).getClubName() );
         round.setRoundGame(listmp.get(1).getRoundGame() );
         round.setIdround(listmp.get(1).getIdround() );
              LOG.info("from listmp, round = = " + round.getIdround() );
         round.setRoundCompetition(listmp.get(1).getRoundCompetition() );
    //     round.setRoundDate(listmp.get(1).getRoundDate() );
    
     //    round.setRoundDate(listmp.get(1).getRoundDate() );
         java.util.Date d = listmp.get(1).getRoundDate();
     //    LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         LocalDateTime date = DatetoLocalDateTime(d);
         round.setRoundDate(date);
     //          inscription.setPlayerhasroundTeam(listmp.get(1).getPlayerhasroundTeam() );
         matchplay.setRoundCompetitionName(listmp.get(1).getRoundCompetitionName() );
     //          matchplay.setPlayerhasroundPlayerNumber(listmp.get(1).getPlayerhasroundPlayerNumber() );
               // et d'autres ...
         return listmp;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    
public List<ScoreScramble> listScrambleRounds(String formula){
 try {
        LOG.info("entering  CourseController : listScrambleRounds with formula =  " + formula);
            lists.ScrambleList sl = new lists.ScrambleList();
            listscr = sl.getList(conn, formula);
       //        String [][] s = listmp.toArray(sc1);
 //             Matchplay [] countries = listmp.toArray(new Matchplay[listmp.size()]);
 //               LOG.info("countries = " + Arrays.deepToString(countries));
         LOG.info("from CourseController : listscr = " + listscr.toString());
         LOG.info("from CourseController : listscr rounds = " + listscr.size() );
 //        if(listscr.size() == 2)
 //           {course.setIdcourse(listscr.get(0).getIdcourse() );}
         course.setIdcourse(listscr.get(0).getIdcourse() );
                LOG.info("setted idcourse on = " + course.getIdcourse());
         course.setCourseName(listscr.get(0).getCourseName() );
                LOG.info("setted course name on = " + course.getCourseName() );
         club.setIdclub(listscr.get(0).getIdclub() );
         club.setClubName(listscr.get(0).getClubName() );
         round.setRoundGame(listscr.get(0).getRoundGame() );
         round.setIdround(listscr.get(0).getIdround() );
              LOG.info("from listscr, round = " + round.getIdround() );
         round.setRoundCompetition(listscr.get(0).getRoundCompetition() );
         round.setRoundDate(listscr.get(0).getRoundDate());
         return listscr;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    


public List<Old_Matchplay> getListmp() {
        return listmp;
    }

public void setListmp(List<Old_Matchplay> listmp) {
        this.listmp = listmp;
    }

public List<ECourseList> listPlayedRounds(String formula) throws SQLException{
     LOG.debug(" ... entering listPlayedRounds WITHOUT formula = " + formula);
  //    lists.PlayedList pl = new lists.PlayedList();
 return new lists.PlayedList().getPlayedList(player, conn);
}

public List<ECourseList> listStablefordPlayedRounds(){  // from selectStablefordRounds.xhtml
    try {
        LOG.debug(" ... entering listPlayedRounds ! " );
        //    lists.PlayedList pl = new lists.PlayedList();
            return new lists.PlayedList().getPlayedList(player, conn);
        } catch (SQLException ex) {
            LOG.error("Exception in listPlayedRounds  " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method
 
public int[] LoadTeeStart(){
      //  course.setIdcourse(in_course);
    LOG.info(" ... entering LoadTeeStart in courseC from for " + round.getIdround());
               
        try {
            return null;  // back to originating view
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method
  
public String LoadTarif(){ // new 31/03/2018 utilisé dans viewActioncredit_card_accepted.xhtml ??
    LOG.info(" ... entering LoadTarif for testing purposes payment.xhtml");
        try {
            tarifGreenfee.setPriceGreenfee(99.0);
            String[][] equip = new String[10][1];
 //           LOG.info("line 00");
            equip[0][0]="20";
            equip[1][1]="30";
            equip[2][2]="40";
            equip[3][3]="50";
            tarifGreenfee.setPriceEquipments(equip);
            LOG.info("tarif généré for testing purposes = " + tarifGreenfee.toString()); //Arrays.deepToString(equip));
            
            return null;  // back to originating view
        } catch (Exception ex) {
            LOG.error("Exception in ! " + ex);
            showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method

public String to_selectMatchplayRounds_xhtml(String s) {
            LOG.info("entering to_ ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectMatchplayRounds.xhtml?faces-redirect=true";
   }

public String to_selectPlayer_xhtml(String s) {
            LOG.info("entering to selectPlayer_xhtml... with string = " + s);
            reset(s);
       return "selectPlayer.xhtml?faces-redirect=true";
   }

public String to_selectScrambleRounds_xhtml(String s){
            LOG.info("entering to_selectScrambleRounds_xhtml ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectScrambleRounds.xhtml?faces-redirect=true";
   }
public String to_selectRegisteredRounds_xhtml(String s){
            LOG.info("entering to_selectRegisteredRounds_xhtml with String = " + s);
       reset(s);
       return "selectRegisteredRounds.xhtml?faces-redirect=true&cmd=" + s;
   }

public String to_selectParticipantsRound_xhtml(String s){
            LOG.info("entering to_selectParticpantsRound_xhtml with String = " + s);
       reset(s);
       return "selectParticipantsRound.xhtml?faces-redirect=true&cmd=" + s;
   }

// just fo testing grom menu TEST
public String to_select_tarifMembers_xhtml(Integer i) throws SQLException {
            LOG.info("entering to_select_tarifMembers with string = " + i);
        //    FindTarifMembersData ft = new FindTarifMembersData();
            club.setIdclub(i);
            
            tarifMember = new FindTarifMembersData().findTarif(club,conn);
            reset(String.valueOf(i));
        return "cotisation.xhtml?faces-redirect=true";
   }

public String to_select_inscription_xhtml(String s) {
            LOG.info("entering to_select_inscription_round ... with string = " + s);
            setFilteredInscriptions(null);
            reset(s);
       if(s.equals(Round.GameType.STABLEFORD.toString()))
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals(Round.GameType.SCRAMBLE.toString()))   //  Ã  implémenter pu alors autre solution chacun s'inscrit isolÃ©ment et on regroupe ensuite ...
            { return "selectInscription.xhtml?faces-redirect=true&cmd=" + s;}
       if(s.equals("mp_"))
            { return "selectPlayers_mp.xhtml?faces-redirect=true&cmd=" + s;}
  //      url="selectPlayers_mp.xhtml?cmd=ini"
 return "playing formule not found";           
   }

public String to_selectStablefordRounds_xhtml(String s) {
            LOG.info("entering to_selectStablefordRounds_xhtml with string = " + s);
            reset(s);
       return "selectStablefordRounds.xhtml?faces-redirect=true";
   }

public String to_show_played_rounds_xhtml(String s){
       LOG.info("entering to_show_played_rounds_xhtml with string s = " + s);
            reset(s);
            setInputPlayedRounds(s); // new 29/03/2016
       return "show_played_rounds.xhtml?faces-redirect=true&cmd=" + s; // mod 28/03/2016
   }

public String to_club_xhtml(String s){
        LOG.info("entering to_club_hxtml ... with string = " + s);
            reset(s);
       club.setCreateModify(true);  // gestion button dans club.xhtml
       return "club.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_course_xhtml(String s) {
        LOG.info("entering to_course_xthml ... with string = " + s);
            reset(s);
       course.setCreateModify(true);  // gestion button dans ccourse.xhtml
       return "course.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_tee_xhtml(String s) {
        LOG.info("entering to_tee_xthml ... with string = " + s);
            reset(s);
       tee.setCreateModify(true);  // gestion button dans tee.xhtml
       return "tee.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_clubModify_xhtml(String s){
        LOG.info("entering to_clubModify_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       course.setInputSelectCourse(s);
       return "modifyClubCourseTee.xhtml?faces-redirect=true";
   }
public String to_clubDelete_xhtml(String s){
        LOG.info("entering to_clubDelete_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       course.setInputSelectCourse(s);
       return "deleteClubCourseTee.xhtml?faces-redirect=true";
   }

public String to_player_xhtml(String s){
         LOG.info("entering to_player_xthml ... with string = " + s);
       reset(s);
       return "player.xhtml?faces-redirect=true";
   }

public void to_player_modify(String s){
         LOG.info("entering to_player_modify ... with string = " + s);
  //     reset(s);
        createModifyPlayer = s;
 //      return "player_modify.xhtml?faces-redirect=true";
   }

public String to_show_handicap_xhtml(String s){
        LOG.info("entering to_show_handicap ... with string = " + s);
            reset(s);
       return "show_handicap.xhtml?faces-redirect=true";
   }

public String to_delete_player_xhtml(String s) throws Exception {
        LOG.info("entering to_delete_player ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   //         return "";
   }
public String to_delete_club_xhtml(String s) throws Exception {
    // utilisé ?
            LOG.info("entering to_delete_club ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   //         return "";
   }
  public void deleteCascadingPlayer() throws SQLException, Exception{
 try{  // fonctionne ?
     delete.DeletePlayer dp = new delete.DeletePlayer();
    dp.deletePlayerAndChilds( getDeletePlayer(),conn );
    // ajouter boolean = correct insert !!!
  //  if(ok)
  //  {
  //          LOG.info("player created, next step = photo");
  //      setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
  //  }else{
        // error in create player
  //      LOG.info("error : new player ,not created !!");
   // }
 }catch (Exception ex){
            String msg = "Exception in deletePlayer and childs " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player

public String scorecard(ECourseList ecl) {
    LOG.info("Entering scorecard");// with ecl = " + ecl.toString());
        LOG.info("Entering scorecard with ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        inscription = ecl.getInscription();
  
            LOG.info("TeeStart from scorecard = " + inscription.getInscriptionTeeStart());
        String msg = "Select EcourseList Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
  //      showMessageInfo(msg);
        return "show_scorecard.xhtml?faces-redirect=true";
     } //end

public List<ECourseList> getScoreCardList1() throws SQLException, LCCustomException {
 try{
     //   lists.ScoreCard1List scl = new lists.ScoreCard1List();
        List<ECourseList> li = new lists.ScoreCard1List().getScoreCardList1(player, round, conn);
            LOG.info("exiting getScoreCardList1");
        return li; //scl.getScoreCardList1(player, round, conn);
 }catch (NullPointerException | SQLException ex){
            String msg = "Exception in getScoreCardList1() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
    } finally {

    }
} //end method

    public static boolean isShowButtonCreditCard() {
        return ShowButtonCreditCard;
    }

    public static void setShowButtonCreditCard(boolean ShowButtonCreditCard) {
  
        CourseController.ShowButtonCreditCard = ShowButtonCreditCard;
    }

public List<ECourseList> getScoreCardList2() throws SQLException {
 try{   LOG.info("entering getScoreCardList2");
        // le nom est trompeur : fait beaucup plus que son nom l'indique !
     //   find.FindSlopeRating fsr =  new find.FindSlopeRating();
      List<ECourseList> li = new find.FindSlopeRating().getSlopeRating(player, round, conn);
    //    return fsr.getSlopeRating(player, round, conn);
        // pour utiliser dans getScoreCardList3
      inscriptionNew.setInscriptionTeeStart(li.get(0).EinscriptionNew.getInscriptionTeeStart() );
      LOG.info("exiting getScoreCardList2");
      // astuce !! chiotage ...
      lists.ScoreCard3List sc = new lists.ScoreCard3List();
      sc.getScoreCardList3(player, round, inscriptionNew, conn);
                 return li;
    }catch (Exception ex){
            String msg = "Exception in getScoreCardList2() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

public List<ECourseList> getScoreCardList3() throws SQLException {
   try{ LOG.info("entering getScoreCardList3");
       //   lists.ScoreCard3List sc = new lists.ScoreCard3List();
            return new lists.ScoreCard3List().getScoreCardList3(player, round, inscriptionNew, conn);
      }catch (Exception ex){
            String msg = "Exception in getScoreCardList3() " + ex;
                LOG.error(msg);
            showMessageFatal(msg);
            return null;
      } finally {
      }
    } //end method

 public int TotalPar()
         // bizarre utilisé dans
         // les autes totaux viennent de TotalController
{ int total = 0;
     LOG.info(" starting getTotalPar () ");
  for (ECourseList golf : lists.ScoreCard3List.getListe())
    {total += golf.EscoreStableford.getScorePar();}
     LOG.info("total par = " + total);
  return total;
}

public List<Player> listPlayers() throws SQLException {
//            LOG.info("... entering listPlayers with conn = " + conn);
   try {
     //  lists.PlayersList pl = new lists.PlayersList();
       return new lists.PlayersList().getListAllPlayers(conn);
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
     LOG.info("... entering listSubscriptionReneval " + s); // a quoi sert le s ? paramètre ?
         //   lists.SubscriptionRenewalList srl = new lists.SubscriptionRenewalList();
             subscriptionRenewal = new lists.SubscriptionRenewalList().getListSubscriptions(conn);
    //         subscriptionReneval.forEach(item -> LOG.info("liste " + item));  // java 8 lambda
            String msg = "We send subscription Renewal Mails = " + subscriptionRenewal.size();
            LOG.info(msg);
            LCUtil.showDialogInfo(msg);
             
   } catch (Exception ex) {
            String msg = "Exception in listSubscriptionRenewal " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
       //     return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method        
        
 public List<Car> listCars() throws SQLException {
    //        LOG.info("... entering listCars");
 try {
            lists.CarList cl = new lists.CarList();
            return cl.getListAllCars(conn);
 } catch (Exception ex) {
            String msg = "Exception in listCars() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
 public void validatePlayer()// throws SQLException, Exception
{
     setNextPanelPlayer(true);  //affiche le 2e panelGrid
}

 public String creditCardMail() throws Exception{
     LOG.info("entering creditCardMail with creditcard typePayment = " + creditcard.getTypePayment());
     // à faire : renvoyer le mail status dans l'écran 
        if(Creditcard.etypePayment.GREENFEE.toString().equals(creditcard.getTypePayment())){
           // creditCardInscriptionMail();
           boolean ok = new mail.CreditcardMail().sendMailInscription(creditcard,tarifGreenfee, round, inscription);
        }
        if(Creditcard.etypePayment.SUBSCRIPTION.toString().equals(creditcard.getTypePayment())){
            //creditCardSubscriptionMail();
            boolean ok = new mail.CreditcardMail().sendMailSubscription(creditcard, subscription);
        }
        if(Creditcard.etypePayment.COTISATION.toString().equals(creditcard.getTypePayment())){
          //  creditCardCotisationMail();
          boolean ok = new mail.CreditcardMail().sendMailCotisation(creditcard, cotisation, club, tarifMember);
        }
     return "creditcard_payment_executed.xhtml?faces-redirect=true";
  }
 /*
public String creditCardInscriptionMail() throws MessagingException, Exception{
    LOG.info("entering creditCardMail");
  //  mail.CreditcardMail3 ccm = new mail.CreditcardMail3();
    boolean ok = new mail.CreditcardMail3().sendMail(creditcard,tarif, round, inscription);
  //  mail.CreditcardMail2..CreditcardMail2();
   
    //   LOG.info("HTML Mail status = " + b);
       return null;
    //   return "creditcard_accepted.xhtml?faces-redirect=true";
}
 */
/*
public String creditCardSubscriptionMail() throws MessagingException, Exception{
    LOG.info("entering creditCardSubscriptionMail");
  //  mail.CreditcardMail3 ccm = new mail.CreditcardMail3();
    boolean ok = new mail.CreditcardMail3().sendCreditcardMailSubscription(creditcard, subscription);
  //  mail.CreditcardMail2..CreditcardMail2();
   
  //     LOG.info("HTML Mail status = " + b);
       return null;
      // return "creditcard_accepted.xhtml?faces-redirect=true";
 }
*/
/*
public String creditCardCotisationMail() throws MessagingException, Exception{
    LOG.info("entering creditCardCotisationMail");
  //  mail.CreditcardMail3 ccm = new mail.CreditcardMail3();
    boolean ok = new mail.CreditcardMail3().sendCreditcardMailCotisation(creditcard, cotisation, club);
  //     LOG.info("HTML Mail status = " + b);
       return null;
      // return "creditcard_accepted.xhtml?faces-redirect=true";
 }
*/
public void createPlayer() throws SQLException, Exception{
 try{
     LOG.info("entering createPlayer");
 //     create.CreatePlayer cp = new create.CreatePlayer();
  //  boolean ok = cp.createPlayer(player, handicap, conn, "A"); // "A" signifie avec Activation (non en batch)
    if(new create.CreatePlayer().createPlayer(player, handicap, conn, "A")){
            LOG.info("player created, next step = photo");
        setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
    }else{
        // error in create player
        LOG.info("error : new player ,not created !!");
    }
 }catch (Exception ex){
            String msg = "Exception in createPlayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
} //end method create player
//////////////////////////////////////////////////////////////////////////////// club

public String createClub() throws SQLException, IOException{
        LOG.info("entering CreateClub");
  //  create.CreateClub cc = new create.CreateClub();
  //  boolean ok = cc.createClub(club, conn);
  LOG.info("entering createclub");
 // LOG.info("with club = " + club.toString());
    if(new create.CreateClub().createClub(club, conn)){
        LOG.info("club created : we go to course !!");
        course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
    }
    return null;
} // end method createClub

public String addCourse(ECourseList ecl) throws SQLException, Exception{
 try{
      LOG.info("entering addCourse");
      //   load.LoadClub lc = new load.LoadClub();
         club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
         LOG.info("adding a course for idclub = " + ecl.Eclub.getIdclub() + club.getClubName());
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

public String addTee(ECourseList ecl) throws SQLException, Exception{
 try{
      LOG.info("entering addTeee");
      //   load.LoadClub lcl = new load.LoadClub();
         club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
      //   load.LoadCourse lco = new load.LoadCourse();
         course = new load.LoadCourse().LoadCourse(conn, ecl.Ecourse.getIdcourse());
            LOG.info("idclub forced at " + ecl.Eclub.getIdclub() + club.getClubName());
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

public String deleteClub(ECourseList ecl) throws SQLException, Exception{ 
 try{
     LOG.info("entering deleteClub for " + ecl.Eclub.getIdclub());
    //   delete.DeleteClub dc= new delete.DeleteClub();
       boolean OK = new delete.DeleteClub().deleteClub(ecl.Eclub.getIdclub(), conn);
            LOG.info(" result of deleteClub = " + OK);
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

public String deleteCourse(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.info("entering deleteTee for " + ecl.Ecourse.getIdcourse());
    // delete.DeleteCourse dc = new delete.DeleteCourse();
     boolean OK = new delete.DeleteCourse().deleteCourse(ecl.Ecourse.getIdcourse(), conn); //.deleteHoles(ecl.Etee.getIdtee(), conn);
            LOG.info(" result of deleteCourse = " + OK);
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

public String deleteTee(ECourseList ecl) throws SQLException, Exception{
 try{
            LOG.info("entering deleteTee for " + ecl.Etee.getIdtee());
    //    delete.DeleteTee dt = new delete.DeleteTee();
        boolean OK = new delete.DeleteTee().deleteTee(ecl.Etee.getIdtee(), conn);
            LOG.info(" result of deleteTee = " + OK);
        lists.CourseList.setListe(null);// reset
        listCourses(); // refresh list without the deleted item    
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteTee" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method deleteTee

public  int getCountHoles(int hol) throws SQLException{
        LOG.info("getCountHoles input = " + hol);
    find.FindCountHoles fch = new find.FindCountHoles();
    return fch.findCountHoles(hol,conn);
}

public String deleteHoles(ECourseList ecl) throws SQLException, Exception{ 
 try{
       LOG.info("entering deleteHoles for Tee = " + ecl.Etee.getIdtee());
  //   delete.DeleteHoles dh = new delete.DeleteHoles();
     boolean OK = new delete.DeleteHoles().deleteHoles(ecl.Etee.getIdtee(), conn);
       LOG.info(" result of deleteHoles = " + OK);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
     return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteTee" + ex;
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
       LOG.info("entering loadCourse");
       LOG.info("with ecl = " + ecl.toString());
       LOG.info("idcourse in loadCourse = " + ecl.Ecourse.getIdcourse() );
 //    load.LoadCourse lco = new load.LoadCourse();
     course = new load.LoadCourse().LoadCourse(conn, ecl.Ecourse.getIdcourse() );
     
  //   load.LoadClub lcl = new load.LoadClub();
     club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("idclub after loadCourse= " + club.getIdclub());  // si est null faut complémenter
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
        LOG.info("error : course not retreaved !!");
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

 public String modifyCourse() throws Exception {
        LOG.info("entering modifyCourse  "); 
        LOG.info("course to be modified = " + course.toString());
 //   modify.ModifyCourse mc = new modify.ModifyCourse();
  //  boolean OK = mc.modifyCourse(course, conn);
    if(new modify.ModifyCourse().modifyCourse(course, conn)) {
        String msg = "course Modified !! ";
        LOG.info(msg);
        showMessageInfo(msg);
    }
 return null;
    } // end modifyCourse

public String loadTee(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.info("entering loadTee");
        LOG.info("loadTee entering ecl = " + ecl.toString());
  //   load.LoadTee lt = new load.LoadTee();
     tee = new load.LoadTee().LoadTee(conn, ecl.Etee.getIdtee() );
   //  load.LoadCourse lco = new load.LoadCourse();
     course = new load.LoadCourse().LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
   //  load.LoadClub lcl = new load.LoadClub();
     club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.info("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null)
     {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.info("idcourse forced");
     }
    if(tee != null){
        tee.setCreateModify(false); // gestion button dans club.xhtml
        return "tee.xhtml?faces-redirect=true&operation=modify";
    }else{
        // error in create player
        LOG.info("error : tee not retreaved !!");
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadCourse " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

public String loadHoles(ECourseList ecl) throws SQLException, Exception{  // multiple Hole
 try{
        LOG.info("entering loadHoles - multiple");
  //   load.LoadTee lt = new load.LoadTee();
     tee = new load.LoadTee().LoadTee(conn, ecl.Etee.getIdtee() );
  //   load.LoadHoles lh = new load.LoadHoles();
     holesGlobal = new load.LoadHoles().LoadHolesArray(conn, tee.getIdtee());
  //   load.LoadCourse lco = new load.LoadCourse();
     course = new load.LoadCourse().LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
  //   load.LoadClub lcl = new load.LoadClub();
     club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("course after loadHoles = " + course.toString()); 
        LOG.info("tee after loadHoles = " + tee.toString());
  //       course.setIdcourse(tee.getCourse_idcourse());
  //       LOG.info("idcourse forced");
      hole.setCreateModify(false); // gestion button dans club.xhtml
      return "modifyHoles.xhtml?faces-redirect=true&operation=modify holes";
 }catch (Exception ex){
            String msg = "Exception in loadHoles " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
} //end method loadHolee

 public String modifyHolesGlobal() throws Exception{
        LOG.info("entering modifyHolesGlobal  "); 
 //       LOG.info("holesGlobal - new holes values = " + holesGlobal.toString());
 //       LOG.info("tee = " + tee.toString());
 //       LOG.info("course = " + course.toString());
 
 /// solution de contournement pour créer en une seule fois tous les trous
 /// ne fonctionne pas encore !!!
   //   CreateHolesGlobal2 chg = new CreateHolesGlobal2(); 
   //   boolean OK = chg.createHoles(holesGlobal, tee, course, conn);
 // à restaurer !!
 
  //  modify.ModifyHolesGlobal mhg = new modify.ModifyHolesGlobal(); 
 //   boolean OK = mhg.updateHoles(holesGlobal, tee, conn);
    if(new modify.ModifyHolesGlobal().updateHoles(holesGlobal, tee, conn)){
         String msg = "Holes Modified !! ";
         LOG.info(msg);
         showMessageFatal(msg);
     }
 return null;
    } // end modifyHolesGlobal
 
 public String createHolesGlobal() throws Exception // new 13/08/2017
    {   
        LOG.info("entering createHolesGlobal  "); 
 //       LOG.info("holesGlobal - new holes values = " + holesGlobal.toString());
 //       LOG.info("tee = " + tee.toString());
 //       LOG.info("course = " + course.toString());
 
 /// solution de contournement pour créer en une seule fois tous les trous
 /// ne fonctionne pas encore !!!
 //    create.CreateHolesGlobal chg = new create.CreateHolesGlobal(); 
     boolean OK = new create.CreateHolesGlobal().createHoles(holesGlobal, tee, course, conn);
 // à restaurer !!
 
 //   ModifyHolesGlobal mhg = new ModifyHolesGlobal(); 
//    boolean OK = mhg.updateHoles(holesGlobal, tee, conn);
 
 
    if(OK){
         String msg = "Holes Modified !! ";
            LOG.info(msg);
         showMessageFatal(msg);
     }
 return null;
    } // end modifyHolesGlobal
 
public String loadHole(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.info("entering loadTeee");
  //   load.LoadTee lt = new load.LoadTee();
     tee = new load.LoadTee().LoadTee(conn, ecl.Etee.getIdtee() );
  //   load.LoadCourse lco = new load.LoadCourse();
     course = new load.LoadCourse().LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
  //   load.LoadClub lcl = new load.LoadClub();
     club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.info("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null) {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.info("idcourse forced");
     }
    if(tee != null){
        tee.setCreateModify(false); // gestion button dans club.xhtml
        return "hole.xhtml?faces-redirect=true&operation=modify hole";
    }else{
        // error in create player
        LOG.info("error : tee not retreaved !!");
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
     //modify club from modifyClubCourseTee.xhtml
        LOG.info("entering modifyTee  "); 
        LOG.info("tee to be modified = " + tee.toString());
    //    modify.ModifyTee mt = new modify.ModifyTee();
   //     boolean OK = mt.modifyTee(tee, conn);
    if(new modify.ModifyTee().modifyTee(tee, conn)){
        tee.setNextTee(true); // affiche le bouton next(Course) bas ecran a droite
        LOG.info("tee Modified !!");
    }
 return null;
    } // end modifyTee

public String loadClub(ECourseList ecl) throws SQLException, Exception{
 try{
        LOG.info("entering loadClub");
  //  load.LoadClub lcl = new load.LoadClub();
    club = new load.LoadClub().LoadClub(conn, ecl.Eclub.getIdclub() );
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
        LOG.info("entering modifyClub  ");
        LOG.info("club to be modified = " + club.toString());
   //     modify.ModifyClub mc = new modify.ModifyClub();
    //    boolean OK= mc.modifyClub(club, conn);
    if(new modify.ModifyClub().modifyClub(club, conn)){
        course.setNextCourse(false); // n'affiche PAS le bouton next(Course) bas ecran a droite
        LOG.info("club is wel Modified !!");
    }
 return null;
    } // end modifyClub


public void modifyPlayer() throws SQLException, Exception{
 try{
        LOG.info("entering modiftPlayer");
 //    modify.ModifyPlayer mp = new modify.ModifyPlayer();
  //   boolean ok = mp.modifyPlayer(player, conn); // "A" signifie avec Activation (non en batch)
    if(new modify.ModifyPlayer().modifyPlayer(player, conn)){
            LOG.info("player modified, next step = photo");
        setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
    //    setCreateModifyPlayer("M");
        createModifyPlayer = "M";  // c'est trop tard !
    }else{
        // error in create player
        LOG.info("error : new player ,not created !!");
    }
 }catch (Exception ex){
            String msg = "Exception in modifyPlayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player
/* enlevé 29-11-2018 à réécrire
 public String listParticipants_mp(Old_Matchplay mp) throws SQLException {
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
*/
 public String listParticipants_stableford(ECourseList ecl) throws SQLException {
  try {
          LOG.debug(" -- entering listParticipants_stableford = " + ecl.Eround.getIdround() );
            //    round.setIdround(ecl.Eround.getIdround());
          round = ecl.getRound(); // mod 25-11-2018
          club = ecl.Eclub;
          course = ecl.Ecourse;
  //        lists.ParticipantsStableford ps = new lists.ParticipantsStableford();
          listStableford = new lists.ParticipantsStableford().listAllParticipants(round, conn);
             LOG.debug(" -- exiting listParticipants_stableford = ");
             LOG.debug("liste participants stableford = " + Arrays.deepToString(listStableford.toArray()) );
             
             LOG.info("PlayersString was " + round.getPlayersString());
          String s1 = utils.LCUtil.fillRoundPlayersStringEcl(listStableford);
          round.setPlayersString(s1);
             LOG.info("PlayersString is now " + round.getPlayersString());
           round.setRoundGame(listStableford.get(0).Eround.getRoundGame());
           return "show_participants_stableford.xhtml?faces-redirect=true&cmd=SCR";
        } catch (Exception ex) {
            String msg = "Exception in listParticipants_scramble" + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

 public List<ECourseList> listCourses() throws SQLException{
 try {
          LOG.debug(" -- entering listCourses ");
          LOG.info("listCourses - course indicator = " + course.getInputSelectCourse());
  //     lists.CourseList cl = new lists.CourseList();
   //    List<ECourseList> ec = cl.getCourseList(conn);
   //    return ec;
       return new lists.CourseList().getCourseList(conn);
    } catch (Exception ex) {
            String msg = "Exception in listCourses() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
 public List<ECourseList> listDetailClub() throws SQLException{
 try {
          LOG.debug(" -- entering listDetailClub ");
   //    lists.ClubDetailList cl = new lists.ClubDetailList();
       List<ECourseList> ecl = new lists.ClubDetailList().detailList(club, conn); //.getCourseList(conn);
       club = ecl.get(0).Eclub;
       return ecl; //cl.detailList(course, conn);
    } catch (Exception ex) {
            String msg = "Exception in listDetailClub() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        }
    } //end method
 public List<ECourseList> listClubsCoursesTees() throws SQLException {
   try {
       LOG.debug(" -- entering listClubsCoursesTee ");
//       lists.ClubCourseTeeList cctl = new lists.ClubCourseTeeList();
   //    List<ECourseList> ecl = null;
  //     ecl = cctl.getCourseList(conn);
 //      course = ecl.get(0).Ecourse;
       return new lists.ClubCourseTeeList().getCourseList(conn);
    } catch (SQLException ex) {
            String msg = "Exception in listClubsCoursesTees() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
  public List<ECourseList> listInscriptions() throws SQLException{
   try {
            LOG.debug(" -- entering listInscriptions with inputInscription = " + getInputInscription());
 //       lists.InscriptionList il = new lists.InscriptionList();
        return new lists.InscriptionList().getInscriptionList(conn);
         } catch (Exception ex) {
            String msg = "Exception in getInscriptionList() " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method

  public String cancelInscription(ECourseList ecl) throws Exception {// throws SQLException  {
  try{
      LOG.info(" starting cancelInscription ");
      LOG.info(" with ecl = " + ecl.toString());
      setPlayer2(ecl.Eplayer);
      setRound(ecl.Eround);
      /// controle ne devait canceler que ses propres rounds !!!
      LOG.info("current player = " + player.getIdplayer());
      LOG.info("current player ROLE = " + player.getPlayerRole());
      LOG.info("cancellation for player = " + player2.getIdplayer());
      LOG.info("cancellation for player ROLE = " + player2.getPlayerRole());
      // new 04-11-2018
      if((! player.getPlayerRole().equals("ADMIN")) && (player.getIdplayer().intValue() != player2.getIdplayer().intValue())){  // mod 01-12-018 player 2 et ||
          String msgerr =  LCUtil.prepareMessageBean("cancel.inscription");
          msgerr = msgerr + player.getIdplayer() + " /\\ " + player2.getIdplayer() + " /\\ " + player.getPlayerRole();
          LOG.error(msgerr);
          showMessageFatal(msgerr);
          throw new Exception(msgerr);
      }
            
   //   delete.DeleteInscription di = new delete.DeleteInscription();
      new delete.DeleteInscription().deleteInscription(player2, round, ecl, conn);
      
      lists.ParticipantsStableford.setListe(null);  // reset
      
  //    lists.ParticipantsStableford ps = new lists.ParticipantsStableford();
      listStableford = new lists.ParticipantsStableford().listAllParticipants(round, conn); // refresh list without the deleted item
       //new 01-12-2018/
       String s1 = utils.LCUtil.fillRoundPlayersStringEcl(listStableford);
       round.setPlayersString(s1);
         LOG.info("PlayersString is now " + round.getPlayersString());
       // compteur aussi !!  
        short sh8 = round.getRoundPlayers();
        round.setRoundPlayers(--sh8);
             LOG.info("RoundPlayers is now " +  round.getRoundPlayers());
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
     //    lists.HandicapList hl = new lists.HandicapList(); 
         return new lists.HandicapList().getHandicapList(player, conn);
      } catch (Exception ex) {
            String msg = "Exception in listHandicaps " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
    /* à réécrire enlevé 29-11-2018
  public String cancelHandicap(EcourseList ccr) throws Exception{
      LOG.info(" starting cancelHandicap ");
      LOG.info(" with CCR = " + ccr.toString());
      LOG.info(" for player Id = " + ccr.getIdplayer());
      LOG.info(" for player Last Name = " + ccr.getPlayerLastName());
      LOG.info(" for handicap = " + ccr.getHandicapStart());
      
///      delete.DeleteHandicap.deleteHandicap(ccr.getIdplayer(), ccr.getIdhandicap(), conn);
      
      lists.HandicapList.setListe(null);  // reset
      listHandicaps();  // refresh list without the deleted item
    return "show_handicap.xhtml?faces-redirect=true";  // refresh view without message !
  }
  */
  public String cancelRound(ECourseList ecl) throws Exception{
        LOG.info(" starting cancelRound ");
        LOG.info(" with ecl = " + ecl.toString());
  //    delete.DeleteRound dr = new delete.DeleteRound();
      new delete.DeleteRound().deleteRound(ecl.Eround.getIdround(), conn);
      
      lists.InscriptionList.setListe(null);  // reset
      listInscriptions();  // refresh list without the deleted item
    return "selectInscription.xhtml?faces-redirect=true";  // refresh view without message !
  }
    
  //https://dzone.com/articles/javaserver-faces-23-1
@Inject
@SessionMap
private Map<String, Object> sessionMapJSF23;
@Inject
@ApplicationMap 
private Map<String, Object> applicationMap;
public String selectPlayer(Player in_player) throws SQLException{
try{
    LOG.info(" starting selectPlayer ");
 //   LOG.info(" starting selectPlayer = " + player.getIdplayer().toString());
        this.player =  in_player;
        // change language according to the user database language 
        LanguageController.setLanguage(player.getPlayerLanguage());
            LOG.info("Language Player = " + player.getPlayerLanguage());
        String msg="selected Player = " + player.toString();
            LOG.info(msg);
  //         LOG.info("playerbirthdate = " + player.getPlayerBirthDate());
        int age = utils.LCUtil.calculateAgeFirstJanuary(player.getPlayerBirthDate());
        LOG.info("Your age at first january current year = " + age);
  // mod 07-08-2018     
        
 LOG.info("1. verifying if there is a password");
        LOG.info("player password = " + player.getPlayerPassword());
        if(player.getPlayerPassword() == null){
            msg = "The player has no password yet !! ";
            LOG.info(msg);
            showMessageInfo(msg);
            return "password_create.xhtml?faces-redirect=true";
        }
          // new 27-08-2018 https://stackoverflow.com/questions/7644968/httpsession-how-to-get-the-session-setattribute
          
        // donne le même résultat !! ancienne façon et nouvelle façon   
////       FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("playerid", player.getIdplayer());
       sessionMapJSF23.put("playerid", player.getIdplayer());
       sessionMapJSF23.put("playerlastname", player.getPlayerLastName());
       sessionMapJSF23.put("playerage", age);
            LOG.info("printing sessonMapJSF23 = ");
       utils.LCUtil.logMap(sessionMapJSF23);
       setConnected(true); // affiche le bouton Logout dans header.xhtml

 LOG.info("2. verifying if there is a valid subscription");
 
        if(new find.FindSubscriptionStatus().subscriptionStatus(subscription, player, conn)){  //true
            LOG.info("subscription found = " + subscription);
            LOG.info("subscription end date = " + subscription.getEndDate());
            if(subscription.getEndDate() == null){
                LOG.info("this was an initial record creation");
                return "subscription.xhtml?faces-redirect=true";
            }else{
                LOG.info(" subscription verification OK , going to welcome.xhtml "); // subscription OK
              return "welcome.xhtml?faces-redirect=true";
            }
            
        }else{
           LOG.info(" subscription verification NOT ok, going to : subscription.xhtml");  // no valid subscription
           msg = LCUtil.prepareMessageBean("subscription.invalid"); 
           LOG.info(msg);
           showMessageInfo(msg);
           return "subscription.xhtml?faces-redirect=true";
        }
  } catch (Exception e) {
            String msg = "££ Exception selectPlayer = " + e.getMessage() + " for player = " + player.getPlayerLastName();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }  
} // end method

public String findSun() throws SQLException, IOException
{
    // ajouter boolean = correct insert !!!

    LocalDate today = LocalDate.now();
////    String r = find.SunriseSunset.findSunriseSunset(dtf.format(today), player.getPlayerTimeZone().getTimeZoneId());
//   String r = "fake";
 //   LOG.info("string returned in findSun = " + r);
    return "fake";
  //  if(ok)
  //  {
   //     LOG.info("club created : we go to course !!");
    //    course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
    }

public boolean createCotisation() throws SQLException{  // à adapter ??
 try{
     // la cotisation est payée : il faut l'enregistrer en DB
      LOG.info("entering createCotisation");
      LOG.info("with cotisation = " + cotisation);
      
   //   create.CreateCotisation cc = new create.CreateCotisation();
  //    boolean OK = new create.CreateCotisation().create(player, conn);
      if(new create.CreateCotisation().create(cotisation, conn)){
        LOG.info("after createCotisation : we are OK");
        String msg = LCUtil.prepareMessageBean("cotisation.success");
        msg = msg + cotisation.getStartDate().format(ZDF_DAY) + " - " 
                  + cotisation.getEndDate().format(ZDF_DAY)
                  + " club = " + club.getClubName();
        LOG.info(msg);
        showMessageInfo(msg);
        boolean ok = new mail.CreditcardMail().sendMailCotisation(creditcard, cotisation, club, tarifMember);
        return true;
     }else{
        String msg = "Error : cotisation NOT modified !!";
        LOG.error(msg);
        showMessageInfo(msg);
        return false; // retourne d'ou il vient : où ??
    }
 //}catch (SQLException ex){
 //           String msg = "SQLException in handleSubscription " + ex;
 //           LOG.error(msg);
 //           showMessageFatal(msg);
 //           return null;
  }catch (Exception ex){
            String msg = "Exception in createCotisation " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method modify subscription
 // }

public boolean createGreenfee() throws SQLException{  // à adapter ??
 try{
     // le greenfee est payée : il faut l'enregistrer en DB
      LOG.info("entering createGreenfee");
      LOG.info("with greenfee = " + greenfee);
      
   //   create.CreateCotisation cc = new create.CreateCotisation();
  //    boolean OK = new create.CreateCotisation().create(player, conn);
      if(new create.CreateGreenfee().create(greenfee, conn)){
        LOG.info("after createGreenfee : we are OK");
        String msg = LCUtil.prepareMessageBean("greenfee.success");
        msg = msg + greenfee.getRoundDate().format(ZDF_DAY) + " - " 
              //    + greenfee.getEndDate().format(ZDF_DAY)
                  + " round = " + round.getRoundGame();
        LOG.info(msg);
        showMessageInfo(msg);
        boolean ok = new mail.CreditcardMail().sendMailGreenfee(creditcard, greenfee, club);
        return true;
     }else{
        String msg = "Error : cotisation NOT modified !!";
        LOG.error(msg);
        showMessageInfo(msg);
        return false; // retourne d'ou il vient : où ??
    }
 //}catch (SQLException ex){
 //           String msg = "SQLException in handleSubscription " + ex;
 //           LOG.error(msg);
 //           showMessageFatal(msg);
 //           return null;
  }catch (Exception ex){
            String msg = "Exception in createCotisation " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
  }
 } //end method modify subscription
 // }


public String modifySubscription() throws Exception{ 
 try{
      LOG.info("entering modifySubscription");
      LOG.info("with subscription = " + subscription);
 //   subscription.setPaymentReference(creditcard.getReference());
 //   boolean ok = new modify.ModifySubscription().modify(subscription, conn);
  //  LOG.info("after modifySubscription");
    // quel est le contexte ??
    // on est dans une nouvelle session ?
    if(new modify.ModifySubscription().modify(subscription, conn)){
        LOG.info("after modifySubscription : we are OK");
        String msg = LCUtil.prepareMessageBean("subscription.success") + subscription.getEndDate();
        LOG.info(msg);
     //   showMessageInfo(msg);
        return "login.xhtml?faces-redirect=true&cmd=paid";
     }else{
        String msg = "error : subscription NOT modified !!";
        LOG.error(msg);
        showMessageInfo(msg);
        return null; // retourne d'ou il vient
    }
 }catch (SQLException ex){
            String msg = "SQLException in handleSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }catch (Exception ex){
            String msg = "Exception in handleSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
 } //end method modify subscription
 // }
  public String manageCotisation() throws Exception{ // called from subscription.xhtml
 try{
      LOG.info("entering manageCotisation ");
   cotisation.setIdplayer(player.getIdplayer());
      LOG.info("Cotisation = " + cotisation.toString());
              // concerne le payment avec carte de crédit
      LOG.info("tarif Member = " + tarifMember.toString());
            // il faudrait ici 
 //    fills the data
       cotisation = new load.LoadCotisation().load(tarifMember, cotisation, club);
       boolean OK = false;
       if(cotisation.getPrice() != 0){
          LOG.info("amount non ZERO payment needed !");
          OK = CreditcardPayment(cotisation);  // le paiement par carte de crédit est exécuté correctement
      }else{
          String msg = "amount ZERO no payment needed !!";
          LOG.info(msg);
          showMessageInfo(msg);
       }
       /// si OK alors il faut enregistrer la cotisation et créer le membre
       /// si pas de paiement nécessaire ??
       
        // tester return ??
       if(OK){
           String msg = "Paiement par creditcard OK";
            LOG.info(msg);
            showMessageInfo(msg);
        }else{
           String msg = "paiement par creditcard KO : quelle conclusion ?";
            LOG.info(msg);
            showMessageInfo(msg);
        }
     LOG.info("after call to credidtcardpayment");
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
         return null;
 } //end method manageSubscripti 
  
  public String manageGreenfee() throws Exception{ // called from subscription.xhtml
 try{
     
     // à adapter !! remplacer cotisation par Greenfee !!
     
     
        LOG.info("entering manageGreenfee");
     greenfee.setIdplayer(player.getIdplayer());
        LOG.info("Greenfee = " + greenfee.toString());
              // concerne le payment avec carte de crédit
   //   LOG.info("tarif Member = " + tarifMember.toString());
            // il faudrait ici 
 //    fills the data
 
 
 //???
       cotisation = new load.LoadCotisation().load(tarifMember, cotisation, club);
       boolean OK = false;
       if(cotisation.getPrice() != 0){
          LOG.info("amount non ZERO payment needed !");
          OK = CreditcardPayment(cotisation);  // le paiement par carte de crédit est exécuté correctement
      }else{
          String msg = "amount ZERO no payment needed !!";
          LOG.info(msg);
          showMessageInfo(msg);
       }
       /// si OK alors il faut enregistrer la cotisation et créer le membre
       /// si pas de paiement nécessaire ??
       
        // tester return ??
       if(OK){
           String msg = "Paiement par creditcard OK";
            LOG.info(msg);
            showMessageInfo(msg);
        }else{
           String msg = "paiement par creditcard KO : quelle conclusion ?";
            LOG.info(msg);
            showMessageInfo(msg);
        }
     LOG.info("after call to credidtcardpayment");
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
         return null;
 } //end method manageSubscripti 
  
  
  
  
 public String manageSubscription() throws Exception{ // called from subscription.xhtml
 try{
            LOG.info("entering manageSubcription ");
            LOG.info("Subscription = " + subscription.toString());
            LOG.info("Subscription subCode = " + subscription.getSubCode());
            String sub = subscription.getSubCode();
            
            List<Subscription> ls = new find.FindSubscription().subscriptionDetail(player ,conn);
      //       if(new find.FindSubscriptionStatus().subscriptionStatus(subscription, player, conn)){  //true
            subscription = ls.get(0);
            subscription.setSubCode(sub);
      //      subscription.setIdplayer(player.getIdplayer());
            LOG.info("first element of the list is " + subscription);
              // concerne le payment avec carte de crédit
        if(subscription.getSubCode().equals("TRIAL") && subscription.getEndDate().isAfter(LocalDate.now()) ){
        //    String msg = "Nothing to do : Trial and endate > currentdate";
            String msg =  LCUtil.prepareMessageBean("subscription.notrial") + subscription.getEndDate();
            LOG.info(msg);
            showMessageInfo(msg);
            return null; // retourne d'où il vient
        }        
     //         fills the data
 //    LOG.info("line 01");
        subscription = new load.LoadSubscription().load(subscription);
  //    LOG.info("line 02");   
        if(subscription.getPrice() != 0.0){
            CreditcardPayment(subscription);
        }else{
            modifySubscription();
            String msg="Trial accepted, your count is : " + subscription.getTrialCount() + " of a maximum of 5";
            LOG.info(msg);
            showMessageInfo(msg);
            return "login.xhtml?faces-redirect=true";
        }
     LOG.info("after call to credidtcardpayment");
 }catch (SQLException ex){
            String msg = "SQLException in manageSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }catch (Exception ex){
            String msg = "Exception in manageSubscription " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
         return null;
 } //end method manageSubscription

public String registereIDPlayer(){ // throws javax.smartcardio.CardException  // mod 03-12-2017
    try{
        LOG.info("entering register eID Player");
 //   HandleSmartCard hsc = new HandleSmartCard();
    return new HandleSmartCard().formatPlayer();
 //   }catch (javax.smartcardio.CardException ex){
 //           String msg = "Card Exception in registereIDPLayer " + ex;
 //       /    LOG.error(msg);
 //           showMessageFatal(msg);
 //           return null;
    }catch (Exception ex){
            String msg = "Exception in registereIDPLayer " + ex;
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
  }
}  // end method    

public void onRowToggle(ToggleEvent event) {
        FacesMessage msg = new FacesMessage(
                "Row State " + event.getVisibility()
        + " , Date Round : " + ((ECourseList) event.getData()).Eround.getRoundDate());  // mod 03-11-2018
     //       LOG.info(msg);
     //       showMessageInfo(msg);
    FacesContext.getCurrentInstance().addMessage(null, msg);
    }

public void rowPlayerSelect(SelectEvent event) { // used in selectOtherPlayers.xhtml
        // fonctionne ?
        LOG.info("entering rowPlayerSelect");
        String msg = "size selected players = " + player.getSelectedOtherPlayers().size();
        LOG.info(msg);
     //   showMessageInfo(msg);
        if (player.getSelectedOtherPlayers().size() > 4) {
  ///          player.getSelectedOtherPlayers().remove(event.getObject());
            msg = "You cannot selected more than 4 players";
            LOG.error(msg);
            showMessageFatal(msg);
        }
    }

public void loginAPI(){
    LOG.info("entering loginAPI coming from login_securityAPI.xhtml");
    LOG.info("username = " + login.getUsername());
    LOG.info("password = " + login.getPassword());
  }

@Inject
private ExternalContext ec;
public String login() throws IOException, SQLException {
        LOG.info("entering login coming from login.xhtml");
        // faire ici l'enregistrement du logout dans audit_in_out
      //  LCUtil.stopAuditLogin(Integer.toString(player.getIdplayer()));
    //    utils.Audit.stopAuditLogin(player, conn);
        listeners.MySessionCounter msc = new listeners.MySessionCounter(); 
      LOG.info("active sessions t this moment= " + msc.getActiveSessions());
    //  LOG.info("active sessions = " + msc.sessionCreated());
   
            LOG.info("browser language = " + ec.getRequestLocale());
            LOG.info("session buffersize = " + ec.getResponseBufferSize());
            LOG.info("session character encoding = " + ec.getRequestCharacterEncoding());
            LOG.info("session timeout = " + ec.getSessionMaxInactiveInterval() );
        reset("from login"); // new 28/09/2014
        player = new Player(); // new 28/09/2014
            LOG.info("from login : new player !!");
 ////       ec.invalidateSession();
 ////           LOG.info("session invalidated !! - going back to login.xhtml " );
    //    ec.redirect(ec.getRequestContextPath() + "/sessionExpired.xhtml?cmd=endSession");
 //    return "login_securityAPI.xhtml";  // back to login.xhtml
 
       sessionMapJSF23.put("playerid", "");
       sessionMapJSF23.put("playerlastname", "");
       sessionMapJSF23.put("playerage", 0);
 
     return null;
  //      return "login.xhtml?faces-redirect=true";  // old - doesn't work
    } // end method
//@Inject
//private ExternalContext ec;
public String logout(String lgt) throws IOException, SQLException {
        LOG.info("entering logout() for playerid = " + player.getIdplayer());
        LOG.info("entering with parameter = " + lgt);
        // faire ici l'enregistrement du logout dans audit_in_out
     if(player.getIdplayer() != null){
        modify.ModifyAudit ma = new modify.ModifyAudit();
        boolean ok = ma.stopAuditLogin(player, conn);
     }

        reset("from logout"); // new 28/09/2014
        player = new Player(); // new 28/09/2014
            LOG.info("exiting logout() !!!");
        ec.invalidateSession();
            LOG.info("session invalidated !! - staying on sessionExpired.xhtml " );
 ////   enlevé 29-10-2018    ec.redirect(ec.getRequestContextPath() + "/sessionExpired.xhtml?cmd=endSession");
// à àfaire : si 
    if(lgt.equals("from button Logout")){
          String msg = "You asked a logout";
          LOG.info(msg);
          showMessageInfo(msg);
        return "login.xhtml?faces-redirect=true";}
    if (lgt.equals("time-out from masterTemplate")){
          String msg = "Time-out for inactivity !";
          LOG.info(msg);
          showMessageFatal(msg);
          return null; //"sessionExpired.xhtml?faces-redirect=true";
    }else{
        return null;
    }
    
  //       // old - doesn't work
    } // end method

public String onFlowProcess(FlowEvent event) {
        LOG.info("Current wizard step:" + event.getOldStep());
        LOG.info("Next wizard step:" + event.getNewStep());
        if (skip) {
            skip = false;//reset in case user goes back
            return "confirm";
        } else {
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
        LOG.info("entering savePlayer !!");
        FacesMessage msg = new FacesMessage("Successful", "Welcome :" + player.getPlayerFirstName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public static boolean isPostback() {
        return FacesContext.getCurrentInstance().isPostback();
    }

    public void preRenderClub() throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        LOG.info("preRenderClub called");
        LOG.info("preRenderView  : idclub = " + club.getIdclub());
        PhaseId currentPhaseId = FacesContext.getCurrentInstance().getCurrentPhaseId();
        LOG.info("currentPhaseId 1 = " + currentPhaseId);
        LOG.info("isPostBack ? = " + isPostback());
        //    if(club.getIdclub()!= null)
        if ((!isPostback()) && (club.getIdclub() != null))
        //    postback = false
        {
            club = new Club();
            LOG.info("preRenderClub : club forced to null ");
        }
    }

    public void preRenderCourse() {
        LOG.info("preRenderCourse called");
    }

 public String CurrentTimeWithZoneOffset(){
  try{
        LOG.info("entering CurrentTimeWithZoneOffset with : " + player.getPlayerTimeZone().getTimeZoneId());
        
        LOG.info("playerid = " + player.getIdplayer());
        if(player.getPlayerZoneId() != null)
            return utils.LCUtil.getCurrentTimeWithZoneOffset(player.getPlayerZoneId());
        else
            return null;
        } catch (Exception e) {
            String msg = "££ Exception in CurrentTime ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }
    }  //end method
            

    // new 26/05/2017 used in inscription.xhtml : on n'affiche que les tee existants pour le course
    public List<String> getTeeStartListe() throws SQLException {
         LOG.info("entering getTeeStartListe = ..." );
//         LOG.info("course = " + course.toString());
 //        LOG.info("player = " + player.toString());
     try{   
         find.FindTeeStart fts = new find.FindTeeStart();
     //    teeStartListe = fts.teeStart(course, player, conn);
     //       LOG.info("TeeStartListe = " + teeStartListe);
     //       return teeStartListe;  // YELLOW, BLUE ...
            return fts.teeStart(course, player, conn);
      } catch (Exception e) {
            String msg = "££ Exception in getTeeStartListe ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }
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

public String getMdate()
{   // File Modification Date (affichée dans footer.xhtml pour views
    //log.info(" -- message from function : entering mdate");
    FacesContext ctx = FacesContext.getCurrentInstance();
    HttpServletRequest sr = (HttpServletRequest) ctx.getExternalContext().getRequest();
 //    LOG.info("servlet = " + Servlet.getServletConfig().ServletConfig.getServletContext() );
    String uri = sr.getRequestURI();
    File file = new File(Constants.AP_TARGET + uri);
    return "<b>Last modification :</b> " + SDF_TIME.format(file.lastModified()); // + " / " + file.getName() + file.getPath();
      //  + " for file = " + file.toString()
    //    + " for uri = " + uri;
} 
public void participantsMatchplay() throws SQLException{
    // for testing purposes 3/2/2015
    lists.ParticipantsMatchplay pmp = new lists.ParticipantsMatchplay();
    pmp.listAllParticipants(221, conn);
}

public void checkCaptcha(ActionEvent e){ 
       FacesContext.getCurrentInstance().addMessage(null, 
       new FacesMessage(FacesMessage.SEVERITY_INFO, "Your Captcha Code Is Correct !",null)); 
} 
// new 09/06/2015

public static class TeeStart{
	public String teeLabel;
	public String teeValue;
 
public TeeStart(String teeLabel, String teeValue){
            this.teeLabel = teeLabel;
            this.teeValue = teeValue;
		}
 
public String getTeeLabel(){
		return teeLabel;
		}
 
public String getTeeValue(){
		return teeValue;
		}
 	} // end class Color
private TeeStart[] teeList;


       public List<ECourseList> listStableford() {
     //   LOG.info(" -- listScramble - entry index = " );
        return listStableford;
    } //end method
  /* enlevé 29-11-2018 à réécrire    
     public List<CEcourseList> listScramble1() {
      //  LOG.info(" -- listScramble1 - entry index = ");
      //       listScramble1 = listScramble.subList(0, 1);// 1ère ligne uniquement
      //       LOG.info(" -- subList listScramble1 = " + Arrays.toString(listScramble1.toArray() ) );
      // subList Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive
            return  listScramble.subList(0, 1); 
    } //end method
*/    
   public List<ECourseList> listStableford1() {  // uniquement la 1ére ligne
      //  LOG.info(" -- listScramble1 - entry index = ");
      //       listScramble1 = listScramble.subList(0, 1);// 1ère ligne uniquement
      //       LOG.info(" -- subList listScramble1 = " + Arrays.toString(listScramble1.toArray() ) );
      // subList Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive
      LOG.info("listStableford pour cancelInscription = " + listStableford.size());
      if (listStableford.size() > 0){
        return  listStableford.subList(0, 1);
      }else{
        return null;  
      }     
    } //end method
     
     
 public void startTask(ActionEvent ae) {
        LOG.info("starting startTask ActionEvent ");
      executorService = Executors.newSingleThreadExecutor();
      executorService.execute(this::startCreditcardProgressBar);
       LOG.info("exiting startTask");
  }

private void startCreditcardProgressBar() {  // confirmation du paiement !!!
try{
      LOG.info("starting startLongTask");
      progressInteger.set(0);
      for (int i = 0; i < 100; i++) {
          progressInteger.getAndIncrement();  //simulating interaction with creditcard company
          try {
              Thread.sleep(ThreadLocalRandom.current().nextInt(1, 100));
          } catch (InterruptedException e) {
              LOG.error("Exception in progressInteger loop :" + e);
          }
      }
      LOG.info("progressInteger is now = " + progressInteger.get());
        LOG.info("ending startLongTask");
     if(progressInteger.get() == 100){
        //  String s = handlePayments();
          LOG.info("100% atteint donc paiement est exécuté = " );
                     
      //    FacesContext fc = FacesContext.getCurrentInstance();
      //    ExternalContext eco = fc.getExternalContext();
      //    Flash flash = eco.getFlash();
      //    flash.setKeepMessages(true);
      //    eco.dispatch("login.xhtml?faces-redirect=true");
          // ici rediriger vers login.xhtml ??
      }else{
         LOG.info("abnormal situation on progressinteger = " + progressInteger.get());
         creditcard.setPaymentOK(false); // le paiement n'est pas exécuté!
     }
      executorService.shutdownNow();
      executorService = null;
  } catch (Exception e) {
            String msg = "££ Exception in startLongTask ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
       //     return null; // indicates that the same view should be redisplayed
        } finally {

        }
    } //end method

public String handlePayments() {
try{
            LOG.info("entering handlePayments");
            LOG.info("creditcard payment executed !");
       UUID reference = UUID.randomUUID();
       creditcard.setReference(reference.toString());
       creditcard.setPaymentOK(true); // le paiement est exécuté!
            LOG.info("creditcard typePayment = " + creditcard.getTypePayment());
       if(creditcard.getTypePayment().equals("SUBSCRIPTION")){
           subscription.setPaymentReference(creditcard.getReference());
           modifySubscription();
           return "login.xhtml?faces-redirect=true";
       }
       if(creditcard.getTypePayment().equals("COTISATION")){
           cotisation.setPaymentReference(creditcard.getReference());
      //     boolean OK = createCotisation(); // dans courseC
           if(createCotisation()){
              String msg = "Create Cotisation SUCCESS"; 
              LOG.info(msg);
              showMessageInfo(msg);
              return "login.xhtml?faces-redirect=true";
           }else{
               String msg = "Create Cotisation FAILED";
               LOG.error(msg);
               showMessageFatal(msg);
               return null;
           }
       }
       if(creditcard.getTypePayment().equals("GREENFEE")){
           greenfee.setPaymentReference(creditcard.getReference());
     //      boolean OK = createCotisation(); // dans courseC
        //   boolean OK = false;
           if(createCotisation()){
              String msg = "Create Greenfee SUCCESS"; 
              LOG.info(msg);
              showMessageInfo(msg);
              return "login.xhtml?faces-redirect=true";
           }else{
               String msg = "Create Greenfee FAILED";
               LOG.error(msg);
               showMessageFatal(msg);
               return null;
           }
       }

       
       
       
 } catch (Exception e) {
            String msg = "££ Exception in handlePayments or CreateCotisation ... = " + e.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
           return null; // indicates that the same view should be redisplayed
        } finally {

        }
return null;
    } //end method
  public int getProgress() {
      return progressInteger.get();
  }

  
    public void showClubDetail(ECourseList ecl){
        LOG.info("entering showClubDetail");
        LOG.info("with ecl = "+ ecl);
        club = ecl.Eclub;
    Map<String,Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", false);
    options.put("resizable", true);
    options.put("width", 900);
    options.put("height", 300);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("closable", true); // closed by a button
    options.put("includeViewParams", true); 
    Map<String, List<String>> params = new HashMap<>(); 
    List<String> values = new ArrayList<>(); 
    values.add("!! This is a param bookName"); 
    params.put("bookName", values); 
 //       LOG.info("current club = " + CourseController.getClub().getIdclub());

    PrimeFaces.current().dialog().openDynamic("dialogClubDetail.xhtml", options, params);
        LOG.info("dialogClubDetail.xhtml is opened !");
}
  
  
  
  
   public void main(String args[]) throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        //    allNull(cl);
        LOG.info(" -- main terminated");
    } // end main
} // end class