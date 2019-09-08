package lists;

import entite.Course;
import entite.Hole;
import entite.Tee;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

public class HoleList {
    private static List<Hole> liste = null;
    
public List<Hole> list(final Course course, final Tee tee,final Connection conn) throws Exception{
if(liste == null){
        PreparedStatement ps = null;
        ResultSet rs = null;
 try{
        liste = new ArrayList<>();
        String h = utils.DBMeta.listMetaColumnsLoad(conn, "hole");  // fields list, comma separated
        final String query =
        "SELECT " + h +
        " FROM hole, tee" +
        " WHERE hole.tee_course_idcourse = ?" +
        "	AND tee.idtee = ?" +
        "	LIMIT ? , ?" //pos 1 = 0 ou 9, position 2 = 9 ou 18
        ;
             ps = conn.prepareStatement(query);
     ps.setInt(1, course.getIdcourse());
     ps.setInt(2, tee.getIdtee());
 
     int start = 0;
     int trous = 9;
 //    if(tee.getTeeHolesPlayed().equals("01-09")){ // default
 //        start = 0; trous = 9;
 //    }
     if(tee.getTeeHolesPlayed().equals("10-18")){
         start = 9;// trous = 9;
     }
     if(tee.getTeeHolesPlayed().equals("01-18")){
       //  start = 0;
         trous = 18;
     }
     ps.setInt(3, start); // 0 pour 01-09, 9 pour 10-18
     ps.setInt(4, trous); // 0 pour 01-09, 18 pour 10-18
             utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     while (rs.next()){
         liste.add(entite.Hole.mapHole(rs));
     }
  liste.forEach(item -> LOG.info("Hole list " + item));  // java 8 lambda
return liste;

} catch(SQLException sqle){
    String msg = "£££ SQL exception in ListAllPlayers = " + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}catch(Exception e){
    String msg = "£££ Exception in ListAllPlayers = " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
           DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}else{
  ////   LOG.debug("escaped to listallplayers repetition thanks to lazy loading");
    return liste;  //plusieurs fois ??
   //then you should introduce lazy loading inside the getter method. I.e. if the property is null,
    //then load and assign it to the property, else return it.
} //end if

} //end method
    

    public static List<Hole> getListe() {
        return liste;
    }

    public static void setListe(List<Hole> liste) {
        HoleList.liste = liste;
    }
    
  public static void main(String[] args) throws SQLException, Exception {// testing purposes
    Connection conn = new DBConnection().getConnection();
   Course course = new Course(); 
   course.setIdcourse(135);
   Tee tee = new Tee();
   tee.setIdtee(148);
   tee.setTeeHolesPlayed("01-18"); // ou 10-18   ou 01-09
    List<Hole> p1 = new HoleList().list(course, tee, conn);
        LOG.info("Hole list = " + p1.toString());
    DBConnection.closeQuietly(conn, null, null, null);
}// end main

} //end Class