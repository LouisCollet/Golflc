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
import java.time.LocalDateTime;
import java.util.Arrays;
import javax.inject.Named;
import utils.DBConnection;
import utils.LCUtil;

@Named
public class CreateTarifGreenfee {
    
//@JsonPropertyOrder({"datesSeason","days","teeTimes","priceEquipments"}) // new 23/01/2019 ajouté, était dans entite TarifGreenfee
    public boolean createTarif(final TarifGreenfee tarif, final Course course, final Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try{
            LOG.info("starting create Tarif "); 
            LOG.info("with tarif = " + tarif.toString());
            LOG.info("for course = " + course.toString());
        ObjectMapper om = new ObjectMapper();
   // 	om.enable(SerializationFeature.INDENT_OUTPUT);//Set pretty printing of json
        om.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        om.configure(SerializationFeature.INDENT_OUTPUT, true);
         tarif.RemoveNull(); // remove null from arrays
        String json = om.writeValueAsString(tarif);
            LOG.info("tarif converted in json format = " + json);
            
            /// just to verify if working
////        Tarif t = om.readValue(json,Tarif.class); //, Criteria.class);
////            LOG.info("tarif restored from json format = " + NEW_LINE +t.toString());
   //     conn = DBConnection.getConnection();
   LOG.info(" tarif : DatesSeason =  " + Arrays.deepToString(tarif.getDatesSeason()));
   LOG.info(" tarif : DatesSeason length =  " + tarif.getDatesSeason().length);
   LOG.info(" tarif : Days =  " + tarif.getDays().length);
   LOG.info(" tarif : Hours =  " + tarif.getTeeTimes().length);
   LOG.info(" tarif : Equipment =  " + tarif.getPriceEquipments().length);
   if(tarif.getDatesSeason().length == 0 
     && tarif.getDays().length == 0
     && tarif.getTeeTimes().length == 0
     && tarif.getPriceEquipments().length == 0)
     {
       String msgerr =  LCUtil.prepareMessageBean("tarif.empty");
       LOG.error(msgerr);
       LCUtil.showMessageFatal(msgerr);
       throw new Exception(msgerr);
     }
   
   java.util.Date ddeb = SDF.parse(tarif.getDatesSeason()[0][0]);  // première ligne, première colonne
        LOG.info("Date ddeb = " + ddeb);
   LocalDateTime lddeb = utils.LCUtil.DatetoLocalDateTime(ddeb);
        LOG.info("LocalDateTime ddeb = " + lddeb);
   int le = tarif.getDatesSeason().length;
   java.util.Date dfin = SDF.parse(tarif.getDatesSeason()[le - 1][1]);  // dernière ligne, 2e colonne
//        LOG.info("LocalDateTime date dernière date = " + dfin);
   LocalDateTime ldfin = utils.LCUtil.DatetoLocalDateTime(dfin);
        LOG.info("LocalDateTime dfin = " + ldfin);
      
   final String query = LCUtil.generateInsertQuery(conn, "tarif_greenfee");
    //        LOG.info("generated query = " + query);
            ps = conn.prepareStatement(query);
            ps.setNull(1,java.sql.Types.INTEGER);  //autoincrement
               java.sql.Timestamp ts = Timestamp.valueOf(lddeb);
            ps.setTimestamp(2,ts);
               ts = Timestamp.valueOf(ldfin);
            ps.setTimestamp(3,ts);
            ps.setInt(4,course.getIdcourse()); 
            ps.setString(5,json);
            ps.setTimestamp(6,LCUtil.getCurrentTimeStamp());

            utils.LCUtil.logps(ps); 
            int row = ps.executeUpdate(); // write into database
             LOG.info("row  = " + row);
               String msg = "<br/><br/><h1>Tarif Created for = "
                  //      + course.getIdcourse() + "</h1>"
                        // + "<br/>name club = " + club.getClubName()
                        + "<br/>Course = " + course.getIdcourse()
                        + " / " + course.getCourseName();
                LOG.info(msg);
                LCUtil.showMessageInfo(msg);
             return true;
   }catch (Exception e){
            String msg = "£££ Exception in CreateTarifGreenfee = " + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
            return false;
   }finally{
         DBConnection.closeQuietly(null, null, null, ps); 
          }
   } // end main//
} // en class