package lc.golfnew;


import SmartCard.HandleSmartCard;
import calc.CalcStablefordPlayingHandicap;
import calc.CalcTarifGreenfee;
import create.*;
import delete.DeleteClub;
import delete.DeleteCourse;
import entite.*;
import exceptions.LCCustomException;
import exceptions.TimeLimitException;
import find.FindPassword;
import find.FindTarifData;
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
import java.time.ZoneId;
import java.util.*;
//import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lists.ScramblePlayersList;
import modify.*;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.*;
//import org.primefaces.model.chart.BarChartModel;
//mport org.primefaces.model.chart.CartesianChartModel;
//import org.primefaces.model.chart.LineChartModel;
//import org.primefaces.model.map.Circle;
//import org.primefaces.model.map.DefaultMapModel;
//import org.primefaces.model.map.MapModel;
//import org.primefaces.model.map.Marker;
//import org.primefaces.model.map.Overlay;
import utils.*;
@Named("courseC") // this qualifier  makes a bean EL-injectable (Expression Language)
@SessionScoped

public class CourseController implements Serializable, interfaces.GolfInterface, interfaces.Log
{
     private @Inject Club club;
     @Inject private Car car;
     @Inject private Course course; 
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
     @Inject private ScoreCard scorecard;
     @Inject private PlayerHasRound inscription;
     @Inject private Old_Matchplay matchplay;
     @Inject private Subscription subscription;
     @Inject private HolesGlobal holesGlobal;  // new 13/08/2017
     @Inject private Flight flight;
     @Inject private Tarif tarif;
     @Inject private Creditcard creditcard;
     

    private ClubCourseRound clubcourseround = new ClubCourseRound();
    private final static List<Integer> STROKEINDEX = new ArrayList<>();
    private final static List<Integer> NUMBERS = new ArrayList<>();
    private static List<ClubCourseRound> listccr;
 ///   private static List<ClubCourseRound> listcourse = null; mod 03/08/2014
    private ClubCourseRound selectedCourse;
    private ECourseList selectedHandicap; // mod 11-03-2018
 //   private ClubCourseRound selectedHandicap;
    private ECourseList selectedPlayedRound; // mod 11/03/2018
    private final static List<SelectItem> GAMES = new ArrayList<>();
    private final static List<SelectItem> START = new ArrayList<>();
    private static String[] games = null; 
//    private static String[] start = null; 
    private static int[] parArray = null;
    private String parArrayString = "";
    
//    private static int[][] dataHoles = null;
    
    private static String otherGame = null; // new 15/06/2014
    private SelectItem[] gameOptions; // new 01/12/2013
 //   private Status status; // new 20/02/2014
    private static String[] seasons = null; 
    private SelectItem[] seasonOptions; // new 01/12/2013
    private final static List<SelectItem> LANGUAGES = new ArrayList<>();
    private final static List<Integer> VALUES = new ArrayList<>();
   // private static String[] sc1;
    private static String[][] sc2;
 //   private final List<Player> listplayer = null; // mod 31/08/2014
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
    
    private boolean NextPanelPassword = false;  // 16/11//2013
 //   private CartesianChartModel linearModel;
 //   private CartesianChartModel barModel;
    private List filteredCars; // ne pas supprimer :Â©cessaire depuis Primefaces 3.4 (faut une List) 
    private List filteredCourses; // new 03/08/2014
    private ArrayList<Flight> filteredFlights;
    private List filteredPlayers; // new 03/08/2014
    private List filteredHandicaps; // new 03/08/2014
    private List filteredPlayedRounds; // new 03/08/2014
    private List filteredInscriptions; // new 03/08/2014
    // le nom de la liste vient d'un exemple ...
    private List<ClubCourseRound> filteredClubCourseRound;
    private List<ClubCourseRound> listmatchplay;
    private List<ClubCourseRound> listScramble;
    private List<ECourseList> listStableford;
    private Player selectedPlayer;
    
    private List<Player> selectedPlayersMatchPlay; // new 31/08/2014
  //  private List<Player> selectedOtherPlayers; // new 30/06/2017
    private DualListModel<Player> dlPlayers; // new 03/09/2014
//    private List<Player> playersTarget; // new 03/09/2014
    List<Player> playersTarget = new ArrayList<>();
    List<Player> subscriptionRenewal;
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
    private String inputSelectCourse = null; // new 01/04/2016
    private String inputSelectCourse2 = null; // new 16/09/2018
    
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
    private String radioButtonJSF;
    private String createModifyPlayer = "C";  // utilisé pour choisir player.xhtml ou player_modify.xhtml
    private String SunRiseSet; // = null; 
    private AtomicInteger progressInteger = new AtomicInteger();
    private ExecutorService executorService;
    private String uuid; // new 14/08/2018
   private String emoji;
   private int cptCourse = 0;
   
    public CourseController()  // constructor
    {
        this.listavg = null;
        this.setSunRiseSet(null);
    }

