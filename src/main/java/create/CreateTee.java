package create;

import entite.Course;
import entite.Tee;
import entite.ValidationsLC;
import entite.ValidationsLC.ValidationStatus;
import static interfaces.Log.LOG;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import utils.DBConnection;
import utils.LCUtil;
import static utils.LCUtil.showMessageFatal;
import static utils.LCUtil.showMessageInfo;

public class CreateTee{
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public boolean create(
      //  final Club club,
        final Course course, final Tee tee,final Connection conn) throws SQLException{
   final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
        PreparedStatement ps = null;
 try {
            LOG.debug("... starting " + methodName);
        //    LOG.debug("with club = " + club);
            LOG.debug("with course = " + course);
            LOG.debug("with tee = " + tee);
       ValidationsLC vlc = validate(tee, course, conn);
  //         LOG.debug("line 04 v = "+ vlc.toString());
       if(vlc.getStatus0().equals(ValidationStatus.REJECTED.toString())){
           LOG.error(vlc.getStatus1());
           LCUtil.showMessageFatal(vlc.getStatus1());
           return false;
       }else{
            LCUtil.showMessageInfo(vlc.getStatus1());
       }
       
  
    final String query = LCUtil.generateInsertQuery(conn, "tee");
    ps = conn.prepareStatement(query);
            // insérer dans l'ordre de la database : 1 = first db field
    ps.setNull(1, java.sql.Types.INTEGER);
    ps.setString(2, tee.getTeeGender());
    ps.setString(3, tee.getTeeStart());
    ps.setInt(4, tee.getTeeSlope());
    ps.setBigDecimal(5, tee.getTeeRating());
    ps.setInt(6, tee.getTeeClubHandicap());   // quelle est son utilité ??
    ps.setString(7, tee.getTeeHolesPlayed());
    ps.setShort(8,tee.getTeePar());
    ps.setInt(9, 9999);  // MASTER TEE ! mod 15-08-2020 provisoire, sera modifié plus loin !
    ps.setInt(10, 9999); // Distance TEE ! mod 12-08-2023 provisoire, sera updated plus loin !
  //  ps.setInt(9, tee.getTeeMasterTee());// mod 23-08-2023    
  //  ps.setInt(10,tee.getTeeDistanceTee());
    ps.setInt(11, course.getIdcourse());
    ps.setTimestamp(12, Timestamp.from(Instant.now()));
    utils.LCUtil.logps(ps);
    int row = ps.executeUpdate(); // write into database
    if(row != 0) { // create OK
         tee.setIdtee(LCUtil.generatedKey(conn));
              LOG.debug("Tee created = {}" ,tee);
         String msg = "Tee created = " + tee
         //     + " <br/> </h1> name club = " + club.getClubName()
              + " <br/> name course = " + course.getCourseName();
          //    + " <br/> gender = " + tee.getTeeGender()
          //    + " <br/> Start = " + tee.getTeeStart()
          //    + " <br/> Rating = " + tee.getTeeRating()
           //   + " <br/> HolesPlayed = " + tee.getTeeHolesPlayed() ;
                LOG.debug(msg);
                msg =  LCUtil.prepareMessageBean("tee.created", "<br/>tee= " + tee + "<br/>course = " + course);
                showMessageInfo(msg);
    //      // fill in Master Tee à enlever creation suffit si validation préalable : le flow est bcp plus fluide !!
        Tee t = completeMasterTeeAndDistanceTee(tee, course,conn); 
        if(new update.UpdateTee().update(t, conn)){
              msg = "Tee updated with MasterTee = " + tee.getTeeMasterTee() + ", DistanceTee = " + tee.getTeeDistanceTee();
              LOG.debug("msg");
              showMessageInfo(msg);
              return true;
         }else{
           LOG.debug("we have ERROR ! = creation continue ... " + t.isNotFound());
              return false;
         }   
    //       LOG.debug("starting completeDistanceTee");
    //   completeDistanceTee(tee, course,conn);
     //     LOG.debug("completeDistanceTee done ");
      }else{  // create KO
//                LOG.debug("line 02");
                String msg = "<br/><br/>NOT NOT Succesful insert for tee = " + tee
                 //       + " <br/> name club = " + club.getClubName()
                        + " <br/> name course = " + course.getCourseName();
                   //     + " <br/> gender = " + tee.getTeeGender()
                   //     + " <br/> Start = " + tee.getTeeStart()
                    //    + " <br/> Slope = " + tee.getTeeSlope()
                    //    + " <br/> Rating = " + tee.getTeeRating();
                LOG.debug(msg);
                showMessageInfo(msg);
                return false;
       }
 }catch(SQLException sqle) {
            String msg = "£££ SQLexception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
 }catch(Exception nfe) {
           String msg = "£££ Exception in " + methodName + nfe.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return false;
        } finally {
          //  DBConnection.closeQuietly(conn, null, null, ps); // new 10/12/2011
            DBConnection.closeQuietly(null, null, null, ps); // new 14/08/2014
        }
// return false;
    } //end createTee


public static Tee completeMasterTeeAndDistanceTee (Tee tee, Course course, Connection conn) throws SQLException{
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    try{
            
        if(tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                String msg = "-- this tee is his own Master Tee and Distance Tee !!";
                LOG.info(msg);
                showMessageInfo(msg);
                tee.setTeeMasterTee(tee.getIdtee());
                tee.setTeeDistanceTee(tee.getIdtee());
                return tee;
        }
        if(!tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                LOG.debug("this tee is a distance Tee !!");
                int i = new find.FindMasterTee().find(conn, course);
                if(i == 0){
                    String msg = "-- Fatal error : Master tee not found !! first create a tee with 'YELLOW' and 'M' and '01-18'";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    tee.setNotFound(true);
                }else{
                    String msg = "-- Don't forget to create the distances button Modification Trous DistanceTee !!";
                    LOG.info(msg);
                    showMessageInfo(msg);
                    tee.setTeeMasterTee(i);
                    tee.setTeeDistanceTee(tee.getIdtee());
                  //  return tee;
                }
              //  return tee;
        }else{
                LOG.debug("this tee is a residual value Tee !!");
                int i = new find.FindMasterTee().find(conn, course);
                if(i == 0){
                    String msg = "-- Fatal error : Master tee not found !! first create a Master tee with 'YELLOW' and 'M' and '01-18'";
                    LOG.error(msg);
                    showMessageFatal(msg);
                    tee.setNotFound(true);
              //      return tee;
                }else{
                    tee.setTeeMasterTee(i);
                    tee.setTeeDistanceTee(tee.getIdtee());
                }
                i = new find.FindDistanceTee().find(course, tee, conn);
                if(i == 0){   //error not found
                   String msg = "-- Fatal error : Distance tee not found !! first create a Distance tee with 'YELLOW' and 'M' and '01-18'";
                   LOG.error(msg);
                   showMessageFatal(msg);
                   tee.setNotFound(true);
                }else{
                   tee.setTeeDistanceTee(i);
                }
        } // end if residual   
  return tee;
  
 }catch(SQLException sqle) {
            String msg = "£££ SQLexception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
 }catch(Exception nfe) {
           String msg = "£££ Exception in " + methodName + nfe.getMessage();
            LOG.error(msg);
            showMessageFatal(msg);
            return null;
        } finally {
        }
}// end method

