package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import entite.Club;
import entite.Greenfee;
import entite.TarifGreenfee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Locale;
import utils.DBConnection;
import utils.LCUtil;

//@Named  enlevé 26-04-2022
public class CreateTarifGreenfee {
     private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
//@JsonPropertyOrder({"datesSeason","days","teeTimes","priceEquipments"}) // new 23/01/2019 ajouté, était dans entite TarifGreenfee
     
 public boolean create(final TarifGreenfee tarif, final Club club, final Connection conn) throws SQLException, Exception {
        final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
        PreparedStatement ps = null;
 try{
            LOG.debug("starting " + methodName); 
            LOG.debug("with tarif = " + tarif);
            LOG.debug("for club = " + club);

            /// just to verify if working
  //    Tarif t = om.readValue(json,Tarif.class); //, Criteria.class);
////            LOG.debug("tarif restored from json format = " + NEW_LINE +t.toString());
 //  LOG.debug(" tarif : DatesSeason =  " + tarif.getDatesSeasonsList());
 //  LOG.debug(" tarif : DatesSeason length =  " + tarif.getDatesSeasonsList().size());
   
//   LOG.debug(" tarif : DatesSeason new =  " + tarif.getDatesSeasonsList());
//   LOG.debug(" tarif : DatesSeason length new =  " + tarif.getDatesSeasonsList().size());
/*
    Locale locale = Locale.of("", club.getAddress().getCountry().getCode());
    LOG.debug(" Locale calculated = " + locale);
     LOG.debug("locale symbol = " + Currency.getInstance(locale).getSymbol()); // USD
    LOG.debug("locale currency = " + Currency.getInstance(locale)); // USD
        LOG.debug("locale symbol = " + Currency.getInstance(locale).getSymbol()); // USD
        LOG.debug("currency code = " + Currency.getInstance(locale).getCurrencyCode()); // USD
        LOG.debug("display name = " + Currency.getInstance(locale).getDisplayName()); // USD
        LOG.debug("display name locale = " + Currency.getInstance(locale).getDisplayName(locale)); // USD
  */   

 LOG.debug("currency code = " + club.getAddress().getCountry().getCurrency());

    if(tarif.getDatesSeasonsList().isEmpty()){    
       String msgerr = LCUtil.prepareMessageBean("create.greenfee.season.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       return false;
    }
    if(tarif.getEquipmentsList().isEmpty()){
       String msgerr = LCUtil.prepareMessageBean("create.greenfee.equipments.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       return false;
    }
  // new 09/06/2022
    if(tarif.getTwilightList().isEmpty() && tarif.isTwilightReady() ){
       String msgerr = LCUtil.prepareMessageBean("create.greenfee.twilight.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       return false;
    }
 
 //  LOG.debug(" length : Seasons =  " + tarif.getDatesSeasonsList().size());
 //  LOG.debug(" length : Days =  " + tarif.getDays().length);
 //  LOG.debug(" length : Hours =  " + tarif.getTeeTimesList().size());
 //  LOG.debug(" length : Equipment =  " + tarif.getEquipmentsList().size());
 //  LOG.debug(" length : Greenfee =  " + tarif.getBasicList().size());
   
   if(tarif.getDatesSeasonsList().isEmpty()
     && tarif.getDaysList().isEmpty()
     && tarif.getTeeTimesList().isEmpty()
     && tarif.getEquipmentsList().isEmpty() // 04/05/2022
     && tarif.getBasicList().isEmpty()){
       String msgerr =  LCUtil.prepareMessageBean("create.greenfee.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       throw new Exception(msgerr);
     }
   
      // à modifier première et dernière date si introduite chronologiquement !!
    LocalDateTime lddeb = tarif.getDatesSeasonsList().get(0).getStartDate().truncatedTo(ChronoUnit.DAYS);
    int j = tarif.getDatesSeasonsList().size() -1;
    LocalDateTime ldfin = tarif.getDatesSeasonsList().get(j).getStartDate().truncatedTo(ChronoUnit.DAYS);
           LOG.debug("dfin format LocalDateTime = " + ldfin);
           
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());  // traiter LocalDateTime format 
 //       om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    om.configure(SerializationFeature.INDENT_OUTPUT, true);//Set pretty printing of json
    String json = om.writeValueAsString(tarif);
       LOG.debug("tarif converted in json format = " + json);        
 
    final String query = LCUtil.generateInsertQuery(conn, "tarif_greenfee");
            ps = conn.prepareStatement(query);
            ps.getWarnings();
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
            ps.setInt(2, lddeb.getYear());  // à modifier : saisons à cheval sur deux années civiles ??
            ps.setTimestamp(3,Timestamp.valueOf(lddeb));
            ps.setTimestamp(4,Timestamp.valueOf(ldfin));
            ps.setInt(5,tarif.getTarifCourseId());
            ps.setString(6,json);
            ps.setString(7,club.getAddress().getCountry().getCurrency());
            LOG.debug("currency code = " + club.getAddress().getCountry().getCurrency());
            ps.setTimestamp(8,Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
            LOG.debug("row  = " + row);
            if (row != 0) {
                String msg = "Tarif Created for Course = " + tarif.getTarifCourseId()
                       + "<br/>tarif = " + tarif;
          //      String msg =  LCUtil.prepareMessageBean("round.created")
          //               String msg = "Tarif Created for Course = " + tarif.getTarifCourseId()
          //             + "<br/>tarif = " + tarif;
                LOG.debug(msg);
                LCUtil.showMessageInfo(msg);
                return true;
            } else {
                String msg = "<br/>NOT NOT Successful TarifGreenfee inserted for " + tarif.getTarifCourseId();
                LOG.error(msg);
                LCUtil.showMessageFatal(msg);
                return false;
            }
   } catch (SQLException sqle) {
            String msg = "££ SQL exception in CreateTarifGreenfee = " + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }catch (Exception e){
            String msg = "££ Exception in CreateTarifGreenfee = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
  //          throw new Exception(msg);
            return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
   }
 } // create
 
  
public static void main(String args[])throws SQLException, Exception{     
     Connection conn = new DBConnection().getConnection();
  try{
  //  Greenfee g = new Greenfee();
  //  g.setIdclub(151); // Iberostar Playa Paraiso club country = MX
   // CreateTarifGreenfee ctg = new CreateTarifGreenfee();
    TarifGreenfee tarif = new TarifGreenfee();
    Club club = new Club();
    club.setIdclub(151);
    club = new read.ReadClub().read(club, conn);
    LOG.debug("club = " + club);
    boolean b = new CreateTarifGreenfee().create(tarif, club, conn);
    LOG.debug("result = " + b);
    
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
        DBConnection.closeQuietly(conn, null, null , null);
          }
 } // end main//
} // en class