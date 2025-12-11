package migration;

import entite.Course;
import entite.Distance;
import entite.Tee;
import find.FindDistances;
import static interfaces.Log.LOG;
import static interfaces.Log.NEW_LINE;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import utils.DBConnection;
import utils.LCUtil;

// 13-08-2023
// complete column TeeDistanceTee
// execution ONE SHOT une seule fois pour la migration!!

public class DistanceTeeMigration2 {
    private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
    
public void list(final Connection conn) throws Exception{
     final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
       LOG.debug("entering " + methodName);
        PreparedStatement ps = null;
        ResultSet rs = null;
 /* 
        la table migration a été créée par la requête manuelle
        CREATE TABLE migration
	SELECT idtee, TeeDistanceTee, distanceIdTee, distancearray
	FROM tee
	LEFT JOIN distances
	ON tee.TeeDistanceTee = distances.DistanceIdTee
	WHERE distances.DistanceIdTee IS NULL;
   */     
 
 
 
 // but compléter la table distance des données récupérées dans hole.HoleDistance du master TEE
 try{
    final String query = """
        SELECT *
        FROM migration
      """;
     ps = conn.prepareStatement(query);
 //    ps.setInt(1, course.getIdcourse());
     utils.LCUtil.logps(ps);
     rs = ps.executeQuery();
     int i = 0;
     int distanceTee = 0;
     int idtee = 0;
     String teeStart = "";
     String holesPlayed = "";
     while(rs.next()){
         i++;
       //     LOG.debug("tee = " + tee);
         idtee = rs.getInt("idtee");
         distanceTee = rs.getInt("TeeDistanceTee");
         teeStart = rs.getString("TeeStart");
         holesPlayed = rs.getString("TeeHolesPlayed");
           LOG.debug(NEW_LINE + "no distanceArray found for DistanceTee = " + distanceTee);
         // créer line dans table 'distances'
         Tee tee = new Tee();
         tee.setIdtee(idtee);
         tee.setTeeHolesPlayed(holesPlayed);
         tee.setTeeStart(teeStart);
         tee.setTeeDistanceTee(distanceTee);
        int [] v = new migration.LoadHolesMigration().load(tee, conn); //LoadHolesMigration on va les chercher dans la table hole, field HoleDistance
          LOG.debug("array extracted from table holes = " + Arrays.toString(v));
        Distance distance = new Distance();
        distance.setIdTee(idtee);
        distance.setDistanceArray(v);
        if (i < 200){ // max 161
          //  Distance d = new find.FindDistances().find(tee, conn);
          //  LOG.debug("distance d = " + d);
            boolean b = new create.CreateDistances().create(distance, conn);
        }
      } // end while
       LOG.debug("ResultSet " + methodName + " has " + i + " lines.");
} catch(SQLException sqle){
    String msg = "£££ SQL exception in " + methodName + "/" + sqle.getMessage() + " ,SQLState = " +
            sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}catch(Exception e){
    String msg = "£££ Exception in " + methodName + " / " + e.getMessage();
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
}finally{
    DBConnection.closeQuietly(null, null, rs, ps);
}
} //end method


// LOG.debug("input dataHoles for distance = " + Arrays.deepToString(holesGlobal.getDataHoles()));
   //          var v = utils.LCUtil.extractFrom2D(holesGlobal.getDataHoles(),3); //// 18 trous, 4 données : number, par, strokeindex, distance

/*
    public static void completeDistanceTee (Tee tee, Connection conn){
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME);
    try{
     //   LOG.debug("entering completeDistanceTee");
        Course course = new Course();
        course.setIdcourse(tee.getCourse_idcourse());
        tee.setTeeDistanceTee(new find.FindDistanceTee().find(course, tee, conn));
           LOG.debug("DistanceTee found = " + tee.getTeeDistanceTee() + NEW_LINE);
            if(tee.getTeeDistanceTee() == 0){   //error not found
                  String msg = "-- Fatal error : Distance tee not found !! first create a tee with 'YELLOW' and 'M' and '01-18'";
                  LOG.error(msg);
                  LCUtil.showMessageFatal(msg);
                  throw new Exception(msg);
             }
            if(new update.UpdateTee().update(tee, conn)){
                String msg = "DistanceTee inserted/modified = " + tee;
                LOG.debug("msg");
      //          LCUtil.showMessageInfo(msg);
            }

 }catch(SQLException sqle) {
            String msg = "£££ SQLexception in " + methodName + sqle.getMessage() + " ,SQLState = "
                    + sqle.getSQLState() + " ,ErrorCode = " + sqle.getErrorCode();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
 }catch(Exception e) {
           String msg = "£££ Exception in " + methodName + e.getMessage();
            LOG.error(msg);
            LCUtil.showMessageFatal(msg);
        } finally {
        }
}// end method
  */  
  void main() throws SQLException, Exception {
    Connection conn = new utils.DBConnection().getConnection();
    LOG.debug("entering main");
    new DistanceTeeMigration2().list(conn);  // method is static
        LOG.debug("main - tee list completed ! ") ; //+ tees.size());
   // tees.forEach(item -> LOG.debug("Tee list migration " + item));
    utils.DBConnection.closeQuietly(conn, null, null, null);
}// end main
} //end Class