  public static ValidationsLC validate(final Tee tee, Course course, Connection conn) throws SQLException{
   try{
       LOG.debug("entering validate before createTee");
     ValidationsLC vlc = new ValidationsLC();
     vlc.setStatus0(ValidationsLC.ValidationStatus.APPROVED.toString());
     vlc.setStatus1("");
     vlc.setStatus2("00");

        if(tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                LOG.debug("this tee is a MasterTee !!");
    //            int i = new find.FindMasterTee().find(conn, course);
    //            if(i == 0){
    //                vlc.setStatus0(ValidationsLC.ValidationStatus.REJECTED.toString());
                  //  String msgerr =  ;//LCUtil.prepareMessageBean("mastertee.notfound");
                    vlc.setStatus1( "this tee is a MasterTee");
                    return vlc;
         }
       int i = 0;
       if(!tee.getTeeStart().equals("YELLOW") && tee.getTeeGender().equals("M") && tee.getTeeHolesPlayed().equals("01-18")){
                LOG.debug("we try to create a distance Tee !!");
                i = new find.FindMasterTee().find(conn, course);
                if(i == 0){
                    vlc.setStatus0(ValidationStatus.REJECTED.toString());
                    String[] array = new String[2];
                    array[0] = " for distanceTee";
                    array[1] = tee.toString();
                    String msgerr =  LCUtil.prepareMessageBean("mastertee.notfound",array);
                 //   msgerr = msgerr + " for distanceTee";
                    vlc.setStatus1(msgerr);
                    return vlc;
                }

      //          }else{
      //              String msg = "-- Don't forget to create the distances button Modification Trous DistanceTee !!";
      //              LOG.info(msg);
       //             LCUtil.showMessageInfo(msg);
       //             tee.setTeeMasterTee(i);
       //             tee.setTeeDistanceTee(tee.getIdtee());
                  //  return tee;
       //         }
              //  return tee;
        }else{
                LOG.debug("we try to create a residual value Tee !!");
                i = new find.FindMasterTee().find(conn, course);
                if(i == 0){
                    vlc.setStatus0(ValidationStatus.REJECTED.toString());
                    String msgerr =  LCUtil.prepareMessageBean("mastertee.notfound", " for a residualtee");
                    vlc.setStatus1(msgerr);
                    return vlc;
                }
              //  }
           //         String msg = "-- Fatal error : Master tee not found !! first create a Master tee with 'YELLOW' and 'M' and '01-18'";
           //         LOG.error(msg);
           //         LCUtil.showMessageFatal(msg);
           //         tee.setNotFound(true);
              //      return tee;
        //        }else{
        //            tee.setTeeMasterTee(i);
        //            tee.setTeeDistanceTee(tee.getIdtee());
         //       }
        //        }
                i = new find.FindDistanceTee().find(course, tee, conn);
                if(i == 0){ 
                   vlc.setStatus0(ValidationStatus.REJECTED.toString());
                    String msgerr =  LCUtil.prepareMessageBean("distancetee.notfound", " for a residual tee with color = " + tee.getTeeStart());
                    vlc.setStatus1(msgerr);
                    return vlc;
                }   
             //       String msg = "-- Fatal error : Distance tee not found !! first create a Distance tee with 'YELLOW' and 'M' and '01-18'";
             //      LOG.error(msg);
             //      LCUtil.showMessageFatal(msg);
             //      tee.setNotFound(true);
             //   }else{
             //      tee.setTeeDistanceTee(i);
              //  }
             //   }
        } // end if residual   
     return vlc; // is approved
        } catch (Exception e) {
            String msg = "Â£Â£ Exception in validate of CreateTee " + e.getMessage();
            LOG.error(msg);
            return null;
   }finally{   }
  } //end validate


