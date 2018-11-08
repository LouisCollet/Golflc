
package calc;

import entite.Club;
import entite.Course;
import entite.Player;
import entite.Round;
import entite.Tarif;
import find.FindTarifData;
import java.sql.Connection;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.DatetoLocalDate;
// rester à faire le cas des invités / guests
// a faire : greenfee 0 pour memebre du club ! via une table member : greenfee 0 pour les membres du club
public class CalcTarifGreenfee implements interfaces.GolfInterface, interfaces.Log{

  public double greenFee (Tarif tarif, Round round, Club club, Player player)
{
     LOG.info(" -- Start of greenFee with Tarif = " + tarif.toString());
     LOG.info(" -- Start of greenFee with round = " + round.toString());
     LOG.info(" -- Start of greenFee with club = " + club.toString());
     LOG.info(" -- Start of greenFee with playerBirthDate = " + player.getPlayerBirthDate()); //toString());
 try {
 //       LOG.info("printing first elem");
     if(tarif.getDays().length > 0){
        LOG.info("first elem days      = " + tarif.getDays()[0][0]);
     }
     if(tarif.getDatesSeason().length > 0){
        LOG.info("first elem period    = " + tarif.getDatesSeason()[0][0]);
     }
     if(tarif.getTeeTimes().length > 0){
        LOG.info("first elem teeTimes    = " + tarif.getTeeTimes()[0][0]);
     }
     if(tarif.getPriceEquipments().length > 0){
        LOG.info("first elem equipment = "+ tarif.getPriceEquipments()[0]);
     }
 //    LOG.info("line 01");
     
     LocalDate ldround = round.getRoundDate().toLocalDate();
        LOG.info("LocalDate ldround = " + ldround);
     LocalTime tround = round.getRoundDate().toLocalTime();
        LOG.info("LocalTime tround = " + tround);
   //  LocalDate lddob = utils.LCUtil.asLocalDate(player.getPlayerBirthDate(), ZoneId.systemDefault());
     LocalDate lddob = DatetoLocalDate(player.getPlayerBirthDate()); //, ZoneId.systemDefault());
        LOG.info("LocalDate lddob = " + lddob);
//1.    // l'ordre a de l'importance : il faut commencer par Days !!  il utilise les 2 premieres cells de dates Season qui existe donc ! 
     if(tarif.getDays().length > 0){
  //          LOG.info("first elem days      = " + tarif.getDays()[0][0]);
            String typePlayer = "A";   // Adult
            Double priceDays = findDays(tarif, ldround, typePlayer, club.getClubCountry(), lddob); // FRIDAY, WEEKEND, WEEK, HOLIDAY
            LOG.info(" priceDays = " + priceDays);
            return priceDays;
     }
//2. 
        
     if(tarif.getDatesSeason().length > 0 || tarif.getTeeTimes().length > 0){   
        String season = findPeriod(tarif,ldround);
            LOG.info("Season searched = " + season);
        double priceHours = findHours(tarif, tround, season);
            LOG.info("priceHours = " + priceHours);
        return priceHours;
     }

 return 888.8;

 } catch (Exception e) {
      String msg = " -- Error in calcTarif " + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return 0.0;
 }
 finally
 {
   // LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
} // end method setNewHandicap

    
 public String findPeriod (Tarif tarif, LocalDate ldround)
{
   //  LOG.info(" -- Start of calcTarif with Tarif = " + tarif.toString());
  //   LOG.info(" -- Start of calcTarif with round = " + round.toString());
    // 0 = ddeg
    // 1 = dfin
    // 2 = season H, M or L
      LOG.info("array datesSeason = " + Arrays.deepToString(tarif.getDatesSeason()));
 try
 {
  String season = "H";  // default
     for (int row = 0; row < tarif.getDatesSeason().length; row++)
        {
       //   LOG.info("length row = " + tarif.getDatesSeason().length);
                 if(tarif.getDatesSeason()[row][0] == null){
                    break;
                }else{
                    LocalDate ddeb = LocalDate.parse(tarif.getDatesSeason()[row][0],ZDF_DAY);
                    LocalDate dfin = LocalDate.parse(tarif.getDatesSeason()[row][1],ZDF_DAY);
                    
                    if (ldround.isEqual(ddeb)
                     || ldround.isEqual(dfin)
                     || (ldround.isAfter(ddeb) && (ldround.isBefore(dfin))))
                    {
                        LOG.info("Found in datesSeason for !!= " + ldround);
            // alors on a la période code prix !!H M L 
                        
                        season = tarif.getDatesSeason()[row][2];
                        LOG.info("season = " + season);
                        LOG.info("ddeb = " + ddeb);
                        LOG.info("dfin = " + dfin);
                //        break;
                    }  
                } // end else
        } // end for
          
 return season;
 } catch (Exception e) {
     
     // LOG.info(" -- Error in calcNewHandicap" + e);
      String msg = " -- Error in calcTarif" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return null;
 }
 finally
 {

 }
} // end method 
  
   public double findHours (Tarif tarif, LocalTime tround, String season)
{
   //  LOG.info(" -- Start of calcTarif with Tarif = " + tarif.toString());
  //   LOG.info(" -- Start of calcTarif with round = " + round.toString());
 try
 {
     LOG.info("before search teeTimes " );
     LOG.info("array teeTimes = " + Arrays.deepToString(tarif.getTeeTimes()));
    // 0 = deb
    // 1 = dfin
    // 2 = price H
    // 3 = price M
    // 4 = price L
     double price = 0;
        for (int row = 0; row < tarif.getTeeTimes().length; row++)
        {
                 if(tarif.getTeeTimes()[row][0] == null){
                    break;
                }else{
                    LocalTime tdeb = LocalTime.parse(tarif.getTeeTimes()[row][0],ZDF_HOURS);
                    LocalTime tfin = LocalTime.parse(tarif.getTeeTimes()[row][1],ZDF_HOURS);
                    
                    if (tround.equals(tdeb)
                     || tround.equals(tfin)
                     || (tround.isAfter(tdeb) && (tround.isBefore(tfin))))
                    {
                        LOG.info("Found in teeTimes for !!= " + tround);
                        LOG.info("tdeb = " + tdeb);
                        LOG.info("tfin = " + tfin);
                        
                switch(season)
                {
                    case "H":
                        price = Double.valueOf(tarif.getTeeTimes()[row][2]);
                        LOG.info("case H");
                        break;
                    case "M":
                         price = Double.valueOf(tarif.getTeeTimes()[row][3]);
                         LOG.info("case M");
                        break;
                    case "L":
                         price = Double.valueOf(tarif.getTeeTimes()[row][4]);
                         LOG.info("case L");
                        break;
                    default:
                        LOG.info("price not found !!!= ");
                } //end switch
                    }  // end if
                } // end else
        } // end for
   LOG.info("at the end the price is = " + price);
          
 return price;

 } catch (Exception e) {
     
      String msg = " -- Error in calcTarif" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return 0.0;
 }
 finally
 {
   // LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
} // end method hours
  
  public Double findDays (Tarif tarif, LocalDate ldround, String typeplayer, String country, LocalDate lddob)
{   // 0 = "A"dult
    // 1 = "G"uest
    // 2 = "J"unior
 try
 {
     LOG.info("entering search days " );
     LOG.info("array days = " + Arrays.deepToString(tarif.getDays()));
     LOG.info("typepplayer = " + typeplayer);
     
// Junior ??
     if(Period.between(lddob, ldround).getYears() < 18){    // age < 18 ans
           typeplayer = "J"; // Junior
           LOG.info("typepplayer modified => Junior " + typeplayer);
       }
     
     int i = 0;
     String day = null;
     double price = 0;
        // is it an holiday ?
     utils.Holidays ho = new utils.Holidays();
     boolean ok = ho.CountryHolidays(ldround, country.toUpperCase());   // BE, ES ...
     if (ok){
        LOG.info("this is an Holiday !! " + ldround);
        day = "HOLIDAY";
        i = 3;
     }else{
        DayOfWeek dayOfWeek = ldround.getDayOfWeek();
            LOG.info("dayOfWeek Name = " + dayOfWeek.name());
//        int dayOfWeekIntValue = dayOfWeek.getValue(); // 6
//            LOG.info("dayOfWeekIntValue = " + dayOfWeekIntValue);
       switch(dayOfWeek)
       {            case MONDAY:
                         day = "MONDAY";
                         i = 0;
                        break;
                    case FRIDAY:
                         day = "FRIDAY";
                         i = 2;
                        break;
                    case SATURDAY: case SUNDAY:
                         day = "WEEKEND";
                         i = 3;
                        break;
                    default:  // autres jours de la semaine
                        day = "WEEK";
                        i = 1;
       } //end switch
    
     } // end else
 //   LOG.info("at the end the index is = " + index);
    LOG.info(" the day is = " + day);
    LOG.info(" the index is = " + i);
    

    switch(typeplayer)
    {
      case "A":   // Adult
           price = Double.valueOf(tarif.getDays()[i][0]);
           break;
      case "G":   // Guest
           price = Double.valueOf(tarif.getDays()[i][1]);
            break;
      case "J":   // Junior
           price = Double.valueOf(tarif.getDays()[i][2]);
            break;      
      default:
          LOG.info("Type player not found !");
          price = 999.9;
    }
   LOG.info("DAYS at the end the price is = " + price);
   return price;

 } catch (Exception e) {
     
      String msg = " -- Error in calcTarif" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return 0.0;
 }
 finally
 {

 }
} // end method hours
  
  
  
   public double findEquipments (Tarif tarif)
{
     LOG.info(" -- Start of calcTarif with Tarif = " + tarif.toString());
  //   LOG.info(" -- Start of calcTarif with round = " + round.toString());
    
double price = 0.0;
 try
 {
  
 return price;

 } catch (Exception e) {
     
     // LOG.info(" -- Error in calcNewHandicap" + e);
      String msg = " -- Error in calcTarif" + e.getMessage();
      LOG.error(msg);
      LCUtil.showMessageFatal(msg);
     return 0.0;
 }
 finally
 {
   // LOG.info(" -- New Handicap = " + LCUtil.myRound(newHcp,2));
 }
} // end method setNewHandicap
    
    
    
public static void main(String[] args) throws ParseException, Exception //throws SQLException // testing purposes
{ LOG.info("starting main");

DBConnection dbc = new DBConnection();
Connection conn = dbc.getConnection();
    try{
Course course = new Course();
course.setIdcourse(1); // 102=santana  1=americain Tournette

FindTarifData ft = new FindTarifData();
Tarif tarif = ft.findCourseTarif(course, conn);
    LOG.info("main tarif = " + tarif.toString());

Round round = new Round(); 
round.setRoundDate(LocalDateTime.of(2018, Month.FEBRUARY, 17, 12, 15));
Club club = new Club();
club.setClubCountry("ES");
Player player = new Player();
//SDF.parse("01/03/2010");
player.setPlayerBirthDate(SDF.parse("01/03/2010"));
CalcTarifGreenfee ct = new CalcTarifGreenfee();
double dd = ct.greenFee(tarif, round, club, player);
LOG.info("price greenfee = " + dd);
        
 } catch (Exception e) {
            String msg = "££ Exception in main CalcTarif= " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null,null); 
          }
  
//LOG.info(" -- Bravo ! Voici votre nouveau Handicap : = " + hcp );

}// end main    

} //end class