package create;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import entite.Course;
import entite.TarifGreenfee;
import static interfaces.GolfInterface.SDF;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateTarifGreenfee {
//@JsonPropertyOrder({"datesSeason","days","teeTimes","priceEquipments"}) // new 23/01/2019 ajouté, était dans entite TarifGreenfee
    public boolean create(final TarifGreenfee tarif, final Course course, final Connection conn) throws SQLException, Exception {
        PreparedStatement ps = null;
        try{
            LOG.info("starting create Tarif "); 
            LOG.info("with tarif = " + tarif.toString());
            LOG.info("for course = " + course.toString());
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);//Set pretty printing of json
        tarif.RemoveNull(); // remove null from arrays
        String json = om.writeValueAsString(tarif);
            LOG.info("tarif converted in json format = " + json);
            
            /// just to verify if working
////        Tarif t = om.readValue(json,Tarif.class); //, Criteria.class);
////            LOG.info("tarif restored from json format = " + NEW_LINE +t.toString());
   LOG.info(" tarif : DatesSeason =  " + Arrays.deepToString(tarif.getDatesSeason()));
   LOG.info(" tarif : DatesSeason length =  " + tarif.getDatesSeason().length);
   if(tarif.getDatesSeason().length == 0){
       String msgerr = LCUtil.prepareMessageBean("create.greenfee.season.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
        return false;
     }
      if(tarif.getPriceEquipments().length == 0){
       String msgerr = LCUtil.prepareMessageBean("create.greenfee.equipments.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       return false;
     }
      
   LOG.info(" length : Seasons =  " + tarif.getDatesSeason().length);
   LOG.info(" length : Days =  " + tarif.getDays().length);
   LOG.info(" length : Hours =  " + tarif.getTeeTimes().length);
   LOG.info(" length : Equipment =  " + tarif.getPriceEquipments().length);
   LOG.info(" length : Greenfee =  " + tarif.getPriceGreenfees().length);
   
   if(tarif.getDatesSeason().length == 0 
     && tarif.getDays().length == 0
     && tarif.getTeeTimes().length == 0
     && tarif.getPriceEquipments().length == 0
     && tarif.getPriceGreenfees().length == 0){
       String msgerr =  LCUtil.prepareMessageBean("create.greenfee.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       throw new Exception(msgerr);
     }
   LOG.info("line 01");
   java.util.Date ddeb = SDF.parse(tarif.getDatesSeason()[0][0]);  // première ligne, première colonne
        LOG.info("Date format ddeb = " + ddeb);
   LocalDateTime lddeb = utils.LCUtil.DatetoLocalDateTime(ddeb);
        LOG.info("LocalDateTime format ddeb = " + lddeb);
   tarif.setDatesSeason(utils.LCUtil.removeNull2D(tarif.getDatesSeason()));  // sinon sernière date = null donc 01-01-1970
   int le = tarif.getDatesSeason().length;
   java.util.Date dfin = SDF.parse(tarif.getDatesSeason()[le - 1][1]);  // dernière ligne, 2e colonne=date de fin
     LOG.info("Date format dfin from dernière ligne = " + dfin);
   LocalDateTime ldfin = utils.LCUtil.DatetoLocalDateTime(dfin);
        LOG.info("LocalDateTime format dfin = " + ldfin);
      
   final String query = LCUtil.generateInsertQuery(conn, "tarif_greenfee");
    //        LOG.info("generated query = " + query);
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
               java.sql.Timestamp ts = Timestamp.valueOf(lddeb);
               LOG.info("ts lddeb = " + ts.toString() );
            ps.setTimestamp(2,Timestamp.valueOf(lddeb));
       //        ts = Timestamp.valueOf(ldfin);
       //        LOG.info("ts ldfin = " + ts.toString() );
            ps.setTimestamp(3,Timestamp.valueOf(ldfin));
            ps.setInt(4,course.getIdcourse()); 
            ps.setString(5,json);
            ps.setTimestamp(6,Timestamp.from(Instant.now()));

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
             LOG.info("row  = " + row);
               String msg = "Tarif Created for = "
                  //      + course.getIdcourse() + "</h1>"
                        // + "<br/>name club = " + club.getClubName()
                        + " Course = " + course.getIdcourse()
                        + "(" + course.getCourseName() + ")";
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
             return true;
   }catch (Exception e){
            String msg = "£££ Exception in CreateTarifGreenfee = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            throw new Exception(msg);
     //       return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
          }
   } // end main//
} // en class