  void main() throws SQLException, Exception{ //enlevé static
      Connection conn = new DBConnection().getConnection();
  try{
      LOG.debug("entering main");
   Tee tee = new Tee();
   tee.setTeeStart("YELLOW");
   tee.setTeeGender("M");
   tee.setTeeHolesPlayed("01-18");
   tee.setTeeSlope((short)131);
   tee.setTeeRating(BigDecimal.valueOf(72.9));
   tee.setTeePar((short)72);
   Course course = new Course();
   course.setIdcourse(167);
   
    boolean cr = new CreateTee().create(course, tee, conn);
        LOG.debug("from main, after lp = " + cr);
   
   
 //  var v = completeMasterTeeAndDistanceTee(tee, course, conn);   // static  
  //     LOG.debug("from main, after lp = " + v);
 //      if(v.isNotFound()){
  //         LOG.debug("we have an error ! = creation rejected " + v.isNotFound());
  //     }else{
  //         LOG.debug("we have NO error ! = creation done ! " + v.isNotFound());
  
  /*    
      
      
   Club club = new Club();
   club.setIdclub(101);
   club = new read.ReadClub().read(club, conn);

   Course course = new Course();
   course.setIdcourse(1);
   course = new read.ReadCourse().load(course,conn);
  
   Tee tee = new Tee();
   tee.setTeeStart("WHITE");
   tee.setTeeSlope((short)131);
   tee.setTeeRating(BigDecimal.valueOf(72.9));
   tee.setTeeGender("M");
   tee.setTeeHolesPlayed("01-18");
   tee.setTeePar((short)72);

    boolean cr = new CreateTee().create(club, course, tee, conn);
        LOG.debug("from main, after lp = " + cr);
        
    */    
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
} //end class