 @PostConstruct
 public void init() // attention !! ne peut absolument pas avoir : throws SQLException
    {
    try{
    //        LOG.info("new");
           conn = lc.golfnew.PostStartupBean.getConn();
           LOG.info("cette connection database sera utilisée pendant toute la session = "+ conn);
            
  //         conn = lc.golfnew.PostStartupBean.getConn2();
  //         LOG.info("cette connection database sera utilisée pendant toute la session = "+ conn);
           
           
  //    LOG.info("connection from poststartupbean = " + conn);
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
        if (LANGUAGES.isEmpty())
        {
            LANGUAGES.add(new SelectItem("en", "English"));  //first field = itemValue, stocke dans DB
            LANGUAGES.add(new SelectItem("de", "German"));
            LANGUAGES.add(new SelectItem("fr", "Français"));
            LANGUAGES.add(new SelectItem("nl", "Nederlands"));
        }
             //   LOG.info("LANGUAGES initialized" + LANGUAGES.toString() );//Arrays.toString(games)/
              //  LOG.info("LANGUAGES initialized" + Arrays.toString(LANGUAGES.toArray()));
              
        if (GAMES.isEmpty())
        {
            GAMES.add(new SelectItem("STABLEFORD", "Stableford"));  //first field = itemValue, stockÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©e dans DB
            GAMES.add(new SelectItem("SCRAMBLE", "Scramble"));
            GAMES.add(new SelectItem("CHAPMAN", "Chapman"));
            GAMES.add(new SelectItem("STROKEPLAY", "Strokeplay"));
            GAMES.add(new SelectItem("ZWANZEURS", "Zwanzeurs"));
            GAMES.add(new SelectItem("MP_FOURBALL", "Fourball"));
            GAMES.add(new SelectItem("MP_FOURSOME", "Foursome"));
            GAMES.add(new SelectItem("MP_SINGLE", "Single"));
                LOG.info("GAMES initialized" + GAMES.toString() );
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
              //   LOG.info("GAMES initialized" + Arrays.toString(GAMES.toArray()));
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
  if (games == null) 
     {
         games = new String[8];
         // public enum GameType {STABLEFORD,SCRAMBLE,CHAPMAN,STROKEPLAY,ZWANZEURS,MP_FOURBALL,MP_FOURSOME,MP_SINGLE}
         games[0]="STABLEFORD";
         games[0]=Round.GameType.SCRAMBLE.toString();
         games[1]="ZWANZEURS";
         games[2]="SCRAMBLE";
         games[3]="CHAPMAN";
         games[4]="STROKEPLAY";
         games[5]="MP_FOURBALL";
         games[6]="MP_FOURSOME";
         games[7]="MP_SINGLE";
   //              LOG.info("games initialized = "+ Arrays.toString(games) );
      }
       gameOptions = createFilterOptions(games); // used in show_played_rounds.xhtmlnew 01/12/2013
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
  InputStream istr = clo.getResourceAsStream("version_components.properties");
  Properties prop = new Properties();
  prop.load(istr);
  String value = prop.getProperty("message.test3", "message.test3 not found");
  LOG.info("Value from version_components.properties : " + value);
//  LOG.info("Value from System.getProperty : " + System.getProperty("primefaces.version"));

  istr = clo.getResourceAsStream("myPOM.properties");
  Properties prop1 = new Properties();
  prop1.load(istr);
  String version = prop1.getProperty("primefaces.version");
//  LOG.info("Value from properties : " + value2);
  LOG.info("Primefaces version from myPOM.properties: " + version); 
  
  FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        Enumeration e = session.getAttributeNames();
    while (e.hasMoreElements())
    {
     String attr = (String)e.nextElement();
        if(attr.equals("facelets.ui.DebugOutput")) continue;  // print view !!
     
        LOG.info("   session   attr  = "+ attr);
        Object value2 = session.getAttribute(attr);
        LOG.info("   session   value = "+ value2);
    }
  ServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
  HttpServletResponse res = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
  
    LOG.info("we start a reset for all the working fields of the new session ");
  reset("from init in CourseController");
 //  LOG.info("leaving " + this.getClass().getSimpleName() + " Postconstruct init()");
   LOG.info("NEW session just started !! " + NEW_LINE);
 //  utils.MySessionCounter msc = new utils.MySessionCounter();
 //  LOG.info("The are activeSessions = : " + msc.getActiveSessions()); 

 } catch (Exception e){
            String msg = "££ Exception in creating Connection or init in courseC = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
     }
  } //end method init

    @PreDestroy
    public void exit() {
        LOG.info(" ------------------ from CourseController PreDestroy exit ()...");
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
        LCUtil.showMessageFatal(msg);
  //      String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
        throw new ValidatorException(new FacesMessage(msg));
    }
 }  //end try ValidatorException
 catch (ValidatorException ve)
     {
            String msg = "ValidatorException = " + ve.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
         } 
 catch (NullPointerException npe)
     {
            String msg = "NullPointerException = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
         } 
}

    public Tarif getTarif() {
        return tarif;
    }

    public void setTarif(Tarif tarif) {
        this.tarif = tarif;
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

    public boolean isNextPanelPassword() {
        return NextPanelPassword;
    }

    public void setNextPanelPassword(boolean NextPanelPassword) {
        this.NextPanelPassword = NextPanelPassword;
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

    public PlayerHasRound getInscription() {
        return inscription;
    }

    public void setInscription(PlayerHasRound inscription) {
        this.inscription = inscription;
    }

    public  Flight getFlight() {
        return flight;
    }

      public void selectFlightFromDialog(Flight flight) throws IOException{
      LOG.info("entering selectflightfromdialog");
      LOG.info("entering selectflightfromdialog with source = " + flight.toString() );
      this.flight = flight;
     
        LOG.info("after setflight, flight = " + flight.getFlightStart() );
           LOG.info("after setflight, flight HHMM = " + Constants.dtf_HHmm.format(flight.getFlightStart()) );
     round.setWorkHour(Constants.dtf_HHmm.format(flight.getFlightStart() ));
     round.setRoundDate(flight.getFlightStart());
     LOG.info("getRoundDate = " + round.getRoundDate());
     LOG.info("getWorkHour = " + round.getWorkHour());
     DialogController.closeDialog("dialogFlight.xhtml");
  //   DialogController.closeFlightFromDialog(flight);
 //  PrimeFaces.dialog().closeDynamic("dialogFlight.xhtml");
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

     public String getInputSelectCourse() {
    //     LOG.info("getted InputSelectCourse = " + inputSelectCourse);
        return inputSelectCourse;
    }

    public void setInputSelectCourse(String inputSelectCourse) {
    //      LOG.info("setted InputSelectCourse = " + inputSelectCourse);
        this.inputSelectCourse = inputSelectCourse;
    }

    public String getInputSelectCourse2() {
        return inputSelectCourse2;
    }

    public void setInputSelectCourse2(String inputSelectCourse2) {
        LOG.info("setted InputSelectCourse 2 = " + inputSelectCourse2);
        this.inputSelectCourse2 = inputSelectCourse2;
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

    public void setInputPlayedRounds(String inputPlayedRounds)
    {
        this.inputPlayedRounds = inputPlayedRounds;
            LOG.info("setInput (new played list) = " + inputPlayedRounds);
        if (inputPlayedRounds.equals("ini"))
        {
                LOG.info(" -- Listcontroller/listround, filteredCars  set to null ! ");
            lists.__RoundList.setListe(null);  //lazy loading forced !!
            filteredCars = null; // new 15/12/2013
          //  gamesOptions = null; // new 15/12/2013
                LOG.info(" -- Listcontroller/listround = " + lists.__RoundList.getListe() );
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
    
/* remplacÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© 14/12/2014 par to_selectMpatchplayRounds_xhtml
    public void setInputScore(String inputScore) // new 14/07/2013
        {
     //   this.inputScore = inputScore;
        LOG.info("setInput (new score !) = " + inputScore);
        this.inputScore = inputScore;
        if (inputScore.equals("matchplay")) {
            
            club = new Club();
            course = new Course();
            round = new Round();
            scoreStableford = new ScoreStableford();
         //   ListController.setListplayed(null);
            lists.PlayedList.setListe(null);
            lists.RoundList.setListe(null);  //lazy loading forced !!
            setShowButtonCreateStatistics(false);
            filteredCars = null; // new 15/12/2013
        }
    }
*/
    public String getLastSession() throws SQLException {
        LOG.info("before last session");
        return sdf_timeHHmm.format(utils.LCUtil.getLastAuditLogin(player, conn));
     //   LOG.info("last login was : " + )
    }

    public int getDeletePlayer() {
        return deletePlayer;
    }

    public void setDeletePlayer(int deletePlayer) {
        this.deletePlayer = deletePlayer;
    }

    public void setStartSession() throws SQLException {
       // LCUtil.startAuditLogin(Integer.toString(player.getIdplayer()));
        //LCUtil.startAuditLogin(player, conn);
        utils.Audit.startAuditLogin(player, conn); // mod 20/12/2014
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

    public ScoreCard getScorecard() {
        return scorecard;
    }

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
  //      creditcard.setPaymentOK(b);
    //    LOG.info("ShowButtonCreateStatistics setted to : " + ShowButtonCreateStatistics);
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

    public List getFilteredInscriptions() {
        return filteredInscriptions;
    }

    public void setFilteredInscriptions(List filteredInscriptions) {
        this.filteredInscriptions = filteredInscriptions;
    }

    public List getFilteredCourses() {
        return filteredCourses;
    }

    public void setFilteredCourses(List filteredCourses) {
        this.filteredCourses = filteredCourses;
    }

    public List getFilteredPlayedRounds() {
        return filteredPlayedRounds;
    }

    public void setFilteredPlayedRounds(List filteredPlayedRounds) {
        this.filteredPlayedRounds = filteredPlayedRounds;
    }

    public List getFilteredCars() {
     //   LOG.info("from getFilteredCars = " + filteredCars);
        return filteredCars;
    }



    public List getFilteredPlayers() {
        return filteredPlayers;
    }

    public void setFilteredPlayers(List filteredPlayers) {
        this.filteredPlayers = filteredPlayers;
    }

    public List getFilteredHandicaps() {
        return filteredHandicaps;
    }

    public void setFilteredHandicaps(List filteredHandicaps) {
        this.filteredHandicaps = filteredHandicaps;
    }

    public ECourseList getSelectedHandicap() {
        return selectedHandicap;
    }

    public void setSelectedHandicap(ECourseList selectedHandicap) {
        this.selectedHandicap = selectedHandicap;
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
                            //+ selectedPlayersMatchPlay.size() );
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

    public void setFilteredCars(List filteredCars) {
        LOG.info("from setFilteredCars = " + filteredCars);
        this.filteredCars = filteredCars;
    }

    public Old_Matchplay getMatchplay() {
        return matchplay;
    }

    public void setMatchplay(Old_Matchplay matchplay) {
        this.matchplay = matchplay;
    }

    public List<ClubCourseRound> getFilteredClubCourseRound()
    {
            LOG.info("from getFilteredClubCourseRound = " + filteredClubCourseRound);
        return filteredClubCourseRound;
    }

    public void setFilteredClubCourseRound(List<ClubCourseRound> filteredClubCourseRound)
    {
        this.filteredClubCourseRound = filteredClubCourseRound;
            LOG.info("from setFilteredClubCourseRound = " + this.getFilteredClubCourseRound() );
    }

    public void setScorecard(ScoreCard scorecard) {
        this.scorecard = scorecard;
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

    public ClubCourseRound getClubcourseround() {
        return clubcourseround;
    }

    public void setClubcourseround(ClubCourseRound clubcourseround) {
        this.clubcourseround = clubcourseround;
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

    public List<ClubCourseRound> getListmatchplay() {
        return listmatchplay;
    }

    public void setListmatchplay(List<ClubCourseRound> listmatchplay) {
        this.listmatchplay = listmatchplay;
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
        LOG.info("starting getMapModel ...");
        LOG.info("club = "+ club.toString());
     if (club.getIdclub() == null){
         LOG.info("clubIdclub == null");
         return mapModel;
     }
     if(mapModel == null)
     {    
         LOG.info("getMapModel is null !");
        mapModel = new DefaultMapModel();  
       org.primefaces.model.map.LatLng latlng = new org.primefaces.model.map.LatLng(club.getClubLatitude().doubleValue(), club.getClubLongitude().doubleValue() );
       Marker marker = new Marker(latlng, "title : " + club.getClubName() );  // affiche nom du club dans marker
 //      marker.setTitle("This is the marker title");
       mapModel.addOverlay(marker);
       
       Circle circle = new Circle(latlng, 200);
       circle.setFillColor("green");
       circle.setFillOpacity(0.5);
       circle.setStrokeColor("#00ff00");
       circle.setStrokeOpacity(0.7);
       mapModel.addOverlay(circle);
       
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

public String findHomeClub() //used in player.xhtml
{       LOG.info("entering findHomeClub " );
   try{     
       // explication !! on profite du button "HomeClub" pour cherhcer les coordiantes du player = chipotage !!!
 //      player = find.FindPlayerCoordinates.findPlayerLatLngTz(player);
     //   return "club.xhtml?faces-redirect=true";
       return "selectHomeClub.xhtml?faces-redirect=true";
  
     }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findHomeClub = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
           // return null;
        }
}
public String findClubLatLng() //used in player.xhtml
{       LOG.info("entering findClubLatLng " );
   try{     
       find.FindClubCoordinates fcc = new find.FindClubCoordinates();
       club = fcc.findClubLatLngTz(club);
        return "club.xhtml?faces-redirect=true";
    
     }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubLatLng " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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
            FacesContext.getCurrentInstance().getExternalContext().redirect(red);
            return null;
   }catch (Exception e){
            String msg = "Â£ Exception in CourseController - findClubWebsite = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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
        LOG.info("creditcardNumber OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("creditcardNumber NewValue = " + valueChangeEvent.getNewValue());
    creditcard.setCreditCardNumber(valueChangeEvent.getNewValue().toString() );
}

public void creditCardTypeListener(ValueChangeEvent valueChangeEvent) {
        LOG.info("creditcardType OldValue = " + valueChangeEvent.getOldValue());
        LOG.info("creditcardType NewValue = " + valueChangeEvent.getNewValue());
    creditcard.setCreditCardType(valueChangeEvent.getNewValue().toString() );
    if( !creditcard.getCreditCardIssuer().equals(creditcard.getCreditCardType())){   // issuer = calculated  cardType = input data
        String msg = "WARNING !!! "
                + " <br/> Issuer detected = " + creditcard.getCreditCardIssuer()
                + " <br/> Card Type data in = " + creditcard.getCreditCardType();

        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
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
   public String pickListPlayers() throws SQLException {  //new 03/09/2014, mod 30/06/2017
        LOG.info("starting picklistPlayers = ");
        LOG.info("selectedOtherPlayers  = " + player.getSelectedOtherPlayers().toString());
        LOG.info("there are ?? new inscriptions = " + player.getSelectedOtherPlayers().size());
        if(player.getSelectedOtherPlayers().size() == 3){
            String msg = "Third and last player";
            LCUtil.showMessageInfo(msg);
   };
 //       List<Player> sortedPlayers = Collections.sort(selectedPlayersMatchPlay); // new 04/09/2014
        dlPlayers = new DualListModel<Player>(player.getSelectedOtherPlayers(), playersTarget);
        LOG.info("after dlPlayers getSource 1 = " + dlPlayers.getSource().toString());
 //       LOG.info("after dlPlayers getTarget 1 = " + dlPlayers.getTarget().toString());
  //return "picklistPlayers.xhtml?faces-redirect=true&cmd=bid&operation=create";
        return "inscriptions_other_players.xhtml?faces-redirect=true";
    }
    
    public String createOtherPlayers() throws SQLException {  //new 03/09/2014, mod 30/06/2017
        LOG.info("starting createOtherPlayers = ");
  //      LOG.info("List for players = " + playersTarget.toString());
        LOG.info("after dlPlayers getSource 2 = " + dlPlayers.getSource().toString()); // colonne de gauche
        LOG.info("after dlPlayers getTarget 2 = " + dlPlayers.getTarget().toString()); // colonne de droite
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
   

// new 16/12/2012
    public List<ClubCourseRound> getList() {
        return listccr;
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
    
    public String selectHomeClub(ECourseList ecl) { 
        LOG.info(" selected Home Club = " + ecl.toString());
        player.setPlayerHomeClub(ecl.Eclub.getIdclub()); // nw 09/02/2013
            LOG.info("setted HomeClub = " + player.getPlayerHomeClub());
        club.setClubName(ecl.Eclub.getClubName());
            LOG.info("setted ClubName = " + club.getClubName());
        String msg = "Select Club Successfull = "
                + " <br/> Club name = " + club.getClubName() // + " <br/> Course name = " + course.getCourseName()
                ;
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        // tester sur createmodifypalyer
        if(createModifyPlayer.equals("M"))
        {   LOG.info("This is a modification player = " + createModifyPlayer);
            return "player_modify.xhtml?faces-redirect=true";
        }else{
            LOG.info("This is a creation player = " + createModifyPlayer);
            return "player.xhtml?faces-redirect=true";} 
    //    return null; // mod 06-04-2017 aussi player-modify est possible
    } // end selectClub
/*
    public String deleteClub(ClubCourseRound clubcourseround) {   //select Homeclub
        LOG.info(" deleteClub = " + clubcourseround);
        this.clubcourseround = clubcourseround;
            LOG.info("initial Club = " + club.getIdclub());
        club.setIdclub(clubcourseround.getIdclub());
            LOG.info("setted idClub = " + club.getIdclub());
        club.setClubName(clubcourseround.getClubName());
            LOG.info("ClubName = " + club.getClubName());
        String msg = "Select Club Successfull = "
                + " <br/> Club name = " + club.getClubName() // + " <br/> Course name = " + course.getCourseName()
                ;
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        return "club.xhtml?faces-redirect=true&cmd=bid&operation=delete";
    } // end selectClub
*/
    public String selectCourse(ECourseList ecl) // mod 28/07/2017
    { 
    try {
            LOG.info(" entering select Course(ECourseList) ... = ");
            LOG.info(" select Course = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        String msg = "Select Course Successfull = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        
         LOG.info("selectCourse, inputSelectCourse2 = " + getInputSelectCourse2());
///// workaround temporaire 10-02-2018        
        setInputSelectCourse(getInputSelectCourse2()); // restauration
         if(getInputSelectCourse()== null){
                msg = "No InputSelectCourse !";
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                setInputSelectCourse("CreateRound");
         }
         LOG.info("selectCourse, inputSelectCourse TESTORED !! = " + getInputSelectCourse());
         
        if (getInputSelectCourse().equals("CreateRound")) {
            return "round.xhtml?faces-redirect=true&cmd=round";} // mod 30/07/2014
        
        if (getInputSelectCourse().equals("ini")) {
            return "round.xhtml?faces-redirect=true&cmd=ini";} // mod 30/07/2014
        
        if (getInputSelectCourse().equals("CreateTarif")) {
            return "tarif_menu.xhtml?faces-redirect=true";} // mod 16-09-2018
        
        if (getInputSelectCourse().equals("ChartCourse")) {
            return "statChartCourse.xhtml?faces-redirect=true";} // mod 01/04/2016
     //   }
        } catch (Exception e) {
            String msg = "££ Exception in selectCourse = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
    return null;
    } // end class selectcourse
    
public String selectTravel(ECourseList CCR) // mod 28/07/2017
    { 
    try {
        LOG.info(" entering select Travel ...");
            LOG.info(" select Travel with clubcourseround = " + CCR.toString());
     ////    this.clubcourseround = CCR;
  //          LOG.info("selectCourse, inputStat  = " + getInputStat());
 //           LOG.info("selectCourse, inputRound = " + getInputRound());
        club.setIdclub(CCR.Eclub.getIdclub());
            LOG.info("setted idClub = " + club.getIdclub());
        club.setClubName(CCR.Eclub.getClubName());
            LOG.info("setted ClubName = " + club.getClubName()); // pour trajet map
        club.setClubCountry(CCR.Eclub.getClubCountry());
            LOG.info("setted Travel ClubCountry = " + club.getClubCountry());
        club.setClubCity(CCR.Eclub.getClubCity());  // new 14/08/2015 pour trajet
            LOG.info("setted Travel ClubCity = " + club.getClubCity());
        club.setClubAddress(CCR.Eclub.getClubAddress());  // new 14/08/2015 pour trajet
            LOG.info("setted Travel ClubAddress = " + club.getClubAddress());
            
        course.setIdcourse(CCR.Ecourse.getIdcourse());
            LOG.info("setted idCourse = " + course.getIdcourse());
        course.setCourseName(CCR.Ecourse.getCourseName());
            LOG.info("setted CourseName = " + course.getCourseName());
        course.setCourseBegin(CCR.Ecourse.getCourseBegin());
            LOG.info("setted CourseBegin = " + course.getCourseBegin());
        course.setCourseEnd(CCR.Ecourse.getCourseEnd());
            LOG.info("setted CourseEnd = " + course.getCourseEnd());    
  //      round.setIdround(clubcourseround.getIdround());
  //          LOG.info("setted IdRound = " + round.getIdround());
        String msg = "Select Course Successfull = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        // à vérifier 
          LOG.info("getInputSelectCourse = " + getInputSelectCourse() ); // à vérifer
//        if (getInputSelectCourse().equals("ChartCourse")) {
            return "maps_home_club.xhtml?faces-redirect=true"; //?cmd=Rnd"; // mod 30/07/2014
//        } else {
//            String s = "statChartRound.xhtml?faces-redirect=true";
//            LOG.info("return = " + s);
//            return s;
//        }
    } catch (Exception e) {
            String msg = "££ Exception in selectTravel = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
    }
    } // end class selectTravel
 
  public String selectChart(ClubCourseRound CCR, String cmd)
  { try {
            LOG.info("starting selectChart !! = " );
            LOG.info("starting selectChart with parameter 1 = " + CCR.toString());
            LOG.info("starting selectChart with parameter 2 cmd = " + cmd);
         this.clubcourseround = CCR;
        club.setIdclub(clubcourseround.getIdclub());
            LOG.info("setted idClub = " + club.getIdclub());
        club.setClubName(clubcourseround.getClubName());
            LOG.info("setted ClubName = " + club.getClubName());
        club.setClubWebsite(clubcourseround.getClubWebsite());
            LOG.info("setted ClubWebSite = " + club.getClubWebsite());
        club.setClubCity(clubcourseround.getClubCity());  // new 14/08/2015 pour trajet
            LOG.info("setted ClubCity = " + club.getClubCity());
        club.setClubAddress(clubcourseround.getClubAddress());  // new 14/08/2015 pour trajet
            LOG.info("setted ClubAddress = " + club.getClubAddress());    
        course.setIdcourse(clubcourseround.getIdcourse());
            LOG.info("setted idCourse = " + course.getIdcourse());
        course.setCourseName(clubcourseround.getCourseName());
            LOG.info("setted CourseName = " + course.getCourseName());
        course.setCourseBegin(clubcourseround.getCourseBeginDate());
            LOG.info("setted CourseBegin = " + course.getCourseBegin());
        course.setCourseEnd(clubcourseround.getCourseEndDate());
            LOG.info("setted CourseEnd = " + course.getCourseEnd());    
        round.setIdround(clubcourseround.getIdround());
            LOG.info("setted IdRound = " + round.getIdround());
        String msg = "Select Course Successfull = "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> Course name = " + course.getCourseName();
            LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        // à vérifier 
          LOG.info("getInputSelectCourse = " + getInputSelectCourse() ); // à vérifer
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
            LCUtil.showMessageFatal(msg);
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
            LOG.info("continuing CourseBarModel with getInputSelectCourse = " + getInputSelectCourse());
            LOG.info("continuing CourseBarModel with getInputPlayedRounds = " + getInputPlayedRounds());
            charts.ChartsBarModel cm = new charts.ChartsBarModel();
          return cm.getBarModel(conn, player, course, round, "course"); // round is not used
   }catch (SQLException e){
            String msg = "CourseController - getBarModel : £££ Exception = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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
            LCUtil.showMessageFatal(msg);
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
            LCUtil.showMessageFatal(msg);
            return null;
        } 
} // end method RoundBarModel

public void checkMail(String ini)
{
        LOG.info("starting checkMail with : " + ini);
///    utils.CheckingMails.main(ini); // argument bidon !!
        LOG.info("ending checkMail with : " + ini);
}
// new 14/08/2014, called from menu
public String reset(String ini) // new 14/08/2014, called from menu        
{
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
    lists.__RoundList.setListe(null);
    lists.ScoreCard1List.setListe(null);
    lists.CourseList.setListe(null); // new 28/07/2017
    
    lists.ScoreCard3List.setListe(null);
//        LOG.info("ending  initialize lists = " + ini);
    find.FindSlopeRating.setListe(null);  // new 22/6/2015
    find.FindSubscription.setListe(null); // new 04/02/2017
    SunriseSunsetApiController.setListe(null); // new 09/05/2017
    ScramblePlayersList.setListe(null); // new 16/06/2017
 //2. entites
    if(ini.equals("non_belgian"))
        {   LOG.info("starting reset with non_belgian : " + ini);
            player = new Player();
            LOG.info("new player : " + player);
        }
    club = new Club();
    clubcourseround = new ClubCourseRound();
    course = new Course();
    handicap = new Handicap();
    hole = new Hole();
    matchplay = new Old_Matchplay();
    // player = no
    inscription = new PlayerHasRound();
    round = new Round();
    scorecard = new ScoreCard();
    scoreMatchplay = new ScoreMatchplay();
    scoreStableford = new ScoreStableford();
    scoreScramble = new ScoreScramble(); // new 26/07/2015
    tee = new Tee();
    subscription = new Subscription(); // new 01/02/2017
    playingHcp = new PlayingHcp(); // new 16/06/2017
    tarif = new Tarif(); // new 18/03/2018
    creditcard = new Creditcard();
    holesGlobal = new HolesGlobal();
 //      LOG.info("ending initialize entites , param = " + ini);
    return "reset OK "; // on retourne d'où on vient
     }catch (Exception ex){
            LOG.error("error in reset ! " + ex);
            LCUtil.showMessageFatal("Exception reset  = " + ex.toString());
            return "error";
        }
}

public void createCourse() throws SQLException // 07/06/2014
{
        LOG.info("start to create course, clubID = " + club.getIdclub() );
    CreateCourse cc = new CreateCourse();
    boolean ok = cc.createCourse(club, course, conn);
    if(ok)
    {
        LOG.info("course created, next step = tee");
        tee.setNextTee(true); // affiche le bouton next(Tee) bas ecran à droite
    }
}

public void createTee() throws SQLException
{
        LOG.info("Starting createTee !");
    CreateTee ct = new CreateTee();
    boolean ok = ct.createTee(club, course, tee, conn);
    if(ok)
    {
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
    CalcStablefordPlayingHandicap cph = new CalcStablefordPlayingHandicap();
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
    CreateHole ch = new CreateHole();
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
/*
    public void convertYtoM() {       //LOG.debug("starting 1 conversion");
        try {
            LOG.debug("starting conversion Yards to Meters with distance = " + hole.getHoleDistance());
            if (hole.getHoleDistance() == null) {
                LOG.debug("holeDistance = null");
            } else {
        //short s = Short.valueOf(hole.getHoleDistance()); // convert String to short
                //    LOG.debug("short s = " + s);
                //double m = (short)s; // convert short to double
                double d = Double.valueOf(hole.getHoleDistance()); // convert String to double
                    LOG.debug("Double d (yards) = " + d);
                double meters = d * Constants.YARD_TO_METER;  // voir golfInterface
                    LOG.debug("Meters = " + meters);
                hole.setHoleDistance((short) meters); // convert double to short
                    LOG.debug("ending conversion Yards to Meters with = " + hole.getHoleDistance().toString());
            } // end if
        } catch (NullPointerException npe) {
            String msg = "Â£Â£Â£ NullPointerException = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (NumberFormatException npe) {
            String msg = "Â£Â£ NumberFormatException = " + npe.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } catch (Exception e) {
            String msg = "Â£Â£ Exception in convert = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        }
    } //end method
 */
    
    public void createRound() throws SQLException
{   LOG.info("entering createRound ");
    CreateRound cr = new CreateRound();
    boolean ok = cr.createRound(round, course, conn);
    // ajouter boolean = correct insert !!!
    if(ok)
    {
        LOG.info("round created : we go to course !!");
         setNextInscription(true); // affiche le bouton next(Inscription) bas ecran Ã  droite
    }
}

    public List<SelectItem> getGames() {
        
       // LOG.info("List GAMES  = " );
        // for(SelectItem d:GAMES)
        // {LOG.info("game = " + d);
        // }
        return GAMES;
    }
    
  public List<SelectItem> getStart() {
        
       // LOG.info("List GAMES  = " );
        // for(SelectItem d:GAMES)
        // {LOG.info("game = " + d);
        // }
  return START;
    }
    
  
  public String createInscriptionOtherPlayers() throws SQLException
{
  LOG.info("entering createInscriptionOtherPlayers");
  LOG.info("selectedOtherPlayers = " + player.getDroppedPlayers().toString());
  LOG.info("round = " + round.toString());
  LOG.info("inscription = " + inscription.toString());
  LOG.info("club = " + club.toString());
  LOG.info("course = " + course.toString());
  LOG.info("list dropped players = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
  for(int i=0; i < player.getDroppedPlayers().size() ; i++)
     {
         
         // fields pour envoyer le mail de confirmation de l'inscription
            LOG.debug(" -- item in for idplayer = " + player.getDroppedPlayers().get(i).getIdplayer() );
        player2.setIdplayer(player.getDroppedPlayers().get(i).getIdplayer());
        player2.setPlayerLastName(player.getDroppedPlayers().get(i).getPlayerLastName());
        player2.setPlayerFirstName(player.getDroppedPlayers().get(i).getPlayerFirstName());
        player2.setPlayerCity(player.getDroppedPlayers().get(i).getPlayerCity());
        player2.setPlayerLanguage(player.getDroppedPlayers().get(i).getPlayerLanguage());
        player2.setPlayerEmail(player.getDroppedPlayers().get(i).getPlayerEmail());
        
        CreateInscription ci = new CreateInscription();
        boolean OK = ci.createInscription(round, player2, player, inscription, club, course, conn); // new 21/07/2014
        if(OK)
            {
            LOG.info("Inscription created for other players");
            LOG.info("il faudrait supprimer " + player2.getIdplayer() + " de la liste DroppedPlayers");
            player.getDroppedPlayers().remove(i);
            LOG.info("list dropped players after remove = " + Arrays.toString(player.getDroppedPlayers().toArray() ) );
       //     return "inscription_other_players.xhtml?faces-redirect=true";
           return null;  // retourne d'où il vient
        }else{
          LOG.info( "error creation inscription other players");
          return null;
        }
     }  // end for
    LOG.info("exiting createInscriptionOtherPlayers");
   return null;
}  // end method

public void createInscription() throws SQLException
{
    // ici afficher un écran avec le Tarif
    CreateInscription ci = new CreateInscription();
    boolean b = ci.createInscription(round, player, player, inscription, club, course, conn); // mod 10/11/2014
    if(b == false)
             {
                  LOG.info("boolean returned from create inscription is 'false' ");
                  inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
             } 
    if(b == true)
             {
                  LOG.info("boolean returned from create inscription is 'true' ");
                  LOG.info("Inscription created !");
                  inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
             } 
               
         setNextScorecard(true); // affiche le bouton carte de score ??
  //  }
}    

public String forgetPassword() throws SQLException, Exception
{
    LOG.info("entering forgetPassword");
// inset into table Activation
    CreateActivationPassword cap = new CreateActivationPassword();
    cap.createActivation(conn, player); // y compris envoi du mail
    LOG.info("line 02");
// send mail to user
 //   mail.ResetPasswordMail.sendResetPasswordMail(player);

    LOG.info("line03");
/*
  
    boolean b = modify.ModifyPassword.modifypassword(player, conn);
    
    if(b == true)
             {
                  LOG.info("boolean returned from modifyPassword is 'true' ");
                  String msg = "<br> <br> <h1>Password Created !! ";
                   LOG.info(msg + player.getWrkpassword());
                  LCUtil.showMessageInfo(msg);
                  player.setWrkpassword("***********");
                  return find.FindSubscriptionStatus.subscriptionStatus(subscription, player, conn); //, subscription);
             } 
    if(b == false)
             {
                  LOG.info("boolean returned from modifyPassword is 'false' ");
                  LOG.info("Inscription created !");
                  return null;
              } 
   //      setNextScorecard(true); // affiche le bouton carte de score ??
  //  }
    */
  return null;
}    

    public String getUuid() { // from password_check.xhtml
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

public String resetPassword() throws SQLException, Exception, Throwable
{       // called from password.check.xhtml
  //  LOG.info("entering resetPassword");
  try{
       LOG.info("entering resetPassword with uuid = " + uuid);  // à mon avis c'est pas bon ???
    PasswordController pc = new PasswordController();
//    LOG.info("line 01");
    boolean b = pc.checkPassword(uuid, conn);
    LOG.info("back  to courseC with b = " + b);
    if(b){  //true
        String msg = ("checkPassword success");
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
         return "login.xhtml?faces-redirect=true"; //&language=" + language + "&id=" + playerid;
    }else{ // false
        String msg = ("courseC : checkPassword failure ! " );
        LOG.error(msg);
        LCUtil.showMessageFatal(msg);
      //return null;  
         return "activation_failure.xhtml?faces-redirect=true";
     }
   }catch (TimeLimitException e){
        String msg = " $$$ TimeLimitException in PasswordController.checkpassword = " + e.toString();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
   }catch(Exception ex){
        String msg = "Course controller : resetPassword Exception ! " + ex;
        System.err.print(msg);
        LOG.error(msg);
        System.err.print("system error LC" + ex);
        LCUtil.showMessageFatal(msg);
        return null;
}    
  //  return str;
}

public String createPassword() throws SQLException, Exception
{
    LOG.info("entering createPassword");
    // à développer
 //    String msg = "<br> <br> <h1>Password Created !! ";
     
  //   modify.ModifyPassword.modifypassword(player, conn);
  //   LOG.info(msg + player.getWrkpassword());
    ModifyPassword mp = new ModifyPassword();
    boolean b = mp.modifypassword(player, conn);
    
    if(b == true)
             {
                  LOG.info("boolean returned from modifyPassword is 'true' ");
                  String msg = "<br> <br> <h1>Password Created !! ";
                   LOG.info(msg + player.getWrkpassword());
                  LCUtil.showMessageInfo(msg);
                  player.setWrkpassword("***********");
                  find.FindSubscriptionStatus fss = new find.FindSubscriptionStatus();
                  return fss.subscriptionStatus(subscription, player, conn); //, subscription);
             } 
    if(b == false)
             {
                  LOG.info("boolean returned from modifyPassword is 'false' ");
                  LOG.info("Inscription created !");
                  return null;
              } 
   //      setNextScorecard(true); // affiche le bouton carte de score ??
  //  }
  return null;
}    

  public String validateExistingPassword() throws SQLException  // used in modify_password_.xhtml pour afficher 2e panelGrid
{
        LOG.info("entering validateExistingPassword");
         LOG.info("password Wrk= " + player.getWrkpassword()); 
        LOG.info("password Player = " + player.getPlayerPassword());
        FindPassword fp = new FindPassword();
        boolean b = fp.passwordMatch(player, conn);
        LOG.info("from validateExistingPassword, after = " + Boolean.toString(b).toUpperCase());
     if(b)   // b is true
             {
                String msg = "existing password correct ! ";
                  LOG.info(msg);
                  LCUtil.showDialogInfo(msg);
                  setNextPanelPassword(true);  //affiche le 2e panelGrid
                  return null;
             } else{
                  String msg = "existing password not correct ! ";
                  LOG.error(msg);
                  LCUtil.showDialogFatal(msg);
                  return null;
             } 
   //  return null;
} // end method
  
public String modifyPassword() throws SQLException, Exception
{
    LOG.info("entering modifyPassword");
    // à développer
     LOG.info("password Wrk= " + player.getWrkpassword()); //"<br> <br> <h1>Password Created !! ";
     LOG.info("password Player = " + player.getPlayerPassword());
     FindPassword fp = new FindPassword();
     boolean b = fp.passwordMatch(player, conn);
  // tester ici si passwrd match ...
     setNextPanelPassword(false);  //affiche pas le 2e panelGrid pour prochaine utlisation dans la même session ?
     LOG.info("password match = " + b); //msg + player.getWrkpassword());
     ModifyPassword mp = new ModifyPassword();
     b = mp.modifypassword(player, conn);
     player.setWrkpassword("**********");
     LOG.info("passwordwrk set to  = " + "**********");
    if(b == true)
             {
                    LOG.info("boolean returned from modifyPassword is 'true' ");
                  String msg = "<br> <br> <h1>Password Created !! ";
                    LOG.info(msg + player.getWrkpassword());
                  LCUtil.showMessageInfo(msg);
                  find.FindSubscriptionStatus fss = new find.FindSubscriptionStatus();
                  return fss.subscriptionStatus(subscription, player, conn); //, subscription);
             } 
    if(b == false)
             {
                  LOG.info("boolean returned from modifyPassword is 'false' ");
                  LOG.info("Inscription created !");
                  return null;
       //           inscription.setInscriptionOK(true); // new 16/7/2016 used in inscription.xhtml
             } 
               
   //      setNextScorecard(true); // affiche le bouton carte de score ??
  //  }
  return null;
}    

public String findTarif() throws SQLException
{
    LOG.info("starting findTarif");
    LOG.info("course = " + course.toString());
//    LOG.info("just before player");
    LOG.info("player = " + player.toString());
   FindTarifData ft = new FindTarifData();
    tarif = ft.findCourseTarif(course, conn);
 //   boolean c = create.CreateInscription
  //  boolean b = create.CreateInscription.createInscription(round, player, inscription, club, course, conn); // mod 10/11/2014
    if(tarif == null){
                  LOG.info("Tarif returned from findTarifdata is null ");
               //   return "inscription.xhtml?faces-redirect=true";
          return null;  // donc inscription.xhtml
             } 
    if(tarif != null){
             LOG.info("tarifdata OK");
          CalcTarifGreenfee ct = new CalcTarifGreenfee();
          double dd = ct.greenFee(tarif, round, club, player);
             LOG.info("price green fee = " + dd);
          tarif.setPriceGreenfee(dd);
             } 
               

 return "tarif_round.xhtml?faces-redirect=true";
}    

public String findWeather() throws SQLException, Exception
{
 try{
    LOG.info("starting findWeather");
    LOG.info("club = " + club.toString());

    LOG.info("player = " + player.toString());
    LOG.info("round = " + round.toString());
        LOG.info("just before findWeather");
    find.FindWeather fw = new find.FindWeather();
    String weather = fw.currentWeatherByCityName(club.getClubCity(), club.getClubCountry());
 //   boolean c = create.CreateInscription
  //  boolean b = create.CreateInscription.createInscription(round, player, inscription, club, course, conn); // mod 10/11/2014
    if(weather == null){
                  LOG.info("Weather returned from findWeather is null ");
               //   return "inscription.xhtml?faces-redirect=true";
                  return null;  // donc inscription.xhtml
             } 
    if(weather != null){
                  LOG.info("weather data is  OK = " + weather);
           //       double dd = calc.CalcTarifGreenfee.greenFee(tarif, round, club, player);
           //       LOG.info("price green fee = " + dd);
           //       tarif.setPriceGreenfee(dd);
             } 
  //   return "tarif_round.xhtml?faces-redirect=true";
     return null;
 
 }catch(Exception ex){
    String msg = "finWeather Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
 
}   

public String selectTarif(ECourseList ecl) throws SQLException {
try{
    LOG.info("entering selectTarif");
  //  selectCourse(ecl);
           LOG.info("  selectTarif, ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
    return "tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in selectTarif ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} //end method

public String printTarif() throws SQLException {
try{
    LOG.info("entering printTarif");
    if(tarif == null){
        LOG.info("tarif = null");
    }
    if(tarif.getDatesSeason().length == 0 
     && tarif.getDays().length == 0
     && tarif.getTeeTimes().length == 0
     && tarif.getPriceEquipments().length == 0)
     {
       String msgerr =  LCUtil.prepareMessageBean("tarif.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       throw new Exception("from printTarif" +  msgerr);
     }
    LOG.info("entering printTarif with tarif = " + tarif.toString());
  //  selectCourse(ecl);
      LCUtil.showMessageInfo(tarif.toString());
    return null; //"tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in findTarif ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} //end method




public String findTarif(ECourseList ecl) throws SQLException {
try{
    LOG.info("entering findTarif");
  //  selectCourse(ecl);
           LOG.info("  selectTarif, ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        FindTarifData ft = new FindTarifData();
        tarif = ft.findCourseTarif(course, conn);
        if(tarif == null){
            String msg = "No Tarif available for this course";
            LOG.info(msg) ;
            LCUtil.showMessageInfo(msg);
        }else{
            String msg = "Tarif returned = " + tarif.toString();
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
        }
        // a faire : extraire le prix de la partie !!!
      
    return null; //"tarif_menu.xhtml?faces-redirect=true";
} catch(Exception ex){
    String msg = "Exception in findTarif ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} //end method

    public String selectRecentInscription(ECourseList ecl) throws SQLException {
        
        LOG.info("entering selectRecentInscrition");
           LOG.info("  selectRecentInscription, ecl = " + ecl);
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
           LOG.info("on cherche le nombre de joueurs  déjà inscrits et leur nom"); 
        lists.ScramblePlayersList spl  = new lists.ScramblePlayersList();
        lp = spl.listAllParticipants(round, conn);
           LOG.info("after lists.ScramblePlayersList ");
  if(lp != null)
  {
        LOG.info("nombre de players stableford = lp size = " + lp.size());
        inscription.setInscriptionOK(true); // new 12/7/2017 used in inscription.xhtml
     String s = "";
     for(int i=0; i < lp.size() ; i++)
     {
        LOG.debug(" -- item in for idplayer = " + lp.get(i).getIdplayer() );
        s = s + lp.get(i).getPlayerLastName();
        s = s + " (";
        s = s + lp.get(i).getIdplayer();
        s = s + "), ";
     } // end for 
        
     round.setPlayersString(s);
       LOG.info("c'est fait ! joueurs déjà inscrits = " + round.getPlayersString() );
  }else{
         LOG.info("nombre de players stableford =  zero" );
       round.setPlayersString("no players inscrits");
     }
           
        inscription.setInscriptionTeeStart(clubcourseround.getTeeStart());
          LOG.info("TeeStart = " + inscription.getInscriptionTeeStart());
           
        String msg = "Select ClubCourseRound Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
   //     LCUtil.showMessageInfo(msg);
        //String s = Integer.toString(player.getIdplayer() );
        return "inscription.xhtml?faces-redirect=true";
    } // end method
    
public String scoreMatchplay(ClubCourseRound clubcourseround) throws SQLException 
{
    LOG.info(" ... entering ScoreMatchplay = " + clubcourseround);
    return null;
}

//public String scoreStablefordMatchplay(ClubCourseRound clubcourseround)
//{
//    return null;
//}

// enlevé 20-01-2018
    public String scoreStableford(ECourseList ecl) throws SQLException {
  //       public String scoreStableford(ClubCourseRound clubcourseround) throws SQLException {
        LOG.info(" ... entering ScoreStableford "); // + clubcourseround);
        LOG.info(" ... entering ScoreStableford  with ecl = " + ecl.toString());
        
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
 /*       
        this.clubcourseround = clubcourseround;
        club.setIdclub(clubcourseround.getIdclub());
            LOG.info("idClub = " + club.getIdclub());
        club.setClubName(clubcourseround.getClubName());
            LOG.info("ClubName = " + club.getClubName());
        course.setIdcourse(clubcourseround.getIdcourse());
            LOG.info("idCourse = " + course.getIdcourse());
        course.setCourseName(clubcourseround.getCourseName());
            LOG.info("CourseName = " + course.getCourseName());
        course.setCourseBegin(clubcourseround.getCourseBeginDate());
            LOG.info("CourseBegin = " + course.getCourseBegin());    
        course.setCourseEnd(clubcourseround.getCourseEndDate());
            LOG.info("CourseEnd = " + course.getCourseEnd());    
        round.setIdround(clubcourseround.getIdround());
            LOG.info("IdRound = " + round.getIdround());
        round.setRoundDate(clubcourseround.getRoundDate());
        //    LOG.info("RoundDate = " + SDF.format(round.getRoundDate()));
            LOG.info("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
        String msg = "";
        round.setRoundCompetition(clubcourseround.getRoundCompetition());
            LOG.info("RoundCompetition = " + round.getRoundCompetition());
        round.setRoundGame(clubcourseround.getRoundGame()); // new 19/06/2014
            LOG.info("RoundGame = " + round.getRoundGame());    
        round.setRoundHoles(clubcourseround.getRoundHoles());
            LOG.info("RoundHoles = " + round.getRoundHoles());
        round.setRoundStart(clubcourseround.getRoundStart());
            LOG.info("RoundStart = " + round.getRoundStart());
            LOG.info("Player = " + player.getIdplayer());
*/
        find.FindCountScore sciup = new find.FindCountScore();
        int rows = sciup.getCountScore(conn, player, round, "rows");
        // LOG.info("there are : " + rows + " if > 0 we replace the previous score");
    //    parArray = LoadParArray(course.getIdcourse() );
        load.LoadParArray lpa = new load.LoadParArray();
        parArray = lpa.LoadParArray(conn, player, course);  //mod 15/08/2014
        if (rows != 0) // update
        {
            LOG.info("there are : " + rows + " ==> this is a UPDATE, thus we are prefilling the score !");
            load.LoadScoreArray lsa = new load.LoadScoreArray();
            scoreStableford.setHoles(lsa.LoadScoreArray(conn, player, round) );
            // score.setHoles(ListController.getScoreArray(conn, player, round) );
            
            load.LoadStatisticsArray lstta = new load.LoadStatisticsArray();
            scoreStableford.setStatistics(lstta.LoadStatisticsArray(conn, player, round) );
            // score.setStatistics(ListController.getStatisticsArray(conn, player, round) );
            LOG.info("Score is NOW prefilled !!");
        }
        String msg = "Select ClubCourseRound Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
//à mofifier : renvoyer en fonction de Stableford ou Matchplay FOURBALL ou FOURSOME ou SINGLE
        //Str.startsWith("_MP") 
     //   return "score_matchplay.xhtml?faces-redirect=true";
        return "score_stableford.xhtml?faces-redirect=true";
    }

 //   public String show_participants(ClubCourseRound clubcourseround)
 //   {
 //       return show.__ShowParticipants.show_participants(clubcourseround, club, course, round);
 //   }
    
    public String inputTarifSeasons() throws SQLException, Exception
{
   LOG.info("entering inputTarifSeasons !");
try{
      //  LOG.info("entering inputtarif seasons with round = " + round.toString());
        LOG.info("entering tarif seasons with course = " + course.toString());
        LOG.info("datesSeason = "  + Arrays.deepToString(tarif.getDatesSeason()));
    String msg = " input Period Seasons = " + tarif.getTarifIndexSeasons() + " / " 
                                            + Arrays.deepToString(tarif.getDatesSeason()[tarif.getTarifIndexSeasons()]);
    LOG.info(msg);
    LCUtil.showMessageInfo(msg);
    
    tarif.setTarifIndexSeasons(tarif.getTarifIndexSeasons() + 1);
    LOG.info("Tarif Index Seasons is now :" + tarif.getTarifIndexSeasons());
     return null; // retourne d'ouù il vient
    // à modifier
 //    boolean ok = create.CreateTarif.createTarif(club, conn);
}catch(Exception ex){
    String msg = "inputTarifSeasons Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} // end method

  public String createTarif() throws SQLException, Exception
{
   LOG.info("entering createTarif !");
try{
     //   LOG.info("entering tarif seasons with round = " + round.toString());
        LOG.info("entering createTarif with course = " + course.toString());
        LOG.info("datesSeason to be created = " + Arrays.deepToString(tarif.getDatesSeason()));
        LOG.info("equipments to be created = " + Arrays.deepToString(tarif.getPriceEquipments()));
        LOG.info("days to be created = " + Arrays.deepToString(tarif.getDays()));
        LOG.info("tarif index = " + tarif.getTarifIndexSeasons());
    tarif.RemoveNull(); // remove null from arrays
    String msg = " Tarif to be created = " + tarif.toString();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
 //   tarif.setTarifIndex(tarif.getTarifIndex() + 1);
    create.CreateTarif ct = new create.CreateTarif();
    boolean b = ct.createTarif(tarif, course, conn);
    LOG.info("is tarif created ? = " + b);
     return null; // retourne d'ouù il vient
    // à modifier
 //    boolean ok = create.CreateTarif.createTarif(club, conn);
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} // end method
        
public String inputTarifHours() throws SQLException, Exception
{
   LOG.info("entering inputTarifHours !");
try{
        LOG.info("entering tarif Hours with course = " + course.toString());
        LOG.info("teeTimes = "  + Arrays.deepToString(tarif.getTeeTimes()));
    String msg = " input Hours = " + tarif.getTarifIndexHours() + " / " 
                                   + Arrays.deepToString(tarif.getTeeTimes()[tarif.getTarifIndexHours()]);
    LOG.info(msg);
    LCUtil.showMessageInfo(msg);
    
    tarif.setTarifIndexHours(tarif.getTarifIndexHours() + 1);
        LOG.info("tarif index Hours is now :" + tarif.getTarifIndexHours());
     return null; // retourne d'ouù il vient
}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} // end method
    
public String inputTarifEquipments() throws SQLException, Exception
{
   LOG.info("entering inputTarifEquipments !");
try{
     //   LOG.info("entering tarifEquipments with round = " + round.toString());
         LOG.info("entering tarif esuipments with course = " + course.toString());
        LOG.info("equipments = "  + Arrays.deepToString(tarif.getPriceEquipments()));
    String msg = " input Equipments = "  + Arrays.deepToString(tarif.getPriceEquipments());
    LOG.info(msg);
    LCUtil.showMessageInfo(msg);
 //   tarif.setTarifIndexHours(tarif.getTarifIndexHours() + 1);
 //       LOG.info("tarif index Hours is now :" + tarif.getTarifIndex());
     return null; // retourne d'ouù il vient

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} // end method
        
public String inputTarifDaysOfWeek() throws SQLException, Exception
{
   LOG.info("entering inputTarifDaysOfWeek !");
try{
         LOG.info("entering tarif Days with course = " + course.toString());
     LOG.info("days = "  + Arrays.deepToString(tarif.getDays()));
     String msg = "days = "  + Arrays.deepToString(tarif.getDays());
//        LOG.info("début période days = " + tarif.getStartDays());
//        LOG.info("fin période days = " + tarif.getEndDays());
//    String msg = " input days = "  + Arrays.deepToString(tarif.getDays()) 
//            + " / " + tarif.getStartDays() + " / " + tarif.getEndDays();
    LOG.info(msg);
    LCUtil.showMessageInfo(msg);
    
 //   tarif.setTarifIndexHours(tarif.getTarifIndexHours() + 1);
 //       LOG.info("tarif index Hours is now :" + tarif.getTarifIndex());
     return null; // retourne d'ouù il vient

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
}    
} // end method        
        
        
        
public void createScoreStableford() throws SQLException, Exception
{
   LOG.info("entering createScoreStableford !");
try{
    LOG.info("entering with round game = " + round.getRoundGame());
  //  List<Player> lp = null;
if(Round.GameType.SCRAMBLE.toString().equals(round.getRoundGame()))
{
        LOG.info("this is a SCRAMBLE game");
     lists.ScramblePlayersList spl = new lists.ScramblePlayersList();
     lp = spl.listAllParticipants(round, conn);
 //       LOG.info("lp ");
   LOG.info("nombre de players stableford = lp size = " + lp.size());
// on enregistre le résultat pour CHAQUE joueur = principe de fonctionnement
     for(int i=0; i < lp.size() ; i++)
     {
         LOG.debug(" -- item in for idplayer = " + lp.get(i).getIdplayer() );
        player2.setIdplayer(lp.get(i).getIdplayer()); // mod 04-12-2017 en player2
        player2.setPlayerLastName(lp.get(i).getPlayerLastName());
        CreateScoreStableford css = new CreateScoreStableford();
        boolean ok = css.createModifyScore(scoreStableford, round, player2, conn);
        if(ok)
            {
            LOG.info("ScoreStableford created for multiple players in scramble round!");
            setShowButtonCreateStatistics(true);  // affiche le bouton bas ecran
        }else{
          LOG.info( "error creation score scramble");
        }
     }    //end for 

}else{ // single stableford : on enregistrele résultat pour un seul joueur !
        CreateScoreStableford css = new CreateScoreStableford();
        boolean ok = css.createModifyScore(scoreStableford, round, player, conn);
        if(ok)
            {
            LOG.info("ScoreStableford created !");
            setShowButtonCreateStatistics(true);  // affiche le bouton bas ecran
        }else{
          LOG.info( "error creation score stableford");
        }    
    }

}catch(Exception ex){
    String msg = "Exception ! " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
}    
 //   }
} // end method
    

public void createScoreMatchplay() throws SQLException
{
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
        CreateScoreMatchplay csmp = new CreateScoreMatchplay();
        csmp.createAllScores(scoreMatchplay, round, conn);
} catch(Exception ex)
{
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
     //       return null;
}
} // end method
 
public void validateScoreHoleMatchplay2()
{
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
    if (pass == null)
    {
        pass = (String) passComponent.getValue();
         LOG.info(" validateMP4 - pass2 = " + pass);
    }
    
    if (!pass.equals(confirm))
    {
        LOG.info(" validateMP4 - pass not equal confirm = " );
        String msg = toValidate.getClientId(context);
        LCUtil.showMessageFatal(msg);
   //     String err = Application.getMessage(context, UsersDialog.ERROR_PASSWORD_MATCH);
     //   throw new ValidatorException(new FacesMessage(err));
    }
}
    
public void createStatistics() throws SQLException
{
    sc2 = scoreStableford.getStatistics();
    CreateStatistics cs = new CreateStatistics();
    boolean ok = cs.createStatistics(player, round, sc2, conn);
    // ajouter boolean = correct insert !!!
    if(ok)
    {
        LOG.info("statistics created : we go to XXX !!");
         setNextScorecard(true); // affiche le bouton next(Scorecard) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  droite
   }
}
    
public List<Integer> getValues() {
        LOG.info("entering getValues ...");
        //List<Integer> values = new ArrayList<>();

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
public String show_scorecard() throws SQLException
{
    LOG.info("entering show_scoreCard with :!" + round.getRoundQualifying() );
    show.ShowScoreCard scc = new show.ShowScoreCard();
    return scc.show_scorecard(player, club, course, round, inscription, conn);
}

public String show_scorecard_empty(ECourseList ecl) throws SQLException
{
    LOG.info("entering show_scoreCard_empty with :!" + ecl.toString() );
    club.setIdclub(ecl.Eclub.getIdclub());
    course.setIdcourse(ecl.Ecourse.getIdcourse());
    
    return show.ShowScoreCard.show_scorecard_empty(player, club, course, round, inscription, conn);
}


     public List<ECourseList> listRounds() {
   //      public List<ClubCourseRound> listRounds() {
        LOG.info("from CourseController : listRounds = " );
     try {
         lists.RecentList rl = new lists.RecentList();
         return rl.getRecentRoundList(player, conn);
     } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
     }
    } //end method
 
     // used in selectFlight.xhtml
        public List<Flight> listFlights() {
            LOG.info("from CourseController : entering listFlights ... " );
        try {
            // for testing purposes, à adapter ultérieurement!!
                LOG.info("course id = " + course.getIdcourse());
                if(round.getWorkDate() == null)
                {
                    String msg = "Test : listFlights : round workdate = null";
                    LOG.error(msg);
                    LCUtil.showMessageInfo(msg);
                }else{
                     LOG.info("round workdate = " + round.getWorkDate());
                }
 //               LOG.info("line 222 = ");

          if(club.getClubLatitude().compareTo(BigDecimal.ZERO) == 0){
            String msg = "Latitude du club == " + club.getClubLatitude();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
      }
      
    //  String lat = club.getClubLatitude().toString();
    //  String lng = club.getClubLongitude().toString();
 //      LOG.info("line 223 = ");
                LOG.info("latitude = " + club.getClubLatitude().toString());
                LOG.info("latitude = " + club.getClubLongitude().toString());
  ///          à modifier ici !!! : utiliser le latlng du club !!
  
  
// date, lat, lng, tz
     //       ArrayList<Flight> fl = SunriseSunsetApiController.findSunriseSunset(round.getWorkDate(),"50.826267","4.357043","Europe/Brussels",conn);
       ArrayList<Flight> fl = SunriseSunsetApiController
               .findSunriseSunset(round.getWorkDate(),club.getClubLatitude().toString(),club.getClubLongitude().toString(),"Europe/Brussels",conn);
            LOG.info("ArrayList<Flight> = " + fl);
            
            //Founded sunrise = 2017-04-09T04:59:02+00:00 
            //Founded sunset  = 2017-04-09T18:28:53+00:00 
             LOG.info("from CourseController : exiting listFlights() ... " );
            return fl;
        } catch (Exception ex) {
            String msg = "Exception in listFlights= " + ex.toString();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        }
    } //end method
     
     
     
public List<Old_Matchplay> listMatchplayRounds(String formula)
    {
        LOG.info("from CourseController : listMatchplayRounds for formula = " + formula);
        try {
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
         LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         round.setRoundDate(date);
         
         
     //          inscription.setPlayerhasroundTeam(listmp.get(1).getPlayerhasroundTeam() );
         matchplay.setRoundCompetitionName(listmp.get(1).getRoundCompetitionName() );
     //          matchplay.setPlayerhasroundPlayerNumber(listmp.get(1).getPlayerhasroundPlayerNumber() );
               // et d'autres ...
         return listmp;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    
public List<ScoreScramble> listScrambleRounds(String formula)
    {
        LOG.info("entering  CourseController : listScrambleRounds with formula =  " + formula);
        try {
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
  //       round.setRoundDate(listscr.get(0).getRoundDate() );
         
 ///        java.util.Date d = listscr.get(0).getRoundDate();
 ///        LocalDateTime date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         round.setRoundDate(listscr.get(0).getRoundDate());
         
         
     //          inscription.setPlayerhasroundTeam(listmp.get(1).getPlayerhasroundTeam() );
 ////        scoreScramble.setRoundCompetitionName(listscr.get(0).getRoundCompetitionName() );
     //          matchplay.setPlayerhasroundPlayerNumber(listmp.get(1).getPlayerhasroundPlayerNumber() );
               // et d'autres ...
         return listscr;
 //               return "score_matchplay.xhtml?faces-redirect=true";
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
        }
    } //end method    


public List<Old_Matchplay> getListmp() {
        return listmp;
    }

public void setListmp(List<Old_Matchplay> listmp) {
        this.listmp = listmp;
    }

public List<ECourseList> listPlayedRounds(String formula) throws SQLException
  //      public List<ClubCourseRound> listPlayedRounds(String formula) throws SQLException
{     LOG.debug(" ... entering listPlayedRounds WITHOUT formula = " + formula);
      lists.PlayedList pl = new lists.PlayedList();
 return pl.getPlayedList(player, conn);
}

public List<ECourseList> listStablefordPlayedRounds()  // from selectStablefordRounds.xhtml
   //     public List<ClubCourseRound> listStablefordPlayedRounds()  // from selectStablefordRounds.xhtml
    { LOG.debug(" ... entering listPlayedRounds ! " );
        try {
            lists.PlayedList pl = new lists.PlayedList();
            return pl.getPlayedList(player, conn);
        } catch (SQLException ex) {
            LOG.error("Exception in listPlayedRounds  " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method
 
public int[] LoadTeeStart() // new 09/06/2015
    {
      //  course.setIdcourse(in_course);
    LOG.info(" ... entering LoadTeeStart in courseC from for " + round.getIdround());
               
        try {
            return null;  // back to originating view
        } catch (Exception ex) {
            LOG.error("Exception ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method
  
public String LoadTarif() // new 31/03/2018
    {
    LOG.info(" ... entering LoadTarif for testing purposes payment.xhtml");
        try {
            tarif.setPriceGreenfee(99.0);
            String[] equip = new String [10];
 //           LOG.info("line 00");
            equip[0]="20";
            equip[1]="30";
            equip[2]="40";
            equip[3]="50";
            tarif.setPriceEquipments(equip);
            LOG.info("tarif généré for testing purposes = " + tarif.toString()); //Arrays.deepToString(equip));
            
            return null;  // back to originating view
        } catch (Exception ex) {
            LOG.error("Exception in ! " + ex);
            LCUtil.showMessageFatal("Exception = " + ex.toString());
            return null;
        } finally {
        }
    } //end method


public String to_selectMatchplayRounds_xhtml(String s)
   {
            LOG.info("entering to_ ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectMatchplayRounds.xhtml?faces-redirect=true";
   }

public String to_selectPlayer_xhtml(String s)
   {
            LOG.info("entering to selectPlayer_xhtml... with string = " + s);
            reset(s);
       return "selectPlayer.xhtml?faces-redirect=true";
   }

public String to_selectScrambleRounds_xhtml(String s)
   {
            LOG.info("entering to_selectScrambleRounds_xhtml ... with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectScrambleRounds.xhtml?faces-redirect=true";
   }
public String to_selectRegisteredRounds_xhtml(String s)
   {
            LOG.info("entering to_selectRegisteredRounds_xhtml with String = " + s);
       reset(s);
       return "selectRegisteredRounds.xhtml?faces-redirect=true&cmd=" + s;
   }

public String to_selectParticipantsRound_xhtml(String s)
   {
            LOG.info("entering to_selectParticpantsRound_xhtml with String = " + s);
       reset(s);
       return "selectParticipantsRound.xhtml?faces-redirect=true&cmd=" + s;
   }

public String to_select_inscription_xhtml(String s) // register score stableford
   {
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

public String to_selectStablefordRounds_xhtml(String s) // register score stableford
   {
            LOG.info("entering to_selectStablefordRounds_xhtml with string = " + s);
            reset(s);
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "selectStablefordRounds.xhtml?faces-redirect=true";
   }

public String to_show_played_rounds_xhtml(String s)
   {
       LOG.info("entering to_show_played_rounds_xhtml with string s = " + s);
            reset(s);
            setInputPlayedRounds(s); // new 29/03/2016
   //    return "selectMatchplayRounds.xhtml?faces-redirect=true&game=matchplay&operation=create";
       return "show_played_rounds.xhtml?faces-redirect=true&cmd=" + s; // mod 28/03/2016
   }

public String to_club_xhtml(String s)
   {
        LOG.info("entering to_club_hxtml ... with string = " + s);
            reset(s);
       club.setCreateModify(true);  // gestion button dans club.xhtml
       return "club.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_course_xhtml(String s) // 12/08/2017
   {
        LOG.info("entering to_course_xthml ... with string = " + s);
            reset(s);
       course.setCreateModify(true);  // gestion button dans ccourse.xhtml
       return "course.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_tee_xhtml(String s) // 12/08/2017
   {
        LOG.info("entering to_tee_xthml ... with string = " + s);
            reset(s);
       tee.setCreateModify(true);  // gestion button dans tee.xhtml
       return "tee.xhtml?faces-redirect=true&operation=" + s;
   }

public String to_clubModify_xhtml(String s)
   {
        LOG.info("entering to_clubModify_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       setInputSelectCourse(s);
       return "modifyClubCourseTee.xhtml?faces-redirect=true";
   }
public String to_clubDelete_xhtml(String s)
   {
        LOG.info("entering to_clubDelete_xhtml ... with string = " + s);
       reset("clubRestart " + s);
       setFilteredCourses(null);
       setInputSelectCourse(s);
       return "deleteClubCourseTee.xhtml?faces-redirect=true";
   }

public String to_player_xhtml(String s)
   {
         LOG.info("entering to_player_xthml ... with string = " + s);
       reset(s);
       return "player.xhtml?faces-redirect=true";
   }

public void to_player_modify(String s)
   {
         LOG.info("entering to_player_modify ... with string = " + s);
  //     reset(s);
        createModifyPlayer = s;
 //      return "player_modify.xhtml?faces-redirect=true";
   }

public String to_show_handicap_xhtml(String s)
   {
        LOG.info("entering to_show_handicap ... with string = " + s);
            reset(s);
       return "show_handicap.xhtml?faces-redirect=true";
   }

public String to_delete_player_xhtml(String s) throws Exception
   {
        LOG.info("entering to_delete_player ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
        
////        delete.DeletePlayer.deletePlayerAndChilds(conn, Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   //         return "";
   }
public String to_delete_club_xhtml(String s) throws Exception
   {
        LOG.info("entering to_delete_club ... with string = " + s);
        reset(s);
        setDeletePlayer(Integer.parseInt(s));
        
////        delete.DeletePlayer.deletePlayerAndChilds(conn, Integer.parseInt(s));
       return "delete_cascading_player.xhtml?faces-redirect=true";
   //         return "";
   }
  public void deleteCascadingPlayer() throws SQLException, Exception
{
 try{
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
            LCUtil.showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player

public String to_selectCourse_xhtml(String s)
   {
        LOG.info("entering to_selectCourse_xhtml ... with string = " + s);
        // CreateRound
        // ChartRound
        // ChartCourse?cmd=ini&amp;operation=restart"
       reset("selectcourse " + s);
       setFilteredCourses(null);
       setInputSelectCourse(s);
        LOG.info(" course selected for : " + getInputSelectCourse());
       return "selectCourse.xhtml?faces-redirect=true&cmd=" + s;
   }

public String scorecard(ECourseList ecl) {
   // public String scorecard(ClubCourseRound clubcourseround) {
   LOG.info("Entering scorecard");// with ecl = " + ecl.toString());
        LOG.info("Entering scorecard with ecl = " + ecl.toString());
        club = ecl.getClub();
        course = ecl.getCourse();
        round = ecl.getRound();
        /*
        this.clubcourseround = clubcourseround;
        club.setIdclub(clubcourseround.getIdclub());
            LOG.info("idClub = " + club.getIdclub());
        club.setClubName(clubcourseround.getClubName());
            LOG.info("ClubName = " + club.getClubName());
        course.setIdcourse(clubcourseround.getIdcourse());
            LOG.info("idCourse = " + course.getIdcourse());
        course.setCourseName(clubcourseround.getCourseName());
            LOG.info("CourseName = " + course.getCourseName());
        round.setIdround(clubcourseround.getIdround());
            LOG.info("IdRound = " + round.getIdround());
            
        round.setRoundDate(clubcourseround.getRoundDate());
          //  LOG.info("RoundDate = " + SDF.format(round.getRoundDate()) );
            LOG.info("RoundDate = " + round.getRoundDate().format(ZDF_TIME));
        //    roundDate.format(ZDF_TIME));
            
        round.setRoundCompetition(clubcourseround.getRoundCompetition());
            LOG.info("RoundCompetition = " + round.getRoundCompetition());
        round.setRoundGame(clubcourseround.getRoundGame());
        round.setRoundQualifying(clubcourseround.getRoundQualifying() );
        round.setRoundHoles(clubcourseround.getRoundHoles());
*/
        inscription = ecl.getInscription();
  //      inscription.setInscriptionTeeStart(clubcourseround.getTeeStart());
            LOG.info("TeeStart from scorecard = " + inscription.getInscriptionTeeStart());
        String msg = "Select ClubCourseRound Successful "
                + " <br/> Club name = " + club.getClubName()
                + " <br/> last name = " + course.getCourseName()
                + " <br/> round = " + round.getIdround();
        LOG.info(msg);
  //      LCUtil.showMessageInfo(msg);
        return "scorecard.xhtml?faces-redirect=true";
     } //end

public List<ScoreCard> getScoreCardList1() throws SQLException, LCCustomException {
    try
    {
        lists.ScoreCard1List scl = new lists.ScoreCard1List();
        return scl.getScoreCardList1(player, round, conn);
    }catch (NullPointerException | SQLException ex){
            String msg = "Exception in getScoreCardList1() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
    } finally {

    }

    } //end method

    public static boolean isShowButtonCreditCard() {
        return ShowButtonCreditCard;
    }

    public static void setShowButtonCreditCard(boolean ShowButtonCreditCard) {
     //   creditcard.setPaymentOK(ShowButtonCreditCard);
        CourseController.ShowButtonCreditCard = ShowButtonCreditCard;
    }

public List<StablefordResult> getScoreCardList2() throws SQLException {
    try
    {  
        // le nom est trompeur : fait beaucup plus que son nom l'indique !
        find.FindSlopeRating fsr =  new find.FindSlopeRating();
        List<StablefordResult> l = fsr.getSlopeRating(player, round, conn);
        // pour utiliser dans getScoreCardList3
        inscription.setInscriptionTeeStart(l.get(0).getInscriptionTeeStart() );
            return l;
    }catch (Exception ex){
            String msg = "Exception in getScoreCardList2() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

public List<ScoreCard> getScoreCardList3() throws SQLException {
      try{
          lists.ScoreCard3List sc3l = new lists.ScoreCard3List();
            return sc3l.getScoreCardList3(player, round, inscription, conn);
      }catch (Exception ex){
            String msg = "Exception in getScoreCardList3() " + ex;
                LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
      } finally {

      }

    } //end method

public List<Player> listPlayers() throws SQLException {
//            LOG.info("... entering listPlayers with conn = " + conn);
   try {
       lists.PlayersList pl = new lists.PlayersList();
       return pl.getListAllPlayers(conn);
   } catch (Exception ex) {
            String msg = "Exception in CourseController.listPlayers() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
public void listSubscriptionRenewal(String s) throws SQLException {
            LOG.info("... entering listSubscriptionReneval " + s);
        try {
            lists.SubscriptionRenewalList srl = new lists.SubscriptionRenewalList();
             subscriptionRenewal = srl.getListSubscriptions(conn);
    //         subscriptionReneval.forEach(item -> LOG.info("liste " + item));  // java 8 lambda
             for(Player item : subscriptionRenewal)
             {
        	LOG.info("Player to send a Subscription Renewal mail = " + item.getPlayerLastName());
                String msg =
                  " Please consider your subcription renewal at the famous GolfLC !!" 
                + " <br/>Your subscription end Date is : " + item.getEndDate()
                + " <br/><b>ID         = </b>" + item.getIdplayer()
                + " <br/><b>First Name = </b>" + item.getPlayerFirstName()
                + " <br/><b>Last Name  = </b>" + item.getPlayerLastName()
                + " <br/><b>Language   = </b>" + item.getPlayerLanguage()
                + " <br/><b>City       = </b>" + item.getPlayerCity()
                + " <br/><b>Email      = </b>" + item.getPlayerEmail()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                LOG.info("mail to be sended = " + msg);

                String sujet = "Your Subscription Renewal for GolfLC";
                String to = "louis.collet@skynet.be";
                utils.SendEmail sm = new utils.SendEmail();
                boolean b = sm.sendHtmlMail(sujet,msg,to);
                    LOG.info("HTML Mail status = " + b);
                    LCUtil.showDialogInfo("sending one subscription Renewal Mail !!");
              } //end for
             LCUtil.showDialogInfo("End sending subscription Renewal Mails = " + subscriptionRenewal.size() );
        } catch (Exception ex) {
            String msg = "Exception in listSubscriptionRenewal " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
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
            String msg = "Exception in getListAllCars() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
 public void validatePlayer()// throws SQLException, Exception
{
     setNextPanelPlayer(true);  //affiche le 2e panelGrid
}

 
public String creditCardMail() throws MessagingException, Exception{
    LOG.info("entering creditCardMail");
    String sujet = "Your creditcard payment for your Round Inscription via GolfLC";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Tarif details  = " + tarif.toString()
                + " <br/> credit card details  = " + creditcard.toString()       
        //        + " <br/> Round Date   = " + round.getRoundDate().format(ZDF_TIME_HHmm)
        //        + " <br/> Course Name  = " + course.getCourseName()
        //        + " <br/> Club Name    = " + club.getClubName()
        //        + " <br/> Club City    = " + club.getClubCity()
       //         + " <br/><b>ID         = </b>" + player.getIdplayer()
        //        + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
        //        + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
       //         + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
      //          + " <br/><b>City       = </b>" + player.getPlayerCity()
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
     //           + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                
                String to = "louis.collet@skynet.be";
                utils.SendEmail sm = new utils.SendEmail();
                boolean b = sm.sendHtmlMail(sujet,mail,to);
                    LOG.info("HTML Mail status = " + b);
          return "creditcard_accepted.xhtml?faces-redirect=true";
}
public Boolean creditCardSubscriptionMail() throws MessagingException, Exception{
    LOG.info("entering creditCardSubscriptionMail");
    try{
    String sujet = "Your Subscription via GolfLC is paid !";

                String mail = 
                  " <br/>Payment Confirmation - GolfLC!"
                + " <br/>" + SDF_TIME.format(new java.util.Date() )
                + " <br/> Montant payé = " + creditcard.getTotalPrice()
                + " <br/> Creditcard Number  = " + creditcard.getCreditCardNumber()
                + " <br/> Creditcard Issuer  = " + creditcard.getCreditCardIssuer()
                + " <br/> Communication = " + creditcard.getCommunication()
                + " <br/> credit card details  = " + creditcard.toString()
                + " <br/> subscription details  = " + subscription.toString()        
                + " <br/> Subscription EndDate  = " + subscription.getEndDate().format(ZDF_TIME_DAY)
        //        + " <br/> Course Name  = " + course.getCourseName()
        //        + " <br/> Club Name    = " + club.getClubName()
        //        + " <br/> Club City    = " + club.getClubCity()
       //         + " <br/><b>ID         = </b>" + player.getIdplayer()
        //        + " <br/><b>First Name = </b>" + player.getPlayerFirstName()
        //        + " <br/><b>Last Name  = </b>" + player.getPlayerLastName()
       //         + " <br/><b>Language   = </b>" + player.getPlayerLanguage()
      //          + " <br/><b>City       = </b>" + player.getPlayerCity()
      //          + " <br/><b>Email      = </b>" + player.getPlayerEmail()
     //           + " <br/><b>Invited by     = </b>" + invitedBy.getPlayerLastName() + ", " + invitedBy.getPlayerFirstName()
                + " <br/> Thank you !"
                + " <br/> The GolfLC team"
                    ; 
                
                String to = "louis.collet@skynet.be";
                utils.SendEmail sm = new utils.SendEmail();
                boolean b = sm.sendHtmlMail(sujet,mail,to);
                    LOG.info("HTML Mail status = " + b);
        //  return "creditcard_accepted.xhtml?faces-redirect=true";
          return b;
    }catch (Exception ex){
            String msg = "Exception in creditCardSubscription mail " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
}
} //end method

public void createPlayer() throws SQLException, Exception
{
 try{
     LOG.info("entering createPlayer");
      create.CreatePlayer cp = new create.CreatePlayer();
    boolean ok = cp.createPlayer(player, handicap, conn, "A"); // "A" signifie avec Activation (non en batch)
    // ajouter boolean = correct insert !!!
    if(ok)
    {
            LOG.info("player created, next step = photo");
        setNextPlayer(true); // affiche le bouton next(photo) bas ecran ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â  droite
    }else{
        // error in create player
        LOG.info("error : new player ,not created !!");
    }
 }catch (Exception ex){
            String msg = "Exception in createPlayer " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
  //          return null;
  }
} //end method create player
//////////////////////////////////////////////////////////////////////////////// club

public String createClub() throws SQLException, IOException
{
    LOG.info("entering CreateClub");
    
 //   FacesContext fc = FacesContext.getCurrentInstance();
 //   ExternalContext ec = fc.getExternalContext();
 //   ec.responseSendError(401, "You are not AUThorized to fetch this resource.");  // was 401
 //   fc.responseComplete();
    //return null;
    //401 = HttpServletResponse.SC_UNAUTHORIZED
    // 404 = HttpServletResponse.SC_NOT_FOUND
    CreateClub cc = new CreateClub();
    boolean ok = cc.createClub(club, conn);
    // ajouter boolean = correct insert !!!
    if(ok)
    {
        LOG.info("club created : we go to course !!");
        course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
    }
    return null;
} // end method createClub

public String addCourse(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering addCourse");
 try{
         load.LoadClub lc = new load.LoadClub();
         club = lc.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
         LOG.info("adding a course for idclub = " + ecl.Eclub.getIdclub() + club.getClubName());
        course.setCreateModify(true); // mod 12/122017 was false gestion button dans course.xhtml
        return "course.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addCourse " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method addCourse

public String addTee(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering addTeee");
 try{
         load.LoadClub lcl = new load.LoadClub();
         club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
         load.LoadCourse lco = new load.LoadCourse();
         course = lco.LoadCourse(conn, ecl.Ecourse.getIdcourse());
            LOG.info("idclub forced at " + ecl.Eclub.getIdclub() + club.getClubName());
         tee.setCreateModify(true); // gestion button dans tee.xhtml
        return "tee.xhtml?faces-redirect=true&operation=add";
 }catch (Exception ex){
            String msg = "Exception in addTee" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method addTee

public String deleteClub(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering deleteClub for " + ecl.Eclub.getIdclub());
 try{
       DeleteClub dc= new DeleteClub();
       String del = dc.deleteClub(ecl.Eclub.getIdclub(), conn);;
            LOG.info(" result of deleteClub = " + del);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteClub" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method deleteClub

public String deleteCourse(ECourseList ecl) throws SQLException, Exception
{    LOG.info("entering deleteTee for " + ecl.Ecourse.getIdcourse());
 try{
     DeleteCourse dc = new DeleteCourse();
     String del = dc.deleteCourse(ecl.Ecourse.getIdcourse(), conn); //.deleteHoles(ecl.Etee.getIdtee(), conn);
            LOG.info(" result of deleteCourse = " + del);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteCourse" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method deleteCourse

public String deleteTee(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering deleteTee for " + ecl.Etee.getIdtee());
 try{
     delete.DeleteTee dt = new delete.DeleteTee();
        String del = dt.deleteTee(ecl.Etee.getIdtee(), conn);
            LOG.info(" result of deleteTee = " + del);
        lists.CourseList.setListe(null);// reset
        listCourses(); // refresh list without the deleted item    
        return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteTee" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method deleteTee

public  int getCountHoles(int hol) throws SQLException{
    LOG.info("getCountHoles input = " + hol);
    find.FindCountHoles fch = new find.FindCountHoles();
    return fch.findCountHoles(hol,conn);
}

public String deleteHoles(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering deleteHoles for Tee = " + ecl.Etee.getIdtee());
 try{
     delete.DeleteHoles dh = new delete.DeleteHoles();
     String del = dh.deleteHoles(ecl.Etee.getIdtee(), conn);
       LOG.info(" result of deleteHoles = " + del);
      lists.CourseList.setListe(null);// reset
      listCourses(); // refresh list without the deleted item
     return "deleteClubCourseTee.xhtml?faces-redirect=true";
 }catch (Exception ex){
            String msg = "Exception in deleteTee" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method deleteHoles

    public Creditcard getCreditcard() {
        return creditcard;
    }

    public void setCreditcard(Creditcard creditcard) {
        this.creditcard = creditcard;
    }

public String loadCourse(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering loadCourse");
 try{
       LOG.info("ecl = " + ecl.toString());
       LOG.info("idcourse in loadCourse = " + ecl.Ecourse.getIdcourse() );
     load.LoadCourse lco = new load.LoadCourse();
     course = lco.LoadCourse(conn, ecl.Ecourse.getIdcourse() );
     load.LoadClub lcl = new load.LoadClub();
     club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
     LOG.info("idclub after loadCourse= " + club.getIdclub());  // si est null faut complémenter
     if(club.getIdclub() == null){
         club.setIdclub(course.getClub_idclub());
         LOG.info("Idclub forced because it was null ");
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
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

 public String modifyCourse() throws Exception // new 18/07/2013
    {   
        LOG.info("entering modifyCourse  "); // + clubcourseround);
        LOG.info("course to be modified = " + course.toString());
    ModifyCourse mc = new ModifyCourse();
    boolean OK = mc.modifyCourse(course, conn);
    if(OK)
    {
     String msg = "course Modified !! ";
     LOG.info(msg);
     LCUtil.showMessageInfo(msg);
    }
 return null;
    } // end modifyCourse

public String loadTee(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering loadTeee");
 try{
     LOG.info("loadTee entering ecl = " + ecl.toString());
     load.LoadTee lt = new load.LoadTee();
     tee = lt.LoadTee(conn, ecl.Etee.getIdtee() );
     load.LoadCourse lco = new load.LoadCourse();
     course = lco.LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
     load.LoadClub lcl = new load.LoadClub();
     club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.info("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null)
     {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.info("idcourse forced");
     }
    if(tee != null)
    {
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
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method loadCourse

public String loadHoles(ECourseList ecl) throws SQLException, Exception  // multiple Hole
{   LOG.info("entering loadHoles - multiple");
 try{
     load.LoadTee lt = new load.LoadTee();
     tee = lt.LoadTee(conn, ecl.Etee.getIdtee() );
     load.LoadHoles lh = new load.LoadHoles();
     holesGlobal = lh.LoadHolesArray(conn, tee.getIdtee());
     load.LoadCourse lco = new load.LoadCourse();
     course = lco.LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
     load.LoadClub lcl = new load.LoadClub();
     club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("course after loadHoles = " + course.toString());  // si est null faut coplémter
        LOG.info("tee after loadHoles = " + tee.toString());  // si est null faut coplémter
  //       course.setIdcourse(tee.getCourse_idcourse());
  //       LOG.info("idcourse forced");
      hole.setCreateModify(false); // gestion button dans club.xhtml
      return "modifyHoles.xhtml?faces-redirect=true&operation=modify holes";
 }catch (Exception ex){
            String msg = "Exception in loadHoles " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method loadHolee

 public String modifyHolesGlobal() throws Exception // new 13/08/2017
    {   
        LOG.info("entering modifyHolesGlobal  "); 
 //       LOG.info("holesGlobal - new holes values = " + holesGlobal.toString());
 //       LOG.info("tee = " + tee.toString());
 //       LOG.info("course = " + course.toString());
 
 /// solution de contournement pour créer en une seule fois tous les trous
 /// ne fonctionne pas encore !!!
   //   CreateHolesGlobal2 chg = new CreateHolesGlobal2(); 
   //   boolean OK = chg.createHoles(holesGlobal, tee, course, conn);
 // à restaurer !!
 
    ModifyHolesGlobal mhg = new ModifyHolesGlobal(); 
    boolean OK = mhg.updateHoles(holesGlobal, tee, conn);
 
 
    if(OK)
    {
         String msg = "Holes Modified !! ";
            LOG.info(msg);
         LCUtil.showMessageFatal(msg);
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
     CreateHolesGlobal chg = new CreateHolesGlobal(); 
     boolean OK = chg.createHoles(holesGlobal, tee, course, conn);
 // à restaurer !!
 
 //   ModifyHolesGlobal mhg = new ModifyHolesGlobal(); 
//    boolean OK = mhg.updateHoles(holesGlobal, tee, conn);
 
 
    if(OK)
    {
         String msg = "Holes Modified !! ";
            LOG.info(msg);
         LCUtil.showMessageFatal(msg);
     }
 return null;
    } // end modifyHolesGlobal
 
 
 
 
 
public String loadHole(ECourseList ecl) throws SQLException, Exception  // single Hole
{   LOG.info("entering loadTeee");
 try{
     load.LoadTee lt =new load.LoadTee();
     tee = lt.LoadTee(conn, ecl.Etee.getIdtee() );
     load.LoadCourse lco = new load.LoadCourse();
     course = lco.LoadCourse(conn, ecl.Ecourse.getIdcourse() ); // pour avoir coursename, etc...
     load.LoadClub lcl = new load.LoadClub();
     club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );  // pour avoir clubname, etc...
        LOG.info("idcourse after loadCourse= " + course.getIdcourse());  // si est null faut coplémter
        LOG.info("idtee after loadCourse= " + tee.getIdtee());  // si est null faut coplémter
     if(course.getIdcourse() == null)
     {
         course.setIdcourse(tee.getCourse_idcourse());
         LOG.info("idcourse forced");
     }
    if(tee != null)
    {
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
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method loadHolee


 public String modifyTee() throws Exception // new 18/07/2013
    {   //modify club from modifyClubCourseTee.xhtml
        LOG.info("entering modifyTee  "); // + clubcourseround);
        LOG.info("tee to be modified = " + tee.toString());
        ModifyTee mt = new ModifyTee();
        boolean OK = mt.modifyTee(tee, conn);
    if(OK)
    {
        tee.setNextTee(true); // affiche le bouton next(Course) bas ecran a droite
        LOG.info("tee Modified !!");
    }
 return null;
    } // end modifyTee

public String loadClub(ECourseList ecl) throws SQLException, Exception
{   LOG.info("entering loadClub");
 try{
 //    LOG.info("line01");
 //    LOG.info("ecl = " + ecl);
 //    LOG.info("line02");
    load.LoadClub lcl = new load.LoadClub();
    club = lcl.LoadClub(conn, ecl.Eclub.getIdclub() );
    // ajouter boolean = correct insert !!!
    if(club != null)
    {
        club.setCreateModify(false); // gestion button dans club.xhtml
        return "club.xhtml?faces-redirect=true&operation=modify";
    }else{
        // error in create player
        LOG.info("error : club not retreaved !!");
        return null;
    }
 }catch (Exception ex){
            String msg = "Exception in loadClub " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
} //end method loadClub


    public String modifyClub() throws Exception // new 18/07/2013
    {   //modify club from modifyClubCourseTee.xhtml
        LOG.info("entering modifyClub  "); // + clubcourseround);
        LOG.info("club to be modified = " + club.toString());
        ModifyClub mc = new ModifyClub();
        boolean OK= mc.modifyClub(club, conn);
    if(OK)
    {
        course.setNextCourse(false); // n'affiche PAS le bouton next(Course) bas ecran a droite
        LOG.info("club is wel Modified !!");
    }
 return null;
    } // end modifyClub


public void modifyPlayer() throws SQLException, Exception
{
 try{
     LOG.info("entering modiftPlayer");
     ModifyPlayer mp = new ModifyPlayer();
     boolean ok = mp.modifyPlayer(player, conn); // "A" signifie avec Activation (non en batch)
    // ajouter boolean = correct insert !!!
    if(ok)
    {
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
            LCUtil.showMessageFatal(msg);
  //          return null;
  }
} //en dmethod create player

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
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

      public String listParticipants_stableford(ECourseList ecl) throws SQLException {
        try {
                LOG.debug(" -- entering listParticipants_stableford = " + ecl.Eround.getIdround() );
                round.setIdround(ecl.Eround.getIdround());
                lists.ParticipantsStableford ps = new lists.ParticipantsStableford();
        //        listScramble = ps.listAllParticipants(round, conn);
                listStableford = ps.listAllParticipants(round, conn);
                   LOG.debug(" -- exiting listParticipants_stableford = ");
                   LOG.debug("liste participants stableford = " + Arrays.deepToString(listStableford.toArray()) );
         //          LOG.debug("liste participants stableford = " + Arrays.deepToString(listStableford.toArray())); //.toUpperCase());//.Eplayer.getPlayerLastName()); //+ Arrays.deepToString(listStableford.toArray()) );
  //            course.setIdcourse(inputPlayingHcp);
  
                round.setRoundGame(listStableford.get(0).Eround.getRoundGame());
           return "show_participants_stableford.xhtml?faces-redirect=true&cmd=SCR";
        } catch (Exception ex) {
            String msg = "Exception in listParticipants_scramble" + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
  
    public List<ClubCourseRound> __getRoundList() throws SQLException {   // rounds played by a player
        try {
            //  LOG.debug(" -- entering getRoundList = " + player.getIdplayer() );
            lists.__RoundList rl = new lists.__RoundList();
            return rl.getRoundList(player, conn);
    //return listccr;
        } catch (Exception ex) {
            String msg = "Exception in getRoundList() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

 public List<ECourseList> listCourses() throws SQLException {
   try {
       LOG.debug(" -- entering listCourses ");
       cptCourse = cptCourse +1;
       if(cptCourse == 1){
           setInputSelectCourse2(getInputSelectCourse());
       }
       LOG.info("course indicator = " + getInputSelectCourse2());
       
       lists.CourseList cl = new lists.CourseList();
       List<ECourseList> ec = cl.getCourseList(conn);
       return ec;
  //     return cl.getCourseList(conn);
    } catch (SQLException ex) {
            String msg = "Exception in listCourses() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method

     public List<ECourseList> listClubsCoursesTees() throws SQLException {
   try {
       LOG.debug(" -- entering listClubsCoursesTee ");
   //    List<ECourseList> lccr = new ArrayList<>();
    //   LOG.info("line 01");
       
   //    lccr = lists.CourseList.getCourseList(conn);
    //    LOG.info("line 02");
       lists.ClubCourseTeeList cctl = new lists.ClubCourseTeeList();
       return cctl.getCourseList(conn);
     //       return lists.CourseList.getCourseList(conn); 
    } catch (SQLException ex) {
            String msg = "Exception in listClubsCoursesTees() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
      //  DBConnection.closeQuietly(conn, null, rs, ps);
        }
    } //end method
  public List<ECourseList> listInscriptions() throws SQLException
  {
        try {
            LOG.debug(" -- entering listInscriptions with inputInscription = " + getInputInscription());
     //       return lists.InscriptionList.getInscriptionList(player, getInputInscription(), conn);
        lists.InscriptionList il = new lists.InscriptionList();
        return il.getInscriptionList(conn);
    // return lists.InscriptionList.getInscriptionList(player, conn);
         } catch (Exception ex) {
            String msg = "Exception in getInscriptionList() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method

  public String cancelInscription(ClubCourseRound ccr) throws Exception// throws SQLException
  {
      LOG.info(" starting cancelInscription ");
      LOG.info(" with CCR = " + ccr.toString());
      LOG.info(" for player = " + ccr.getIdplayer());
      LOG.info(" for player = " + ccr.getPlayerLastName());
      LOG.info(" for round = " + ccr.getIdround());
      player2.setIdplayer(ccr.getIdplayer()); // pour éviter confusin avec current player !!
      player2.setPlayerLastName(ccr.getPlayerLastName());
      player2.setPlayerFirstName(ccr.getPlayerFirstName());
      player2.setPlayerLanguage(ccr.getPlayerLanguage());
      player2.setPlayerCity(ccr.getPlayerCity());
      player2.setPlayerEmail(ccr.getPlayerEmail());
      LOG.info(" for course = " + ccr.getCourseName());
//      LOG.info("round =  + round.idround");
      round.setIdround(ccr.getIdround());
      round.setRoundGame(ccr.getRoundGame());
      round.setRoundDate(ccr.getRoundDate());
      
      delete.DeleteInscription di = new delete.DeleteInscription();
      di.deleteInscription(player2, round, ccr, conn);
      
      lists.ParticipantsStableford.setListe(null);  // reset
      lists.ParticipantsStableford ps = new lists.ParticipantsStableford();
      listStableford = ps.listAllParticipants(round, conn); // refresh list without the deleted item
   return "show_participants_stableford.xhtml?faces-redirect=true";  // refresh view without message !
  //    return null;  // back to originating view   ne refresh view but with message !
  }
 //   @NotNull // new 25/10/2015
    public List<ECourseList> listHandicaps() throws SQLException {
   //     public List<ClubCourseRound> listHandicaps() throws SQLException {
        try {
            LOG.debug(" -- entering getHandicapList = " + player.getIdplayer() );
            // listccr = ListController.getHandicapList(player.getIdplayer() );
         //   return ListController.getHandicapList(player.getIdplayer());
         lists.HandicapList hl = new lists.HandicapList(); 
         return  hl.getHandicapList(player, conn);
      } catch (Exception ex) {
            String msg = "Exception in getHandicapList() " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
        } finally {
        }
    } //end method
    
  public String cancelHandicap(ClubCourseRound ccr) throws Exception
  {
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
  public String cancelRound(ECourseList ecl) throws Exception
//    public String cancelRound(ClubCourseRound ccr) throws Exception       
  {
      LOG.info(" starting cancelRound ");
      LOG.info(" with ecl = " + ecl.toString());
 //     LOG.info(" for round Id = " + ccr.getIdround());
 //     LOG.info(" for round Date = " + ccr.getRoundDate());
      delete.DeleteRound dr = new delete.DeleteRound();
      dr.deleteRound(ecl.Eround.getIdround(), conn);
      
      lists.InscriptionList.setListe(null);  // reset
      listInscriptions();  // refresh list without the deleted item
    return "selectInscription.xhtml?faces-redirect=true";  // refresh view without message !
  }
    
public String selectPlayer(Player in_player) throws SQLException
{
try{
    LOG.info(" starting selectPlayer ");
 //   LOG.info(" starting selectPlayer = " + player.getIdplayer().toString());
        this.player =  in_player;
        // change language according to the user database language 
        LanguageController.setLanguage(player.getPlayerLanguage());
        LOG.info("Language Player = " + player.getPlayerLanguage());
  //      String msg = "Select Player Successful " + player.toString()
  //              + " <br/> idplayer   = " + player.getIdplayer()
  //              + " <br/> First name = " + player.getPlayerFirstName()
  //              + " <br/> last name = " + player.getPlayerLastName()
  //              + " <br/> country = " + player.getPlayerCountry()
  //              + " <br/> city = " + player.getPlayerCity();
  //      LOG.info(msg);
        String msg="selected Player = " + player.toString();
        LOG.info(msg);
        
     // new 27-08-2018 https://stackoverflow.com/questions/7644968/httpsession-how-to-get-the-session-setattribute
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("playerid", player.getIdplayer());
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("playerlastname", player.getPlayerLastName());
   // mod 07-08-2018     
        LOG.info("player password = " + player.getPlayerPassword());
        if(player.getPlayerPassword() == null){
            LOG.info("Yhe player has no password yet !! "); // + player.getPlayerPassword());
            return "password_create.xhtml?faces-redirect=true";
        }
       
        LOG.info("going to subscriptionStatus ");
        setConnected(true); // affiche le bouton Logout dans header.xhtml
    // charger sun rise and sunset pour le joueur
    
 ////       if(getSunRiseSet() == null)
 ////       {   LOG.info("Calling findSun from selectPlayer");
        
        /// à modifier ultérieurement
////            String s = findSun();
////            LOG.info("String s = " + s);
////            setSunRiseSet(s);
 ////       }else{
  ////          LOG.info("getSunRiseSet = " + getSunRiseSet());
 ////       }
// pour vérifier si subscription OK !!! et si pas OK afficher subscription.xhtml        
    //    return subscriptionStatus ( player); //, subscription);
        LOG.info("going to subscription status");
         find.FindSubscriptionStatus fss = new find.FindSubscriptionStatus();
     return fss.subscriptionStatus(subscription, player, conn); //, subscription);

  } catch (Exception e) {
            String msg = "££ Exception selectPlayer = " + e.getMessage() + " for player = " + player.getPlayerLastName();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }  
} // end method

public String findSun() throws SQLException, IOException
{
    // ajouter boolean = correct insert !!!
 //   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    LocalDate today = LocalDate.now();
////    String r = SunriseSunsetApiController.findSunriseSunset(dtf.format(today), player.getPlayerTimeZone().getTimeZoneId());
String r = "fake";
    LOG.info("string returned in findSun = " + r);
    return r;
  //  if(ok)
  //  {
   //     LOG.info("club created : we go to course !!");
    //    course.setNextCourse(true); // affiche le bouton next(Course) bas ecran a droite
    }
/*
     public String subscriptionStatus (Subscription subscription, Player player)
     {
     try{
         LOG.info("entering subcriptionStatus");
        subscr = find.FindSubscription.subscriptionDetail(player, conn);
    //        LOG.info("subdcription = " + subscr..toPrint());
        if(subscr == null)  // player non trouvé ??
            {  String msg = "££ pas de subscription record for player = " + player.getIdplayer();
                create.CreateSubscription.createSubscription(player, conn);
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                
            }
    //    LOG.info("after call findssubcription");
        LOG.info("subscription playerid " + subscr.get(0).getIdplayer());
     subscription.setStartDate(subscr.get(0).getStartDate() );
     subscription.setEndDate(subscr.get(0).getEndDate() );
     subscription.setTrialCount(subscr.get(0).getTrialCount() );
        LOG.info("subscription endDate " + subscription.getEndDate());
        LOG.info("subscription Trial Count " + subscription.getTrialCount());
       
      if(subscription.getTrialCount() > 5)
          {LOG.info("subscription Trial > 5 - Subscription Month of Year !!!");
            String msg = "Trial exceeded "
                                + " player = " + player.getIdplayer()
                                + " , trial  = <h1>" + subscription.getTrialCount() + "</h1>"
                                ;
             LOG.info(msg);
             LCUtil.showMessageInfo(msg);
             LOG.info("return subscription.xhtml");
            return "subscription.xhtml?faces-redirect=true";}
       LOG.info("LocalDate now() = " + LocalDate.now());
      if(LocalDate.now().isAfter(subscription.getEndDate()))
             {LOG.info("now is after endLocal - subscription not valid !!!");
             LOG.info("return subscription.xhtml");
            return "subscription.xhtml?faces-redirect=true";
        }else{
            LOG.info("now is BEFORE endLocal - subscription IS valid !!!");
            LOG.info("ret5urn welcome.xhtml");
            return "welcome.xhtml?faces-redirect=true";}  // le player est accepté
      }catch (Exception e){
            String msg = "££ Exception subscriptionStatus = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }         
    } //end method
*/
  public String getResultCreditCard() throws Exception {
      if(progressInteger.get() == 100){
          UUID uuid = UUID.randomUUID();
          CourseController.setShowButtonCreditCard(true);
          // le paiement est fait !
          creditcard.setPaymentOK(true);
            LOG.info("going back to modifySubscription !");
          modifySubscription();
          return "Creditcard Payment Done : " + uuid + " endate = \n" + subscription.getEndDate();
     }else{
          return "null";
  //    return progressInteger.get() == 100 ? "Creditcard Payment Done" : "";
 
  }
} // end method
//@Inject @HashAlgorithm(algorithm = Algorithm.SHA256); 
public String modifySubscription() throws Exception // called from subscription.xhtml
{     // testing purpose !!!
  
 try{
        LOG.info("entering modifySubcription ");
      subscription.setIdplayer(player.getIdplayer());
        LOG.info("Subcription = " + subscription.toString());
     
        ///   juste pour tester !!!
   //     String text = "Louis Collet 324713";
    //    HashGenerator hash = null;
   //     LOG.info("generated hash = " + hash.getHashText(text));
                
        
 if (!creditcard.isPaymentOK())
 {       
     if(subscription.getSubCode().equals("M")){
         creditcard.setTotalPrice(Double.parseDouble("15.0"));
      //   creditcard.setCommunication("Souscription Abonnement mensuel");
         creditcard.setCommunication(LCUtil.prepareMessageBean("subscription.month"));
     }
     if(subscription.getSubCode().equals("Y")){
         creditcard.setTotalPrice(Double.parseDouble("100.0"));
       //  creditcard.setCommunication("Souscription Abonnement annuel");
         creditcard.setCommunication(LCUtil.prepareMessageBean("subscription.year"));
     }
     if(subscription.getSubCode().equals("M") || subscription.getSubCode().equals("Y")){
          return "creditcard.xhtml?faces-redirect=true&cmd=subscription"; 
     }
 }   
    ModifySubscription msu = new ModifySubscription();
    boolean ok = msu.modifySubscription(subscription, conn);
    if(ok){
        LOG.info("subscription modified ! ");
      //  String s = subscriptionStatus (player); //, subscription);
        find.FindSubscriptionStatus fss = new find.FindSubscriptionStatus();
        String s = fss.subscriptionStatus(subscription, player, conn);
        LOG.info("subscription returned =  ! " + s);
        creditCardSubscriptionMail();
  //     String msg = "Successful Subscription for "
 //  //                        + " player = " + player.getIdplayer()
   //                    //    + " , new end date  = <h1>" + d + "</h1>"
 //                          ;
   //              LOG.info(msg);
     //            LCUtil.showMessageInfo(msg); 
       // return s;  // renvoie subscription.xhtml ou welcome.xhtml
       if(subscription.getSubCode().equals("M") || subscription.getSubCode().equals("Y")){
          return "creditcard_payment_executed.xhtml?faces-redirect=true&cmd=paid"; 
        }else{
          return "welcome.xhtml?faces-redirect=true&cmd=paid"; // trial one day
       }
     }else{
        LOG.error("error : subscription not modified !!");
        return null;
    }
 }catch (SQLException ex){
            String msg = "SQLException in modifySubscription " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }
// return null;
} //end method modify subscription

public String registereIDPlayer() // throws javax.smartcardio.CardException  // mod 03-12-2017
 {
    try{
        LOG.info("entering register eID Player");
    HandleSmartCard hsc = new HandleSmartCard();
    
    return hsc.formatPlayer();
 //   }catch (javax.smartcardio.CardException ex){
 //           String msg = "Card Exception in registereIDPLayer " + ex;
 //       /    LOG.error(msg);
 //           LCUtil.showMessageFatal(msg);
 //           return null;
    }catch (Exception ex){
            String msg = "Exception in registereIDPLayer " + ex;
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null;
  }

}  // end method    


public void onRowToggle(ToggleEvent event) {
        FacesMessage msg = new FacesMessage(
                "Row State " + event.getVisibility()
                + " , Date Round : " + ((ClubCourseRound) event.getData()).getRoundDate());
     //       LOG.info(msg);
     //       LCUtil.showMessageInfo(msg);
    FacesContext.getCurrentInstance().addMessage(null, msg);
    }

public void rowPlayerSelect(SelectEvent event) {
        String msg = "size selected players = " + player.getSelectedOtherPlayers().size();
        LOG.info(msg);
        LCUtil.showMessageInfo(msg);
        if (player.getSelectedOtherPlayers().size() > 3) {
  ///          player.getSelectedOtherPlayers().remove(event.getObject());
            msg = "You cannot selected more than 3";
            LOG.info(msg);
            LCUtil.showMessageInfo(msg);
     //       RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage());

    //        return;
        }
    }

public String login() throws IOException, SQLException {
        LOG.info("entering login coming from login.xhtml");
        // faire ici l'enregistrement du logout dans audit_in_out
      //  LCUtil.stopAuditLogin(Integer.toString(player.getIdplayer()));
    //    utils.Audit.stopAuditLogin(player, conn);
        listeners.MySessionCounter msc = new listeners.MySessionCounter(); 
      //  msc.
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
   //     msc.sessionInvalidate(ec);
            LOG.info("browser language = " + ec.getRequestLocale());
            LOG.info("session buffersize = " + ec.getResponseBufferSize());
            LOG.info("session character encoding = " + ec.getRequestCharacterEncoding());
            LOG.info("session timeout = " + ec.getSessionMaxInactiveInterval() );
        reset("from login"); // new 28/09/2014
        player = new Player(); // new 28/09/2014
            LOG.info("from login : player initialized !!!");
        ec.invalidateSession();
            LOG.info("session invalidated !! - going back to login.xhtml " );
    //    ec.redirect(ec.getRequestContextPath() + "/sessionExpired.xhtml?cmd=endSession");
 //    return "login_securityAPI.xhtml";  // back to login.xhtml
     return null;
  //      return "login.xhtml?faces-redirect=true";  // old - doesn't work
    } // end method

public String logout() throws IOException, SQLException {
        LOG.info("End of session for player = " + player.getIdplayer());
        // faire ici l'enregistrement du logout dans audit_in_out
      //  LCUtil.stopAuditLogin(Integer.toString(player.getIdplayer()));
        utils.Audit.stopAuditLogin(player, conn);
      //  listeners.MySessionCounter msc = new listeners.MySessionCounter(); 
        
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
   //     msc.sessionInvalidate(ec);
       //     LOG.info("browser language = " + ec.getRequestLocale());
       //     LOG.info("session buffersize = " + ec.getResponseBufferSize());
       //     LOG.info("session character encoding = " + ec.getRequestCharacterEncoding());
       //     LOG.info("session timeout = " + ec.getSessionMaxInactiveInterval() );
        reset("from logout"); // new 28/09/2014
        player = new Player(); // new 28/09/2014
            LOG.info("from logout : player initialized !!!");
        ec.invalidateSession();
            LOG.info("session invalidated !! - going to sessionExpired.xhtml " );
        ec.redirect(ec.getRequestContextPath() + "/sessionExpired.xhtml?cmd=endSession");

     return null;
  //      return "login.xhtml?faces-redirect=true";  // old - doesn't work
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
        if ((!isPostback()) && (club.getIdclub() != null));
        //    postback = false
        {
            club = new Club();
            LOG.info("preRenderClub : club forced to null ");
        }
    }

    public void preRenderCourse() {
        LOG.info("preRenderCourse called");
    }

    public String CurrentTimeWithZoneOffset()
    {
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
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }
    }  //end method
            
    public ClubCourseRound getSelectedCourse() {
         LOG.info("getSelectedCourse = ..." + selectedCourse);
        return selectedCourse;
    }

    // new 26/05/2017 used in inscription.xhtml : on n'affiche que les tee existants pour le course
    public List<String> getTeeStartListe() throws SQLException {
         LOG.info("getTeeStartArray = ..." );
     try{   
         find.FindTeeStart fts = new find.FindTeeStart();
         teeStartListe = fts.teeStart(course, conn);
            LOG.info("TeeStartListe = " + teeStartListe);
            return teeStartListe;  // YELLOW, BLUE ...
      } catch (Exception e) {
            String msg = "££ Exception in getTeeStartListe ... = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return null; // indicates that the same view should be redisplayed
        } finally {

        }
    } //end method
    
    
    public void setSelectedCourse(ClubCourseRound selectedCourse) {
        this.selectedCourse = selectedCourse;
         LOG.info("setSelectedCourse = ..." + this.selectedCourse);
    }
    

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
//public void testFindSlopeRating() throws SQLException, Exception{
    // for testing purposes 3/2/2015
//    find.FindSlopeRating.main("sss");
//}

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


    public List<ClubCourseRound> listScramble() {
     //   LOG.info(" -- listScramble - entry index = " );
        return  listScramble;
    } //end method

      public List<ECourseList> listStableford() {
     //   LOG.info(" -- listScramble - entry index = " );
        return listStableford;
    } //end method
      
     public List<ClubCourseRound> listScramble1() {
      //  LOG.info(" -- listScramble1 - entry index = ");
      //       listScramble1 = listScramble.subList(0, 1);// 1ère ligne uniquement
      //       LOG.info(" -- subList listScramble1 = " + Arrays.toString(listScramble1.toArray() ) );
      // subList Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive
            return  listScramble.subList(0, 1); 
    } //end method
    
         public List<ECourseList> listStableford1() {  // uniquement la 1ére ligne
      //  LOG.info(" -- listScramble1 - entry index = ");
      //       listScramble1 = listScramble.subList(0, 1);// 1ère ligne uniquement
      //       LOG.info(" -- subList listScramble1 = " + Arrays.toString(listScramble1.toArray() ) );
      // subList Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive
            return  listStableford.subList(0, 1); 
    } //end method
     
     
   public void startTask(ActionEvent ae) {
      executorService = Executors.newSingleThreadExecutor();
      executorService.execute(this::startLongTask);
  }

  private void startLongTask() {
      progressInteger.set(0);
      for (int i = 0; i < 100; i++) {
          progressInteger.getAndIncrement();
          //simulating long running task
          try {
              Thread.sleep(ThreadLocalRandom.current().nextInt(1, 100));
          } catch (InterruptedException e) {
              LOG.info("Exception in startLongTask");
          }
      }
      executorService.shutdownNow();
      executorService = null;
  }

  public int getProgress() {
      return progressInteger.get();
  }


     
     
   public void main(String args[]) throws InstantiationException, SQLException, ClassNotFoundException, IllegalAccessException {
        //    allNull(cl);
        LOG.info(" -- main terminated");
    } // end main
} // end class