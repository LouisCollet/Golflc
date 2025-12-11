package find;

import entite.Course;
import entite.Player;
import entite.Round;
import static interfaces.Log.LOG;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import utils.LCUtil;

// liste des tee d'un course //
public class FindTeeStart{
   private static List<String> liste = null;
   private final static String CLASSNAME = utils.LCUtil.getCurrentClassName();
   
   public List<String> find(final Course course , final Player player, final Round round, final Connection conn) throws SQLException{ 
if(liste == null){
       LOG.debug("starting FindTeeStart for course = " + course);
       LOG.debug("starting FindTeeStart for player = " + player);
       LOG.debug("starting FindTeeStart for round = " + round);
    PreparedStatement ps = null;
    ResultSet rs = null;
    final String methodName = utils.LCUtil.getCurrentMethodName(CLASSNAME); 
try{   
    final String query = """
             SELECT *
             FROM course, tee
             WHERE course.idcourse = ?
                 AND tee.TeeGender = ?
                 AND tee.course_idcourse = course.idcourse
                 AND SUBSTR(TeeHolesPlayed, 1, 2) = ?
                 AND SUBSTR(TeeHolesPlayed, 4, 2) = ?
          """;
//
// format 01-18 01-09 10-18   

    ps = conn.prepareStatement(query);
    ps.setInt(1, course.getIdcourse()); 
    ps.setString(2, player.getPlayerGender());
  // new 12-10-2021 pour compatibilité avec mySQL '1' n'est pas '01'
    DecimalFormat df = new DecimalFormat("00");
    ps.setString(3, df.format(round.getRoundStart()));
    ps.setString(4, df.format(round.getRoundHoles()+ round.getRoundStart() - 1));
    utils.LCUtil.logps(ps); 
    rs =  ps.executeQuery();
    
    liste = new ArrayList<>();
	while(rs.next()){
	    liste.add( //fillColor(rs.getString("TeeStart"))
                   //       rs.getString("TeeStart") // YELLOW, BLUE, etc.
                       //  "<span style='color:red;background:green'>"
                          rs.getString("TeeStart")
                      //   + "</span>"
                        + " / " + rs.getString("TeeGender")
                        + " / " + rs.getString("TeeHolesPlayed")
                        + " / " + rs.getInt("idtee"));  
	}
     if(liste.isEmpty()){
         String msg = "££ Empty Result for " + methodName;
         liste.add("No TeeStart found for gender : " + player.getPlayerGender());
         LOG.error(msg);
  //       LCUtil.showMessageFatal(msg);
     }else{
         LOG.debug("ResultSet " + methodName + " has " + liste.size() + " lines.");
     }   
      liste.forEach(item -> LOG.debug("TeeStart list " + item));  // java 8 lambda
    return liste;

}catch(SQLException e){
        String msg = "SQL Exception in FindTeeStart() = " + e.toString() + ", SQLState = " + e.getSQLState()
            + ", ErrorCode = " + e.getErrorCode();
	LOG.error(msg);
        LCUtil.showMessageFatal(msg);
        return null;
}catch (Exception ex){
    String msg = "Exception in FindTeeStart() " + ex;
    LOG.error(msg);
    LCUtil.showMessageFatal(msg);
    return null;
}finally{
        DBConnection.closeQuietly(null, null, rs, ps); // new 14/08/2014
}
}
else{
///       LOG.debug("escaped to FindSubscription repetition with lazy loading");
    return liste;  //plusieurs fois ??
  // }
} //end method
}
    public static List<String> getListe() {
        return liste;
    }

    public static void setListe(List<String> liste) {
        FindTeeStart.liste = liste;
    }
    
 public static void main (String[] args) throws Exception {
    Connection conn = new DBConnection().getConnection();
  try{
        Course course = new Course();
        course.setIdcourse(99); // empereur
        Player player = new Player();
        player.setPlayerGender("M");
        Round round = new Round();
        round.setIdround(748);
        round = new read.ReadRound().read(round, conn);
 //     Short.valueOf("1");
    //    round.setRoundStart((short)1);
    //    round.setRoundHoles((short)9);
        List<String> b = new FindTeeStart().find(course, player,round, conn);
        b.forEach(item -> LOG.debug("TeeStart list " + item));  // java 8 lambda
        LOG.debug("from main, after = " + b);
 } catch (Exception e) {
            String msg = "Â£Â£ Exception in main = " + e.getMessage();
            LOG.error(msg);
      //      LCUtil.showMessageFatal(msg);
   }finally{
         DBConnection.closeQuietly(conn, null, null , null); 
   }
   } // end main
 /*   
    private static String fillColor(String c) {
        if (c.equals("YELLOW")){
            return "<style='color:red'/>";
        }
        return "";
    }
    */
}  // end class